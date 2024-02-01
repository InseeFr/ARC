<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">
<c:if test="${scope==null}">
<head>
<link rel="icon" href="data:,"/>
<title><spring:message code="header.export"/></title>
<c:import url="tiles/defaulthead.jsp">
<c:param name="pageJs" value="/js/maintenanceOperations.js" />
</c:import>
</head>
</c:if>
<body class="bg-body">

<form
spellcheck="false"
action="selectOperations.action"
id="selectOperations"
method="post"
accept-charset="UTF-8"
>

<c:import url="tiles/header.jsp">
<c:param name="currentPage" value="operations" />
</c:import>

<div class="container-fluid">
<div class="row">
<div class="col-md-12">
<div class="row">
<div class="col-md">
<div id="viewOperations">
<button
id="viewOperations.generateErrorMessageInLogs"
class="btn btn-secondary btn-sm"
type="submit"
doAction="generateErrorMessageInLogsOperations"
scope="viewOperations;"
value="<spring:message code="gui.button.generateErrorMessageInLogs"/>"
><span class="fa fa-eye-open">&nbsp;</span><spring:message code="gui.button.generateErrorMessageInLogs"/></button>
</div>
</div>
</div>
</div>
</div>

<c:if test="${isKube}">
<hr />
<div class="row">
<div class="col-md-12">
<h2>
Kubernetes
</h2>
<div class="row">
<div class="col-md">
<div id="viewKubernetes">

<p>Url</p>
<textarea cols="60" rows="1" class="border" type="text" m="" name="url">${url}</textarea>
<p>Http Type</p>
<textarea cols="10" rows="1" class="border" type="text" m="" name="httpType">${httpType}</textarea>
<p>Content</p>
<textarea cols="60" rows="20" class="border" type="text" m="" name="json">${json}</textarea>
</br>

<input type="submit" class="btn btn-primary btn-sm" id="viewKubernetes.createPods" 
value="<spring:message code="gui.button.createPods"/>"
scope="viewKubernetes;" doAction="createPods" />

<input type="submit" class="btn btn-primary btn-sm"
id="viewKubernetes.deletePods"
value="<spring:message code="gui.button.deletePods"/>"
scope="viewKubernetes;" doAction="deletePods"
/>

</br>
<p>output</p>
<textarea cols="60" rows="20" class="border" type="text" m="" name="httpOutput">${httpOutput}</textarea>

</div>
</div>
</div>
</div>
</div>
</c:if>

</div>
</form>


</body>
</html>