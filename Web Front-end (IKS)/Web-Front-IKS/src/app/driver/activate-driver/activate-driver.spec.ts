import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivateDriver } from './activate-driver';

describe('ActivateDriver', () => {
  let component: ActivateDriver;
  let fixture: ComponentFixture<ActivateDriver>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivateDriver]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActivateDriver);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
