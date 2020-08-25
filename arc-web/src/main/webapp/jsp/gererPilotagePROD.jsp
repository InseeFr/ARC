<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<link rel="stylesheet" type="text/css" href="<c:url value='/css/style.css' />" />
<script type="text/javascript" src="<c:url value='/js/jquery-2.1.3.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='/js/arc.js'/>"></script>
<script type="text/javascript" src="<c:url value='/js/gererPilotagePROD.js'/>"></script>
<script type="text/javascript" src="<c:url value='/js/component.js'/>"></script>
<!-- <link rel="stylesheet" href="./css/tablesorter.css" type="text/css" /> -->
<%-- 	<script src="<c:url value='/js/util/noSelectionTexte.js" type="text/javascript' />"></script> --%>
<%-- <script type="text/javascript" src="./js/jquery-1.11.1.min.js"></script> --%>
<%-- <script type="text/javascript" src="./js/jquery.filtertable.js"></script> --%>
</head>
<body>
	<div>
		<div style="float: left;" class="titre1">Suivi de Production</div>
		<div style="position:relative;top:3px;left:100px;">
			<a class="onglet" href="<c:url value="accueil.action"/>">Accueil</a>
			<a class="onglet" href="<c:url value="selectNorme.action"/>">Gérer les Normes</a>
			<a class="onglet" href="<c:url value="selectFamilleNorme.action"/>">Gérer les familles</a>
		</div>
	</div>
	<div style="position: absolute; top: 40px; left: 0px; width: 99.5%;">
		<s:form spellcheck="false" namespace="/" method="POST" theme="simple" enctype="multipart/form-data">
			<div class="container" id="viewPilotagePROD" style="float: left;">
				<c:if test="${viewPilotagePROD.isInitialized && viewPilotagePROD.isScoped}">
					<div class="bandeau">
						${viewPilotagePROD.title}
					</div>
					<input type="hidden" name="viewPilotagePROD.headerSortDLabel" value="" />
					<table class="fixedHeader">
						<thead>
							<tr>
								<th style="display: none;"></th>
								<s:iterator value="viewPilotagePROD.headersVLabel" var="head" status="incr">
									<s:if test="viewPilotagePROD.headersVisible[#incr.index]">
										<th class="sort" style="width:<s:property value='viewPilotagePROD.headersVSize[#incr.index]'/>;">
										<s:property /></th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:property /></th>
									</s:else>
								</s:iterator>
							</tr>
							<tr style="display: none;">
								<th style="display: none;"></th>
								<s:iterator value="viewPilotagePROD.headersDLabel" var="head" status="incr">
									<th><s:property /></th>
								</s:iterator>
							</tr>
							<tr style="display: none;">
								<th style="display: none;"></th>
								<s:iterator value="viewPilotagePROD.headersDLabel" var="head" status="incr">
									<th><s:checkbox name="viewPilotagePROD.selectedColumns[%{#incr.index}]" theme="simple"></s:checkbox></th>
								</s:iterator>
							</tr>
							<tr>
								<th style="display: none;"></th>
								<s:iterator value="viewPilotagePROD.headersVLabel" var="head" status="incr">
									<s:if test="viewPilotagePROD.headersVisible[#incr.index]">
										<th><s:textarea name="viewPilotagePROD.filterFields[%{#incr.index}]" value="%{viewPilotagePROD.filterFields[#incr.index]}" theme="simple"></s:textarea></th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:textarea name="viewPilotagePROD.filterFields[%{#incr.index}]" value="%{viewPilotagePROD.filterFields[#incr.index]}" theme="simple"></s:textarea></th>
									</s:else>
								</s:iterator>
							</tr>
						</thead>
						<tbody>
							<s:if test="viewPilotagePROD.content!=null && viewPilotagePROD.content.size()>0">
								<s:iterator value="viewPilotagePROD.content" var="line" status="incr1">
									<tr>
										<td style="display: none;"><s:checkbox name="viewPilotagePROD.selectedLines[%{#incr1.index}]" onclick="updateCheckBox('viewPilotagePROD',$(this));" theme="simple"></s:checkbox></td>
										<s:iterator value="#line" status="incr2">
											<s:if test="viewPilotagePROD.headersVisible[#incr2.index]">
												<td onclick="updateCheckBoxGrid('viewPilotagePROD;viewRapportPROD',$(this));"><s:if test='"text".equals(viewPilotagePROD.headersVType[#incr2.index])'>
														<s:textarea cssClass="noselect" readonly="true" name="viewPilotagePROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewPilotagePROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
													</s:if> <s:else>
														<s:select cssClass="noselect" readonly="true" list="%{viewPilotagePROD.headersVSelect[#incr2.index]}" name="viewPilotagePROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewPilotagePROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
													</s:else></td>
											</s:if>
											<s:else>
												<td style="display: none;"><s:if test='"text".equals(viewPilotagePROD.headersVType[#incr2.index])'>
														<s:textarea cssClass="noselect" name="viewPilotagePROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewPilotagePROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
														<%-- 										<s:property /> --%>
													</s:if> <s:else>
														<s:select cssClass="noselect" list="%{viewPilotagePROD.headersVSelect[#incr2.index]}" name="viewPilotagePROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewPilotagePROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
														<%--  #{'PHASEV3':'PHASEV3', 'PHASEV2':'PHASEV2'}	%{viewPilotagePROD.headersVSelect[#incr2.index]}  --%>
													</s:else></td>
											</s:else>
										</s:iterator>
									</tr>
								</s:iterator>
							</s:if>
							<s:else>
								<s:if test="hasActionErrors()">
									<s:actionerror />
								</s:if>
								<s:else>
								</s:else>
							</s:else>
						</tbody>
					</table>
					<div class="alert">
						<s:property value="%{viewPilotagePROD.message}" />
					</div>
					<div style="float: left;">
						<input type="submit" id="viewPilotagePROD.select" value="Selectionner" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="selectPilotagePROD" />
						<input type="submit" id="viewPilotagePROD.sort" value="Trier" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="sortPilotagePROD" />
					</div>
					<s:if test='1!=viewPilotagePROD.nbPages'>
						<div style="float: left; margin-left: 20px;">
							<table style="width: 200px;">
								<tr>
									<td style="width: 40px;">Page :</td>
									<td style="width: 50px; background-color: #ffffff;"><s:textarea name="viewPilotagePROD.idPage" value="%{viewPilotagePROD.idPage}" theme="simple" /></td>
									<td style="width: 10px;">/</td>
									<td style="width: 25px;"><s:property value="%{viewPilotagePROD.nbPages}" /></td>
									<td class="smallButton" onclick="gotoPage('viewPilotagePROD',$(this),-999999999);">&lt;&lt;</td>
									<td class="smallButton" onclick="gotoPage('viewPilotagePROD',$(this),-1);">&lt;</td>
									<td class="smallButton" onclick="gotoPage('viewPilotagePROD',$(this),1);">&gt;</td>
									<td class="smallButton" onclick="gotoPage('viewPilotagePROD',$(this),999999999);">&gt;&gt;</td>
								</tr>
							</table>
						</div>
					</s:if>
					<div style="margin-bottom: 30px;"></div>
				</c:if>
				
				<div>
					<input type="submit" id="ActionsProd.informationInitialisationPROD" value="Information sur la production" scope="viewPilotagePROD;" doAction="informationInitialisationPROD" onclick="" />
					<br/>
					<input type="submit" id="ActionsProd.retarderBatchInitialisationPROD" value="Retarder l'initialisation à dans 7 jours" scope="viewPilotagePROD;" doAction="retarderBatchInitialisationPROD" onclick="return confirm('Etes vous sur de retarder le batch d initialisation ?');"/>
					<input type="submit" id="ActionsProd.demanderBatchInitialisationPROD" value="Demander l'initialisation maintenant" scope="viewPilotagePROD;" doAction="demanderBatchInitialisationPROD" onclick="return confirm('Etes vous sur de demander le batch d initialisation maintenant ?');" />
					<br/>
					<input type="submit" id="ActionsProd.toggleOnPROD" value="Demander le demarrage de la production" scope="viewPilotagePROD;" doAction="toggleOnPROD" onclick="return confirm('Etes vous sur de demander le demarrage de la prod ?');"/>
					<input type="submit" id="ActionsProd.toggleOffPROD" value="Demander l'arret de la production" scope="viewPilotagePROD;" doAction="toggleOffPROD" onclick="return confirm('Etes vous sur de demander l'arret immédiat de la prod ?');"/>
				</div>		
				<div style="margin-bottom: 10px;"></div>
				
			</div>
			<div class="container" id="viewRapportPROD" style="float: left; margin-left: 20px;">
			<s:if test="viewPilotagePROD.isInitialized==true&&viewRapportPROD.isScoped==true&&viewRapportPROD.content.size()>0">
					<div class="bandeau">
						<s:property value="%{viewRapportPROD.title}" />
					</div>
					<s:hidden name="viewRapportPROD.headerSortDLabel" value="" />
					<table class="fixedHeader">
						<thead>
							<tr>
								<th style="display: none;"></th>
								<s:iterator value="viewRapportPROD.headersVLabel" var="head" status="incr">
									<s:if test="viewRapportPROD.headersVisible[#incr.index]">
										<th class="sort" style="width:<s:property value='viewRapportPROD.headersVSize[#incr.index]'/>;">
										<s:property /></th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:property /></th>
									</s:else>
								</s:iterator>
							</tr>
							<tr style="display: none;">
								<th style="display: none;"></th>
								<s:iterator value="viewRapportPROD.headersDLabel" var="head" status="incr">
									<th><s:property /></th>
								</s:iterator>
							</tr>
							<tr style="display: none;">
								<th style="display: none;"></th>
								<s:iterator value="viewRapportPROD.headersDLabel" var="head" status="incr">
									<th><s:checkbox name="viewRapportPROD.selectedColumns[%{#incr.index}]" theme="simple"></s:checkbox></th>
								</s:iterator>
							</tr>
							<tr>
								<th style="display: none;"></th>
								<s:iterator value="viewRapportPROD.headersVLabel" var="head" status="incr">
									<s:if test="viewRapportPROD.headersVisible[#incr.index]">
										<th><s:textarea name="viewRapportPROD.filterFields[%{#incr.index}]" value="%{viewRapportPROD.filterFields[#incr.index]}" theme="simple"></s:textarea></th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:textarea name="viewRapportPROD.filterFields[%{#incr.index}]" value="%{viewRapportPROD.filterFields[#incr.index]}" theme="simple"></s:textarea></th>
									</s:else>
								</s:iterator>
							</tr>
						</thead>
						<tbody>
							<s:if test="viewRapportPROD.content!=null && viewRapportPROD.content.size()>0">
								<s:iterator value="viewRapportPROD.content" var="line" status="incr1">
									<tr>
										<td style="display: none;"><s:checkbox name="viewRapportPROD.selectedLines[%{#incr1.index}]" onclick="updateCheckBox('viewRapportPROD',$(this));" theme="simple"></s:checkbox></td>
										<s:iterator value="#line" status="incr2">
											<s:if test="viewRapportPROD.headersVisible[#incr2.index]">
												<td onclick="updateCheckBoxGrid('viewRapportPROD;viewPilotagePROD',$(this));"><s:if test='"text".equals(viewRapportPROD.headersVType[#incr2.index])'>
														<s:textarea cssClass="noselect" readonly="true" onkeypress="updateCells(event,'viewRapportPROD',$(this));" name="viewRapportPROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewRapportPROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
														<%-- 										<s:property /> --%>
													</s:if> <s:else>
														<s:select cssClass="noselect" readonly="true" onkeypress="updateCells(event,'viewRapportPROD',$(this));" list="%{viewRapportPROD.headersVSelect[#incr2.index]}" name="viewRapportPROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewRapportPROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
													</s:else></td>
											</s:if>
											<s:else>
												<td style="display: none;"><s:if test='"text".equals(viewRapportPROD.headersVType[#incr2.index])'>
														<s:textarea cssClass="noselect" name="viewRapportPROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewRapportPROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
													</s:if> <s:else>
														<s:select cssClass="noselect" list="%{viewRapportPROD.headersVSelect[#incr2.index]}" name="viewRapportPROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewRapportPROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
													</s:else></td>
											</s:else>
										</s:iterator>
									</tr>
								</s:iterator>
							</s:if>
							<s:else>
								<s:if test="hasActionErrors()">
									<s:actionerror />
								</s:if>
								<s:else>
								</s:else>
							</s:else>
						</tbody>
					</table>
					<div class="alert">
						<s:property value="%{viewRapportPROD.message}" />
					</div>
					<div style="float: left;">
						<input type="submit" id="viewRapportPROD.select" value="Selectionner" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="selectRapportPROD" />
						<input type="submit" id="viewRapportPROD.sort" value="Trier" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="sortRapportPROD" />
					</div>
					<s:if test='1!=viewRapportPROD.nbPages'>
						<div style="float: left; margin-left: 20px;">
							<table style="width: 200px;">
								<tr>
									<td style="width: 40px;">Page :</td>
									<td style="width: 50px; background-color: #ffffff;"><s:textarea name="viewRapportPROD.idPage" value="%{viewRapportPROD.idPage}" theme="simple" /></td>
									<td style="width: 10px;">/</td>
									<td style="width: 25px;"><s:property value="%{viewRapportPROD.nbPages}" /></td>
									<td class="smallButton" onclick="gotoPage('viewRapportPROD',$(this),-999999999);">&lt;&lt;</td>
									<td class="smallButton" onclick="gotoPage('viewRapportPROD',$(this),-1);">&lt;</td>
									<td class="smallButton" onclick="gotoPage('viewRapportPROD',$(this),1);">&gt;</td>
									<td class="smallButton" onclick="gotoPage('viewRapportPROD',$(this),999999999);">&gt;&gt;</td>
								</tr>
							</table>
						</div>
					</s:if>
					<div style="margin-bottom: 30px;"></div>
				</s:if>
			</div>
		</s:form>
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		<s:form spellcheck="false" namespace="/" method="POST" theme="simple" enctype="multipart/form-data">
			<div class="container" style="clear: both; height: 65px;" id="viewEntrepotPROD">
			<!-- 	<div class="bandeau">Actions de suivi de production</div> -->
				<div style="float: left; width: 35%;">
					<h2>Charger des fichiers</h2>
					<s:file id="ActionsPROD.selectFiles" multiple="true" name="viewPilotagePROD.fileUpload" cssStyle="width:200px;" />
					<b style="margin-left:20px;">Entrepôt Cible :</b>
					<s:select cssStyle="width:%{viewEntrepotPROD.headersVSize[0]};" list="%{viewEntrepotPROD.getV(0,viewEntrepotPROD.content)}" value="%{viewEntrepotPROD.customValues['entrepotEcriture']}" name="viewEntrepotPROD.customValues['entrepotEcriture']" theme="simple" emptyOption="true"></s:select>
					<input type="submit" id="ActionsPROD.load" value="Charger" scope="viewEntrepotPROD;viewPilotagePROD;viewRapportPROD;viewFichierPROD;viewArchivePROD;" doAction="filesUploadPROD" onclick="updateConsoleState=true;" multipart="true" />
				</div>
				<div style="float: left; width: 40%;">
					<h2>Télécharger des archives</h2>
					<b style="margin-left: 25px;">Entrepôt de ***REMOVED*** :</b>
					<s:select cssStyle="width:%{viewEntrepotPROD.headersVSize[0]};" list="%{viewEntrepotPROD.getV(0,viewEntrepotPROD.content)}" value="%{viewEntrepotPROD.customValues['entrepotLecture']}" name="viewEntrepotPROD.customValues['entrepotLecture']" theme="simple" emptyOption="true"></s:select>
					<input type="submit" id="ActionsPROD.visualiserEntrepot" value="Visualiser le contenu" scope="viewEntrepotPROD;viewRapportPROD;viewPilotagePROD;-viewFichierPROD;viewArchivePROD;" doAction="visualiserEntrepotPROD" onclick="updateConsoleState=true;" style="margin-left: 25px;" />
				</div>
			</div>
		</s:form>
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		<s:form spellcheck="false" namespace="/" method="POST" theme="simple" enctype="multipart/form-data">
			<div class="container" id="viewArchivePROD">
				<s:if test="viewArchivePROD.isInitialized==true&&viewArchivePROD.isScoped==true">
					<div class="bandeau">
						<s:property value="%{viewArchivePROD.title}" />
					</div>
					<s:hidden name="viewArchivePROD.headerSortDLabel" value="" />
					<table class="fixedHeader">
						<thead>
							<tr>
								<th></th>
								<s:iterator value="viewArchivePROD.headersVLabel" var="head" status="incr">
									<s:if test="viewArchivePROD.headersVisible[#incr.index]">
										<th class="sort" style="width:<s:property value='viewArchivePROD.headersVSize[#incr.index]'/>;">
										<s:property /></th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:property /></th>
									</s:else>
								</s:iterator>
							</tr>
							<tr style="display: none;">
								<th></th>
								<s:iterator value="viewArchivePROD.headersDLabel" var="head" status="incr">
									<th><s:property /></th>
								</s:iterator>
							</tr>
							<tr>
								<th></th>
								<s:iterator value="viewArchivePROD.headersVLabel" var="head" status="incr">
									<s:if test="viewArchivePROD.headersVisible[#incr.index]">
										<th><s:textarea name="viewArchivePROD.filterFields[%{#incr.index}]" value="%{viewArchivePROD.filterFields[#incr.index]}" theme="simple"></s:textarea></th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:textarea name="viewArchivePROD.filterFields[%{#incr.index}]" value="%{viewArchivePROD.filterFields[#incr.index]}" theme="simple"></s:textarea></th>
									</s:else>
								</s:iterator>
							</tr>
						</thead>
						<tbody>
							<s:if test="viewArchivePROD.content!=null && viewArchivePROD.content.size()>0">
								<s:iterator value="viewArchivePROD.content" var="line" status="incr1">
									<tr>
										<td><s:checkbox name="viewArchivePROD.selectedLines[%{#incr1.index}]" theme="simple"></s:checkbox></td>
										<s:iterator value="#line" status="incr2">
											<s:if test="viewArchivePROD.headersVisible[#incr2.index]">
												<td><s:if test='"text".equals(viewArchivePROD.headersVType[#incr2.index])'>
														<s:textarea name="viewArchivePROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewArchivePROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
														<%-- 										<s:property /> --%>
													</s:if> <s:else>
														<s:select list="%{viewArchivePROD.headersVSelect[#incr2.index]}" name="viewArchivePROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewArchivePROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
														<%--  #{'PHASEV3':'PHASEV3', 'PHASEV2':'PHASEV2'}	%{viewArchivePROD.headersVSelect[#incr2.index]}  --%>
													</s:else></td>
											</s:if>
											<s:else>
												<td style="display: none;"><s:if test='"text".equals(viewArchivePROD.headersVType[#incr2.index])'>
														<s:textarea name="viewArchivePROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewArchivePROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
														<%-- 										<s:property /> --%>
													</s:if> <s:else>
														<s:select list="%{viewArchivePROD.headersVSelect[#incr2.index]}" name="viewArchivePROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewArchivePROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
														<%--  #{'PHASEV3':'PHASEV3', 'PHASEV2':'PHASEV2'}	%{viewArchivePROD.headersVSelect[#incr2.index]}  --%>
													</s:else></td>
											</s:else>
										</s:iterator>
									</tr>
								</s:iterator>
							</s:if>
							<s:else>
								<s:if test="hasActionErrors()">
									<s:actionerror />
								</s:if>
								<s:else>
								</s:else>
							</s:else>
						</tbody>
					</table>
					<div style="float: left;">
						<input type="submit" id="viewArchivePROD.downloadEnveloppe" value="Telecharger Enveloppe" scope="viewPilotagePROD;viewRapportPROD;viewEntrepotPROD;" doAction="downloadEnveloppeFromArchivePROD" ajax="false" />
					</div>
					<s:if test='1!=viewArchivePROD.nbPages'>
						<div style="float: left; margin-left: 20px;">
							<table style="width: 200px;">
								<tr>
									<td style="width: 40px;">Page :</td>
									<td style="width: 50px; background-color: #ffffff;"><s:textarea name="viewArchivePROD.idPage" value="%{viewArchivePROD.idPage}" theme="simple" /></td>
									<td style="width: 10px;">/</td>
									<td style="width: 25px;"><s:property value="%{viewArchivePROD.nbPages}" /></td>
									<td class="smallButton" onclick="gotoPage('viewArchivePROD',$(this),-999999999);">&lt;&lt;</td>
									<td class="smallButton" onclick="gotoPage('viewArchivePROD',$(this),-1);">&lt;</td>
									<td class="smallButton" onclick="gotoPage('viewArchivePROD',$(this),1);">&gt;</td>
									<td class="smallButton" onclick="gotoPage('viewArchivePROD',$(this),999999999);">&gt;&gt;</td>
								</tr>
							</table>
						</div>
					</s:if>
				</s:if>
			</div>
	</s:form>
	<s:form spellcheck="false" namespace="/" method="POST" theme="simple" enctype="multipart/form-data">
		<div class="container" id="viewFichierPROD" style="width: 98%;">
		<s:if test="viewFichierPROD.isInitialized==true&&viewFichierPROD.isScoped==true">
					<div class="bandeau">
						<s:property value="%{viewFichierPROD.title}" />
					</div>
					<s:hidden name="viewFichierPROD.headerSortDLabel" value="" />
					<table class="fixedHeader"  style="width:100%;">
						<thead>
							<tr>
								<th><input id="viewControle.checkAll" type="checkbox" /></th>
								<s:iterator value="viewFichierPROD.headersVLabel" var="head" status="incr">
									<s:if test="viewFichierPROD.headersVisible[#incr.index]">
										<th class="sort" style="width:<s:property value='viewFichierPROD.headersVSize[#incr.index]'/>;">
										<s:property /></th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:property /></th>
									</s:else>
								</s:iterator>
							</tr>
							<tr style="display: none;">
								<th></th>
								<s:iterator value="viewFichierPROD.headersDLabel" var="head" status="incr">
									<th><s:property /></th>
								</s:iterator>
							</tr>
							<tr>
								<th></th>
								<s:iterator value="viewFichierPROD.headersVLabel" var="head" status="incr">
									<s:if test="viewFichierPROD.headersVisible[#incr.index]">
										<th><s:textarea name="viewFichierPROD.filterFields[%{#incr.index}]" value="%{viewFichierPROD.filterFields[#incr.index]}" theme="simple"></s:textarea></th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:textarea name="viewFichierPROD.filterFields[%{#incr.index}]" value="%{viewFichierPROD.filterFields[#incr.index]}" theme="simple"></s:textarea></th>
									</s:else>
								</s:iterator>
							</tr>
						</thead>
						<tbody>
							<s:if test="viewFichierPROD.content!=null && viewFichierPROD.content.size()>0">
								<s:iterator value="viewFichierPROD.content" var="line" status="incr1">
									<tr>
										<td><s:checkbox name="viewFichierPROD.selectedLines[%{#incr1.index}]" theme="simple"></s:checkbox></td>
										<s:iterator value="#line" status="incr2">
											<s:if test="viewFichierPROD.headersVisible[#incr2.index]">
												<td><s:if test='"text".equals(viewFichierPROD.headersVType[#incr2.index])'>
														<s:textarea name="viewFichierPROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewFichierPROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
														<%-- 										<s:property /> --%>
													</s:if> <s:else>
														<s:select list="%{viewFichierPROD.headersVSelect[#incr2.index]}" name="viewFichierPROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewFichierPROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
														<%--  #{'PHASEV3':'PHASEV3', 'PHASEV2':'PHASEV2'}	%{viewFichierPROD.headersVSelect[#incr2.index]}  --%>
													</s:else></td>
											</s:if>
											<s:else>
												<td style="display: none;"><s:if test='"text".equals(viewFichierPROD.headersVType[#incr2.index])'>
														<s:textarea name="viewFichierPROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewFichierPROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
														<%-- 										<s:property /> --%>
													</s:if> <s:else>
														<s:select list="%{viewFichierPROD.headersVSelect[#incr2.index]}" name="viewFichierPROD.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewFichierPROD.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
														<%--  #{'PHASEV3':'PHASEV3', 'PHASEV2':'PHASEV2'}	%{viewFichierPROD.headersVSelect[#incr2.index]}  --%>
													</s:else></td>
											</s:else>
										</s:iterator>
									</tr>
								</s:iterator>
							</s:if>
							<s:else>
								<s:if test="hasActionErrors()">
									<s:actionerror />
								</s:if>
								<s:else>
								</s:else>
							</s:else>
						</tbody>
					</table>
					<div style="float: left;">
						<input type="submit" id="viewFichierPROD.select" value="Selectionner" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="selectFichierPROD" />
						<input type="submit" id="viewFichierPROD.sort" value="Trier" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="sortFichierPROD" />
						<s:submit type="submit" id="viewFichierPROD.downloadBd" value="Telecharger BD" doAction="downloadBdPROD" ajax="false" />
						<s:submit type="submit" id="viewFichierPROD.downloadFichier" value="Telecharger fichier" doAction="downloadFichierPROD" ajax="false" />
						<input type="submit" id="viewFichierPROD.downloadEnveloppe" value="Telecharger Enveloppe" doAction="downloadEnveloppePROD" ajax="false" />
						<input type="submit" id="viewFichierPROD.toDelete" value="Supprimer fichiers" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="toDeletePROD" onclick="updateConsoleState=true;" />
						<input type="submit" id="viewFichierPROD.toRestore" value="Rejouer fichiers" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="toRestorePROD" onclick="updateConsoleState=true;" />
						<input type="submit" id="viewFichierPROD.toRestoreArchive" value="Rejouer Archive" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="toRestoreArchivePROD" onclick="updateConsoleState=true;" />
						<input type="submit" id="viewFichierPROD.undoAction" value="Annulation action" scope="viewPilotagePROD;viewRapportPROD;viewFichierPROD;-viewArchivePROD;viewEntrepotPROD;" doAction="undoActionPROD" onclick="updateConsoleState=true;" />
					</div>
					<s:if test='1!=viewFichierPROD.nbPages'>
						<div style="float: left; margin-left: 20px;">
							<table style="width: 200px;">
								<tr>
									<td style="width: 40px;">Page :</td>
									<td style="width: 50px; background-color: #ffffff;"><s:textarea name="viewFichierPROD.idPage" value="%{viewFichierPROD.idPage}" theme="simple" /></td>
									<td style="width: 10px;">/</td>
									<td style="width: 25px;"><s:property value="%{viewFichierPROD.nbPages}" /></td>
									<td class="smallButton" onclick="gotoPage('viewFichierPROD',$(this),-999999999);">&lt;&lt;</td>
									<td class="smallButton" onclick="gotoPage('viewFichierPROD',$(this),-1);">&lt;</td>
									<td class="smallButton" onclick="gotoPage('viewFichierPROD',$(this),1);">&gt;</td>
									<td class="smallButton" onclick="gotoPage('viewFichierPROD',$(this),999999999);">&gt;&gt;</td>
								</tr>
							</table>
						</div>
					</s:if>
				</s:if>
			</div>
		</s:form>
	</div>
</body>
</html>
