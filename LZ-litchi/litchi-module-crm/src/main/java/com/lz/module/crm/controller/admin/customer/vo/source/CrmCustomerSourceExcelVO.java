package com.lz.module.crm.controller.admin.customer.vo.source;
import com.alibaba.excel.annotation.ExcelProperty;
import com.lz.framework.common.validation.i18n.I18nNotEmpty;
import com.lz.framework.common.validation.i18n.I18nNotNull;
import com.lz.framework.excel.core.annotations.ExcelColumnSelect;
import com.lz.framework.excel.core.annotations.ExcelI18n;
import com.lz.framework.excel.core.convert.DictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * CRM 客户来源 Excel VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM 客户来源Excel VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false)
public class CrmCustomerSourceExcelVO {

    /**
    * 分类名称
    */
    @ExcelProperty("分类名称")
    @ExcelI18n(i18nKey = "crm.customerSource.field.name")
    @I18nNotEmpty(i18nKey = "crm.customerSource.back.name.notEmpty", message = "分类名称不能为空")
    private String name;

    /**
    * 父级编号
    */
    @ExcelProperty("父级编号")
    @ExcelI18n(i18nKey = "crm.customerSource.field.parentId")
    @I18nNotNull(i18nKey = "crm.customerSource.back.parentId.notNull", message = "父级编号不能为空")
    private Long parentId;

    /**
    * 状态
    */
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @ExcelColumnSelect(dictType = "common_status", i18n = true)
    @ExcelI18n(i18nKey = "crm.customerSource.field.status")
    @I18nNotNull(i18nKey = "crm.customerSource.back.status.notNull", message = "状态不能为空")
    private Integer status;

    /**
    * 排序
    */
    @ExcelProperty("排序")
    @ExcelI18n(i18nKey = "crm.customerSource.field.orderNum")
    @I18nNotNull(i18nKey = "crm.customerSource.back.orderNum.notNull", message = "排序不能为空")
    private Integer orderNum;

}
