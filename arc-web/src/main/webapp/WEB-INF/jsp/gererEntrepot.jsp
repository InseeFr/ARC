<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">
<head>
<link rel="icon" href="data:,"/>
<title><spring:message code="header.entrepot"/></title>
<c:import url="tiles/defaulthead.jsp">
<c:param name="pageJs" value="/js/entrepot.js" />
</c:import>
</head>

<body class="bg-body">
<form id="selectEntrepot" action="selectEntrepot.action"
spellcheck="false" method="post" accept-charset="UTF-8">

<c:import url="tiles/header.jsp">
<c:param name="currentPage" value="entrepot" />
</c:import>

<div class="container-fluid">
<div class="row">
<div class="col-md-6">
<!-- filestore list -->
<c:set var="view" value="${viewEntrepot}"  scope="request"/>
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
</c:import>
</div>
</div>
</div>
</form>


</body>
</html>