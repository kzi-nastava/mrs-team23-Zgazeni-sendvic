import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailedHorAdmin } from './detailed-hor-admin';

describe('DetailedHorAdmin', () => {
  let component: DetailedHorAdmin;
  let fixture: ComponentFixture<DetailedHorAdmin>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetailedHorAdmin]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetailedHorAdmin);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
