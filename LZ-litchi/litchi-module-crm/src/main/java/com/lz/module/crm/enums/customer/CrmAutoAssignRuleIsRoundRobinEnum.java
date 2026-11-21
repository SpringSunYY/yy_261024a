package com.lz.module.crm.enums.customer;

import com.lz.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CrmAutoAssignRuleIsRoundRobinEnum implements ArrayValuable<Integer> {

    IS_ROUND_ROBIN_1(1, "循环分配"),
    IS_ROUND_ROBIN_2(2, "随机分配");

    public static final Integer[] ARRAYS =
            Arrays.stream(values()).map(CrmAutoAssignRuleIsRoundRobinEnum::getStatus).toArray(Integer[]::new);

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
