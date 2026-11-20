package com.lz.module.crm.service.customer;

import java.util.*;

import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceReqVO;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceSaveReqVO;
import jakarta.validation.*;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerSourceDO;

/**
 * CRM 客户来源 Service 接口
 *
 * @author 荔枝软件
 */
public interface CrmCustomerSourceService {

    /**
     * 创建CRM 客户来源
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCustomerSource(@Valid CrmCustomerSourceSaveReqVO createReqVO);

    /**
     * 更新CRM 客户来源
     *
     * @param updateReqVO 更新信息
     */
    void updateCustomerSource(@Valid CrmCustomerSourceSaveReqVO updateReqVO);

    /**
     * 删除CRM 客户来源
     *
     * @param id 编号
     * @param isDeleteChildren 是否同时删除子CRM 客户来源
     */
    void deleteCustomerSource(Long id, Boolean isDeleteChildren);


    /**
     * 获得CRM 客户来源
     *
     * @param id 编号
     * @return CRM 客户来源
     */
    CrmCustomerSourceDO getCustomerSource(Long id);

    /**
     * 获得CRM 客户来源列表
     *
     * @param listReqVO 查询条件
     * @return CRM 客户来源列表
     */
    List<CrmCustomerSourceDO> getCustomerSourceList(CrmCustomerSourceReqVO listReqVO);

    /**
     * 获得CRM 客户来源的父级列表（从顶级到直属父级）
     *
     * @param id 编号
     * @return CRM 客户来源父级列表
     */
    List<CrmCustomerSourceDO> getParentCustomerSourceList(Long id);

}
