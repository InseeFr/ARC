<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>

<head>

	<title>Index</title>
	<c:import url="tiles/defaultcss.jsp" />
	<c:import url="tiles/defaultjs.jsp" />
	<script type="text/javascript" src="<c:url value='/js/index.js'/>"></script>
	<script type="text/javascript" src="<c:url value='/js/component.js'/>"></script>
	
	<script>
		$(document).ready(function() {
			// Lancer un timer pour ne pas que la session expire
			sessionPersist();
		});
	</script>

</head>

<body class='bg-light'>
	<form spellcheck="false" action="index" method="post"
		enctype="multipart/form-data">
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
							<p class="lead">Version 20191025a</p>
						</div>
					</div>
				</div>
			</div>
		</div>
	</form>
</body>
</html>