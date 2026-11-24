package com.lz.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.lz.framework.common.exception.ServiceException;
import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.common.util.object.BeanUtils;
import com.lz.framework.common.util.object.ObjectUtils;
import com.lz.module.crm.controller.admin.business.vo.business.CrmBusinessTransferReqVO;
import com.lz.module.crm.controller.admin.contact.vo.CrmContactTransferReqVO;
import com.lz.module.crm.controller.admin.contract.vo.contract.CrmContractTransferReqVO;
import com.lz.module.crm.controller.admin.customer.vo.customer.*;
import com.lz.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigSimpVO;
import com.lz.module.crm.dal.dataobject.business.CrmBusinessDO;
import com.lz.module.crm.dal.dataobject.contact.CrmContactDO;
import com.lz.module.crm.dal.dataobject.contract.CrmContractDO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerDO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerPoolConfigDO;
import com.lz.module.crm.dal.dto.customer.CrmCustomerAssignConditionDto;
import com.lz.module.crm.dal.mysql.customer.CrmCustomerMapper;
import com.lz.module.crm.enums.common.CrmBizTypeEnum;
import com.lz.module.crm.enums.common.CrmSceneTypeEnum;
import com.lz.module.crm.enums.customer.CrmAutoAssignRuleTriggerActionsEnum;
import com.lz.module.crm.enums.customer.CrmCustomerLimitConfigApplicableTypeEnum;
import com.lz.module.crm.enums.permission.CrmPermissionLevelEnum;
import com.lz.module.crm.framework.permission.core.annotations.CrmPermission;
import com.lz.module.crm.service.business.CrmBusinessService;
import com.lz.module.crm.service.contact.CrmContactService;
import com.lz.module.crm.service.contract.CrmContractService;
import com.lz.module.crm.service.customer.bo.CrmCustomerCreateReqBO;
import com.lz.module.crm.service.permission.CrmPermissionService;
import com.lz.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import com.lz.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import com.lz.module.system.api.user.AdminUserApi;
import com.lz.module.system.api.user.dto.AdminUserRespDTO;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static com.lz.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lz.framework.common.util.collection.CollectionUtils.filterList;
import static com.lz.module.crm.enums.ErrorCodeConstants.*;
import static com.lz.module.crm.enums.LogRecordConstants.*;
import static com.lz.module.crm.enums.customer.CrmCustomerLimitConfigTypeEnum.CUSTOMER_LOCK_LIMIT;
import static com.lz.module.crm.enums.customer.CrmCustomerLimitConfigTypeEnum.CUSTOMER_OWNER_LIMIT;
import static java.util.Collections.singletonList;

/**
 * 客户 Service 实现类
 *
 * @author Wanwan
 */
@Service
@Slf4j
@Validated
public class CrmCustomerServiceImpl implements CrmCustomerService {

    @Resource
    private CrmCustomerMapper customerMapper;

    @Resource
    private CrmPermissionService permissionService;
    @Resource
    private CrmCustomerLimitConfigService customerLimitConfigService;
    @Resource
    @Lazy
    private CrmCustomerPoolConfigService customerPoolConfigService;
    @Resource
    @Lazy
    private CrmContactService contactService;
    @Resource
    @Lazy
    private CrmBusinessService businessService;
    @Resource
    @Lazy
    private CrmContractService contractService;
    @Resource
    private CrmCustomerSourceService customerSourceService;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private CrmAutoAssignRuleService autoAssignRuleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_CREATE_SUB_TYPE, bizNo = "{{#customer.id}}",
            success = CRM_CUSTOMER_CREATE_SUCCESS)
    public Long createCustomer(CrmCustomerSaveReqVO createReqVO, Long userId) {
        createReqVO.setId(null);
        // 1.1 校验客户来源
        customerSourceService.validateCustomerSourceExists(createReqVO.getSource(), null);
        // 1.2 校验客户名称唯一
        validateCustomerNameUnique(createReqVO.getName(), null);
        // 1.3 校验手机号唯一
        validateCustomerMobileUnique(createReqVO.getMobile(), null);
        // 1.3 初始化客户负责人
        initCustomerOwner(createReqVO);

        // 2. 插入客户
        CrmCustomerDO customer = initCustomer(createReqVO, createReqVO.getOwnerUserId());
        customerMapper.insert(customer);

        // 3. 创建数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .setBizId(customer.getId()).setUserId(customer.getOwnerUserId()).setLevel(CrmPermissionLevelEnum.OWNER.getLevel())); // 设置客户负责人为权限负责人

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("customer", customer);
        return customer.getId();
    }

    /**
     * 初始化客户负责人（管理后台 - CRM 客户新增/修改 Request VO）
     *
     * @param createReqVO 创建信息
     */
    private void initCustomerOwner(CrmCustomerSaveReqVO createReqVO) {
        createReqVO.setOwnerUserId(resolveOwnerUserId(createReqVO.getOwnerUserId(), createReqVO.getSource(),
                createReqVO.getIndustryId(), createReqVO.getLevel(),
                CrmAutoAssignRuleTriggerActionsEnum.TRIGGER_ACTIONS_1, null));
    }

    /**
     * 初始化客户负责人（内部 - 客户创建 Create Req BO）
     *
     * @param createReqBO 创建信息
     */
    private void initCustomerOwner(CrmCustomerCreateReqBO createReqBO) {
        createReqBO.setOwnerUserId(resolveOwnerUserId(createReqBO.getOwnerUserId(), createReqBO.getSource(),
                createReqBO.getIndustryId(), createReqBO.getLevel(),
                CrmAutoAssignRuleTriggerActionsEnum.TRIGGER_ACTIONS_1, null));
    }

    /**
     * 解析客户负责人
     *
     * <p>1. 前端指定了负责人：直接校验其拥有的客户数是否达到上限，达到上限则直接报错，不做自动分配；
     * <p>2. 前端未指定负责人：走自动分配规则获取负责人，并校验其拥有的客户数是否达到上限，
     *    达到上限则跳过该负责人，继续获取下一个负责人，直到找到可用的负责人为止；
     * <p>3. 若所有可分配的负责人均已达到上限（或没有可分配的负责人），则报错「没有可分配的负责人」。
     *
     * @param ownerUserId  前端指定的负责人编号，为空时走自动分配
     * @param source       客户来源编号
     * @param industryId   所属行业编号
     * @param level        客户等级
     * @param triggerAction 触发动作（新增 / 导入 / 转移），决定自动分配规则是否命中
     * @param excludeOwnerUserId 需要排除的负责人编号，为空表示不排除
     * @return 负责人编号
     */
    private Long resolveOwnerUserId(Long ownerUserId, Long source, Long industryId, Long level,
                                    CrmAutoAssignRuleTriggerActionsEnum triggerAction,
                                    Long excludeOwnerUserId) {
        // 1. 前端指定了负责人，直接校验上限
        if (ObjUtil.isNotEmpty(ownerUserId)) {
            validateCustomerExceedOwnerLimit(ownerUserId, 1);
            return ownerUserId;
        }
        // 2. 前端未指定负责人，走自动分配
        // 2.1 拿到可分配的负责人总数，作为循环的最大次数
        Long ownerCount = autoAssignRuleService.getAssignableOwnerUserTotal();
        if (ownerCount == null || ownerCount <= 0) {
            throw exception(CUSTOMER_NO_ASSIGNABLE_OWNER);
        }
        CrmCustomerAssignConditionDto condition = new CrmCustomerAssignConditionDto(source, industryId, level);
        String triggerActionCode = triggerAction.getStatus().toString();
        Long ruleId = autoAssignRuleService.getAutoAssignRuleId();
        // 2.2 循环获取负责人：每次获取到的负责人都校验上限，达到上限则跳过该负责人，继续获取下一个
        for (int i = 0; i < ownerCount; i++) {
            Long nextOwnerUserId = autoAssignRuleService.getNextOwnerUserId(triggerActionCode, condition);
            // 没有可用的负责人（规则未启用 / 不满足触发条件 / 候选人员已耗尽）
            if (nextOwnerUserId == null) {
                break;
            }
            // 需要排除的负责人（如转移场景下的当前负责人），跳过他并推进规则
            if (excludeOwnerUserId != null && Objects.equals(excludeOwnerUserId, nextOwnerUserId)) {
                log.info("[resolveOwnerUserId][负责人({})为需要排除的负责人，自动分配下一个负责人]", nextOwnerUserId);
                autoAssignRuleService.skipCurrentAssignee(ruleId, nextOwnerUserId);
                continue;
            }
            // 该负责人拥有的客户数未达到上限，直接使用
            if (!isCustomerExceedOwnerLimit(nextOwnerUserId, 1)) {
                return nextOwnerUserId;
            }
            // 达到上限，则跳过该负责人，把规则推进到下一个负责人，使下一次获取负责人时取到下一个员工
            log.info("[resolveOwnerUserId][负责人({})拥有的客户数已达到上限，自动分配下一个负责人]", nextOwnerUserId);
            autoAssignRuleService.skipCurrentAssignee(ruleId, nextOwnerUserId);
        }
        // 3. 所有可分配的负责人都已达到上限
        throw exception(CUSTOMER_NO_ASSIGNABLE_OWNER);
    }

    /**
     * 初始化客户的通用字段
     *
     * @param customer    客户信息
     * @param ownerUserId 负责人编号
     * @return 客户信息 DO
     */
    private static CrmCustomerDO initCustomer(Object customer, Long ownerUserId) {
        return BeanUtils.toBean(customer, CrmCustomerDO.class).setOwnerUserId(ownerUserId)
                .setOwnerTime(LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_UPDATE_SUB_TYPE, bizNo = "{{#updateReqVO.id}}",
            success = CRM_CUSTOMER_UPDATE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#updateReqVO.id", level = CrmPermissionLevelEnum.WRITE)
    public void updateCustomer(CrmCustomerSaveReqVO updateReqVO) {
        Assert.notNull(updateReqVO.getId(), "客户编号不能为空");
        // 1. 校验存在
        CrmCustomerDO oldCustomer = validateCustomerExists(updateReqVO.getId());
        // 1.1 校验客户来源
        customerSourceService.validateCustomerSourceExists(updateReqVO.getSource(), oldCustomer.getSource());
        // 1.2 校验客户名称唯一（排除自身）
        validateCustomerNameUnique(updateReqVO.getName(), updateReqVO.getId());
        // 1.3 校验手机号唯一（排除自身）
        validateCustomerMobileUnique(updateReqVO.getMobile(), updateReqVO.getId());

        // 2. 更新客户
        CrmCustomerDO updateObj = BeanUtils.toBean(updateReqVO, CrmCustomerDO.class);
        customerMapper.updateById(updateObj);

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, BeanUtils.toBean(oldCustomer, CrmCustomerSaveReqVO.class));
        LogRecordContext.putVariable("customerName", oldCustomer.getName());
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_UPDATE_DEAL_STATUS_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CUSTOMER_UPDATE_DEAL_STATUS_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.WRITE)
    public void updateCustomerDealStatus(Long id, Boolean dealStatus) {
        // 1.1 校验存在
        CrmCustomerDO customer = validateCustomerExists(id);
        // 1.2 校验是否重复操作
        if (Objects.equals(customer.getDealStatus(), dealStatus)) {
            throw exception(CUSTOMER_UPDATE_DEAL_STATUS_FAIL);
        }

        // 2. 更新客户的成交状态
        customerMapper.updateById(new CrmCustomerDO().setId(id).setDealStatus(dealStatus));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("customerName", customer.getName());
        LogRecordContext.putVariable("dealStatus", dealStatus);
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_FOLLOW_UP_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CUSTOMER_FOLLOW_UP_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.WRITE)
    public void updateCustomerFollowUp(Long id, LocalDateTime contactNextTime, String contactLastContent) {
        // 1.1 校验存在
        CrmCustomerDO customer = validateCustomerExists(id);

        // 2. 更新客户的跟进信息
        customerMapper.updateById(new CrmCustomerDO().setId(id).setFollowUpStatus(true).setContactNextTime(contactNextTime)
                .setContactLastTime(LocalDateTime.now()).setContactLastContent(contactLastContent));

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("customerName", customer.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_DELETE_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CUSTOMER_DELETE_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void deleteCustomer(Long id) {
        // 1.1 校验存在
        CrmCustomerDO customer = validateCustomerExists(id);
        // 1.2 检查引用
        validateCustomerReference(id);

        // 2. 删除客户
        customerMapper.deleteById(id);
        // 3. 删除数据权限
        permissionService.deletePermission(CrmBizTypeEnum.CRM_CUSTOMER.getType(), id);

        // 4. 记录操作日志上下文
        LogRecordContext.putVariable("customerName", customer.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_TRANSFER_SUB_TYPE, bizNo = "{{#reqVO.id}}",
            success = CRM_CUSTOMER_TRANSFER_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#reqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void transferCustomer(CrmCustomerTransferReqVO reqVO, Long userId) {
        // 1.1 校验客户是否存在
        CrmCustomerDO customer = validateCustomerExists(reqVO.getId());
        // 1.2 解析新负责人：
        //      指定了负责人：直接校验其拥有的客户数是否达到上限，达到上限则报错，不做自动分配；
        //      未指定负责人：走自动分配规则获取负责人（触发动作 = 转移），并校验其上限，
        //      达到上限则跳过该负责人，继续获取下一个负责人，直到找到可用的负责人为止
        Long newOwnerUserId = resolveOwnerUserId(reqVO.getNewOwnerUserId(), customer.getSource(),
                customer.getIndustryId(), customer.getLevel(),
                CrmAutoAssignRuleTriggerActionsEnum.TRIGGER_ACTIONS_2, customer.getOwnerUserId());
        // 回填新负责人，供后续的权限转移与关联业务转移使用
        reqVO.setNewOwnerUserId(newOwnerUserId);
        // 2.1 数据权限转移
        permissionService.transferPermission(new CrmPermissionTransferReqBO(userId, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                reqVO.getId(), reqVO.getNewOwnerUserId(), reqVO.getOldOwnerPermissionLevel()));
        // 2.2 转移后重新设置负责人
        customerMapper.updateById(new CrmCustomerDO().setId(reqVO.getId())
                .setOwnerUserId(reqVO.getNewOwnerUserId()).setOwnerTime(LocalDateTime.now()));

        // 2.3 同时转移
        if (CollUtil.isNotEmpty(reqVO.getToBizTypes())) {
            transfer(reqVO, userId);
        }

        // 3. 记录转移日志
        LogRecordContext.putVariable("customer", customer);
    }

    /**
     * 转移客户时，需要额外有【联系人】【商机】【合同】
     *
     * @param reqVO  请求
     * @param userId 用户编号
     */
    private void transfer(CrmCustomerTransferReqVO reqVO, Long userId) {
        if (reqVO.getToBizTypes().contains(CrmBizTypeEnum.CRM_CONTACT.getType())) {
            List<CrmContactDO> contactList = contactService.getContactListByCustomerIdOwnerUserId(reqVO.getId(), userId);
            contactList.forEach(item -> contactService.transferContact(new CrmContactTransferReqVO(item.getId(), reqVO.getNewOwnerUserId(),
                    reqVO.getOldOwnerPermissionLevel()), userId));
        }
        if (reqVO.getToBizTypes().contains(CrmBizTypeEnum.CRM_BUSINESS.getType())) {
            List<CrmBusinessDO> businessList = businessService.getBusinessListByCustomerIdOwnerUserId(reqVO.getId(), userId);
            businessList.forEach(item -> businessService.transferBusiness(new CrmBusinessTransferReqVO(item.getId(), reqVO.getNewOwnerUserId(),
                    reqVO.getOldOwnerPermissionLevel()), userId));
        }
        if (reqVO.getToBizTypes().contains(CrmBizTypeEnum.CRM_CONTRACT.getType())) {
            List<CrmContractDO> contractList = contractService.getContractListByCustomerIdOwnerUserId(reqVO.getId(), userId);
            contractList.forEach(item -> contractService.transferContract(new CrmContractTransferReqVO(item.getId(), reqVO.getNewOwnerUserId(),
                    reqVO.getOldOwnerPermissionLevel()), userId));
        }
    }

    @Override
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_LOCK_SUB_TYPE, bizNo = "{{#lockReqVO.id}}",
            success = CRM_CUSTOMER_LOCK_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#lockReqVO.id", level = CrmPermissionLevelEnum.OWNER)
    public void lockCustomer(CrmCustomerLockReqVO lockReqVO, Long userId) {
        // 1.1 校验当前客户是否存在
        CrmCustomerDO customer = validateCustomerExists(lockReqVO.getId());
        // 1.2 校验当前是否重复操作锁定/解锁状态
        if (customer.getLockStatus().equals(lockReqVO.getLockStatus())) {
            throw exception(customer.getLockStatus() ? CUSTOMER_LOCK_FAIL_IS_LOCK : CUSTOMER_UNLOCK_FAIL_IS_UNLOCK);
        }
        // 1.3 校验锁定上限
        if (lockReqVO.getLockStatus()) {
            validateCustomerExceedLockLimit(userId);
        }

        // 2. 更新锁定状态
        customerMapper.updateById(BeanUtils.toBean(lockReqVO, CrmCustomerDO.class));

        // 3. 记录操作日志上下文
        // tips: 因为这里使用的是老的状态所以记录时反着记录，也就是 lockStatus 为 true 那么就是解锁反之为锁定
        LogRecordContext.putVariable("customer", customer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_CREATE_SUB_TYPE, bizNo = "{{#customer.id}}",
            success = CRM_CUSTOMER_CREATE_SUCCESS)
    public Long createCustomer(CrmCustomerCreateReqBO createReqBO, Long userId) {
        // 1.1 校验客户名称唯一
        validateCustomerNameUnique(createReqBO.getName(), null);
        // 1.2 校验手机号唯一
        validateCustomerMobileUnique(createReqBO.getMobile(), null);
        // 1.3 初始化客户负责人
        initCustomerOwner(createReqBO);
        // 2. 插入客户
        CrmCustomerDO customer = initCustomer(createReqBO, createReqBO.getOwnerUserId());
        customerMapper.insert(customer);

        // 2. 创建数据权限
        permissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                .setBizId(customer.getId()).setUserId(customer.getOwnerUserId()).setLevel(CrmPermissionLevelEnum.OWNER.getLevel())); // 设置客户负责人为权限负责人

        // 3. 记录操作日志上下文
        LogRecordContext.putVariable("customer", customer);
        return customer.getId();
    }

    @Override
    public CrmCustomerImportRespVO importCustomerList(List<CrmCustomerImportExcelVO> importCustomers,
                                                      CrmCustomerImportReqVO importReqVO) {
        // 校验非空
        importCustomers = filterList(importCustomers, item -> Objects.nonNull(item.getName()));
        if (CollUtil.isEmpty(importCustomers)) {
            throw exception(CUSTOMER_IMPORT_LIST_IS_EMPTY);
        }

        // 逐条处理
        CrmCustomerImportRespVO respVO = CrmCustomerImportRespVO.builder().createCustomerNames(new ArrayList<>())
                .updateCustomerNames(new ArrayList<>()).failureCustomerNames(new LinkedHashMap<>()).build();
        importCustomers.forEach(importCustomer -> {
            // 校验，判断是否有不符合的原因
            try {
                validateCustomerForCreate(importCustomer);
            } catch (ServiceException ex) {
                respVO.getFailureCustomerNames().put(importCustomer.getName(), ex.getMessage());
                return;
            }
            // 情况一：判断如果不存在，在进行插入
            CrmCustomerDO existCustomer = customerMapper.selectByCustomerName(importCustomer.getName());
            if (existCustomer == null) {
                // 1.0 校验手机号唯一
                try {
                    validateCustomerMobileUnique(importCustomer.getMobile(), null);
                } catch (ServiceException ex) {
                    respVO.getFailureCustomerNames().put(importCustomer.getName(), ex.getMessage());
                    return;
                }
                // 1.1 解析负责人：前端指定了负责人则用指定的（并校验上限），
                //      未指定则走自动分配规则（触发动作 = 导入），自动分配失败则该行导入失败
                Long ownerUserId;
                try {
                    ownerUserId = resolveOwnerUserId(importReqVO.getOwnerUserId(), importCustomer.getSource(),
                            importCustomer.getIndustryId(), importCustomer.getLevel(),
                            CrmAutoAssignRuleTriggerActionsEnum.TRIGGER_ACTIONS_3, null);
                } catch (ServiceException ex) {
                    respVO.getFailureCustomerNames().put(importCustomer.getName(), ex.getMessage());
                    return;
                }
                // 1.2 插入客户信息
                CrmCustomerDO customer = initCustomer(importCustomer, ownerUserId);
                customerMapper.insert(customer);
                respVO.getCreateCustomerNames().add(importCustomer.getName());
                // 1.3 创建数据权限
                if (ownerUserId != null) {
                    permissionService.createPermission(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                            .setBizId(customer.getId()).setUserId(ownerUserId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
                }
                // 1.4 记录操作日志
                getSelf().importCustomerLog(customer, false);
                return;
            }

            // 情况二：如果存在，判断是否允许更新
            if (!importReqVO.getUpdateSupport()) {
                respVO.getFailureCustomerNames().put(importCustomer.getName(),
                        StrUtil.format(CUSTOMER_NAME_EXISTS.getMsg(), importCustomer.getName()));
                return;
            }
            // 2.0 校验手机号唯一（排除自身）
            try {
                validateCustomerMobileUnique(importCustomer.getMobile(), existCustomer.getId());
            } catch (ServiceException ex) {
                respVO.getFailureCustomerNames().put(importCustomer.getName(), ex.getMessage());
                return;
            }
            // 2.1 更新客户信息
            CrmCustomerDO updateCustomer = BeanUtils.toBean(importCustomer, CrmCustomerDO.class)
                    .setId(existCustomer.getId());
            customerMapper.updateById(updateCustomer);
            respVO.getUpdateCustomerNames().add(importCustomer.getName());
            // 2.2 记录操作日志
            getSelf().importCustomerLog(updateCustomer, true);
        });
        return respVO;
    }

    /**
     * 记录导入客户时的操作日志
     *
     * @param customer 客户信息
     * @param isUpdate 是否更新；true - 更新，false - 新增
     */
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_IMPORT_SUB_TYPE, bizNo = "{{#customer.id}}",
            success = CRM_CUSTOMER_IMPORT_SUCCESS)
    public void importCustomerLog(CrmCustomerDO customer, boolean isUpdate) {
        LogRecordContext.putVariable("customer", customer);
        LogRecordContext.putVariable("isUpdate", isUpdate);
    }

    // ==================== 公海相关操作 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_POOL_SUB_TYPE, bizNo = "{{#id}}",
            success = CRM_CUSTOMER_POOL_SUCCESS)
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.OWNER)
    public void putCustomerPool(Long id) {
        // 1. 校验存在
        CrmCustomerDO customer = customerMapper.selectById(id);
        if (customer == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 1.2. 校验是否为公海数据
        validateCustomerOwnerExists(customer, true);
        // 1.3. 校验客户是否锁定
        validateCustomerIsLocked(customer, true);

        // 2. 客户放入公海
        putCustomerPool(customer);

        // 记录操作日志上下文
        LogRecordContext.putVariable("customerName", customer.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveCustomer(List<Long> ids, Long ownerUserId, Boolean isReceive) {
        // 1.1 校验存在
        List<CrmCustomerDO> customers = customerMapper.selectByIds(ids);
        if (customers.size() != ids.size()) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 1.2 校验负责人是否存在
        adminUserApi.validateUserList(singletonList(ownerUserId));
        // 1.3 校验状态
        customers.forEach(customer -> {
            // 校验是否已有负责人
            validateCustomerOwnerExists(customer, false);
            // 校验是否锁定
            validateCustomerIsLocked(customer, false);
            // 校验成交状态
            validateCustomerDeal(customer);
        });
        // 1.4  校验负责人是否到达上限
        validateCustomerExceedOwnerLimit(ownerUserId, customers.size());

        // 2. 领取公海数据
        List<CrmCustomerDO> updateCustomers = new ArrayList<>();
        List<CrmPermissionCreateReqBO> createPermissions = new ArrayList<>();
        customers.forEach(customer -> {
            // 2.1. 设置负责人
            updateCustomers.add(new CrmCustomerDO().setId(customer.getId())
                    .setOwnerUserId(ownerUserId).setOwnerTime(LocalDateTime.now()));
            // 2.2. 创建负责人数据权限
            createPermissions.add(new CrmPermissionCreateReqBO().setBizType(CrmBizTypeEnum.CRM_CUSTOMER.getType())
                    .setBizId(customer.getId()).setUserId(ownerUserId).setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        });
        // 2.2 更新客户负责人
        customerMapper.updateBatch(updateCustomers);
        // 2.3 创建负责人数据权限
        permissionService.createPermissionBatch(createPermissions);
        // TODO @YY：要不要处理关联的联系人？？？

        // 3. 记录操作日志
        AdminUserRespDTO user = null;
        if (!isReceive) {
            user = adminUserApi.getUser(ownerUserId);
        }
        for (CrmCustomerDO customer : customers) {
            getSelf().receiveCustomerLog(customer, user == null ? null : user.getNickname());
        }
    }

    @Override
    public int autoPutCustomerPool() {
        CrmCustomerPoolConfigDO poolConfig = customerPoolConfigService.getCustomerPoolConfig();
        if (poolConfig == null || !poolConfig.getEnabled()) {
            return 0;
        }
        // 1. 获得需要放到的客户列表
        List<CrmCustomerDO> customerList = customerMapper.selectListByAutoPool(poolConfig);
        // 2. 逐个放入公海
        int count = 0;
        for (CrmCustomerDO customer : customerList) {
            try {
                getSelf().putCustomerPool(customer);
                count++;
            } catch (Throwable e) {
                log.error("[autoPutCustomerPool][客户({}) 放入公海异常]", customer.getId(), e);
            }
        }
        return count;
    }

    @Transactional(rollbackFor = Exception.class) // 需要 protected 修饰，因为需要在事务中调用
    protected void putCustomerPool(CrmCustomerDO customer) {
        // 1. 设置负责人为 NULL
        int updateOwnerUserIncr = customerMapper.updateOwnerUserIdById(customer.getId(), null);
        if (updateOwnerUserIncr == 0) {
            throw exception(CUSTOMER_UPDATE_OWNER_USER_FAIL);
        }

        // 2. 联系人的负责人，也要设置为 null。因为：因为领取后，负责人也要关联过来，这块和 receiveCustomer 是对应的
        contactService.updateOwnerUserIdByCustomerId(customer.getId(), null);

        // 3. 删除负责人数据权限
        // 注意：需要放在 contactService 后面，不然【客户】数据权限已经被删除，无法操作！
        permissionService.deletePermission(CrmBizTypeEnum.CRM_CUSTOMER.getType(), customer.getId(),
                CrmPermissionLevelEnum.OWNER.getLevel());
    }

    @LogRecord(type = CRM_CUSTOMER_TYPE, subType = CRM_CUSTOMER_RECEIVE_SUB_TYPE, bizNo = "{{#customer.id}}",
            success = CRM_CUSTOMER_RECEIVE_SUCCESS)
    public void receiveCustomerLog(CrmCustomerDO customer, String ownerUserName) {
        // 记录操作日志上下文
        LogRecordContext.putVariable("customer", customer);
        LogRecordContext.putVariable("ownerUserName", ownerUserName);
    }

    //======================= 查询相关 =======================

    @Override
    @CrmPermission(bizType = CrmBizTypeEnum.CRM_CUSTOMER, bizId = "#id", level = CrmPermissionLevelEnum.READ)
    public CrmCustomerDO getCustomer(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    public List<CrmCustomerDO> getCustomerList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return customerMapper.selectByIds(ids);
    }

    @Override
    public PageResult<CrmCustomerDO> getCustomerPage(CrmCustomerPageReqVO pageReqVO, Long userId) {
        return customerMapper.selectPage(pageReqVO, userId);
    }

    @Override
    public PageResult<CrmCustomerDO> getPutPoolRemindCustomerPage(CrmCustomerPageReqVO pageVO, Long userId) {
        CrmCustomerPoolConfigDO poolConfig = customerPoolConfigService.getCustomerPoolConfig();
        if (ObjUtil.isNull(poolConfig)
                || Boolean.FALSE.equals(poolConfig.getEnabled())
                || Boolean.FALSE.equals(poolConfig.getNotifyEnabled())) {
            return PageResult.empty();
        }
        return customerMapper.selectPutPoolRemindCustomerPage(pageVO, poolConfig, userId);
    }

    @Override
    public Long getPutPoolRemindCustomerCount(Long userId) {
        CrmCustomerPoolConfigDO poolConfig = customerPoolConfigService.getCustomerPoolConfig();
        if (ObjUtil.isNull(poolConfig)
                || Boolean.FALSE.equals(poolConfig.getEnabled())
                || Boolean.FALSE.equals(poolConfig.getNotifyEnabled())) {
            return 0L;
        }
        CrmCustomerPageReqVO pageVO = new CrmCustomerPageReqVO()
                .setPool(null)
                .setContactStatus(CrmCustomerPageReqVO.CONTACT_TODAY)
                .setSceneType(CrmSceneTypeEnum.OWNER.getType());
        return customerMapper.selectPutPoolRemindCustomerCount(pageVO, poolConfig, userId);
    }

    @Override
    public Long getTodayContactCustomerCount(Long userId) {
        return customerMapper.selectCountByTodayContact(userId);
    }

    @Override
    public Long getFollowCustomerCount(Long userId) {
        return customerMapper.selectCountByFollow(userId);
    }

    // ======================= 校验相关 =======================

    /**
     * 校验客户名称是否唯一
     *
     * @param name      客户名称
     * @param excludeId 需要排除的客户编号（修改场景，排除自身），为空时不排除
     */
    private void validateCustomerNameUnique(String name, Long excludeId) {
        if (StrUtil.isEmptyIfStr(name)) {
            return;
        }
        if (customerMapper.selectByCustomerName(name, excludeId) != null) {
            throw exception(CUSTOMER_NAME_EXISTS, name);
        }
    }

    /**
     * 校验客户手机号是否唯一
     *
     * @param mobile    手机号
     * @param excludeId 需要排除的客户编号（修改场景，排除自身），为空时不排除
     */
    private void validateCustomerMobileUnique(String mobile, Long excludeId) {
        if (StrUtil.isEmptyIfStr(mobile)) {
            return;
        }
        if (customerMapper.selectByMobile(mobile, excludeId) != null) {
            throw exception(CUSTOMER_MOBILE_EXISTS, mobile);
        }
    }

    private void validateCustomerForCreate(CrmCustomerImportExcelVO importCustomer) {
        // 校验客户名称不能为空
        if (StrUtil.isEmptyIfStr(importCustomer.getName())) {
            throw exception(CUSTOMER_CREATE_NAME_NOT_NULL);
        }
        // 校验客户来源编号是否存在（导入时直接传入来源编号）
        customerSourceService.validateCustomerSourceExists(importCustomer.getSource(), null);
    }

    /**
     * 校验客户是否被引用
     *
     * @param id 客户编号
     */
    private void validateCustomerReference(Long id) {
        if (contactService.getContactCountByCustomerId(id) > 0) {
            throw exception(CUSTOMER_DELETE_FAIL_HAVE_REFERENCE, CrmBizTypeEnum.CRM_CONTACT.getName());
        }
        if (businessService.getBusinessCountByCustomerId(id) > 0) {
            throw exception(CUSTOMER_DELETE_FAIL_HAVE_REFERENCE, CrmBizTypeEnum.CRM_BUSINESS.getName());
        }
        if (contractService.getContractCountByCustomerId(id) > 0) {
            throw exception(CUSTOMER_DELETE_FAIL_HAVE_REFERENCE, CrmBizTypeEnum.CRM_CONTRACT.getName());
        }
    }

    /**
     * 校验客户是否存在
     *
     * @param id 客户 id
     */
    @Override
    public void validateCustomer(Long id) {
        validateCustomerExists(id);
    }

    private void validateCustomerOwnerExists(CrmCustomerDO customer, Boolean pool) {
        if (customer == null) { // 防御一下
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        // 校验是否为公海数据
        if (pool && customer.getOwnerUserId() == null) {
            throw exception(CUSTOMER_IN_POOL, customer.getName());
        }
        // 负责人已存在
        if (!pool && customer.getOwnerUserId() != null) {
            throw exception(CUSTOMER_OWNER_EXISTS, customer.getName());
        }
    }

    private CrmCustomerDO validateCustomerExists(Long id) {
        CrmCustomerDO customerDO = customerMapper.selectById(id);
        if (customerDO == null) {
            throw exception(CUSTOMER_NOT_EXISTS);
        }
        return customerDO;
    }

    private void validateCustomerIsLocked(CrmCustomerDO customer, Boolean pool) {
        if (customer.getLockStatus()) {
            throw exception(pool ? CUSTOMER_LOCKED_PUT_POOL_FAIL : CUSTOMER_LOCKED, customer.getName());
        }
    }

    private void validateCustomerDeal(CrmCustomerDO customer) {
        if (customer.getDealStatus()) {
            throw exception(CUSTOMER_ALREADY_DEAL);
        }
    }

    /**
     * 校验用户拥有的客户数量，是否到达上限
     *
     * <p>达到上限时抛出异常，异常信息区分「用户维度」与「部门维度」。
     *
     * @param userId   用户编号
     * @param newCount 附加数量
     */
    private void validateCustomerExceedOwnerLimit(Long userId, int newCount) {
        if (isCustomerExceedOwnerLimit(userId, newCount)) {
            CrmCustomerLimitConfigSimpVO simpVO = getOwnerLimitConfig(userId);
            //判断是部门还是用户
            if (Objects.equals(simpVO.getApplicableType(),
                    CrmCustomerLimitConfigApplicableTypeEnum.CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE_2.getValue())) {
                throw exception(CUSTOMER_OWNER_EXCEED_LIMIT_USER, simpVO.getUserName(), simpVO.getUserName(), simpVO.getMaxCount(), simpVO.getName(), simpVO.getName(), simpVO.getId());
            } else {
                throw exception(CUSTOMER_OWNER_EXCEED_LIMIT_DEPT, simpVO.getUserName(), simpVO.getDeptName(), simpVO.getMaxCount(), simpVO.getName(), simpVO.getId());
            }
        }
    }

    /**
     * 判断用户拥有的客户数量，是否到达上限（不抛异常，用于自动分配时的逐个试探）
     *
     * @param userId   用户编号
     * @param newCount 附加数量
     * @return 是否达到上限
     */
    private boolean isCustomerExceedOwnerLimit(Long userId, int newCount) {
        CrmCustomerLimitConfigSimpVO simpVO = getOwnerLimitConfig(userId);
        if (simpVO == null) {
            return false;
        }
        Long ownerCount = customerMapper.selectCountByDealStatusAndOwnerUserId(null, userId);
        Long dealOwnerCount = customerMapper.selectCountByDealStatusAndOwnerUserId(true, userId);
        long nowCount = simpVO.getDealCountEnabled() != null && simpVO.getDealCountEnabled() ? ownerCount : ownerCount - dealOwnerCount;
        return nowCount + newCount > simpVO.getMaxCount();
    }

    /**
     * 获得用户的「客户拥有数量上限」配置
     *
     * @param userId 用户编号
     * @return 上限配置；未配置或未配置最大数量时，返回 null
     */
    private CrmCustomerLimitConfigSimpVO getOwnerLimitConfig(Long userId) {
        CrmCustomerLimitConfigSimpVO simpVO = customerLimitConfigService.getCustomerLimitConfigListByUserId(
                CUSTOMER_OWNER_LIMIT.getType(), userId);
        if (ObjectUtils.isNull(simpVO) || simpVO.getMaxCount() == null) {
            return null;
        }
        return simpVO;
    }

    /**
     * 校验用户锁定的客户数量，是否到达上限
     *
     * @param userId 用户编号
     */
    private void validateCustomerExceedLockLimit(Long userId) {
        CrmCustomerLimitConfigSimpVO simpVO = customerLimitConfigService.getCustomerLimitConfigListByUserId(
                CUSTOMER_LOCK_LIMIT.getType(), userId);
        if (ObjUtil.isNull(simpVO)) {
            return;
        }
        Long lockCount = customerMapper.selectCountByLockStatusAndOwnerUserId(true, userId);
        Integer maxCount = simpVO.getMaxCount();
        assert maxCount != null;
        if (lockCount >= maxCount) {
            // 判断是部门还是用户
            if (Objects.equals(simpVO.getApplicableType(),
                    CrmCustomerLimitConfigApplicableTypeEnum.CUSTOMER_LIMIT_CONFIG_APPLICABLE_TYPE_2.getValue())) {
                throw exception(CUSTOMER_LOCK_EXCEED_LIMIT_USER, simpVO.getUserName(), simpVO.getUserName(), simpVO.getName(), simpVO.getName(), simpVO.getId());
            } else {
                throw exception(CUSTOMER_LOCK_EXCEED_LIMIT_DEPT, simpVO.getDeptName(), simpVO.getDeptName(), simpVO.getName(), simpVO.getId());
            }
        }
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private CrmCustomerServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
