<%@ taglib prefix="s" uri="/struts-tags" %>

<s:form spellcheck="false" action="enterPilotageBAS8" namespace="/" method="POST" theme="simple" class="navbar-form navbar-left">

<nav class="navbar navbar-expand-lg navbar-light mb-2" style="background-color: #FFFFFF">
  <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
  </button>
  <div class="collapse navbar-collapse" id="navbarSupportedContent">
    <ul class="navbar-nav mr-0">
   		<li class="nobullet mt-auto">
      		<a class="navbar-brand" href="index"><s:text name="header.applicationHome" /></a>
      	</li>
      <li class="nav-item mt-auto">
        <a class="nav-link" href="index"><s:text name="header.home" /></a>
      </li>
      <li class="nav-item mt-auto">
        <a class="nav-link" href="selectFamilleNorme"><s:text name="header.familyManagement" /></a>
      </li>
      <li class="nav-item mt-auto">
        <a class="nav-link" href="selectNorme"><s:text name="header.normManagement" /></a>
      </li>
      <li class="nav-item mt-auto">
        <a class="nav-link" href="selectListNomenclatures"><s:text name="header.externalFile" /></a>
      </li>
      </ul>
     <ul class="navbar-nav navbar-right ml-auto">
     	<li class="nobullet mr-5">
	   		<s:if test="isDataBaseOK">
	   		<button id="connectionCheck" class="btn btn-success" type="button"><s:text name="header.database.ok" /></button>
	   		</s:if>
		<s:else>
		    <button id="connectionCheck"  class="btn btn-danger" type="button"><s:text name="header.database.ko" /></button>
		</s:else>   
      </li>
      <li class="nobullet mt-auto">
        		<label class="mr-sm-2" for="environnementTravail"><s:text name="header.sandboxChoice" /> :</label>
      </li>
  	  <li class="nobullet mt-auto" style="width: 7rem;">
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
		</li>  
		<li class="nobullet mt-auto">	      
		      <s:submit
		        id="env.select"
		        class="btn btn-secondary"
		        type="submit"
		        doAction='%{actionName}'
		        ajax="false"
		        key="header.validate"
		      ></s:submit>
  		</li>
       <li class="nobullet mt-auto">
	      		<ul style="margin: 0;">
				<li class="nobullet">	
				<s:url action="locale" var="localeFR"><s:param name="request_locale" >fr</s:param></s:url>
				<s:a href="%{localeFR}" >FR</s:a>
				</li>
				<li class="nobullet">
				<s:url action="locale" var="localeEN"><s:param name="request_locale" >en</s:param></s:url>
				<s:a href="%{localeEN}" >EN</s:a>
				</li>		
     		</ul> 
      </li>
    </ul>
    </div>
</nav>
</s:form>