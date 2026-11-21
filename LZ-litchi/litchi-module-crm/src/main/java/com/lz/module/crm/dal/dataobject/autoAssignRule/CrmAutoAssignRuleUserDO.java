package com.lz.module.crm.dal.dataobject.autoAssignRule;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.lz.framework.mybatis.core.dataobject.BaseDO;

/**
 * CRM  规则人员明细 DO
 *
 * @author 荔枝软件
 */
@TableName("crm_auto_assign_rule_user")
@KeySequence("crm_auto_assign_rule_user_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmAutoAssignRuleUserDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 规则
     */
    private Long ruleId;
    /**
     * 员工
     */
    private Long userId;
    /**
     * 分配数值
     */
    private Integer assignValue;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 排序号
     */
    private Integer sortOrder;

}
