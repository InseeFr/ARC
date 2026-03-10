package fr.insee.arc.ws.services.importServlet.bo;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.ws.services.importServlet.ImportStep1InitializeClientTablesService;
import fr.insee.arc.ws.services.importServlet.ImportStep2GetTableNameService;
import fr.insee.arc.ws.services.importServlet.ImportStep3GetTableDataService;
import fr.insee.arc.ws.services.importServlet.ServletArc;
import fr.insee.arc.ws.services.importServlet.actions.SendResponse;

public class ExecuteStep extends ServletArc {

	private static final long serialVersionUID = -4856211705461299454L;

	public static String executeImportStep1(JSONObject clientJsonInput) throws ArcException, UnsupportedEncodingException
	{
		ArcClientIdentifier clientJsonInputValidated= new ArcClientIdentifier(new ArcClientIdentifierUnsafe(clientJsonInput), null);
		ImportStep1InitializeClientTablesService imp = new ImportStep1InitializeClientTablesService(clientJsonInputValidated);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		SendResponse sentResponse = new SendResponse(bos);
		imp.execute(sentResponse);
		return bos.toString(StandardCharsets.UTF_8);
	}
	
	public static String executeImportStep2(JSONObject clientJsonInput) throws ArcException, UnsupportedEncodingException
	{
		ArcClientIdentifier clientJsonInputValidated= new ArcClientIdentifier(new ArcClientIdentifierUnsafe(clientJsonInput), null);
		ImportStep2GetTableNameService imp = new ImportStep2GetTableNameService(clientJsonInputValidated);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		SendResponse sentResponse = new SendResponse(bos);
		imp.execute(sentResponse);
		return bos.toString(StandardCharsets.UTF_8);
	}
	
	public static ByteArrayOutputStream executeImportStep3(JSONObject clientJsonInput) throws ArcException, UnsupportedEncodingException
	{
		ArcClientIdentifier clientJsonInputValidated= new ArcClientIdentifier(new ArcClientIdentifierUnsafe(clientJsonInput), null);
		ImportStep3GetTableDataService imp = new ImportStep3GetTableDataService(clientJsonInputValidated);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		SendResponse sentResponse = new SendResponse(bos);
		imp.execute(sentResponse);
		return bos;
	}
	
	
}
