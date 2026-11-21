package com.lz.module.crm.controller.admin.customer.vo.autoAssignRule;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.lz.framework.common.pojo.PageParam;
import com.lz.framework.common.annotation.Sortable;
import com.lz.framework.common.validation.i18n.I18nSize;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.lz.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * CRM 自动分配规则分页 Request VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM 自动分配规则分页 Request VO")
@Data
public class CrmAutoAssignRulePageReqVO extends PageParam {

    /**
    * 自动分配
    */
    @Schema(description = "自动分配")
    private Integer isEnabled;

    /**
    * 触发动作
    *
    * <p>数据库中以 JSON 数组字符串存储，分页查询时不支持按该字段过滤
    */
    @Schema(description = "触发动作")
    private String triggerActions;

    /**
    * 适用范围
    */
    @Schema(description = "适用范围", example = "2")
    private Integer scopeType;

    /**
    * 具体条件
    */
    @Schema(description = "具体条件")
    private String scopeConditions;

    /**
    * 分配方案
    */
    @Schema(description = "分配方案")
    private Integer allocationMethod;

    /**
    * 循环分配
    */
    @Schema(description = "循环分配")
    private Integer isRoundRobin;

    /**
    * 是否放入公海
    */
    @Schema(description = "是否放入公海")
    private Integer isPutPool;

    /**
    * 放入公海额度
    */
    @Schema(description = "放入公海额度")
    private Integer isPutPoolQuota;

    /**
    * 当前分配游标索引
    */
    @Schema(description = "当前分配游标索引")
    private Integer currentCursorIndex;

    /**
    * 当前分配员工
    */
    @Schema(description = "当前分配员工", example = "6068")
    private Long currentAssigneeId;

    /**
    * 当前分配员工额度
    */
    @Schema(description = "当前分配员工额度")
    private Integer currentRemainQuota;

    /**
    * 创建时间
    */
    @Sortable(value = "create_time")
    @I18nSize(i18nKey = "crm.autoAssignRule.back.createTime.size", min = 0, max = 2, message = "创建时间长度不能超过2")
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
