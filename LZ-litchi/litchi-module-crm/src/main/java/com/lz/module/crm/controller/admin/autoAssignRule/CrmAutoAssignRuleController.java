package com.lz.module.crm.controller.admin.autoAssignRule;

import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule.CrmAutoAssignRuleExcelVO;
import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule.CrmAutoAssignRulePageReqVO;
import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule.CrmAutoAssignRuleRespVO;
import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRule.CrmAutoAssignRuleSaveReqVO;
import com.lz.module.crm.controller.admin.autoAssignRule.vo.autoAssignRuleUser.CrmAutoAssignRuleUserRespVO;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.util.*;
import java.io.IOException;

import com.lz.framework.common.pojo.PageParam;
import com.lz.framework.common.pojo.PageResult;
import com.lz.framework.common.pojo.CommonResult;
import com.lz.framework.common.util.object.BeanUtils;
import static com.lz.framework.common.pojo.CommonResult.success;

import com.lz.framework.excel.core.util.ExcelUtils;

import com.lz.framework.apilog.core.annotation.ApiAccessLog;
import static com.lz.framework.apilog.core.enums.OperateTypeEnum.*;

import com.lz.module.crm.dal.dataobject.autoAssignRule.CrmAutoAssignRuleDO;
import com.lz.module.crm.service.autoAssignRule.CrmAutoAssignRuleService;

/**
 * CRM 自动分配规则 Controller
 *
 * @author 荔枝软件
 */
@Tag(name = "管理后台 - CRM 自动分配规则")
@RestController
@RequestMapping("/crm/auto-assign-rule")
@Validated
public class CrmAutoAssignRuleController {

    @Resource
    private CrmAutoAssignRuleService autoAssignRuleService;

    /**
     * 创建CRM 自动分配规则
     */
    @PostMapping("/create")
    @Operation(summary = "创建CRM 自动分配规则")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:create')")
    public CommonResult<Long> createAutoAssignRule(@Valid @RequestBody CrmAutoAssignRuleSaveReqVO createReqVO) {
        return success(autoAssignRuleService.createAutoAssignRule(createReqVO));
    }

    /**
     * 更新CRM 自动分配规则
     */
    @PutMapping("/update")
    @Operation(summary = "更新CRM 自动分配规则")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:update')")
    public CommonResult<Boolean> updateAutoAssignRule(@Valid @RequestBody CrmAutoAssignRuleSaveReqVO updateReqVO) {
        autoAssignRuleService.updateAutoAssignRule(updateReqVO);
        return success(true);
    }

    /**
     * 删除CRM 自动分配规则
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除CRM 自动分配规则")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:delete')")
    public CommonResult<Boolean> deleteAutoAssignRule(@RequestParam("id") Long id) {
        autoAssignRuleService.deleteAutoAssignRule(id);
        return success(true);
    }

    /**
     * 批量删除CRM 自动分配规则
     */
    @DeleteMapping("/delete-list")
    @Parameter(name = "ids", description = "编号", required = true)
    @Operation(summary = "批量删除CRM 自动分配规则")
                @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:delete')")
    public CommonResult<Boolean> deleteAutoAssignRuleList(@RequestParam("ids") List<Long> ids) {
        autoAssignRuleService.deleteAutoAssignRuleListByIds(ids);
        return success(true);
    }

    /**
     * 获取CRM 自动分配规则详情
     */
    @GetMapping("/get")
    @Operation(summary = "获得CRM 自动分配规则")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:query')")
    public CommonResult<CrmAutoAssignRuleRespVO> getAutoAssignRule(@RequestParam("id") Long id) {
        CrmAutoAssignRuleDO autoAssignRule = autoAssignRuleService.getAutoAssignRule(id);
        return success(BeanUtils.toBean(autoAssignRule, CrmAutoAssignRuleRespVO.class));
    }

    /**
     * 获取CRM 自动分配规则分页
     */
    @GetMapping("/page")
    @Operation(summary = "获得CRM 自动分配规则分页")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:query')")
    public CommonResult<PageResult<CrmAutoAssignRuleRespVO>> getAutoAssignRulePage(@Valid CrmAutoAssignRulePageReqVO pageReqVO) {
        PageResult<CrmAutoAssignRuleDO> pageResult = autoAssignRuleService.getAutoAssignRulePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, CrmAutoAssignRuleRespVO.class));
    }

    /**
     * 导出CRM 自动分配规则 Excel
     */
    @GetMapping("/export-excel")
    @Operation(summary = "导出CRM 自动分配规则 Excel")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportAutoAssignRuleExcel(@Valid CrmAutoAssignRulePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        List<CrmAutoAssignRuleDO> list = autoAssignRuleService.getAutoAssignRulePage(pageReqVO).getList();
        // 导出 Excel
        ExcelUtils.write(response, "CRM 自动分配规则.xls", "数据", CrmAutoAssignRuleExcelVO.class,
                        BeanUtils.toBean(list, CrmAutoAssignRuleExcelVO.class));
    }


    // ==================== 子表（CRM  规则人员明细） ====================

    /**
     * 获取CRM  规则人员明细列表
     */
    @GetMapping("/auto-assign-rule-user/list-by-rule-id")
    @Operation(summary = "获得CRM  规则人员明细列表")
    @Parameter(name = "ruleId", description = "规则")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:query')")
    public CommonResult<List<CrmAutoAssignRuleUserRespVO>> getAutoAssignRuleUserListByRuleId(@RequestParam("ruleId") Long ruleId) {
        return success(BeanUtils.toBean(autoAssignRuleService.getAutoAssignRuleUserListByRuleId(ruleId), CrmAutoAssignRuleUserRespVO.class));
    }

}
