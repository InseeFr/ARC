package fr.insee.arc.web.gui.pilotage.model;

import java.util.ArrayList;

import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.web.gui.all.util.VObject;

public final class PilotageFileSearch {

	private static final String PHASE = "fileSearchPhase";
	private static final String PATTERN = "fileSearchPattern";
	private static final String ENVIRONMENT = "fileSearchEnvironment";
	private static final String ETAT = "fileSearchEtat";

	private PilotageFileSearch() {
	}

	public static TraitementPhase getPhase(VObject view) {
		String phase = view.getCustomValue(PHASE);
		return TraitementPhase.getListPhaseReportedInBas().stream()
				.filter(candidate -> candidate.toString().equals(phase)).findFirst().orElse(null);
	}

	public static TraitementEtat getEtat(VObject view) {
		String etat = view.getCustomValue(ETAT);
		if (etat == null || etat.isBlank()) {
			return TraitementEtat.OK; // défaut pour rétro-compatibilité
		}
		try {
			return TraitementEtat.valueOf(etat);
		} catch (IllegalArgumentException e) {
			return TraitementEtat.OK;
		}
	}


	public static String getPattern(VObject view) {
		return view.getCustomValue(PATTERN);
	}

	public static boolean isActive(VObject view) {
		String pattern = getPattern(view);
		return getPhase(view) != null && pattern != null && !pattern.isBlank();
	}

	/**
	 * Démarre une recherche par nom de fichier.
	 * @param view le VObject de détail (viewFichierBAS)
	 * @param phase phase choisie (RECEPTION..MAPPING)
	 * @param etat état choisi (OK ou KO)
	 * @param pattern motif de recherche (peut contenir des jokers %/_)
	 * @param environment bac à sable
	 * @return true si la recherche est valide et démarrée, false sinon
	 */
	public static boolean start(VObject view, String phase, String etat, String pattern, String environment) {
		clear(view);
		boolean validPhase = TraitementPhase.getListPhaseReportedInBas().stream()
				.anyMatch(candidate -> candidate.toString().equals(phase));
		boolean validEtat = etat != null && !etat.isBlank() && (TraitementEtat.OK.toString().equals(etat) || TraitementEtat.KO.toString().equals(etat));
		if (!validPhase || !validEtat || pattern == null || pattern.isBlank()) {
			return false;
		}
		view.setCustomValue(PHASE, phase);
		view.setCustomValue(ETAT, etat);
		view.setCustomValue(PATTERN, pattern.trim());
		view.setCustomValue(ENVIRONMENT, environment);
		return true;
	}

	public static boolean clearIfEnvironmentChanged(VObject view, String environment) {
		String previousEnvironment = view.getCustomValue(ENVIRONMENT);
		if (previousEnvironment != null && !previousEnvironment.equals(environment)) {
			clear(view);
			return true;
		}
		return false;
	}

	public static void clear(VObject view) {
		view.setCustomValue(PHASE, null);
		view.setCustomValue(PATTERN, null);
		view.setCustomValue(ENVIRONMENT, null);
		view.setCustomValue(ETAT, null);
		view.setFilterFields(new ArrayList<>());
		view.setSelectedLines(new ArrayList<>());
		view.setSelectedColumns(new ArrayList<>());
		view.setIdPage("1");
		view.setMainQuery(null);
		view.clear();
	}
}
