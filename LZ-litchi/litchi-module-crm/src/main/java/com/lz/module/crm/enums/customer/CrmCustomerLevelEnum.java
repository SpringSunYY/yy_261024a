package com.lz.module.crm.enums.customer;

import com.lz.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 客户等级
 *
 * @author Wanwan
 */
@Getter
@AllArgsConstructor
public enum CrmCustomerLevelEnum implements ArrayValuable<Long> {

    IMPORTANT(1L, "A（重点客户）"),
    GENERAL(2L, "B（普通客户）"),
    LOW_PRIORITY(3L, "C（非优先客户）");

    public static final Long[] ARRAYS = Arrays.stream(values()).map(CrmCustomerLevelEnum::getLevel).toArray(Long[]::new);

    /**
     * 状态
     */
    private final Long level;
    /**
     * 状态名
     */
    private final String name;

    @Override
    public Long[] array() {
        return ARRAYS;
    }

}
