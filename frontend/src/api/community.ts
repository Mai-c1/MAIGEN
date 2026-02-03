import request from '@/utils/request';

export interface CommunityContent {
  id: number;
  userId: number;
  nickname?: string;
  avatar?: string;
  title: string;
  description: string;
  dataFilePath: string;
  categoryId: number;
  categoryName?: string;
  status: number;
  viewCount: number;
  downloadCount: number;
  likeCount: number;
  ratingAvg: number;
  ratingCount: number;
  points: number;
  tags?: string[];
  isUnlocked?: boolean;
  isLiked?: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export function getCommunityList(params: any) {
  return request.get<{ list: CommunityContent[]; total: number }>('/community/list', params);
}

export function getCommunityDetail(id: string) {
  return request.get<CommunityContent>(`/community/detail/${id}`);
}

export function shareContent(data: any) {
  return request.post('/community/share', data);
}

export function downloadResource(contentId: string) {
  return request.post<{ downloadUrl: string }>(`/community/${contentId}/download`);
}

export function likeContent(id: string) {
  return request.post(`/community/like/${id}`);
}

export function rateContent(data: any) {
  return request.post('/community/rate', data);
}

export function getCategories() {
  return request.get<Category[]>('/category/list');
}

export function getTags() {
  return request.get<string[]>('/tag/list');
}
