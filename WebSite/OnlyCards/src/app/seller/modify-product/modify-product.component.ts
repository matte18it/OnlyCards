import {Component, OnInit} from '@angular/core';
import {HandlerService} from "../../../service/error-handler/handler.service";
import {Title} from "@angular/platform-browser";
import {ActivatedRoute} from "@angular/router";
import {NgxSpinnerService} from "ngx-spinner";
import {ProductService} from "../../../service/product/product.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {Condition, getConditionArray, getConditionFromString, getConditionString} from "../../../model/enum/conditionEnum";
import {ProductEdit} from "../../../model/product";
import {ProductImageEdit} from "../../../model/productImage";
import {LoginService} from "../../../service/login/login.service";

@Component({
  selector: 'app-modify-product',
  templateUrl: './modify-product.component.html',
  styleUrl: './modify-product.component.css'
})

export class ModifyProductComponent implements OnInit {
  // ----- Variabili -----
  protected readonly getConditionArray = getConditionArray;
  protected readonly getConditionString = getConditionString;

  private id: string = '';
  protected productEdit: ProductEdit = new ProductEdit([]);
  protected productModifyForm: FormGroup = new FormGroup({});
  protected uploadedImages: { file: File; url: string }[] = [];

  // ----- Costruttore -----
  constructor(private handler: HandlerService, private title: Title, private route: ActivatedRoute, private spinner: NgxSpinnerService, private product: ProductService, private formBuilder: FormBuilder, private login: LoginService) {}

  // ----- Metodi -----
  async ngOnInit(): Promise<void> {
    await this.spinner.show();

    this.title.setTitle('Onlycards | Modifica prodotto');
    this.id = this.route.snapshot.params['id'];
    await this.getProduct();

    await this.spinner.hide();
  } // Metodo per l'inizializzazione del componente
  private async getProduct(): Promise<void> {
    await this.spinner.show();

    try {
      await new Promise<void> ((resolve, reject) => {
        this.product.getSingleProduct(this.id, this.login.loadUserId()).subscribe({
          next: async (response) => {
            if (response) {
              this.productEdit = response;

              if (this.productEdit.images) {
                this.productEdit.images.forEach((image: ProductImageEdit) => {
                  this.uploadedImages.push({ file: image.photo, url: image.photo + "" });
                });
              }
              this.createForm();
            }

            //console.log(this.productEdit);
            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    }
    catch (error) {
      this.handler.throwErrorToast("Errore durante il caricamento del prodotto");
    }

    await this.spinner.hide();
  } // Metodo per ottenere il prodotto
  private createForm(): void {
    this.productModifyForm = this.formBuilder.group({
      condition: [this.getConditionString(this.productEdit.condition ?? Condition.MINT), [Validators.required]],
      stateDescription: [this.productEdit.stateDescription ?? '', [Validators.required, Validators.minLength(1), Validators.maxLength(200)]],
      price: [this.productEdit.price?.amount ?? '', [Validators.required, Validators.pattern('^[0-9]+([,.][0-9]{1,2})?$'), Validators.min(0.01), Validators.max(500000)]],
    });
  } // Metodo per creare il form
  protected goBack(): void {
    window.history.back();
  } // Metodo per tornare indietro
  protected async saveProduct(): Promise<void> {
    await this.spinner.show();

    let condition: Condition | undefined;
    if (this.productModifyForm.value.condition) {
      condition = getConditionFromString(this.productModifyForm.value.condition);
    }

    let stateDescription: string | undefined;
    if (this.productModifyForm.value.stateDescription &&
      this.productModifyForm.value.stateDescription !== this.productEdit.stateDescription) {
      stateDescription = this.productModifyForm.value.stateDescription;
    }

    let price: { amount: number; currency: string } | undefined;
    const currency = this.productModifyForm.value.currency;

    if (this.productEdit.price &&
      (currency !== this.productEdit.price.currency ||
        this.productModifyForm.value.price !== this.productEdit.price.amount)) {
      price = {
        amount: this.productModifyForm.value.price,
        currency: "EUR"
      };
    }

    const images: ProductImageEdit[] = this.uploadedImages.map(image => {
      if (image.file) {
        const fileName = image.file.name || '';
        const validExtensions = ['jpg', 'jpeg', 'png'];

        const extension = fileName.split('.').pop()?.toLowerCase();
        if (extension && validExtensions.includes(extension)) {
          return new ProductImageEdit(image.file);
        }
      }
      return null;
    }).filter((image): image is ProductImageEdit => image !== null);

    const updatedProductEdit: ProductEdit = new ProductEdit(images, condition, stateDescription, price);
    try {
      await new Promise<void>((resolve, reject) => {
        this.product.updateProduct(this.id, updatedProductEdit).subscribe({
          next: async (response) => {
            if (response) {
              this.handler.throwSuccessToast("Prodotto modificato con successo");
              window.location.reload();
            }
            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    } catch (error) {
      this.handler.throwErrorToast("Errore durante il salvataggio del prodotto");
    }

    await this.spinner.hide();
  } // Metodo per salvare il prodotto

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
  protected async removeImage(index: number): Promise<void> {
    await this.spinner.show();

    // Elimino l'immagine se non è presente in productEdit, oppure se è presente ma non è l'unica immagine
    if (this.productEdit.images[index] === undefined || this.productEdit.images.length > 1) {
      this.uploadedImages.splice(index, 1);

      try {
        await new Promise<void>((resolve, reject) => {
          if (this.productEdit.images[index] !== undefined) {
            const imageId = this.productEdit.images[index].id || '';
            this.product.deleteImage(this.id, imageId).subscribe({
              next: async (response) => {
                if (response) {
                  this.handler.throwSuccessToast("Immagine rimossa con successo");
                  window.location.reload();
                }
                resolve();
              },
              error: async (error) => reject(error)
            });
          } else {
            resolve();
          }
        });
      }
      catch (error) {
        this.handler.throwErrorToast("Errore durante la rimozione dell'immagine");
      }
    } else {
      this.handler.throwErrorToast("Non puoi eliminare l'unica immagine salvata.");
    }

    await this.spinner.hide();
  } // Metodo per rimuovere un'immagine
  public getFileNames(): string {
    const fileNames = this.uploadedImages
      .map(image => image.file.name)
      .filter(name => name);

    return fileNames.join(', ');
  } // Metodo per ottenere i nomi dei file
}
