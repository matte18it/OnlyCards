import {Component, OnInit} from '@angular/core';
import { CartService } from "../../service/cart/cart.service";
import { Location } from "@angular/common";
import {ActivatedRoute, Router} from "@angular/router";
import { LoginService } from "../../service/login/login.service";
import {HandlerService} from "../../service/error-handler/handler.service";
import {NgxSpinnerService} from "ngx-spinner";
import {WishlistService} from "../../service/wishlist/wishlist.service";
import {OrderService} from "../../service/order/order.service";
import {EmailService} from "../../service/email/email.service";
import {UserService} from "../../service/user/user.service";
import {HttpParams} from "@angular/common/http";
import {Product} from "../../model/product";
import {AdminProductComponent} from "../admin-product/admin-product.component";
import {ProductService} from "../../service/product/product.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import { error } from '@angular/compiler-cli/src/transformers/util';

@Component({
  selector: 'app-sale-product',
  templateUrl: './sale-product.component.html',
  styleUrl: './sale-product.component.css'
})
export class SaleProductComponent implements OnInit{

  private cardID: string = '';
  protected wishlists: any = [];
  protected userID: string  | null = null;
  protected card: any = null;
  protected currentPaginationWishlist = 1;
  protected currentPageWishlist = 0;
  protected totalItemsWishlist = 0;

  constructor(private cartService: CartService,
              private location: Location,
              private route: ActivatedRoute,
              protected login: LoginService,
              private handler: HandlerService,
              private spinner: NgxSpinnerService,
              private wishlistService: WishlistService,
              private orderService: OrderService,
              private emailService: EmailService,
              private router: Router,
              private userService: UserService,
              private cardService: ProductService,
              private modal: NgbModal
  ) {}

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      this.cardID = params.get('cardID')!;
    });
    this.getProduct();
  }

  updatePageWishlist() {
    this.currentPageWishlist = this.currentPaginationWishlist - 1;
    this.getWishLists();
  }

  private async getProduct(){
    try {
      await this.spinner.show();
      const data: any = await this.cartService.getProduct(this.cardID).toPromise();
      this.card = data;
    } catch (err) {
      console.error('Error to get sale product:', err);
    } finally {
      await this.spinner.hide();
    }
  }

  protected async getWishLists(){
    if(!this.login.isAuthenticated()){
      await this.handler.throwGlobalWarning("Devi effettuare l'accesso per poter aggiungere carte alla wishlist");
      return;
    }

    try {
      await this.spinner.show();
      this.userID = this.login.loadUserId();
      let httpParams = new HttpParams()
        .set('sort', 'new')
        .set('is-owner', 'true')
        .set('page', this.currentPageWishlist.toString())
        .set('size', '10');

      const data: any = await this.userService.getUserWishlist(httpParams, this.userID).toPromise();
      if(data.body == null){
        this.handler.throwErrorToast("Non sei in possesso di nessuna wishlist, creane una.")
        return;
      }
      this.wishlists = data.body.content;
      this.currentPageWishlist = data.body.number;
      this.totalItemsWishlist = data.body.totalElements;


    } catch (err) {
      console.error('Error to get wishlists:', err);
    } finally {
      await this.spinner.hide();
    }

  }

  protected goBack(){
    this.location.back();
  }

  async addToCart(){
    if(!this.login.isAuthenticated()){
      await this.handler.throwGlobalWarning("Devi effettuare l'accesso per poter aggiungere carte al carrello");
      return;
    }

    this.userID = this.login.loadUserId();
    this.cartService.addProduct(this.userID,this.cardID).subscribe({
      next: () => {
        if(this.userID){
          this.handler.throwSuccessToast("Prodotto aggiunto al carrello!");
        }
      },
      error: (err) => {
        this.handler.throwGlobalError("Impossibile aggiungere il prodotto al carrello!");
      }
    });
  }

  async addToWishList(wishlistID: string){
    this.wishlistService.addProductToWishlist(wishlistID, this.cardID).subscribe({
      next: () => {
        this.handler.throwSuccessToast("Prodotto aggiunto alla wishlist!");
      },
      error: (err) => {
        if(err.status == 409){
          this.handler.throwErrorToast("Prodotto già presente nella wishlist!");

      }else{
        this.handler.throwErrorToast("Errore durante l'aggiunta alla wishlist!");
      }
    }
    });
  }

  async buyNow(){
    if(!this.login.isAuthenticated()){
      await this.handler.throwGlobalWarning("Devi effettuare l'accesso per poter acquistare una carta");
      return;
    }
    this.userID = this.login.loadUserId();
    const data = new FormData();
    let cardList: any =  [];
    cardList.push(this.card);
    if(this.userID){
      data.append('userID',this.userID);
      const productsJSON = JSON.stringify(cardList);
      data.append('products', productsJSON);
    }

    let products: string[] = [this.cardID];
    try {
      await this.spinner.show();
      if(this.card.sold){
        await this.spinner.hide();
        this.handler.throwErrorToast("Carta non disponibile!");
        return;
      }
      await this.orderService.createOrder(this.userID, products).toPromise();

      /* invio mail riepilogo ordine */
      await new Promise<void>((resolve, reject) => {
        this.emailService.sendOrderConfirmation(data).subscribe({
          next: () => {
            this.handler.throwSuccessToast("Ordine completato con successo!\ncontrolla la mail");
            resolve();
          },
          error: async (error) => {
            this.handler.throwErrorToast("Impossibile effettuare l'ordine\nriprova più tardi");
            reject(error);
          }
        })
      });

      await this.router.navigate(['/home-page']);

    } catch (err){
      console.error(err);
    } finally {
      await this.spinner.hide();
    }
  }

  // ---------- ADMIN -----------
  protected openModalAdmin(product: Product) {
    const openModal= this.modal.open(AdminProductComponent, {size: 'lg', centered: true});
    openModal.componentInstance.product = product;
    openModal.result.then((result) => {
        if(result){
          this.clearData();
          this.ngOnInit();
          this.handler.throwSuccessToast("Product updated successfully.");}
      },
      (ignored) => {

      },
    );
  } // Funzione per aprire il modal di amministrazione
  protected clearData() {
    this.card = null;
    this.wishlists = [];
  }  // Funzione per pulire i dati
  protected deleteProduct(product: Product) {
    this.spinner.show();
    this.cardService.deleteProduct(product.id).subscribe({
      next: () => {
        this.spinner.hide();
        this.clearData();
        this.ngOnInit();
        this.handler.throwSuccessToast("Product deleted successfully.");
        this.location.back();
      },
      error: () => {
        this.spinner.hide();
        this.handler.throwErrorToast("Error deleting product.");
      },

    });
  } // Funzione per eliminare un prodotto
}
