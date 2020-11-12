<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>

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
				<select id="environnementTravail" class="form-control mr-sm-2" name="bacASable" required>
					<c:forEach items="${envMap.keySet()}" var="bas">
						<option value="${bas}" ${bas == sessionScope.ENV ? "class='font-weight-bold' selected" : ''}>${envMap.get(bas)}</option>
					</c:forEach>
				</select></li>
<!-- 			<li class="nobullet mt-auto"><input -->
<!-- 				class="btn btn-secondary btn-sm" id="enterPilotageBAS8Button" -->
<!-- 				type="submit" doAction="enterPilotageBAS8" ajax="false" value="" -->
<!-- 				style="display: none;" /></li> -->
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'envManagement' ? ' font-weight-bold' : ''}"" data-target=".navbar-collapse" href="enterPilotageBAS8"
				onclick="$(this).attr('href', 'enterPilotageBAS8?bacASable='+$('#environnementTravail option:selected').val());console.log($(this).attr('href'));"><spring:message code="header.manageEnvironment"/></a></li>
			<li class="nav-item mt-auto"><a class="nav-link${param.currentPage == 'envManagement' ? ' font-weight-bold' : ''}"" data-target=".navbar-collapse" href="selectExport"
				onclick="$(this).attr('href', 'selectExport?bacASable='+$('#environnementTravail option:selected').val());console.log($(this).attr('href'));"><spring:message code="header.export"/></a></li>

		</ul>
		


		<ul class="navbar-nav navbar-right ml-auto">
     	<li class="nobullet mr-auto mt-auto">
	   		<div id="connectionCheck" 
	   			class="btn ${isDataBaseOK ? 'btn-success' : 'btn-danger'} btn-sm" 
	   			><spring:message code="header.database.${isDataBaseOK ? 'ok' : 'ko'}"/></div>
      	</li>


       <li class="nobullet mt-auto">
	      		<ul style="margin: 0; padding-inline-start: 0.5rem;">
	      		<c:choose>
				<c:when test="${current_locale == 'en'}">
					<li class="nobullet nav-link">	
						<c:url value="locale.action" var="localeFR"><c:param name="request_locale"  value="fr" /></c:url>
						<a href="${localeFR}">FR</a>
					</li>
					<li class="nobullet btn-light nav-link" style="color:#000000;">
	      				${current_locale.toString().toUpperCase()}
	      			</li>
				</c:when>
				<c:otherwise>
					<li class="nobullet btn-light nav-link" style="color:#000000;">
	      				${current_locale.toString().toUpperCase()}
	      			</li>
					<li class="nobullet nav-link">
						<c:url value="locale.action" var="localeEN"><c:param name="request_locale"  value="en" /></c:url>
						<a href="${localeEN}">EN</a>
					</li>
				</c:otherwise>
				</c:choose>
     			</ul> 
      	</li>
    </ul>
    </div>
</nav>
