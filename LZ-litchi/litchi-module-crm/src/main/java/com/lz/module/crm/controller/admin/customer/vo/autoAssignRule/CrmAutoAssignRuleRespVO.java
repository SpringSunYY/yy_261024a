package com.lz.module.crm.controller.admin.customer.vo.autoAssignRule;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
     *
     * <p>多个触发动作，数据库中以 JSON 数组字符串存储
     */
    @Schema(description = "触发动作", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2]")
    private List<Integer> triggerActions;

    /**
     * 适用范围
     */
    @Schema(description = "适用范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer scopeType;

    /**
     * 具体条件 - 客户来源编号列表
     *
     * <p>适用范围为「满足条件」时生效，由后端从具体条件中解析
     */
    @Schema(description = "客户来源编号列表", example = "[1,2]")
    private List<Long> sourceIds;

    /**
     * 具体条件 - 客户所属行业编号列表
     *
     * <p>适用范围为「满足条件」时生效，由后端从具体条件中解析
     */
    @Schema(description = "客户所属行业编号列表", example = "[1,2]")
    private List<Long> industryIds;

    /**
     * 具体条件 - 客户级别编号列表
     *
     * <p>适用范围为「满足条件」时生效，由后端从具体条件中解析
     */
    @Schema(description = "客户级别编号列表", example = "[1,2]")
    private List<Long> levelIds;

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
     * 放入公海额度
     */
    @Schema(description = "放入公海额度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer putPoolQuota;

    /**
     * 下一个用户额度
     */
    @Schema(description = "下一个用户额度", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer nextUserQuota;

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
     * 老负责人处理方式
     */
    @Schema(description = "老负责人处理方式", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean oldOwnerHandler;

    /**
     * 老负责人权限级别
     *
     * <p>关联 {@link com.lz.module.crm.enums.permission.CrmPermissionLevelEnum}
     */
    @Schema(description = "老负责人权限级别", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long oldOwnerPermissionLevel;

    /**
     * 同时转移的关联业务类型
     *
     * <p>数据库中以 JSON 数组字符串存储
     */
    @Schema(description = "同时转移的关联业务类型", example = "[3,4,5]")
    private List<Integer> toBizTypes;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
