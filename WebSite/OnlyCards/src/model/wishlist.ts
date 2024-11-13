import {Product} from "./product";
import {Price} from "./price";
import {ProductType} from "./productType";


export class wishlist{
  id: string;
  name: string;
  accounts: userWishlist[]=[];
  cards: CardWishlist[]= [];
  lastUpdate:Date;
  token:string|undefined;
  isPublic: boolean;
  constructor(obj: any) {
    this.id = obj.id;
    this.name = obj.name;
    this.isPublic = obj.isPublic;
    if(obj.accounts!=null)
      this.accounts = obj.accounts.map((account: any) => new userWishlist(account));
    if(obj.cards!=null)
      this.cards = obj.cards.map((card: any) => new CardWishlist(card));
    this.lastUpdate = new Date(obj.lastUpdate);
    if(obj.token!=null)
      this.token = obj.token;
  }
}
export type WishlistFilter = 'true' | 'false' | 'all';

export class userWishlist{
  id:string;
  username:string;
  keyOwner:string;
  valueOwner:string;
  constructor(obj: any) {
    this.id = obj.id;
    this.username = obj.username;
    this.keyOwner = obj.keyOwnership;
    this.valueOwner = obj.valueOwnership;
  }
}
export class CardWishlist{
  id:string;
  releaseDate:Date;
  images:string[];
  price:Price;
  account:userWishlist;
  condition:string;
  name:string;
  language:string;
  game:string;
  gameUrl:string;

  constructor(obj: any) {
    this.id = obj.id;
    this.releaseDate = new Date(obj.releaseDate);
    this.images = obj.images;
    this.price = new Price(obj.price);
    this.account = new userWishlist(obj.account);
    this.condition = obj.condition;
    this.name = obj.name;
    this.language = obj.language;
    this.game = obj.game;
    this.gameUrl = obj.gameUrl;
  }
}
export class WishlistEdit{
  name: string | undefined;
  isPublic: boolean | undefined;

  constructor(name?: string, isPublic?: boolean) {
    this.name = name;
    this.isPublic = isPublic;
  }
}

