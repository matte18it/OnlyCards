import { Component, OnInit } from '@angular/core';
import { ReCaptchaV3Service } from "ng-recaptcha";
import { Title } from "@angular/platform-browser";
import { CaptchaService } from "../../service/login/captcha.service";
import { TimeService } from "../../service/time/time.service";
import { InputCheckService } from "../../service/utility/input-check.service";
import Swal from "sweetalert2";
import { LoginService } from "../../service/login/login.service";
import { NgxSpinnerService } from "ngx-spinner";
import { Router } from "@angular/router";
import { UserRegistration } from "../../model/user-registration";

@Component({
  selector: 'app-sign-up',
  templateUrl: './sign-up.component.html',
  styleUrls: ['./sign-up.component.css']
})
export class SignUpComponent implements OnInit {
  protected labelTime: string = "";

  constructor(private router: Router,
              private loading: NgxSpinnerService,
              private loginService: LoginService,
              private recaptchaV3Service: ReCaptchaV3Service,
              private title: Title,
              private captchaService: CaptchaService,
              private time: TimeService,
              private check: InputCheckService) {}

  ngOnInit() {
    this.title.setTitle("Only Cards | Login");
    this.labelTime = this.time.getLabel();
    if (this.loginService.isAuthenticated()) {
      this.router.navigate(['/']);
    }
  }

  protected send(): void {
    const email = (<HTMLInputElement>document.getElementById('emailField')).value;
    const password = (<HTMLInputElement>document.getElementById('passwordField')).value;
    const confirmPassword = (<HTMLInputElement>document.getElementById('passwordConfirmField')).value;
    const username = (<HTMLInputElement>document.getElementById('usernameField')).value;
    const cellphoneNumber = (<HTMLInputElement>document.getElementById('phoneField')).value;  // Usa `phoneField` come identificatore

    // Aggiungi questo log per verificare il valore di cellphoneNumber
    console.log("Captured cellphoneNumber: ", cellphoneNumber);

    if (!this.check.checkUsername(username)) {
      this.alertError('L\'username deve contenere solo caratteri alfanumerici e il trattino basso "_" e deve essere lungo tra i 3 e i 20 caratteri. Si prega di inserire un username valido.');
      return;
    }

    if (!this.check.checkEmail(email)) {
      this.alertError('L\'email inserita non è valida. Si prega di inserire un\'email valida.');
      return;
    }

    if (cellphoneNumber.length > 0 && !this.check.checkPhoneNumber(cellphoneNumber)) {
      this.alertError('Il numero di telefono inserito non è valido. Si prega di inserire un numero valido.');
      return;
    }

    if (!this.check.checkPassword(password)) {
      this.alertError('La password deve essere lunga almeno 8 caratteri e deve contenere almeno una lettera minuscola, una lettera maiuscola, un numero e un carattere speciale. Si prega di inserire una password valida.');
      return;
    }

    if (password !== confirmPassword) {
      this.alertError('Le password non coincidono. Si prega di confermare la stessa password.');
      return;
    }

    this.checkCaptcha();
    this.loading.show();

    // Crea l'oggetto usando il valore di cellphoneNumber
    const user: UserRegistration = new UserRegistration({
      email: email,
      password: password,
      username: username,
      cellphoneNumber: cellphoneNumber  // Aggiungi `cellphoneNumber` all'oggetto
    });

    // Debug per controllare l'oggetto prima di inviarlo al backend
    console.log("User object before sending to backend:", JSON.stringify(user));

    this.loginService.signUp(user).subscribe(response => {
      this.loading.hide();
      Swal.fire({
        icon: 'success',
        title: 'Registrazione effettuata',
        text: 'La registrazione è stata effettuata con successo. Ora puoi effettuare l\'accesso.',
        background: 'var(--alert-background)',
        confirmButtonColor: 'var(--accent-color)',
        color: 'var(--text-color)'
      });
      this.router.navigate(['/login']);
    }, error => {
      this.loading.hide();
      if (error.status === 409) {
        this.alertError('L\'email o lo username inseriti sono già in uso. Si prega di inserire un\'altra email o username.');
      } else {
        this.alertError('Si è verificato un errore durante la registrazione. Si prega di riprovare più tardi.');
      }
    });
  }


  private alertError(message: string): void {
    Swal.fire({
      icon: 'error',
      text: message,
      background: 'var(--alert-background)',
      confirmButtonColor: 'var(--accent-color)',
      color: 'var(--text-color)'
    });
  }

  protected checkCaptcha(): void {
    this.recaptchaV3Service.execute('verify').subscribe(token => {
      this.captchaService.verifyCaptcha(token).subscribe(response => {
        if (!response.success) {
          Swal.fire({
            icon: 'error',
            title: 'Errore',
            text: 'Sembra che ci sia stato un problema con la verifica del tuo accesso. Si prega di riprovare più tardi o contattare l\'assistenza.',
            background: 'var(--alert-background)',
            confirmButtonColor: 'var(--accent-color)',
            color: 'var(--text-color)'
          });
        }
      });
    });
  }

  protected addBounce(component: string) {
    document.getElementById(component)?.classList.add('fa-bounce');
  }

  protected removeBounce(component: string) {
    document.getElementById(component)?.classList.remove('fa-bounce');
  }

  protected viewPassword() {
    const component = document.getElementById('passwordField');
    const component2 = document.getElementById('passwordIcon');

    if (component?.getAttribute('type') == 'password') {
      component?.setAttribute('type', 'text');
      component2?.classList.remove('fa-eye');
      component2?.classList.add('fa-eye-slash');
    } else {
      component?.setAttribute('type', 'password');
      component2?.classList.remove('fa-eye-slash');
      component2?.classList.add('fa-eye');
    }
  }

  protected viewPasswordConfirm() {
    const confirmPasswordField = document.getElementById('passwordConfirmField') as HTMLInputElement;
    const confirmPasswordIcon = document.getElementById('passwordConfirmIcon');

    if (confirmPasswordField && confirmPasswordIcon) {
      if (confirmPasswordField.type === 'password') {
        confirmPasswordField.type = 'text';
        confirmPasswordIcon.classList.remove('fa-eye');
        confirmPasswordIcon.classList.add('fa-eye-slash');
      } else {
        confirmPasswordField.type = 'password';
        confirmPasswordIcon.classList.add('fa-eye');
        confirmPasswordIcon.classList.remove('fa-eye-slash');
      }
    }
  }

  oauth2login(provider: string) {
    return this.loginService.oauth2Login(provider).then(() => {
      if(this.loginService.isAuthenticated())
        this.router.navigate(['/home']);
    });
  }
}
