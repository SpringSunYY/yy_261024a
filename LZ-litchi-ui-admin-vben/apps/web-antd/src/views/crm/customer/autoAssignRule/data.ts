import type { VbenFormSchema } from '#/adapter/form';

import { $t } from '@vben/locales';

import { BizTypeEnum, PermissionLevelEnum } from '#/api/crm/permission';
import { CommonStatusEnum, DICT_TYPE, getDictOptions } from '#/utils';
import {
  CrmAutoAssignRuleAllocationMethod,
  CrmAutoAssignRuleIsRoundRobin,
  CrmAutoAssignRuleScopeType,
} from '#/utils/constants/crm';

/** 适用范围 = 满足条件 */
const SCOPE_TYPE_CONDITION = CrmAutoAssignRuleScopeType.SCOPE_TYPE_2;

/**
 * CRM 自动分配规则 - 配置表单（全局只有一条规则）
 *
 * <p>布局：一行两列。适用范围为「满足条件」时，才展示具体条件的 3 个多选字段。
 *
 * <p>「放入公海额度」与「下一个用户额度」用于处理「连续联系不上客户」的场景：
 * 放入公海额度为 0 表示不放入公海，下一个用户额度为 0 表示不转交给下一个用户。
 *
 * <p>「当前分配员工」与「当前分配员工额度」由 index.vue 的插槽渲染，
 * 需要根据规则人员明细动态绑定选项与上限。
 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    /** 自动分配 */
    {
      fieldName: 'isEnabled',
      label: $t('crm.autoAssignRule.field.isEnabled'),
      rules: 'required',
      component: 'I18nRadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: CommonStatusEnum.ENABLE,
    },
    /** 触发动作（多选，数据库中以 JSON 数组存储） */
    {
      fieldName: 'triggerActions',
      label: $t('crm.autoAssignRule.field.triggerActions'),
      rules: 'required',
      component: 'I18nCheckboxGroup',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.CRM_AUTO_ASSIGN_RULE_TRIGGER_ACTIONS,
          'number',
        ),
      },
      defaultValue: [],
    },
    /** 适用范围 */
    {
      fieldName: 'scopeType',
      label: $t('crm.autoAssignRule.field.scopeType'),
      rules: 'required',
      component: 'I18nRadioGroup',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.CRM_AUTO_ASSIGN_RULE_SCOPE_TYPE,
          'number',
        ),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: CrmAutoAssignRuleScopeType.SCOPE_TYPE_1,
    },
    /**
     * 客户来源（多选，仅「满足条件」时显示）
     *
     * <p>实际渲染由 index.vue 的插槽完成（树形勾选面板），
     * 这里的配置仅作为插槽未生效时的兜底。
     *
     * <p>与其他条件的关系：来源 / 行业 / 级别三者之间为「或」关系，
     * 任一命中即视为满足条件；每个条件内部为「或」关系（列表中任一编号命中即可）。
     */
    {
      fieldName: 'sourceIds',
      label: $t('crm.autoAssignRule.field.scopeSource'),
      component: 'Input',
      dependencies: {
        triggerFields: ['scopeType'],
        // 切换适用范围时保留原有的条件值，不做清空
        show: (values) => values.scopeType === SCOPE_TYPE_CONDITION,
      },
    },
    /** 所属行业（多选，仅「满足条件」时显示） */
    {
      fieldName: 'industryIds',
      label: $t('crm.autoAssignRule.field.scopeIndustry'),
      component: 'I18nCheckboxGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.CRM_CUSTOMER_INDUSTRY, 'number'),
      },
      defaultValue: [],
      dependencies: {
        triggerFields: ['scopeType'],
        show: (values) => values.scopeType === SCOPE_TYPE_CONDITION,
      },
    },
    /** 客户级别（多选，仅「满足条件」时显示） */
    {
      fieldName: 'levelIds',
      label: $t('crm.autoAssignRule.field.scopeLevel'),
      component: 'I18nCheckboxGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.CRM_CUSTOMER_LEVEL, 'number'),
      },
      defaultValue: [],
      dependencies: {
        triggerFields: ['scopeType'],
        show: (values) => values.scopeType === SCOPE_TYPE_CONDITION,
      },
    },
    /** 分配方案 */
    {
      fieldName: 'allocationMethod',
      label: $t('crm.autoAssignRule.field.allocationMethod'),
      rules: 'required',
      component: 'I18nRadioGroup',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.CRM_AUTO_ASSIGN_RULE_ALLOCATION_METHOD,
          'number',
        ),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: CrmAutoAssignRuleAllocationMethod.ALLOCATION_METHOD_1,
    },
    /** 循环分配 */
    {
      fieldName: 'isRoundRobin',
      label: $t('crm.autoAssignRule.field.isRoundRobin'),
      rules: 'required',
      component: 'I18nRadioGroup',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.CRM_AUTO_ASSIGN_RULE_IS_ROUND_ROBIN,
          'number',
        ),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: CrmAutoAssignRuleIsRoundRobin.IS_ROUND_ROBIN_1,
    },
    /**
     * 放入公海额度
     *
     * <p>连续 N 次联系不上客户时，将客户放入公海（负责人保持不变）；为 0 时表示不放入公海。
     */
    {
      fieldName: 'putPoolQuota',
      label: $t('crm.autoAssignRule.field.putPoolQuota'),
      rules: 'required',
      component: 'InputNumber',
      help: $t('crm.autoAssignRule.help.putPoolQuota'),
      componentProps: {
        min: 0,
        defaultValue: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.putPoolQuota'),
        ]),
      },
      defaultValue: 0,
    },
    /**
     * 下一个用户额度
     *
     * <p>连续 N 次联系不上客户时，将客户转交给下一个用户作为负责人；为 0 时表示不转交。
     */
    {
      fieldName: 'nextUserQuota',
      label: $t('crm.autoAssignRule.field.nextUserQuota'),
      rules: 'required',
      component: 'InputNumber',
      help: $t('crm.autoAssignRule.help.nextUserQuota'),
      componentProps: {
        min: 0,
        defaultValue: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.nextUserQuota'),
        ]),
      },
      defaultValue: 0,
    },
    /**
     * 老负责人处理方式
     *
     * <p>自动转交给下一个用户时，老负责人的处理方式：
     * true - 保留在团队中（降级为团队成员，权限级别见 oldOwnerPermissionLevel）；
     * false - 移出团队（移除其数据权限）。
     *
     * <p>这样可以避免出现「一个客户多个负责人」的情况。
     */
    {
      fieldName: 'oldOwnerHandler',
      label: $t('crm.autoAssignRule.field.oldOwnerHandler'),
      rules: 'required',
      component: 'I18nRadioGroup',
      componentProps: {
        options: [
          {
            label: $t('crm.permission.field.add'),
            value: true,
          },
          {
            label: $t('crm.permission.field.remove'),
            value: false,
          },
        ],
      },
      defaultValue: false,
    },
    /**
     * 老负责人权限级别
     *
     * <p>老负责人保留在团队中时的权限级别，只读 / 读写，不能是「负责人（OWNER）」。
     */
    {
      fieldName: 'oldOwnerPermissionLevel',
      label: $t('crm.autoAssignRule.field.oldOwnerPermissionLevel'),
      component: 'I18nRadioGroup',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.CRM_PERMISSION_LEVEL,
          'number',
        ).filter((dict) => dict.value !== PermissionLevelEnum.OWNER),
      },
      dependencies: {
        triggerFields: ['oldOwnerHandler'],
        show: (values) => values.oldOwnerHandler,
        trigger(values) {
          if (!values.oldOwnerHandler) {
            values.oldOwnerPermissionLevel = undefined;
          }
        },
      },
      rules: 'required',
    },
    /**
     * 同时转移的关联业务类型
     *
     * <p>自动转交给下一个用户时，同时转移的关联业务：联系人、商机、合同。
     */
    {
      fieldName: 'toBizTypes',
      label: $t('crm.autoAssignRule.field.toBizTypes'),
      component: 'CheckboxGroup',
      componentProps: {
        options: [
          {
            label: $t('crm.permission.field.contact'),
            value: BizTypeEnum.CRM_CONTACT,
          },
          {
            label: $t('crm.permission.field.business'),
            value: BizTypeEnum.CRM_BUSINESS,
          },
          {
            label: $t('crm.permission.field.contract'),
            value: BizTypeEnum.CRM_CONTRACT,
          },
        ],
      },
      defaultValue: [],
    },
    /**
     * 当前分配员工
     *
     * <p>实际渲染由 index.vue 的插槽完成（需要动态绑定规则人员明细里的员工），
     * 这里的配置仅作为插槽未生效时的兜底。
     */
    {
      fieldName: 'currentAssigneeId',
      label: $t('crm.autoAssignRule.field.currentAssigneeId'),
      component: 'Select',
      componentProps: {
        options: [],
        allowClear: true,
        placeholder: $t('ui.placeholder.select', [
          $t('crm.autoAssignRule.field.currentAssigneeId'),
        ]),
      },
      defaultValue: undefined,
    },
    /**
     * 当前分配员工额度
     *
     * <p>实际渲染由 index.vue 的插槽完成（需要动态绑定 max = 当前分配员工的分配数值），
     * 这里的配置仅作为插槽未生效时的兜底。
     */
    {
      fieldName: 'currentRemainQuota',
      label: $t('crm.autoAssignRule.field.currentRemainQuota'),
      component: 'InputNumber',
      componentProps: {
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.currentRemainQuota'),
        ]),
      },
      defaultValue: 0,
    },
    /** 当前分配游标索引（只读，由后端根据当前分配员工的顺序计算，从 1 开始） */
    {
      fieldName: 'currentCursorIndex',
      label: $t('crm.autoAssignRule.field.currentCursorIndex'),
      component: 'InputNumber',
      componentProps: {
        min: 0,
        disabled: true,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.currentCursorIndex'),
        ]),
      },
      defaultValue: 0,
    },
  ];
}
