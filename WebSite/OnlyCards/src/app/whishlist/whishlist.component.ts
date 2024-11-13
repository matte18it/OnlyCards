import { Component, OnInit, TemplateRef} from '@angular/core';
import {LoginService} from "../../service/login/login.service";
import {ActivatedRoute, Router} from "@angular/router";
import {CardWishlist, userWishlist, wishlist, WishlistEdit, WishlistFilter} from "../../model/wishlist";
import {HttpParams} from "@angular/common/http";
import {environment} from "../../utility/environment";
import {UserService} from "../../service/user/user.service";
import {HandlerService} from "../../service/error-handler/handler.service";
import {NgxSpinnerService} from "ngx-spinner";
import {WishlistService} from "../../service/wishlist/wishlist.service";
import {ProductService} from "../../service/product/product.service";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import { NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {WishlistSharingOptionsComponent} from "./wishlist-sharing-options/wishlist-sharing-options.component";
import {ProductTypeService} from "../../service/product-type/product-type.service";
import { Title } from '@angular/platform-browser';
import { CartService } from '../../service/cart/cart.service';
import { Page } from '../../model/page';

@Component({
  selector: 'app-whishlist',
  templateUrl: './whishlist.component.html',
  styleUrl: './whishlist.component.css'
})

export class WhishlistComponent implements OnInit {
  protected whishlists: Page<wishlist> = {
    content: [],  // inizialmente vuoto
    pageable: {
      pageNumber: 0,
      pageSize: 10,
      sort: {
        sorted: false,
        unsorted: true,
        empty: true
      },
      offset: 0,
      paged: true,
      unpaged: false
    },
    totalPages: 0,
    totalElements: 0,
    last: false,
    numberOfElements: 0,
    size: 10,
    number: 0,
    sort: {
      sorted: false,
      unsorted: true,
      empty: true
    },
    first: true,
    empty: true
  };
  protected searchForm: FormGroup;
  protected currentPage = 0;
  protected currentPageWislist = 0;
  protected pageSize = 0;
  protected selectedWishlist: number = 0;
  protected noContent = true;
  protected sortingOptions: Map<string, string> = new Map<string, string>();
  protected loadParams: boolean = false;
  protected selectedSortingOption: string = '-';
  protected sort = 'new';
  protected noContentCurrentWishlist: boolean = true;
  collectionSize: number = 0;
  protected currentPagination: number = 1;
  protected currentPaginationWishlist: number = 1;
  protected token: string = '';
  protected username: string = '';
  private cardSearch: string = '';

  constructor(private title:Title, private productTypeService:ProductTypeService, private activeRoute:ActivatedRoute, private modal: NgbModal, private fb: FormBuilder, private handler: HandlerService, private cardService: ProductService, private wishlistService: WishlistService, private spinner: NgxSpinnerService, private error: HandlerService, private login: LoginService, private router: Router, private userService: UserService, private cartService:CartService) {

    this.searchForm = this.fb.group({
      cardSearch: ['', Validators.compose([ Validators.pattern('^[a-zA-Z0-9 ]*$')])],
      ownerSearch: ['', Validators.compose([ Validators.pattern('^[a-zA-Z0-9]*$')])]
    });
    this.newWishlistForm = this.fb.group({
      name: ['', Validators.compose([Validators.required, Validators.minLength(3), Validators.maxLength(30), Validators.pattern('^[a-zA-Z0-9 ]*$')])],
      isPublic : [false]
    });

    this.searchForm.get('cardSearch')?.valueChanges.subscribe(value => {
      if (this.searchForm.get('cardSearch')?.valid && this.searchForm.get('cardSearch')?.value.trim() != this.cardSearch){
        this.cardSearch = value;
        this.makeSearch();
      }


    });
    this.searchForm.get('ownerSearch')?.valueChanges.subscribe(ignored => {
      if (this.searchForm.get('ownerSearch')?.valid)
        this.makeSearch();

    });


  }


  async ngOnInit(): Promise<void> {
    this.token=this.activeRoute.snapshot.paramMap.get('token') || '';
    this.username=this.activeRoute.snapshot.paramMap.get('username') || '';
    console.log(this.username);
    if (!this.loadParams) {
      await this.loadSortingOptions();
      this.loadParams = true;
    }
    this.pageSize = environment.getSizeParameter() / 2;
    const httpParams = new HttpParams().set('sort', 'new');
    if(this.isCapabilty())
      this.getWishlistByToken();
    else if(this.username == ''){
      if(this.login.loadRoles()?.includes('ROLE_ADMIN') ){ //admin cannot have wishlist
        this.router.navigate(['/home-page']);
      }
      this.getWishlists(httpParams);

    }else{
      this.getPublicWishlists(httpParams);

    }



  }
  isCapabilty(){
    return this.token != '' && this.username == '';
  }

  changeSelectedWishlist(i: number) {
    this.selectedWishlist = i;
    this.currentPagination = 1;
    this.title.setTitle("Only Cards | "+this.whishlists.content[i].name);
    this.currentPage = 0;
    if(this.isCapabilty()){
      this.getCardsWishlistByToken();
      return;
    }
    if(this.username == ''){
    this.getWishlist(this.whishlists.content[i].id);
    this.getCardsWishlist(this.whishlists.content[i].id);
    }
    else{
      this.getPublicWishlist(this.whishlists.content[i].name);
      this.getCardsPublicWishlist(this.whishlists.content[i].name);
    }
  }

  changeSortingWishlists(sort: string) {
    this.currentPaginationWishlist =1;
    this.currentPageWislist = 0;
    this.sort = sort;
    if(this.username == ''){
    if (this.isCreator == 'all')
      this.getWishlists(new HttpParams().set('sort', sort));
    else
      this.getWishlists(new HttpParams().set('sort', sort).set('is-owner', this.isCreator.toString()));
    }else{
      
        this.getPublicWishlists(new HttpParams().set('sort', sort));

    }
  }

  private getCardsWishlist(id: string) {
    let httpParams = this.getParamsForCards();
    this.spinner.show();
    this.whishlists.content[this.selectedWishlist].cards = [];
    this.wishlistService.getProductsFromWishlist(id, httpParams).subscribe((response: any) => {
      if (response.body.empty == true) {
        this.noContentCurrentWishlist = true;
      }

      for (let i = 0; i < response.body.numberOfElements; i++) {
        this.whishlists.content[this.selectedWishlist].cards.push(new CardWishlist(response.body.content[i]));

      }
      if (this.currentPagination == 1)
        this.collectionSize = response.body.totalElements;

      this.currentPage = response.body.pageable.pageNumber;


      this.currentPagination = this.currentPage + 1;

    }, error => {
      this.spinner.hide();
      if(error.status == 401)
        return;

     if(error.status==403){
        this.error.throwGlobalWarning("Non hai i permessi per visualizzare questa wishlist");
        window.location.reload();
      }
        this.error.throwErrorToast("Errore durante il caricamento della wishlist. Riprova più tardi.");
        this.router.navigate(['/']);
    }, () => {
      this.spinner.hide();
    });
  }

  private getWishlists(httpParams: HttpParams) {
    this.spinner.show();
    this.whishlists.content = [];
    this.currentPage = 0;
    this.currentPagination = 1;
    this.collectionSize = 0;
    httpParams = httpParams.set('page', this.currentPageWislist.toString()).set('size', '10');
    this.userService.getUserWishlist(httpParams, this.login.loadUserId()).subscribe((response: any) => {
      if (response.status == 204) {
        this.noContent = true;
        return;
      }
      if (response.body) {
        this.noContent = false;
        this.selectedWishlist = 0;
        this.whishlists= response.body;
        if(this.whishlists.content.length>0){
            this.title.setTitle("Only Cards | "+this.whishlists.content[0].name);
            this.getCardsWishlist(this.whishlists.content[0].id);
            this.getWishlist(this.whishlists.content[0].id);
        }
      } else {
        return;
      }
    }, error => {
      this.spinner.hide();
      if(error.status == 401)
        return;
      if(error.status==403){
        this.error.throwGlobalWarning("Non hai i permessi per visualizzare le wishlist").then(() => {
          this.router.navigate(['/']);
        });
        return;
      }
      this.error.throwGlobalError("Errore durante il caricamento delle wishlist. Riprova più tardi.");
      this.router.navigate(['/']);
    }, () => {
      this.spinner.hide();
    });

  }

  async loadSortingOptions() {
    try {
      const response = await this.productTypeService.getSortingOptions().toPromise();
      if (response && response.body) {
        this.sortingOptions = response.body;
      }
    } catch (error) {
      this.handler.throwGlobalError("Errore durante il caricamento della pagina. Riprova più tardi.").then(() => {
        this.router.navigate(['/']);
      })
    }

  }


  getSortingOptionValues() {
    return Array.from(Object.values(this.sortingOptions));
  }

  cambiaOrdine(sort: string) {
    this.selectedSortingOption = sort;
    this.currentPage = 0;
    if(this.isCapabilty())
      this.getCardsWishlistByToken();
    else if(this.username == ''){
      this.getCardsWishlist(this.whishlists.content[this.selectedWishlist].id);
    }else{
      this.getCardsPublicWishlist(this.whishlists.content[this.selectedWishlist].name);
    }

  }

  makeSearch() {
  if(this.isCapabilty())
    this.getCardsWishlistByToken();
  else if(this.username == ''){
    this.getCardsWishlist(this.whishlists.content[this.selectedWishlist].id);}
  else{
    this.getCardsPublicWishlist(this.whishlists.content[this.selectedWishlist].name);
  }


  }

  getCurrentWishlistName() {
    if (this.whishlists.content[this.selectedWishlist])
      return this.whishlists.content[this.selectedWishlist].name;
    return 'Loading...';
  }


  updatePage() {
    this.currentPage = this.currentPagination - 1;
    if(this.isCapabilty())
      this.getCardsWishlistByToken();
    else if(this.username == ''){
      this.getCardsWishlist(this.whishlists.content[this.selectedWishlist].id);
    }
    else{
      this.getCardsPublicWishlist(this.whishlists.content[this.selectedWishlist].name);
    }
  }
  updatePageWishlist() {
    this.currentPageWislist = this.currentPaginationWishlist - 1;
    if(this.username == ''){
    this.getWishlists(new HttpParams().set('sort', this.sort));
    }else{
      this.getPublicWishlists(new HttpParams().set('sort', this.sort));
    }
  }





  async deleteCardFromWishlist(id: string) {
    this.spinner.show();
    this.wishlistService.deleteProductFromWishlist(this.whishlists.content[this.selectedWishlist].id, id).subscribe((ignored: any) => {
      this.currentPage = 0;
      this.collectionSize = 0;
      this.currentPagination = 1;
      this.getCardsWishlist(this.whishlists.content[this.selectedWishlist].id);
      this.spinner.hide();
        this.handler.throwSuccessToast("Carta rimossa dalla wishlist");

    }, error => {
      this.spinner.hide();
      if(error.status == 401)
        return;
      if (error.status == 403 ){
        this.error.throwGlobalWarning("Non hai i permessi per rimuovere questa carta dalla wishlist");
        window.location.reload();
        return;
      }
      this.error.throwGlobalError("Errore durante la rimozione della carta dalla wishlist. Riprova più tardi.");



    });

  }

  isOwner(i: number) {
    if(this.username!=''){
      return false;
    }
    if (this.whishlists.content[i])
      return this.whishlists.content[i].accounts.filter(value => value.id === this.login.loadUserId() && value.keyOwner === "owner").length > 0;
    return false;
  }


  getCurrentWishlistCards() {
    if (this.whishlists.content[this.selectedWishlist])
      return this.whishlists.content[this.selectedWishlist].cards;
    return [];
  }

  protected readonly wishlist = wishlist;
  isCreator: WishlistFilter = 'all';
  newWishlistForm: FormGroup;

  getCurrentWishlistLastUpdateDate() {
    if (this.whishlists.content[this.selectedWishlist])
      return environment.formatDate(this.whishlists.content[this.selectedWishlist].lastUpdate);
    return environment.formatDate(new Date());
  }

  isPublic() {
   
    return this.whishlists.content[this.selectedWishlist].isPublic==true ;
  }
  getToken(){
    if(this.whishlists.content[this.selectedWishlist])
     return this.whishlists.content[this.selectedWishlist].token;
    return undefined;
  }

  getNumberOfUsers() {
    if (this.whishlists.content[this.selectedWishlist])
      return this.whishlists.content[this.selectedWishlist].accounts.filter(value => value.keyOwner !== "owner").length;
    return 0;
  }

  openSharingOptions() {
    const modalRef = this.modal.open(WishlistSharingOptionsComponent, {
      centered: true,
      size: 'lg',
      windowClass: 'modal-holder'
    });
    modalRef.componentInstance.wishlist = this.whishlists.content[this.selectedWishlist];
    modalRef.componentInstance.username= this.username;
  }

  private getWishlist(i: string) {
    this.wishlistService.getWishlist(i).subscribe((response: any) => {
        this.whishlists.content[this.selectedWishlist].lastUpdate = new Date(response.body.lastUpdate);
      this.whishlists.content[this.selectedWishlist].token = response.body.token;
      this.whishlists.content[this.selectedWishlist].isPublic = response.body.isPublic;
        this.whishlists.content[this.selectedWishlist].accounts = response.body.accounts.map((account: any) => new userWishlist(account));
      }, error => {
      if(error.status == 401)
        return;
      if(error.status==403){
        this.error.throwGlobalWarning("Non hai i permessi per visualizzare questa wishlist");
        window.location.reload();
        return;
      }
      if(error.status==404){
        this.error.throwGlobalWarning("Wishlist non trovata");
        window.location.reload();
        return;
      }

          this.error.throwErrorToast("Errore durante il caricamento della wishlist. Riprova più tardi.");
          this.router.navigate(['/']);

      }
    );


  }


  changeFilterOption(true1: string) {
    this.isCreator = true1 as WishlistFilter;
    this.whishlists.content = [];
    this.currentPageWislist = 0;
    this.currentPaginationWishlist = 1;
    if(this.username == ''){
    if (this.isCreator !== 'all')
      this.getWishlists(new HttpParams().set('sort', this.sort).set('is-owner', this.isCreator.toString()));
    else
      this.getWishlists(new HttpParams().set('sort', this.sort));
    }
    else{
      this.getPublicWishlists(new HttpParams().set('sort', this.sort));
    }
  }

  createNewWishlist(content: TemplateRef<any>) {
    this.modal.open(content, {ariaLabelledBy: 'modal-add-wishlist'}).result.then((result) => {
      if (result === 'Add') {
        if (this.newWishlistForm.get("name")?.valid) {
          let wishlist:WishlistEdit = new WishlistEdit(this.newWishlistForm.get("name")?.value, this.newWishlistForm.get("isPublic")?.value);
          const userId = this.login.loadUserId();
          if(!userId)
            return;
          this.userService.createNewWishlist(wishlist, userId).subscribe(() => {
            this.handler.throwSuccessToast("Wishlist creata con successo");
            window.location.reload();

          }, error => {
            if(error.status == 401)
              return;
            if(error.status==409){
              this.error.throwGlobalWarning("Wishlist con lo stesso nome già esistente");
              return;}
            if(error.status==422){
              this.error.throwGlobalWarning("Hai raggiunto il numero massimo di wishlist, cancellane una per poterne creare un'altra");
              return;}
            this.error.throwGlobalError("Errore durante la creazione della wishlist. Riprova più tardi.");





          });
        }
      }
    });


  }

  showError(form: FormGroup, cardSearch: string) {
    return form.get(cardSearch)?.value != '' && this.searchForm.get(cardSearch)?.invalid;

  }

  errorAllNewWishlist() {
    return this.newWishlistForm.get('name')?.invalid && this.newWishlistForm.get('name')?.touched;

  }

  errorNewWishlist(error: string) {
    return this.newWishlistForm.get('name')?.hasError(error) &&  this.newWishlistForm.get('name')?.touched;


  }

  getCurrentWishlistCardsSize() {
    if(this.whishlists.content[this.selectedWishlist])
      return this.whishlists.content[this.selectedWishlist].cards.length;
    else return 1;
  }

  isPublicAndToken() {
    return this.isPublic() && this.getToken();
  }
  private  getCardsWishlistByName(username:string, wishlistName:string) {
    let httpParams = this.getParamsForCards();
    this.spinner.show();
    this.whishlists.content[this.selectedWishlist].cards = [];
    this.wishlistService.getProductsFromWishlistName(username, wishlistName,  httpParams).subscribe((response: any) => {
      if (response.body.empty == true) {
        this.noContentCurrentWishlist = true;
      }

      for (let i = 0; i < response.body.numberOfElements; i++) {
        this.whishlists.content[this.selectedWishlist].cards.push(new CardWishlist(response.body.content[i]));

      }
      if (this.currentPagination == 1)
        this.collectionSize = response.body.totalElements;

      this.currentPage = response.body.pageable.pageNumber;


      this.currentPagination = this.currentPage + 1;

    }, error => {
      this.spinner.hide();
      if(error.status == 401)
        return;

     if(error.status==403){
        this.error.throwGlobalWarning("Non hai i permessi per visualizzare questa wishlist");
        window.location.reload();
      }
        this.error.throwErrorToast("Errore durante il caricamento della wishlist. Riprova più tardi.");
        this.router.navigate(['/']);
    }, () => {
      this.spinner.hide();
    });
  }
  private getPublicWishlists(httpParams: HttpParams) {
    this.spinner.show();
    this.whishlists.content = [];
    this.currentPage = 0;
    this.currentPagination = 1;
    this.collectionSize = 0;
    httpParams = httpParams.set('page', this.currentPageWislist.toString()).set('size', '10');
    this.userService.getPublicWishlistByUsername(this.username, httpParams).subscribe((response: any) => {
      if (response.status == 204) {
        this.noContent = true;
        return;
      }
      if (response.body) {
        this.noContent = false;
        this.selectedWishlist = 0;
        this.whishlists= response.body;
        
            this.selectedWishlist = 0;
            if(this.whishlists.content.length>0){
            this.title.setTitle("Only Cards | "+this.whishlists.content[0].name);
            this.getCardsPublicWishlist(this.whishlists.content[0].name);
            this.getPublicWishlist(this.whishlists.content[0].name);
            }
      } else {
        return;
      }
    }, error => {
      this.spinner.hide();
      if(error.status == 401)
        return;
      if(error.status==403){
        this.error.throwGlobalWarning("Non hai i permessi per visualizzare le wishlist").then(() => {
          this.router.navigate(['/']);
        });
        return;
      }
      if(error.status==404){
        this.error.throwGlobalWarning("Wishlist o utente non trovato");
        this.router.navigate(['/']);
        return;
      }
      this.error.throwGlobalError("Errore durante il caricamento delle wishlist. Riprova più tardi.");
      this.router.navigate(['/']);
    }, () => {
      this.spinner.hide();
    });

  }
  private getWishlistByToken() {
    this.spinner.show();
    this.wishlistService.getWishlistByToken(this.token).subscribe((response: any) => {
        this.whishlists.content[this.selectedWishlist] = new wishlist(response.body);
      this.spinner.hide();
        this.getCardsWishlistByToken();
      }, error => {
      this.spinner.hide();

        if(error.status==404){
          this.error.throwGlobalWarning("Wishlist non trovata");
          this.router.navigate(['/']);
          return;
        }
        if(error.status==400){
          this.error.throwGlobalWarning("Url non valido");
          this.router.navigate(['/']);
          return;
        }

        this.error.throwErrorToast("Errore durante il caricamento della wishlist. Riprova più tardi.");
        this.router.navigate(['/']);

      }
    );

  }
  private getCardsWishlistByToken() {
    this.spinner.show();
    let httpParams = this.getParamsForCards();

    this.wishlistService.getCardsWishlistByToken(httpParams, this.token).subscribe((response: any) => {

      if (response.status == 204) {
        this.noContent = true;
        return;
      }
      if (response.body) {
        this.noContent = false;
        this.whishlists.content[this.selectedWishlist].cards = [];
        if (this.currentPagination == 1)
          this.collectionSize = response.body.totalElements;

        this.currentPage = response.body.pageable.pageNumber;


        this.currentPagination = this.currentPage + 1;

        for (let i = 0; i < response.body.content.length; i++) {
           this.whishlists.content[this.selectedWishlist].cards.push(new CardWishlist(response.body.content[i]));



        }
      } else {
        return;
      }
    }, error => {
      this.spinner.hide();
      if(error.status==400){
        this.error.throwGlobalWarning("Url non valido");
        this.router.navigate(['/']);
        return;
      }

      this.error.throwGlobalError("Errore durante il caricamento delle wishlist. Riprova più tardi.");
      this.router.navigate(['/']);
    }, () => {
      this.spinner.hide();
    });
  }

  private getParamsForCards() {
    let httpParams = new HttpParams().set('page', this.currentPage.toString()).set('size', this.pageSize.toString());
    if (this.selectedSortingOption !== '-') {
      for (let [key, value] of Object.entries(this.sortingOptions)) {
        if (value === this.selectedSortingOption) {
          httpParams = httpParams.set('sort', key);

        }
      }
    }
    if (this.searchForm.get('cardSearch')?.value && this.searchForm.get('cardSearch')?.valid)
      httpParams = httpParams.set('name', this.searchForm.get('cardSearch')?.value);
    if (this.searchForm.get('ownerSearch')?.value && this.searchForm.get('ownerSearch')?.valid)
      httpParams = httpParams.set('owner', this.searchForm.get('ownerSearch')?.value);
    return httpParams;
  }

private getCardsPublicWishlist(name: string) {
  let httpParams = this.getParamsForCards();
  this.spinner.show();
  this.wishlistService.getProductsFromWishlistName(this.username, name, httpParams).subscribe((response: any) => {
    if (response.body.empty == true) {
      this.noContentCurrentWishlist = true;
    }
    this.whishlists.content[this.selectedWishlist].cards = [];

    for (let i = 0; i < response.body.numberOfElements; i++) {
      this.whishlists.content[this.selectedWishlist].cards.push(new CardWishlist(response.body.content[i]));

    }
    if (this.currentPagination == 1)
      this.collectionSize = response.body.totalElements;

    this.currentPage = response.body.pageable.pageNumber;


    this.currentPagination = this.currentPage + 1;

  }, error => {
    this.spinner.hide();
    if(error.status == 404)
      return;

    if(error.status == 401)
      return;

   if(error.status==403){
      this.error.throwGlobalWarning("Non hai i permessi per visualizzare questa wishlist");
      window.location.reload();
    }
      this.error.throwErrorToast("Errore durante il caricamento della wishlist. Riprova più tardi.");
      this.router.navigate(['/']);
  }, () => {
    this.spinner.hide();
  });
}
private getPublicWishlist(name: string) {
  this.wishlistService.getPublicWishlist(this.username, name).subscribe((response: any) => {
      this.whishlists.content[this.selectedWishlist].lastUpdate = new Date(response.body.lastUpdate);
    this.whishlists.content[this.selectedWishlist].token = response.body.token;
    this.whishlists.content[this.selectedWishlist].isPublic = response.body.isPublic;
      this.whishlists.content[this.selectedWishlist].accounts = response.body.accounts.map((account: any) => new userWishlist(account));
    }, error => {
    if(error.status == 401)
      return;
    if(error.status==403){
      this.error.throwGlobalWarning("Non hai i permessi per visualizzare questa wishlist");
      window.location.reload();
      return;
    }
    if(error.status==404){
      this.error.throwGlobalWarning("Wishlist non trovata");
      window.location.reload();
      return;
    }

        this.error.throwErrorToast("Errore durante il caricamento della wishlist. Riprova più tardi.");
        this.router.navigate(['/']);

    }
  );


}
protected isShared() {
  return this.whishlists.content[this.selectedWishlist].accounts.length > 1;
}
getVisibilityStatus(): string {
  let status = '';

  // Controllo se è pubblica
  if (this.isPublic()) {
    status += 'pubblica';
  } else {
    status += 'privata';
  }

  // Controllo se è condivisa
  if (this.isShared()) {
    status += ` e condivisa con ${this.getNumberOfUsers()} utenti`;
  }

  // Controllo se è condivisa tramite URL
  if (this.getToken()) {
    if (status.includes('condivisa')) {
      status += ' e tramite url';
    } else {
      status += ' e condivisa tramite url';
    }
  }

  return status;
}


getIconClass(): string {
  if (this.isPublic() && this.isShared() && !this.getToken()) {
    return 'fa-solid fa-users p-1';
  } else if (this.isPublic() && !this.isShared() && !this.getToken()) {
    return 'fa-solid fa-globe';
  } else if (this.isPublicAndToken() && this.isShared()) {
    return 'fa-solid fa-users p-1';
  } else if (this.isPublicAndToken() && !this.isShared()) {
    return 'fa-solid fa-share';
  } else if (this.getToken() && !this.isPublicAndToken()) {
    return 'fa-solid fa-share';
  } else if (!this.isPublic() && !this.getToken()) {
    return 'fa-solid fa-lock';
  }
  return '';
}
addToCart(id: string) {
  if(!this.login.isAuthenticated()){
    this.handler.throwGlobalWarning("Devi essere loggato per poter aggiungere carte al carrello");
    return;
  }
  
  this.spinner.show();
  const userId = this.login.loadUserId();
  this.cartService.addProduct(userId, id).subscribe((response: any) => {
    this.spinner.hide();
    this.handler.throwSuccessToast("Carta aggiunta al carrello");
  }, error => {
    this.spinner.hide();
    this.handler.throwErrorToast("Errore durante l'aggiunta al carrello. Riprova più tardi.");
  });

}


}