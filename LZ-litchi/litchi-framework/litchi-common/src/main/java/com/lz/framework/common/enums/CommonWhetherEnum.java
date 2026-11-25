package com.lz.framework.common.enums;

import com.lz.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CommonWhetherEnum implements ArrayValuable<Integer> {

    WHETHER_0(0, "是"),
    WHETHER_1(1, "否");

    public static final Integer[] ARRAYS =
            Arrays.stream(values()).map(CommonWhetherEnum::getStatus).toArray(Integer[]::new);

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
