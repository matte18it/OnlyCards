import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Params, Router} from "@angular/router";
import {AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn} from "@angular/forms";

import {environment} from "../../utility/environment";
import {ProductTypeService} from "../../service/product-type/product-type.service";
import {NgxSpinnerService} from "ngx-spinner";
import {HttpParams} from "@angular/common/http";
import {combineLatest} from "rxjs";
import {FeatureSearch} from "../../model/feature";
import {LocalStorageService} from "../../service/utility/local-storage.service";
import {ProductType} from "../../model/productType";
import {ProductService} from "../../service/product/product.service";
import {HandlerService} from "../../service/error-handler/handler.service";
import {LoginService} from "../../service/login/login.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AdminProductTypeComponent} from "../admin-product-type/admin-product-type.component";
import { Title } from '@angular/platform-browser';

function prezzoValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const minPrezzo = control.get('minPrezzo')?.value;
    const maxPrezzo = control.get('maxPrezzo')?.value;
    if (minPrezzo !== null && maxPrezzo !== null && minPrezzo !== 0 && maxPrezzo !== 0 && minPrezzo > maxPrezzo) {
      return { 'prezzoInvalido': true };
    } else {
      return null;
    }
  };
}


@Component({
  selector: 'app-search-page',
  templateUrl: './search-page.component.html',
  styleUrl: './search-page.component.css',
})

export class SearchPageComponent implements OnInit{

  queryParams: HttpParams = new HttpParams();
  size: number = 10;
  totalElements: number = 0;
  page:number=0;
  totalPages:number=0;
  searchForm: FormGroup;
  features:FeatureSearch[]=[];
  products: ProductType[] = [];
  game: string = "";
  sortingOptions: Map<string, string> = new Map<string, string>();
  languages: Map<string, string> = new Map<string, string>();
  languagesWithoutAll: Map<string, string> = new Map<string, string>();
  loadParams:boolean=false;
  private types: Map<string, string> = new Map<string, string>();


  constructor(private title:Title,  private productTypeService:ProductTypeService, private modalService:NgbModal, protected login:LoginService, private handler:HandlerService, protected productService:ProductService, protected localStorage:LocalStorageService, private spinner:NgxSpinnerService, private cardTypeService:ProductTypeService, private route: ActivatedRoute, private router: Router, private fb: FormBuilder) {
    this.searchForm = this.fb.group({
      nomeCarta: [''],
      lingua: '-',
      minPrezzo: null,
      maxPrezzo: null,
      ordinamento: '-',
      tipo: '-'
    },);
    this.searchForm.setValidators(prezzoValidator());
  }

  async ngOnInit(): Promise<void> {
    this.game = this.route.snapshot.params['game'];
    this.title.setTitle("Only Cards | Ricerca " + this.game);

    if(!this.loadParams){
      await this.loadSortingOptions();
      await this.loadLanguages();
      await this.loadTypes();
      this.loadParams=true;
    }
    combineLatest([this.route.paramMap, this.route.queryParams]).subscribe(([params, query]) => {
        this.queryParams = new HttpParams();
        this.setSizeParameter();
        this.spinner.show();
        //create a query[] object from query params
      let customQuery: { [key: string]: string } = {};
        // transform all query in lowercase and replace space with %20
        for (let key in query) {
          console.log(key);
          customQuery[key] = query[key].toLowerCase().split('%20').join(' ');
        }

        this.caricaRicercaAvanzata().then(()=>{
          if (customQuery['lan'] && this.queryParams.get('lan') !== customQuery['lan'] ){
            this.queryParams = this.queryParams.set('lan', customQuery['lan']);
            this.searchForm.get('lingua')?.setValue(Object.entries(this.languages).find(([key, value]) => key === customQuery['lan'])?.[1]);
          }
          if (customQuery['type'] && this.queryParams.get('type') !== customQuery['type']){
            this.queryParams = this.queryParams.set('type', customQuery['type']);
            this.searchForm.get('tipo')?.setValue(Object.entries(this.types).find(([key, value]) => key === customQuery['type'])?.[1]);
          }

          if (customQuery['name'] && this.queryParams.get('name') !== customQuery['name']){
            this.queryParams = this.queryParams.set('name', customQuery['name']);
            this.searchForm.get('nomeCarta')?.setValue(customQuery['name']);
          }
          if (customQuery['min-price'] && this.queryParams.get('min-price') !== customQuery['min-price']){
            this.queryParams = this.queryParams.set('min-price', customQuery['min-price']);
            this.searchForm.get('minPrezzo')?.setValue(customQuery['min-price']);
          }
          if (customQuery['max-price'] && this.queryParams.get('max-price') !== customQuery['max-price']){
            this.queryParams = this.queryParams.set('max-price', customQuery['max-price']);
            this.searchForm.get('maxPrezzo')?.setValue(customQuery['max-price']);
          }


          this.caricaCarteFeatures(customQuery);

          if (customQuery['sort'] && this.queryParams.get('sort') !== customQuery['sort']){
            this.queryParams = this.queryParams.set('sort', customQuery['sort']);
            if(Object.keys(this.sortingOptions).includes(customQuery['sort'])){
              const sortValue = Object.entries(this.sortingOptions).find(([key, value]) => key === customQuery['sort'])?.[1];
              if(sortValue)
                this.searchForm.get('ordinamento')?.setValue(sortValue);
            }
          }
          this.queryParams = this.queryParams.set('page', this.page.toString());
          this.queryParams = this.queryParams.set('size', this.size.toString());
          this.loadProduct();
          this.spinner.hide();
        });
      }, error => {
      this.spinner.hide();
        this.handler.throwGlobalError("Errore durante il caricamento della pagina. Riprova più tardi.").then(()=>{
          this.router.navigate(['/']);
        });
      }, () => {
        this.spinner.hide();
      }
    );



  }








  cercaCarte() {


    this.spinner.show();
    // Parametri di presenti in ogni gioco
    let urlFrontend = `/${this.game}/products`;


    const queryParamsObj: { [key: string]: string | number  } = {};
    const linguaValue = this.searchForm.get('lingua')?.value;
    // find the key of languages
    for(let [key, value] of Object.entries(this.languages)){
      if(value === linguaValue && key !== 'ALL'){
        queryParamsObj['lan'] = key;
        break;
      }
    }
    const typeValue = this.searchForm.get('tipo')?.value;
    // find the key of languages
    for(let [key, value] of Object.entries(this.types)){
      if(value === typeValue && key !== 'ALL'){
        queryParamsObj['type'] = key;
        break;
      }
    }

    const nomeCartaValue = this.searchForm.get('nomeCarta')?.value;
    if (nomeCartaValue) {
      queryParamsObj['name'] = nomeCartaValue;
    }

    const minPrezzoValue = this.searchForm.get('minPrezzo')?.value;
    if (minPrezzoValue != null && minPrezzoValue != 0) {
      queryParamsObj['min-price'] = minPrezzoValue;
    }

    const maxPrezzoValue = this.searchForm.get('maxPrezzo')?.value;
    if (maxPrezzoValue != null) {
      queryParamsObj['max-price'] = maxPrezzoValue;
    }
    for(let feature of this.features){
      let value=this.searchForm.get(feature.name)?.value;
      if(value && value!=='no selection'){
        queryParamsObj[feature.name.toString()]=value;
    }}

    const ordinamento = this.searchForm.get('ordinamento')?.value;
    if(this.sortingOptions){
    for (let [key, value] of Object.entries(this.sortingOptions)) {
      if(value === ordinamento){
        queryParamsObj['sort'] = key
        break;
      }
    }}
    // Create a new object with transformed values
    const transformedQueryParamsObj: { [key: string]: string | number } = {};
    for (let key in queryParamsObj) {
        transformedQueryParamsObj[key.toLowerCase()] = queryParamsObj[key]
          .toString()
          .toLowerCase()
          .split(' ')
          .join('%20');

    }


// Naviga alla nuova URL con i parametri della query string
    this.router.navigate([urlFrontend], { queryParams: transformedQueryParamsObj });



    this.spinner.hide();
  }

  cambiaOrdine(sort: string) {
    if(this.sortingOptions){
      for (let [key, value] of Object.entries(this.sortingOptions)) {
        if(value === sort){
          this.searchForm.get('ordinamento')?.setValue(value.split(' ')[0]);
          this.router.navigate([], { queryParams: { sort: key.toString().toLowerCase().split(' ').join('%20') }, queryParamsHandling: 'merge' });
          break;
        }
      }}

  }






  protected readonly environment = environment;




  private caricaRicercaAvanzata(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.cardTypeService.getProductTypeFeatures(this.game).subscribe(
        features => {
          this.features = features.map(f => new FeatureSearch(f));
          this.features.forEach(feature => {
            this.searchForm.addControl(feature.name, this.fb.control('no selection'));
          });
          resolve();  // Risolve la Promise una volta completato tutto
        },
        error => {
          reject(error); // Rifiuta la Promise in caso di errore
        }
      );
    });
  }


  private loadProduct() {

    this.cardTypeService.getProductTypes(this.game, this.queryParams).subscribe(
      cardsData => {
        // Mappa i dati delle carte per creare istanze di CardType
        this.products = cardsData.content.map((card: any) => new ProductType(card));
        this.page = cardsData.number;
        this.totalPages = cardsData.totalPages;
        this.totalElements = cardsData.totalElements;

      }, error => {
        this.spinner.hide();
        this.handler.throwGlobalError("Errore durante il caricamento della pagina. Riprova più tardi.").then(()=>{
          this.router.navigate(['/']);
        });
      }
      );
  }

  cambiaLingua(value: string) {
    this.searchForm.get('lingua')?.setValue(value);

  }

  private caricaCarteFeatures(query:Params):void {
    for(let feature of this.features){
      let featureUrl= feature.name;
      if(query[featureUrl] && this.queryParams.get(featureUrl) !== query[featureUrl]){
        this.queryParams = this.queryParams.set(featureUrl, query[featureUrl]);
        let featureValue = feature.value.find(value => value.toLowerCase()=== query[featureUrl].toLowerCase());
        if(featureValue)
          this.searchForm.get(feature.name)?.setValue(featureValue);
      }
    }
  }


  get pages(): number[] {
    const totalVisible = 5; // Numero totale di pagine da mostrare attorno alla pagina corrente
    let start = Math.max(this.page - Math.floor(totalVisible / 2), 1);
    let end = Math.min(start + totalVisible - 1, this.totalPages);

    if (end === this.totalPages) {
      start = Math.max(this.totalPages - totalVisible + 1, 1);
    }

    return Array.from({ length: (end - start + 1) }, (_, i) => start + i);


  }

  changePage(number: number) {
    this.page = number-1;
    this.queryParams = this.queryParams.set('page', this.page.toString());
    this.queryParams = this.queryParams.set('size', this.size.toString());
    this.loadProduct();

  }




  private setSizeParameter() {
    this.page = 0;
    this.size=environment.getSizeParameter();
  }


  getSortingOptionValues():string[] {
    return Array.from(Object.values(this.sortingOptions));
  }
  async loadSortingOptions() {
    try {
      const response = await this.productTypeService.getSortingOptions().toPromise();
      if (response && response.body) {
        this.sortingOptions = response.body;
      }
    } catch (error) {
      this.handler.throwGlobalError("Errore durante il caricamento della pagina. Riprova più tardi.").then(()=>{
        this.router.navigate(['/']);
      })
    }
  }

  private async loadLanguages() {
    try {
      const response = await this.productTypeService.getLanguages().toPromise();
      if (response && response.body) {
        this.languages = response.body;
        Object.assign(this.languagesWithoutAll , this.languages);
        Object.assign(this.languages, { ALL: '-' });

      }
    } catch (error) {
      console.log(error);
      this.handler.throwGlobalError("Errore durante il caricamento della pagina. Riprova più tardi.").then(()=>{
        this.router.navigate(['/']);
      })
    }
  }

  getLanguageValues() {
    return Array.from(Object.values(this.languages));
  }

  openModalAddProductType() {
    const modalRef = this.modalService.open(AdminProductTypeComponent, {fullscreen:true});
    modalRef.componentInstance.productType = null;
    modalRef.componentInstance.currentGame = this.game;
    modalRef.componentInstance.languages = this.languagesWithoutAll;
    modalRef.componentInstance.features = this.features;
    modalRef.componentInstance.types = this.types;
    modalRef.result.then(
			(result) => {
        this.ngOnInit();
        this.handler.throwSuccessToast("Tipo di prodotto salvato con successo");
			},
			(reason) => {
			},
		);


  }

  private async loadTypes() {
    try {
      const response = await this.productTypeService.getTypes().toPromise();
      if (response && response.body) {
        this.types = response.body;
        Object.assign(this.types, { ALL: '-' });
      }
    } catch (error) {
      console.log(error);
      this.handler.throwGlobalError("Errore durante il caricamento della pagina. Riprova più tardi.").then(()=>{
        this.router.navigate(['/']);
      })
    }

  }

  getTypeValues() {
    return Array.from(Object.values(this.types));
  }

  cambiaTipo(tipo: string) {
    this.searchForm.get('tipo')?.setValue(tipo);

  }

  modifyProductType(product: ProductType) {
    const modalRef = this.modalService.open(AdminProductTypeComponent, {fullscreen:true});
    modalRef.componentInstance.productType = product;
    modalRef.componentInstance.currentGame = this.game;

    modalRef.componentInstance.languages = this.languagesWithoutAll;
    modalRef.componentInstance.features = this.features;
    modalRef.componentInstance.types = this.types;
    modalRef.result.then(
			(result) => {
        this.ngOnInit();
        this.handler.throwSuccessToast("Tipo di prodotto modificato con successo");
			},
			(reason) => {
			},
		);

  }

  deleteProductType(product: ProductType) {
    this.spinner.show();
    this.productTypeService.deleteProductType(product.id).subscribe(
      response => {
        this.spinner.hide();
        this.ngOnInit();
        this.handler.throwSuccessToast("Tipo di prodotto eliminato con successo");
      },
      error => {
        this.spinner.hide();
        if(error.status===403){
          this.handler.throwGlobalError("Non sei autorizzato ad eliminare questo tipo di prodotto");
          return;
        }
        if(error.status===401){
          this.handler.throwGlobalError("La tua sessione è scaduta. Effettua nuovamente il login");
          return;
        }
        if(error.status==409){
          this.handler.throwGlobalError("Impossibile eliminare il tipo di prodotto. Ci sono dei prodotti associati a questo tipo");
          return;
        }
        this.handler.throwGlobalError("Errore durante l'eliminazione del tipo di prodotto. Riprova più tardi.");

      });

  }
}

