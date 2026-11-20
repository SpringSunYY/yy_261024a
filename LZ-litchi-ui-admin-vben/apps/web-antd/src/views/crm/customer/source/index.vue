<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CustomerSourceApi } from '#/api/crm/customer/source';

import { ref } from 'vue';

import { Page, useVbenModelDrawer } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteCustomerSource,
  exportCustomerSource,
  getCustomerSourceList,
} from '#/api/crm/customer/source';
import { CascadeDeleteSwitch } from '#/components/cascade-delete-switch';
import { $t } from '#/locales';
import { pickSort } from '#/utils';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModalDrawer, formModalDrawerApi] = useVbenModelDrawer({
  connectedComponent: Form,
  destroyOnClose: true,
  type: 'modal',
});

/** 切换树形展开/收缩状态 */
const isExpanded = ref(true);
function toggleExpand() {
  isExpanded.value = !isExpanded.value;
  gridApi.grid.setAllTreeExpand(isExpanded.value);
}

/** 刷新表格 */
function onRefresh() {
  gridApi.query();
}

/** 创建CRM 客户来源 */
function handleCreate() {
  formModalDrawerApi.setData({}).open();
}

/** 编辑CRM 客户来源 */
function handleEdit(row: CustomerSourceApi.CustomerSource) {
  formModalDrawerApi.setData(row).open();
}

/** 新增下级CRM 客户来源 */
function handleAppend(row: CustomerSourceApi.CustomerSource) {
  formModalDrawerApi.setData({ parentId: row.id }).open();
}

/** 全局级联删除开关（默认 false） */
const isDeleteChildren = ref(false);

/** 删除CRM 客户来源 */
async function handleDelete(row: CustomerSourceApi.CustomerSource) {
  const hideLoading = message.loading({
    content: $t('ui.actionMessage.deleting', [row.id]),
    key: 'action_key_msg',
  });
  try {
    await deleteCustomerSource(row.id as number, isDeleteChildren.value);
    message.success({
      content: $t('ui.actionMessage.deleteSuccess', [row.id]),
      key: 'action_key_msg',
    });
    onRefresh();
  } finally {
    hideLoading();
  }
}

/** 导出CRM 客户来源 */
const exportLoading = ref(false);
async function handleExport() {
  try {
    exportLoading.value = true;
    message.loading({
      content: $t('ui.actionMessage.exporting'),
      key: 'action_key_msg',
    });
    const data = await exportCustomerSource(await gridApi.formApi.getValues());
    downloadFileFromBlobPart({
      fileName: `${$t('crm.customerSource.source')}.xls`,
      source: data,
    });
  } finally {
    exportLoading.value = false;
  }
}

const [Grid, gridApi] = useVbenVxeGrid<CustomerSourceApi.CustomerSource>({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    treeConfig: {
      parentField: 'parentId',
      rowField: 'id',
      transform: true,
      expandAll: true,
      reserve: true,
    },
    pagerConfig: {
      enabled: false,
    },
    proxyConfig: {
      ajax: {
        query: async (ctx, formValues) => {
          const { sortBy, sort } = pickSort(ctx);
          return await getCustomerSourceList({
            ...formValues,
            ...(sortBy?.length ? { sortBy, sort } : {}),
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: { code: 'query' },
      search: true,
    },
    sortConfig: {
      remote: true,
      multiple: true,
    },
  } as VxeTableGridOptions,
  gridEvents: {
    sortChange: () => gridApi.query(),
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModalDrawer @success="onRefresh" />

    <Grid :table-title="$t('crm.customerSource.list')">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: isExpanded ? $t('common.collapse') : $t('common.expand'),
              type: 'primary',
              onClick: toggleExpand,
            },
            {
              label: $t('ui.actionTitle.create', [
                $t('crm.customerSource.customerSource'),
              ]),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['crm:customer-source:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export', [
                $t('crm.customerSource.customerSource'),
              ]),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['crm:customer-source:export'],
              onClick: handleExport,
              loading: exportLoading,
            },
          ]"
        />
        <CascadeDeleteSwitch v-model="isDeleteChildren" />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.append'),
              type: 'link',
              icon: ACTION_ICON.ADD,
              auth: ['crm:customer-source:create'],
              onClick: handleAppend.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'link',
              icon: ACTION_ICON.EDIT,
              auth: ['crm:customer-source:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'link',
              danger: true,
              icon: ACTION_ICON.DELETE,
              auth: ['crm:customer-source:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteCascadeConfirm', [
                  $t('crm.customerSource.source'),
                ]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
