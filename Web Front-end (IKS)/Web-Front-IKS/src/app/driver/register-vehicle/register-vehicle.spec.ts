import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterVehicle } from './register-vehicle';

describe('RegisterVehicle', () => {
  let component: RegisterVehicle;
  let fixture: ComponentFixture<RegisterVehicle>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterVehicle]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterVehicle);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
