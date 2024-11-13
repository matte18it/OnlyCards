import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {CardWishlist, userWishlist, wishlist, WishlistEdit} from "../../model/wishlist";
import {Page} from "../../model/page";
import {HttpClient, HttpParams, HttpResponse} from "@angular/common/http";
import {environment} from "../../utility/environment";
import {LoginService} from "../login/login.service";

@Injectable({
  providedIn: 'root'
})
export class WishlistService {
  getPublicWishlist(username: string, name: string) {
    return this.http.get(environment.backendUrl+"/v1/users/"+username+"/public-wishlists/"+name, {observe: 'response'});
  }
  getProductsFromWishlistName(username: string, wishlistName: string, httpParams: HttpParams) :Observable<HttpResponse<Page<CardWishlist >>>{
    return this.http.get<Page<CardWishlist>>(environment.backendUrl+"/v1/users/"+username+"/public-wishlists/"+wishlistName+"/products", {observe: 'response', params: httpParams});
  }


  constructor(private http: HttpClient, private loginService:LoginService) { }

  getProductsFromWishlist(id: string, httpParams: HttpParams):Observable<HttpResponse<Page<CardWishlist | null>>|null> {
    return this.loginService.authenticatedRequest<HttpResponse<Page<CardWishlist | null>>>("/v1/wishlists/"+id+"/products", 'GET', true,httpParams, null);


  }



  deleteProductFromWishlist(wishlistId: string, cardId: string):Observable<HttpResponse<void>|null>{
    return this.loginService.authenticatedRequest("/v1/wishlists/"+wishlistId+"/products/"+cardId, 'DELETE', true,undefined, null);

  }

  addProductToWishlist(wishlistId: string, productId: string) {
    return this.loginService.authenticatedRequest("/v1/wishlists/"+wishlistId+"/products", 'POST', true,undefined,  {id: productId});

  }

  getWishlist(i: string) {
    return this.loginService.authenticatedRequest("/v1/wishlists/"+i, 'GET', true,undefined, null);

  }



  changeWishlistName(id: string, wishlist:WishlistEdit) {
    return this.loginService.authenticatedRequest("/v1/wishlists/"+id, 'PATCH', true,undefined, wishlist);

  }

  addUserToWishlist(id: string, username: any):Observable<HttpResponse<void>|null> {
    const data = {username: username};
    const account:userWishlist = new userWishlist(data);
    return this.loginService.authenticatedRequest("/v1/wishlists/"+id+"/users", 'POST',true, undefined, account);

  }

  deleteUserFromWishlist(idWishlist: string, idUser: string) {
    return this.loginService.authenticatedRequest("/v1/wishlists/"+idWishlist+"/users/"+idUser, 'DELETE',true, undefined, null);
  }

  deleteWishlist(id: string) {
    return this.loginService.authenticatedRequest("/v1/wishlists/"+id, 'DELETE', true,undefined, null);

  }

 

  generateToken(id: string):Observable<HttpResponse<void>|null>  {
    return this.loginService.authenticatedRequest("/v1/wishlists/"+id+"/token", 'POST', true,undefined, null);

  }

  deleteToken(id: string, token:string):Observable<HttpResponse<void>|null> {
    return this.loginService.authenticatedRequest("/v1/wishlists/"+id+"/token/"+token, 'DELETE', true,undefined, null);

  }

  getWishlistByToken(token: string) {
    return this.http.get(environment.backendUrl+"/v1/wishlists/token/"+token, {observe: 'response'});

  }


  getCardsWishlistByToken(httpParams: HttpParams, token: string) {
    return this.http.get(environment.backendUrl+"/v1/wishlists/token/"+token+"/products", {observe: 'response', params: httpParams});

  }
}
