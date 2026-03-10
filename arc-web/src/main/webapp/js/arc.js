var lineHeight=22;
var nbLinesConsole=500;
var couleurSelection="rgb(106, 171, 224)";
var couleurOver="rgb(206, 171, 224)";
var opaciteSelection='0.8';

function arcReady() {
	// Add a connection check
	$("#connectionCheck").click(function() {
		checkConnection(event, this);
	})
}

//ping the database to check the connection
function checkConnection (event, el){
	var $el = $(el);
	$.post("status", function( data ) {
		 var status = JSON.parse(data);
		 if(status.code==0){
			 $el.addClass("btn-sucess");
			 $el.removeClass("btn-danger")

		 } else {
			 $el.addClass("btn-danger");
			 $el.removeClass("btn-success")
		 }
		 $el.text(status.commentary)
			 
		})
		.fail(function(){
			console.log("an error occured :( ")
		});
}

$(document).ready(arcReady);
$(document).on('readyAgain', arcReady);
