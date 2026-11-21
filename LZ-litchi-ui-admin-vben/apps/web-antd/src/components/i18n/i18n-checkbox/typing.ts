import type { DictDataType } from '#/utils';

export interface I18nCheckboxProps {
  modelValue?: string[];
  option: DictDataType;
  disabled?: boolean;
}

export interface I18nCheckboxGroupProps {
  /** 选中的值，外部可能传入数字（如后端返回的字典值），组件内部会统一转成字符串 */
  modelValue?: (number | string)[];
  options?: DictDataType[];
  disabled?: boolean;
}
