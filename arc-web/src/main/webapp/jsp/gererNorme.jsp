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
<!DOCTYPE html>
<html>
<s:if test="scope==null">
	<head>
<title><s:text name="header.normManagement"/></title>
<link
	rel="stylesheet"
	href="<s:url value='/css/bootstrap.min.css'/>"
/>
<link
	rel="stylesheet"
	type="text/css"
	href="<s:url value='/css/style.css' />"
/>
<link
	href="<s:url value='/css/font-awesome.min.css'/>"
	rel="stylesheet"
/>
<script
	type="text/javascript"
	src="<s:url value='/js/jquery-2.1.3.min.js'/>"
></script>

<script	src="<s:url value='/js/lib/popper.min.js'/>" ></script>
<script	src="<s:url value='/js/lib/bootstrap.min.js'/>"></script>

<script
	type="text/javascript"
	src="<s:url value='/js/arc.js'/>"
></script>
<script
	type="text/javascript"
	src="<s:url value='/js/gererNomenclature.js'/>"
></script>
<script
	type="text/javascript"
	src="<s:url value='/js/component.js'/>"
></script>
	</head>
</s:if>
<body class='bg-light'>

<s:form
	spellcheck="false"
	namespace="/"
	method="POST"
	theme="simple"
	enctype="multipart/form-data"
>

	<%@include file="tiles/header.jsp"%>


	<div class="container-fluid">

			<div class="row">
				<!-- left column -->
				<div class="col-md-5 border-right"
				>
					<div class="row">
						<div class="col-md">
							<!-- norm list -->
							<s:include value="tiles/templateVObject.jsp">
								<s:set
									var="view"
									value="%{viewNorme}"
									scope="request"
								></s:set>
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
								<s:param name="extraScopeAdd">viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;</s:param>
								<s:param name="extraScopeDelete">-viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;</s:param>
								<s:param name="extraScopeUpdate">viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;</s:param>
								<s:param name="extraScopeSee">viewCalendrier;-viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;</s:param>

							</s:include>
						</div>
					</div>

					<div class="row">
						<div class="col-md">
							<!-- calendar list -->
							<s:include value="tiles/templateVObject.jsp">
								<s:set
									var="view"
									value="%{viewCalendrier}"
									scope="request"
								></s:set>
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
								<s:param name="extraScopeAdd">viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;</s:param>
								<s:param name="extraScopeDelete">viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;</s:param>
								<s:param name="extraScopeUpdate">viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;</s:param>
								<s:param name="extraScopeSee">viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;</s:param>

							</s:include>
						</div>
					</div>

					<!-- rule set list -->
					<div class="row">
						<div class="col-md">
							<s:include value="tiles/templateVObject.jsp">
								<s:set
									var="view"
									value="%{viewJeuxDeRegles}"
									scope="request"
								></s:set>
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
								<s:param name="extraScopeAdd">viewChargement;viewNormage;viewControle;viewFiltrage;viewMapping;-viewJeuxDeReglesCopie;</s:param>
								<s:param name="extraScopeUpdate">viewChargement;viewNormage;viewControle;viewFiltrage;viewMapping;-viewJeuxDeReglesCopie;</s:param>
								<s:param name="extraScopeSee">viewJeuxDeRegles;viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;viewModuleButtons;</s:param>

							</s:include>
						</div>
					</div>

				</div>

				<div class="col-md-7">
					<div class="row">
						<div  id="viewModuleButtons" class="col-md">
							<!-- tab to choose the module -->
						<s:if test="viewChargement.isInitialized==true&&viewChargement.isScoped==true
						|| viewNormage.isInitialized==true&&viewNormage.isScoped==true
						|| viewControle.isInitialized==true&&viewControle.isScoped==true
						|| viewFiltrage.isInitialized==true&&viewFiltrage.isScoped==true
						|| viewMapping.isInitialized==true&&viewMapping.isScoped==true
						">
							<ul
								class="nav nav-tabs mb-2"
								id="chooseModule"
								role="tablist"
							>
							
							
								<li class="nav-item"><a
										class="nav-link active"
										id="load-tab"
										data-toggle="tab"
										href="#load"
										role="tab"
										aria-controls="load"
										aria-selected="true"
										onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','viewJeuxDeRegles;viewChargement;-viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
									>
										<s:text name="normManagement.load" />
									</a></li>
								<li class="nav-item"><a
										class="nav-link"
										id="structurize-tab"
										data-toggle="tab"
										href="#structurize"
										role="tab"
										aria-controls="structurize"
										aria-selected="false"
										onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','viewJeuxDeRegles;-viewChargement;viewNormage;-viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
									>
										<s:text name="normManagement.structurize" />
									</a></li>
								<li class="nav-item"><a
										class="nav-link"
										id="control-tab"
										data-toggle="tab"
										href="#control"
										role="tab"
										aria-controls="control"
										aria-selected="false"
										onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','viewJeuxDeRegles;-viewChargement;-viewNormage;viewControle;-viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
									>
										<s:text name="normManagement.control" />
									</a></li>
								<li class="nav-item"><a
										class="nav-link"
										id="filter-tab"
										data-toggle="tab"
										href="#filter"
										role="tab"
										aria-controls="filter"
										aria-selected="false"
										onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;viewFiltrage;-viewMapping;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
									>
										<s:text name="normManagement.filter" />
									</a></li>
								<li class="nav-item"><a
										class="nav-link"
										id="mapmodel-tab"
										data-toggle="tab"
										href="#mapmodel"
										role="tab"
										aria-controls="mapmodel"
										aria-selected="false"
										onclick="$('[id=\x22viewJeuxDeRegles.select\x22]').attr('scope','viewJeuxDeRegles;-viewChargement;-viewNormage;-viewControle;-viewFiltrage;viewMapping;-viewJeuxDeReglesCopie;'); $('[id=\x22viewJeuxDeRegles.select\x22]').click();"
									>
										<s:text name="normManagement.mapmodel" />
									</a></li>
							</ul>
						</s:if>

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
									<s:include value="tiles/templateVObject.jsp">
										<s:set
											var="view"
											value="%{viewChargement}"
											scope="request"
										></s:set>
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
										<s:param name="otherButton">

											<input
												class="btn btn-primary btn-sm"
												id="viewChargement.truncate"
												type="submit"
												doAction="viderChargement"
												scope="viewChargement;"
												value="<s:text name="gui.button.deleteRuleset"/>"
											/>
											<input
												class="btn btn-primary btn-sm"
												id="viewChargement.copie"
												type="submit"
												doAction="selectJeuxDeReglesChargementCopie"
												scope="viewChargement;viewJeuxDeReglesCopie;"
												value="<s:text name="gui.button.replaceRuleset"/>"
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
													>Choose file</label>
												</div>
												<div class="input-group-append">
													<button
														class="btn btn-primary btn-sm"
														id="viewChargement.import"
														type="submit"
														doAction="importChargement"
														scope="viewChargement;"
														multipart="true"
													><span class="fa fa-upload">&nbsp;</span> <s:text
															name="general.uploadFile"
														/></button>
												</div>
											</div>

										</s:param>

									</s:include>
								</div>

								<%-- STRUCTURIZE PANEL --%>
								<div
									id="structurize"
									role="tabpanel"
									aria-labelledby="structurize-tab"
								>
									<s:include value="tiles/templateVObject.jsp">
										<s:set
											var="view"
											value="%{viewNormage}"
											scope="request"
										></s:set>
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
										<s:param name="otherButton">
											<s:if
												test='(viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas"))'
											>
												<input
													class="btn btn-primary btn-sm"
													id="viewNormage.truncate"
													type="submit"
													doAction="viderNormage"
													scope="viewNormage;"
													value="<s:text name="gui.button.deleteRuleset"/>"
												/>
												<input
													class="btn btn-primary btn-sm"
													id="viewNormage.copie"
													type="submit"
													doAction="selectJeuxDeReglesNormageCopie"
													scope="viewNormage;viewJeuxDeReglesCopie;"
													value="<s:text name="gui.button.replaceRuleset"/>"
												/>
												<div class="input-group my-3">
													<div class="custom-file">
														<input
															name="fileUploadstructure"
															type="file"
															class="custom-file-input"
															id="inputGroupFilestructure"
														/> <label
															class="custom-file-label"
															for="inputGroupFilestructure"
															aria-describedby="Choose file to upload for structure module"
														>Choose file</label>
													</div>
													<div class="input-group-append">
														<button
															class="btn btn-primary btn-sm"
															id="viewNormage.import"
															type="submit"
															doAction="importNormage"
															scope="viewNormage;"
															multipart="true"
														><span class="fa fa-upload">&nbsp;</span> <s:text
																name="general.uploadFile"
															/></button>
													</div>
												</div>
											</s:if>
										</s:param>

									</s:include>
								</div>

								<%-- CONTROL TAB --%>
								<div
									id="control"
									role="tabpanel"
									aria-labelledby="control-tab"
								>
									<s:include value="tiles/templateVObject.jsp">
										<s:set
											var="view"
											value="%{viewControle}"
											scope="request"
										></s:set>
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
										<s:param name="otherButton">
											<s:if
												test='(viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas"))'
											>
												<input
													class="btn btn-primary btn-sm"
													id="viewControle.truncate"
													type="submit"
													doAction="viderControle"
													scope="viewControle;"
													value="<s:text name="gui.button.deleteRuleset"/>"
												/>
												<input
													class="btn btn-primary btn-sm"
													id="viewControle.copie"
													type="submit"
													doAction="selectJeuxDeReglesControleCopie"
													scope="viewControle;viewJeuxDeReglesCopie;"
													value="<s:text name="gui.button.replaceRuleset"/>"
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
														>Choose file</label>
													</div>
													<div class="input-group-append">
														<button
															class="btn btn-primary btn-sm"
															type="submit"
															doAction="importControle"
															scope="viewControle;"
															multipart="true"
														><span class="fa fa-upload">&nbsp;</span> <s:text
																name="general.uploadFile"
															/></button>
													</div>
												</div>
											</s:if>
										</s:param>

									</s:include>
								</div>
								<%-- FILTER TAB --%>
								<div
									id="filter"
									role="tabpanel"
									aria-labelledby="filter-tab"
								>
									<s:include value="tiles/templateVObject.jsp">
										<s:set
											var="view"
											value="%{viewFiltrage}"
											scope="request"
										></s:set>
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
										<s:param name="otherButton">
											<s:if
												test='(viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas"))'
											>
												<input
													class="btn btn-primary btn-sm"
													id="viewFiltrage.truncate"
													type="submit"
													doAction="viderFiltrage"
													scope="viewFiltrage;"
													value="<s:text name="gui.button.deleteRuleset"/>"
												></input>
												<input
													class="btn btn-primary btn-sm"
													id="viewFiltrage.copie"
													type="submit"
													doAction="selectJeuxDeReglesFiltrageCopie"
													scope="viewFiltrage;viewJeuxDeReglesCopie;"
													value="<s:text name="gui.button.replaceRuleset"/>"
												></input>

												<s:if test="viewFiltrage.content.lines.size()==0">
													<input
														id="viewFiltrage.creerNouveau"
														type="submit"
														doAction="preGenererRegleFiltrage"
														scope="viewFiltrage;"
														value="<s:text name="gui.button.generateRuleset"/>"
													></input>
												</s:if>
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
														>Choose file</label>
													</div>
													<div class="input-group-append">
														<button
															class="btn btn-primary btn-sm"
															id="viewFiltrage.import"
															type="submit"
															doAction="importFiltrage"
															scope="viewFiltrage;"
															multipart="true"
														><span class="fa fa-upload">&nbsp;</span> <s:text
																name="general.uploadFile"
															/></button>
													</div>
												</div>
											</s:if>
										</s:param>

									</s:include>
								</div>

								<%-- MAPPING TAB --%>
								<div
									id="mapmodel"
									role="tabpanel"
									aria-labelledby="mapmodel-tab"
								>
									<s:include value="tiles/templateVObject.jsp">
										<s:set
											var="view"
											value="%{viewMapping}"
											scope="request"
										></s:set>
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
										<s:param name="otherButton">
											<s:if
												test='(viewJeuxDeRegles.mapContentSelected().get("etat").get(0).toLowerCase().contains(".bas"))'
											>
												<input
													class="btn btn-primary btn-sm"
													id="viewMapping.truncate"
													type="submit"
													doAction="viderMapping"
													scope="viewMapping;"
													value="<s:text name="gui.button.deleteRuleset"/>"
												></input>
												<input
													class="btn btn-primary btn-sm"
													id="viewMapping.copie"
													type="submit"
													doAction="selectJeuxDeReglesMappingCopie"
													scope="viewMapping;viewJeuxDeReglesCopie;"
													value="<s:text name="gui.button.replaceRuleset"/>"
												></input>

												<s:if test="viewMapping.content.lines.size()==0">
													<input
														id="viewMapping.creerNouveau"
														type="submit"
														doAction="preGenererRegleMapping"
														scope="viewMapping;"
														value="<s:text name="gui.button.generateRuleset"/>"
													></input>
												</s:if>
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
														>Choose file</label>
													</div>
													<div class="input-group-append">
														<button
															class="btn btn-primary btn-sm"
															id="viewMapping.import"
															type="submit"
															doAction="importMapping"
															scope="viewMapping;"
															multipart="true"
														><span class="fa fa-upload">&nbsp;</span> <s:text
																name="general.uploadFile"
															/></button>
													</div>
												</div>
											</s:if>
										</s:param>

									</s:include>
								</div>
							</div>





							<%-- Modal to rule copy --%>
										<div>


											<s:include value="tiles/templateVObject.jsp">
												<s:set
													var="view"
													value="%{viewJeuxDeReglesCopie}"
													scope="request"
												></s:set>
												<s:param name="btnSee">true</s:param>
												<s:param name="btnSelect">true</s:param>
												<s:param name="btnSort">true</s:param>
												<s:param name="ligneFilter">true</s:param>
												<s:param name="checkbox">true</s:param>
												<s:param name="checkboxVisible">true</s:param>
												<s:param name="otherButton">
													<input
														class="btn btn-primary btn-sm"
														id="viewJeuxDeReglesCopie.copie"
														type="submit"
														doAction="copieJeuxDeRegles"
														scope="-viewJeuxDeReglesCopie;<s:property value="%{viewJeuxDeReglesCopie.customValues['SELECTED_RULESET_NAME']}"/>;"
														value="Copier"
													></input>
												</s:param>

											</s:include>


										</div>
						</div>
					</div>
				</div>
			</div>
	</div>
</s:form>
	
</body>
</html>
