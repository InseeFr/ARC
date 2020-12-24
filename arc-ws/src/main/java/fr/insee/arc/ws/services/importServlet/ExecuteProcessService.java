package fr.insee.arc.ws.services;

import java.io.File;
import java.io.FileOutputStream;

import java.util.Base64;

import org.apache.commons.lang3.EnumUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.ws.actions.SendResponse;
import fr.insee.arc.utils.dao.PreparedStatementBuilder;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.utils.JsonKeys;
import fr.insee.arc.utils.utils.ManipString;

public class ExecuteProcessService {

	private JSONObject dsnRequest;

	public ExecuteProcessService(JSONObject dsnRequest) {
		super();
		this.dsnRequest = dsnRequest;
	}

	private long timestamp;

	private String environnement;

	private String phase;

	private String fileName;

	private String fileData;
	
	String rootDirectory = PropertiesHandler.getInstance().getBatchParametersDirectory();

	private String nbLinesToBeProcessed="10000000";
	
	private String decodedString;
	
	public ExecuteProcessService buildParam() {
		timestamp = System.currentTimeMillis();

		environnement = dsnRequest.getString(JsonKeys.ENVIRONNEMENT.getKey());

		phase = dsnRequest.getString(JsonKeys.PHASE.getKey());

		fileData = dsnRequest.getString("file");
		
		
//		fileName = dsnRequest.getString(JsonKeys.FILENAME.getKey());
//
//		fileData = dsnRequest.getString(JsonKeys.FILEDATA.getKey());

		return this;
	}

	public void execute(SendResponse resp) {

		System.out.println(fileData);
    	byte[] decodedBytes = Base64.getDecoder().decode(fileData.replace(" ","+"));
		this.decodedString = new String(decodedBytes);
	    
	    /**
	     * service invocation
	     */

    	if (phase.equals(TraitementPhase.RECEPTION.toString()))
    	{
    		register();
    	}
	    
	    
	    if (this.phase.endsWith("RULES"))
	    {	    
	    	updateRules();
	    }

	    // execute required service
	    ApiServiceFactory.getService(
				this.phase
				, "arc.ihm"
				, this.environnement
				,  PropertiesHandler.getInstance().getBatchParametersDirectory()
				, nbLinesToBeProcessed
				).invokeApi();
	    
	    
//		ApiServiceFactory.getService(TraitementPhase.RECEPTION.toString(), "arc.ihm", this.environnement,
//				InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire"),
//				String.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter())).invokeApi();
//		ApiServiceFactory.getService(TraitementPhase.CHARGEMENT.toString(), "arc.ihm", this.environnement,
//				InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire"),
//				String.valueOf(TraitementPhase.CHARGEMENT.getNbLigneATraiter())).invokeApi();
//		ApiServiceFactory.getService(TraitementPhase.NORMAGE.toString(), "arc.ihm", this.environnement,
//				InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire"),
//				String.valueOf(TraitementPhase.NORMAGE.getNbLigneATraiter())).invokeApi();
//		ApiServiceFactory.getService(TraitementPhase.CONTROLE.toString(), "arc.ihm", this.environnement,
//				InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire"),
//				String.valueOf(TraitementPhase.CONTROLE.getNbLigneATraiter())).invokeApi();
		resp.send("OK");
		resp.endSending();
          
		 
	}
	
	/**
	 * delete sandbox and register file
	 */
	public void register()
	{
	    // delete sandbox upon register

		try {
			ApiInitialisationService.clearPilotageAndDirectories(this.rootDirectory, this.environnement);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	// set file inside directory
    	if (fileData!=null)
    	{
			
			File f=new File(this.rootDirectory+this.environnement.toUpperCase()+"\\RECEPTION_DEFAULT\\animal.xml");
			
		    try {
				FileOutputStream outputStream = new FileOutputStream(f);
			    byte[] strToBytes = decodedString.getBytes();
				outputStream.write(strToBytes);
			    outputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}

	}
	
	/**
	 * Update rules
	 */
	public void updateRules()
	{
		this.environnement=this.environnement.replace("_", ".");

    	this.phase=ManipString.substringBeforeLast(this.phase, "_RULES");
    	if (this.phase.contentEquals(TraitementPhase.CHARGEMENT.toString()))
    	{
    		System.out.println(">>>>>>>>>"+decodedString);
    		
    		JSONArray j=new JSONArray(decodedString);
    		System.out.println(">>>>>>>>>"+j.getJSONObject(0).getString("FileType"));
    		
    		
    		StringBuilder query=new StringBuilder();
    		// delete rule
    		query.append("\n delete from arc.ihm_chargement_regle a where exists (select from arc.ihm_jeuderegle b where a.id_norme=b.id_norme and a.validite_inf=b.validite_inf and a.validite_sup=b.validite_sup and a.version=b.version and a.periodicite=b.periodicite and b.etat='"+this.environnement+"'); ");

    		// add rule
    		query.append("\n insert into arc.ihm_chargement_regle(id_regle,id_norme,validite_inf,validite_sup,version,periodicite,type_fichier,delimiter,format,commentaire) ");
    		query.append("\n select row_number() over (),id_norme,validite_inf,validite_sup,version,periodicite");
    		query.append("\n ,'"+j.getJSONObject(0).getString("FileType")+"'");
    		query.append("\n , null, null, 'IS2 RULE TEST' ");
    		query.append("\n FROM arc.ihm_jeuderegle ");
    		query.append("\n where etat='"+this.environnement+"' ; ");
    		
    		try {
				UtilitaireDao.get("arc").executeRequest(null, new PreparedStatementBuilder(query));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    
		this.environnement=this.environnement.replace(".", "_");
    	this.phase=TraitementPhase.INITIALISATION.toString();

	}

}
