package com.lz.module.crm.controller.admin.customer.vo.autoAssignRuleUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import com.lz.framework.common.validation.i18n.I18nNotEmpty;
import com.lz.framework.common.validation.i18n.I18nNotNull;
import jakarta.validation.constraints.*;

/**
 * CRM  规则人员明细新增/修改 Request VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM  规则人员明细新增/修改 Request VO")
@Data
public class CrmAutoAssignRuleUserSaveReqVO {

    /**
     * 编号
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17286")
    private Long id;

    /**
     * 员工
     */
    @Schema(description = "员工", requiredMode = Schema.RequiredMode.REQUIRED, example = "6808")
    @I18nNotNull(i18nKey = "crm.autoAssignRuleUser.back.userId.notNull", message = "员工不能为空")
    private Long userId;

    /**
     * 分配数值
     */
    @Schema(description = "分配数值", requiredMode = Schema.RequiredMode.REQUIRED)
    @I18nNotNull(i18nKey = "crm.autoAssignRuleUser.back.assignValue.notNull", message = "分配数值不能为空")
    private Integer assignValue;

    /**
     * 状态
     */
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @I18nNotNull(i18nKey = "crm.autoAssignRuleUser.back.status.notNull", message = "状态不能为空")
    private Integer status;

    /**
     * 排序号（由后端按列表顺序统一重排，前端可不传）
     */
    @Schema(description = "排序号", example = "1")
    private Integer sortOrder;

}
