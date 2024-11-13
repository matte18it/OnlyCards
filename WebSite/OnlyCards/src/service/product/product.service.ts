import { Injectable } from '@angular/core';
import {HttpClient, HttpResponse} from "@angular/common/http";
import {map, Observable} from "rxjs";
import {Product, ProductEdit} from "../../model/product";
import {environment} from "../../utility/environment";
import {Page} from "../../model/page";
import { LoginService } from '../login/login.service';
import { serialize } from 'object-to-formdata';

@Injectable({
  providedIn: 'root'
})

export class ProductService {
  deleteProduct(id: string) {
    return this.login.authenticatedRequest<HttpResponse<void>>(`/v1/products/${id}`, "DELETE", true, undefined, null);
  }
  deleteImage(productId: string, imageId: string) {
    return this.login.authenticatedRequest<HttpResponse<void>>(`/v1/products/${productId}/images/${imageId}`, "DELETE", true, undefined, null);
  }
  updateProduct(id: string, productEdit: ProductEdit) {
    const options = {
      indices: true,
      dotsForObjectNotation: true,
      nullsAsUndefineds: true
    }

    const data = serialize(productEdit, options);
    return this.login.authenticatedRequest<HttpResponse<void>>(`/v1/products/${id}`, "PATCH", false, undefined, data);

  }
  getCurrencies():Observable<HttpResponse<{ [key: string]: string }> | null> {
    return this.login.authenticatedRequest<HttpResponse<{ [key: string]: string }>>(`/v1/products/currencies`, 'GET', true, undefined, null);
  }
  constructor(private http : HttpClient, private login:LoginService) {}

  public getCardInfo(id: string, page: number): Observable<Page<Product>> {
    return this.http.get<Page<Product>>(`${environment.backendUrl}/v1/products/info/${id}?page=${page}`);
  }

  public saveProduct(userId: String, data: FormData): Observable<null> {
    return this.login.authenticatedRequest(`/v1/products/product/${userId}`, "POST", true, undefined, data).pipe(
      map(response =>  null)
    );
  }

  public getLastProducts(game: string): Observable<Product[]> {
    return this.http.get<Product[]>(`${environment.backendUrl}/v1/products/info/lastAdd/${game}`);
  }

  public getProductById(id: string | null, page: number): Observable<Product[]> {
    return this.login.authenticatedRequest<Product[]>(`/v1/products/productUser/${id}?page=${page}`, "GET", true, undefined, null).pipe(
      map((response: any) => {
        return response.body;
      })
    );
  }

  public countProducts(id: string | null) {
    return this.login.authenticatedRequest<number>(`/v1/products/productUser/total/${id}`, "GET", true, undefined, null).pipe(
      map((response: any) => {
        return response.body;
      })
    );
  }

  public getSingleProduct(productId: string, userId: string | null): Observable<ProductEdit> {
    return this.login.authenticatedRequest<ProductEdit>(`/v1/products/single/${productId}?userId=${userId}`, "GET", true, undefined, null).pipe(
      map((response: any) => {
        return response.body;
      })
    );
  }
}
