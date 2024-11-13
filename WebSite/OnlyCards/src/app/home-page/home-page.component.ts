import { Component, OnInit } from '@angular/core';
import {Title} from "@angular/platform-browser";
import {Router} from "@angular/router";
import {environment} from "../../utility/environment";
import {LocalStorageService} from "../../service/utility/local-storage.service";
import {GameService} from "../../service/game/game.service";
import {HandlerService} from "../../service/error-handler/handler.service";
import {NgxSpinnerService} from "ngx-spinner";
import { ThemeService } from '../../service/theme/theme.service';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})

export class HomePageComponent implements OnInit {
  games: Map<string, string> = new Map<string, string>();
  constructor( private loading:NgxSpinnerService, private handler:HandlerService, private gameService:GameService, private title: Title, private localStorage:LocalStorageService,  private router:Router) {}

  async ngOnInit() {
    this.title.setTitle("Only Cards | Homepage");
    let game = this.localStorage.getItem('game');

    // Se c'è un gioco selezionato, vado direttamente alla pagina del gioco
    if(game)
      await this.router.navigate([game]);

    // Altrimenti carico la pagina
    await this.loading.show();

    // Prendo i giochi
    await this.getGames();
  }

  private async getGames() {
    await new Promise<void>((resolve, reject) => {
      this.gameService.games.subscribe({
        next: (response) => {
          if(response.body)
            this.games = response.body;
          this.loading.hide();
        },
        error: (error) => {
          this.handler.throwErrorToast("Errore nel caricamento dei giochi, riprova più tardi!");
          this.loading.hide();
        }
      })
    });
  }

  scrollDown() {
    let gridContainer = document.getElementById("grid-container");

    if (gridContainer) {
      let containerTop = gridContainer.getBoundingClientRect().top + window.pageYOffset;

      window.scroll({
        top: containerTop,
        behavior: 'smooth'
      });
    }}

  protected vaiAlGioco(gioco: string) {
    let index = this.getGamesValue().indexOf(gioco);
    this.localStorage.setItem('game', this.getGamesKey()[index]);

    this.router.navigate([this.getGamesKey()[index]]).then(r => {});
  }

  protected readonly environment = environment;

  getGamesValue() {
    return Array.from(Object.values(this.games));
  }

  private getGamesKey() {
    return Array.from(Object.keys(this.games));
  }
  protected isDarkTheme() {
      return document.documentElement.getAttribute('data-theme') === 'dark';
    
  }
}
