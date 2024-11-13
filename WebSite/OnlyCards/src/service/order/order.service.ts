import { Injectable } from '@angular/core';
import {map, Observable, of} from 'rxjs';
import { Order, OrderDetails, OrderInfo, OrderEdit } from '../../model/order';
import { environment } from '../../utility/environment';
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { catchError } from 'rxjs/operators';
import {LoginService} from "../login/login.service";
import {Page} from "../../model/page";

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private apiUrl = `${environment.backendUrl}/v1/orders`;

  constructor(private http: HttpClient, private login: LoginService) { }

  // Recupera tutti gli ordini
  getOrders( buyer?:string, seller?:string, orderBy?:string, direction?:string,page?:number, size?:number): Observable<HttpResponse<Page<OrderInfo>> | null> {
    let params = new HttpParams();
    if(orderBy) {
      params = params.set('order-by', orderBy);
    }
    if(direction) {
      params = params.set('direction', direction);
    }
    if(page) {
      params = params.set('page', page.toString());
    }
    if(size) {
      params = params.set('size', size.toString());
    }
    if(buyer) {
      params = params.set('buyer', buyer);
    }
    if(seller) {
      params = params.set('seller', seller);
    }

    return this.login.authenticatedRequest(`/v1/orders`, 'GET', true, params);
  }

  // Recupera un singolo ordine per ID
  getOrderById(id: string):Observable<HttpResponse<OrderDetails> | null> {
    return this.login.authenticatedRequest(`/v1/orders/${id}`, 'GET', true);
  }

  // Aggiungi un nuovo ordine
  addOrder(order: Order): Observable<Order> {
    return this.http.post<Order>(this.apiUrl, order)
      .pipe(
        catchError(this.handleError<Order>('addOrder'))
      );
  }

  // Aggiorna un ordine esistente
  updateOrder(id: string, orderEdit:OrderEdit):Observable<HttpResponse<void> | null>  {
    return this.login.authenticatedRequest(`/v1/orders/${id}`, 'PATCH', true, undefined, orderEdit);
  }

  // Gestione degli errori
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(`${operation} failed: ${error.message}`);
      return of(result as T);
    };
  }

  // Recupera gli ordini con filtri
  getOrdersByFilters(data: HttpParams) {
    return this.login.authenticatedRequest(`/v1/orders/`, 'GET', true, data).pipe(
      map((response: any) => {
        return response.body;
      })
    );
  }


  createOrder(userID: string | null, productsID: string[]){
    return this.login.authenticatedRequest(`/v1/orders/users/${userID}`,'POST',true, undefined, productsID);
  }

  changeStatusOrder(orderId: string, status: string, userId: string | undefined){
    return this.login.authenticatedRequest(`/v1/orders/status/${orderId}`,'PATCH',true, undefined, {"status": status, "userId": userId});
  }
}
