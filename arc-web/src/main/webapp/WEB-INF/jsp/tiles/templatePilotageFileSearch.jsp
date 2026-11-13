<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>

<div class="pilotage-file-search border-top pt-2 mt-2">
    <div class="font-weight-bold mb-2"><spring:message code="managementSandbox.search.label"/></div>
    <div class="d-flex align-items-center flex-wrap" style="gap: 8px;">
        <input type="text" id="fileSearchPatternInput"
               class="form-control form-control-sm pilotage-search-input"
               style="flex: 1 1 240px; min-width: 160px; width: auto;"
               aria-label="<spring:message code='managementSandbox.search.label' htmlEscape='true'/>"
               placeholder="Nom du fichier ou de l'archive"
               value="<c:out value="${viewFichierBAS.customValues['fileSearchPattern']}"/>" />
        <select id="fileSearchPhase" name="fileSearchPhase" m="js"
                class="form-control form-control-sm"
                style="flex: 0 0 160px; width: 160px;"
                aria-label="<spring:message code='managementSandbox.search.phase' htmlEscape='true'/>">
            <c:forEach items="${viewPilotageBAS.headersDLabel}" var="column">
                <c:if test="${column.endsWith('_ok')}">
                    <c:set var="searchPhase" value="${column.substring(0, column.length() - 3).toUpperCase()}" />
                    <option value="${searchPhase}" ${viewFichierBAS.customValues['fileSearchPhase'] == searchPhase ? 'selected' : ''}>
                        <spring:message code="${searchPhase}"/> OK
                    </option>
                </c:if>
            </c:forEach>
        </select>
        <button type="submit" class="btn btn-primary btn-sm pilotage-search-button"
                doAction="searchPilotageBAS" ajax="true"
                scope="viewPilotageBAS;viewRapportBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;"
                onclick="return preparePilotageFileSearch(this);">
            <spring:message code="managementSandbox.search.button"/>
        </button>
    </div>
    <c:import url="/WEB-INF/jsp/tiles/templatePilotageFileSearchContext.jsp" />
</div>
