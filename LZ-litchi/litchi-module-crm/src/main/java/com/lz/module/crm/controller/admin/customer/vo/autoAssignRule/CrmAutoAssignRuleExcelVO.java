package com.lz.module.crm.controller.admin.customer.vo.autoAssignRule;
import com.alibaba.excel.annotation.ExcelProperty;
import com.lz.framework.common.validation.i18n.I18nNotEmpty;
import com.lz.framework.common.validation.i18n.I18nNotNull;
import com.lz.framework.excel.core.annotations.ExcelColumnSelect;
import com.lz.framework.excel.core.annotations.ExcelI18n;
import com.lz.framework.excel.core.convert.DictConvert;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * CRM 自动分配规则 Excel VO
 *
 * @author 荔枝软件
 */
@Schema(description = "管理后台 - CRM 自动分配规则Excel VO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = false)
public class CrmAutoAssignRuleExcelVO {

    /**
    * 自动分配
    */
    @ExcelProperty(value = "自动分配", converter = DictConvert.class)
    @ExcelColumnSelect(dictType = "common_status", i18n = true)
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.isEnabled")
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.isEnabled.notNull", message = "自动分配不能为空")
    private Integer isEnabled;

    /**
    * 触发动作
    *
    * <p>数据库中以 JSON 数组字符串存储，导出时原样输出
    */
    @ExcelProperty("触发动作")
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.triggerActions")
    private String triggerActions;

    /**
    * 适用范围
    */
    @ExcelProperty(value = "适用范围", converter = DictConvert.class)
    @ExcelColumnSelect(dictType = "crm_auto_assign_rule_scope_type", i18n = true)
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.scopeType")
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.scopeType.notNull", message = "适用范围不能为空")
    private Integer scopeType;

    /**
    * 具体条件
    */
    @ExcelProperty("具体条件")
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.scopeConditions")
    private String scopeConditions;

    /**
    * 分配方案
    */
    @ExcelProperty(value = "分配方案", converter = DictConvert.class)
    @ExcelColumnSelect(dictType = "crm_auto_assign_rule_allocation_method", i18n = true)
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.allocationMethod")
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.allocationMethod.notNull", message = "分配方案不能为空")
    private Integer allocationMethod;

    /**
    * 循环分配
    */
    @ExcelProperty(value = "循环分配", converter = DictConvert.class)
    @ExcelColumnSelect(dictType = "crm_auto_assign_rule_is_round_robin", i18n = true)
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.isRoundRobin")
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.isRoundRobin.notNull", message = "循环分配不能为空")
    private Integer isRoundRobin;

    /**
    * 是否放入公海
    */
    @ExcelProperty(value = "是否放入公海", converter = DictConvert.class)
    @ExcelColumnSelect(dictType = "common_status", i18n = true)
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.isPutPool")
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.isPutPool.notNull", message = "是否放入公海不能为空")
    private Integer isPutPool;

    /**
    * 放入公海额度
    */
    @ExcelProperty("放入公海额度")
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.isPutPoolQuota")
    private Integer isPutPoolQuota;

    /**
    * 当前分配游标索引
    */
    @ExcelProperty("当前分配游标索引")
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.currentCursorIndex")
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.currentCursorIndex.notNull", message = "当前分配游标索引不能为空")
    private Integer currentCursorIndex;

    /**
    * 当前分配员工
    */
    @ExcelProperty("当前分配员工")
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.currentAssigneeId")
    private Long currentAssigneeId;

    /**
    * 当前分配员工额度
    */
    @ExcelProperty("当前分配员工额度")
    @ExcelI18n(i18nKey = "crm.autoAssignRule.field.currentRemainQuota")
    @I18nNotNull(i18nKey = "crm.autoAssignRule.back.currentRemainQuota.notNull", message = "当前分配员工额度不能为空")
    private Integer currentRemainQuota;

}
