import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios';
import { Message, Modal } from '@arco-design/web-vue';

// 定义后端返回的数据结构
export interface Result<T = any> {
  code: number;
  msg: string;
  data: T;
}

class RequestHttp {
  service: AxiosInstance;

  constructor(config: AxiosRequestConfig) {
    this.service = axios.create(config);

    // 请求拦截器
    this.service.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token');
        if (token) {
          config.headers['Authorization'] = token;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // 响应拦截器
    this.service.interceptors.response.use(
      (response: AxiosResponse) => {
        const res = response.data as Result;
        // 业务状态码判断
        if (res.code !== 200) {
          Message.error(res.msg || 'Error');
          
          // Token 失效处理
          if (res.code === 401) {
            Modal.warning({
              title: '登录已过期',
              content: '您的登录状态已失效，请重新登录',
              okText: '重新登录',
              onOk: () => {
                localStorage.clear();
                window.location.href = '/login';
              },
            });
          }
          return Promise.reject(new Error(res.msg || 'Error'));
        }
        return res as any;
      },
      (error) => {
        const { response } = error;
        if (response && response.status === 429) {
          Message.error('请求过于频繁，请稍后再试');
        } else {
          Message.error(error.message || '网络异常');
        }
        return Promise.reject(error);
      }
    );
  }

  // 常用方法封装
  get<T>(url: string, params?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    return this.service.get(url, { params, ...config });
  }

  post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    return this.service.post(url, data, config);
  }

  put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    return this.service.put(url, data, config);
  }

  delete<T>(url: string, params?: any, config?: AxiosRequestConfig): Promise<Result<T>> {
    return this.service.delete(url, { params, ...config });
  }
}

// 导出实例
export default new RequestHttp({
  baseURL: '/api',
  timeout: 10000,
});
