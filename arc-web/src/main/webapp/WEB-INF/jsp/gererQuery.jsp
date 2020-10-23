<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="${pageContext.response.locale}">
<head>
<c:import url="tiles/defaultcss.jsp" />
<c:import url="tiles/defaultjs.jsp">
	<c:param name="pageJs" value="/js/gererQuery.js" />
</c:import>
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