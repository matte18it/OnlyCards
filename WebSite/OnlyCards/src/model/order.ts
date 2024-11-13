import {Status} from "./enum/statusEnum";
import {User} from "./user";
import {Transaction, TransactionInfo} from "./transaction";

export class Order {
  id: string
  addDate: Date;
  modifyDate: Date;
  userLastModify: string;
  status: Status;
  user: User;
  transactions: Transaction[];
  vendorId: string;

  constructor(obj: any) {
    this.id = obj.id;
    this.addDate = obj.addDate;
    this.status = obj.status;
    this.modifyDate = obj.modifyDate;
    this.userLastModify = obj.userLast
    this.user = new User(obj.user);
    this.transactions = obj.transactions.map((transaction: any) => new Transaction(transaction));
    this.vendorId = obj.vendorId;
  }
}

export interface OrderInfo {
  id:string;
  addDate: Date;
  buyer: string;
  seller: string;
  status: Status;

}
export interface OrderDetails {
  modifyDate: Date;
  userLastEdit: string;
  transactions: TransactionInfo[];
}
export class OrderEdit {
  status: Status;

  constructor(status: Status) {
    this.status = status;
  }
}
