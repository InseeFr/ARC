<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Pilotage bac à sable</title>

<link rel="stylesheet" href="<s:url value='/css/bootstrap.min.css'/>" />
<link rel="stylesheet" type="text/css"
	href="<s:url value='/css/style.css' />" />
<link href="<s:url value='/css/font-awesome.min.css'/>" rel="stylesheet" />
<script type="text/javascript"
	src="<s:url value='/js/jquery-2.1.3.min.js'/>"></script>

<script src="<s:url value='/js/lib/popper.min.js'/>"></script>
<script src="<s:url value='/js/lib/bootstrap.min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/js/arc.js'/>"></script>
<script type="text/javascript"
	src="<s:url value='/js/gererPilotage.js'/>"></script>
<script type="text/javascript" src="<s:url value='/js/component.js'/>"></script>
</head>
<body class="bg-light">

	<s:form spellcheck="false" namespace="/" method="POST" theme="simple"
		enctype="multipart/form-data">

		<%@include file="tiles/header.jsp"%>

		<div class="container-fluid">
			<div class="row">
				<div class="col-md-7">
					<div class="row">
						<s:include value="tiles/templateVObject.jsp">
							<s:set var="view" value="%{viewPilotageBAS8}" scope="request"></s:set>
							<s:param name="taille">col-md</s:param>
							<s:param name="btnSelect">true</s:param>
							<s:param name="btnSee">true</s:param>
							<s:param name="btnSort">true</s:param>
							<s:param name="checkbox">true</s:param>
							<s:param name="checkboxVisible">false</s:param>
							<s:param name="extraScopeSee">viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;</s:param>
						</s:include>
					</div>

					<div id="viewEntrepotBAS8">

						<div class="row">
							<div class="col-md-8">
								<h3>
									<s:text name="managementSandbox.loadFile" />
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
										><s:text name="managementSandbox.fileToLoad"/></label>
									</div>
									<div class="input-group-append">										
										<label class="ml-2" for="entrepotCible"><s:text
												name="managementSandbox.repository" /> :</label>
										<s:select id="entrepotCible"
											list="%{viewEntrepotBAS8.getV(0,viewEntrepotBAS8.content)}"
											value="%{viewEntrepotBAS8.customValues['entrepotEcriture']}"
											name="viewEntrepotBAS8.customValues['entrepotEcriture']"
											theme="simple" emptyOption="true"></s:select>
										<button
											class="btn btn-primary btn-sm"
											id="ActionsBAS8.load"
											type="submit"
											doAction="filesUploadBAS8"
											scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;viewArchiveBAS8;viewEntrepotBAS8;"
											multipart="true"
											ajax="false"
											onclick="updateConsoleState=true;"
										><span class="fa fa-upload">&nbsp;</span> <s:text
												name="managementSandbox.load"
											/></button>
									</div>
								</div>
							</div>

							<div class="col-md-4 border-left">
								<h3>
									<s:text name="managementSandbox.download" />
								</h3>
								<b><s:text name="managementSandbox.readingRepository" /></b>
								<s:select cssStyle="width:%{viewEntrepotBAS8.headersVSize[0]};"
									list="%{viewEntrepotBAS8.getV(0,viewEntrepotBAS8.content)}"
									value="%{viewEntrepotBAS8.customValues['entrepotLecture']}"
									name="viewEntrepotBAS8.customValues['entrepotLecture']"
									theme="simple" emptyOption="true"></s:select>
								<input class="btn btn-primary btn-sm" 
									type="submit" id="ActionsBAS8.visualiserEntrepot"
									value=<s:property value="getText('managementSandbox.seeRepository')" />
									scope="viewEntrepotBAS8;viewRapportBAS8;viewPilotageBAS8;-viewFichierBAS8;viewArchiveBAS8;"
									doAction="visualiserEntrepotBAS8"
									onclick="updateConsoleState=true;" style="margin-left: 25px;" />
							</div>
						</div>
					</div>


				</div>
				<div class="col-md-5 border-left">
					<div class="row">
						<s:include value="tiles/templateVObject.jsp">
							<s:set var="view" value="%{viewRapportBAS8}" scope="request"></s:set>
							<s:param name="taille">col-md</s:param>
							<s:param name="btnSelect">true</s:param>
							<s:param name="btnSee">true</s:param>
							<s:param name="btnSort">true</s:param>
							<s:param name="checkbox">true</s:param>
							<s:param name="checkboxVisible">false</s:param>
							<s:param name="extraScopeSee">viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;</s:param>
						</s:include>
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
										<s:text name="managementSandbox.runModule" />
									</h3>
								</div>

								<div class="card-body p-0">

									<%--Bouton action --%>
									<input id="savePhaseChoice" type="hidden" name="phaseAExecuter"
										value="" />

									<div class="btn-group d-flex btn-group-sm" role="group"
										aria-label="action">
										<s:iterator value="listePhase" var="phase" status="i">
												<button ajax="true" doAction="executerBatch"
													class="btn btn-primary w-100"
													scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
													onclick="return alimenterPhase($(this));" type="submit"
													label="<s:property value='phase'/>"
													name="phaseAExecuter"
													value='<s:property value="phase"/>'>
													<s:property value="phase" />
												</button>
										</s:iterator>

									</div>

									<%--Bouton RA action --%>

									<div class="btn-group d-flex btn-group-sm mt-1" role="group"
										aria-label="RA action">
										<s:iterator value="listePhase" var="phase" status="i">
												<button ajax="true" doAction="undoBatch"
													class="btn btn-primary w-100"
													scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
													onclick="return alimenterPhase($(this));" type="submit"
													label="<s:property value='phase'/>"
													name="phaseAExecuter"
													value='<s:property value="phase"
								/>'>
													RA
													<s:property value="phase" />
												</button>
										</s:iterator>
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
												<s:text name="managementSandbox.console" />
											</h3>
										</div>
									</div>
								</div>
								<div class="col-md-6" style="justify-content: flex-end; display: flex;">
									<button type="button" class="btn btn-secondary"
										onclick="$('[name=&quot;consoleIhm&quot;]').html('')">
										<s:text name="managementSandbox.emptyConsole" />
									</button>
								</div>
							</div>
							<div class="card-body p-0">
								<div class="row">
								<div class="col-md">
									<s:textarea id="console" name="consoleIhm"
										class="noselect w-100" readonly="true"
										target="updateConsoleBAS8" theme="simple"></s:textarea>
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
				<s:include value="tiles/templateVObject.jsp">
					<s:set var="view" value="%{viewArchiveBAS8}" scope="request"></s:set>
					<s:param name="taille">col-md</s:param>
					<s:param name="checkbox">true</s:param>
					<s:param name="otherButton">
						<input class="btn btn-primary btn-sm"
							type="submit" id="viewArchiveBAS8.downloadEnveloppe"
							value="Telecharger Enveloppe"
							scope="viewPilotageBAS8;viewRapportBAS8;viewEntrepotBAS8;"
							doAction="downloadEnveloppeFromArchiveBAS8" ajax="false" />
					</s:param>
				</s:include>
			</div>
			<div class="row">
				<s:include value="tiles/templateVObject.jsp">
					<s:set var="view" value="%{viewFichierBAS8}" scope="request"></s:set>
					<s:param name="taille">col-md</s:param>
					<s:param name="btnSelect">true</s:param>
					<s:param name="btnSort">true</s:param>
					<s:param name="checkbox">true</s:param>
					<s:param name="ligneFilter">true</s:param>
					<s:param name="otherButton">
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.downloadBd" value="<s:text name="gui.button.downloadDatabase"/>"
							doAction="downloadBdBAS8" ajax="false" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.downloadFichier" value="<s:text name="gui.button.downloadFile"/>"
							doAction="downloadFichierBAS8" ajax="false" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.downloadEnveloppe"
							value="<s:text name="gui.button.downloadArchive"/>" doAction="downloadEnveloppeBAS8"
							ajax="false" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.toDelete" value="<s:text name="gui.button.deleteFiles"/>"
							scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
							doAction="toDeleteBAS8" onclick="updateConsoleState=true;" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.toRestore" value="<s:text name="gui.button.replayFiles"/>"
							scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
							doAction="toRestoreBAS8" onclick="updateConsoleState=true;" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.toRestoreArchive" value="<s:text name="gui.button.replayArchives"/>"
							scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
							doAction="toRestoreArchiveBAS8"
							onclick="updateConsoleState=true;" />
						<input class="btn btn-primary btn-sm" type="submit"
							id="viewFichierBAS8.undoAction" value="<s:text name="gui.button.cancelTodo"/>"
							scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
							doAction="undoActionBAS8" onclick="updateConsoleState=true;" />
					</s:param>
				</s:include>
			</div>
		</div>
		</div>

	</s:form>
</body>
</html>
