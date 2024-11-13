import {Component, OnInit} from '@angular/core';
import {NgxSpinnerService} from "ngx-spinner";
import {CartService} from "../../service/cart/cart.service";
import {Product} from "../../model/product";
import {LoginService} from "../../service/login/login.service";
import {Router} from "@angular/router";
import {Title} from "@angular/platform-browser";
import {OrderService} from "../../service/order/order.service";

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit {
  protected userID: string | null = null;
  protected cartCards: any;
  protected isAuthenticated: null | boolean = null;
  private totalPrice: number = 0;
  protected viewMode: string = "GRID";
  private productsID: string[] = [];

  constructor(private cartService: CartService,
              private spinner: NgxSpinnerService,
              private login: LoginService,
              private router: Router,
              private title: Title
  ) {}

  ngOnInit() {
    this.title.setTitle('OnlyCards | Cart')
    if(this.login.isAuthenticated()) {
      this.isAuthenticated = true;
      this.userID = this.login.loadUserId();
      this.getCards();
    } else {
      this.isAuthenticated = false;
    }

  }

  private async getCards() {
    try {
      this.spinner.show();
      const data = await this.cartService.getCartCards(this.userID).toPromise();
      this.cartCards = data.body;
    } catch (error) {
      console.error('Errore nel recupero delle carte:', error);
    } finally {
      this.spinner.hide();
    }
    this.initialTotalPrice();
  }

  removeProduct(cardID: string){
    this.spinner.show();
    this.cartService.removeProduct(this.userID, cardID).subscribe(
      async () => {
        await this.getCards();
      },
      () => {
        this.spinner.hide();
      });
  }

  getPrice(price:string,currency:string){
    if(currency=='EUR') return price + '€';
    else return price;
  }

  checkout(){
    this.cartService.setProductsID(this.productsID)
    this.router.navigate(["/checkout"]);
  }

  initialTotalPrice(){
    this.totalPrice = 0;
    this.cartCards.forEach((card:any) => {
      this.totalPrice += card.price.amount;
      this.productsID.push(card.id);
    });
    this.totalPrice = +this.totalPrice.toFixed(2)
  }

  updateTotal(card: any, event: Event) {
    const isChecked = (event.target as HTMLInputElement).checked;
    const cardPrice = card.price.amount;
    const cardID = card.id;

    if (isChecked) {
      this.totalPrice += cardPrice;
      this.totalPrice = +this.totalPrice.toFixed(2);
      this.productsID.push(cardID);

    } else {
      this.totalPrice -= cardPrice;
      this.totalPrice = +this.totalPrice.toFixed(2);
      this.productsID = this.productsID.filter(id => id !== cardID);

    }
  }

  getTotalPrice(){
    return this.totalPrice + '€';
  }

  setView(mode: string){
    this.viewMode = mode;
    this.initialTotalPrice();
  }







}
