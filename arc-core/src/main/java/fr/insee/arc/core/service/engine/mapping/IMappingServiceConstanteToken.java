package fr.insee.arc.core.service.engine.mapping;

/**
 * 
 * @author QV47IK
 * 
 */
public interface IMappingServiceConstanteToken {
    /*
     * PARAMETRES
     */
    //
    public static final String nomTokenColonneIdSource = "{:idSource}";
    public static final String nomColonneIdSource = "id_source";
    //
    public static final String nomTokenEtatTraitement = "{:etatTraitement}";
    public static final String nomTokenMessage = "{:message}";
    //
    public static final String nomTokenNombreLigneMaximal = "{:nombreLigneMaximal}";
    //
    public static final String nomTokenPhaseTraitementCourant = "{:phaseTraitementCourant}";
    //
    public static final String nomPrefixeTemp = "temp";
    public static final String nomTokenSchema = "{:schema}";
    //
    public static final String nomTableFiltrageOkTemp = "temp_filtrage_ok";
    //
    public static final String nomTokenTableFichierBonPourMapping = "{:tableNomFichierBonPourMapping}";
    public static final String nomTableNomFichierBonPourFiltrage = "nom_fichier_bon_pour_filtrage";
    public static final String nomTableParsingRegleFiltrage = "parsing_regle_filtrage";
    public static final String nomTableRegleFiltrage = "regle";
    public static final String nomTableRegleFiltrageActive = "regle_filtrage_active";
    public static final String nomTableRubriqueDefiniDansRegleFiltrage = "rubrique_defini_dans_regle";// _filtrage
    public static final String nomTableRubriqueExistant = "rubrique_existant";
    public static final String regexSelectionRubrique = "\\{[^\\{:\\}]*\\}";
    public static final String nomTableTouteRubriqueFiltrage = "toute_rubrique_filtrage";
    public static final String nomTokenValeurIdSource = "{:valeurIdSource}";
    //
    public static final String nomTokenValeurNormeAsText = "{:valeurNormeText}";
    //
    public static final String nomTokenValeurPeriodiciteAsText = "{:valeurPeriodiciteText}";
    //
    public static final String nomTokenValeurUpdate = "{:valeurUpdate}";
    //
    public static final String nomTokenValeurWhere = "{:valeurWhere}";
    //
}
