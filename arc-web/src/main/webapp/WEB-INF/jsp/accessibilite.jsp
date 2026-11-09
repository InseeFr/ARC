<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">
<head>
<link rel="icon" href="data:,"/>
<title><spring:message code="header.accessibility" /></title>
<c:import url="tiles/defaulthead.jsp">
<c:param name="pageJs" value="/js/index.js" />
</c:import>
</head>

<body class="bg-body">
<form spellcheck="false" action="accessibilite" method="post" accept-charset="UTF-8">
<c:import url="tiles/header.jsp">
<c:param name="currentPage" value="accessibility" />
</c:import>
<div class="container-fluid">
<div class="row justify-content-md-center">
<div class="col-md-8">

<h1 class="mt-3"><spring:message code="accessibility.title" /></h1>

<p><spring:message code="accessibility.commitment" /></p>

<p>
    <spring:message code="accessibility.strategy" />&#32;<a href="https://www.insee.fr/fr/information/7621795"><spring:message code="accessibility.schema.link" />&#32;${accessibilitySchemaPeriod}</a>.
</p>

<p>
    <spring:message code="accessibility.scope" />&#32;<c:choose><c:when test="${not empty accessibilitySiteUrl}"><a href="${accessibilitySiteUrl}">${accessibilitySiteUrl}</a></c:when><c:otherwise><spring:message code="accessibility.notProvided" /></c:otherwise></c:choose>.
</p>


<h2 class="mt-4"><spring:message code="accessibility.compliance.title" /></h2>
<p><spring:message code="accessibility.compliance.text" arguments="${application}" /></p>

<h2 class="mt-4"><spring:message code="accessibility.tests.title" /></h2>
<p><spring:message code="accessibility.tests.text" /></p>

<h2 class="mt-4"><spring:message code="accessibility.technologies.title" /></h2>
<ul>
    <li>HTML5</li>
    <li>CSS 3</li>
    <li>JavaScript (jQuery)</li>
    <li>Java</li>
</ul>

<h2 class="mt-4"><spring:message code="accessibility.contact.title" /></h2>
<p>
    <spring:message code="accessibility.contact.text" arguments="${application}" />&#32;<c:choose><c:when test="${not empty accessibilityMail}"><a href="mailto:${accessibilityMail}">${accessibilityMail}</a></c:when><c:otherwise><spring:message code="accessibility.notProvided" /></c:otherwise></c:choose>
</p>
<p>
    <spring:message code="accessibility.contact.rani" />&#32;<a href="mailto:${accessibilityRaniMail}"><spring:message code="accessibility.contact.rani.link" /></a>.
</p>

<h2 class="mt-4"><spring:message code="accessibility.remedies.title" /></h2>
<p><spring:message code="accessibility.remedies.text" /></p>
<p><spring:message code="accessibility.remedies.means" /></p>
<ul>
    <li><spring:message code="accessibility.remedies.write" /></li>
    <li><spring:message code="accessibility.remedies.delegate" /></li>
    <li>
        <spring:message code="accessibility.remedies.post" />
        <address class="mt-1 mb-0">Défenseur des droits<br />Libre réponse 71120<br />75342 Paris CEDEX 07</address>
    </li>
</ul>

</div>
</div>
</div>
</form>
</body>
</html>