/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.eample.demo;

import com.eample.demo.services.DataService;
import com.eample.demo.services.MicroServicesListener;
import com.example.demo.Endpoints;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.shared.communication.PushMode;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main view contains a button and a template element.
 */
@HtmlImport("styles/shared-styles.html")
@Route("")
@Push(PushMode.AUTOMATIC)
public class MainView extends VerticalLayout implements RouterLayout, Handler<Stock> {
    UI ui;

    Map<String,List<Stock>> chartStockMap = new HashMap<>();
    Map<String,ListDataProvider<Stock>> dataProviderMap = new HashMap<>();

    Chart chart;
    Grid<Stock> grid;

    public MainView() {
        ui = UI.getCurrent();
        chart = createChart();
        grid = createGrid();

        HorizontalLayout main = new HorizontalLayout(grid, chart);
        main.setAlignItems(Alignment.START);
        main.setSizeFull();
        add(main);
        setHeight("100vh");
    }


    @Override
    public void handle(Stock stock) {
        ui.access(() -> {
            List<Stock> list = chartStockMap.get(stock.getSymbol());
            if(list == null){
                list = new ArrayList();
                chartStockMap.put(stock.getSymbol(),list);
                ListDataProvider<Stock> dataProvider = DataProvider.ofCollection(list);
                dataProviderMap.put(stock.getSymbol(),dataProvider);
                createStockDS(stock.getSymbol(),dataProvider);
            }
            list.add(stock);
            dataProviderMap.get(stock.getSymbol()).refreshAll();
        });
    }

    private void createStockDS(String name, DataProvider dataProvider) {
        DataProviderSeries<Stock> ds = new DataProviderSeries<>(dataProvider,Stock::getAsk);
        ds.setName(name);
        chart.getConfiguration().addSeries(ds);
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        DataService.getService().subscribe(Endpoints.MARKET_DATA, this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        DataService.getService().unSubscribe(Endpoints.MARKET_DATA, this);
    }

    private Grid createGrid() {
        Grid<Stock> grid = new Grid<>();
        grid.addColumn(Stock::getExchange).setHeader("Exchange");
        grid.addColumn(Stock::getSymbol).setHeader("Symbol");
        grid.addColumn(Stock::getBid).setHeader("Bid");
        grid.addColumn(Stock::getAsk).setHeader("Ask");
        grid.addColumn(Stock::getVolume).setHeader("Volume");
        grid.addColumn(Stock::getBid).setHeader("Status");
        grid.addColumn(Stock::getBid).setHeader("Status");
       // grid.setDataProvider(dataProvider);
        return grid;
    }

    public Chart createChart() {
        chart = new Chart(ChartType.AREA);
        final Configuration configuration = chart.getConfiguration();
        configuration.getTitle().setText("Data from Vaadin DataProvider");
        configuration.getLegend().setEnabled(false);
        YAxis yAxis = configuration.getyAxis();

        yAxis.setTitle(new AxisTitle("Exchange rate"));
        yAxis.setMin(0.6);
        yAxis.setStartOnTick(false);
        yAxis.setShowFirstLabel(false);

        configuration.getTooltip().setShared(true);
        PlotOptionsArea plotOptions = new PlotOptionsArea();
        plotOptions.setShadow(false);

        Marker marker = plotOptions.getMarker();
        marker.setEnabled(false);
        Hover hoverState = new Hover(true);
        hoverState.setRadius(5);
        //   hoverState.setLineWidth(1);
        marker.getStates().setHover(hoverState);

        plotOptions.getStates().setHover(new Hover(true));
        plotOptions.setShadow(false);
        configuration.setPlotOptions(plotOptions);
        chart.drawChart();
        return chart;
    }
}
