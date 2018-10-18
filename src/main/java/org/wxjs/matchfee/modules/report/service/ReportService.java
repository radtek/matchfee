package org.wxjs.matchfee.modules.report.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.wxjs.matchfee.common.config.Global;
import org.wxjs.matchfee.common.persistence.Page;
import org.wxjs.matchfee.common.service.BaseService;
import org.wxjs.matchfee.common.utils.DateUtils;
import org.wxjs.matchfee.common.utils.Util;
import org.wxjs.matchfee.modules.charge.entity.Charge;
import org.wxjs.matchfee.modules.report.dao.ChargeReportDao;
import org.wxjs.matchfee.modules.report.dao.ReportDao;
import org.wxjs.matchfee.modules.report.dataModel.ChartData;
import org.wxjs.matchfee.modules.report.dataModel.ColumnData;
import org.wxjs.matchfee.modules.report.dataModel.PieData;
import org.wxjs.matchfee.modules.report.dataModel.ReportData;
import org.wxjs.matchfee.modules.report.dataModel.TableColModel;
import org.wxjs.matchfee.modules.report.dataModel.TableData;
import org.wxjs.matchfee.modules.report.entity.ReportEntity;
import org.wxjs.matchfee.modules.report.entity.ReportParam;
import org.wxjs.matchfee.modules.report.entity.TaxProtectReport;

import com.google.common.collect.Lists;



@Service
@Transactional(readOnly = true)
public class ReportService extends BaseService{
	
	@Autowired
	public ReportDao reportDao;
	
	public static enum reportTypes {monthTimes, yearTimes};
	
	public static final String columnReport = "columnReport";
	
	public static final String monthReport = "monthReport";
	
	public static final String pieReport = "pieReport";
    
    protected static HashMap<String, String> ChartTypeMap = new HashMap<String, String>();
    
    static{
    	ChartTypeMap.put(reportTypes.monthTimes.toString(), columnReport);
    	ChartTypeMap.put(reportTypes.yearTimes.toString(), columnReport);
    }
    
    @Autowired
    ChargeReportDao chargeReportDao;
  
    
    /**
     * not get data from table in this method
     * @param param
     * @return
     */
    public ReportData getReportPre(ReportParam param) {
    	String title = "";
    	String label = "";
    	String reportType = param.getReportType();
		if(reportType.equalsIgnoreCase(reportTypes.monthTimes.toString())){
			title = "MONTH_TIMES_REPORT";
			label = "MONTH";
		}else if(reportType.equalsIgnoreCase(reportTypes.yearTimes.toString())){
			title = "YEAR_TIMES_REPORT";
			label = "YEAR";
		}
		
		String chartType = ChartTypeMap.get(reportType);
		
		return new ReportData(Global.getConfig(title), chartType, null, label, null);
    }
    
    public ReportData getReport(ReportParam param) {
		
		Collection<HashMap<String, Object>> col = null;
		
		col = chargeReportDao.report(param);

		//sort by value
		/*
		Collections.sort((List<HashMap<String, Object>>)col, new Comparator<Object>(){
			public int compare(Object o1, Object o2) {
				int rst = 0;
				HashMap<String, Object> hm1 = (HashMap<String, Object>)o1;
				HashMap<String, Object> hm2 = (HashMap<String, Object>)o2;
				float v1 = Util.getFloat(hm1.get("ITEMVALUE"));
				float v2 = Util.getFloat(hm2.get("ITEMVALUE"));
				if(v1>v2){
					rst = -1;
				}else{
					rst = 1;
				}
				return rst;
			}
		});
		*/
		
		ReportData rst = getReportPre(param);
		
		ChartData chartData = null;
		if(rst.getChartType().equals(pieReport)){
			chartData = new PieData();

			chartData.setTitle("");
			chartData.loadData(col,  Global.getConfig(rst.getTableLabel()));			
		}else{
			chartData = new ColumnData();
			
			((ColumnData)chartData).setyTitle(Global.getConfig(this.getyTitle(param)));
			((ColumnData)chartData).setyAxisUnit(Global.getConfig(this.getyAxisUnit(param)));
			chartData.loadData(col, Global.getConfig(rst.getTableLabel()));
		}

		
		TableData tableData = new TableData();
		
		tableData.setColModels(this.getColModels(param, rst.getTableLabel()));
		
		tableData.loadData(col);
		
		rst.setChartData(chartData);
		
		rst.setTableData(tableData);
		
		logger.debug("model:"+tableData.getJsonColModel()+", model"+tableData.getJsonData());
		
		return rst;
	}
    
    public List<ReportEntity> getMonthCountMoneyReport(ReportParam param) {

    	List<ReportEntity> list = reportDao.monthCountMoneyReport(param);
    	
    	int totalCount = 0;
    	double totalMoney = 0;
    	for(ReportEntity entity : list){
    		totalCount = totalCount + entity.getCount();
    		totalMoney += entity.getMoney();
    	}
    	ReportEntity entity = new ReportEntity();
    	entity.setMonth("小计");
    	entity.setCount(totalCount);
    	entity.setMoney(totalMoney);
    	
    	list.add(entity);
    	
    	return list;
	}
    
    public List<ReportEntity> getYearCountMoneyReport(ReportParam param) {

    	List<ReportEntity> list = reportDao.yearCountMoneyReport(param);
    	
    	int totalCount = 0;
    	double totalMoney = 0;
    	for(ReportEntity entity : list){
    		totalCount = totalCount + entity.getCount();
    		totalMoney += entity.getMoney();
    	}
    	ReportEntity entity = new ReportEntity();
    	entity.setYear("小计");
    	entity.setCount(totalCount);
    	entity.setMoney(totalMoney);
    	
    	list.add(entity);
    	
    	return list;
	}
	
	protected List<TableColModel> getColModels(ReportParam param, String firstColumnTitle){
		List<TableColModel> colModels = new ArrayList<TableColModel>();
		
		colModels.add(new TableColModel(Global.getConfig(firstColumnTitle), "ITEMNAME", 40));
		colModels.add(new TableColModel(Global.getConfig(this.getyTitle(param)), "ITEMVALUE", 30));
		colModels.add(new TableColModel(Global.getConfig("PERCENT"), "PERCENT", 30));		
		
		return colModels;
	}
	
	protected String getyTitle(ReportParam param){
		String rst = "";
		if(param.getReportType().equalsIgnoreCase(reportTypes.monthTimes.toString())){
			rst = "REPORT_TIMES";
		}else if(param.getReportType().equalsIgnoreCase(reportTypes.yearTimes.toString())){
			rst = "REPORT_TIMES";
		}
		return rst;
	}
	
	protected String getyAxisUnit(ReportParam param){
		String rst = "";
		if(param.getReportType().equalsIgnoreCase(reportTypes.monthTimes.toString())){
			rst = "UNIT_TIMES";
		}else if(param.getReportType().equalsIgnoreCase(reportTypes.yearTimes.toString())){
			rst = "UNIT_TIMES";
		}
		return rst;
	}
	
	public Collection<HashMap<String, Object>> dashboardDeclare(){
		Collection<HashMap<String, Object>> map=reportDao.dashboardDeclare();
		return map;
	}
	
	public Collection<HashMap<String, Object>> dashboardChargeMoney(){
		Collection<HashMap<String, Object>> map=reportDao.dashboardChargeMoney();
		return map;
	}
	
	public Collection<HashMap<String, Object>> dashboardChargeStatus(){
		Collection<HashMap<String, Object>> map=reportDao.dashboardChargeStatus();
		return map;
	}
	
	public List<TaxProtectReport> taxProtectReport(ReportParam param){
		List<TaxProtectReport> list = reportDao.taxProtect(param);
		//append sequence
		int i = 0;
		for(TaxProtectReport item: list){
			i++;
			item.setSeq(i+"");
		}
		
		return list;
	}
	
	public List<Charge> monthlyReport(Charge charge){
		return null;
	}
	
	public List<Charge> yearlyReport(Charge charge){
		return null;
	}
	
}
