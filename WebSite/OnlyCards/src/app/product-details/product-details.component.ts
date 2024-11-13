import { AfterViewInit, AfterViewChecked, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { Title } from "@angular/platform-browser";
import { ActivatedRoute } from "@angular/router";
import { ProductService } from "../../service/product/product.service";
import { NgxSpinnerService } from "ngx-spinner";
import { Product } from "../../model/product";
import VanillaTilt from "vanilla-tilt";
import {CardDetailsChartService} from "../../service/chart/card-details-chart.service";
import {HandlerService} from "../../service/error-handler/handler.service";
import {GameService} from "../../service/game/game.service";
import {Condition, getConditionString} from "../../model/enum/conditionEnum";
import {ProductTypeService} from "../../service/product-type/product-type.service";
import {environment} from "../../utility/environment";

@Component({
  selector: 'app-product-details',
  templateUrl: './product-details.component.html',
  styleUrls: ['./product-details.component.css']
})

export class ProductDetailsComponent implements OnInit, AfterViewInit, AfterViewChecked {
  @ViewChild('tiltElement') tiltElement!: ElementRef;
  // ---------- VARIABILI -----------
  protected cardId: string = "";
  protected gameType: string = "";
  protected gameName: string = "";
  protected setName: string = "";
  protected cardInfo: Product[] = [];
  protected language: Map<string, string> = new Map<string, string>();
  protected cardType: any;
  protected page: number = 0;
  protected cardForPage: number = 50;
  private isTiltInitialized: boolean = false;
  protected dailyAverages: number[] = []; // Array per memorizzare i prezzi medi giornalieri
  private lastDate: string[] = []; // Array per memorizzare le date degli ultimi 30 giorni
  private games: Map<string, string> = new Map<string, string>();
  // ---------- ENUM -----------
  protected readonly Condition = Condition;
  protected readonly getConditionString = getConditionString;

  // ---------- COSTRUTTORE -----------
  constructor(private productTypeService: ProductTypeService, private handler: HandlerService, private title: Title, private route: ActivatedRoute, private cardService: ProductService, private spinner: NgxSpinnerService, private chart : CardDetailsChartService, private gameService: GameService) {}

  // ---------- INIZIALIZZAZIONE -----------
  async ngOnInit() {
    await this.spinner.show(); // mostro lo spinner
    await this.getGames(); // prendo i giochi

    this.cardId = this.route.snapshot.params['id']; // prendo l'id della carta
    this.gameType = this.route.snapshot.params['type']; // prendo il tipo di gioco
    this.gameName = Array.from(Object.values(this.games))[Array.from(Object.keys(this.games)).indexOf(this.route.snapshot.params['type'])];

    await this.getLanguage(); // prendo le lingue
    await this.getCardType(); // prendo il tipo di carta
    await this.getCardInfo(); // prendo le informazioni della carta

    this.title.setTitle("Only Cards | " + this.cardType.name);  // setto il titolo della pagina
    this.calculateDailyAverages(); // calcolo i prezzi medi giornalieri
    this.chart.createChart(this.dailyAverages, this.lastDate); // creo il grafico

    await this.spinner.hide(); // nascondo lo spinner
  }
  ngAfterViewInit() {
    this.checkAndInitializeTilt();
  }
  ngAfterViewChecked() {
    this.checkAndInitializeTilt();
  }

  // ---------- METODI -----------
  protected async getGames() {
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
  private async getCardInfo(): Promise<void> {
    try {
      await new Promise<void>((resolve, reject) => {
        this.cardService.getCardInfo(this.cardId, this.page).subscribe( {
          next: (result) => {
            if (result != null && result.content) {
              this.cardInfo.push(...result.content);
              this.checkImageAvailability();  // Controllo se le immagini sono disponibili
            }

            if(result != null && result.content.length === 0) {
              this.handler.throwInfoToast("Non ci sono altri prodotti disponibili!");
            }

            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    }
    catch (error) {
      this.handler.throwErrorToast("Errore nel caricamento dei dati!");
    }
  } // Funzione per prendere le informazioni della carta
  private async getCardType(): Promise<void> {
    try {
      await new Promise<void>((resolve, reject) => {
        this.productTypeService.getProductType(this.cardId).subscribe({
          next: (response) => {
            if (response) {
              this.cardType = response;
            }

            // tra le features prendo il set
            this.cardType.features.forEach((feature: any) => {
              if (feature.name === 'set') {
                this.setName = feature.value;
              }
            });

            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    } catch (e) {
      this.handler.throwErrorToast("Errore nel caricamento del tipo di carta, riprova più tardi!");
    }
  } // Funzione per prendere il tipo di carta
  private async getLanguage(): Promise<void> {
    try {
      await new Promise<void>((resolve, reject) => {
        this.productTypeService.getLanguages().subscribe({
          next: (response) => {
            if (response.body) {
              this.language = new Map(Object.entries(response.body));
            }
            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    } catch (e) {
      this.handler.throwErrorToast("Errore nel caricamento delle lingue, riprova più tardi!");
    }
  } // Funzione per prendere le lingue
  protected getCssClass(languageCode: string | undefined): string {
    return environment.getCssClassMap().get(languageCode ?? '') || '';
  } // Funzione per prendere la classe css
  protected getFeaturesName(featureName: string | undefined): string {
    return environment.getFeaturesNames().get(featureName ?? '') || '';
  } // Funzione per prendere il nome della feature
  protected getLanguageName(languageCode: string | undefined): string {
    return environment.getLanguageNames().get(languageCode ?? '') || '';
  } // Funzione per prendere il nome della lingua
  private checkAndInitializeTilt(): void {
    if (!this.isTiltInitialized && this.cardType && this.tiltElement) {
      this.setImageAnimation();
      this.isTiltInitialized = true;
    }
  } // Funzione per controllare e inizializzare l'animazione delle immagini
  private setImageAnimation(): void {
    if (this.tiltElement) {
      VanillaTilt.init(this.tiltElement.nativeElement, {
        max: 25,
        speed: 400,
      });
    }
  } // Funzione per animare le immagini
  protected async loadMore(): Promise<void> {
    await this.spinner.show();

    this.page++;
    await this.getCardInfo();

    this.calculateDailyAverages(); // calcolo i prezzi medi giornalieri
    this.chart.createChart(this.dailyAverages, this.lastDate); // creo il grafico

    await this.spinner.hide();
  } // Funzione per caricare più carte quando richiesto
  private calculateDailyAverages(): void {
    // Inizializzazione
    this.dailyAverages = [];
    this.lastDate = [];
    const now: Date = new Date();
    const dailyData: { [key: string]: number[] } = {};

    // Raccogliere i dati giornalieri
    this.cardInfo.forEach(card => {
      const releaseDate: Date = new Date(card.releaseDate);
      const dateStr: string = releaseDate.toISOString().split('T')[0]; // 'YYYY-MM-DD'
      if (!dailyData[dateStr]) {
        dailyData[dateStr] = [];
      }
        dailyData[dateStr].push(card.price.amount);
    });

    this.dailyAverages = Array(30).fill(0); // Prepara un array di 30 giorni inizializzato a 0

    let lastAverage: number = 0; // Ultimo valore medio calcolato
    let foundFirstValue: boolean = false;

    for (let i: number = 0; i < 30; i++) {
      const date: Date = new Date();
      date.setDate(now.getDate() - (29 - i));
      const dateStr = date.toISOString().split('T')[0]; // 'YYYY-MM-DD'
      this.lastDate.push(dateStr);  // Inserisco la data nell'array

      if (dailyData[dateStr]) {
        const dailyPrices: number[] = dailyData[dateStr];
        const average: number = parseFloat((dailyPrices.reduce((sum: number, price: number) => sum + price, 0) / dailyPrices.length).toFixed(2));
        this.dailyAverages[i] = average; // Inserisci il valore nel posto giusto
        lastAverage = average; // Aggiorna l'ultimo valore medio calcolato
        foundFirstValue = true; // Indica che abbiamo trovato il primo valore
      } else if (foundFirstValue) {
        this.dailyAverages[i] = lastAverage; // Riempie i valori mancanti con l'ultimo valore calcolato
      } else {
        this.dailyAverages[i] = 0; // Riempie i valori mancanti con 0 prima di trovare il primo valore
      }
    }
} // Funzione per calcolare i prezzi medi giornalieri
  private checkImageAvailability() {
    let imageUrl: string = this.cardType.photo;
    this.checkImageAvailabilityPromise(imageUrl).catch(() => {
      this.cardInfo[this.cardInfo.length - 1].productType.photo = '/assets/img/errorCard.png';
    });
  } // Funzione per controllare se l'immagine è disponibile
  private checkImageAvailabilityPromise(imageUrl: string): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      const img = new Image();  // creo un'immagine
      img.src = imageUrl; // setto l'url dell'immagine, faccio partire il caricamento

      // Se l'immagine è caricata correttamente, allora risolvo la promise  (immagine caricata correttamente)
      img.onload = () => { resolve(); };
      // Se l'immagine non è caricata correttamente, allora rigetto la promise (errore nel caricamento)
      img.onerror = () => { reject(); };
    });
  } // Funzione per controllare se l'immagine è disponibile
}

