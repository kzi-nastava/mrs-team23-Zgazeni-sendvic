import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HorDriver } from './hor-driver';

describe('HorDriver', () => {
  let component: HorDriver;
  let fixture: ComponentFixture<HorDriver>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HorDriver]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HorDriver);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
