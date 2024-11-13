import {Component, OnInit} from '@angular/core';
import {EmailService} from "../../service/email/email.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {HandlerService} from "../../service/error-handler/handler.service";
import {Title} from "@angular/platform-browser";
import {NgxSpinnerService} from "ngx-spinner";
import {LoginService} from "../../service/login/login.service";

@Component({
  selector: 'app-help-form',
  templateUrl: './help-form.component.html',
  styleUrl: './help-form.component.css'
})

export class HelpFormComponent implements OnInit {
  // ----- ATTRIBUTI -----
  protected helpForm: FormGroup = new FormGroup({});

  // ----- COSTRUTTORE -----
  constructor(private emailService: EmailService, private formBuilder: FormBuilder, private handler: HandlerService, private title: Title, private spinner: NgxSpinnerService, private login: LoginService) {
    this.createHelpForm();
  }

  // ----- METODI -----
  async ngOnInit(): Promise<void> {
    await this.spinner.show();

    this.title.setTitle('OnlyCards | Richiesta di Aiuto');

    await this.spinner.hide();
  } // metodo di inizializzazione

  // Metodi per gestire il form
  protected createHelpForm() {
    this.helpForm = this.formBuilder.group({
      object: ['', [Validators.required]],
      description: ['', [Validators.required]]
    });
  } // Funzione per creare il form
  protected resetForm() {
    this.helpForm.reset();
  } // Funzione per resettare il form
  protected async sendHelpRequest() {
    await this.spinner.show();

    if(this.helpForm.valid) {
      const data = new FormData();
      data.append('object', this.helpForm.get('object')?.value);
      data.append('description', this.helpForm.get('description')?.value);
      data.append('userId', this.login.loadUserId()?.toString() || 'Anonimo');

      try {
        await new Promise<void>((resolve, reject) => {
          this.emailService.sendHelpRequest(data).subscribe({
            next: (response) => {
              this.handler.throwSuccessToast("Richiesta inviata con successo!");
              this.resetForm();

              resolve();
            },
            error: async (error) => reject(error)
          })
        });
      } catch (e) {
        this.handler.throwErrorToast("Errore nell'invio della richiesta, riprova più tardi!");
      }
    }

    await this.spinner.hide();
  }
}
