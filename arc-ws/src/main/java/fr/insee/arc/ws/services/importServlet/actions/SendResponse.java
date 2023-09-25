package fr.insee.arc.ws.services.importServlet.actions;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.utils.LoggerHelper;



/**Cette classe permet d'envoyer la réponse au client via un flux compressé.
 * La réponse aura une des formes suivantes :
 * 
 * Dans le cas d'un appel au service "arcClient" :
 * {
 *	"type":"jsonwsp/response",
 *	"responses":
 *		[
 *			{
 *				"id":"int",
 *				"table":
 *					[
 *						"nomColonne1;nomColonne2;...;nomColonnen;",
 *						"typeColonne1;typeColonne2;...;typeColonnen;",
 *						"a11;a12;...;a1n;",
 *						"...",
 *						"an1;an2;...;ann";
 *					]
 *			},
 *		],
 *	"nomenclatures" :
 *		[
 *			{
 *				"id":"int",
 *				"table":
 *					[
 *						"nomColonne1;nomColonne2;...;nomColonnen;",
 *						"typeColonne1;typeColonne2;...;typeColonnen;",
 *						"a11;a12;...;a1n;",
 *						"...",
 *						"an1;an2;...;ann;"
 *					]
 *			},
 *		],
 * 	"varMetier" :
 *		[
 *			{
 *				"id":"int",
 *				"table":
 *					[
 *						"nomColonne1;nomColonne2;...;nomColonnen;",
 *						"typeColonne1;typeColonne2;...;typeColonnen;",
 *						"a11;a12;...;a1n;",
 *						"...",
 *						"an1;an2;...;ann;"
 *					]
 *			},
 *		]
 * }
 *
 * - dans le cas d'une erreur :
 * {
 *	"type":"jsonwsp/response",
 *	"error":"string"
 * }
 *
 * @author N6YF91
 *
 */
public class SendResponse {

    private static final Logger LOGGER = LogManager.getLogger(SendResponse.class);

	private ServletOutputStream wr;

	public SendResponse( HttpServletResponse response ){
		try {
			response.setBufferSize(128 * 1024);
			this.wr=response.getOutputStream();
		}
		catch (IOException e) {
			StaticLoggerDispatcher.error(LOGGER, "** Error in servlet SendResponse **");
		}
	}



	/**Ecrit la chaîne de caractères dans le flux de réponse compressé.
	 * @param string
	 */
	public void send( String string ){
			try {
				this.wr.write( string.getBytes() );//"UTF-8"
			} catch (IOException ex) {
				LoggerHelper.errorGenTextAsComment(getClass(), "send()", LOGGER, ex);
			}
	}



	/**Fermeture du flux.
	 *
	 */
	public void endSending(){
		try {
			this.wr.flush();
			this.wr.close();
		} catch (IOException ex) {
			LoggerHelper.errorGenTextAsComment(getClass(), "endSending()", LOGGER, ex);
		}
	}



	public ServletOutputStream getWr() {
		return wr;
	}



	public void setWr(ServletOutputStream wr) {
		this.wr = wr;
	}
	
	
	
	
}
