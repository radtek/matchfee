<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>征收报表</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {	
			
			$("#btnExport").click(function(){
				top.$.jBox.confirm("确认要导出数据吗？","系统提示",function(v,h,f){
					if(v=="ok"){
						$("#searchForm").attr("action","${ctx}/report/report/yearlyReportExport");
						$("#searchForm").submit();
					}
				},{buttonsFocus:1});
				top.$('.jbox-body .jbox-icon').css('top','55px');
			});
			
			$("#btnSubmit").click(function(){
				$("#searchForm").attr("action","${ctx}/report/report/yearlyReport");
				$("#searchForm").submit();
			});	
			
		});	
	</script>
</head>
<body>
	<legend>征收年报</legend>
	<form:form id="searchForm" modelAttribute="reportParam" action="${ctx}/report/report/yearlyReport" method="post" class="breadcrumb form-search">		
		<ul class="ul-form">	
			<li>		
			    <label>缴费年份 ：</label>
				<input name="dateFrom" type="text" readonly="readonly" maxlength="20" class="input-medium Wdate"
					value="<fmt:formatDate value="${reportParam.dateFrom}" pattern="yyyy"/>"
					onclick="WdatePicker({dateFmt:'yyyy',isShowClear:false});"/>				
			</li>			
			<li class="btns"><input id="btnSubmit" class="btn btn-primary" type="button" value=" 查 询 "/>
			<input id="btnExport" class="btn btn-primary" type="button" value=" 导 出 "/>
			</li>
			<li class="clearfix"></li>
		</ul>		
	</form:form>	
	
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		<thead>
			<tr>
			    <th>序号</th>
			    <th>代码</th>
				<th>项目编号</th>
				<th>申报单位</th>
				<th>项目名称</th>
				<!--
				<th>项目地址</th>
				<th>申报人</th>
				-->
				<th>申报日期</th>
				<th>结算金额</th>
				<th>缴费日期</th>
				<th>缴费金额</th>
				<th width="8%">状态</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${list}" var="charge" varStatus="status">
			<tr>
			    <td>${charge.seq}</td>
				<td>
					${charge.id}
				</td>			
				<td>
					${charge.project.prjNum}
				</td>
				<td>
					${charge.reportEntity}
				</td>				
				<td>
					${charge.project.prjName}
				</td>
				<!--
				<td>
					${charge.project.prjAddress}
				</td>
				<td>
					${charge.reportStaff.name}
				</td>
				-->
				<td>
					<fmt:formatDate value="${charge.reportDate}" pattern="yyyy-MM-dd"/>
				</td>
				<td style="text-align:right">
					<fmt:formatNumber value="${charge.calMoney}" pattern="#,##0.00"/>
				</td>
				<td>
					<fmt:formatDate value="${charge.maxPayDate}" pattern="yyyy-MM-dd"/>
				</td>				
				<td style="text-align:right">
					<fmt:formatNumber value="${charge.payMoney}" pattern="#,##0.00"/>
				</td>
				<td>
					${charge.statusLabel}
				</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
</body>
</html>