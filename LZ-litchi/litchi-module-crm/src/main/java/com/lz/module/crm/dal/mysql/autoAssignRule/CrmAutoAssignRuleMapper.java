package com.lz.module.crm.dal.mysql.autoAssignRule;

import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lz.framework.mybatis.core.mapper.BaseMapperX;
import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule.CrmAutoAssignRulePageReqVO;
import com.lz.module.crm.dal.dataobject.autoAssignRule.CrmAutoAssignRuleDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CRM 自动分配规则 Mapper
 *
 * @author 荔枝软件
 */
@Mapper
public interface CrmAutoAssignRuleMapper extends BaseMapperX<CrmAutoAssignRuleDO> {

    default PageResult<CrmAutoAssignRuleDO> selectPage(CrmAutoAssignRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmAutoAssignRuleDO>()
                .eqIfPresent(CrmAutoAssignRuleDO::getIsEnabled, reqVO.getIsEnabled())
                .eqIfPresent(CrmAutoAssignRuleDO::getTriggerActions, reqVO.getTriggerActions())
                .eqIfPresent(CrmAutoAssignRuleDO::getScopeType, reqVO.getScopeType())
                .eqIfPresent(CrmAutoAssignRuleDO::getScopeConditions, reqVO.getScopeConditions())
                .eqIfPresent(CrmAutoAssignRuleDO::getAllocationMethod, reqVO.getAllocationMethod())
                .eqIfPresent(CrmAutoAssignRuleDO::getIsRoundRobin, reqVO.getIsRoundRobin())
                .eqIfPresent(CrmAutoAssignRuleDO::getIsPutPool, reqVO.getIsPutPool())
                .eqIfPresent(CrmAutoAssignRuleDO::getIsPutPoolQuota, reqVO.getIsPutPoolQuota())
                .eqIfPresent(CrmAutoAssignRuleDO::getCurrentCursorIndex, reqVO.getCurrentCursorIndex())
                .eqIfPresent(CrmAutoAssignRuleDO::getCurrentAssigneeId, reqVO.getCurrentAssigneeId())
                .eqIfPresent(CrmAutoAssignRuleDO::getCurrentRemainQuota, reqVO.getCurrentRemainQuota())
                .betweenIfPresent(CrmAutoAssignRuleDO::getCreateTime, reqVO.getCreateTime())
                .applyOrderDesc(reqVO, CrmAutoAssignRuleDO::getId));
    }

}
