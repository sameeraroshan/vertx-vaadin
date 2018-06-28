import { BrowserModule } from '@angular/platform-browser';
import {CUSTOM_ELEMENTS_SCHEMA, NgModule} from '@angular/core';

import { AppComponent } from './app.component';
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {MatButtonModule, MatCheckboxModule, MatGridListModule, MatCardModule, MatMenuModule, MatIconModule} from '@angular/material';
import { DashboardComponent } from './dashboard/dashboard.component';
import {ChartModule, HIGHCHARTS_MODULES} from "angular-highcharts";
import { ChartComponent } from './components/chart/chart.component';

import more from 'highcharts/highcharts-more.src';
import highstock from 'highcharts/modules/stock.src';
import { TableComponent } from './components/table/table.component';
import {DataService} from "./service/data.service";
import {MatTableModule} from '@angular/material/table';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    ChartComponent,
    TableComponent
  ],
  imports: [
    BrowserModule,ChartModule,MatTableModule,
    BrowserAnimationsModule,MatButtonModule, MatCheckboxModule, MatGridListModule, MatCardModule, MatMenuModule, MatIconModule
  ],
  schemas:[CUSTOM_ELEMENTS_SCHEMA],
  providers: [
    DataService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {

}
