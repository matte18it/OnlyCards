import {Condition} from "./enum/conditionEnum";
import {User} from "./user";
import {ProductType} from "./productType";
import {Price, PriceInfo} from "./price";
import { ProductImage, ProductImageEdit } from "./productImage";

export class Product {
  id: string;
  productType: ProductType;
  stateDescription: string;
  condition: Condition;
  releaseDate: Date;
  price: Price;
  account: User;
  sold: boolean;
  images: ProductImage[] = [];

  constructor(obj: any) {
    this.id = obj.id;
    this.productType = new ProductType(obj.productType);
    this.stateDescription = obj.stateDescription;
    this.condition = obj.condition;
    this.releaseDate = new Date(obj.releaseDate);
    this.price = new Price(obj.price);
    this.account = new User(obj.user);
    this.sold = obj.sold;
    if (obj.images) {
      obj.images.forEach((image: any) => {
        this.images.push(new ProductImage(image));
      });
    }
  }
}

export class ProductEdit {
  condition: Condition | undefined;
  stateDescription: string | undefined;
  price: Price | undefined;
  images: ProductImageEdit[] = [];

  constructor( images: ProductImageEdit[], condition?: Condition, stateDescription?: string, price?: Price) {
    this.condition = condition;
    this.stateDescription = stateDescription;
    this.price = price;
    this.images = images;
  }

}
export interface ProductInfo{
  id:string;
  releaseDate:Date;
  name:string;
  language:string;
  game : string;
  gameUrl:string;
  condition:string;
  price:PriceInfo;
  images:string[];


}
