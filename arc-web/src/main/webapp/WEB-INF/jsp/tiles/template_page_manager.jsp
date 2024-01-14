<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>

<c:set var="allowResize" value="${param.allowResize}" />
<div class="d-flex">
		<c:if test="${allowResize}">
			<div class="ml-2 mr-2">
				<label class="text-body w-100"><spring:message code="gui.page.size" />:&nbsp;
					<input type="number" class="input-sm"
					name="${view.sessionName}.paginationSize"
					value="${view.paginationSize}" min="5" max="100" />
				</label>
			</div>
		</c:if>
		<c:if test='${1!=view.nbPages}'>
			<div class="ml-2 mr-2">
				<label class="text-body w-100">Page&nbsp;<input class="input-sm" type="number"
					min="1" max="${view.nbPages}" name="${view.sessionName}.idPage"
					value="${view.idPage}" />/${view.nbPages}
				</label>
			</div>
			<div class="ml-2 mr-2">
				<span class="smallButton"
					onclick="gotoPage('${view.sessionName}',${1 - view.idPage});">&lt;&lt;</span>
				<span class="smallButton"
					onclick="gotoPage('${view.sessionName}',-1);">&lt;</span> <span
					class="smallButton" onclick="gotoPage('${view.sessionName}',1);">&gt;</span>
				<span class="smallButton"
					onclick="gotoPage('${view.sessionName}',${view.nbPages - view.idPage});">&gt;&gt;</span>
				</div>
		</c:if>
</div>