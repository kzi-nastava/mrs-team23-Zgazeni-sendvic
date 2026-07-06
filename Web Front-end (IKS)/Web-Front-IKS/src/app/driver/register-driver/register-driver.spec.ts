import { TestBed } from '@angular/core/testing';
import { RegisterDriver } from './register-driver';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';

describe('RegisterDriver', () => {
  let httpMock: HttpTestingController;

  function flushVehicles(httpMock: HttpTestingController) {
    const req = httpMock.expectOne(r =>
      r.method === 'GET' &&
      r.url.includes('/api/driver/vehicles')
    );

    req.flush([]);
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterDriver, NoopAnimationsModule],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create component and initialize form', () => {
    const fixture = TestBed.createComponent(RegisterDriver);
    fixture.detectChanges(); // triggers GET

    flushVehicles(httpMock); // ONCE ONLY

    const cmp = fixture.componentInstance;
    expect(cmp.form).toBeTruthy();
    expect(cmp.form.invalid).toBeTrue();
  });

  it('should NOT send request if form is invalid', () => {
    const fixture = TestBed.createComponent(RegisterDriver);
    fixture.detectChanges();

    flushVehicles(httpMock); // ✔ ONCE ONLY

    fixture.componentInstance.submit();

    const reqs = httpMock.match(() => true);
    expect(reqs.length).toBe(0);
  });

  it('should send POST with correct payload when form is valid', () => {
    const fixture = TestBed.createComponent(RegisterDriver);
    fixture.detectChanges();

    flushVehicles(httpMock); // ONCE ONLY

    const cmp = fixture.componentInstance;

    cmp.form.patchValue({
      email: 'driver@test.com',
      name: 'Pera',
      surname: 'Peric',
      phone: '+38164111222',
      vehicleId: 2
    });

    cmp.submit();

    const req = httpMock.expectOne(r =>
      r.method === 'POST' &&
      r.url.endsWith('/api/driver')
    );

    expect(req.request.body).toEqual({
      email: 'driver@test.com',
      name: 'Pera',
      lastName: 'Peric',
      phoneNumber: '+38164111222',
      vehicleId: 2,
      address: null,
      imgString: null
    });

    req.flush({});
  });

  it('should block submission if email format is invalid', () => {
    const fixture = TestBed.createComponent(RegisterDriver);
    fixture.detectChanges();

    flushVehicles(httpMock); // ONCE ONLY

    const cmp = fixture.componentInstance;

    cmp.form.patchValue({
      email: 'not-an-email',
      name: 'Pera',
      surname: 'Peric',
      phone: '+38164111222',
      vehicleId: 1
    });

    cmp.submit();

    const reqs = httpMock.match(() => true);
    expect(reqs.length).toBe(0);
  });

  it('clicking Create driver button triggers submit', () => {
    const fixture = TestBed.createComponent(RegisterDriver);
    fixture.detectChanges();

    flushVehicles(httpMock); // ONCE ONLY

    const cmp = fixture.componentInstance;

    cmp.form.patchValue({
      email: 'driver@test.com',
      name: 'Pera',
      surname: 'Peric',
      phone: '+38164111222',
      vehicleId: 2
    });

    const btn: HTMLButtonElement =
      fixture.nativeElement.querySelector('[data-testid="create-driver-btn"]');

    btn.click();

    const req = httpMock.expectOne(r =>
      r.method === 'POST' &&
      r.url.endsWith('/api/driver')
    );

    req.flush({});
  });
});
