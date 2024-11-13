import {Feature} from "./feature";
import {Price} from "./price";

export class ProductType {
  id: string;
  name: string;
  language: string;
  price: Price;
  minPrice: Price;
  photo: string;
  lastAdd: Date;
  numSell: number;
  type: string;
  game: string;
  features: Feature[];

  constructor(obj: any) {
    this.id = obj.id;
    this.name = obj.name;
    this.language = obj.language;
    this.price = new Price(obj.price);
    this.minPrice = new Price(obj.minPrice);
    this.type = obj.type;
    this.game = obj.game;
    this.photo = obj.photo;
    this.lastAdd = new Date(obj.lastAdd);
    this.numSell = obj.numSell;
    this.features = obj.features;
  }
}
export class ProductTypeRegistration {
  name: string;
  language: string;
  game: string;
  type: string;
  features: Feature[] = [];
  photo:File | null = null;
  photoUrl: string | null = null;

  constructor(name: string, language: string, game: string, type: string, features: Feature[], photo:File | null, photoUrl: string | null) {
    this.name = name;
    this.language = language;
    this.game = game;
    this.type = type;
    this.features = features;
    this.photo = photo;
    if(photoUrl!="")
      this.photoUrl = photoUrl;


  }
}
