package com.lz.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRulePageReqVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleRespVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleSaveReqVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRuleUser.CrmAutoAssignRuleUserSaveReqVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import com.lz.module.crm.dal.dataobject.customer.CrmAutoAssignRuleDO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleScopeConditionDTO;
import com.lz.module.crm.dal.dataobject.customer.CrmAutoAssignRuleUserDO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.common.util.json.JsonUtils;
import com.lz.framework.common.util.object.BeanUtils;

import com.lz.module.crm.dal.mysql.customer.CrmAutoAssignRuleMapper;
import com.lz.module.crm.dal.mysql.autoAssignRuleUser.CrmAutoAssignRuleUserMapper;

import static com.lz.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lz.module.crm.enums.ErrorCodeConstants.*;

/**
 * CRM 自动分配规则 Service 实现类
 *
 * @author 荔枝软件
 */
@Service
@Validated
@Slf4j
public class CrmAutoAssignRuleServiceImpl implements CrmAutoAssignRuleService {

    @Resource
    private CrmAutoAssignRuleMapper autoAssignRuleMapper;
    @Resource
    private CrmAutoAssignRuleUserMapper autoAssignRuleUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAutoAssignRule(CrmAutoAssignRuleSaveReqVO saveReqVO) {
        // 1. 存在，则进行更新
        CrmAutoAssignRuleDO dbRule = getAutoAssignRule();
        CrmAutoAssignRuleDO rule = BeanUtils.toBean(saveReqVO, CrmAutoAssignRuleDO.class);
        // 2. 触发动作：组装成 JSON 数组字符串存储
        rule.setTriggerActions(serializeTriggerActions(saveReqVO.getTriggerActions()));
        // 3. 具体条件：组装成 DTO 后序列化为 JSON 存储
        rule.setScopeConditions(serializeScopeConditions(buildScopeCondition(saveReqVO)));
        // 4. 处理当前分配员工：校验 + 计算游标索引
        fillCurrentAssignee(rule, dbRule);
        if (dbRule != null) {
            rule.setId(dbRule.getId());
            autoAssignRuleMapper.updateById(rule);
            return rule.getId();
        }

        // 5. 不存在，则进行插入
        autoAssignRuleMapper.insert(rule);
        return rule.getId();
    }

    /**
     * 组装「具体条件」DTO
     *
     * @param saveReqVO 保存信息
     * @return 具体条件 DTO
     */
    private CrmAutoAssignRuleScopeConditionDTO buildScopeCondition(CrmAutoAssignRuleSaveReqVO saveReqVO) {
        CrmAutoAssignRuleScopeConditionDTO scopeCondition = new CrmAutoAssignRuleScopeConditionDTO();
        scopeCondition.setSourceIds(saveReqVO.getSourceIds());
        scopeCondition.setIndustryIds(saveReqVO.getIndustryIds());
        scopeCondition.setLevelIds(saveReqVO.getLevelIds());
        return scopeCondition;
    }

    /**
     * 处理「当前分配员工」相关字段
     *
     * <p>1. 校验当前分配员工是否存在于规则人员明细中；
     * <p>2. 依据该员工在明细中的顺序，更新当前分配游标索引（从 1 开始，与列表展示的顺序一致）；
     * <p>3. 校验当前分配员工额度不能大于该员工的分配数值。
     *
     * @param rule 待保存的规则
     * @param dbRule 数据库中的规则（新增时为 null）
     */
    private void fillCurrentAssignee(CrmAutoAssignRuleDO rule, CrmAutoAssignRuleDO dbRule) {
        Long assigneeId = rule.getCurrentAssigneeId();
        if (assigneeId == null) {
            // 未指定当前分配员工：游标索引为 0，额度必须为 0
            rule.setCurrentCursorIndex(0);
            rule.setCurrentRemainQuota(0);
            return;
        }
        // 1. 校验当前分配员工存在于规则人员明细中
        Long ruleId = rule.getId() != null ? rule.getId() : (dbRule != null ? dbRule.getId() : null);
        List<CrmAutoAssignRuleUserDO> userList = ruleId != null
                ? autoAssignRuleUserMapper.selectListByRuleId(ruleId)
                : Collections.emptyList();
        int index = -1;
        CrmAutoAssignRuleUserDO assignee = null;
        for (int i = 0; i < userList.size(); i++) {
            if (ObjectUtil.equal(userList.get(i).getUserId(), assigneeId)) {
                index = i;
                assignee = userList.get(i);
                break;
            }
        }
        if (assignee == null) {
            throw exception(AUTO_ASSIGN_RULE_ASSIGNEE_NOT_EXISTS, assigneeId);
        }
        // 2. 依据该员工在明细中的顺序，更新当前分配游标索引（从 1 开始，与列表展示的顺序一致）
        rule.setCurrentCursorIndex(index + 1);
        // 3. 校验当前分配员工额度不能大于该员工的分配数值
        Integer assignValue = assignee.getAssignValue();
        Integer remainQuota = rule.getCurrentRemainQuota();
        if (assignValue == null || remainQuota == null || remainQuota < 0) {
            throw exception(AUTO_ASSIGN_RULE_QUOTA_EXCEED, remainQuota, assignValue);
        }
        if (remainQuota > assignValue) {
            throw exception(AUTO_ASSIGN_RULE_QUOTA_EXCEED, remainQuota, assignValue);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAutoAssignRule(Long id) {
        // 校验存在
        validateAutoAssignRuleExists(id);
        // 删除
        autoAssignRuleMapper.deleteById(id);

        // 删除子表
        autoAssignRuleUserMapper.deleteByRuleId(id);
    }

    private CrmAutoAssignRuleDO validateAutoAssignRuleExists(Long id) {
        CrmAutoAssignRuleDO autoAssignRule = autoAssignRuleMapper.selectById(id);
        if (autoAssignRule == null) {
            throw exception(AUTO_ASSIGN_RULE_NOT_EXISTS);
        }
        return autoAssignRule;
    }

    @Override
    public CrmAutoAssignRuleDO getAutoAssignRule() {
        return autoAssignRuleMapper.selectOne();
    }

    @Override
    public CrmAutoAssignRuleRespVO getAutoAssignRuleResp() {
        CrmAutoAssignRuleDO rule = getAutoAssignRule();
        if (rule == null) {
            return null;
        }
        CrmAutoAssignRuleRespVO respVO = BeanUtils.toBean(rule, CrmAutoAssignRuleRespVO.class);
        // 触发动作：从 JSON 解析为列表
        respVO.setTriggerActions(parseTriggerActions(rule.getTriggerActions()));
        // 具体条件：从 JSON 解析为 DTO，再回填到各个条件字段
        CrmAutoAssignRuleScopeConditionDTO scopeCondition = parseScopeConditions(rule.getScopeConditions());
        if (scopeCondition != null) {
            respVO.setSourceIds(scopeCondition.getSourceIds());
            respVO.setIndustryIds(scopeCondition.getIndustryIds());
            respVO.setLevelIds(scopeCondition.getLevelIds());
        }
        // 当前分配游标索引：按当前人员明细的顺序重新计算
        respVO.setCurrentCursorIndex(getAssigneeCursorIndex(rule.getId(), rule.getCurrentAssigneeId()));
        return respVO;
    }

    /**
     * 获得当前分配员工在规则人员明细中的游标索引（从 1 开始）
     *
     * @param ruleId 规则编号
     * @param assigneeId 当前分配员工编号
     * @return 游标索引；未指定当前分配员工或该员工不在明细中时，返回 0
     */
    private Integer getAssigneeCursorIndex(Long ruleId, Long assigneeId) {
        if (ruleId == null || assigneeId == null) {
            return 0;
        }
        List<CrmAutoAssignRuleUserDO> userList = autoAssignRuleUserMapper.selectListByRuleId(ruleId);
        for (int i = 0; i < userList.size(); i++) {
            if (ObjectUtil.equal(userList.get(i).getUserId(), assigneeId)) {
                return i + 1;
            }
        }
        return 0;
    }

    @Override
    public CrmAutoAssignRuleScopeConditionDTO parseScopeConditions(String scopeConditions) {
        if (StrUtil.isBlank(scopeConditions)) {
            return null;
        }
        try {
            CrmAutoAssignRuleScopeConditionDTO condition = JsonUtils.parseObject(scopeConditions,
                    CrmAutoAssignRuleScopeConditionDTO.class);
            return condition != null ? condition : new CrmAutoAssignRuleScopeConditionDTO();
        } catch (Exception e) {
            log.warn("[parseScopeConditions][解析具体条件失败({})]", scopeConditions, e);
            return null;
        }
    }

    @Override
    public String serializeScopeConditions(CrmAutoAssignRuleScopeConditionDTO scopeCondition) {
        return scopeCondition != null ? JsonUtils.toJsonString(scopeCondition) : null;
    }

    @Override
    public List<Integer> parseTriggerActions(String triggerActions) {
        if (StrUtil.isBlank(triggerActions)) {
            return Collections.emptyList();
        }
        try {
            List<Integer> list = JsonUtils.parseObject(triggerActions, new TypeReference<List<Integer>>() {
            });
            return list != null ? list : Collections.emptyList();
        } catch (Exception e) {
            log.warn("[parseTriggerActions][解析触发动作失败({})]", triggerActions, e);
            return Collections.emptyList();
        }
    }

    @Override
    public String serializeTriggerActions(List<Integer> triggerActions) {
        return CollUtil.isEmpty(triggerActions) ? null : JsonUtils.toJsonString(triggerActions);
    }

    @Override
    public PageResult<CrmAutoAssignRuleDO> getAutoAssignRulePage(CrmAutoAssignRulePageReqVO pageReqVO) {
        return autoAssignRuleMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<CrmAutoAssignRuleRespVO> getAutoAssignRuleRespPage(CrmAutoAssignRulePageReqVO pageReqVO) {
        PageResult<CrmAutoAssignRuleDO> pageResult = getAutoAssignRulePage(pageReqVO);
        List<CrmAutoAssignRuleRespVO> list = new ArrayList<>(pageResult.getList().size());
        for (CrmAutoAssignRuleDO rule : pageResult.getList()) {
            CrmAutoAssignRuleRespVO respVO = BeanUtils.toBean(rule, CrmAutoAssignRuleRespVO.class);
            // 触发动作：从 JSON 解析为列表
            respVO.setTriggerActions(parseTriggerActions(rule.getTriggerActions()));
            // 具体条件：从 JSON 解析为 DTO，再回填到各个条件字段
            CrmAutoAssignRuleScopeConditionDTO scopeCondition = parseScopeConditions(rule.getScopeConditions());
            if (scopeCondition != null) {
                respVO.setSourceIds(scopeCondition.getSourceIds());
                respVO.setIndustryIds(scopeCondition.getIndustryIds());
                respVO.setLevelIds(scopeCondition.getLevelIds());
            }
            // 当前分配游标索引：按当前人员明细的顺序重新计算
            respVO.setCurrentCursorIndex(getAssigneeCursorIndex(rule.getId(), rule.getCurrentAssigneeId()));
            list.add(respVO);
        }
        return new PageResult<>(list, pageResult.getTotal());
    }


    // ==================== 子表（CRM  规则人员明细） ====================

    @Override
    public List<CrmAutoAssignRuleUserDO> getAutoAssignRuleUserListByRuleId(Long ruleId) {
        return autoAssignRuleUserMapper.selectListByRuleId(ruleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAutoAssignRuleUser(Long ruleId, CrmAutoAssignRuleUserSaveReqVO saveReqVO) {
        // 校验：同一个员工只能有一条规则
        validateAutoAssignRuleUserUnique(ruleId, saveReqVO.getUserId(), null);
        // 排序号：排在当前最后一位
        CrmAutoAssignRuleUserDO user = BeanUtils.toBean(saveReqVO, CrmAutoAssignRuleUserDO.class);
        user.setRuleId(ruleId);
        user.setSortOrder(getNextSortOrder(ruleId));
        autoAssignRuleUserMapper.insert(user);
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAutoAssignRuleUser(Long ruleId, CrmAutoAssignRuleUserSaveReqVO saveReqVO) {
        // 校验存在
        CrmAutoAssignRuleUserDO dbUser = validateAutoAssignRuleUserExists(ruleId, saveReqVO.getId());
        // 校验：同一个员工只能有一条规则
        validateAutoAssignRuleUserUnique(ruleId, saveReqVO.getUserId(), dbUser.getId());
        CrmAutoAssignRuleUserDO user = BeanUtils.toBean(saveReqVO, CrmAutoAssignRuleUserDO.class);
        user.setId(dbUser.getId());
        user.setRuleId(ruleId);
        // 排序号保持不变
        user.setSortOrder(dbUser.getSortOrder());
        autoAssignRuleUserMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAutoAssignRuleUserSort(Long ruleId, List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        List<CrmAutoAssignRuleUserDO> oldList = autoAssignRuleUserMapper.selectListByRuleId(ruleId);
        Map<Long, CrmAutoAssignRuleUserDO> idMap = oldList.stream()
                .collect(Collectors.toMap(CrmAutoAssignRuleUserDO::getId, o -> o));

        List<CrmAutoAssignRuleUserDO> updateList = new ArrayList<>();
        int sortOrder = 1;
        for (Long id : ids) {
            CrmAutoAssignRuleUserDO dbUser = idMap.get(id);
            if (dbUser == null) {
                continue;
            }
            CrmAutoAssignRuleUserDO updateUser = new CrmAutoAssignRuleUserDO();
            updateUser.setId(dbUser.getId());
            updateUser.setSortOrder(sortOrder++);
            updateList.add(updateUser);
        }
        if (CollUtil.isNotEmpty(updateList)) {
            autoAssignRuleUserMapper.updateBatch(updateList);
        }
        // 排序变化后，重新计算规则的当前分配游标索引
        refreshRuleCursorIndex(ruleId);
    }

    /**
     * 重新计算并更新规则的「当前分配游标索引」
     *
     * <p>规则人员明细的顺序变化后，当前分配员工在明细中的位置也会变化，
     * 因此需要同步更新规则的游标索引。
     *
     * @param ruleId 规则编号
     */
    private void refreshRuleCursorIndex(Long ruleId) {
        CrmAutoAssignRuleDO rule = autoAssignRuleMapper.selectById(ruleId);
        if (rule == null) {
            return;
        }
        Integer cursorIndex = getAssigneeCursorIndex(ruleId, rule.getCurrentAssigneeId());
        if (ObjectUtil.equal(rule.getCurrentCursorIndex(), cursorIndex)) {
            return;
        }
        CrmAutoAssignRuleDO updateRule = new CrmAutoAssignRuleDO();
        updateRule.setId(ruleId);
        updateRule.setCurrentCursorIndex(cursorIndex);
        autoAssignRuleMapper.updateById(updateRule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAutoAssignRuleUser(Long id) {
        CrmAutoAssignRuleUserDO user = autoAssignRuleUserMapper.selectById(id);
        autoAssignRuleUserMapper.deleteById(id);
        // 删除后当前分配员工的游标索引可能变化，重新计算
        if (user != null) {
            refreshRuleCursorIndex(user.getRuleId());
        }
    }

    /**
     * 校验：同一个员工只能有一条规则人员明细
     *
     * @param ruleId 规则
     * @param userId 员工
     * @param excludeId 需要排除的明细编号（修改场景，即自身）
     */
    private void validateAutoAssignRuleUserUnique(Long ruleId, Long userId, Long excludeId) {
        if (userId == null) {
            return;
        }
        List<CrmAutoAssignRuleUserDO> userList = autoAssignRuleUserMapper.selectListByRuleId(ruleId);
        boolean duplicated = userList.stream()
                .anyMatch(user -> ObjectUtil.equal(user.getUserId(), userId)
                        && !ObjectUtil.equal(user.getId(), excludeId));
        if (duplicated) {
            throw exception(AUTO_ASSIGN_RULE_USER_DUPLICATE, userId);
        }
    }

    /**
     * 校验规则人员明细存在
     *
     * @param ruleId 规则
     * @param id 编号
     * @return 规则人员明细
     */
    private CrmAutoAssignRuleUserDO validateAutoAssignRuleUserExists(Long ruleId, Long id) {
        CrmAutoAssignRuleUserDO user = id != null ? autoAssignRuleUserMapper.selectById(id) : null;
        if (user == null || !ObjectUtil.equal(user.getRuleId(), ruleId)) {
            throw exception(AUTO_ASSIGN_RULE_USER_NOT_EXISTS);
        }
        return user;
    }

    /**
     * 获得下一个排序号（当前最大排序号 + 1）
     */
    private Integer getNextSortOrder(Long ruleId) {
        List<CrmAutoAssignRuleUserDO> userList = autoAssignRuleUserMapper.selectListByRuleId(ruleId);
        int maxSortOrder = userList.stream()
                .map(CrmAutoAssignRuleUserDO::getSortOrder)
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(Integer::intValue))
                .orElse(0);
        return maxSortOrder + 1;
    }

}
