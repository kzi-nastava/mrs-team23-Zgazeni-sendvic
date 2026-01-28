import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideTracking } from './ride-tracking';

describe('RideTracking', () => {
  let component: RideTracking;
  let fixture: ComponentFixture<RideTracking>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideTracking]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideTracking);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
