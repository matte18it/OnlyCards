import { Component, OnInit } from '@angular/core';
import { UserService } from '../../service/user/user.service';
import { NgxSpinnerService } from 'ngx-spinner';
import { ActivatedRoute, Router } from '@angular/router';
import { HandlerService } from '../../service/error-handler/handler.service';
import { UserPublic } from '../../model/user';
import { Product, ProductInfo } from '../../model/product';
import { Page } from '../../model/page';
import { LoginService } from '../../service/login/login.service';
import { CartService } from '../../service/cart/cart.service';

@Component({
  selector: 'app-public-profile',
  templateUrl: './public-profile.component.html',
  styleUrl: './public-profile.component.css'
})
export class PublicProfileComponent implements OnInit {
  constructor(private userService:UserService,  private spinner:NgxSpinnerService, private activeRoute: ActivatedRoute, private router:Router, private handler:HandlerService, private login:LoginService, private cart:CartService) { }
  username: string = '';
  page: number = 0;
  currentPagination: number = 1;
  size: number = 10;
  products: Page<ProductInfo> = {
    content: [] as ProductInfo[],  // Inizializziamo un array vuoto di ProductInfo
    pageable: {
      pageNumber: 0,
      pageSize: 0,
      sort: {
        sorted: false,
        unsorted: true,
        empty: true
      },
      offset: 0,
      paged: false,
      unpaged: true
    },
    totalPages: 0,
    totalElements: 0,
    last: false,
    numberOfElements: 0,
    size: 0,
    number: 0,
    sort: {
      sorted: false,
      unsorted: true,
      empty: true
    },
    first: false,
    empty: true
  };
  
  userInfo:UserPublic = {
    username: '',
    profileImage: ''
  };
  ngOnInit(): void {
    this.username=this.activeRoute.snapshot.paramMap.get('username') || '';
    if(this.username.trim() == ''){
      this.router.navigate(['/home-page']);
    }
    this.spinner.show();
    this.userService.getUserPublicProfile(this.username).subscribe((data:any)=>{
      this.spinner.hide();
      this.userInfo = data.body;
      this.getProducts();
      this.getProfileImage(this.userInfo.profileImage);
    },(error:any)=>{
      this.spinner.hide();
      if(error.status == 404){
        this.handler.throwGlobalWarning("Utente non trovato").then(()=>{
          this.router.navigate(['/home-page']);
        });
        return;
      }
      this.handler.throwGlobalError("Errore durante il caricamento del profilo pubblico di "+this.username).then(()=>{
        this.router.navigate(['/home-page']);
      });
    });
  }
  getProducts() {
   this.spinner.show();
    this.userService.getUserProducts(this.username, this.page, this.size).subscribe((data:any)=>{
      this.spinner.hide();
      this.products = data.body;
    },(error:any)=>{
      this.spinner.hide();
      this.handler.throwErrorToast("Errore durante il caricamento dei prodotti dell'utente "+this.username);
    });
  }
  getProfileImage(profileImage: string) {
    this.spinner.show();
   this.userService.getProfileImage(profileImage).subscribe((data:any)=>{
      this.userInfo.profileImage = data.body.url;
      this.spinner.hide();
    },(error:any)=>{
      this.spinner.hide();
      this.handler.throwErrorToast("Errore durante il caricamento dell'immagine profilo");
    });
  }
  addToCart(id: string) {
    if(!this.login.isAuthenticated()){
      this.handler.throwGlobalWarning("Devi essere loggato per poter aggiungere carte al carrello");
      return;
    }
    
    this.spinner.show();
    const userId = this.login.loadUserId();
    this.cart.addProduct(userId, id).subscribe((response: any) => {
      this.spinner.hide();
      this.handler.throwSuccessToast("Carta aggiunta al carrello");
    }, error => {
      this.spinner.hide();
      this.handler.throwErrorToast("Errore durante l'aggiunta al carrello. Riprova più tardi.");
    });
  
  }
  updatePage() {
    this.page = this.currentPagination - 1;
    this.getProducts();
  }

}
