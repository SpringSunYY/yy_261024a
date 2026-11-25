package com.lz.module.crm.dal.mysql.followup;

import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.mybatis.core.mapper.BaseMapperX;
import com.lz.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lz.module.crm.controller.admin.followup.vo.CrmFollowUpRecordPageReqVO;
import com.lz.module.crm.dal.dataobject.followup.CrmFollowUpRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 跟进记录 Mapper
 *
 * @author 荔枝源码
 */
@Mapper
public interface CrmFollowUpRecordMapper extends BaseMapperX<CrmFollowUpRecordDO> {

    default PageResult<CrmFollowUpRecordDO> selectPage(CrmFollowUpRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmFollowUpRecordDO>()
                .eqIfPresent(CrmFollowUpRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(CrmFollowUpRecordDO::getBizId, reqVO.getBizId())
                .betweenIfPresent(CrmFollowUpRecordDO::getContactTime, reqVO.getContactTime())
                // 按联系时间倒序，而不是按创建时间：跟进记录可能是事后补录的
                .orderByDesc(CrmFollowUpRecordDO::getContactTime)
                .orderByDesc(CrmFollowUpRecordDO::getId));
    }

    default void deleteByBiz(Integer bizType, Long bizId) {
        delete(new LambdaQueryWrapperX<CrmFollowUpRecordDO>()
                .eq(CrmFollowUpRecordDO::getBizType, bizType)
                .eq(CrmFollowUpRecordDO::getBizId, bizId));
    }

    default List<CrmFollowUpRecordDO> selectListByBiz(Integer bizType, Collection<Long> bizIds) {
        return selectList(new LambdaQueryWrapperX<CrmFollowUpRecordDO>()
                .eq(CrmFollowUpRecordDO::getBizType, bizType)
                .in(CrmFollowUpRecordDO::getBizId, bizIds)
                // 按联系时间倒序，而不是按创建时间：跟进记录可能是事后补录的
                .orderByDesc(CrmFollowUpRecordDO::getContactTime)
                .orderByDesc(CrmFollowUpRecordDO::getId));
    }

    /**
     * 获得指定业务对象的跟进记录列表（按联系时间倒序，即最近联系的记录在前）
     *
     * <p>用于统计「连续 N 次联系不上」的次数。
     *
     * <p>注意：这里必须按「联系时间」排序，而不是按编号 / 创建时间排序。
     * <p>因为跟进记录可能是「事后补录」的 —— 之前联系过客户，但记录是后来才创建的，
     * 按创建时间排序会算错「连续失败」的次数。
     * <p>联系时间相同时，再按编号倒序，保证排序稳定。
     *
     * @param bizType 业务类型
     * @param bizId   业务编号
     * @param limit   最多返回的记录数
     * @return 跟进记录列表，最近联系的记录在前
     */
    default List<CrmFollowUpRecordDO> selectRecentListByBiz(Integer bizType, Long bizId, int limit) {
        return selectList(new LambdaQueryWrapperX<CrmFollowUpRecordDO>()
                .eq(CrmFollowUpRecordDO::getBizType, bizType)
                .eq(CrmFollowUpRecordDO::getBizId, bizId)
                .orderByDesc(CrmFollowUpRecordDO::getContactTime)
                .orderByDesc(CrmFollowUpRecordDO::getId)
                .last("LIMIT " + limit));
    }

    /**
     * 获得指定业务对象在某个时间之后的跟进记录列表（按联系时间倒序，即最近联系的记录在前）
     *
     * <p>用于统计「当前负责人任期内连续 N 次联系不上」的次数：
     * 只统计联系时间在客户「成为负责人的时间」之后的记录，
     * 避免把上一任负责人的失败记录算到新负责人头上。
     *
     * <p>注意：这里必须按「联系时间」排序，而不是按编号 / 创建时间排序。
     * <p>因为跟进记录可能是「事后补录」的 —— 之前联系过客户，但记录是后来才创建的，
     * 按创建时间排序会算错「连续失败」的次数。
     * <p>联系时间相同时，再按编号倒序，保证排序稳定。
     *
     * @param bizType     业务类型
     * @param bizId       业务编号
     * @param contactTime 联系时间的下限（含），为空时不限制
     * @param limit       最多返回的记录数
     * @return 跟进记录列表，最近联系的记录在前
     */
    default List<CrmFollowUpRecordDO> selectRecentListByBizAndContactTime(Integer bizType, Long bizId,
                                                                        LocalDateTime contactTime, int limit) {
        return selectList(new LambdaQueryWrapperX<CrmFollowUpRecordDO>()
                .eq(CrmFollowUpRecordDO::getBizType, bizType)
                .eq(CrmFollowUpRecordDO::getBizId, bizId)
                .geIfPresent(CrmFollowUpRecordDO::getContactTime, contactTime)
                .orderByDesc(CrmFollowUpRecordDO::getContactTime)
                .orderByDesc(CrmFollowUpRecordDO::getId)
                .last("LIMIT " + limit));
    }

}
