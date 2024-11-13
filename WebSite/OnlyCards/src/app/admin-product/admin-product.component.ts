import { Component, inject, Input, OnInit, TemplateRef } from '@angular/core';
import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Product, ProductEdit } from '../../model/product';
import { AbstractControl, FormArray, FormBuilder, FormGroup, ValidatorFn, Validators } from '@angular/forms';
import { Condition, getConditionString, getConditionFromString } from '../../model/enum/conditionEnum';
import { ProductService } from '../../service/product/product.service';
import { HandlerService } from '../../service/error-handler/handler.service';
import { NgxSpinner, NgxSpinnerComponent, NgxSpinnerModule, NgxSpinnerService } from 'ngx-spinner';
import { Price } from '../../model/price';
import { ProductImageEdit } from '../../model/productImage';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-admin-product',
  templateUrl: './admin-product.component.html',
  styleUrl: './admin-product.component.css'
})
export class AdminProductComponent implements OnInit{
maxLenght() {
  return this.getImageFormArray().length>=10
}
getConditionFromString(_t18: string) {
return getConditionFromString(_t18);
}

  getPhotoFile(index: number): File {
    return this.getImageFormArray().at(index).get('photoFile')?.value;
  }
  cleanFormFile() {
    this.getImageFormArray().at(this.currentImage).patchValue({
      photoFile: null
    });
  }
  cleanFormUrl() {
    this.getImageFormArray().at(this.currentImage).patchValue({
      photoUrl: ""
    });
  }
  changeFile($event: Event) {
    const file = ($event.target as HTMLInputElement).files?.item(0);
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.getImageFormArray().at(this.currentImage).patchValue({
          photoFile: file
        });
      };
      reader.readAsDataURL(file);
    }

  }

  currencyMap: { [key: string]: string } = {};

  activeModal = inject(NgbActiveModal);
  @Input() product!: Product;
  form:FormGroup;
  active: number = 1;
  currentImage: number = 0;
  constructor(private modal:NgbModal, private fb:FormBuilder, private productService:ProductService, private handler:HandlerService, private spinner:NgxSpinnerService) {
    this.form = this.fb.group({
      condition: ["", Validators.required],
      stateDescription: ["", Validators.compose([Validators.required, Validators.minLength(10)])],
      currency: ["", Validators.required],
      amount: ["", Validators.compose([Validators.required, Validators.min(0.01), Validators.pattern("^-?\\d+(\\.\\d+)?$")])],
      images:this.fb.array([])
    });
  }
  ngOnInit(): void {
    this.loadCurrencies();
    this.form.patchValue({
      condition: this.product.condition,
      stateDescription: this.product.stateDescription,
      currency: this.product.price.currency,
      amount: this.product.price.amount
    });
    for (let i = 0; i < this.product.images.length; i++) {
      this.getImageFormArray().push(this.fb.group({
          photoUrl: [this.product.images[i].photo],
          photoFile: [null]
        })
      );
    }
  }
  getImageFormArray() {
    return this.form.get('images') as FormArray;
  }
  getConditionArray(): string[] {
    const conditionList: Condition[] = Object.keys(Condition)
      .map((key) => Condition[key as keyof typeof Condition]);
    return conditionList.map((condition) => getConditionString(condition));
  }
  loadCurrencies() {
    this.spinner.show();
    this.productService.getCurrencies().subscribe((currencies) => {
      this.spinner.hide();
      if(currencies === null || currencies.body === null) {
        this.handler.throwErrorToast("Failed to load currencies.");
        return;
      }else{
        this.currencyMap = currencies.body;
        this.form.patchValue({
          currency: this.getCurrencyValue(this.product.price.currency)
        });
      }
    }, (error) => {
      this.spinner.hide();
      if(error.status === 401) {
        this.handler.throwErrorToast("Session expired. Please login again.");
        return;
      }
      if(error.status === 403) {
        this.handler.throwErrorToast("You are not authorized to access this resource.");
        return;
      }
      this.handler.throwGlobalError("Failed to load currencies.");

    });
  }
  getCurrencyArray(): any {
    return Object.values(this.currencyMap);
  }
  noChangeForm(): boolean {
    let check1= this.form.value.condition === this.product.condition &&
      this.form.value.stateDescription === this.product.stateDescription &&
      this.form.value.currency === this.getCurrencyValue(this.product.price.currency) &&
      this.form.value.amount === this.product.price.amount && this.getImageFormArray().length === this.product.images.length;
    if (!check1) {
      return false;
    }

    
    return check1;}
  deleteImage(index: number) {
    if(this.getImageFormArray().at(index).get('photoUrl')?.value !== "") {
      this.spinner.show();
      this.productService.deleteImage(this.product.id, this.product.images[index].id).subscribe((product) => {
        this.spinner.hide();
          this.activeModal.close(true);
        
      }, (error) => {
        this.spinner.hide();
        if(error.status === 401) {
          this.handler.throwErrorToast("Session expired. Please login again.");
          return;
        }
        if(error.status === 403) {
          this.handler.throwErrorToast("You are not authorized to access this resource.");
          return;
        }
        this.handler.throwGlobalError("Failed to remove image.");
      });
    }
    this.getImageFormArray().removeAt(index);
    this.handler.throwSuccessToast("Image removed.");

  }
  closedModal: boolean = true;
  addNewImageModal(modal: TemplateRef<any>) {
    this.addNewImageForm();
    this.currentImage = this.getImageFormArray().length - 1;
    this.closedModal= false;
    this.modal.open(modal, { centered: true }).result.then(
      (result) => {
        this.closedModal = true;
      },
      (reason) => {
        this.getImageFormArray().removeAt(this.currentImage);
        this.currentImage = 0;
        this.closedModal = true;
      },
    );
  }
  addNewImageForm() {
    this.getImageFormArray().push(this.fb.group({
      photoFile: [null,  this.fileSizeValidator(20)],
      photoUrl: [""]
    }));
  }
  fileSizeValidator(maxSizeMB: number): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const file = control.value;
      if (file && file.size > maxSizeMB * 1024 * 1024) {
        this.form.get('photo')?.markAsDirty();
        return { 'fileSizeExceeded': true };
      }
      return null;
    };
  }

  updateProduct() {
    this.spinner.show();
    let condition:Condition | undefined ;
    if(this.form.value.condition !== "" && this.form.value.condition !== null && this.form.value.condition !== this.product.condition) {
      condition =  getConditionFromString(this.form.value.condition);}
    let stateDescription:string|undefined;
    if(this.form.value.stateDescription !== "" && this.form.value.stateDescription !== null && this.form.value.stateDescription !== this.product.stateDescription) {
      stateDescription = this.form.value.stateDescription;
    }
    let price:Price | undefined;

    if(this.getCurrencyKey(this.form.value.currency) != this.product.price.currency || this.form.value.amount != this.product.price.amount) {
      const currency:string = this.form.value.currency;
      const amount:number = this.form.value.amount;
      const data={
        amount: amount,
        currency: this.getCurrencyKey(currency),
      }
      price= new Price(data);
    }

    const images:ProductImageEdit[] = [];
    for(let i = 0; i < this.getImageFormArray().length; i++) {
      const photoFile = this.getImageFormArray().at(i).get('photoFile')?.value;
      if(photoFile !== null) {
        images.push(new ProductImageEdit( photoFile));
      }
    }
    const productEdit:ProductEdit = new ProductEdit(images, condition, stateDescription, price);
    this.productService.updateProduct(this.product.id, productEdit).subscribe((product) => {
      this.spinner.hide();
        this.activeModal.close(true);
      
    }, (error) => {
      this.spinner.hide();
      if(error.status === 401) {
        console.log("Session expired. Please login again.");
        this.handler.throwGlobalError("Session expired. Please login again.");
        return;
      }
      if(error.status === 400) {
        this.handler.throwGlobalError("Invalid data., Please check the data and try again.");
        return;
      }
      if(error.status === 403) {
        this.handler.throwGlobalError("You are not authorized to access this resource.");
        return;
      }
      this.handler.throwGlobalError("Failed to update product.");
    });
  }


  getCurrencyKey(currency: string): string {
    for (const key in this.currencyMap) {
      if (this.currencyMap[key] === currency) {
        return key;
      }
    }
    return "";
  }
  getCurrencyValue(key: string): string {
    return this.currencyMap[key];
  }

}
