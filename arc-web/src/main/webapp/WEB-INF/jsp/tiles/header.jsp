<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib prefix="c" uri="jakarta.tags.core"%>
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

<nav class="navbar vw-menu-bar menu-box-style navbar-expand-lg navbar-light collapse show pt-2 pb-0 pl-0 pr-0 align-items-baseline vh-menu-bar position-fixed left-navbar">
		<ul class="navbar-nav flex-column ml-1 mr-0 mb-4">
			<li><h4 class="">Menu</h4></li>
			<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="index"/><c:param name="linkId" value="home"/></c:import>

			<li><h5 class="mt-4">Règles</h5></li>
			<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="selectFamilleNorme" /><c:param name="linkId" value="familyManagement" /></c:import>
			<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="selectNorme" /><c:param name="linkId" value="normManagement" /></c:import>
			<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="selectListNomenclatures" /><c:param name="linkId" value="externalFile" /></c:import>
			<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="selectWebserviceContext" /><c:param name="linkId" value="webserviceManagement" /></c:import>
    		<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="selectEntrepot" /><c:param name="linkId" value="entrepot" /></c:import>

			<li><h5 class="mt-4">Bacs à sable</h5></li>
			<li class="nobullet mt-1 mb-1 text-left">
				<select id="environnementTravail" class="form-control mr-sm-2" name="bacASable" m="js" required>
					<c:forEach items="${envMap.keySet()}" var="bas">
						<option value="${bas}" ${bas == bacASable ? "class='font-weight-bold' selected" : ''}>${envMap.get(bas)}</option>
					</c:forEach>
				</select></li>

			<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="enterPilotageBAS" /><c:param name="linkId" value="envManagement" /></c:import>
			<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="selectExport" /><c:param name="linkId" value="export" /></c:import>
			
			<li><h5 class="mt-4">Maintenance</h5></li>
				<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="selectParameters" /><c:param name="linkId" value="parameters" /></c:import>
				<c:import url="tiles/template_header_link.jsp"><c:param name="linkRef" value="selectOperations" /><c:param name="linkId" value="operations" /></c:import>
    	</ul>
</nav>

<div class="float-left vh-menu-bar vw-menu-bar left-navbar collapse show"></div>
