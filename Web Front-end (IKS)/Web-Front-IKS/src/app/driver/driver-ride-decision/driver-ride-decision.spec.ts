import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverRideDecision } from './driver-ride-decision';

describe('DriverRideDecision', () => {
  let component: DriverRideDecision;
  let fixture: ComponentFixture<DriverRideDecision>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverRideDecision]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverRideDecision);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
