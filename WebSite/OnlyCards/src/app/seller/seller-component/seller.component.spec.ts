import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SellerComponent } from './seller.component';

describe('SellerComponentComponent', () => {
  let component: SellerComponent;
  let fixture: ComponentFixture<SellerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SellerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SellerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
