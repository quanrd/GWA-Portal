package com.gmi.nordborglab.browser.client.util;

import com.gmi.nordborglab.browser.client.dto.SNPAllele;
import com.gmi.nordborglab.browser.shared.proxy.FacetProxy;
import com.gmi.nordborglab.browser.shared.proxy.FacetTermProxy;
import com.gmi.nordborglab.browser.shared.proxy.LocalityProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SampleDataProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitStatsProxy;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Doubles;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Random;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataColumn;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.RoleType;
import com.googlecode.gwt.charts.client.corechart.ColumnChartOptions;
import com.googlecode.gwt.charts.client.options.Animation;
import com.googlecode.gwt.charts.client.options.AnimationEasing;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataTableUtils {

    public static Ordering<SNPAllele> snpOrdering = new Ordering<SNPAllele>() {
        @Override
        public int compare(SNPAllele left, SNPAllele right) {
            Preconditions.checkNotNull(left);
            Preconditions.checkNotNull(right);
            Double value1 = 0d;
            Double value2 = 0d;
            try {
                value1 = Double.valueOf(left.getPhenotype());
            } catch (Exception e) {

            }
            try {
                value2 = Double.valueOf(right.getPhenotype());
            } catch (Exception e) {

            }

            return Doubles.compare(value1, value2);
        }
    };

    public static DataTable createDataTable(String json) {
        return DataTable.create(json);
    }

    public static DataTable createPhentoypeExplorerTable(ImmutableList<TraitProxy> traits) {
        DataTable dataTable = getDataTableForPhenotypeExplorereTable();
        dataTable.addRows(traits.size());
        int i = 0;
        String name = "";
        Long id = null;
        String accession = "";
        Double longitude = null;
        Double latitude = null;
        Double phenotype = null;
        String country = "";
        for (TraitProxy trait : traits) {
            try {
                PassportProxy passport = trait.getObsUnit().getStock()
                        .getPassport();
                id = passport.getId();
                accession = passport.getAccename();
                name = accession + " ID:" + passport.getId() + " Phenotype:"
                        + trait.getValue();
                if (trait.getValue() != null && !trait.getValue().equals(""))
                    phenotype = Double.parseDouble(trait.getValue());
                LocalityProxy locality = trait.getObsUnit().getStock()
                        .getPassport().getCollection().getLocality();
                longitude = locality.getLongitude();
                latitude = locality.getLatitude();
                country = locality.getCountry();

            } catch (Exception e) {

            }
            addRowToPhentoypeExplorerTable(dataTable, i, accession, id, latitude, longitude, phenotype, country);
            i = i + 1;
        }
        return dataTable;
    }

    private static DataTable getDataTableForPhenotypeExplorereTable() {
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.STRING, "ID Name Phenotype");
        dataTable.addColumn(ColumnType.NUMBER, "Date");
        dataTable.addColumn(ColumnType.NUMBER, "Longitude");
        dataTable.addColumn(ColumnType.NUMBER, "Latitude");
        dataTable.addColumn(ColumnType.NUMBER, "Phenotype");
        dataTable.addColumn(ColumnType.STRING, "Accession");
        dataTable.addColumn(ColumnType.STRING, "Country");
        return dataTable;
    }

    private static void addRowToPhentoypeExplorerTable(DataTable dataTable, int index, String accession, Long id, Double latitude, Double longitude, Double value, String country) {
        String name = accession + " ID:" + id + " Phenotype:"
                + value;
        dataTable.setValue(index, 0, name);
        dataTable.setValue(index, 1, 1900);
        if (longitude != null)
            dataTable.setValue(index, 2, longitude);
        if (latitude != null)
            dataTable.setValue(index, 3, latitude);
        if (value != null)
            dataTable.setValue(index, 4, value);
        dataTable.setValue(index, 5, accession);
        dataTable.setValue(index, 6, country);
    }


    public static DataTable createPhentoypeExplorerTableFromStats(ImmutableList<TraitStatsProxy> traits) {
        DataTable dataTable = getDataTableForPhenotypeExplorereTable();
        if (traits != null) {
            dataTable.addRows(traits.size());
            int i = 0;
            for (TraitStatsProxy trait : traits) {
                addRowToPhentoypeExplorerTable(dataTable, i, trait.getAccename(), trait.getPassportId(), trait.getLatitude(), trait.getLongitude(), trait.getAvgValue(), trait.getCountry());
                i = i + 1;
            }
        }
        return dataTable;
    }

    public static DataTable createPhenotypeHistogramTable(ImmutableSortedMap<Double, Integer> histogram) {
        DataTable histogramData = DataTable.create();
        NumberFormat numberFormat = NumberFormat.getDecimalFormat().overrideFractionDigits(2);
        histogramData.addColumn(ColumnType.STRING, "Bin");
        histogramData.addColumn(ColumnType.NUMBER, "Frequency");
        if (histogram != null) {
            histogramData.addRows(histogram.size() - 1);
            ImmutableList<Double> keys = histogram.keySet().asList();
            ImmutableList<Integer> values = histogram.values().asList();
            for (int i = 0; i < histogram.size() - 1; i++) {
                histogramData.setValue(i, 0, numberFormat.format(keys.get(i)) + " - " + numberFormat.format(keys.get(i + 1)));
                histogramData.setValue(i, 1, values.get(i));
            }
        } else {
            histogramData.addRows(3);
            histogramData.setValue(0, 0, "A");
            histogramData.setValue(0, 1, 5);
            histogramData.setValue(1, 0, "B");
            histogramData.setValue(1, 1, 10);
            histogramData.setValue(2, 0, "C");
            histogramData.setValue(2, 1, 7);
        }
        return histogramData;
    }

    public static DataTable createPhenotypeHistogramTable2(Map<String, Double> histogram) {
        DataTable histogramData = DataTable.create();
        NumberFormat numberFormat = NumberFormat.getDecimalFormat().overrideFractionDigits(2);
        histogramData.addColumn(ColumnType.STRING, "Accession");
        histogramData.addColumn(ColumnType.NUMBER, "Phenotype");
        histogramData.addRows(histogram.size());
        for (Map.Entry<String, Double> entry : histogram.entrySet()) {
            int i = histogramData.addRow();
            histogramData.setValue(i, 0, entry.getKey());
            histogramData.setValue(i, 1, entry.getValue());
        }
        return histogramData;
    }

    public static DataTable createPhenotypeGeoChartTable(Multiset<String> data) {
        DataTable geoChartData = DataTable.create();
        geoChartData.addColumn(ColumnType.STRING, "Country");
        geoChartData.addColumn(ColumnType.NUMBER, "Frequency");
        if (data != null) {
            for (String cty : data.elementSet()) {
                int i = geoChartData.addRow();
                geoChartData.setValue(i, 0, cty);
                geoChartData.setValue(i, 1, data.count(cty));
            }
        }
        return geoChartData;
    }

    public static DataTable createFroMFacets(FacetProxy facet) {
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.STRING, facet.getName());
        dataTable.addColumn(ColumnType.NUMBER, "count");
        int rowCount = dataTable.addRows(facet.getTerms().size());
        for (int i = 0; i <= rowCount; i++) {
            FacetTermProxy term = facet.getTerms().get(i);
            dataTable.setValue(i, 0, term.getTerm());
            dataTable.setValue(i, 1, term.getValue());
        }
        return dataTable;
    }

    public static ColumnChartOptions getDefaultPhenotypeHistogramOptions() {
        ColumnChartOptions options = ColumnChartOptions.create();
        options.setTitle("Phenotype Histogram");
        Animation animationOptions = Animation.create();
        animationOptions.setDuration(1000);
        animationOptions.setEasing(AnimationEasing.OUT);
        options.setAnimation(animationOptions);
        return options;
    }

    public static com.googlecode.gwt.charts.client.corechart.HistogramOptions getDefaultPhenotypeHistogramOptions2() {
        com.googlecode.gwt.charts.client.corechart.HistogramOptions options = com.googlecode.gwt.charts.client.corechart.HistogramOptions.create();
        options.setTitle("Phenotype Histogram");
        Animation animationOptions = Animation.create();
        animationOptions.setDuration(1000);
        animationOptions.setEasing(AnimationEasing.OUT);
        options.setAnimation(animationOptions);
        return options;
    }

    public static DataTable getDataTableForSNPAllelePhenotypeTable() {
        DataTable dataTable = getDataTableForPhenotypeExplorereTable();
        dataTable.addColumn(ColumnType.STRING, "Allele");
        return dataTable;
    }

    public static DataTable createSNPAllelePhenotypeTable(Collection<SNPAllele> snpAlleles) {
        DataTable dataTable = getDataTableForSNPAllelePhenotypeTable();
        if (snpAlleles != null) {
            dataTable.addRows(snpAlleles.size());
            int i = 0;
            for (SNPAllele snpAllele : snpAlleles) {
                LocalityProxy locality = snpAllele.getPassport().getCollection().getLocality();
                String country = "N/A";
                if (locality != null && locality.getCountry() != null)
                    country = locality.getCountry();
                Double value = null;
                try {
                    value = Double.parseDouble(snpAllele.getPhenotype());
                } catch (NumberFormatException e) {

                }
                addRowToPhentoypeExplorerTable(dataTable, i,
                        snpAllele.getPassport().getAccename(),
                        snpAllele.getPassport().getId(),
                        locality.getLatitude(),
                        locality.getLongitude(),
                        value,
                        country);
                dataTable.setValue(i, 7, snpAllele.getAllele());
                i = i + 1;
            }
        }
        return dataTable;
    }

    public static DataTable createSampleDataTable(Collection<SampleDataProxy> data, int index) {
        DataTable dataTable = getDataTableForPhenotypeExplorereTable();
        if (data != null) {
            int i = 0;
            for (SampleDataProxy sample : data) {
                if (sample.isIdKnown() && !sample.isParseError() && (sample.getParseMask() & (1 << index + 1)) == 0) {
                    dataTable.addRow();
                    Double value = null;
                    try {
                        value = Double.parseDouble(sample.getValues().get(index));
                    } catch (NumberFormatException e) {

                    }
                    addRowToPhentoypeExplorerTable(dataTable, i,
                            sample.getAccessionName(),
                            sample.getPassportId(),
                            sample.getLatitude(),
                            sample.getLongitude(),
                            value,
                            sample.getCountry());
                    i = i + 1;
                }
            }
        }
        return dataTable;
    }

    public static ImmutableMultimap<String, SNPAllele> getAlleleToPhenotype(Collection<SNPAllele> snpAlleles) {
        return Multimaps.index(snpAlleles, new Function<SNPAllele, String>() {
            @Nullable
            @Override
            public String apply(SNPAllele snpAllele) {
                Preconditions.checkNotNull(snpAllele);
                return snpAllele.getAllele() != null ? snpAllele.getAllele() : "N/A";
            }
        });
    }

    public static DataTable createSNPAllelePhenotypeForBoxplotTable(ImmutableMultimap<String, SNPAllele> groupedByAllele, SNPInfoProxy alleleInfo) {
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.STRING, null);
        dataTable.addColumn(ColumnType.NUMBER, alleleInfo.getRef());
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, alleleInfo.getAlt());
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, null);
        dataTable.addColumn(ColumnType.NUMBER, null);
        Set<String> alleles = groupedByAllele.keySet();
        Map<String, Double[]> stats = new HashMap<>();
        NumberFormat df = NumberFormat.getDecimalFormat();
        df.overrideFractionDigits(4);
        for (String allele : alleles) {
            ImmutableList<SNPAllele> orderedSNPAlleles = snpOrdering.immutableSortedCopy(groupedByAllele.get(allele));
            Double[] stat = new Double[4];
            int size = orderedSNPAlleles.size();
            if (orderedSNPAlleles.get(0).getPhenotype() != null)
                stat[0] = Double.valueOf(orderedSNPAlleles.get(0).getPhenotype());
            if (orderedSNPAlleles.get((int) Math.round(size * 25 / 100)).getPhenotype() != null)
                stat[1] = Double.valueOf(orderedSNPAlleles.get((int) Math.round(size * 25 / 100)).getPhenotype());
            if (orderedSNPAlleles.get((int) Math.round(size * 75 / 100)).getPhenotype() != null)
                stat[2] = Double.valueOf(orderedSNPAlleles.get((int) Math.round(size * 75 / 100)).getPhenotype());
            if (orderedSNPAlleles.get(orderedSNPAlleles.size() - 1).getPhenotype() != null)
                stat[3] = Double.valueOf(orderedSNPAlleles.get(orderedSNPAlleles.size() - 1).getPhenotype());
            stats.put(allele, stat);
        }
        int i = 0;
        for (String allele : Arrays.asList(alleleInfo.getRef() != null ? alleleInfo.getRef() : "N/A", alleleInfo.getAlt() != null ? alleleInfo.getAlt() : "N/A")) {
            dataTable.addRow();
            int startix = i * 4;
            //dataTable.setValue(i,0,allele);
            Double[] value = stats.get(allele);
            if (value != null) {
                for (int j = 1; j <= 4; j++) {
                    if (value[j - 1] != null) {
                        dataTable.setValue(i, startix + j, value[j - 1]);
                    }
                }
            }
            i = i + 1;
        }
        return dataTable;
    }

    public static DataTable createSNPAllelePhenotypeForStripChartTable(Collection<SNPAllele> snpAlleles, SNPInfoProxy alleleInfo) {
        String allele1 = alleleInfo.getRef();
        String allele2 = alleleInfo.getAlt();
        DataTable dataTable = DataTable.create();
        dataTable.addColumn(ColumnType.NUMBER, "Allele");
        dataTable.addColumn(ColumnType.NUMBER, allele1);
        dataTable.addColumn(DataColumn.create(ColumnType.STRING, RoleType.TOOLTIP));
        dataTable.addColumn(ColumnType.NUMBER, allele2);
        dataTable.addColumn(DataColumn.create(ColumnType.STRING, RoleType.TOOLTIP));
        NumberFormat df = NumberFormat.getDecimalFormat();
        df.overrideFractionDigits(4);
        int jitter = 1;
        int i = 0;
        for (SNPAllele allele : snpAlleles) {
            if (allele.getPhenotype() == null)
                continue;
            dataTable.addRow();
            Double value = Double.valueOf(allele.getPhenotype());
            double random = Random.nextDouble();
            Double xValue;
            if (allele1.equals(allele.getAllele())) {

                dataTable.setValue(i, 1, value);
                xValue = (2.5 + jitter * random);
                dataTable.setValue(i, 2, df.format(value));
            } else {
                dataTable.setValue(i, 3, value);
                xValue = (6.5 + jitter * random);
                dataTable.setValue(i, 4, df.format(value));
            }
            dataTable.setValue(i, 0, xValue);
            i = i + 1;
        }
        return dataTable;
    }
}
