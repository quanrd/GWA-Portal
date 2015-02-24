package com.gmi.nordborglab.browser.client.mvp.home.wizard;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by uemit.seren on 2/24/15.
 */
public class BasicStudyWizardModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(BasicStudyWizardPresenter.class,
                BasicStudyWizardPresenter.MyView.class, BasicStudyWizardView.class,
                BasicStudyWizardPresenter.MyProxy.class);
    }
}
