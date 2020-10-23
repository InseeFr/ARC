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
			<li class="nobullet mt-auto">
        		<label class="ml-5 mr-1" for="environnementTravail"><spring:message code="header.sandboxChoice"/> :</label>
      		</li>			
			<li class="nobullet mt-auto">
				<select id="environnementTravail" class="form-control mr-sm-2" name="bacASable" m="js" required>
					<c:forEach items="${envMap.keySet()}" var="bas">
						<option value="${bas}" ${bas == bacASable ? "class='font-weight-bold' selected" : ''}>${envMap.get(bas)}</option>
					</c:forEach>
				</select></li>
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'envManagement' ? ' font-weight-bold' : ''}"" data-target=".navbar-collapse" href="enterPilotageBAS"
				onclick="$(this).attr('href', 'enterPilotageBAS?bacASable='+$('#environnementTravail option:selected').val());console.log($(this).attr('href'));"><spring:message code="header.manageEnvironment"/></a></li>
		</ul>
		


		<ul class="navbar-nav navbar-right ml-auto">
     	<li class="nobullet mr-auto mt-auto">
	   		<div id="connectionCheck" 
	   			class="btn ${isDataBaseOK ? 'btn-success' : 'btn-danger'} btn-sm" 
	   			><spring:message code="header.database.${isDataBaseOK ? 'ok' : 'ko'}"/></div>
      	</li>


       <li class="nobullet mt-auto">
     		<ul style="margin: 0; padding-inline-start: 0.5rem;">
				<li class="nobullet nav-link">	
					<c:url value="" var="localeFR"><c:param name="lang"  value="fr" /></c:url>
					<a href="${localeFR}">FR</a>
				</li>
				<li class="nobullet nav-link">
					<c:url value="" var="localeEN"><c:param name="lang"  value="en" /></c:url>
					<a href="${localeEN}">EN</a>
				</li>
   			</ul>
      	</li>
    </ul>
    </div>
</nav>
