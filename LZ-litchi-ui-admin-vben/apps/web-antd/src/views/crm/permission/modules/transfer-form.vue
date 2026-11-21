<script lang="ts" setup>
import type { CrmPermissionApi } from '#/api/crm/permission';

import { computed } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { transferBusiness } from '#/api/crm/business';
import { transferClue } from '#/api/crm/clue';
import { transferContact } from '#/api/crm/contact';
import { transferContract } from '#/api/crm/contract';
import { transferCustomer } from '#/api/crm/customer';
import { BizTypeEnum } from '#/api/crm/permission';
import { $t } from '#/locales';

import { useTransferFormSchema } from './data';

const emit = defineEmits(['success']);

const bizType = defineModel<number>('bizType');

const getTitle = computed(() => {
  return $t('crm.permission.action.transfer');
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
  schema: useTransferFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const data = (await formApi.getValues()) as CrmPermissionApi.TransferReq;
    try {
      switch (bizType.value) {
        case BizTypeEnum.CRM_BUSINESS: {
          await transferBusiness(data);
          break;
        }
        case BizTypeEnum.CRM_CLUE: {
          await transferClue(data);
          break;
        }
        case BizTypeEnum.CRM_CONTACT: {
          await transferContact(data);
          break;
        }
        case BizTypeEnum.CRM_CONTRACT: {
          await transferContract(data);
          break;
        }
        case BizTypeEnum.CRM_CUSTOMER: {
          await transferCustomer(data);
          break;
        }
        default: {
          message.error($t('crm.permission.message.noTransferApi'));
          return;
        }
      }
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formApi.resetForm();
      return;
    }
    const data = modalApi.getData<{ bizType: number; id: number }>();
    if (!data) {
      return;
    }
    if (data.bizType) {
      bizType.value = data.bizType;
    }
    // 一次性设置 bizType、id
    // 注意：不能拆成多次 setFieldValue，因为表单未挂载时每次调用都会去等待挂载，
    // 而 StateHandler 只保存一个等待中的回调，后一次会覆盖前一次，导致前面的值设置丢失
    await formApi.setValues(
      {
        bizType: data.bizType,
        id: data.id,
      },
      false,
    );
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-[40%]">
    <Form class="mx-4" />
  </Modal>
</template>
