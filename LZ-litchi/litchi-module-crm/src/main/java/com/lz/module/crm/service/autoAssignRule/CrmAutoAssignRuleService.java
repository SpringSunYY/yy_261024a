package com.lz.module.crm.service.autoAssignRule;

import java.util.*;

import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule.CrmAutoAssignRulePageReqVO;
import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule.CrmAutoAssignRuleSaveReqVO;
import jakarta.validation.*;
import com.lz.module.crm.dal.dataobject.autoAssignRule.CrmAutoAssignRuleDO;
import com.lz.module.crm.dal.dataobject.autoAssignRule.CrmAutoAssignRuleUserDO;
import com.lz.framework.common.pojo.PageResult;

/**
 * CRM 自动分配规则 Service 接口
 *
 * @author 荔枝软件
 */
public interface CrmAutoAssignRuleService {

    /**
     * 创建CRM 自动分配规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createAutoAssignRule(@Valid CrmAutoAssignRuleSaveReqVO createReqVO);

    /**
     * 更新CRM 自动分配规则
     *
     * @param updateReqVO 更新信息
     */
    void updateAutoAssignRule(@Valid CrmAutoAssignRuleSaveReqVO updateReqVO);

    /**
     * 删除CRM 自动分配规则
     *
     * @param id 编号
     */
    void deleteAutoAssignRule(Long id);

    /**
    * 批量删除CRM 自动分配规则
    *
    * @param ids 编号
    */
    void deleteAutoAssignRuleListByIds(List<Long> ids);

    /**
     * 获得CRM 自动分配规则
     *
     * @param id 编号
     * @return CRM 自动分配规则
     */
    CrmAutoAssignRuleDO getAutoAssignRule(Long id);

    /**
     * 获得CRM 自动分配规则分页
     *
     * @param pageReqVO 分页查询
     * @return CRM 自动分配规则分页
     */
    PageResult<CrmAutoAssignRuleDO> getAutoAssignRulePage(CrmAutoAssignRulePageReqVO pageReqVO);


    // ==================== 子表（CRM  规则人员明细） ====================

    /**
     * 获得CRM  规则人员明细列表
     *
     * @param ruleId 规则
     * @return CRM  规则人员明细列表
     */
    List<CrmAutoAssignRuleUserDO> getAutoAssignRuleUserListByRuleId(Long ruleId);

}
