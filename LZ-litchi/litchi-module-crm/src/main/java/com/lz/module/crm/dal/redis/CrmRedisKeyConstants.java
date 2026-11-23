package com.lz.module.crm.dal.redis;

/**
 * CRM Redis Key 枚举类
 *
 * @author 荔枝源码
 */
public interface CrmRedisKeyConstants {

    /**
     * 序号的缓存
     *
     * KEY 格式：trade_no:{prefix}
     * VALUE 数据格式：编号自增
     */
    String NO = "crm:seq_no:";

    /**
     * 客户来源祖籍name
     */
    public static final String CUSTOMER_SOURCE_ANCESTOR_NAME = "customer:source:ancestor:name";

    /**
     * 客户来源完整树（只包含开启状态的来源）
     *
     * <p>缓存名后缀为 ttl，单位：d 天、h 小时、m 分钟、s 秒
     */
    public static final String CUSTOMER_SOURCE_TREE = "customer:source:tree#30m";

    /**
     * 客户来源名称（根据来源编号获得）
     *
     * <p>缓存名后缀为 ttl，单位：d 天、h 小时、m 分钟、s 秒
     */
    public static final String CUSTOMER_SOURCE_NAME = "customer:source:name#30m";

}
