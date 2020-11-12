<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>
<!DOCTYPE html>
<html>
<c:if test="${scope==null}">
	<head>
<title><spring:message code="header.export"/></title>
<link
	rel="stylesheet"
	href="<c:url value='/css/bootstrap.min.css'/>"
/>
<link
	rel="stylesheet"
	type="text/css"
	href="<c:url value='/css/style.css' />"
/>
<link
	href="<c:url value='/css/font-awesome.min.css'/>"
	rel="stylesheet"
/>
<script
	type="text/javascript"
	src="<c:url value='/js/jquery-2.1.3.min.js'/>"
></script>

<script	src="<c:url value='/js/lib/popper.min.js'/>" ></script>
<script	src="<c:url value='/js/lib/bootstrap.min.js'/>"></script>

<script
	type="text/javascript"
	src="<c:url value='/js/arc.js'/>"
></script>
<script
	type="text/javascript"
	src="<c:url value='/js/gererExport.js'/>"
></script>
<script
	type="text/javascript"
	src="<c:url value='/js/component.js'/>"
></script>
	</head>
</c:if>
<body class='bg-light'>

<form
	spellcheck="false"
	action="selectExport.action"
	id="selectExport"
	method="post"
	enctype="multipart/form-data"
>
	<c:import url="tiles/header.jsp">
		<c:param name="currentPage" value="export" />
	</c:import>


	<div class="container-fluid">
		<div class="row">
				<!-- left column -->
				<div class="col-md-12 border-right" style="margin-top: 2.25rem;"
				>
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
								<c:param name="otherButton">
									<input 
										class="btn btn-primary btn-sm"
										id="viewExport.start"
										type="submit"
										doAction="startExport"
										scope="viewExport;viewFileExport;"
										value="<spring:message code="gui.button.exportStart"/>"
										value="Lancer les exports"></input>
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