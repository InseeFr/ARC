
<!-- Voir le guide dans siera/siera-webutils/documentation -->

<!-- Pour appeler cette jsp, il faut copier les lignes suivantes: -->
<%-- <c:set var="view" value="${viewProfil}"  scope="request"/> --%>						
<%-- <c:import url="tiles/templateVObject.jsp"> --%>
<%-- 		<c:param name="taille" value ="col-md-4" /> --%>
<%-- 		<c:param name="btnSelect" value ="true" /> --%>
<%-- 		<c:param name="btnSee" value ="true" /> --%>
<%-- 		<c:param name="btnSort" value ="true" /> --%>
<%-- 		<c:param name="btnAdd" value ="true" /> --%>
<%-- 		<c:param name="btnUpdate" value ="true" /> --%>
<%-- 		<c:param name="btnDelete" value ="true" /> --%>
<%-- 		<c:param name="ligneAdd" value ="true" /> --%>
<%-- 		<c:param name="ligneFilter" value ="true" /> --%>
<%-- 		<c:param name="checkbox" value ="true" /> --%>
<%-- 		<c:param name="checkboxVisible" value ="true" /> --%>
<%-- 		<c:param name="multiSelection" value ="true" /> --%>
<%-- 		<c:param name="extraScopeAdd" value ="aScope" /> --%>
<%-- 		<c:param name="extraScopeDelete" value ="aScope" /> --%>
<%-- 		<c:param name="extraScopeUpdate" value ="aScope" /> --%>
<%-- 		<c:param name="extraScopeSee" value ="aScope" /> --%>
<%-- 		<c:param name="otherButton" value ="'[button1, button2 ...]" /> --%>
<%-- 	</c:import> --%>


<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>

<c:set var="view" value="${request.view}" />
<c:set
	var="viewType"
	value="${view.sessionName.substring(4)}"
/>
<c:set var="taille" value="${param.taille}" />
<c:set var="btnSelect" value="${param.btnSelect}" />
<c:set var="btnSee" value="${param.btnSee}" />
<c:set var="btnSort" value="${param.btnSort}" />
<c:set var="btnAdd" value="${param.btnAdd}" />
<c:set var="btnUpdate" value="${param.btnUpdate}" />
<c:set var="btnDelete" value="${param.btnDelete}" />
<c:set var="ligneAdd" value="${param.ligneAdd}" />
<c:set var="ligneFilter" value="${param.ligneFilter}" />
<c:set var="checkbox" value="${param.checkbox}" />
<c:set var="checkboxVisible" value="${param.checkboxVisible}" />
<c:set var="multiSelection" value="${param.multiSelection}" />
<c:set var="extraScopeSelect" value="${param.extraScopeSelect}" />
<c:set var="extraScopeUpdate" value="${param.extraScopeUpdate}" />
<c:set var="extraScopeDelete" value="${param.extraScopeDelete}" />
<c:set var="extraScopeAdd" value="${param.extraScopeAdd}" />
<c:set var="extraScopeSee" value="${param.extraScopeSee}" />
<c:set var="otherButton" value="${param.otherButton}" />
<c:set var="allowResize" value="${param.allowResize}" />
<div
	id="${view.sessionName}"
	class='${taille}'
>
	<c:if test="${view.isInitialized && view.isScoped}">
		<div class="row">
			<div class="col-md">
				<div class="card  no-margin">
					<div class="card-header bg-primary p-0">
						<h3 class="text-white m-1"><spring:message code="${view.title}"/></h3>
					</div>
					<div class="card-body p-0">
						<input name="${view.sessionName}.headerSortDLabel" type="hidden"
						id="${view.sessionName}_headerSortDLabel" />
						<table class="fixedHeader w-100 ">
							<thead>
								<tr>
									<c:if test="${checkbox.equals('true')}">
										<c:choose>
											<c:when test="${checkboxVisible.equals('false')}">
												<th style="display: none;"></th>
											</c:when>
											<c:otherwise>
												<th></th>
											</c:otherwise>
										</c:choose>
									</c:if>
									<c:forEach items="${view.headersVLabel}" var="head" varStatus="incr"> 
										<c:choose>
											<c:when test="${view.headersVisible[incr.index]}">
												<th
													class="sort"
													scope="col"
													style="width:${view.headersVSize[incr.index]}">
												<spring:message code="${view.headersVLabel[incr.index]}" text="${view.headersVLabel[incr.index]}"/>
												</th>
											</c:when>
											<c:otherwise>
												<th style="display: none;">${head}</th>
											</c:otherwise>
										</c:choose>
									</c:forEach>
							</tr>
							<tr style="display: none;">
									<th></th>
									<c:forEach items="${view.headersVLabel}" var="head" varStatus="incr"> 
										<th>${head}</th>
									</c:forEach>
							</tr>
								<tr style="display: none;">
									<c:if test="${checkbox.equals('true')}">
										<th ${checkboxVisible.equals('false') ? 'style="display: none;"':''}></th>
									</c:if>
									<c:forEach items="${view.headersVLabel}" var="head" varStatus="incr"> 
										<th>
											<input type="checkbox"
												name="${view.sessionName}.selectedColumns[${incr.index}]"
												value="true"
												id="${view.sessionName}_selectedColumns_${incr.index}_" 
												/> <input type="hidden"
												id="__checkbox_${view.sessionName}.selectedColumns_${incr.index}_"
												name="__checkbox_${view.sessionName}_selectedColumns[${incr.index}]"
												value="__checkbox_${view.selectedColumns[incr.index] ? 'true' : 'false'}"/>
										</th>
									</c:forEach>
							</tr>
								<c:if test="${ligneFilter.equals('true')}">
									<tr>
										<c:if test="${checkbox.equals('true')}">
											<c:choose>
												<c:when test="${checkboxVisible.equals('false')}">
													<th style="display: none;"></th>
												</c:when>
												<c:otherwise>
													<th style="text-align:center; font-weight:bold;font-size:1.5em;"></th>
												</c:otherwise>
											</c:choose>
										</c:if>
										<c:forEach items="${view.headersVLabel}" var="head" varStatus="incr"> 
											<c:choose>
												<c:when test="${view.headersVisible[incr.index]}">
													<th>
														<textarea 
															name="${view.sessionName}.filterFields[${incr.index}]"
															cols="" rows=""
															id="${view.sessionName}.filterFields_${incr.index}_">${view.filterFields[incr.index]}</textarea>
													</th>
												</c:when>
												<c:otherwise>
													<th style="display: none;">
														<textarea 
															name="${view.sessionName}.filterFields[${incr.index}]"
															cols="" rows=""
															id="${view.sessionName}.filterFields_${incr.index}_">${view.filterFields[incr.index]}</textarea>
													</th>
												</c:otherwise>
											</c:choose>
										</c:forEach>
									</tr>
								</c:if>
						</thead>
							<tbody>
								<c:if test="${view.content != null && view.content.size() > 0}">
									<c:forEach items="${view.content.iterator()}" var="line" varStatus="incr1">
										<tr>
											<c:if test="${checkbox.equals('true')}">
												<td ${checkboxVisible.equals('false') ? 'style="display: none;"':''}>
												<c:choose>
													<c:when test="${multiSelection.equals('true')}">
														<input type="checkbox"
															name="${view.sessionName}.selectedLines[${incr1.index}]"
															value="true"
															id="${view.sessionName}_selectedLines_${incr1.index}_"
															class="chooseLine"															
														/> <input type="hidden"
															id="__checkbox_${view.sessionName}_selectedLines_${incr1.index}_"
															name="__checkbox_${view.sessionName}.selectedLines[${incr1.index}]"
															value="__checkbox_${view.selectedLines[incr1.index] ? 'true' : 'false'}">
													</c:when>
													<c:otherwise>
														<input type="checkbox"
															name="${view.sessionName}.selectedLines[${incr1.index}]"
															value="true"
															id="${view.sessionName}_selectedLines_${incr1.index}_"
															onclick="updateCheckBox('${view.sessionName}',$(this));"
															class="chooseLine"								
														> <input type="hidden"
															id="__checkbox_${view.sessionName}_selectedLines_${incr1.index}_"
															name="__checkbox_${view.sessionName}.selectedLines[${incr1.index}]"
															value="__checkbox_${view.selectedLines[incr1.index] ? 'true' : 'false'}"												
														>
													</c:otherwise>
												</c:choose>
												</td>
											</c:if>
											<c:forEach items="${line.iterator()}" varStatus="incr2">
												<c:choose>
												<c:when test="${view.headersVisible[incr2.index]}">
													<td>
													<c:choose>
														<c:when test="${view.headersUpdatable[incr2.index]}" >
															<c:choose>
																<c:when
																	test="${view.headersVType[incr2.index].equals('text')}"
																>
																	<textarea name="${view.sessionName}.content.t[${incr1.index}].d[${incr2.index}]"
																		cols="" rows=""
																		id="${view.sessionName}_content_t_${incr1.index}__d_${incr2.index}_">${view.content.t[incr1.index].d[incr2.index]}</textarea>
																</c:when>
	
																<c:when
																	test="${view.headersVType[incr2.index].equals('datepicker')}'"
																>
																	<input type="text"
																		class="datepicker full-width"
																		type2="date"
																		dateFormat="yyyy-mm-dd"
																		name="${view.sessionName}.content.t[${incr1.index}].d[${incr2.index}]"
																		id="${view.sessionName}_content_t_${incr1.index}__d_${incr2.index}_"
																		value="${view.content.t[incr1.index].d[incr2.index]}"
																		theme="simple"
																	/>
																</c:when>
																<c:when
																	test="${view.headersVType[incr2.index].equals('multiSelect')}'"
																>
																	<textarea name="${view.sessionName}.content.t[${incr1.index}].d[${incr2.index}]"
																		cols="" rows=""
																		id="${view.sessionName}_content_t_${incr1.index}__d_${incr2.index}_"
																		>${view.content.t[incr1.index].d[incr2.index]}</textarea>
																</c:when>
																<c:otherwise>
																	<select class="w-100" 
																		id="${view.sessionName}_content_t_${incr1.index}__d_${incr2.index}_"
																		name="${view.sessionName}.content.t[${incr1.index}].d[${incr2.index}]">
																		<c:forEach items="${view.headersVSelect[incr2.index]}" var="option">
																			<option value="${option}" ${option == view.content.t[incr1.index].d[incr2.index] ? 'required' : ''}></option>
																		</c:forEach>
																	</select>
																</c:otherwise>
															</c:choose>
														</c:when> 
														<c:otherwise>
															<textarea name="${view.sessionName}.content.t[${incr1.index}].d[${incr2.index}]"
																cols="" rows=""
 																id="${view.sessionName}_content_t_${incr1.index}__d_${incr2.index}_" readonly>${view.content.t[incr1.index].d[incr2.index]}</textarea>
														</c:otherwise>
														</c:choose>
														</td>
												</c:when>
												<c:otherwise>
													<td style="display: none;">
														<c:choose>
															<c:when
																test="${view.headersVType[incr2.index].equals('text')}'"
															>
																<textarea id="${view.sessionName}.content.t[${incr1.index}].d[${incr2.index}]"
																	name="${view.sessionName}.content.t[${incr1.index}].d[${incr2.index}]" >${view.content.t[incr1.index].d[incr2.index]}</textarea>
															</c:when> <c:otherwise>
																<select class="w-100" id="${view.sessionName}.content.t[${incr1.index}].d[${incr2.index}]"
																	name="${view.sessionName}.content.t[${incr1.index}].d[${incr2.index}]">
																	<c:forEach items="${view.headersVSelect[incr2.index]}" var="option">
																		<option value="${option}" ${option == view.content.t[incr1.index].d[incr2.index] ? 'required' : ''}></option>
																	</c:forEach>
																</select>
															</c:otherwise>
														</c:choose>
													</td>
												</c:otherwise>
												</c:choose>
											</c:forEach>
										</tr>
									</c:forEach>
								</c:if>
								<c:if test="${ligneAdd.equals('true')}">
									<tr style="background-color: #fffaee;">
										<c:if test="${checkbox.equals('true')}">
											<td style="text-align:center; font-weight:bold;font-size:1.5em;">+</td>
										</c:if>
										<c:forEach items="${view.headersDLabel}" var="input" varStatus='incr'>
											<c:choose>
											<c:when test="${view.headersVisible[incr.index]}">
												<c:choose>
												<c:when test="${!view.headersUpdatable[incr.index]}">
													<td>
														<textarea name="${view.sessionName}.inputFields[${incr.index}]" readonly></textarea>
													</td>
												</c:when>
												<c:otherwise>
													<td>
													<c:choose>
													<c:when 
															test="${view.headersVType[incr.index].equals('text')}"
														>
															<textarea id="${view.sessionName}.inputFields[${incr.index}]"
																name="${view.sessionName}.inputFields[${incr.index}]" theme="simple"></textarea>
														</c:when>
														<c:when 
															test="${view.headersVType[incr.index].equals('datepicker')}"
														>
															<input type="text" class="datepicker full-width" type2="date"
																dateFormat="yyyy-mm-dd" 
																name="${view.sessionName}.inputFields[${incr.index}]"
																id="${view.sessionName}.inputFields[${incr.index}]"
																value="${view.sessionName.inputFields[incr.index]}"/>
														</c:when> 
														<c:when 
															test="${view.headersVType[incr.index].equals('multiSelect')}"
														>
															<select class="w-100" id="${view.sessionName}.inputFields[${incr.index}]"
																name="${view.sessionName}.inputFields[${incr.index}]" multiple>
																<option value=""></option>
																<c:forEach items="${view.headersVSelect[incr.index]}" var="option">
																	<option value="${option}"></option>
																</c:forEach>
															</select>
														</c:when>
														<c:otherwise>
															<select class="w-100" 
																id="${view.sessionName}.inputFields[${incr.index}]"
																name="${view.sessionName}.inputFields[${incr.index}]">
																<option value=""></option>
																<c:forEach items="${view.headersVSelect[incr.index]}" var="option">
																	<option value="${option}"></option>
																</c:forEach>
															</select>
														</c:otherwise>
														</c:choose>
														</td>
												</c:otherwise>
												</c:choose>
											</c:when>
											<c:otherwise>
												<td style="display: none;">
													<c:choose>
														<c:when
															test="${view.headersVType[incr.index].equals('text')}"
														>
															<textarea name="${view.sessionName}.inputFields[${incr.index}]"
																id="${view.sessionName}.inputFields[${incr.index}]"></textarea>
														</c:when>
														<c:otherwise>
															<select class="w-100" id="${view.sessionName}.inputFields[${incr.index}]"
																name="${view.sessionName}.inputFields[${incr.index}]" required>
																<c:forEach items="${view.headersVSelect[incr.index]}" var="option" varStatus="optionStatus">
																	<option value="${option}" ${optionsStatus.index == 0 ? 'selected' : ''}></option>
																</c:forEach>
															</select>
														</c:otherwise>
													</c:choose>
													</td>
											</c:otherwise>
											</c:choose>
										</c:forEach>
									</tr>
								</c:if>
						</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
		<div class="alert">
			${view.message}
		</div>
		<!--            Les boutons pour faire défiler les pages de résultat -->

		<div class="row mt-3">
			<!--             Les boutons d'action -->

			<div class='col-md'>
				<div
					class="btn-text-sm"
					role="group"
					style="float: left;"
				>
					<c:if test="${btnSelect.equals('true')}">
						<button
							id="${view.sessionName}.select"
							class="btn btn-secondary btn-sm "
							type="submit"
							doAction="select${viewType}"
							scope="${view.sessionName};"
							value="<spring:message code="gui.button.refresh"/>"
						><span class="fa fa-refresh">&nbsp;</span><spring:message code="gui.button.refresh"/></button>
					</c:if>
					<c:if test="${btnSee.equals('true')}">
						<button
							id="${view.sessionName}.see"
							style="display: none;"
							class="btn btn-secondary btn-sm"
							type="submit"
							doAction="select${viewType}"
							scope="${param.extraScopeSee}"
							value="<spring:message code="gui.button.see"/>"
						><span class="fa fa-eye-open">&nbsp;</span><spring:message code="gui.button.see"/></button>
					</c:if>
					<c:if test="${btnSort.equals('true')}">
						<button
							id="${view.sessionName}.sort"
							style="display: none;"
							class="btn btn-secondary btn-sm""
							type="submit"
							doAction="sort${viewType}"
							scope="${view.sessionName};"
							value="<spring:message code="gui.button.sort"/>"
						><span class="fa fa-sort">&nbsp;</span><spring:message code="gui.button.sort"/></button>
					</c:if>
					<c:if test="${btnAdd.equals('true')}">
						<button
							id="${view.sessionName}.add"
							class="btn btn-primary btn-sm "
							type="submit"
							doAction="add${viewType}"
							scope="${view.sessionName};${param.extraScopeAdd}"
							value="<spring:message code="gui.button.add"/>"
						><span class="fa fa-check">&nbsp;</span><spring:message code="gui.button.add"/></button>
					</c:if>
					<c:if test="${btnUpdate.equals('true')}">
						<button
							id="${view.sessionName}.update"
							class="btn btn-primary btn-sm "
							type="submit"
							doAction="update${viewType}"
							scope="${view.sessionName};${param.extraScopeUpdate}"
							value="<spring:message code="gui.button.update"/>"
						><span class="fa fa-save">&nbsp;</span><spring:message code="gui.button.update"/></button>
					</c:if>
					<c:if test="${btnDelete.equals('true')}">
						<button
							id="${view.sessionName}.delete"
							class="btn btn-primary btn-sm "
							type="submit"
							doAction="delete${viewType}"
							scope="${view.sessionName};${param.extraScopeDelete}"
							value="<spring:message code="gui.button.delete"/>"
						><span class="fa fa-remove">&nbsp;</span><spring:message code="gui.button.delete"/></button>
					</c:if>

					<c:if test="${otherButton != null}">
						<c:forEach items="${otherButton.split('\\\|')}" var="button">
							<c:out 
								value="${button}"
								escapeXml="false"
							/>
						</c:forEach>
					</c:if>
				</div>
				
				<c:import url="/jsp/tiles/template_page_manager.jsp">
					<c:param name="allowResize">${allowResize}</c:param>
				</c:import>
			</div>
		</div>
		<br>
	</c:if>
</div>