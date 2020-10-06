<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
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
	<script type="text/javascript" src="<c:url value='/js/arc.js'/>"></script>
	<script type="text/javascript" src="<c:url value='/js/gererQuery.js'/>"></script>
	<script type="text/javascript" src="<c:url value='/js/component.js'/>"></script>
</head>
<body class='bg-light'>
<form spellcheck="false" action="selectQuery.action" method="POST">

<div class="container-fluid">

 <div class="row">
 
 	<div class="col-md-4 border-right">
 		<textarea m="" cols="" rows="" name="mySchema" style="width:150px; height:20px;">${mySchema}</textarea>
	 	<c:set var="view" value="${viewTable}"  scope="request"/>
		<c:import url="tiles/templateVObject.jsp">
			<c:param name="taille" value ="col-md4" />
			<c:param name="ligneAdd" value="true" />
			<c:param name="btnSelect" value="true" />
			<c:param name="btnSee" value="true" />
			<c:param name="btnSort" value="true" />
			<c:param name="checkbox" value="true" />
			<c:param name="checkboxVisible" value="true" />
			<c:param name="allowResize" value="true" />
			<c:param name="extraScopeSee" value="viewQuerySql;viewQuery;" />
		</c:import>
	</div>

	<div class="col-md-4" id="viewQuerySql">
	   <input id="viewQuery.select" type="submit" doAction="selectQuery" scope="viewQuery;viewTable;" value="Executer"></input>
	   	<textarea m="" name="myQuery" cssStyle="width:300px;height:500px;">${myQuery}</textarea>
	</div>
	<div class="col-md-4">
	  <c:set var="view" value="${viewQuery}"  scope="request"/>
		<c:import url="tiles/templateVObject.jsp">
			<c:param name="taille" value ="col-md" />
		</c:import>
    </div>
  </div>
</div>
</form>
    
	

</body>
</html>