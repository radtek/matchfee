/**
 * Copyright &copy; 2012-2016 千里目 All rights reserved.
 */
package org.wxjs.matchfee.modules.base.utils;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.wxjs.matchfee.common.config.Global;
import org.wxjs.matchfee.common.utils.Encodes;
import org.wxjs.matchfee.common.utils.PdfUtil;
import org.wxjs.matchfee.common.utils.Util;
import org.wxjs.matchfee.modules.charge.entity.ProjectLicense;
import org.wxjs.matchfee.modules.charge.entity.DeductionDocItem;
import org.wxjs.matchfee.modules.charge.entity.ProjectDeduction;
import org.wxjs.matchfee.modules.charge.entity.PayTicket;
import org.wxjs.matchfee.modules.charge.entity.SettlementList;
import org.wxjs.matchfee.modules.charge.entity.LandPayTicket;

public class ExportSettlementList {
	
	private static Logger log = LoggerFactory.getLogger(ExportSettlementList.class);
	
	private final static int tableWidth = 90;
	
	final static float[] widths = new float[]{0.25f, 0.12f, 0.16f ,0.24f ,0.23f};
	
	final static float[] widthsGap = new float[]{0.25f, 0.12f, 0.16f ,0.47f};
	
	private SettlementList settlementList;
	
	public ExportSettlementList(SettlementList settlementList){
		this.settlementList = settlementList;
	}
	
	private void generate(OutputStream os) throws DocumentException{
		
		Document document = null;
		PdfWriter writer = null;
		
		PdfPTable table = null;
        Phrase phrase = null;
        
        Paragraph pragraph = null;
        
    	phrase = new Phrase("");
        PdfPCell cell_pending = new PdfPCell(phrase);
        cell_pending.setBorderWidth(0);
        
		try{
			document = new Document(PageSize.A4);
			
			writer = PdfWriter.getInstance(document, os);
			
            document.open();
            
            //add title
            
            pragraph = new Paragraph("无锡市城市基础设施配套费结算清单", PdfUtil.getTitle1Font(true));
            pragraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(pragraph);
            
            document.add(PdfUtil.generateTable4Padding());

//            pragraph = new Paragraph("申报信息", PdfUtil.getTitle3Font(false));
//            pragraph.setAlignment(Paragraph.ALIGN_LEFT);
//            document.add(pragraph);
//            
//            document.add(PdfUtil.generateTable4Padding());
            
            //add table
			table = this.getChargeInfoTable();
			document.add(table);
            
            document.add(PdfUtil.generateTable4Padding());
            
            String[] headers = new String[]{"条目","面积\n（平米）","金额\n（元）","说明","备注"};
    		
            
            List<String[]> items = new ArrayList<String[]>();
			
			table = PdfUtil.generateTable(headers, PdfUtil.getTextFont(true), null, PdfUtil.getTextFont(true), widths, tableWidth, true, 1);
			document.add(table);	
			
			//上期待清算金额（元） 
	        table = new PdfPTable(5);
	        table.setWidths(widths);
	        table.setWidthPercentage(tableWidth);
	        
	        PdfPCell cell;
	        
        	phrase = new Phrase("*上期待清算金额（元）", PdfUtil.getTextFont(true));
        	cell = new PdfPCell(phrase);
        	cell.setBorderWidth(1);
        	cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        	table.addCell(cell);
        	
        	phrase = new Phrase("", PdfUtil.getTextFont(true));
        	PdfPCell emptyCell = new PdfPCell(phrase);
        	emptyCell.setBorderWidth(1);
        	emptyCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        	table.addCell(emptyCell);       
        	
        	phrase = new Phrase(settlementList.getCharge().getPreviousRemainDisplay(), PdfUtil.getTextFont(false));
        	cell = new PdfPCell(phrase);
        	cell.setBorderWidth(1);
        	cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        	table.addCell(cell);   
        	
        	table.addCell(emptyCell); 
        	table.addCell(emptyCell); 
			
			document.add(table);			
			
			table = PdfUtil.generateTable(new String[]{"*结算项目"}, PdfUtil.getTextFont(true), null, PdfUtil.getTextFont(false), new float[]{1f}, tableWidth);
			document.add(table);
			
			items.clear();
			for(ProjectLicense item : settlementList.getProjectLicenses()){
				items.add(new String[]{item.getName(), 
						Util.formatMoneyArea(item.getTotalArea()),
						Util.formatMoneyArea(item.getTotalMoney()),
						item.getDescription4Plain(), 
						item.getRemarks4Plain()});
			}
			
			table = PdfUtil.generateTable(null, PdfUtil.getTextFont(true), items, PdfUtil.getTextFont(false), widths, tableWidth);
			document.add(table);
			
			
			table = PdfUtil.generateTable(new String[]{"*抵扣项(设计院证明)"}, PdfUtil.getTextFont(true), null, PdfUtil.getTextFont(false), new float[]{1f}, tableWidth);
			document.add(table);
			
			items.clear();
			for(DeductionDocItem item : settlementList.getDeductionDocItems()){
				items.add(new String[]{item.getItem().getName(), Util.formatMoneyArea(item.getArea()) +"", Util.formatMoneyArea(item.getMoney()) +"", item.getDescription4Plain(), item.getRemarks4Plain()});
			}
			
			table = PdfUtil.generateTable(null, PdfUtil.getTextFont(true), items, PdfUtil.getTextFont(false), widths, tableWidth);
			document.add(table);
			
			table = PdfUtil.generateTable(new String[]{"*国土已缴费"}, PdfUtil.getTextFont(true), null, PdfUtil.getTextFont(false), new float[]{1f}, tableWidth);
			document.add(table);
			
			items.clear();
			for(LandPayTicket item : settlementList.getLandPayTickets()){
				items.add(new String[]{item.getName(), "", Util.formatMoneyArea(item.getMoney()) +"", item.getDescription4Plain(), item.getRemarks4Plain()});
			}
			
			table = PdfUtil.generateTable(null, PdfUtil.getTextFont(true), items, PdfUtil.getTextFont(false), widths, tableWidth);
			document.add(table);			
			
			
			table = PdfUtil.generateTable(new String[]{"*其他减项"}, PdfUtil.getTextFont(true), null, PdfUtil.getTextFont(false), new float[]{1f}, tableWidth);
			document.add(table);
			
			items.clear();
			for(ProjectDeduction item : settlementList.getProjectDeductions()){
				items.add(new String[]{item.getName(), Util.formatMoneyArea(item.getArea()) +"", Util.formatMoneyArea(item.getMoney()) +"", item.getDescription4Plain(), item.getRemarks4Plain()});
			}
			
			table = PdfUtil.generateTable(null, PdfUtil.getTextFont(true), items, PdfUtil.getTextFont(false), widths, tableWidth);
			document.add(table);
			
			
			table = PdfUtil.generateTable(new String[]{"*缴费情况"}, PdfUtil.getTextFont(true), null, PdfUtil.getTextFont(false), new float[]{1f}, tableWidth);
			document.add(table);
			
			items.clear();
			items.add(new String[]{"结算金额", "", Util.formatMoneyArea(settlementList.getCharge().getCalMoney()) +"", "", ""});
			for(PayTicket item : settlementList.getPayTickets()){
				items.add(new String[]{"缴费", "", Util.formatMoneyArea(item.getMoney()) +"", item.getDescription4Plain(), item.getRemarks4Plain()});
			}
			
			table = PdfUtil.generateTable(null, PdfUtil.getTextFont(true), items, PdfUtil.getTextFont(false), widths, tableWidth);
			document.add(table);
			
			items.clear();
			String gapHint = "待清算金额";
			/*
			String gapHint = "多缴费";
			if(settlementList.getCharge().getMoneyGap()<0){ 
				gapHint = "少缴费";
			}
			*/
			String bankAccountMemo = settlementList.getCharge().getBankAccountMemo();
			items.add(new String[]{gapHint, "", settlementList.getCharge().getMoneyGapDisplay() +"", 
					               PdfUtil.toPlain(bankAccountMemo)});
			
			table = PdfUtil.generateTable(null, PdfUtil.getTextFont(true), items, PdfUtil.getTextFont(false), widthsGap, tableWidth);
			document.add(table);
			
			/*
			document.add(PdfUtil.generateTable4Padding());
			
			//add account information
			items.clear();
			items.add(new String[]{"", PdfUtil.toPlain(Global.getConfig("BANK_ACCOUNT"))});
			
			float[] widthsAcc = new float[]{0.15f, 0.85f};
			
			table = PdfUtil.generateTable(null, null, items, PdfUtil.getTextFont(true), widthsAcc, tableWidth, false, 0);
			document.add(table);
			*/
			
		}finally{
			if(document!=null){
				try{
					document.close();
				}catch(Exception ex){
				}
			}
		}
	}
	
	private PdfPTable getChargeInfoTable() throws DocumentException{
		
		float[] widths = new float[]{0.2f, 0.3f, 0.2f ,0.3f};
		
        PdfPTable table = new PdfPTable(4);
        table.setWidths(widths);
        table.setWidthPercentage(tableWidth);
        
        table.addCell(this.getLabelCell("申报代码:"));
        table.addCell(this.getContentCell(this.settlementList.getCharge().getId()));
        
        table.addCell(this.getLabelCell("申报日期:"));
        table.addCell(this.getContentCell(this.settlementList.getCharge().getReportDateYYYYMMDD()));
        
        table.addCell(this.getLabelCell("项目编号:"));
        table.addCell(this.getContentCell(this.settlementList.getCharge().getProject().getPrjNum()));
        
        table.addCell(this.getLabelCell("项目名称:"));
        table.addCell(this.getContentCell(this.settlementList.getCharge().getProject().getPrjName()));
        
        table.addCell(this.getLabelCell("建设单位代码:"));
        table.addCell(this.getContentCell(this.settlementList.getCharge().getProject().getBuildCorpCode()));
        
        table.addCell(this.getLabelCell("建设单位名称:"));
        table.addCell(this.getContentCell(this.settlementList.getCharge().getProject().getBuildCorpName()));
        
        table.addCell(this.getLabelCell("状态:"));
        table.addCell(this.getContentCell(this.settlementList.getCharge().getStatusLabel()));
        
        table.addCell(this.getLabelCell("结算金额（元）:"));
        table.addCell(this.getContentCell(Util.formatMoneyArea(this.settlementList.getCharge().getCalMoney()), Element.ALIGN_RIGHT));
        
        log.debug("this.settlementList.getCharge().getCalMoney(): "+this.settlementList.getCharge().getCalMoney());
        
        if("40".equals(this.settlementList.getCharge().getStatus())){
            table.addCell(this.getLabelCell("缴费金额（元）:"));
            table.addCell(this.getContentCell(Util.formatMoneyArea(this.settlementList.getCharge().getPayMoney())));
            
            table.addCell(this.getLabelCell("待清算金额（元）:"));
            table.addCell(this.getContentCell(Util.formatMoneyArea(this.settlementList.getCharge().getMoneyGapDisplay())));
        	
        }
        
        return table;
		
	}
	
	private PdfPCell getLabelCell(String content){
		Phrase phrase = new Phrase(content, PdfUtil.getTextFont(true));
		PdfPCell cell = new PdfPCell(phrase);
    	cell.setBorderWidth(0);
    	cell.setHorizontalAlignment(Element.ALIGN_LEFT);
    	//cell.setBackgroundColor(Color.lightGray);
    	return cell;
	}
	
	private PdfPCell getContentCell(String content){
		return this.getContentCell(content, Element.ALIGN_LEFT);
	}
	
	private PdfPCell getContentCell(String content, int align){
		Phrase phrase = new Phrase(content, PdfUtil.getTextFont(false));
		PdfPCell cell = new PdfPCell(phrase);
    	cell.setBorderWidth(0);
    	cell.setHorizontalAlignment(align);
    	return cell;
	}

	
	/**
	 * 输出到客户端
	 * @param fileName 输出文件名
	 * @throws DocumentException 
	 */
	public ExportSettlementList write(HttpServletResponse response, String fileName) throws IOException, DocumentException{
		response.reset();
        response.setContentType("application/octet-stream; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename="+Encodes.urlEncode(fileName));
		this.generate(response.getOutputStream());
		return this;
	}
	
	/**
	 * 输出到文件
	 * @param fileName 输出文件名
	 * @throws DocumentException 
	 */
	public ExportSettlementList writeFile(String name) throws FileNotFoundException, IOException, DocumentException{
		FileOutputStream os = new FileOutputStream(name);
		this.generate(os);
		return this;
	}
	


}
