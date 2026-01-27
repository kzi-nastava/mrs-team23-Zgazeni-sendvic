// DTOs for authentication

export interface LoginRequest {
  email: string;
  password: string;
  remember?: boolean;
}

export interface LoginResponse {
  token: string;
  user: User;
  // Add other fields as needed
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  adress: string;
  username: string;
  password: string;
  // photo?: File; // If sending as multipart
}

export interface RegisterResponse {
  message: string;
  user: User;
  // Add other fields
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ForgotPasswordResponse {
  message: string;
  // Maybe a reset token or instructions
}

export interface ResetPasswordRequest {
  token: string; // Assuming token from URL or email
  password: string;
}

export interface ResetPasswordResponse {
  message: string;
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  // Add other user fields
}