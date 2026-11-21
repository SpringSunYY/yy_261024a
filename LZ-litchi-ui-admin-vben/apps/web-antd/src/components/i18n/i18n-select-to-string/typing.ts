import type { DictDataType } from '#/utils';

export type SelectType = 'check' | 'select';
export type SelectMode = 'multiple' | 'single';

export interface SelectToStringProps {
  /** 选中的值，外部可能传入数字（如后端返回的字典值），组件内部会统一转成字符串 */
  modelValue?: number | string;
  options?: DictDataType[];
  disabled?: boolean;
  separator?: string;
  type?: SelectType;
  mode?: SelectMode;
}
