import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RidesReport } from './rides-report';

describe('RidesReport', () => {
  let component: RidesReport;
  let fixture: ComponentFixture<RidesReport>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RidesReport]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RidesReport);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
