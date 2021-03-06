package com.gmi.nordborglab.browser.client.mvp.diversity.meta.candidategenelist.detail;

import com.gmi.nordborglab.browser.client.manager.SearchManager;
import com.gmi.nordborglab.browser.shared.proxy.annotation.GeneProxy;
import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 25.09.13
 * Time: 13:27
 * To change this template use File | Settings | File Templates.
 */
public interface CandidateGeneListDetailUiHandlers extends UiHandlers {

    void onSearchForGene(String request, final SearchManager.SearchCallback callback);

    void onSelectGene(String gene);

    void onAddGene();

    void onEdit();

    void onDelete();

    void onConfirmDelete();

    void onCancel();

    void onSave();

    void onShare();

    void onDeleteGene(GeneProxy gene);

    void refresh();
}
