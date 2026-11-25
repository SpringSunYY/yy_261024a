package com.lz.module.crm.dal.mysql.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.mybatis.core.mapper.BaseMapperX;
import com.lz.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lz.framework.mybatis.core.query.MPJLambdaWrapperX;
import com.lz.module.crm.controller.admin.customer.vo.customer.CrmCustomerPageReqVO;
import com.lz.module.crm.dal.dataobject.clue.CrmClueDO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerDO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerPoolConfigDO;
import com.lz.module.crm.enums.common.CrmBizTypeEnum;
import com.lz.module.crm.enums.common.CrmSceneTypeEnum;
import com.lz.module.crm.util.CrmPermissionUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerMapper extends BaseMapperX<CrmCustomerDO> {

    default Long selectCountByLockStatusAndOwnerUserId(Boolean lockStatus, Long ownerUserId) {
        return selectCount(new LambdaUpdateWrapper<CrmCustomerDO>()
                .eq(CrmCustomerDO::getLockStatus, lockStatus)
                .eq(CrmCustomerDO::getOwnerUserId, ownerUserId));
    }

    default Long selectCountByDealStatusAndOwnerUserId(@Nullable Boolean dealStatus, Long ownerUserId) {
        return selectCount(new LambdaQueryWrapperX<CrmCustomerDO>()
                .eqIfPresent(CrmCustomerDO::getDealStatus, dealStatus)
                .eq(CrmCustomerDO::getOwnerUserId, ownerUserId));
    }

    /**
     * 更新客户的公海标记
     *
     * @param id    客户编号
     * @param pool  是否为公海数据
     * @return 影响行数
     */
    default int updatePoolById(Long id, Boolean pool) {
        return update(new LambdaUpdateWrapper<CrmCustomerDO>()
                .eq(CrmCustomerDO::getId, id)
                .set(CrmCustomerDO::getPool, pool));
    }

    default PageResult<CrmCustomerDO> selectPage(CrmCustomerPageReqVO pageReqVO, Long ownerUserId) {
        MPJLambdaWrapperX<CrmCustomerDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        if (Boolean.TRUE.equals(pageReqVO.getPool())) {
            // 公海数据：pool = true
            query.eq(CrmCustomerDO::getPool, Boolean.TRUE);
        } else {
            CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                    CrmCustomerDO::getId, ownerUserId, pageReqVO.getSceneType());
            // 非公海数据：pool = false 或 null
            query.and(w -> w.eq(CrmCustomerDO::getPool, Boolean.FALSE).or().isNull(CrmCustomerDO::getPool));
        }
        // 拼接自身的查询条件
        query.selectAll(CrmCustomerDO.class)
                .likeIfPresent(CrmCustomerDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, pageReqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getIndustryId, pageReqVO.getIndustryId())
                .eqIfPresent(CrmCustomerDO::getLevel, pageReqVO.getLevel())
                .eqIfPresent(CrmCustomerDO::getSource, pageReqVO.getSource())
                .eqIfPresent(CrmCustomerDO::getFollowUpStatus, pageReqVO.getFollowUpStatus())
                .eqIfPresent(CrmCustomerDO::getCreateTime, pageReqVO.getCreateTime());

        // backlog 查询
        if (ObjUtil.isNotNull(pageReqVO.getContactStatus())) {
            Assert.isNull(pageReqVO.getPool(), "pool 必须是 null");
            LocalDateTime beginOfToday = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
            LocalDateTime endOfToday = LocalDateTimeUtil.endOfDay(LocalDateTime.now());
            if (pageReqVO.getContactStatus().equals(CrmCustomerPageReqVO.CONTACT_TODAY)) { // 今天需联系
                query.between(CrmCustomerDO::getContactNextTime, beginOfToday, endOfToday);
            } else if (pageReqVO.getContactStatus().equals(CrmCustomerPageReqVO.CONTACT_EXPIRED)) { // 已逾期
                query.lt(CrmCustomerDO::getContactNextTime, beginOfToday);
            } else if (pageReqVO.getContactStatus().equals(CrmCustomerPageReqVO.CONTACT_ALREADY)) { // 已联系
                query.between(CrmCustomerDO::getContactLastTime, beginOfToday, endOfToday);
            } else {
                throw new IllegalArgumentException("未知联系状态：" + pageReqVO.getContactStatus());
            }
        }
        return selectJoinPage(pageReqVO, CrmCustomerDO.class, query);
    }

    default CrmCustomerDO selectByCustomerName(String name) {
        List<CrmCustomerDO> list = selectList(new LambdaQueryWrapperX<CrmCustomerDO>().eq(CrmCustomerDO::getName, name));
        return CollUtil.isEmpty(list) ? null : list.getFirst();
    }

    /**
     * 获得指定客户名称的客户
     *
     * @param name      客户名称
     * @param excludeId 需要排除的客户编号（修改场景，排除自身）
     * @return 客户
     */
    default CrmCustomerDO selectByCustomerName(String name, Long excludeId) {
        List<CrmCustomerDO> list = selectList(new LambdaQueryWrapperX<CrmCustomerDO>()
                .eq(CrmCustomerDO::getName, name)
                .neIfPresent(CrmCustomerDO::getId, excludeId));
        return CollUtil.isEmpty(list) ? null : list.getFirst();
    }

    /**
     * 获得指定手机号的客户
     *
     * @param mobile    手机号
     * @param excludeId 需要排除的客户编号（修改场景，排除自身）
     * @return 客户
     */
    default CrmCustomerDO selectByMobile(String mobile, Long excludeId) {
        List<CrmCustomerDO> list = selectList(new LambdaQueryWrapperX<CrmCustomerDO>()
                .eq(CrmCustomerDO::getMobile, mobile)
                .neIfPresent(CrmCustomerDO::getId, excludeId));
        return CollUtil.isEmpty(list) ? null : list.getFirst();
    }

    /**
     * 客户查重：按手机号 / 客户名称查询，【不受数据权限限制】，查询所有客户
     *
     * @param pageReqVO 分页查询
     * @return 客户分页
     */
    default PageResult<CrmCustomerDO> selectDuplicatePage(CrmCustomerPageReqVO pageReqVO) {
        MPJLambdaWrapperX<CrmCustomerDO> query = new MPJLambdaWrapperX<>();
        query.selectAll(CrmCustomerDO.class)
                .likeIfPresent(CrmCustomerDO::getName, pageReqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, pageReqVO.getMobile());
        return selectJoinPage(pageReqVO, CrmCustomerDO.class, query);
    }

    default PageResult<CrmCustomerDO> selectPutPoolRemindCustomerPage(CrmCustomerPageReqVO pageReqVO,
                                                                      CrmCustomerPoolConfigDO poolConfig,
                                                                      Long ownerUserId) {
        final MPJLambdaWrapperX<CrmCustomerDO> query = buildPutPoolRemindCustomerQuery(pageReqVO, poolConfig, ownerUserId);
        return selectJoinPage(pageReqVO, CrmCustomerDO.class, query.selectAll(CrmCustomerDO.class));
    }

    default Long selectPutPoolRemindCustomerCount(CrmCustomerPageReqVO pageReqVO,
                                                  CrmCustomerPoolConfigDO poolConfig,
                                                  Long userId) {
        final MPJLambdaWrapperX<CrmCustomerDO> query = buildPutPoolRemindCustomerQuery(pageReqVO, poolConfig, userId);
        return selectCount(query);
    }

    private static MPJLambdaWrapperX<CrmCustomerDO> buildPutPoolRemindCustomerQuery(CrmCustomerPageReqVO pageReqVO,
                                                                                    CrmCustomerPoolConfigDO poolConfig,
                                                                                    Long ownerUserId) {
        MPJLambdaWrapperX<CrmCustomerDO> query = new MPJLambdaWrapperX<>();
        // 拼接数据权限的查询条件
        CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                CrmCustomerDO::getId, ownerUserId, pageReqVO.getSceneType());

        // 未锁定 + 未成交
        query.eq(CrmCustomerDO::getLockStatus, false).eq(CrmCustomerDO::getDealStatus, false);

        // 情况一：未成交提醒日期区间
        Integer dealExpireDays = poolConfig.getDealExpireDays();
        LocalDateTime startDealRemindTime = LocalDateTime.now().minusDays(dealExpireDays);
        LocalDateTime endDealRemindTime = LocalDateTime.now()
                .minusDays(Math.max(dealExpireDays - poolConfig.getNotifyDays(), 0));
        // 情况二：未跟进提醒日期区间
        Integer contactExpireDays = poolConfig.getContactExpireDays();
        LocalDateTime startContactRemindTime = LocalDateTime.now().minusDays(contactExpireDays);
        LocalDateTime endContactRemindTime = LocalDateTime.now()
                .minusDays(Math.max(contactExpireDays - poolConfig.getNotifyDays(), 0));
        query.and(q -> {
            // 情况一：成交超时提醒
            q.between(CrmCustomerDO::getOwnerTime, startDealRemindTime, endDealRemindTime)
                    // 情况二：跟进超时提醒
                    .or(w -> w.between(CrmCustomerDO::getOwnerTime, startContactRemindTime, endContactRemindTime)
                            .and(p -> p.between(CrmCustomerDO::getContactLastTime, startContactRemindTime, endContactRemindTime)
                                    .or().isNull(CrmCustomerDO::getContactLastTime)));
        });
        return query;
    }

    /**
     * 获得需要过期到公海的客户列表
     *
     * @return 客户列表
     */
    default List<CrmCustomerDO> selectListByAutoPool(CrmCustomerPoolConfigDO poolConfig) {
        LambdaQueryWrapper<CrmCustomerDO> query = new LambdaQueryWrapper<>();
        query.gt(CrmCustomerDO::getOwnerUserId, 0);
        // 未锁定 + 未成交
        query.eq(CrmCustomerDO::getLockStatus, false).eq(CrmCustomerDO::getDealStatus, false);
        // 排除已在公海中的客户
        query.and(w -> w.eq(CrmCustomerDO::getPool, Boolean.FALSE).or().isNull(CrmCustomerDO::getPool));
        // 已经超时
        LocalDateTime dealExpireTime = LocalDateTime.now().minusDays(poolConfig.getDealExpireDays());
        LocalDateTime contactExpireTime = LocalDateTime.now().minusDays(poolConfig.getContactExpireDays());
        query.and(q -> {
            // 情况一：成交超时
            q.lt(CrmCustomerDO::getOwnerTime, dealExpireTime)
                    // 情况二：跟进超时
                    .or(w -> w.lt(CrmCustomerDO::getOwnerTime, contactExpireTime)
                            .and(p -> p.lt(CrmCustomerDO::getContactLastTime, contactExpireTime)
                                    .or().isNull(CrmCustomerDO::getContactLastTime)));
        });
        return selectList(query);
    }

    default Long selectCountByTodayContact(Long ownerUserId) {
        MPJLambdaWrapperX<CrmCustomerDO> query = new MPJLambdaWrapperX<>();
        // 我负责的 + 非公海
        CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                CrmCustomerDO::getId, ownerUserId, CrmSceneTypeEnum.OWNER.getType());
        // 今天需联系
        LocalDateTime beginOfToday = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
        LocalDateTime endOfToday = LocalDateTimeUtil.endOfDay(LocalDateTime.now());
        query.between(CrmCustomerDO::getContactNextTime, beginOfToday, endOfToday);
        return selectCount(query);
    }

    default Long selectCountByFollow(Long ownerUserId) {
        MPJLambdaWrapperX<CrmCustomerDO> query = new MPJLambdaWrapperX<>();
        // 我负责的 + 非公海
        CrmPermissionUtils.appendPermissionCondition(query, CrmBizTypeEnum.CRM_CUSTOMER.getType(),
                CrmCustomerDO::getId, ownerUserId, CrmSceneTypeEnum.OWNER.getType());
        // 未跟进
        query.eq(CrmClueDO::getFollowUpStatus, false);
        return selectCount(query);
    }

}
