package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.domain.AppData;
import com.gmi.nordborglab.browser.server.domain.BreadcrumbItem;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.phenotype.TransformationData;
import com.gmi.nordborglab.browser.server.domain.stats.AppStat;
import com.gmi.nordborglab.browser.server.domain.stats.DateStatHistogramFacet;
import com.gmi.nordborglab.browser.server.domain.util.UserNotification;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.rest.SampleData;
import com.gmi.nordborglab.browser.shared.proxy.DateStatHistogramProxy;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.util.List;

public interface HelperService {

    List<BreadcrumbItem> getBreadcrumbs(Long id, String object);

    AppData getAppData();

    List<String> getGenesFromCanddiateGeneListUpload(byte[] inputStream) throws IOException;

    List<TransformationData> calculateTransformations(List<Double> values);

    @PreAuthorize("hasRole('ROLE_USER')")
    List<UserNotification> getUserNotifications(Integer limit);

    List<AppStat> getAppStats();

    List<DateStatHistogramFacet> findRecentTraitHistogram(DateStatHistogramProxy.INTERVAL interval);

    Study applyTransformation(Study study);

    ExperimentUploadData getExperimentUploadDataFromIsaTab(byte[] isaTabData);

    ExperimentUploadData getExperimentUploadDataFromCsv(byte[] csvData) throws IOException;

    SampleData parseAndUpdateAccession(SampleData value);
}
