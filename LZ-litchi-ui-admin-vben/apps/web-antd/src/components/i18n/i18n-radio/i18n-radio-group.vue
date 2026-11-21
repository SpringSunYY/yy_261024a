<script setup lang="ts" generic>
import type { I18nRadioGroupProps } from './typing';

import { computed, defineAsyncComponent } from 'vue';

import { Radio } from 'ant-design-vue';

import { $t } from '#/locales';

defineOptions({ name: 'I18nRadioGroup' });

const props = withDefaults(defineProps<I18nRadioGroupProps>(), {
  modelValue: undefined,
  options: () => [],
  disabled: false,
  buttonStyle: 'outline',
  optionType: 'default',
});

const emit = defineEmits<{
  change: [value: boolean | number | string];
  'update:modelValue': [value: boolean | number | string];
}>();

const RadioGroup = defineAsyncComponent(() =>
  import('ant-design-vue/es/radio').then((res) => res.RadioGroup),
);

const i18nOptions = computed(() =>
  props.options.map((item) => {
    let label = item.label;
    if (item.i18n) {
      const translated = $t(item.i18n) as string;
      if (translated && translated !== item.i18n) {
        label = translated;
      }
    }
    return { ...item, label };
  }),
);

function handleChange(e: any) {
  // antd Radio 的事件值是字符串，需要还原为 options 中原始的值类型（number / boolean），
  // 否则数字类型的值（如状态 0/1）会因为类型不一致导致「选中态不回显 / 选不中」
  const rawValue = (e.target as HTMLInputElement).value;
  const matched = props.options.find(
    (item) => String(item.value) === String(rawValue),
  );
  const value = matched ? matched.value : rawValue;
  emit('update:modelValue', value);
  emit('change', value);
}
</script>

<template>
  <RadioGroup
    :value="modelValue"
    :button-style="buttonStyle"
    :disabled="props.disabled"
    :option-type="optionType"
    @change="handleChange"
  >
    <Radio
      v-for="item in i18nOptions"
      :key="String(item.value)"
      :value="item.value"
    >
      {{ item.label }}
    </Radio>
  </RadioGroup>
</template>
