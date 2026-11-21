package com.lz.module.crm.enums.customer;

import com.lz.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CrmAutoAssignRuleTriggerActionsEnum implements ArrayValuable<Integer> {

    TRIGGER_ACTIONS_1(1, "新增"),
    TRIGGER_ACTIONS_2(2, "转移"),
    TRIGGER_ACTIONS_3(3, "导入");

    public static final Integer[] ARRAYS =
            Arrays.stream(values()).map(CrmAutoAssignRuleTriggerActionsEnum::getStatus).toArray(Integer[]::new);

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
