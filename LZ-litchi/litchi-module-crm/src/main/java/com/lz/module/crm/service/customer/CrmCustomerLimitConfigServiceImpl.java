package com.lz.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.lz.framework.common.enums.CommonStatusEnum;
import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.common.util.object.BeanUtils;
import com.lz.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigPageReqVO;
import com.lz.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigSaveReqVO;
import com.lz.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigSimpVO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;
import com.lz.module.crm.dal.mysql.customer.CrmCustomerLimitConfigMapper;
import com.lz.module.crm.enums.customer.CrmCustomerLimitConfigApplicableTypeEnum;
import com.lz.module.crm.enums.customer.CrmCustomerLimitConfigTypeEnum;
import com.lz.module.system.api.dept.DeptApi;
import com.lz.module.system.api.dept.dto.DeptRespDTO;
import com.lz.module.system.api.user.AdminUserApi;
import com.lz.module.system.api.user.dto.AdminUserRespDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;

import static com.lz.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lz.module.crm.enums.ErrorCodeConstants.*;
import static com.lz.module.crm.enums.LogRecordConstants.*;

/**
 * 客户限制配置 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Validated
public class CrmCustomerLimitConfigServiceImpl implements CrmCustomerLimitConfigService {

    @Resource
    private CrmCustomerLimitConfigMapper customerLimitConfigMapper;

    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @LogRecord(type = CRM_CUSTOMER_LIMIT_CONFIG_TYPE, subType = CRM_CUSTOMER_LIMIT_CONFIG_CREATE_SUB_TYPE, bizNo = "{{#limitId}}",
            success = CRM_CUSTOMER_LIMIT_CONFIG_CREATE_SUCCESS)
    public Long createCustomerLimitConfig(CrmCustomerLimitConfigSaveReqVO createReqVO) {
        validateUserAndDept(createReqVO.getUserIds(), createReqVO.getDeptIds(), createReqVO.getApplicableType());
        validateCustomerOrConfigExists(null, createReqVO.getType(), createReqVO.getApplicableType(), createReqVO.getUserIds(), createReqVO.getDeptIds());
        // 插入
        CrmCustomerLimitConfigDO limitConfig = BeanUtils.toBean(createReqVO, CrmCustomerLimitConfigDO.class);
        customerLimitConfigMapper.insert(limitConfig);

        // 记录操作日志上下文
        LogRecordContext.putVariable("limitType", CrmCustomerLimitConfigTypeEnum.getNameByType(limitConfig.getType()));
        LogRecordContext.putVariable("limitId", limitConfig.getId());
        return limitConfig.getId();
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_LIMIT_CONFIG_TYPE, subType = CRM_CUSTOMER_LIMIT_CONFIG_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_CUSTOMER_LIMIT_CONFIG_UPDATE_SUCCESS)
    public void updateCustomerLimitConfig(CrmCustomerLimitConfigSaveReqVO updateReqVO) {
        // 校验存在
        CrmCustomerLimitConfigDO oldLimitConfig = validateCustomerLimitConfigExists(updateReqVO.getId());
        validateUserAndDept(updateReqVO.getUserIds(), updateReqVO.getDeptIds(), updateReqVO.getApplicableType());
        //校验客户或者配置是否已存在限制
        validateCustomerOrConfigExists(updateReqVO.getId(), updateReqVO.getType(), updateReqVO.getApplicableType(), updateReqVO.getUserIds(), updateReqVO.getDeptIds());
        // 更新
        CrmCustomerLimitConfigDO updateObj = BeanUtils.toBean(updateReqVO, CrmCustomerLimitConfigDO.class);
        customerLimitConfigMapper.updateById(updateObj);

        // 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldLimitConfig, CrmCustomerLimitConfigSaveReqVO.class));
    }

    /**
     * 校验该用户/部门在当前规则类型下，是否已经存在限制配置
     *
     * @param id             配置编号，为空表示新增
     * @param type           规则类型
     * @param applicableType 适用类型：1-部门，2-人群
     * @param userIds        适用人群
     * @param deptIds        适用部门
     */
    private void validateCustomerOrConfigExists(Long id, Integer type, Integer applicableType,
                                               List<Long> userIds, List<Long> deptIds) {
        // 人群适用：用户不能为空，且同一规则类型下，一个用户只能存在一条限制配置
        if (Objects.equals(applicableType, CrmCustomerLimitConfigApplicableTypeEnum.CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE_2.getValue())) {
            if (CollUtil.isEmpty(userIds)) {
                throw exception(CUSTOMER_LIMIT_CONFIG_USER_NOT_NULL);
            }
            for (Long userId : userIds) {
                for (CrmCustomerLimitConfigDO config : customerLimitConfigMapper.selectListByTypeAndUserId(type, userId)) {
                    // 更新时，跳过自身
                    if (id != null && id.equals(config.getId())) {
                        continue;
                    }
                    AdminUserRespDTO user = adminUserApi.getUser(userId);
                    throw exception(CUSTOMER_LIMIT_CONFIG_USER_EXISTS,
                            user != null ? user.getNickname() : String.valueOf(userId), config.getName(), config.getId());
                }
            }
            return;
        }
        // 部门适用：部门不能为空，且同一规则类型下，一个部门只能存在一条限制配置
        if (CollUtil.isEmpty(deptIds)) {
            throw exception(CUSTOMER_LIMIT_CONFIG_DEPT_NOT_NULL);
        }
        for (Long deptId : deptIds) {
            for (CrmCustomerLimitConfigDO config : customerLimitConfigMapper.selectListByTypeAndDeptId(type, deptId)) {
                // 更新时，跳过自身
                if (id != null && id.equals(config.getId())) {
                    continue;
                }
                DeptRespDTO dept = deptApi.getDept(deptId);
                throw exception(CUSTOMER_LIMIT_CONFIG_DEPT_EXISTS,
                        dept != null ? dept.getName() : String.valueOf(deptId), config.getMaxCount(), config.getId());
            }
        }
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_LIMIT_CONFIG_TYPE, subType = CRM_CUSTOMER_LIMIT_CONFIG_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CUSTOMER_LIMIT_CONFIG_DELETE_SUCCESS)
    public void deleteCustomerLimitConfig(Long id) {
        // 校验存在
        CrmCustomerLimitConfigDO limitConfig = validateCustomerLimitConfigExists(id);
        // 删除
        customerLimitConfigMapper.deleteById(id);

        // 记录操作日志上下文
        LogRecordContext.putVariable("limitType", CrmCustomerLimitConfigTypeEnum.getNameByType(limitConfig.getType()));
    }

    @Override
    public CrmCustomerLimitConfigDO getCustomerLimitConfig(Long id) {
        return customerLimitConfigMapper.selectById(id);
    }

    @Override
    public PageResult<CrmCustomerLimitConfigDO> getCustomerLimitConfigPage(CrmCustomerLimitConfigPageReqVO pageReqVO) {
        return customerLimitConfigMapper.selectPage(pageReqVO);
    }

    private CrmCustomerLimitConfigDO validateCustomerLimitConfigExists(Long id) {
        CrmCustomerLimitConfigDO limitConfigDO = customerLimitConfigMapper.selectById(id);
        if (limitConfigDO == null) {
            throw exception(CUSTOMER_LIMIT_CONFIG_NOT_EXISTS);
        }
        return limitConfigDO;
    }

    /**
     * 校验入参的用户和部门
     *
     * @param userIds        用户 ids
     * @param deptIds        部门 ids
     * @param applicableType 适用类型：1-部门，2-人群
     */
    private void validateUserAndDept(Collection<Long> userIds, Collection<Long> deptIds, Integer applicableType) {
        if (Objects.equals(applicableType, CrmCustomerLimitConfigApplicableTypeEnum.CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE_2.getValue())) {
            adminUserApi.validateUserList(userIds);
        } else {
            deptApi.validateDeptList(deptIds);
        }
    }

    @Override
    public CrmCustomerLimitConfigSimpVO getCustomerLimitConfigListByUserId(Integer type, Long userId) {
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        Assert.notNull(user, "用户({})不存在", userId);
        // 查询该类型下所有「开启中」的限制配置，避免按部门逐个查询
        List<CrmCustomerLimitConfigDO> limitConfigs = customerLimitConfigMapper.selectListByTypeAndUserId(type, userId).stream()
                .filter(config -> Objects.equals(config.getStatus(), CommonStatusEnum.ENABLE.getStatus()))
                .toList();
        // 1. 用户自身的限制，优先级最高
        List<CrmCustomerLimitConfigDO> userConfigs = limitConfigs.stream()
                .filter(config -> CollUtil.contains(config.getUserIds(), userId))
                .toList();
        if (!userConfigs.isEmpty()) {
            // 返回用户自身的限制配置，实际上是一个列表，但这里只取第一个
            CrmCustomerLimitConfigSimpVO configSimpVO = BeanUtils.toBean(userConfigs.getFirst(), CrmCustomerLimitConfigSimpVO.class);
            configSimpVO.setUserName(user.getNickname());
            return configSimpVO;
        }
        // 2. 部门限制：自己所在部门 -> 直属上级 -> ... -> 顶级部门，越靠近用户优先级越高
        if (user.getDeptId() == null) {
            return new CrmCustomerLimitConfigSimpVO();
        }
        List<Long> deptIds = new ArrayList<>();
        deptIds.add(user.getDeptId());
        deptApi.getParentDeptList(user.getDeptId()).forEach(parentDept -> deptIds.add(parentDept.getId()));
        for (Long deptId : deptIds) {
            List<CrmCustomerLimitConfigDO> deptConfigs = customerLimitConfigMapper.selectListByTypeAndDeptId(type, deptId).stream()
                    .filter(config -> Objects.equals(config.getStatus(), CommonStatusEnum.ENABLE.getStatus()))
                    .toList();
            // 如果该部门已经有限制了，则不再查询上级部门限制，因为部门限制优先级更高
            if (!deptConfigs.isEmpty()) {
                // 返回部门限制配置，实际上是一个列表，但这里只取第一个
                CrmCustomerLimitConfigSimpVO configSimpVO = BeanUtils.toBean(deptConfigs.getFirst(), CrmCustomerLimitConfigSimpVO.class);
                configSimpVO.setUserName(user.getNickname());
                configSimpVO.setDeptName(deptApi.getDept(deptId).getName());
                return configSimpVO;
            }
        }
        return new CrmCustomerLimitConfigSimpVO();
    }

}
