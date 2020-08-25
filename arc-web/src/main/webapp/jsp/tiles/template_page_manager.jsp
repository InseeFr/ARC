<%@ taglib prefix="s" uri="/struts-tags"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib  prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="${current_locale}"/>

<s:set
	var="view"
	value="#request.view"
/>
<s:set var="allowResize">${param.allowResize}</s:set>
  <div style="float: left; margin-left: 20px;">  	
	<s:if test='1!=#view.nbPages'>
	    <table style="width: 200px;">
	      <tr>
	        <td style="width: 40px;">Page :</td>
	        <td style="width: 50px; background-color: #ffffff;">
	          <s:textarea name="%{#view.sessionName}.idPage" value="%{#view.idPage}" theme="simple" />
	        </td>
	        <td style="width: 10px;">/</td>
	        <td style="width: 25px;">
	          <s:property value="%{#view.nbPages}" />
	        </td>
	        <td class="smallButton" onclick="gotoPage('<s:property value="#view.sessionName"/>',$(this),-999999999);">&lt;&lt;</td>
	        <td class="smallButton" onclick="gotoPage('<s:property value="#view.sessionName"/>',$(this),-1);">&lt;</td>
	        <td class="smallButton" onclick="gotoPage('<s:property value="#view.sessionName"/>',$(this),1);">&gt;</td>
	        <td class="smallButton" onclick="gotoPage('<s:property value="#view.sessionName"/>',$(this),999999999);">&gt;&gt;</td>
	      </tr>
	    </table>
	</s:if>
	<s:if test="#allowResize.equals('true')">
		<table style="width: 200px;">
	      <tr>
	        <td style="width: 40px;">Nombre d'éléments par page :</td>
	        <td style="width: 50px; background-color: #ffffff;">
	          <s:textarea name="%{#view.sessionName}.paginationSize" value="%{#view.paginationSize}" theme="simple" />
	        </td>
	      </tr>
	    </table>
	</s:if>
  </div>