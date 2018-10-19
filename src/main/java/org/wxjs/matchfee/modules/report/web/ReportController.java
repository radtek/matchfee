/**
 * Copyright &copy; 2016-2018 千里目.
 */
package org.wxjs.matchfee.modules.report.web;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.wxjs.matchfee.common.persistence.Page;
import org.wxjs.matchfee.common.utils.DateUtils;
import org.wxjs.matchfee.common.utils.Util;
import org.wxjs.matchfee.common.utils.excel.ExportExcel;
import org.wxjs.matchfee.common.web.BaseController;
import org.wxjs.matchfee.modules.charge.entity.Charge;
import org.wxjs.matchfee.modules.charge.entity.DeductionDoc;
import org.wxjs.matchfee.modules.charge.entity.DeductionDocItem;
import org.wxjs.matchfee.modules.charge.entity.LandPayTicket;
import org.wxjs.matchfee.modules.charge.entity.OpinionBook;
import org.wxjs.matchfee.modules.charge.entity.OpinionBookItem;
import org.wxjs.matchfee.modules.charge.entity.PayTicket;
import org.wxjs.matchfee.modules.charge.entity.Project;
import org.wxjs.matchfee.modules.charge.entity.ProjectDeduction;
import org.wxjs.matchfee.modules.charge.entity.ProjectLicense;
import org.wxjs.matchfee.modules.charge.service.ChargeService;
import org.wxjs.matchfee.modules.charge.service.DeductionDocItemService;
import org.wxjs.matchfee.modules.charge.service.DeductionDocService;
import org.wxjs.matchfee.modules.charge.service.LandPayTicketService;
import org.wxjs.matchfee.modules.charge.service.OpinionBookItemService;
import org.wxjs.matchfee.modules.charge.service.OpinionBookService;
import org.wxjs.matchfee.modules.charge.service.PayTicketService;
import org.wxjs.matchfee.modules.charge.service.ProjectDeductionService;
import org.wxjs.matchfee.modules.charge.service.ProjectLicenseService;
import org.wxjs.matchfee.modules.report.dataModel.ReportData;
import org.wxjs.matchfee.modules.report.entity.ReportEntity;
import org.wxjs.matchfee.modules.report.entity.ReportParam;
import org.wxjs.matchfee.modules.report.entity.TaxProtectReport;
import org.wxjs.matchfee.modules.report.service.ReportService;
import org.wxjs.matchfee.modules.sys.entity.User;
import org.wxjs.matchfee.modules.sys.utils.UserUtils;

import com.google.gson.Gson;


/**
 * ReportController
 * @author GLQ
 * @version 2017-12-07
 */
@Controller
@RequestMapping(value = "${adminPath}/report/report")
public class ReportController extends BaseController {	
	
	@Autowired
	ReportService reportService;
	
	@Autowired
	ChargeService chargeService;
	
	@Autowired
	private DeductionDocService deductionDocService;
	
	@Autowired
	private DeductionDocItemService  deductionDocItemService;
	
	@Autowired
	private OpinionBookService opinionBookService;
	
	@Autowired
	private OpinionBookItemService opinionBookItemService;
	
	@Autowired
	private ProjectDeductionService projectDeductionService;
	
	@Autowired
	private ProjectLicenseService projectLicenseService;
	
	@Autowired
	private PayTicketService payTicketService;
	
	@Autowired
	private LandPayTicketService landPayTicketService;
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"query"})
	public String query(ReportParam reportParam, HttpServletRequest request, HttpServletResponse response, Model model) {
		
		if(reportParam==null){
			reportParam = new ReportParam();
		}
		
		if("yearTimes".equals(reportParam.getReportType())){
			String dateFrom = Util.getString(request.getParameter("dateFrom"));
			String dateTo = Util.getString(request.getParameter("dateTo"));
			
			logger.debug("reportParam==null:{}, dateFrom:{}", (reportParam==null), dateFrom);

			Calendar cal=Calendar.getInstance();
			if(StringUtils.isNotEmpty(dateFrom)){
				cal.set(Calendar.YEAR, Util.getInteger(dateFrom));
			}
			reportParam.setDateFrom(cal.getTime());
			
			cal=Calendar.getInstance();
			if(StringUtils.isNotEmpty(dateTo)){
				cal.set(Calendar.YEAR, Util.getInteger(dateTo));
			}
			reportParam.setDateTo(cal.getTime());			
		}else{
			if(reportParam.getDateFrom()==null){
				Calendar cal=Calendar.getInstance();
				cal.set(cal.get(Calendar.YEAR), 0, 1);
				reportParam.setDateFrom(cal.getTime());
			}
			
			if(reportParam.getDateTo()==null){
				Calendar cal=Calendar.getInstance();
				reportParam.setDateTo(cal.getTime());
			}			
		}
		
		ReportData data = reportService.getReport(reportParam);
		
		model.addAttribute("reportData", data);
		
		model.addAttribute("reportParam", reportParam);
		
		return "modules/report/report";
	}

	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"monthCountMoneyReport"})
	public String monthCountMoneyReport(ReportParam reportParam, HttpServletRequest request, HttpServletResponse response, Model model) {
		
		if(reportParam==null){
			reportParam = new ReportParam();
		}
		
		logger.debug("reportParam.getDateFrom():{}", reportParam.getDateFrom());
		

		if(reportParam.getDateFrom()==null){
			Calendar cal=Calendar.getInstance();
			cal.set(cal.get(Calendar.YEAR), 0, 1);
			reportParam.setDateFrom(cal.getTime());
		}
		
		if(reportParam.getDateTo()==null){
			Calendar cal=Calendar.getInstance();
			reportParam.setDateTo(cal.getTime());
		}
		
		List<ReportEntity> list = reportService.getMonthCountMoneyReport(reportParam);
		
		model.addAttribute("list", list);
		
		model.addAttribute("reportParam", reportParam);
		
		return "modules/report/monthCountMoneyReport";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"yearCountMoneyReport"})
	public String yearCountMoneyReport(ReportParam reportParam, HttpServletRequest request, HttpServletResponse response, Model model) {
		
		String dateFrom = Util.getString(request.getParameter("dateFrom"));
		String dateTo = Util.getString(request.getParameter("dateTo"));
		
		logger.debug("reportParam==null:{}, dateFrom:{}", (reportParam==null), dateFrom);
		
		if(reportParam==null){
			reportParam = new ReportParam();
		}
		
		logger.debug("reportParam.getDateFrom():{}", reportParam.getDateFrom());

		Calendar cal=Calendar.getInstance();
		if(StringUtils.isNotEmpty(dateFrom)){
			cal.set(Calendar.YEAR, Util.getInteger(dateFrom));
		}
		reportParam.setDateFrom(cal.getTime());
		
		cal=Calendar.getInstance();
		if(StringUtils.isNotEmpty(dateTo)){
			cal.set(Calendar.YEAR, Util.getInteger(dateTo));
		}
		reportParam.setDateTo(cal.getTime());
		
		List<ReportEntity> list = reportService.getYearCountMoneyReport(reportParam);
		
		model.addAttribute("list", list);
		
		model.addAttribute("reportParam", reportParam);
		
		return "modules/report/yearCountMoneyReport";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"monthlyReport"})
	public String monthlyReport(ReportParam reportParam, HttpServletRequest request, HttpServletResponse response, Model model) {
		
		if(reportParam==null){
			reportParam = new ReportParam();
		}

		if(reportParam.getDateFrom()==null){
			Calendar cal=Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			reportParam.setDateFrom(cal.getTime());
		}
		
		Charge charge = new Charge();
		charge.setDateFrom(reportParam.getDateFrom());
		
		List<Charge> list = chargeService.monthlyReport(charge);
		
		model.addAttribute("list", list);
		
		model.addAttribute("reportParam", reportParam);
		
		return "modules/report/monthlyReport";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = "monthlyReportExport", method=RequestMethod.POST)
	public String monthlyReportExport(ReportParam reportParam, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
		
		try {
            String fileName = DateUtils.formatDate(reportParam.getDateFrom(), "yyyy年MM月") + "-城市基础设施配套费收费台账.xlsx";
            
    		Charge charge = new Charge();
    		charge.setDateFrom(reportParam.getDateFrom());
    		
    		List<Charge> list = chargeService.monthlyReport(charge);
            
    		new ExportExcel("城市基础设施配套费收费台账", Charge.class).setDataList(list).write(response, fileName).dispose();
    		
    		return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导出失败！失败信息："+e.getMessage());
		}
		return "redirect:" + adminPath + "/report/report/monthlyReport?repage";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"yearlyReport"})
	public String yearlyReport(ReportParam reportParam, HttpServletRequest request, HttpServletResponse response, Model model) {
		
		String dateFrom = Util.getString(request.getParameter("dateFrom"));
		
		if(reportParam==null){
			reportParam = new ReportParam();
		}

		Calendar cal=Calendar.getInstance();
		if(StringUtils.isNotEmpty(dateFrom)){
			cal.set(Calendar.YEAR, Util.getInteger(dateFrom));
		}
		reportParam.setDateFrom(cal.getTime());
		
		Charge charge = new Charge();
		charge.setDateFrom(reportParam.getDateFrom());
		
		List<Charge> list = chargeService.yearlyReport(charge);
		
		model.addAttribute("list", list);
		
		model.addAttribute("reportParam", reportParam);
		
		return "modules/report/yearlyReport";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = "yearlyReportExport", method=RequestMethod.POST)
	public String yearlyReportExport(ReportParam reportParam, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
		String dateFrom = Util.getString(request.getParameter("dateFrom"));
		
		Calendar cal=Calendar.getInstance();
		if(StringUtils.isNotEmpty(dateFrom)){
			cal.set(Calendar.YEAR, Util.getInteger(dateFrom));
		}
		reportParam.setDateFrom(cal.getTime());
		
		try {
			String fileName = DateUtils.formatDate(reportParam.getDateFrom(), "yyyy年") + "-城市基础设施配套费收费台账.xlsx";
            
    		Charge charge = new Charge();
    		charge.setDateFrom(reportParam.getDateFrom());
    		
    		List<Charge> list = chargeService.yearlyReport(charge);
            
    		new ExportExcel("城市基础设施配套费收费台账", Charge.class).setDataList(list).write(response, fileName).dispose();
    		
    		return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导出失败！失败信息："+e.getMessage());
		}
		return "redirect:" + adminPath + "/report/report/yearlyReport?repage";
	}
	
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"search"})
	public String search(Charge charge, HttpServletRequest request, HttpServletResponse response, Model model) {

		if(charge.getDateFrom()==null){
			Calendar cal=Calendar.getInstance();
			cal.set(cal.get(Calendar.YEAR), 0, 1);
			charge.setDateFrom(cal.getTime());
		}
		
		if(charge.getDateTo()==null){
			Calendar cal=Calendar.getInstance();
			charge.setDateTo(cal.getTime());
		}
		
		User user = UserUtils.getUser();

		if(user.getIsQyUser()){
			Project projectParam = new Project();
			projectParam.setPrjNum(user.getProject().getPrjNum());
			charge.setProject(projectParam);			
		}

		
		Page<Charge> page = chargeService.findPage(new Page<Charge>(request, response), charge);
		
		model.addAttribute("page", page);
		
		model.addAttribute("charge", charge);
		
		return "modules/report/queryChargeList";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"search4CommonUser"})
	public String search4CommonUser(Charge charge, HttpServletRequest request, HttpServletResponse response, Model model) {

		if(charge.getDateFrom()==null){
			Calendar cal=Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			charge.setDateFrom(cal.getTime());
		}
		
		if(charge.getDateTo()==null){
			Calendar cal=Calendar.getInstance();
			charge.setDateTo(cal.getTime());
		}
		
		User user = UserUtils.getUser();

		if(user.getIsQyUser()){
			Project projectParam = new Project();
			projectParam.setPrjNum(user.getProject().getPrjNum());
			charge.setProject(projectParam);			
		}

		
		Page<Charge> page = chargeService.findPage(new Page<Charge>(request, response), charge);
		
		model.addAttribute("page", page);
		
		model.addAttribute("charge", charge);
		
		return "modules/report/queryChargeList4CommonUser";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"toSearch4CommonUser"})
	public String toSearch4CommonUser(Charge charge, HttpServletRequest request, HttpServletResponse response, Model model) {

		if(charge.getDateFrom()==null){
			Calendar cal=Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			charge.setDateFrom(cal.getTime());
		}
		
		if(charge.getDateTo()==null){
			Calendar cal=Calendar.getInstance();
			charge.setDateTo(cal.getTime());
		}
		
		model.addAttribute("charge", charge);
		
		return "modules/report/queryChargeList4CommonUser";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"searchInfo"})
	public String searchInfo(Charge entity, HttpServletRequest request, HttpServletResponse response, Model model) {
		
		Charge charge = chargeService.get(entity);
		
		
		//OpinionBook
		OpinionBook opinionBookParam = new OpinionBook();
		opinionBookParam.setPrjNum(charge.getProject().getPrjNum());
		
		charge.setOpinionBookList(this.opinionBookService.findList(opinionBookParam));
		
		for(OpinionBook item : charge.getOpinionBookList()){
			OpinionBookItem opinionBookItemParam = new OpinionBookItem();
			opinionBookItemParam.setDoc(item);
			item.setOpinionBookItemList(opinionBookItemService.findList(opinionBookItemParam));
		}
		
		//LandPayTicket
		LandPayTicket landPayTicketParam = new LandPayTicket();
		landPayTicketParam.setPrjNum(charge.getProject().getPrjNum());
		
		charge.setLandPayTicketList(this.landPayTicketService.findList(landPayTicketParam));
		
		//ProjectLicense
		ProjectLicense projectLicenseParam = new ProjectLicense();
		projectLicenseParam.setCharge(entity);
		
		charge.setProjectLicenseList(this.projectLicenseService.findList(projectLicenseParam));
		
		
		//ProjectDeduction
		ProjectDeduction projectDeductionParam = new ProjectDeduction();
		projectDeductionParam.setCharge(entity);
		
		charge.setProjectDeductionList(this.projectDeductionService.findList(projectDeductionParam));
		
		//DeductionDoc
		DeductionDoc deductionDocParam = new DeductionDoc();
		deductionDocParam.setCharge(entity);
		
		charge.setDeductionDocList(this.deductionDocService.findList(deductionDocParam));
		
		for(DeductionDoc item : charge.getDeductionDocList()){
			DeductionDocItem deductionDocItemParam = new DeductionDocItem();
			deductionDocItemParam.setDoc(item);
			item.setDeductionDocItemList(deductionDocItemService.findList(deductionDocItemParam));
			item.setCharge(charge);
		}
		
		//PayTicket
		PayTicket payTicketParam = new PayTicket();
		
		payTicketParam.setCharge(entity);
		
		charge.setPayTicketList(this.payTicketService.findList(payTicketParam));
		
		
		model.addAttribute("charge", charge);
		
		return "modules/report/queryChargeInfo";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"dashboard"})
	public String dashboard(ReportParam param, HttpServletRequest request, HttpServletResponse response, Model model) {
		//第一个用户框数据框
		User currentUser=UserUtils.getUser();
		model.addAttribute("user", currentUser);
		//第二个征收汇总数据框
		Collection<HashMap<String, Object>> map1=reportService.dashboardChargeStatus();
		String chargeStatusData=new Gson().toJson(map1);
		model.addAttribute("chargeStatusData", chargeStatusData);
		//第三个申报数按月统计数据框
		Collection<HashMap<String, Object>> map2=reportService.dashboardDeclare();
		String declareData=new Gson().toJson(map2);
		model.addAttribute("declareData", declareData);
		//第四个征收金额按月统计框
		Collection<HashMap<String, Object>> map3=reportService.dashboardChargeMoney();
		String chargeMoneyData=new Gson().toJson(map3);
		model.addAttribute("chargeMoneyData", chargeMoneyData);
		
		return "modules/report/dashboard";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = {"taxProtect"})
	public String taxProtect(ReportParam param, HttpServletRequest request, HttpServletResponse response, Model model) {
		
		if(param.getDateFrom() == null){
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MONTH, -1);
			param.setDateFrom(cal.getTime());

			param.setDateTo(param.getDateFrom());
		}
		
		List<TaxProtectReport> list = reportService.taxProtectReport(param);
		
		model.addAttribute("list", list);
		
		model.addAttribute("reportParam", param);
		
		return "modules/report/taxtProtectReport";
	}
	
	@RequiresPermissions("report:report:view")
	@RequestMapping(value = "taxProtectExport", method=RequestMethod.POST)
	public String taxProtectExport(ReportParam param, HttpServletRequest request, HttpServletResponse response, Model model, RedirectAttributes redirectAttributes) {
		
		try {
            String fileName = DateUtils.formatDate(param.getDateFrom(), "yyyy-MM") + "-城市基础设施配套费征收情况.xlsx";
            
            List<TaxProtectReport> list = reportService.taxProtectReport(param);
            
    		new ExportExcel("城市基础设施配套费征收情况", TaxProtectReport.class).setDataList(list).write(response, fileName).dispose();
    		
    		return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导出失败！失败信息："+e.getMessage());
		}
		return "redirect:" + adminPath + "/report/report/taxProtect?repage";
	}

}