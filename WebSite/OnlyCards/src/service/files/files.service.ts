import { Injectable } from '@angular/core';
import {HttpResponse} from "@angular/common/http";
import {map, Observable} from "rxjs";
import {LoginService} from "../login/login.service";

@Injectable({
  providedIn: 'root'
})

export class FilesService {
  constructor(private login: LoginService) {}

  checkImage(userId: string | null): Observable<string | null> {
    return this.login.authenticatedRequest<HttpResponse<{url: string} | null>>(`/v1/files/${userId}`, "GET", true).pipe(
      map(response => response?.body?.url || null)
    );
  }

  uploadImage(data: FormData): Observable<string | null> {
    return this.login.authenticatedRequest<HttpResponse<string | null>>(`/v1/files`, "POST", true, undefined, data).pipe(
      map(response => response?.body || null)
    );
  }
}
