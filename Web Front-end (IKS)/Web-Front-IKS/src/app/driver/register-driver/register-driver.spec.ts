import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RegisterDriver } from './register-driver';

describe('RegisterDriver', () => {
  let component: RegisterDriver;
  let fixture: ComponentFixture<RegisterDriver>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterDriver],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterDriver);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
