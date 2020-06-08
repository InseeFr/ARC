//Modules disponibles
//Render:Tree;
//Render:Pilotage;
//Render:AlertBox;
//IHM:TableMultiCheckbox;
//IHM:Onglet;
//IHM:TextareaHotkeys;
//IHM:Accessibility;
//Render:TextareaEllipsis;
//VObject:Sort;
//ICS:AjaxDataSelector;
//Render:ConsoleIhm;
//Render:FixedHeader;
//Render:ChooseFileWithName;

var fadeDelay=0, setTimeoutConst;

var lastDivTreeClicked=null;

var zoomLvl=100;

var borderX=10;
var borderY=140;

var scrX=1420;
var scrY=scrX/screen.width*screen.height;

var lineHeight=20;

var zoomLvl=screen.width/scrX*100;

var configJS;

var updateConsoleState;

function dummy()
{
}



// s : chaine en entrée
// 2 : précision attendue des décimales
function formatDecimal(s, n) {
	if (s==null || s=="" || s=="NaN")
	{
		return null;
	}
	
	if (s.indexOf(".")==-1)
	{
		s=s+".";
		for (var z=0;z<n;z++)
		{
			s=s+"0";
		}
		return s;
	}

	
	var k=n-(s.length-1-s.indexOf("."));
	
	if (s.indexOf(".")+n+1>s.length)
	{
		for (var z=0;z<k;z++)
		{
			s=s+"0";
		}
		return s;
	}

	return s.substring(0,s.indexOf(".")+n+1);
	
}


$(document).on('ready readyAgain',function() {


	
	try {
		console.log("Chargement des modules js : "+configJS);
	}
	catch(e) {
		console.log("Aucun module js");
		configJS="";
	}

	// on repart sur une base propre pour les event
// jQuery(document).off("keydown keyup change")
//			
	
	if (configJS.indexOf("Render:TextareaFullSize;")>-1)
	{
		$('td textarea').each(function(){
			if (this.scrollHeight>(lineHeight*2))
				{
					$(this).css('max-height', this.scrollHeight-4);
				}
		});

	}
	

	if (configJS.indexOf("Render:FixedHeader;")>-1)
	{
		// met une dimension au th sans width des tableaux à largeur fixe
		$(".fixedHeader th,.light th").each(
				function() {
					if ($(this).attr('style')!=null &&  $(this).attr('style').indexOf("width:/**/;")>-1)
					{
						$(this).css("width",$(this).text().trim().split(" ")[0].length*9+20);
					}
				}
		);

		$('.bandeau').each(function(){
			console.log($(this).siblings().filter("table.fixedHeader").css("width"));
			$(this).css("width",$(this).siblings().filter("table.fixedHeader").css("width"));			

		}
		);
		
		
		// display block : nous permet d'avoir une barre de scroll vertical sur
		// le tbody
		// on le l'active que si y'a l'attribut max-height sur le tbody
		// met les bonnes dimensions sur la premiere ligne du tbody (on les
		// prend du thead)
		$(".fixedHeader,.light thead").find("tr:eq(0) th").each(
				function()
				{
					var $tb=$(this).parent().parent().parent().find("tbody");
					if ($tb.attr("style")!=undefined && $tb.attr("style").toLowerCase().indexOf("max-height")>-1)
						{
							$tb.find("tr:eq(0) td:eq("+$(this).index()+")").css("width",$(this).get(0).clientWidth);	
						}
				}
				);
				
				
	
		
		// 
		
	}

	if (configJS.indexOf("Render:Format;")>-1)
	{	
		applyFormat();
	}

	
	
	if (configJS.indexOf("Render:ConsoleIhm;")>-1)
	{		
		$('[name="consoleIhm"]').width(Math.min($('body').width()-$('div[id^="viewPilotage"]').width()-$('div[id^="viewRapport"]').width()-60,500));
		$('[name="consoleIhm"]').height(Math.max($('div[id^="viewPilotage"]').height(),$('div[id^="viewRapport"]').height())-50);
	}

/* Fix Bootstrap  not showing the file name with custom-file-input class.*/
	if (configJS.indexOf("Render:ChooseFileWithName;"))
	{
		$('.custom-file-input').on('change', function(e){
			var fileName = this.files[0].name;
			var nextSibling = e.target.nextElementSibling;
			nextSibling.innerText = fileName;
		});
	}


	if (configJS.indexOf("ICS:AjaxDataSelector;")>-1)
	{
		// pour rendre la chose plus propre par rapport à html
		// lorsqu'on clique un bouton d'action, ce n'est pas le bouton qui
		// envoie
		// l'action mais le formulaire
		// A noter qu'en html5, le bouton peut désormais etre auteur de l'action
		// mais on ne retrouve rien dans le dom... lol
		$("input[doAction]").off('click').on('click', function() {
			$(this).parents().filter("form").attr("action",$(this).attr('doAction'));
			$(this).parents().filter("form").attr("multipart",$(this).attr('multipart'));
			$(this).parents().filter("form").attr("ajax",$(this).attr('ajax'));
			$(this).parents().filter("form").attr("scope",$(this).attr('scope'));
		});

		$("button[doAction]").off('click').on('click', function() {
			$(this).parents().filter("form").attr("action",$(this).attr('doAction'));
			$(this).parents().filter("form").attr("multipart",$(this).attr('multipart'));
			$(this).parents().filter("form").attr("ajax",$(this).attr('ajax'));
			$(this).parents().filter("form").attr("scope",$(this).attr('scope'));
		});

		$('textarea').not('.noselect').not('.tree').on('textchange',function(){
			$(this).css("background-color","#ffcccc");
			$(this).attr('m','js');
		});
		
		$('input').not('.noselect').not('.tree').on('textchange',function(){
			$(this).css("background-color","#ffcccc");
			$(this).attr('m','js');
		});

		$('.datepicker').on('focus',function(){$(this).attr('m','js');});

		$('select').on('change',function(){
			$(this).css("background-color","#ffcccc");
			$(this).attr('m','js');
		});

		$(":checkbox").on('change',function(){
			$(this).attr('m','js');
			$(this).siblings().attr('m','js');
		});

		ajaxConfigurationCall();
	}


	// les element de classe "sort" lancent l'action sort
	if (configJS.indexOf("VObject:Sort;")>-1)
	{
		$(".sort").off('click').on('click',function(event){updateSort(event,$(this));});
	}

	// les textarea des vue dont le nom commence par "view" sont mis sur une
	// ligne
	// elle s'agrandissent si besoin pour voir tout le contenu quand on clique
	// desseus
	if (configJS.indexOf("Render:TextareaEllipsis;")>-1)
	{
		$('td textarea, th textarea').each(function(){
			$(this).css('height', '1px').css('min-height', 28);
			$(this).css('height', '1px').css('height', this.scrollHeight);
		});

		

		$('td textarea, th textarea').each(function()
				{
			drawEllipsis(this);
				}
		);

		$('td textarea, th textarea').off('focusin').on('focusin',function(){drawEllipsis(this);});
		$('td textarea, th textarea').off('focusout').on('focusout',function(){drawEllipsis(this);});

	}


	if (configJS.indexOf("IHM:TextareaHotkeys;")>-1)
	{
		// les raccourcis claviers
		$("textarea").filter("[name*='.content']").off('keydown').on('keydown',function(event){updateCells(event,$(this));});
		$("textarea").filter("[name*='.inputFields']").off('keydown').on('keydown',function(event){addCells(event,$(this));});
		$("textarea").filter("[name*='.filterFields']").off('keydown').on('keydown',function(event){updateSelect(event,$(this));});
		$("textarea").filter("[name*='.idPage']").off('keydown').on('keydown',function(event){updateSelect(event,$(this));});

		jQuery(document).off('keydown').on('keydown',function(evt) {
			if (evt.keyCode == 27) {
				evt.preventDefault();
				$("[id$='.select']").last().trigger('click');

			}
		});
	}
	
	if (configJS.indexOf("IHM:Accessibility;")>-1)
	{
		// les raccourcis claviers
		$("table").on('keydown', function(event) {accessibility(event,$(this));});
		$("table").on('keyup', function(event) {ctrlKeyUp(event,$(this));});	
	}



	// sessionPersist();


	if (configJS.indexOf("IHM:Onglet;")>-1) 
	{
		updateVisualTabs();
	}

	if (configJS.indexOf("IHM:TableMultiCheckbox;")>-1) 
	{
		$("[id$='.checkAll']").off('click').on('click',function(event) {
			if ($(this).prop("checked")) { // je viens de la cocher
				$(this).closest("table").find("[type=checkbox]").each(function() {
					$(this).prop("checked",true);
					$(this).attr('m','js');
				});
			} else { // sinon je viens de la décocher
				$(this).closest("table").find("[type=checkbox]").each(function() {
					$(this).prop("checked",false);
					$(this).attr('m','js');
				});
			};
		});
	}

	if (configJS.indexOf("Render:TableBlock;")>-1) 
	{
		renderTableBlock();
	}

	
	// adapte le niveau de zoom de l'écran et les objects taggé avec la classe
	// resolution
	if (configJS.indexOf("Render:Zoom;")>-1) 
		{
	var realX=scrX-borderX;
	var realY=scrY-borderY;
	
	if (($(window).height()-20)/zoomLvl*100>realY)
		{
			realY=($(window).height()-20)/zoomLvl*100;
		}
	
	
	// redimensionne les div pour la résolution de l'écran
	$("[name='screenResolution']").each(
	function(){
		var pos=30;
		var $t=$(this);
		
		while ($t.get(0).offsetTop!=undefined)
			{
				pos=pos+$t.get(0).offsetTop*zoomLvl/100;
				$t=$t.parent();
			}

		
		
// $(this).css("height",(((realY*zoomLvl/100)-pos)/zoomLvl*100)+"px");
	}		
	
	);

	$("html").css("height",realY+"px");
	$("html").css("width",realX+"px");
	$("body").css("height",realY+"px");
	$("body").css("width",realX+"px");
	$("body").css("border","1px solid #aaaaaa");
	// $("html").css("zoom",zoomLvl+"%");
	// $("html").css("-moz-transform","scale("+zoomLvl/100+")");
	$("html").css("transform","matrix("+zoomLvl/100+", 0, 0, "+zoomLvl/100+", 0, 0)");
	$("html").css("transform-origin","0 0");
	$("html").css("transform-style","flat");
	}
	
	
	// créer des tableaux fixed header avec un tbody déroulant
	$(".fixedHeader,.light thead").each(
			function() {

				// on donne la bonne dimension au table (plus propre)
// $(this).parent().css("width",width);
				
				var $tb=$(this).parent().find("tbody");
				if ($tb.attr("style")!=undefined && $tb.attr("style").toLowerCase().indexOf("max-height")>-1)
				{
					$tb.css("display","block");

					// on va donner la bonne dimension au tbody à partir de
					// celle trouvée dans le thead
					var width=$(this).get(0).clientWidth;
				
					// si y'a une scroll bar on ajoute 1em*zoom lvl (la taille
					// de la barre varie en fn du zoom)
						if ($tb.hasScroll("y"))
						{
							width=width+18;
						}
					
					$tb.css("width",width);
					$tb.css("visibility","");
				}
				
				
			}
	);
	
	$("table.light thead").css("display","none");

	
	// symbolise les valeurs manquantes
	$("text:not([h^='etat']):not([h^='code']):not([h^='bilan_']):not([h^='def_'])").filter(function(){return $(this).text()=="";}).css("background","repeating-linear-gradient(0deg, #555555, #ffffff 1%)");
	
// if (configJS.indexOf("Render:Comment;")>-1)
// {
// $("[name='comment']").each(function(){if
// ($(this).find(".comment").text()==""){$(this).css('display','none');} else
// {$(this).css('display','block');}});
// }

// $("textarea").off('mouseup').on('mouseup',function(e){if (e.which == 3)
// {e.preventDefault(); copyToClipboard(e.target)}});
// $("body").off('contextemenu').on('contextmenu',function(e){e.preventDefault();});
// $("textarea").off('contextmenu').on('contextmenu',function(e){copyToClipboard(e.target)});


	if (configJS.indexOf("Render:AlertBox;")>-1) 
	{
		$(".alert").each(function(){
			if ($(this).text().trim()!="")
			{
				alert($(this).text().trim());
			}
			$(this).text("");
		});
	}

	
});

function renderTableBlock()
{
	var m=0;

	var $t=$("[idblock='true']");
	for (var i=0;i<$t.length;i++)
	{

		// on alterne les couleurs de block en block
		if (m%2==1)
		{
			$t.eq(i).parent().parent().css("background-color","#cccccc");
		}
		else
		{
			$t.eq(i).parent().parent().css("background-color","#ffffff");
		}

		if ($t.eq(i).text()==$t.eq(i+1).text())
		{
			$t.eq(i).parent().css("border-bottom","1px solid transparent").siblings().css("border-bottom","1px solid transparent");
			$t.eq(i+1).css("visibility","hidden");
			$t.eq(i+1).parent().siblings().find("[block='true']").css("visibility","hidden");
		}
		else
		{
			m++;
		}
	}
}

function applyFormat()
{
	$("[f^='Decimal:']").each(function(){
		$(this).text(formatDecimal($(this).text(),parseFloat($(this).attr("f").split(":")[1])));
		$(this).css("text-align","right");
	});
	
	$("[f^='Heure:']").each(function(){
		var toDisplay=formatDecimal($(this).text(),parseFloat($(this).attr("f").split(":")[1]));
		console.log("toDisplay "+ toDisplay);
		if (toDisplay!=null && toDisplay!="NULL.00") { toDisplay=toDisplay+" h"; } else { toDisplay="" }
		$(this).text(toDisplay);
		$(this).css("text-align","right");
	});
	
	$("[f^='Euro:']").each(function(){
		var toDisplay=formatDecimal($(this).text(),parseFloat($(this).attr("f").split(":")[1]));
		if (toDisplay!=null) { toDisplay=toDisplay+" €"; }
		$(this).text(toDisplay);
		$(this).css("text-align","right");
	});
	
	$("[f^='Hide']").each(function(){
		$(this).css("display","none");
	});
}


function copyToClipboard(element) {
	$("#copy").val($(element).text()).focus().select();
// try {
// document.execCommand("copy");
// }
// catch (e)
// {
// }

	// $temp.remove();
}


function drawEllipsis(t)
{
	setTimeout(function(){
		if (t.scrollHeight-5 > t.clientHeight)
		{
			$(t).css("background-image", 'url("./img/ellipsis.png")');
		}
		else
		{
			$(t).css("background-image", 'none');
		}

	}
	,0);
}



// efface les checkbox de la vue
function updateCheckBox(view, t)
{


// on marque toutes les cases cochées : elle vont passer à false et on doit donc
// indiquer au formulaire le changement
// les checkbox struts2 on une soeur cachée donc marquer aussi la soeur cachée
// pour la sérialiser
	$('input[name^="'+view+'.selectedLines"]').filter(function(){
		return $(this).prop('checked') == true
	}).attr('m','js');
	$('input[name^="'+view+'.selectedLines"]').filter(function(){
		return $(this).prop('checked') == true
	}).siblings().attr('m','js');
	$('input[name^="'+view+'.selectedLines"]').prop('checked',false);

// on coche la case cliquée par l'utilisateur et on la marque pour être passée
// au formulaire
	$(t).prop('checked',true);
	$(t).siblings().attr('m','js');
	$(t).attr('m','js');
	$('[id="'+view+'.see"]').trigger('click');

}

// efface les checkbox de la vue
function updateCheckBoxNoAction(view, t)
{


// on marque toutes les cases cochées : elle vont passer à false et on doit donc
// indiquer au formulaire le changement
// les checkbox struts2 on une soeur cachée donc marquer aussi la soeur cachée
// pour la sérialiser
	$('input[name^="'+view+'.selectedLines"]').filter(function(){
		return $(this).prop('checked') == true
	}).attr('m','js');
	$('input[name^="'+view+'.selectedLines"]').filter(function(){
		return $(this).prop('checked') == true
	}).siblings().attr('m','js');
	$('input[name^="'+view+'.selectedLines"]').prop('checked',false);

// on coche la case cliquée par l'utilisateur et on la marque pour être passée
// au formulaire
	$(t).prop('checked',true);
	$(t).siblings().attr('m','js');
	$(t).attr('m','js');
}



// efface les checkbox de la vue
function multiCheckBoxEtRecopie(view, t)
{

// on coche la case cliquée par l'utilisateur et on la marque pour être passée
// au formulaire
	$(t).siblings().attr('m','js');
	$(t).attr('m','js');
	
	// recopier la denière ligne cochée
	
			$("[name^='"+view+".inputFields']").each(function(){
				
				var nomColonne=$(this).attr('h');		
				if (this.tagName=="TEXTAREA")
					{
					// if (!listeColonneAExclure.includes(nomColonne))
					if ($.inArray(nomColonne, listeColonneAExclure) < 0)

						{
						
							if ($(t).prop('checked') == true)
							{
								var contenu=$(t).parent().parent().find("[h='"+nomColonne+"']").text();
							}
						else
							{
							var contenu="";
							}
						
							$(this).text(contenu);
						}
					else
						{
							$(this).text("");
						}
					}
				
				if (this.tagName=="SELECT")
				{
					
					if ($(t).prop('checked') == true)
					{
						var contenu=$(t).parent().parent().find("[h='"+nomColonne+"']").text();
						var index=$(this).children().index($(this).children().filter("[value='"+contenu+"']"));
					}
				else
				{
						var index=0;
				}
				$(this).prop('selectedIndex',index);
				}
			});
}





function deleteByLine(e,t)
{
	$(t).parent().find("input[type='checkbox']:checked").prop('checked',false).attr('m','js');
	$(t).parent().find("tr").removeClass("boxSelect");
	if (configJS.indexOf("Render:TableBlock;")>-1) 
	{
		renderTableBlock();
	}
}


function selectByLines(e,t)
{
	
	// On cherche la ligne du filtre complémentaire
	var indexColonneFiltre = parseInt($("#indexColonneFiltre").attr("value"));
	var filtreNext = $(t).next().find("textarea").eq(indexColonneFiltre).text().replace("n","");
	var filtrePrev = $(t).prev().find("textarea").eq(indexColonneFiltre).text().replace("n","");
	var filtreOriginal = $(t).find("textarea").eq(indexColonneFiltre).text().replace("n","");
// alert(filtreOriginal);
// alert(filtreOriginal.replace("F","nF"));
// var $t2 = t;
// alert(indexColonneFiltre);
// alert(filtreNext);
// alert(filtrePrev);
// alert(filtreOriginal);
	var $nouvelleLigne = $(t);
	if(filtreNext===filtreOriginal){
		$nouvelleLigne=$(t).next();
	}
	else if(filtrePrev===filtreOriginal){
		$nouvelleLigne=$(t).prev();
	}
	// désélection
	if ($(t).find("input[type='checkbox']").prop('checked') || 
			$nouvelleLigne.find("input[type='checkbox']").prop('checked'))
	{
		deleteByLine(e,t);
// deleteByLine(e,$nouvelleLigne);
	}
	else
		// selection
	{
		deleteByLine(e,t);
		
		$(t).find("input[type='checkbox']").prop('checked',true);
		$(t).find("input[type='checkbox']").attr('m','');
		$(t).find("input[type='checkbox']").siblings().attr('m','');
		$(t).addClass("boxSelect");
		
		$nouvelleLigne.find("input[type='checkbox']").prop('checked',true);
		$nouvelleLigne.find("input[type='checkbox']").attr('m','');
		$nouvelleLigne.find("input[type='checkbox']").siblings().attr('m','');
		$nouvelleLigne.addClass("boxSelect");
	
		if (configJS.indexOf("Render:TableBlock;")>-1) 
		{
			var val=$(t).find("[idblock='true']").text();
	
			var $d=$(t);
			
			var val2=$nouvelleLigne.find("[idblock='true']").text();
			
			var $d2=$nouvelleLigne;
	
			while ($d.find("[idblock='true']").text()==val)
			{
				$d.addClass("boxSelect");
				$d=$d.prev();
			}
	
			$d=$nouvelleLigne;
	
			while ($d.find("[idblock='true']").text()==val)
			{
				$d.addClass("boxSelect");
				$d=$d.next();
			}
			
			while ($d2.find("[idblock='true']").text()==val2)
			{
				$d2.addClass("boxSelect");
				$d2=$d.prev();
			}
	
			$d2=$(t);
	
			while ($d2.find("[idblock='true']").text()==val2)
			{
				$d2.addClass("boxSelect");
				$d2=$d2.next();
			}
		}
	}
}

function selectByLine(e,t)
{
	// désélection
	if ($(t).find("input[type='checkbox']").prop('checked'))
	{
		deleteByLine(e,t);
	}
	else
		// selection
	{
		deleteByLine(e,t);
		
		$(t).find("input[type='checkbox']").prop('checked',true);
		$(t).find("input[type='checkbox']").attr('m','js');
		$(t).find("input[type='checkbox']").siblings().attr('m','js');
		$(t).addClass("boxSelect")

		if (configJS.indexOf("Render:TableBlock;")>-1) 
		{
			var val=$(t).find("[idblock='true']").text();

			var $d=$(t);

			while ($d.find("[idblock='true']").text()==val)
			{
				$d.addClass("boxSelect")
				$d=$d.prev();
			}

			$d=$(t);

			while ($d.find("[idblock='true']").text()==val)
			{
				$d.addClass("boxSelect")
				$d=$d.next();
			}
		}

	}

}


function updateCells(e,t)
{
	var view=$(t).parents().filter('div[id]').attr('id');
	
	// validation avec entrée
	if (e.ctrlKey && e.keyCode == 13) {
		e.preventDefault();
		$('[id="'+view+'.update"]').trigger('click');
	}

}

var clicks=0;

/**
 * Définit l'action a lancer en cas de double clique et l'action a lancer en cas
 * de simple
 * 
 * @param button =
 *            action au double clique
 * @param fnInit =
 *            init en cas de double clique
 * @param fnDo =
 *            fonction executée au simple clique (genre selection, ...).
 *            Eventuellement effectuée au double clique si doOnDoubleClick=true
 * @param e :
 *            event
 * @param t :
 *            this
 * @param initOnDoubleClick :
 * @param doOnDoubleClick :
 */
function doubleClickAction(button,fnInit,fnDo,e,t, initOnDoubleClick, doOnDoubleClick)
{
	clicks++;
	// si aucun clique enregistré
	if (clicks==1)
		{
		var args= [e,t] ;
		setTimeout(function(){
			if (clicks==1){
				window[fnDo].apply( window,args);
				}
			else
			{
				if (initOnDoubleClick)
					{
						window[fnInit].apply( window,args);
					}
				
				if (doOnDoubleClick)
					{
						window[fnDo].apply( window,args);
					}
				
				$("[id='"+button+"']").click();
			}
			clicks=0;
		
		},dblClickDelay);
		}
}	



function ajaxConfigurationCall()
{
	$('form').off('submit').on('submit', function(e) {
		// on met l'écran d'attente avec le sablier
		var $this = $(this);

		if ($this.attr('ajax')=="false")
		{
			console.log("pas d'ajax");
		}
		else
		{
			var attributesSaved=savElementAttributes($this.attr('scope'));
			
			$("body").append("<div id='hourglass'></div>");
			updateConsole();
			if ($this.attr('ajax')!="false" && $this.attr('multipart')=="true")
			{
				e.preventDefault();
				var formdata = new FormData(this);
				formdata.append("scope", splitAndEval($this.attr('scope')));

				$.ajax( {
					url: $this.attr('action'),
					type: 'POST',
					data: formdata,
					processData: false,
					contentType: false,
					success: function(xml) {
						xml="<root>"+xml+"</root>";
						var scope=splitAndEvalArray($this.attr('scope'));

						for (var i=0;i<scope.length;i++)
						{
							if (scope[i].substr(0,1)=="-")
							{
								document.getElementById(scope[i].substr(1)).innerHTML="";
							}
							else
							{
								document.getElementById(scope[i]).innerHTML=$(xml).find("#"+scope[i]).get(0).innerHTML;
							}

						}
						$(document).trigger('readyAgain');
						updateConsoleState=false;

						$("#hourglass").remove();
						$("[m='js']").remove("attr","m");
						
						applyElementAttributes($this.attr('scope'),attributesSaved);
						
					}
				} );
			}
			else
			{
				console.log("Appel ajax");
				e.preventDefault();
				var z0=new Date().getTime();


				console.log($this.attr('action'));
				console.log($this.attr('method'));
				console.log($this.serialize2()+"&scope="+splitAndEval($this.attr('scope')));
				
				$.ajax({
					url: $this.attr('action'),
					type: $this.attr('method'),
					data: $this.serialize2()+"&scope="+splitAndEval($this.attr('scope')),
					dataType: 'text',
					success: function(xml) {
						
						$("table.light thead").css("display","table-header-group");
						
						xml="<root>"+xml+"</root>";
						var z1=new Date().getTime();

						var scope=splitAndEvalArray(splitAndEval($this.attr('scope')));
						console.log(scope);

						for (var i=0;i<scope.length;i++)
						{
							console.log("actualisation de la div : "+scope[i]);

							try{
							if (scope[i].substr(0,1)=="-")
							{
								document.getElementById(scope[i].substr(1)).innerHTML="";
							}
							else
							{
									
									document.getElementById(scope[i]).innerHTML=$(xml).find("#"+scope[i]).get(0).innerHTML;
							}
							}catch(error) {
								console.error(error);
								console.log(scope[i]);
							}

						}


						var z2=new Date().getTime();

						$(document).trigger('readyAgain');
						updateConsoleState=false;

						$("#hourglass").remove();
						
						applyElementAttributes($this.attr('scope'),attributesSaved);

					}
				});
			}
		}

	});


}

function AfficherDimension(){
	var $z=$('textarea').filter('[name="viewmapping.content.t[6].d[7]"]');
	console.log("scrollHeight:"+$z.get(0).scrollHeight);
	console.log("clientHeight:"+$z.get(0).clientHeight);
	console.log("offsetHeight:"+$z.get(0).offsetHeight);
	setTimeout(function(){AfficherDimension();},3000);}


/**
 * Mise à jour du visuel des boutons d'onglet (quand un est cliqué, il apparait
 * cliqué et les autres remontent)
 */
function updateVisualTabs()
{
	$("[class^='button.ctrl0']").each(
			function()
			{
				if ($("#"+$(this).attr('target')).children().eq(0).length>0)
				{
					$(this).css('border','1px inset #777777');
					$(this).css('font-weight','bold');
				}
				else
				{
					$(this).css('border','1px solid #aaaaaa');
					$(this).css('font-weight','normal');
				}
			}
	);

}



function splitAndEval(tScope)
{
	var tScope=splitAndEvalArray(tScope);
	var tScopeCurrent="";
	for (var i=0;i<tScope.length;i++)
	{
		tScopeCurrent+=tScope[i]+";";
	}
	return tScopeCurrent;

}

function splitAndEvalArray(scope)
{
	var tScope=scope.split(";");
	var tScopeCurrent=[];

	for (var i=0;i<tScope.length-1;i++)
	{
		// on enleve la partie avant le [
		var divMain=tScope[i].split("[")[0];
		
		if (divMain.indexOf("(")>-1)
		{
			tScopeCurrent[i]=eval(divMain);
		}
		else
		{
			tScopeCurrent[i]=divMain;
		}
	}
	return tScopeCurrent;
}


function savElementAttributes(scope)
{
	var tScope=scope.split(";");
	var tScopeCurrent=[];

	for (var i=0;i<tScope.length-1;i++)
	{

		// on enleve la partie avant le [
		if (tScope[i].indexOf("[")>-1)
			{
			var divMain=tScope[i].split("[")[0];

			if (divMain.indexOf("(")>-1)
				{
					divMain=eval(divMain);
				}
			
			// on garde ce qui est entre les crochets
			var aTraiter=tScope[i].split("[")[1].split("]")[0];
			// on itere sur les virgules : [elem1, attr à conserver1, elem2,
			// attr à conserver 2,...]
			var elem=aTraiter.split(",");
			for (var j=0;j<elem.length;j=j+2)
			{
			
				var targetElementToSave=elem[j];
				var targetAttributeToSave=elem[j+1];
			
				tScopeCurrent[tScopeCurrent.length]=$("#"+divMain+"").find("["+targetElementToSave+"]")[targetAttributeToSave]();
				}
			}	
	}
	return tScopeCurrent;
}


function applyElementAttributes(scope, attributesSaved)
{
	var tScope=scope.split(";");
	var tScopeCurrent=[];
	var k=0;

	for (var i=0;i<tScope.length-1;i++)
	{

		// on enleve la partie avant le [
		if (tScope[i].indexOf("[")>-1)
			{
			var divMain=tScope[i].split("[")[0];

			if (divMain.indexOf("(")>-1)
				{
					divMain=eval(divMain);
				}
			
			// on garde ce qui est entre les crochets
			var aTraiter=tScope[i].split("[")[1].split("]")[0];
			// on itere sur les virgules : [elem1, attr à conserver1, elem2,
			// attr à conserver 2,...]
			var elem=aTraiter.split(",");
			for (var j=0;j<elem.length;j=j+2)
			{
			
				var targetElementToSave=elem[j];
				var targetAttributeToSave=elem[j+1];
			
				console.log(divMain);
				console.log(targetElementToSave);
				console.log(targetAttributeToSave);
				console.log(attributesSaved);
				console.log(k);
				console.log(attributesSaved[k]);
				
				
				$("#"+divMain+"").find("["+targetElementToSave+"]")[targetAttributeToSave](attributesSaved[k]);
				k++;
				}
			}	
	}
	return tScopeCurrent;
}



function updateSelect(e,t)
{
	var view=$(t).parents().filter('div[id]').attr('id');

	// validation avec entrée
	if (e.keyCode == 13) {
		e.preventDefault();
		$('[id="'+view+'.select"]').trigger('click');
	}

}

function deleteTwilight(e,t,view)
{
	var searchString=$(t).find("[h='type_grappe']").text()+"-"+$(t).find("[h='def_grappe']").text();
	$("[id='"+view+"'] textarea").filter(function(){return $(this).text()==searchString}).css("border","");
	
}

function twilight(e,t,view)
{
var searchString=$(t).find("[h='type_grappe']").text()+"-"+$(t).find("[h='def_grappe']").text();
$("[id='"+view+"'] textarea").filter(function(){return $(this).text()==searchString}).css("border","3px solid #ff0000");

}

function updateSort(e,t)
{
	var view=$(t).parents().filter('div[id]').attr('id');
	$('[name="'+view+'.databaseColumnSort"]').attr('value',$(t).parent().siblings().children().eq($(t).parent().children().index($(t))).text().trim());
	$('[name="'+view+'.databaseColumnSort"]').attr('m','js');
	$('[id="'+view+'.sort"]').trigger('click');
}



function addCells(e,t)
{
	var view=$(t).parents().filter('div[id]').attr('id');

	// validation avec entrée
	if (e.keyCode == 13) {
		e.preventDefault();
		$('[id="'+view+'.add"]').trigger('click');
	}

}


function updateCheckBoxGrid(v,t)
{
	var view=v.split(";");

	
	// test si l'utilisateur a déjà clické sur la case
	// si oui, on retire la class "noselect" pour pouvoir selectionner le
	// contenu de la case et on return sans faire d'action
	var $lineClicked=$('input[name^="'+view[0]+'.selectedLines"]').filter(function(){return $(this).prop('checked') == true});
	var $colClicked=$('input[name^="'+view[0]+'.selectedColumns"]').filter(function(){return $(this).prop('checked') == true});
	
	var $lineSelected=$(t).siblings().find('[name^="'+view[0]+'.selectedLines"]');
	var $colSelected=$('[name^="'+view[0]+'.selectedColumns"]').eq($(t).index()-1);
	
	if ($lineClicked.get(0)==$lineSelected.get(0) && $colClicked.get(0)==$colSelected.get(0))
		{
		$(t).find("textarea").removeClass("noselect");
		return;
		}

	
	
	for (var i=0;i<view.length; i++)
	{
		$('input[name^="'+view[i]+'.selectedLines"]').filter(function(){
			return $(this).prop('checked') == true
		}).attr('m','js').siblings().attr('m','js');
		$('input[name^="'+view[i]+'.selectedLines"]').prop('checked',false);

		$('input[name^="'+view[i]+'.selectedColumns"]').filter(function(){
			return $(this).prop('checked') == true
		}).attr('m','js').siblings().attr('m','js');
		$('[name^="'+view[i]+'.selectedColumns"]').prop('checked',false);
	}

	if ($(t).index()>1) {
		$(t).siblings().find('[name^="'+view[0]+'.selectedLines"]').prop('checked',true).attr('m','js').siblings().attr('m','js');
		$('[name^="'+view[0]+'.selectedColumns"]').eq($(t).index()-1).prop('checked',true).attr('m','js').siblings().attr('m','js');
		$('[id="'+view[0]+'.see"]').trigger('click');
	}
}

var selected;
function selectByGrid(v,t)
{
	var view=v.split(";");

	var $lineClicked=$('input[name^="'+view[0]+'.selectedLines"]').filter(function(){return $(this).prop('checked') == true});
	var $colClicked=$('input[name^="'+view[0]+'.selectedColumns"]').filter(function(){return $(this).prop('checked') == true});
	
	var $lineSelected=$(t).siblings().find('[name^="'+view[0]+'.selectedLines"]');
	var $colSelected=$('[name^="'+view[0]+'.selectedColumns"]').eq($(t).index()-1);
	

	
	for (var i=0;i<view.length; i++)
	{
		$('input[name^="'+view[i]+'.selectedLines"]').filter(function(){
			return $(this).prop('checked') == true
		}).attr('m','js').siblings().attr('m','js');
		$('input[name^="'+view[i]+'.selectedLines"]').prop('checked',false);

		$('input[name^="'+view[i]+'.selectedColumns"]').filter(function(){
			return $(this).prop('checked') == true
		}).attr('m','js').siblings().attr('m','js');
		$('[name^="'+view[i]+'.selectedColumns"]').prop('checked',false);
		
		$('#'+view[i]).find(t.get(0).tagName).removeClass("boxSelect");
	}

	if ($lineClicked.get(0)==$lineSelected.get(0) && $colClicked.get(0)==$colSelected.get(0))
	{
		return;
	}
	
		$(t).siblings().find('[name^="'+view[0]+'.selectedLines"]').prop('checked',true).attr('m','js').siblings().attr('m','js');
		$('[name^="'+view[0]+'.selectedColumns"]').eq($(t).index()-1).prop('checked',true).attr('m','js').siblings().attr('m','js');
		$(t).addClass("boxSelect")

	
	
}



/**
 * Mise à jour de la console
 */
function updateConsole()
{
	if ($('[name="consoleIhm"]').get(0)!=undefined)
	{

		var view=$('[name="consoleIhm"]').attr("target");
		jQuery.post(
				serverUrl+view+".action",null
				, function(data, textStatus) {
					$('[name="consoleIhm"]').append(data);


					var contentConsole="";
					var lines=$('[name="consoleIhm"]').text().split("\n");

					for (var i=Math.max(0,lines.length-nbLinesConsole);i<lines.length;i++)
					{
						contentConsole=contentConsole+lines[i];
						if (i<lines.length-1)
						{
							contentConsole=contentConsole+"\n"
						}

					}

					$('[name="consoleIhm"]').text(contentConsole);
					contentConsole="";


					$('[name="consoleIhm"]').scrollTop($('[name="consoleIhm"]')[0].scrollHeight);
					if (updateConsoleState)
					{
						setTimeout(function(){ updateConsole();},1000);
					}
				}
		);

	}
}

/**
 * Rafraichit la session persistante tous les 500s
 */
function sessionPersist()
{
	setTimeout(function(){$('[id$=".select"]').first().trigger('click'); sessionPersist();},300000);

}

function triggerButton(action)
{
	$('[id="'+action+'"]').trigger('click');	
}

function triggerAction(view,action)
{
	$('[id="'+view+'.'+action+'"]').trigger('click');	
}

function gotoPage(view,t,delta)
{
	$("[name='"+view+".idPage']").val(parseInt($("[name='"+view+".idPage']").val())+parseInt(delta));
	$("[name='"+view+".idPage']").attr('m','js');

	$('[id="'+view+'.select"]').trigger('click');
}

// USAGE: $("#form").serializefiles();
(function($) {
	$.fn.serializefiles = function() {
		var obj = $(this);
		/* ADD FILE TO PARAM AJAX */
		var formData = new FormData();
		$.each($(obj).find("input[type='file']"), function(i, tag) {
			$.each($(tag)[0].files, function(i, file) {
				formData.append(tag.name, file);
			});
		});
		var params = $(obj).serializeArray();
		$.each(params, function (i, val) {
			formData.append(val.name, val.value);
		});
		return formData;
	};
})(jQuery);



jQuery.param2 = function( a, traditional ) {
	var r20 = /%20/g;

	var prefix,
	s = [],
	add = function( key, value, todo ) {
		// If value is a function, invoke it and return its value
		value = jQuery.isFunction( value ) ? value() : ( value == null ? "" : value );

		if (todo!=undefined)
		{
			s[ s.length ] = encodeURIComponent( key ) + "=" + encodeURIComponent( value );
		}
		else
		{
			s[ s.length ] ="";
		}
	};

	// Set traditional to true for jQuery <= 1.3.2 behavior.
	if ( traditional === undefined ) {
		traditional = jQuery.ajaxSettings && jQuery.ajaxSettings.traditional;
	}

	// If an array was passed in, assume that it is an array of form elements.
	if ( jQuery.isArray( a ) || ( a.jquery && !jQuery.isPlainObject( a ) ) ) {
		// Serialize the form elements
		jQuery.each( a, function() {
			add( this.name, this.value, this.todo );
		});

	} else {
		// If traditional, encode the "old" way (the way 1.3.2 or older
		// did it), otherwise encode params recursively.
		for ( prefix in a ) {
			buildParams( prefix, a[ prefix ], traditional, add );
		}
	}

	// Return the resulting serialization
	var t=[];
	for (var i=0;i<s.length;i++)
	{
		if (s[i]!="")
		{
			t[ t.length ]=s[i];
		}
	}

	return t.join( "&" ).replace( r20, "+" );
};


jQuery.fn.extend({
	serialize2: function() {
		return jQuery.param2( this.serializeArray2() );
	},
	serializeArray2: function() {

		var rCRLF = /\r?\n/g,
		rsubmitterTypes = /^(?:submit|button|image|reset|file)$/i,
		rsubmittable = /^(?:input|select|textarea|keygen)/i,
		rcheckableType = (/^(?:checkbox|radio)$/i);

		return this.map(function() {
			// Can add propHook for "elements" to filter or add form elements
			var elements = jQuery.prop( this, "elements" );
			return elements ? jQuery.makeArray( elements ) : this;
		})
		.filter(function() {
			var type = this.type;

			// Use .is( ":disabled" ) so that fieldset[disabled] works
			return this.name && !jQuery( this ).is( ":disabled" ) &&
			rsubmittable.test( this.nodeName ) && !rsubmitterTypes.test( type ) &&
			( this.checked || !rcheckableType.test( type ) );
		})
		.map(function( i, elem ) {
			var val = jQuery( this ).val();
			var m = jQuery( this ).attr("m");

			return val == null ?
					null :
						jQuery.isArray( val ) ?
								jQuery.map( val, function( val ) {
									return { name: elem.name, value: val.replace( rCRLF, "\r\n" ), todo: m };
								}) :
								{ name: elem.name, value: val.replace( rCRLF, "\r\n" ), todo: m };
		}).get();
	}
});



function setCookie(key, value) {
	var expires = new Date();
	expires.setTime(expires.getTime() + (1 * 24 * 60 * 60 * 1000));

	document.cookie = key + '=' + encodeURIComponent(value) + ';expires=' + expires.toUTCString();
}

function getCookie(key) {
	var keyValue = document.cookie.match('(^|;) ?' + key + '=([^;]*)(;|$)');
	return keyValue ? decodeURIComponent(keyValue[2]) : null;
}

/*
 * ! jQuery TextChange Plugin
 * http://www.zurb.com/playground/jquery-text-change-custom-event
 * 
 * Copyright 2010, ZURB Released under the MIT License
 */
(function(a){a.event.special.textchange={setup:function(){a(this).data("lastValue",this.contentEditable==="true"?a(this).html():a(this).val());a(this).on("keyup.textchange",a.event.special.textchange.handler);a(this).on("cut.textchange paste.textchange input.textchange",a.event.special.textchange.delayedHandler)},teardown:function(){a(this).off(".textchange")},handler:function(){a.event.special.textchange.triggerIfChanged(a(this))},delayedHandler:function(){var c=a(this);setTimeout(function(){a.event.special.textchange.triggerIfChanged(c)},
		25)},triggerIfChanged:function(a){var b=a[0].contentEditable==="true"?a.html():a.val();b!==a.data("lastValue")&&(a.trigger("textchange",[a.data("lastValue")]),a.data("lastValue",b))}};a.event.special.hastext={setup:function(){a(this).on("textchange",a.event.special.hastext.handler)},teardown:function(){a(this).off("textchange",a.event.special.hastext.handler)},handler:function(c,b){b===""&&b!==a(this).val()&&a(this).trigger("hastext")}};a.event.special.notext={setup:function(){a(this).on("textchange",
				a.event.special.notext.handler)},teardown:function(){a(this).off("textchange",a.event.special.notext.handler)},handler:function(c,b){a(this).val()===""&&a(this).val()!==b&&a(this).trigger("notext");}}})(jQuery);


$.fn.hasScroll = function(axis){
    var sX = this.css("overflow-x"),
        sY = this.css("overflow-y");

    if(typeof axis == "undefined"){
        // Check both x and y declarations
        if(
            (sX == "hidden" && sY == "hidden") ||
            (sX == "visible" && sY == "visible")
        ){
            return false;
        }

        if(sX == "scroll" || sY == "scroll"){
            return true;
        }
    }else{
        // Check individual axis declarations
        switch(axis){
            case "x":
                if(sX == "hidden" || sX == "visible") return false;
                if(sX == "scroll") return true;
            break;
            case "y":
                if(sY == "hidden" || sY == "visible") return false;
                if(sY == "scroll") return true;
            break;
        }
    }

    // Compare client and scroll dimensions to see if a scrollbar is needed
    var bVertical = this[0].clientHeight < this[0].scrollHeight,
        bHorizontal = this[0].clientWidth < this[0].scrollWidth;

    return bVertical || bHorizontal;
};


/*
 * Notre tableau est de la forme suivante
 * 
 * |filterField[0] |filterField[1] |filterField[2] |filterField[3]
 * |filterField[4]... viewCampagne.selectedLines[0]
 * |viewCampagne.content.t[0].d[0] |viewCampagne.content.t[0].d[1]
 * |viewCampagne.content.t[0].d[2] |viewCampagne.content.t[0].d[3]
 * |viewCampagne.content.t[0].d[4] viewCampagne.selectedLines[1]
 * |viewCampagne.content.t[1].d[0] |viewCampagne.content.t[1].d[1]
 * |viewCampagne.content.t[1].d[2] |viewCampagne.content.t[1].d[3]
 * |viewCampagne.content.t[1].d[4] viewCampagne.selectedLines[2]
 * |viewCampagne.content.t[2].d[0] |viewCampagne.content.t[2].d[1]
 * |viewCampagne.content.t[2].d[2] |viewCampagne.content.t[2].d[3]
 * |viewCampagne.content.t[2].d[4] ... viewCampagne.selectedLines[n]
 * |viewCampagne.content.t[n].d[0] |viewCampagne.content.t[n].d[1]
 * |viewCampagne.content.t[n].d[2] |viewCampagne.content.t[n].d[3]
 * |viewCampagne.content.t[n].d[4] |viewCampagne.inputFields[0]
 * |viewCampagne.inputFields[1] |viewCampagne.inputFields[2]
 * |viewCampagne.inputFields[3] |viewCampagne.inputFields[4]
 * 
 */

var map = {}; // pour stocker les inputs
var table;
var cel;
var ligne;

function accessibility (e,t){
	if (e.keyCode==17 || map[17] ){ // pour éviter comportements étranges, on
									// regarde si on appuie sur ctrl
		map[e.keyCode] = e.type == 'keydown';	


		// On récupère le tableau dans lequel on est
		table = $(this).parents().filter("table");
		
		// la cellule active
		cel = $(":focus");
		

		if (map[17] && map[37]) { // CTRL+GAUCHE
			goLeft(cel)
		} else if (map[17] && map[38]) { // CTRL+HAUT
			goUp(cel);
		} else if (map[17] && map[39]) { // CTRL+DROITE
			goRight(cel)
		} else if (map[17] && map[40]) { // CTRL+BAS
			goDown(cel)
		}
	}
}


// si on relache ctrl on vide la list des input
function ctrlKeyUp(e,t){
	if (e.keyCode == 17) {
		e.preventDefault();
		map={};
	}
}


function goUp(cel){
	table = cel.parents().filter("tr").siblings();	// on récupère les lignes de
													// la table

	var nbLigne = table.length;
	nbLigne = nbLigne -1; 							// on doit rajouter 1 car on
													// prend en compte l'élément
													// actuel également
	
	var partsArray = cel.attr("name").split('.');   // on parse le chaine le nom
													// de l'objet selectionné
	
	if(cel.attr("name").indexOf("content")!=-1){
		
		// cas général viewA.content.t[Y].d[X]
		var tY = partsArray[2];							// on récupère t[Y]
		
		var numCel = parseInt(tY.split('[')[1].split(']')[0]) 	// on récupère Y
		
			numCel = numCel -1;
			partsArray[2]='t['+numCel+']'
			/*
			 * C'est moche mais ça fait le taf En gros pour savoir si on a
			 * atteint la limite haute du tableau on regarde si une case
			 * 'content' existe si oui on la focus, sinon on focus le
			 * filterFields
			 */
			if ($('[name="'+partsArray[0]+"."+partsArray[1]+"."+partsArray[2]+"."+partsArray[3]+'"]').length){
				$('[name="'+partsArray[0]+"."+partsArray[1]+"."+partsArray[2]+"."+partsArray[3]+'"]').focus()
			} else {				
				var numLigne = partsArray[3].substring(1);						// on
																				// récupère
																				// [X]
																				// (le
																				// num
																				// de
																				// la
																				// col)
				
				$('[name="'+partsArray[0]+'.filterFields'+numLigne+'"]').focus()
			}
			
	}else if (cel.attr("name").indexOf("inputFields")!=-1){
		
		var numCol = parseInt(partsArray[1].split('[')[1].split(']')[0]);
		
		$('[name="'+partsArray[0]+'.content.t['+nbLigne+'].d['+numCol+']"]').focus()
	} else if (cel.attr("name").indexOf("selectedLines")!=-1){
		var partsArray = cel.attr("name").split('.');   // on parse le chaine le
														// nom de l'objet
														// selectionné
		// cas général viewA.content.t[Y].d[X]
		var tY = partsArray[1];							// on récupère t[Y]
		
		var numCel = parseInt(tY.split('[')[1].split(']')[0]) 	// on récupère Y
		
		if (numCel>0) {
			numCel = numCel -1;
			partsArray[1]='selectedLines['+numCel+']'
			
			$('[name="'+partsArray[0]+"."+partsArray[1]+'"]').focus()
	}
	}
	map[38]=false;
	
}

function goDown(cel){
	table = cel.parents().filter("tr").siblings();	// on récupère les lignes de
													// la table

	var nbLigne = table.length;
	nbLigne = nbLigne +1; 							// on doit rajouter un car
													// on prend en compte
													// l'élément actuel
													// également
				
	var partsArray = cel.attr("name").split('.');   // on parse le chaine le nom
													// de l'objet selectionné
	
	if(cel.attr("name").indexOf("content")!=-1){
	
		// cas général viewA.content.t[Y].d[X]
		var tY = partsArray[2];							// on récupère t[Y]
		
		var numCel = parseInt(tY.split('[')[1].split(']')[0]) 	// on récupère Y
		
			numCel = numCel +1;
			partsArray[2]='t['+numCel+']'
			
			/*
			 * C'est moche mais ça fait le taf En gros pour savoir si on a
			 * atteint la limite basse du tableau on regarde si une case content
			 * existe si oui on la focus, sinon on focus l'inputField
			 */
			if ($('[name="'+partsArray[0]+"."+partsArray[1]+"."+partsArray[2]+"."+partsArray[3]+'"]').length){
				$('[name="'+partsArray[0]+"."+partsArray[1]+"."+partsArray[2]+"."+partsArray[3]+'"]').focus()
			} else {				
				var numLigne = partsArray[3].substring(1);						// on
																				// récupère
																				// [X]
																				// (le
																				// num
																				// de
																				// la
																				// col)
				$('[name="'+partsArray[0]+'.inputFields'+numLigne+'"]').focus()
			}
			

	} else if (cel.attr("name").indexOf("filterFields")!=-1){
	
	var numCol = parseInt(partsArray[1].split('[')[1].split(']')[0]);
	
	$('[name="'+partsArray[0]+'.content.t[0].d['+numCol+']"]').focus()
	
}else if (cel.attr("name").indexOf("selectedLines")!=-1){
		var partsArray = cel.attr("name").split('.');   // on parse le chaine le
														// nom de l'objet
														// selectionné
		// cas général viewA.content.t[Y].d[X]
		var tY = partsArray[1];							// on récupère t[Y]
		
		var numCel = parseInt(tY.split('[')[1].split(']')[0]) 	// on récupère Y
		
		if (numCel < nbLigne) {
			numCel = numCel +1;
			partsArray[1]='selectedLines['+numCel+']'
			
			$('[name="'+partsArray[0]+"."+partsArray[1]+'"]').focus()
	}
	}
	map[40]=false;
}

function goLeft(cel){
	ligne = cel.parent().siblings();

	var nbCase = ligne.length;
	nbCase = nbCase +1; 							// on doit rajouter un car
													// on prend en compte
													// l'élément actuel
													// également
	var partsArray = cel.attr("name").split('.');   // on parse le chaine le nom
													// de l'objet selectionné
	
	if(cel.attr("name").indexOf("content")!=-1){	// on regarde si on est dans
													// une case content
		
		if(cel.attr("name").indexOf("content")!=-1){	
											// cas viewA.content.t[Y].d[X]
			var dX = partsArray[3];							// on récupère d[X]
			
			var numCel = parseInt(dX.split('[')[1].split(']')[0]) 	// on
																	// récupère
																	// X
			if (numCel>0){								// si le numCel>0 on
														// peut encore aller sur
														// la gauche
				
				numCel = numCel -1;
				partsArray[3]='d['+numCel+']'
				
				while(numCel>0 &&
						!$('[name="'+partsArray[0]+"."+partsArray[1]+"."+partsArray[2]+"."+partsArray[3]+'"]').is(":visible")){
					numCel = numCel -1;
					partsArray[3]='d['+numCel+']'
				}
				if (numCel>=0) {
					$('[name="'+partsArray[0]+"."+partsArray[1]+"."+partsArray[2]+"."+partsArray[3]+'"]').focus()
				} else {
					var numLigne = partsArray[2].substring(1);						// on
																					// récupère
																					// [Y]
																					// (le
																					// num
																					// de
																					// la
																					// ligne)

					$('[name="'+partsArray[0]+'.selectedLines'+numLigne+'"]').focus()
				}
				
			} else if (numCel==0){						// on doit aller sur la
														// case selectdLines
				var numLigne = partsArray[2].substring(1);						// on
																				// récupère
																				// [Y]
																				// (le
																				// num
																				// de
																				// la
																				// ligne)

				$('[name="'+partsArray[0]+'.selectedLines'+numLigne+'"]').focus()
			}
		} 
	} else if (cel.attr("name").indexOf("inputFields")!=-1){
		var dX = partsArray[1];							// on récupère d[X]
		
		var numCel = parseInt(dX.split('[')[1].split(']')[0]) 	// on récupère X
		
		numCel = numCel -1;
		while(numCel>0 && !$('[name="'+partsArray[0]+'.inputFields['+numCel+']"]').is(":visible") ){
			numCel = numCel -1;
		}
		$('[name="'+partsArray[0]+'.inputFields['+numCel+']"]').focus()
			
	}else if (cel.attr("name").indexOf("filterFields")!=-1){
		var dX = partsArray[1];							// on récupère d[X]
		
		var numCel = parseInt(dX.split('[')[1].split(']')[0]) 	// on récupère X
		
		numCel = numCel -1;
			
		while(numCel>0 && !$('[name="'+partsArray[0]+'.filterFields['+numCel+']"]').is(":visible") ){
			numCel = numCel -1;
		}
		$('[name="'+partsArray[0]+'.filterFields['+numCel+']"]').focus()
			
	}

	map[37]=false;
}

function goRight(cel){

	ligne = cel.parent().siblings();

	var nbCase = ligne.length;
	nbCase = nbCase +1; 							// on doit rajouter un car
													// on prend en compte
													// l'élément actuel
													// également
				
	var partsArray = cel.attr("name").split('.');   // on parse le chaine le nom
													// de l'objet selectionné
													// cas général
													// viewA.content.t[Y].d[X]

	if(cel.attr("name").indexOf("selectedLine")!=-1){ // on regarde si on est
														// dans une case
														// selectedLine
		var longueurChaine =  ('selectedLines').length;
		var numLigne = partsArray[1].substring(longueurChaine);				// on
																			// récupère
																			// [Y]
																			// (le
																			// num
																			// de
																			// la
																			// ligne)
		
		var i = 0;
		while(i<nbCase &&
				!$('[name="'+partsArray[0]+'.content.t'+numLigne+'.d['+i+']"]').is(":visible") ){
			i = i +1;
		}
		$('[name="'+partsArray[0]+'.content.t'+numLigne+'.d['+i+']"]').focus()
		
	}else if(cel.attr("name").indexOf("content")!=-1){
		var dX = partsArray[3];							// on récupère d[X]
		
		var numCel = parseInt(dX.split('[')[1].split(']')[0]) 	// on récupère X
				
		if (numCel<nbCase) {
			numCel = numCel +1;
			partsArray[3]='d['+numCel+']'

			while(numCel<nbCase &&
					!$('[name="'+partsArray[0]+"."+partsArray[1]+"."+partsArray[2]+"."+partsArray[3]+'"]').is(":visible") ){
				numCel = numCel +1;
				partsArray[3]='d['+numCel+']'
			}
			$('[name="'+partsArray[0]+"."+partsArray[1]+"."+partsArray[2]+"."+partsArray[3]+'"]').focus()
		
		}
	}else if (cel.attr("name").indexOf("inputFields")!=-1){
		var dX = partsArray[1];							// on récupère d[X]
		
		var numCel = parseInt(dX.split('[')[1].split(']')[0]) 	// on récupère X
		
		numCel = numCel +1;
			
		while(numCel<nbCase && !$('[name="'+partsArray[0]+'.inputFields['+numCel+']"]').is(":visible") ){
			numCel = numCel +1;
		}
		$('[name="'+partsArray[0]+'.inputFields['+numCel+']"]').focus()
			
	} else if (cel.attr("name").indexOf("filterFields")!=-1){
		var dX = partsArray[1];							// on récupère d[X]
		
		var numCel = parseInt(dX.split('[')[1].split(']')[0]) 	// on récupère X
		
		numCel = numCel +1;
			
		while(numCel<nbCase && !$('[name="'+partsArray[0]+'.filterFields['+numCel+']"]').is(":visible") ){
			numCel = numCel +1;
		}
		$('[name="'+partsArray[0]+'.filterFields['+numCel+']"]').focus()
			
	}
	map[39]=false;
}


// Pour navbar bootstrap
$(function() {
	var loc = window.location.href;
	   $(".navbar .navbar-nav > li").each(function() {
	      if (loc.match($(this).children('a').attr('href'))) {
	        $(this).addClass("active"); 
	        }
	   });
	   
	   $(".dropdown-menu>li").each(function() {
		      if ($(this).children('a').length>0 && loc.match($(this).children('a').attr('href'))) {
		        $(this).addClass("active"); 
		        $(this).parents(".dropdown").addClass("active"); 
		        }
		   });
	      
	   $('a[href="selectOutils"]').on('click', function(e) {		   
		   if (($('#Campagne_selected').text().length)==0) {
			   
			   alert('Veuillez selectionner une campagne sur la page d\'index');
			    e.preventDefault();
			    return false;
		}
		  
		});
	   
	   $('a[href="selectGrappeVisibilite"]').on('click', function(e) {		   
		   if (($('#Campagne_selected').text().length)==0) {
			   
			   alert('Veuillez selectionner une campagne sur la page d\'index');
			    e.preventDefault();
			    return false;
		}
		  
		});
	   
	   $('a[href="selectGererParametre"]').on('click', function(e) {		   
		   if (($('#Campagne_selected').text().length)==0) {
			   
			   alert('Veuillez selectionner une campagne sur la page d\'index');
			    e.preventDefault();
			    return false;
		}
		  
		});
	   
	   
	});

