package com.lz.module.crm.service.customer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lz.framework.common.enums.CommonConstants;
import com.lz.framework.common.enums.CommonStatusEnum;
import com.lz.framework.common.util.object.BeanUtils;
import com.lz.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceReqVO;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceSaveReqVO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerSourceDO;
import com.lz.module.crm.dal.mysql.customer.CrmCustomerSourceMapper;
import com.lz.module.crm.dal.redis.CrmRedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static com.lz.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lz.module.crm.enums.ErrorCodeConstants.*;

/**
 * CRM 客户来源 Service 实现类
 *
 * @author 荔枝软件
 */
@Slf4j
@Service
@Validated
public class CrmCustomerSourceServiceImpl implements CrmCustomerSourceService {

    @Resource
    private CrmCustomerSourceMapper customerSourceMapper;

    @Override
    @CacheEvict(cacheNames = {CrmRedisKeyConstants.CUSTOMER_SOURCE_ANCESTOR_NAME, CrmRedisKeyConstants.CUSTOMER_SOURCE_TREE,
            CrmRedisKeyConstants.CUSTOMER_SOURCE_NAME}, allEntries = true)
    public Long createCustomerSource(CrmCustomerSourceSaveReqVO createReqVO) {
        if (createReqVO.getParentId() == null) {
            createReqVO.setParentId(CrmCustomerSourceDO.PARENT_ID_ROOT);
        }
        // 校验父级编号的有效性
        validateParentCustomerSource(null, createReqVO.getParentId());
        // 校验分类名称的唯一性
        validateCustomerSourceNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

        // 插入
        CrmCustomerSourceDO customerSource = BeanUtils.toBean(createReqVO, CrmCustomerSourceDO.class);
        // 设置祖级列表：从顶级到直属父级的顺序，例如 “0,100,101”
        customerSource.setAncestors(buildAncestors(customerSource.getParentId()));
        customerSourceMapper.insert(customerSource);

        // 返回
        return customerSource.getId();
    }

    @Override
    @CacheEvict(cacheNames = {CrmRedisKeyConstants.CUSTOMER_SOURCE_ANCESTOR_NAME, CrmRedisKeyConstants.CUSTOMER_SOURCE_TREE,
            CrmRedisKeyConstants.CUSTOMER_SOURCE_NAME}, allEntries = true) // allEntries 清空所有缓存，因为名称或父级变更会影响自身及所有子级的祖先名称
    public void updateCustomerSource(CrmCustomerSourceSaveReqVO updateReqVO) {
        if (updateReqVO.getParentId() == null) {
            updateReqVO.setParentId(CrmCustomerSourceDO.PARENT_ID_ROOT);
        }
        // 校验存在
        validateCustomerSourceExists(updateReqVO.getId());
        // 校验父级编号的有效性
        validateParentCustomerSource(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验分类名称的唯一性
        validateCustomerSourceNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新
        CrmCustomerSourceDO updateObj = BeanUtils.toBean(updateReqVO, CrmCustomerSourceDO.class);
        // 重新计算祖级列表：从顶级到直属父级的顺序，例如 “0,100,101”
        String ancestors = buildAncestors(updateObj.getParentId());
        updateObj.setAncestors(ancestors);
        // 记录更新前的父级编号，用于判断是否需要同步更新子级的祖级列表
        CrmCustomerSourceDO oldCustomerSource = customerSourceMapper.selectById(updateReqVO.getId());
        customerSourceMapper.updateById(updateObj);

        // 如果父级发生变化，需要同步更新所有子级的祖级列表
        if (oldCustomerSource != null && !Objects.equals(oldCustomerSource.getParentId(), updateObj.getParentId())) {
            updateChildrenAncestors(updateReqVO.getId(), ancestors);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {CrmRedisKeyConstants.CUSTOMER_SOURCE_ANCESTOR_NAME, CrmRedisKeyConstants.CUSTOMER_SOURCE_TREE,
            CrmRedisKeyConstants.CUSTOMER_SOURCE_NAME}, allEntries = true) // allEntries 清空所有缓存，因为删除后自身及所有子级的祖先名称缓存都会失效
    public void deleteCustomerSource(Long id, Boolean isDeleteChildren) {
        // 校验存在
        validateCustomerSourceExists(id);
        // 校验是否有子CRM 客户来源
        Long childrenCount = customerSourceMapper.selectCountByParentId(id);
        if (childrenCount > 0) {
            if (!Boolean.TRUE.equals(isDeleteChildren)) {
                throw exception(CUSTOMER_SOURCE_EXIST_CHILDREN, childrenCount);
            }
            // 级联删除所有子CRM 客户来源
            List<CrmCustomerSourceDO> children = new ArrayList<>();
            selectCustomerSourceChildren(id, children);
            children.forEach(child -> customerSourceMapper.deleteById(child.getId()));
        }
        // 删除
        customerSourceMapper.deleteById(id);
    }

    /**
     * 递归收集所有子CRM 客户来源
     *
     * @param parentId 父级编号
     * @param result   收集结果
     */
    private void selectCustomerSourceChildren(Long parentId, List<CrmCustomerSourceDO> result) {
        List<CrmCustomerSourceDO> children = customerSourceMapper.selectListByParentId(Collections.singleton(parentId));
        if (CollUtil.isEmpty(children)) {
            return;
        }
        result.addAll(children);
        children.forEach(child -> selectCustomerSourceChildren(child.getId(), result));
    }

    /**
     * 更新所有子孙CRM 客户来源的祖级列表
     *
     * @param customerSourceId        CRM 客户来源编号
     * @param customerSourceAncestors 该CRM 客户来源的祖级列表
     */
    private void updateChildrenAncestors(Long customerSourceId, String customerSourceAncestors) {
        List<CrmCustomerSourceDO> children = customerSourceMapper.selectListByParentId(Collections.singleton(customerSourceId));
        for (CrmCustomerSourceDO child : children) {
            // 子级的祖级列表 = 父级的祖级列表 + 父级编号
            String childAncestors = customerSourceAncestors + CommonConstants.ANCESTORS_SEPARATOR + customerSourceId;
            CrmCustomerSourceDO updateObj = new CrmCustomerSourceDO();
            updateObj.setId(child.getId());
            updateObj.setAncestors(childAncestors);
            customerSourceMapper.updateById(updateObj);
            // 递归处理下一层
            updateChildrenAncestors(child.getId(), childAncestors);
        }
    }

    /**
     * 计算祖级列表，从顶级到直属父级的顺序，例如 “0,100,101”
     *
     * @param parentId 父级编号
     * @return 祖级列表
     */
    private String buildAncestors(Long parentId) {
        if (parentId == null || CrmCustomerSourceDO.PARENT_ID_ROOT.equals(parentId)) {
            return CommonConstants.ANCESTORS_ROOT.toString();
        }
        // 自下而上收集父级编号
        List<Long> ancestorIds = new ArrayList<>();
        Long currentId = parentId;
        for (int i = 0; i < Short.MAX_VALUE; i++) { // 使用 Short.MAX_VALUE 避免 bug 场景下，存在死循环
            if (currentId == null || CrmCustomerSourceDO.PARENT_ID_ROOT.equals(currentId)) {
                break;
            }
            CrmCustomerSourceDO customerSource = customerSourceMapper.selectById(currentId);
            if (customerSource == null) {
                break;
            }
            ancestorIds.add(currentId);
            currentId = customerSource.getParentId();
        }
        // 反转：从顶级到直属父级的顺序
        Collections.reverse(ancestorIds);
        StringBuilder sb = new StringBuilder(CommonConstants.ANCESTORS_ROOT.toString());
        for (Long ancestorId : ancestorIds) {
            sb.append(CommonConstants.ANCESTORS_SEPARATOR).append(ancestorId);
        }
        return sb.toString();
    }

    private CrmCustomerSourceDO validateCustomerSourceExists(Long id) {
        if (id == null) {
            return null;
        }
        CrmCustomerSourceDO customerSource = customerSourceMapper.selectById(id);
        if (customerSource == null) {
            throw exception(CUSTOMER_SOURCE_NOT_EXISTS);
        }
        return customerSource;
    }

    private void validateParentCustomerSource(Long id, Long parentId) {
        if (parentId == null || CommonConstants.ANCESTORS_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父CRM 客户来源
        if (Objects.equals(id, parentId)) {
            throw exception(CUSTOMER_SOURCE_PARENT_ERROR);
        }
        // 2. 父CRM 客户来源不存在
        CrmCustomerSourceDO parentCustomerSource = customerSourceMapper.selectById(parentId);
        if (parentCustomerSource == null) {
            throw exception(CUSTOMER_SOURCE_PARENT_NOT_EXISTS);
        }
        // 3. 递归校验父CRM 客户来源，如果父CRM 客户来源是自己的子CRM 客户来源，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentCustomerSource.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(CUSTOMER_SOURCE_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父CRM 客户来源
            if (parentId == null || CrmCustomerSourceDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentCustomerSource = customerSourceMapper.selectById(parentId);
            if (parentCustomerSource == null) {
                break;
            }
        }
    }

    private void validateCustomerSourceNameUnique(Long id, Long parentId, String name) {
        CrmCustomerSourceDO customerSource = customerSourceMapper.selectByParentIdAndName(parentId, name);
        if (customerSource == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的CRM 客户来源
        if (id == null) {
            throw exception(CUSTOMER_SOURCE_DUPLICATE);
        }
        if (ObjectUtil.notEqual(customerSource.getId(), id)) {
            throw exception(CUSTOMER_SOURCE_DUPLICATE);
        }
    }

    @Override
    public CrmCustomerSourceDO getCustomerSource(Long id) {
        return customerSourceMapper.selectById(id);
    }

    @Override
    public List<CrmCustomerSourceDO> getCustomerSourceList(CrmCustomerSourceReqVO listReqVO) {
        return customerSourceMapper.selectList(listReqVO);
    }

    @Override
    public List<CrmCustomerSourceDO> getParentCustomerSourceList(Long id) {
        List<CrmCustomerSourceDO> result = new ArrayList<>();
        if (id == null) {
            return result;
        }
        CrmCustomerSourceDO customerSource = customerSourceMapper.selectById(id);
        if (customerSource == null) {
            return result;
        }
        // 1. 从祖级列表中解析出所有父级编号，顺序为：顶级 -> 直属父级
        List<Long> ancestorIds = parseAncestors(customerSource.getAncestors());
        if (ancestorIds.isEmpty()) {
            // 2. 兜底：祖级列表为空（历史数据）时，通过 parent_id 向上遍历
            ancestorIds = getAncestorIdsByParent(customerSource);
        }
        if (ancestorIds.isEmpty()) {
            return result;
        }
        // 3. 一次性查询所有父级
        Map<Long, CrmCustomerSourceDO> customerSourceMap = customerSourceMapper.selectByIds(ancestorIds).stream()
                .collect(Collectors.toMap(CrmCustomerSourceDO::getId, customerSourceDO -> customerSourceDO));
        // 4. 按「自己的上级 -> 顶级」的顺序返回
        for (int i = ancestorIds.size() - 1; i >= 0; i--) {
            CrmCustomerSourceDO parent = customerSourceMap.get(ancestorIds.get(i));
            if (parent != null) {
                result.add(parent);
            }
        }
        return result;
    }

    @Override
    public List<CrmCustomerSourceDO> getCustomerSourceChain(Long id) {
        List<CrmCustomerSourceDO> result = new ArrayList<>();
        if (id == null) {
            return result;
        }
        CrmCustomerSourceDO customerSource = customerSourceMapper.selectById(id);
        if (customerSource == null) {
            return result;
        }
        // 1. 父级链路，顺序为：顶级 -> 直属父级
        List<CrmCustomerSourceDO> parentList = getParentCustomerSourceList(id);
        // getParentCustomerSourceList 返回的是「自己的上级 -> 顶级」，这里需要反转成「顶级 -> 直属父级」
        Collections.reverse(parentList);
        result.addAll(parentList);
        // 2. 追加自身
        result.add(customerSource);
        return result;
    }

    @Override
    @Cacheable(cacheNames = CrmRedisKeyConstants.CUSTOMER_SOURCE_NAME, key = "#source",
            condition = "#source != null")
    public String getCustomerSourceName(Long source) {
        if (source == null) {
            return String.valueOf(source);
        }
        CrmCustomerSourceDO customerSource = customerSourceMapper.selectById(source);
        if (customerSource == null) {
            return String.valueOf(source);
        }
        return customerSource.getName();
    }

    @Override
    public void validateCustomerSourceExists(Long newSourceId, Long oldSourceId) {
        // 如果新的id为空直接返回true
        if (newSourceId == null) {
            return;
        }
        // 如果旧的id不为空且等于新的id，返回true
        if (oldSourceId != null && oldSourceId.equals(newSourceId)) {
            return;
        }
        try {
            if (customerSourceMapper.selectById(newSourceId) == null) {
                throw exception(CUSTOMER_SOURCE_NOT_EXISTS);
            }
        } catch (NumberFormatException e) {
            log.error("CRM 客户来源编号解析异常:{}", e.getMessage());
            throw exception(CUSTOMER_SOURCE_NOT_EXISTS);
        }
    }

    @Override
    @Cacheable(cacheNames = CrmRedisKeyConstants.CUSTOMER_SOURCE_ANCESTOR_NAME, key = "#source")
    public String getCustomerSourceAncestorsName(Long source) {
        if (source == null) {
            return String.valueOf(source);
        }
        Long sourceId = null;
        try {
            sourceId = Long.valueOf(source);
        } catch (NumberFormatException e) {
            log.error("CRM 客户来源编号解析异常:{}", e.getMessage());
            return String.valueOf(source);
        }
        //查询到自己
        CrmCustomerSourceDO customerSource = customerSourceMapper.selectById(sourceId);
        if (customerSource == null) {
            return String.valueOf(source);
        }
        //查询到所有父级
        List<CrmCustomerSourceDO> parentCustomerSourceList = getParentCustomerSourceList(sourceId);

        //把自己拼到父级然后拼接name
        parentCustomerSourceList.add(customerSource);
        return parentCustomerSourceList.stream().map(CrmCustomerSourceDO::getName).collect(Collectors.joining(CommonConstants.ANCESTORS_SEPARATOR));
    }

    @Override
    @Cacheable(cacheNames = CrmRedisKeyConstants.CUSTOMER_SOURCE_TREE, key = "'all'")
    public List<CrmCustomerSourceDO> getCustomerSourceTree() {
        // 只查询开启状态的来源，关闭的来源不参与级联选择
        return customerSourceMapper.selectList(new LambdaQueryWrapperX<CrmCustomerSourceDO>()
                .eq(CrmCustomerSourceDO::getStatus, CommonStatusEnum.ENABLE.getStatus())
                .orderByAsc(CrmCustomerSourceDO::getOrderNum)
                .orderByAsc(CrmCustomerSourceDO::getId));
    }

    /**
     * 解析祖级列表，得到父级编号，顺序为：顶级 -> 直属父级
     *
     * @param ancestors 祖级列表，例如 “0,100,101”
     * @return 父级编号列表
     */
    private List<Long> parseAncestors(String ancestors) {
        List<Long> ancestorIds = new ArrayList<>();
        if (StrUtil.isBlank(ancestors)) {
            return ancestorIds;
        }
        for (String ancestor : StrUtil.split(ancestors, CommonConstants.ANCESTORS_SEPARATOR)) {
            Long ancestorId = NumberUtil.parseLong(ancestor);
            // 跳过根标识 “0”
            if (CrmCustomerSourceDO.PARENT_ID_ROOT.equals(ancestorId)) {
                continue;
            }
            ancestorIds.add(ancestorId);
        }
        return ancestorIds;
    }

    /**
     * 通过 parent_id 向上遍历获得父级编号，顺序为：顶级 -> 直属父级
     *
     * @param customerSource CRM 客户来源
     * @return 父级编号列表
     */
    private List<Long> getAncestorIdsByParent(CrmCustomerSourceDO customerSource) {
        List<Long> ancestorIds = new ArrayList<>();
        Long parentId = customerSource.getParentId();
        for (int i = 0; i < Short.MAX_VALUE; i++) { // 使用 Short.MAX_VALUE 避免 bug 场景下，存在死循环
            if (parentId == null || CrmCustomerSourceDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            CrmCustomerSourceDO parent = customerSourceMapper.selectById(parentId);
            if (parent == null) {
                break;
            }
            ancestorIds.add(parentId);
            parentId = parent.getParentId();
        }
        Collections.reverse(ancestorIds);
        return ancestorIds;
    }

}
