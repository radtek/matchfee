<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>征收管理</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			
		});
		
	    function toNewOpinonBook(){
		       window.location.replace("${ctx}/charge/opinionBook/form?prjNum=${charge.project.prjNum}");
	    }
	    
	    function toNewItem(docId){
		   window.location.replace("${ctx}/charge/opinionBookItem/form?doc.id="+docId);
	    }		
	</script>
	
	<style type="text/css">
		.docHint {margin: 10px 0 10px 20px;font-size: 16px;font-weight:bold;}
	</style>
	
</head>
<body>
    <sys:message content="${message}"/>
    <legend>配套费征收</legend>	
    <matchfee:chargeViewWithButtons charge="${charge}"></matchfee:chargeViewWithButtons><br>

	<matchfee:chargeTabController tab="2"></matchfee:chargeTabController>
	
	<div class="docHint">*住宅项目请上传无锡市项目建设条件意见书，非住宅项目不需上传。</div>

<c:if test="${empty charge.opinionBookList}">
  
	<div style="margin:10px 60px 10px 0;width:100%">
	   <div align="right">
		    <shiro:hasPermission name="charge:charge:edit">
		    <c:if test="${charge.status lt '20' || (charge.status ge '20' && fns:getUser().isShy)}">
		    <input id="btnAdd" class="btn btn-primary" type="button" value="添加文件" onclick="toNewOpinonBook()"/>
		    </c:if>
		    </shiro:hasPermission>	   
	   </div>
	</div>    
    
</c:if>


<c:if test="${not empty charge.opinionBookList}">
	
	<c:forEach items="${charge.opinionBookList}" var="opinionBook">
	
	<div>

		<legend>条件意见书</legend>
		<div style="margin:10px 60px 10px 0;text-align:right">
			<shiro:hasPermission name="charge:charge:edit">
			<c:if test="${(not charge.opinionBookApproved) || (charge.opinionBookApproved && fns:getUser().isShy)}">
		 		<a href="${ctx}/charge/opinionBook/form?id=${opinionBook.id}">修改</a>
				<a href="${ctx}/charge/opinionBook/delete?id=${opinionBook.id}" onclick="return confirmx('确认要删除该条件意见书吗？', this.href)">删除</a>
			</c:if>
			</shiro:hasPermission>					
		</div>	
		
		<matchfee:opinionBookView opinionBook="${opinionBook}"></matchfee:opinionBookView>
	    
	   <c:choose>
	      <c:when test="${(not charge.opinionBookApproved) || (charge.opinionBookApproved && fns:getUser().isShy)}">
			<div style="margin:10px 60px 10px 0;text-align:right">
				<input id="btnAddItem" class="btn btn-primary" type="button" value="添加项目" onclick="toNewItem(${opinionBook.id})"/>
			</div>	      
		    <matchfee:opinionBookItemView opinionBook="${opinionBook}" withOperation="1"></matchfee:opinionBookItemView>
  		  </c:when>			   			  	   			  		   			  
  		  <c:otherwise>
            <matchfee:opinionBookItemView opinionBook="${opinionBook}" withOperation="0"></matchfee:opinionBookItemView>  		  
  		  </c:otherwise>		   			  
	   </c:choose>	    
	    
<!--  
	  <table style="text-align:top;width:100%" cellspacing="10">
	    <tr>
	      <td style="text-align:top;width:49%">
			<div style="margin:10px 60px 10px 0;width:100%">
			   <div align="right"><input id="btnAddItem" class="btn btn-primary" type="button" value="添加项目" onclick="toNewItem(${opinionBook.id})"/></div>
			</div>	      
	      <matchfee:opinionBookItemView opinionBook="${opinionBook}" withOperation="1"></matchfee:opinionBookItemView>
	      </td>
	      <td width="1%"></td>
	      <td width="50%">
	      <embed width="800" height="600" src="${opinionBook.path}"> </embed>
	      </td>
	    </tr>
	  </table>
-->	
    </div>
	</c:forEach>		

</c:if>

<matchfee:logListView chargeId="${charge.id}"></matchfee:logListView>
    
</body>
</html>