import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {CartService} from "../../service/cart/cart.service";
import {Title} from "@angular/platform-browser";
import {OrderService} from "../../service/order/order.service";
import {LoginService} from "../../service/login/login.service";
import {HandlerService} from "../../service/error-handler/handler.service";
import {NgxSpinnerService} from "ngx-spinner";
import {EmailService} from "../../service/email/email.service";
import { Location } from "@angular/common";


@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html',
  styleUrls: ['./checkout.component.css']
})


export class CheckoutComponent implements OnInit {

  private productsIDList: string[] = [];
  protected cartCards: any = [];
  private userID: string | null = null;

  constructor(private cart: CartService,
              private title: Title,
              private order: OrderService,
              private login: LoginService,
              private handler: HandlerService,
              private spinner: NgxSpinnerService,
              private router: Router,
              private email: EmailService,
              private location: Location
  ) {}

  ngOnInit(): void {
    this.spinner.show();

    this.title.setTitle("OnlyCards | Checkout")
    this.productsIDList = this.cart.getProductsID();
    this.userID = this.login.loadUserId()
    this.getProduct();

    this.spinner.hide();
  }

  private async getProduct(){
    try {
      this.spinner.show();
      for (const cardID of this.productsIDList) {
        const data: any = await this.cart.getProduct(cardID).toPromise();
        this.cartCards.push(data);
      }
    } catch (err) {
      console.error('Error to get sale product:', err);
    } finally {
      this.spinner.hide();
    }
  }

  getTotalPrice(){
    let totalPrice: number = 0;
    this.cartCards.forEach((card: any) => {
      totalPrice += card.price.amount;
    });
    return totalPrice.toFixed(2) + " € ";
  }

  goBack(){
    this.location.back();
  }


  async submitOrder() {
    const data = new FormData();
    if(this.userID){
      data.append('userID',this.userID);
      const productsJSON = JSON.stringify(this.cartCards);
      data.append('products', productsJSON);
    }

    try {
      this.spinner.show();
      this.cartCards.forEach((card: any) => {
        if(card.sold){
          this.spinner.hide();
          this.handler.throwErrorToast("Carta non disponibile!");
          return;
        }
      });
      await this.order.createOrder(this.userID, this.productsIDList).toPromise();
      await this.cart.emptyCart(this.userID, this.productsIDList).toPromise();
      await this.handler.throwSuccessToast("Ordine effettuato con successo!");

      /* invio mail riepilogo ordine */
      await new Promise<void>((resolve, reject) => {
        this.email.sendOrderConfirmation(data).subscribe({
          next: (response) => {
            this.handler.throwSuccessToast("Controlla la mail");
            resolve();
          },
          error: async (err) => reject(err)
          });
        });

    } catch (error) {
      console.error(error);
    } finally {
      this.spinner.hide();
      this.router.navigate(['/home-page'])
    }

  }









}
