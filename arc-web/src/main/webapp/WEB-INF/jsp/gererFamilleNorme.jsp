<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">
<head>
	<link rel="icon" href="data:,"/>
	<title><spring:message code="header.familyManagement" /></title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/gererFamilleNorme.js" />
	</c:import>
</head>

<body class="bg-body">
	<form id="selectFamilleNorme" action="selectFamilleNorme.action"
		spellcheck="false" method="post">

		<c:import url="tiles/header.jsp">
			<c:param name="currentPage" value="familyManagement" />
		</c:import>

		<div class="container-fluid">
			<div class="row">
				<div class="col-md-4">
					<c:set var="view" value="${viewFamilleNorme}" scope="request" />
					<c:import url="tiles/templateVObject.jsp">
						<c:param name="ligneAdd" value="true" />
						<c:param name="btnSelect" value="true" />
						<c:param name="btnSee" value="true" />
						<c:param name="btnSort" value="true" />
						<c:param name="btnAdd" value="true" />
						<c:param name="btnUpdate" value="true" />
						<c:param name="btnDelete" value="true" />
						<c:param name="confirmDelete" value="true" />
						<c:param name="checkbox" value="true" />
						<c:param name="checkboxVisible" value="true"/>
						<c:param name="extraScopeSee"
							value="viewClient;viewTableMetier;viewHostAllowed;viewVariableMetier;" />
						<c:param name="extraScopeDelete" value ="-viewClient;-viewTableMetier;-viewHostAllowed;-viewVariableMetier;" />
						<c:param name="otherButton">
							<button class="btn btn-primary btn-sm"
								id="viewFamilleNorme.download" type="submit"
								doAction="downloadFamilleNorme" ajax="false">
								<span class="fa fa-download">&nbsp;</span>
								<spring:message code="gui.button.downloadRuleset" />
							</button>
							<div class="custom-file">
								<input
									name="fileUploadDDI"
									type="file"
									class="custom-file-input"
									id="inputGroupFileLoadDDI"
								/> <label
									class="custom-file-label"
									for="inputGroupFileLoadDDI"
									aria-describedby="Choose file to upload"
								><spring:message code="general.chooseFile"/></label>
							</div>
							<button class="btn btn-primary btn-sm"
								id="viewFamilleNorme.importDDI"
								type="submit"
								doAction="importDDI"
								multipart="true"
								scope="viewFamilleNorme;"
								>
								<span class="fa fa-upload">&nbsp;</span>
								<spring:message code="gui.button.importDDI" />
							</button>
						</c:param>
					</c:import>

					<!-- VIEW TABLE APPLI CLIENTE -->
					<c:set var="view" value="${viewClient}" scope="request" />
					<c:import url="tiles/templateVObject.jsp">
						<c:param name="ligneAdd" value="true" />
						<c:param name="btnSelect" value="true" />
						<c:param name="btnSee" value="true" />
						<c:param name="btnSort" value="true" />
						<c:param name="btnAdd" value="true" />
						<c:param name="btnUpdate" value="true" />
						<c:param name="btnDelete" value="true" />
						<c:param name="checkbox" value="true" />
						<c:param name="checkboxVisible" value="true" />
						<c:param name="extraScopeSee" value="viewHostAllowed;" />
						<c:param name="extraScopeAdd" value="viewHostAllowed;" />
						<c:param name="extraScopeUpdate" value="viewHostAllowed;" />
						<c:param name="extraScopeDelete" value="viewHostAllowed;" />
					</c:import>
					<!-- VIEW TABLE METIER -->
					<c:set var="view" value="${viewTableMetier}" scope="request" />
					<c:import url="tiles/templateVObject.jsp">
						<c:param name="ligneAdd" value="true" />
						<c:param name="btnSelect" value="true" />
						<c:param name="btnSee" value="true" />
						<c:param name="btnSort" value="true" />
						<c:param name="btnAdd" value="true" />
						<c:param name="btnUpdate" value="false" />
						<c:param name="btnDelete" value="true" />
						<c:param name="confirmDelete" value="true" />
						<c:param name="checkbox" value="true" />
						<c:param name="checkboxVisible" value="true" />
						<c:param name="extraScopeAdd" value="viewVariableMetier;" />
						<c:param name="extraScopeUpdate" value="viewVariableMetier;" />
						<c:param name="extraScopeDelete" value="viewVariableMetier;" />
					</c:import>
					<!-- VIEW HOST ALLOWED -->
					<c:set var="view" value="${viewHostAllowed}" scope="request" />
					<c:import url="tiles/templateVObject.jsp">
						<c:param name="ligneAdd" value="true" />
						<c:param name="btnSelect" value="true" />
						<c:param name="btnSee" value="true" />
						<c:param name="btnSort" value="true" />
						<c:param name="btnAdd" value="true" />
						<c:param name="btnUpdate" value="true" />
						<c:param name="btnDelete" value="true" />
						<c:param name="checkbox" value="true" />
						<c:param name="checkboxVisible" value="true" />
					</c:import>
				</div>
				<div class="col-md-8">
					<!-- VIEW VARIABLE METIER -->
					<c:set var="view" value="${viewVariableMetier}" scope="request" />
					<c:import url="tiles/templateVObject.jsp">
						<c:param name="ligneAdd" value="true" />
						<c:param name="btnSelect" value="true" />
						<c:param name="btnSee" value="true" />
						<c:param name="btnSort" value="true" />
						<c:param name="btnAdd" value="true" />
						<c:param name="btnUpdate" value="true" />
						<c:param name="btnDelete" value="true" />
						<c:param name="checkbox" value="true" />
						<c:param name="checkboxVisible" value="true" />
						<c:param name="ligneFilter" value="true" />
						<c:param name="ligneAdd" value="true" />
						<c:param name="allowResize" value="true" />
					</c:import>
				</div>

			</div>
		</div>
	</form>
</body>
</html>