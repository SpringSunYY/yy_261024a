package com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CRM 自动分配规则 Response VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM 自动分配规则 Response VO")
@Data
public class CrmAutoAssignRuleRespVO {

    /**
     * 编号
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15933")
    private Long id;

    /**
     * 自动分配
     */
    @Schema(description = "自动分配", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isEnabled;

    /**
     * 触发动作
     */
    @Schema(description = "触发动作", requiredMode = Schema.RequiredMode.REQUIRED)
    private String triggerActions;

    /**
     * 适用范围
     */
    @Schema(description = "适用范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer scopeType;

    /**
     * 具体条件
     */
    @Schema(description = "具体条件")
    private String scopeConditions;

    /**
     * 分配方案
     */
    @Schema(description = "分配方案", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer allocationMethod;

    /**
     * 循环分配
     */
    @Schema(description = "循环分配", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isRoundRobin;

    /**
     * 是否放入公海
     */
    @Schema(description = "是否放入公海", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer isPutPool;

    /**
     * 放入公海额度
     */
    @Schema(description = "放入公海额度")
    private Integer isPutPoolQuota;

    /**
     * 当前分配游标索引
     */
    @Schema(description = "当前分配游标索引", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer currentCursorIndex;

    /**
     * 当前分配员工
     */
    @Schema(description = "当前分配员工", example = "6068")
    private Long currentAssigneeId;

    /**
     * 当前分配员工额度
     */
    @Schema(description = "当前分配员工额度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer currentRemainQuota;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
