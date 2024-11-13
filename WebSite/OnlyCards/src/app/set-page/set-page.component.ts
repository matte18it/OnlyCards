import {Component, OnInit} from '@angular/core';
import {ProductTypeService} from "../../service/product-type/product-type.service";
import {HandlerService} from "../../service/error-handler/handler.service";
import {Title} from "@angular/platform-browser";
import {ActivatedRoute} from "@angular/router";
import {NgxSpinnerService} from "ngx-spinner";
import {ProductType} from "../../model/productType";
import {LocalStorageService} from "../../service/utility/local-storage.service";
import {GameService} from "../../service/game/game.service";

@Component({
  selector: 'app-set-page',
  templateUrl: './set-page.component.html',
  styleUrl: './set-page.component.css'
})

export class SetPageComponent implements OnInit {
  // Attributi
  protected gameType: string = '';
  protected gameName: string = '';
  protected set: string = '';
  private page: number = 0;
  protected cardType: ProductType[] = [];
  private games: Map<string, string> = new Map<string, string>();
  protected numberOfPages: number = 0;
  protected numberOfCardForPage: number = 10;
  protected currentPage: number = 1;

  // Costruttore
  constructor(private productTypeService: ProductTypeService, private handler: HandlerService, private title: Title, private route: ActivatedRoute, private spinner: NgxSpinnerService, private gameService: GameService) {}

  // Inizializzazione
  async ngOnInit(): Promise<void> {
    await this.spinner.show();
    await this.getGames();

    this.gameType = this.route.snapshot.params['type']; // prendo il tipo di gioco
    this.gameName = Array.from(Object.values(this.games))[Array.from(Object.keys(this.games)).indexOf(this.route.snapshot.params['type'])];
    this.set = this.route.snapshot.params['set']; // prendo il set
    this.title.setTitle(`Onlycards | ${this.set}`); // imposto il titolo della pagina

    await this.getNumberOfCardFromSet();
    await this.getCardTypeFromSet();

    await this.spinner.hide();
  }

  // Metodi
  protected async getGames(): Promise<void> {
    try {
      await new Promise<void>((resolve, reject) => {
        this.gameService.games.subscribe({
          next: (response) => {
            if (response.body) {
              this.games = response.body;
            }
            resolve();
          },
          error: async (error) => reject(error)
        })
      });
    } catch (e) {
      this.handler.throwErrorToast("Errore nel caricamento dei giochi, riprova più tardi!");
    }
  } // Funzione per prendere i giochi
  private async getCardTypeFromSet(): Promise<void> {
    try {
      await new Promise<void>((resolve, reject) => {
        this.productTypeService.getCardFromSet(this.set, this.gameName, this.page).subscribe({
          next: (result) => {
            if (Array.isArray(result) && result.length > 0)
              this.cardType = result;

            resolve();
          },
          error: async (error) => reject(error)
        })
      });
    }
    catch (error) {
      this.handler.throwErrorToast("Errore durante il caricamento delle carte!");
    }
  } // Funzione per prendere le carte dal set
  private async getNumberOfCardFromSet(): Promise<void> {
    try {
      await new Promise<void>((resolve, reject) => {
        this.productTypeService.getNumberOfCardFromSet(this.set, this.gameName).subscribe({
          next: (result) => {
            if (result) {
              this.numberOfPages = Math.ceil(result / this.numberOfCardForPage);
            }

            resolve();
          },
          error: async (error) => reject(error)
        })
      });
    }
    catch (error) {
      this.handler.throwErrorToast("Errore durante il caricamento delle carte!");
    }
  } // Funzione per prendere il numero di carte dal set
  protected goToInfoPage(index: number) {
    window.location.href = `cards/${this.gameType}/card/${this.cardType[index].id}`;
  } // Funzione per andare alla pagina delle informazioni della carta
  protected async nextPage(): Promise<void> {
    await this.spinner.show();

    // Controllo se la pagina è minore del numero di pagine
    if (this.page < this.numberOfPages) {
      if(this.currentPage < this.numberOfPages)
        this.currentPage++;

      this.page++;
      await this.getCardTypeFromSet();
    }

    await this.spinner.hide();
  } // Funzione per andare alla pagina successiva
  protected async previousPage(): Promise<void> {
    await this.spinner.show();

    // Controllo se la pagina è maggiore di 0
    if (this.page > 0) {
      if(this.currentPage > 1)
        this.currentPage--;

      this.page--;
      await this.getCardTypeFromSet();
    }

    await this.spinner.hide();
  } // Funzione per andare alla pagina precedente
  protected getPagesToDisplay(): number[] {
    const maxPages = 3; // Numero massimo di pagine da mostrare
    const pages = [];

    let startPage = Math.max(this.currentPage - Math.floor(maxPages / 2), 1);
    let endPage = startPage + maxPages - 1;

    // Se siamo vicini alla fine, sposta il range verso il basso
    if (endPage > this.numberOfPages) {
      endPage = this.numberOfPages;
      startPage = Math.max(endPage - maxPages + 1, 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  } // Funzione per prendere le pagine da mostrare
  protected async goToPage(page: number): Promise<void> {
    if (page >= 1 && page <= this.numberOfPages) {
      this.currentPage = page;
      this.page = page - 1;  // Se `this.page` è zero-indicizzato
      await this.getCardTypeFromSet();
    }
  } // Funzione per andare alla pagina specifica
}
