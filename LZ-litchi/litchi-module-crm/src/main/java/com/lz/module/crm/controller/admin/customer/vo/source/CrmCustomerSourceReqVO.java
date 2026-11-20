package com.lz.module.crm.controller.admin.customer.vo.source;

import com.lz.framework.common.annotation.Sortable;
import com.lz.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.lz.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * CRM 客户来源列表 Request VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM 客户来源列表 Request VO")
@Data
public class CrmCustomerSourceReqVO extends PageParam {

    @Schema(description = "分类编号", example = "20328")
    private Long id;

    /**
    * 分类名称
    */
    @Schema(description = "分类名称", example = "李四")
    private String name;

    /**
    * 父级编号
    */
    @Schema(description = "父级编号", example = "18366")
    private Long[] parentId;

    /**
    * 状态
    */
    @Schema(description = "状态", example = "1")
    private Integer[] status;

    @Sortable(value = "order_num")
    @Schema(description = "排序")
    private Integer orderNum;

    @Schema(description = "创建者")
    private String creator;

    /**
    * 创建时间
    */
    @Sortable(value = "create_time")
    @Size(max = 2, message = "创建时间长度不能超过2")
    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
