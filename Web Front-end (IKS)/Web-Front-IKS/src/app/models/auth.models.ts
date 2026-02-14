// DTOs for authentication

//What is left: potentially rework login response, though it should match
//Aside from that, change fields of forms to match these interfaces' field names

export interface LoginRequest {
  email: string;
  password: string;
  
}

export interface LoginResponse {
  token: string;
  expiresIn: number;
  user: User;
}


export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNum: string;
  address: string;
  password: string;
  pictUrl: string; //for now later will add file upload
  // photo?: File; // If sending as multipart
}

export interface RegisterResponse {
  pictureToken: string; //redirects to login, so only message for now
 
}

export interface PictureUploadResponse {
  id: number;
  url: string;
  contentType: string;
  size: number;
  createdAt: string;
}

export interface ForgotPasswordRequest {
  email: string;
}

export interface ForgotPasswordResponse {
  message: string;
  // Response essentially, no data needed as reset link is sent via email
}

export interface ResetPasswordRequest {
    //User clicked on link with token, so token is sent and the newly typed in password
  token: string; //  token from URL or email
  newPassword: string;
}

export interface ResetPasswordResponse {
  message: string;
}

export interface User {
  userID: number;
  email: string;
  firstName: string;
  lastName: string;
  pictUrl: string;
  role: string;
}