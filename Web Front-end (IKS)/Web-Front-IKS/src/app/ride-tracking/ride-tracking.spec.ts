import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { RideTracking } from './ride-tracking';
import { RideTrackingWebSocketService } from '../service/ride-tracking-websocket.service';
import { RideTrackingUpdate } from '../models/ride-tracking.models';
import { AuthService } from '../services/auth.service';
import { of } from 'rxjs';

describe('RideTracking', () => {
  let component: RideTracking;
  let fixture: ComponentFixture<RideTracking>;
  let httpMock: HttpTestingController;
  let mockRideTrackingService: jasmine.SpyObj<RideTrackingWebSocketService>;
  let mockAuthService: jasmine.SpyObj<AuthService>;

  const createMockRide = (overrides?: Partial<RideTrackingUpdate>): RideTrackingUpdate => ({
    rideId: 123,
    vehicleId: 456,
    status: 'FINISHED',
    currentLatitude: 45.2671,
    currentLongitude: 19.8335,
    route: [],
    price: 1000,
    startTime: '2026-02-18T10:00:00',
    estimatedEndTime: '2026-02-18T10:30:00',
    timeLeft: '5 min',
    driver: {
      id: 789,
      name: 'John Doe',
      phoneNumber: '+1234567890'
    },
    ...overrides
  });

  beforeEach(async () => {
    mockRideTrackingService = jasmine.createSpyObj('RideTrackingWebSocketService', [
      'connect',
      'disconnect',
      'getRideUpdates',
      'getConnectionStatus'
    ]);
    mockRideTrackingService.getRideUpdates.and.returnValue(of(null));
    mockRideTrackingService.getConnectionStatus.and.returnValue(of(false));

    mockAuthService = jasmine.createSpyObj('AuthService', [
      'getCurrentUserId',
      'getRole'
    ]);
    mockAuthService.getCurrentUserId.and.returnValue(1);
    mockAuthService.getRole.and.returnValue('PASSENGER');

    await TestBed.configureTestingModule({
      imports: [RideTracking],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: RideTrackingWebSocketService, useValue: mockRideTrackingService },
        { provide: AuthService, useValue: mockAuthService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideTracking);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Rating Form', () => {
    it('should initialize with default rating values', () => {
      expect(component.driverRating).toBe(5);
      expect(component.vehicleRating).toBe(5);
      expect(component.ratingComment).toBe('');
    });

    it('should open rate form and close note form', () => {
      component.showNoteForm = true;
      component.showRateForm = false;

      component.openRate();

      expect(component.showRateForm).toBe(true);
      expect(component.showNoteForm).toBe(false);
    });

    it('should close rate form and reset values', () => {
      component.showRateForm = true;
      component.driverRating = 6;
      component.vehicleRating = 7;
      component.ratingComment = 'Great ride';

      component.closeRateForm();

      expect(component.showRateForm).toBe(false);
      expect(component.driverRating).toBe(5);
      expect(component.vehicleRating).toBe(5);
      expect(component.ratingComment).toBe('');
    });

    it('should update driver rating value', () => {
      component.driverRating = 9;
      expect(component.driverRating).toBe(9);
    });

    it('should update vehicle rating value', () => {
      component.vehicleRating = 8;
      expect(component.vehicleRating).toBe(8);
    });

    it('should update rating comment', () => {
      const testComment = 'Excellent service!';
      component.ratingComment = testComment;
      expect(component.ratingComment).toBe(testComment);
    });

    it('should send rating successfully with current ride', () => {
      component.currentRide = createMockRide();
      component.driverRating = 9;
      component.vehicleRating = 8;
      component.ratingComment = 'Great experience';
      component.showRateForm = true;

      component.sendRating();

      const req = httpMock.expectOne('http://localhost:8080/api/ride-driver-rating/1');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        userId: 1,
        rideId: 123,
        driverRating: 9,
        vehicleRating: 8,
        comment: 'Great experience'
      });

      req.flush({ success: true });

      expect(component.showRateForm).toBe(false);
      expect(component.driverRating).toBe(5);
      expect(component.vehicleRating).toBe(5);
      expect(component.ratingComment).toBe('');
    });

    it('should not send rating without current ride', () => {
      component.currentRide = null;
      spyOn(console, 'error');

      component.sendRating();

      expect(console.error).toHaveBeenCalledWith('No current ride to rate');
      httpMock.expectNone('http://localhost:8080/api/ride-driver-rating/1');
    });

    it('should not send rating without user id', () => {
      component.currentRide = createMockRide();
      component['userId'] = null;
      spyOn(console, 'error');

      component.sendRating();

      expect(console.error).toHaveBeenCalledWith('No logged in user found for rating');
      httpMock.expectNone('http://localhost:8080/api/ride-driver-rating/1');
    });

    it('should handle rating submission error', () => {
      component.currentRide = createMockRide();
      component.driverRating = 10;
      component.vehicleRating = 10;
      spyOn(console, 'error');

      component.sendRating();

      const req = httpMock.expectOne('http://localhost:8080/api/ride-driver-rating/1');
      req.error(new ProgressEvent('error'));

      expect(console.error).toHaveBeenCalledWith('Error sending rating:', jasmine.any(Object));
      expect(component.showRateForm).toBe(false);
    });

    it('should validate driver rating range (1-10)', () => {
      component.driverRating = 1;
      expect(component.driverRating).toBeGreaterThanOrEqual(1);
      expect(component.driverRating).toBeLessThanOrEqual(10);

      component.driverRating = 10;
      expect(component.driverRating).toBeGreaterThanOrEqual(1);
      expect(component.driverRating).toBeLessThanOrEqual(10);
    });

    it('should validate vehicle rating range (1-10)', () => {
      component.vehicleRating = 1;
      expect(component.vehicleRating).toBeGreaterThanOrEqual(1);
      expect(component.vehicleRating).toBeLessThanOrEqual(10);

      component.vehicleRating = 10;
      expect(component.vehicleRating).toBeGreaterThanOrEqual(1);
      expect(component.vehicleRating).toBeLessThanOrEqual(10);
    });
  });
});
