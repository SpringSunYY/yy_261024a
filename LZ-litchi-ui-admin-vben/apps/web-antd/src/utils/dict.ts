// TODO @YY：后续再优化
// TODO @YY：可以共享么？

import { $t } from '@vben/locales';
import { isObject } from '@vben/utils';

import { useDictStore } from '#/store';

// TODO @dhb52：top-level 调用 导致："getActivePinia()" was called but there was no active Pinia
// 先临时移入到方法中
// const dictStore = useDictStore();

// TODO @dhb: antd 组件的 color 类型
type ColorType = 'error' | 'info' | 'success' | 'warning';

export interface DictDataType {
  dictType?: string;
  label: string;
  i18n?: string;
  value: boolean | number | string;
  colorType?: ColorType;
  cssClass?: string;
}

export interface NumberDictDataType extends DictDataType {
  value: number;
}

export interface StringDictDataType extends DictDataType {
  value: string;
}

/**
 * 获取字典标签
 *
 * @param dictType 字典类型
 * @param value 字典值
 * @returns 字典标签
 */
function getDictLabel(dictType: string, value: any) {
  const dictStore = useDictStore();
  const dictObj = dictStore.getDictData(dictType, value);
  if (!isObject(dictObj)) {
    return '';
  }
  if (dictObj?.i18n) {
    return $t(dictObj.i18n);
  }
  if (dictObj?.label) {
    return dictObj.label;
  }
  return '';
}

/**
 * 获取字典对象
 *
 * @param dictType 字典类型
 * @param value 字典值
 * @returns 字典对象
 */
function getDictObj(dictType: string, value: any) {
  const dictStore = useDictStore();
  const dictObj = dictStore.getDictData(dictType, value);
  return isObject(dictObj) ? dictObj : null;
}

/**
 * 获取字典数组 用于select radio 等
 *
 * @param dictType 字典类型
 * @param valueType 字典值类型，默认 string 类型
 * @returns 字典数组
 */
function getDictOptions(
  dictType: string,
  valueType: 'boolean' | 'number' | 'string' = 'string',
): DictDataType[] {
  const dictStore = useDictStore();
  const dictOpts = dictStore.getDictOptions(dictType);
  const dictOptions: DictDataType[] = [];
  if (dictOpts.length > 0) {
    let dictValue: boolean | number | string = '';
    dictOpts.forEach((d) => {
      switch (valueType) {
        case 'boolean': {
          dictValue = `${d.value}` === 'true';
          break;
        }
        case 'number': {
          dictValue = Number.parseInt(`${d.value}`);
          break;
        }
        case 'string': {
          dictValue = `${d.value}`;
          break;
        }
        // No default
      }
      dictOptions.push({
        value: dictValue,
        label: d.label,
        i18n: d.i18n,
      });
    });
  }
  return dictOptions.length > 0 ? dictOptions : [];
}

// TODO @dhb52：下面的一系列方法，看看能不能复用 getDictOptions 方法
export const getIntDictOptions = (dictType: string): NumberDictDataType[] => {
  // 获得通用的 DictDataType 列表
  const dictOptions = getDictOptions(dictType) as DictDataType[];
  // 转换成 number 类型的 NumberDictDataType 类型
  // why 需要特殊转换：避免 IDEA 在 v-for="dict in getIntDictOptions(...)" 时，el-option 的 key 会告警
  const dictOption: NumberDictDataType[] = [];
  dictOptions.forEach((dict: DictDataType) => {
    dictOption.push({
      ...dict,
      value: Number.parseInt(`${dict.value}`),
    });
  });
  return dictOption;
};

// TODO @dhb52：下面的一系列方法，看看能不能复用 getDictOptions 方法
export const getStrDictOptions = (dictType: string) => {
  // 获得通用的 DictDataType 列表
  const dictOptions = getDictOptions(dictType) as DictDataType[];
  // 转换成 string 类型的 StringDictDataType 类型
  // why 需要特殊转换：避免 IDEA 在 v-for="dict in getStrDictOptions(...)" 时，el-option 的 key 会告警
  const dictOption: StringDictDataType[] = [];
  dictOptions.forEach((dict: DictDataType) => {
    dictOption.push({
      ...dict,
      value: `${dict.value}`,
    });
  });
  return dictOption;
};

// TODO @dhb52：下面的一系列方法，看看能不能复用 getDictOptions 方法
export const getBoolDictOptions = (dictType: string) => {
  const dictOption: DictDataType[] = [];
  const dictOptions = getDictOptions(dictType) as DictDataType[];
  dictOptions.forEach((dict: DictDataType) => {
    dictOption.push({
      ...dict,
      value: `${dict.value}` === 'true',
    });
  });
  return dictOption;
};

enum DICT_TYPE {
  BPM_MODEL_FORM_TYPE = 'bpm_model_form_type',
  // ========== BPM 模块 ==========
  BPM_MODEL_TYPE = 'bpm_model_type',
  BPM_OA_LEAVE_TYPE = 'bpm_oa_leave_type',
  BPM_PROCESS_INSTANCE_STATUS = 'bpm_process_instance_status',
  BPM_PROCESS_LISTENER_TYPE = 'bpm_process_listener_type',
  BPM_PROCESS_LISTENER_VALUE_TYPE = 'bpm_process_listener_value_type',
  BPM_TASK_CANDIDATE_STRATEGY = 'bpm_task_candidate_strategy',
  BPM_TASK_STATUS = 'bpm_task_status',
  COMMON_STATUS = 'common_status',
  COMMON_WHETHER = 'common_whether', // 通用是或否
  CRM_AUDIT_STATUS = 'crm_audit_status', // CRM 审批状态
  CRM_AUTO_ASSIGN_RULE_ALLOCATION_METHOD = 'crm_auto_assign_rule_allocation_method', // CRM 自动赋值规则分配方式
  CRM_AUTO_ASSIGN_RULE_IS_ROUND_ROBIN = 'crm_auto_assign_rule_is_round_robin', // CRM 自动赋值规则是否轮询
  CRM_AUTO_ASSIGN_RULE_SCOPE_TYPE = 'crm_auto_assign_rule_scope_type', // CRM 自动赋值规则适用范围类型
  // ========== CRM - 客户管理模块 ==========
  CRM_AUTO_ASSIGN_RULE_TRIGGER_ACTIONS = 'crm_auto_assign_rule_trigger_actions', // CRM 自动赋值规则触发动作
  CRM_BIZ_TYPE = 'crm_biz_type', // CRM 业务类型
  CRM_BUSINESS_END_STATUS_TYPE = 'crm_business_end_status_type', // CRM 商机结束状态类型
  CRM_CUSTOMER_INDUSTRY = 'crm_customer_industry', // CRM 客户所属行业

  CRM_CUSTOMER_LEVEL = 'crm_customer_level', // CRM 客户级别
  CRM_CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE = 'crm_customer_limit_config_applicable_type',
  CRM_CUSTOMER_SOURCE = 'crm_customer_source', // CRM 客户来源
  CRM_FOLLOW_UP_TYPE = 'crm_follow_up_type', // CRM 跟进方式
  CRM_PERMISSION_LEVEL = 'crm_permission_level', // CRM 数据权限的级别
  CRM_PRODUCT_STATUS = 'crm_product_status', // CRM 商品状态
  CRM_PRODUCT_UNIT = 'crm_product_unit', // CRM 产品单位
  CRM_RECEIVABLE_RETURN_TYPE = 'crm_receivable_return_type', // CRM 回款的还款方式
  DATE_INTERVAL = 'date_interval', // 数据间隔

  // ========== ERP - 企业资源计划模块  ==========
  ERP_AUDIT_STATUS = 'erp_audit_status', // ERP 审批状态
  ERP_STOCK_RECORD_BIZ_TYPE = 'erp_stock_record_biz_type', // 库存明细的业务类型
  // ========== MALL - 交易模块 ==========
  EXPRESS_CHARGE_MODE = 'trade_delivery_express_charge_mode', // 快递的计费方式

  // ========== INFRA 模块 ==========
  INFRA_API_ERROR_LOG_PROCESS_STATUS = 'infra_api_error_log_process_status',
  INFRA_AREA_LEVEL = 'infra_area_level', // 地区等级
  INFRA_BOOLEAN_STRING = 'infra_boolean_string',
  INFRA_CODEGEN_FRONT_TYPE = 'infra_codegen_front_type',
  INFRA_CODEGEN_SCENE = 'infra_codegen_scene',
  INFRA_CODEGEN_TEMPLATE_TYPE = 'infra_codegen_template_type',
  INFRA_CONFIG_TYPE = 'infra_config_type',
  INFRA_FILE_FILE_TYPE = 'infra_file_file_type', // 文件类型
  INFRA_FILE_PATH_TYPE = 'infra_file_path_type', // 文件路径类型
  INFRA_FILE_RETURN_TYPE = 'infra_file_return_type', // 文件返回类型
  INFRA_FILE_STORAGE = 'infra_file_storage',

  INFRA_I18N_KEY_IS_SYSTEM = 'infra_i18n_key_is_system', // 是否内置
  INFRA_I18N_KEY_USE_TYPE = 'infra_i18n_key_use_type', // 使用类型
  INFRA_I18N_LOCALE_IS_DEFAULT = 'infra_i18n_locale_is_default', // 国际化国家信息是否默认
  INFRA_I18N_LOCALE_STATUS = 'infra_i18n_locale_status', // 国际化国家信息状态
  INFRA_I18N_LOCALE_TARGET = 'infra_i18n_locale_target', // 国际化键端
  INFRA_JOB_LOG_STATUS = 'infra_job_log_status',

  INFRA_JOB_STATUS = 'infra_job_status',

  INFRA_OPERATE_TYPE = 'infra_operate_type',

  SYSTEM_DATA_SCOPE = 'system_data_scope',
  SYSTEM_LOGIN_RESULT = 'system_login_result',

  SYSTEM_LOGIN_TYPE = 'system_login_type',
  SYSTEM_MAIL_SEND_STATUS = 'system_mail_send_status',

  SYSTEM_MENU_LAYOUT = 'system_menu_layout', // 菜单的布局
  SYSTEM_MENU_TYPE = 'system_menu_type',
  SYSTEM_MODULE_TYPE = 'system_module_type', // 模块类型
  SYSTEM_NOTICE_TYPE = 'system_notice_type',
  SYSTEM_NOTIFY_TEMPLATE_TYPE = 'system_notify_template_type',
  SYSTEM_OAUTH2_GRANT_TYPE = 'system_oauth2_grant_type',
  SYSTEM_ROLE_TYPE = 'system_role_type',
  SYSTEM_SMS_CHANNEL_CODE = 'system_sms_channel_code',
  SYSTEM_SMS_RECEIVE_STATUS = 'system_sms_receive_status',
  SYSTEM_SMS_SEND_STATUS = 'system_sms_send_status',

  SYSTEM_SMS_TEMPLATE_TYPE = 'system_sms_template_type',
  SYSTEM_SOCIAL_TYPE = 'system_social_type',
  SYSTEM_TENANT_INDUSTRY = 'system_tenant_industry', // 租户行业
  SYSTEM_TENANT_PACKAGE_PUBLISHED = 'system_tenant_package_published', // 租户套餐发布状态
  SYSTEM_TENANT_PACKAGE_STATUS = 'system_tenant_package_status', // 租户套餐状态
  SYSTEM_TENANT_PACKAGE_SUBSCRIBE_PAY_STATUS = 'system_tenant_package_subscribe_pay_status', // 租户套餐订阅付费状态
  SYSTEM_TENANT_PACKAGE_SUBSCRIBE_STATUS = 'system_tenant_package_subscribe_status', // 租户套餐订阅状态
  SYSTEM_TENANT_PACKAGE_TYPE = 'system_tenant_package_type', // 租户套餐类型
  SYSTEM_TENANT_STATUS = 'system_tenant_status', // 租户状态
  SYSTEM_TENANT_TYPE = 'system_tenant_type', // 租户类型

  // ========== SYSTEM 模块 ==========
  SYSTEM_USER_SEX = 'system_user_sex',
  USER_TYPE = 'user_type',
}

export { DICT_TYPE, getDictLabel, getDictObj, getDictOptions };
