package com.lz.module.crm.dal.dataobject.followup;

import com.lz.framework.mybatis.core.dataobject.BaseDO;
import com.lz.framework.mybatis.core.type.LongListTypeHandler;
import com.lz.framework.mybatis.core.type.StringListTypeHandler;
import com.lz.module.crm.dal.dataobject.business.CrmBusinessDO;
import com.lz.module.crm.dal.dataobject.contact.CrmContactDO;
import com.lz.module.crm.enums.DictTypeConstants;
import com.lz.module.crm.enums.common.CrmBizTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 跟进记录 DO
 *
 * 用于记录客户、联系人的每一次跟进
 *
 * @author 荔枝源码
 */
@TableName(value = "crm_follow_up_record", autoResultMap = true)
@KeySequence("crm_follow_up_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmFollowUpRecordDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;

    /**
     * 数据类型
     *
     * 枚举 {@link CrmBizTypeEnum}
     */
    private Integer bizType;
    /**
     * 数据编号
     *
     * 关联 {@link CrmBizTypeEnum} 对应模块 DO 的 id 字段
     */
    private Long bizId;

    /**
     * 跟进类型
     *
     * 关联 {@link DictTypeConstants#CRM_FOLLOW_UP_TYPE} 字典
     */
    private Integer type;

    /**
     * 联系成功
     *
     * 枚举 {@link com.lz.framework.common.enums.CommonWhetherEnum}
     */
    private Integer isSuccess;
    /**
     * 跟进内容
     */
    private String content;
    /**
     * 联系时间
     *
     * <p>本次实际联系客户的时间，不分业务类型，必选。
     * <p>用于统计「连续 N 次联系不上」的次数：
     * 「放入公海」使用总的连续失败次数，而「下一个用户」只统计当前负责人成为负责人之后的失败次数。
     */
    private LocalDateTime contactTime;
    /**
     * 下次联系时间
     */
    private LocalDateTime nextTime;

    /**
     * 图片
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> picUrls;
    /**
     * 附件
     */
    @TableField(typeHandler = StringListTypeHandler.class)
    private List<String> fileUrls;

    /**
     * 关联的商机编号数组
     *
     * 关联 {@link CrmBusinessDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> businessIds;
    /**
     * 关联的联系人编号数组
     *
     * 关联 {@link CrmContactDO#getId()}
     */
    @TableField(typeHandler = LongListTypeHandler.class)
    private List<Long> contactIds;


}
