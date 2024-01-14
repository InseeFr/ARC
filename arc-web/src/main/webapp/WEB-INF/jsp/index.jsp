<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">

<head>
	<link rel="icon" href="data:,"/>

	<title><spring:message code="header.home" /></title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/index.js" />
	</c:import>
</head>

<body class="bg-body">
	<form spellcheck="false" action="index" method="post"
		enctype="multipart/form-data" accept-charset="UTF-8">
		<c:import url="tiles/header.jsp">
			<c:param name="currentPage" value="home" />
		</c:import>

		<div class="container-fluid">

			<div class="row justify-content-md-center">
				<div class="col-md-8">
					<div class="jumbotron jumbotron-fluid">
						<div class="container">
							<h1 class="display-4">
								<spring:message code="home.welcome" />
							</h1>
							<p>${version}</p>
						</div>
					</div>
				</div>
			</div>
			<hr />
			<div class="row justify-content-md-center">
				<div class="col-md-4">
					<c:set var="view" value="${viewIndex}" scope="request"/>
					<c:import url="tiles/templateVObject.jsp">
						<c:param name="taille" value ="col-md" />
					</c:import>
				</div>
			</div>
		</div>
	</form>
</body>
</html>