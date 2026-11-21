package com.lz.module.crm.controller.admin.customer;

import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleExcelVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRulePageReqVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleRespVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRule.CrmAutoAssignRuleSaveReqVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRuleUser.CrmAutoAssignRuleUserRespVO;
import com.lz.module.crm.controller.admin.customer.vo.autoAssignRuleUser.CrmAutoAssignRuleUserSaveReqVO;
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

import com.lz.module.crm.dal.dataobject.customer.CrmAutoAssignRuleDO;
import com.lz.module.crm.service.customer.CrmAutoAssignRuleService;

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
     * 保存CRM 自动分配规则（存在则更新，不存在则新增）
     */
    @PutMapping("/save")
    @Operation(summary = "保存CRM 自动分配规则")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:update')")
    public CommonResult<Long> saveAutoAssignRule(@Valid @RequestBody CrmAutoAssignRuleSaveReqVO saveReqVO) {
        return success(autoAssignRuleService.saveAutoAssignRule(saveReqVO));
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
     * 获取CRM 自动分配规则详情（全局只有一条配置）
     */
    @GetMapping("/get")
    @Operation(summary = "获得CRM 自动分配规则")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:query')")
    public CommonResult<CrmAutoAssignRuleRespVO> getAutoAssignRule() {
        return success(autoAssignRuleService.getAutoAssignRuleResp());
    }

    /**
     * 获取CRM 自动分配规则分页
     */
    @GetMapping("/page")
    @Operation(summary = "获得CRM 自动分配规则分页")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:query')")
    public CommonResult<PageResult<CrmAutoAssignRuleRespVO>> getAutoAssignRulePage(@Valid CrmAutoAssignRulePageReqVO pageReqVO) {
        return success(autoAssignRuleService.getAutoAssignRuleRespPage(pageReqVO));
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

    /**
     * 新增CRM  规则人员明细
     */
    @PostMapping("/auto-assign-rule-user/create")
    @Operation(summary = "新增CRM  规则人员明细")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:create')")
    public CommonResult<Long> createAutoAssignRuleUser(@RequestParam("ruleId") Long ruleId,
                                                        @Valid @RequestBody CrmAutoAssignRuleUserSaveReqVO saveReqVO) {
        return success(autoAssignRuleService.createAutoAssignRuleUser(ruleId, saveReqVO));
    }

    /**
     * 修改CRM  规则人员明细
     */
    @PutMapping("/auto-assign-rule-user/update")
    @Operation(summary = "修改CRM  规则人员明细")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:update')")
    public CommonResult<Boolean> updateAutoAssignRuleUser(@RequestParam("ruleId") Long ruleId,
                                                           @Valid @RequestBody CrmAutoAssignRuleUserSaveReqVO saveReqVO) {
        autoAssignRuleService.updateAutoAssignRuleUser(ruleId, saveReqVO);
        return success(true);
    }

    /**
     * 更新CRM  规则人员明细的排序号（ids 按目标顺序排列）
     */
    @PutMapping("/auto-assign-rule-user/update-sort")
    @Operation(summary = "更新CRM  规则人员明细排序")
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:update')")
    public CommonResult<Boolean> updateAutoAssignRuleUserSort(@RequestParam("ruleId") Long ruleId,
                                                               @Valid @RequestBody List<Long> ids) {
        autoAssignRuleService.updateAutoAssignRuleUserSort(ruleId, ids);
        return success(true);
    }

    /**
     * 删除CRM  规则人员明细
     */
    @DeleteMapping("/auto-assign-rule-user/delete")
    @Operation(summary = "删除CRM  规则人员明细")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:auto-assign-rule:delete')")
    public CommonResult<Boolean> deleteAutoAssignRuleUser(@RequestParam("id") Long id) {
        autoAssignRuleService.deleteAutoAssignRuleUser(id);
        return success(true);
    }

}
