import request from '@/utils/request';

export interface PointsBalance {
  balance: number;
}

export interface PointsRecord {
  id: number;
  userId: number;
  amount: number;
  source: string;
  sourceName?: string;
  relatedId?: string;
  description: string;
  createdAt: string;
}

export function getPointsBalance() {
  return request.get<PointsBalance>('/points/balance');
}

export function getPointsRecords(params: any) {
  return request.get<{ list: PointsRecord[]; total: number }>('/points/records', params);
}

export function signIn() {
  return request.post('/points/sign-in');
}

export function getMonthSignInDays() {
  return request.get<string[]>('/points/sign-in/month');
}

export function adReward() {
  return request.post<number>('/points/ad-reward');
}
