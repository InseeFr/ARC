package fr.insee.arc.core.service.p4controle.operation;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import fr.insee.arc.core.model.XMLConstant;
import fr.insee.arc.core.service.global.bo.FileIdCard;
import fr.insee.arc.core.service.p4controle.bo.ControleTypeCode;
import fr.insee.arc.core.service.p4controle.bo.RegleControle;
import fr.insee.arc.core.service.p4controle.dao.ControleRegleDao;
import fr.insee.arc.core.util.StaticLoggerDispatcher;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;

@Component
public class ServiceJeuDeRegleOperation {

	private static final Logger LOGGER = LogManager.getLogger(ServiceJeuDeRegleOperation.class);
	private ControleRegleDao dao;

	/**
	 * Liste des rubriques de la table de données DSN
	 */
	private List<String> listRubTable = new ArrayList<>();

	public ServiceJeuDeRegleOperation() {
		this.dao = new ControleRegleDao();
	}

	/**
	 * Executer les règles liées à un jeu de règle sur une table donnée
	 *
	 * @param connexion
	 *
	 * @param jdr       le jeu de règle dont il faut appliquer les règles
	 * @param table     la table de travail dont les enregistrement seront "marqués"
	 * @throws ArcException
	 */
	public void executeJeuDeRegle(Connection connexion, FileIdCard fileIdCard, String table) throws ArcException {
		StaticLoggerDispatcher.debug(LOGGER, "executeJeuDeRegle");

		java.util.Date date = new java.util.Date();

		// register the columns from the source table into listRubTable
		registerRubriquesFromSourceTable(connexion, table);

		// execute the correction rules (aka "preAction") before the control step
		preAction(connexion, fileIdCard, table);

		// execute the control rules
		executeQueryControl(connexion, fileIdCard, table);

		StaticLoggerDispatcher.info(LOGGER, "Temps de controle : " + (new java.util.Date().getTime() - date.getTime()));

	}

	/**
	 * Get the columns (also named "rubriques") list in uppercase from the table in
	 * order to know what controls must be evaluated
	 * 
	 * @param connexion
	 * @param table
	 * @throws ArcException
	 */
	private void registerRubriquesFromSourceTable(Connection connexion, String table) throws ArcException {
		this.listRubTable = UtilitaireDao.get(0).getColumns(connexion, table);
		this.listRubTable.replaceAll(String::toUpperCase);
	}

	/**
	 * Execute the correction actions before control
	 * 
	 * @param connexion
	 * @param jdr
	 * @param table
	 * @param structure
	 * @throws ArcException
	 */
	private void preAction(Connection connexion, FileIdCard fileIdCard, String table) throws ArcException {

		// exécuter les préactions
		StaticLoggerDispatcher.info(LOGGER, "Debut Pré-actions");

		List<String> p = new ArrayList<>();

		// récupérer les préactions du jeu de regle
		/**
		 * Attention, on suppose que la preaction ne contient qu'une seule rubrique et
		 * en plus celle de la règle
		 */
		for (RegleControle reg : fileIdCard.getIdCardControle().getReglesControle()) {
			if (reg.getPreAction() != null && !StringUtils.isEmpty(reg.getPreAction())) {
				p.add(ManipString.extractAllRubrique(reg.getPreAction()) + " as " + reg.getRubriquePere());
			}
		}

		// appliquer les pré-actions ; modifier la table avec un fast update
		if (!p.isEmpty()) {
			String[] pa = p.toArray(new String[0]);
			LoggerHelper.debug(LOGGER, "Longueur de mon tableau de préaction :", pa.length);
			LoggerHelper.debug(LOGGER, "Contenu a priori :", p.toString());
			UtilitaireDao.get(0).fastUpdate(connexion, table, "id", this.listRubTable, "true", pa);
		}

		StaticLoggerDispatcher.debug(LOGGER, "Fin Pré-actions");
	}

	/**
	 * Execute the control rules.
	 * 
	 * @param connexion
	 * @param jdr
	 * @param table
	 * @param structure
	 * @throws ArcException
	 */
	private void executeQueryControl(Connection connexion, FileIdCard fileIdCard, String table) throws ArcException {

		StringBuilder blocRequete = new StringBuilder();
		blocRequete.append(this.dao.initTemporaryTable(table));

		int nbRegles = 0;
		int nbTotalRegles = fileIdCard.getIdCardControle().getReglesControle().size();

		for (RegleControle reg : fileIdCard.getIdCardControle().getReglesControle()) {
			nbRegles++;
			//reg.setTable(table); //inutile?

			StaticLoggerDispatcher.info(LOGGER, "n° " + reg.getIdRegle() + " / classe : " + reg.getTypeControle()
					/*+ " / commentaire : " + reg.getCommentaire()*/);
			switch (reg.getTypeControle()) {
			case NUM:
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.dao.ctlIsNumeric(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			case DATE:
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.dao.ctlIsDate(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			case ALPHANUM:
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.dao.ctlIsAlphanum(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			case CARDINALITE:
				if (this.listRubTable.contains(reg.getRubriquePere())
						// rules to set tree root and father label are ignored
						&& !(reg.getRubriquePere().equalsIgnoreCase(XMLConstant.ROOT))) {
					blocRequete.append(executeRegleCardinalite(reg));
					blocRequete.append(System.lineSeparator());
				} else {
					StaticLoggerDispatcher.info(LOGGER,
							"la rubrique : " + reg.getRubriquePere() + " n'existe pas dans ce fichier");
				}
				break;
			case CONDITION:
				blocRequete.append(executeRegleCondition(fileIdCard, reg));
				blocRequete.append(System.lineSeparator());
				break;
			case REGEXP:
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.dao.ctlMatchesRegexp(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			case ENUM_BRUTE:
			case ENUM_TABLE:
				if (regleEstAAppliquer(this.listRubTable, reg)) {
					blocRequete.append(this.dao.ctlIsValueIn(reg));
					blocRequete.append(System.lineSeparator());
				}
				break;
			default:
				StaticLoggerDispatcher.info(LOGGER, "Classe pas connue");
			}

			StaticLoggerDispatcher.info(LOGGER, "Execution de " + nbRegles + "/" + nbTotalRegles);

			if (blocRequete.length() > FormatSQL.TAILLE_MAXIMAL_BLOC_SQL) {
				UtilitaireDao.get(0).executeImmediate(connexion,
						"SET enable_nestloop=off; " + blocRequete.toString() + "SET enable_nestloop=on; ");
				blocRequete.setLength(0);
			}

		}

		StaticLoggerDispatcher.info(LOGGER, "Execution de " + nbRegles + "/" + nbTotalRegles);
		blocRequete.append(this.dao.markTableResultat());
		blocRequete.append(this.dao.dropControleTemporaryMarkTable());

		UtilitaireDao.get(0).executeImmediate(connexion,
				"SET enable_nestloop=off; " + blocRequete + "SET enable_nestloop=on; ");
		StaticLoggerDispatcher.info(LOGGER, "Fin executeJeuDeRegle");

	}

	/** Vérifie si la règle est à appliquer pour ces rubriques. */
	private boolean regleEstAAppliquer(List<String> listRubriques, RegleControle reg) {
		if (!listRubriques.contains(reg.getRubriquePere())) {
			StaticLoggerDispatcher.info(LOGGER,
					"la rubrique : " + reg.getRubriquePere() + " n'existe pas dans ce fichier");
			return false;
		}
		return true;
	}

	/**
	 * Préparation à l'exécution d'une règle de type CONDITION Pour chaque rubrique
	 * de la condition, il faut lui associé sa règle de typage
	 *
	 * @param reg
	 * @param jdr
	 * @param reg
	 * @param table
	 * @throws ArcException
	 */
	private String executeRegleCondition(FileIdCard fileIdCard, RegleControle reg) {
		StaticLoggerDispatcher.info(LOGGER, "Je lance executeRegleCondition()");
		String requete = "";

		List<String> listRubrique = ManipString.extractRubriques(reg.getCondition());
		listRubrique.replaceAll(String::toUpperCase);

		if (this.listRubTable.containsAll(listRubrique)) {
			Map<String, RegleControle> mapRubrique = new HashMap<>();
			for (String rub : listRubrique) {
				StaticLoggerDispatcher.debug(LOGGER, "Je parcours la liste listRubrique sur l'élément : " + rub);
				RegleControle regle = findType(fileIdCard, rub);
				mapRubrique.put(rub, regle);
			}
			StaticLoggerDispatcher.debug(LOGGER, "MapRubrique contient : " + mapRubrique.toString());
			requete = this.dao.ctlCondition(reg, mapRubrique);
		} else {
			StaticLoggerDispatcher.info(LOGGER, "Exécution de CONDITION non appliquée car il manque des rubriques");
		}
		return requete;
	}

	/**
	 * Préparation à l'exécution d'une règle de type CONDITION Pour chaque rubrique
	 * de la condition, il faut lui associé sa règle de typage
	 *
	 * @param reg
	 * @param jdr
	 * @param reg
	 * @param table
	 * @throws ArcException
	 */
	private String executeRegleCardinalite(RegleControle reg) {
		StaticLoggerDispatcher.info(LOGGER, "Je lance executeRegleCardinalite()");
		String requete = "";

		List<String> listRubrique = ManipString.extractRubriques(reg.getCondition());
		listRubrique.replaceAll(String::toUpperCase);

		requete = this.dao.ctlCardinalite(reg, listRubrique, listRubTable);
		return requete;
	}

	/**
	 * Trouver le typage d'une rubrique dans un jeu de règle donné par défaut, le
	 * type sera considéré comme ALPHANUMERIQUE
	 *
	 * @param jdr , le jeu de règle contenant la rubrique
	 * @param rub
	 * @return
	 */
	private RegleControle findType(FileIdCard fileIdCard, String rub) {
		RegleControle reg = new RegleControle();
		boolean isFind = false;
		StaticLoggerDispatcher.debug(LOGGER, "La rubrique dont on cherche le type : " + rub);
		String rubriquePere = "";
		ControleTypeCode idClasse;
		for (RegleControle regC : fileIdCard.getIdCardControle().getReglesControle()) {
			rubriquePere = regC.getRubriquePere();
			idClasse = regC.getTypeControle();
			StaticLoggerDispatcher.debug(LOGGER,
					"La rubrique de la regle testée : " + rubriquePere + " et le type : " + idClasse);
			if (rub.equals(rubriquePere)
					&& (idClasse.equals(ControleTypeCode.NUM) || idClasse.equals(ControleTypeCode.DATE) || idClasse.equals(ControleTypeCode.ALPHANUM))) {
				StaticLoggerDispatcher.debug(LOGGER, "J'ai trouvé une règle de typage");
				reg.setIdRegle(regC.getIdRegle());
				reg.setTypeControle(regC.getTypeControle());
				reg.setRubriquePere(rub);
				reg.setCondition(regC.getCondition());
				isFind = true;
				break;// je ne m'interesse qu'au premier
			}
		}
		if (!isFind) {// par défaut le type sera considéré comme numérique
			reg.setTypeControle(ControleTypeCode.ALPHANUM);
			reg.setRubriquePere(rub);
		}
		return reg;
	}

	// Getters et Setters
	public ControleRegleDao getServSql() {
		return this.dao;
	}

	public void setServSql(ControleRegleDao servSql) {
		this.dao = servSql;
	}

	public List<String> getListRubTable() {
		return listRubTable;
	}

	public void setListRubTable(List<String> listeAttribut) {
		this.listRubTable = listeAttribut;
	}

}