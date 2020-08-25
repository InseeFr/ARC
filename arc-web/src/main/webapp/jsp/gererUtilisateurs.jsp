<%@page import="java.util.Enumeration"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<s:if test="scope==null">
	<head>
<title>Gestion des utilisateurs</title>
<link rel="stylesheet" type="text/css"
	href="<c:url value='/css/style.css' />" />
<script type="text/javascript"
	src="<c:url value='/js/jquery-2.1.3.min.js'/>"></script>
<script type="text/javascript" src="<c:url value='/js/arc.js'/>"></script>
<script type="text/javascript" src="<c:url value='/js/component.js'/>"></script>
<script type="text/javascript"
	src="<c:url value='/js/gererUtilisateurs.js'/>"></script>
<script type="text/javascript">
	$(document).on(
			"ready readyAgain",
			function() {
				$("tr>td:nth-child(3)>textarea:empty").on(
						"textchange",
						function() {
							console.log($(this).val().length);
							if ($(this).val().length == 0) {
								$(this).parents("tr").children("td").children(
										"select").removeAttr("required");
							} else {
								$(this).parents("tr").children("td").children(
										"select").attr("required", "true");
							}
						});
			});
</script>
	</head>
</s:if>
<body>
	<s:if test="scope==null">
		<div class="titre1">Gérer les profils et les utilisateurs</div>
		<div style="position: absolute; top: 40px; left: 10px;">
			<s:a class="onglet" href="accueil.action">Accueil</s:a>
			<s:a class="onglet" href="selectNorme.action">Gérer les normes</s:a>
			<s:a class="onglet" href="selectPilotageBAS.action">Bac à Sable 1</s:a>
			<s:a class="onglet" href="selectPilotageBAS2.action">Bac à Sable 2</s:a>
			<s:a class="onglet" href="selectPilotageBAS3.action">Bac à Sable 3</s:a>
			<s:a class="onglet" href="selectPilotageBAS4.action">Bac à Sable 4</s:a>
            <s:a class="onglet" href="selectPilotageBAS5.action">Bac à Sable 5</s:a>
      <s:a class="onglet" href="selectPilotageBAS6.action">Bac à Sable 6</s:a>
      <s:a class="onglet" href="selectPilotageBAS7.action">Bac à Sable 7</s:a>
      <s:a class="onglet" href="selectPilotageBAS8.action">Bac à Sable 8</s:a>
			<s:a class="onglet" href="selectPilotagePROD.action">Production</s:a>
		</div>
	</s:if>
	<!-- affichage de la liste des utilisateurs -->
	<div style="position: absolute; top: 75px; left: 0px; width: 450px;">
		<div style="position: relative;">
			<div class="container" id="viewListProfils">
				<s:if
					test="viewListProfils.isInitialized==true&&viewListProfils.isScoped==true">
					<s:form spellcheck="false" namespace="/" method="post"
						theme="simple" cssStyle="margin-bottom:60px;">
						<div class="bandeau">
							<s:property value="%{viewListProfils.title}" />
						</div>
						<s:hidden name="viewListProfils.headerSortDLabel" value="" />
						<table class="fixedHeader">
							<thead>
								<tr>
									<th></th>
									<s:iterator value="viewListProfils.headersVLabel" var="head"
										status="incr">
										<s:if test="viewListProfils.headersVisible[#incr.index]">
											<th class="sort"
												style="width:<s:property value='viewListProfils.headersVSize[#incr.index]'/>;">
												<s:property />
											</th>
										</s:if>
										<s:else>
											<th style="display: none;"><s:property /></th>
										</s:else>
									</s:iterator>
								</tr>
								<tr style="display: none;">
									<th></th>
									<s:iterator value="viewListProfils.headersDLabel" var="head"
										status="incr">
										<th><s:property /></th>
									</s:iterator>
								</tr>
								<tr style="display: none;">
									<th></th>
									<s:iterator value="viewListProfils.headersDLabel" var="head"
										status="incr">
										<th><s:checkbox
												name="viewListProfils.selectedColumns[%{#incr.index}]"
												theme="simple"></s:checkbox></th>
									</s:iterator>
								</tr>
								<tr>
									<th></th>
									<s:iterator value="viewListProfils.headersVLabel" var="head"
										status="incr">
										<s:if test="viewListProfils.headersVisible[#incr.index]">
											<th><s:textarea
													name="viewListProfils.filterFields[%{#incr.index}]"
													value="%{viewListProfils.filterFields[#incr.index]}"
													theme="simple"></s:textarea></th>
										</s:if>
										<s:else>
											<th style="display: none;"><s:textarea
													name="viewListProfils.filterFields[%{#incr.index}]"
													value="%{viewListProfils.filterFields[#incr.index]}"
													theme="simple"></s:textarea></th>
										</s:else>
									</s:iterator>
								</tr>
							</thead>
							<tbody>
								<s:if
									test="viewListProfils.content!=null && viewListProfils.content.size()>0">
									<s:iterator value="viewListProfils.content" var="line"
										status="incr1">
										<tr>
											<td><s:checkbox
													name="viewListProfils.selectedLines[%{#incr1.index}]"
													onclick="updateCheckBox('viewListProfils',$(this));"
													theme="simple"></s:checkbox></td>
											<s:iterator value="#line" status="incr2">
												<s:if test="viewListProfils.headersVisible[#incr2.index]">
													<td><s:if
															test='"text".equals(viewListProfils.headersVType[#incr2.index])'>
															<s:textarea
																name="viewListProfils.content.t[%{#incr1.index}].d[%{#incr2.index}]"
																value="%{viewListProfils.content.t[#incr1.index].d[#incr2.index]}"
																theme="simple"></s:textarea>
														</s:if> <s:else>
															<s:select
																list="%{viewListProfils.headersVSelect[#incr2.index]}"
																name="viewListProfils.content.t[%{#incr1.index}].d[%{#incr2.index}]"
																value="%{viewListProfils.content.t[#incr1.index].d[#incr2.index]}"
																theme="simple"></s:select>
														</s:else></td>
												</s:if>
												<s:else>
													<td style="display: none;"><s:if
															test='"text".equals(viewListProfils.headersVType[#incr2.index])'>
															<s:textarea
																name="viewListProfils.content.t[%{#incr1.index}].d[%{#incr2.index}]"
																value="%{viewListProfils.content.t[#incr1.index].d[#incr2.index]}"
																theme="simple"></s:textarea>
														</s:if> <s:else>
															<s:select
																list="%{viewListProfils.headersVSelect[#incr2.index]}"
																name="viewListProfils.content.t[%{#incr1.index}].d[%{#incr2.index}]"
																value="%{viewListProfils.content.t[#incr1.index].d[#incr2.index]}"
																theme="simple"></s:select>
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
								<tr>
									<td></td>
									<s:iterator value="viewListProfils.headersDLabel" var="input"
										status="incr">
										<s:if test="viewListProfils.headersVisible[#incr.index]">
											<td><s:if
													test='"text".equals(viewListProfils.headersVType[#incr.index])'>
													<s:textarea
														name="viewListProfils.inputFields[%{#incr.index}]"
														theme="simple"></s:textarea>
												</s:if> <s:else>
													<s:select
														list="%{viewListProfils.headersVSelect[#incr.index]}"
														emptyOption="true"
														name="viewListProfils.inputFields[%{#incr.index}]"
														theme="simple"></s:select>
												</s:else></td>
										</s:if>
										<s:else>
											<td style="display: none;"><s:if
													test='"text".equals(viewListProfils.headersVType[#incr.index])'>
													<s:textarea
														name="viewListProfils.inputFields[%{#incr.index}]"
														theme="simple"></s:textarea>
												</s:if> <s:else>
													<s:select
														list="%{viewListProfils.headersVSelect[#incr.index]}"
														value="%{viewListProfils.headersVSelect[#incr.index][0]}"
														name="viewListProfils.inputFields[%{#incr.index}]"
														theme="simple"></s:select>
												</s:else></td>
										</s:else>
									</s:iterator>
								</tr>
							</tbody>
						</table>
						<div>
							<s:property value="%{viewListProfils.message}" />
						</div>
						<div style="float: left;">
							<input id="viewListProfils.select" type="submit"
								doAction="selectGererUtilisateurs"
								scope="viewListProfils;viewListUtilisateursDuProfil;"
								value="Rafraîchir">
							</input>
							<input id="viewListProfils.see"
								type="submit" doAction="selectGererUtilisateurs"
								scope="viewListProfils;viewListUtilisateursDuProfil;"
								value="Voir">
							</input>
							<input id="viewListProfils.sort"
								type="submit" doAction="sortListProfils"
								scope="viewListProfils;" value="Trier">
							</input>
						</div>
						<div style="float: left;">
							<input id="viewListProfils.add" type="submit"
								doAction="addProfil" scope="viewListProfils;"
								value="Ajouter un groupe">
							</input>
							<input id="viewListProfils.delete" type="submit"
								doAction="deleteProfil" scope="viewListProfils;"
								value="Supprimer un groupe">
							</input>
							<!-- <input id="viewListProfils.update" type="submit" doAction="updateListProfils" scope="viewListProfils;" value="Mettre à jour"></input> -->
						</div>
						<s:if test='1!=viewListProfils.nbPages'>
							<div style="float: left; margin-left: 20px;">
								<table style="width: 200px;">
									<tr>
										<td style="width: 40px;">Page :</td>
										<td style="width: 50px; background-color: #ffffff;"><s:textarea
												name="viewListProfils.idPage"
												value="%{viewListProfils.idPage}" theme="simple" /></td>
										<td style="width: 10px;">/</td>
										<td style="width: 25px;"><s:property
												value="%{viewListProfils.nbPages}" /></td>
										<td class="smallButton"
											onclick="gotoPage('viewListProfils',$(this),-999999999);">&lt;&lt;</td>
										<td class="smallButton"
											onclick="gotoPage('viewListProfils',$(this),-1);">&lt;</td>
										<td class="smallButton"
											onclick="gotoPage('viewListProfils',$(this),1);">&gt;</td>
										<td class="smallButton"
											onclick="gotoPage('viewListProfils',$(this),999999999);">&gt;&gt;</td>
									</tr>
								</table>
							</div>
						</s:if>
					</s:form>
				</s:if>
			</div>
		</div>
		<!-- début insertion Profils -->
		<!-- VIEW TABLE UTILISATEURS -->
		<div class="container" id="viewListUtilisateursDuProfil"
			style="margin-bottom: 40px;">
			<s:if
				test="viewListUtilisateursDuProfil.isInitialized==true&&viewListUtilisateursDuProfil.isScoped==true">
				<s:form spellcheck="false" namespace="/" method="post"
					theme="simple">
					<div class="bandeau">
						<s:property value="%{viewListUtilisateursDuProfil.title}" />
					</div>
					<s:hidden name="viewListUtilisateursDuProfil.headerSortDLabel"
						value="" />
					<table class="fixedHeader">
						<thead>
							<tr>
								<th></th>
								<s:iterator value="viewListUtilisateursDuProfil.headersVLabel"
									var="head" status="incr">
									<s:if
										test="viewListUtilisateursDuProfil.headersVisible[#incr.index]">
										<th class="sort"
											style="width:<s:property value='viewListUtilisateursDuProfil.headersVSize[#incr.index]'/>;">
											<s:property />
										</th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:property /></th>
									</s:else>
								</s:iterator>
							</tr>
							<tr style="display: none;">
								<th></th>
								<s:iterator value="viewListUtilisateursDuProfil.headersDLabel"
									var="head" status="incr">
									<th><s:property /></th>
								</s:iterator>
							</tr>
							<tr style="display: none;">
								<th></th>
								<s:iterator value="viewListUtilisateursDuProfil.headersDLabel"
									var="head" status="incr">
									<th><s:checkbox
											name="viewListUtilisateursDuProfil.selectedColumns[%{#incr.index}]"
											theme="simple"></s:checkbox></th>
								</s:iterator>
							</tr>
							<tr>
								<th></th>
								<s:iterator value="viewListUtilisateursDuProfil.headersVLabel"
									var="head" status="incr">
									<s:if
										test="viewListUtilisateursDuProfil.headersVisible[#incr.index]">
										<th><s:textarea
												name="viewListUtilisateursDuProfil.filterFields[%{#incr.index}]"
												value="%{viewListUtilisateursDuProfil.filterFields[#incr.index]}"
												theme="simple"></s:textarea></th>
									</s:if>
									<s:else>
										<th style="display: none;"><s:textarea
												name="viewListUtilisateursDuProfil.filterFields[%{#incr.index}]"
												value="%{viewListUtilisateursDuProfil.filterFields[#incr.index]}"
												theme="simple"></s:textarea></th>
									</s:else>
								</s:iterator>
							</tr>
						</thead>
						<tbody>
							<s:if
								test="viewListUtilisateursDuProfil.content!=null && viewListUtilisateursDuProfil.content.size()>0">
								<s:iterator value="viewListUtilisateursDuProfil.content"
									var="line" status="incr1">
									<tr>
										<td><s:checkbox
												name="viewListUtilisateursDuProfil.selectedLines[%{#incr1.index}]"
												theme="simple"></s:checkbox></td>
										<s:iterator value="#line" status="incr2">
											<s:if
												test="viewListUtilisateursDuProfil.headersVisible[#incr2.index]">
												<td><s:if
														test='"text".equals(viewListUtilisateursDuProfil.headersVType[#incr2.index])'>
														<s:textarea
															name="viewListUtilisateursDuProfil.content.t[%{#incr1.index}].d[%{#incr2.index}]"
															value="%{viewListUtilisateursDuProfil.content.t[#incr1.index].d[#incr2.index]}"
															theme="simple"
															readonly="%{viewListUtilisateursDuProfil.headersUpdatable[#incr2.index]}"></s:textarea>
													</s:if> <s:else>
														<s:select
															list="%{viewListUtilisateursDuProfil.headersVSelect[#incr2.index]}"
															name="viewListUtilisateursDuProfil.content.t[%{#incr1.index}].d[%{#incr2.index}]"
															value="%{viewListUtilisateursDuProfil.content.t[#incr1.index].d[#incr2.index]}"
															theme="simple"></s:select>
													</s:else></td>
											</s:if>
											<s:else>
												<td style="display: none;"><s:if
														test='"text".equals(viewListUtilisateursDuProfil.headersVType[#incr2.index])'>
														<s:textarea
															name="viewListUtilisateursDuProfil.content.t[%{#incr1.index}].d[%{#incr2.index}]"
															value="%{viewListUtilisateursDuProfil.content.t[#incr1.index].d[#incr2.index]}"
															theme="simple"></s:textarea>
													</s:if> <s:else>
														<s:select
															list="%{viewListUtilisateursDuProfil.headersVSelect[#incr2.index]}"
															name="viewListUtilisateursDuProfil.content.t[%{#incr1.index}].d[%{#incr2.index}]"
															value="%{viewListUtilisateursDuProfil.content.t[#incr1.index].d[#incr2.index]}"
															theme="simple"></s:select>
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
							<tr>
								<td></td>
								<s:iterator value="viewListUtilisateursDuProfil.headersDLabel"
									var="input" status="incr">
									<s:if
										test="viewListUtilisateursDuProfil.headersVisible[#incr.index]">
										<td><s:if
												test='"text".equals(viewListUtilisateursDuProfil.headersVType[#incr.index])'>
												<s:textarea
													name="viewListUtilisateursDuProfil.inputFields[%{#incr.index}]"
													theme="simple"></s:textarea>
											</s:if> <s:else>
												<s:select
													list="%{viewListUtilisateursDuProfil.headersVSelect[#incr.index]}"
													emptyOption="true"
													name="viewListUtilisateursDuProfil.inputFields[%{#incr.index}]"
													theme="simple"></s:select>
											</s:else></td>
									</s:if>
									<s:else>
										<td style="display: none;"><s:if
												test='"text".equals(viewListUtilisateursDuProfil.headersVType[#incr.index])'>
												<s:textarea
													name="viewListUtilisateursDuProfil.inputFields[%{#incr.index}]"
													theme="simple"></s:textarea>
											</s:if> <s:else>
												<s:select
													list="%{viewListUtilisateursDuProfil.headersVSelect[#incr.index]}"
													value="%{viewListUtilisateursDuProfil.headersVSelect[#incr.index][0]}"
													name="viewListUtilisateursDuProfil.inputFields[%{#incr.index}]"
													theme="simple"></s:select>
											</s:else></td>
									</s:else>
								</s:iterator>
							</tr>
						</tbody>
					</table>
					<div class="alert">
						<s:property value="%{viewListUtilisateursDuProfil.message}" />
					</div>
					<div style="float: left;">
						<input id="viewListUtilisateursDuProfil.select" type="submit"
							doAction="selectTableUtilisateur"
							scope="viewListUtilisateursDuProfil;" value="Rafraîchir">
						</input>
						<input id="viewListUtilisateursDuProfil.see" type="submit"
							doAction="selectTableUtilisateur"
							scope="viewListUtilisateursDuProfil;" value="Voir">
						</input>
						<input id="viewListUtilisateursDuProfil.sort" type="submit"
							doAction="sortTableUtilisateur"
							scope="viewListUtilisateursDuProfil;" value="Trier">
						</input>
						<input id="viewListUtilisateursDuProfil.add" type="submit"
							doAction="addTableUtilisateur"
							scope="viewListUtilisateursDuProfil;" value="Ajouter un agent">
						</input>
						<input id="viewListUtilisateursDuProfil.delete" type="submit"
							doAction="deleteTableUtilisateur"
							scope="viewListUtilisateursDuProfil;" value="Supprimer un agent">
						</input>
					</div>
					<s:if test='1!=viewListUtilisateursDuProfil.nbPages'>
						<div style="float: left; margin-left: 20px;">
							<table style="width: 200px;">
								<tr>
									<td style="width: 40px;">Page :</td>
									<td style="width: 50px; background-color: #ffffff;"><s:textarea
											name="viewListUtilisateursDuProfil.idPage"
											value="%{viewListUtilisateursDuProfil.idPage}" theme="simple" />
									</td>
									<td style="width: 10px;">/</td>
									<td style="width: 25px;"><s:property
											value="%{viewListUtilisateursDuProfil.nbPages}" /></td>
									<td class="button"
										onclick="gotoPage('viewListUtilisateursDuProfil',$(this),-999999999);">&lt;&lt;</td>
									<td class="button"
										onclick="gotoPage('viewListUtilisateursDuProfil',$(this),-1);">&lt;</td>
									<td class="button"
										onclick="gotoPage('viewListUtilisateursDuProfil',$(this),1);">&gt;</td>
									<td class="button"
										onclick="gotoPage('viewListUtilisateursDuProfil',$(this),999999999);">&gt;&gt;</td>
								</tr>
							</table>
						</div>
					</s:if>
				</s:form>
			</s:if>
		</div>
		<!-- fin insertion utilisateurs -->
	</div>
</body>
</html>