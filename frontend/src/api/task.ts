import request from '@/utils/request';

export interface TaskStatus {
  taskId: number;
  title: string;
  status: number;
  progress: number;
  resultUrl?: string;
  errorMessage?: string;
  createTime: string;
  updateTime: string;
}

export interface TaskDetail {
  id: string;
  title: string;
  problemDescription: string;
  standardCode: string;
  testcaseCount: number;
  timeLimit: number;
  memoryLimit: number;
  workflowId: string;
  workflowName: string;
  workflowDescription?: string;
  status: number;
  progress: number;
  errorMessage?: string;
  resultUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface TaskStatistics {
  inProgressCount: number;
  completedCount: number;
  failedCount: number;
  totalCount: number;
}

export function createTask(data: any) {
  return request.post('/task/create', data);
}

export function getWorkflows() {
  return request.get('/task/workflows');
}

export function getTaskStatus(taskId: string) {
  return request.get<TaskStatus>(`/task/status/${taskId}`);
}

export function getTaskDetail(taskId: string) {
  return request.get<TaskDetail>(`/task/detail/${taskId}`);
}

export function getTaskList(params: any) {
  return request.get<{ list: TaskStatus[]; total: number }>('/task/list', params);
}

export function getTaskStatistics() {
  return request.get<TaskStatistics>('/task/statistics');
}

export function retryTask(taskId: string) {
  return request.post(`/task/retry/${taskId}`);
}

export function cancelTask(taskId: string) {
  return request.post(`/task/cancel/${taskId}`);
}

export function deleteTask(taskId: string) {
  return request.delete(`/task/${taskId}`);
}

export function getTaskLogs(taskId: string) {
  return request.get<any[]>(`/task/logs/${taskId}`);
}
