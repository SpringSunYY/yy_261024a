package com.lz.module.crm.service.customer;

import com.lz.framework.common.pojo.PageResult;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRulePageReqVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleRespVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleSaveReqVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRuleUser.CrmAutoAssignRuleUserSaveReqVO;
import com.lz.module.crm.dal.dataobject.customer.CrmAutoAssignRuleDO;
import com.lz.module.crm.dal.dataobject.customer.CrmAutoAssignRuleUserDO;
import com.lz.module.crm.dal.dto.customer.CrmAutoAssignRuleScopeConditionDTO;
import com.lz.module.crm.dal.dto.customer.CrmCustomerAssignConditionDto;
import jakarta.validation.Valid;

import java.util.List;

/**
 * CRM 自动分配规则 Service 接口
 *
 * @author 荔枝软件
 */
public interface CrmAutoAssignRuleService {

    /**
     * 保存CRM 自动分配规则（存在则更新，不存在则新增）
     *
     * @param saveReqVO 保存信息
     * @return 编号
     */
    Long saveAutoAssignRule(@Valid CrmAutoAssignRuleSaveReqVO saveReqVO);

    /**
     * 删除CRM 自动分配规则
     *
     * @param id 编号
     */
    void deleteAutoAssignRule(Long id);

    /**
     * 获得CRM 自动分配规则
     *
     * @return CRM 自动分配规则，全局只有一条，不存在时返回 null
     */
    CrmAutoAssignRuleDO getAutoAssignRule();

    /**
     * 获得CRM 自动分配规则（具体条件已解析为 DTO）
     *
     * @return CRM 自动分配规则，全局只有一条，不存在时返回 null
     */
    CrmAutoAssignRuleRespVO getAutoAssignRuleResp();

    /**
     * 获得CRM 自动分配规则分页
     *
     * @param pageReqVO 分页查询
     * @return CRM 自动分配规则分页
     */
    PageResult<CrmAutoAssignRuleDO> getAutoAssignRulePage(CrmAutoAssignRulePageReqVO pageReqVO);

    /**
     * 获得CRM 自动分配规则分页（具体条件已解析为各个条件字段）
     *
     * @param pageReqVO 分页查询
     * @return CRM 自动分配规则分页
     */
    PageResult<CrmAutoAssignRuleRespVO> getAutoAssignRuleRespPage(CrmAutoAssignRulePageReqVO pageReqVO);

    /**
     * 解析规则中的「具体条件」（适用范围为「满足条件」时的生效条件）
     *
     * @param scopeConditions 具体条件的 JSON 字符串
     * @return 具体条件；解析失败或为空时返回 null
     */
    CrmAutoAssignRuleScopeConditionDTO parseScopeConditions(String scopeConditions);

    /**
     * 序列化「具体条件」为 JSON 字符串
     *
     * @param scopeCondition 具体条件
     * @return JSON 字符串
     */
    String serializeScopeConditions(CrmAutoAssignRuleScopeConditionDTO scopeCondition);

    /**
     * 解析规则中的「触发动作」
     *
     * @param triggerActions 触发动作的 JSON 数组字符串
     * @return 触发动作列表；解析失败或为空时返回空列表
     */
    List<Integer> parseTriggerActions(String triggerActions);

    /**
     * 序列化「触发动作」为 JSON 数组字符串
     *
     * @param triggerActions 触发动作列表
     * @return JSON 数组字符串
     */
    String serializeTriggerActions(List<Integer> triggerActions);


    // ==================== 子表（CRM  规则人员明细） ====================

    /**
     * 获得CRM  规则人员明细列表
     *
     * @param ruleId 规则
     * @return CRM  规则人员明细列表
     */
    List<CrmAutoAssignRuleUserDO> getAutoAssignRuleUserListByRuleId(Long ruleId);

    /**
     * 新增CRM  规则人员明细
     *
     * @param ruleId    规则
     * @param saveReqVO 保存信息
     * @return 编号
     */
    Long createAutoAssignRuleUser(Long ruleId, @Valid CrmAutoAssignRuleUserSaveReqVO saveReqVO);

    /**
     * 修改CRM  规则人员明细
     *
     * @param ruleId    规则
     * @param saveReqVO 保存信息
     */
    void updateAutoAssignRuleUser(Long ruleId, @Valid CrmAutoAssignRuleUserSaveReqVO saveReqVO);

    /**
     * 更新CRM  规则人员明细的排序号
     *
     * @param ruleId 规则
     * @param ids    规则人员明细编号列表，按目标顺序排列
     */
    void updateAutoAssignRuleUserSort(Long ruleId, @Valid List<Long> ids);

    /**
     * 删除CRM  规则人员明细
     *
     * @param id 编号
     */
    void deleteAutoAssignRuleUser(Long id);


    /**
     * 获得下一个负责人
     *
     * @param triggerAction  触发动作
     * @param condition 具体条件
     * @return 负责人编号
     */
    Long getNextOwnerUserId(String triggerAction, CrmCustomerAssignConditionDto condition);

    /**
     * 获得可分配负责人总数
     * @return 可分配负责人总数
     */
    Long getAssignableOwnerUserTotal();

    /**
     * 跳过某个员工，将规则的当前分配员工更新为下一个员工
     *
     * <p>用于自动分配场景下：分配到的员工拥有的客户数已达到上限，需要跳过该员工，
     * 把规则的当前分配员工更新为下一个员工（按规则人员明细的排序号顺序循环），
     * 并把该员工（下一个员工）的剩余额度重置为他的完整分配数值。
     *
     * <p>若被跳过的员工不存在于规则人员明细中，则默认分配给第一个员工。
     *
     * @param ruleId        规则编号
     * @param rejectedUserId 被跳过的员工编号
     */
    void skipCurrentAssignee(Long ruleId, Long rejectedUserId);

    /**
     * 获得当前的自动分配规则编号
     *
     * @return 规则编号；不存在规则时返回 null
     */
    Long getAutoAssignRuleId();
}
