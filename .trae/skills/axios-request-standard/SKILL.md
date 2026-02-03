---
name: axios-request-standard
description: 强制前端 API 请求使用 RequestHttp 封装类。在编写或修改 frontend/src/api 下的接口文件时调用。
---

# Axios Request Standard

强制统一使用 `src/utils/request.ts` 中封装的 `RequestHttp` 类进行网络请求，禁止直接使用 axios 或原始配置对象。

## 核心规则

1.  **禁止直接使用 Axios**：
    *   ❌ `import axios from 'axios'` (仅在 `utils/request.ts` 允许)
    *   ❌ `axios.get(...)`

2.  **禁止使用原始配置对象**：
    *   ❌ `request({ url: '/api', method: 'get' })`

3.  **强制使用封装方法**：
    *   ✅ `request.get<T>(url, params)`
    *   ✅ `request.post<T>(url, data)`
    *   ✅ `request.put<T>(url, data)`
    *   ✅ `request.delete<T>(url, params)`

4.  **接口定义位置**：
    *   所有 API 函数必须定义在 `src/api/` 目录下，禁止在 Vue 组件中直接调用 `request`。

## 代码示例

### 1. 工具类引用
```typescript
import request from '@/utils/request';
```

### 2. GET 请求
```typescript
// ❌ 错误写法
export function getList(params: any) {
  return request({
    url: '/list',
    method: 'get',
    params
  });
}

// ✅ 正确写法
export function getList(params: any) {
  return request.get('/list', params);
}
```

### 3. POST 请求
```typescript
// ❌ 错误写法
export function create(data: any) {
  return request({
    url: '/create',
    method: 'post',
    data
  });
}

// ✅ 正确写法
export function create(data: any) {
  return request.post('/create', data);
}
```

### 4. 复杂请求 (自定义 Header 或 Query)
对于 `post`/`put` 请求，如果需要同时传递 `data` (body) 和 `params` (query)，或自定义配置：

```typescript
// 第三个参数是 config
return request.post('/auth/send-code', null, { params: { email } });
```

## 检查清单
- [ ] 是否引入了 `import request from '@/utils/request'`？
- [ ] 是否使用了 `.get()`, `.post()` 等语义化方法？
- [ ] 是否移除了 `method: 'xxx'` 属性？
- [ ] 是否正确区分了 `params` (GET/Query) 和 `data` (POST/Body)？
