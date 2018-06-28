import {Component, OnDestroy, OnInit} from '@angular/core';
import {Chart} from "angular-highcharts";
import {Stock} from "../../dao/Stock";
import {EventService} from "../../service/event.service";
import {Subscription} from "rxjs/internal/Subscription";

@Component({
  selector: 'stock-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css']
})
export class ChartComponent implements OnInit,OnDestroy{
  stockChart: Chart;
  subscription:Subscription;
  seriesLablels:Array<string> = [];

  constructor(private eventService: EventService) {
    this.stockChart = new Chart({
      chart: {
        type: 'area'
      },
      title: {
        text: 'Stock Chart'
      },
      credits: {
        enabled: false
      }
    });

   this.eventService.on("onAddSeries").subscribe(results => {
      let stock:Stock = results[0];
      this.seriesLablels.push(stock.symbol);
      this.stockChart.addSerie({
        data:[stock.ask],
        name:stock.symbol
      },true,true);
    })

    this.subscription= this.eventService.on("onReceivestock").subscribe(results => {
      let stock:Stock = results[0];
      for(let i=0; i<this.seriesLablels.length; i++){
        if(this.seriesLablels[i] === stock.symbol){
          this.stockChart.addPoint(stock.ask,i,true);
        } //use i instead of 0
      }
    })
  }

  ngOnDestroy(){
    this.subscription.unsubscribe();
  }

  ngOnInit() {


  }

}
