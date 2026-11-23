package com.lz.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.lz.framework.common.enums.CommonStatusEnum;
import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.common.util.json.JsonUtils;
import com.lz.framework.common.util.object.BeanUtils;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRulePageReqVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleRespVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleSaveReqVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRuleUser.CrmAutoAssignRuleUserSaveReqVO;
import com.lz.module.crm.dal.dataobject.customer.CrmAutoAssignRuleDO;
import com.lz.module.crm.dal.dataobject.customer.CrmAutoAssignRuleUserDO;
import com.lz.module.crm.dal.dto.customer.CrmAutoAssignRuleScopeConditionDTO;
import com.lz.module.crm.dal.dto.customer.CrmCustomerAssignConditionDto;
import com.lz.module.crm.dal.mysql.autoAssignRuleUser.CrmAutoAssignRuleUserMapper;
import com.lz.module.crm.dal.mysql.customer.CrmAutoAssignRuleMapper;
import com.lz.module.crm.enums.customer.CrmAutoAssignRuleAllocationMethodEnum;
import com.lz.module.crm.enums.customer.CrmAutoAssignRuleIsRoundRobinEnum;
import com.lz.module.crm.enums.customer.CrmAutoAssignRuleScopeTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

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
     * @param rule   待保存的规则
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
     * @param ruleId     规则编号
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
     * @param ruleId    规则
     * @param userId    员工
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
     * @param id     编号
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


    @Override
    public Long getNextOwnerUserId(String triggerAction, CrmCustomerAssignConditionDto condition) {
        //首先获得规则
        CrmAutoAssignRuleDO rule = this.getAutoAssignRule();
        //校验规则是否启用
        if (rule == null
                || !rule.getIsEnabled().equals(CommonStatusEnum.ENABLE.getStatus())) {
            return null;
        }
        //判断是否要执行该规则
        if (!rule.getTriggerActions().contains(triggerAction)) {
            return null;
        }
        //判断是否是可以走规则
        boolean canExecute = false;
        //如果是全部条件,就不需要判断
        if (rule.getScopeType().equals(CrmAutoAssignRuleScopeTypeEnum.SCOPE_TYPE_1.getStatus())) {
            canExecute = true;
        } else {
            canExecute = getCanExecuteByCondition(condition, rule);
        }
        if (!canExecute) {
            return null;
        }
        // 首先拿到规则人员明细列表（按排序号升序，即分配顺序），并过滤掉关闭的用户
        List<CrmAutoAssignRuleUserDO> userList = autoAssignRuleUserMapper.selectListByRuleId(rule.getId());
        userList.removeIf(user -> !user.getStatus().equals(CommonStatusEnum.ENABLE.getStatus()));
        // 没有候选用户了
        if (CollUtil.isEmpty(userList)) {
            return null;
        }
        // 【按数量分配】
        if (isAllocationByQuantity(rule)) {
            // 当前分配员工在候选列表中的位置
            int currentIndex = indexOfUser(userList, rule.getCurrentAssigneeId());
            // 当前分配员工还有剩余额度，就分配给他，额度减 1
            if (rule.getCurrentRemainQuota() != null && rule.getCurrentRemainQuota() > 0 && currentIndex >= 0) {
                return updateRuleAssignee(rule.getId(), rule.getCurrentAssigneeId(), rule.getCurrentCursorIndex(),
                        rule.getCurrentRemainQuota() - 1);
            }
            // 当前分配员工的额度已用完（或还没指定分配员工），取下一个员工分配给他
            // 下一个员工本次已经分配出去一次，所以他的剩余额度 = 分配数值 - 1
            CrmAutoAssignRuleUserDO nextUser = getNextUser(userList, rule, currentIndex);
            if (nextUser == null) {
                return null;
            }
            return updateRuleAssignee(rule.getId(), nextUser.getUserId(), nextUser.getSortOrder(),
                    getRemainQuota(nextUser, true));
        }
        // 【平均分配】：先拿「当前分配员工」作为负责人，再把规则推进到下一个员工
        int currentIndex = indexOfUser(userList, rule.getCurrentAssigneeId());
        // 当前分配员工不存在时，默认取第一个员工作为负责人
        int ownerIndex = currentIndex >= 0 ? currentIndex : 0;
        Long ownerUserId = userList.get(ownerIndex).getUserId();
        // 推进规则到下一个员工（下一个员工本次还没有被分配，所以额度为他的完整分配数值）
        CrmAutoAssignRuleUserDO nextUser = getNextUser(userList, rule, ownerIndex);
        if (nextUser == null) {
            return ownerUserId;
        }
        updateRuleAssignee(rule.getId(), nextUser.getUserId(), nextUser.getSortOrder(),
                getRemainQuota(nextUser, false));
        return ownerUserId;
    }

    /**
     * 是否是「按数量分配」
     */
    private boolean isAllocationByQuantity(CrmAutoAssignRuleDO rule) {
        return rule.getAllocationMethod() != null
                && rule.getAllocationMethod().equals(CrmAutoAssignRuleAllocationMethodEnum.ALLOCATION_METHOD_1.getStatus());
    }

    /**
     * 获得员工在列表中的位置
     *
     * @param userList 候选员工列表
     * @param userId   员工编号
     * @return 位置；不存在时返回 -1
     */
    private int indexOfUser(List<CrmAutoAssignRuleUserDO> userList, Long userId) {
        if (userId == null) {
            return -1;
        }
        for (int i = 0; i < userList.size(); i++) {
            // currentAssigneeId 存的是员工编号 userId，不是明细主键 id
            if (ObjectUtil.equal(userList.get(i).getUserId(), userId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 按「循环分配 / 随机分配」的方式，获得下一个员工
     *
     * @param userList    候选员工列表（已过滤掉关闭的用户，按排序号升序）
     * @param rule        规则
     * @param currentIndex 当前分配员工在列表中的位置；为 -1 时表示当前分配员工不存在
     * @return 下一个员工；没有候选员工时返回 null
     */
    private CrmAutoAssignRuleUserDO getNextUser(List<CrmAutoAssignRuleUserDO> userList, CrmAutoAssignRuleDO rule,
                                                 int currentIndex) {
        if (CollUtil.isEmpty(userList)) {
            return null;
        }
        // 【随机分配】：从候选员工中随机取一个
        if (rule.getIsRoundRobin() != null
                && rule.getIsRoundRobin().equals(CrmAutoAssignRuleIsRoundRobinEnum.IS_ROUND_ROBIN_2.getStatus())) {
            return userList.get(new Random().nextInt(userList.size()));
        }
        // 【循环分配】：取「当前分配员工」的下一个；当前分配员工不存在时，取第一个员工
        int nextIndex = currentIndex >= 0 ? (currentIndex + 1) % userList.size() : 0;
        return userList.get(nextIndex);
    }

    /**
     * 获得员工分配出去后的剩余额度
     *
     * @param user       员工
     * @param allocated  该员工本次是否已经分配出去一次
     * @return 剩余额度
     */
    private Integer getRemainQuota(CrmAutoAssignRuleUserDO user, boolean allocated) {
        int assignValue = user.getAssignValue() != null ? user.getAssignValue() : 1;
        // 已经分配出去一次，额度需要减去这次
        return allocated ? assignValue - 1 : assignValue;
    }

    /**
     * 更新规则的「当前分配员工」与「当前分配员工额度」
     *
     * @param ruleId          规则编号
     * @param assigneeId      当前分配员工
     * @param cursorIndex     当前分配游标索引
     * @param remainQuota     当前分配员工额度（已经是扣减后的值）
     * @return 当前分配员工
     */
    private Long updateRuleAssignee(Long ruleId, Long assigneeId, Integer cursorIndex, Integer remainQuota) {
        CrmAutoAssignRuleDO rule = autoAssignRuleMapper.selectById(ruleId);
        if (rule == null) {
            return null;
        }
        rule.setCurrentAssigneeId(assigneeId);
        rule.setCurrentCursorIndex(cursorIndex);
        rule.setCurrentRemainQuota(remainQuota);
        autoAssignRuleMapper.updateById(rule);
        return assigneeId;
    }

    ;

    /**
     * 根据条件判断规则是否可以执行
     *
     * @param condition 条件
     * @param rule      规则
     * @return 是否可以执行
     */
    private boolean getCanExecuteByCondition(CrmCustomerAssignConditionDto condition, CrmAutoAssignRuleDO rule) {
        //从规则条件中获取具体条件
        CrmAutoAssignRuleScopeConditionDTO scopeCondition = parseScopeConditions(rule.getScopeConditions());
        //如果规则条件为空，则返回false
        if (scopeCondition == null) {
            return false;
        }
        //判断客户来源是否在规则条件中
        if (scopeCondition.getSourceIds() != null && scopeCondition.getSourceIds().contains(condition.getSource())) {
            return true;
        }
        //判断客户行业是否在规则条件中
        if (scopeCondition.getIndustryIds() != null && scopeCondition.getIndustryIds().contains(condition.getIndustryId())) {
            return true;
        }
        //判断客户级别是否在规则条件中
        if (scopeCondition.getLevelIds() != null && scopeCondition.getLevelIds().contains(condition.getLevelId())) {
            return true;
        }
        return false;
    }

    @Override
    public Long getAutoAssignRuleId() {
        CrmAutoAssignRuleDO rule = this.getAutoAssignRule();
        return rule != null ? rule.getId() : null;
    }

    @Override
    public void skipCurrentAssignee(Long ruleId, Long rejectedUserId) {
        CrmAutoAssignRuleDO rule = autoAssignRuleMapper.selectById(ruleId);
        if (rule == null) {
            return;
        }
        // 拿到规则人员明细列表（按排序号升序，即循环分配的顺序）
        List<CrmAutoAssignRuleUserDO> userList = autoAssignRuleUserMapper.selectListByRuleId(ruleId);
        // 过滤掉关闭的用户
        userList.removeIf(user -> !user.getStatus().equals(CommonStatusEnum.ENABLE.getStatus()));
        // 没有可用的员工
        if (CollUtil.isEmpty(userList)) {
            return;
        }
        // 找到被跳过员工在列表中的位置
        int rejectedIndex = indexOfUser(userList, rejectedUserId);
        // 跳过该员工，分配给下一个员工；如果该员工不存在，则默认分配给第一个员工
        int nextIndex = rejectedIndex >= 0 ? (rejectedIndex + 1) % userList.size() : 0;
        CrmAutoAssignRuleUserDO nextUser = userList.get(nextIndex);
        // 更新规则的当前分配员工、游标索引，以及该员工的剩余额度（他本次还没有被分配，所以额度为完整的分配数值）
        rule.setCurrentAssigneeId(nextUser.getUserId());
        rule.setCurrentCursorIndex(nextUser.getSortOrder());
        rule.setCurrentRemainQuota(getRemainQuota(nextUser, false));
        autoAssignRuleMapper.updateById(rule);
    }

    @Override
    public Long getAssignableOwnerUserTotal() {
        // 只统计「当前规则」下启用中的人员明细，与 getNextOwnerUserId 的候选范围保持一致
        CrmAutoAssignRuleDO rule = this.getAutoAssignRule();
        if (rule == null) {
            return 0L;
        }
        return autoAssignRuleUserMapper.selectCount(new LambdaQueryWrapper<CrmAutoAssignRuleUserDO>()
                .eq(CrmAutoAssignRuleUserDO::getRuleId, rule.getId())
                .eq(CrmAutoAssignRuleUserDO::getStatus, CommonStatusEnum.ENABLE.getStatus()));
    }

}
