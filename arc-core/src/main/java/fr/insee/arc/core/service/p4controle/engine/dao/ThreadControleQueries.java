package fr.insee.arc.core.service.p4controle.engine.dao;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.service.p4controle.engine.bo.ControleMarkCode;

public class ThreadControleQueries {

	private ThreadControleQueries() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * definition of the extra columns and their default values added by the controle phase in the controle data table
	 * a column controle with default value 0 (no error on record)
	 * a column brokenrules as an array, which will optionnaly contain the rules broken by the record
	 * @return
	 */
	public static String extraColumnsAddedByControle() {
		return "'" + ControleMarkCode.RECORD_WITH_NOERROR.getCode()
				+ "'::text collate \"C\" as controle, null::text[] collate \"C\" as brokenrules";
	}

	/**
	 * Insertion des données d'une table dans une autre avec un critère de sélection
	 *
	 * @param listColTableIn
	 *
	 * @param phase
	 *
	 * @param tableIn              la table des données à insérer
	 * @param tableOut             la table réceptacle
	 * @param tableControlePilTemp la table de pilotage des fichiers
	 * @param etatNull             pour sélectionner certains fichiers
	 * @param condEnregistrement   la condition pour filtrer la recopie
	 * @return
	 */
	private static String queryAjoutTableControle(String tableIn, String tableOut, String tableControlePilTemp,
			String condFichier, String condEnregistrement) {

		StringBuilder requete = new StringBuilder();
		requete.append("\n INSERT INTO " + tableOut + " ");
		requete.append("\n SELECT * ");
		requete.append("\n FROM " + tableIn + " a ");
		requete.append("\n WHERE " + condEnregistrement + " ");
		requete.append("\n EXISTS (select 1 from  " + tableControlePilTemp + " b where " + condFichier + ") ");
		requete.append(";");
		return requete.toString();
	}

	/**
	 * insert in OK the record for which 1- the etat_traitement of the file is in OK
	 * or OK,KO 2- AND records which have no error or errors that can be kept
	 * 
	 * @param tableControleDataTemp
	 * @param tableOutOkTemp
	 * @param tableControlePilTemp
	 * @return
	 */
	public static String querySelectRecordsOK(String tableControleDataTemp, String tableOutOkTemp,
			String tableControlePilTemp) {
		return queryAjoutTableControle(tableControleDataTemp, tableOutOkTemp, tableControlePilTemp,
				"etat_traitement in ('{" + TraitementEtat.OK + "}','{" + TraitementEtat.OK + "," + TraitementEtat.KO
						+ "}') ",
				"controle in ('" + ControleMarkCode.RECORD_WITH_NOERROR.getCode() + "','"
						+ ControleMarkCode.RECORD_WITH_ERROR_TO_KEEP.getCode() + "') AND ");
	}

	/**
	 * insert in OK the record for which 1- the etat_traitement of the file is in OK
	 * or OK,KO 2- AND records which have no error or errors that can be kept
	 * 
	 * @param tableControleDataTemp
	 * @param tableOutOkTemp
	 * @param tableControlePilTemp
	 * @return
	 */
	public static String querySelectRecordsKO(String tableControleDataTemp, String tableOutKoTemp,
			String tableControlePilTemp) {
		return queryAjoutTableControle(tableControleDataTemp, tableOutKoTemp, tableControlePilTemp,
				"etat_traitement ='{" + TraitementEtat.KO + "}' ",
				"controle='" + ControleMarkCode.RECORD_WITH_ERROR_TO_EXCLUDE.getCode() + "' OR ");
	}

}
