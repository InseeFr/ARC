<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %> 

<li class="nav-item mt-1 mb-1 text-left">
<c:choose>
    <c:when test="${param.currentPage == param.linkId}">
		<input class="pl-1 pr-1 nav-link font-weight-lightbold menu-input menu-selected" type="submit" doAction="<spring:message code="action.${param.linkRef}"/>" ajax="false" value="<spring:message code="header.${param.linkId}"/>"></input>
    </c:when>    
    <c:otherwise>
    	<input class="pl-1 pr-1 nav-link menu-input" type="submit" doAction="<spring:message code="action.${param.linkRef}"/>" ajax="false" value="<spring:message code="header.${param.linkId}"/>"></input>
    </c:otherwise>
</c:choose>
</li>

