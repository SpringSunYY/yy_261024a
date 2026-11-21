import type { VbenFormSchema } from '#/adapter/form';
import type { OnActionClickFn, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { AutoAssignRuleApi } from '#/api/crm/customer/autoAssignRule';

import { useAccess } from '@vben/access';
import { $t } from '@vben/locales';

import { DICT_TYPE, getDictOptions, getRangePickerDefaultProps } from '#/utils';

const { hasAccessByCodes } = useAccess();

/** 新增/修改的表单 */
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
    },
    /** 触发动作 */
    {
      fieldName: 'triggerActions',
      label: $t('crm.autoAssignRule.field.triggerActions'),
      rules: 'required',
      component: 'I18nSelectToString',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.CRM_AUTO_ASSIGN_RULE_TRIGGER_ACTIONS,
          'string',
        ),
      },
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
    },
    /** 具体条件 */
    {
      fieldName: 'scopeConditions',
      label: $t('crm.autoAssignRule.field.scopeConditions'),
      component: 'Input',
      componentProps: {
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.scopeConditions'),
        ]),
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
    },
    /** 是否放入公海 */
    {
      fieldName: 'isPutPool',
      label: $t('crm.autoAssignRule.field.isPutPool'),
      rules: 'required',
      component: 'I18nSelect',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: $t('ui.placeholder.select', [
          $t('crm.autoAssignRule.field.isPutPool'),
        ]),
      },
    },
    /** 放入公海额度 */
    {
      fieldName: 'isPutPoolQuota',
      label: $t('crm.autoAssignRule.field.isPutPoolQuota'),
      component: 'InputNumber',
      componentProps: {
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.isPutPoolQuota'),
        ]),
      },
    },
    /** 当前分配游标索引 */
    {
      fieldName: 'currentCursorIndex',
      label: $t('crm.autoAssignRule.field.currentCursorIndex'),
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.currentCursorIndex'),
        ]),
      },
    },
    /** 当前分配员工 */
    {
      fieldName: 'currentAssigneeId',
      label: $t('crm.autoAssignRule.field.currentAssigneeId'),
      component: 'InputNumber',
      componentProps: {
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.currentAssigneeId'),
        ]),
      },
    },
    /** 当前分配员工额度 */
    {
      fieldName: 'currentRemainQuota',
      label: $t('crm.autoAssignRule.field.currentRemainQuota'),
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.currentRemainQuota'),
        ]),
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    /** 自动分配 */
    {
      fieldName: 'isEnabled',
      label: $t('crm.autoAssignRule.field.isEnabled'),
      component: 'I18nSelect',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: $t('ui.placeholder.select', [
          $t('crm.autoAssignRule.field.isEnabled'),
        ]),
      },
    },
    /** 触发动作 */
    {
      fieldName: 'triggerActions',
      label: $t('crm.autoAssignRule.field.triggerActions'),
      component: 'I18nCheckbox',
      componentProps: {
        allowClear: true,
        options: getDictOptions(
          DICT_TYPE.CRM_AUTO_ASSIGN_RULE_TRIGGER_ACTIONS,
          'number',
        ),
      },
    },
    /** 适用范围 */
    {
      fieldName: 'scopeType',
      label: $t('crm.autoAssignRule.field.scopeType'),
      component: 'I18nSelect',
      componentProps: {
        allowClear: true,
        options: getDictOptions(
          DICT_TYPE.CRM_AUTO_ASSIGN_RULE_SCOPE_TYPE,
          'number',
        ),
        placeholder: $t('ui.placeholder.select', [
          $t('crm.autoAssignRule.field.scopeType'),
        ]),
      },
    },
    /** 具体条件 */
    {
      fieldName: 'scopeConditions',
      label: $t('crm.autoAssignRule.field.scopeConditions'),
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.scopeConditions'),
        ]),
      },
    },
    /** 分配方案 */
    {
      fieldName: 'allocationMethod',
      label: $t('crm.autoAssignRule.field.allocationMethod'),
      component: 'I18nSelect',
      componentProps: {
        allowClear: true,
        options: getDictOptions(
          DICT_TYPE.CRM_AUTO_ASSIGN_RULE_ALLOCATION_METHOD,
          'number',
        ),
        placeholder: $t('ui.placeholder.select', [
          $t('crm.autoAssignRule.field.allocationMethod'),
        ]),
      },
    },
    /** 循环分配 */
    {
      fieldName: 'isRoundRobin',
      label: $t('crm.autoAssignRule.field.isRoundRobin'),
      component: 'I18nSelect',
      componentProps: {
        allowClear: true,
        options: getDictOptions(
          DICT_TYPE.CRM_AUTO_ASSIGN_RULE_IS_ROUND_ROBIN,
          'number',
        ),
        placeholder: $t('ui.placeholder.select', [
          $t('crm.autoAssignRule.field.isRoundRobin'),
        ]),
      },
    },
    /** 是否放入公海 */
    {
      fieldName: 'isPutPool',
      label: $t('crm.autoAssignRule.field.isPutPool'),
      component: 'I18nSelect',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: $t('ui.placeholder.select', [
          $t('crm.autoAssignRule.field.isPutPool'),
        ]),
      },
    },
    /** 放入公海额度 */
    {
      fieldName: 'isPutPoolQuota',
      label: $t('crm.autoAssignRule.field.isPutPoolQuota'),
      component: 'InputNumber',
      componentProps: {
        allowClear: true,
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.isPutPoolQuota'),
        ]),
      },
    },
    /** 当前分配游标索引 */
    {
      fieldName: 'currentCursorIndex',
      label: $t('crm.autoAssignRule.field.currentCursorIndex'),
      component: 'InputNumber',
      componentProps: {
        allowClear: true,
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.currentCursorIndex'),
        ]),
      },
    },
    /** 当前分配员工 */
    {
      fieldName: 'currentAssigneeId',
      label: $t('crm.autoAssignRule.field.currentAssigneeId'),
      component: 'InputNumber',
      componentProps: {
        allowClear: true,
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.currentAssigneeId'),
        ]),
      },
    },
    /** 当前分配员工额度 */
    {
      fieldName: 'currentRemainQuota',
      label: $t('crm.autoAssignRule.field.currentRemainQuota'),
      component: 'InputNumber',
      componentProps: {
        allowClear: true,
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.autoAssignRule.field.currentRemainQuota'),
        ]),
      },
    },
    /** 创建时间 */
    {
      fieldName: 'createTime',
      label: $t('crm.autoAssignRule.field.createTime'),
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<AutoAssignRuleApi.AutoAssignRule>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    /** 编号 */
    {
      field: 'id',
      title: $t('crm.autoAssignRule.field.id'),
      minWidth: 120,
    },
    /** 自动分配 */
    {
      field: 'isEnabled',
      title: $t('crm.autoAssignRule.field.isEnabled'),
      minWidth: 120,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    /** 触发动作 */
    {
      field: 'triggerActions',
      title: $t('crm.autoAssignRule.field.triggerActions'),
      minWidth: 120,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.CRM_AUTO_ASSIGN_RULE_TRIGGER_ACTIONS },
      },
    },
    /** 适用范围 */
    {
      field: 'scopeType',
      title: $t('crm.autoAssignRule.field.scopeType'),
      minWidth: 120,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.CRM_AUTO_ASSIGN_RULE_SCOPE_TYPE },
      },
    },
    /** 具体条件 */
    {
      field: 'scopeConditions',
      title: $t('crm.autoAssignRule.field.scopeConditions'),
      minWidth: 120,
    },
    /** 分配方案 */
    {
      field: 'allocationMethod',
      title: $t('crm.autoAssignRule.field.allocationMethod'),
      minWidth: 120,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.CRM_AUTO_ASSIGN_RULE_ALLOCATION_METHOD },
      },
    },
    /** 循环分配 */
    {
      field: 'isRoundRobin',
      title: $t('crm.autoAssignRule.field.isRoundRobin'),
      minWidth: 120,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.CRM_AUTO_ASSIGN_RULE_ALLOCATION_METHOD },
      },
    },
    /** 是否放入公海 */
    {
      field: 'isPutPool',
      title: $t('crm.autoAssignRule.field.isPutPool'),
      minWidth: 120,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    /** 放入公海额度 */
    {
      field: 'isPutPoolQuota',
      title: $t('crm.autoAssignRule.field.isPutPoolQuota'),
      minWidth: 120,
    },
    /** 当前分配游标索引 */
    {
      field: 'currentCursorIndex',
      title: $t('crm.autoAssignRule.field.currentCursorIndex'),
      minWidth: 120,
    },
    /** 当前分配员工 */
    {
      field: 'currentAssigneeId',
      title: $t('crm.autoAssignRule.field.currentAssigneeId'),
      minWidth: 120,
    },
    /** 当前分配员工额度 */
    {
      field: 'currentRemainQuota',
      title: $t('crm.autoAssignRule.field.currentRemainQuota'),
      minWidth: 120,
    },
    /** 创建时间 */
    {
      field: 'createTime',
      title: $t('crm.autoAssignRule.field.createTime'),
      minWidth: 120,
      sortable: true,
      formatter: 'formatDateTime',
    },
    {
      title: $t('common.operation'),
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ==================== 子表（CRM  规则人员明细） ====================

/** 新增/修改列表的字段 */
export function useAutoAssignRuleUserGridEditColumns(
  onActionClick?: OnActionClickFn<AutoAssignRuleApi.AutoAssignRuleUser>,
): VxeTableGridOptions<AutoAssignRuleApi.AutoAssignRuleUser>['columns'] {
  return [
    /** 员工 */
    {
      field: 'userId',
      title: $t('crm.autoAssignRuleUser.field.userId'),
      minWidth: 120,
      slots: { default: 'userId' },
    },
    /** 分配数值 */
    {
      field: 'assignValue',
      title: $t('crm.autoAssignRuleUser.field.assignValue'),
      minWidth: 120,
      slots: { default: 'assignValue' },
    },
    /** 状态 */
    {
      field: 'status',
      title: $t('crm.autoAssignRuleUser.field.status'),
      minWidth: 120,
      slots: { default: 'status' },
      params: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
    /** 排序号 */
    {
      field: 'sortOrder',
      title: $t('crm.autoAssignRuleUser.field.sortOrder'),
      minWidth: 120,
      slots: { default: 'sortOrder' },
    },
    {
      title: $t('common.operation'),
      width: 200,
      cellRender: {
        attrs: {
          nameField: 'id',
          nameTitle: '学生',
          onClick: onActionClick,
        },
        name: 'CellOperation',
        options: [
          {
            code: 'edit',
            show: hasAccessByCodes(['crm:auto-assign-rule:update']),
          },
          {
            code: 'delete',
            show: hasAccessByCodes(['crm:auto-assign-rule:create']),
          },
        ],
      },
    },
  ];
}
