<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
%>
<%@ taglib
	prefix="s"
	uri="/struts-tags"
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Family norm management</title>

<link
	rel="stylesheet"
	href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
	integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
	crossorigin="anonymous"
/>
<link
	rel="stylesheet"
	type="text/css"
	href="<s:url value='/css/style.css' />"
/>
<link
	href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"
	rel="stylesheet"
/>
<script
	type="text/javascript"
	src="<s:url value='/js/jquery-2.1.3.min.js'/>"
></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"
	integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"
	crossorigin="anonymous"
></script>
<script
	src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
	integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
	crossorigin="anonymous"
></script>
<script
	type="text/javascript"
	src="<s:url value='/js/arc.js'/>"
></script>
<script
	type="text/javascript"
	src="<s:url value='/js/gererFamilleNorme.js'/>"
></script>
<script
	type="text/javascript"
	src="<s:url value='/js/component.js'/>"
></script>
</head>
<body class="bg-light">
	<%@include file="tiles/header.jsp"%>

	<div class="container-fluid">


		<s:form
			spellcheck="false"
			namespace="/"
			method="POST"
			theme="simple"
			enctype="multipart/form-data"
		>
			<div class="row">
				<div
					class="col-md-4"
					class="aside"
				>
					<div class="row">
						<s:include value="tiles/templateVObject.jsp">
							<s:set
								var="view"
								value="%{viewFamilleNorme}"
								scope="request"
							></s:set>
							<s:param name="taille">col-md</s:param>
							<s:param name="ligneAdd" >true</s:param>
							<s:param name="btnSelect">true</s:param>
							<s:param name="btnSee">true</s:param>
							<s:param name="btnSort">true</s:param>
							<s:param name="btnAdd">true</s:param>
							<s:param name="btnUpdate">true</s:param>
							<s:param name="btnDelete">true</s:param>
							<s:param name="checkbox">true</s:param>
							<s:param name="checkboxVisible">true</s:param>
							<s:param name="extraScopeSee">viewClient;viewTableMetier;viewVariableMetier;</s:param>
						</s:include>
					</div>




					<div class="row">
						<!-- VIEW TABLE APPLI CLIENTE -->
						<s:include value="tiles/templateVObject.jsp">
							<s:set
								var="view"
								value="%{viewClient}"
								scope="request"
							></s:set>
							<s:param name="taille">col-md</s:param>
							<s:param name="ligneAdd" >true</s:param>
							<s:param name="btnSelect">true</s:param>
							<s:param name="btnSee">true</s:param>
							<s:param name="btnSort">true</s:param>
							<s:param name="btnAdd">true</s:param>
							<s:param name="btnUpdate">true</s:param>
							<s:param name="btnDelete">true</s:param>
							<s:param name="checkbox">true</s:param>
							<s:param name="checkboxVisible">true</s:param>
						</s:include>
					</div>


					<div class="row">
						<!-- VIEW TABLE METIER -->
						<s:include value="tiles/templateVObject.jsp">
							<s:set
								var="view"
								value="%{viewTableMetier}"
								scope="request"
							></s:set>
							<s:param name="taille">col-md</s:param>
							<s:param name="ligneAdd" >true</s:param>
							<s:param name="btnSelect">true</s:param>
							<s:param name="btnSee">true</s:param>
							<s:param name="btnSort">true</s:param>
							<s:param name="btnAdd">true</s:param>
							<s:param name="btnUpdate">true</s:param>
							<s:param name="btnDelete">true</s:param>
							<s:param name="checkbox">true</s:param>
							<s:param name="checkboxVisible">true</s:param>
						</s:include>
					</div>
				</div>

				<div
					class="col-md-8"
					class="central"
				>
					<div class="row">
						<!-- VIEW VARIABLE METIER -->
						<s:include value="tiles/templateVObject.jsp">
							<s:set
								var="view"
								value="%{viewVariableMetier}"
								scope="request"
							></s:set>
							<s:param name="taille">col-md</s:param>
							<s:param name="ligneAdd" >true</s:param>
							<s:param name="btnSelect">true</s:param>
							<s:param name="btnSee">true</s:param>
							<s:param name="btnSort">true</s:param>
							<s:param name="btnAdd">true</s:param>
							<s:param name="btnUpdate">true</s:param>
							<s:param name="btnDelete">true</s:param>
							<s:param name="checkbox">true</s:param>
							<s:param name="checkboxVisible">true</s:param>
							<s:param name="ligneFilter" >true</s:param>
							<s:param name="ligneAdd">true</s:param>
						</s:include>
					</div>
				</div>

			</div>


		</s:form>

	</div>


</body>
</html>