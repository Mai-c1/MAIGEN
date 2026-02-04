import request from '@/utils/request';

export interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  email: string;
  avatar: string;
  points: number;
  invitationCode: string;
  roles: string[];
  permissions: string[];
  createdAt: string;
}

export function getUserInfo() {
  return request.get<UserInfo>('/user/info');
}

export function updateUserInfo(data: any) {
  return request.put('/user/info', data);
}

export function changePassword(data: any) {
  return request.put('/user/password', data);
}
