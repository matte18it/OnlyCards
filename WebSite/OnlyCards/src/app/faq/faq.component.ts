import { Component } from '@angular/core';
import {Title} from "@angular/platform-browser";

@Component({
  selector: 'app-faq',
  templateUrl: './faq.component.html',
  styleUrl: './faq.component.css'
})
export class FaqComponent {
  constructor(private title: Title) {}

  ngOnInit(): void {
    this.title.setTitle('OnlyCards | FAQ');
  }
}
