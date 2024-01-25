<%@taglib prefix="c" uri="jakarta.tags.core"%>
<c:if test="${_csrf != null}">
	<meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
</c:if>