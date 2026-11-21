<script lang="ts" setup>
import type { AutoAssignRuleApi } from '#/api/crm/customer/autoAssignRule';

import { computed, ref } from 'vue';

import { useVbenModelDrawer } from '@vben/common-ui';

import { message, Tabs } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createAutoAssignRule,
  getAutoAssignRule,
  updateAutoAssignRule,
} from '#/api/crm/customer/autoAssignRule';
import { $t } from '#/locales';

import { useFormSchema } from '../data';
import AutoAssignRuleUserForm from './auto-assign-rule-user-form.vue';

const emit = defineEmits(['success']);
const formData = ref<AutoAssignRuleApi.AutoAssignRule>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', [$t('crm.autoAssignRule.autoAssignRule')])
    : $t('ui.actionTitle.create', [$t('crm.autoAssignRule.autoAssignRule')]);
});

/** 子表的表单 */
const subTabsName = ref('autoAssignRuleUser');
const autoAssignRuleUserFormRef =
  ref<InstanceType<typeof AutoAssignRuleUserForm>>();

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
    // 子表校验

    const detailValid = autoAssignRuleUserFormRef.value?.validate();
    if (!detailValid) {
      subTabsName.value = 'autoAssignRuleUser';
      return;
    }
    modalDrawerApi.lock();
    // 提交表单
    const data =
      (await formApi.getValues()) as AutoAssignRuleApi.AutoAssignRule;
    // 拼接子表的数据
    data.autoAssignRuleUsers = autoAssignRuleUserFormRef.value?.getData();
    try {
      await (formData.value?.id
        ? updateAutoAssignRule(data)
        : createAutoAssignRule(data));
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
    let data = modalDrawerApi.getData<AutoAssignRuleApi.AutoAssignRule>();
    if (!data) {
      return;
    }
    if (data.id) {
      modalDrawerApi.lock();
      try {
        data = await getAutoAssignRule(data.id);
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
    <!-- 子表的表单 -->
    <Tabs v-model:active-key="subTabsName">
      <Tabs.TabPane
        key="autoAssignRuleUser"
        :tab="$t('crm.autoAssignRuleUser.autoAssignRuleUser')"
        force-render
      >
        <AutoAssignRuleUserForm
          ref="autoAssignRuleUserFormRef"
          :rule-id="formData?.id"
        />
      </Tabs.TabPane>
    </Tabs>
  </ModalDrawer>
</template>
