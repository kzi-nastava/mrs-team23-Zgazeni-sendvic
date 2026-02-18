import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideOrder } from './ride-order';

describe('RideOrder', () => {
  let component: RideOrder;
  let fixture: ComponentFixture<RideOrder>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideOrder]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideOrder);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
