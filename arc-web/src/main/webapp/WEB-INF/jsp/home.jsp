<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html lang="${pageContext.response.locale}">
<head>
<link rel="icon" href="data:,"/>
<title>ARC</title>
</head>

<body class="bg-body">
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


Vous êtes sur la page d'accueil non sécurisée de l'application
</br>
<a href="./secure/index">Cliquer sur ce lien sécurisé et mettre la page en favori</a>

</body>
</html>