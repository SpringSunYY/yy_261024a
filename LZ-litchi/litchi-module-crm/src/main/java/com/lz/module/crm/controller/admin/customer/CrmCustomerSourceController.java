package com.lz.module.crm.controller.admin.customer;

import com.lz.framework.apilog.core.annotation.ApiAccessLog;
import com.lz.framework.common.pojo.CommonResult;
import com.lz.framework.common.util.object.BeanUtils;
import com.lz.framework.excel.core.util.ExcelUtils;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceExcelVO;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceReqVO;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceRespVO;
import com.lz.module.crm.controller.admin.customer.vo.source.CrmCustomerSourceSaveReqVO;
import com.lz.module.crm.dal.dataobject.customer.CrmCustomerSourceDO;
import com.lz.module.crm.service.customer.CrmCustomerSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.lz.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.lz.framework.common.pojo.CommonResult.success;

/**
 * CRM 客户来源 Controller
 *
 * @author 荔枝软件
 */
@Tag(name = "管理后台 - CRM 客户来源")
@RestController
@RequestMapping("/crm/customer-source")
@Validated
public class CrmCustomerSourceController {

    @Resource
    private CrmCustomerSourceService customerSourceService;

    /**
     * 创建CRM 客户来源
     */
    @PostMapping("/create")
    @Operation(summary = "创建CRM 客户来源")
    @PreAuthorize("@ss.hasPermission('crm:customer-source:create')")
    public CommonResult<Long> createCustomerSource(@Valid @RequestBody CrmCustomerSourceSaveReqVO createReqVO) {
        return success(customerSourceService.createCustomerSource(createReqVO));
    }

    /**
     * 更新CRM 客户来源
     */
    @PutMapping("/update")
    @Operation(summary = "更新CRM 客户来源")
    @PreAuthorize("@ss.hasPermission('crm:customer-source:update')")
    public CommonResult<Boolean> updateCustomerSource(@Valid @RequestBody CrmCustomerSourceSaveReqVO updateReqVO) {
        customerSourceService.updateCustomerSource(updateReqVO);
        return success(true);
    }

    /**
     * 删除CRM 客户来源
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除CRM 客户来源")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('crm:customer-source:delete')")
    public CommonResult<Boolean> deleteCustomerSource(@RequestParam("id") Long id) {
        customerSourceService.deleteCustomerSource(id);
        return success(true);
    }

    /**
     * 批量删除CRM 客户来源
     */

    /**
     * 获取CRM 客户来源详情
     */
    @GetMapping("/get")
    @Operation(summary = "获得CRM 客户来源")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('crm:customer-source:query')")
    public CommonResult<CrmCustomerSourceRespVO> getCustomerSource(@RequestParam("id") Long id) {
        CrmCustomerSourceDO customerSource = customerSourceService.getCustomerSource(id);
        return success(BeanUtils.toBean(customerSource, CrmCustomerSourceRespVO.class));
    }

    /**
     * 获取CRM 客户来源列表
     */
    @GetMapping("/list")
    @Operation(summary = "获得CRM 客户来源列表")
    @PreAuthorize("@ss.hasPermission('crm:customer-source:query')")
    public CommonResult<List<CrmCustomerSourceRespVO>> getCustomerSourceList(@Valid CrmCustomerSourceReqVO listReqVO) {
        List<CrmCustomerSourceDO> list = customerSourceService.getCustomerSourceList(listReqVO);
        return success(BeanUtils.toBean(list, CrmCustomerSourceRespVO.class));
    }

    /**
     * 导出CRM 客户来源 Excel
     */
    @GetMapping("/export-excel")
    @Operation(summary = "导出CRM 客户来源 Excel")
    @PreAuthorize("@ss.hasPermission('crm:customer-source:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportCustomerSourceExcel(@Valid CrmCustomerSourceReqVO listReqVO,
                                          HttpServletResponse response) throws IOException {
        List<CrmCustomerSourceDO> list = customerSourceService.getCustomerSourceList(listReqVO);
        // 导出 Excel
        ExcelUtils.write(response, "CRM 客户来源.xls", "data", CrmCustomerSourceExcelVO.class,
                BeanUtils.toBean(list, CrmCustomerSourceExcelVO.class));
    }
}
