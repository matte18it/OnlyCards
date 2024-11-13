import {Component, OnInit} from '@angular/core';
import {Title} from "@angular/platform-browser";
import {CaptchaService} from "../../service/login/captcha.service";
import {TimeService} from "../../service/time/time.service";
import {ReCaptchaV3Service} from "ng-recaptcha";
import {InputCheckService} from "../../service/utility/input-check.service";
import {LoginService} from "../../service/login/login.service";
import {Router} from "@angular/router";
import {NgxSpinnerService} from "ngx-spinner";
import {Location} from "@angular/common";
import {HandlerService} from "../../service/error-handler/handler.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})

export class LoginComponent implements OnInit {
  protected labelTime: string = "";

  // ------------- COSTRUTTORE ---------------
  constructor(private handlerService:HandlerService, private location:Location, private spinner:NgxSpinnerService, private router:Router, private loginService:LoginService, private recaptchaV3Service: ReCaptchaV3Service,  private captchaService: CaptchaService, private time: TimeService, private check : InputCheckService, private title: Title) {}

  // ------------- METODO DI INIZIALIZZAZIONE ---------------
  ngOnInit() {
    this.title.setTitle('OnlyCards | Accedi');
    this.labelTime = this.time.getLabel();
    if(this.loginService.isAuthenticated()){
      this.router.navigate(['/home']);
    }
  }

  // ------------- METODI ---------------
  protected send(): void {
    // prendo i valori inseriti dall'utente
    var password = (<HTMLInputElement>document.getElementById('passwordField')).value;
    var username = (<HTMLInputElement>document.getElementById('usernameField')).value;

    if(!username.includes('@')){
    // controllo che lo username sia corretto
    if(!this.check.checkUsername(username)){
      this.handlerService.throwGlobalError("L'username inserito non è valido. Si prega di inserire un username valido.");
      return;
    }}else {

    // controllo che l'email sia corretta
    if(!this.check.checkEmail(username)){
      this.handlerService.throwGlobalError("L'email inserita non è valida. Si prega di inserire un'email valida.");
      return;
    }}

    // controllo che la password sia corretta
    if(!this.check.checkPassword(password)){
      this.handlerService.throwGlobalError("La password inserita non è valida. Si prega di inserire una password valida.");
      return;
    }

    // controllo il captcha
    this.checkCaptcha();
  } //funzione per verificare i dati inseriti dall'utente
  protected checkCaptcha(): void {
    this.recaptchaV3Service.execute('verify').subscribe(token => {
      this.captchaService.verifyCaptcha(token).subscribe(response => {
        // Esegui l'accesso solo se la verifica del captcha ha avuto successo
        if (response.success) {
          this.login();
        } else {
         this.handlerService.throwGlobalError('Sembra che ci sia stato un problema con la verifica del tuo accesso. Si prega di riprovare più tardi o contattare l\'assistenza.');
        }
      });
    });
  } //funzione per verificare il captcha

  protected login(){
    this.spinner.show();
    var password = (<HTMLInputElement>document.getElementById('passwordField')).value;
    var username = (<HTMLInputElement>document.getElementById('usernameField')).value;
    this.loginService.login(username, password).subscribe(response => {



        this.handlerService.throwGlobalSuccess('Accesso effettuato con successo!').then(() => {
          if(this.loginService.isAuthenticated()){
            let canGoBack = document.referrer;
            if(canGoBack.includes("http://localhost:4200")){
              this.location.back();
            }else
              this.router.navigate(['/home']);
          }
        });
    } , error => {
      if(error.status==401){
        this.handlerService.throwGlobalWarning('Username o password errati. Si prega di riprovare.');
      }else {
        this.handlerService.throwGlobalError('Sembra che ci sia stato un problema con il server. Si prega di riprovare più tardi o contattare l\'assistenza.');}
      this.spinner.hide();
    }, () => {
      this.spinner.hide();
    });

  }  // funzione per effettuare l'accesso

  // metodi per aggiungere e rimuovere l'effetto bounce
  protected addBounce(component: string) {
    document.getElementById(component)?.classList.add('fa-bounce');
  }
  protected removeBounce(component: string) {
    document.getElementById(component)?.classList.remove('fa-bounce');
  }

  // metodo per visualizzare/nascondere la password
  protected viewPassword() {
    var component = document.getElementById('passwordField');
    var component2 = document.getElementById('passwordIcon');

    if (component?.getAttribute('type') == 'password') {
      component?.setAttribute('type', 'text');
      component2?.classList.remove('fa-eye');
      component2?.classList.add('fa-eye-slash');
    }
    else{
      component?.setAttribute('type', 'password');
      component2?.classList.remove('fa-eye-slash');
      component2?.classList.add('fa-eye');
    }
  }


  protected oauth2Login(provider: string) {
    this.loginService.oauth2Login(provider).then(() => {
      if(this.loginService.isAuthenticated())
        this.router.navigate(['/home']);
    });

  }
}
