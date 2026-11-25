package com.lz.module.crm.service.followup;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.lz.framework.common.enums.CommonWhetherEnum;
import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.common.util.object.BeanUtils;
import com.lz.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import com.lz.module.crm.controller.admin.followup.vo.CrmFollowUpRecordSaveReqVO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerDO;
import com.lz.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import com.lz.module.crm.dal.mysql.followup.CrmFollowUpRecordMapper;
import com.lz.module.crm.enums.common.CrmBizTypeEnum;
import com.lz.module.crm.enums.permission.CrmPermissionLevelEnum;
import com.lz.module.crm.framework.permission.core.annotations.CrmPermission;
import com.lz.module.crm.service.business.CrmBusinessService;
import com.lz.module.crm.service.clue.CrmClueService;
import com.lz.module.crm.service.contact.CrmContactService;
import com.lz.module.crm.service.contract.CrmContractService;
import com.lz.module.crm.service.customer.CrmCustomerService;
import com.lz.module.crm.service.followup.bo.CrmFollowUpCreateReqBO;
import com.lz.module.crm.service.permission.CrmPermissionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.lz.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lz.module.crm.enums.ErrorCodeConstants.FOLLOW_UP_RECORD_DELETE_DENIED;
import static com.lz.module.crm.enums.ErrorCodeConstants.FOLLOW_UP_RECORD_NOT_EXISTS;

/**
 * 跟进记录 Service 实现类
 *
 * @author 荔枝源码
 */
@Slf4j
@Service
@Validated
public class CrmFollowUpRecordServiceImpl implements CrmFollowUpRecordService {

    /**
     * 统计「连续联系不上」次数时，最多查询的跟进记录数
     */
    private static final int MAX_CONSECUTIVE_FAIL_COUNT = 100;

    @Resource
    private CrmFollowUpRecordMapper crmFollowUpRecordMapper;

    @Resource
    @Lazy
    private CrmPermissionService permissionService;
    @Resource
    @Lazy
    private CrmBusinessService businessService;
    @Resource
    @Lazy
    private CrmClueService clueService;
    @Resource
    @Lazy
    private CrmContactService contactService;
    @Resource
    @Lazy
    private CrmContractService contractService;
    @Resource
    @Lazy
    private CrmCustomerService customerService;

    @Override
    @CrmPermission(bizTypeValue = "#createReqVO.bizType", bizId = "#createReqVO.bizId", level = CrmPermissionLevelEnum.WRITE)
    public Long createFollowUpRecord(CrmFollowUpRecordSaveReqVO createReqVO) {
        // 1. 创建更进记录
        CrmFollowUpRecordDO record = BeanUtils.toBean(createReqVO, CrmFollowUpRecordDO.class);
        crmFollowUpRecordMapper.insert(record);

        // 2. 更新 bizId 对应的记录
        if (ObjUtil.equal(CrmBizTypeEnum.CRM_CUSTOMER.getType(), record.getBizType())) { // 更新客户跟进信息
            customerService.updateCustomerFollowUp(record.getBizId(), record.getNextTime(), record.getContent());
        }
        if (ObjUtil.equal(CrmBizTypeEnum.CRM_BUSINESS.getType(), record.getBizType())) { // 更新商机跟进信息
            businessService.updateBusinessFollowUp(record.getBizId(), record.getNextTime(), record.getContent());
        }
        if (ObjUtil.equal(CrmBizTypeEnum.CRM_CLUE.getType(), record.getBizType())) { // 更新线索跟进信息
            clueService.updateClueFollowUp(record.getBizId(), record.getNextTime(), record.getContent());
        }
        if (ObjUtil.equal(CrmBizTypeEnum.CRM_CONTACT.getType(), record.getBizType())) { // 更新联系人跟进信息
            contactService.updateContactFollowUp(record.getBizId(), record.getNextTime(), record.getContent());
        }
        if (ObjUtil.equal(CrmBizTypeEnum.CRM_CONTRACT.getType(), record.getBizType())) { // 更新合同跟进信息
            contractService.updateContractFollowUp(record.getBizId(), record.getNextTime(), record.getContent());
        }

        // 3.1 更新 contactIds 对应的记录，只更新 nextTime
        if (CollUtil.isNotEmpty(createReqVO.getContactIds())) {
            contactService.updateContactContactNextTime(createReqVO.getContactIds(), createReqVO.getNextTime());
        }
        // 3.2 需要更新 businessIds 对应的记录，只更新 nextTime
        if (CollUtil.isNotEmpty(createReqVO.getBusinessIds())) {
            businessService.updateBusinessContactNextTime(createReqVO.getBusinessIds(), createReqVO.getNextTime());
        }
        // 4. 客户的跟进记录：处理「连续联系不上」的后果（放入公海 / 转交给下一个用户）
        if (ObjUtil.equal(CrmBizTypeEnum.CRM_CUSTOMER.getType(), record.getBizType())) {
            handleCustomerFollowUpFail(record);
        }
        return record.getId();
    }

    /**
     * 处理客户「连续联系不上」的后果
     *
     * <p>只有客户类型（bizType = 客户）的跟进记录才会触发。
     * <p>当前这条跟进记录联系失败时，统计该客户连续联系不上的次数，
     * 再交给客户 Service 按自动分配规则处理（放入公海 / 转交给下一个用户）。
     *
     * <p>这里统计了两个口径的连续失败次数：
     * <ul>
     *     <li><b>总的连续失败次数</b>：不看负责人，从最近联系的记录往前数，直到遇到一条联系成功的记录。
     *         用于判断「放入公海额度」，因为放入公海与负责人是谁无关；</li>
     *     <li><b>当前负责人的连续失败次数</b>：只统计联系时间在客户「成为负责人的时间」之后的记录。
     *         用于判断「下一个用户额度」，因为新负责人不应该继承上一任负责人的失败次数。</li>
     * </ul>
     *
     * <p>注意：记录是按「联系时间」倒序的，而不是按创建时间 —— 跟进记录可能是事后补录的。
     *
     * @param record 跟进记录
     */
    private void handleCustomerFollowUpFail(CrmFollowUpRecordDO record) {
        // 1. 本次联系成功，不触发处理
        if (!CommonWhetherEnum.WHETHER_1.getStatus().equals(record.getIsSuccess())) {
            return;
        }
        // 2. 统计总的连续联系不上的次数：从最近联系的记录往前数，直到遇到一条联系成功的记录
        //    最多查询 100 条，避免客户跟进记录过多时性能问题
        List<CrmFollowUpRecordDO> recordList = crmFollowUpRecordMapper.selectRecentListByBiz(
                record.getBizType(), record.getBizId(), MAX_CONSECUTIVE_FAIL_COUNT);
        int totalFailCount = countConsecutiveFail(recordList);
        if (totalFailCount == 0) {
            return;
        }
        // 3. 统计当前负责人任期内连续联系不上的次数：
        //    只算「成为负责人的时间」之后的记录，避免上一任负责人的失败记录被算到新负责人头上
        int ownerFailCount = countOwnerConsecutiveFail(record.getBizId(), record.getBizType());
        // 4. 交给客户 Service 处理
        customerService.handleCustomerFollowUpFail(record.getBizId(), totalFailCount, ownerFailCount);
    }

    /**
     * 统计列表中「从第一条开始，连续联系不上」的次数
     *
     * <p>列表需要按「联系时间」倒序（最近联系的在前）。遇到一条联系成功的记录就停止统计。
     *
     * <p>注意：这里按联系时间而不是创建时间来排序，因为跟进记录可能是事后补录的，
     * 之前联系过客户但记录是后来才创建的。
     *
     * @param recordList 跟进记录列表，最近联系的记录在前
     * @return 连续联系不上的次数
     */
    private int countConsecutiveFail(List<CrmFollowUpRecordDO> recordList) {
        int consecutiveFailCount = 0;
        for (CrmFollowUpRecordDO item : recordList) {
            // 遇到一条联系成功的记录，停止统计
            if (!CommonWhetherEnum.WHETHER_1.getStatus().equals(item.getIsSuccess())) {
                break;
            }
            consecutiveFailCount++;
        }
        return consecutiveFailCount;
    }

    /**
     * 统计当前负责人任期内「连续联系不上」的次数
     *
     * <p>只统计联系时间在客户「成为负责人的时间」之后的记录。
     * <p>注意：需要先拿到客户，才能知道「成为负责人的时间」；拿不到客户时，退化为统计总的连续失败次数。
     *
     * @param customerId 客户编号
     * @param bizType    业务类型
     * @return 当前负责人任期内连续联系不上的次数
     */
    private int countOwnerConsecutiveFail(Long customerId, Integer bizType) {
        // 1. 获得客户，拿到「成为负责人的时间」
        LocalDateTime ownerTime = null;
        try {
            CrmCustomerDO customer = customerService.getCustomer(customerId);
            ownerTime = customer != null ? customer.getOwnerTime() : null;
        } catch (Exception e) {
            log.warn("[countOwnerConsecutiveFail][获得客户({})失败({})]", customerId, e.getMessage());
        }
        if (ownerTime == null) {
            // 拿不到负责人时间时，退化为统计总的连续失败次数
            return countConsecutiveFail(crmFollowUpRecordMapper.selectRecentListByBiz(
                    bizType, customerId, MAX_CONSECUTIVE_FAIL_COUNT));
        }
        // 2. 只统计联系时间在「成为负责人的时间」之后的记录
        List<CrmFollowUpRecordDO> recordList = crmFollowUpRecordMapper.selectRecentListByBizAndContactTime(
                bizType, customerId, ownerTime, MAX_CONSECUTIVE_FAIL_COUNT);
        return countConsecutiveFail(recordList);
    }

    @Override
    public void createFollowUpRecordBatch(List<CrmFollowUpCreateReqBO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        crmFollowUpRecordMapper.insertBatch(BeanUtils.toBean(list, CrmFollowUpRecordDO.class));
    }

    @Override
    public void deleteFollowUpRecord(Long id, Long userId) {
        // 校验存在
        CrmFollowUpRecordDO followUpRecord = validateFollowUpRecordExists(id);
        // 校验权限
        if (!permissionService.hasPermission(followUpRecord.getBizType(), followUpRecord.getBizId(), userId, CrmPermissionLevelEnum.OWNER)) {
            throw exception(FOLLOW_UP_RECORD_DELETE_DENIED);
        }

        // 删除
        crmFollowUpRecordMapper.deleteById(id);
    }

    @Override
    public void deleteFollowUpRecordByBiz(Integer bizType, Long bizId) {
        crmFollowUpRecordMapper.deleteByBiz(bizType, bizId);
    }

    private CrmFollowUpRecordDO validateFollowUpRecordExists(Long id) {
        CrmFollowUpRecordDO followUpRecord = crmFollowUpRecordMapper.selectById(id);
        if (followUpRecord == null) {
            throw exception(FOLLOW_UP_RECORD_NOT_EXISTS);
        }
        return followUpRecord;
    }

    @Override
    public CrmFollowUpRecordDO getFollowUpRecord(Long id) {
        return crmFollowUpRecordMapper.selectById(id);
    }

    @Override
    @CrmPermission(bizTypeValue = "#pageReqVO.bizType", bizId = "#pageReqVO.bizId", level = CrmPermissionLevelEnum.READ)
    public PageResult<CrmFollowUpRecordDO> getFollowUpRecordPage(CrmFollowUpRecordPageReqVO pageReqVO) {
        return crmFollowUpRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmFollowUpRecordDO> getFollowUpRecordByBiz(Integer bizType, Collection<Long> bizIds) {
        return crmFollowUpRecordMapper.selectListByBiz(bizType, bizIds);
    }

}
