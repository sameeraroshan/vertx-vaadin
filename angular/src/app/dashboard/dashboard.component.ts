import {Component, OnInit} from '@angular/core';
import {StockChart} from "angular-highcharts";
@Component({
  selector: 'dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit{
  stock: StockChart;
  cards = [
    { title: 'Card 1', cols: 1, rows: 2,type:"chart" },
    { title: 'Card 2', cols: 1, rows: 2 ,type:"table"}
  ];
  ngOnInit() {

  }
}
