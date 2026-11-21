package com.lz.module.crm.enums.customer;

import com.lz.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CrmAutoAssignRuleAllocationMethodEnum implements ArrayValuable<Integer> {

    ALLOCATION_METHOD_1(1, "按数量分配"),
    ALLOCATION_METHOD_2(2, "平均分配");

    public static final Integer[] ARRAYS =
            Arrays.stream(values()).map(CrmAutoAssignRuleAllocationMethodEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
