<%@ page
	language="java"
	contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"
%>
<%@ taglib
	prefix="s"
	uri="/struts-tags"
%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>




<head>

<title>Index</title>
<link
	rel="stylesheet"
	href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css"
	integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T"
	crossorigin="anonymous"
/>
<link
	rel="stylesheet"
	type="text/css"
	href="<c:url value='/css/style.css' />"
/>


<script
	type="text/javascript"
	src="<c:url value='/js/jquery-2.1.3.min.js'/>"
></script>
<script
	src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.14.3/umd/popper.min.js"
	integrity="sha384-ZMP7rVo3mIykV+2+9J3UJ46jBk0WLaUAdn689aCwoqbBJiSnjAK/l8WvCWPIPm49"
	crossorigin="anonymous"
></script>
<script
	src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"
	integrity="sha384-JjSmVgyd0p3pXB1rRibZUAYoIIy6OrQ6VrjIEaFf/nJGzIxFDsf4x0xIM+B07jRM"
	crossorigin="anonymous"
></script>
<script
	type="text/javascript"
	src="<c:url value='/js/arc.js'/>"
></script>
<script
	type="text/javascript"
	src="<c:url value='/js/index.js'/>"
></script>
<script
	type="text/javascript"
	src="<c:url value='/js/component.js'/>"
></script>

<script>
	$(document).ready(function() {
		// Lancer un timer pour ne pas que la session expire
		sessionPersist();
	});
</script>

</head>

<body class='bg-light'>
<s:form
	spellcheck="false"
	namespace="/"
	method="POST"
	theme="simple"
	enctype="multipart/form-data"
>
	<%@include file="tiles/header.jsp"%>

	<div class="container-fluid">
	
	<div class="row justify-content-md-center">
	<div class="col-md-8">
				<div class="jumbotron jumbotron-fluid">
					<div class="container">
						<h1 class="display-4"><spring:message code="home.welcome"/></h1>
						<p class="lead">Version 20191025a</p>
					</div>
				</div>
			</div>
	</div>
	</div>
</s:form>
</body>
</html>