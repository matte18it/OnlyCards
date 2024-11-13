import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SignUpComponent} from "./sign-up/sign-up.component";
import { HomePageComponent} from "./home-page/home-page.component";
import { NavbarComponent} from "./navbar/navbar.component";
import { ProductPageComponent} from "./product-page/product-page.component";
import { ProfileComponent} from "./profile/profile.component";
import { WhishlistComponent} from "./whishlist/whishlist.component";
import { CartComponent} from "./cart/cart.component";
import { FooterComponent} from "./footer/footer.component";
import { LoginComponent} from "./login/login.component";
import { NgOptimizedImage} from "@angular/common";
import { FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import { RECAPTCHA_V3_SITE_KEY, RecaptchaV3Module} from "ng-recaptcha";
import { environment} from "../utility/environment";
import { SearchPageComponent } from './search-page/search-page.component';
import { NgxSliderModule} from "@angular-slider/ngx-slider";
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { NgxSpinnerModule} from "ngx-spinner";
import { BrowserAnimationsModule} from "@angular/platform-browser/animations";
import { SupportComponent } from './support/support.component';
import { FaqComponent } from './faq/faq.component';
import { PolicyComponent } from './policy/policy.component';
import { AboutComponent } from './about/about.component';
import { ProductDetailsComponent } from './product-details/product-details.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { AdminOrdersComponent } from './admin-orders/admin-orders.component';
import { AdminUsersComponent } from './admin-users/admin-users.component';
import { WishlistSharingOptionsComponent } from './whishlist/wishlist-sharing-options/wishlist-sharing-options.component';
import {UrlInterceptor} from "../service/interceptor/url-interceptor.service";
import { AdminProductTypeComponent } from './admin-product-type/admin-product-type.component';
import { SetPageComponent } from './set-page/set-page.component';
import { SaleProductComponent } from './sale-product/sale-product.component';
import { AdminProductComponent } from './admin-product/admin-product.component';
import { SellerComponent } from './seller/seller-component/seller.component';
import {MatAutocomplete, MatAutocompleteTrigger, MatOption} from "@angular/material/autocomplete";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {OrderModalComponent} from "./order-modal/order-modal.component";
import {ProductRequestComponent} from "./seller/product-request/product-request.component";
import {UserEditModalComponent} from "./user-edit-modal/user-edit-modal.component";
import { HelpFormComponent } from './help-form/help-form.component';
import { UploadedProductsComponent } from './seller/uploaded-products/uploaded-products.component';
import { PublicProfileComponent } from './public-profile/public-profile.component';
import { ModifyProductComponent } from './seller/modify-product/modify-product.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    SignUpComponent,
    HomePageComponent,
    NavbarComponent,
    ProductPageComponent,
    ProfileComponent,
    WhishlistComponent,
    CartComponent,
    FooterComponent,
    SearchPageComponent,
    SupportComponent,
    FaqComponent,
    PolicyComponent,
    AboutComponent,
    ProductDetailsComponent,
    CheckoutComponent,
    AdminOrdersComponent,
    AdminUsersComponent,
    WishlistSharingOptionsComponent,
    AdminProductTypeComponent,
    SetPageComponent,
    SaleProductComponent,
    AdminProductComponent,
    SellerComponent,
    OrderModalComponent,
    ProductRequestComponent,
    UserEditModalComponent,
    HelpFormComponent,
    UploadedProductsComponent,
    PublicProfileComponent,
    ModifyProductComponent
  ],
    imports: [
        RecaptchaV3Module,
        BrowserModule,
        AppRoutingModule,
        NgOptimizedImage,
        FormsModule,
        HttpClientModule,
        NgxSliderModule,
        NgbModule,
        ReactiveFormsModule,
        NgxSpinnerModule,
        BrowserAnimationsModule,
        MatAutocomplete,
        MatOption,
        MatFormField,
        MatInput,
        MatAutocompleteTrigger,
        MatLabel,
        FormsModule,
    ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: UrlInterceptor,
      multi: true
    },
    {
      provide: RECAPTCHA_V3_SITE_KEY,
      useValue: environment.recaptcha.siteKey
    }
  ],
  bootstrap: [AppComponent]
})

export class AppModule { }
