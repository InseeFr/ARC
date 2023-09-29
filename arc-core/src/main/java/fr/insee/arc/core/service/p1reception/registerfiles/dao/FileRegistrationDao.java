package fr.insee.arc.core.service.p1reception.registerfiles.dao;

import java.text.SimpleDateFormat;
import java.util.Date;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.utils.utils.FormatSQL;

public class FileRegistrationDao {

	private FileRegistrationDao() {
		throw new IllegalStateException("dao class");
	}

	public static void insertPilotage(StringBuilder requete, String tablePilotage, String originalContainer,
			String newContainer, String virtualContainer, String fileName, TraitementEtat etat, String rapport) {
		Date d = new Date();

		// si ko, etape vaut 2
		String etape = etat.equals(TraitementEtat.KO) ? "2" : "1";

		if (requete.length() == 0) {
			requete.append("INSERT INTO " + tablePilotage + " ");
			requete.append("(o_container, container, v_container, " + ColumnEnum.ID_SOURCE.getColumnName()
					+ ", date_entree,phase_traitement,etat_traitement,date_traitement, rapport, nb_enr, etape) VALUES ");
		} else {
			requete.append("\n,");
		}
		requete.append(" (");
		requete.append(FormatSQL.cast(originalContainer));
		requete.append("," + FormatSQL.cast(newContainer));
		requete.append("," + FormatSQL.cast(virtualContainer));
		requete.append("," + FormatSQL.cast(fileName));
		requete.append("," + FormatSQL.cast(new SimpleDateFormat(ArcDateFormat.DATE_HOUR_FORMAT_CONVERSION.getApplicationFormat()).format(d)));
		requete.append("," + FormatSQL.cast(TraitementPhase.RECEPTION.toString()));
		requete.append("," + FormatSQL.cast("{" + etat + "}"));
		requete.append("," + "to_timestamp(" + FormatSQL.cast(new SimpleDateFormat(ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getApplicationFormat()).format(d))
				+ ",'" + ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getDatastoreFormat() + "')");
		requete.append("," + FormatSQL.cast(rapport));
		requete.append(",1");
		requete.append("," + etape);
		requete.append(") ");
	}

}
