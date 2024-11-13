import { Injectable } from '@angular/core';
import {HttpClient, HttpResponse} from "@angular/common/http";
import {environment} from "../../utility/environment";
import {Observable} from "rxjs";
import {LocalStorageService} from "../utility/local-storage.service";

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private giochiMapvalue: Map<string,string> = new Map<string, string>();

  constructor(private http:HttpClient, private local:LocalStorageService) {}

  get games(): Observable<HttpResponse<Map<string, string>>> {
    return this.http.get<Map<string, string>>(`${environment.backendUrl}/v1/product-types/games`, {
      responseType: 'json',
      observe: 'response'
    });
  }






}
