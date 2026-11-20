package com.lz.framework.excel.core.convert;

import cn.hutool.core.convert.Convert;
import com.lz.framework.ip.core.Area;
import com.lz.framework.ip.core.utils.AreaUtils;
import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import lombok.extern.slf4j.Slf4j;

/**
 * Excel 数据地区转换器
 *
 * <p>导入时：将 Excel 单元格中的地区路径（如 {@code 河北省/石家庄市/新华区}）解析为地区编号。
 * <p>导出时：将地区编号反向转换为地区路径，与下拉框（{@code @ExcelColumnSelect}）的选项保持一致。
 *
 * @author HUIHUI
 */
@Slf4j
public class AreaConvert implements Converter<Object> {

    /**
     * 地区路径分隔符，与 {@code AreaExcelColumnSelectFunction} 生成的路径格式一致
     */
    private static final String PATH_SEPARATOR = "/";

    @Override
    public Class<?> supportJavaTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        throw new UnsupportedOperationException("暂不支持，也不需要");
    }

    @Override
    public Object convertToJavaData(ReadCellData readCellData, ExcelContentProperty contentProperty,
                                    GlobalConfiguration globalConfiguration) {
        // 解析地区编号
        String label = readCellData.getStringValue();
        Area area = AreaUtils.parseArea(label);
        if (area == null) {
            log.error("[convertToJavaData][label({}) 解析不掉]", label);
            return null;
        }
        // 将 value 转换成对应的属性
        Class<?> fieldClazz = contentProperty.getField().getType();
        return Convert.convert(fieldClazz, area.getCode());
    }

    @Override
    public WriteCellData<?> convertToExcelData(Object value, ExcelContentProperty contentProperty,
                                               GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("");
        }
        String code = String.valueOf(value);
        // 地区编号 -> 地区路径（如 130104 -> 河北省/石家庄市/新华区），与下拉框选项格式保持一致
        String label = AreaUtils.format(code, PATH_SEPARATOR);
        if (label == null) {
            // 查不到地区时，降级输出原始编号，避免导出报错
            log.warn("[convertToExcelData][code({}) 找不到对应地区，降级输出编号]", code);
            return new WriteCellData<>(code);
        }
        return new WriteCellData<>(label);
    }

}
