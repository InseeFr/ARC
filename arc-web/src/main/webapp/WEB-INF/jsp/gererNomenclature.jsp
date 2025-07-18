<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">
<head>
<link rel="icon" href="data:,"/>
<title><spring:message code="header.externalFile"/></title>
<c:import url="tiles/defaulthead.jsp">
<c:param name="pageJs" value="/js/gererNomenclature.js" />
</c:import>
</head>

<body class="bg-body">
<form
spellcheck="false"
action="selectListNomenclatures.action"
id="selectListNomenclatures"
method="post"
accept-charset="UTF-8"
>

<c:import url="tiles/header.jsp">
<c:param name="currentPage" value="externalFile" />
</c:import>

<div class="container-fluid">
<div class="row">
<!-- left column -->
<div class="col-md-3 border-right mt-2-25">
<div class="row">
<div class="col-md">
<!-- norm list -->
<c:set var="view" value="${viewListNomenclatures}"  scope="request"/>
<c:import url="tiles/templateVObject.jsp">
<c:param name="btnSelect" value ="true" />
<c:param name="btnSee" value ="true" />
<c:param name="btnSort" value ="true" />
<c:param name="btnAdd" value ="true" />
<c:param name="btnUpdate" value ="true" />
<c:param name="btnDelete" value ="true" />
<c:param name="ligneAdd" value ="true" />
<c:param name="ligneFilter" value ="true" />
<c:param name="checkbox" value ="true" />
<c:param name="checkboxVisible" value ="true" />
<c:param name="extraScopeAdd" value ="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" />
<c:param name="extraScopeDelete" value ="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" />
<c:param name="extraScopeUpdate" value ="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" />
<c:param name="extraScopeSee" value ="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" />
<c:param name="otherButton">
<button class="btn btn-primary btn-sm"
id="viewNomenclature.download" type="submit"
doAction="downloadListNomenclatures" ajax="false">
<span class="fa fa-download">&nbsp;</span>
<spring:message code="gui.button.downloadFileExport" />
</button>
<div class="input-group my-3">
<div class="custom-file">
<input
name="fileUpload"
type="file"
class="custom-file-input"
id="externalFileLoad"
size="40"
/> <label
class="custom-file-label"
for="externalFile"
><spring:message code="general.chooseFile"/></label>
</div>
<div class="input-group-append">
<button
class="btn btn-primary btn-sm"
id="btnFileUpload"
type="submit"
doAction="importListNomenclatures"
scope="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;"
multipart="true"
ajax="true"
><span class="fa fa-upload">&nbsp;</span> <spring:message code="managementSandbox.load"/></button>
</div>
</div>
</c:param>
</c:import>
</div>
</div>
</div>

<div class="col-md-2 border-left mt-2-25">
<div class="row">
<div class="col-md">
<!-- norm list -->
<c:set var="view" value="${viewSchemaNmcl}"  scope="request"/>
<c:import url="tiles/templateVObject.jsp">
<c:param name="btnSee" value ="true" />
<c:param name="btnSort" value ="true" />
<c:param name="ligneFilter" value ="true" />
<c:param name="extraScopeSee" value ="viewSchemaNmcl;" />
</c:import>
</div>
</div>
</div>

<div class="col-md-7 border-right mt-2-25">
<div class="row">
<div class="col-md">
<!-- norm list -->
<c:set var="view" value="${viewNomenclature}"  scope="request"/>
<c:import url="tiles/templateVObject.jsp">
<c:param name="btnSelect" value ="true" />
<c:param name="btnSee" value ="true" />
<c:param name="btnSort" value ="true" />
<c:param name="btnAdd" value ="false" />
<c:param name="btnUpdate" value ="false" />
<c:param name="btnDelete" value ="false" />
<c:param name="ligneAdd" value ="false" />
<c:param name="ligneFilter" value ="true" />
<c:param name="allowResize" value ="true" />
<c:param name="checkbox" value ="false" />
<c:param name="checkboxVisible" value ="false" />
<c:param name="extraScopeSee" value ="viewNomenclature;" />
</c:import>
</div>

</div>
</div>


</div>

</div>
</form>


</body>
</html>