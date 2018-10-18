/**
 * Copyright &copy; 2012-2016 千里目 All rights reserved.
 */
package org.wxjs.matchfee.modules.charge.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wxjs.matchfee.common.config.Global;
import org.wxjs.matchfee.common.persistence.Page;
import org.wxjs.matchfee.common.service.CrudService;
import org.wxjs.matchfee.common.utils.Util;
import org.wxjs.matchfee.modules.base.entity.DeductionItem;
import org.wxjs.matchfee.modules.charge.entity.Charge;
import org.wxjs.matchfee.modules.charge.entity.DeductionDoc;
import org.wxjs.matchfee.modules.charge.entity.DeductionDocItem;
import org.wxjs.matchfee.modules.charge.entity.OpinionBook;
import org.wxjs.matchfee.modules.charge.entity.OpinionBookItem;
import org.wxjs.matchfee.modules.charge.entity.PayTicket;
import org.wxjs.matchfee.modules.charge.entity.Project;
import org.wxjs.matchfee.modules.charge.entity.ProjectDeduction;
import org.wxjs.matchfee.modules.charge.entity.ProjectLicense;
import org.wxjs.matchfee.modules.charge.entity.SettlementList;
import org.wxjs.matchfee.modules.charge.entity.LandPayTicket;
import org.wxjs.matchfee.modules.charge.dao.ChargeDao;

import org.wxjs.matchfee.common.utils.DateUtils;

import com.google.common.collect.Lists;

/**
 * 征收Service
 * @author GLQ
 * @version 2017-11-25
 */
@Service
@Transactional(readOnly = true)
public class ChargeService extends CrudService<ChargeDao, Charge> {
	
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

	public Charge get(String id) {
		Charge charge = super.get(id);
		//fill history charges
		charge.setHistoryCharges(this.getHistoryCharges(charge));
		//fill isOpinionBookApproved
		charge.setOpinionBookApproved(this.isOpinionBookApproved(charge));
		
		return charge;
	}
	
	public Charge get(Charge charge) {
		return this.get(charge.getId());
	}
	
	public Charge loadChargeAndSubs(Charge chargeParam) {
		Charge charge = super.get(chargeParam.getId());
		
		//OpinionBook
		OpinionBook opinionBook = new OpinionBook();
		opinionBook.setPrjNum(charge.getProject().getPrjNum());		
		charge.setOpinionBookList(opinionBookService.findList(opinionBook));
		
	    //ProjectLicense
		ProjectLicense projectLicense = new ProjectLicense();
		projectLicense.setCharge(chargeParam);
		charge.setProjectLicenseList(projectLicenseService.findList(projectLicense));
		
		//LandPayTicket
		LandPayTicket landPayTicket = new LandPayTicket();
		landPayTicket.setPrjNum(charge.getProject().getPrjNum());
		charge.setLandPayTicketList(landPayTicketService.findList(landPayTicket));
			
		//DeductionDoc
		DeductionDoc deductionDoc = new DeductionDoc();
		deductionDoc.setCharge(chargeParam);
		charge.setDeductionDocList(deductionDocService.findList(deductionDoc));
			
		//ProjectDeduction
		ProjectDeduction projectDeduction = new ProjectDeduction();
		projectDeduction.setCharge(charge);
		charge.setProjectDeductionList(projectDeductionService.findList(projectDeduction));
			
		//PayTicket	
		PayTicket payTicket = new PayTicket();
		payTicket.setCharge(chargeParam);
		charge.setPayTicketList(payTicketService.findList(payTicket));
		
		return charge;
	}
	
	public List<Charge> findList(Charge charge) {
		return super.findList(charge);
	}
	
	public Page<Charge> findPage(Page<Charge> page, Charge charge) {
		return super.findPage(page, charge);
	}
	
	public List<Charge> getHistoryCharges(Charge charge) {
		return dao.getHistoryCharges(charge);
	}
	
	@Transactional(readOnly = false)
	public void save(Charge charge) {
		super.save(charge);
	}
	
	@Transactional(readOnly = false)
	public void updateReport(Charge charge) {
		dao.updateReport(charge);
	}
	
	@Transactional(readOnly = false)
	public void updateCalculate(Charge charge) {
		dao.updateCalculate(charge);
	}
	
	@Transactional(readOnly = false)
	public void updateApprove(Charge charge) {
		dao.updateApprove(charge);
	}
	
	@Transactional(readOnly = false)
	public void updateConfirm(Charge charge) {
		dao.updateConfirm(charge);
	}
	
	@Transactional(readOnly = false)
	public void updateStatus(Charge charge) {
		dao.updateStatus(charge);
	}
	
	@Transactional(readOnly = false)
	public void delete(Charge charge) {
		
		//get all subs
		Charge chargeWithSubs = this.loadChargeAndSubs(charge);
		
		super.delete(charge);
		
		//delete subs
	    //ProjectLicense
		for(ProjectLicense item : chargeWithSubs.getProjectLicenseList()){
			projectLicenseService.deleteOnly(item);
		}
			
		//DeductionDoc
		for(DeductionDoc item : chargeWithSubs.getDeductionDocList()){
			deductionDocService.delete(item);
		}

		//ProjectDeduction
		for(ProjectDeduction item : chargeWithSubs.getProjectDeductionList()){
			projectDeductionService.deleteOnly(item);
		}			
			
		//PayTicket		
		for(PayTicket item : chargeWithSubs.getPayTicketList()){
			payTicketService.delete(item);
		}
	}
	
	@Transactional(readOnly = true)
	public SettlementList settle(String chargeId) {
		SettlementList settlementList = new SettlementList();
		
		
		double calMoney = 0; //结算金额
		double payMoney = 0; //缴费金额
		
		Charge charge = this.get(chargeId);
		
		//calculate previous remain, previous land pay money
		
		String prjNum = charge.getProject().getPrjNum();
		Charge chargeParam = new Charge();
		Project project = new Project();
		project.setPrjNum(prjNum);
		chargeParam.setProject(project);
		List<Charge> charges = this.findList(chargeParam);
		
		StringBuffer landPayMoneyHistory = new StringBuffer();
		double landPayMoneyTotal = 0;
		
		int temp = 0;
		for(Charge item: charges){
			int itemId = Util.getInteger(item.getId());
			if(itemId > temp && itemId < Util.getInteger(chargeId)){
				temp = itemId;
				charge.setPreviousRemain(item.getMoneyGap());
			}
			
			double landPayMoney = Util.getDouble(item.getLandPayMoney());
			
			if(itemId <= Util.getInteger(chargeId) && landPayMoney > 0){
				landPayMoneyHistory.append("征收"+itemId)
				.append(", 抵扣 ")
				.append(Util.formatMoneyArea(item.getLandPayMoney()) +"元<br>");
				landPayMoneyTotal += landPayMoney;
			}
		}
		
		landPayMoneyHistory.append("至本期累计已抵扣"+Util.formatMoneyArea(landPayMoneyTotal)+"元");
		
		calMoney -= charge.getPreviousRemain();
		
		settlementList.setCharge(charge);
		
		//OpinionBookItem
		OpinionBookItem opinionBookItem = new OpinionBookItem();
		OpinionBook opinionBook = new OpinionBook();
		opinionBook.setPrjNum(charge.getProject().getPrjNum());
		opinionBookItem.setDoc(opinionBook);
		
		settlementList.setOpinionBookItems(this.opinionBookItemService.findList(opinionBookItem));
		
		Map<String, String> opinionBookItemMap = new HashMap<String, String>();
		for(OpinionBookItem item : settlementList.getOpinionBookItems()){
			opinionBookItemMap.put(item.getItem().getId(), item.getArea());
		}
		
		double totalArea = 0;
		double totalUpArea = 0;
		double totalDownArea = 0;
		double totalMoney = 0;
		
		//ProjectLicense
		ProjectLicense projectLicense = new ProjectLicense();
		projectLicense.setCharge(charge);
		settlementList.setProjectLicenses(this.projectLicenseService.findList(projectLicense));
		
		totalUpArea = 0;
		totalDownArea = 0;
		
		for(ProjectLicense item : settlementList.getProjectLicenses()){
			calMoney += item.getTotalMoney();
			//item.setRemarks("规划许可证号: "+item.getDocumentNo());
			
			item.setDescription("规划许可证号: "+item.getDocumentNo());
			
			totalUpArea += Util.getDouble(item.getUpArea());
			totalDownArea += Util.getDouble(item.getDownArea());
		}
		
		if(settlementList.getProjectLicenses()!=null && settlementList.getProjectLicenses().size()>1){
			projectLicense = new ProjectLicense();
			projectLicense.setName("<小计>");
			projectLicense.setUpArea(totalUpArea + "");
			projectLicense.setDownArea(totalDownArea + "");
			settlementList.getProjectLicenses().add(projectLicense);
		}
		
		//LandPayTicket
		LandPayTicket landPayTicket = new LandPayTicket();
		landPayTicket.setPrjNum(prjNum);
		List<LandPayTicket> landPayTickets = landPayTicketService.findList(landPayTicket);
		
		StringBuffer lptDescription = new StringBuffer();
		StringBuffer lptRemarks = new StringBuffer();
		double lptTotal = 0;
		for(LandPayTicket item : landPayTickets){
			lptTotal += Util.getDouble(item.getMoney());
			lptDescription.append("金额（元）： " +Util.formatMoneyArea(item.getMoney()));
			lptDescription.append(", 票据号： " +item.getTicketNo());
			lptDescription.append("<br>");
			
			lptRemarks.append(item.getRemarks()).append("<br>");
		}
		lptDescription.append("合计已缴费金额（元）：");
		lptDescription.append(Util.formatMoneyArea(lptTotal));
		lptDescription.append("<br>");
		
		//deducted previously
		lptDescription.append(landPayMoneyHistory);
		
		landPayTicket.setName("国土已缴费抵扣");
		landPayTicket.setMoney(charge.getLandPayMoney());

		landPayTicket.setDescription(lptDescription.toString());
		landPayTicket.setRemarks(lptRemarks.toString());
		
		List<LandPayTicket> landPayTicketList = Lists.newArrayList();
		landPayTicketList.add(landPayTicket);
		
		settlementList.setLandPayTickets(landPayTicketList);
		
		calMoney -= Util.getDouble(charge.getLandPayMoney());
		
		//DeductionDocItem
		DeductionDocItem deductionDocItem = new DeductionDocItem();
		
		DeductionDoc deductionDoc = new DeductionDoc();
		deductionDoc.setCharge(charge);
		deductionDoc.setPrjNum(charge.getProject().getPrjNum());
		deductionDocItem.setDoc(deductionDoc);
		
		settlementList.setDeductionDocItems(this.deductionDocItemService.findList(deductionDocItem));

		//get settled item till this charge
		List<DeductionDocItem> settledItemList = this.deductionDocItemService.sumDeductions(deductionDocItem);
		Map<String, String> settledItemMap = new HashMap<String, String>();
		for(DeductionDocItem item : settledItemList){
			settledItemMap.put(item.getItem().getId(), item.getArea());
		}
		
		totalArea = 0;
		totalMoney = 0;
		
		for(DeductionDocItem item : settlementList.getDeductionDocItems()){
			
			calMoney -= Util.getDouble(item.getMoney());
			
			String opinionBookValue = opinionBookItemMap.get(item.getItem().getId());
			if(opinionBookValue == null){
				opinionBookValue = "无";
			}else{
				opinionBookValue = opinionBookValue + "平米";
			}
			String settledValue = settledItemMap.get(item.getItem().getId());
			if(settledValue == null){
				settledValue = "0平米";
			}else{
				settledValue = settledValue + "平米";
			}			
			StringBuilder sb = new StringBuilder();
			sb.append("意见书总面积: ").append(opinionBookValue);
			sb.append("，至本期共抵扣: ").append(settledValue);
			
			//item.setRemarks(sb.toString());
			item.setDescription(sb.toString());
			
			totalArea += Util.getDouble(item.getArea());
			totalMoney += Util.getDouble(item.getMoney());
		}
		
		if(settlementList.getDeductionDocItems()!=null && settlementList.getDeductionDocItems().size() > 1){
			deductionDocItem = new DeductionDocItem();
			DeductionItem deductionItem = new DeductionItem();
			deductionItem.setName("<小计>");
			deductionDocItem.setItem(deductionItem);
			deductionDocItem.setArea(totalArea + "");
			deductionDocItem.setMoney(totalMoney + "");
			
			settlementList.getDeductionDocItems().add(deductionDocItem);
		}
		
		//ProjectDeduction
		ProjectDeduction projectDeduction = new ProjectDeduction();
		projectDeduction.setCharge(charge);
		
		settlementList.setProjectDeductions(this.projectDeductionService.findList(projectDeduction));
		
		totalArea = 0;
		totalMoney = 0;
		
		for(ProjectDeduction item : settlementList.getProjectDeductions()){
			calMoney -= Util.getDouble(item.getMoney());
			
			totalArea += Util.getDouble(item.getArea());
			totalMoney += Util.getDouble(item.getMoney());
			
			item.setDescription("文件编号：" + item.getDocumentNo());
		}
		
		if(settlementList.getProjectDeductions()!=null && settlementList.getProjectDeductions().size()>1){
			projectDeduction = new ProjectDeduction();
			projectDeduction.setName("<小计>");
			projectDeduction.setArea(totalArea + "");
			projectDeduction.setMoney(totalMoney + "");
			settlementList.getProjectDeductions().add(projectDeduction);
		}
		
		
		//PayTicket
		PayTicket payTicket = new PayTicket();
		payTicket.setCharge(charge);
		
		settlementList.setPayTickets(this.payTicketService.findList(payTicket));
		
		for(PayTicket item : settlementList.getPayTickets()){
			payMoney += Util.getDouble(item.getMoney());
			//item.setRemarks("票据号: "+item.getTicketNo()+", 缴费日期: "+DateUtil.formatDate(item.getPayDate(), "yyyy-MM-dd"));
			item.setDescription("票据号: "+item.getTicketNo()+", 缴费日期: "+DateUtils.formatDate(item.getPayDate(), "yyyy-MM-dd"));
		}
		
		//已有结果，为避免计算精度导致差异，直接用原来的值
		//settlementList.getCharge().setCalMoney(calMoney + "");
		//settlementList.getCharge().setPayMoney(payMoney + "");
		
		return settlementList;
	}
	
	public boolean isOpinionBookApproved(Charge charge){
		boolean flag = false;
		
		Charge chargeParam = new Charge();
		Project project = new Project();
		project.setPrjNum(charge.getProject().getPrjNum());
		chargeParam.setProject(project);
		
		List<Charge> list = dao.findList(chargeParam);
		
		for(Charge entity : list){
			if(entity.getStatus().compareTo("30") >=0){
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	public List<Charge> monthlyReport(Charge charge){
		List<Charge> list = dao.monthlyReport(charge);
		
		this.addSum(list);
		
		return list;
	}
	
	public List<Charge> yearlyReport(Charge charge){
		List<Charge> list = dao.yearlyReport(charge);
		
		this.addSum(list);
		
		return list;
	}
	
	private void addSum(List<Charge> list){
		double totalCal = 0;
		double totalPay = 0;
		int seq = 0;
		for(Charge entity : list){
			totalCal += Util.getDouble(entity.getCalMoney());
			totalPay += Util.getDouble(entity.getPayMoney());
			seq ++;
			entity.setSeq(seq + "");
		}
		Charge entity = new Charge();
		entity.setReportEntity("小计");
		entity.setCalMoney(Util.getString(totalCal));
		entity.setPayMoney(Util.getString(totalPay));
		list.add(entity);
	}
	
	
	
}