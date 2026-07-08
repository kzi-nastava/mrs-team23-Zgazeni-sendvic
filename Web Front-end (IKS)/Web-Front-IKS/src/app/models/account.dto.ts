export interface GetAccountDTO {
  id: number;
  email: string;
  name: string;
  lastName: string;
  address: string;
  phoneNumber: string;
  imgString?: string;

  role: 'ADMIN' | 'DRIVER' | 'USER';
  totalDrivingHours?: number;
}

export interface AccountLookupDTO {
  id: number;
  email: string;
  name: string;
  lastName: string;
}

export interface AccountAdminViewDTO {
  id: number;
  email: string;
  name: string;
  lastName: string;

  phoneNumber: string;
  address: string;

  confirmed: boolean;
  banned: boolean;

  accountType: string;
}
