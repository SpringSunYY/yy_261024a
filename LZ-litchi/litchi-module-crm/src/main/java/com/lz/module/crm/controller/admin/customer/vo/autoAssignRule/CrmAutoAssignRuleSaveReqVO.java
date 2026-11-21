package com.lz.module.crm.controller.admin.customer.vo.autoAssignRule;

import com.lz.framework.common.validation.i18n.I18nNotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import com.lz.framework.common.validation.i18n.I18nNotNull;

import java.util.List;

/**
 * CRM 自动分配规则新增/修改 Request VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM 自动分配规则新增/修改 Request VO")
@Data
public class CrmAutoAssignRuleSaveReqVO {

    /**
     * 编号
     */
    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15933")
    private Long id;

    /**
     * 自动分配
     */
    @Schema(description = "自动分配", requiredMode = Schema.RequiredMode.REQUIRED)
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.isEnabled.notNull", message = "自动分配不能为空")
    private Integer isEnabled;

    /**
     * 触发动作
     *
     * <p>多个触发动作，数据库中以 JSON 数组字符串存储
     */
    @Schema(description = "触发动作", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2]")
    @I18nNotEmpty(i18nKey = "crm.autoAssignRule.back.triggerActions.notNull", message = "触发动作不能为空")
    private List<Integer> triggerActions;

    /**
     * 适用范围
     */
    @Schema(description = "适用范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.scopeType.notNull", message = "适用范围不能为空")
    private Integer scopeType;

    /**
     * 具体条件 - 客户来源编号列表
     *
     * <p>适用范围为「满足条件」时生效，由后端组装成具体条件后存储
     */
    @Schema(description = "客户来源编号列表", example = "[1,2]")
    private List<Long> sourceIds;

    /**
     * 具体条件 - 客户所属行业编号列表
     *
     * <p>适用范围为「满足条件」时生效，由后端组装成具体条件后存储
     */
    @Schema(description = "客户所属行业编号列表", example = "[1,2]")
    private List<Long> industryIds;

    /**
     * 具体条件 - 客户级别编号列表
     *
     * <p>适用范围为「满足条件」时生效，由后端组装成具体条件后存储
     */
    @Schema(description = "客户级别编号列表", example = "[1,2]")
    private List<Long> levelIds;

    /**
     * 分配方案
     */
    @Schema(description = "分配方案", requiredMode = Schema.RequiredMode.REQUIRED)
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.allocationMethod.notNull", message = "分配方案不能为空")
    private Integer allocationMethod;

    /**
     * 循环分配
     */
    @Schema(description = "循环分配", requiredMode = Schema.RequiredMode.REQUIRED)
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.isRoundRobin.notNull", message = "循环分配不能为空")
    private Integer isRoundRobin;

    /**
     * 是否放入公海
     */
    @Schema(description = "是否放入公海", requiredMode = Schema.RequiredMode.REQUIRED)
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.isPutPool.notNull", message = "是否放入公海不能为空")
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
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.currentCursorIndex.notNull", message = "当前分配游标索引不能为空")
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
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.currentRemainQuota.notNull", message = "当前分配员工额度不能为空")
    private Integer currentRemainQuota;

}
