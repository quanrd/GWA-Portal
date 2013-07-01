package com.gmi.nordborglab.browser.shared.service;

import java.util.List;

import com.gmi.nordborglab.browser.shared.proxy.*;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.RequestContext;
import com.google.web.bindery.requestfactory.shared.ServiceName;


@ServiceName(value = "com.gmi.nordborglab.browser.server.service.HelperService", locator = "com.gmi.nordborglab.browser.server.service.SpringServiceLocator")
public interface HelperRequest extends RequestContext {

    Request<List<BreadcrumbItemProxy>> getBreadcrumbs(Long id, String object);

    Request<AppDataProxy> getAppData();

    Request<List<TransformationDataProxy>> calculateTransformations(List<Double> values);

    Request<List<UserNotificationProxy>> getUserNotifications(Integer limit);

    Request<List<DateStatHistogramFacetProxy>> findRecentTraitHistogram(DateStatHistogramProxy.INTERVAL interval);
}
