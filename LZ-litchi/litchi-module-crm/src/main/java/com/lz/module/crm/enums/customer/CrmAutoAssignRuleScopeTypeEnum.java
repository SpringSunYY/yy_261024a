package com.lz.module.crm.enums.customer;

import com.lz.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CrmAutoAssignRuleScopeTypeEnum implements ArrayValuable<Integer> {

    SCOPE_TYPE_1(1, "全部"),
    SCOPE_TYPE_2(2, "满足条件");

    public static final Integer[] ARRAYS =
            Arrays.stream(values()).map(CrmAutoAssignRuleScopeTypeEnum::getStatus).toArray(Integer[]::new);

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
