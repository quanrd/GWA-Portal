package com.gmi.nordborglab.browser.client.mvp.widgets.filter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 08.10.13
 * Time: 17:06
 * To change this template use File | Settings | File Templates.
 */
public abstract class FilterItemPresenterWidgetView<C extends FilterItemPresenterWidget.MyView>
        extends ViewWithUiHandlers<FilterItemPresenterUiHandlers> implements FilterItemPresenterWidget.MyView {

    @UiField
    Anchor filterLabel;
    protected Widget widget;
    private boolean initialized = false;

    @UiField
    protected Widget container;

    protected final Modal popup = new Modal();

    protected FilterItemPresenterWidgetView() {
    }

    protected void initContainer() {
        popup.setDataBackdrop(ModalBackdrop.STATIC);
        popup.setClosable(true);
        popup.setTitle("Add filter");
        Button cancelEditBtn = new Button("Cancel", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onCancel();
            }
        });
        cancelEditBtn.setType(ButtonType.DEFAULT);
        Button saveEditBtn = new Button("Save", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getUiHandlers().onAdd();
            }
        });
        saveEditBtn.setType(ButtonType.PRIMARY);
        ModalFooter footer = new ModalFooter();
        footer.add(cancelEditBtn);
        footer.add(saveEditBtn);
        ModalBody modalBody = new ModalBody();
        modalBody.add(container);
        popup.add(modalBody);
        popup.add(footer);
        container.setVisible(true);
    }

    @UiHandler("filterLabel")
    public void onClickFilterLabel(ClickEvent e) {
        getUiHandlers().onOpenFilterSettings();
    }


    @Override
    public void showPopup(boolean show) {
        if (!initialized) {
            initContainer();
            initialized = true;
        }
        if (show) {
            popup.show();
        } else {
            popup.hide();
        }
    }

    public void setFilterLabel(String label) {
        filterLabel.setText(label);
    }


}