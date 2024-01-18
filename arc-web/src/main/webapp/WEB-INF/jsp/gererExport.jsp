<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>
<!DOCTYPE html>
<html>
<c:if test="${scope==null}">
	<head>
	<link rel="icon" href="data:,"/>
	<title><spring:message code="header.export"/></title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/gererExport.js" />
	</c:import>
	</head>
</c:if>

<body class="bg-body">

<c:import url="tiles/header.jsp">
	<c:param name="currentPage" value="export" />
</c:import>

<form
	spellcheck="false"
	action="selectExport.action"
	id="selectExport"
	method="post"
	accept-charset="UTF-8"
>
	<div class="container-fluid">
		<c:import url="/WEB-INF/jsp/tiles/template_environment.jsp"></c:import>
		<div class="row">
				<div class="col-md-12 border-right">
					<div class="row">
						<div class="col-md">
							<!-- norm list -->
							<c:set var="view" value="${viewExport}"  scope="request"/>
							<c:import url="tiles/templateVObject.jsp">
								<c:param name="btnSelect" value ="true" />
								<c:param name="btnSee" value ="true" />
								<c:param name="btnSort" value ="true" />
								<c:param name="btnAdd" value ="true" />
								<c:param name="btnUpdate" value ="true" />
								<c:param name="btnDelete" value ="true" />
								<c:param name="ligneAdd" value ="true" />
								<c:param name="ligneFilter" value ="true" />
								<c:param name="checkbox" value ="true" />
								<c:param name="checkboxVisible" value ="true" />
								<c:param name="extraScopeAdd" value ="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" />
								<c:param name="extraScopeDelete" value ="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" />
								<c:param name="extraScopeUpdate" value ="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" />
								<c:param name="extraScopeSee" value ="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" />
								<c:param name="multiSelection" value ="true" />
								<c:param name="otherButton">
									<input 
										class="btn btn-primary btn-sm"
										id="viewExport.start"
										type="submit"
										doAction="startExport"
										scope="viewExport;viewFileExport;"
										value="<spring:message code="gui.button.exportStart"/>"></input>
								</c:param>
							</c:import>
						</div>
					</div>
			</div>			
		</div>
		<div class="row">
		<div class="col-md-12 border-right" style="margin-top: 2.25rem;"
				>
					<div class="row">
						<div class="col-md">
							<!-- norm list -->
							<c:set var="view" value="${viewFileExport}"  scope="request"/>
							<c:import url="tiles/templateVObject.jsp">
								<c:param name="btnSelect" value ="true" />
								<c:param name="btnSee" value ="true" />
								<c:param name="btnSort" value ="true" />
								<c:param name="btnAdd" value ="true" />
								<c:param name="btnUpdate" value ="true" />
								<c:param name="btnDelete" value ="true" />
								<c:param name="ligneAdd" value ="true" />
								<c:param name="ligneFilter" value ="true" />
								<c:param name="checkbox" value ="true" />
								<c:param name="checkboxVisible" value ="true" />
								<c:param name="extraScopeAdd" value ="viewSchemaNmcl;" />
								<c:param name="extraScopeDelete" value ="viewSchemaNmcl;" />
								<c:param name="extraScopeUpdate" value ="viewSchemaNmcl;" />
								<c:param name="extraScopeSee" value ="viewSchemaNmcl;" />
								<c:param name="multiSelection" value ="true" />
								<c:param name="otherButton">
									<button
										class="btn btn-primary btn-sm"
										id="viewviewFileExport.download"
										type="submit"
										doAction="downloadFileExport"
										ajax="false"
									><span class="fa fa-download">&nbsp;</span> <spring:message code="gui.button.downloadFileExport"/></button>
								</c:param>
							</c:import>
						</div>
					</div>
			</div>
		</div>
	</div>	
</form>
	

</body>
</html>