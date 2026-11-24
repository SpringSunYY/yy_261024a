<script lang="ts" setup>
import type { FileType } from 'ant-design-vue/es/upload/interface';

import type { CrmCustomerApi } from '#/api/crm/customer';

import { h } from 'vue';

import { useVbenModal, z } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { Button, message, Modal as AntModal, Upload } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { importCustomer, importCustomerTemplate } from '#/api/crm/customer';
import { getSimpleUserList } from '#/api/system/user';
import { $t } from '#/locales';

const emit = defineEmits(['success']);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: [
    {
      fieldName: 'ownerUserId',
      label: $t('crm.customer.field.ownerUserId'),
      component: 'ApiSelect',
      componentProps: {
        api: () => getSimpleUserList(),
        fieldNames: {
          label: 'nickname',
          value: 'id',
        },
        class: 'w-full',
        allowClear: true,
      },
      help: $t('crm.customer.message.importOwnerHelp'),
    },
    {
      fieldName: 'file',
      label: $t('crm.customer.action.import'),
      component: 'Upload',
      rules: 'required',
      help: $t('crm.customer.message.importHelp'),
    },
    {
      fieldName: 'updateSupport',
      label: $t('crm.customer.message.updateSupport'),
      component: 'Switch',
      componentProps: {
        checkedChildren: $t('common.yes'),
        unCheckedChildren: $t('common.no'),
      },
      rules: z.boolean().default(false),
      help: $t('crm.customer.message.updateSupportHelp'),
    },
  ],
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const data = await formApi.getValues();
    try {
      const result = await importCustomer({
        // 负责人为空时，由后端按自动分配规则自动分配；这里不传 null，避免被序列化成字符串 "null"
        ownerUserId: data.ownerUserId ?? undefined,
        file: data.file,
        updateSupport: data.updateSupport,
      });
      await modalApi.close();
      emit('success');
      // 提示导入结果明细
      showImportResult(result);
    } finally {
      modalApi.unlock();
    }
  },
});

/**
 * 提示导入结果明细
 *
 * <p>新增 / 更新成功显示数量，失败显示每一行的客户名与失败原因。
 */
function showImportResult(result: CrmCustomerApi.CustomerImportResult) {
  const createCount = result?.createCustomerNames?.length ?? 0;
  const updateCount = result?.updateCustomerNames?.length ?? 0;
  const failureList = Object.entries(result?.failureCustomerNames ?? {});

  if (failureList.length === 0) {
    // 全部成功
    message.success(
      $t('crm.customer.message.importSuccess', [createCount, updateCount]),
    );
    return;
  }

  // 存在失败行：把每一行的失败原因列出来
  AntModal.warning({
    title: $t('crm.customer.message.importPartialSuccess', [
      createCount,
      updateCount,
      failureList.length,
    ]),
    width: 800,
    content: h(
      'div',
      { class: 'max-h-72 overflow-y-auto' },
      failureList.map(([name, reason]) =>
        h('div', { class: 'py-1' }, [
          h('span', { class: 'font-medium' }, name),
          h('span', { class: 'mx-1' }, '：'),
          h('span', { class: 'text-error' }, reason),
        ]),
      ),
    ),
  });
}

/** 上传前 */
function beforeUpload(file: FileType) {
  formApi.setFieldValue('file', file);
  return false;
}

/** 下载模版 */
async function handleDownload() {
  const data = await importCustomerTemplate();
  downloadFileFromBlobPart({
    fileName: `${$t('crm.customer.message.importTemplate')}`,
    source: data,
  });
}
</script>

<template>
  <Modal :title="$t('crm.customer.action.import')" class="w-[30%]">
    <Form class="mx-4">
      <template #file>
        <div class="w-full">
          <Upload
            :max-count="1"
            accept=".xls,.xlsx"
            :before-upload="beforeUpload"
          >
            <Button type="primary">
              {{ $t('crm.customer.message.selectExcel') }}
            </Button>
          </Upload>
        </div>
      </template>
    </Form>
    <template #prepend-footer>
      <div class="flex flex-auto items-center">
        <Button @click="handleDownload">
          {{ $t('crm.customer.message.downloadTemplate') }}
        </Button>
      </div>
    </template>
  </Modal>
</template>
