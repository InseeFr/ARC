/**
 * 
 */
$(document).ready(function () {

	
	
	/***********************************************************************************************************************************/
	/***********************************************MISE EN PAGE DU DOCUMENT************************************************************/
	/***********************************************************************************************************************************/
	
	
	//On ajoute un datePicker pour la saisie des dates debut et fin de chargement
	$('#datepicker1').datepicker();
	$('#datepicker2').datepicker();
	
	//On rend inactif le bouton envoyer
	document.getElementById("submitFormulaire").disabled= true ;
	/***********************************************************************************************************************************/
	/***********************************************GESTION DES EVENEMENTS************************************************************/
	/***********************************************************************************************************************************/
	
	//recuperer le nom du bloc a laquelle appartient la rubrique sélectionnée 
	var obtenirNomDuBlocDeLaRubriqueTest = function(){
		$('.rubrique_checkbox').click( function () {
			var parenttag = $($(this).parent()).parent();
			alert($(':nth-child(3)', parenttag).text());
		});
	};
	
	//recuperer le nom du bloc a laquelle appartient la rubrique sélectionnée 
	var obtenirNomDuBlocDeLaRubrique = function(rubrique){
			var nomBloc ="";
			var parenttag = $($(rubrique).parent()).parent();
			nomBloc=$(':nth-child(3)', parenttag).text();
			return nomBloc;
	};
	
	//recuperer le nom du bloc lorsqu'il est selectionné 
	var obtenirNomDuBlocTest = function(){
		var nomDuBloc = "" ;
		$('.bloc_checkbox').click( function () {
			var parenttag = $($(this).parent()).parent();
			nomDuBloc = $(':nth-child(2)', parenttag).text();
		});

	};
	
	//recuperer le nom du bloc lorsqu'il est selectionné 
	var obtenirNomDuBloc = function(bloc){
		var nomDuBloc = "" ;
		var parenttag = $($(this).parent()).parent();
		nomDuBloc = $(':nth-child(2)', parenttag).text();
		return nomDuBloc;
	};
	
/****************************************************************************************************/
/********************************Click sur la checkbox LISTE DES BLOCS ******************************/
/****************************************************************************************************/
	//fonction qui efface le choix précédent lorqu'on clique sur un nouveau bouton
	//de la checkbox
	var refreshCheckBoxOnClick = function(){ 
		$('#table_blocs input').change(function() {
	        if($(this).is(":checked")) {
	        	var monInput = $(this);
	        	$('#table_blocs input').prop("checked", false);
	        	monInput.prop("checked", true);
	        }
	    });
	};
	
	
	
	
	var afficherRubriquesDuBlocSelectionne = function(){
		
		$('.bloc_checkbox').click( function () {
			
			//on efface le bloc selectionné précedemment
			refreshCheckBoxOnClick();
			
			//on récupère le nom du bloc
			var parenttag = $($(this).parent()).parent();
			var nomDuBloc = $(':nth-child(2)', parenttag).text();
			var rubriquesChecked = true;
			
			//on affiche les rubriques du bloc sélectionné et on cache les autres
			$('#liste_rubriques tr td input[class="rubrique_checkbox"]').each(function(index){
				
				var nomDuBlocRubrique ="";
				
				nomDuBlocRubrique = obtenirNomDuBlocDeLaRubrique(this);
				
				if(nomDuBlocRubrique==nomDuBloc){
					$($(this).parent()).parent().show();
					if($(this).is(":checked")) {
						
					}else{
						rubriquesChecked = false;
					}
				}else{
					$($(this).parent()).parent().hide();
				}
			});
			
			
			//on met à jour le bouton du dessus de la checkbox : liste des rubriques
			//si toutes les rubriques sont à true alors il prend la valeur true, sinon false
			$('.checkbox_header').prop("checked", rubriquesChecked);
			
			//on passe le tableau des rubriques à visible si ce n'est pas le cas			
			$('#tableauRubriques').css('visibility','visible');
			
		});
	};
	
	
	afficherRubriquesDuBlocSelectionne();
	
	
/****************************************************************************************************/
/********************************Click sur la checkbox LISTE DES RUBRIQUES **************************/
/****************************************************************************************************/
	
	
	//Gestion du click sur le bouton du haut
	//lorsqu'on click sur celui-ci, toutes rubriques affichés prennent la même valeur (true ou false)
	$('.checkbox_header').click( function () {
		
		var checkboxHeader = $(this);
		
		$('.rubrique_checkbox:visible').each(function(index){
			
			if($(checkboxHeader).is(":checked")) {
				$(this).prop("checked", true);
			}else{
				$(this).prop("checked", false);
			}
		});
	
	});
	
	
	//gestion du click sur les boutons rubriques
	$('.rubrique_checkbox').click( function () {
		
		//on met à jour le bouton du haut lorsqu'on clique sur l'un des boutons de la séléection
		if ($(".rubrique_checkbox:not(:checked):visible").is(':empty')){
			$('.checkbox_header').prop("checked", false);
		}else{
			$('.checkbox_header').prop("checked", true);
		}
		
		
		//Cette partie gère la synchronisation avec le tableau récapitulatif
		var parent = $(this).parent().parent();
		
		if ($(this).is(":checked")){
			//ici on ajoute la rubrique dans le tableau récapitulatif si elle est selectionnée dans le tableau ListeRubriques
			$(parent).clone().appendTo("#bodyRecapitulatifId");
			$('#bodyRecapitulatifId td').attr('disabled', true);
			$('#bodyRecapitulatifId td input').attr('disabled', true);
		}else{
			//ici on supprime dans le tableau récapitulatif si la rurique est decochée dans le tableau ListeRubriques
			var text=$(':nth-child(2)', parent).text();
			 $('#bodyRecapitulatifId tr').each(function() {
				
			        if ($(':nth-child(2)', this).text() === text) {
			            $(this).remove();
			        }
			    });
		}
		
	});

	/****************************************************************************************************/
	/********************************Mise à jour du tableau récapitulatif *******************************/
	/****************************************************************************************************/
	
	//Début de validité
	$("#selecteur1").change(function () {
	    var str = "Début de validité: ";
	    $( "#selecteur1 option:selected" ).each(function() {
	      str += $("#debValiditeSelecteur option:selected").text() + " ";
	    });
	    
	    var ajoutHtml ="<tr id=\"debValiditeRecapTrId\" style=\"height:20px;\"><td></td><td id=\"debValiditeRecapTdId\">"
	    				+str
	    				+"</td><td></td><td></td></tr>";
	   
	    //Deux cas possibles: soit c'est un ajout, soit c'est une mise à jour
	    if( $('#debValiditeRecapTrId').length ){
	    	$('#debValiditeRecapTdId').html(str);
	    }else{
	    	$(ajoutHtml).appendTo("#bodyRecapitulatifId");
	    }
	    	    
	  });
	
	//Fin de validité
	$("#selecteur2").change(function () {
	    var str = "Fin de validité: ";
	    $( "#selecteur2 option:selected" ).each(function() {
	      str += $("#finValiditeSelecteur option:selected").text() + " ";
	    });

	    var ajoutHtml ="<tr id=\"finValiditeRecapTrId\" style=\"height:20px;\"><td></td><td id=\"finValiditeRecapTdId\">"
	    				+str
	    				+"</td><td></td><td></td></tr>";
	   
	    //Deux cas possibles: soit c'est un ajout, soit c'est une mise à jour
	    if( $('#finValiditeRecapTrId').length ){
	    	
	    	$('#finValiditeRecapTdId').html(str);
	    }else{
	    	
	    	$(ajoutHtml).appendTo("#bodyRecapitulatifId");
	    }
	    

	    
	  });

	//Debut de chargement
	$("#datepicker1").change(function () {
		
	    var str = "Début de chargement: ";
	    
        str += $("#datepicker1").val() + " ";

	    var ajoutHtml ="<tr id=\"debChargementRecapTrId\" style=\"height:20px;\"><td></td><td id=\"debChargementRecapTdId\">"
	    				+str
	    				+"</td><td></td><td></td></tr>";
	   
	    //Deux cas possibles: soit c'est un ajout, soit c'est une mise à jour
	    if( $('#debChargementRecapTrId').length ){
	    	$('#debChargementRecapTdId').html(str);
	    }else{
	    	$(ajoutHtml).appendTo("#bodyRecapitulatifId");
	    } 
	    
	   
	    
	  });	
	
	//Fin de chargement
	$("#datepicker2").change(function () {
		
	    var str = "Fin de chargement: ";
	   	    
        str += $("#datepicker2").val() + " ";

	    var ajoutHtml ="<tr id=\"finChargementRecapTrId\" style=\"height:20px;\"><td></td><td id=\"finChargementRecapTdId\">"
	    				+str
	    				+"</td><td></td><td></td></tr>";
	   
	    //Deux cas possibles: soit c'est un ajout, soit c'est une mise à jour
	    if( $('#finChargementRecapTrId').length ){
	    	$('#finChargementRecapTdId').html(str);
	    }else{
	    	$(ajoutHtml).appendTo("#bodyRecapitulatifId");
	    } 
	    

	    
	  });	

	//Bac à sable
	$("#selecteur3").change(function () {
		
		var str = "";
	   	$( "#selecteur3 option:selected" ).each(function() {
	   		str +=  $("#bacASableSelecteur option:selected").text() + " ";
		    });
	    
	   	var ajoutHtml ="<tr id=\"bacASableRecapTrId\" style=\"height:20px;\"><td></td><td id=\"bacASableRecapTdId\">"
	   					+str
	   					+"</td><td></td><td></td></tr>";
	   
	    //Deux cas possibles: soit c'est un ajout, soit c'est une mise à jour
	    if( $('#bacASableRecapTrId').length ){
	    	$('#bacASableRecapTdId').html(str);
	    }else{
	    	$(ajoutHtml).appendTo("#bodyRecapitulatifId");
	    } 
	    

	    
	  });	

	
	//les valeurs filtres des rubriques
	$("#liste_rubriques tr td:nth-child(4)").change(function () {
		
		var valeurSaisie = $(this).children().val();
		var checkBox = $(this).parent().children().first().children();

		//Si la checkbox est checked dans le tableau Rubriques, alors on synchronise le tableau récapitulatif,
		//sinon on ne fait rien
		if($(checkBox).is(":checked")){

			var parent =  $(checkBox).parent().parent();
			var text=$(':nth-child(2)', parent).text();
			$('#bodyRecapitulatifId tr').each(function() {
			        if ($(':nth-child(2)', this).text() === text) {
			        	$(':nth-child(4) input', this).val(valeurSaisie);
			        }
			    });
		}			
	    
	  });	
	
	//les valeurs filtres des rubriques
	$("#liste_rubriques tr td:nth-child(5) ").change(function () {

		var filtreCheckBox = $(this).children().first();
		var isChecked = $(filtreCheckBox).prop("checked");
		var checkBox = $(this).parent().children().first().children();

		//Si la checkbox est checked dans le tableau Rubriques, alors on synchronise le tableau récapitulatif,
		//sinon on ne fait rien
		if($(checkBox).is(":checked")){

			var parent =  $(checkBox).parent().parent();
			var libelle=$(':nth-child(2)', parent).text();
			$('#bodyRecapitulatifId tr').each(function() {
			        if ($(':nth-child(2)', this).text() === libelle) {
			        	if (isChecked){
			        		$(':nth-child(5) input', this).prop('checked',true);
			        	}else{
			        		$(':nth-child(5) input', this).prop('checked',false);
			        	}
			        	
			        }
			    });
		}			
	    
	  });	
	
	
	
	/****************************************************************************************************/
	/********************************Click sur les boutons **********************************************/
	/****************************************************************************************************/
	
	//Gestion de la validation
	$('#valider_demande').click( function () {
		
		
		var debValidite = $("#debValiditeSelecteur").val();
		var finValidite = $("#finValiditeSelecteur").val();
		var bacASable = $("#bacASableSelecteur").val();
		var isRubriqueSelected = false;
		
		$('#bodyRecapitulatifId tr td input').each(function() {
			isRubriqueSelected = true;
			return false;
		});
		
		
		//On contrôle que ces champs ont été sélectionnés
		if (debValidite=="-1"){
			alert("Vous devez choisir un début de validité!!")
		}else if(finValidite=="-1"){
			alert("Vous devez choisir une fin de validité!!")
		}else if (bacASable=="-1"){
			alert("Vous devez choisir un bac sable!!")
		}else if (!isRubriqueSelected){
			alert("Vous devez sélectionner au minimum une rubrique!!")
		}else{
			//Contrôle OK
			//On rend actif le bouton envoyer
			document.getElementById("submitFormulaire").disabled= false;
			document.getElementById("valider_demande").disabled= true;
		}
	});

	//Gestion de l'annulation
	$('#annuler').click( function () {
		//On rend inactif le bouton envoyer
		document.getElementById("submitFormulaire").disabled= true;
		document.getElementById("valider_demande").disabled= false;
		
		//On reinitialise les formulaires
		$('#formValidite')[0].reset();
		$('#formChargement')[0].reset();
		$('#formBlocs')[0].reset();
		$('#formRubriques')[0].reset();
		$('#formBacASable')[0].reset();
		$('#formulaireDemandeExtraction')[0].reset();

		//On vide le tableau récapitulatif
		$('#bodyRecapitulatifId tr').remove();
		
	});
	
	
	
	var recupererLesChampsSaisis = function(){
		
		//periode de validité
		$("#debutValiditeId").val($("#debValiditeSelecteur").val());
	    $("#finValiditeId").val($("#finValiditeSelecteur").val());
	    //periode de chargement
	    $("#debutChargementId").val($("#datepicker1").val()); 
	    $("#finChargementId").val($("#datepicker2").val());
	    //bac à sable
	    $("#bacASableId").val($("#bacASableSelecteur option:selected").text());
		
	    //les rubriques sélectionnées sont récupérés depuis le tableau récapitulatif
		var rubriquesSelectionnes="";
		$('#bodyRecapitulatifId tr').each(function() {
			
			if(!$(this).attr('id')){
				
				//ajout si besoin d'un point virgule
				if(rubriquesSelectionnes!==""){
					rubriquesSelectionnes += ";";
				}
				
				//on récupère le nom de la rubrique
				var rubriqueName="\""+$(':nth-child(2)', this).text();
				
				//on récupère la valeur du filtre si elle existe
				var filtre = ":";
				
				if($(':nth-child(5) input', this).prop("checked")){
					
					filtre+="valeur obligatoire"
				}
				
				else if($(':nth-child(4) input', this).val()){
					filtre+= $(':nth-child(4) input', this).val() ;
				}
				filtre += "\"";
		
				if(rubriquesSelectionnes===""){
					rubriquesSelectionnes = rubriqueName + filtre;
				}else{
					rubriquesSelectionnes += rubriqueName + filtre;
				}
				
			}
			
	    });

		//mise à jour de l'élément listeRubriquesId
		$("#listeRubriquesId").val(rubriquesSelectionnes);
	};
	
	
	var afficherBoiteDialogue = function(){
		
		var numeroBacASableChoisi=$("#bacASableId").val().substring(12, 13);
		var repertoireBacADable="ARC_BAS";
		if (numeroBacASableChoisi!=="1"){
			repertoireBacADable+=numeroBacASableChoisi;
		}
		
		var cheminFichierResultat = "arc/pd/fichiers-batch/" +repertoireBacADable+"/EXPORT/export.csv"
		
		/**Creation de la div contenant la boite de dialogue*/
        var dynamicDialog = $('<div id="conformBox">'+'</span><b>Votre demande d\'extraction a été envoyée. Le fichier résultat est disponbile dans le répertoire suivant: '+cheminFichierResultat+' <b></div>');
        
        dynamicDialog.dialog({
                title : "Arc Application",
                closeOnEscape: true,
                modal : true,
        
               buttons : 
                        [{
                                text : "Fermer",
                                click : function() {
                                        $(this).dialog("close");
                                                                               
                                }
                        }]
        });
	};
	
	
	
	/**
	 * Soumission du formulaire en asynchrone
	 */
	$("#formulaireDemandeExtraction").submit(function(event) {

		// Stop form from submitting normally
		event.preventDefault();
		 
		//préparation du formulaire qui va être envoyé au serveur: on récupère l'ensemble des valeurs sélectionnées que l'on affecte
		//aux variables cachées du formulaire 
		recupererLesChampsSaisis();
		
		var $form = $(this),
		url = $form.attr("action");
		
        var posting = $.post(url, $("#formulaireDemandeExtraction").serialize());
        
        //On rend inactif le bouton envoyer
    	document.getElementById("submitFormulaire").disabled= true ;
    	document.getElementById("valider_demande").disabled= true;
    	document.getElementById("annuler").disabled= true;
    	
        afficherBoiteDialogue();

	});
	
	
	

	
});
