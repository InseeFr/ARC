<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>

  <head>
	<link rel="stylesheet" type="text/css" href="<s:url value='/css/style.css' />" />
	<link rel="stylesheet" type="text/css" href="<s:url value='/css/jquery-ui.min.css' />" />
  </head>

<body>


<!-- TABLEAU POUR SELECTIONNER LES BLOCS -->
<div style="position: absolute; top: 318px; left: 16px; margin-bottom: 25px;">

	   <div>
		<h2>Choix des rubriques à importer</h2>
	  </div>
	  
      <div class="container" id="selection_rubriques">
          <s:form id="formBlocs" spellcheck="false" namespace="/" method="POST" theme="simple">
            <div class="bandeau">
              Liste des blocs
            </div>
            <table id="table_blocs" style="width:500px;" class="fixedHeader">
            	<thead>
            		<tr>
                	<th></th>	
                  	<th>Intitulé du bloc</th>
                 </tr>
            	</thead>
            	 <tbody>
					<s:iterator value="rubriquesParBloc">
  						<tr>
                      		<td style="width:10%;">
                        		<s:checkbox class="bloc_checkbox" name="block1" theme="simple" ></s:checkbox>
                      		</td>
                      
                      		<td style="width:90%;"><s:property value="key" /></td>
                      
                      	</tr>
  					</s:iterator> 
            	 </tbody>
            </table>
          </s:form>
      </div>
</div>
	
<!-- MENU  -->
<div>
	<div style="float: left;" class="titre1">Importer des données</div>
	<div style="position: relative; top: 3px; left: 100px;">
		<s:a class="onglet" href="index.action">Accueil</s:a>
		<s:a class="onglet" href="selectNorme.action">Gérer les Normes</s:a>
		<s:a class="onglet" href="selectFamilleNorme.action">Gérer les familles</s:a>
	</div>
</div>
	
<!-- BLOC CONTENANT LA PÉRIODE DE VALIDITÉ À SÉLÉCTIONNER-->
  <div style="position: absolute; top: 62px; left: 16px; margin-bottom: 25px;">
  
  	<div>
		<h2>Choix de la période de validité</h2>
	</div>

      <div class="container" id="viewFormulaireValidite">
          <s:form id="formValidite" name="formValidite" class="formulaire" spellcheck="false" namespace="/" method="POST" theme="simple">
            <div class="bandeau">Validités disponibles</div>
	            <table id='table_validite' style="width:223px;" class="fixedHeader">
	              <thead>
	                <tr>
	                	<th></th>	
	                  	<th>Début de validité</th>
	                  	<th>Fin de validité</th>
	                 </tr>
	              </thead> 
	              <tbody>
	              	<tr>
	              		<td></td>
	              		<td id="selecteur1" style="height:20px;">
	             			<s:select headerKey="-1" headerValue="---" id="debValiditeSelecteur" name="debutValidite" value="---" list='listValiditeDisponibles'/>
	              		</td>	
	              		<td id="selecteur2" style="height:20px;">
	              			<s:select headerKey="-1" headerValue="---" id="finValiditeSelecteur" name="finValidite" value="---" list='listValiditeDisponibles' />
	              		</td>
	              	</tr>
	              </tbody>
	          </table>
	      </s:form>
      </div>
    </div>

<!-- TABLEAU CONTENANT LA PÉRIODE DE CHARGEMENT À SÉLÉCTIONNER-->
  <div style="position: absolute; top: 189px; left: 16px; margin-bottom: 25px;">
      
      <div>
		<h2>Choix de la période de chargement</h2>
	  </div>
      
      <div class="container" id="periode_chargement">
          
          <s:form id="formChargement" class="formulaire" spellcheck="false" namespace="/" method="POST" theme="simple">
            
            <div class="bandeau"> Dates de chargement</div>
            <table id='table_chargement' style="width:245px;" class="fixedHeader">
              <thead>
                <tr>
                	<th></th>	
                  	<th>Date de début</th>
                  	<th>Date de fin</th>
                 </tr>
              </thead> 
              <tbody>
              	<tr>
              		<td></td>
              		<td>
 						<input id="datepicker1" style="height:20px;" type="text" name="debutChargement"></input>
              		</td>	
              		<td>
              			<input id="datepicker2" style="height:20px;" type="text" name="finChargement"></input>
              		</td>
              	</tr>
              </tbody>
              </table>
              
          </s:form>
      </div>
    </div>

<!-- TABLEAU POUR SÉLECTIONNER LE BAC À SABLE OÙ SERA DÉPOSÉ LE FICHIER RESULTAT DE LA REQUÊTE  -->
<div style="position: absolute; top: 525px; left: 16px; margin-bottom: 25px;">
      
       <div>
		<h2>Choix de l'espace de travail</h2>
	  </div>
      
      <div class="container" id="viewFormulaireBac">
          
          <s:form id="formBacASable" spellcheck="false" namespace="/" method="POST" theme="simple">
            
            <div class="bandeau">Bacs à sable disponibles</div>
            <table id='table_bac_a_sable' class="fixedHeader" style="width: 179px;">
              <thead>
                <tr>
                	<th></th>	
                  	<th>Bac à sable</th>
                 </tr>
              </thead> 
              <tbody>
              	<tr>
              		<td></td>
              		<td id="selecteur3">
             			<s:select
             				id="bacASableSelecteur"
             				headerKey="-1" 
             				headerValue="---"
             				name="bacASable" 
             				list="#{'1':'Bac à sable 1', '2':'Bac à sable 2', '3':'Bac à sable 3', '4':'Bac à sable 4', '5':'Bac à sable 5','6':'Bac à sable 6','7':'Bac à sable 7','8':'Bac à sable 8'}" 
             			/>
              		</td>	
              	</tr>
              </tbody>
              </table>
          </s:form>
      </div>
    </div>
    
<!-- BLOC RECAPITULATIF DES INFORMATIONS SAISIES -->
<div style="position: absolute; top: 657px; left: 16px; margin-bottom: 25px;">
	<div class="container" id="viewRecapitulatif">
	 
	   <div class="bandeau">Récapitulatif de la demande</div>
		 <table id='table_recapitulatif' class="fixedHeader" style="width: 732px;">
			 <thead>
	           	<tr>
	                <th style="width: 22px;"></th>	
	                <th style="width: 473px;">Champ sélectionné</th>
	                <th style="width: 175px;">Filtre sur valeurs saisies</th>
	                <th style="width: 62px;">Non vide</th>
	            </tr>
	        </thead> 
	        <tbody id="bodyRecapitulatifId">
<!-- 	cette partie est remplie dynamiquement via des méthodes jquery -->
	     	</tbody>
	             
		</table>
		<div style="float: left; width: 250px;">
			<br></br>
				<input id="valider_demande" class="button" style="width: 75px; height:21px;" type="submit" value="Valider" />
				<input id="annuler" 		class="button" style="width: 75px; height:21px; left:84px;" type="submit" value="Annuler" />
		</div> 
		<s:form id="formulaireDemandeExtraction" action="traitementDemandeExtraction" spellcheck="false" namespace="/" method="POST" theme="simple">		
			
			<!-- Ici on stocke les sélections des autres blocs qui vont être envoyé au serveur -->
			<!-- Bloc validité -->
            <s:hidden id="debutValiditeId" name="debutValidite"/>
            <s:hidden id="finValiditeId" name="finValidite" />
            
            <!-- Bloc chargement -->
            <s:hidden id="debutChargementId" name="debutChargement"/>
            <s:hidden id="finChargementId" name="finChargement"/>
            
			<!-- Bloc Bac à sable -->
            <s:hidden id="bacASableId" name="bacASable"/>
           
           	<!-- Bloc Rubrique -->
           	<s:hidden id="listeRubriquesId" name="listeRubriquesSelectionnees"/>
           
			<!-- Bouton qui envoie le formulaire au serveur -->
             <s:submit id="submitFormulaire" class="button" style="width: 75px; height:21px; left:-169px; top:24px" value="Envoyer" />
		</s:form>
		
	</div>
</div>

 <!-- TABLEAU POUR SELECTIONNER LES RUBRIQUES EN FONCTION DU BLOC CHOISI -->
<div id="tableauRubriques" style="position: absolute; top: 62px; right:81px; margin-bottom: 25px; visibility: hidden;">
      <div class="container" id="selection_rubriques">
          <s:form id="formRubriques" spellcheck="false" namespace="/" method="POST" theme="simple">
            <div class="bandeau">
              Liste des rubriques
            </div>
            <table id="table_rubriques" style="width:725px;" class="fixedHeader">
            	
            	<thead>
            		<tr>
                	<th style="width: 23px; text-align:center; "><s:checkbox class="checkbox_header" name="block1" theme="simple" ></s:checkbox></th>	
                  	<th style="width: 406px;">Intitulé de la rubrique</th>
                  	<th style="width: 236px;">Sélection sur une ou plusieurs valeurs *</th>
                  	<th style="width: 60px;">Non vide **</th>
                 </tr>
            	</thead>
            	
            	 <tbody id='liste_rubriques'>
					<s:iterator value="rubriquesParBloc">
						
						<s:iterator value="value" status="incr">	
										
							<tr>
               					<td style="width: 23px; text-align:center; ">
                       				<s:checkbox class="rubrique_checkbox" name="block1" theme="simple" ></s:checkbox>
               					</td>
           						<td style="width: 406px;"><s:property /></td>
                      
           						<td style="width: 145px;" hidden class="keyHidden"><s:property value="key" /></td>
                      						
       							<td style="width: 236px;"><input type="text" name="filtre"/> </td>
                   				
                   				<td style="width: 60px; text-align:center; ">
                       				<input type="checkbox" name="filtreCheckBox"/>
               					</td>
                   							
           					</tr>
                      			            						
						</s:iterator>
  					</s:iterator> 
            	 </tbody>
            </table>
			<p>* les valeurs doivent être separées par des points virgules, par exemple: valeur1;valeur2;valeur3 </p>
			<p>** les lignes ayant la rubrique non renseignée ne seront pas selectionnées</p>
          </s:form>
      </div>
</div>
 
	<script type="text/javascript" src="<s:url value='/js/jquery-2.1.3.min.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/lib/jquery-ui.min.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/lib/jquery.ui.datepicker-fr.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/arc.js'/>"></script>
	<script type="text/javascript" src="<s:url value='/js/component.js'/>"></script> 
	<script type="text/javascript" src="<s:url value='/js/extraction.js'/>"></script>
</body>
</html>