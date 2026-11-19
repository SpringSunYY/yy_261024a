package com.lz.module.crm.dal.mysql.customer;

import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.mybatis.core.mapper.BaseMapperX;
import com.lz.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lz.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigPageReqVO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;
import com.lz.module.crm.enums.customer.CrmCustomerLimitConfigApplicableTypeEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客户限制配置 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerLimitConfigMapper extends BaseMapperX<CrmCustomerLimitConfigDO> {

    default PageResult<CrmCustomerLimitConfigDO> selectPage(CrmCustomerLimitConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmCustomerLimitConfigDO>()
                .eqIfPresent(CrmCustomerLimitConfigDO::getApplicableType, reqVO.getApplicableType())
                .eqIfPresent(CrmCustomerLimitConfigDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CrmCustomerLimitConfigDO::getType, reqVO.getType())
                .orderByDesc(CrmCustomerLimitConfigDO::getId));
    }

    /**
     * 根据类型和用户ID查询客户限制配置
     *
     * @param type  类型
     * @param userId 用户ID
     * @return 客户限制配置
     */
    default List<CrmCustomerLimitConfigDO> selectListByTypeAndUserId(
            Integer type, Long userId) {
        LambdaQueryWrapperX<CrmCustomerLimitConfigDO> query = new LambdaQueryWrapperX<CrmCustomerLimitConfigDO>()
                .eq(CrmCustomerLimitConfigDO::getType, type)
                .eq(CrmCustomerLimitConfigDO::getApplicableType,
                        CrmCustomerLimitConfigApplicableTypeEnum.CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE_2.getValue());
        query.apply("FIND_IN_SET({0}, user_ids) > 0", userId);
        return selectList(query);
    }

    /**
     * 根据类型和部门ID查询客户限制配置
     *
     * @param type   类型
     * @param deptId 部门ID
     * @return 客户限制配置
     */
    default List<CrmCustomerLimitConfigDO> selectListByTypeAndDeptId(Integer type, Long deptId) {
        LambdaQueryWrapperX<CrmCustomerLimitConfigDO> query = new LambdaQueryWrapperX<CrmCustomerLimitConfigDO>()
                .eq(CrmCustomerLimitConfigDO::getType, type)
                .eq(CrmCustomerLimitConfigDO::getApplicableType,
                        CrmCustomerLimitConfigApplicableTypeEnum.CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE_1.getValue());
        query.apply("FIND_IN_SET({0}, dept_ids) > 0", deptId);
        return selectList(query);

    }
}
