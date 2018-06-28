import {Injectable} from '@angular/core';
import {Stock} from "../dao/Stock";
import {EventService} from "./event.service";
//import * as EventBus from 'vertx3-eventbus-client';
declare var EventBus: any;

@Injectable({
  providedIn: 'root'
})
export class DataService {
  stockList: Map<string, Array<Stock>> = new Map<string, Array<Stock>>();

  constructor(private eventService: EventService) {

  }

  init(){
    var eb = new EventBus("/eventbus/");
    var _this = this;
    eb.onopen = function () {
      console.log('connected');
      eb.registerHandler("websocket.data.service", function (err, message) {
        if (message) {
          _this.getStock(message);
        }
      });

      eb.send('demo.api.gateway.end.point.quotes', {
        name: 'from ' + navigator.product
      }, null, function (a, message) {
        if (message == null) {
          console.log("ERROR: response null");
        } else {
          console.log('response: ', message.body);
        }
      });
    };
    eb.onclose = function () {
      console.log("disconnected");
      eb = null;
    };
  }

  private getStock(message) {
    let stock: Stock = message.body
    let stockDataList: Array<Stock> = this.stockList.get(stock.symbol);
    if (stockDataList === null || stockDataList === undefined) {
      stockDataList = [];
      this.stockList.set(stock.symbol, stockDataList);
      this.eventService.broadcast("onAddSeries", stock);
    }
    stockDataList.push(stock);
    this.eventService.broadcast("onReceivestock", stock);
  }

}
