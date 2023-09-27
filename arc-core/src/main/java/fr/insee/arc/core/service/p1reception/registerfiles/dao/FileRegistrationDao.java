package fr.insee.arc.core.service.p1reception.registerfiles.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.ApiService;
import fr.insee.arc.utils.utils.FormatSQL;

public class FileRegistrationDao {

	private FileRegistrationDao() {
		throw new IllegalStateException("dao class");
	}

	public static void insertPilotage(StringBuilder requete, String tablePilotage, String originalContainer,
			String newContainer, String v_container, String fileName, TraitementEtat etat, String rapport) {
		Date d = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH");
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

		// si ko, etape vaut 2
		String etape = etat.equals(TraitementEtat.KO) ? "2" : "1";

		if (requete.length() == 0) {
			requete.append("INSERT INTO " + tablePilotage + " ");
			requete.append("(o_container, container, v_container, " + ColumnEnum.ID_SOURCE.getColumnName()
					+ ", date_entree,phase_traitement,etat_traitement,date_traitement, rapport, nb_enr, etape) VALUES ");
		} else {
			requete.append("\n,");
		}
		requete.append(" (" + FormatSQL.cast(originalContainer) + "," + FormatSQL.cast(newContainer) + ","
				+ FormatSQL.cast(v_container) + ", " + FormatSQL.cast(fileName) + ","
				+ FormatSQL.cast(dateFormat.format(d)) + "," + FormatSQL.cast(TraitementPhase.RECEPTION.toString())
				+ "," + FormatSQL.cast("{" + etat + "}") + "," + "to_timestamp(" + FormatSQL.cast(formatter.format(d))
				+ ",'" + ApiService.DATABASE_DATE_FORMAT + "')" + "," + FormatSQL.cast(rapport) + ",1," + etape + ") ");
	}

}
