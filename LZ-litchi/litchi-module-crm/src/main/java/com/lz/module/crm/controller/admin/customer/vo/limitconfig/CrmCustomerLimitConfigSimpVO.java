package com.lz.module.crm.controller.admin.customer.vo.limitconfig;

import com.lz.framework.common.pojo.PageParam;
import com.lz.framework.common.validation.i18n.I18nNotNull;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 客户限制配置VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CrmCustomerLimitConfigSimpVO extends PageParam {

    @Schema(description = "编号", example = "1")
    private Integer id;


    @Schema(description = "配置名称", example = "规则1")
    private String name;

    @Schema(description = "规则类型", example = "1")
    private Integer type;

    /**
     * 适用类型
     */
    @Schema(description = "适用类型", example = "1")
    private Integer applicableType;

    /**
     * 最大数量
     */
    @Schema(description = "最大数量", example = "10")
    private Integer maxCount;

    /**
     * 成交客户是否占有拥有客户数(当 type = 1 时)
     */
    @Schema(description = "成交客户是否占有拥有客户数(当 type = 1 时)", example = "true")
    private Boolean dealCountEnabled;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "2")
    private Integer status;

    /**
     * 用户名称
     */
    @Schema(description = "用户名称", example = "张三")
    private String userName;

    /**
     * 部门名称
     */
    @Schema(description = "部门名称", example = "部门1")
    private String deptName;

}
