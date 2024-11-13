import {Component, inject, Input, OnInit, TemplateRef} from '@angular/core';
import {NgbActiveModal, NgbCollapse, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {userWishlist, wishlist, WishlistEdit} from "../../../model/wishlist";
import {LoginService} from "../../../service/login/login.service";
import {Router} from "@angular/router";
import {WishlistService} from "../../../service/wishlist/wishlist.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {HandlerService} from "../../../service/error-handler/handler.service";
import {error} from "@angular/compiler-cli/src/transformers/util";
import {HttpErrorResponse} from "@angular/common/http";
import {NgxSpinnerService} from "ngx-spinner";

@Component({
  selector: 'app-wishlist-sharing-options',
  templateUrl: './wishlist-sharing-options.component.html',
  styleUrl: './wishlist-sharing-options.component.css'
})
export class WishlistSharingOptionsComponent implements OnInit{

  protected nameForm:FormGroup;
  protected newAccountForm:FormGroup;
  constructor(private spinner:NgxSpinnerService, private modal:NgbModal, private handler:HandlerService, private fb:FormBuilder, private wishlistService:WishlistService, protected login:LoginService, private router: Router) {
    this.nameForm = this.fb.group({
      name: ["", Validators.compose([Validators.required, Validators.pattern('^[a-zA-Z0-9][a-zA-Z0-9 ]*$'), Validators.minLength(3)])],
      isPublic : [false, Validators.required]
    });
    this.newAccountForm = this.fb.group({
      username: ["", Validators.compose([Validators.required, Validators.minLength(3), Validators.maxLength(20),  Validators.pattern('^[a-zA-Z0-9_]+( [a-zA-Z0-9_]+)*$')])]
    });
  }
  activeModal = inject(NgbActiveModal);
  @Input() wishlist!: wishlist;
  @Input() username!: string;
  isCollapsed: boolean=true;

  isOwner() {
    return this.wishlist.accounts.filter(value => value.id === this.login.loadUserId() && value.keyOwner === "owner").length > 0 && this.username=='';
  }

  goToAccountPage(id: string) {
    this.activeModal.close();
    this.router.navigate(['/users', id]);

  }

  ngOnInit(): void {
    this.nameForm.get('name')?.setValue(this.wishlist.name);
    this.nameForm.get('isPublic')?.setValue(this.wishlist.isPublic);

  }


  changeWishlist() {
    this.spinner.show();
    let name = undefined;
    let isPublic = undefined;
    if(this.nameForm.get('name')?.value!=this.wishlist.name)
      name = this.nameForm.get('name')?.value;
    if(this.nameForm.get('isPublic')?.value!=this.wishlist.isPublic)
      isPublic = this.nameForm.get('isPublic')?.value;
    let wishlist = new WishlistEdit(name, isPublic);
    if (name !== undefined || isPublic !== undefined) {
    this.wishlistService.changeWishlistName(this.wishlist.id, wishlist).subscribe(value => {
        this.spinner.hide();
      this.handler.throwSuccessToast("La wishlist è stata modificata con successo");
        this.activeModal.close();
        window.location.reload();


    }, error => {
        this.spinner.hide();
      if(error.status === 401)
        return;
      if(error.status === 409){
        this.handler.throwGlobalWarning("Hai già una wishlist con questo nome");
        return;
      }

      this.handler.throwGlobalError("Errore durante il cambio del nome della wishlist").then(value => {
            this.activeModal.close();
          }
        )
    }
    );
  }else{
      this.spinner.hide();
      this.handler.throwGlobalWarning("Nessuna modifica effettuata");}
  }

  addANewUser(content:TemplateRef<any>) {
    this.newAccountForm.get('username')?.reset();
    this.modal.open(content, {ariaLabelledBy: 'modal-add-new-user'}).result.then((result) => {
      if(result=="Add"){
      if(this.newAccountForm.get('username')?.valid){
        this.spinner.show();
        const username = this.newAccountForm.get('username')?.value;
        this.newAccountForm.get('username')?.reset();
        this.wishlistService.addUserToWishlist(this.wishlist.id, username).subscribe(value => {
          this.handler.throwSuccessToast("Utente aggiunto con successo");
            this.getWishlist();
        },  (error:HttpErrorResponse)=>{
          this.spinner.hide();
          if(error.status === 401)
            return;
          if(error.status === 409){
            this.handler.throwGlobalWarning("Utente già presente nella wishlist");
            return;
          }
          if(error.status === 404){
            this.handler.throwGlobalWarning("Utente non trovato");
            return;
          }
          if(error.status === 422){
            this.handler.throwGlobalWarning("Limite massimo di utenti con cui condividere la wishlist raggiunto");
            return;
          }

          this.spinner.hide();
          this.handler.throwGlobalError("Errore durante l'aggiunta dell'utente, riprova più tardi").then(value => {
            this.activeModal.close();
          })


        });
      }}

      });


  }

  private getWishlist() {
    this.spinner.show();
    this.wishlistService.getWishlist(this.wishlist.id).subscribe((response: any) => {
        this.wishlist.accounts = response.body.accounts.map((account: any) => new userWishlist(account));
      this.spinner.hide();
      },error => {
      this.spinner.hide();
      if(error.status === 401)
        return;


          this.handler.throwGlobalError("Errore durante il caricamento della wishlist. Riprova più tardi.").then(value => {
            this.activeModal.close();
          })


      }
    );

  }

  deleteUser(id: string) {
    this.spinner.show();
    this.wishlistService.deleteUserFromWishlist(this.wishlist.id, id).subscribe(value => {
      this.handler.throwSuccessToast("Utente rimosso con successo");
      this.getWishlist();
    }, (error:HttpErrorResponse)=>{
      this.spinner.hide();
      if(error.status === 401)
        return;
      if(error.status === 404){
        this.handler.throwGlobalWarning("Utente non trovato").then(value => {
          this.activeModal.close();
          window.location.reload();
        })
        return;
      }
      this.handler.throwGlobalError("Errore durante la rimozione dell'utente, riprova più tardi").then(value => {
        this.activeModal.close();
      })
    });

  }

  deleteWishlist(content:TemplateRef<any>) {
    this.modal.open(content, {ariaLabelledBy: 'modal-delete-user'}).result.then((result) => {
        if(result=="Delete"){
          this.wishlistService.deleteWishlist(this.wishlist.id).subscribe(value => {
            this.handler.throwSuccessToast("Wishlist eliminata con successo");
            this.activeModal.close();
            window.location.reload();
          }, (error:HttpErrorResponse)=>{
            if(error.status === 401)
              return;
            if(error.status === 404){
              this.handler.throwGlobalWarning("Wishlist non trovata").then(value => {
                this.activeModal.close();
                window.location.reload();
              })
              return;
            }
            this.handler.throwGlobalError("Errore durante l'eliminazione della wishlist, riprova più tardi").then(value => {
              this.activeModal.close();

            })
          });
        }
      });

  }

  errorNameForm(error: string) {
    return this.nameForm.get('name')?.hasError(error) && this.nameForm.get('name')?.get('name')?.value!='';
  }

  errorUsernameForm(error: string) {
    return this.newAccountForm.get('username')?.hasError(error) && this.newAccountForm.get('username')?.touched;
  }
  errorAllUsernameForm() {
    return this.newAccountForm.get('username')?.invalid && this.newAccountForm.get('username')?.touched;
  }

  errorAllNameForm() {
    return this.nameForm.get('name')?.invalid && this.nameForm.get('name')?.touched;
  }




    copyUrl() {
      let url = "http://localhost:4200/whishlists/"+this.wishlist.token;
      navigator.clipboard.writeText(url).then(value => {
        this.handler.throwSuccessToast("Link copiato negli appunti");
      }, error => {
        this.handler.throwGlobalError("Errore durante la copia del link").then(value => {
          this.activeModal.close();
        })
      })
    }


  showUrl(collapse: NgbCollapse) {
    if(this.wishlist.token){
      collapse.toggle();
      return;
    }
    this.spinner.show();
    this.wishlistService.generateToken(this.wishlist.id).subscribe((response:any) => {
      this.wishlist.token = response.body.token;
      this.spinner.hide();
      collapse.toggle();
    }, error => {
      this.spinner.hide();
      if(error.status === 401)
        return;
      this.handler.throwGlobalError("Errore durante la generazione del link, riprova più tardi");

    });


  }

  deleteLink() {
    this.spinner.show();
    if(!this.wishlist.token)
      return;
    this.wishlistService.deleteToken(this.wishlist.id, this.wishlist.token).subscribe(value => {
        this.isCollapsed = true;
      this.handler.throwSuccessToast("Link rimosso con successo");
      this.wishlist.token = undefined;
      this.spinner.hide();
    }, error => {
      this.spinner.hide();
      if(error.status === 401)
        return;
      this.handler.throwGlobalError("Errore durante la rimozione del link, riprova più tardi");
    });

  }

}

