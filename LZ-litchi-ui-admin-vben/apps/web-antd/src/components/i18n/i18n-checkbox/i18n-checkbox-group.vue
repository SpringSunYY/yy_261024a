<script setup lang="ts" generic>
import type { I18nCheckboxGroupProps } from './typing';

import { computed } from 'vue';

import { Checkbox } from 'ant-design-vue';

import { $t } from '#/locales';

defineOptions({ name: 'I18nCheckboxGroup', inheritAttrs: false });

const props = withDefaults(defineProps<I18nCheckboxGroupProps>(), {
  modelValue: () => [],
  options: () => [],
  disabled: false,
});

const emit = defineEmits<{
  change: [value: string[]];
  'update:modelValue': [value: string[]];
}>();

/**
 * Checkbox.Group 的选项
 *
 * <p>必须通过 options 属性渲染，不能用默认插槽：
 * 插槽内容在父组件的渲染上下文中创建，Checkbox 拿不到 Checkbox.Group 提供的上下文，
 * 导致 checked 状态永远为 undefined（无法回显选中）。
 */
const groupOptions = computed(() =>
  props.options.map((item) => {
    let label = item.label;
    if (item.i18n) {
      const translated = $t(item.i18n) as string;
      if (translated && translated !== item.i18n) {
        label = translated;
      }
    }
    return {
      label,
      value: String(item.value),
    };
  }),
);

/**
 * 选中的值
 *
 * <p>Checkbox 的值统一为字符串，这里把外部传入的值也统一转成字符串，
 * 避免数字与字符串比较不相等，导致回填时无法选中。
 */
const checkedValues = computed(() => (props.modelValue ?? []).map(String));

function handleChange(values: any) {
  // ant-design-vue 的 Checkbox.Group change 事件直接回传选中的值数组
  const raw = Array.isArray(values) ? values : values?.target?.value;
  const list = Array.isArray(raw) ? raw : [];
  const normalized = list.map(String);
  emit('update:modelValue', normalized);
  emit('change', normalized);
}
</script>

<template>
  <Checkbox.Group
    :value="checkedValues"
    :disabled="props.disabled"
    :options="groupOptions"
    @change="handleChange"
  />
</template>
