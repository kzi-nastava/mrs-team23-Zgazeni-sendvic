import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HORAdmin } from './hor-admin';

describe('HORAdmin', () => {
  let component: HORAdmin;
  let fixture: ComponentFixture<HORAdmin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HORAdmin]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HORAdmin);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
