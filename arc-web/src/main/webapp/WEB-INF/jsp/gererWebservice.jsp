<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<c:if test="${scope==null}">
	<head>
	<link rel="icon" href="data:,"/>
	<title><spring:message code="header.webserviceManagement"/></title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/gererWebservice.js" />
	</c:import>
	</head>
</c:if>

<body class="bg-body">

	<form id="selectWebserviceContext" action="selectWebserviceContext.action"
	spellcheck="false" method="post" enctype="multipart/form-data" accept-charset="UTF-8">

		<c:if test="${scope==null}">
			<c:import url="tiles/header.jsp">
				<c:param name="currentPage" value="webserviceManagement" />
			</c:import>
		</c:if>

		<div class="container-fluid">

			<div class="row">
				<!-- left column -->
				<div class="col-md-5">
					<!-- norm list -->
					<c:set var="view" value="${viewWebserviceContext}" scope="request" />
					<c:import url="tiles/templateVObject.jsp">
						<c:param name="btnSelect" value="true" />
						<c:param name="btnSee" value="true" />
						<c:param name="btnSort" value="true" />
						<c:param name="btnAdd" value="true" />
						<c:param name="btnUpdate" value="true" />
						<c:param name="btnDelete" value="true" />
						<c:param name="ligneAdd" value="true" />
						<c:param name="ligneFilter" value="true" />
						<c:param name="checkbox" value="true" />
						<c:param name="checkboxVisible" value="true" />
						<c:param name="extraScopeAdd" value="-viewWebserviceQuery;" />
						<c:param name="extraScopeDelete" value="-viewWebserviceQuery;" />
						<c:param name="extraScopeUpdate" value="-viewWebserviceQuery;" />
						<c:param name="extraScopeSee" value="viewWebserviceQuery;" />
					</c:import>
				</div>
				<div class="col-md-7">
					<!-- calendar list -->
					<c:set var="view" value="${viewWebserviceQuery}" scope="request" />
					<c:import url="tiles/templateVObject.jsp">
						<c:param name="btnSelect" value="true" />
						<c:param name="btnSee" value="true" />
						<c:param name="btnSort" value="true" />
						<c:param name="btnAdd" value="true" />
						<c:param name="btnUpdate" value="true" />
						<c:param name="btnDelete" value="true" />
						<c:param name="ligneAdd" value="true" />
						<c:param name="ligneFilter" value="true" />
						<c:param name="checkbox" value="true" />
						<c:param name="checkboxVisible" value="true" />
					</c:import>
				</div>

			</div>
		</div>
	</form>
</body>
</html>
