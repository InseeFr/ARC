<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<link rel="stylesheet" type="text/css" href="<s:url value='/css/style.css' />" />
	<script type="text/javascript" src="<s:url value='/js/jquery-2.1.3.min.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/arc.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/gererQuery.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/component.js'/>"></script>
	
</head>
<body>
<s:form spellcheck="false" namespace="/" method="POST" theme="simple">

<div class="container" id="viewTable" style="position: absolute; left:0px;">

 <div style="position: absolute; left:0px; top:0px; width:290px;height:850px;">
		<div style="position: absolute; top:0px; left:0px; z-index:2001; width:100%;">
			<input id="viewTable.select" type="submit" doAction="selectTable" scope="viewQuery;viewTable;" value="Afficher tables du schema"></input>
			<input id="viewTable.see" type="submit" doAction="seeTable" scope="viewQuery;viewTable;" value="Voir la table"></input>
			
		</div> 
		<div style="position: absolute; top:20px; left:0px; z-index:2001; width:100%;">
			<s:textarea m="" name="mySchema" value="%{mySchema}" cssStyle="width:150px; height:20px;"></s:textarea>
	    </div>
      
		<div style="position: absolute; top:60px; left:0px; z-index:2001; width:100%; height:800px; overflow-y:auto;">
          <s:if test="viewTable.isInitialized==true&&viewTable.isScoped==true">
          
            <div class="bandeau">
              <s:property value="%{viewTable.title}" />
            </div>
            <s:hidden name="viewTable.databaseColumnsSortLabel" value="" />
            <table class="fixedHeader" style="table-layout:auto; width:100%;">
              <thead>
                <tr>
                  <th></th>
                  <s:iterator value="viewTable.guiColumnsLabel" var="head" status="incr">
                    <s:if test="viewTable.visibleHeaders[#incr.index]">
                      <th class="sort" style="width:<s:property value='viewTable.guiColumnsSize[#incr.index]'/>;">
                        <s:property />
                      </th>
                    </s:if>
                    <s:else>
                      <th style="display: none;">
                        <s:property />
                      </th>
                    </s:else>
                  </s:iterator>
                </tr>
                <tr style="display: none;">
                  <th></th>
                  <s:iterator value="viewTable.databaseColumnsLabel" var="head" status="incr">
                    <th>
                      <s:property />
                    </th>
                  </s:iterator>
                </tr>
                <tr style="display: none;">
                  <th></th>
                  <s:iterator value="viewTable.databaseColumnsLabel" var="head" status="incr">
                    <th>
                      <s:checkbox name="viewTable.selectedColumns[%{#incr.index}]" theme="simple"></s:checkbox>
                    </th>
                  </s:iterator>
                </tr>
                <tr>
                  <th></th>
                  <s:iterator value="viewTable.guiColumnsLabel" var="head" status="incr">
                    <s:if test="viewTable.visibleHeaders[#incr.index]">
                      <th>
                        <s:textarea name="viewTable.filterFields[%{#incr.index}]" value="%{viewTable.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:if>
                    <s:else>
                      <th style="display: none;">
                        <s:textarea name="viewTable.filterFields[%{#incr.index}]" value="%{viewTable.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:else>
                  </s:iterator>
                </tr>
              </thead>
              <tbody>
                <s:if test="viewTable.content!=null && viewTable.content.size()>0">
                  <s:iterator value="viewTable.content" var="line" status="incr1">
                    <tr>
                      <td>
                        <s:checkbox name="viewTable.selectedLines[%{#incr1.index}]" onclick="updateCheckBox('viewTable',$(this));" theme="simple"></s:checkbox>
                      </td>
                      <s:iterator value="#line" status="incr2">
                        <s:if test="viewTable.visibleHeaders[#incr2.index]">
                          <td>
                            <s:if test='"text".equals(viewTable.guiColumnsType[#incr2.index])'>
                              <s:textarea name="viewTable.content.lines[%{#incr1.index}].data[%{#incr2.index}]" value="%{viewTable.content.lines[#incr1.index].data[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewTable.guiSelectedColumns[#incr2.index]}" name="viewTable.content.lines[%{#incr1.index}].data[%{#incr2.index}]" value="%{viewTable.content.lines[#incr1.index].data[#incr2.index]}" theme="simple"></s:select>
                            </s:else>
                          </td>
                        </s:if>
                        <s:else>
                          <td style="display: none;">
                            <s:if test='"text".equals(viewTable.guiColumnsType[#incr2.index])'>
                              <s:textarea name="viewTable.content.lines[%{#incr1.index}].data[%{#incr2.index}]" value="%{viewTable.content.lines[#incr1.index].data[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewTable.guiSelectedColumns[#incr2.index]}" name="viewTable.content.lines[%{#incr1.index}].data[%{#incr2.index}]" value="%{viewTable.content.lines[#incr1.index].data[#incr2.index]}" theme="simple"></s:select>
                            </s:else>
                          </td>
                        </s:else>
                      </s:iterator>
                    </tr>
                  </s:iterator>
                </s:if>
                <s:else>
                  <s:if test="hasActionErrors()">
                    <s:actionerror />
                  </s:if>
                  <s:else>
                  </s:else>
                </s:else>
                <tr>
                  <td></td>
                  <s:iterator value="viewTable.databaseColumnsLabel" var="input" status="incr">
                    <s:if test="viewTable.visibleHeaders[#incr.index]">
                      <td>
                        <s:if test='"text".equals(viewTable.guiColumnsType[#incr.index])'>
                          <s:textarea name="viewTable.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewTable.guiSelectedColumns[#incr.index]}" emptyOption="true" name="viewTable.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:if>
                    <s:else>
                      <td style="display: none;">
                        <s:if test='"text".equals(viewTable.guiColumnsType[#incr.index])'>
                          <s:textarea name="viewTable.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewTable.guiSelectedColumns[#incr.index]}" value="%{viewTable.guiSelectedColumns[#incr.index][0]}" name="viewTable.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:else>
                  </s:iterator>
                </tr>
              </tbody>
            </table>
            <s:if test='1!=viewTable.nbPages'>
              <div style="float: left; margin-left: 20px;">
                <table style="width: 200px;">
                  <tr>
                    <td style="width: 40px;">Page :</td>
                    <td style="width: 50px; background-color: #ffffff;">
                      <s:textarea name="viewTable.idPage" value="%{viewTable.idPage}" theme="simple" />
                    </td>
                    <td style="width: 10px;">/</td>
                    <td style="width: 25px;">
                      <s:property value="%{viewTable.nbPages}" />
                    </td>
                    <td class="smallButton" onclick="gotoPage('viewTable',$(this),-999999999);">&lt;&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewTable',$(this),-1);">&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewTable',$(this),1);">&gt;</td>
                    <td class="smallButton" onclick="gotoPage('viewTable',$(this),999999999);">&gt;&gt;</td>
                  </tr>
                </table>
              </div>
            </s:if>
        </s:if>
        </div>
    </div>

</div>


<div class="container" id="viewQuery" style="position: absolute; left:300px;">

    <div>
              <input id="viewQuery.select" type="submit" doAction="selectQuery" scope="viewQuery;viewTable;" value="Executer"></input>
    </div>
    <div style="position: absolute; left:0px; z-index:2000;">
   		<s:textarea m="" name="myQuery" value="%{myQuery}" cssStyle="width:300px;height:500px;"></s:textarea>
	</div>

  <div style="position: absolute; left:330px; top:20px; width:1000px; overflow-x:auto;">
  
  <div style="width:20000px;">
      	<div>
      		<s:property value="%{viewQuery.message}" />
    	</div>
      
        <s:if test="viewQuery.isInitialized==true&&viewQuery.isScoped==true">
          
            <div class="bandeau">
              <s:property value="%{viewQuery.title}" />
            </div>
            <s:hidden name="viewQuery.databaseColumnsSortLabel" value="" />
            <table class="fixedHeader" style="table-layout:auto; width:auto;">
              <thead>
                <tr>
                  <th></th>
                  <s:iterator value="viewQuery.guiColumnsLabel" var="head" status="incr">
                    <s:if test="viewQuery.visibleHeaders[#incr.index]">
                      <th class="sort" style="width:<s:property value='viewQuery.guiColumnsSize[#incr.index]'/>;">
                        <s:property />
                      </th>
                    </s:if>
                    <s:else>
                      <th style="display: none;">
                        <s:property />
                      </th>
                    </s:else>
                  </s:iterator>
                </tr>
                <tr style="display: none;">
                  <th></th>
                  <s:iterator value="viewQuery.databaseColumnsLabel" var="head" status="incr">
                    <th>
                      <s:property />
                    </th>
                  </s:iterator>
                </tr>
                <tr style="display: none;">
                  <th></th>
                  <s:iterator value="viewQuery.databaseColumnsLabel" var="head" status="incr">
                    <th>
                      <s:checkbox name="viewQuery.selectedColumns[%{#incr.index}]" theme="simple"></s:checkbox>
                    </th>
                  </s:iterator>
                </tr>
                <tr>
                  <th></th>
                  <s:iterator value="viewQuery.guiColumnsLabel" var="head" status="incr">
                    <s:if test="viewQuery.visibleHeaders[#incr.index]">
                      <th>
                        <s:textarea name="viewQuery.filterFields[%{#incr.index}]" value="%{viewQuery.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:if>
                    <s:else>
                      <th style="display: none;">
                        <s:textarea name="viewQuery.filterFields[%{#incr.index}]" value="%{viewQuery.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:else>
                  </s:iterator>
                </tr>
              </thead>
              <tbody>
                <s:if test="viewQuery.content!=null && viewQuery.content.size()>0">
                  <s:iterator value="viewQuery.content" var="line" status="incr1">
                    <tr>
                      <td>
                        <s:checkbox name="viewQuery.selectedLines[%{#incr1.index}]" onclick="updateCheckBox('viewQuery',$(this));" theme="simple"></s:checkbox>
                      </td>
                      <s:iterator value="#line" status="incr2">
                        <s:if test="viewQuery.visibleHeaders[#incr2.index]">
                          <td>
                            <s:if test='"text".equals(viewQuery.guiColumnsType[#incr2.index])'>
                              <s:textarea name="viewQuery.content.lines[%{#incr1.index}].data[%{#incr2.index}]" value="%{viewQuery.content.lines[#incr1.index].data[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewQuery.guiSelectedColumns[#incr2.index]}" name="viewQuery.content.lines[%{#incr1.index}].data[%{#incr2.index}]" value="%{viewQuery.content.lines[#incr1.index].data[#incr2.index]}" theme="simple"></s:select>
                            </s:else>
                          </td>
                        </s:if>
                        <s:else>
                          <td style="display: none;">
                            <s:if test='"text".equals(viewQuery.guiColumnsType[#incr2.index])'>
                              <s:textarea name="viewQuery.content.lines[%{#incr1.index}].data[%{#incr2.index}]" value="%{viewQuery.content.lines[#incr1.index].data[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewQuery.guiSelectedColumns[#incr2.index]}" name="viewQuery.content.lines[%{#incr1.index}].data[%{#incr2.index}]" value="%{viewQuery.content.lines[#incr1.index].data[#incr2.index]}" theme="simple"></s:select>
                            </s:else>
                          </td>
                        </s:else>
                      </s:iterator>
                    </tr>
                  </s:iterator>
                </s:if>
                <s:else>
                  <s:if test="hasActionErrors()">
                    <s:actionerror />
                  </s:if>
                  <s:else>
                  </s:else>
                </s:else>
                <tr>
                  <td></td>
                  <s:iterator value="viewQuery.databaseColumnsLabel" var="input" status="incr">
                    <s:if test="viewQuery.visibleHeaders[#incr.index]">
                      <td>
                        <s:if test='"text".equals(viewQuery.guiColumnsType[#incr.index])'>
                          <s:textarea name="viewQuery.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewQuery.guiSelectedColumns[#incr.index]}" emptyOption="true" name="viewQuery.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:if>
                    <s:else>
                      <td style="display: none;">
                        <s:if test='"text".equals(viewQuery.guiColumnsType[#incr.index])'>
                          <s:textarea name="viewQuery.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewQuery.guiSelectedColumns[#incr.index]}" value="%{viewQuery.guiSelectedColumns[#incr.index][0]}" name="viewQuery.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:else>
                  </s:iterator>
                </tr>
              </tbody>
            </table>
            <s:if test='1!=viewQuery.nbPages'>
              <div style="float: left; margin-left: 20px;">
                <table style="width: 200px;">
                  <tr>
                    <td style="width: 40px;">Page :</td>
                    <td style="width: 50px; background-color: #ffffff;">
                      <s:textarea name="viewQuery.idPage" value="%{viewQuery.idPage}" theme="simple" />
                    </td>
                    <td style="width: 10px;">/</td>
                    <td style="width: 25px;">
                      <s:property value="%{viewQuery.nbPages}" />
                    </td>
                    <td class="smallButton" onclick="gotoPage('viewQuery',$(this),-999999999);">&lt;&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewQuery',$(this),-1);">&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewQuery',$(this),1);">&gt;</td>
                    <td class="smallButton" onclick="gotoPage('viewQuery',$(this),999999999);">&gt;&gt;</td>
                  </tr>
                </table>
              </div>
            </s:if>
        </s:if>
        </div>
    </div>
    
       </div>
    
</s:form>
    
	

</body>
</html>