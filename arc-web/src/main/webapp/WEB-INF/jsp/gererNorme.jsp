﻿<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
	trimDirectiveWhitespaces="true"
%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="${pageContext.response.locale}">
<c:if test="${scope==null}">
<head>
	<title><spring:message code="header.normManagement"/></title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/gererNorme.js" />
	</c:import>
</head>
</c:if>

<body class='bg-light'>

<form
	spellcheck="false"
	id="selectNorme"
	action="selectNorme.action"
	method="post"
	enctype="multipart/form-data"
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
				<c:param name="extraScopeAdd" value ="-viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
				<c:param name="extraScopeDelete" value ="-viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
				<c:param name="extraScopeUpdate" value ="-viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
				<c:param name="extraScopeSee" value ="viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
	
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
				<c:param name="extraScopeAdd" value ="-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
				<c:param name="extraScopeDelete" value ="-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
				<c:param name="extraScopeUpdate" value ="-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
				<c:param name="extraScopeSee" value ="viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
	
			</c:import>
		</div>
	<div class="col-md">
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
			<c:param name="extraScopeAdd" value ="-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
			<c:param name="extraScopeUpdate" value ="-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
			<c:param name="extraScopeSee" value ="viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;viewModuleButtons;" />
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
				<div  id="viewModuleButtons" class="col-md">
					<!-- tab to choose the module -->
				<c:if test="${viewChargement.isInitialized==true&&viewChargement.isScoped==true
				|| viewNormage.isInitialized==true&&viewNormage.isScoped==true
				|| viewControle.isInitialized==true&&viewControle.isScoped==true
				|| viewFiltrage.isInitialized==true&&viewFiltrage.isScoped==true
				|| viewMapping.isInitialized==true&&viewMapping.isScoped==true
				|| viewExpression.isInitialized==true&&viewExpression.isScoped==true
				 }">
					<ul
						class="nav nav-tabs mb-2"
						id="chooseModule"
						role="tablist"
					>
					
					
						<li class="nav-item"><a
								class="nav-link ${viewChargement.isInitialized==true&&viewChargement.isScoped==true ? 'active' : '' }"
								id="load-tab"
								data-toggle="tab"
								href="#load"
								role="tab"
								aria-controls="load"
								aria-selected="true"
								onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
							>
								<spring:message code="normManagement.load"/>
							</a></li>
						<li class="nav-item"><a
								class="nav-link ${viewNormage.isInitialized==true&&viewNormage.isScoped==true ? 'active' : '' }"
								id="structurize-tab"
								data-toggle="tab"
								href="#structurize"
								role="tab"
								aria-controls="structurize"
								aria-selected="false"
								onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','-viewChargement;viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
							>
								<spring:message code="normManagement.structurize"/>
							</a></li>
						<li class="nav-item"><a
								class="nav-link ${viewControle.isInitialized==true&&viewControle.isScoped==true ? 'active' : '' }"
								id="control-tab"
								data-toggle="tab"
								href="#control"
								role="tab"
								aria-controls="control"
								aria-selected="false"
								onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','-viewChargement;-viewNormage;viewControle;-viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
							>
								<spring:message code="normManagement.control"/>
							</a></li>
						<li class="nav-item"><a
								class="nav-link ${viewFiltrage.isInitialized==true&&viewFiltrage.isScoped==true ? 'active' : '' }"
								id="filter-tab"
								data-toggle="tab"
								href="#filter"
								role="tab"
								aria-controls="filter"
								aria-selected="false"
								onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','-viewChargement;-viewNormage;-viewControle;viewFiltrage;-viewMapping;-viewExpression;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
							>
								<spring:message code="normManagement.filter"/>
							</a></li>
						<li class="nav-item"><a
								class="nav-link ${viewMapping.isInitialized==true&&viewMapping.isScoped==true ? 'active' : '' }"
								id="mapmodel-tab"
								data-toggle="tab"
								href="#mapmodel"
								role="tab"
								aria-controls="mapmodel"
								aria-selected="false"
								onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','-viewChargement;-viewNormage;-viewControle;-viewFiltrage;viewMapping;-viewExpression;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
							>
								<spring:message code="normManagement.mapmodel"/>
							</a></li>
							<li class="nav-item font-smaller" style="margin-left:auto"><a
								class="nav-link ${viewExpression.isInitialized==true&&viewExpression.isScoped==true ? 'active' : '' }"
								id="expressions-tab"
								data-toggle="tab"
								href="#expressions"
								role="tab"
								aria-controls="expressions"
								aria-selected="false"
								onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;viewExpression;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
							>
								<spring:message code="normManagement.expression"/>
							</a></li>
					</ul>
				</c:if>

					<!-- The content of the tab -->
					<div
						class="tab-content mt-3"
						id="tabVOjectRulesSet"
					>
						<%-- LOAD PANEL --%>
						<div
							
							id="load"
							role="tabpanel"
							aria-labelledby="load-tab"
						>
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
								<c:param name="otherButton">

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
												scope="viewModuleButtons;viewChargement;"
												multipart="true"
											><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
										</div>
									</div>

								</c:param>

							</c:import>
						</div>

						<%-- STRUCTURIZE PANEL --%>
						<div
							id="structurize"
							role="tabpanel"
							aria-labelledby="structurize-tab"
						>
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
													scope="viewModuleButtons;viewNormage;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>

							</c:import>
						</div>

						<%-- CONTROL TAB --%>
						<div
							id="control"
							role="tabpanel"
							aria-labelledby="control-tab"
						>
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
													scope="viewModuleButtons;viewControle;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>

							</c:import>
						</div>
						<%-- FILTER TAB --%>
						<div
							id="filter"
							role="tabpanel"
							aria-labelledby="filter-tab"
						>
							<c:set var="view" value="${viewFiltrage}"  scope="request"/>
							<c:import url="tiles/templateVObject.jsp">
								<c:param name="btnSelect" value ="true" />
								<c:param name="btnSee" value ="true" />
								<c:param name="btnSort" value ="true" />
								<c:param name="btnUpdate" value ="true" />
								<c:param name="btnDelete" value ="true" />
								<c:param name="ligneFilter" value ="true" />
								<c:param name="checkbox" value ="true" />
								<c:param name="checkboxVisible" value ="true" />
								<c:param name="otherButton">
									<c:if
										test='${viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas")}'
									>
										<input
											class="btn btn-primary btn-sm"
											id="viewFiltrage.truncate"
											type="submit"
											doAction="viderFiltrage"
											scope="viewFiltrage;"
											value="<spring:message code="gui.button.deleteRuleset"/>"
										></input>
										<input
											class="btn btn-primary btn-sm"
											id="viewFiltrage.copie"
											type="submit"
											doAction="selectJeuxDeReglesFiltrageCopie"
											scope="viewFiltrage;viewJeuxDeReglesCopie;"
											value="<spring:message code="gui.button.replaceRuleset"/>"
										></input>

										<c:if test="${viewFiltrage.content.t.size()==0}">
											<input
												class="btn btn-primary btn-sm"
												id="viewFiltrage.creerNouveau"
												type="submit"
												doAction="preGenererRegleFiltrage"
												scope="viewFiltrage;"
												value="<spring:message code="gui.button.generateRuleset"/>"
											></input>
										</c:if>
										<div class="input-group my-3">
											<div class="custom-file">
												<input
													name="fileUploadFilter"
													type="file"
													class="custom-file-input"
													id="inputGroupFileFilter"
												/> <label
													class="custom-file-label"
													for="inputGroupFileFilter"
													aria-describedby="Choose file to upload for filter module"
												><spring:message code="general.chooseFile"/></label>
											</div>
											<div class="input-group-append">
												<button
													class="btn btn-primary btn-sm"
													id="viewFiltrage.import"
													type="submit"
													doAction="importFiltrage"
													scope="viewModuleButtons;viewFiltrage;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>

							</c:import>
						</div>

						<%-- MAPPING TAB --%>
						<div
							id="mapmodel"
							role="tabpanel"
							aria-labelledby="mapmodel-tab"
						>
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
													scope="viewModuleButtons;viewMapping;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>

							</c:import>
						</div>
						<%-- EXPRESSIONS TAB --%>
						<div
							id="expressions"
							role="tabpanel"
							aria-labelledby="expressions-tab"
						>
						</div>
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
													scope="viewModuleButtons;viewExpression;"
													multipart="true"
												><span class="fa fa-upload">&nbsp;</span> <spring:message code="gui.button.importRuleset"/></button>
											</div>
										</div>
									</c:if>
								</c:param>
							</c:import>
					</div>





					<%-- Modal to rule copy --%>
								<div>

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
		</div>
	</div>
</form>
	
</body>
</html>
