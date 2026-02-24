import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideReport } from './ride-report';

describe('RideReport', () => {
  let component: RideReport;
  let fixture: ComponentFixture<RideReport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideReport]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideReport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
