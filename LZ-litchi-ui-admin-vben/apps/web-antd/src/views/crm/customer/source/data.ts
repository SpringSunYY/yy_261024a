import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CustomerSourceApi } from '#/api/crm/customer/source';

import { $t } from '@vben/locales';
import { handleTree } from '@vben/utils';

import { getCustomerSourceList } from '#/api/crm/customer/source';
import {
  CommonStatusEnum,
  DICT_TYPE,
  getDictOptions,
  getRangePickerDefaultProps,
} from '#/utils';
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
    /** 父级编号 */
    {
      fieldName: 'parentId',
      label: $t('crm.customerSource.field.parentId'),
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: async () => {
          const data = await getCustomerSourceList({});
          data.unshift({
            id: 0,
            name: $t('ui.treeRoot', [$t('crm.customerSource.field.parentId')]),
          });
          return handleTree(data);
        },
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
        placeholder: $t('ui.placeholder.select', [
          $t('crm.customerSource.field.parentId'),
        ]),
        treeDefaultExpandAll: true,
      },
      rules: 'selectRequired',
    },
    /** 分类名称 */
    {
      fieldName: 'name',
      label: $t('crm.customerSource.field.name'),
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: $t('ui.placeholder.input', [
          $t('crm.customerSource.field.name'),
        ]),
      },
    },
    /** 状态 */
    {
      fieldName: 'status',
      label: $t('crm.customerSource.field.status'),
      rules: 'required',
      component: 'I18nRadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: CommonStatusEnum.ENABLE,
    },
    /** 排序 */
    {
      fieldName: 'orderNum',
      label: $t('crm.customerSource.field.orderNum'),
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.customerSource.field.orderNum'),
        ]),
      },
      defaultValue: 3,
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    /** 分类名称 */
    {
      fieldName: 'name',
      label: $t('crm.customerSource.field.name'),
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: $t('ui.placeholder.input', [
          $t('crm.customerSource.field.name'),
        ]),
      },
    },
    /** 父级编号 */
    {
      fieldName: 'parentId',
      label: $t('crm.customerSource.field.parentId'),
      component: 'InputNumber',
      componentProps: {
        allowClear: true,
        min: 0,
        controlsPosition: 'right',
        placeholder: $t('ui.placeholder.input', [
          $t('crm.customerSource.field.parentId'),
        ]),
      },
    },
    /** 状态 */
    {
      fieldName: 'status',
      label: $t('crm.customerSource.field.status'),
      component: 'I18nSelect',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: $t('ui.placeholder.select', [
          $t('crm.customerSource.field.status'),
        ]),
      },
    },
    /** 创建时间 */
    {
      fieldName: 'createTime',
      label: $t('crm.customerSource.field.createTime'),
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<CustomerSourceApi.CustomerSource>['columns'] {
  return [
    /** 分类编号 */
    {
      field: 'id',
      title: $t('crm.customerSource.field.id'),
      minWidth: 120,
    },
    /** 分类名称 */
    {
      field: 'name',
      title: $t('crm.customerSource.field.name'),
      minWidth: 120,
      treeNode: true,
    },
    /** 父级编号 */
    {
      field: 'parentId',
      title: $t('crm.customerSource.field.parentId'),
      minWidth: 120,
    },
    /** 状态 */
    {
      field: 'status',
      title: $t('crm.customerSource.field.status'),
      minWidth: 120,
      cellRender: {
        name: 'CellI18nDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    /** 排序 */
    {
      field: 'orderNum',
      title: $t('crm.customerSource.field.orderNum'),
      minWidth: 120,
      sortable: true,
    },
    /** 创建时间 */
    {
      field: 'createTime',
      title: $t('crm.customerSource.field.createTime'),
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
