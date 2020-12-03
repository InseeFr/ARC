<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div><span class="fa fa-question-circle">&nbsp;</span><a href="<c:url value='${param.helpPage}'/>" target="_blank"><spring:message code="gui.help"/></a></div>