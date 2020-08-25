﻿<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Pilotage bac à sable</title>

<link rel="stylesheet" href="<c:url value='/css/bootstrap.min.css'/>" />
<link rel="stylesheet" type="text/css"
	href="<c:url value='/css/style.css' />" />
<link href="<c:url value='/css/font-awesome.min.css'/>" rel="stylesheet" />
<script type="text/javascript"
	src="<c:url value='/js/jquery-2.1.3.min.js'/>"></script>

<script src="<c:url value='/js/lib/popper.min.js'/>"></script>
<script src="<c:url value='/js/lib/bootstrap.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='/js/arc.js'/>"></script>
<script type="text/javascript"
	src="<c:url value='/js/gererPilotage.js'/>"></script>
<script type="text/javascript" src="<c:url value='/js/component.js'/>"></script>
</head>
<body class="bg-light">

	<form spellcheck="false" id="selectPilotageBAS8" action="selectPilotageBAS8.action"
		method="post"
		enctype="multipart/form-data">

		<%@include file="tiles/header.jsp"%>

		<div class="container-fluid">
			<div class="row">
				<div class="col-md-7">
					<div class="row">
						<c:set var="view" value="${viewPilotageBAS8}"  scope="request"/>
						<c:import url="tiles/templateVObject.jsp">
							<c:param name="taille" value ="col-md" />
							<c:param name="btnSelect" value ="true" />
							<c:param name="btnSee" value ="true" />
							<c:param name="btnSort" value ="true" />
							<c:param name="checkbox" value ="true" />
							<c:param name="checkboxVisible" value ="false" />
							<c:param name="extraScopeSee" value ="viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;" />
						</c:import>
					</div>

					<div id="viewEntrepotBAS8">

						<div class="row">
							<div class="col-md-8">
								<h3>
									<spring:message code="managementSandbox.loadFile"/>
								</h3>
								<div class="input-group my-3">
									<div class="custom-file">
										<input
											name="viewPilotageBAS8.fileUpload"
											type="file"
											class="custom-file-input"
											id="ActionsBAS8.selectFiles"
											size="40"
											multiple="true"
										/> <label
											class="custom-file-label"
											for="ActionsBAS8.selectFiles"
										><spring:message code="managementSandbox.fileToLoad"/></label>
									</div>
									<div class="input-group-append">										
										<label class="ml-4 mt-2 mr-1" for="entrepotCible"><spring:message code="managementSandbox.repository"/> :</label>
										<select id="entrepotCible" name="viewEntrepotBAS8.customValues['entrepotEcriture']">
											<c:forEach items="${viewEntrepotBAS8.getV(0,viewEntrepotBAS8.content)}" var="entrepot">
												<option ${entrepot == viewEntrepotBAS8.customValues['entrepotEcriture'] ? 'selected' : ''}>${entrepot}</option>
											</c:forEach>
										</select>
										<button
											class="btn btn-primary btn-sm ml-4"
											id="ActionsBAS8.load"
											type="submit"
											doAction="filesUploadBAS8"
											scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;viewArchiveBAS8;viewEntrepotBAS8;"
											multipart="true"
											ajax="false"
											onclick="updateConsoleState=true;"
										><span class="fa fa-upload">&nbsp;</span> <spring:message code="managementSandbox.load"/></button>
									</div>
								</div>
							</div>

							<div class="col-md-4 border-left">
								<h3>
									<spring:message code="managementSandbox.download"/>
								</h3>
								<b><spring:message code="managementSandbox.readingRepository"/></b>
								<select name="viewEntrepotBAS8.customValues['entrepotLecture']" style="width:${viewEntrepotBAS8.headersVSize[0]};">
									<option></option>
									<c:forEach items="${viewEntrepotBAS8.getV(0,viewEntrepotBAS8.content)}" var="entrepot">
										<option ${entrepot == viewEntrepotBAS8.customValues['entrepotLecture'] ? 'selected' : ''}>${entrepot}</option>
									</c:forEach>
								</select>
								<input class="btn btn-primary btn-sm" 
									type="submit" id="ActionsBAS8.visualiserEntrepot"
									value="<spring:message code='managementSandbox.seeRepository'/>"
									scope="viewEntrepotBAS8;viewRapportBAS8;viewPilotageBAS8;-viewFichierBAS8;viewArchiveBAS8;"
									doAction="visualiserEntrepotBAS8"
									onclick="updateConsoleState=true;" style="margin-left: 25px;" />
							</div>
						</div>
					</div>


				</div>
				<div class="col-md-5 border-left">
					<div class="row">
						<c:set var="view" value="${viewRapportBAS8}"  scope="request"/>
						<c:import url="tiles/templateVObject.jsp">
							<c:param name="taille" value ="col-md" />
							<c:param name="btnSelect" value ="true" />
							<c:param name="btnSee" value ="true" />
							<c:param name="btnSort" value ="true" />
							<c:param name="checkbox" value ="true" />
							<c:param name="checkboxVisible" value ="false" />
							<c:param name="extraScopeSee" value ="viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;" />
						</c:import>
					</div>
				</div>


			</div>

			<hr />
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
													scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
													onclick="return alimenterPhase($(this));" type="submit"
													label="<spring:message code='${phase}'/>"
													name="phaseAExecuter"
													value="${phase}">
													${phase}
												</button>
										</c:forEach>

									</div>

									<%--Bouton RA action --%>

									<div class="btn-group d-flex btn-group-sm mt-1" role="group"
										aria-label="RA action">
										<c:forEach items="${listePhase}" var="phase" varStatus="i">
												<button ajax="true" doAction="undoBatch"
													class="btn btn-primary w-100"
													scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
													onclick="return alimenterPhase($(this));" type="submit"
													label="<spring:message code='RA_${phase}'/>"
													name="phaseAExecuter"
													value='${phase}'>
													RA_${phase}
												</button>
										</c:forEach>
									</div>
								</div>
							</div>
						</div>

						<div class="col-md-3 border-left">

							<div class="row">
								<div class="col-md-6">
									<div class="card no-margin">
										<div class="card-header bg-primary p-0">
											<h3 class="text-white m-1">
												<spring:message code="managementSandbox.console"/>
											</h3>
										</div>
									</div>
								</div>
								<div class="col-md-6" style="justify-content: flex-end; display: flex;">
									<button type="button" class="btn btn-secondary"
										onclick="$('[name=&quot;consoleIhm&quot;]').html('')">
										<spring:message code="managementSandbox.emptyConsole"/>
									</button>
								</div>
							</div>
							<div class="card-body p-0">
								<div class="row">
								<div class="col-md">
									<textarea id="console" name="consoleIhm" cols="" rows=""
										class="noselect w-100" readonly
										target="updateConsoleBAS8"></textarea>
								</div>
								</div>
							</div>

						</div>

					</div>

				</div>
			</div>
		</div>



		</div>
		<hr />

		<div class="col-md-12">
			<div class="row">
				<c:set var="view" value="${viewArchiveBAS8}"  scope="request"/>
				<c:import url="tiles/templateVObject.jsp">
					<c:param name="taille" value ="col-md" />
					<c:param name="checkbox" value ="true" />
					<c:param name="otherButton">
						<input class="btn btn-primary btn-sm"
							type="submit" id="viewArchiveBAS8.downloadEnveloppe"
							value="Telecharger Enveloppe"
							scope="viewPilotageBAS8;viewRapportBAS8;viewEntrepotBAS8;"
							doAction="downloadEnveloppeFromArchiveBAS8" ajax="false" />
					</c:param>
				</c:import>
			</div>
			<div class="row">
				<c:set var="view" value="${viewFichierBAS8}"  scope="request"/>
				<c:import url="tiles/templateVObject.jsp">
					<c:param name="taille" value ="col-md" />
					<c:param name="btnSelect" value ="true" />
					<c:param name="btnSort" value ="true" />
					<c:param name="checkbox" value ="true" />
					<c:param name="ligneFilter" value ="true" />
					<c:param name="multiSelection" value ="true" />
					<c:param name="otherButton">
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.downloadBd" value="<spring:message code="gui.button.downloadDatabase"/>"
							doAction="downloadBdBAS8" ajax="false" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.downloadFichier" value="<spring:message code="gui.button.downloadFile"/>"
							doAction="downloadFichierBAS8" ajax="false" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.downloadEnveloppe"
							value="<spring:message code="gui.button.downloadArchive"/>" doAction="downloadEnveloppeBAS8"
							ajax="false" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.toDelete" value="<spring:message code="gui.button.deleteFiles"/>"
							scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
							doAction="toDeleteBAS8" onclick="updateConsoleState=true;" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.toRestore" value="<spring:message code="gui.button.replayFiles"/>"
							scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
							doAction="toRestoreBAS8" onclick="updateConsoleState=true;" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.toRestoreArchive" value="<spring:message code="gui.button.replayArchives"/>"
							scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
							doAction="toRestoreArchiveBAS8"
							onclick="updateConsoleState=true;" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.undoAction" value="<spring:message code="gui.button.cancelTodo"/>"
							scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
							doAction="undoActionBAS8" onclick="updateConsoleState=true;" />
					</c:param>
				</c:import>
			</div>
		</div>
		</div>

	</form>
</body>
</html>
