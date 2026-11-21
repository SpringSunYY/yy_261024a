package com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRuleUser;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.lz.framework.common.pojo.PageParam;
import com.lz.framework.common.annotation.Sortable;
import com.lz.framework.common.validation.i18n.I18nSize;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.lz.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * CRM  规则人员明细分页 Request VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM  规则人员明细分页 Request VO")
@Data
public class CrmAutoAssignRuleUserPageReqVO extends PageParam {

    /**
    * 规则
    */
    @Schema(description = "规则", example = "22428")
    private Long ruleId;

    /**
    * 员工
    */
    @Schema(description = "员工", example = "6808")
    private Long userId;

    /**
    * 分配数值
    */
    @Schema(description = "分配数值")
    private Integer assignValue;

    /**
    * 状态
    */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
    * 排序号
    */
    @Schema(description = "排序号")
    private Integer sortOrder;

    /**
    * 创建时间
    */
    @Sortable(value = "create_time")
    @I18nSize(i18nKey = "crm.autoAssignRuleUser.back.createTime.size", min = 0, max = 2, message = "创建时间长度不能超过2")
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
