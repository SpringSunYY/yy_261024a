import type { VbenFormSchema } from '#/adapter/form';
import type { CustomerSourceApi } from '#/api/crm/customer/source';

import { nextTick, ref } from 'vue';

import {
  getCustomerSourceChain,
  getCustomerSourceTree,
} from '#/api/crm/customer/source';

/** 级联选择器的字段名后缀 */
export const SOURCE_FIELD_PREFIX = 'sourceLevel';

/** 顶级来源的父级编号 */
export const ROOT_PARENT_ID = 0;

/**
 * 客户来源数据源
 *
 * 负责：
 * 1. 拉取并缓存后端返回的完整来源数据（后端已过滤掉关闭状态的来源）
 * 2. 根据父级编号筛选出「某一级」的候选项
 * 3. 根据来源编号推导出完整链路（顶级 -> 自身），用于编辑回显
 */
class CustomerSourceStore {
  /** 完整来源数据（扁平结构） */
  readonly allSources = ref<CustomerSourceApi.CustomerSource[]>([]);

  /** 是否已经加载完成 */
  private loaded = false;

  /** 加载中的 Promise，避免并发重复请求 */
  private loadingPromise: null | Promise<CustomerSourceApi.CustomerSource[]> =
    null;

  /**
   * 通过本地数据推导某个来源的链路（顶级 -> 自身）
   *
   * <p>如果链路没有到达顶级（例如中间有来源被关闭过滤掉），返回空数组，由调用方回退到后端接口
   */
  findChainById(id: number): CustomerSourceApi.CustomerSource[] {
    const chain: CustomerSourceApi.CustomerSource[] = [];
    let current = this.allSources.value.find((item) => Number(item.id) === id);
    // 最多循环 100 层，避免异常数据造成死循环
    for (let i = 0; i < 100 && current; i++) {
      chain.unshift(current);
      const parentId = Number(current.parentId ?? ROOT_PARENT_ID);
      if (parentId === ROOT_PARENT_ID) {
        // 已经到达顶级，链路完整
        return chain;
      }
      current = this.allSources.value.find(
        (item) => Number(item.id) === parentId,
      );
    }
    // 没有到达顶级，说明链路不完整
    return [];
  }

  /**
   * 获得某个来源的完整链路（顶级 -> 自身）
   *
   * @param id 来源编号，支持字符串类型（客户表 / 线索表的 source 字段是字符串）
   */
  async getChain(
    id: number | string | undefined,
  ): Promise<CustomerSourceApi.CustomerSource[]> {
    const sourceId = Number(id);
    if (!Number.isFinite(sourceId) || sourceId <= 0) {
      return [];
    }
    // 优先使用本地数据推导，避免额外请求
    const localChain = this.findChainById(sourceId);
    if (localChain.length > 0) {
      return localChain;
    }
    // 本地推导失败（例如数据被关闭过滤掉）时，回退到后端接口
    return (await getCustomerSourceChain(sourceId)) ?? [];
  }

  /**
   * 获得某个来源的名称
   */
  getName(id: number | string | undefined): string {
    if (id === undefined || id === null || id === '') {
      return '';
    }
    const target = this.allSources.value.find(
      (item) => Number(item.id) === Number(id),
    );
    return target?.name ?? '';
  }

  /**
   * 获得某一级来源的候选项
   *
   * @param parentId 父级编号，为 0 时表示顶级
   */
  getOptions(
    parentId: number | string | undefined,
  ): CustomerSourceApi.CustomerSource[] {
    const parent = Number(parentId ?? ROOT_PARENT_ID);
    return this.allSources.value.filter(
      (item) => Number(item.parentId ?? ROOT_PARENT_ID) === parent,
    );
  }

  /**
   * 加载完整来源数据（前端只请求一次）
   */
  async load(): Promise<CustomerSourceApi.CustomerSource[]> {
    if (this.loaded) {
      return this.allSources.value;
    }
    if (!this.loadingPromise) {
      this.loadingPromise = getCustomerSourceTree()
        .then((data) => {
          this.allSources.value = data ?? [];
          this.loaded = true;
          return this.allSources.value;
        })
        .finally(() => {
          this.loadingPromise = null;
        });
    }
    return this.loadingPromise;
  }

  /**
   * 清空缓存（客户来源新增 / 修改 / 删除后调用）
   */
  reset(): void {
    this.allSources.value = [];
    this.loaded = false;
    this.loadingPromise = null;
  }
}

/** 全局的客户来源数据源（单例） */
export const customerSourceStore = new CustomerSourceStore();

/**
 * 获得级联选择器某一级的字段名
 *
 * @param level 层级，从 0 开始
 */
export function getSourceFieldName(level: number): string {
  return `${SOURCE_FIELD_PREFIX}_${level}`;
}

/**
 * 从字段名中解析出层级，非级联字段返回 -1
 */
export function getSourceFieldLevel(fieldName: string): number {
  if (!fieldName?.startsWith(`${SOURCE_FIELD_PREFIX}_`)) {
    return -1;
  }
  const level = Number(fieldName.slice(SOURCE_FIELD_PREFIX.length + 1));
  return Number.isFinite(level) ? level : -1;
}

/**
 * 构造级联选择器某一级的组件配置
 *
 * @param options 该级的候选项
 * @param placeholder 占位提示
 */
export function buildSourceSelectProps(
  options: CustomerSourceApi.CustomerSource[],
  placeholder?: string,
) {
  return {
    allowClear: true,
    // 前端本地 like 查询（后端已返回完整数据）
    filterOption: (input: string, option: any) =>
      String(option?.label ?? '')
        .toLowerCase()
        .includes(input.toLowerCase()),
    optionFilterProp: 'label',
    options: options.map((item) => ({
      label: item.name ?? String(item.id),
      value: Number(item.id),
    })),
    placeholder,
    showSearch: true,
  };
}

/** 级联选择器所需的最小 formApi 能力 */
interface SourceCascadeFormApi {
  setState: (state: { schema: VbenFormSchema[] }) => void;
  setFieldValue: (field: string, value: any) => Promise<void> | void;
}

interface UseSourceCascadeOptions {
  /** 基础表单 schema（每次调用返回新的数组） */
  baseSchema: () => VbenFormSchema[];
  /** 插入级联字段的目标字段名，级联字段会插入到它之后 */
  insertAfter: string;
  /** 第一级的标签（例如「客户来源」） */
  rootLabel: string;
  /** 占位提示的生成方式 */
  placeholder?: (label: string) => string;  /** formApi */
  formApi: SourceCascadeFormApi;
}

/**
 * 客户来源级联选择器
 *
 * <p>客户（Customer）与线索（Clue）共用同一套来源数据，因此把级联逻辑抽到这里
 */
export function useSourceCascade(options: UseSourceCascadeOptions) {
  const { baseSchema, insertAfter, rootLabel, formApi } = options;

  /** 各级已选择的来源 id，key 为层级（0 开始） */
  const selectedSourceIds = ref<Record<number, number | undefined>>({});

  /** 是否正在重建级联（重建期间忽略程序触发的清空操作） */
  const isRebuilding = ref(false);

  /**
   * 获得某一级的标题：第一级为「客户来源」，其余为上级所选来源的名称
   */
  function getSourceLabel(level: number): string {
    if (level === 0) {
      return rootLabel;
    }
    const parentId = selectedSourceIds.value[level - 1];
    return customerSourceStore.getName(parentId) || rootLabel;
  }

  /**
   * 构造包含级联字段的表单 schema
   *
   * @param levels 需要渲染的层级数量
   */
  function buildSchema(levels: number): VbenFormSchema[] {
    const base = baseSchema();
    const cascade: VbenFormSchema[] = [];
    for (let level = 0; level < levels; level++) {
      const parentId =
        level === 0
          ? ROOT_PARENT_ID
          : (selectedSourceIds.value[level - 1] ?? ROOT_PARENT_ID);
      const label = getSourceLabel(level);
      cascade.push({
        fieldName: getSourceFieldName(level),
        label,
        component: 'Select',
        componentProps: {
          ...buildSourceSelectProps(
            customerSourceStore.getOptions(parentId),
            options.placeholder?.(label),
          ),
          // 用户选择时，动态追加 / 移除下一级字段
          onChange: (value: any) => handleSourceChange(level, value),
        },
        // 每一级级联字段都是必选
        rules: 'selectRequired',
      });
    }
    // 把级联字段插入到指定字段之后
    const insertIndex = base.findIndex(
      (item) => item.fieldName === insertAfter,
    );
    return [
      ...base.slice(0, insertIndex + 1),
      ...cascade,
      ...base.slice(insertIndex + 1),
    ];
  }

  /**
   * 处理某一级来源的选择变化
   *
   * @param level 层级
   * @param value 选择的来源编号
   */
  async function handleSourceChange(level: number, value: any) {
    if (isRebuilding.value) {
      return;
    }
    // 1. 记录当前选择
    selectedSourceIds.value[level] =
      value === undefined || value === null || value === ''
        ? undefined
        : Number(value);
    // 2. 清空更深层级的选择
    const deeperLevels = Object.keys(selectedSourceIds.value)
      .map(Number)
      .filter((item) => item > level);
    deeperLevels.forEach((item) => {
      selectedSourceIds.value[item] = undefined;
    });
    // 3. 计算新的层级数量：从当前层级往下，直到没有子级为止
    let levels = level + 1;
    let currentId = selectedSourceIds.value[level];
    while (currentId !== undefined && currentId !== null) {
      if (customerSourceStore.getOptions(currentId).length === 0) {
        break;
      }
      levels += 1;
      currentId = selectedSourceIds.value[levels - 1];
    }
    // 4. 更新 schema（setState 整体替换，从而支持动态新增 / 删除字段）
    isRebuilding.value = true;
    formApi.setState({ schema: buildSchema(levels) });
    await nextTick();
    // 5. 清理掉被移除字段的残留值
    for (const item of deeperLevels) {
      await formApi.setFieldValue(getSourceFieldName(item), undefined);
    }
    isRebuilding.value = false;
  }

  /**
   * 获得最后一级选择的来源 id
   */
  function getFinalSourceId(): string | undefined {
    const levels = Object.keys(selectedSourceIds.value)
      .map(Number)
      .sort((a, b) => b - a);
    for (const level of levels) {
      const id = selectedSourceIds.value[level];
      if (id !== undefined && id !== null) {
        return String(id);
      }
    }
    return undefined;
  }

  /**
   * 清空所有级联字段的值（用于打开表单时清理上一次残留）
   */
  async function clearSourceFields() {
    const levels = Object.keys(selectedSourceIds.value).map(Number);
    for (const level of levels) {
      await formApi.setFieldValue(getSourceFieldName(level), undefined);
    }
  }

  /**
   * 重置已选择的来源 id
   */
  function resetSelected() {
    selectedSourceIds.value = {};
  }

  /**
   * 根据来源编号还原级联选择器（编辑场景）
   *
   * @param source 来源编号
   * @returns 还原的层级数量
   */
  async function restoreFromSource(
    source: number | string | undefined,
  ): Promise<number> {
    const chain = await customerSourceStore.getChain(source);
    chain.forEach((item, index) => {
      selectedSourceIds.value[index] = Number(item.id);
    });
    const levels = Math.max(chain.length, 1);
    formApi.setState({ schema: buildSchema(levels) });
    await nextTick();
    // 逐级设置级联字段的值
    for (let level = 0; level < chain.length; level++) {
      await formApi.setFieldValue(
        getSourceFieldName(level),
        Number(chain[level]!.id),
      );
    }
    return levels;
  }

  return {
    buildSchema,
    clearSourceFields,
    getFinalSourceId,
    getSourceLabel,
    handleSourceChange,
    isRebuilding,
    resetSelected,
    restoreFromSource,
    selectedSourceIds,
  };
}
