<%@ taglib prefix="s" uri="/struts-tags" %>
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
			<li class="nav-item mt-auto"><a class="nav-link" data-target=".navbar-collapse" href="index"><spring:message
						code="header.home"/></a></li>
			<li class="nav-item mt-auto"><a class="nav-link" data-target=".navbar-collapse"
				href="selectFamilleNorme"><spring:message code="header.familyManagement"/></a>
			</li>
			<li class="nav-item mt-auto"><a class="nav-link" data-target=".navbar-collapse"
				href="selectNorme"><spring:message code="header.normManagement"/></a></li>
			<li class="nav-item mt-auto"><a class="nav-link" data-target=".navbar-collapse"
				href="selectListNomenclatures"><spring:message code="header.externalFile"/></a></li>
			<li class="nobullet mt-auto">
        		<label class="ml-5 mr-1" for="environnementTravail"><spring:message code="header.sandboxChoice"/> :</label>
      		</li>			
			<li class="nobullet mt-auto"><s:select id="environnementTravail"
					class="form-control mr-sm-2" list="envMap" name="bacASable"
					value="%{session.ENV}" theme="simple" emptyOption="false"
					required="true"></s:select></li>
<!-- 			<li class="nobullet mt-auto"><input -->
<!-- 				class="btn btn-secondary btn-sm" id="enterPilotageBAS8Button" -->
<!-- 				type="submit" doAction="enterPilotageBAS8" ajax="false" value="" -->
<!-- 				style="display: none;" /></li> -->
			<li class="nav-item mt-auto"><a class="nav-link" data-target=".navbar-collapse" href="enterPilotageBAS8"
				onclick="$(this).attr('href', 'enterPilotageBAS8?bacASable='+$('#environnementTravail option:selected').val());console.log($(this).attr('href'));"><spring:message code="header.manageEnvironment"/></a></li>
		</ul>
		


		<ul class="navbar-nav navbar-right ml-auto">
     	<li class="nobullet mr-auto mt-auto">
	   		<s:if test="isDataBaseOK">
	   		<button id="connectionCheck" class="btn btn-success btn-sm" type="button"><spring:message code="header.database.ok"/></button>
	   		</s:if>
		<s:else>
		    <button id="connectionCheck"  class="btn btn-danger btn-sm" type="button"><spring:message code="header.database.ko"/></button>
		</s:else>
      </li>


       <li class="nobullet mt-auto">
	      		<ul style="margin: 0; padding-inline-start: 0.5rem;">
	      		<c:choose>
				<c:when test="${current_locale == 'en'}">
					<li class="nobullet nav-link">	
						<s:url action="locale" var="localeFR"><s:param name="request_locale" >fr</s:param></s:url>
						<s:a href="%{localeFR}" >FR</s:a>
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
						<s:url action="locale" var="localeEN"><s:param name="request_locale" >en</s:param></s:url>
						<s:a href="%{localeEN}" >EN</s:a>
					</li>
				</c:otherwise>
				</c:choose>
     			</ul> 
      	</li>
    </ul>
    </div>
</nav>
