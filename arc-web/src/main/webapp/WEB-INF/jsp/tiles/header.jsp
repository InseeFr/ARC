<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<nav class="navbar navbar-expand-lg navbar-light mb-3 mt-2">
  <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
  </button>
  <div class="collapse navbar-collapse" id="navbarSupportedContent">
		<ul class="navbar-nav mr-0 mt-auto">
			<li class="nobullet mt-auto"><a class="navbar-brand"
				href="index"><spring:message code="header.applicationHome"/></a></li>
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'home' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse" href="index"><spring:message
						code="header.home"/></a></li>
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'familyManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectFamilleNorme"><spring:message code="header.familyManagement"/></a>
			</li>
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'normManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectNorme"><spring:message code="header.normManagement"/></a></li>
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'externalFile' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectListNomenclatures"><spring:message code="header.externalFile"/></a></li>
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'webserviceManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectWebserviceContext"><spring:message code="header.webserviceManagement"/></a></li>
			<li class="nobullet mt-auto">
        		<label class="ml-5 mr-1" for="environnementTravail"><spring:message code="header.sandboxChoice"/> :</label>
      		</li>			
			<li class="nobullet mt-auto">
				<select id="environnementTravail" class="form-control mr-sm-2" name="bacASable" m="js" required>
					<c:forEach items="${envMap.keySet()}" var="bas">
						<option value="${bas}" ${bas == bacASable ? "class='font-weight-bold' selected" : ''}>${envMap.get(bas)}</option>
					</c:forEach>
				</select></li>
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'envManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse" href="enterPilotageBAS"
				onclick="$(this).attr('href', 'enterPilotageBAS?bacASable='+$('#environnementTravail option:selected').val());"><spring:message code="header.manageEnvironment"/></a></li>
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'export' ? ' font-weight-bold' : ''}"" data-target=".navbar-collapse" href="selectExport"
				onclick="$(this).attr('href', 'selectExport?bacASable='+$('#environnementTravail option:selected').val());"><spring:message code="header.export"/></a></li>
			<c:if test="${userManagementActive}">
				<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'userManagement' ? ' font-weight-bold' : ''}" data-target=".navbar-collapse"
				href="selectGererUtilisateurs"><spring:message code="header.userManagement"/></a>
			</li>
			</c:if>
		</ul>
		


		<ul class="navbar-nav navbar-right ml-auto">
     	<li class="nobullet mr-auto mt-auto">
	   		<div id="connectionCheck" 
	   			class="btn ${isDataBaseOK ? 'btn-success' : 'btn-danger'} btn-sm" 
	   			><spring:message code="header.database.${isDataBaseOK ? 'ok' : 'ko'}"/></div>
      	</li>


       <li class="nobullet mt-auto">
     		<ul style="margin: 0; padding-inline-start: 0.5rem;">
     		<c:forEach items="en,fr" var="lang" >
     			<c:choose>
     				<c:when test="${pageContext.response.locale == lang}">
     					<li class="nobullet">	
							<a class="btn-sm font-weight-bold">${lang}</a>
						</li>
     				</c:when>
		     		<c:otherwise>
						<li class="nobullet">								
							<c:url value="" var="localeUrl"><c:param name="lang"  value="${lang}" /></c:url>
							<a class="btn-sm text-dark" href="${localeUrl}">${lang}</a>
						</li>
					</c:otherwise>
     			</c:choose>
			</c:forEach>
   			</ul>
      	</li>
    </ul>
    </div>
</nav>
