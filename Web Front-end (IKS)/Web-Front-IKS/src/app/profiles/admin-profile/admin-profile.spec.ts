import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminProfile } from './admin-profile';

describe('AdminProfile', () => {
  let component: AdminProfile;
  let fixture: ComponentFixture<AdminProfile>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminProfile]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminProfile);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
