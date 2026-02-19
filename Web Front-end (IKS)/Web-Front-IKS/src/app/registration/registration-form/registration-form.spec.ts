import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuthService } from '../../service/auth.service';
import { RegistrationForm } from './registration-form';

describe('RegistrationForm', () => {
  let component: RegistrationForm;
  let fixture: ComponentFixture<RegistrationForm>;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let router: Router;
  let routerNavigateSpy: jasmine.Spy;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['register', 'uploadProfilePicture']);
    await TestBed.configureTestingModule({
      imports: [RegistrationForm],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        provideRouter([])
      ]
    })
    .compileComponents();

    router = TestBed.inject(Router);
    routerNavigateSpy = spyOn(router, 'navigate');

    fixture = TestBed.createComponent(RegistrationForm);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('SECTION 1 - Field values are stored', () => {
    it('should store first name', () => {
      component.form.patchValue({ firstName: 'Ana' });
      expect(component.form.get('firstName')?.value).toBe('Ana');
    });

    it('should store last name', () => {
      component.form.patchValue({ lastName: 'Test' });
      expect(component.form.get('lastName')?.value).toBe('Test');
    });

    it('should store email', () => {
      component.form.patchValue({ email: 'ana@test.com' });
      expect(component.form.get('email')?.value).toBe('ana@test.com');
    });

    it('should store phone', () => {
      component.form.patchValue({ phone: '0641234567' });
      expect(component.form.get('phone')?.value).toBe('0641234567');
    });

    it('should store address', () => {
      component.form.patchValue({ adress: 'Test Street 1' });
      expect(component.form.get('adress')?.value).toBe('Test Street 1');
    });

    it('should store password', () => {
      component.form.patchValue({ password: 'Password1' });
      expect(component.form.get('password')?.value).toBe('Password1');
    });

    it('should store confirm password', () => {
      component.form.patchValue({ confirmPassword: 'Password1' });
      expect(component.form.get('confirmPassword')?.value).toBe('Password1');
    });
  });

  describe('SECTION 2 - Validation errors for all fields', () => {
    it('should show error for empty first name', () => {
      touchField('firstName');
      expect(component.getErrorMessage('firstName', 'First name')).toBe('First name is required');
    });

    it('should show error for short first name', () => {
      component.form.patchValue({ firstName: 'A' });
      touchField('firstName');
      expect(component.getErrorMessage('firstName', 'First name')).toContain('at least');
    });

    it('should show error for empty last name', () => {
      touchField('lastName');
      expect(component.getErrorMessage('lastName', 'Last name')).toBe('Last name is required');
    });

    it('should show error for short last name', () => {
      component.form.patchValue({ lastName: 'B' });
      touchField('lastName');
      expect(component.getErrorMessage('lastName', 'Last name')).toContain('at least');
    });

    it('should show error for empty email', () => {
      touchField('email');
      expect(component.getErrorMessage('email', 'Email')).toBe('Email is required');
    });

    it('should show error for invalid email format', () => {
      component.form.patchValue({ email: 'invalid-email' });
      touchField('email');
      expect(component.getErrorMessage('email', 'Email')).toBe('Invalid email format');
    });

    it('should show error for empty phone', () => {
      touchField('phone');
      expect(component.getErrorMessage('phone', 'Phone')).toBe('Phone is required');
    });

    it('should show error for invalid phone format', () => {
      component.form.patchValue({ phone: '12345' });
      touchField('phone');
      expect(component.getErrorMessage('phone', 'Phone')).toBe('Phone format is invalid');
    });

    it('should show error for empty address', () => {
      touchField('adress');
      expect(component.getErrorMessage('adress', 'Address')).toBe('Address is required');
    });

    it('should show error for empty password', () => {
      touchField('password');
      expect(component.getErrorMessage('password', 'Password')).toBe('Password is required');
    });

    it('should show error for short password', () => {
      component.form.patchValue({ password: 'Short1' });
      touchField('password');
      expect(component.getErrorMessage('password', 'Password')).toContain('at least');
    });

    it('should show error for empty confirm password', () => {
      touchField('confirmPassword');
      expect(component.getErrorMessage('confirmPassword', 'Confirm password')).toBe('Confirm password is required');
    });

    it('should show error when passwords do not match', () => {
      component.form.patchValue({ password: 'Password1', confirmPassword: 'Different123' });
      touchField('confirmPassword');
      expect(component.getErrorMessage('confirmPassword', 'Confirm password')).toBe('Passwords do not match');
    });
  });

  describe('SECTION 3 - Submit behavior', () => {
    it('should not submit when form is invalid', () => {
      const markAllAsTouchedSpy = spyOn(component.form, 'markAllAsTouched').and.callThrough();

      component.submit();

      expect(markAllAsTouchedSpy).toHaveBeenCalled();
      expect(component.form.invalid).toBeTrue();
      expect(authServiceSpy.register).not.toHaveBeenCalled();
    });

    it('should submit valid form and send entered data', () => {
      authServiceSpy.register.and.returnValue(of({ pictureToken: 'token-123' }));

      setValidFormValues();

      component.submit();

      expect(authServiceSpy.register).toHaveBeenCalledTimes(1);
      expect(authServiceSpy.register).toHaveBeenCalledWith({
        firstName: 'Ana',
        lastName: 'Test',
        email: 'ana@test.com',
        phoneNum: '0641234567',
        address: 'Test Street 1',
        password: 'Password1',
        pictUrl: 'DefaultUrl'
      });
      expect(routerNavigateSpy).toHaveBeenCalledWith(['/login']);
    });

    it('should upload selected photo after registration', () => {
      const pictureToken = 'token-abc';
      const file = new File(['abc'], 'photo.png', { type: 'image/png' });

      authServiceSpy.register.and.returnValue(of({ pictureToken }));
      authServiceSpy.uploadProfilePicture.and.returnValue(of({
        id: 1,
        url: '/images/account-1.png',
        contentType: 'image/png',
        size: 3,
        createdAt: '2026-02-13T23:29:49.395206Z'
      }));

      setValidFormValues();
      component.selectedPhotoFile = file;

      component.submit();

      expect(authServiceSpy.uploadProfilePicture).toHaveBeenCalledWith(file, pictureToken);
      expect(routerNavigateSpy).toHaveBeenCalledWith(['/login']);
    });

    it('should set error when picture token is missing', () => {
      authServiceSpy.register.and.returnValue(of({ pictureToken: '' }));

      setValidFormValues();
      component.selectedPhotoFile = new File(['abc'], 'photo.png', { type: 'image/png' });

      component.submit();

      expect(component.registerError()).toBe('Registration succeeded but image token is missing.');
      expect(routerNavigateSpy).not.toHaveBeenCalled();
      expect(authServiceSpy.uploadProfilePicture).not.toHaveBeenCalled();
    });

    it('should set error when picture upload fails', () => {
      const pictureToken = 'token-err';

      authServiceSpy.register.and.returnValue(of({ pictureToken }));
      authServiceSpy.uploadProfilePicture.and.returnValue(throwError(() => new Error('upload failed')));

      setValidFormValues();
      component.selectedPhotoFile = new File(['abc'], 'photo.png', { type: 'image/png' });

      component.submit();

      expect(component.registerError()).toBe('Registration succeeded, but image upload failed. Please try again.');
      expect(routerNavigateSpy).not.toHaveBeenCalled();
    });

    it('should set error when registration fails', () => {
      authServiceSpy.register.and.returnValue(throwError(() => new Error('fail')));

      setValidFormValues();

      component.submit();

      expect(component.registerError()).toBe('Registration failed. Please try again.');
      expect(routerNavigateSpy).not.toHaveBeenCalled();
    });

    it('should clear previous error on submit', () => {
      component.registerError.set('Old error');
      authServiceSpy.register.and.returnValue(of({ pictureToken: 'token-123' }));

      setValidFormValues();

      component.submit();

      expect(component.registerError()).toBeNull();
    });
  });

  function setValidFormValues() {
    component.form.setValue({
      firstName: 'Ana',
      lastName: 'Test',
      email: 'ana@test.com',
      phone: '0641234567',
      adress: 'Test Street 1',
      password: 'Password1',
      confirmPassword: 'Password1'
    });
  }

  function touchField(fieldName: string) {
    const control = component.form.get(fieldName);
    control?.markAsTouched();
    control?.updateValueAndValidity();
  }

  describe('SECTION 4 - UI helpers and validators', () => {
    it('should toggle password visibility flags', () => {
      const initialPasswordState = component.hidePassword;
      const initialConfirmState = component.hideConfirmPassword;

      component.togglePasswordVisibility();
      component.toggleConfirmPasswordVisibility();

      expect(component.hidePassword).toBe(!initialPasswordState);
      expect(component.hideConfirmPassword).toBe(!initialConfirmState);
    });

    it('should return empty error message when control has no errors', () => {
      component.form.patchValue({ firstName: 'Ana' });
      expect(component.getErrorMessage('firstName', 'First name')).toBe('');
    });

    it('should set password mismatch error on confirm password', () => {
      component.form.patchValue({ password: 'Password1', confirmPassword: 'Different123' });
      touchField('confirmPassword');
      expect(component.getErrorMessage('confirmPassword', 'Confirm password')).toBe('Passwords do not match');
    });

    it('should accept matching passwords', () => {
      component.form.patchValue({ password: 'Password1', confirmPassword: 'Password1' });
      touchField('confirmPassword');
      expect(component.getErrorMessage('confirmPassword', 'Confirm password')).toBe('');
    });

    it('should update selected photo and button label', () => {
      const file = new File(['abc'], 'photo.png', { type: 'image/png' });
      component.onPhotoSelected({ target: { files: [file] } });

      expect(component.selectedPhotoFile).toBe(file);
      expect(component.photoButtonLabel).toContain('photo.png');
    });

    it('should not accept non-image file', () => {
      const file = new File(['abc'], 'doc.txt', { type: 'text/plain' });
      component.onPhotoSelected({ target: { files: [file] } });

      expect(component.selectedPhotoFile).toBeNull();
    });

    it('should show upload label on hover', () => {
      component.isHoveringUploadBtn = true;
      expect(component.photoButtonLabel).toBe('Upload photo');
    });
  });
});
