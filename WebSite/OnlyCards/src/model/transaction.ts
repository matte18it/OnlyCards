import {Wallet} from "./wallet";
import {Product} from "./product";
import {Order} from "./order";
import {Price, PriceInfo} from "./price";

export class Transaction {
  date: Date;
  value: Price;
  type: boolean;
  wallet: Wallet;
  product: Product;
  order: Order;

  constructor(obj: any) {
    this.date = obj.date;
    this.value = new Price(obj.value);
    this.type = obj.type;
    this.wallet = new Wallet(obj.wallet);
    this.product = new Product(obj.product);
    this.order = new Order(obj.order);
  }
}

export interface TransactionInfo {
  productName: string;
  productPhoto: string;
  value:PriceInfo;
}
