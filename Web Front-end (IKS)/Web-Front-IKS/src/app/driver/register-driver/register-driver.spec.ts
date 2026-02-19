// src/app/register-driver/register-driver.spec.ts
import { TestBed } from '@angular/core/testing';
import { RegisterDriver } from './register-driver';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('RegisterDriver (2.2.3)', () => {
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterDriver, NoopAnimationsModule],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create component and initialize form', () => {
    const fixture = TestBed.createComponent(RegisterDriver);
    fixture.detectChanges();

    const cmp = fixture.componentInstance;
    expect(cmp.form).toBeTruthy();
    expect(cmp.form.invalid).toBeTrue();
  });

  it('should NOT send request if form is invalid', () => {
    const fixture = TestBed.createComponent(RegisterDriver);
    fixture.detectChanges();

    fixture.componentInstance.submit();

    // no HTTP calls
    expect(httpMock.match(() => true).length).toBe(0);
  });

  it('should send POST with correct payload when form is valid', () => {
    const fixture = TestBed.createComponent(RegisterDriver);
    fixture.detectChanges();

    const cmp = fixture.componentInstance;
    cmp.form.patchValue({
      email: 'driver@test.com',
      name: 'Pera',
      surname: 'Peric',
      phone: '+38164111222',
      vehicleId: 2
    });

    // click button (optional)
    const btn: HTMLButtonElement = fixture.nativeElement.querySelector('[data-testid="create-driver-btn"]');
    btn.click();

    const req = httpMock.expectOne((r) =>
      r.method === 'POST' &&
      r.url === 'http://localhost:8080/api/admin/drivers'
    );

    expect(req.request.body).toEqual({
      email: 'driver@test.com',
      name: 'Pera',
      surname: 'Peric',
      phone: '+38164111222',
      vehicleId: 2
    });

    req.flush(null);
  });

  it('should block submission if email format is invalid', () => {
    const fixture = TestBed.createComponent(RegisterDriver);
    fixture.detectChanges();

    const cmp = fixture.componentInstance;
    cmp.form.patchValue({
      email: 'not-an-email',
      name: 'Pera',
      surname: 'Peric',
      phone: '+38164111222',
      vehicleId: 1
    });

    cmp.submit();
    expect(httpMock.match(() => true).length).toBe(0);
  });
});
