package com.eample.demo;

import com.eample.demo.dao.Stock;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

@SuppressWarnings("serial")
public class CustomerForm extends FormLayout {
    private TextField firstName = new TextField("First name");
    private TextField lastName = new TextField("Last name");
    private ComboBox<CustomerStatus> status = new ComboBox<>("Status");
    private Button save = new Button("Save");
    private Button delete = new Button("Delete");

    private CustomerService service = CustomerService.getInstance();
    private Stock stock;
    private MainView view;

    private Binder<Stock> binder = new Binder<>(Stock.class);

    public CustomerForm(MainView view) {
        this.view = view;

        HorizontalLayout buttons = new HorizontalLayout(save, delete);

        add(firstName, lastName, status, buttons);

        status.setItems(CustomerStatus.values());

        save.getElement().setAttribute("theme", "primary");

        binder.bindInstanceFields(this);

        save.addClickListener(e -> this.save());
        delete.addClickListener(e -> this.delete());

        setStock(null);
    }

    public void setStock(Stock stock) {
        this.stock = stock;
        binder.setBean(stock);
        boolean enabled = stock != null;
        save.setEnabled(enabled);
        delete.setEnabled(enabled);
        if (enabled) {
            firstName.focus();
        }
    }

    private void delete() {
        service.delete(stock);
         setStock(null);
    }

    private void save() {
        service.save(stock);
        setStock(null);
    }
}
