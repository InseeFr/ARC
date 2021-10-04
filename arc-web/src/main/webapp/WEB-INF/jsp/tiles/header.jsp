<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>  


<div class="menu-box-style" style="display: flex;flex-direction:row;flex-wrap:nowrap;justify-content: space-between;position: fixed;top: 0;width: 100%;z-index: 2000;">
	
	<div>
	<button type="button" class="navbar-light btn-sm p-1 ml-1 border-0"
		style="outline:none;" data-toggle="collapse"
		data-target=".left-navbar"
		aria-controls="left-navbar" aria-expanded="false"
		aria-label="Left menu">
		<span class="navbar-toggler-icon"></span>
	</button>
	<c:forEach items="en,fr" var="lang">
		<c:choose>
			<c:when test="${fn:startsWith(pageContext.response.locale,lang)}">
				<a class="btn-sm text-body font-weight-bold"><u>${lang}</u></a>
			</c:when>
			<c:otherwise>
				<c:url value="" var="localeUrl">
					<c:param name="lang" value="${lang}" />
				</c:url>
				<a class="btn-sm text-body" href="${localeUrl}">${lang}</a>
			</c:otherwise>
		</c:choose>
	</c:forEach>
	</div>
	<span class="badge badge-light arc-page-title">
		<spring:message code="header.${param.currentPage}"/>
	</span>
	<div id="connectionCheck"
		class="btn-sm float-right btn ${isDataBaseOK ? 'btn-success' : 'btn-danger'}">
		<spring:message code="header.database.${isDataBaseOK ? 'ok' : 'ko'}" />
	</div>
</div>

<div class="mt-4-5"></div>

<nav class="navbar vw-menu-bar menu-box-style navbar-expand-lg navbar-light collapse pt-2 pb-0 pl-0 pr-0 align-items-baseline vh-menu-bar position-fixed left-navbar">
		<ul class="navbar-nav flex-column ml-1 mr-0 mb-4">
			<li><h5 class="">Accueil</h5></li>
			<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'home' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse" href="index"><spring:message
			code="header.home"/></a></li>
			<li><h5 class="mt-4">Règles</h5></li>
			<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'familyManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectFamilleNorme"><spring:message code="header.familyManagement"/></a>
			</li>
			<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'normManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectNorme"><spring:message code="header.normManagement"/></a></li>
			<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'externalFile' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectListNomenclatures"><spring:message code="header.externalFile"/></a></li>
			<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'webserviceManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectWebserviceContext"><spring:message code="header.webserviceManagement"/></a></li>
			<li><h5 class="mt-4">Bacs à sable</h5></li>
			<li class="nobullet mt-1 mb-1 text-left">
				<select id="environnementTravail" class="form-control mr-sm-2" name="bacASable" m="js" required>
					<c:forEach items="${envMap.keySet()}" var="bas">
						<option value="${bas}" ${bas == bacASable ? "class='font-weight-bold' selected" : ''}>${envMap.get(bas)}</option>
					</c:forEach>
				</select></li>
			<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'envManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse" href="enterPilotageBAS"
				onclick="$(this).attr('href', 'enterPilotageBAS?bacASable='+$('#environnementTravail option:selected').val());"><spring:message code="header.envManagement"/></a></li>
			<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'export' ? ' font-weight-bold' : ''}"" data-target=".navbar-collapse" href="selectExport"
				onclick="$(this).attr('href', 'selectExport?bacASable='+$('#environnementTravail option:selected').val());"><spring:message code="header.export"/></a></li>
			
			<li><h5 class="mt-4">Maintenance</h5></li>
			<c:if test="${userManagementActive}">
				<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'userManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
					href="selectGererUtilisateurs"><spring:message code="header.userManagement"/></a>
				</li>
			</c:if>
			<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'parameters' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectParameters"><spring:message code="header.parameters"/></a></li>
			<li class="nav-item mt-1 mb-1 text-left"><a class="pl-1 pr-1 nav-link${param.currentPage == 'operations' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectOperations"><spring:message code="header.operations"/></a></li>
    	</ul>
</nav>

<div class="float-left vh-menu-bar vw-menu-bar left-navbar collapse"></div>
