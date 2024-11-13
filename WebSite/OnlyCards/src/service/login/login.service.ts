import { Injectable } from '@angular/core';
import {lastValueFrom, Observable, of, switchMap, tap, throwError} from "rxjs";
import {environment} from "../../utility/environment";
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams, HttpResponse} from "@angular/common/http";
import {LocalStorageService} from "../utility/local-storage.service";
import {catchError} from "rxjs/operators";
import {Router} from "@angular/router";
import {NgxSpinnerService} from "ngx-spinner";
import {HandlerService} from "../error-handler/handler.service";
import {UserRegistration} from "../../model/user-registration";
import {Location} from "@angular/common";

@Injectable({
  providedIn: 'root'
})
export class LoginService {
  constructor(private location:Location,private handler:HandlerService,private spinner:NgxSpinnerService,private http:HttpClient, private localStorage:LocalStorageService, private router:Router) { }

  login(username: string, password: string): Observable<HttpResponse<any>> {
    const headers = new HttpHeaders({
      'Authorization': 'Basic ' + btoa(username + ':' + password),
    });
    return this.http.post<HttpResponse<any>>(`${environment.backendUrl}/v1/auth/login`, {},  { headers:headers,  observe: 'response' } )
      .pipe(
        tap(response => {
          this.localStorage.removeAll();
          const jwt = response.headers.get('Authorization')?.replace('Bearer ', '');
          const refreshToken = response.headers.get('Refresh-Token');
          if (jwt && refreshToken) {
            this.localStorage.setItem('Access-Token', jwt);
            this.localStorage.setItem('Refresh-Token', refreshToken);
            const roles = this.getUserRoles();
            const userId = this.getUserId();
            if (roles && userId) {
              this.localStorage.setItem('userId', userId);
              this.localStorage.setItem('roles', roles);


                this.router.navigate(['/user/profile']); // Reindirizza alla pagina profilo o a quella di default

            }
          }
        })
      );
  }
  refreshToken(): Observable<HttpResponse<any > | null> {
    const refreshToken = this.localStorage.getItem('Refresh-Token');
    if (!refreshToken) {
      return of(null);
    }
    const headers = new HttpHeaders({
      'Refresh-Token': refreshToken
    });
    return this.http.get<HttpResponse<any>>(`${environment.backendUrl}/v1/auth/refresh-token`,   { headers:headers,  observe: 'response' } )
      .pipe(
        tap(response => {
          const jwt = response.headers.get('Authorization')?.replace('Bearer ', '');
          const refreshToken = response.headers.get('Refresh-Token');
          if (jwt && refreshToken) {
            this.localStorage.setItem('Access-Token', jwt);
            this.localStorage.setItem('Refresh-Token', refreshToken);
          }
        }),catchError((err:HttpErrorResponse) => {
          if(err.status === 401){
            this.localStorage.removeItem('Access-Token');
            this.localStorage.removeItem('Refresh-Token');
            this.localStorage.removeItem('userId');
            this.localStorage.removeItem('roles');
            this.router.navigate(['login']).then(() => {
              window.location.reload();
            });}
          else {
            this.handler.throwGlobalError("Sembra che ci sia stato un problema con il server. Si prega di riprovare più tardi o contattare l'assistenza.").then(() =>{
              this.router.navigate(['/home']);
            });
          }
          return of(null);


        })
      );

  }
  authenticatedRequest<T>(url: string, method: string, json:boolean, params?: HttpParams, body?: any): Observable<T | null> {
    const userId = this.localStorage.getItem('userId');
    if (!userId ) {
        this.router.navigate(['login']);
        return of(null);
    }
    if(userId){
    return this.makeAuthenticatedRequest<T>(url, method,json,  params, body).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && this.localStorage.getItem('Access-Token')) {
          return this.refreshToken().pipe(
            switchMap(() => this.makeAuthenticatedRequest<T>(url, method, json, params, body))
          );
        }else if (error.status === 401 && !this.localStorage.getItem('Access-Token')) {
            this.router.navigate(['login']);
            this.localStorage.removeItem('userId');
            this.localStorage.removeItem('roles');
            return of(null);
        }
        return throwError(error);
      })
    );}
    return of(null);
  }

  private makeAuthenticatedRequest<T>(
    url: string,
    method: string,
    json: boolean,
    params?: HttpParams,
    body?: any
  ): Observable<T> {
    const jwt = this.localStorage.getItem('Access-Token');
    let headers = new HttpHeaders({
      'Authorization': `Bearer ${jwt}`
    });

    // Se stai inviando JSON, imposta manualmente il Content-Type
    if (json && !(body instanceof FormData)) {
      headers = headers.set('Content-Type', 'application/json');
    }

    const options = {
      headers: headers,
      params: params,
      body: body,
      observe: 'response' as 'body'
    };

    if (!jwt) {
      return this.http.request<T>(method, environment.backendUrl + url, {
        params: params,
        body: body,
        observe: 'response' as 'body',
        withCredentials: true
      });
    }

    return this.http.request<T>(method, environment.backendUrl + url, options);
  }

  private getUserId():string | null {
      const jwt = this.localStorage.getItem('Access-Token');
      if (jwt) {
        const payload = jwt.split('.')[1];
        const decodedPayload = atob(payload);
        const payloadJson = JSON.parse(decodedPayload);
        return payloadJson.sub;
      }
      return null;
  }
  private  getUserRoles():string | null {
      const jwt = this.localStorage.getItem('Access-Token');
      if (jwt) {
        const payload = jwt.split('.')[1];
        const decodedPayload = atob(payload);
        const payloadJson = JSON.parse(decodedPayload);
        return payloadJson.roles;
      }

      return null;
  }


  logout():Observable<HttpResponse<any>> {
    const jwt = this.localStorage.getItem('Access-Token');
    const refreshToken = this.localStorage.getItem('Refresh-Token');
    const userId = this.localStorage.getItem('userId');

    if (jwt && refreshToken) {
      const headers = new HttpHeaders({
        'Authorization': `Bearer ${jwt}`,
        'Refresh-Token': refreshToken,
      });
      return this.http.post<HttpResponse<any>>(`${environment.backendUrl}/v1/auth/logout`, {}, { headers: headers, observe:"response" }).pipe(
        tap(response => {
          this.localStorage.removeAll();
          this.router.navigate(['/home-page']);
        },  error => {
          this.localStorage.removeAll();
          this.router.navigate(['/home-page']);


          return of(null);})
      );
    } else if (userId) {
      return this.http.post<HttpResponse<any>>(`${environment.backendUrl}/v1/auth/logout`, {}, { withCredentials: true, observe:"response"}).pipe(
        tap(response => {
          this.localStorage.removeAll();
          this.router.navigate(['/home-page']);
        },  error => {
          this.localStorage.removeAll();
          this.router.navigate(['/home-page']);

          return of(null);
        })
      );
   }
    throw new Error('Server error');
  }
  isOAuthUser(): boolean {
    const userId = this.localStorage.getItem('userId');
    const token = this.localStorage.getItem('Access-Token');
    return userId!=null && token==null;

  }

  async loginPopup(provider: string): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      // Apri il popup
      const popup = window.open(`${environment.backendUrl}/oauth2/authorization/${provider}`, '_blank', 'width=600,height=600');
      if (popup) {
        this.spinner.show();

        // Aggiungi l'ascoltatore
        const messageHandler = (event: MessageEvent) => {
          if (event.origin === environment.backendBaseUrl && event.data.event === 'userData') {
            const userId = event.data.userId;
            const roles = event.data.roles;

            if (userId && roles) {
              this.localStorage.removeAll();
              this.localStorage.setItem('userId', userId);
              this.localStorage.setItem('roles', roles);
            }
          }
        };

        window.addEventListener('message', messageHandler);

        // Intervallo per controllare se il popup è chiuso
        const checkInterval = setInterval(() => {
          if (popup.closed) {
            this.spinner.hide();
            clearInterval(checkInterval);

            // Rimuovi l'event listener quando il popup è chiuso
            window.removeEventListener('message', messageHandler);

            resolve();
          }
        }, 1000);
      } else {
        reject(new Error('Popup could not be opened.'));
      }
    });
  }

  userIsSellerOrBuyer() {
    const roles = this.loadRoles();
    if (roles) return roles.includes('SELLER') || roles.includes('BUYER');
    return false;
  }

  userIsSeller() {
    const roles = this.loadRoles();
    if (roles) return roles.includes('SELLER');
    return false;
  }

  userIsBuyer() {
    const roles = this.loadRoles();
    if (roles) return roles.includes('BUYER');
    return false;
  }

  userIsAdmin() {
    const roles = this.loadRoles();
    if (roles) return roles.includes('ADMIN');
    return false;
  }






  isAuthenticated() {
    return this.localStorage.getItem('userId') != null;
  }

  loadUserId() {
    return this.localStorage.getItem('userId');
  }
  loadRoles() {
    return this.localStorage.getItem('roles');
  }

  signUp(user: UserRegistration) {
    return this.http.post(`${environment.backendUrl}/v1/users`, user, {observe: 'response'});

  }
  oauth2Login(provider:string) {
    return this.loginPopup(provider);


  }

  userHasRole(role: string): boolean {
    const roles = this.loadRoles();
    if (roles) return roles.includes(role);
    return false;
  }



}

