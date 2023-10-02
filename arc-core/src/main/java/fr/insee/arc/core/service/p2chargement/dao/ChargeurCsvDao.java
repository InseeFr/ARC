package fr.insee.arc.core.service.p2chargement.dao;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p2chargement.bo.FileAttributes;
import fr.insee.arc.core.service.p2chargement.bo.FormatRulesCsv;
import fr.insee.arc.core.service.p2chargement.bo.Norme;
import fr.insee.arc.core.service.p2chargement.engine.ParseFormatRulesOperation;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;

public class ChargeurCsvDao {

	private Sandbox sandbox;
	private FileAttributes fileAttributes;
	private Norme norme;
	private ParseFormatRulesOperation<FormatRulesCsv> parser; 
	
	public ChargeurCsvDao(Sandbox sandbox, FileAttributes fileAttributes, Norme norme, ParseFormatRulesOperation<FormatRulesCsv> parser)
	{
		this.sandbox = sandbox;
		this.fileAttributes = fileAttributes;
		this.norme= norme;
		this.parser=parser;
	}
	
	/**
	 * evaluate by posgres a character expression
	 * @param expression
	 * @return
	 * @throws ArcException
	 */
	public String execQueryEvaluateCharExpression(String expression) throws ArcException
	{		
		// si le quote est une expression complexe, l'interpreter par postgres
		if (expression != null && expression.length() > 1 && expression.length() < 8) {
			ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
			query.append("SELECT " + expression + " ");
			return UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), query).get(2).get(0);
		}
		else
		{
			return expression;
		}
	}
	
	
	/**
	 * Create the table where the csv file data will be stored
	 * 
	 * @throws ArcException
	 */
	public void initializeCsvTableContainer() throws ArcException {
		StringBuilder req = new StringBuilder();
		req.append("DROP TABLE IF EXISTS " + ViewEnum.TMP_CHARGEMENT_BRUT.getFullName() + ";");
		
		req.append("CREATE TEMPORARY TABLE " + ViewEnum.TMP_CHARGEMENT_BRUT.getFullName() + " (");
		for (String nomCol : fileAttributes.getHeadersV()) {
			req.append(nomCol).append(" text,");
		}
		req.append("id SERIAL");
		req.append(");");

		UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), req);
	}

	
	
	public void execQueryCopyCsv(InputStream streamContent) throws ArcException
	{

		// tuple des headers
		String columns = "(" + StringUtils.join(fileAttributes.getHeadersV(), SQL.COMMA.toString())+ ")";
		
		boolean ignoreFirstLine = (parser.getValue(FormatRulesCsv.HEADERS) == null);

		String separateur = norme.getRegleChargement().getDelimiter();
		
		String quote = parser.getValue(FormatRulesCsv.QUOTE);
		
		String encoding = parser.getValue(FormatRulesCsv.ENCODING);

		UtilitaireDao.get(0).importing(this.sandbox.getConnection(), ViewEnum.TMP_CHARGEMENT_BRUT.getFullName(), columns, streamContent,
				ignoreFirstLine, separateur, quote, encoding);
	}
	
	
	/**
	 * Create the final table of loaded file with metadata and all required columns (i_col, v_col)
	 * @throws ArcException
	 */
	public void execQueryCreateContainerWithArcMetadata() throws ArcException
	{
	StringBuilder req = new StringBuilder();
	req.append("DROP TABLE IF EXISTS " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName() + ";");
	req.append("CREATE TEMPORARY TABLE " + ViewEnum.TMP_CHARGEMENT_ARC.getFullName());
	req.append(" AS (SELECT ");
	req.append("\n '" + fileAttributes.getFileName() + "'::text collate \"C\" as " + ColumnEnum.ID_SOURCE.getColumnName());
	req.append("\n ,id::integer as id");
	req.append("\n ," + fileAttributes.getIntegrationDate() + "::text collate \"C\" as date_integration ");
	req.append("\n ,'" + norme.getIdNorme() + "'::text collate \"C\" as id_norme ");
	req.append("\n ,'" + norme.getPeriodicite() + "'::text collate \"C\" as periodicite ");
	req.append("\n ,'" + fileAttributes.getValidite() + "'::text collate \"C\" as validite ");
	req.append("\n ,0::integer as nombre_colonne");

	req.append("\n , ");

	for (int i = 0; i < fileAttributes.getHeaders().length; i++) {
		req.append("id as " + fileAttributes.getHeadersI()[i] + ", " + fileAttributes.getHeadersV()[i] + ",");
	}

	req.setLength(req.length() - 1);

	req.append("\n FROM " + ViewEnum.TMP_CHARGEMENT_BRUT.getFullName() + ");");
	req.append("DROP TABLE IF EXISTS " + ViewEnum.TMP_CHARGEMENT_BRUT.getFullName() + ";");

	UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), req);
	}
	
}
