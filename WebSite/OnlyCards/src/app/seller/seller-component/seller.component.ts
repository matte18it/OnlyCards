import {Component, OnInit} from '@angular/core';
import {ProductTypeService} from "../../../service/product-type/product-type.service";
import {HandlerService} from "../../../service/error-handler/handler.service";
import {Title} from "@angular/platform-browser";
import {NgxSpinnerService} from "ngx-spinner";
import {ProductType} from "../../../model/productType";
import {AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators} from "@angular/forms";
import {getConditionArray, getConditionFromString} from "../../../model/enum/conditionEnum";
import {GameService} from "../../../service/game/game.service";
import {ProductService} from "../../../service/product/product.service";
import {LoginService} from "../../../service/login/login.service";
import {AdvancedSearchProductType} from "../../../model/advancedSearchProductType";

@Component({
  selector: 'app-seller-component',
  templateUrl: './seller.component.html',
  styleUrl: './seller.component.css'
})

export class SellerComponent implements OnInit {
  // ----- ATTRIBUTI -----
  protected readonly getConditionArray = getConditionArray;
  protected readonly Object = Object;
  protected readonly Array = Array;

  protected productTypes: ProductType[] = [];
  protected filteredProductTypes: ProductType[] = [];
  protected productForm: FormGroup = new FormGroup({});
  protected selectedProductType: ProductType | undefined = undefined;
  protected advancedSearchPhoto: AdvancedSearchProductType[] = [];
  protected games: Map<string, string> = new Map<string, string>();
  protected uploadedImages: { file: File; url: string }[] = [];
  protected page = 0;
  protected showButton = true;

  // ----- COSTRUTTORE -----
  constructor(private login: LoginService, private productService: ProductService, private gameService: GameService, private formBuilder: FormBuilder, private productTypeService: ProductTypeService, private handler: HandlerService, private title: Title, private spinner: NgxSpinnerService) {}

  // ----- METODI -----
  async ngOnInit(): Promise<void> {
    await this.spinner.show();

    this.title.setTitle('OnlyCards | Carica Prodotto');
    await this.getGames();
    this.createProductForm();

    await this.spinner.hide();
  } // metodo di inizializzazione

  // Metodi per ottenere i dati
  protected async getProductTypesSeller(type: string): Promise<void> {
    await this.spinner.show();
    this.resetForm(false);

    try {
      await new Promise<void> ((resolve, reject) => {
        this.productTypeService.getProductTypesSeller(type, this.login.loadUserId(), this.page).subscribe({
          next: (result) => {
            if(Array.isArray(result)) {
              this.productTypes = result;
              this.filteredProductTypes = result;
            }

            //console.log(this.productTypes);
            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    }
    catch (e) {
      this.handler.throwErrorToast("Errore nel caricamento dei prodotti!");
    }

    this.checkImageAvailability();
    await this.spinner.hide();
  } // metodo per ottenere tutti i tipi di prodotti
  protected async getMoreProducts(type: string): Promise<void> {
    await this.spinner.show();
    this.page++;

    try {
      await new Promise<void> ((resolve, reject) => {
        this.productTypeService.getProductTypesSeller(type, this.login.loadUserId(), this.page).subscribe({
          next: (result) => {
            if(Array.isArray(result) && result.length > 0) {
              this.productTypes.push(...result);

              // Aggiorno i prodotti filtrati
              if(this.productForm.get('productName')?.value == null)
                this.filterProductType('');
              else
                this.filterProductType(this.productForm.get('productName')?.value);
            } else if (result.length == 0) {
              this.handler.throwInfoToast("Non ci sono più prodotti da caricare!");
              this.showButton = false;
            }

            //console.log(this.productTypes);
            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    }
    catch (e) {
      this.handler.throwInfoToast("Non ci sono più prodotti da caricare!");
      this.showButton = false;
    }

    this.checkImageAvailability();
    await this.spinner.hide();
  } // metodo per ottenere più prodotti
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
  protected async advancedSearch(name: string): Promise<void> {
    await this.spinner.show();
    this.advancedSearchPhoto = [];

    try {
      await new Promise<void> ((resolve, reject) => {
        this.productTypeService.getAdvancedSearch(name, this.productForm.get('gameType')?.value, this.login.loadUserId()).subscribe({
          next: (result) => {
            if(Array.isArray(result))
              this.advancedSearchPhoto = result;

            //console.log(result);
            resolve();
          },
          error: async (error) => reject(error)
        })
      });
    }
    catch (e) {
      this.handler.throwErrorToast("Errore nella ricerca avanzata!");
    }

    // Controllo se le carte sono raggiungibili
    for (let i = 0; i < this.advancedSearchPhoto.length; i++) {
      const photo = this.advancedSearchPhoto.at(i);
      if (photo)
        this.checkImageAvailabilityPromise(photo.image).catch(() => {
          photo.image = '/assets/img/errorCard.png';
        });
    }

    await this.spinner.hide();
  } // metodo per la ricerca avanzata

  // Metodi per gestire il form
  protected createProductForm(): void {
    this.productForm = this.formBuilder.group({
      gameType: ['', Validators.required],
      productName: ['', [Validators.required, this.productTypeValidator.bind(this)]],
      stateDescription: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(200)]],
      condition: ['', Validators.required],
      price: ['', [Validators.required, Validators.pattern('^[0-9]+([,.][0-9]{1,2})?$'), Validators.min(0.01), Validators.max(500000)]],
      imageComponent: ['']
    });
  } // metodo per creare il form
  protected selectProductType(index: number): void {
    this.selectedProductType = this.filteredProductTypes[index];
  } // metodo per selezionare un productType
  protected async saveProduct(): Promise<void> {
    await this.spinner.show();

    if (this.productForm.valid) {
      // creo il dataform
      const formData = new FormData();
      formData.append('description', this.productForm.get('stateDescription')?.value);
      formData.append('condition', getConditionFromString(this.productForm.get('condition')?.value as string));
      formData.append('price.amount', this.productForm.get('price')?.value);
      formData.append('price.currency', "EUR");
      formData.append('productType', this.selectedProductType?.id as string);
      formData.append('game', this.productForm.get('gameType')?.value);
      this.uploadedImages.forEach((image, index) => { formData.append(`images[${index}].photo`, image.file); });

      try {
        await new Promise<void>((resolve, reject) => {
          this.productService.saveProduct(localStorage.getItem('userId') as string, formData).subscribe({
            next: (result) => {
              this.handler.throwSuccessToast("Prodotto caricato correttamente!");
              this.resetForm(true);
              resolve();
            },
            error: async (error) => reject(error)
          })
        });
      }
      catch (e) {
        this.handler.throwErrorToast("Errore nel caricamento del prodotto!");
      }
    }

    await this.spinner.hide();
  } // metodo per salvare il prodotto

  // Metodi per filtrare i productType
  protected onProductTypeChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.filterProductType(value);
  } // metodo per filtrare i productType
  private filterProductType(value: string): void {
    this.filteredProductTypes = this.productTypes.filter(productType => productType.name.toLowerCase().includes(value.toLowerCase()));
  } // metodo per filtrare i productType

  // Validatore personalizzato per controllare se il valore del prodotto è nell'elenco dei productTypes
  private productTypeValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value?.toLowerCase();    // Ottieni il nome inserito nel form
    const isValid = this.productTypes.some(pt => pt.name.toLowerCase() === value); // Controlla se il valore è presente nell'array productTypes
    return isValid ? null : { invalidProductType: true }; // Se il valore non è valido, ritorna un errore, altrimenti null
  } // metodo per validare il productType

  // Metodo per verificare se le immagini sono state caricate
  private checkImageAvailability() {
    for (let i = 0; i < this.productTypes.length; i++) {
      this.checkImageAvailabilityPromise(this.productTypes[i].photo).catch(() => {
        this.productTypes[i].photo = '/assets/img/errorCard.png';
      });
    }
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

  // Metodi per gestire le immagini
  protected onImageUpload(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      Array.from(input.files).forEach((file) => {
        if (this.uploadedImages.length < 5) {
          const reader = new FileReader();
          reader.onload = (e: ProgressEvent<FileReader>) => {
            const url = e.target?.result as string;
            this.uploadedImages.push({ file, url });
          };
          reader.readAsDataURL(file);
        }
      });
    }
  } // Metodo per caricare un'immagine
  protected removeImage(index: number): void {
    this.productForm.get('imageComponent')?.setValue('');
    this.uploadedImages.splice(index, 1);
  } // Metodo per rimuovere un'immagine
  protected getFileNames(): string {
    return this.uploadedImages.map(image => image.file.name).join(', ');
  } // Metodo per ottenere i nomi dei file

  // Metodi utili
  protected async selectImage(item: AdvancedSearchProductType): Promise<void> {
    await this.spinner.show();

    try {
      // salvo il productType
      await new Promise<void>((resolve, reject) => {
        this.productTypeService.saveAdvancedSearch(this.login.loadUserId(), item.id, this.productForm.get('gameType')?.value).subscribe({
          next: (result) => {
            if(result) {
              this.productTypes.push(result);
              this.selectedProductType = result;

              if(result.name != this.productForm.get('productName')?.value)
                this.productForm.get('productName')?.setValue(result.name);
              this.productForm.get('productName')?.updateValueAndValidity();
            }

            //console.log(result);
            resolve();
          },
          error: async (error) => reject(error)
        })
      });
    }
    catch (e) {
      this.handler.throwErrorToast("Errore nel salvataggio del prodotto!");
    }

    this.advancedSearchPhoto = [];

    // chiudo il modal
    const closeBtn = document.getElementById('closeAdvancedSearchModal');
    if (closeBtn)
      closeBtn.click();

    await this.spinner.hide();
  } // Funzione per selezionare l'immagine
  protected resetForm(value: boolean): void {
    // resetto il form
    if(value)
      this.productForm.get('gameType')?.setValue('');
    this.productForm.get('productName')?.reset();
    this.productForm.get('stateDescription')?.reset();
    this.productForm.get('condition')?.setValue('');
    this.productForm.get('price')?.reset();
    this.productForm.get('imageComponent')?.reset();

    // resetto le variabili
    this.uploadedImages = [];
    this.productTypes = [];
    this.selectedProductType = undefined;
    this.filteredProductTypes = [];
    this.advancedSearchPhoto = [];
    this.page = 0;
  } // Funzione per resettare il form
}
