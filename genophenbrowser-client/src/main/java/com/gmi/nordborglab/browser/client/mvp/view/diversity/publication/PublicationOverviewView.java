package com.gmi.nordborglab.browser.client.mvp.view.diversity.publication;

import com.gmi.nordborglab.browser.client.NameTokens;
import com.gmi.nordborglab.browser.client.ParameterizedPlaceRequest;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.publication.PublicationOverviewPresenter;
import com.gmi.nordborglab.browser.client.resources.CustomDataGridResources;
import com.gmi.nordborglab.browser.client.ui.CustomPager;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkPlaceManagerColumn;
import com.gmi.nordborglab.browser.shared.proxy.PublicationProxy;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/2/13
 * Time: 6:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationOverviewView extends ViewImpl implements
        PublicationOverviewPresenter.MyView {

    interface Binder extends UiBinder<Widget, PublicationOverviewView> {

    }

    private final Widget widget;
    @UiField(provided = true)
    DataGrid<PublicationProxy> dataGrid;
    @UiField
    CustomPager pager;
    private final PlaceManager placeManger;

    @Inject
    public PublicationOverviewView(final Binder binder, final PlaceManager placeManger,
                                   final CustomDataGridResources dataGridResources) {
        this.placeManger = placeManger;
        dataGrid = new DataGrid<PublicationProxy>(20, dataGridResources, new EntityProxyKeyProvider<PublicationProxy>());
        initGrid();
        widget = binder.createAndBindUi(this);
        pager.setDisplay(dataGrid);
    }

    private void initGrid() {
        dataGrid.setWidth("100%");
        dataGrid.setEmptyTableWidget(new Label("No Records found"));

        dataGrid.setEmptyTableWidget(new Label("No Records found"));
        final PlaceRequest request = new ParameterizedPlaceRequest(NameTokens.publication);
        dataGrid.addColumn(new HyperlinkPlaceManagerColumn<PublicationProxy>(new HyperlinkCell(), placeManger) {
            @Override
            public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(PublicationProxy object) {
                String name = object.getFirstAuthor();
                String url = "#" + placeManger.buildHistoryToken(request.with("id", object.getId().toString()));
                return new HyperlinkParam(name, url);
            }
        }, "Author");
        dataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return object.getTitle();
            }
        }, "Title");
        dataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return String.valueOf(object.getPubDate().getYear());
            }
        }, "Year");
        dataGrid.addColumn(new Column<PublicationProxy, String>(new TextCell()) {
            @Override
            public String getValue(PublicationProxy object) {
                return object.getJournal();
            }
        }, "Journal");
        dataGrid.addColumn(new Column<PublicationProxy, HyperlinkPlaceManagerColumn.HyperlinkParam>(new HyperlinkCell(true)) {
            @Override
            public HyperlinkPlaceManagerColumn.HyperlinkParam getValue(PublicationProxy object) {
                return new HyperlinkPlaceManagerColumn.HyperlinkParam(object.getDOI(), object.getURL());
            }
        }, "DOI");
    }

    @Override
    public HasData<PublicationProxy> getDisplay() {
        return dataGrid;
    }

    @Override
    public Widget asWidget() {
        return widget;
    }
}