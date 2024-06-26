<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="${pageContext.response.locale}">
<head>
<link rel="icon" href="data:,"/>
<title><spring:message code="header.query"/></title>
<c:import url="tiles/defaulthead.jsp">
<c:param name="pageJs" value="/js/gererQuery.js" />
</c:import>
</head>

<body class="bg-body">
<form spellcheck="false" action="selectQuery.action" method="POST">

<div class="container-fluid">

<div id="viewHeaders" class="row">
<div class="col-md-1">
<p>Connection index</p>
 <textarea class="border" cols="4" rows="1" type="text" m="" name="myDbConnection">${myDbConnection}</textarea>
</div>
<div class="col-md-3">
<p>Schema</p>
 <textarea class="border" cols="10" rows="1" type="text" m="" name="mySchema">${mySchema}</textarea>
</div>
<div class="col-md-8" id="viewQuerySql">
<p>Query</p>
<textarea class="border" m="" cols="150" name="myQuery" aria-label="Query">${myQuery}</textarea>
<br>
<input id="viewQuery.selectFromTextBox" type="submit" doAction="selectQueryFromTextBox" scope="viewQuery;viewHeaders;" value="Execute query"></input>
</div>
</div>
<div class="row align-items-start">
<div class="col-md-4 border-right">
<c:set var="view" value="${viewTable}"  scope="request"/>
<c:import url="tiles/templateVObject.jsp">
<c:param name="taille" value ="col-md4" />
<c:param name="ligneAdd" value="false" />
<c:param name="btnSelect" value="true" />
<c:param name="btnSee" value="true" />
<c:param name="btnSort" value="true" />
<c:param name="checkbox" value="true" />
<c:param name="checkboxVisible" value="true" />
<c:param name="allowResize" value="true" />
<c:param name="extraScopeSelect" value="viewQuery;viewHeaders;" />
<c:param name="extraScopeSee" value="viewQuery;viewHeaders;" />
</c:import>
</div>
<div class="col-md-8">
<c:set var="view" value="${viewQuery}"  scope="request"/>
<c:import url="tiles/templateVObject.jsp">
<c:param name="taille" value ="col-md" />
<c:param name="btnSelect" value="false" />
<c:param name="btnSee" value="false" />
</c:import>
</div>
</div>
</div>
</form>
</body>
</html>