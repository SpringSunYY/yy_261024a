import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AutoAssignRuleApi {
  /** CRM  规则人员明细信息 */
  export interface AutoAssignRuleUser {
    id: number; // 编号
    ruleId?: number; // 规则
    userId?: number; // 员工
    assignValue?: number; // 分配数值
    status?: number; // 状态
    sortOrder?: number; // 排序号
  }

  /** CRM 自动分配规则信息 */
  export interface AutoAssignRule {
    id: number; // 编号
    isEnabled?: number; // 自动分配
    triggerActions?: number; // 触发动作
    scopeType?: number; // 适用范围
    scopeConditions: string; // 具体条件
    allocationMethod?: number; // 分配方案
    isRoundRobin?: number; // 循环分配
    isPutPool?: number; // 是否放入公海
    isPutPoolQuota: number; // 放入公海额度
    currentCursorIndex?: number; // 当前分配游标索引
    currentAssigneeId: number; // 当前分配员工
    currentRemainQuota?: number; // 当前分配员工额度
    autoAssignRuleUsers?: AutoAssignRuleUser[];
  }
}

/** 查询CRM 自动分配规则分页 */
export function getAutoAssignRulePage(params: PageParam) {
  return requestClient.get<PageResult<AutoAssignRuleApi.AutoAssignRule>>(
    '/crm/auto-assign-rule/page',
    { params },
  );
}

/** 查询CRM 自动分配规则详情 */
export function getAutoAssignRule(id: number) {
  return requestClient.get<AutoAssignRuleApi.AutoAssignRule>(
    `/crm/auto-assign-rule/get?id=${id}`,
  );
}

/** 新增CRM 自动分配规则 */
export function createAutoAssignRule(data: AutoAssignRuleApi.AutoAssignRule) {
  return requestClient.post('/crm/auto-assign-rule/create', data);
}

/** 修改CRM 自动分配规则 */
export function updateAutoAssignRule(data: AutoAssignRuleApi.AutoAssignRule) {
  return requestClient.put('/crm/auto-assign-rule/update', data);
}

/** 删除CRM 自动分配规则 */
export function deleteAutoAssignRule(id: number) {
  return requestClient.delete(`/crm/auto-assign-rule/delete?id=${id}`);
}

/** 批量删除CRM 自动分配规则 */
export function deleteAutoAssignRuleList(ids: number[]) {
  return requestClient.delete(
    `/crm/auto-assign-rule/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出CRM 自动分配规则 */
export function exportAutoAssignRule(params: AutoAssignRuleApi.AutoAssignRule) {
  return requestClient.download('/crm/auto-assign-rule/export-excel', {
    params,
    timeout: 30_000,
  });
}

// ==================== 子表（CRM  规则人员明细） ====================

/** 获得CRM  规则人员明细列表 */
export function getAutoAssignRuleUserListByRuleId(ruleId: number) {
  return requestClient.get<AutoAssignRuleApi.AutoAssignRuleUser[]>(
    `/crm/auto-assign-rule/auto-assign-rule-user/list-by-rule-id?ruleId=${ruleId}`,
  );
}
