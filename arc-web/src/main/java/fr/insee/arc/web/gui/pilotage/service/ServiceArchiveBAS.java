package fr.insee.arc.web.gui.pilotage.service;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.format.Format;
import fr.insee.arc.utils.structure.GenericBean;

@Service
public class ServiceArchiveBAS extends InteractorPilotage {

	private static final Logger LOGGER = LogManager.getLogger(ServiceArchiveBAS.class);

	/**
	 * Téléchargement d'enveloppe contenu dans le dossier d'archive
	 *
	 * @return
	 */
	public void downloadEnveloppeFromArchiveBAS(HttpServletResponse response) {

		loggerDispatcher.trace("*** Téléchargement des enveloppes à partir de l'archive ***", LOGGER);
		// récupération de la liste des noms d'enveloppe
		Map<String, ArrayList<String>> selection = views.getViewArchiveBAS().mapContentSelected();

		initializeArchiveBAS();

		ArcPreparedStatementBuilder querySelection = new ArcPreparedStatementBuilder();

		querySelection.append("select distinct alias_de_table.nom_archive as nom_fichier from (");
		querySelection.append(this.views.getViewArchiveBAS().getMainQuery());
		querySelection.append(") alias_de_table ");
		querySelection.append(this.vObjectService.buildFilter(this.views.getViewArchiveBAS().getFilterFields(),
				this.views.getViewArchiveBAS().getHeadersDLabel()));

		if (!selection.isEmpty()) {
			querySelection.append(" AND nom_archive IN " + Format.sqlListe(selection.get("nom_archive")) + " ");
		}

		ArrayList<String> listRepertoire = new ArrayList<>();
		GenericBean g;
		String entrepot = "";
		try {

			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			requete.append("SELECT DISTINCT entrepot FROM (");
			requete.append(this.views.getViewArchiveBAS().getMainQuery());
			requete.append(") alias_de_table ");

			g = new GenericBean(UtilitaireDao.get("arc").executeRequest(null, requete));
			entrepot = g.mapContent().get("entrepot").get(0);
		} catch (ArcException e) {
			loggerDispatcher.error("Error in PilotageBasAction.downloadEnveloppeFromArchiveBAS()", LOGGER);
		}
		listRepertoire.add(TraitementPhase.RECEPTION + "_" + entrepot + "_ARCHIVE");
		String chemin = Paths.get(this.repertoire, getBacASable().toUpperCase()).toString();

		this.vObjectService.downloadEnveloppe(views.getViewArchiveBAS(), response, querySelection, chemin,
				listRepertoire);
	}

}