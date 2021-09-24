<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri = "http://www.springframework.org/tags/form" prefix = "form"%>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>
<!DOCTYPE html>
<html>
<c:if test="${scope==null}">
	<head>
<title><spring:message code="header.export"/></title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/maintenanceOperations.js" />
	</c:import>
	</head>
</c:if>
<!--   <script>$(function(){$(".datepicker").datepicker();});</script> -->
<body class="bg-body">

<form
	spellcheck="false"
	action="selectExport.action"
	id="selectExport"
	method="post"
	enctype="multipart/form-data"	
	accept-charset="UTF-8"
>
	<c:import url="tiles/header.jsp">
		<c:param name="currentPage" value="operations" />
	</c:import>


	<div class="container-fluid">

		<div class="row">
			<div class="col-md-12">
				<div class="row">
					<div class="col-md">
						<div id="viewOperations">
							<button
								id="viewOperations.generateErrorMessageInLogs"
								class="btn btn-secondary btn-sm"
								type="submit"
								doAction="generateErrorMessageInLogsOperations"
								scope="viewOperations;"
								value="<spring:message code="gui.button.generateErrorMessageInLogs"/>"
							><span class="fa fa-eye-open">&nbsp;</span><spring:message code="gui.button.generateErrorMessageInLogs"/></button>
                        </div>
                        <div id="viewOperations">
                            <form:form method="POST" action="/deleteLastImportRequestOperations" modelAttribute="deleteRequest">
                                <button
                                    id="viewOperations.deleteLastImportRequest"
                                    class="btn btn-secondary btn-sm"
                                    type="submit"
                                    doAction="deleteLastImportRequestOperations"
                                    scope="viewOperations;"
                                    value="<spring:message code="gui.button.deleteLastImportRequest"/>"
                                ><span class="fa fa-eye-open">&nbsp;</span><spring:message code="gui.button.deleteLastImportRequest"/></button>
                                <spring:message code="label.client.software"/>
                                <form:select path = "ihmClient">
                                    <form:option value = "NONE" label = "Select"/>
                                    <form:options items = "${ihmClients}" />
                                </form:select>   
                                <spring:message code="label.import.date.low"/><input type="date" class="datepicker" id="lowDate">
                                <spring:message code="label.import.date.high"/><input type="date" class="datepicker" id="highDate">
                            </form:form>
						</div>
					</div>
				</div>
			</div>			
		</div>
	</div>	
</form>
	

</body>
</html>