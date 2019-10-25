var configJS="Render:FixedHeader;" +
		"ICS:AjaxDataSelector;" +
		"VObject:Sort;" +
		"Render:TextareaEllipsis;" +
		"IHM:TextareaHotkeys;" +
		"IHM:TableMultiCheckbox;" +
		"Render:AlertBox;" +
		"IHM:Onglet;";

$( document ).on('ready readyAgain',function() {
	
	$('#chooseModule a').on('click', function (e) {
		  e.preventDefault()
		  $(this).tab('show')
		})
		
}
);
