package fr.insee.arc.core.service.p2chargement.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.p2chargement.bo.XMLColumns;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.security.SqlInjectionChecked;
import fr.insee.arc.utils.utils.FormatSQL;

public class ChargeurXMLDao {

	private Sandbox sandbox;
	private FileIdCard fileIdCard;

	public ChargeurXMLDao(Sandbox sandbox, FileIdCard fileIdCard) {
		this.sandbox = sandbox;
		this.fileIdCard = fileIdCard;
	}

	private static final Logger LOGGER = LogManager.getLogger(ChargeurXMLDao.class);

	/**
	 * crée la table container des chargements
	 * 
	 * @param tableTempA
	 * @throws ArcException
	 */
	@SqlInjectionChecked
	public void execQueryCreateTemporaryLoadDataTable(String tableTempA) throws ArcException {
		StaticLoggerDispatcher.info(LOGGER, "** Création de la table temporaire de chargement tableTempA **");

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		requete.append(FormatSQL.dropTable(tableTempA));
		requete.append("CREATE ");

		if (!tableTempA.contains(".")) {
			requete.append("TEMPORARY ");
		} else {
			requete.append(" ");
		}

		// la table temporaire A : ids|id|d|data|nombre_colonnes
		// data : contiendra les données chargé au format text séparée par des virgules
		// nombre_colonnes : contiendra le nombre de colonne contenue dans data,
		// nécessaire pour compléter la ligne avec des virgules

		requete.append(" TABLE " + tableTempA + " (");
		boolean noComma = true;
		for (int i = 0; i < XMLColumns.tempTableAColumnsLongName.size(); i++) {
			if (noComma) {
				noComma = false;
			} else {
				requete.append(",");
			}
			requete.append(XMLColumns.tempTableAColumnsShortName.get(i) + " "
					+ XMLColumns.tempTableAColumnsLongName.get(i).getColumnType().getTypeCollated() + " ");
		}
		requete.append(") ");
		requete.append(FormatSQL.WITH_NO_VACUUM);
		requete.append(";");

		UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), requete);

		StaticLoggerDispatcher.info(LOGGER,
				"** fin création de la table temporaire de chargement tableTempA **");
	}

	/**
	 * execute la requete bilan qui marque le résultat du chargement dans la table
	 * de piltoage temporaire
	 * 
	 * @param tableChargementPilTemp
	 * @param currentPhase
	 * @param rapport
	 * @param jointure
	 * @throws ArcException
	 */
	@SqlInjectionChecked
	public void execQueryBilan(String tableChargementPilTemp, TraitementPhase currentPhase, String rapport, String jointure)
			throws ArcException {
		ArcPreparedStatementBuilder requeteBilan = new ArcPreparedStatementBuilder();
		requeteBilan.append(ApiService.pilotageMarkIdsource(tableChargementPilTemp, fileIdCard.getIdSource(),
				currentPhase, TraitementEtat.OK, rapport, jointure));
		UtilitaireDao.get(0).executeBlock(sandbox.getConnection(), requeteBilan);
	}

}
