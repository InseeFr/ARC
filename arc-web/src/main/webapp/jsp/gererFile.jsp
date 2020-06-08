<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<link rel="stylesheet" type="text/css" href="<s:url value='/css/style.css' />" />
	<script type="text/javascript" src="<s:url value='/js/jquery-2.1.3.min.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/arc.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/gererFile.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/component.js'/>"></script>
	
</head>
<body>
<s:form spellcheck="false" namespace="/" method="POST" theme="simple">

<div class="container" id="viewDirIn" style="position: absolute; left:0px;">

 <div style="position: absolute; left:0px; top:0px; width:400px; height:850px;">
		<div style="position: absolute; top:0px; left:0px; z-index:2001; width:100%;">
			<s:textarea m="" action='select' name="dirIn" value="%{dirIn}" cssStyle="width:500px;height:20px;"></s:textarea>
	    </div>

		<div style="position: absolute; top:30px; left:0px; z-index:2001; width:100%;">
			<input id="viewDirIn.select" type="submit" doAction="selectDirIn" scope="viewDirIn;" value="Visualiser le répertoire"></input>
			<input id="viewDirIn.see" type="submit" doAction="seeDirIn" scope="viewDirIn;" value="Visualiser le répertoire" style="display:none;"></input>
			<input id="viewDirIn.transfer" type="submit" doAction="transferDirIn" scope="viewDirIn;viewDirOut;" value="Transférer"></input>
			<input id="viewDirIn.add" type="submit" doAction="addDirIn" scope="viewDirIn;viewDirOut;" value="Créer" style="display:none;"></input>
			<input id="viewDirIn.del" type="submit" doAction="delDirIn" scope="viewDirIn;viewDirOut;" value="Effacer"></input>
			<input id="viewDirIn.update" type="submit" doAction="renameIn" scope="viewDirIn;viewDirOut;" value="Mettre à jour" style="display:none;"></input>
			<input id="viewDirIn.copy" type="submit" doAction="copyDirIn" scope="viewDirIn;viewDirOut;" value="Copier"></input>
		</div> 
      
		<div style="position: absolute; top:60px; left:0px; z-index:2001; width:100%; height:800px; overflow-y:auto;">
          <s:if test="viewDirIn.isInitialized==true&&viewDirIn.isScoped==true">
          
            <div class="bandeau">
              <s:property value="%{viewDirIn.title}" />
            </div>
            <s:hidden name="viewDirIn.databaseColumnsSort" value="" />
            <table class="fixedHeader" style="table-layout:auto; width:100%;">
              <thead>
                <tr>
                  <th></th>
                  <s:iterator value="viewDirIn.guiColumnsLabel" var="head" status="incr">
                    <s:if test="viewDirIn.visibleHeaders[#incr.index]">
                      <th class="sort" style="width:<s:property value='viewDirIn.guiColumnsSize[#incr.index]'/>;">
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
                  <s:iterator value="viewDirIn.databaseColumnsLabel" var="head" status="incr">
                    <th>
                      <s:property />
                    </th>
                  </s:iterator>
                </tr>
                <tr style="display: none;">
                  <th></th>
                  <s:iterator value="viewDirIn.databaseColumnsLabel" var="head" status="incr">
                    <th>
                      <s:checkbox name="viewDirIn.selectedColumns[%{#incr.index}]" theme="simple"></s:checkbox>
                    </th>
                  </s:iterator>
                </tr>
                <tr>
                  <th></th>
                  <s:iterator value="viewDirIn..guiColumnsLabel" var="head" status="incr">
                    <s:if test="viewDirIn.visibleHeaders[#incr.index]">
                      <th>
                        <s:textarea name="viewDirIn.filterFields[%{#incr.index}]" value="%{viewDirIn.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:if>
                    <s:else>
                      <th style="display: none;">
                        <s:textarea name="viewDirIn.filterFields[%{#incr.index}]" value="%{viewDirIn.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:else>
                  </s:iterator>
                </tr>
              </thead>
              <tbody>
                <s:if test="viewDirIn.content!=null && viewDirIn.content.size()>0">
                  <s:iterator value="viewDirIn.content" var="line" status="incr1">
                    <tr>
                      <td>
                        <s:checkbox name="viewDirIn.selectedLines[%{#incr1.index}]" onclick="if ($(this).parent().siblings().eq(1).text().trim()=='true') {updateCheckBox('viewDirIn',$(this));} else {return true;}" theme="simple"></s:checkbox>
                      </td>
                      <s:iterator value="#line" status="incr2">
                        <s:if test="viewDirIn.visibleHeaders[#incr2.index]">
                          <td>
                            <s:if test='"text".equals(viewDirIn.guiColumnsType[#incr2.index])'>
                              <s:textarea name="viewDirIn.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewDirIn.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewDirIn.guiSelectedColumns[#incr2.index]}" name="viewDirIn.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewDirIn.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
                            </s:else>
                          </td>
                        </s:if>
                        <s:else>
                          <td style="display: none;">
                            <s:if test='"text".equals(viewDirIn.guiColumnsType[#incr2.index])'>
                              <s:textarea name="viewDirIn.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewDirIn.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewDirIn.guiSelectedColumns[#incr2.index]}" name="viewDirIn.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewDirIn.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
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
                  <s:iterator value="viewDirIn.databaseColumnsLabel" var="input" status="incr">
                    <s:if test="viewDirIn.visibleHeaders[#incr.index]">
                      <td>
                        <s:if test='"text".equals(viewDirIn.guiColumnsType[#incr.index])'>
                          <s:textarea name="viewDirIn.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewDirIn.guiSelectedColumns[#incr.index]}" emptyOption="true" name="viewDirIn.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:if>
                    <s:else>
                      <td style="display: none;">
                        <s:if test='"text".equals(viewDirIn.guiColumnsType[#incr.index])'>
                          <s:textarea name="viewDirIn.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewDirIn.guiSelectedColumns[#incr.index]}" value="%{viewDirIn.guiSelectedColumns[#incr.index][0]}" name="viewDirIn.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:else>
                  </s:iterator>
                </tr>
              </tbody>
            </table>
            <s:if test='1!=viewDirIn.nbPages'>
              <div style="float: left; margin-left: 20px;">
                <table style="width: 200px;">
                  <tr>
                    <td style="width: 40px;">Page :</td>
                    <td style="width: 50px; background-color: #ffffff;">
                      <s:textarea name="viewDirIn.idPage" value="%{viewDirIn.idPage}" theme="simple" />
                    </td>
                    <td style="width: 10px;">/</td>
                    <td style="width: 25px;">
                      <s:property value="%{viewDirIn.nbPages}" />
                    </td>
                    <td class="smallButton" onclick="gotoPage('viewDirIn',$(this),-999999999);">&lt;&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewDirIn',$(this),-1);">&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewDirIn',$(this),1);">&gt;</td>
                    <td class="smallButton" onclick="gotoPage('viewDirIn',$(this),999999999);">&gt;&gt;</td>
                  </tr>
                </table>
              </div>
            </s:if>
        </s:if>
        </div>
    </div>

</div>


<div class="container" id="viewDirOut" style="position: absolute; left:0px;">

 <div style="position: absolute; left:600px; top:0px; width:400px; height:850px;">
		<div style="position: absolute; top:0px; left:0px; z-index:2001; width:100%;">
			<s:textarea m="" action='select' name="dirOut" value="%{dirOut}" cssStyle="width:500px;height:20px;"></s:textarea>
	    </div>

		<div style="position: absolute; top:30px; left:0px; z-index:2001; width:100%;">
			<input id="viewDirOut.select" type="submit" doAction="selectDirOut" scope="viewDirOut;" value="Visualiser le répertoire"></input>
			<input id="viewDirOut.see" type="submit" doAction="seeDirOut" scope="viewDirOut;" value="Visualiser le répertoire" style="display:none;"></input>
			<input id="viewDirOut.transfer" type="submit" doAction="transferDirOut" scope="viewDirIn;viewDirOut;" value="Transférer"></input>
			<input id="viewDirOut.add" type="submit" doAction="addDirOut" scope="viewDirIn;viewDirOut;" value="Créer" style="display:none;"></input>
			<input id="viewDirOut.del" type="submit" doAction="delDirOut" scope="viewDirIn;viewDirOut;" value="Effacer"></input>
			<input id="viewDirOut.update" type="submit" doAction="renameOut" scope="viewDirIn;viewDirOut;" value="Mettre à jour" style="display:none;"></input>
			<input id="viewDirOut.copy" type="submit" doAction="copyDirOut" scope="viewDirOut;viewDirOut;" value="Copier"></input>
		</div> 
      
		<div style="position: absolute; top:60px; left:0px; z-index:2001; width:100%; height:800px; overflow-y:auto;">
          <s:if test="viewDirOut.isInitialized==true&&viewDirOut.isScoped==true">
          
            <div class="bandeau">
              <s:property value="%{viewDirOut.title}" />
            </div>
            <s:hidden name="viewDirOut.databaseColumnsSort" value="" />
            <table class="fixedHeader" style="table-layout:auto; width:100%;">
              <thead>
                <tr>
                  <th></th>
                  <s:iterator value="viewDirOut.guiColumnsLabel" var="head" status="incr">
                    <s:if test="viewDirOut.visibleHeaders[#incr.index]">
                      <th class="sort" style="width:<s:property value='viewDirOut.headersVSize[#incr.index]'/>;">
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
                  <s:iterator value="viewDirOut.databaseColumnsLabel" var="head" status="incr">
                    <th>
                      <s:property />
                    </th>
                  </s:iterator>
                </tr>
                <tr style="display: none;">
                  <th></th>
                  <s:iterator value="viewDirOut.databaseColumnsLabel" var="head" status="incr">
                    <th>
                      <s:checkbox name="viewDirOut.selectedColumns[%{#incr.index}]" theme="simple"></s:checkbox>
                    </th>
                  </s:iterator>
                </tr>
                <tr>
                  <th></th>
                  <s:iterator value="viewDirOut.guiColumnsLabel" var="head" status="incr">
                    <s:if test="viewDirOut.visibleHeaders[#incr.index]">
                      <th>
                        <s:textarea name="viewDirOut.filterFields[%{#incr.index}]" value="%{viewDirOut.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:if>
                    <s:else>
                      <th style="display: none;">
                        <s:textarea name="viewDirOut.filterFields[%{#incr.index}]" value="%{viewDirOut.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:else>
                  </s:iterator>
                </tr>
              </thead>
              <tbody>
                <s:if test="viewDirOut.content!=null && viewDirOut.content.size()>0">
                  <s:iterator value="viewDirOut.content" var="line" status="incr1">
                    <tr>
                      <td>
                        <s:checkbox name="viewDirOut.selectedLines[%{#incr1.index}]" onclick="if ($(this).parent().siblings().eq(1).text().trim()=='true') {updateCheckBox('viewDirOut',$(this));} else {return true;}" theme="simple"></s:checkbox>
                      </td>
                      <s:iterator value="#line" status="incr2">
                        <s:if test="viewDirOut.visibleHeaders[#incr2.index]">
                          <td>
                            <s:if test='"text".equals(viewDirOut.guiColumnsType[#incr2.index])'>
                              <s:textarea name="viewDirOut.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewDirOut.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewDirOut.guiSelectedColumns[#incr2.index]}" name="viewDirOut.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewDirOut.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
                            </s:else>
                          </td>
                        </s:if>
                        <s:else>
                          <td style="display: none;">
                            <s:if test='"text".equals(viewDirOut.guiColumnsType[#incr2.index])'>
                              <s:textarea name="viewDirOut.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewDirOut.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewDirOut.guiSelectedColumns[#incr2.index]}" name="viewDirOut.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewDirOut.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
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
                  <s:iterator value="viewDirOut.databaseColumnsLabel" var="input" status="incr">
                    <s:if test="viewDirOut.visibleHeaders[#incr.index]">
                      <td>
                        <s:if test='"text".equals(viewDirOut.guiColumnsType[#incr.index])'>
                          <s:textarea name="viewDirOut.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewDirOut.guiSelectedColumns[#incr.index]}" emptyOption="true" name="viewDirOut.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:if>
                    <s:else>
                      <td style="display: none;">
                        <s:if test='"text".equals(viewDirOut.guiColumnsType[#incr.index])'>
                          <s:textarea name="viewDirOut.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewDirOut.guiSelectedColumns[#incr.index]}" value="%{viewDirOut.guiSelectedColumns[#incr.index][0]}" name="viewDirOut.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:else>
                  </s:iterator>
                </tr>
              </tbody>
            </table>
            <s:if test='1!=viewDirOut.nbPages'>
              <div style="float: left; margin-left: 20px;">
                <table style="width: 200px;">
                  <tr>
                    <td style="width: 40px;">Page :</td>
                    <td style="width: 50px; background-color: #ffffff;">
                      <s:textarea name="viewDirOut.idPage" value="%{viewDirOut.idPage}" theme="simple" />
                    </td>
                    <td style="width: 10px;">/</td>
                    <td style="width: 25px;">
                      <s:property value="%{viewDirOut.nbPages}" />
                    </td>
                    <td class="smallButton" onclick="gotoPage('viewDirOut',$(this),-999999999);">&lt;&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewDirOut',$(this),-1);">&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewDirOut',$(this),1);">&gt;</td>
                    <td class="smallButton" onclick="gotoPage('viewDirOut',$(this),999999999);">&gt;&gt;</td>
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