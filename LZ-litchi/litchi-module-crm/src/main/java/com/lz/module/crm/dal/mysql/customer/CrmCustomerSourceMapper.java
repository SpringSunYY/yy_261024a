package com.lz.module.crm.dal.mysql.customer;

import java.util.*;

import com.lz.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lz.framework.mybatis.core.mapper.BaseMapperX;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceReqVO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerSourceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CRM 客户来源 Mapper
 *
 * @author 荔枝软件
 */
@Mapper
public interface CrmCustomerSourceMapper extends BaseMapperX<CrmCustomerSourceDO> {

    default List<CrmCustomerSourceDO> selectList(CrmCustomerSourceReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CrmCustomerSourceDO>()
                .likeIfPresent(CrmCustomerSourceDO::getName, reqVO.getName())
                .eqIfPresent(CrmCustomerSourceDO::getParentId, reqVO.getParentId())
                .eqIfPresent(CrmCustomerSourceDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CrmCustomerSourceDO::getCreateTime, reqVO.getCreateTime())
                .applyOrderAsc(reqVO, CrmCustomerSourceDO::getOrderNum));
    }

	default CrmCustomerSourceDO selectByParentIdAndName(Long parentId, String name) {
	    return selectOne(CrmCustomerSourceDO::getParentId, parentId, CrmCustomerSourceDO::getName, name);
	}

    default Long selectCountByParentId(Long parentId) {
        return selectCount(CrmCustomerSourceDO::getParentId, parentId);
    }

}
