package fr.insee.arc.web.gui.norme.service;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fr.insee.arc.core.famille.comparaison.ComparaisonRegleService;
import fr.insee.arc.core.famille.comparaison.DifferenceRegle;
import fr.insee.arc.core.famille.model.*;
import fr.insee.arc.core.service.global.bo.JeuDeRegle;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.utils.exception.ArcException;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

@Service
public class ServiceViewJeuxDeReglesCopie extends InteractorNorme {

	private static final Logger LOGGER = LogManager.getLogger(ServiceViewJeuxDeReglesCopie.class);

	private final ComparaisonRegleService comparaisonRegleService;

	@Autowired
	public ServiceViewJeuxDeReglesCopie(ComparaisonRegleService comparaisonRegleService) {
		this.comparaisonRegleService = comparaisonRegleService;
	}

	/**
	 * Action trigger by requesting the load rules of the register rule set to copy
	 * in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesChargementCopie(Model model) {
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewChargement().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewChargement().getSessionName());
		views.getViewJeuxDeReglesCopie()
				.getCustomValues()
				.put("MODE", "COPIE");
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the structurize rules of the register rule set
	 * to copy in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesNormageCopie(Model model) {

		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewNormage().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewNormage().getSessionName());
		views.getViewJeuxDeReglesCopie()
				.getCustomValues()
				.put("MODE", "COPIE");
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the control rules of the register rule set to
	 * copy in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesControleCopie(Model model) {

		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewControle().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewControle().getSessionName());
		views.getViewJeuxDeReglesCopie()
				.getCustomValues()
				.put("MODE", "COPIE");
		return generateDisplay(model, RESULT_SUCCESS);
	}

	/**
	 * Action trigger by requesting the map rules of the register rule set to copy
	 * in the actual rule set
	 * 
	 * @return
	 */
	public String selectJeuxDeReglesMappingCopie(Model model) {

		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewMapping().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewMapping().getSessionName());
		views.getViewJeuxDeReglesCopie()
				.getCustomValues()
				.put("MODE", "COPIE");
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String selectJeuxDeReglesExpressionCopie(Model model) {

		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_TABLE, this.views.getViewExpression().getTable());
		this.views.getViewJeuxDeReglesCopie().setCustomValue(SELECTED_RULESET_NAME, this.views.getViewExpression().getSessionName());
		views.getViewJeuxDeReglesCopie()
				.getCustomValues()
				.put("MODE", "COPIE");
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String selectJeuxDeReglesCopie(Model model) {
		return basicAction(model, RESULT_SUCCESS);
	}

	public String copieJeuxDeRegles(Model model) {
		loggerDispatcher.info("Mon action pour copier un jeu de règles", LOGGER);
		
		// le jeu de regle à copier
		Map<String, List<String>> selectionOut = views.getViewJeuxDeRegles().mapContentSelected();
		// le nouveau jeu de regle
		Map<String, List<String>> selectionIn = views.getViewJeuxDeReglesCopie().mapContentSelected();
		
		if (!selectionOut.isEmpty() && !selectionIn.isEmpty()) {

			try {
			dao.execQueryCopieJeuxDeRegles(views.getViewJeuxDeRegles(), views.getViewJeuxDeReglesCopie(), this.getSelectedJeuDeRegle());
			
			this.vObjectService.destroy(views.getViewJeuxDeReglesCopie());
		} catch (ArcException ex) {
			loggerDispatcher.error("Error in copieJeuxDeRegles", ex, LOGGER);
		}
		} else {
			loggerDispatcher.info("No rule set choosed", LOGGER);
			this.views.getViewJeuxDeRegles().setMessage("normManagement.copyRuleset.noSelection");
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String selectJeuxDeReglesComparaison(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}

	public String compareJeuxDeReglesDownload(Model model, HttpServletResponse response) throws ParseException {

		Map<String, List<String>> reference =
				views.getViewJeuxDeRegles().mapContentSelected();

		Map<String, List<String>> compare =
				views.getViewJeuxDeReglesCompare().mapContentSelected();

		if (reference.isEmpty() || compare.isEmpty()) {
			views.getViewJeuxDeReglesCompare()
					.setMessage("general.noSelection");

			return generateDisplay(model, RESULT_SUCCESS);
		}

		JeuDeRegle jdrReference = JeuDeRegle.fromMap(reference);
		JeuDeRegle jdrCompare = JeuDeRegle.fromMap(compare);

		try {
			List<DifferenceRegle<?>> differences =
					comparaisonRegleService.comparerJeuDeRegles(
							null,
							jdrReference,
							jdrCompare);

			downloadDifferences(response, differences);

			return "none";

		} catch (ArcException ex) {
			loggerDispatcher.error(
					"Error in compareJeuxDeRegles",
					ex,
					LOGGER
			);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	private void downloadDifferences (
			HttpServletResponse response,
			List<DifferenceRegle<?>> differences) throws ArcException{

		response.setHeader(
				"Content-Disposition",
				"attachment; filename=differences_jeux_de_regles.zip");

		try (ZipOutputStream zos =
					 new ZipOutputStream(response.getOutputStream())) {

			writeCsvIfNotEmpty(
					zos,
					"Rules_load.csv",
					differences,
					ChargementRegle.class);

			writeCsvIfNotEmpty(
					zos,
					"Rules_structurize.csv",
					differences,
					NormageRegle.class);

			writeCsvIfNotEmpty(
					zos,
					"Rules_control.csv",
					differences,
					ControleRegle.class);

			writeCsvIfNotEmpty(
					zos,
					"Rules_mapping.csv",
					differences,
					MappingRegle.class);

			writeCsvIfNotEmpty(
					zos,
					"Rules_expression.csv",
					differences,
					ExpressionRegle.class);

			writeCsvIfNotEmpty(
					zos,
					"Norme.csv",
					differences,
					Norme.class);

			writeCsvIfNotEmpty(
					zos,
					"Tables_metiers.csv",
					differences,
					TableMetier.class);

			writeCsvIfNotEmpty(
					zos,
					"Variables_metiers.csv",
					differences,
					VariableMetier.class);

			writeCsvIfNotEmpty(
					zos,
					"Clients.csv",
					differences,
					Client.class);
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			throw new ArcException(ArcExceptionMessage.FILE_WRITE_FAILED);
		}
	}

	private <T  extends RegleComparable<?>> void writeCsvIfNotEmpty(
			ZipOutputStream zos,
			String fileName,
			List<DifferenceRegle<?>> differences,
			Class<T> ruleClass) throws IOException {

		List<DifferenceRegle<T>> differencesByRuleType =
				getDifferencesByRuleType(differences, ruleClass);

		if (!differencesByRuleType.isEmpty()) {
			writeCsv(
					zos,
					fileName,
					differencesByRuleType.stream()
							.map(this::toDifferenceCsv)
							.toList()
			);
		}
	}


	private <T extends RegleComparable<?>>
	DifferenceCsv toDifferenceCsv(DifferenceRegle<T> difference) {

		return new DifferenceCsv(
				difference.getType().name(),
				formatRegles(difference.getReglesReference()),
				formatRegles(difference.getReglesComparees())
		);
	}

	private String formatRegles(
			List<? extends RegleComparable<?>> regles) {

		return regles.stream()
				.map(RegleComparable::formatPourExport)
				.collect(Collectors.joining(" | "));
	}


	@SuppressWarnings("unchecked")
	private <T extends RegleComparable<?>> List<DifferenceRegle<T>> getDifferencesByRuleType(
			List<DifferenceRegle<?>> differences,
			Class<T> ruleClass) {

		return differences.stream()
				.filter(difference -> {
					if (!difference.getReglesReference().isEmpty()) {
						return ruleClass.isInstance(
								difference.getReglesReference().get(0));
					}

					return !difference.getReglesComparees().isEmpty()
							&& ruleClass.isInstance(
							difference.getReglesComparees().get(0));
				})
				.map(difference -> (DifferenceRegle<T>) difference)
				.toList();
	}

	private void writeCsv(
			ZipOutputStream zos,
			String fileName,
			List<DifferenceCsv> differences) throws IOException {

		zos.putNextEntry(new ZipEntry(fileName));

		CsvSchema schema = CSV_MAPPER
				.schemaFor(DifferenceCsv.class)
				.withHeader()
				.withColumnSeparator(';');

		CSV_MAPPER
				.writer(schema)
				.writeValue(zos, differences);

		zos.closeEntry();
	}

	public record DifferenceCsv(
			String type,
			String reference,
			String compare) {
	}

	private static final CsvMapper CSV_MAPPER = CsvMapper.builder()
			.disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
			.build();

}
