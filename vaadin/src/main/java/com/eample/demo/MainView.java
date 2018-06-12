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

import com.eample.demo.dao.Metrics;
import com.eample.demo.dao.Stock;
import com.eample.demo.services.DataService;
import com.example.demo.Endpoints;
import com.vaadin.flow.component.AttachEvent;
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
public class MainView extends VerticalLayout implements RouterLayout {

    //ui data model
    Map<String, List<Stock>> chartStockMap = new HashMap<>();
    Map<String, ListDataProvider<Stock>> dataProviderMap = new HashMap<>();

    Map<String, Metrics> metricsMap = new HashMap<>();
    Map<String, Stock> stockMap = new HashMap<>();

    //ui
    UI ui;
    Chart chart;
    Grid<Stock> stocksGrid;
    Grid<Metrics> metricsGrid;
    //data handlers
    Handler<Stock> stockHandler;
    Handler<Metrics> metricsHandler;


    public MainView() {
        ui = UI.getCurrent();
        chart = createChart();
        stocksGrid = createStocsGrid();
        metricsGrid = createMetricsGrid();

        HorizontalLayout hUpper = new HorizontalLayout(stocksGrid, chart);
        hUpper.setSizeFull();
        hUpper.setMargin(true);
        hUpper.setAlignItems(Alignment.START);
        HorizontalLayout hLower = new HorizontalLayout(metricsGrid);
        hLower.setSizeFull();
        hLower.setMargin(true);
        hLower.setAlignItems(Alignment.CENTER);
        VerticalLayout main = new VerticalLayout(hUpper,hLower);
        main.setAlignItems(Alignment.START);
        main.setSizeFull();
        main.setMargin(false);
        add(main);
        setSizeFull();
        //setHeight("100vh");
    }

    protected void createHandlers() {
        stockHandler = stock -> {
            ui.access(() -> {
                //for table
                stockMap.put(stock.getName(), stock);
                stocksGrid.getDataProvider().refreshAll();
                //for chart
                List<Stock> list = chartStockMap.get(stock.getSymbol());
                if (list == null) {
                    list = new ArrayList();
                    chartStockMap.put(stock.getSymbol(), list);
                    ListDataProvider<Stock> dataProvider = DataProvider.ofCollection(list);
                    dataProviderMap.put(stock.getSymbol(), dataProvider);
                    createStockChartDS(stock.getSymbol(), dataProvider);
                }
                list.add(stock);
                dataProviderMap.get(stock.getSymbol()).refreshAll();
            });
        };

        metricsHandler = metric -> {
            ui.access(() -> {
                System.out.println("metric data:" + metric);
                metricsMap.put(metric.getName(), metric);
                metricsGrid.getDataProvider().refreshAll();
            });
        };
    }

    private ListDataProvider<Stock> createStockskDS(String name) {
        ListDataProvider<Stock> ds = DataProvider.ofCollection(stockMap.values());
        return ds;
    }

    private ListDataProvider<Metrics> createMetricskDS(String name) {
        ListDataProvider<Metrics> ds = DataProvider.ofCollection(metricsMap.values());
        return ds;
    }

    private void createStockChartDS(String name, DataProvider dataProvider) {
        DataProviderSeries ds = new DataProviderSeries<Stock>(dataProvider, Stock::getAsk);
        ds.setName(name);
        chart.getConfiguration().addSeries(ds);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        createHandlers();
        DataService.getService().subscribe(Endpoints.MARKET_DATA, stockHandler);
        DataService.getService().subscribe(Endpoints.METRICS_SERICE, metricsHandler);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        DataService.getService().unSubscribe(Endpoints.MARKET_DATA, stockHandler);
        DataService.getService().unSubscribe(Endpoints.METRICS_SERICE, metricsHandler);
    }

    private Grid createStocsGrid() {
        stocksGrid = new Grid();
        stocksGrid.addColumn(Stock::getExchange).setHeader("Exchange");
        stocksGrid.addColumn(Stock::getSymbol).setHeader("Symbol");
        stocksGrid.addColumn(Stock::getBid).setHeader("Bid");
        stocksGrid.addColumn(Stock::getAsk).setHeader("Ask");
        stocksGrid.addColumn(Stock::getVolume).setHeader("Volume");
        stocksGrid.addColumn(Stock::getBid).setHeader("Status");
        stocksGrid.addColumn(Stock::getBid).setHeader("Status");
        stocksGrid.setDataProvider(createStockskDS(""));
        return stocksGrid;
    }

    private Grid<Metrics> createMetricsGrid() {
        metricsGrid = new Grid();
        metricsGrid.addColumn(Metrics::getName).setHeader("Name");
        metricsGrid.setDataProvider(createMetricskDS(""));
        return metricsGrid;
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
