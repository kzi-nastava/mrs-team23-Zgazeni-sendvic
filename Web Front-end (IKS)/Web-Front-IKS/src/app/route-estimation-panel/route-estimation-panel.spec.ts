import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RouteEstimationPanel } from './route-estimation-panel';

describe('RouteEstimationPanel', () => {
  let component: RouteEstimationPanel;
  let fixture: ComponentFixture<RouteEstimationPanel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RouteEstimationPanel]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RouteEstimationPanel);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
