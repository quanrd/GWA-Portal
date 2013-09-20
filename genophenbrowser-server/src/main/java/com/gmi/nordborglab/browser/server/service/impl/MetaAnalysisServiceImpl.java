package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.annotation.SNPAnnot;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.data.es.ESTermsFacet;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.meta.MetaAnalysisTopResultsCriteria;
import com.gmi.nordborglab.browser.server.domain.meta.MetaSNPAnalysis;
import com.gmi.nordborglab.browser.server.domain.pages.MetaSNPAnalysisPage;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.gmi.nordborglab.browser.server.service.MetaAnalysisService;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.lucene.queryparser.xml.builders.RangeFilterBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.facet.range.RangeFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 03.06.13
 * Time: 19:12
 * To change this template use File | Settings | File Templates.
 */


@Service
@Transactional(readOnly = true)
public class MetaAnalysisServiceImpl implements MetaAnalysisService {

    @Resource
    protected Client client;

    @Resource
    protected StudyRepository studyRepository;

    @Resource
    protected AnnotationDataService annotationDataService;

    @Resource
    protected EsAclManager esAclManager;

    @Override
    public List<MetaSNPAnalysis> findAllAnalysisForRegion(int start, int end, String chr) {
        List<MetaSNPAnalysis> metaSNPAnalysises = Lists.newArrayList();
        // GET all studyids
        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());
        FilterBuilder filter = FilterBuilders.boolFilter().must(
                FilterBuilders.hasChildFilter("meta_analysis_snps", FilterBuilders.boolFilter().
                        must(
                                FilterBuilders.numericRangeFilter("position").from(start).to(end),
                                FilterBuilders.termFilter("chr", chr)
                        )
                ),
                esAclManager.getAclFilter(Lists.newArrayList("read")));
        builder.addFields("_id", "_parent").setTypes("study").setQuery(QueryBuilders.constantScoreQuery(filter));
        SearchResponse response = builder.execute().actionGet();
        List<Long> ids = Lists.newArrayList();
        for (SearchHit hit : response.getHits()) {
            ids.add(Long.parseLong(hit.getId()));
        }
        if (ids.size() == 0) {
            return metaSNPAnalysises;
        }
        Iterable<Study> studies = studyRepository.findAll(ids);
        Map<Long, Study> studyCache = Maps.uniqueIndex(studies, new Function<Study, Long>() {
            @Nullable
            @Override
            public Long apply(@Nullable Study study) {
                return study.getId();
            }
        });
        // Check permission
        /*for (String id : ids) {

        } */


        // GET  all SNPs
        builder = client.prepareSearch(esAclManager.getIndex());
        filter = FilterBuilders.
                boolFilter().
                must(
                        FilterBuilders.hasParentFilter("study", FilterBuilders.idsFilter().ids(Lists.transform(ids, new Function<Long, String>() {
                            @Nullable
                            @Override
                            public String apply(@Nullable Long aLong) {
                                return String.valueOf(aLong);
                            }
                        }).toArray(new String[]{})))
                        , FilterBuilders.numericRangeFilter("position").from(start).to(end),
                        FilterBuilders.termFilter("chr", chr));
        builder.addSort("score", SortOrder.DESC).addFields("position", "mac", "maf", "_parent", "score", "overFDR", "studyid", "gene", "annotation", "inGene").setTypes("meta_analysis_snps").setQuery(QueryBuilders.constantScoreQuery(filter));
        response = builder.execute().actionGet();
        for (SearchHit searchHit : response.getHits()) {
            try {
                Map<String, SearchHitField> fields = searchHit.getFields();
                Long studyId = null;
                if (fields.containsKey("studyid")) {
                    studyId = (long) (Integer) fields.get("studyid").getValue();
                } else {
                    studyId = Long.valueOf((String) fields.get("_parent").getValue());
                }
                Study study = studyCache.get(studyId);
                SNPAnnot annot = new SNPAnnot();
                annot.setPosition((Integer) fields.get("position").getValue());
                annot.setChr(chr);
                if (fields.containsKey("annotation")) {
                    annot.setAnnotation((String) fields.get("annotation").getValue());
                    annot.setInGene((Boolean) fields.get("inGene").getValue());
                }
                MetaSNPAnalysis.Builder metaAnalysisBuilder = new MetaSNPAnalysis.Builder()
                        .setAnalysisId(studyId)
                        .setSnpAnnotation(annot)
                        .setpValue((Double) fields.get("score").getValue())
                        .setAnalysis(study.getName())
                        .setPhenotype(study.getPhenotype().getLocalTraitName())
                        .setStudy(study.getPhenotype().getExperiment().getName())
                        .setMethod(study.getProtocol().getAnalysisMethod())
                        .setGenotype(study.getAlleleAssay().getName())
                        .setOverFDR((Boolean) fields.get("overFDR").getValue())
                        .setPhenotypeId(study.getPhenotype().getId())
                        .setStudyId(study.getPhenotype().getExperiment().getId())
                        .setMac((Integer) fields.get("mac").getValue())
                        .setMaf((Double) fields.get("maf").getValue());

                metaSNPAnalysises.add(metaAnalysisBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return metaSNPAnalysises;
    }


    @Override
    public List<ESFacet> findMetaStats(MetaAnalysisTopResultsCriteria criteria) {
        List<ESFacet> facets = Lists.newArrayList();
        //TODO acl filter

        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());
        builder.addFacet(FacetBuilders.termsFacet("chr").field("chr").size(5).order(TermsFacet.ComparatorType.TERM))
                .addFacet(FacetBuilders.rangeFacet("maf").field("maf").field("maf")
                        .addUnboundedFrom(0.01)
                        .addRange(0.01, 0.05)
                        .addRange(0.05, 0.1)
                        .addUnboundedTo(0.1))
                .addFacet(FacetBuilders.termsFacet("inGene").field("inGene").size(2))
                        //.addFacet(FacetBuilders.termsFacet("overFDR").field("overFDR").size(2))
                .addFacet(FacetBuilders.termsFacet("annotation").field("annotation").size(5));
        FilterBuilder filter = getFilterFromCriteria(criteria);
        if (filter == null) {
            builder.setQuery(QueryBuilders.matchAllQuery());
        } else {
            builder.setQuery(QueryBuilders.constantScoreQuery(filter));
        }
        builder.setSize(0);
        SearchResponse response = builder.execute().actionGet();
        Facets searchFacets = response.getFacets();


        // get maf facet
        RangeFacet mafFacet = (RangeFacet) searchFacets.facetsAsMap().get("maf");
        List<ESTermsFacet> terms = Lists.newArrayList();
        for (RangeFacet.Entry rangeEntry : mafFacet) {
            String range = "";
            if (rangeEntry.getFrom() == Double.NEGATIVE_INFINITY) {
                range = "< " + rangeEntry.getTo() * 100 + "%";
            } else if (rangeEntry.getTo() == Double.POSITIVE_INFINITY) {
                range = "> " + rangeEntry.getFrom() * 100 + "%";
            } else {
                range = String.format("%s - %s", rangeEntry.getFrom() * 100, rangeEntry.getTo() * 100 + "%");
            }
            terms.add(new ESTermsFacet(range, rangeEntry.getCount()));
        }
        facets.add(new ESFacet("maf", 0, 0, 0, terms));

        // get chr facet
        TermsFacet searchFacet = (TermsFacet) searchFacets.facetsAsMap().get("chr");
        terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            terms.add(new ESTermsFacet(String.format("Chr%s", termEntry.getTerm().string()), termEntry.getCount()));
        }
        facets.add(new ESFacet("chr", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));

        // get inGene facet
        searchFacet = (TermsFacet) searchFacets.facetsAsMap().get("inGene");
        terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            String term = "intergenic";
            if (termEntry.getTerm().string().equalsIgnoreCase("T")) {
                term = "genic";
            }
            terms.add(new ESTermsFacet(term, termEntry.getCount()));
        }
        facets.add(new ESFacet("inGene", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));
        // get overFDR
       /* searchFacet = (TermsFacet) searchFacets.facetsAsMap().get("overFDR");
        terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            String term = "non-significant";
            if (termEntry.getTerm().string().equalsIgnoreCase("T")) {
                term = "significant";
            }
            terms.add(new ESTermsFacet(term, termEntry.getCount()));
        }
        facets.add(new ESFacet("overFDR", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));*/

        // get annotation
        searchFacet = (TermsFacet) searchFacets.facetsAsMap().get("annotation");
        terms = Lists.newArrayList();
        for (TermsFacet.Entry termEntry : searchFacet) {
            terms.add(new ESTermsFacet(termEntry.getTerm().string(), termEntry.getCount()));
        }
        facets.add(new ESFacet("annotation", searchFacet.getMissingCount(), searchFacet.getTotalCount(), searchFacet.getOtherCount(), terms));

        return facets;
    }

    private FilterBuilder getFilterFromCriteria(MetaAnalysisTopResultsCriteria criteria) {
        AndFilterBuilder filter = FilterBuilders.andFilter();
        BoolFilterBuilder boolFilter = FilterBuilders.boolFilter();
        boolFilter.must(FilterBuilders.hasParentFilter("study", esAclManager.getAclFilter(Lists.newArrayList("read"))));
        filter.add(boolFilter);
        if (criteria != null) {
            if (criteria.getChr() != null) {
                boolFilter.must(FilterBuilders.termFilter("chr", criteria.getChr()));
            }
            if (criteria.getAnnotation() != null) {
                boolFilter.must(FilterBuilders.termFilter("annotation", criteria.getAnnotation()));
            }
            if (criteria.isOverFDR() != null) {
                boolFilter.must(FilterBuilders.termFilter("overFDR", criteria.isOverFDR()));
            }
            if (criteria.isInGene() != null) {
                boolFilter.must(FilterBuilders.termFilter("inGene", criteria.isInGene()));
            }
            if (criteria.getMafFrom() != null || criteria.getMafTo() != null) {
                // use numeric because there is already a facet on it: http://elasticsearch-users.115913.n3.nabble.com/Just-Pushed-Numeric-Range-Filter-td1715331.html
                NumericRangeFilterBuilder mafFilter = FilterBuilders.numericRangeFilter("maf");
                if (criteria.getMafFrom() != null) {
                    mafFilter.gte(criteria.getMafFrom());
                }
                if (criteria.getMafTo() != null) {
                    mafFilter.lte(criteria.getMafTo());
                }
                // use and filter to combine bool and numeric_range because of performance
                //https://groups.google.com/forum/#!msg/elasticsearch/PS12RcyNSWc/I1PX1r0RfFcJ
                filter.add(mafFilter);
            }
        }

        return filter;

    }

    @Override
    public MetaSNPAnalysisPage findTopAnalysis(MetaAnalysisTopResultsCriteria criteria, int start, int size) {
        List<MetaSNPAnalysis> metaSNPAnalysises = Lists.newArrayList();
        SearchRequestBuilder builder = client.prepareSearch(esAclManager.getIndex());
        /*FilterBuilder filter = FilterBuilders.
                boolFilter().
                must(FilterBuilders.termFilter("chr", chr));
*/
        FilterBuilder filter = getFilterFromCriteria(criteria);
        if (filter == null) {
            builder.setQuery(QueryBuilders.matchAllQuery());
        } else {
            builder.setQuery(QueryBuilders.constantScoreQuery(filter));
        }
        builder.setSize(size).setFrom(start).addSort("score", SortOrder.DESC).addFields("position", "mac", "maf", "chr", "score", "overFDR", "studyid", "_parent", "gene", "annotation", "inGene", "_parent").setTypes("meta_analysis_snps");
        SearchResponse response = builder.execute().actionGet();

        for (SearchHit searchHit : response.getHits()) {
            try {
                Map<String, SearchHitField> fields = searchHit.getFields();
                Long studyId = null;
                if (fields.containsKey("studyid")) {
                    studyId = (long) (Integer) fields.get("studyid").getValue();
                } else {
                    studyId = Long.parseLong((String) fields.get("_parent").getValue());
                }
                Study study = studyRepository.findOne(studyId);
                SNPAnnot annot = new SNPAnnot();
                annot.setPosition((Integer) fields.get("position").getValue());
                annot.setChr((String) fields.get("chr").getValue());
                if (fields.containsKey("annotation")) {
                    annot.setAnnotation((String) fields.get("annotation").getValue());
                    annot.setInGene((Boolean) fields.get("inGene").getValue());
                }
                if (fields.containsKey("gene")) {
                    Map<String, Object> gene = (Map<String, Object>) ((List<Map<String, Object>>) fields.get("gene").getValues().get(0)).get(0);
                    annot.setGene((String) gene.get("name"));

                }
                MetaSNPAnalysis.Builder metaAnalysisBuilder = new MetaSNPAnalysis.Builder()
                        .setAnalysisId(studyId)
                        .setSnpAnnotation(annot)
                        .setpValue((Double) fields.get("score").getValue())
                        .setAnalysis(study.getName())
                        .setPhenotype(study.getPhenotype().getLocalTraitName())
                        .setStudy(study.getPhenotype().getExperiment().getName())
                        .setMethod(study.getProtocol().getAnalysisMethod())
                        .setGenotype(study.getAlleleAssay().getName())
                        .setOverFDR((Boolean) fields.get("overFDR").getValue())
                        .setPhenotypeId(study.getPhenotype().getId())
                        .setStudyId(study.getPhenotype().getExperiment().getId())
                        .setMac((Integer) fields.get("mac").getValue())
                        .setMaf((Double) fields.get("maf").getValue());

                metaSNPAnalysises.add(metaAnalysisBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new MetaSNPAnalysisPage(metaSNPAnalysises, new PageRequest(start / size, size), response.getHits().getTotalHits());
    }
}
