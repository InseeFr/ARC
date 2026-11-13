<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core" %>

<c:if test="${not empty viewFichierBAS.customValues['fileSearchPhase']}">
    <div class="p-2 border-bottom">
        <strong><spring:message code="${viewFichierBAS.customValues['fileSearchPhase']}"/> OK</strong>
        — <spring:message code="managementSandbox.search.context"/>
        <c:out value="${viewFichierBAS.customValues['fileSearchPattern']}"/>
        <button type="submit" class="btn btn-secondary btn-sm ml-2"
                doAction="clearPilotageSearchBAS" ajax="true"
                scope="viewPilotageBAS;viewRapportBAS;viewFichierBAS;-viewArchiveBAS;viewEntrepotBAS;">
            <spring:message code="managementSandbox.search.clear"/>
        </button>
    </div>
</c:if>
