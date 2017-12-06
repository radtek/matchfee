<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>工程许可证管理</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			
		});
		
	    function toNewPage(){
		       window.location.replace("${ctx}/charge/projectLicense/form");
	    }		
	</script>
</head>
<body>
    <sys:message content="${message}"/>
    <matchfee:chargeViewWithButtons charge="${charge}"></matchfee:chargeViewWithButtons><br>

	<ul class="nav nav-tabs">
		<li><a href="${ctx}/charge/charge/opinionBookTab">条件意见书</a></li>
		<li class="active"><a href="${ctx}/charge/charge/projectLicenseTab">工程许可证</a></li>
		<li><a href="${ctx}/charge/charge/deductionDocTab">设计院证明</a></li>
		<li><a href="${ctx}/charge/charge/projectDeductionTab">其他减项</a></li>
	</ul>
	
	<div style="margin:10px 60px 10px 0;width='100%'">
	   <div align="right">
		    <shiro:hasPermission name="charge:charge:edit">
		    <input id="btnAdd" class="btn btn-primary" type="button" value="添加" onclick="toNewPage()"/>
		    </shiro:hasPermission>	   
	   </div>
	</div>
	
	<sys:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		<thead>
			<tr>
				<th>名称</th>
				<th>文件编号</th>
				<th>保存路径</th>
				<th>文档日期</th>
				<th>地上面积（平米）</th>
				<th>地下面积（平米）</th>
				<shiro:hasPermission name="charge:charge:edit"><th>操作</th></shiro:hasPermission>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${charge.projectLicenseList}" var="projectLicense">
			<tr>
				<td><a href="${ctx}/charge/projectLicense/form?id=${projectLicense.id}">
					${projectLicense.name}
				</a></td>
				<td>
					${projectLicense.documentNo}
				</td>
				<td>
				<input type="hidden" id="path${projectDeduction.id}" name="path${projectDeduction.id}" value="${projectDeduction.path}">
				<sys:ckfinder input="path${projectDeduction.id}" type="files" uploadPath="/charge/projectDeduction" selectMultiple="false" readonly="true"/>					
				</td>
				<td>
					<fmt:formatDate value="${projectLicense.documentDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
				</td>
				<td>
					${projectLicense.upArea}
				</td>
				<td>
					${projectLicense.downArea}
				</td>
				<shiro:hasPermission name="charge:charge:edit"><td>
    				<a href="${ctx}/charge/projectLicense/form?id=${projectLicense.id}">修改</a>
					<a href="${ctx}/charge/projectLicense/delete?id=${projectLicense.id}" onclick="return confirmx('确认要删除该工程许可证吗？', this.href)">删除</a>
				</td></shiro:hasPermission>
			</tr>
		</c:forEach>
		</tbody>
	</table>
</body>
</html>