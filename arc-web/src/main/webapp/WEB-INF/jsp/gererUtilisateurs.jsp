<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">
<head>
	<title><spring:message code="header.userManagement"/></title>
	
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/gererUtilisateurs.js" />
	</c:import>
</head>

<body class="bg-body">
<form id="selectGererUtilisateurs"
	action="selectGererUtilisateurs.action"
	spellcheck="false"
	method="post"
>

	<%@include file="tiles/header.jsp"%>

	<div class="container-fluid">
			<div class="row">
				<div
					class="col-md-4"
					class="aside"
				>
					<div class="row">
						<!-- affichage de la liste des utilisateurs -->
						<c:set var="view" value="${viewUserProfiles}"  scope="request"/>
						<c:import url="tiles/templateVObject.jsp">
							<c:param name="taille" value ="col-md" />
							<c:param name="ligneAdd" value="true" />
							<c:param name="btnSelect" value="true" />
							<c:param name="btnSee" value="true" />
							<c:param name="btnSort" value="true" />
							<c:param name="btnAdd" value="true" />
							<c:param name="btnUpdate" value="false" />
							<c:param name="btnDelete" value="true" />
							<c:param name="checkbox" value="true" />
							<c:param name="checkboxVisible" value="true" />
							<c:param name="extraScopeSee" value="viewUserList;" />
						</c:import>
					</div>

					<div class="row">
						<!-- VIEW TABLE UTILISATEURS -->
						<c:set var="view" value="${viewUserList}"  scope="request"/>
						<c:import url="tiles/templateVObject.jsp">
							<c:param name="taille" value ="col-md" />
							<c:param name="ligneAdd" value="true" />
							<c:param name="btnSelect" value ="true" />
							<c:param name="btnSee" value ="true" />
							<c:param name="btnSort" value ="true" />
							<c:param name="btnAdd" value ="true" />
							<c:param name="btnUpdate" value ="false" />
							<c:param name="btnDelete" value ="true" />
							<c:param name="checkbox" value ="true" />
							<c:param name="checkboxVisible" value ="true" />
						</c:import>
					</div>
				</div>

		</form>

	</div>


</body>
</html>