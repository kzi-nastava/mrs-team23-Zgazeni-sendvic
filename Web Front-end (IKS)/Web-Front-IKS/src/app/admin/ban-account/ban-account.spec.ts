import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BanAccount } from './ban-account';

describe('BanAccount', () => {
  let component: BanAccount;
  let fixture: ComponentFixture<BanAccount>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BanAccount]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BanAccount);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
