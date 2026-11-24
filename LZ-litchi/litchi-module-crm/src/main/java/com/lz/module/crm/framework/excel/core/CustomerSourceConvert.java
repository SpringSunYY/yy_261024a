package com.lz.module.crm.framework.excel.core;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.lz.framework.common.util.cache.CacheUtils;
import com.lz.module.crm.service.customer.CrmCustomerSourceService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Excel 数据客户来源转换器
 *
 * <p>导入时：将 Excel 单元格中的客户来源路径（如 {@code 官网/广告投放}）解析为客户来源编号。
 * <p>导出时：将客户来源编号反向转换为客户来源路径，与下拉框（{@code @ExcelColumnSelect}）的选项保持一致。
 *
 * <p>兼容历史模板：单元格填写的是纯数字时，直接视为客户来源编号。
 *
 * @author 荔枝软件
 */
@Slf4j
public class CustomerSourceConvert implements Converter<Object> {

    /**
     * 客户来源路径分隔符，与 {@code CustomerSourceExcelColumnSelectFunction} 生成的路径格式一致
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * 解析失败的标记值
     *
     * <p>该编号在数据库中不存在，导入校验时会被转换为「客户来源不存在」的失败原因，
     * 从而只让该行导入失败，不影响其它行
     */
    public static final Long NOT_EXISTS_SOURCE_ID = -1L;

    /**
     * 客户来源编号 -> 客户来源路径 的缓存
     *
     * <p>转换器实例会被 EasyExcel 复用，因此这里使用带刷新时间的缓存，避免逐行查询数据库；
     * 同时保证客户来源变更后，最多 1 分钟即可生效
     */
    private static final LoadingCache<String, Map<Long, String>> SOURCE_ID_TO_PATH_CACHE =
            CacheUtils.buildAsyncReloadingCache(Duration.ofMinutes(1L), new CacheLoader<String, Map<Long, String>>() {
                @Override
                public Map<Long, String> load(String cacheKey) {
                    return SpringUtil.getBean(CrmCustomerSourceService.class).getCustomerSourcePathMap();
                }
            });

    /**
     * 客户来源路径 -> 客户来源编号 的缓存
     */
    private static final LoadingCache<String, Map<String, Long>> SOURCE_PATH_TO_ID_CACHE =
            CacheUtils.buildAsyncReloadingCache(Duration.ofMinutes(1L), new CacheLoader<String, Map<String, Long>>() {
                @Override
                public Map<String, Long> load(String cacheKey) {
                    Map<String, Long> pathToIdMap = new HashMap<>();
                    SOURCE_ID_TO_PATH_CACHE.getUnchecked(cacheKey).forEach((id, path) -> pathToIdMap.put(path, id));
                    return pathToIdMap;
                }
            });

    @Override
    public Class<?> supportJavaTypeKey() {
        return null;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return null;
    }

    @Override
    public Object convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        // 1. 数字单元格：直接视为客户来源编号（兼容历史模板）
        if (readCellData.getType() == CellDataTypeEnum.NUMBER && readCellData.getNumberValue() != null) {
            return readCellData.getNumberValue().longValue();
        }
        String label = readCellData.getStringValue();
        if (StrUtil.isBlank(label)) {
            return null;
        }
        label = label.trim();
        // 2. 按客户来源路径匹配
        Long sourceId = SOURCE_PATH_TO_ID_CACHE.getUnchecked("all").get(label);
        if (sourceId != null) {
            return sourceId;
        }
        // 3. 纯数字：视为客户来源编号（兼容历史模板）
        if (NumberUtil.isLong(label)) {
            return Long.parseLong(label);
        }
        // 4. 匹配不到：返回标记值，由业务校验转换为该行的失败原因
        log.error("[convertToJavaData][label({}) 解析不到对应的客户来源]", label);
        return NOT_EXISTS_SOURCE_ID;
    }

    @Override
    public WriteCellData<?> convertToExcelData(Object value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        Long sourceId = Convert.convert(Long.class, value);
        if (sourceId == null) {
            return new WriteCellData<>("");
        }
        // 客户来源编号 -> 客户来源路径（如 100 -> 官网/广告投放），与下拉框选项格式保持一致
        String path = SOURCE_ID_TO_PATH_CACHE.getUnchecked("all").get(sourceId);
        if (StrUtil.isEmpty(path)) {
            // 查不到客户来源时，降级输出原始编号，避免导出报错
            log.warn("[convertToExcelData][sourceId({}) 找不到对应客户来源，降级输出编号]", sourceId);
            return new WriteCellData<>(String.valueOf(sourceId));
        }
        return new WriteCellData<>(path);
    }

    /**
     * 清除缓存（当客户来源变更时调用）
     */
    public static void clearCache() {
        SOURCE_ID_TO_PATH_CACHE.invalidateAll();
        SOURCE_PATH_TO_ID_CACHE.invalidateAll();
    }

}
