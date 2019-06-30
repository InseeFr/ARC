<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
<title>Nomenclatures</title>
<link rel="stylesheet" type="text/css" href="<s:url value='/css/style.css' />" />
<script type="text/javascript" src="<s:url value='/js/jquery-2.1.3.min.js'/>"></script>
<script type="text/javascript" src="<s:url value='/js/arc.js'/>"></script>
<script type="text/javascript" src="<s:url value='/js/gererNomenclature.js'/>"></script>
<script type="text/javascript" src="<s:url value='/js/component.js'/>"></script>
</head>
<body>
  <s:if test="scope==null">
    <div class="titre1">Gérer les nomenclatures</div>
    <s:a class="onglet" href="accueil.action">Accueil</s:a>
    <s:a class="onglet" href="selectFamilleNorme.action">Gérer les familles</s:a>
    <s:a class="onglet" href="selectNorme.action">Gérer les normes</s:a>
    <s:a class="onglet" href="selectPilotageBAS.action">Bac à Sable 1</s:a>
    <s:a class="onglet" href="selectPilotageBAS2.action">Bac à Sable 2</s:a>
    <s:a class="onglet" href="selectPilotageBAS3.action">Bac à Sable 3</s:a>
    <s:a class="onglet" href="selectPilotageBAS4.action">Bac à Sable 4</s:a>
    <s:a class="onglet" href="selectPilotageBAS5.action">Bac à Sable 5</s:a>
    <s:a class="onglet" href="selectPilotageBAS6.action">Bac à Sable 6</s:a>
    <s:a class="onglet" href="selectPilotageBAS7.action">Bac à Sable 7</s:a>
    <s:a class="onglet" href="selectPilotageBAS8.action">Bac à Sable 8</s:a>
    <s:a class="onglet" href="selectPilotagePROD.action">Production</s:a>
  </s:if>
  
<!--   VIEW LISTE NOMENCLATURE -->
  <div style="position: absolute; top: 75px; left: 10px; width: 490px;">
    <div class="container" id="viewListNomenclatures">
      <s:if test="viewListNomenclatures.isInitialized==true&&viewListNomenclatures.isScoped==true">
        <s:form spellcheck="false" namespace="/" method="POST" theme="simple">
          <div class="bandeau">
            <s:property value="%{viewListNomenclatures.title}" />
          </div>
          <s:hidden name="viewListNomenclatures.headerSortDLabel" value="" />
          <table class="fixedHeader">
            <thead>
              <tr>
                <th></th>
                <s:iterator value="viewListNomenclatures.headersVLabel" var="head" status="incr">
                  <s:if test="viewListNomenclatures.headersVisible[#incr.index]">
                    <th class="sort" style="width:<s:property value='viewListNomenclatures.headersVSize[#incr.index]'/>;">
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
                <s:iterator value="viewListNomenclatures.headersDLabel" var="head" status="incr">
                  <th>
                    <s:property />
                  </th>
                </s:iterator>
              </tr>
              <tr style="display: none;">
                <th></th>
                <s:iterator value="viewListNomenclatures.headersDLabel" var="head" status="incr">
                  <th>
                    <s:checkbox name="viewListNomenclatures.selectedColumns[%{#incr.index}]" theme="simple"></s:checkbox>
                  </th>
                </s:iterator>
              </tr>
              <tr>
                <th></th>
                <s:iterator value="viewListNomenclatures.headersVLabel" var="head" status="incr">
                  <s:if test="viewListNomenclatures.headersVisible[#incr.index]">
                    <th>
                      <s:textarea name="viewListNomenclatures.filterFields[%{#incr.index}]" value="%{viewListNomenclatures.filterFields[#incr.index]}" theme="simple"></s:textarea>
                    </th>
                  </s:if>
                  <s:else>
                    <th style="display: none;">
                      <s:textarea name="viewListNomenclatures.filterFields[%{#incr.index}]" value="%{viewListNomenclatures.filterFields[#incr.index]}" theme="simple"></s:textarea>
                    </th>
                  </s:else>
                </s:iterator>
              </tr>
            </thead>
            <tbody>
              <s:if test="viewListNomenclatures.content!=null && viewListNomenclatures.content.size()>0">
                <s:iterator value="viewListNomenclatures.content" var="line" status="incr1">
                  <tr>
                    <td>
                      <s:checkbox name="viewListNomenclatures.selectedLines[%{#incr1.index}]" onclick="updateCheckBox('viewListNomenclatures',$(this));" theme="simple"></s:checkbox>
                    </td>
                    <s:iterator value="#line" status="incr2">
                      <s:if test="viewListNomenclatures.headersVisible[#incr2.index]">
                        <td>
                          <s:if test='"text".equals(viewListNomenclatures.headersVType[#incr2.index])'>
                            <s:textarea name="viewListNomenclatures.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewListNomenclatures.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
                          </s:if>
                          <s:else>
                            <s:select list="%{viewListNomenclatures.headersVSelect[#incr2.index]}" name="viewListNomenclatures.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewListNomenclatures.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
                          </s:else>
                        </td>
                      </s:if>
                      <s:else>
                        <td style="display: none;">
                          <s:if test='"text".equals(viewListNomenclatures.headersVType[#incr2.index])'>
                            <s:textarea name="viewListNomenclatures.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewListNomenclatures.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
                          </s:if>
                          <s:else>
                            <s:select list="%{viewListNomenclatures.headersVSelect[#incr2.index]}" name="viewListNomenclatures.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewListNomenclatures.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
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
                <s:iterator value="viewListNomenclatures.headersDLabel" var="input" status="incr">
                  <s:if test="viewListNomenclatures.headersVisible[#incr.index]">
                    <td>
                      <s:if test='"text".equals(viewListNomenclatures.headersVType[#incr.index])'>
                        <s:textarea name="viewListNomenclatures.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                      </s:if>
                      <s:else>
                        <s:select list="%{viewListNomenclatures.headersVSelect[#incr.index]}" emptyOption="true" name="viewListNomenclatures.inputFields[%{#incr.index}]" theme="simple"></s:select>
                      </s:else>
                    </td>
                  </s:if>
                  <s:else>
                    <td style="display: none;">
                      <s:if test='"text".equals(viewListNomenclatures.headersVType[#incr.index])'>
                        <s:textarea name="viewListNomenclatures.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                      </s:if>
                      <s:else>
                        <s:select list="%{viewListNomenclatures.headersVSelect[#incr.index]}" value="%{viewListNomenclatures.headersVSelect[#incr.index][0]}" name="viewListNomenclatures.inputFields[%{#incr.index}]" theme="simple"></s:select>
                      </s:else>
                    </td>
                  </s:else>
                </s:iterator>
              </tr>
            </tbody>
          </table>
          <div class="alert">
            <s:property value="%{viewListNomenclatures.message}" />
          </div>
          <div style="float: left;">
            <input id="viewListNomenclatures.select" type="submit" doAction="selectListNomenclatures" scope="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" value="Rafraîchir"></input>
            <input id="viewListNomenclatures.see" type="submit" doAction="selectListNomenclatures" scope="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" value="Voir"></input>
            <input id="viewListNomenclatures.add" type="submit" doAction="addListNomenclatures" scope="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" value="Ajouter"></input>
            <input id="viewListNomenclatures.update" type="submit" doAction="updateListNomenclatures" scope="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" value="Mettre à jour"></input>
            <input id="viewListNomenclatures.delete" type="submit" doAction="deleteListNomenclatures" scope="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" value="Supprimer"></input>
            <input id="viewListNomenclatures.sort" type="submit" doAction="sortListNomenclatures" scope="viewListNomenclatures;viewSchemaNmcl;viewNomenclature;" value="Trier"></input>
          </div>
          <s:if test='1!=viewListNomenclatures.nbPages'>
            <div style="float: left; margin-left: 20px;">
              <table style="width: 200px;">
                <tr>
                  <td style="width: 40px;">Page :</td>
                  <td style="width: 50px; background-color: #ffffff;">
                    <s:textarea name="viewListNomenclatures.idPage" value="%{viewListNomenclatures.idPage}" theme="simple" />
                  </td>
                  <td style="width: 10px;">/</td>
                  <td style="width: 25px;">
                    <s:property value="%{viewListNomenclatures.nbPages}" />
                  </td>
                  <td class="smallButton" onclick="gotoPage('viewListNomenclatures',$(this),-999999999);">&lt;&lt;</td>
                  <td class="smallButton" onclick="gotoPage('viewListNomenclatures',$(this),-1);">&lt;</td>
                  <td class="smallButton" onclick="gotoPage('viewListNomenclatures',$(this),1);">&gt;</td>
                  <td class="smallButton" onclick="gotoPage('viewListNomenclatures',$(this),999999999);">&gt;&gt;</td>
                </tr>
              </table>
            </div>
          </s:if>
        </s:form>
      </s:if>
    <s:form spellcheck="false" namespace="/" method="POST" theme="simple" enctype="multipart/form-data">
	    <s:file name="fileUpload" label="Select a File to upload" size="40" />
	    <input type="submit" id="btnFileUpload" value="Importer" scope="" doAction="importListNomenclatures" onclick="submitForm()" style="margin-left: 25px;" ajax="false" />
    </s:form>      	
    
    </div>
  </div>


  
	<!--   VIEW SCHEMA NOMENCLATURE -->0
<!--     <div style="position: relative; top: 20px; left: 0px; margin-bottom: 20px;"> -->
    <div style="position: absolute; top: 75px; left: 500px; width: 890px; border-left: 1px solid #000000; height: 90%;">
      <div class="container" id="viewSchemaNmcl">
        <s:if test="viewSchemaNmcl.isInitialized==true && viewSchemaNmcl.isScoped==true">
          <%-- 	<s:actionmessage /> --%>
          <%-- 	<s:actionerror /> --%>
          <s:form spellcheck="false" namespace="/" method="POST" theme="simple">
            <div class="bandeau">
              <s:property value="%{viewSchemaNmcl.title}" />
            </div>
            <s:hidden name="viewSchemaNmcl.headerSortDLabel" value="" />
            <table class="fixedHeader">
              <thead>
                <tr>
                  <th></th>
                  <s:iterator value="viewSchemaNmcl.headersVLabel" var="head" status="incr">
                    <s:if test="viewSchemaNmcl.headersVisible[#incr.index]">
                      <th class="sort" style="width:<s:property value='viewSchemaNmcl.headersVSize[#incr.index]'/>;">
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
                  <s:iterator value="viewSchemaNmcl.headersDLabel" var="head" status="incr">
                    <th>
                      <s:property />
                    </th>
                  </s:iterator>
                </tr>
                <tr style="display: none;">
                  <th></th>
                  <s:iterator value="viewSchemaNmcl.headersDLabel" var="head" status="incr">
                    <th>
                      <s:checkbox name="viewSchemaNmcl.selectedColumns[%{#incr.index}]" theme="simple"></s:checkbox>
                    </th>
                  </s:iterator>
                </tr>
                <tr>
                  <th></th>
                  <s:iterator value="viewSchemaNmcl.headersVLabel" var="head" status="incr">
                    <s:if test="viewSchemaNmcl.headersVisible[#incr.index]">
                      <th>
                        <s:textarea name="viewSchemaNmcl.filterFields[%{#incr.index}]" value="%{viewSchemaNmcl.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:if>
                    <s:else>
                      <th style="display: none;">
                        <s:textarea name="viewSchemaNmcl.filterFields[%{#incr.index}]" value="%{viewSchemaNmcl.filterFields[#incr.index]}" theme="simple"></s:textarea>
                      </th>
                    </s:else>
                  </s:iterator>
                </tr>
              </thead>
              <tbody>
                <s:if test="viewSchemaNmcl.content!=null && viewSchemaNmcl.content.size()>0">
                  <s:iterator value="viewSchemaNmcl.content" var="line" status="incr1">
                    <tr>
                      <td>
                        <s:checkbox name="viewSchemaNmcl.selectedLines[%{#incr1.index}]" theme="simple"></s:checkbox>
                      </td>
                      <s:iterator value="#line" status="incr2">
                        <s:if test="viewSchemaNmcl.headersVisible[#incr2.index]">
                          <td>
                            <s:if test='"text".equals(viewSchemaNmcl.headersVType[#incr2.index])'>
                              <s:textarea name="viewSchemaNmcl.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewSchemaNmcl.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewSchemaNmcl.headersVSelect[#incr2.index]}" name="viewSchemaNmcl.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewSchemaNmcl.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
                            </s:else>
                          </td>
                        </s:if>
                        <s:else>
                          <td style="display: none;">
                            <s:if test='"text".equals(viewSchemaNmcl.headersVType[#incr2.index])'>
                              <s:textarea name="viewSchemaNmcl.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewSchemaNmcl.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
                            </s:if>
                            <s:else>
                              <s:select list="%{viewSchemaNmcl.headersVSelect[#incr2.index]}" name="viewSchemaNmcl.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewSchemaNmcl.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
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
                  <s:iterator value="viewSchemaNmcl.headersDLabel" var="input" status="incr">
                    <s:if test="viewSchemaNmcl.headersVisible[#incr.index]">
                      <td>
                        <s:if test='"text".equals(viewSchemaNmcl.headersVType[#incr.index])'>
                          <s:textarea name="viewSchemaNmcl.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewSchemaNmcl.headersVSelect[#incr.index]}" emptyOption="true" name="viewSchemaNmcl.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:if>
                    <s:else>
                      <td style="display: none;">
                        <s:if test='"text".equals(viewSchemaNmcl.headersVType[#incr.index])'>
                          <s:textarea name="viewSchemaNmcl.inputFields[%{#incr.index}]" theme="simple"></s:textarea>
                        </s:if>
                        <s:else>
                          <s:select list="%{viewSchemaNmcl.headersVSelect[#incr.index]}" value="%{viewSchemaNmcl.headersVSelect[#incr.index][0]}" name="viewSchemaNmcl.inputFields[%{#incr.index}]" theme="simple"></s:select>
                        </s:else>
                      </td>
                    </s:else>
                  </s:iterator>
                </tr>
              </tbody>
            </table>
            <div class="alert">
              <s:property value="%{viewSchemaNmcl.message}" />
            </div>
            <div style="float: left;">
              <input id="viewSchemaNmcl.select" type="submit" doAction="selectSchemaNmcl" scope="viewSchemaNmcl;" value="Rafraîchir"></input>
              <input id="viewSchemaNmcl.see" type="submit" doAction="selectSchemaNmcl" scope="viewSchemaNmcl;" value="Voir"></input>
              <input id="viewSchemaNmcl.sort" type="submit" doAction="sortSchemaNmcl" scope="viewSchemaNmcl;" value="Trier"></input>
              <input id="viewSchemaNmcl.add" type="submit" doAction="addSchemaNmcl" scope="viewSchemaNmcl;" value="Ajouter"></input>
              <input id="viewSchemaNmcl.delete" type="submit" doAction="deleteSchemaNmcl" scope="viewSchemaNmcl;" value="Supprimer"></input>
              <input id="viewSchemaNmcl.update" type="submit" doAction="updateSchemaNmcl" scope="viewSchemaNmcl;" value="Mettre à jour"></input>
            </div>
            <s:if test='1!=viewSchemaNmcl.nbPages'>
              <div style="float: left; margin-left: 20px;">
                <table style="width: 200px;">
                  <tr>
                    <td style="width: 40px;">Page :</td>
                    <td style="width: 50px; background-color: #ffffff;">
                      <s:textarea name="viewSchemaNmcl.idPage" value="%{viewSchemaNmcl.idPage}" theme="simple" />
                    </td>
                    <td style="width: 10px;">/</td>
                    <td style="width: 25px;">
                      <s:property value="%{viewSchemaNmcl.nbPages}" />
                    </td>
                    <td class="smallButton" onclick="gotoPage('viewSchemaNmcl',$(this),-999999999);">&lt;&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewSchemaNmcl',$(this),-1);">&lt;</td>
                    <td class="smallButton" onclick="gotoPage('viewSchemaNmcl',$(this),1);">&gt;</td>
                    <td class="smallButton" onclick="gotoPage('viewSchemaNmcl',$(this),999999999);">&gt;&gt;</td>
                  </tr>
                </table>
              </div>
            </s:if>
          </s:form>
        </s:if>
      </div>
    </div>
  
  
<!--    VIEW NOMENCLATURE  -->
  <div style="position: absolute; top: 75px; left: 900px; border-left: 1px solid #000000; height: 90%;">
    <div class="container" id="viewNomenclature">
      <s:if test="viewNomenclature.isInitialized==true&&viewNomenclature.isScoped==true">
        <s:form spellcheck="false" namespace="/" method="POST" theme="simple">
          <div class="bandeau">
            <s:property value="%{viewNomenclature.title}" />
          </div>
          <s:hidden name="viewNomenclature.headerSortDLabel" value="" />
          <table class="fixedHeader">
            <thead>
              <tr>
                <th></th>
                <s:iterator value="viewNomenclature.headersVLabel" var="head" status="incr">
                  <s:if test="viewNomenclature.headersVisible[#incr.index]">
                    <th class="sort" style="width:<s:property value='viewNomenclature.headersVSize[#incr.index]'/>;">
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
                <s:iterator value="viewNomenclature.headersDLabel" var="head" status="incr">
                  <th>
                    <s:property />
                  </th>
                </s:iterator>
              </tr>
              <tr style="display: none;">
                <th></th>
                <s:iterator value="viewNomenclature.headersDLabel" var="head" status="incr">
                  <th>
                    <s:checkbox name="viewNomenclature.selectedColumns[%{#incr.index}]" theme="simple"></s:checkbox>
                  </th>
                </s:iterator>
              </tr>
              <tr>
                <th></th>
                <s:iterator value="viewNomenclature.headersVLabel" var="head" status="incr">
                  <s:if test="viewNomenclature.headersVisible[#incr.index]">
                    <th>
                      <s:textarea name="viewNomenclature.filterFields[%{#incr.index}]" value="%{viewNomenclature.filterFields[#incr.index]}" theme="simple"></s:textarea>
                    </th>
                  </s:if>
                  <s:else>
                    <th style="display: none;">
                      <s:textarea name="viewNomenclature.filterFields[%{#incr.index}]" value="%{viewNomenclature.filterFields[#incr.index]}" theme="simple"></s:textarea>
                    </th>
                  </s:else>
                </s:iterator>
              </tr>
            </thead>
            <tbody>
              <s:if test="viewNomenclature.content!=null && viewNomenclature.content.size()>0">
                <s:iterator value="viewNomenclature.content" var="line" status="incr1">
                  <tr>
                    <td>
                      <s:checkbox name="viewNomenclature.selectedLines[%{#incr1.index}]" theme="simple"></s:checkbox>
                    </td>
                    <s:iterator value="#line" status="incr2">
                      <s:if test="viewNomenclature.headersVisible[#incr2.index]">
                        <td>
                          <s:if test='"text".equals(viewNomenclature.headersVType[#incr2.index])'>
                            <s:textarea name="viewNomenclature.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewNomenclature.content.t[#incr1.index].d[#incr2.index]}" theme="simple" readonly="%{viewNomenclature.headersUpdatable[#incr2.index]}"></s:textarea>
                          </s:if>
                          <s:else>
                            <s:select list="%{viewNomenclature.headersVSelect[#incr2.index]}" name="viewNomenclature.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewNomenclature.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
                          </s:else>
                        </td>
                      </s:if>
                      <s:else>
                        <td style="display: none;">
                          <s:if test='"text".equals(viewNomenclature.headersVType[#incr2.index])'>
                            <s:textarea name="viewNomenclature.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewNomenclature.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:textarea>
                          </s:if>
                          <s:else>
                            <s:select list="%{viewNomenclature.headersVSelect[#incr2.index]}" name="viewNomenclature.content.t[%{#incr1.index}].d[%{#incr2.index}]" value="%{viewNomenclature.content.t[#incr1.index].d[#incr2.index]}" theme="simple"></s:select>
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
<!--               <tr> -->
<!--                 <td></td> -->
<%--                 <s:iterator value="viewNomenclature.headersDLabel" var="input" status="incr"> --%>
<%--                   <s:if test="viewNomenclature.headersVisible[#incr.index]"> --%>
<!--                     <td> -->
<%--                       <s:if test='"text".equals(viewNomenclature.headersVType[#incr.index])'> --%>
<%--                         <s:textarea name="viewNomenclature.inputFields[%{#incr.index}]" theme="simple"></s:textarea> --%>
<%--                       </s:if> --%>
<%--                       <s:else> --%>
<%--                         <s:select list="%{viewNomenclature.headersVSelect[#incr.index]}" emptyOption="true" name="viewNomenclature.inputFields[%{#incr.index}]" theme="simple"></s:select> --%>
<%--                       </s:else> --%>
<!--                     </td> -->
<%--                   </s:if> --%>
<%--                   <s:else> --%>
<!--                     <td style="display: none;"> -->
<%--                       <s:if test='"text".equals(viewNomenclature.headersVType[#incr.index])'> --%>
<%--                         <s:textarea name="viewNomenclature.inputFields[%{#incr.index}]" theme="simple"></s:textarea> --%>
<%--                       </s:if> --%>
<%--                       <s:else> --%>
<%--                         <s:select list="%{viewNomenclature.headersVSelect[#incr.index]}" value="%{viewNomenclature.headersVSelect[#incr.index][0]}" name="viewNomenclature.inputFields[%{#incr.index}]" theme="simple"></s:select> --%>
<%--                       </s:else> --%>
<!--                     </td> -->
<%--                   </s:else> --%>
<%--                 </s:iterator> --%>
<!--               </tr> -->
            </tbody>
          </table>
          <div class="alert">
            <s:property value="%{viewNomenclature.message}" />
          </div>
          <div style="float: left;">
            <input id="viewNomenclature.select" type="submit" doAction="selectNomenclature" scope="viewNomenclature;" value="Rafraîchir"></input>
            <input id="viewNomenclature.sort" type="submit" doAction="sortNomenclature" scope="viewNomenclature;" value="Trier"></input>
                        
<!--             <input id="viewNomenclature.add" type="submit" doAction="addNomenclature" scope="viewNomenclature;" value="Ajouter"></input> -->
<!--             <input id="viewNomenclature.update" type="submit" doAction="updateNomenclature" scope="viewNomenclature;" value="Mettre à jour"></input> -->
<!--             <input id="viewNomenclature.delete" type="submit" doAction="deleteNomenclature" scope="viewNomenclature;" value="Supprimer"></input> -->
          </div>
          <s:if test='1!=viewNomenclature.nbPages'>
            <div style="float: left; margin-left: 20px;">
              <table style="width: 200px;">
                <tr>
                  <td style="width: 40px;">Page :</td>
                  <td style="width: 50px; background-color: #ffffff;">
                    <s:textarea name="viewNomenclature.idPage" value="%{viewNomenclature.idPage}" theme="simple" />
                  </td>
                  <td style="width: 10px;">/</td>
                  <td style="width: 25px;">
                    <s:property value="%{viewNomenclature.nbPages}" />
                  </td>
                  <td class="smallButton" onclick="gotoPage('viewNomenclature',$(this),-999999999);">&lt;&lt;</td>
                  <td class="smallButton" onclick="gotoPage('viewNomenclature',$(this),-1);">&lt;</td>
                  <td class="smallButton" onclick="gotoPage('viewNomenclature',$(this),1);">&gt;</td>
                  <td class="smallButton" onclick="gotoPage('viewNomenclature',$(this),999999999);">&gt;&gt;</td>
                </tr>
              </table>
            </div>
          </s:if>
        </s:form>
      </s:if>
    </div>
  </div>
  
</body>
</html>