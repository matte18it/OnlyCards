import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})

export class TimeService {
  private data: Date = new Date();  // Oggetto Date

  constructor() {}

  public getCurrentHours(): number {
    return this.data.getHours();
  }

  public getCurrentMinutes(): number {
    return this.data.getMinutes();
  }

  public getCurrentSeconds(): number {
    return this.data.getSeconds();
  }

  public getCurrentTime(): string {
    return this.getCurrentHours() + ":" + this.getCurrentMinutes() + ":" + this.getCurrentSeconds();
  }

  public getLabel(){
    let hours = this.getCurrentHours();
    let label = "Buongiorno";
    if(hours >= 13 && hours < 18){
      label = "Buon pomeriggio";
    } else if(hours >= 18 && hours < 24){
      label = "Buonasera";
    }
    return label;
  }
}
