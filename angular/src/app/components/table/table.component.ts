import { Component, OnInit } from '@angular/core';
import {Stock} from "../../dao/Stock";
import {EventService} from "../../service/event.service";
import {MatTableDataSource} from "@angular/material";


@Component({
  selector: 'stock-table',
  templateUrl: './table.component.html',
  styleUrls: ['./table.component.css']
})
export class TableComponent implements OnInit {

  displayedColumns: string[] = ['exchange', 'symbol', 'bid','ask','volume'];
  data : Stock[] = [];
  dataSource:MatTableDataSource<Stock> = new MatTableDataSource<Stock>()

  constructor(private eventService: EventService) {
    this.dataSource.data = this.data;
    this.eventService.on("onAddSeries").subscribe(results => {
      let stock:Stock = results[0];
      this.data.push(stock);
    })

   this.eventService.on("onReceivestock").subscribe(results => {
      let stock:Stock = results[0];
     this.data.map((s, i) => {
       if (s.symbol === stock.symbol){
         this.data[i] = stock;
         this.dataSource.data = this.data;
       }
     });
    })
  }

  ngOnInit() {
  }

}
