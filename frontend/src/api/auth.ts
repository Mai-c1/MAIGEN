import request from '@/utils/request';

export function login(data: any) {
  return request.post('/auth/login', data);
}

export function register(data: any) {
  return request.post('/auth/register', data);
}

export function sendCode(params: { email: string; type: string }) {
  return request.post('/auth/send-code', null, { params });
}
