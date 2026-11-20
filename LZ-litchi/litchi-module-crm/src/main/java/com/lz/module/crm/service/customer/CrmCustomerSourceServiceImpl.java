package com.lz.module.crm.service.customer;

import com.lz.framework.common.enums.CommonConstants;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceReqVO;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceSaveReqVO;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import com.lz.module.crm.dal.dataobject.customer.CrmCustomerSourceDO;
import com.lz.framework.common.util.object.BeanUtils;

import com.lz.module.crm.dal.mysql.customer.CrmCustomerSourceMapper;

import static com.lz.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lz.framework.common.util.collection.CollectionUtils.convertList;
import static com.lz.module.crm.enums.ErrorCodeConstants.*;

/**
 * CRM 客户来源 Service 实现类
 *
 * @author 荔枝软件
 */
@Service
@Validated
public class CrmCustomerSourceServiceImpl implements CrmCustomerSourceService {

    @Resource
    private CrmCustomerSourceMapper customerSourceMapper;

    @Override
    public Long createCustomerSource(CrmCustomerSourceSaveReqVO createReqVO) {
        // 校验父级编号的有效性
        validateParentCustomerSource(null, createReqVO.getParentId());
        // 校验分类名称的唯一性
        validateCustomerSourceNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

        // 插入
        CrmCustomerSourceDO customerSource = BeanUtils.toBean(createReqVO, CrmCustomerSourceDO.class);
        customerSourceMapper.insert(customerSource);

        // 返回
        return customerSource.getId();
    }

    @Override
    public void updateCustomerSource(CrmCustomerSourceSaveReqVO updateReqVO) {
        // 校验存在
        validateCustomerSourceExists(updateReqVO.getId());
        // 校验父级编号的有效性
        validateParentCustomerSource(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验分类名称的唯一性
        validateCustomerSourceNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新
        CrmCustomerSourceDO updateObj = BeanUtils.toBean(updateReqVO, CrmCustomerSourceDO.class);
        customerSourceMapper.updateById(updateObj);
    }

    @Override
    public void deleteCustomerSource(Long id) {
        // 校验存在
        validateCustomerSourceExists(id);
        // 校验是否有子CRM 客户来源
        if (customerSourceMapper.selectCountByParentId(id) > 0) {
            throw exception(CUSTOMER_SOURCE_EXIST_CHILDREN);
        }
        // 删除
        customerSourceMapper.deleteById(id);
    }


    private CrmCustomerSourceDO validateCustomerSourceExists(Long id) {
        CrmCustomerSourceDO customerSource =customerSourceMapper.selectById(id);
        if (customerSource == null) {
            throw exception(CUSTOMER_SOURCE_NOT_EXISTS);
        }
        return customerSource;
    }

    private void validateParentCustomerSource(Long id, Long parentId) {
        if (parentId == null || CommonConstants.ANCESTORS_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父CRM 客户来源
        if (Objects.equals(id, parentId)) {
            throw exception(CUSTOMER_SOURCE_PARENT_ERROR);
        }
        // 2. 父CRM 客户来源不存在
        CrmCustomerSourceDO parentCustomerSource = customerSourceMapper.selectById(parentId);
        if (parentCustomerSource == null) {
            throw exception(CUSTOMER_SOURCE_PARENT_NOT_EXISTS);
        }
        // 3. 递归校验父CRM 客户来源，如果父CRM 客户来源是自己的子CRM 客户来源，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentCustomerSource.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(CUSTOMER_SOURCE_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父CRM 客户来源
            if (parentId == null || CrmCustomerSourceDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentCustomerSource = customerSourceMapper.selectById(parentId);
            if (parentCustomerSource == null) {
                break;
            }
        }
    }

    private void validateCustomerSourceNameUnique(Long id, Long parentId, String name) {
        CrmCustomerSourceDO customerSource = customerSourceMapper.selectByParentIdAndName(parentId, name);
        if (customerSource == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的CRM 客户来源
        if (id == null) {
            throw exception(CUSTOMER_SOURCE_DUPLICATE);
        }
        if (!Objects.equals(customerSource.getId(), id)) {
            throw exception(CUSTOMER_SOURCE_DUPLICATE);
        }
    }

    @Override
    public CrmCustomerSourceDO getCustomerSource(Long id) {
        return customerSourceMapper.selectById(id);
    }

    @Override
    public List<CrmCustomerSourceDO> getCustomerSourceList(CrmCustomerSourceReqVO listReqVO) {
        return customerSourceMapper.selectList(listReqVO);
    }

}
