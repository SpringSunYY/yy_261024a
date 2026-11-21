<script lang="ts" setup>
import type { OnActionClickParams } from '#/adapter/vxe-table';
import type { AutoAssignRuleApi } from '#/api/crm/customer/autoAssignRule';

import { h, nextTick, watch } from 'vue';

import { Plus } from '@vben/icons';

import { Button, InputNumber, message, Select } from 'ant-design-vue';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getAutoAssignRuleUserListByRuleId } from '#/api/crm/customer/autoAssignRule';
import { $t } from '#/locales';

import { useAutoAssignRuleUserGridEditColumns } from '../data';

const props = defineProps<{
  ruleId?: number; // 规则（主表的关联字段）
}>();

/** 表格操作按钮的回调函数 */
function onActionClick({
  code,
  row,
}: OnActionClickParams<AutoAssignRuleApi.AutoAssignRuleUser>) {
  switch (code) {
    case 'delete': {
      onDelete(row);
      break;
    }
  }
}

const [Grid, gridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: useAutoAssignRuleUserGridEditColumns(onActionClick),
    border: true,
    showOverflow: true,
    autoResize: true,
    keepSource: true,
    rowConfig: {
      keyField: 'id',
    },
    pagerConfig: {
      enabled: false,
    },
    toolbarConfig: {
      enabled: false,
    },
  },
});

/** 添加CRM  规则人员明细 */
const onAdd = async () => {
  await gridApi.grid.insertAt({} as AutoAssignRuleApi.AutoAssignRuleUser, -1);
};

/** 删除CRM  规则人员明细 */
const onDelete = async (row: AutoAssignRuleApi.AutoAssignRuleUser) => {
  await gridApi.grid.remove(row);
};

/** 提供获取表格数据的方法供父组件调用 */
defineExpose({
  getData: (): AutoAssignRuleApi.AutoAssignRuleUser[] => {
    const data =
      gridApi.grid.getData() as AutoAssignRuleApi.AutoAssignRuleUser[];
    const removeRecords =
      gridApi.grid.getRemoveRecords() as AutoAssignRuleApi.AutoAssignRuleUser[];
    const insertRecords =
      gridApi.grid.getInsertRecords() as AutoAssignRuleApi.AutoAssignRuleUser[];
    return data
      .filter((row) => !removeRecords.some((removed) => removed.id === row.id))
      ?.concat(insertRecords.map((row: any) => ({ ...row, id: undefined })));
  },
  /** 校验子表数据：返回 true 通过；返回 string[] 所有未通过的错误信息 */
  validate: (): boolean => {
    // 同时取全量数据 + 当前数据，确保即便有删除/未挂载的行也能覆盖
    const data =
      (gridApi.grid.getTableData?.()
        .fullData as AutoAssignRuleApi.AutoAssignRuleUser[]) ||
      (gridApi.grid.getData() as AutoAssignRuleApi.AutoAssignRuleUser[]);
    const emptyFieldLabels: Record<string, string> = {
      userId: $t('crm.autoAssignRuleUser.field.userId'),
      assignValue: $t('crm.autoAssignRuleUser.field.assignValue'),
      status: $t('crm.autoAssignRuleUser.field.status'),
      sortOrder: $t('crm.autoAssignRuleUser.field.sortOrder'),
    };
    for (const [i, row] of data.entries()) {
      for (const [field, label] of Object.entries(emptyFieldLabels)) {
        const value = (row as any)[field];
        if (value === undefined || value === null || value === '') {
          message.warn($t('ui.placeholder.subTableInput', [i + 1, label]));
          return false;
        }
      }
    }
    return true;
  },
});

/** 监听主表的关联字段的变化，加载对应的子表数据 */
watch(
  () => props.ruleId,
  async (val) => {
    if (!val) {
      return;
    }
    await nextTick();
    await gridApi.grid.loadData(
      await getAutoAssignRuleUserListByRuleId(props.ruleId!),
    );
  },
  { immediate: true },
);
</script>

<template>
  <Grid class="mx-4">
    <template #userId="{ row }">
      <InputNumber v-model:value="row.userId" />
    </template>
    <template #assignValue="{ row }">
      <InputNumber v-model:value="row.assignValue" />
    </template>
    <template #status="{ row, column }">
      <Select v-model:value="row.status" class="w-full">
        <Select.Option
          v-for="option in column.params.options"
          :key="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </Select.Option>
      </Select>
    </template>
    <template #sortOrder="{ row }">
      <InputNumber v-model:value="row.sortOrder" />
    </template>
  </Grid>
  <div class="-mt-4 flex justify-center">
    <Button
      :icon="h(Plus)"
      type="primary"
      ghost
      @click="onAdd"
      v-access:code="['crm:auto-assign-rule:create']"
    >
      {{
        $t('ui.actionTitle.create', [
          $t('crm.autoAssignRuleUser.autoAssignRuleUser'),
        ])
      }}
    </Button>
  </div>
</template>
