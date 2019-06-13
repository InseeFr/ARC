var configJS = "Render:FixedHeader;" + "Render:ConsoleIhm;"
		+ "ICS:AjaxDataSelector;" + "VObject:Sort;"
		+ "Render:TextareaEllipsis;" + "IHM:TextareaHotkeys;"
		+ "IHM:TableMultiCheckbox;" + "Render:AlertBox;" + "Render:Pilotage;";

function alimenterPhase(t){
	console.log("savePhaseChoice :"+$("#savePhaseChoice").attr("value"));
	console.log("$(t) :"+$(t).attr("value"));
	$("#savePhaseChoice").attr("value",$(t).attr("value"));
	$("#savePhaseChoice").attr("m","js");
	console.log("savePhaseChoice :"+$("#savePhaseChoice").attr("value"));
	console.log("savePhaseChoice :"+$("#savePhaseChoice").attr("m"));

	return true;
}

$(document).on('ready readyAgain', function() {


	$("#viewPilotageBAS8").find("td").click(function() {
		updateCheckBoxGrid('viewPilotageBAS8;viewRapportBAS8',$(this));
	})

});
