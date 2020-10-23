var configJS = "Render:FixedHeader;"
		+ "ICS:AjaxDataSelector;"
		+ "VObject:Sort;"
		+ "Render:TextareaEllipsis;"
		+ "IHM:TextareaHotkeys;"
		+ "IHM:TableMultiCheckbox;"
		+ "Render:AlertBox;";

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

	$("#viewPilotageBAS").find("td").off('click').on('click',function() {
		updateCheckBoxGrid('viewPilotageBAS;viewRapportBAS',$(this));
	})

	$("[name^='viewPilotage']").filter("[name*='selectedColumns']").closest("table").find("tbody").find("tr").find("td").css("background-color","");
	$("[name^='viewPilotage']").filter("[name*='selectedColumns']:checked").closest("table").find("tbody").find("tr").eq($("[name^='viewPilotage']").filter("[name*='selectedLines']:checked").closest("tr").index()).find("td").eq($("[name^='viewPilotage']").filter("[name*='selectedColumns']:checked").closest("th").index()).css("background-color","#aaaabb");
	$("[name^='viewRapport']").filter("[name*='selectedColumns']:checked").closest("table").find("tbody").find("tr").eq($("[name^='viewRapport']").filter("[name*='selectedLines']:checked").closest("tr").index()).css("background-color","#aaaabb");
	$("[id^='viewPilotage'] .sort:contains(' KO')").attr('style',$("[id^='viewPilotage'] .sort:contains(' KO')").attr('style')+";outline: rgb(255, 0, 0) solid 3px;");

});
