import request from '@/utils/request';

// --- 用户管理 (ManageUserController) ---
export const adminUser = {
  list: (params: any) => request.get('/admin/user/list', params),
  create: (data: any) => request.post('/admin/user', data),
  update: (id: number | string, data: any) => request.put(`/admin/user/${id}`, data),
  updateStatus: (id: number | string, status: number) => request.put(`/admin/user/${id}/status`, null, { params: { status } }),
  adjustPoints: (id: number | string, data: { amount: number; reason: string }) => request.post(`/admin/user/${id}/points`, data),
  listInvitations: (params: any) => request.get('/admin/user/invitations', params)
};

// --- 内容管理 (ManageContentController) ---
export const adminContent = {
  list: (params: any) => request.get('/admin/content/list', params),
  audit: (id: number | string, data: { status: string; reason: string }) => request.put(`/admin/content/${id}/audit`, data),
  delete: (id: number | string) => request.delete(`/admin/content/${id}`),
  
  // 分类
  listCategories: () => request.get('/admin/content/category/list'),
  createCategory: (data: any) => request.post('/admin/content/category', data),
  updateCategory: (data: any) => request.put('/admin/content/category', data),
  deleteCategory: (id: number | string) => request.delete(`/admin/content/category/${id}`),
  
  // 标签
  listTags: () => request.get('/admin/content/tag/list'),
  createTag: (data: any) => request.post('/admin/content/tag', data),
  deleteTag: (id: number | string) => request.delete(`/admin/content/tag/${id}`)
};

// --- 任务管理 (ManageTaskController) ---
export const adminTask = {
  list: (params: any) => request.get('/admin/task/list', params),
  delete: (id: number | string) => request.delete(`/admin/task/${id}`),
};

// --- 角色管理 (ManageRoleController) ---
export const adminRole = {
  list: (params: any) => request.get('/admin/role/list', params),
  create: (data: any) => request.post('/admin/role/create', data),
  update: (data: any) => request.post('/admin/role/update', data),
  delete: (id: number | string) => request.post('/admin/role/delete', null, { params: { id } }),
};

// --- 权限管理 (ManagePermissionController) ---
export const adminPermission = {
  list: (params: any) => request.get('/admin/permission/list', params),
  listAll: () => request.get('/admin/permission/list-all'),
  create: (data: any) => request.post('/admin/permission/create', data),
  update: (data: any) => request.post('/admin/permission/update', data),
  delete: (id: number | string) => request.post('/admin/permission/delete', null, { params: { id } }),
};

// --- 方案管理 (ManageAiWorkflowController) ---
export const adminWorkflow = {
  list: (params: any) => request.get('/admin/workflow/list', params),
  create: (data: any) => request.post('/admin/workflow/create', data),
  update: (data: any) => request.post('/admin/workflow/update', data),
  delete: (id: number | string) => request.post('/admin/workflow/delete', null, { params: { id } }),
  copy: (id: number | string) => request.post('/admin/workflow/copy', null, { params: { id } }),
  getSteps: (workflowId: number | string) => request.get('/admin/workflow/steps', { workflowId }),
  saveSteps: (workflowId: number | string, steps: any[]) => request.post('/admin/workflow/steps/save', steps, { params: { workflowId } })
};

// --- 系统管理 (ManageSystemController) ---
export const adminSystem = {
  // 配置
  listConfigs: () => request.get('/admin/system/config/list'),
  addConfig: (data: any) => request.post('/admin/system/config', data),
  updateConfig: (data: any) => request.put('/admin/system/config', data),
  deleteConfig: (id: number | string) => request.delete(`/admin/system/config/${id}`),
  
  // 积分规则
  listPointsRules: () => request.get('/admin/system/points/rule/list'),
  updatePointsRule: (data: any) => request.put('/admin/system/points/rule', data),
  listPointsRecords: (params: any) => request.get('/admin/system/points/records', params),
  
  // 日志与统计
  listLogs: (params: any) => request.get('/admin/system/log/list', params),
  getStatistics: () => request.get('/admin/system/statistics')
};
