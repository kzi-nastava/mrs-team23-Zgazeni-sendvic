import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FutureRides } from './future-rides';

describe('FutureRides', () => {
  let component: FutureRides;
  let fixture: ComponentFixture<FutureRides>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FutureRides]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FutureRides);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
