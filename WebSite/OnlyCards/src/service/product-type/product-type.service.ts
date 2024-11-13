import { Injectable } from '@angular/core';
import {map, Observable} from "rxjs";
import {Product} from "../../model/product";
import {environment} from "../../utility/environment";
import {HttpClient, HttpParams, HttpResponse} from "@angular/common/http";
import {Page} from "../../model/page";
import {FeatureSearch} from "../../model/feature";
import {LoginService} from "../login/login.service";
import {ProductType, ProductTypeRegistration} from "../../model/productType";
import { serialize } from 'object-to-formdata';
import {AdvancedSearchProductType} from "../../model/advancedSearchProductType";

@Injectable({
  providedIn: 'root'
})
export class ProductTypeService {

  constructor(private http: HttpClient, private login: LoginService) {}

  public getProductType(id: string): Observable<ProductType> {
    return this.http.get<ProductType>(`${environment.backendUrl}/v1/product-types/single/${id}`);
  }

  public getTopSeller(type: string | null): Observable<ProductType[]> {
    return this.http.get<ProductType[]>(`${environment.backendUrl}/v1/product-types/top-seller/${type}`);
  }

  public getBestPurchases(type: string | null): Observable<ProductType[]> {
    return this.http.get<ProductType[]>(`${environment.backendUrl}/v1/product-types/best-purchases/${type}`);
  }
  getSortingOptions():Observable<HttpResponse<Map<string,string>>> {
    return this.http.get<Map<string, string>>(`${environment.backendUrl}/v1/product-types/sorting-options`, {observe:'response'});

  }

  getLanguages():Observable<HttpResponse<Map<string,string>>> {
    return this.http.get<Map<string, string>>(`${environment.backendUrl}/v1/product-types/languages`, {observe:'response'});

  }
  getTypes():Observable<HttpResponse<Map<string,string>>> {
    return this.http.get<Map<string, string>>(`${environment.backendUrl}/v1/product-types/types`, {observe:'response'});
  }

  getProductTypes(game:string, params:HttpParams):Observable<Page<Product>> {
    return this.http.get<Page<Product>>(`${environment.backendUrl}/v1/product-types/${game}/products`, {params: params});
  }

  getProductTypeFeatures(game:string):Observable<FeatureSearch[]> {
    return this.http.get<FeatureSearch[]>(`${environment.backendUrl}/v1/product-types/${game}/features`);
  }

  saveProductType(productType: ProductTypeRegistration){
    const options = {
      indices: true,
      dotsForObjectNotation: true,
      nullsAsUndefineds: true
    }

    const data = serialize(productType, options);
    return this.login.authenticatedRequest("/v1/product-types", "POST", false, undefined, data).pipe(
      map((response: any) => {
        return response.body;
      })
    );
  }
  modifyProductType(id: string ,productType: ProductTypeRegistration) {
    const options = {
      indices: true,
      dotsForObjectNotation: true,
      nullsAsUndefineds: true
    }

    const data = serialize(productType, options);
    return this.login.authenticatedRequest<HttpResponse<ProductType>>(`/v1/product-types/${id}`, "PUT", true, undefined, data);
  }

  getProductTypesSeller(type: string, userId: string | null, page: number): Observable<ProductType[]> {
    return this.login.authenticatedRequest(`/v1/product-types/all/${type}?userId=${userId}&page=${page}`, "GET", true, undefined).pipe(
      map((response: any) => {
        return response.body;
      })
    );
  }

  deleteProductType(id: string) {
    return this.login.authenticatedRequest<HttpResponse<any>>(`/v1/product-types/${id}`, "DELETE", true, undefined, undefined);
  }

  getCardFromSet(setName: string, game: string, page: number) {
    return this.http.get<Page<ProductType>>(`${environment.backendUrl}/v1/product-types/set/${setName}?game=${game}&page=${page}&size=20`);
  }

  getNumberOfCardFromSet(setName: string, game: string) {
    return this.http.get<number>(`${environment.backendUrl}/v1/product-types/set/number/${setName}?game=${game}`);
  }

  getAdvancedSearch(name: string, game: string, userId: string | null): Observable<AdvancedSearchProductType[]> {
    return this.login.authenticatedRequest<HttpResponse<AdvancedSearchProductType[]>>(`/v1/product-types/advanced-search/${userId}?gameType=${game}&name=${name}`, "GET", true, undefined).pipe(
      map((response: any) => {
        return response.body;
      })
    );
  }

  saveAdvancedSearch(userId: string | null, cardId: string, gameType: string) {
    return this.login.authenticatedRequest<HttpResponse<AdvancedSearchProductType>>(`/v1/product-types/save/${gameType}/${userId}`, "POST", true, undefined, cardId).pipe(
      map((response: any) => {
        return response.body;
      })
    );
  }
}
