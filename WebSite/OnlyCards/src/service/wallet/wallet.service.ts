import {Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {LoginService} from "../login/login.service";

@Injectable({
  providedIn: 'root'
})
export class WalletService {


  constructor(private http: HttpClient,
              private loginService: LoginService,
  ) {}

  getWallet(userID: string, page: number = 0, size: number = 10){
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.loginService.authenticatedRequest(`/v1/wallets/users/${userID}`,'GET',true,params,null);
  }

  rechargeWallet(userID: string, amount: number){
    const params = new HttpParams()
      .set('amount', amount.toString());
    return this.loginService.authenticatedRequest(`/v1/wallets/users/${userID}/recharge`, 'POST', true, params, null);
  }

  withdrawFromWallet(userID: string, amount: number){
    const params = new HttpParams()
      .set('amount', amount.toString());
    return this.loginService.authenticatedRequest(`/v1/wallets/users/${userID}/withdraw`,'POST',true, params,null);
  }



}
