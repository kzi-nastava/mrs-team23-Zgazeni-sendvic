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
