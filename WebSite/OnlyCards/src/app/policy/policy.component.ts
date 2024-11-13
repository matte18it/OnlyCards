import { Component } from '@angular/core';
import {Title} from "@angular/platform-browser";

@Component({
  selector: 'app-policy',
  templateUrl: './policy.component.html',
  styleUrl: './policy.component.css'
})

export class PolicyComponent {
  constructor(private title: Title) {}

  ngOnInit(): void {
    this.title.setTitle('OnlyCards | Termini e Condizioni');
  }
}
