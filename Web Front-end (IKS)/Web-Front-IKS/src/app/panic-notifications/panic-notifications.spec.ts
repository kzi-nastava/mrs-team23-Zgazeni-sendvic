import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PanicNotifications } from './panic-notifications';

describe('PanicNotifications', () => {
  let component: PanicNotifications;
  let fixture: ComponentFixture<PanicNotifications>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PanicNotifications]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PanicNotifications);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
