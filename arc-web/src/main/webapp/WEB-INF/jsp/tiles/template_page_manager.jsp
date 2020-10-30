<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<c:set var="allowResize" value="${param.allowResize}" />
<div style="float: left; margin-left: 20px;">
	<div class="row">
		<c:if test="${allowResize}">
			<div class="col-xs-6">
				<label><spring:message code="gui.page.size" />:&nbsp;
					<input type="number" class="input-sm w-25"
					name="${view.sessionName}.paginationSize"
					value="${view.paginationSize}" min="5" max="100" />
				</label>
			</div>
		</c:if>
		<c:if test='${1!=view.nbPages}'>
			<div class="col">
				<label>Page&nbsp;<input class="input-sm w-25" type="number"
					min="1" max="${view.nbPages}" name="${view.sessionName}.idPage"
					value="${view.idPage}" />/${view.nbPages}
				</label>
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
</div>