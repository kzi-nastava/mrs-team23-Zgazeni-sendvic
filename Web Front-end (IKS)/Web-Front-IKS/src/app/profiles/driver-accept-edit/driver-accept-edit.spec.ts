import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverAcceptEdit } from './driver-accept-edit';

describe('DriverAcceptEdit', () => {
  let component: DriverAcceptEdit;
  let fixture: ComponentFixture<DriverAcceptEdit>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverAcceptEdit]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverAcceptEdit);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
