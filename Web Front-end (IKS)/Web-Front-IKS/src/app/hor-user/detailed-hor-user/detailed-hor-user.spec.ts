import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailedHorUser } from './detailed-hor-user';

describe('DetailedHorUser', () => {
  let component: DetailedHorUser;
  let fixture: ComponentFixture<DetailedHorUser>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetailedHorUser]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetailedHorUser);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
