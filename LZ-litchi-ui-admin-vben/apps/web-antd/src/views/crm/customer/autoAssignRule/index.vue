<script lang="ts" setup>
import type { AutoAssignRuleApi } from '#/api/crm/customer/autoAssignRule';

import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Button, Card, InputNumber, message, Select } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  getAutoAssignRule,
  getAutoAssignRuleUserListByRuleId,
  saveAutoAssignRule,
} from '#/api/crm/customer/autoAssignRule';
import { getSimpleUserList } from '#/api/system/user';
import { $t } from '#/locales';
import { CrmAutoAssignRuleAllocationMethod } from '#/utils/constants/crm';

import { useFormSchema } from './data';
import AutoAssignRuleUserForm from './modules/auto-assign-rule-user-form.vue';
import CustomerSourceTreeSelect from './modules/customer-source-tree-select.vue';

/** 规则数据（全局只有一条） */
const ruleData = ref<AutoAssignRuleApi.AutoAssignRule>({});
/** 子表组件实例 */
const ruleUserFormRef = ref<InstanceType<typeof AutoAssignRuleUserForm>>();
/** 「当前分配员工」的下拉选项（顺序与规则人员明细一致） */
const assigneeOptions = ref<
  { assignValue: number; label: string; value: number }[]
>([]);
/** 当前分配员工的分配数值（即「当前分配员工额度」的最大值） */
const currentAssigneeQuotaMax = ref<number | undefined>(undefined);
/** 分配方案（平均分配时，「当前分配员工额度」只读） */
const allocationMethod = ref<number | undefined>(undefined);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 140,
  },
  // 一行两列
  wrapperClass: 'grid-cols-2',
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
  handleValuesChange: (values, changedFields) => {
    if (changedFields.includes('allocationMethod')) {
      allocationMethod.value = values.allocationMethod;
    }
  },
});

/** 标题 */
const title = computed(() => $t('crm.autoAssignRule.field.ruleTitle'));

/**
 * 加载「当前分配员工」的下拉选项
 *
 * <p>只允许从规则人员明细中选择，选项的顺序与明细的顺序一致。
 *
 * <p>同时会同步「当前分配游标索引」：人员明细的顺序变化后（新增、删除、拖动排序），
 * 当前分配员工在明细中的位置也会变化。
 */
async function loadAssigneeOptions() {
  const ruleId = ruleData.value.id;
  if (!ruleId) {
    assigneeOptions.value = [];
    return;
  }
  const [ruleUserList, userList] = await Promise.all([
    getAutoAssignRuleUserListByRuleId(ruleId),
    getSimpleUserList(),
  ]);
  const userMap = new Map(
    (userList ?? []).map((user) => [user.id, user.nickname ?? '']),
  );
  // 只保留规则人员明细里的员工，并按明细顺序排列
  assigneeOptions.value = (ruleUserList ?? [])
    .filter(
      (ruleUser) => ruleUser.userId !== undefined && ruleUser.userId !== null,
    )
    .map((ruleUser) => ({
      assignValue: ruleUser.assignValue ?? 0,
      label: userMap.get(ruleUser.userId) ?? String(ruleUser.userId),
      value: ruleUser.userId as number,
    }));
  // 同步额度上限（不改动已保存的额度值）
  const assignee = assigneeOptions.value.find(
    (option) => option.value === ruleData.value.currentAssigneeId,
  );
  currentAssigneeQuotaMax.value = assignee?.assignValue;
  // 同步游标索引：当前分配员工在明细中的位置（从 1 开始）
  syncCursorIndex(ruleData.value.currentAssigneeId);
}

/**
 * 同步「当前分配游标索引」
 *
 * <p>游标索引 = 当前分配员工在规则人员明细中的顺序（从 1 开始，与后端一致）。
 *
 * @param assigneeId 当前分配员工编号
 */
function syncCursorIndex(assigneeId?: null | number) {
  const index = assigneeOptions.value.findIndex(
    (option) => option.value === assigneeId,
  );
  formApi.setFieldValue('currentCursorIndex', index === -1 ? 0 : index + 1);
}

/**
 * 切换当前分配员工后，同步「当前分配员工额度」与「当前分配游标索引」
 *
 * <p>额度直接取当前分配员工的分配数值，同时作为额度的最大值；
 * 游标索引为该员工在规则人员明细中的顺序（从 1 开始，与后端一致）。
 *
 * @param assigneeId 当前分配员工编号
 */
function syncAssignee(assigneeId?: null | number) {
  const index = assigneeOptions.value.findIndex(
    (option) => option.value === assigneeId,
  );
  const assignee = index === -1 ? undefined : assigneeOptions.value[index];
  currentAssigneeQuotaMax.value = assignee?.assignValue;
  // 额度直接取当前分配员工的分配数值
  formApi.setFieldValue('currentRemainQuota', assignee?.assignValue ?? 0);
  // 游标索引 = 该员工在规则人员明细中的顺序（从 1 开始）
  syncCursorIndex(assigneeId);
}

/**
 * 「当前分配员工」下拉框 change 事件
 *
 * @param assigneeId 当前分配员工编号
 */
function handleAssigneeChange(assigneeId: any) {
  syncAssignee(assigneeId);
}

/** 「客户来源」当前选中的来源编号 */
const sourceIdsValue = ref<number[]>([]);

/**
 * 「客户来源」勾选变化
 *
 * <p>树形面板里的勾选结果同步到表单字段
 *
 * @param ids 选中的来源编号列表
 */
function handleSourceIdsChange(ids: number[]) {
  sourceIdsValue.value = ids;
  formApi.setFieldValue('sourceIds', ids);
}

/** 保存规则（存在则更新，不存在则新增） */
async function onSubmit() {
  const { valid } = await formApi.validate();
  if (!valid) {
    return;
  }
  const data = (await formApi.getValues()) as AutoAssignRuleApi.AutoAssignRule;
  // 带上 id，保证是更新而不是新增
  data.id = ruleData.value.id;
  await saveAutoAssignRule(data);
  message.success($t('ui.actionMessage.operationSuccess'));
  await getConfigInfo();
}

/** 加载规则配置 */
async function getConfigInfo() {
  try {
    const res = await getAutoAssignRule();
    ruleData.value = res ?? {};
    // filterFields = false：不过滤字段，保证条件字段等都能回填
    await formApi.setValues(ruleData.value as Record<string, any>, false);
    allocationMethod.value = ruleData.value.allocationMethod;
    // 同步「客户来源」树形面板的选中值
    sourceIdsValue.value = ruleData.value.sourceIds ?? [];
    // 加载「当前分配员工」的选项，并同步额度上限与游标索引
    await loadAssigneeOptions();
    // 规则 id 变化后，刷新子表
    await ruleUserFormRef.value?.refresh();
  } catch (error) {
    console.error(error);
  }
}

/** 刷新：重新加载规则配置与规则人员明细 */
async function onRefresh() {
  await getConfigInfo();
  message.success($t('ui.actionMessage.operationSuccess'));
}

onMounted(() => {
  getConfigInfo();
});
</script>

<template>
  <Page auto-content-height>
    <Card :title="title">
      <Form class="w-100%">
        <!--
          客户来源：树形勾选面板
          通过插槽渲染，便于使用 VbenTree 树形多选
        -->
        <template #sourceIds="slotProps">
          <CustomerSourceTreeSelect
            :value="sourceIdsValue"
            :disabled="slotProps.disabled"
            :placeholder="
              $t('ui.placeholder.select', [
                $t('crm.autoAssignRule.field.scopeSource'),
              ])
            "
            @update:value="handleSourceIdsChange"
          />
        </template>
        <!--
          当前分配员工：只能从规则人员明细中选择
          通过插槽渲染，便于动态绑定选项与 change 事件
        -->
        <template #currentAssigneeId="slotProps">
          <Select
            v-bind="slotProps"
            :allow-clear="true"
            :options="assigneeOptions"
            class="w-full"
            :placeholder="
              $t('ui.placeholder.select', [
                $t('crm.autoAssignRule.field.currentAssigneeId'),
              ])
            "
            @change="handleAssigneeChange"
          />
        </template>
        <!--
          当前分配员工额度：最大值 = 当前分配员工的分配数值
          通过插槽渲染，便于动态绑定 max
        -->
        <template #currentRemainQuota="slotProps">
          <InputNumber
            v-bind="slotProps"
            style="width: 100%"
            :disabled="
              allocationMethod ===
              CrmAutoAssignRuleAllocationMethod.ALLOCATION_METHOD_2
            "
            :max="currentAssigneeQuotaMax"
            :min="0"
            controls-position="right"
          />
        </template>
      </Form>
      <div class="flex justify-center gap-2">
        <Button @click="onRefresh">
          <template #icon>
            <IconifyIcon icon="lucide:refresh-cw" />
          </template>
          {{ $t('common.refresh') }}
        </Button>
        <Button
          type="primary"
          @click="onSubmit"
          v-access:code="['crm:auto-assign-rule:update']"
        >
          {{ $t('common.save') }}
        </Button>
      </div>
    </Card>

    <Card class="mt-4" :title="$t('crm.autoAssignRuleUser.autoAssignRuleUser')">
      <AutoAssignRuleUserForm
        ref="ruleUserFormRef"
        :rule-id="ruleData?.id"
        @success="loadAssigneeOptions"
      />
    </Card>
  </Page>
</template>
