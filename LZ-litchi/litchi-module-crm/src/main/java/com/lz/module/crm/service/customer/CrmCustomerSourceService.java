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

    /**
     * 校验CRM 客户来源是否存在，如果存在则返回true
     *
     * @param newSourceId 新来源编号 如果只有新编号，只查询是否存在
     * @param oldSourceId 旧来源编号 如果有旧编号，校验新编号和旧编号是否一致，如果一致则返回true
     * @return 是否存在
     */
    void validateCustomerSourceExists(Long newSourceId, Long oldSourceId);

    /**
     * 获得CRM 客户来源的祖先名称
     *
     * @param source 来源
     * @return 祖先名称
     */
    String getCustomerSourceAncestorsName(Long source);

    /**
     * 获得CRM 客户来源的完整树（只包含开启状态的来源）
     *
     * <p>用于前端的级联选择器，顶级为 parentId = 0 的数据
     *
     * @return 客户来源树
     */
    List<CrmCustomerSourceDO> getCustomerSourceTree();

    /**
     * 获得CRM 客户来源的链路（从顶级到自身，包含自身）
     *
     * <p>用于前端编辑时，还原级联选择器已经选择的每一级
     *
     * @param id 编号
     * @return 来源链路，顺序为：顶级 -> 自身
     */
    List<CrmCustomerSourceDO> getCustomerSourceChain(Long id);

    /**
     * 获得CRM 客户来源的名称
     *
     * @param source 来源编号
     * @return 来源名称
     */
    String getCustomerSourceName(Long source);
}
