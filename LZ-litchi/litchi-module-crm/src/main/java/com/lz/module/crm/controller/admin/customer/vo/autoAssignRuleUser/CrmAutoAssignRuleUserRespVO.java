package com.lz.module.crm.controller.admin.customer.vo.autoAssignRuleUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * CRM  规则人员明细 Response VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM  规则人员明细 Response VO")
@Data
public class CrmAutoAssignRuleUserRespVO {

    /**
     * 编号
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17286")
    private Long id;

    /**
     * 规则
     */
    @Schema(description = "规则", requiredMode = Schema.RequiredMode.REQUIRED, example = "22428")
    private Long ruleId;

    /**
     * 员工
     */
    @Schema(description = "员工", requiredMode = Schema.RequiredMode.REQUIRED, example = "6808")
    private Long userId;

    /**
     * 分配数值
     */
    @Schema(description = "分配数值", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer assignValue;

    /**
     * 状态
     */
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    /**
     * 排序号
     */
    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
