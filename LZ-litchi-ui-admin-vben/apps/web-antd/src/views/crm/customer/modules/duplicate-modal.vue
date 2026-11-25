<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmCustomerApi } from '#/api/crm/customer';

import { useVbenModal } from '@vben/common-ui';

import { Button, message } from 'ant-design-vue';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { getDuplicateCustomerPage } from '#/api/crm/customer';
import { $t } from '#/locales';

import { useDuplicateGridFormSchema, useGridColumns } from '../data';

defineOptions({ name: 'CrmCustomerDuplicateModal' });

const emit = defineEmits(['select']);

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useDuplicateGridFormSchema(),
    showCollapseButton: false,
  },
  gridOptions: {
    columns: useGridColumns(),
    // 固定高度，避免表格内容过多时撑开弹窗
    height: 500,
    keepSource: true,
    proxyConfig: {
      // 不自动加载，必须用户主动点击「搜索」
      autoLoad: false,
      ajax: {
        query: async ({ page }, formValues) => {
          // 搜索条件不能完全为空：客户名称、手机号码至少填写一个
          const name = formValues?.name?.trim();
          const mobile = formValues?.mobile?.trim();
          if (!name && !mobile) {
            message.warning(
              $t('crm.customer.message.duplicateConditionRequired'),
            );
            return { list: [], total: 0 };
          }
          return await getDuplicateCustomerPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
    },
    toolbarConfig: {
      enabled: false,
    },
  } as VxeTableGridOptions<CrmCustomerApi.Customer>,
});

const [Modal, modalApi] = useVbenModal({
  showConfirmButton: false,
  destroyOnClose: true,
});

/** 选择客户 */
function handleSelect(row: CrmCustomerApi.Customer) {
  emit('select', row);
  modalApi.close();
}
</script>

<template>
  <Modal
    :title="$t('crm.customer.message.duplicateTitle')"
    class="w-4/5"
    :footer="false"
  >
    <div class="mb-2 text-xs text-gray-500">
      {{ $t('crm.customer.message.duplicateHelp') }}
    </div>
    <Grid>
      <template #name="{ row }">
        <Button type="link" @click="handleSelect(row)">
          {{ row.name }}
        </Button>
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.detail'),
              type: 'link',
              icon: ACTION_ICON.VIEW,
              onClick: handleSelect.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Modal>
</template>
