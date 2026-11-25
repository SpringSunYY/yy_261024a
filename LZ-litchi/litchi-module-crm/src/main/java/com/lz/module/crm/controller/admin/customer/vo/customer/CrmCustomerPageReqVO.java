package com.lz.module.crm.controller.admin.customer.vo.customer;

import com.lz.framework.common.annotation.Sortable;
import com.lz.framework.common.pojo.PageParam;
import com.lz.framework.common.validation.InEnum;
import com.lz.framework.common.validation.i18n.I18nSize;
import com.lz.module.crm.enums.common.CrmSceneTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.lz.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - CRM 客户分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerPageReqVO extends PageParam {

    /**
     * 联系状态 - 今日需联系
     */
    public static final int CONTACT_TODAY = 1;
    /**
     * 联系状态 - 已逾期
     */
    public static final int CONTACT_EXPIRED = 2;
    /**
     * 联系状态 - 已联系
     */
    public static final int CONTACT_ALREADY = 3;

    @Schema(description = "客户名称", example = "赵六")
    private String name;

    @Schema(description = "手机", example = "18000000000")
    private String mobile;

    @Schema(description = "所属行业", example = "1")
    private Long industryId;

    @Schema(description = "客户等级", example = "1")
    private Long level;

    @Schema(description = "客户来源", example = "1")
    private Long source;

    @Schema(description = "场景类型", example = "1")
    @InEnum(CrmSceneTypeEnum.class)
    private Integer sceneType; // 场景类型，为 null 时则表示全部

    @Schema(description = "是否为公海数据", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    private Boolean pool; // null 则表示为不是公海数据

    @Schema(description = "联系状态", example = "1")
    private Integer contactStatus; // backlog 查询条件

    @Schema(description = "跟进状态", example = "true")
    private Boolean followUpStatus;

    /**
     * 初次成交时间
     */
    @I18nSize(i18nKey = "crm.customer.back.firstTransactionTime.size", min = 0, max = 2, message = "初次成交时间长度不能超过2")
    @Schema(description = "初次成交时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] firstTransactionTime;

    /**
     * 最后跟进时间
     */
    @I18nSize(i18nKey = "crm.customer.back.contactLastTime.size", min = 0, max = 2, message = "最后跟进时间长度不能超过2")
    @Schema(description = "最后跟进时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] contactLastTime;

    /**
     * 下次联系时间
     */
    @I18nSize(i18nKey = "crm.customer.back.contactNextTime.size", min = 0, max = 2, message = "下次联系时间长度不能超过2")
    @Schema(description = "下次联系时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] contactNextTime;

    /**
     * 创建时间
     */
    @I18nSize(i18nKey = "crm.customer.back.createTime.size", min = 0, max = 2, message = "创建时间长度不能超过2")
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;



}
