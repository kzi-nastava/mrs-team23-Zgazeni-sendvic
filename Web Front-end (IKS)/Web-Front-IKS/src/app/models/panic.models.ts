export interface PanicNotificationDTO {
  id: Long;
  callerId: Long;
  callerName: string;
  rideId: Long;
  createdAt: string;
  resolved: boolean;
  resolvedAt: string | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort?: unknown;
  first?: boolean;
  last?: boolean;
  numberOfElements?: number;
  empty?: boolean;
}

export type Long = number;
