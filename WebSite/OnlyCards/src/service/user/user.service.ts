import { Injectable } from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import { User, UserPublic } from '../../model/user';
import { environment } from '../../utility/environment';
import {LoginService} from "../login/login.service";
import {wishlist, WishlistEdit} from "../../model/wishlist";
import {Address} from "../../model/address";
import {Page} from "../../model/page";
import {Role} from "../../model/role";

@Injectable({
  providedIn: 'root'
})
export class UserService {



  private apiUserUrl = `${environment.backendUrl}/v1/users`; // URL per gli user

  constructor(private http: HttpClient, private login:LoginService) { }


  // Recupera tutti gli utenti
  getUsers(username?: string, email?: string, orderBy?:string, direction?:string,page?:number, size?:number): Observable<HttpResponse<Page<User>> | null> {
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
    if(username) {
      params = params.set('username', username);
    }
    if(email) {
      params = params.set('email', email);
    }

    return this.login.authenticatedRequest<HttpResponse<Page<User>>>("/v1/users", "GET", true, params);
  }

  // Recupera gli indirizzi dell'utente
  getUserAddresses(userId: string): Observable<Address[]> {
    return this.login.authenticatedRequest<HttpResponse<Address[]>>(`/v1/users/${userId}/addresses`, 'GET', true)
      .pipe(
        map((response: HttpResponse<Address[]> | null) => response?.body || [])  // Gestione di null e body
      );
  }

  getUserRoles(userId: string): Observable<Role[]> {
    return this.login.authenticatedRequest<HttpResponse<Role[]>>(`/v1/users/${userId}/roles`, 'GET', true)
      .pipe(
        map((response: HttpResponse<Role[]> | null) => response?.body || [])  // Gestione di null e body
      );
  }

  // Modifica un utente esistente
  updateUser(id: string, user: User): Observable<User | null> {
    return this.login.authenticatedRequest<User | null>(`/v1/users/${id}`, 'PATCH', true, undefined, user);
  }

  // Elimina un utente
  deleteUser(id: string): Observable<void | null> {
    return this.login.authenticatedRequest<void | null>(`/v1/users/${id}`, 'DELETE', true);
  }

  getUserWishlist(httpParams: HttpParams, loadUserId: string | null):Observable<Page<wishlist> | null> {
    return this.login.authenticatedRequest<Page<wishlist>  | null>(`/v1/users/${loadUserId}/wishlists`, 'GET', true, httpParams);

  }
  getPublicWishlistByUsername(username: string, httpParams: HttpParams):Observable<HttpResponse<Page<wishlist>> > {
    return this.http.get<Page<wishlist>>(`${environment.backendUrl}/v1/users/${username}/public-wishlists`, {observe: 'response', params: httpParams});
  }

  getUserInfo(id: string | null, userId: string | null): Observable<User | null> {
    return this.login.authenticatedRequest<HttpResponse<User | null>>(`/v1/users/single/${id}?userId=${userId}`, 'GET', true).pipe(
      map(response => response?.body || null)  // Se response.body è null, restituisce null
    );
  }

  updateProfile(user: User, userId: string | null): Observable<User | null> {
    return this.login.authenticatedRequest<HttpResponse<User | null>>(`/v1/users/${user.id}?userId=${userId}`, 'PUT', true, undefined, user).pipe(
      map(response => response?.body || null)
    );
  }

  updateAddress(address: Address, addressId: string | undefined, userId: string | undefined): Observable<Address | null> {
    return this.login.authenticatedRequest<HttpResponse<Address | null>>(`/v1/users/address/${addressId}?userId=${userId}`, 'PUT', true, undefined, address).pipe(
      map(response => response?.body || null)
    );
  }

  addAddress(address: Address, userId: string | undefined): Observable<Address | null> {
    return this.login.authenticatedRequest<HttpResponse<Address | null>>(`/v1/users/address/${userId}`, 'POST', true, undefined, address).pipe(
      map(response => response?.body || null)
    );
  }

  deleteAddress(addressId: string | undefined, userId: string | undefined): Observable<void | null> {
    return this.login.authenticatedRequest<void | null>(`/v1/users/address/${addressId}?userId=${userId}`, 'DELETE', true);
  }

  getUserPublicProfile(username: string):Observable<HttpResponse<UserPublic>>{
    return this.http.get<UserPublic>(`${environment.backendUrl}/v1/users/${username}`, {observe: 'response'});
  }
  getUserProducts(username: string,page: number, size: number):Observable<HttpResponse<Page<wishlist>>> {
    return this.http.get<Page<wishlist>>(`${environment.backendUrl}/v1/users/${username}/products`, {observe: 'response', params: new HttpParams().set('page', page.toString()).set('size', size.toString())});
  }
  getProfileImage(profileImage: string) :Observable<HttpResponse<String>>{
    return this.http.get<String>(profileImage, {observe: 'response'});
  }
  createNewWishlist(wishlist:WishlistEdit, userId:string) {
    return this.login.authenticatedRequest(`/v1/users/${userId}/wishlists`, 'POST', true,undefined, wishlist);


  }
}
