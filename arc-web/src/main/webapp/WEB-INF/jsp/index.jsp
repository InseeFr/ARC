<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">

<head>

	<title>Index</title>
	<c:import url="tiles/defaulthead.jsp">
		<c:param name="pageJs" value="/js/index.js" />
	</c:import>
	
	<script>
		$(document).ready(function() {
			// Lancer un timer pour ne pas que la session expire
			sessionPersist();
		});
	</script>

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
		</div>
	</form>
</body>
</html>