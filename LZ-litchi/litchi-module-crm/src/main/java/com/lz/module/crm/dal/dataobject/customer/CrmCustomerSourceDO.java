package com.lz.module.crm.dal.dataobject.customer;

import lombok.*;
import com.baomidou.mybatisplus.annotation.*;
import com.lz.framework.mybatis.core.dataobject.BaseDO;

/**
 * CRM 客户来源 DO
 *
 * @author 荔枝软件
 */
@TableName("crm_customer_source")
@KeySequence("crm_customer_source_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerSourceDO extends BaseDO {

    public static final Long PARENT_ID_ROOT = 0L;

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 分类名称
     */
    private String name;
    /**
     * 父级编号
     */
    private Long parentId;
    /**
     * 状态
     *
     * 枚举 {@link TODO common_status 对应的类}
     */
    private Integer status;
    /**
     * 排序
     */
    private Integer orderNum;
    /**
     * 祖级列表
     */
    private String ancestors;


}
