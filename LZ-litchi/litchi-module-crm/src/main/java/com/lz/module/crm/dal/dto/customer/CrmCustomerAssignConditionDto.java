package com.lz.module.crm.dal.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 客户分配条件
 *
 * @Project: LZ-litchi
 * @Author: YY
 * @CreateTime: 2026-11-23  16:10
 * @Version: 1.0
 */
@Data
@Schema(description = "管理后台 - CRM 自动分配规则具体条件 DTO")
@AllArgsConstructor
@NoArgsConstructor
public class CrmCustomerAssignConditionDto {

    /**
     * 客户来源编号
     *
     * <p>为空表示不限制客户来源
     */
    @Schema(description = "客户来源编号", example = "1")
    private Long source;

    /**
     * 客户所属行业编号列表
     *
     * <p>为空表示不限制所属行业
     */
    @Schema(description = "客户所属行业编号", example = "1")
    private Long industryId;

    /**
     * 客户级别编号列表
     *
     * <p>为空表示不限制客户级别
     */
    @Schema(description = "客户级别编号", example = "1")
    private Long levelId;
}
