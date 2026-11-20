package com.lz.module.crm.controller.admin.customer.vo.source;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * CRM 客户来源 Excel Response VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM 客户来源Excel Response VO")
@Data
@Builder
public class CrmCustomerSourceExcelRespVO {

    /**
     * 导入/导出结果信息
     */
    @Schema(description = "信息", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;

}
