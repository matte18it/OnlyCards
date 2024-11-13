import {Component, HostListener, OnInit} from '@angular/core';
import {NgxSpinnerService} from "ngx-spinner";
import {LocalStorageService} from "../../service/utility/local-storage.service";
import {ActivatedRoute, Router} from "@angular/router";
import {forkJoin, map, Observable, of} from "rxjs";
import {ProductType} from "../../model/productType";
import {ProductTypeService} from "../../service/product-type/product-type.service";
import {GameService} from "../../service/game/game.service";
import {HandlerService} from "../../service/error-handler/handler.service";
import {ProductService} from "../../service/product/product.service";
import {Product} from "../../model/product";
import {Title} from "@angular/platform-browser";
import {catchError} from "rxjs/operators";

@Component({
  selector: 'app-product-page',
  templateUrl: './product-page.component.html',
  styleUrl: './product-page.component.css'
})

export class ProductPageComponent implements OnInit {
  // ------------- VARIABILI ---------------
  protected topCard : ProductType[] = []; //array delle carte prese dal db (più vendute)
  protected bestPurchases : ProductType[] = []; // array di carte prese dal db (miglior prezzo)
  protected lastProducts : Product[] = []; // array di carte prese dal db (ultimi prodotti)
  protected visibleCards: ProductType[] = []; // Array per le carte da visualizzare nella sezione delle carte più vendute
  protected nonVisibleCards: ProductType[] = []; // Array per le carte non visibili nella sezione delle carte più vendute
  protected visibleCardsBestPrice: ProductType[] = []; // Array per le carte visibili nella sezione delle carte col miglior prezzo
  protected nonVisibleCardsBestPrice: ProductType[] = []; // Array per le carte altre carte della sezione della carte col miglior prezzo
  protected lastProductsVisible: Product[] = []; // Array per le carte visibili nella sezione degli ultimi prodotti
  protected lastProductsNotVisible: Product[] = []; // Array per le altre carte della sezione degli ultimi prodotti
  protected topSellerTooltip: string = "Questa sezione fornisce informazioni sulle carte più vendute nel negozio."; // tooltip per le top seller
  protected bestPurchasesTooltip: string = "Questa sezione fornisce informazioni sulle carte messe in vendita al miglior prezzo.";
  protected indexTopSeller : number = 0; // indice per le top seller
  protected indexBestPurchases : number = 0;  // indice per le best purchases
  protected categoryCarousel: string[] = [];  // array per le immagini del carousel
  protected carouselGame: string = ""; // variabile per il gioco selezionato
  protected games: Map<string, string> = new Map<string, string>(); // mappa per i giochi

  // ------------- COSTRUTTORE ---------------
  constructor(private router:Router, private productTypeService : ProductTypeService, private spinner : NgxSpinnerService, private localStorage : LocalStorageService, private activeRouter: ActivatedRoute, private gameService: GameService, private handler: HandlerService, private productService: ProductService, private title: Title) {}
  protected game: string = ""; // variabile per il gioco selezionato

  // ------------- METODO DI INIZIALIZZAZIONE ---------------
  async ngOnInit() {
    this.spinner.show().then(r => {}); // mostro lo spinner
    // Accedi al parametro di rotta 'game'
    let game = this.activeRouter.snapshot.params['game'];
    if(game && this.localStorage.getItem('game') == game)
      this.game = game;
    else{
      this.localStorage.removeItem('game');
      this.router.navigate(['/home-page']).then(r => {});
    }

    await this.getGames();  // prendo i giochi
    this.carouselGame = Array.from(Object.values(this.games))[Array.from(Object.keys(this.games)).indexOf(this.activeRouter.snapshot.params['game'])];
    this.title.setTitle("OnlyCards | " + Array.from(Object.values(this.games))[Array.from(Object.keys(this.games)).indexOf(this.activeRouter.snapshot.params['game'])]); // setto il titolo della pagina

    this.setCarousel(); // setto il carousel
    this.getCards(); // prendo le top carte vendute
    this.spinner.hide().then(r => {}); // nascondo lo spinner
  } // metodo di inizializzazione

  // ------------- METODI ---------------
  private getCards() {
    // Chiamo i metodi del service per ottenere le carte
    const topSeller$: Observable<ProductType[]> = this.productTypeService.getTopSeller(this.game).pipe(
      catchError(error => {
        console.error('Errore nel recupero dei topSeller', error);
        return of([]); // Ritorna un array vuoto per mantenere il tipo Observable<ProductType[]>
      })
    );

    const bestPurchases$: Observable<ProductType[]> = this.productTypeService.getBestPurchases(this.game).pipe(
      catchError(error => {
        console.error('Errore nel recupero dei bestPurchases', error);
        return of([]); // Ritorna un array vuoto per mantenere il tipo Observable<ProductType[]>
      })
    );

    const lastProducts$: Observable<Product[]> = this.productService.getLastProducts(this.carouselGame).pipe(
      catchError(error => {
        console.error('Errore nel recupero dei lastProducts', error);
        return of([]); // Ritorna un array vuoto per mantenere il tipo Observable<Product[]>
      })
    );

    // Usare forkJoin per aspettare la fine di tutte le chiamate
    forkJoin([topSeller$, bestPurchases$, lastProducts$]).pipe(
      map(([topSellerResponse, bestPurchasesResponse, lastProductResponse]) => {
        // Popolare gli array se le risposte non sono vuote
        if (topSellerResponse.length > 0) this.topCard = topSellerResponse;
        if (bestPurchasesResponse.length > 0) this.bestPurchases = bestPurchasesResponse;
        if (lastProductResponse.length > 0) this.lastProducts = lastProductResponse;

        // Calcolare le carte visibili
        this.calculateVisibleCards();

        // Controllare se le immagini sono disponibili
        this.checkPhoto();
      })
    ).subscribe();
  } // Funzione per prendere i prodotti delle varie categorie
  protected async getGames() {
    try {
      await new Promise<void>((resolve, reject) => {
        this.gameService.games.subscribe({
          next: (response) => {
            if (response.body) {
              this.games = response.body;

              //console.log(this.games);
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
  private calculateVisibleCards() {
    const containerWidth: any = window.innerWidth;  // componente che contiene le carte topSeller
    let columns = 4; // Numero predefinito di colonne

    if (containerWidth >= 992)                              columns = 4; // Lg
    else if(containerWidth < 992 && containerWidth >= 768)  columns = 3; // Md
    else if(containerWidth < 768 && containerWidth >= 576)  columns = 2; // Sm
    else                                                    columns = 1; // Xs

    // Imposta le carte da visualizzare
    this.visibleCards = this.topCard.slice(0, columns);
    this.nonVisibleCards = this.topCard.slice(columns);
    this.visibleCardsBestPrice = this.bestPurchases.slice(0, columns);
    this.nonVisibleCardsBestPrice = this.bestPurchases.slice(columns);
    this.lastProductsVisible = this.lastProducts.slice(0, columns);
    this.lastProductsNotVisible = this.lastProducts.slice(columns);

    this.indexTopSeller = this.visibleCards.length + 1; // setto l'indice delle top seller
    this.indexBestPurchases = this.visibleCardsBestPrice.length + 1;  // setto l'indice delle best purchases
  } // metodo per determinare il numero delle carte da visualizzare sulla row
  private setCarousel() {
    if(this.localStorage.getItem("game") == 'pokemon')
      this.categoryCarousel = ['/assets/img/homeImage/pokemonCarousel_1.png', '/assets/img/homeImage/pokemonCarousel_2.png', '/assets/img/homeImage/pokemonCarousel_3.png'];
    else if(this.localStorage.getItem("game") == 'yu-gi-oh')
      this.categoryCarousel = ['/assets/img/homeImage/yugiohCarousel_1.png', '/assets/img/homeImage/yugiohCarousel_2.png', '/assets/img/homeImage/yugiohCarousel_3.png'];
    else
      this.categoryCarousel = ['/assets/img/homeImage/magicCarousel_1.png', '/assets/img/homeImage/magicCarousel_2.png', '/assets/img/homeImage/magicCarousel_3.png'];
  } // metodo per settare il carousel
  private checkPhoto() {
    // Controllo se l'immagine è disponibile, altrimenti metto un'immagine di default
    for(let i: number = 0; i < this.topCard.length; i++) {
      let imageUrl : string= this.topCard[i].photo;
      this.checkImageAvailability(imageUrl).catch(() => {
        this.topCard[i].photo = '/assets/img/errorCard.png';
      });
    }

    // Controllo se l'immagine è disponibile, altrimenti metto un'immagine di default
    for(let i : number= 0; i < this.bestPurchases.length; i++) {
      let imageUrl: string = this.bestPurchases[i].photo;
      this.checkImageAvailability(imageUrl).catch(() => {
        this.bestPurchases[i].photo = '/assets/img/errorCard.png';
      });
    }
  } // metodo per controllare se l'immagine è disponibile
  private checkImageAvailability(imageUrl: string): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      const img = new Image();  // creo un'immagine
      img.src = imageUrl; // setto l'url dell'immagine, faccio partire il caricamento

      // Se l'immagine è caricata correttamente, allora risolvo la promise  (immagine caricata correttamente)
      img.onload = () => { resolve(); };
      // Se l'immagine non è caricata correttamente, allora rigetto la promise (errore nel caricamento)
      img.onerror = () => { reject(); };
    });
  } // metodo per controllare se l'immagine è disponibile
  protected scrollToElement(element : HTMLElement) {
    if (element)
      window.scrollTo({ top: element.getBoundingClientRect().top + window.scrollY - 100, behavior: 'smooth' });
  } // metodo per scorrere fino ad un dato elemento della pagina
  protected isTopGamePage() {
    return ['pokemon', 'magic', 'yu-gi-oh'].find((game) => game == this.game);
  } // metodo per controllare se è una pagina tra quelle conosciute

  // ------------- LISTENER ---------------
  // Evento che si attiva al resize della pagina per cambiare dinamicamente il numero delle carte visualizzate
  @HostListener('window:resize', ['$event'])
  protected onResize() {
    this.calculateVisibleCards();
  } // metodo per cambiare il numero delle carte visualizzate in base alla grandezza della pagina
}
