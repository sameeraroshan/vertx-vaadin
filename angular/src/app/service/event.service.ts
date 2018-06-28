import { Injectable } from '@angular/core';
import {Subject} from "rxjs/internal/Subject";
import {Subscription} from "rxjs/internal/Subscription";

@Injectable({
  providedIn: 'root'
})
export class EventService {

  private listeners = [];


  on(name: string): Subject<any> {
    let subject: Subject<any> = this.listeners[name];
    if (!subject) {
      subject = new Subject<any>();
      this.listeners[name] = subject;
    }
    return subject;
  }

  unSubscribe(subscription: Subscription) {
    subscription.unsubscribe();
  }


  broadcast(name: string, ...args) {
    let subject:Subject<any> = this.listeners[name];
    if(subject){
      subject.next(args);
    }
  }

}
