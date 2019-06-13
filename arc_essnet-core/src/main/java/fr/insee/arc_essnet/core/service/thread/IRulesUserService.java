package fr.insee.arc_essnet.core.service.thread;

public interface IRulesUserService {

    /**
     * Requete permettant de récupérer les règles pour un id_source donnée et une table de regle
     * @param idSource : identifiant du fichier
     * @param tableRegle : table de regle
     * @param tablePilotage : table de pilotage
     * @return
     */
    public default String getRegles(String idSource, String tableRegle, String tablePilotage) {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n SELECT * FROM "+tableRegle+" a WHERE ");
    	requete.append(conditionRegle(idSource, tablePilotage));
    	return requete.toString();
    }
    
    /**
     * Récupère toutes les rubriques utilisées dans les regles relatives au fichier
     * @param idSource
     * @param tablePilotage
     * @param tableNormageRegle
     * @param tableControleRegle
     * @param tableFiltrageRegle
     * @param tableMappingRegle
     * @return
     */
    public default String getAllRubriquesInRegles(String idSource, String tablePilotage, String tableNormageRegle, String tableControleRegle, String tableFiltrageRegle, String tableMappingRegle) {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n SELECT * FROM ( ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(expr_regle_col),'{([iv]_{1,1}[^{}]+)}','g')) as var from "+tableMappingRegle+" a WHERE ");
    	requete.append(conditionRegle(idSource, tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(expr_regle_filtre),'{([iv]_{1,1}[^{}]+)}','g')) as var from "+tableFiltrageRegle+" a WHERE ");
    	requete.append(conditionRegle(idSource, tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_pere) as var from "+tableControleRegle+" a WHERE ");
    	requete.append(conditionRegle(idSource, tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_fils) as var from "+tableControleRegle+" a WHERE ");
    	requete.append(conditionRegle(idSource, tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(condition),'{([iv]_{1,1}[^{}]+)}','g')) as var from "+tableControleRegle+" a WHERE ");
    	requete.append(conditionRegle(idSource, tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, unnest(regexp_matches(lower(pre_action),'{([iv]_{1,1}[^{}]+)}','g')) as var from "+tableControleRegle+" a WHERE ");
    	requete.append(conditionRegle(idSource, tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique) as var from "+tableNormageRegle+" a where id_classe!='suppression' AND ");
    	requete.append(conditionRegle(idSource, tablePilotage));
    	requete.append("\n UNION ");
    	requete.append("\n SELECT id_norme, validite_inf, validite_sup, periodicite, lower(rubrique_nmcl) as var from "+tableNormageRegle+" a where id_classe!='suppression' AND ");
    	requete.append(conditionRegle(idSource, tablePilotage));
    	requete.append("\n ) ww where var is NOT NULL; ");
    	return requete.toString();
    }
    
    /**
     * Retourne la clause WHERE SQL qui permet de selectionne les bonne regles pour un fichier
     * @param idSource
     * @param tablePilotage
     * @return
     */
    public default String conditionRegle (String idSource, String tablePilotage)
    {
    	StringBuilder requete = new StringBuilder();
    	requete.append("\n ");
    	requete.append("EXISTS ( SELECT FROM "+tablePilotage+" b WHERE b.id_source='"+idSource+"' ");
    	requete.append("AND a.id_norme=b.id_norme ");
    	requete.append("AND a.periodicite=b.periodicite ");
    	requete.append("AND a.validite_inf<=to_date(b.validite,'YYYY-MM-DD') ");
    	requete.append("AND a.validite_sup>=to_date(b.validite,'YYYY-MM-DD') ");
    	requete.append(") ");
    	return requete.toString();
    }
    
}
