package com.lz.module.crm.framework.operatelog.core;

import cn.hutool.core.util.StrUtil;
import com.lz.module.crm.service.customer.CrmCustomerSourceService;
import com.mzt.logapi.service.IParseFunction;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * CRM 客户来源编号的 {@link IParseFunction} 实现类
 *
 * <p>客户表的 source 字段存储的是「CRM 客户来源」表中的编号，因此这里通过来源服务获取名称
 *
 * @author 荔枝软件
 */
@Component
@Slf4j
public class CrmCustomerSourceIdParseFunction implements IParseFunction {

    public static final String NAME = "getCustomerSourceIdName";

    @Resource
    private CrmCustomerSourceService customerSourceService;

    @Override
    public boolean executeBefore() {
        return true; // 先转换值后对比
    }

    @Override
    public String functionName() {
        return NAME;
    }

    @Override
    public String apply(Object value) {
        if (StrUtil.isEmptyIfStr(value)) {
            return "";
        }
        return customerSourceService.getCustomerSourceName(value.toString());
    }

}
