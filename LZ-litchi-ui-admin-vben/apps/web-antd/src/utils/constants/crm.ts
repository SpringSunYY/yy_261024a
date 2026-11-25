// =====crm枚举=====

/** 默认商机状态（name/key 为显示用中文，渲染层会通过 $t 翻译） */
export const DEFAULT_STATUSES = [
  {
    endStatus: 1,
    key: '结束',
    name: '赢单',
    percent: 100,
  },
  {
    endStatus: 2,
    key: '结束',
    name: '输单',
    percent: 0,
  },
  {
    endStatus: 3,
    key: '结束',
    name: '无效',
    percent: 0,
  },
];

/** CRM自动分配规则 */
export const CrmAutoAssignRuleIsRoundRobin = {
  /** 循环分配 */
  IS_ROUND_ROBIN_1: 1,

  /** 随机分配 */
  IS_ROUND_ROBIN_2: 2,
} as const;

/** CRM自动分配规则 */
export const CrmAutoAssignRuleAllocationMethod = {
  /** 按数量分配 */
  ALLOCATION_METHOD_1: 1,

  /** 平均分配 */
  ALLOCATION_METHOD_2: 2,
} as const;

/** CRM自动分配规则 */
export const CrmAutoAssignRuleScopeType = {
  /** 全部 */
  SCOPE_TYPE_1: 1,

  /** 满足条件 */
  SCOPE_TYPE_2: 2,
} as const;

/** CRM自动分配规则 */
export const CrmAutoAssignRuleTriggerActions = {
  /** 新增 */
  TRIGGER_ACTIONS_1: 1,

  /** 转移 */
  TRIGGER_ACTIONS_2: 2,

  /** 导入 */
  TRIGGER_ACTIONS_3: 3,

  /** 公海分配 */
  TRIGGER_ACTIONS_4: 4,
} as const;
