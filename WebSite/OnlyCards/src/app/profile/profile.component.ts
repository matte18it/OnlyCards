import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {HandlerService} from "../../service/error-handler/handler.service";
import {Title} from "@angular/platform-browser";
import {ActivatedRoute, Router} from "@angular/router";
import {NgxSpinnerService} from "ngx-spinner";
import {LocalStorageService} from "../../service/utility/local-storage.service";
import {User} from "../../model/user";
import {Address} from "../../model/address";
import {UserService} from "../../service/user/user.service";
import {FilesService} from "../../service/files/files.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CsvReaderService} from "../../service/utility/csv-reader.service";
import {environment} from "../../utility/environment";
import {ThemeService} from "../../service/theme/theme.service";
import {getListStatus, getStatusEnum, getStatusValue, Status} from "../../model/enum/statusEnum";
import {OrderService} from "../../service/order/order.service";
import {Order} from "../../model/order";
import {HttpParams} from "@angular/common/http";
import {LoginService} from "../../service/login/login.service";
import {Transaction} from "../../model/transaction";
import {WalletService} from "../../service/wallet/wallet.service";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})

export class ProfileComponent implements OnInit {
  // ----- Attributi -----
  @ViewChild('fileInput') fileInput!: ElementRef;
  protected readonly environment = environment;
  protected readonly getListStatus = getListStatus;
  protected readonly getStatusEnum = getStatusEnum;
  protected readonly Status = Status;
  protected readonly getStatusValue = getStatusValue;

  protected type: string = '';
  protected addressAction: string = '';
  private addressId: string | undefined = '';
  protected user: User | undefined;
  protected defaultAddress: Address | undefined;
  protected roles: string[] | undefined = [];
  protected urlImage: string = '';
  protected currentTime: string = '';
  private intervalId: any;
  protected states: string[] = [];
  protected cities: string[] = [];
  protected orders: Order[] = [];
  protected formProfile: FormGroup = new FormGroup({});
  protected formAddress: FormGroup = new FormGroup({});
  protected filter: FormGroup = new FormGroup({});
  protected theme: number = 0;
  protected page: number = 0;
  protected search: boolean = false;
  protected numberPage: number = 0;
  protected transactions: any = [];
  protected size: number = 10;
  protected totalPages: number = 0;
  protected totalTransactions: number = 0;
  protected wallet: any;
  protected amount: number = 0;
  protected rechargeModal: boolean = false;
  protected withdrawModal: boolean = false;

  // ----- Costruttore -----
  constructor(protected login: LoginService, private ordersService: OrderService, private csvReaderService: CsvReaderService, private formBuilder: FormBuilder, private handler: HandlerService, private title: Title, private route: ActivatedRoute, private spinner: NgxSpinnerService, private localStorage: LocalStorageService, private userService: UserService, private router: Router, private filesServicce: FilesService, private themeService: ThemeService, private walletService: WalletService) {
    if (this.intervalId) clearInterval(this.intervalId);

    this.theme = localStorage.getItem('theme') === 'light' ? 0 : 1;
    this.setTheme(localStorage.getItem('theme') || 'dark').then(r => r);
  }

  // ----- Inizializzazione -----
  async ngOnInit() {
    await this.spinner.show(); // mostro lo spinner
    if(this.localStorage.getItem('userId') != null){
      this.type = this.route.snapshot.params['type']; // prendo il tipo
      await this.loadData(); // carico i dati
    }
    else {
      await this.router.navigate(['/login']);
    }

    this.startClock();  // avvio il timer per aggiornare ogni secondo l'orario
    await this.spinner.hide(); // nascondo lo spinner
  }

  // ----- Metodi -----
  // Metodi per caricare i dati
  private async loadData() {
    switch(this.type) {
      case 'profile':
        this.title.setTitle('OnlyCards | Profilo');
        await this.loadUser();
        await this.loadImage();
        this.updateCurrentTime();
        break;
      case 'address':
        this.title.setTitle('OnlyCards | Indirizzi');
        await this.getState();
        await this.getCities();
        await this.loadUser();
        break;
      case 'orders':
        this.title.setTitle('OnlyCards | Ordini');
        await this.loadUser();
        this.createFilterForm();
        await this.loadOrders();
        break;
      case 'wallet':
        this.title.setTitle('OnlyCards | Portafoglio');
        await this.loadWallet();
        break;
      case 'transactions':
        this.title.setTitle('OnlyCards | Transazioni');
        await this.loadWallet();
        break;
      case 'settings':
        this.title.setTitle('OnlyCards | Impostazioni');
        break;
    }
  } // metodo per caricare i dati
  private async loadImage() {
    try {
      await new Promise<void>((resolve, reject) => {
        this.filesServicce.checkImage(localStorage.getItem('userId')).subscribe({
          next: (result) => {
            if(result)
              this.urlImage = result;

            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    }
    catch(error) {
      this.handler.throwErrorToast("Errore nel caricamento dell'immagine!");
    }
  } // metodo per caricare l'immagine
  private async loadUser() {
    // carico i ruoli dell'utente
    this.roles = this.localStorage.getItem('roles')?.split(',');

    // carico l'utente
    try {
      await new Promise<void>((resolve, reject) => {
        this.userService.getUserInfo(this.localStorage.getItem('userId'), this.localStorage.getItem('userId')).subscribe({
          next: (result) => {
            if(result) {
              this.user = result;

              if(this.type === 'profile')
                this.createProfileForm();
              else if(this.type === 'address')
                this.createAddressForm('null');
            }

            // Setto l'indirizzo di default
            if(this.user?.addresses && this.user?.addresses.length > 0) {
              this.defaultAddress = this.user?.addresses.find(address => address.defaultAddress);

              // Rimuovo l'indirizzo di default dalla lista
              this.user.addresses = this.user.addresses.filter(address => !address.defaultAddress);
            }

            //console.log(result);
            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    }
    catch(error) {
      this.handler.throwErrorToast("Errore nel caricamento dell'utente!");
    }
  } // metodo per caricare l'utente
  protected async loadOrders() {
    if(!this.filter.invalid) {
      await this.spinner.show();

      this.search = true;
      const filter: HttpParams = new HttpParams()
        .set('userId', this.user?.id || '')
        .set('productName', this.filter.get('productName')?.value || '')
        .set('status', this.filter.get('status')?.value || '')
        .set('type', this.filter.get('type')?.value || 'false')
        .set('date', this.filter.get('date')?.value || '')
        .set('minPrice', this.filter.get('minPrice')?.value || '0')
        .set('maxPrice', this.filter.get('maxPrice')?.value || '9999999')
        .set('page', this.page.toString());

      try {
        await new Promise<void>((resolve, reject) => {
          this.ordersService.getOrdersByFilters(filter).subscribe({
            next: (result) => {
              if(result) {
                this.numberPage = Math.ceil(result.totalNumber / 30);
                this.orders = result.ordersDto.content;
                // filtro le transazioni
                this.orders.forEach(order => {
                  order.transactions = order.transactions.filter(transaction => transaction.type === (filter.get('type') === 'true'));
                });
              }

              //console.log(this.orders);

              resolve();
            },
            error: async (error) => reject(error)
          });
        });
      } catch (error) {
        this.handler.throwErrorToast("Errore nel caricamento degli ordini!");
      }

      await this.spinner.hide();
    }
  } // metodo per caricare gli ordini

  // Metodi per aggiornare i dati
  protected async updateImage(event: any): Promise<void> {
    const sendFile = event.target.files[0];
    if (sendFile) {
      const reader = new FileReader();
      reader.onload = () => {
        this.urlImage = reader.result as string;
      };
      reader.readAsDataURL(sendFile);

      // invio il file
      const formData = new FormData();
      formData.append('file', sendFile);
      formData.append('userId', localStorage.getItem('userId') || '');

      try {
        await new Promise<void>((resolve, reject) => {
          this.filesServicce.uploadImage(formData).subscribe({
            next: (result) => {
              if (result)
                this.urlImage = result;

              resolve();
            },
            error: async (error) => reject(error)
          });
        });
      }
      catch(error) {
        this.handler.throwErrorToast("Errore nell'aggiornamento dell'immagine!");
      }
    }
  } // metodo per cambiare il file
  private updateCurrentTime() {
    const now = new Date();
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    const timezone = Intl.DateTimeFormat().resolvedOptions().timeZone;

    this.currentTime = `${hours}:${minutes} (${timezone})`;
  } // metodo per aggiornare l'orario
  protected async updateProfile() {
    if (!this.formProfile.invalid) {
      if (this.user) {
        this.user.username = this.formProfile.get('username')?.value;
        this.user.email = this.formProfile.get('email')?.value;
        this.user.cellphoneNumber = this.formProfile.get('phoneNumber')?.value;
      }

      try {
        await new Promise<void>((resolve, reject) => {
          if (this.user) {
            this.userService.updateProfile(this.user, this.localStorage.getItem("userId")).subscribe({
              next: (result) => {
                if (result) {
                  this.user = result;
                  this.createProfileForm();

                  // Simula un clic sul pulsante nascosto per chiudere il modal
                  const closeBtn = document.getElementById('closeProfileModalButton');
                  if (closeBtn) {
                    closeBtn.click();
                  }

                  this.handler.throwSuccessToast("Dati aggiornati correttamente!");

                  resolve();
                }
              },
              error: async (error) => reject(error)
            });
          }
        });
      } catch (error) {
        this.handler.throwErrorToast("Errore nell'aggiornamento del profilo!");
      }
    }
  } // metodo per aggiornare il profilo
  protected async updateAddress() {
    if (!this.formAddress.invalid) {
      // Prendo i valori dal form
      const address: Address = {
        name: this.formAddress.get('name')?.value,
        surname: this.formAddress.get('surname')?.value,
        telephoneNumber: this.formAddress.get('phoneNumber')?.value,
        street: this.formAddress.get('street')?.value,
        zip: this.formAddress.get('zip')?.value,
        city: this.formAddress.get('city')?.value,
        state: this.formAddress.get('state')?.value,
        defaultAddress: this.formAddress.get('defaultAddress')?.value,
        weekendDelivery: this.formAddress.get('weekendDelivery')?.value
      };

      try {
        await new Promise<void>((resolve, reject) => {
          this.userService.updateAddress(address, this.addressId, this.user?.id).subscribe({
            next: (result) => {
              resolve();
            },
            error: async (error) => reject(error)
          });
        });
      } catch (error) {
        // Mostra il toast di errore
        this.handler.throwErrorToast("Errore nell'aggiornamento dell'indirizzo");
      } finally {
        // Ricarica l'intera pagina
        window.location.reload();
      }
    }
  } // metodo per aggiornare l'indirizzo
  protected async addAddress() {
    if(!this.formAddress.invalid) {
      // Prendo i valori dal form
      const address: Address = {
        name: this.formAddress.get('name')?.value,
        surname: this.formAddress.get('surname')?.value,
        telephoneNumber: this.formAddress.get('phoneNumber')?.value,
        street: this.formAddress.get('street')?.value,
        zip: this.formAddress.get('zip')?.value,
        city: this.formAddress.get('city')?.value,
        state: this.formAddress.get('state')?.value,
        defaultAddress: this.formAddress.get('defaultAddress')?.value,
        weekendDelivery: this.formAddress.get('weekendDelivery')?.value
      };

      try {
        await new Promise<void>((resolve, reject) => {
          this.userService.addAddress(address, this.user?.id).subscribe({
            next: (result) => {
              resolve();
            },
            error: async (error) => reject(error)
          });
        });
      } catch (error) {
        // Mostra il toast di errore
        this.handler.throwErrorToast("Errore nell'aggiunta dell'indirizzo");
      } finally {
        // Ricarica l'intera pagina
        window.location.reload();
      }
    }
  } // metodo per aggiungere un indirizzo
  protected async deleteAddress(id: string | undefined): Promise<void> {
    if(id != undefined) {
      try {
        await new Promise<void>((resolve, reject) => {
          this.userService.deleteAddress(id, this.user?.id).subscribe({
            next: (result) => {
              resolve();
            },
            error: async (error) => reject(error)
          });
        });
      } catch (error) {
        // Mostra il toast di errore
        this.handler.throwErrorToast("Errore nell'eliminazione dell'indirizzo");
      } finally {
        // Ricarica l'intera pagina
        window.location.reload();
      }
    }
  } // metodo per eliminare un indirizzo

  // Metodi di gestione dei form
  private createProfileForm() {
    this.formProfile = this.formBuilder.group({
      username: [this.user?.username, [Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-Z0-9._]+$')]],
      email: [this.user?.email, [Validators.required, Validators.email]],
      phoneNumber: [this.user?.cellphoneNumber, [Validators.required, Validators.pattern('^[0-9]+$')]],
    });
  } // metodo per creare il form del profilo
  protected createAddressForm(action: string, address?: Address) {
    this.addressAction = action === 'modify' ? 'Modifica Indirizzo' : 'Aggiungi Indirizzo';

    if(action === 'modify')
      this.addressId = address?.id;

    this.formAddress = this.formBuilder.group({
      name: [action === 'modify' ? address?.name : '', [Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-Z]+$')]],
      surname: [action === 'modify' ? address?.surname : '', [Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-Z]+$')]],
      phoneNumber: [action === 'modify' ? address?.telephoneNumber : '', [Validators.required, Validators.pattern('^[0-9]+$')]],
      street: [action === 'modify' ? address?.street : '', [Validators.required, Validators.minLength(2)]],
      zip: [action === 'modify' ? address?.zip : '', [Validators.required, Validators.pattern('^[0-9]{5}$')]],
      city: [action === 'modify' ? address?.city : '', [Validators.required, Validators.pattern(`^(${this.cities.join('|')})$`)]],
      state: [action === 'modify' ? address?.state : '', [Validators.required, Validators.pattern(`^(${this.states.join('|')})$`)]],
      defaultAddress: [action === 'modify' ? address?.defaultAddress : false, [Validators.required]],
      weekendDelivery: [action === 'modify' ? address?.weekendDelivery : false, [Validators.required]]
    });
  } // metodo per creare il form dell'indirizzo
  protected createFilterForm() {
    this.filter = this.formBuilder.group({
      productName: ['', [Validators.minLength(2), Validators.pattern('^[a-zA-Z0-9 ]+$')]],
      status: [''],
      type: ['false'],
      date: [''],
      minPrice: ['', [Validators.pattern('^[0-9]+(\\.[0-9]+)?$')]],
      maxPrice: ['', [Validators.pattern('^[0-9]+(\\.[0-9]+)?$')]]
    });
  } // metodo per creare il form del filtro
  protected cancelEdit() {
    this.formProfile.reset({
      username: this.user?.username || '',
      email: this.user?.email || '',
      phoneNumber: this.user?.cellphoneNumber || ''
    });
  } // metodo per annullare la modifica del form del profilo
  private async getState(): Promise<void> {
    await new Promise<void>(resolve => {
      this.csvReaderService.loadCSVFile('assets/csv/nations.csv').subscribe((data) => {
        this.states = this.csvReaderService.parseCSV(data, 'Descrizione');
        resolve();
      });
    });
  } // metodo per ottenere gli stati del mondo
  private async getCities(): Promise<void> {
    await new Promise<void>(resolve => {
      this.csvReaderService.loadCSVFile('assets/csv/cities.csv').subscribe((data) => {
        this.cities = this.csvReaderService.parseCSV(data, 'Nome');
        resolve();
      });
    });
  } // metodo per ottenere le città del mondo

  // Metodi utilitari
  private startClock() {
    this.updateCurrentTime(); // aggiorna l'orario subito
    this.intervalId = setInterval(() => this.updateCurrentTime(), 1000); // aggiorna ogni secondo
  } // metodo per avviare l'orologio
  protected async setTheme(newTheme: string) {
    if(newTheme != localStorage.getItem('theme'))
      if(newTheme === 'light'){
        this.theme = 0;
        document.documentElement.setAttribute('data-theme', newTheme);
        this.themeService.applyTheme(newTheme);
        await this.handler.throwSuccessToast("Solgaleo usa Astrocarica e attiva la Light Mode!");
      }
      else{
        this.theme = 1;
        document.documentElement.setAttribute('data-theme', newTheme);
        this.themeService.applyTheme(newTheme);
        await this.handler.throwSuccessToast("Lunala usa Raggio d'Ombra e attiva la Dark Mode!");
      }
  } // metodo per impostare il tema
  protected getToday(): string {
    return new Date().toISOString().split('T')[0];
  } // metodo per ottenere la data di oggi
  protected async shipOrder(id: string) {
    await this.spinner.show();

    try {
      await new Promise<void>((resolve, reject) => {
        this.ordersService.changeStatusOrder(id, Status.SHIPPED, this.user?.id).subscribe({
          next: (result) => {
            // Lancio il toast di successo
            this.handler.throwSuccessToast("Ordine spedito con successo!");

            // Aggiorno la lista degli ordini
            this.loadOrders();

            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    }
    catch(error) {
      this.handler.throwErrorToast("Errore nell'aggiornamento dell'ordine!");
    }

    await this.spinner.hide();
  } // metodo per spedire un ordine
  protected async deleteOrder(id: string) {
    await this.spinner.show();

    try {
      await new Promise<void>((resolve, reject) => {
        this.ordersService.changeStatusOrder(id,Status.CANCELLED, this.user?.id).subscribe({
          next: (result) => {
            // Lancio il toast di successo
            this.handler.throwSuccessToast("Ordine annullato con successo!");

            // Aggiorno la lista degli ordini
            this.loadOrders();

            resolve();
          },
          error: async (error) => reject(error)
        });
      });
    }
    catch(error) {
      this.handler.throwErrorToast("Errore nell'eliminazione dell'ordine!");
    }

    await this.spinner.hide();
  } // metodo per eliminare un ordine
  protected async goToPage(newPage: number) {
    if (newPage >= 0 && newPage < this.numberPage) {
      this.page = newPage;
      await this.loadOrders();
    }
  } // Metodo per andare alla pagina selezionata
  protected getTotalTransactionValue(transactions: Transaction[]): number {
    return transactions.reduce((acc, transaction) => acc + transaction.value.amount, 0);
  } // metodo per ottenere il valore totale delle transazioni

  // Metodi per il portafoglio
  protected async loadWallet(page: number = 0){
    let userID: string | null = this.login.loadUserId();
    if (!userID) {
      console.error("User ID not found");
      return;
    }
    try {
      this.spinner.show();
      const data: any = await this.walletService.getWallet(userID,page,this.size).toPromise();
      this.wallet = data.body;
      this.transactions = this.wallet.transactions;
      this.totalPages = this.wallet.totalPages;
      this.totalTransactions = this.wallet.totalTransactions;
    } catch (err) {
      console.error(err);
    } finally {
      this.spinner.hide();
    }
  } // metodo per caricare il portafoglio
  protected openRechargeModal(){
    this.withdrawModal = false;
    this.rechargeModal = true;
  } // metodo per aprire il modal di ricarica
  protected openWithdrawModal(){
    this.rechargeModal = false;
    this.withdrawModal = true;
  } // metodo per aprire il modal di prelievo
  protected closeModal(){
    this.rechargeModal = false;
    this.withdrawModal = false;
  } // metodo per chiudere il modal
  protected rechargeWallet(){
    let userID: string | null = this.login.loadUserId();
    try {
      this.spinner.show();
      if(userID){
        this.walletService.rechargeWallet(userID,this.amount).subscribe(() => {
          this.loadWallet();
          this.closeModal();
          this.handler.throwSuccessToast("Portafoglio ricaricato con successo!");
        });
      }
      this.amount = 0;
    } catch (err) {
      console.error(err);
    } finally {
      this.spinner.hide();
    }
  } // metodo per ricaricare il portafoglio
  protected withdrawFromWallet() {
    let userID: string | null = this.login.loadUserId();
    try {
      this.spinner.show();
      if(userID){
        this.walletService.withdrawFromWallet(userID,this.amount).subscribe(() => {
          this.loadWallet();
          this.closeModal();
          this.handler.throwSuccessToast("Prelievo dal portafoglio avvenuto con successo!");
        });
      }
      this.amount = 0;
    } catch (err) {
      console.error(err);
    } finally {
      this.spinner.hide();
    }
  } // metodo per prelevare dal portafoglio
  protected onPageChange(page: number): void {
    if (page >= 0 && page < this.totalPages){
      this.page = page;
      this.loadWallet(page);
    }
  } // metodo per cambiare pagina

  protected redirectToPublicProfile(username:string){
    this.router.navigate(['/users/'+username]);
  }
}
