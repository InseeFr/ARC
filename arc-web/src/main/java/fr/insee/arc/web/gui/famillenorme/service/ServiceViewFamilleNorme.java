package fr.insee.arc.web.gui.famillenorme.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.famille.ModelTable;
import fr.insee.arc.core.model.famille.ModelVariable;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.gui.famillenorme.dao.DDIInsertDAO;
import fr.insee.arc.web.gui.famillenorme.ddi.DDIModeler;
import fr.insee.arc.web.gui.famillenorme.ddi.DDIParser;

@Service
public class ServiceViewFamilleNorme extends InteractorFamilleNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewFamilleNorme.class);

	public String selectFamilleNorme(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String addFamilleNorme(Model model) {
		// Clean up spaces
		String nomFamilleNorme = views.getViewFamilleNorme().getInputFieldFor(ID_FAMILLE);
		views.getViewFamilleNorme().setInputFieldFor(ID_FAMILLE, nomFamilleNorme.trim());
		return addLineVobject(model, RESULT_SUCCESS, views.getViewFamilleNorme());
	}

	public String deleteFamilleNorme(Model model) {
		try {
			ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
			query.append(this.vObjectService.deleteQuery(views.getViewFamilleNorme()));
			query.append(synchronizeRegleWithVariableMetier(
					views.getViewFamilleNorme().mapContentSelected().get(ID_FAMILLE).get(0)));
			query.asTransaction();

			UtilitaireDao.get(0).executeRequest(null, query);
		} catch (ArcException e) {
			this.views.getViewFamilleNorme().setMessage("La suppression des tables a échoué");
		}

		return deleteLineVobject(model, RESULT_SUCCESS, views.getViewFamilleNorme());
	}

	public String updateFamilleNorme(Model model) {
		return updateVobject(model, RESULT_SUCCESS, views.getViewFamilleNorme());
	}

	public String sortFamilleNorme(Model model) {
		return sortVobject(model, RESULT_SUCCESS, views.getViewFamilleNorme());
	}

	public String downloadFamilleNorme(Model model, HttpServletResponse response) {

		Map<String, ArrayList<String>> selection = views.getViewFamilleNorme().mapContentSelected();

		if (!selection.isEmpty()) {

			String selectedFamille = selection.get(ID_FAMILLE).get(0);

			ArcPreparedStatementBuilder requeteTableMetier = new ArcPreparedStatementBuilder();
			requeteTableMetier.append("SELECT a.* ");
			requeteTableMetier.append("FROM arc.ihm_mod_table_metier a ");
			requeteTableMetier.append("WHERE " + ID_FAMILLE + "=");
			requeteTableMetier.appendQuoteText(selectedFamille);

			ArcPreparedStatementBuilder requeteVariableMetier = new ArcPreparedStatementBuilder();
			requeteVariableMetier.append("SELECT a.* ");
			requeteVariableMetier.append("FROM arc.ihm_mod_variable_metier a ");
			requeteVariableMetier.append("WHERE " + ID_FAMILLE + "=");
			requeteVariableMetier.appendQuoteText(selectedFamille);

			ArrayList<ArcPreparedStatementBuilder> queries = new ArrayList<>();
			queries.add(requeteTableMetier);
			queries.add(requeteVariableMetier);

			ArrayList<String> fileNames = new ArrayList<>();
			fileNames.add("modelTables");
			fileNames.add("modelVariables");

			this.vObjectService.download(views.getViewFamilleNorme(), response, fileNames, queries);
			return "none";
		} else {
			this.views.getViewFamilleNorme().setMessage("You didn't select anything");
			return generateDisplay(model, RESULT_SUCCESS);
		}

	}

	/**
	 * Import a zip file of two CSV into the norm family. It allows to import back a
	 * downloaded norm family so it should contain two files named modelTables.csv
	 * and modelVariables.csv
	 * 
	 * @param model
	 * @param fileUpload an external zip file that should contain the
	 *                   modelTables.csv and modelVariables.csv files
	 */
	public String uploadFamilleNorme(Model model, MultipartFile fileUpload) {
		loggerDispatcher.info("uploadFamilleNorme", LOGGER);

		// Ouverture du fichier
		if (fileUpload == null || fileUpload.isEmpty()) {
			this.views.getViewFamilleNorme().setMessage("You must select a file for import.");
			return generateDisplay(model, RESULT_SUCCESS);
		}

		try {
			new DDIInsertDAO(this.dataObjectService).insertDDI(uploadFamilleNormeDansBase(fileUpload.getInputStream()));
		} catch (IOException | ArcException e) {
			this.views.getViewFamilleNorme().setMessage("Import failed");
			loggerDispatcher.error("Error in ServiceViewFamilleNorme.uploadFamilleNorme", LOGGER);
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Adds the norm family imported via {@code uploadFamilleNorme} into the
	 * database.
	 * 
	 * @param fileUploadInputStream the input stream of the zip file to upload
	 * @throws IOException
	 */
	protected DDIModeler uploadFamilleNormeDansBase(InputStream fileUploadInputStream) throws IOException {

		DDIModeler modeler = new DDIModeler();

		try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fileUploadInputStream));
				BufferedReader buffReader = new BufferedReader(new InputStreamReader(zis));
				CSVReader readerTables = new CSVReader(buffReader, ';');
				CSVReader readerVariables = new CSVReader(buffReader, ';');) {

			ZipEntry ze;
			while ((ze = zis.getNextEntry()) != null) {

				if (ze.getName().equals("modelTables.csv")) {

					readerTables.readNext(); // skip en-tête
					readerTables.readNext();

					String[] recordOfTables;
					while ((recordOfTables = readerTables.readNext()) != null) {
						String idFamille = recordOfTables[0];
						String nomTableMetier = keepTableName(recordOfTables[1], idFamille);
						modeler.getModelTables()
								.add(new ModelTable(idFamille, nomTableMetier, recordOfTables[2]));
					}
				} else if (ze.getName().equals("modelVariables.csv")) {

					readerVariables.readNext(); // skip en-tête
					readerVariables.readNext();

					String[] recordOfVariables;
					while ((recordOfVariables = readerVariables.readNext()) != null) {
						String idFamille = recordOfVariables[0];
						String nomTableMetier = keepTableName(recordOfVariables[1], idFamille);
						modeler.getModelVariables().add(new ModelVariable(idFamille, nomTableMetier,
								recordOfVariables[2], recordOfVariables[3], recordOfVariables[4]));
					}
				}
			}
			return modeler;
		}

	}
	
	/**
	 * Deletes if present the prefix and suffix used for table names in ARC mapping table
	 * @param nomTableMetier the table name to check for prefix and suffix
	 * @param idFamille the norm family name to check in the table name prefix
	 * @return the table name, with prefix and suffix removed if present
	 */
	protected String keepTableName(String nomTableMetier, String idFamille) {
		
		
		String prefix = TraitementPhase.MAPPING.toString().toLowerCase() + "_" + idFamille.toLowerCase() + "_";
		String suffix = "_" + TraitementEtat.OK.toString().toLowerCase();
		
		return ManipString.substringBeforeLast(ManipString.substringAfterFirst(nomTableMetier, prefix),suffix);
	}


	/**
	 * Import a xml ddi file into the norm family
	 * 
	 * @param model
	 * @param fileUpload
	 * @return
	 */
	public String importDDI(Model model, MultipartFile fileUpload) {
		loggerDispatcher.debug("importDDI", LOGGER);
		try {

			DDIModeler modeler = DDIParser.parse(fileUpload.getInputStream());

			new DDIInsertDAO(this.dataObjectService).insertDDI(modeler);

		} catch (ArcException | IOException e) {
			this.views.getViewFamilleNorme().setMessage("DDI import failed");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

}
