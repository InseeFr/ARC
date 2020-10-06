<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>

<c:set
	var="view"
	value="${request.view}"
/>
<c:set var="allowResize" value="${param.allowResize}" />
  <div style="float: left; margin-left: 20px;">  	
	<c:if test='${1!=view.nbPages}'>
	    <table style="width: 200px;">
	      <tr>
	        <td style="width: 40px;">Page&nbsp;:</td>
	        <td style="width: 50px; background-color: #ffffff;">
	          <textarea name="${view.sessionName}.idPage" cols="" rows="">${view.idPage}</textarea>
	        </td>
	        <td style="width: 10px;">/</td>
	        <td style="width: 25px;">
	          ${view.nbPages}
	        </td>
	        <td class="smallButton" onclick="gotoPage('${view.sessionName}',$(this),${1 - view.idPage});">&lt;&lt;</td>
	        <td class="smallButton" onclick="gotoPage('${view.sessionName}',$(this),-1);">&lt;</td>
	        <td class="smallButton" onclick="gotoPage('${view.sessionName}',$(this),1);">&gt;</td>
	        <td class="smallButton" onclick="gotoPage('${view.sessionName}',$(this),${view.nbPages - view.idPage});">&gt;&gt;</td>
	      </tr>
	    </table>
	</c:if>
	<c:if test="${allowResize}">
		<table style="width: 200px;">
	      <tr>
	        <td style="width: 40px;">Nombre d'éléments par page&nbsp;:</td>
	        <td style="width: 50px; background-color: #ffffff;">
	          <input type="number" name="${view.sessionName}.paginationSize" value="${view.paginationSize}" min="5" max="100" />
	        </td>
	      </tr>
	    </table>
	</c:if>
  </div>