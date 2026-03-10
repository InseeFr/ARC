<!-- Tab navigation template with a vobject -->
<!-- column 1 : order number -->
<!-- column 2 : label of the tabulation menu -->

<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core"%>

<c:set
	var="viewType"
	value="${view.sessionName.substring(4)}"
/>
<c:set var="taille" value="${param.taille}" />
<div
	id="${view.sessionName}"
	class='bg-body ${taille}'
>

<%-- <input type="hidden" name="${view.sessionName}.sessionName" value="${view.sessionName}" m="js"> --%>
	<c:if test="${view.isInitialized && view.isScoped}">
		<div class="row">
			<div class="col-md">
				<div class="card no-margin overflow-auto w-fitcontent">
					<div class="card-body p-0 ${view.sessionName}">

						<input name="${view.sessionName}.headerSortDLabel" type="hidden"
						id="${view.sessionName}_headerSortDLabel" />

						<table class="fixedHeader w-auto border-top border-bottom" aria-describedby="${view.sessionName}_description">
						<thead class="d-flex">
								<c:if test="${view.content != null && view.content.size() > 0}">
									<c:forEach items="${view.content.iterator()}" var="line" varStatus="incr1">
										<c:set var="cellValue" value="${view.content.t[incr1.index].d[1]}" />
										<tr>
												<td class="vw-menu-bar border-top-0 border-bottom-0">
												<input type="checkbox"
													name="${view.sessionName}.selectedLines[${incr1.index}]"
													value="true"
													id="${view.sessionName}_selectedLines_${incr1.index}_"
													onclick="updateCheckBox('${view.sessionName}',$(this));"
													class="chooseLine d-none"
													${view.selectedLines[incr1.index] ? 'checked' : ''}								
												>
												<label for="${view.sessionName}_selectedLines_${incr1.index}_" class="control-me m-0 w-100 h-100"><spring:message code="${cellValue}" text="${cellValue}"/></label>
												</td>
										</tr>
									</c:forEach>
								</c:if>
						</thead>
						</table>
					</div>
				</div>
			</div>
		</div>

		<!--            Les boutons pour faire défiler les pages de résultat -->

		<div class="row ${view.sessionName}_collapse collapse show mt-0-2">
			<!--             Les boutons d'action -->

				<div class="col btn-text-sm btn-width" role="group">
					<button
						id="${view.sessionName}.select"
						class="btn btn-secondary btn-sm "
						type="submit"
						doAction="select${viewType}"
						scope="${view.sessionName};"
						value="<spring:message code="gui.button.refresh"/>"
						style='display:none'
					><span class="fa fa-refresh">&nbsp;</span><spring:message code="gui.button.refresh"/></button>
					<button
							id="${view.sessionName}.see"
							style="display: none;"
							class="btn btn-secondary btn-sm"
							type="submit"
							doAction="select${viewType}"
							scope="${view.sessionName};<spring:message code="tabNavScope.${param.extraScopeSee}"/>"
							value="<spring:message code="gui.button.see"/>"
						><span class="fa fa-eye-open">&nbsp;</span><spring:message code="gui.button.see"/></button>
				</div>
				
				<c:import url="/WEB-INF/jsp/tiles/template_page_manager.jsp">
					<c:param name="allowResize">${allowResize}</c:param>
				</c:import>
		</div>
	</c:if>
</div>