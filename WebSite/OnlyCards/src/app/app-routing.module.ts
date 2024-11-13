import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { SignUpComponent } from './sign-up/sign-up.component';
import { HomePageComponent } from './home-page/home-page.component';
import { ProductPageComponent } from './product-page/product-page.component';
import { WhishlistComponent } from './whishlist/whishlist.component';
import { CartComponent } from './cart/cart.component';
import { ProfileComponent } from './profile/profile.component';
import { SearchPageComponent } from "./search-page/search-page.component";
import { PolicyComponent } from "./policy/policy.component";
import { FaqComponent } from "./faq/faq.component";
import { SupportComponent } from "./support/support.component";
import { AboutComponent } from "./about/about.component";
import { CheckoutComponent } from "./checkout/checkout.component";
import { AdminUsersComponent } from "./admin-users/admin-users.component";
import { AdminOrdersComponent } from "./admin-orders/admin-orders.component";
import { ProductDetailsComponent } from "./product-details/product-details.component";
import {SetPageComponent} from "./set-page/set-page.component";
import {SaleProductComponent} from "./sale-product/sale-product.component";
import {SellerComponent} from "./seller/seller-component/seller.component";
import {ProductRequestComponent} from "./seller/product-request/product-request.component";
import {HelpFormComponent} from "./help-form/help-form.component";
import {UploadedProductsComponent} from "./seller/uploaded-products/uploaded-products.component";
import {PublicProfileComponent} from "./public-profile/public-profile.component";
import {ModifyProductComponent} from "./seller/modify-product/modify-product.component";

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'sign-up', component: SignUpComponent },
  { path: 'home-page', component: HomePageComponent },
  { path: '', redirectTo: '/home-page', pathMatch: 'full' },
  { path: 'whishlists', component: WhishlistComponent, data: { title: 'Wishlists' }},
  { path: 'whishlists/:token', component: WhishlistComponent, data: { title: 'Wishlists' }},
  { path: 'users/:username/wishlists', component: WhishlistComponent, data: { title: 'Wishlists' }},
  { path: 'cart', component: CartComponent },
  { path: 'sale-product/:cardID', component: SaleProductComponent },
  { path: 'user/:type', component: ProfileComponent },
  { path: 'admin/users', component: AdminUsersComponent },
  { path: 'support/productType', component: ProductRequestComponent },
  { path: 'admin/orders', component: AdminOrdersComponent },
  { path: 'seller', component: SellerComponent },
  { path: 'policy', component: PolicyComponent },
  { path: "users/:username", component: PublicProfileComponent },
  { path: 'seller/products', component: UploadedProductsComponent},
  { path: 'seller/product/:id', component: ModifyProductComponent },
  { path: 'faq', component: FaqComponent },
  { path: 'support', component: SupportComponent },
  { path: 'about', component: AboutComponent },
  { path: 'help', component: HelpFormComponent },
  { path: 'checkout', component: CheckoutComponent },
  { path: 'cards/:type/card/:id', component: ProductDetailsComponent },
  { path: 'cards/:type/:set', component: SetPageComponent },
  { path: ':game/products', component: SearchPageComponent},
  { path: ':game', component: ProductPageComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})

export class AppRoutingModule {}
