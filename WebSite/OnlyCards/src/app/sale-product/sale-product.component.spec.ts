import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SaleProductComponent } from './sale-product.component';

describe('CustomerProductComponent', () => {
  let component: SaleProductComponent;
  let fixture: ComponentFixture<SaleProductComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SaleProductComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SaleProductComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
