<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<s:if test="scope==null">
	<head>
<link rel="stylesheet" href="<s:url value='/css/bootstrap.min.css'/>" />
<link rel="stylesheet" type="text/css"
	href="<s:url value='/css/style.css' />" />
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/themes/default/style.min.css" />
<link href="<s:url value='/css/font-awesome.min.css'/>" rel="stylesheet" />

<script src="<s:url value='/js/jquery-2.1.3.min.js'/>"></script>
<script src="<s:url value='/js/lib/popper.min.js'/>"></script>
<script src="<s:url value='/js/lib/bootstrap.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jstree/3.2.1/jstree.min.js"></script>
<script src="<s:url value='/js/arc.js'/>"></script>
<script src="<s:url value='/js/gererWebservice.js'/>"></script>
<script src="<s:url value='/js/component.js'/>"></script>
	</head>
</s:if>
<body>

	<s:form spellcheck="false" namespace="/" method="POST" theme="simple"
		enctype="multipart/form-data">

		<div class="container-fluid">

			<div class="row mb-5">
				<div class="col">
					<s:if test="scope==null">
						<s:include value="tiles/header.jsp">
							<s:param name="currentPage">webserviceManagement</s:param>
						</s:include>
					</s:if>
				</div>
			</div>

			<div class="row border-bottom">
				<!-- left column -->
				<div class="col-md-5">
					<!-- norm list -->
					<s:include value="tiles/templateVObject.jsp">
						<s:set var="view" value="%{viewWebserviceContext}" scope="request"></s:set>
						<s:param name="btnSelect">true</s:param>
						<s:param name="btnSee">true</s:param>
						<s:param name="btnSort">true</s:param>
						<s:param name="btnAdd">true</s:param>
						<s:param name="btnUpdate">true</s:param>
						<s:param name="btnDelete">true</s:param>
						<s:param name="ligneAdd">true</s:param>
						<s:param name="ligneFilter">true</s:param>
						<s:param name="checkbox">true</s:param>
						<s:param name="checkboxVisible">true</s:param>
						<s:param name="extraScopeAdd">-viewWebserviceQuery;</s:param>
						<s:param name="extraScopeDelete">-viewWebserviceQuery;</s:param>
						<s:param name="extraScopeUpdate">-viewWebserviceQuery;</s:param>
						<s:param name="extraScopeSee">viewWebserviceQuery;</s:param>
					</s:include>
				</div>
				<div class="col-md-7">
					<!-- calendar list -->
					<s:include value="tiles/templateVObject.jsp">
						<s:set var="view" value="%{viewWebserviceQuery}" scope="request"></s:set>
						<s:param name="btnSelect">true</s:param>
						<s:param name="btnSee">true</s:param>
						<s:param name="btnSort">true</s:param>
						<s:param name="btnAdd">true</s:param>
						<s:param name="btnUpdate">true</s:param>
						<s:param name="btnDelete">true</s:param>
						<s:param name="ligneAdd">true</s:param>
						<s:param name="ligneFilter">true</s:param>
						<s:param name="checkbox">true</s:param>
						<s:param name="checkboxVisible">true</s:param>
					</s:include>
				</div>

			</div>
		</div>
	</s:form>
</body>
</html>
