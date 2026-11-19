import type { VbenFormSchema } from '@vben/common-ui';

import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { handleTree } from '@vben/utils';

import {
  CrmCustomerLimitConfigApplicableType,
  LimitConfType,
} from '#/api/crm/customer/limitConfig';
import { getSimpleDeptList } from '#/api/system/dept';
import { getSimpleUserList } from '#/api/system/user';
import { $t } from '#/locales';
import { DICT_TYPE, getDictOptions } from '#/utils';

/** 新增/修改的表单 */
export function useFormSchema(confType: LimitConfType): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: $t('crm.customerLimitConfig.field.name'),
      rules: 'required',
      component: 'Input',
    },
    /** 适用类型 */
    {
      fieldName: 'applicableType',
      label: $t('crm.customerLimitConfig.field.applicableType'),
      rules: 'required',
      component: 'I18nRadioGroup',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.CRM_CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE,
          'number',
        ),
        placeholder: $t('ui.placeholder.select', [
          $t('crm.customerLimitConfig.field.applicableType'),
        ]),
      },
    },
    {
      fieldName: 'userIds',
      label: $t('crm.customerLimitConfig.field.userIds'),
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleUserList,
        fieldNames: {
          label: 'nickname',
          value: 'id',
        },
        mode: 'tags',
        allowClear: true,
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['applicableType'],
        if: ({ applicableType }) =>
          applicableType === CrmCustomerLimitConfigApplicableType.USER,
      },
    },
    {
      fieldName: 'deptIds',
      label: $t('crm.customerLimitConfig.field.deptIds'),
      component: 'ApiTreeSelect',
      componentProps: {
        api: async () => {
          const data = await getSimpleDeptList();
          return handleTree(data);
        },
        multiple: true,
        fieldNames: { label: 'name', value: 'id', children: 'children' },
        placeholder: $t('ui.placeholder.select', [
          $t('crm.customerLimitConfig.field.deptIds'),
        ]),
        treeDefaultExpandAll: true,
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['applicableType'],
        if: ({ applicableType }) =>
          applicableType === CrmCustomerLimitConfigApplicableType.DEPT,
      },
    },
    {
      fieldName: 'maxCount',
      label:
        confType === LimitConfType.CUSTOMER_QUANTITY_LIMIT
          ? $t('crm.customerLimitConfig.field.ownerCustomerCountLimit')
          : $t('crm.customerLimitConfig.field.lockCustomerCountLimit'),
      component: 'InputNumber',
    },
    /** 状态 */
    {
      fieldName: 'status',
      label: $t('crm.customerLimitConfig.field.status'),
      rules: 'required',
      component: 'I18nRadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'dealCountEnabled',
      label: $t('crm.customerLimitConfig.field.dealCountOccupied'),
      component: 'I18nRadioGroup',
      componentProps: {
        options: [
          { label: $t('crm.customerLimitConfig.field.yes'), value: true },
          { label: $t('crm.customerLimitConfig.field.no'), value: false },
        ],
      },
      dependencies: {
        triggerFields: [''],
        show: () => confType === LimitConfType.CUSTOMER_QUANTITY_LIMIT,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  confType: LimitConfType,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: $t('crm.customerLimitConfig.field.id'),
      fixed: 'left',
    },
    {
      field: 'name',
      title: $t('crm.customerLimitConfig.field.name'),
    },
    /** 适用类型 */
    {
      field: 'applicableType',
      title: $t('crm.customerLimitConfig.field.applicableType'),
      width: 120,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.CRM_CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE },
      },
    },
    {
      field: 'users',
      title: $t('crm.customerLimitConfig.field.userIds'),
      formatter: ({ cellValue }) => {
        return cellValue
          .map((user: any) => {
            return user.nickname;
          })
          .join(',');
      },
    },
    {
      field: 'depts',
      title: $t('crm.customerLimitConfig.field.deptIds'),
      formatter: ({ cellValue }) => {
        return cellValue
          .map((dept: any) => {
            return dept.name;
          })
          .join(',');
      },
    },
    {
      field: 'maxCount',
      title:
        confType === LimitConfType.CUSTOMER_QUANTITY_LIMIT
          ? $t('crm.customerLimitConfig.field.ownerCustomerCountLimit')
          : $t('crm.customerLimitConfig.field.lockCustomerCountLimit'),
    },
    /** 状态 */
    {
      field: 'status',
      title: $t('crm.customerLimitConfig.field.status'),
      width: 120,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'dealCountEnabled',
      title: $t('crm.customerLimitConfig.field.dealCountOccupied'),
      visible: confType === LimitConfType.CUSTOMER_QUANTITY_LIMIT,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'createTime',
      title: $t('crm.customer.field.createTime'),
      formatter: 'formatDateTime',
    },
    {
      title: $t('common.operation'),
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
