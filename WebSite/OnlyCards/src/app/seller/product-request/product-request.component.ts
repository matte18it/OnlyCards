import {Component, OnInit} from '@angular/core';
import {GameService} from "../../../service/game/game.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {HandlerService} from "../../../service/error-handler/handler.service";
import {Title} from "@angular/platform-browser";
import {NgxSpinnerService} from "ngx-spinner";
import {EmailService} from "../../../service/email/email.service";
import {LoginService} from "../../../service/login/login.service";

@Component({
  selector: 'app-product-request',
  templateUrl: './product-request.component.html',
  styleUrl: './product-request.component.css'
})

export class ProductRequestComponent implements OnInit{
  // ----- ATTRIBUTI -----
  protected readonly Array = Array;
  protected readonly Object = Object;

  protected games: Map<string, string> = new Map<string, string>();
  protected gamesName: string[] = [];
  protected formRequest: FormGroup = new FormGroup({});
  protected imagePreview: string | ArrayBuffer | null = null;
  private sendFile: any;

  // ----- COSTRUTTORE -----
  constructor(private emailService: EmailService, private gameService: GameService, private formBuilder: FormBuilder, private handler: HandlerService, private title: Title, private spinner: NgxSpinnerService, private login: LoginService) {}

  // ----- METODI -----
  async ngOnInit(): Promise<void> {
    await this.spinner.show();

    this.title.setTitle('OnlyCards | Richiesta Prodotto');
    await this.getGames();
    this.createRequestForm();

    await this.spinner.hide();
  } // metodo di inizializzazione

  // Metodi per ottenere i dati
  protected async getGames() {
    try {
      await new Promise<void>((resolve, reject) => {
        this.gameService.games.subscribe({
          next: (response) => {
            if (response.body) {
              this.games = response.body;
              this.gamesName = Object.values(this.games);

              //console.log(this.games);
            }
            resolve();
          },
          error: async (error) => reject(error)
        })
      });
    } catch (e) {
      this.handler.throwErrorToast("Errore nel caricamento dei giochi, riprova più tardi!");
    }
  } // Funzione per prendere i giochi

  // Metodo per creare il form
  protected createRequestForm() {
    this.formRequest = this.formBuilder.group({
      gameType: ['', Validators.required],
      productName: ['', Validators.required],
      stateDescription: [''],
      imageProduct: ['', Validators.required],
    });
  } // Metodo per creare il form
  protected resetForm() {
    this.formRequest.get('gameType')?.setValue('');
    this.formRequest.get('productName')?.reset();
    this.formRequest.get('stateDescription')?.reset();
    this.formRequest.get('imageProduct')?.reset();
    this.imagePreview = null;
  } // Metodo per resettare il form

  // Metodo per gestire le immagini
  protected onImageSelected(event: any): void {
    this.sendFile = event.target.files[0];
    const input = event.target as HTMLInputElement;

    if (input.files && input.files[0]) {
      const file = input.files[0];
      const reader = new FileReader();

      reader.onload = () => {
        this.imagePreview = reader.result;
      };

      reader.readAsDataURL(file);
    }
  } // Metodo per gestire le immagini
  protected async sendRequest(): Promise<void> {
    await this.spinner.show();

    try {
      await new Promise<void>((resolve, reject) => {
        const data = new FormData();
        data.append('game', this.formRequest.get('gameType')?.value);
        data.append('name', this.formRequest.get('productName')?.value);
        data.append('message', this.formRequest.get('stateDescription')?.value);
        data.append('image', this.sendFile);
        data.append('id', this.login.loadUserId() || '');

        this.emailService.requestProduct(data).subscribe({
          next: () => {
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

    await this.spinner.hide();
  } // Metodo per inviare la richiesta
}
