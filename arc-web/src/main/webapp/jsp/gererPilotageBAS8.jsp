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
<title>Pilotage bac à sable</title>

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
	src="<s:url value='/js/gererPilotage.js'/>"
></script>
<script
	type="text/javascript"
	src="<s:url value='/js/component.js'/>"
></script>
</head>
<body>


	<%@include file="tiles/header.jsp"%>

<s:form
	spellcheck="false"
	namespace="/"
	method="POST"
	theme="simple"
	enctype="multipart/form-data"
>
	<div class="container-fluid">

		<div class="row">
			<div class="col-md d-flex justify-content-center">

				<h1><s:text name="managementSandbox.header" /></h1>
			</div>
		</div>
		<hr />
			<div class="row">
				<div class="col-md">
					<div class="row">
						<div class="col-md-7">
							<h2><s:text name="managementSandbox.filesStates" /></h2>
						</div>

					</div>
					<div class="row">
						<s:include value="tiles/templateVObject.jsp">
							<s:set
								var="view"
								value="%{viewPilotageBAS8}"
								scope="request"
							></s:set>
							<s:param name="taille">col-md</s:param>
							<s:param name="btnSelect">true</s:param>
							<s:param name="btnSort">true</s:param>
							<s:param name="checkbox">true</s:param>
							<s:param name="checkboxVisible">false</s:param>
							<s:param name="extraScopeSee">viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;</s:param>
						</s:include>
					</div>
				</div>
				<div class="col-md-3 border-left">
					<div class="row">
						<div class="col-md">
							<h2><s:text name="managementSandbox.info" /></h2>
							<div class="viewRapportBAS8"></div>

						</div>
					</div>
				</div>
				<div class="col-md-3 border-left">
					<div class="row">
						<div class="col-md">
							<label
								for="console"
								class="mb-0"
							>
								<h2><s:text name="managementSandbox.console" /></h2>
							</label>
						</div>
					</div>
					<div class="row">
						<div class="col-md">
							<s:textarea
								id="console"
								name="consoleIhm"
								class="noselect w-100"
								readonly="true"
								target="updateConsoleBAS8"
								theme="simple"
							></s:textarea>
						</div>

					</div>
					<div class="row mt-2">
						<div class="col-md">
							<button
								type="button"
								class="btn btn-primary"
								onclick="$('[name=&quot;consoleIhm&quot;]').html('')"
							><s:text name="managementSandbox.emptyConsole" /></button>
						</div>
					</div>
				</div>
			</div>
		<hr />
			<div class="row">

				<div class="col-md">

					<h2><s:text name="managementSandbox.runModule" /></h2>

					<%--Bouton action --%>
					<input
						id="savePhaseChoice"
						type="hidden"
						name="phaseAExecuter"
						value=""
					/>

					<div
						class="btn-group d-flex btn-group-sm"
						role="group"
						aria-label="action"
					>
						<s:iterator
							value="listePhase"
							var="phase"
							status="i"
						>
							<s:if test="isInIhm">
								<button
									ajax="true"
									doAction="executerBatch"
									class="btn btn-primary w-100"
									scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
									onclick="return alimenterPhase($(this));"
									type="submit"
									label="<s:property value='nomPhase'/>"
									name="phaseAExecuter"
									value='<s:property value="nomPhase"/>'
								><s:property value="nomPhase" /></button>
							</s:if>
						</s:iterator>

					</div>

					<%--Bouton RA action --%>

					<div
						class="btn-group d-flex btn-group-sm mt-1"
						role="group"
						aria-label="RA action"
					>
						<s:iterator
							value="listePhase"
							var="phase"
							status="i"
						>
							<s:if test="isRAIhm">
								<button
									ajax="true"
									doAction="undoBatch"
									class="btn btn-primary w-100"
									scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
									onclick="return alimenterPhase($(this));"
									type="submit"
									label="<s:property value='nomPhase'/>"
									name="phaseAExecuter"
									value='<s:property value="nomPhase"
								/>'
								>RA <s:property value="nomPhase" /></button>
							</s:if>
						</s:iterator>
					</div>

				</div>

			</div>
			<hr />

			<div class="row">
				<div
					class="col-md-3 border-right"
					id="viewEntrepotBAS8"
				>

					<div class="row">
						<div class="col-md">
							<h2><s:text name="managementSandbox.loadFile" /></h2>
						</div>
					</div>

					<div class="row">
						<div class="col-md">
							<label for="ActionsBAS8.selectFiles"><s:text
									name="managementSandbox.fileToLoad"
								/></label>
							<s:file
								id="ActionsBAS8.selectFiles"
								multiple="true"
								name="viewPilotageBAS8.fileUpload"
							/>
						</div>
					</div>

					<div class="row">
						<div class="col-md">
							<label for="entrepotCible"><s:text
									name="managementSandbox.repository"
								/> :</label>
							<s:select
								id="entrepotCible"
								list="%{viewEntrepotBAS8.getV(0,viewEntrepotBAS8.content)}"
								value="%{viewEntrepotBAS8.customValues['entrepotEcriture']}"
								name="viewEntrepotBAS8.customValues['entrepotEcriture']"
								theme="simple"
								emptyOption="true"
							></s:select>
						</div>
					</div>
					<div class="row">
						<div class="col-md">
							<input
								type="submit"
								id="ActionsBAS8.load"
								value=<s:property value="getText('managementSandbox.load')" />
								scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;viewArchiveBAS8;viewEntrepotBAS8;"
								doAction="filesUploadBAS8"
								onclick="updateConsoleState=true;"
								multipart="true"
								ajax="false"
							/>
						</div>
					</div>
					<hr />
					<div style="float: left;">
						<h2><s:text name="managementSandbox.download" /></h2> <b><s:text
								name="managementSandbox.readingRepository"
							/></b>
						<s:select
							cssStyle="width:%{viewEntrepotBAS8.headersVSize[0]};"
							list="%{viewEntrepotBAS8.getV(0,viewEntrepotBAS8.content)}"
							value="%{viewEntrepotBAS8.customValues['entrepotLecture']}"
							name="viewEntrepotBAS8.customValues['entrepotLecture']"
							theme="simple"
							emptyOption="true"
						></s:select>
						<input
							type="submit"
							id="ActionsBAS8.visualiserEntrepot"
							value=<s:property value="getText('managementSandbox.seeRepository')" />
							scope="viewEntrepotBAS8;viewRapportBAS8;viewPilotageBAS8;-viewFichierBAS8;viewArchiveBAS8;"
							doAction="visualiserEntrepotBAS8"
							onclick="updateConsoleState=true;"
							style="margin-left: 25px;"
						/>
					</div>
	</div>


	<div class="col-md">
			<s:include value="tiles/templateVObject.jsp">
				<s:set
					var="view"
					value="%{viewArchiveBAS8}"
					scope="request"
				></s:set>
				<s:param name="taille">col-md</s:param>
				<s:param name="checkbox">true</s:param>
				<s:param name="otherButton">
					<input
						type="submit"
						id="viewArchiveBAS8.downloadEnveloppe"
						value="Telecharger Enveloppe"
						scope="viewPilotageBAS8;viewRapportBAS8;viewEntrepotBAS8;"
						doAction="downloadEnveloppeFromArchiveBAS8"
						ajax="false"
					/>
				</s:param>
			</s:include>

			<s:include value="tiles/templateVObject.jsp">
				<s:set
					var="view"
					value="%{viewFichierBAS8}"
					scope="request"
				></s:set>
				<s:param name="taille">col-md</s:param>
				<s:param name="btnSelect">true</s:param>
				<s:param name="btnSort">true</s:param>
				<s:param name="checkbox">true</s:param>
				<s:param name="otherButton">
					<s:submit
						class="btn btn-primary btn-sm"
						type="submit"
						id="viewFichierBAS8.downloadBd"
						value="Telecharger BD"
						doAction="downloadBdBAS8"
						ajax="false"
					/>
					<s:submit
						class="btn btn-primary btn-sm"
						type="submit"
						id="viewFichierBAS8.convertBD"
						value="Convertir la table"
						doAction="convertBDBAS8"
						ajax="false"
					/>
					<s:submit
						class="btn btn-primary btn-sm"
						type="submit"
						id="viewFichierBAS8.downloadFichier"
						value="Telecharger fichier"
						doAction="downloadFichierBAS8"
						ajax="false"
					/>
					<input
						class="btn btn-primary btn-sm"
						type="submit"
						id="viewFichierBAS8.downloadEnveloppe"
						value="Telecharger Enveloppe"
						doAction="downloadEnveloppeBAS8"
						ajax="false"
					/>
					<input
						class="btn btn-primary btn-sm"
						type="submit"
						id="viewFichierBAS8.toDelete"
						value="Supprimer fichiers"
						scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
						doAction="toDeleteBAS8"
						onclick="updateConsoleState=true;"
					/>
					<input
						class="btn btn-primary btn-sm"
						type="submit"
						id="viewFichierBAS8.toRestore"
						value="Rejouer fichiers"
						scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
						doAction="toRestoreBAS8"
						onclick="updateConsoleState=true;"
					/>
					<input
						class="btn btn-primary btn-sm"
						type="submit"
						id="viewFichierBAS8.toRestoreArchive"
						value="Rejouer Archive"
						scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
						doAction="toRestoreArchiveBAS8"
						onclick="updateConsoleState=true;"
					/>
					<input
						class="btn btn-primary btn-sm"
						type="submit"
						id="viewFichierBAS8.undoAction"
						value="Annulation action"
						scope="viewPilotageBAS8;viewRapportBAS8;viewFichierBAS8;-viewArchiveBAS8;viewEntrepotBAS8;"
						doAction="undoActionBAS8"
						onclick="updateConsoleState=true;"
					/>
				</s:param>
			</s:include>
	</div>
	</div>
	</div>
</s:form>
</body>
</html>
