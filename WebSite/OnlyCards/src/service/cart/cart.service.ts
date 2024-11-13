import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Product} from "../../model/product";
import {forkJoin, Observable, of, throwError} from "rxjs";
import {LoginService} from "../login/login.service";
import {environment} from "../../utility/environment";
import {catchError} from "rxjs/operators";
import {NgxSpinnerService} from "ngx-spinner";

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private productsID: string[] = [];


  constructor(private http: HttpClient,
              private loginService: LoginService,
  ) {}

  getCartCards(userID: string | null):Observable<any> {
    return this.loginService.authenticatedRequest(`/v1/carts/users/${userID}/products`,'GET',true,undefined,null);
  }

  addProduct(userID: string | null, productID: string | null){
    return this.loginService.authenticatedRequest<Product[]>(`/v1/carts/users/${userID}/add-products/${productID}`,'POST',true, undefined, {productId: productID});
  }

  removeProduct(userID: string | null, productID: string | null){
    return this.loginService.authenticatedRequest<Product[]>(`/v1/carts/users/${userID}/remove-products/${productID}`,'POST',true,undefined, productID);
  }

  emptyCart(userID: string | null, productsID: string[]){
    if (!userID || productsID.length === 0) {
      return of(null); // Restituisce un observable vuoto se non ci sono prodotti o userID
    }

    const removeObservables = productsID.map(productID => {
      return this.removeProduct(userID, productID);
    });

    // Utilizziamo `forkJoin` per eseguire tutte le rimozioni in parallelo e attendere il completamento di tutte
    return forkJoin(removeObservables).pipe(
      catchError(err => {
        console.error(err);
        return throwError(err);
      })
    );
  }

  getProduct(productID: string | null){
    return this.http.get( `http://localhost:8080/api/v1/products/info/single/${productID}`);
    //return this.loginService.authenticatedRequest("/v1/products/"+productID,'GET',true,undefined,null);
  }

  setProductsID(ids: string[]){
    this.productsID = ids;
  }

  getProductsID(){
    return this.productsID;
  }







}
