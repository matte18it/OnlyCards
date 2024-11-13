import {User} from "./user";
import {Transaction} from "./transaction";

export class Wallet {
  balance: number;
  user: User;
  transactions: Transaction[];


  constructor(obj: any) {
    this.balance = obj.balance;
    this.user = new User(obj.user);
    this.transactions = obj.transactions.map((transaction: any) => new Transaction(transaction));
  }
}
