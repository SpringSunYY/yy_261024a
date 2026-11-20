<script lang="ts" setup>
import type { CustomerSourceApi } from '#/api/crm/customer/source';

import { computed, ref } from 'vue';

import { useVbenModelDrawer } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createCustomerSource,
  getCustomerSource,
  updateCustomerSource,
} from '#/api/crm/customer/source';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CustomerSourceApi.CustomerSource>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('crm.customerSource.customerSource')])
    : $t('ui.actionTitle.create', [$t('crm.customerSource.customerSource')]);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [ModalDrawer, modalDrawerApi] = useVbenModelDrawer({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalDrawerApi.lock();
    // 提交表单
    const data =
      (await formApi.getValues()) as CustomerSourceApi.CustomerSource;
    try {
      await (formData.value?.id
        ? updateCustomerSource(data)
        : createCustomerSource(data));
      // 关闭并提示
      await modalDrawerApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalDrawerApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    let data = modalDrawerApi.getData<CustomerSourceApi.CustomerSource>();
    if (!data) {
      return;
    }
    if (data.id) {
      modalDrawerApi.lock();
      try {
        data = await getCustomerSource(data.id);
      } finally {
        modalDrawerApi.unlock();
      }
    }
    // 设置到 values
    formData.value = data;
    await formApi.setValues(formData.value);
  },
});
</script>

<template>
  <ModalDrawer :title="getTitle">
    <Form class="mx-4" />
  </ModalDrawer>
</template>
