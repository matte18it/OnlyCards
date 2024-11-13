import { AfterViewInit, ChangeDetectorRef, Component, HostListener, OnInit } from '@angular/core';
import { Router } from "@angular/router";
import { LocalStorageService } from "../../service/utility/local-storage.service";
import { LoginService } from "../../service/login/login.service";
import { NgxSpinnerService } from "ngx-spinner";
import { HandlerService } from "../../service/error-handler/handler.service";
import { GameService } from "../../service/game/game.service";

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})

export class NavbarComponent implements AfterViewInit, OnInit {
  // ------------------- Variabili -------------------
  protected windowWidth: number = 0;
  protected windowHeight: number = 0;
  protected games: Map<string, string> = new Map<string, string>();
  protected navbarGame: string[] = [];

  // ------------------- Costruttore -------------------
  constructor(private gamesService: GameService, private handler: HandlerService, private spinner: NgxSpinnerService, protected login: LoginService, private router: Router, private localStorage: LocalStorageService, private cdRef: ChangeDetectorRef) {
    this.windowWidth = window.innerWidth;
    this.windowHeight = window.innerHeight;
  }

  // ------------------- Inizializzazione -------------------
  async ngOnInit() {
    await this.getGames();
    this.getActiveGame();
  }

  ngAfterViewInit() {
    this.cdRef.detectChanges();  // Forza il rilevamento delle modifiche
  }

  // ------------------- Metodi -------------------
  protected getActiveGame() {
    this.navbarGame = [];
    if (this.games.size === 0) return;

    for (let i = 0; i < Object.keys(this.games).length; i++) {
      if (Object.keys(this.games)[i] !== this.localStorage.getItem('game')) {
        this.navbarGame.push(Array.from(Object.values(this.games))[i]);
      }
    }
    return Array.from(Object.values(this.games))[Array.from(Object.keys(this.games)).indexOf(<string>this.localStorage.getItem('game'))];
  }

  protected async getGames() {
    try {
      await new Promise<void>((resolve, reject) => {
        this.gamesService.games.subscribe({
          next: (response) => {
            if (response.body) {
              this.games = response.body;
              this.cdRef.detectChanges();  // Forza il rilevamento delle modifiche
            }
            resolve();
          },
          error: async (error) => reject(error)
        })
      });
    } catch (e) {
      this.handler.throwErrorToast("Errore nel caricamento dei giochi, riprova più tardi!");
    }
  }

  protected gotToPage(event: any) {
    let game = event.target.innerHTML;
    this.localStorage.setItem('game', Array.from(Object.keys(this.games))[Array.from(Object.values(this.games)).indexOf(game)]);
    window.location.href = '/' + this.localStorage.getItem('game');
  }

  @HostListener('window:resize', ['$event'])
  onResize() {
    this.windowWidth = window.innerWidth;
    this.windowHeight = window.innerHeight;
  }

  protected redirect(value: string) {
    window.location.href = '/' + value;
  }

  protected effettuaRicerca(term: string) {
    let game = this.localStorage.getItem('game');
    if (game) {
      if (term && term.trim() !== '') {
        this.router.navigate(['/' + game + '/products'], { queryParams: { name: term } }).then(r => {});
      } else {
        this.router.navigate(['/' + game + '/products']).then(r => {});
      }
      return;
    } else {
      this.router.navigate(['/']).then(r => {});
    }
  }

  protected showComponent() {
    return this.localStorage.getItem('game');
  }

  protected isProductPage() {
    return Object.keys(this.games).some(gameKey => this.router.url.includes(gameKey));
  }

  async logout() {
    await this.spinner.show();
    try {
      await new Promise<void>((resolve, reject) => {
        this.login.logout().subscribe({
          next: (response) => {
            if (response) {
              resolve();
            }
          },
          error: async (error) => reject(error)
        })
      });
    } catch (e) {

    } finally {
      await this.spinner.hide();
      this.handler.throwSuccessToast('Logout effettuato con successo');
    }
  }


}
