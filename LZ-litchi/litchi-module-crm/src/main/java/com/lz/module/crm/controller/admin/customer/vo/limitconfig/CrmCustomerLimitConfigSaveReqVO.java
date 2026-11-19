package com.lz.module.crm.controller.admin.customer.vo.limitconfig;

import com.lz.framework.common.validation.i18n.I18nNotNull;
import com.lz.module.crm.framework.operatelog.core.SysAdminUserParseFunction;
import com.lz.module.crm.framework.operatelog.core.SysDeptParseFunction;
import com.mzt.logapi.starter.annotation.DiffLogField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 客户限制配置创建/更新 Request VO")
@Data
public class CrmCustomerLimitConfigSaveReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27930")
    private Long id;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "27930")
    @I18nNotNull(i18nKey = "crm.customerLimitConfig.back.name.notNull", message = "规则名称不能为空")
    @DiffLogField(name = "规则名称")
    private String name;

    @Schema(description = "规则类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @I18nNotNull(i18nKey = "crm.customerLimitConfig.back.type.notNull", message = "规则类型不能为空")
    @DiffLogField(name = "规则类型")
    private Integer type;

    /**
     * 适用类型
     */
    @Schema(description = "适用类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @I18nNotNull(i18nKey = "crm.customerLimitConfig.back.applicableType.notNull", message = "适用类型不能为空")
    private Integer applicableType;

    @Schema(description = "规则适用人群")
    @DiffLogField(name = "规则适用人群", function = SysAdminUserParseFunction.NAME)
    private List<Long> userIds;

    @Schema(description = "规则适用部门")
    @DiffLogField(name = "规则适用部门", function = SysDeptParseFunction.NAME)
    private List<Long> deptIds;

    /**
     * 状态
     */
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @I18nNotNull(i18nKey = "crm.customerLimitConfig.back.status.notNull", message = "状态不能为空")
    private Integer status;

    @Schema(description = "数量上限", requiredMode = Schema.RequiredMode.REQUIRED, example = "28384")
    @I18nNotNull(i18nKey = "crm.customerLimitConfig.back.maxCount.notNull", message = "数量上限不能为空")
    @DiffLogField(name = "数量上限")
    private Integer maxCount;

    @Schema(description = "成交客户是否占有拥有客户数(当 type = 1 时)")
    @DiffLogField(name = "成交客户是否占有拥有客户数")
    private Boolean dealCountEnabled;

}
