import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FavoriteRoutes } from './favorite-routes';

describe('FavoriteRoutes', () => {
  let component: FavoriteRoutes;
  let fixture: ComponentFixture<FavoriteRoutes>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FavoriteRoutes]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FavoriteRoutes);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
