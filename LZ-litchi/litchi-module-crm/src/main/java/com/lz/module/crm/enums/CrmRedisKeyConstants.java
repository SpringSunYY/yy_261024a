package com.lz.module.crm.enums;

/**
 * crm redis key constants
 *
 * @Project: LZ-litchi
 * @Author: YY
 * @CreateTime: 2026-11-20  17:54
 * @Version: 1.0
 */
public class CrmRedisKeyConstants {
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
