package com.lz.module.crm.enums.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.lz.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 客户限制配置适用类型
 *
 * @author Wanwan
 */
@Getter
@AllArgsConstructor
public enum CrmCustomerLimitConfigApplicableTypeEnum implements ArrayValuable<Integer> {

    /**
     * 部门
     */
    CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE_1(1, "部门"),
    /**
     * 人群
     */
    CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE_2(2, "人群"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(CrmCustomerLimitConfigApplicableTypeEnum::getValue).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer value;
    /**
     * 状态名
     */
    private final String name;

    public static String getNameByType(Integer type) {
        CrmCustomerLimitConfigApplicableTypeEnum typeEnum = CollUtil.findOne(CollUtil.newArrayList(CrmCustomerLimitConfigApplicableTypeEnum.values()),
                item -> ObjUtil.equal(item.value, type));
        return typeEnum == null ? null : typeEnum.getName();
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
