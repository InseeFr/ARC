<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="${pageContext.response.locale}">
<head>
	<link rel="icon" href="data:,"/>
	<title>Filesystem Management</title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/gererFile.js" />
	</c:import>	
</head>

<body class="bg-body">
<form
	spellcheck="false"
	id="gererFile"
	action="selectFile.action"
	method="post"
	accept-charset="UTF-8"
>
<div class="container-fluid">
	<div class="row">
		<div class="col-md-5 border-right">
		 	<c:set var="view" value="${viewDirIn}"  scope="request"/>
			<c:import url="tiles/templateVObject.jsp">
				<c:param name="taille" value ="col-md4" />
				<c:param name="ligneAdd" value="true" />
				<c:param name="btnSelect" value="true" />
				<c:param name="btnSee" value="true" />
				<c:param name="btnAdd" value="true" />
				<c:param name="btnDelete" value="true" />
				<c:param name="btnUpdate" value="true" />
				<c:param name="ligneFilter" value ="true" />
				<c:param name="multiSelection" value ="true" />
				<c:param name="checkbox" value="true" />
				<c:param name="checkboxVisible" value="true" />
				<c:param name="extraScopeSee" value ="viewDirIn;viewDirOut;" />
				<c:param name="otherButton">
					<input type="text" m="" action='select' name="dirIn" value="${dirIn}" />
					<input id="viewDirIn.transfer" type="submit" doAction="transferDirIn" scope="viewDirIn;viewDirOut;" value="Transférer"></input>
					<input id="viewDirIn.update" type="submit" doAction="renameDirIn" scope="viewDirIn;viewDirOut;" value="Mettre à jour" style="display:none;"></input>
					<input id="viewDirIn.copy" type="submit" doAction="copyDirIn" scope="viewDirIn;viewDirOut;" value="Copier"></input>
					<input id="viewDirIn.downloadDirIn" type="submit" doAction="downloadDirIn" ajax="false" value="Download"></input>
				</c:param>
			</c:import>
		</div>
	
		<div class="col-md-2">
		</div>
		
		<div class="col-md-5">
			<c:set var="view" value="${viewDirOut}"  scope="request"/>
			<c:import url="tiles/templateVObject.jsp">
				<c:param name="taille" value ="col-md4" />
				<c:param name="ligneAdd" value="true" />
				<c:param name="btnSee" value="true" />
				<c:param name="btnSelect" value="true" />
				<c:param name="btnAdd" value="true" />
				<c:param name="btnDelete" value="true" />
				<c:param name="btnUpdate" value="true" />
				<c:param name="checkbox" value="true" />
				<c:param name="checkboxVisible" value="true" />
				<c:param name="multiSelection" value ="true" />
				<c:param name="extraScopeSee" value ="viewDirIn;viewDirOut;" />
				<c:param name="otherButton">
					<input type="text" m="" action='select' name="dirOut" value="${dirOut}" />
					<input id="viewDirOut.transfer" type="submit" doAction="transferDirOut" scope="viewDirIn;viewDirOut;" value="Transférer"></input>
					<input id="viewDirOut.update" type="submit" doAction="renameDirOut" scope="viewDirIn;viewDirOut;" value="Mettre à jour" style="display:none;"></input>
					<input id="viewDirOut.copy" type="submit" doAction="copyDirOut" scope="viewDirIn;viewDirOut;" value="Copier"></input>
					<input id="viewDirIn.downloadDirOut" type="submit" doAction="downloadDirOut" ajax="false" value="Download"></input>
				</c:param>
			</c:import>
		</div>
	</div>  
</div>
</form>
</body>
</html>