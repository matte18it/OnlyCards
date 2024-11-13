import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UploadedProductsComponent } from './uploaded-products.component';

describe('UploadedProductsComponent', () => {
  let component: UploadedProductsComponent;
  let fixture: ComponentFixture<UploadedProductsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [UploadedProductsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UploadedProductsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
