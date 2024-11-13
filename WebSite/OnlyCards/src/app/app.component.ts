import {Component, OnInit} from '@angular/core';
import {ThemeService} from "../service/theme/theme.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})

export class AppComponent implements OnInit {
  title = 'OnlyCardsFrontend';

  constructor(private themeService: ThemeService) {}

  ngOnInit() {
    // Cambia la favicon in base alle preferenze del tema di default del browser
    const prefersDarkScheme = window.matchMedia("(prefers-color-scheme: dark)");
    this.themeService.setFavicon(prefersDarkScheme.matches);
  }
}
