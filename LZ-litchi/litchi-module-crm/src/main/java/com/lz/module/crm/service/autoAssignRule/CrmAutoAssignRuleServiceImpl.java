package com.lz.module.crm.service.autoAssignRule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule.CrmAutoAssignRulePageReqVO;
import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule.CrmAutoAssignRuleSaveReqVO;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import com.lz.module.crm.dal.dataobject.autoAssignRule.CrmAutoAssignRuleDO;
import com.lz.module.crm.dal.dataobject.autoAssignRule.CrmAutoAssignRuleUserDO;
import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.common.util.object.BeanUtils;

import com.lz.module.crm.dal.mysql.autoAssignRule.CrmAutoAssignRuleMapper;
import com.lz.module.crm.dal.mysql.autoAssignRuleUser.CrmAutoAssignRuleUserMapper;

import static com.lz.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lz.framework.common.util.collection.CollectionUtils.convertList;
import static com.lz.framework.common.util.collection.CollectionUtils.diffList;
import static com.lz.module.crm.enums.ErrorCodeConstants.*;

/**
 * CRM 自动分配规则 Service 实现类
 *
 * @author 荔枝软件
 */
@Service
@Validated
public class CrmAutoAssignRuleServiceImpl implements CrmAutoAssignRuleService {

    @Resource
    private CrmAutoAssignRuleMapper autoAssignRuleMapper;
    @Resource
    private CrmAutoAssignRuleUserMapper autoAssignRuleUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAutoAssignRule(CrmAutoAssignRuleSaveReqVO createReqVO) {
        // 插入
        CrmAutoAssignRuleDO autoAssignRule = BeanUtils.toBean(createReqVO, CrmAutoAssignRuleDO.class);
        autoAssignRuleMapper.insert(autoAssignRule);


        // 插入子表
        createAutoAssignRuleUserList(autoAssignRule.getId(), createReqVO.getAutoAssignRuleUsers());
        // 返回
        return autoAssignRule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAutoAssignRule(CrmAutoAssignRuleSaveReqVO updateReqVO) {
        // 校验存在
        validateAutoAssignRuleExists(updateReqVO.getId());
        // 更新
        CrmAutoAssignRuleDO updateObj = BeanUtils.toBean(updateReqVO, CrmAutoAssignRuleDO.class);
        autoAssignRuleMapper.updateById(updateObj);

        // 更新子表
        updateAutoAssignRuleUserList(updateReqVO.getId(), updateReqVO.getAutoAssignRuleUsers());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAutoAssignRule(Long id) {
        // 校验存在
        validateAutoAssignRuleExists(id);
        // 删除
        autoAssignRuleMapper.deleteById(id);

        // 删除子表
        deleteAutoAssignRuleUserByRuleId(id);
    }

    @Override
        @Transactional(rollbackFor = Exception.class)
    public void deleteAutoAssignRuleListByIds(List<Long> ids) {
        // 删除
        autoAssignRuleMapper.deleteByIds(ids);

    // 删除子表
            deleteAutoAssignRuleUserByRuleIds(ids);
    }


    private CrmAutoAssignRuleDO validateAutoAssignRuleExists(Long id) {
        CrmAutoAssignRuleDO autoAssignRule =autoAssignRuleMapper.selectById(id);
        if (autoAssignRule == null) {
            throw exception(AUTO_ASSIGN_RULE_NOT_EXISTS);
        }
        return autoAssignRule;
    }

    @Override
    public CrmAutoAssignRuleDO getAutoAssignRule(Long id) {
        return autoAssignRuleMapper.selectById(id);
    }

    @Override
    public PageResult<CrmAutoAssignRuleDO> getAutoAssignRulePage(CrmAutoAssignRulePageReqVO pageReqVO) {
        return autoAssignRuleMapper.selectPage(pageReqVO);
    }


    // ==================== 子表（CRM  规则人员明细） ====================

    @Override
    public List<CrmAutoAssignRuleUserDO> getAutoAssignRuleUserListByRuleId(Long ruleId) {
        return autoAssignRuleUserMapper.selectListByRuleId(ruleId);
    }

    private void createAutoAssignRuleUserList(Long ruleId, List<CrmAutoAssignRuleUserDO> list) {
        list.forEach(o -> o.setRuleId(ruleId).clean());
        autoAssignRuleUserMapper.insertBatch(list);
    }

    private void updateAutoAssignRuleUserList(Long ruleId, List<CrmAutoAssignRuleUserDO> list) {
	    list.forEach(o -> o.setRuleId(ruleId).clean());
	    List<CrmAutoAssignRuleUserDO> oldList = autoAssignRuleUserMapper.selectListByRuleId(ruleId);
	    List<List<CrmAutoAssignRuleUserDO>> diffList = diffList(oldList, list, (oldVal, newVal) -> {
            boolean same = ObjectUtil.equal(oldVal.getId(), newVal.getId());
            if (same) {
                newVal.setId(oldVal.getId()).clean(); // 解决更新情况下：updateTime 不更新
            }
            return same;
	    });

	    // 第二步，批量添加、修改、删除
	    if (CollUtil.isNotEmpty(diffList.get(0))) {
	        autoAssignRuleUserMapper.insertBatch(diffList.get(0));
	    }
	    if (CollUtil.isNotEmpty(diffList.get(1))) {
	        autoAssignRuleUserMapper.updateBatch(diffList.get(1));
	    }
	    if (CollUtil.isNotEmpty(diffList.get(2))) {
	        autoAssignRuleUserMapper.deleteByIds(convertList(diffList.get(2), CrmAutoAssignRuleUserDO::getId));
	    }
    }

    private void deleteAutoAssignRuleUserByRuleId(Long ruleId) {
        autoAssignRuleUserMapper.deleteByRuleId(ruleId);
    }

	private void deleteAutoAssignRuleUserByRuleIds(List<Long> ruleIds) {
        autoAssignRuleUserMapper.deleteByRuleIds(ruleIds);
	}


}
