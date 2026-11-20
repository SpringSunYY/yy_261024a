<script lang="ts" setup>
import type { CrmClueApi } from '#/api/crm/clue';

import { computed, nextTick, ref } from 'vue';

import { useVbenModelDrawer } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createClue, getClue, updateClue } from '#/api/crm/clue';
import { $t } from '#/locales';
import {
  customerSourceStore,
  getSourceFieldLevel,
  useSourceCascade,
} from '#/utils/source-cascade';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CrmClueApi.Clue>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('crm.clue.clue')])
    : $t('ui.actionTitle.create', [$t('crm.clue.clue')]);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const {
  buildSchema,
  clearSourceFields,
  getFinalSourceId,
  isRebuilding,
  resetSelected,
  restoreFromSource,
} = useSourceCascade({
  baseSchema: useFormSchema,
  formApi,
  insertAfter: 'name',
  placeholder: (label) => $t('ui.placeholder.select', [label]),
  rootLabel: $t('crm.clue.field.source'),
});

const [ModalDrawer, formModalDrawerApi] = useVbenModelDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    formModalDrawerApi.lock();
    // 提交表单：source 始终传递最后一级选择的 id
    const values = (await formApi.getValues()) as Record<string, any>;
    // 剔除级联选择器的中间字段与只读字段，只保留 source
    const data: Record<string, any> = {};
    Object.keys(values).forEach((key) => {
      if (getSourceFieldLevel(key) < 0 && key !== 'sourceName') {
        data[key] = values[key];
      }
    });
    const payload = {
      ...data,
      source: getFinalSourceId(),
    } as CrmClueApi.Clue;
    try {
      await (formData.value?.id ? updateClue(payload) : createClue(payload));
      // 关闭并提示
      await formModalDrawerApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      formModalDrawerApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 0. 标记重建中，避免程序触发的清空 / 回填被误判为用户操作
    isRebuilding.value = true;
    // 1. 清空上一次打开时残留的级联字段值
    await clearSourceFields();
    // 2. 加载完整来源数据（后端已过滤掉关闭状态的来源）
    await customerSourceStore.load();
    // 3. 重置级联状态
    resetSelected();
    await nextTick();
    // 4. 加载线索数据
    const data = formModalDrawerApi.getData<CrmClueApi.Clue>();
    if (!data?.id) {
      // 新增：只渲染第一级
      formApi.setState({ schema: buildSchema(1) });
      await nextTick();
      isRebuilding.value = false;
      return;
    }
    formModalDrawerApi.lock();
    try {
      formData.value = await getClue(data.id as number);
      // 5. 从 source id 还原出完整链路（顶级 -> 自身）
      await restoreFromSource(formData.value.source);
      // 6. 设置其余表单值（source 由级联字段决定，不需要单独设置）
      const {
        source: _source,
        sourceName: _sourceName,
        ...rest
      } = formData.value as any;
      await formApi.setValues(rest);
    } finally {
      formModalDrawerApi.unlock();
      await nextTick();
      isRebuilding.value = false;
    }
  },
});
</script>

<template>
  <ModalDrawer :title="getTitle" class="w-[40%]">
    <Form class="mx-4" />
  </ModalDrawer>
</template>
