package com.lz.module.crm.controller.admin.customer.vo.source;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import com.lz.framework.common.validation.i18n.I18nNotEmpty;
import com.lz.framework.common.validation.i18n.I18nNotNull;

/**
 * CRM 客户来源新增/修改 Request VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM 客户来源新增/修改 Request VO")
@Data
public class CrmCustomerSourceSaveReqVO {

    /**
     * 分类编号
     */
    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20328")
    private Long id;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @I18nNotEmpty(i18nKey = "crm.customerSource.back.name.notEmpty", message = "分类名称不能为空")
    private String name;

    /**
     * 父级编号
     */
    @Schema(description = "父级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18366")
    @I18nNotNull(i18nKey = "crm.customerSource.back.parentId.notNull", message = "父级编号不能为空")
    private Long parentId;

    /**
     * 状态
     */
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @I18nNotNull(i18nKey = "crm.customerSource.back.status.notNull", message = "状态不能为空")
    private Integer status;

    /**
     * 排序
     */
    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @I18nNotNull(i18nKey = "crm.customerSource.back.orderNum.notNull", message = "排序不能为空")
    private Integer orderNum;

}
