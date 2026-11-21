package com.lz.module.crm.dal.dataobject.customer;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.lz.framework.mybatis.core.dataobject.BaseDO;

/**
 * CRM 自动分配规则 DO
 *
 * @author 荔枝软件
 */
@TableName("crm_auto_assign_rule")
@KeySequence("crm_auto_assign_rule_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmAutoAssignRuleDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 自动分配
     *
     * 枚举 {@link TODO common_status 对应的类}
     */
    private Integer isEnabled;
    /**
     * 触发动作
     *
     * <p>多个触发动作，以 JSON 数组字符串存储，例如：[1,2,3]
     *
     * 枚举 {@link TODO crm_auto_assign_rule_trigger_actions 对应的类}
     */
    private String triggerActions;
    /**
     * 适用范围
     *
     * 枚举 {@link com.lz.module.crm.enums.customer.CrmAutoAssignRuleScopeTypeEnum}
     */
    private Integer scopeType;
    /**
     * 具体条件
     */
    private String scopeConditions;
    /**
     * 分配方案
     *
     * 枚举 {@link com.lz.module.crm.enums.customer.CrmAutoAssignRuleAllocationMethodEnum}
     */
    private Integer allocationMethod;
    /**
     * 循环分配
     *
     * 枚举 {@link com.lz.module.crm.enums.customer.CrmAutoAssignRuleAllocationMethodEnum}
     */
    private Integer isRoundRobin;
    /**
     * 是否放入公海
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer isPutPool;
    /**
     * 放入公海额度
     */
    private Integer isPutPoolQuota;
    /**
     * 当前分配游标索引
     */
    private Integer currentCursorIndex;
    /**
     * 当前分配员工
     */
    private Long currentAssigneeId;
    /**
     * 当前分配员工额度
     */
    private Integer currentRemainQuota;


}
