<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">
<head>
	<link rel="icon" href="data:,"/>
	<title><spring:message code="header.envManagement"/> - ${bacASable}</title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/gererPilotageBAS.js" />
	</c:import>
</head>

<body class="bg-body">

	<form spellcheck="false" id="selectPilotageBAS" action="selectPilotageBAS.action"
		method="post"
		enctype="multipart/form-data"
		accept-charset="UTF-8">

		<c:import url="tiles/header.jsp">
			<c:param name="currentPage" value="envManagement" />
		</c:import>

		<div class="container-fluid">
			<c:import url="/WEB-INF/jsp/tiles/template_environment.jsp"></c:import>
			<div class="row">
				<div class="col-md-7">
					<div class="row">
						<c:set var="view" value="${viewPilotageBAS}" scope="request"/>
						<c:import url="tiles/templateVObject.jsp">
							<c:param name="taille" value ="col-md" />
							<c:param name="btnSelect" value ="true" />
							<c:param name="btnSee" value ="true" />
							<c:param name="btnSort" value ="true" />
							<c:param name="ligneFilter" value ="true" />
							<c:param name="checkbox" value ="true" />
							<c:param name="checkboxVisible" value ="false" />
							<c:param name="extraScopeSee" value ="viewRapportBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;" />
						</c:import>
					</div>

					<div id="viewEntrepotBAS">

						<div class="row">
							<div class="col-md-8">
								<input type="hidden" name="viewEntrepotBAS.sessionName" value="${viewEntrepotBAS.sessionName}" m="js" />
								<h3>
									<spring:message code="managementSandbox.loadFile"/>
								</h3>
								<div class="input-group my-3">
									<div class="custom-file">
										<input
											name="viewPilotageBAS.fileUpload"
											type="file"
											class="custom-file-input"
											id="ActionsBAS.selectFiles"
											size="40"
											multiple="true"
										/> <label
											class="custom-file-label"
											for="ActionsBAS.selectFiles"
										><spring:message code="managementSandbox.fileToLoad"/></label>
									</div>
									<div class="input-group-append">										
										<label class="ml-4 mt-2 mr-1" for="entrepotCible"><spring:message code="managementSandbox.repository"/> :</label>
										<select id="entrepotCible" name="viewEntrepotBAS.customValues['entrepotEcriture']">
											<c:forEach items="${viewEntrepotBAS.getV(0,viewEntrepotBAS.content)}" var="entrepot">
												<option ${entrepot == viewEntrepotBAS.customValues['entrepotEcriture'] ? 'selected' : ''}>${entrepot}</option>
											</c:forEach>
										</select>
										<button
											class="btn btn-primary btn-sm ml-4"
											id="ActionsBAS.load"
											type="submit"
											doAction="filesUploadBAS"
											scope="viewPilotageBAS;viewRapportBAS;viewFichierBAS;viewArchiveBAS;viewEntrepotBAS;"
											multipart="true"
											ajax="false"
										><span class="fa fa-upload">&nbsp;</span> <spring:message code="managementSandbox.load"/></button>
									</div>
								</div>
							</div>

							<div class="col-md-4 border-left">
								<h3>
									<spring:message code="managementSandbox.download"/>
								</h3>
								<strong><spring:message code="managementSandbox.readingRepository"/></strong>
								<select name="viewEntrepotBAS.customValues['entrepotLecture']" style="width:${viewEntrepotBAS.headersVSize[0]};">
									<option></option>
									<c:forEach items="${viewEntrepotBAS.getV(0,viewEntrepotBAS.content)}" var="entrepot">
										<option ${entrepot == viewEntrepotBAS.customValues['entrepotLecture'] ? 'selected' : ''}>${entrepot}</option>
									</c:forEach>
								</select>
								<input class="btn btn-primary btn-sm" 
									type="submit" id="ActionsBAS.visualiserEntrepot"
									value="<spring:message code='managementSandbox.seeRepository'/>"
									scope="viewEntrepotBAS;viewRapportBAS;viewPilotageBAS;-viewFichierBAS;viewArchiveBAS;"
									doAction="visualiserEntrepotBAS"
									style="margin-left: 25px;" />
							</div>
						</div>
					</div>


				</div>
				<div class="col-md-5 border-left">
					<div class="row">
						<c:set var="view" value="${viewRapportBAS}"  scope="request"/>
						<c:import url="tiles/templateVObject.jsp">
							<c:param name="taille" value ="col-md" />
							<c:param name="btnSelect" value ="true" />
							<c:param name="btnSee" value ="true" />
							<c:param name="btnSort" value ="true" />
							<c:param name="ligneFilter" value ="true" />
							<c:param name="checkbox" value ="true" />
							<c:param name="checkboxVisible" value ="false" />
							<c:param name="extraScopeSee" value ="viewPilotageBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;" />
						</c:import>
					</div>
				</div>


			</div>

			<hr />
			<c:if test="${!isEnvProd}">
			<div class="row">
				<div class="col-md-12">

					<div class="row">
						<div class="col-md-9">
							<div class="card no-margin">
								<div class="card-header bg-primary p-0">
									<h3 class="text-white m-1">
										<spring:message code="managementSandbox.runModule"/>
									</h3>
								</div>

								<div class="card-body p-0">

									<%--Bouton action --%>
									<input id="savePhaseChoice" type="hidden" name="phaseAExecuter"
										value="" />

									<div class="btn-group d-flex btn-group-sm" role="group"
										aria-label="action">
										<c:forEach items="${listePhase}" var="phase" varStatus="i">
												<button ajax="true" doAction="executerBatch"
													class="btn btn-primary w-100"
													scope="viewPilotageBAS;viewRapportBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;"
													onclick="return alimenterPhase($(this));" type="submit"
													label="<spring:message code='${phase}'/>"
													name="phaseAExecuter"
													value="${phase}"><spring:message code="${phase}"/></button>
										</c:forEach>

									</div>

									<%--Bouton RA action --%>

									<div class="btn-group d-flex btn-group-sm mt-1" role="group"
										aria-label="RA action">
										<c:forEach items="${listePhase}" var="phase" varStatus="i">
												<button ajax="true" doAction="undoBatch"
													class="btn btn-primary w-100"
													scope="viewPilotageBAS;viewRapportBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;"
													onclick="return alimenterPhase($(this));" type="submit"
													label="<spring:message code='RA_${phase}'/>"
													name="phaseAExecuter"
													value='${phase}'>
													<spring:message code="RA_${phase}" />
												</button>
										</c:forEach>
									</div>
								</div>
							</div>
						</div>
					</div>

				</div>
			</div>
			</c:if>

			<c:if test="${isEnvProd}">
				<div class="row">
					<input type="submit" class="btn btn-secondary btn-sm" id="ActionsProd.informationInitialisationPROD" value="<spring:message code="gui.button.prodInformation"/>" scope="viewPilotageBAS;" doAction="informationInitialisationPROD" onclick="" />
					<br/>
					<input type="submit" class="btn btn-primary btn-sm" id="ActionsProd.retarderBatchInitialisationPROD" value="<spring:message code="gui.button.delayInit"/>" scope="viewPilotageBAS;" doAction="retarderBatchInitialisationPROD" onclick="return confirm('<spring:message code="gui.button.delayInit.confirm" javaScriptEscape="true"/>');"/>
					<input type="submit" class="btn btn-primary btn-sm" id="ActionsProd.demanderBatchInitialisationPROD" value="<spring:message code="gui.button.requestInit"/>" scope="viewPilotageBAS;" doAction="demanderBatchInitialisationPROD" onclick="return confirm('<spring:message code="gui.button.requestInit.confirm" javaScriptEscape="true"/>');" />
					<br/>
					<input type="submit" class="btn btn-primary btn-sm" id="ActionsProd.toggleOnPROD" value="<spring:message code="gui.button.startProd"/>" scope="viewPilotageBAS;" doAction="toggleOnPROD" onclick="return confirm('<spring:message code="gui.button.startProd.confirm" javaScriptEscape="true"/>');"/>
					<input type="submit" class="btn btn-primary btn-sm" id="ActionsProd.toggleOffPROD" value="<spring:message code="gui.button.stopProd"/>" scope="viewPilotageBAS;" doAction="toggleOffPROD" onclick="return confirm('<spring:message code="gui.button.stopProd.confirm" javaScriptEscape="true"/>');"/>
					<input type="submit" class="btn btn-primary btn-sm" id="ActionsProd.applyRulesProd" value="<spring:message code="gui.button.applyRulesProd"/>" scope="viewPilotageBAS;" doAction="applyRulesProd" onclick="return confirm('<spring:message code="gui.button.applyRulesProd.confirm" javaScriptEscape="true"/>');"/>
				</div>
			</c:if>

		<hr />
		<div class="row">
			<div class="col-md-12">
					<c:set var="view" value="${viewArchiveBAS}"  scope="request"/>
					<c:import url="tiles/templateVObject.jsp">
						<c:param name="taille" value ="col-md" />
						<c:param name="checkbox" value ="true" />
						<c:param name="ligneFilter" value ="true" />
						<c:param name="otherButton">
							<input class="btn btn-primary btn-sm"
								type="submit" id="viewArchiveBAS.downloadEnveloppe"
								value="<spring:message code="gui.button.downloadArchive"/>"
								scope="viewPilotageBAS;viewRapportBAS;viewEntrepotBAS;"
								doAction="downloadEnveloppeFromArchiveBAS" ajax="false" />
						</c:param>
					</c:import>
			</div>
		</div>
		<div class="row">
			<div class="col-md-12">
				<c:set var="view" value="${viewFichierBAS}"  scope="request"/>
				<c:import url="tiles/templateVObject.jsp">
					<c:param name="taille" value ="col-md" />
					<c:param name="btnSelect" value ="true" />
					<c:param name="btnSort" value ="true" />
					<c:param name="checkbox" value ="true" />
					<c:param name="ligneFilter" value ="true" />
					<c:param name="multiSelection" value ="true" />
					<c:param name="otherButton">
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS.downloadBd" value="<spring:message code="gui.button.downloadDatabase"/>"
							doAction="downloadBdBAS" ajax="false" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS.downloadFichier" value="<spring:message code="gui.button.downloadFile"/>"
							doAction="downloadFichierBAS" ajax="false" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS.downloadEnveloppe"
							value="<spring:message code="gui.button.downloadEnveloppe"/>" doAction="downloadEnveloppeBAS"
							ajax="false" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS.toDelete" value="<spring:message code="gui.button.deleteFiles"/>"
							scope="viewPilotageBAS;viewRapportBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;"
							doAction="toDeleteBAS" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS.toRestore" value="<spring:message code="gui.button.replayFiles"/>"
							scope="viewPilotageBAS;viewRapportBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;"
							doAction="toRestoreBAS" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS.toRestoreArchive" value="<spring:message code="gui.button.replayArchives"/>"
							scope="viewPilotageBAS;viewRapportBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;"
							doAction="toRestoreArchiveBAS" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS.undoAction" value="<spring:message code="gui.button.cancelTodo"/>"
							scope="viewPilotageBAS;viewRapportBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;"
							doAction="undoActionBAS" />
					</c:param>
				</c:import>
			</div>
		</div>

	</div>
	</form>
</body>
</html>
