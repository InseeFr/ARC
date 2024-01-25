<%@taglib prefix="c" uri="jakarta.tags.core"%>
	<c:import url="tiles/csrf.jsp" />
	<c:import url="tiles/defaultcss.jsp" />
	<c:import url="tiles/defaultjs.jsp">
		<c:param name="pageJs" value="${param.pageJs}" />
	</c:import>