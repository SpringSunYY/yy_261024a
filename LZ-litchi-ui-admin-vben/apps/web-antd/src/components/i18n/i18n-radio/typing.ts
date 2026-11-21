import type { DictDataType } from '#/utils';

export interface I18nRadioProps {
  modelValue?: boolean | number | string;
  option: DictDataType;
  disabled?: boolean;
}

export interface I18nRadioGroupProps {
  modelValue?: boolean | number | string;
  options?: DictDataType[];
  disabled?: boolean;
  /** 按钮样式（antd RadioGroup 的 buttonStyle），仅在 optionType 为 button 时生效 */
  buttonStyle?: 'outline' | 'solid';
  /** 渲染类型（antd RadioGroup 的 optionType），button 为按钮样式 */
  optionType?: 'default' | 'button';
}
