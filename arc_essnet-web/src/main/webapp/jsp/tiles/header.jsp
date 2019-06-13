<%@ taglib prefix="s" uri="/struts-tags" %>

<nav class="navbar navbar-expand-lg navbar-light mb-2" style="background-color: #FFFFFF">
  <a class="navbar-brand" href="index"><s:text name="header.applicationName" /></a>
  <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
  </button>

  <div class="collapse navbar-collapse" id="navbarSupportedContent">
    <ul class="navbar-nav mr-auto">
      <li class="nav-item">
        <a class="nav-link" href="index"><s:text name="header.home" /></a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="selectFamilleNorme"><s:text name="header.familyManagement" /></a>
      </li>
            <li class="nav-item">
        <a class="nav-link" href="selectNorme"><s:text name="header.normManagement" /></a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="selectListNomenclatures"><s:text name="header.externalFile" /></a>
      </li>
    </ul>
    
    <div class="nav-text navbar-left mr-3">
   <s:if test="isDataBaseOK">
   <button id="connectionCheck" class="btn btn-success" type="button">Database ok</button>
   </s:if>
	<s:else>
	    <button id="connectionCheck"  class="btn btn-danger" type="button">Connection error</button>
	
	</s:else>   
    
    </div>
    
    <s:form
    spellcheck="false"
    action="selectPilotageBAS8"
    namespace="/"
    method="POST"
    theme="simple"
    class="navbar-form navbar-left"
  >
    <div class="form-inline">
    <label class="mr-sm-2" for="environnementTravail"><s:text name="header.sandboxChoice" /></label>
      <s:select
        id="environnementTravail"
        class="form-control mr-sm-2"
        list="envMap"
        name="bacASable"
        value="%{session.ENV}"
        theme="simple"
        emptyOption="false"
        required="true"
      ></s:select>

      <s:submit
        id="env.select"
        class="btn btn-outline-primary"
        type="submit"
        doAction='%{actionName}'
        scope=""
        ajax="false"
        key="header.validate"
      ></s:submit>
    </div>
  </s:form>
  </div>
</nav>