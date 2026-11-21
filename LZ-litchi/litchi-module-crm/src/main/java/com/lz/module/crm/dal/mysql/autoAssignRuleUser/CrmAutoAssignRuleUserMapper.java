package com.lz.module.crm.dal.mysql.autoAssignRuleUser;

import java.util.*;

import com.lz.framework.mybatis.core.mapper.BaseMapperX;
import com.lz.module.crm.dal.dataobject.autoAssignRule.CrmAutoAssignRuleUserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CRM  规则人员明细 Mapper
 *
 * @author 荔枝软件
 */
@Mapper
public interface CrmAutoAssignRuleUserMapper extends BaseMapperX<CrmAutoAssignRuleUserDO> {

    default List<CrmAutoAssignRuleUserDO> selectListByRuleId(Long ruleId) {
        return selectList(CrmAutoAssignRuleUserDO::getRuleId, ruleId);
    }

    default int deleteByRuleId(Long ruleId) {
        return delete(CrmAutoAssignRuleUserDO::getRuleId, ruleId);
    }

	default int deleteByRuleIds(List<Long> ruleIds) {
	    return deleteBatch(CrmAutoAssignRuleUserDO::getRuleId, ruleIds);
	}

}
