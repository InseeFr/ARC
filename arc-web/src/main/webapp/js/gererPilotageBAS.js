var configJS = "Render:FixedHeader;"
		+ "ICS:AjaxDataSelector;"
		+ "VObject:Sort;"
		+ "Render:TextareaEllipsis;"
		+ "IHM:TextareaHotkeys;"
		+ "IHM:TableMultiCheckbox;"
		+ "Render:AlertBox;";

function alimenterPhase(t){
	$("#savePhaseChoice").attr("value",$(t).attr("value"));
	$("#savePhaseChoice").attr("m","js");
	return true;
}

function pilotageReady() {

	$("#viewPilotageBAS").find("td").off('click').on('click',function() {
		updateCheckBoxGrid('viewPilotageBAS;viewRapportBAS',$(this));
	})
	
	$("#viewRapportBAS").find("td").filter(":nth-child(5n+3)").off('click').on('click',function() {
		updateCheckBoxGrid('viewRapportBAS;viewPilotageBAS',$(this));
	})
	$("#viewRapportBAS").find("td").filter(":nth-child(5n+4)").off('click').on('click',function() {
		updateCheckBoxGrid('viewRapportBAS;viewPilotageBAS',$(this));
	})

	$("[name^='viewPilotage']").filter("[name*='selectedColumns']").closest("table").find("tbody").find("tr").find("td").css("background-color","");
	$("[name^='viewPilotage']").filter("[name*='selectedColumns']:checked").closest("table").find("tbody").find("tr").eq($("[name^='viewPilotage']").filter("[name*='selectedLines']:checked").closest("tr").index()).find("td").eq($("[name^='viewPilotage']").filter("[name*='selectedColumns']:checked").closest("th").index()).css("background-color","#aaaabb");
	$("[name^='viewRapport']").filter("[name*='selectedColumns']:checked").closest("table").find("tbody").find("tr").eq($("[name^='viewRapport']").filter("[name*='selectedLines']:checked").closest("tr").index()).css("background-color","#aaaabb");
	$("[id^='viewPilotage'] .sort:contains(' KO')").attr('style',$("[id^='viewPilotage'] .sort:contains(' KO')").attr('style')+";outline: rgb(255, 0, 0) solid 3px;");

}

function preparePilotageFileSearch(button) {
	var input = $(button).closest('.pilotage-file-search').find('.pilotage-search-input');
	if (!input.val().trim()) {
		input.trigger('focus');
		return false;
	}
	$('#fileSearchPhase').attr('m', 'js');
	$('#fileSearchEtat').attr('m', 'js');
	$('#fileSearchPattern').val(input.val()).attr('m', 'js');
	return true;
}

$(document).ready(pilotageReady);
$(document).on('readyAgain', pilotageReady);

$(document).on('click', '[data-pilotage-file-action]', function(event) {
	if ($('#hourglass').length) {
		return;
	}
	var button = $(this);
	var form = button.closest('form');
	var action = button.attr('data-pilotage-file-action');
	var scope = button.attr('scope');
	var requestData = form.serialize2() + '&scope=' + splitAndEval(scope);
	var hourglass = $('<div id="hourglass"></div>').appendTo('body');

	$.ajax({
		url: 'countPilotageFilesForAction',
		type: 'POST',
		data: requestData,
		dataType: 'text',
		beforeSend: function(xhr) {
			if ($("meta[name='_csrf']").length) {
				xhr.setRequestHeader($("meta[name='_csrf_header']").attr('content'), $("meta[name='_csrf']").attr('content'));
			}
		}
	}).done(function(count) {
		hourglass.remove();
		if (!/^\d+$/.test(count)) {
			window.alert(form.attr('data-file-action-count-error'));
			return;
		}
		if (count === '0') {
			window.alert(form.attr('data-file-action-empty'));
			return;
		}
		var message = form.attr('data-file-action-confirm').replace('__COUNT__', count);
		if (window.confirm(message)) {
			form.attr({action: action, scope: scope});
			ajaxConfigurationCallNoMultipart(event, form, requestData);
		}
	}).fail(function() {
		hourglass.remove();
		window.alert(form.attr('data-file-action-count-error'));
	});
});

