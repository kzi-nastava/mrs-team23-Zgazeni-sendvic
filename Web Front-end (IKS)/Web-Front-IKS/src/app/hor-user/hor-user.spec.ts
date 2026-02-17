import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HORUser } from './hor-user';

describe('HORUser', () => {
  let component: HORUser;
  let fixture: ComponentFixture<HORUser>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HORUser]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HORUser);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
