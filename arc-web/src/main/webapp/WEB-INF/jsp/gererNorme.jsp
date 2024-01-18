﻿﻿<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
<c:if test="${scope==null}">
<head>
	<link rel="icon" href="data:,"/>
	<title><spring:message code="header.normManagement"/></title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/gererNorme.js" />
	</c:import>
</head>
</c:if>

<body class="bg-body">

<form
	spellcheck="false"
	id="selectNorme"
	action="selectNorme.action"
	method="post"
	accept-charset="UTF-8"
>

<c:import url="tiles/header.jsp">
	<c:param name="currentPage" value="normManagement" />
</c:import>

<div class="container-fluid">
	<div class="row">
		<div class="col-md-6">
			<!-- norm list -->
			<c:set var="view" value="${viewNorme}"  scope="request"/>
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
				<c:param name="extraScopeAdd" value ="-viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;-viewModules;" />
				<c:param name="extraScopeDelete" value ="-viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;-viewModules;" />
				<c:param name="extraScopeUpdate" value ="-viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;-viewModules;" />
				<c:param name="extraScopeSee" value ="viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;-viewModules;" />
	
			</c:import>
		</div>
		<div class="col-md-3">
			<!-- calendar list -->
			<c:set var="view" value="${viewCalendrier}"  scope="request"/>
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
				<c:param name="extraScopeAdd" value ="-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;-viewModules;" />
				<c:param name="extraScopeDelete" value ="-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;-viewModules;" />
				<c:param name="extraScopeUpdate" value ="-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;-viewModules;" />
				<c:param name="extraScopeSee" value ="viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;-viewModules;" />
	
			</c:import>
		</div>
	<div class="col-md-3">
		<!-- rule set list -->
		<c:set var="view" value="${viewJeuxDeRegles}"  scope="request"/>
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
			<c:param name="extraScopeAdd" value ="-viewJeuxDeReglesCopie;" />
			<c:param name="extraScopeDelete" value ="-viewModules;-viewChargement;-viewNormage;-viewControle;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;" />
			<c:param name="extraScopeUpdate" value ="viewModules;viewChargement;viewNormage;viewControle;viewMapping;viewExpression;-viewJeuxDeReglesCopie;" />
			<c:param name="extraScopeSee" value ="viewModules;viewChargement;viewNormage;viewControle;viewMapping;viewExpression;-viewJeuxDeReglesCopie;" />
			<c:param name="otherButton">
				<button
					class="btn btn-primary btn-sm"
					id="viewJeuxDeRegles.download"
					type="submit"
					doAction="downloadJeuxDeRegles"
					ajax="false"
				><span class="fa fa-download">&nbsp;</span> <spring:message code="gui.button.downloadRuleset"/></button>
			</c:param>
		</c:import>
		</div>
	</div>

	<div class="row">
		<div class="col">
			<!-- rule set list -->
			<c:set var="view" value="${viewModules}"  scope="request"/>
			<c:import url="tiles/templateTabnav.jsp">
				<c:param name="extraScopeSee" value ="phase" />
			</c:import>
		</div>
	</div>

		<div class="row">
			<div class="col">

						<%-- LOAD PANEL --%>
							<c:set var="view" value="${viewChargement}"  scope="request"/>
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
								<c:param name="multiSelection" value ="true" />
								<c:param name="extraScopeAdd" value ="viewModules;" />
								<c:param name="extraScopeDelete" value ="viewModules;" />
								<c:param name="extraScopeUpdate" value ="viewModules;" />
								<c:param name="extraScopeSee" value ="viewModules;" />
								<c:param name="otherButton">
									<c:if
										test='${viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas")}'
									>
										<input
											class="btn btn-primary btn-sm"
											id="viewChargement.truncate"
											type="submit"
											doAction="viderChargement"
											scope="viewChargement;"
											value="<spring:message code="gui.button.deleteRuleset"/>"
										/>
										<input
											class="btn btn-primary btn-sm"
											id="viewChargement.copie"
											type="submit"
											doAction="selectJeuxDeReglesChargementCopie"
											scope="viewChargement;viewJeuxDeReglesCopie;"
											value="<spring:message code="gui.button.replaceRuleset"/>"
										/>
									
										<div class="input-group my-3">
											<div class="custom-file">
												<input
													name="fileUploadLoad"
													type="file"
													class="custom-file-input"
													id="inputGroupFileLoad"
												/> <label
													class="custom-file-label"
													for="inputGroupFileLoad"
													aria-describedby="Choose file to upload for load module"
												><spring:message code="general.chooseFile"/></label>
											</div>
											<div class="input-group-append">
												<button
													class="btn btn-primary btn-sm"
													id="viewChargement.import"
													type="submit"
													doAction="importChargement"
													scope="viewChargement;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>
							</c:import>

						<%-- STRUCTURIZE PANEL --%>
							<c:set var="view" value="${viewNormage}"  scope="request"/>
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
								<c:param name="multiSelection" value ="true" />
								<c:param name="extraScopeAdd" value ="viewModules;" />
								<c:param name="extraScopeDelete" value ="viewModules;" />
								<c:param name="extraScopeUpdate" value ="viewModules;" />
								<c:param name="extraScopeSee" value ="viewModules;" />
								<c:param name="otherButton">
									<c:if
										test='${viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas")}'
									>
										<input
											class="btn btn-primary btn-sm"
											id="viewNormage.truncate"
											type="submit"
											doAction="viderNormage"
											scope="viewNormage;"
											value="<spring:message code="gui.button.deleteRuleset"/>"
										/>
										<input
											class="btn btn-primary btn-sm"
											id="viewNormage.copie"
											type="submit"
											doAction="selectJeuxDeReglesNormageCopie"
											scope="viewNormage;viewJeuxDeReglesCopie;"
											value="<spring:message code="gui.button.replaceRuleset"/>"
										/>
										<div class="input-group my-3">
											<div class="custom-file">
												<input
													name="fileUploadStructurize"
													type="file"
													class="custom-file-input"
													id="inputGroupFilestructure"
												/> <label
													class="custom-file-label"
													for="inputGroupFilestructure"
													aria-describedby="Choose file to upload for structure module"
												><spring:message code="general.chooseFile"/></label>
											</div>
											<div class="input-group-append">
												<button
													class="btn btn-primary btn-sm"
													id="viewNormage.import"
													type="submit"
													doAction="importNormage"
													scope="viewNormage;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>
							</c:import>

						<%-- CONTROL TAB --%>
							<c:set var="view" value="${viewControle}"  scope="request"/>
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
								<c:param name="multiSelection" value ="true" />
								<c:param name="extraScopeAdd" value ="viewModules;" />
								<c:param name="extraScopeDelete" value ="viewModules;" />
								<c:param name="extraScopeUpdate" value ="viewModules;" />
								<c:param name="extraScopeSee" value ="viewModules;" />
								<c:param name="allowResize" value ="true" />
								<c:param name="otherButton">
									<c:if
										test='${viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas")}'
									>
										<input
											class="btn btn-primary btn-sm"
											id="viewControle.truncate"
											type="submit"
											doAction="viderControle"
											scope="viewControle;"
											value="<spring:message code="gui.button.deleteRuleset"/>"
										/>
										<input
											class="btn btn-primary btn-sm"
											id="viewControle.copie"
											type="submit"
											doAction="selectJeuxDeReglesControleCopie"
											scope="viewControle;viewJeuxDeReglesCopie;"
											value="<spring:message code="gui.button.replaceRuleset"/>"
										/>
										<div class="input-group my-3">
											<div class="custom-file">
												<input
													name="fileUploadControle"
													type="file"
													class="custom-file-input"
													id="inputGroupFileControle"
												/> <label
													class="custom-file-label"
													for="inputGroupFileControle"
													aria-describedby="Choose file to upload for control module"
												><spring:message code="general.chooseFile"/></label>
											</div>
											<div class="input-group-append">
												<button
													class="btn btn-primary btn-sm"
													type="submit"
													doAction="importControle"
													scope="viewControle;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>
							</c:import>
							
						<%-- MAPPING TAB --%>
							<c:set var="view" value="${viewMapping}"  scope="request"/>
							<c:import url="tiles/templateVObject.jsp">
								<c:param name="btnSelect" value ="true" />
								<c:param name="btnSee" value ="true" />
								<c:param name="btnSort" value ="true" />
								<c:param name="btnUpdate" value ="true" />
								<c:param name="btnDelete" value ="true" />
								<c:param name="ligneFilter" value ="true" />
								<c:param name="checkbox" value ="true" />
								<c:param name="checkboxVisible" value ="true" />
								<c:param name="multiSelection" value ="true" />
								<c:param name="extraScopeAdd" value ="viewModules;" />
								<c:param name="extraScopeDelete" value ="viewModules;" />
								<c:param name="extraScopeUpdate" value ="viewModules;" />
								<c:param name="extraScopeSee" value ="viewModules;" />
								<c:param name="allowResize" value ="true" />
								<c:param name="otherButton">
									<c:if
										test='${viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas")}'
									>
										<input
											class="btn btn-primary btn-sm"
											id="viewMapping.truncate"
											type="submit"
											doAction="viderMapping"
											scope="viewMapping;"
											value="<spring:message code="gui.button.deleteRuleset"/>"
										></input>
										<input
											class="btn btn-primary btn-sm"
											id="viewMapping.copie"
											type="submit"
											doAction="selectJeuxDeReglesMappingCopie"
											scope="viewMapping;viewJeuxDeReglesCopie;"
											value="<spring:message code="gui.button.replaceRuleset"/>"
										></input>

										<c:if test="${viewMapping.content.t.size()==0}">
											<input
												class="btn btn-primary btn-sm"
												id="viewMapping.creerNouveau"
												type="submit"
												doAction="preGenererRegleMapping"
												scope="viewMapping;"
												value="<spring:message code="gui.button.generateRuleset"/>"
											></input>
										</c:if>
										<div class="input-group my-3">
											<div class="custom-file">
												<input
													name="fileUploadMap"
													type="file"
													class="custom-file-input"
													id="inputGroupFileMap"
												/> <label
													class="custom-file-label"
													for="inputGroupFileMap"
													aria-describedby="Choose file to upload for map module"
												><spring:message code="general.chooseFile"/></label>
											</div>
											<div class="input-group-append">
												<button
													class="btn btn-primary btn-sm"
													id="viewMapping.import"
													type="submit"
													doAction="importMapping"
													scope="viewMapping;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>
							</c:import>
							
						<%-- EXPRESSIONS TAB --%>
						<c:set var="view" value="${viewExpression}"  scope="request"/>
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
								<c:param name="extraScopeAdd" value ="viewModules;" />
								<c:param name="extraScopeDelete" value ="viewModules;" />
								<c:param name="extraScopeUpdate" value ="viewModules;" />
								<c:param name="extraScopeSee" value ="viewModules;" />
								<c:param name="allowResize" value ="true" />
								<c:param name="otherButton">
									<c:if
										test='${viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas")}'
									>
										<input
											class="btn btn-primary btn-sm"
											id="viewExpression.truncate"
											type="submit"
											doAction="viderExpression"
											scope="viewExpression;"
											value="<spring:message code="gui.button.deleteRuleset"/>"
										></input>
										<input
											class="btn btn-primary btn-sm"
											id="viewExpression.copie"
											type="submit"
											doAction="selectJeuxDeReglesExpressionCopie"
											scope="viewExpression;viewJeuxDeReglesCopie;"
											value="<spring:message code="gui.button.replaceRuleset"/>"
										></input>
										<div class="input-group my-3">
											<div class="custom-file">
												<input
													name="fileUploadExpression"
													type="file"
													class="custom-file-input"
													id="inputGroupFileExpression"
												/> <label
													class="custom-file-label"
													for="inputGroupFileExpression"
													aria-describedby="Choose file to upload for expressions"
												><spring:message code="general.chooseFile"/></label>
											</div>
											<div class="input-group-append">
												<button
													class="btn btn-primary btn-sm"
													id="viewExpression.import"
													type="submit"
													doAction="importExpression"
													scope="viewExpression;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>
							</c:import>

					<%-- Modal to rule copy --%>
							<c:set var="view" value="${viewJeuxDeReglesCopie}"  scope="request"/>
							<c:import url="tiles/templateVObject.jsp">
								<c:param name="btnSee" value ="true" />
								<c:param name="btnSelect" value ="true" />
								<c:param name="btnSort" value ="true" />
								<c:param name="ligneFilter" value ="true" />
								<c:param name="checkbox" value ="true" />
								<c:param name="checkboxVisible" value ="true" />
								<c:param name="otherButton">
									<input
										class="btn btn-primary btn-sm"
										id="viewJeuxDeReglesCopie.copie"
										type="submit"
										doAction="copieJeuxDeRegles"
										scope="-viewJeuxDeReglesCopie;${viewJeuxDeReglesCopie.customValues['SELECTED_RULESET_NAME']};"
										value="<spring:message code="gui.button.copy"/>"
									></input>
								</c:param>
							</c:import>
			</div>
		</div>
	</div>
</form>
	
</body>
</html>
