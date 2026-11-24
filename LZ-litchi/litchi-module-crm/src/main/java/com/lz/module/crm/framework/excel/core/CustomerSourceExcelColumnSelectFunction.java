package com.lz.module.crm.framework.excel.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.lz.framework.excel.core.function.ExcelColumnSelectFunction;
import com.lz.module.crm.service.customer.CrmCustomerSourceService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 客户来源下拉框数据源的 {@link ExcelColumnSelectFunction} 实现类
 *
 * <p>下拉选项为客户来源路径，格式为「顶级/子级/自身」，例如 {@code 官网/广告投放}
 *
 * @author 荔枝软件
 */
@Service
public class CustomerSourceExcelColumnSelectFunction implements ExcelColumnSelectFunction {

    public static final String NAME = "getCrmCustomerSourceNameList"; // 防止和别的模块重名

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getOptions() {
        // 获取客户来源下拉数据
        Map<Long, String> pathMap = SpringUtil.getBean(CrmCustomerSourceService.class).getCustomerSourcePathMap();
        if (CollUtil.isEmpty(pathMap)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(pathMap.values());
    }

}
