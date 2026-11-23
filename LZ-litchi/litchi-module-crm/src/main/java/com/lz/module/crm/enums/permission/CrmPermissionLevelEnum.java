package com.lz.module.crm.enums.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.lz.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * CRM 数据权限级别枚举
 *
 * OWNER > WRITE > READ
 *
 * @author HUIHUI
 */
@Getter
@AllArgsConstructor
public enum CrmPermissionLevelEnum implements ArrayValuable<Long> {

    OWNER(1L, "负责人"),
    READ(2L, "只读"),
    WRITE(3L, "读写");

    public static final Long[] ARRAYS = Arrays.stream(values()).map(CrmPermissionLevelEnum::getLevel).toArray(Long[]::new);

    /**
     * 级别
     */
    private final Long level;
    /**
     * 级别名称
     */
    private final String name;

    @Override
    public Long[] array() {
        return ARRAYS;
    }

    public static boolean isOwner(Long level) {
        return ObjUtil.equal(OWNER.level, level);
    }

    public static boolean isRead(Long level) {
        return ObjUtil.equal(READ.level, level);
    }

    public static boolean isWrite(Long level) {
        return ObjUtil.equal(WRITE.level, level);
    }

    public static String getNameByLevel(Long level) {
        CrmPermissionLevelEnum typeEnum = CollUtil.findOne(CollUtil.newArrayList(CrmPermissionLevelEnum.values()),
                item -> ObjUtil.equal(item.level, level));
        return typeEnum == null ? null : typeEnum.getName();
    }

}
