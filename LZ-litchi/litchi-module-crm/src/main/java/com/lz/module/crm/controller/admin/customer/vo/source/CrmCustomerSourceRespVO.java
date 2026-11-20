package com.lz.module.crm.controller.admin.customer.vo.source;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CRM 客户来源 Response VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM 客户来源 Response VO")
@Data
public class CrmCustomerSourceRespVO {

    /**
     * 分类编号
     */
    @Schema(description = "分类编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20328")
    private Long id;

    /**
     * 分类名称
     */
    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    private String name;

    /**
     * 父级编号
     */
    @Schema(description = "父级编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18366")
    private Long parentId;

    /**
     * 状态
     */
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    /**
     * 排序
     */
    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer orderNum;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
