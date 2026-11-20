package com.lz.module.system.service.dept;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.annotations.VisibleForTesting;
import com.lz.framework.common.enums.CommonConstants;
import com.lz.framework.common.enums.CommonStatusEnum;
import com.lz.framework.common.util.object.BeanUtils;
import com.lz.framework.datapermission.core.annotation.DataPermission;
import com.lz.module.system.controller.admin.dept.vo.dept.DeptListReqVO;
import com.lz.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
import com.lz.module.system.dal.dataobject.dept.DeptDO;
import com.lz.module.system.dal.mysql.dept.DeptMapper;
import com.lz.module.system.dal.redis.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

import static com.lz.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.lz.framework.common.util.collection.CollectionUtils.convertSet;
import static com.lz.module.system.enums.ErrorCodeConstants.*;

/**
 * 部门 Service 实现类
 *
 * @author 荔枝源码
 */
@Service
@Validated
@Slf4j
public class DeptServiceImpl implements DeptService {


    @Resource
    private DeptMapper deptMapper;

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public Long createDept(DeptSaveReqVO createReqVO) {
        if (createReqVO.getParentId() == null) {
            createReqVO.setParentId(DeptDO.PARENT_ID_ROOT);
        }
        // 校验父部门的有效性
        validateParentDept(null, createReqVO.getParentId());
        // 校验部门名的唯一性
        validateDeptNameUnique(null, createReqVO.getParentId(), createReqVO.getName());

        // 插入部门
        DeptDO dept = BeanUtils.toBean(createReqVO, DeptDO.class);
        // 设置祖级列表：从顶级到直属父级的顺序，例如 “0,100,101”
        dept.setAncestors(buildAncestors(dept.getParentId()));
        deptMapper.insert(dept);
        return dept.getId();
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public void updateDept(DeptSaveReqVO updateReqVO) {
        if (updateReqVO.getParentId() == null) {
            updateReqVO.setParentId(DeptDO.PARENT_ID_ROOT);
        }
        // 校验自己存在
        validateDeptExists(updateReqVO.getId());
        // 校验父部门的有效性
        validateParentDept(updateReqVO.getId(), updateReqVO.getParentId());
        // 校验部门名的唯一性
        validateDeptNameUnique(updateReqVO.getId(), updateReqVO.getParentId(), updateReqVO.getName());

        // 更新部门
        DeptDO updateObj = BeanUtils.toBean(updateReqVO, DeptDO.class);
        // 重新计算祖级列表：从顶级到直属父级的顺序，例如 “0,100,101”
        String ancestors = buildAncestors(updateObj.getParentId());
        updateObj.setAncestors(ancestors);
        // 记录更新前的父部门，用于判断是否需要同步更新子部门的祖级列表
        DeptDO oldDept = deptMapper.selectById(updateReqVO.getId());
        deptMapper.updateById(updateObj);

        // 如果父部门发生变化，需要同步更新所有子部门的祖级列表
        if (oldDept != null && !Objects.equals(oldDept.getParentId(), updateObj.getParentId())) {
            updateChildrenAncestors(updateReqVO.getId(), ancestors);
        }
    }

    /**
     * 更新部门的所有子孙部门的祖级列表
     *
     * @param deptId       部门编号
     * @param deptAncestors 该部门的祖级列表
     */
    private void updateChildrenAncestors(Long deptId, String deptAncestors) {
        List<DeptDO> children = deptMapper.selectListByParentId(Collections.singleton(deptId));
        for (DeptDO child : children) {
            // 子部门的祖级列表 = 父部门的祖级列表 + 父部门编号
            String childAncestors = deptAncestors + CommonConstants.ANCESTORS_SEPARATOR + deptId;
            DeptDO updateObj = new DeptDO();
            updateObj.setId(child.getId());
            updateObj.setAncestors(childAncestors);
            deptMapper.updateById(updateObj);
            // 递归处理下一层
            updateChildrenAncestors(child.getId(), childAncestors);
        }
    }

    /**
     * 计算部门的祖级列表，从顶级到直属父级的顺序，例如 “0,100,101”
     *
     * @param parentId 父部门编号
     * @return 祖级列表
     */
    private String buildAncestors(Long parentId) {
        if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
            return CommonConstants.ANCESTORS_ROOT.toString();
        }
        // 自下而上收集父级编号
        List<Long> ancestorIds = new ArrayList<>();
        Long currentId = parentId;
        for (int i = 0; i < Short.MAX_VALUE; i++) { // 使用 Short.MAX_VALUE 避免 bug 场景下，存在死循环
            if (currentId == null || DeptDO.PARENT_ID_ROOT.equals(currentId)) {
                break;
            }
            DeptDO dept = deptMapper.selectById(currentId);
            if (dept == null) {
                break;
            }
            ancestorIds.add(currentId);
            currentId = dept.getParentId();
        }
        // 反转：从顶级到直属父级的顺序
        Collections.reverse(ancestorIds);
        StringBuilder sb = new StringBuilder(CommonConstants.ANCESTORS_ROOT.toString());
        for (Long ancestorId : ancestorIds) {
            sb.append(CommonConstants.ANCESTORS_SEPARATOR).append(ancestorId);
        }
        return sb.toString();
    }

    @Override
    @CacheEvict(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST,
            allEntries = true) // allEntries 清空所有缓存，因为操作一个部门，涉及到多个缓存
    public void deleteDept(Long id) {
        // 校验是否存在
        validateDeptExists(id);
        // 校验是否有子部门
        if (deptMapper.selectCountByParentId(id) > 0) {
            throw exception(DEPT_EXITS_CHILDREN);
        }
        // 删除部门
        deptMapper.deleteById(id);
    }

    @VisibleForTesting
    void validateDeptExists(Long id) {
        if (id == null) {
            return;
        }
        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            throw exception(DEPT_NOT_FOUND);
        }
    }

    @VisibleForTesting
    void validateParentDept(Long id, Long parentId) {
        if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
            return;
        }
        // 1. 不能设置自己为父部门
        if (Objects.equals(id, parentId)) {
            throw exception(DEPT_PARENT_ERROR);
        }
        // 2. 父部门不存在
        DeptDO parentDept = deptMapper.selectById(parentId);
        if (parentDept == null) {
            throw exception(DEPT_PARENT_NOT_EXITS);
        }
        // 3. 递归校验父部门，如果父部门是自己的子部门，则报错，避免形成环路
        if (id == null) { // id 为空，说明新增，不需要考虑环路
            return;
        }
        for (int i = 0; i < Short.MAX_VALUE; i++) {
            // 3.1 校验环路
            parentId = parentDept.getParentId();
            if (Objects.equals(id, parentId)) {
                throw exception(DEPT_PARENT_IS_CHILD);
            }
            // 3.2 继续递归下一级父部门
            if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            parentDept = deptMapper.selectById(parentId);
            if (parentDept == null) {
                break;
            }
        }
    }

    @VisibleForTesting
    void validateDeptNameUnique(Long id, Long parentId, String name) {
        DeptDO dept = deptMapper.selectByParentIdAndName(parentId, name);
        if (dept == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的部门
        if (id == null) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
        if (ObjectUtil.notEqual(dept.getId(), id)) {
            throw exception(DEPT_NAME_DUPLICATE);
        }
    }

    @Override
    public DeptDO getDept(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public List<DeptDO> getDeptList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return deptMapper.selectByIds(ids);
    }

    @Override
    public List<DeptDO> getDeptList(DeptListReqVO reqVO) {
        List<DeptDO> list = deptMapper.selectList(reqVO);
        list.sort(Comparator.comparing(DeptDO::getSort));
        return list;
    }

    @Override
    public List<DeptDO> getChildDeptList(Collection<Long> ids) {
        List<DeptDO> children = new LinkedList<>();
        // 遍历每一层
        Collection<Long> parentIds = ids;
        for (int i = 0; i < Short.MAX_VALUE; i++) { // 使用 Short.MAX_VALUE 避免 bug 场景下，存在死循环
            // 查询当前层，所有的子部门
            List<DeptDO> depts = deptMapper.selectListByParentId(parentIds);
            // 1. 如果没有子部门，则结束遍历
            if (CollUtil.isEmpty(depts)) {
                break;
            }
            // 2. 如果有子部门，继续遍历
            children.addAll(depts);
            parentIds = convertSet(depts, DeptDO::getId);
        }
        return children;
    }

    @Override
    public List<DeptDO> getParentDeptList(Long id) {
        List<DeptDO> result = new ArrayList<>();
        if (id == null) {
            return result;
        }
        DeptDO dept = deptMapper.selectById(id);
        if (dept == null) {
            return result;
        }
        // 1. 从祖级列表中解析出所有父级编号，顺序为：顶级 -> 直属父级
        List<Long> ancestorIds = parseAncestors(dept.getAncestors());
        if (ancestorIds.isEmpty()) {
            // 2. 兜底：祖级列表为空（历史数据）时，通过 parent_id 向上遍历
            ancestorIds = getAncestorIdsByParent(dept);
        }
        if (ancestorIds.isEmpty()) {
            return result;
        }
        // 3. 一次性查询所有父部门
        Map<Long, DeptDO> deptMap = deptMapper.selectByIds(ancestorIds).stream()
                .collect(Collectors.toMap(DeptDO::getId, deptDO -> deptDO));
        // 4. 按「自己的上级 -> 顶级」的顺序返回
        for (int i = ancestorIds.size() - 1; i >= 0; i--) {
            DeptDO parent = deptMap.get(ancestorIds.get(i));
            if (parent != null) {
                result.add(parent);
            }
        }
        return result;
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
            if (DeptDO.PARENT_ID_ROOT.equals(ancestorId)) {
                continue;
            }
            ancestorIds.add(ancestorId);
        }
        return ancestorIds;
    }

    /**
     * 通过 parent_id 向上遍历获得父级编号，顺序为：顶级 -> 直属父级
     *
     * @param dept 部门
     * @return 父级编号列表
     */
    private List<Long> getAncestorIdsByParent(DeptDO dept) {
        List<Long> ancestorIds = new ArrayList<>();
        Long parentId = dept.getParentId();
        for (int i = 0; i < Short.MAX_VALUE; i++) { // 使用 Short.MAX_VALUE 避免 bug 场景下，存在死循环
            if (parentId == null || DeptDO.PARENT_ID_ROOT.equals(parentId)) {
                break;
            }
            DeptDO parent = deptMapper.selectById(parentId);
            if (parent == null) {
                break;
            }
            ancestorIds.add(parentId);
            parentId = parent.getParentId();
        }
        Collections.reverse(ancestorIds);
        return ancestorIds;
    }


    @Override
    public List<DeptDO> getDeptListByLeaderUserId(Long id) {
        return deptMapper.selectListByLeaderUserId(id);
    }

    @Override
    @DataPermission(enable = false) // 禁用数据权限，避免建立不正确的缓存
    @Cacheable(cacheNames = RedisKeyConstants.DEPT_CHILDREN_ID_LIST, key = "#id")
    public Set<Long> getChildDeptIdListFromCache(Long id) {
        List<DeptDO> children = getChildDeptList(id);
        return convertSet(children, DeptDO::getId);
    }

    @Override
    public void validateDeptList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        // 获得科室信息
        Map<Long, DeptDO> deptMap = getDeptMap(ids);
        // 校验
        ids.forEach(id -> {
            DeptDO dept = deptMap.get(id);
            if (dept == null) {
                throw exception(DEPT_NOT_FOUND);
            }
            if (!CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
                throw exception(DEPT_NOT_ENABLE, dept.getName());
            }
        });
    }

}
