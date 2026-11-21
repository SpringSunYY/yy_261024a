<script lang="ts" setup>
import type { AutoAssignRuleApi } from '#/api/crm/customer/autoAssignRule';

import { computed, nextTick, ref, shallowRef, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import { useSortable } from '@vueuse/integrations/useSortable';
import {
  Button,
  InputNumber,
  message,
  Popconfirm,
  Select,
  Table,
} from 'ant-design-vue';

import {
  createAutoAssignRuleUser,
  deleteAutoAssignRuleUser,
  getAutoAssignRuleUserListByRuleId,
  updateAutoAssignRuleUser,
  updateAutoAssignRuleUserSort,
} from '#/api/crm/customer/autoAssignRule';
import { getSimpleUserList } from '#/api/system/user';
import { I18nRadioGroup } from '#/components/i18n/i18n-radio';
import { $t } from '#/locales';
import { CommonStatusEnum, DICT_TYPE, getDictOptions } from '#/utils';

const props = defineProps<{
  ruleId?: number; // 规则（主表的关联字段）
}>();

const emit = defineEmits(['success']);

/**
 * 列表数据
 *
 * <p>这里必须用 ref（深度响应式）而不是 shallowRef：表格行内编辑时需要直接修改
 * `record.status` / `record.assignValue` 等属性，若使用 shallowRef 则深层属性变更
 * 不会触发更新，导致 RadioGroup / InputNumber 的 value 监听不到变化、
 * 选中态与实际值不一致（例如保存后仍显示旧的「开启」）。
 */
const list = ref<AutoAssignRuleApi.AutoAssignRuleUser[]>([]);
/** 正在编辑的行（rowKey） */
const editingKey = ref<number | string | undefined>(undefined);
/** 是否处于拖动排序模式 */
const sorting = ref(false);
/** 排序前的原始数据，用于取消排序时回滚 */
const originalList = shallowRef<AutoAssignRuleApi.AutoAssignRuleUser[]>([]);

/** 员工下拉选项 */
const userOptions = ref<{ id: number; nickname: string }[]>([]);
/** 状态选项（值为数字，兼容 antd RadioGroup / Select） */
const statusOptions = computed(() =>
  getDictOptions(DICT_TYPE.COMMON_STATUS, 'number').map((o) => ({
    label: o.label,
    value: Number(o.value),
  })),
);

/**
 * 归一化状态值
 *
 * <p>后端返回的 status 有可能是数字，也可能是字符串（例如 JSON 反序列化场景），
 * 这里统一转成数字，避免组件因类型不一致导致「选中不回显 / 显示不出来」。
 */
function normalizeStatus(status?: number | string | null): number | undefined {
  if (status === undefined || status === null || status === '') {
    return undefined;
  }
  const num = Number(status);
  return Number.isNaN(num) ? undefined : num;
}

/** 简易列表 key，用于行编辑状态标识 */
function getRowKey(row: AutoAssignRuleApi.AutoAssignRuleUser, index: number) {
  return row.id ?? `__new_${index}`;
}

/** 当前是否处于编辑状态 */
function isEditing(row: AutoAssignRuleApi.AutoAssignRuleUser, index: number) {
  return editingKey.value === getRowKey(row, index);
}

/** 是否是新增的、还未保存的行 */
function isNewRow(row: AutoAssignRuleApi.AutoAssignRuleUser) {
  return !row.id;
}

/** 表格列定义 */
const columns = computed(() => [
  {
    title: $t('crm.autoAssignRuleUser.field.sortOrder'),
    dataIndex: 'sortOrder',
    key: 'sortOrder',
    align: 'center' as const,
    width: 100,
  },
  {
    title: $t('crm.autoAssignRuleUser.field.userId'),
    dataIndex: 'userId',
    key: 'userId',
    align: 'center' as const,
    width: 200,
  },
  {
    title: $t('crm.autoAssignRuleUser.field.assignValue'),
    dataIndex: 'assignValue',
    key: 'assignValue',
    align: 'center' as const,
    width: 160,
  },
  {
    title: $t('crm.autoAssignRuleUser.field.status'),
    dataIndex: 'status',
    key: 'status',
    align: 'center' as const,
    width: 160,
  },
  {
    title: $t('common.operation'),
    key: 'operation',
    align: 'center' as const,
    fixed: 'right' as const,
    width: 160,
  },
]);

/** 加载员工下拉选项 */
async function loadUserOptions() {
  if (userOptions.value.length > 0) {
    return;
  }
  const users = await getSimpleUserList();
  userOptions.value = (users ?? []).map((user) => ({
    id: user.id!,
    nickname: user.nickname ?? '',
  }));
}

/** 加载列表数据 */
async function loadData() {
  if (!props.ruleId) {
    list.value = [];
    originalList.value = [];
    return;
  }
  // 先加载员工下拉选项，否则只读态无法展示员工名称
  await loadUserOptions();
  const data = await getAutoAssignRuleUserListByRuleId(props.ruleId);
  // 归一化后端数据：status 转数字，assignValue 缺省为 1，避免只读态显示异常
  list.value = (data ?? []).map((item) => ({
    ...item,
    assignValue: item.assignValue ?? 1,
    status: normalizeStatus(item.status) ?? CommonStatusEnum.ENABLE,
  }));
  // 同步一份原始数据，用于「取消编辑」时还原该行
  originalList.value = cloneDeep(list.value);
}

/** 新增一条（默认进入编辑状态） */
function onAdd() {
  // 必须先保存主表规则，才能新增人员明细
  if (!props.ruleId) {
    message.warning($t('crm.autoAssignRuleUser.message.saveRuleFirst'));
    return;
  }
  editingKey.value = undefined;
  const newRow: AutoAssignRuleApi.AutoAssignRuleUser = {
    userId: undefined,
    // 默认分配数值：直接写入数据，InputNumber 的 default-value 不会回填 v-model
    assignValue: 1,
    // 默认状态：开启（CommonStatusEnum.ENABLE = 0）
    status: CommonStatusEnum.ENABLE,
    sortOrder: list.value.length + 1,
  };
  list.value = [...list.value, newRow];
  editingKey.value = getRowKey(newRow, list.value.length - 1);
  loadUserOptions();
}

/** 进入编辑状态 */
function onEdit(row: AutoAssignRuleApi.AutoAssignRuleUser, index: number) {
  editingKey.value = getRowKey(row, index);
  loadUserOptions();
}

/** 取消编辑（还原该行原值） */
function onCancel(row: AutoAssignRuleApi.AutoAssignRuleUser, index: number) {
  if (isNewRow(row)) {
    // 新增的行直接移除
    list.value = list.value.filter((_, i) => i !== index);
  } else {
    // 已存在的行，还原为原始数据
    const origin = originalList.value.find((item) => item.id === row.id);
    if (origin) {
      const newList = [...list.value];
      newList[index] = cloneDeep(origin);
      list.value = newList;
    }
  }
  editingKey.value = undefined;
}

/** 保存编辑（新增或修改当前这一行） */
async function onSave(
  row: AutoAssignRuleApi.AutoAssignRuleUser,
  index: number,
) {
  if (!props.ruleId) {
    message.warning($t('crm.autoAssignRuleUser.message.saveRuleFirst'));
    return;
  }
  // 校验必填项
  if (row.userId === undefined || row.userId === null) {
    message.warn(
      $t('ui.placeholder.subTableInput', [
        index + 1,
        $t('crm.autoAssignRuleUser.field.userId'),
      ]),
    );
    return;
  }
  if (row.assignValue === undefined || row.assignValue === null) {
    message.warn(
      $t('ui.placeholder.subTableInput', [
        index + 1,
        $t('crm.autoAssignRuleUser.field.assignValue'),
      ]),
    );
    return;
  }
  const status = normalizeStatus(row.status);
  if (status === undefined) {
    message.warn(
      $t('ui.placeholder.subTableInput', [
        index + 1,
        $t('crm.autoAssignRuleUser.field.status'),
      ]),
    );
    return;
  }
  const data: AutoAssignRuleApi.AutoAssignRuleUser = {
    id: row.id,
    userId: row.userId,
    assignValue: row.assignValue,
    status,
  };
  // 逐条保存：新增走 create，修改走 update
  await (isNewRow(row)
    ? createAutoAssignRuleUser(props.ruleId, data)
    : updateAutoAssignRuleUser(props.ruleId, data));
  message.success($t('ui.actionMessage.operationSuccess'));
  editingKey.value = undefined;
  await loadData();
  emit('success');
}

/** 删除一条 */
async function onDelete(
  row: AutoAssignRuleApi.AutoAssignRuleUser,
  index: number,
) {
  if (!props.ruleId) {
    message.warning($t('crm.autoAssignRuleUser.message.saveRuleFirst'));
    return;
  }
  if (isNewRow(row)) {
    list.value = list.value.filter((_, i) => i !== index);
    return;
  }
  await deleteAutoAssignRuleUser(row.id!);
  message.success($t('ui.actionMessage.deleteSuccess', [row.id]));
  await loadData();
  emit('success');
}

/** sortable 控制器 */
const sortableCtl = ref<null | {
  option: (name: string, value?: any) => any;
  start: () => void;
  stop: () => void;
}>(null);

/** 进入拖动排序模式 */
async function onSort() {
  if (!props.ruleId) {
    message.warning($t('crm.autoAssignRuleUser.message.saveRuleFirst'));
    return;
  }
  if (list.value.length < 2) {
    message.warning($t('crm.autoAssignRuleUser.message.sortNeedMore'));
    return;
  }
  originalList.value = cloneDeep(list.value);
  sorting.value = true;
  editingKey.value = undefined;

  // 关键：必须等 Table 渲染完成，再去查 tbody 节点
  await nextTick();
  await nextTick();

  const target = document.querySelector('.auto-assign-rule-user-table tbody');
  if (!target) {
    console.warn('[auto-assign-rule-user-sort] 未找到可排序的 tbody 节点');
    sorting.value = false;
    return;
  }

  sortableCtl.value?.stop();
  sortableCtl.value = useSortable('.auto-assign-rule-user-table tbody', list, {
    disabled: false,
    animation: 150,
    handle: '.auto-assign-rule-user-drag-handle',
    // 禁用 vueuse 默认 onUpdate，避免索引错位，我们自己接管 onEnd
    onUpdate: () => {},
    onEnd: (evt) => {
      const tbody = evt.to as HTMLElement | undefined;
      if (!tbody) return;
      // 按 DOM 当前顺序收集所有数据行的 key
      const rowEls = [
        ...tbody.querySelectorAll<HTMLElement>('tr[data-row-key]'),
      ];
      const domKeyOrder: string[] = rowEls
        .map((el) => el.dataset.rowKey)
        .filter((k): k is string => !!k);

      const currentKeys = list.value.map((item, index) =>
        String(getRowKey(item, index)),
      );
      // 顺序没变，直接返回
      if (
        domKeyOrder.length === currentKeys.length &&
        domKeyOrder.every((k, i) => k === currentKeys[i])
      ) {
        return;
      }

      // 按 DOM 顺序重排
      const map = new Map(
        list.value.map((item, index) => [String(getRowKey(item, index)), item]),
      );
      const newList: AutoAssignRuleApi.AutoAssignRuleUser[] = [];
      for (const key of domKeyOrder) {
        const item = map.get(key);
        if (item) newList.push(item);
      }
      // 兜底：补上未出现在 DOM 里的行
      for (const [index, item] of list.value.entries()) {
        const key = String(getRowKey(item, index));
        if (!domKeyOrder.includes(key)) {
          newList.push(item);
        }
      }
      list.value = newList;
    },
  }) as any;
}

/** 提交排序 */
async function onSortSubmit() {
  if (!props.ruleId) {
    message.warning($t('crm.autoAssignRuleUser.message.saveRuleFirst'));
    return;
  }
  const ids = list.value
    .map((item) => item.id)
    .filter((id): id is number => id !== undefined && id !== null);
  if (ids.length !== list.value.length) {
    message.warning($t('crm.autoAssignRuleUser.message.saveBeforeSort'));
    return;
  }
  await updateAutoAssignRuleUserSort(props.ruleId, ids);
  message.success($t('ui.actionMessage.operationSuccess'));
  sorting.value = false;
  sortableCtl.value?.stop();
  sortableCtl.value = null;
  await loadData();
  emit('success');
}

/** 取消排序 */
function onSortCancel() {
  list.value = cloneDeep(originalList.value);
  sorting.value = false;
  sortableCtl.value?.stop();
  sortableCtl.value = null;
}

/** 自定义行渲染：排序模式下加拖动手势 */
function customRow(_record: any) {
  return {
    class: sorting.value ? 'cursor-move' : '',
  };
}

// 监听主表的关联字段的变化，加载对应的子表数据
watch(
  () => props.ruleId,
  async (val) => {
    if (!val) {
      list.value = [];
      return;
    }
    await nextTick();
    await loadData();
  },
  { immediate: true },
);

defineExpose({
  /** 刷新列表 */
  refresh: loadData,
});
</script>

<template>
  <div>
    <Table
      class="auto-assign-rule-user-table"
      :columns="columns"
      :custom-row="customRow"
      :data-source="list"
      :pagination="false"
      :scroll="{ x: '100%' }"
      row-key="id"
      size="small"
    >
      <template #bodyCell="{ column, index, record }">
        <template v-if="!record"></template>

        <!-- 排序号 -->
        <template v-else-if="column.key === 'sortOrder'">
          <div class="flex items-center justify-center gap-1">
            <span
              v-if="sorting"
              class="auto-assign-rule-user-drag-handle icon-[ic--round-drag-indicator] cursor-move text-xl text-gray-500"
            ></span>
            <span>{{ index + 1 }}</span>
          </div>
        </template>

        <!-- 员工 -->
        <template v-else-if="column.key === 'userId'">
          <Select
            v-if="isEditing(record, index)"
            v-model:value="record.userId"
            class="w-full"
            :options="
              userOptions.map((u) => ({ label: u.nickname, value: u.id }))
            "
            option-filter-prop="label"
            placeholder="请选择员工"
            show-search
          />
          <span v-else>
            {{
              userOptions.find((u) => u.id === record.userId)?.nickname ?? '-'
            }}
          </span>
        </template>

        <!-- 分配数值 -->
        <template v-else-if="column.key === 'assignValue'">
          <InputNumber
            v-if="isEditing(record, index)"
            v-model:value="record.assignValue"
            class="auto-assign-rule-user-assign-value w-full"
            :min="1"
            :precision="0"
            placeholder="请输入分配数值"
          />
          <span v-else>{{ record.assignValue ?? '-' }}</span>
        </template>

        <!-- 状态 -->
        <template v-else-if="column.key === 'status'">
          <I18nRadioGroup
            v-if="isEditing(record, index)"
            v-model="record.status"
            class="w-full"
            :options="statusOptions"
            button-style="solid"
            option-type="button"
          />
          <span v-else>
            {{
              statusOptions.find(
                (o) => o.value === normalizeStatus(record.status),
              )?.label ?? '-'
            }}
          </span>
        </template>

        <!-- 操作 -->
        <template v-else-if="column.key === 'operation'">
          <div class="flex items-center justify-center gap-2">
            <template v-if="isEditing(record, index)">
              <Button
                type="link"
                size="small"
                class="px-1"
                @click="onSave(record, index)"
              >
                <template #icon>
                  <IconifyIcon icon="lucide:check" />
                </template>
                {{ $t('common.save') }}
              </Button>
              <Button
                type="link"
                size="small"
                class="px-1"
                @click="onCancel(record, index)"
              >
                {{ $t('common.cancel') }}
              </Button>
            </template>
            <template v-else>
              <Button
                type="link"
                size="small"
                class="px-1"
                @click="onEdit(record, index)"
                v-access:code="['crm:auto-assign-rule:update']"
              >
                {{ $t('common.edit') }}
              </Button>
              <Popconfirm
                :title="$t('ui.actionMessage.deleteConfirm', [record.id])"
                @confirm="onDelete(record, index)"
              >
                <Button
                  type="link"
                  size="small"
                  class="px-1"
                  danger
                  v-access:code="['crm:auto-assign-rule:delete']"
                >
                  {{ $t('common.delete') }}
                </Button>
              </Popconfirm>
            </template>
          </div>
        </template>
      </template>
    </Table>

    <!-- 底部操作 -->
    <div class="mt-3 flex items-center justify-center gap-2">
      <template v-if="sorting">
        <Button @click="onSortCancel">
          {{ $t('common.cancel') }}
        </Button>
        <Button type="primary" @click="onSortSubmit">
          {{ $t('bpm.model.message.saveSort') }}
        </Button>
      </template>
      <template v-else>
        <Button
          type="primary"
          ghost
          @click="onAdd"
          v-access:code="['crm:auto-assign-rule:create']"
        >
          <template #icon>
            <IconifyIcon icon="lucide:plus" />
          </template>
          {{
            $t('ui.actionTitle.create', [
              $t('crm.autoAssignRuleUser.autoAssignRuleUser'),
            ])
          }}
        </Button>
        <Button
          type="primary"
          ghost
          @click="onSort"
          v-access:code="['crm:auto-assign-rule:update']"
        >
          <template #icon>
            <IconifyIcon icon="lucide:align-start-vertical" />
          </template>
          {{ $t('bpm.model.message.sort') }}
        </Button>
      </template>
    </div>
  </div>
</template>

<style scoped>
/* 分配数值输入框：内容居中显示 */
.auto-assign-rule-user-assign-value :deep(.ant-input-number-input) {
  text-align: center;
}
</style>
