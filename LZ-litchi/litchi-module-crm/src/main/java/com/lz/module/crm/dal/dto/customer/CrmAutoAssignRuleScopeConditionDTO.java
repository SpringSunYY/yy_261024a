package com.lz.module.crm.dal.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * CRM 自动分配规则 - 具体条件 DTO
 *
 * <p>适用范围为「满足条件」时的生效条件。请求与响应均以本 DTO 的结构进行传递，
 * 与数据库中的 JSON 字符串之间的转换由后端完成。
 *
 * <p>三个条件之间为「或」关系：来源、行业、级别只要命中其中一个，即可认为满足条件；
 * 每个条件内部为「或」关系，即列表中的任一编号命中即可。
 *
 * <p>注意：某个条件为 null 或空列表时，表示该条件不参与判断（不限制该维度）。
 *
 * @author 荔枝软件
 */
@Data
@Schema(description = "管理后台 - CRM 自动分配规则具体条件 DTO")
public class CrmAutoAssignRuleScopeConditionDTO {

    /**
     * 客户来源编号列表
     *
     */
    @Schema(description = "客户来源编号列表", example = "[1,2]")
    private List<Long> sourceIds;

    /**
     * 客户所属行业编号列表
     */
    @Schema(description = "客户所属行业编号列表", example = "[1,2]")
    private List<Long> industryIds;

    /**
     * 客户级别编号列表
     */
    @Schema(description = "客户级别编号列表", example = "[1,2]")
    private List<Long> levelIds;

}
