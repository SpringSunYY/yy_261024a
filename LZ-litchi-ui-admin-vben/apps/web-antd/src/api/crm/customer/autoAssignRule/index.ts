import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace AutoAssignRuleApi {
  /** CRM  规则人员明细信息 */
  export interface AutoAssignRuleUser {
    id?: number; // 编号
    ruleId?: number; // 规则
    userId?: number; // 员工
    assignValue?: number; // 分配数值
    status?: number; // 状态
    sortOrder?: number; // 排序号
  }

  /** CRM 自动分配规则信息 */
  export interface AutoAssignRule {
    id?: number; // 编号
    isEnabled?: number; // 自动分配
    triggerActions?: number[]; // 触发动作（数据库中以 JSON 数组存储）
    scopeType?: number; // 适用范围
    sourceIds?: number[]; // 具体条件 - 客户来源编号列表
    industryIds?: number[]; // 具体条件 - 客户所属行业编号列表
    levelIds?: number[]; // 具体条件 - 客户级别编号列表
    allocationMethod?: number; // 分配方案
    isRoundRobin?: number; // 循环分配
    putPoolQuota?: number; // 放入公海额度（0 表示不放入公海）
    nextUserQuota?: number; // 下一个用户额度（0 表示不转交给下一个用户）
    oldOwnerHandler?: boolean; // 老负责人处理方式（true = 加入团队，false = 移除）
    oldOwnerPermissionLevel?: number; // 老负责人权限级别（不能是负责人 OWNER）
    toBizTypes?: number[]; // 同时转移的关联业务类型（联系人 / 商机 / 合同）
    currentCursorIndex?: number; // 当前分配游标索引
    currentAssigneeId?: number; // 当前分配员工
    currentRemainQuota?: number; // 当前分配员工额度
    createTime?: string; // 创建时间
  }
}

/** 查询CRM 自动分配规则分页 */
export function getAutoAssignRulePage(params: PageParam) {
  return requestClient.get<PageResult<AutoAssignRuleApi.AutoAssignRule>>(
    '/crm/auto-assign-rule/page',
    { params },
  );
}

/** 查询CRM 自动分配规则详情（全局只有一条配置） */
export function getAutoAssignRule() {
  return requestClient.get<AutoAssignRuleApi.AutoAssignRule>(
    '/crm/auto-assign-rule/get',
  );
}

/** 保存CRM 自动分配规则（存在则更新，不存在则新增） */
export function saveAutoAssignRule(data: AutoAssignRuleApi.AutoAssignRule) {
  return requestClient.put<number>('/crm/auto-assign-rule/save', data);
}

/** 删除CRM 自动分配规则 */
export function deleteAutoAssignRule(id: number) {
  return requestClient.delete(`/crm/auto-assign-rule/delete?id=${id}`);
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

/** 新增CRM  规则人员明细 */
export function createAutoAssignRuleUser(
  ruleId: number,
  data: AutoAssignRuleApi.AutoAssignRuleUser,
) {
  return requestClient.post<number>(
    `/crm/auto-assign-rule/auto-assign-rule-user/create?ruleId=${ruleId}`,
    data,
  );
}

/** 修改CRM  规则人员明细 */
export function updateAutoAssignRuleUser(
  ruleId: number,
  data: AutoAssignRuleApi.AutoAssignRuleUser,
) {
  return requestClient.put(
    `/crm/auto-assign-rule/auto-assign-rule-user/update?ruleId=${ruleId}`,
    data,
  );
}

/** 更新CRM  规则人员明细的排序号（ids 按目标顺序排列） */
export function updateAutoAssignRuleUserSort(ruleId: number, ids: number[]) {
  return requestClient.put(
    `/crm/auto-assign-rule/auto-assign-rule-user/update-sort?ruleId=${ruleId}`,
    ids,
  );
}

/** 删除CRM  规则人员明细 */
export function deleteAutoAssignRuleUser(id: number) {
  return requestClient.delete(
    `/crm/auto-assign-rule/auto-assign-rule-user/delete?id=${id}`,
  );
}
