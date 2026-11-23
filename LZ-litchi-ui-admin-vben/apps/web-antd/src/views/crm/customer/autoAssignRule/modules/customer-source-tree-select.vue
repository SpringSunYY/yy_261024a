<script lang="ts" setup>
import { computed, nextTick, ref, watch } from 'vue';

import { useVbenDrawer, VbenTree } from '@vben/common-ui';
import { handleTree } from '@vben/utils';

import { Button, Checkbox, Spin } from 'ant-design-vue';

import { getCustomerSourceTree } from '#/api/crm/customer/source';
import { $t } from '#/locales';

defineOptions({ name: 'CustomerSourceTreeSelect' });

/** 树控件的引用，用于调用展开 / 收起全部 */
type TreeInstance = {
  collapseAll: () => void;
  expandAll: () => void;
};

const props = withDefaults(
  defineProps<{
    /** 已选中的来源编号 */
    value?: number[];
    /** 按钮文案 */
    placeholder?: string;
    /** 禁用 */
    disabled?: boolean;
  }>(),
  {
    value: () => [],
    placeholder: '',
    disabled: false,
  },
);

const emit = defineEmits<{
  'update:value': [ids: number[]];
}>();

const [Drawer, drawerApi] = useVbenDrawer({
  onCancel() {
    drawerApi.close();
  },
  onConfirm: handleConfirm,
  async onOpenChange(isOpen: boolean) {
    openState.value = isOpen;
    if (!isOpen) {
      return;
    }
    // 每次打开都重新加载，保证来源数据是最新的
    treeData.value = [];
    await loadTree();
    // 回填已选中的来源
    selectedIds.value = [...props.value];
    await nextTick();
    // 默认全部展开
    treeRef.value?.expandAll();
    isExpanded.value = true;
  },
});

/** 是否加载中 */
const loading = ref(false);
/** 抽屉是否已打开 */
const openState = ref(false);
/** 树数据 */
const treeData = ref<any[]>([]);
/** 树控件引用 */
const treeRef = ref<TreeInstance>();
/** 抽屉中勾选的来源编号 */
const selectedIds = ref<number[]>([]);
/** 是否全选 */
const isAllSelected = ref(false);
/** 是否全部展开 */
const isExpanded = ref(false);
/** 父子联动（默认开启） */
const isLinkage = ref(true);

/** 全部节点的编号 */
const allNodeIds = computed(() => {
  const ids: number[] = [];
  const walk = (nodes: any[]) => {
    nodes.forEach((node) => {
      ids.push(Number(node.id));
      if (node.children?.length) {
        walk(node.children);
      }
    });
  };
  walk(treeData.value);
  return ids;
});

/** 树中已存在的编号集合 */
const existsIdSet = computed(() => new Set(allNodeIds.value));

/** 按钮文案 */
const buttonText = computed(
  () =>
    props.placeholder ||
    $t('ui.placeholder.select', [$t('crm.autoAssignRule.field.scopeSource')]),
);

/** 加载来源树 */
async function loadTree() {
  loading.value = true;
  try {
    const data = (await getCustomerSourceTree()) ?? [];
    treeData.value = handleTree(data) as any[];
  } finally {
    loading.value = false;
  }
}

/** 确认：把抽屉中勾选的来源同步出去 */
function handleConfirm() {
  const ids = selectedIds.value.filter((id) => existsIdSet.value.has(id));
  emit('update:value', ids);
  drawerApi.close();
}

/** 全选 / 全不选 */
function toggleSelectAll(e: any) {
  isAllSelected.value = e?.target?.checked ?? false;
  selectedIds.value = isAllSelected.value ? [...allNodeIds.value] : [];
}

/** 展开 / 收起全部 */
function toggleExpandAll(e: any) {
  isExpanded.value = e?.target?.checked ?? false;
  if (isExpanded.value) {
    treeRef.value?.expandAll();
  } else {
    treeRef.value?.collapseAll();
  }
}

/** 切换父子联动 */
function toggleLinkage(e: any) {
  isLinkage.value = e?.target?.checked ?? false;
}

/** 勾选变化时同步「全选」状态 */
watch(selectedIds, (ids) => {
  isAllSelected.value =
    allNodeIds.value.length > 0 &&
    allNodeIds.value.every((id) => ids.includes(id));
});

/**
 * 外部选中的来源变化时（如表单回填），同步到树控件
 *
 * <p>避免「接口返回前就打开抽屉」时勾选态为空、接口返回后树也没更新的问题。
 */
watch(
  () => props.value,
  (value) => {
    const ids = (value ?? []).map(Number);
    // 与当前勾选一致则无需处理，避免打断用户正在进行的勾选操作
    if (
      ids.length === selectedIds.value.length &&
      ids.every((id) => selectedIds.value.includes(id))
    ) {
      return;
    }
    // 抽屉未打开时只记录，等打开时回填
    if (!openState.value) {
      return;
    }
    selectedIds.value = ids;
  },
  { deep: true },
);
</script>

<template>
  <div class="w-full">
    <Button
      block
      class="w-full"
      :disabled="disabled"
      @click="drawerApi.open()"
    >
      {{ buttonText }}
    </Button>

    <Drawer
      class="min-h-full w-[60%]"
      :title="$t('crm.autoAssignRule.field.scopeSource')"
    >
      <Spin
        :spinning="loading"
        class="flex min-h-[60vh] w-full items-center justify-center"
      >
        <VbenTree
          ref="treeRef"
          class="max-h-[100%] overflow-y-auto"
          :tree-data="treeData"
          multiple
          bordered
          :check-strictly="!isLinkage"
          v-model="selectedIds"
          label-field="name"
          value-field="id"
        />
      </Spin>
      <template #prepend-footer>
        <div class="flex flex-auto items-center gap-4">
          <Checkbox :checked="isAllSelected" @change="toggleSelectAll">
            {{ $t('common.selectAll') }}
          </Checkbox>
          <Checkbox :checked="isExpanded" @change="toggleExpandAll">
            {{ $t('common.expandAll') }}
          </Checkbox>
          <Checkbox :checked="isLinkage" @change="toggleLinkage">
            {{ $t('common.checkStrictly') }}
          </Checkbox>
        </div>
      </template>
    </Drawer>
  </div>
</template>
