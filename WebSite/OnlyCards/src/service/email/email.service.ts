import { Injectable } from '@angular/core';
import {LoginService} from "../login/login.service";
import {map} from "rxjs";

@Injectable({
  providedIn: 'root'
})

export class EmailService {

  constructor(private login: LoginService) {}

  requestProduct(data: FormData) {
    return this.login.authenticatedRequest("/v1/emails/request-product", "POST", true, undefined, data).pipe(
      map(response =>  null)
    );
  }

  sendHelpRequest(data: FormData) {
    return this.login.authenticatedRequest("/v1/emails/help-request", "POST", true, undefined, data).pipe(
      map(response => null)
    );
  }

  sendOrderConfirmation(data: FormData){
    return this.login.authenticatedRequest("/v1/emails/order-confirmation", "POST", true, undefined, data).pipe(
      map(response => null)
    );
  }






}
