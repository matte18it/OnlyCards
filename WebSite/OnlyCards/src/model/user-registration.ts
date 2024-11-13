export class UserRegistration {
  email: string;
  password: string;
  username: string;
  cellphoneNumber?: string;

  constructor(obj: any) {
    this.email = obj.email;
    this.password = obj.password;
    this.username = obj.username;
    this.cellphoneNumber = obj.cellphoneNumber;
  }
}

