<script lang="ts" setup>
import type { CrmCustomerApi } from '#/api/crm/customer';

import { computed, nextTick, ref } from 'vue';

import { useVbenForm, useVbenModelDrawer } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';

import { Input, message } from 'ant-design-vue';

import {
  createCustomer,
  getCustomer,
  getCustomerByMobile,
  updateCustomer,
} from '#/api/crm/customer';
import { $t } from '#/locales';
import {
  customerSourceStore,
  getSourceFieldLevel,
  useSourceCascade,
} from '#/utils/source-cascade';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CrmCustomerApi.Customer>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('crm.customer.customer')])
    : $t('ui.actionTitle.create', [$t('crm.customer.customer')]);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  // 一共2列
  wrapperClass: 'grid-cols-2',
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
  rootLabel: $t('crm.customer.field.source'),
});

/**
 * 手机号唯一性校验
 *
 * <p>手机号格式已由表单 schema 的 `mobile` 规则校验通过，这里只负责判断手机号是否已存在。
 * 编辑场景下需要排除自身（excludeId = 当前客户编号）。
 */
const mobileChecking = ref(false);

/**
 * 根据手机号查询已存在的客户
 *
 * @param mobile 手机号
 * @returns 已存在的客户；不存在时返回 null
 */
async function fetchCustomerByMobile(mobile: string) {
  if (!mobile) {
    return null;
  }
  mobileChecking.value = true;
  try {
    return (
      (await getCustomerByMobile(
        mobile,
        formData.value?.id ? Number(formData.value.id) : undefined,
      )) ?? null
    );
  } finally {
    mobileChecking.value = false;
  }
}

/**
 * 手机号输入框失焦时触发校验
 *
 * <p>1. 先校验手机号格式（表单规则），格式不通过则不再请求后端；
 * <p>2. 格式通过后，再校验手机号是否唯一，不唯一时在字段下方展示错误提示。
 */
async function handleMobileBlur() {
  // 1. 触发表单的字段校验（包含手机号格式校验）
  const result = await formApi.validateField('mobile');
  if (!result.valid) {
    return;
  }
  // 2. 格式校验通过，进行唯一性校验
  const values = (await formApi.getValues()) as Record<string, any>;
  const mobile = values.mobile as string;
  const exist = await fetchCustomerByMobile(mobile);
  if (!exist) {
    // 不存在重复，清除之前的唯一性错误提示
    formApi.form.setFieldError('mobile', undefined);
    return;
  }
  // 3. 已存在重复，构造错误提示并挂到字段上
  const msg = $t('crm.customer.message.mobileExists', [
    mobile,
    exist.ownerUserName || '-',
  ]);
  formApi.form.setFieldError('mobile', msg);
  message.error(msg);
}

const [ModalDrawer, formModalDrawerApi] = useVbenModelDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    // 手机号唯一性校验（手机号格式已由表单的 mobile 规则校验通过）
    const values = (await formApi.getValues()) as Record<string, any>;
    const exist = await fetchCustomerByMobile(values.mobile as string);
    if (exist) {
      // 手机号已存在，提示并阻断提交
      const msg = $t('crm.customer.message.mobileExists', [
        values.mobile as string,
        exist.ownerUserName || '-',
      ]);
      formApi.form.setFieldError('mobile', msg);
      message.error(msg);
      return;
    }
    formModalDrawerApi.lock();
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
    } as CrmCustomerApi.Customer;
    try {
      await (formData.value?.id
        ? updateCustomer(payload)
        : createCustomer(payload));
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
    // 4. 加载客户数据
    const data = formModalDrawerApi.getData<CrmCustomerApi.Customer>();
    if (!data?.id) {
      // 新增：只渲染第一级
      formApi.setState({ schema: buildSchema(1) });
      await nextTick();
      isRebuilding.value = false;
      return;
    }
    formModalDrawerApi.lock();
    try {
      formData.value = await getCustomer(data.id as number);
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
  <ModalDrawer :title="getTitle" class="w-[60%]">
    <Form class="mx-4">
      <template #mobile="slotProps">
        <Input
          v-bind="slotProps"
          :placeholder="
            $t('ui.placeholder.input', [$t('crm.customer.field.mobile')])
          "
          :allow-clear="true"
          class="w-full"
          @blur="handleMobileBlur"
        >
          <template #suffix>
            <IconifyIcon
              v-if="mobileChecking"
              class="animate-spin text-gray-400"
              icon="lucide:loader"
            />
          </template>
        </Input>
      </template>
    </Form>
  </ModalDrawer>
</template>
