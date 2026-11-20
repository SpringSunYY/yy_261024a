import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace CustomerSourceApi {
  /** CRM 客户来源信息 */
  export interface CustomerSource {
    id: number; // 分类编号
    name?: string; // 分类名称
    parentId?: number; // 父级编号
    status?: number; // 状态
    orderNum?: number; // 排序
  children?: CustomerSource[];
  }
}

/** 查询CRM 客户来源列表 */
export function getCustomerSourceList(params: any) {
  return requestClient.get<CustomerSourceApi.CustomerSource[]>('/crm/customer-source/list', { params });
}

/** 查询CRM 客户来源详情 */
export function getCustomerSource(id: number) {
  return requestClient.get<CustomerSourceApi.CustomerSource>(`/crm/customer-source/get?id=${id}`);
}

/** 新增CRM 客户来源 */
export function createCustomerSource(data: CustomerSourceApi.CustomerSource) {
  return requestClient.post('/crm/customer-source/create', data);
}

/** 修改CRM 客户来源 */
export function updateCustomerSource(data: CustomerSourceApi.CustomerSource) {
  return requestClient.put('/crm/customer-source/update', data);
}

/** 删除CRM 客户来源 */
export function deleteCustomerSource(id: number, isDeleteChildren = false) {
  return requestClient.delete(
    `/crm/customer-source/delete?id=${id}&isDeleteChildren=${isDeleteChildren}`,
  );
}


/** 导出CRM 客户来源 */
export function exportCustomerSource(params: CustomerSourceApi.CustomerSource) {
  return requestClient.download('/crm/customer-source/export-excel',
          {
            params,
            timeout:300_00
          });
}

/** 获取CRM 客户来源导入模板 */
export function importCustomerSourceTemplate() {
  return requestClient.download('/crm/customer-source/get-import-template');
}

/** 导入CRM 客户来源 */
export function importCustomerSource(file: File) {
  return requestClient.upload('/crm/customer-source/import',
          {file},
          {timeout:300_00}
  );
}