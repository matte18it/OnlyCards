import { Component } from '@angular/core';
import {Title} from "@angular/platform-browser";

@Component({
  selector: 'app-about',
  templateUrl: './about.component.html',
  styleUrl: './about.component.css'
})

export class AboutComponent {
  constructor(private title: Title) {}

  ngOnInit(): void {
    this.title.setTitle('OnlyCards | Chi Siamo');
  }
}
