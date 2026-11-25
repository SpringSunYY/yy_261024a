package com.lz.module.crm.controller.admin.customer.vo.customer;

import com.lz.framework.common.validation.i18n.I18nNotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - CRM 客户分配公海给对应负责人 Request VO")
@Data
public class CrmCustomerDistributeReqVO {

    @Schema(description = "客户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1024]")
    @I18nNotEmpty(i18nKey = "crm.customer.back.ids.notEmpty", message = "客户编号不能为空")
    private List<Long> ids;

    /**
     * 负责人
     *
     * <p>为空时，走自动分配规则（触发动作 = 公海分配），由系统自动分配负责人
     */
    @Schema(description = "负责人，为空时走自动分配规则", example = "1024")
    private Long ownerUserId;

}
