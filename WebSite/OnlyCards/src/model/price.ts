export class Price {
  amount: number;
  currency: string;

  constructor(obj: any) {
    this.amount = obj.amount;
    this.currency = obj.currency;
  }
}

export interface PriceInfo {
  amount: number;
  currency: string;
}
