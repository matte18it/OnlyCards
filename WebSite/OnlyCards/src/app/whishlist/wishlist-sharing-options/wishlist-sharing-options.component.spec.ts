import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WishlistSharingOptionsComponent } from './wishlist-sharing-options.component';

describe('WishlistSharingOptionsComponent', () => {
  let component: WishlistSharingOptionsComponent;
  let fixture: ComponentFixture<WishlistSharingOptionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WishlistSharingOptionsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(WishlistSharingOptionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
