import { Component, OnInit } from '@angular/core';
import { NgxSpinnerService } from "ngx-spinner";
import { LocalStorageService } from "../../../service/utility/local-storage.service";
import { HandlerService } from "../../../service/error-handler/handler.service";
import { ProductService } from "../../../service/product/product.service";
import { Title } from "@angular/platform-browser";
import { Product } from "../../../model/product";
import {Router} from "@angular/router";
import {LoginService} from "../../../service/login/login.service";

@Component({
  selector: 'app-uploaded-products',
  templateUrl: './uploaded-products.component.html',
  styleUrl: './uploaded-products.component.css'
})

export class UploadedProductsComponent implements OnInit {
  // ----- VARIABILI -----
  protected products: Product[] = [];
  protected page: number = 0; // Pagina corrente
  protected numberProducts: number = 0;
  protected numberPage: number = 0;

  // ----- COSTRUTTORE -----
  constructor(private spinner: NgxSpinnerService, private localStorage: LocalStorageService, private handler: HandlerService, private productService: ProductService, private title: Title, private router: Router, private login: LoginService) {}

  // ----- METODI -----
  async ngOnInit(): Promise<void> {
    await this.spinner.show(); // Mostro lo spinner

    this.title.setTitle("OnlyCards | I tuoi prodotti");
    await this.countProducts(); // Conta i prodotti prima di caricare
    await this.getProducts(); // Prendo i prodotti

    await this.spinner.hide(); // Nascondo lo spinner
  } // Metodo per l'inizializzazione

  private async getProducts() {
    try {
      await new Promise<void>((resolve, reject) => {
        this.productService.getProductById(this.localStorage.getItem("userId"), this.page).subscribe({
          next: async (response) => {
            if (response) {
              this.products = response;
            }
            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    } catch (error) {
      this.handler.throwErrorToast("Errore nel caricamento dei prodotti!");
    }
  } // Metodo per ottenere i prodotti
  private async countProducts() {
    try {
      await new Promise<void>((resolve, reject) => {
        this.productService.countProducts(this.localStorage.getItem("userId")).subscribe({
          next: async (response) => {
            if (response) {
              this.numberProducts = response;
              this.numberPage = Math.ceil(this.numberProducts / 30);

              //console.log(this.numberProducts);
            }
            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    } catch (error) {
      this.handler.throwErrorToast("Errore nel conteggio dei prodotti!");
    }
  } // Metodo per contare i prodotti
  protected async deleteProduct(id: string) {
    await this.spinner.show(); // Mostro lo spinner

    try {
      await new Promise<void>((resolve, reject) => {
        this.productService.deleteProduct(id).subscribe({
          next: async (response) => {
            if (response) {
              await this.getProducts();
              this.products = this.products.filter((product) => product.id !== id);
              this.handler.throwSuccessToast("Prodotto eliminato con successo!");
            }
            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    } catch (error) {
      this.handler.throwErrorToast("Errore nell'eliminazione del prodotto!");
    }

    await this.spinner.hide(); // Nascondo lo spinner
  } // Metodo per eliminare un prodotto
  protected async goToPage(newPage: number) {
    if (newPage >= 0 && newPage < this.numberPage) {
      this.page = newPage;
      await this.getProducts();
    }
  } // Metodo per andare alla pagina selezionata
  protected async modifyProduct(id: string) {
    await this.router.navigate(["/seller/product/" + id]);
  }
}
