<%@ page contentType="text/html;charset=UTF-8" %>

<%@ include file="/WEB-INF/views/include/taglib.jsp"%>

<%@ taglib prefix="report" tagdir="/WEB-INF/tags/report" %>

<html>
<head>
	<title>征收统计报表</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {		
		});
	</script>
</head>
<body>

<table width="100%" align="center" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td width="100%" height="30" class="title" align="left"><strong>已缴费按月统计</strong><br>
    </td> 
  </tr>
  <tr>
    <td>
	<form:form id="searchForm" modelAttribute="reportParam" action="${ctx}/report/report/monthCountMoneyReport" method="post" class="breadcrumb form-search">
        <form:hidden path="reportType"/>
		<ul class="ul-form">
			<li>				
			    <label>月份 ：</label>
				<input name="dateFrom" type="text" readonly="readonly" maxlength="20" class="input-medium Wdate"
					value="<fmt:formatDate value="${reportParam.dateFrom}" pattern="yyyy-MM"/>"
					onclick="WdatePicker({dateFmt:'yyyy-MM',isShowClear:false});"/>
				<label>到：</label>
				<input name="dateTo" type="text" readonly="readonly" maxlength="20" class="input-medium Wdate"
					value="<fmt:formatDate value="${reportParam.dateTo}" pattern="yyyy-MM"/>"
					onclick="WdatePicker({dateFmt:'yyyy-MM',isShowClear:false});"/>
			</li>			
			<li class="btns"><input id="btnSubmit" class="btn btn-primary" type="submit" value=" 统 计 "/>
			
			</li>
			<li class="clearfix"></li>
		</ul>		
	</form:form>    
    
    </td>
  </tr>  
  <tr>
    <td>
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed" style="width:60%">
		<thead>
			<tr>
				<th>月份</th>
				<th>项目数</th>
				<th>征收金额（元）</th>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${list}" var="entity">
			<tr>
				<td>
				    ${entity.month}
				</td>
				<td>
					${entity.count}
				</td>
				<td style="text-align:right">
					<fmt:formatNumber value="${entity.money}" pattern="#,##0.00"/>
				</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
		
    </td>
  </tr> 
</table>


</body>
</html>