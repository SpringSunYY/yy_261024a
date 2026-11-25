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
     * 放入公海额度
     *
     * <p>连续 N 次联系不上客户时，将客户放入公海（负责人保持不变）。
     * <p>为 0 时表示不放入公海。
     */
    private Integer putPoolQuota;

    /**
     * 下一个用户额度
     *
     * <p>连续 N 次联系不上客户时，将客户转交给下一个用户作为负责人。
     * <p>为 0 时表示不转交给下一个用户。
     */
    private Integer nextUserQuota;

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

    /**
     * 老负责人处理方式
     *
     * <p>true - 老负责人保留在团队中（降级为团队成员，权限级别见 {@link #oldOwnerPermissionLevel}）；
     * <p>false - 老负责人被移出团队（移除其数据权限）。
     *
     * <p>用于「自动转交给下一个用户」时，避免出现一个客户多个负责人的情况。
     */
    private Boolean oldOwnerHandler;
    /**
     * 老负责人权限级别
     *
     * <p>关联 {@link com.lz.module.crm.enums.permission.CrmPermissionLevelEnum}，
     * 不能是「负责人（OWNER）」。仅在 {@link #oldOwnerHandler} 为 true 时生效。
     */
    private Long oldOwnerPermissionLevel;
    /**
     * 同时转移的关联业务类型
     *
     * <p>多个业务类型，以 JSON 数组字符串存储，例如：[3,4,5]
     *
     * <p>关联 {@link com.lz.module.crm.enums.common.CrmBizTypeEnum}：
     * 联系人（CRM_CONTACT）、商机（CRM_BUSINESS）、合同（CRM_CONTRACT）。
     */
    private String toBizTypes;

}
