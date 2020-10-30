package fr.insee.arc.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import fr.insee.arc.web.model.FamilyManagementModel;
import fr.insee.arc.web.model.viewobjects.ViewVariableMetier;
import fr.insee.arc.web.util.ArcStringUtils;
import fr.insee.arc.web.util.ConstantVObject.ColumnRendering;
import fr.insee.arc.web.util.LineObject;
import fr.insee.arc.web.util.VObject;

@Controller
public class GererFamilleNormeAction extends ArcAction<FamilyManagementModel> {

	private static final String RESULT_SUCCESS = "jsp/gererFamilleNorme.jsp";

    private static final String IHM_MOD_VARIABLE_METIER = "ihm_mod_variable_metier";

    private static final Logger LOGGER = LogManager.getLogger(GererFamilleNormeAction.class);
    private static final int numberOfColumnTableVariableMetier = 5;
    
    private VObject viewFamilleNorme;

    private VObject viewClient;

    private VObject viewTableMetier;

    private VObject  viewVariableMetier;
    
    @Override
    public String getActionName() {
    	return "familyManagement";
    }

    @Override
    public void putAllVObjects(FamilyManagementModel arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);
		
		setViewClient(vObjectService.preInitialize(arcModel.getViewClient()));
		setViewFamilleNorme(vObjectService.preInitialize(arcModel.getViewFamilleNorme()));
		setViewTableMetier(vObjectService.preInitialize(arcModel.getViewTableMetier()));
		setViewVariableMetier(vObjectService.preInitialize(arcModel.getViewVariableMetier()));
		
		putVObject(getViewFamilleNorme(), t -> initializeFamilleNorme());
		putVObject(getViewClient(), t -> initializeClient());
		putVObject(getViewTableMetier(), t -> initializeTableMetier());
		putVObject(getViewVariableMetier(), t -> initializeVariableMetier());

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);	
    }

    /*
     * FAMILLES DE NORMES
     */
    private void initializeFamilleNorme() {
        System.out.println("/* initializeFamilleNorme */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        this.vObjectService.initialize(viewFamilleNorme, "select id_famille from arc.ihm_famille order by id_famille", "arc.ihm_famille", defaultInputFields);
    }

    @RequestMapping("/selectFamilleNorme")
    public String selectFamilleNorme() {
        return basicAction(RESULT_SUCCESS);
    }

    @RequestMapping("/addFamilleNorme")
    public String addFamilleNorme() {
    	//Clean up spaces
    	String nomFamilleNorme = viewFamilleNorme.getInputFieldFor("id_famille");
    	viewFamilleNorme.setInputFieldFor("id_famille", nomFamilleNorme.trim());
        return addLineVobject(RESULT_SUCCESS, getViewFamilleNorme());
    }

    @RequestMapping("/deleteFamilleNorme")
    public String deleteFamilleNorme() {
        return deleteLineVobject(RESULT_SUCCESS, getViewFamilleNorme());
    }

    @RequestMapping("/updateFamilleNorme")
    public String updateFamilleNorme() {
        return updateVobject(RESULT_SUCCESS, getViewFamilleNorme());
    }

    @RequestMapping("/sortFamilleNorme")
    public String sortFamilleNorme() {
        return sortVobject(RESULT_SUCCESS, getViewFamilleNorme());
    }

    /*
     * CLIENT
     */
    public void initializeClient() {
	LoggerHelper.info(LOGGER, "/* initializeClient */");
        try {
			Map<String, ArrayList<String>> selection = viewFamilleNorme.mapContentSelected();
            if (!selection.isEmpty()) {

                StringBuilder requete = new StringBuilder("SELECT id_famille, id_application FROM arc.ihm_client WHERE id_famille='"
                        + selection.get("id_famille").get(0) + "'");

                HashMap<String, String> defaultInputFields = new HashMap<>();
                defaultInputFields.put("id_famille", selection.get("id_famille").get(0));

                this.vObjectService.initialize(viewClient, requete.toString(), "arc.ihm_client", defaultInputFields);
            } else {
                this.vObjectService.destroy(viewClient);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @RequestMapping("/selectClient")
    public String selectClient() {
        System.out.println("selectClient " + getScope());
        return basicAction(RESULT_SUCCESS);
    }

    @RequestMapping("/addClient")
    public String addClient() {
        return addLineVobject(RESULT_SUCCESS, getViewClient());
    }

    /**
     * Suppression de Client Regle de gestion : impossible de supprimer une Client active
     *
     * @return
     */
    @RequestMapping("/deleteClient")
    public String deleteClient() {
        return deleteLineVobject(RESULT_SUCCESS, getViewClient());
    }

    @RequestMapping("/updateClient")
    public String updateClient() {
        return updateVobject(RESULT_SUCCESS, getViewClient());
    }

    @RequestMapping("/sortClient")
    public String sortClient() {
        return sortVobject(RESULT_SUCCESS, getViewClient());
    }

    /*
     * TABLES METIER
     */
    public void initializeTableMetier() {
        try {
            System.out.println("/* initializeTableMetier */");
            Map<String, ArrayList<String>> selection = viewFamilleNorme.mapContentSelected();
            if (!selection.isEmpty()) {
				HashMap<String, String> type = viewFamilleNorme.mapHeadersType();
                StringBuilder requete = new StringBuilder();
                requete.append("select * from arc.ihm_mod_table_metier");
                requete.append(" where id_famille" + ManipString.sqlEqual(selection.get("id_famille").get(0), type.get("id_famille")));
                HashMap<String, String> defaultInputFields = new HashMap<String, String>();
                defaultInputFields.put("id_famille", selection.get("id_famille").get(0));

                this.vObjectService.initialize(viewTableMetier, requete.toString(), "arc.ihm_mod_table_metier", defaultInputFields);
            } else {
                this.vObjectService.destroy(viewTableMetier);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @RequestMapping("/selectTableMetier")
    public String selectTableMetier() {
        return basicAction(RESULT_SUCCESS);
    }

    @RequestMapping("/deleteTableMetier")
    public String deleteTableMetier() {
	
        StringBuilder message = new StringBuilder();
        if (deleteTableMetierWithoutSync(message)) {
            this.vObjectService.delete(viewTableMetier);
        }
        this.viewTableMetier.setMessage(message.toString());
        return generateDisplay(RESULT_SUCCESS);
    }

    @RequestMapping("/sortTableMetier")
    public String sortTableMetier() {
        return sortVobject(RESULT_SUCCESS, getViewTableMetier());
    }

    /**
     *
     * @param idFamille
     * @return la liste des tables métier associées à {@code idFamille}
     */
    private static List<String> getListeTableMetierFamille(String idFamille) {
        StringBuilder requete = new StringBuilder("SELECT nom_table_metier\n").append("  FROM arc.ihm_mod_table_metier\n").append(
                "  WHERE id_famille='" + idFamille + "'");
        return UtilitaireDao.get("arc").getList(null, requete, new ArrayList<String>());
    }

    @RequestMapping("/updateTableMetier")
    public String updateTableMetier() {
		if (isNomTableMetierValide(viewTableMetier.mapInputFields().get("nom_table_metier").get(0))) {
            StringBuilder message = new StringBuilder();
            this.deleteTableMetierWithoutSync(message);
            this.vObjectService.insert(viewTableMetier);
        } else {
            setMessageNomTableMetierInvalide();
        }
        return generateDisplay(RESULT_SUCCESS);
    }

    private boolean deleteTableMetierWithoutSync(StringBuilder message) {
        System.out.println("Destruction de la table");
        boolean drop = true;
        
        Map<String,ArrayList<String>> content=viewTableMetier.mapContentSelected();
        
        for (int i = 0; (i < content.get("nom_table_metier").size()) && drop; i++) {

        	// la condition pour dropper est plutot la suivante
        	// on ne doit plus avoir de variable dans la table arc.ihm_mod_variable_metier pour la famille et la table a dropper
        	// le drop de la table en elle meme est faite à l'initialisation !!!!!!!!!!!!!
        	
        	drop = (UtilitaireDao.get("arc").getInt(null, "SELECT count(1) from "+this.viewVariableMetier.getTable()
        					+" where id_famille='"+content.get("id_famille").get(i)+"' "
        					+ "and nom_table_metier='"+content.get("nom_table_metier").get(i)+"' ")
        					==0);
        	
        }
        if (drop) {
			UtilitaireDao.get("arc").dropTable(null, viewTableMetier.mapContentSelected().get("nom_table_metier").toArray(new String[0]));
            message.append("Les tables sont supprimées avec succès.");
            return true;
        } else {
            message.append("La table ne doit plus avoir de colonne pour pouvoir etre supprimée");
            if (viewTableMetier.mapContentSelected().get("nom_table_metier").size() > 1) {
                message.append("\nRecommencez en supprimant une table à la fois.");
            }
        }
        return false;
    }


    public void initializeVariableMetier() {
	if (CollectionUtils.isNotEmpty(viewFamilleNorme.mapContentSelected().get("id_famille"))) {
		List<String> listeTableFamille = getListeTableMetierFamille(viewFamilleNorme.mapContentSelected().get("id_famille").get(0));
	    HashMap<String, ColumnRendering> rendering = ViewVariableMetier.getInitialRenderingViewVariableMetier(new HashMap<String, ColumnRendering>());
	    rendering.putAll(ViewVariableMetier.getInitialRendering(listeTableFamille));
	    this.vObjectService.initialiserColumnRendering(viewVariableMetier, rendering);
	    try {
		System.out.println("/* initializeVariableMetier */");
		StringBuilder requete = getRequeteListeVariableMetierTableMetier(listeTableFamille,
			viewFamilleNorme.mapContentSelected().get("id_famille").get(0));
		HashMap<String, String> defaultInputFields = new HashMap<String, String>();
		defaultInputFields.put("id_famille", viewFamilleNorme.mapContentSelected().get("id_famille").get(0));
		// this.viewVariableMetier.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewVariableMetier.getSessionName()));
		this.vObjectService.initialize(viewVariableMetier, requete.toString(), "arc."+IHM_MOD_VARIABLE_METIER, defaultInputFields);
		
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	    
	} else {
	    this.vObjectService.destroy(viewVariableMetier);
	}
	
    }


    /**
     *
     * @param listeVariableMetier
     * @param idFamille
     * @return La requête permettant d'obtenir le croisement variable*table pour les variables de la famille
     */
    public static StringBuilder getRequeteListeVariableMetierTableMetier(List<String> listeTableMetier, String idFamille) {
        StringBuilder left = new StringBuilder("(SELECT nom_variable_metier");
        for (int i = 0; i < listeTableMetier.size(); i++) {
            left.append(",\n  CASE WHEN '['||string_agg(nom_table_metier,'][' ORDER BY nom_table_metier)||']' LIKE '%['||'" + listeTableMetier.get(i)
                    + "'||']%' then 'x' else '' end " + listeTableMetier.get(i));
        }
        left.append("\nFROM arc."+IHM_MOD_VARIABLE_METIER+" WHERE id_famille='" + idFamille + "'\n");
        left.append("GROUP BY nom_variable_metier) left_side");
        StringBuilder right = new StringBuilder(
                "(SELECT id_famille, nom_variable_metier, type_variable_metier, type_consolidation, description_variable_metier\n");
        right.append("FROM arc."+IHM_MOD_VARIABLE_METIER+"\n");
        right.append("WHERE id_famille='" + idFamille + "'\n");
        right.append("GROUP BY id_famille, nom_variable_metier, type_variable_metier, type_consolidation, description_variable_metier) right_side");
        StringBuilder returned = new StringBuilder(
                "SELECT right_side.id_famille, right_side.nom_variable_metier, right_side.type_variable_metier, right_side.type_consolidation, right_side.description_variable_metier");
        for (int i = 0; i < listeTableMetier.size(); i++) {
            returned.append(", " + listeTableMetier.get(i));
        }
        returned.append("\nFROM \n" + left.toString() + "\nINNER JOIN \n" + right.toString());
        returned.append("\nON left_side.nom_variable_metier = right_side.nom_variable_metier");
        return returned;
    }

    @RequestMapping("/selectVariableMetier")
    public String selectVariableMetier() {
        return basicAction(RESULT_SUCCESS);
    }

    @RequestMapping("/addVariableMetier")
    public String addVariableMetier() {
	
        StringBuilder message = new StringBuilder();
        StringBuilder bloc = new StringBuilder();
        bloc.append(addNonExistingVariableMetierWithoutSync(message));
        bloc.append(synchronizeRegleWithVariableMetier(message, viewFamilleNorme.mapContentSelected().get("id_famille").get(0)));
        executeRequeteMiseAjourTableMetier(message, bloc);
        this.viewVariableMetier.setMessage(message.toString());
        return generateDisplay(RESULT_SUCCESS);
    }

    @RequestMapping("/sortVariableMetier")
    public String sortVariableMetier() {
        return sortVobject(RESULT_SUCCESS, getViewVariableMetier());
    }

    @RequestMapping("/deleteVariableMetier")
	public String deleteVariableMetier() {
	
	    StringBuilder message = new StringBuilder();
	    StringBuilder bloc = new StringBuilder();
	    bloc.append(deleteVariableMetierWithoutSync(message, viewVariableMetier.mapContentSelected(),
	            viewVariableMetier.listContentSelected(), false));
	    bloc.append(synchronizeRegleWithVariableMetier(message, viewFamilleNorme.mapContentSelected().get("id_famille").get(0)));
	    executeRequeteMiseAjourTableMetier(message, bloc);
	    this.viewVariableMetier.setMessage(message.toString());
	    return generateDisplay(RESULT_SUCCESS);
	}

    @RequestMapping("/updateVariableMetier")
    public String updateVariableMetier() {
	

	try {

	    StringBuilder message = new StringBuilder();
	    StringBuilder requete = new StringBuilder();

	    HashMap<String, ArrayList<String>> mBefore = viewVariableMetier.mapContentBeforeUpdate();
	    List<ArrayList<String>> lBefore = viewVariableMetier.listContentBeforeUpdate();
	    
	    for (LineObject line : this.viewVariableMetier.getContent().getT()) {
	    	int indexOfVar = this.viewVariableMetier.getHeadersDLabel().indexOf("nom_variable_metier");
	    	line.getD().set(indexOfVar, ArcStringUtils.cleanUpVariable(line.getD().get(indexOfVar)));
	    }

	    HashMap<String, ArrayList<String>> mAfter = viewVariableMetier.mapContentAfterUpdate();
	    List<ArrayList<String>> lAfter = viewVariableMetier.listContentAfterUpdate();

	    // partie 1 : update nom de variable
	    // créer une map des noms avant aprés pour modifier les règles et les tables
	    for (int i = 0; i < mAfter.get("nom_variable_metier").size(); i++) {
		if (!mBefore.get("nom_variable_metier").get(i).equals(mAfter.get("nom_variable_metier").get(i))) {
		    // mise à jour du nom de la variable dans la table métier
		    requete.append("\n");
		    requete.append("update arc.ihm_mod_variable_metier set nom_variable_metier='"
			    + mAfter.get("nom_variable_metier").get(i) + "' ");
		    requete.append("where nom_variable_metier='" + mBefore.get("nom_variable_metier").get(i) + "' ");
		    requete.append("and id_famille='" + mAfter.get("id_famille").get(i) + "'; ");

		    // mise à jour du nom de la variable dans la table de règle
		    requete.append("\n");
		    requete.append("update arc.ihm_mapping_regle a set variable_sortie='"
			    + mAfter.get("nom_variable_metier").get(i) + "' ");
		    requete.append("where variable_sortie='" + mBefore.get("nom_variable_metier").get(i) + "' ");
		    requete.append(
			    "and exists (select from arc.ihm_norme b where a.id_norme=b.id_norme and b.id_famille='"
				    + mAfter.get("id_famille").get(i) + "'); ");

		    // mise à jour du nom de la variable dans les tables des environements
		    StringBuilder requeteListeEnvironnement = new StringBuilder(
			    "SELECT distinct replace(id,'.','_') FROM arc.ext_etat_jeuderegle where isenv");
		    List<String> listeEnvironnement = UtilitaireDao.get("arc").getList(null, requeteListeEnvironnement,
			    new ArrayList<String>());

		    for (String envName : listeEnvironnement) {
			for (int k = numberOfColumnTableVariableMetier; k < mBefore.size(); k++) {
			    String nomVeridique = envName + "."
				    + this.viewVariableMetier.getHeadersDLabel().get(k);

			    /**
			     * Si la variable est définie pour cette table
			     */
			    if (StringUtils.isNotBlank(lBefore.get(i).get(k))) {
				/**
				 * Si la table existe, on tente une suppression de la colonne
				 */
				if (UtilitaireDao.get("arc").isTableExiste(null, nomVeridique)) {
				    /**
				     * Pour cela, la colonne doit exister
				     */
				    if (UtilitaireDao.get("arc").isColonneExiste(null, nomVeridique,
					    mBefore.get("nom_variable_metier").get(i))) {

					requete.append("\n");
					requete.append("ALTER TABLE " + nomVeridique + " RENAME "
						+ mBefore.get("nom_variable_metier").get(i) + " TO "
						+ mAfter.get("nom_variable_metier").get(i) + ";");

				    }
				}
			    }
			}

		    }
		}

	    }

	    requete.append("\n");

	    // partie 2 : update du reste des variables
	    if (isMofificationOk(message, mAfter)) {
		requete.append(deleteVariableMetierWithoutSync(message, mAfter, lAfter, true));
		requete.append(addExistingVariableMetierWithoutSync(message, lAfter));
		requete.append(mettreAJourInformationsVariables(this.viewVariableMetier));
		requete.append(synchronizeRegleWithVariableMetier(message,
			viewFamilleNorme.mapContentSelected().get("id_famille").get(0)));
		executeRequeteMiseAjourTableMetier(message, requete);
	    }

	    this.viewVariableMetier.setMessage(message.toString());

	} catch (Exception e) {
	    e.printStackTrace();
	    this.viewVariableMetier.setMessage(e.getMessage());
	}
	return generateDisplay(RESULT_SUCCESS);
    }

	/**
     * Ajoute une variable métier par INSERT (la variable métier va être ajoutée)
     *
     * @param message
     */
    private String addNonExistingVariableMetierWithoutSync(StringBuilder message) {
        try {
            StringBuilder requete = new StringBuilder();
            boolean blank=true;
            for (int i = numberOfColumnTableVariableMetier; i < this.viewVariableMetier.getInputFields().size(); i++) {
                if (StringUtils.isNotBlank(this.viewVariableMetier.getInputFields().get(i))
                		// && this.viewVariableMetier.getInputFields().get(i).equals("oui")
                		) {
                	
                	// au moins une table est renseignée
                	blank=false;

                	String nomVariableMetier = this.viewVariableMetier.getInputFieldFor("nom_variable_metier");
                    this.viewVariableMetier.setInputFieldFor("nom_variable_metier", ArcStringUtils.cleanUpVariable(nomVariableMetier));
                	
                    if (checkIsValide(this.viewVariableMetier.getInputFields())) {
                        requete.append("INSERT INTO arc."+IHM_MOD_VARIABLE_METIER+" (");
                        StringBuilder values = new StringBuilder();
                        for (int j = 0; j < numberOfColumnTableVariableMetier; j++) {
                            if (j > 0) {
                                requete.append(", ");
                                values.append(", ");
                            }
                            requete.append(this.viewVariableMetier.getHeadersDLabel().get(j));
                            values.append("'" + this.viewVariableMetier.getInputFields().get(j) + "'::"
                                    + this.viewVariableMetier.getHeadersDType().get(j));
                        }
                        requete.append(", nom_table_metier) VALUES (" + values.append(", '" + this.viewVariableMetier.getHeadersDLabel().get(i))
                                + "'::text);\n");
                    } else {
                        message.append("La variable "
                                + this.viewVariableMetier.getInputFields().get(1)
                                + " existe déjà. Pour la modifier, passez par la ligne correspondante du tableau variable*table.\nAucune variable n'a été ajoutée.\n");
                        return empty;
                    }
                }
            }
            
        	
       	 if (blank)
       	 {
       		 message.append("Vous avez oublié de spécifier les tables cibles pour votre variable");
                return empty;
       	 }
            
            message.append("L'ajout de variables s'est achevé sur un succès.\n");
            return requete.toString();
        } catch (Exception ex) {
            LOGGER.error("Erreur ", ex);
            message.append("Erreur lors de l'ajout des variables.\n").append(ex.getLocalizedMessage());
        }
        return empty;
    }

    private static boolean checkIsValide(List<String> inputFields) {
        StringBuilder requete = new StringBuilder("SELECT count(1) FROM arc."+IHM_MOD_VARIABLE_METIER+"\n")//
                .append("WHERE id_famille='" + inputFields.get(0) + "'\n")//
                .append("  AND nom_variable_metier='" + inputFields.get(1) + "';");
        return UtilitaireDao.get("arc").getInt(null, requete) == 0;
    }

    

    /**
     * Ajoute une variable métier à des tables par UPDATE (la variable existe déjà)
     *
     * @param message
     */
    private String addExistingVariableMetierWithoutSync(StringBuilder message, List<ArrayList<String>> listContent) {
        try {
            StringBuilder requete = new StringBuilder();
            /**
             * Pour chaque ligne à UPDATE
             */
            for (int i = 0; i < listContent.size(); i++) {
                /**
                 * Et pour l'ensemble des tables métier
                 */
				for (int j = numberOfColumnTableVariableMetier; j < viewVariableMetier.mapContentAfterUpdate(i).size(); j++) {
                    /**
                     * Si une variable est à "oui" pour cette table alors qu'elle n'y était pas...
                     */
					if (StringUtils.isNotBlank(listContent.get(i).get(j))
                            && StringUtils.isBlank(viewVariableMetier.listContentBeforeUpdate().get(i).get(j))) {
                        /**
                         * ... on l'ajoute
                         */
                        requete.append("INSERT INTO arc."+IHM_MOD_VARIABLE_METIER+" (");
                        StringBuilder values = new StringBuilder();
                        for (int k = 0; k < numberOfColumnTableVariableMetier; k++) {
                            if (LOGGER.isTraceEnabled()) {
                                LOGGER.trace(i + " " + j + " " + k);
                            }
                            if (k > 0) {
                                requete.append(", ");
                                values.append(", ");
                            }
                            requete.append(this.viewVariableMetier.getHeadersDLabel().get(k));
                            values.append(//
                            ((listContent.get(i).get(k) == null) ? "null" : ("'" + listContent.get(i).get(k) + "'"))//
                                    + "::" + this.viewVariableMetier.getHeadersDType().get(k));
                        }
                        requete.append(", nom_table_metier) VALUES (" + values.append(", '" + this.viewVariableMetier.getHeadersDLabel().get(j))
                                + "'::text);\n");
                    }
                }
            }
            message.append("L'ajout de variables s'est achevé sur un succès.\n");
            return requete.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            message.append("Erreur lors de l'ajout des variables.\n").append(ex.getLocalizedMessage());
        }
        return empty;
    }

    private static String synchronizeRegleWithVariableMetier(StringBuilder message, String idFamille) {
        /**
         * Sélection des règles à détruire
         */
        StringBuilder requeteListeSupprRegleMapping = new StringBuilder("DELETE FROM arc.ihm_mapping_regle regle\n");
        requeteListeSupprRegleMapping.append("  WHERE NOT EXISTS (");
        requeteListeSupprRegleMapping.append("    SELECT 1 FROM arc."+IHM_MOD_VARIABLE_METIER+" var INNER JOIN arc.ihm_famille fam\n");
        requeteListeSupprRegleMapping.append("    ON var.id_famille=fam.id_famille\n");
        requeteListeSupprRegleMapping.append("    AND regle.variable_sortie=var.nom_variable_metier\n");
        requeteListeSupprRegleMapping.append("    INNER JOIN arc.ihm_norme norme\n");
        requeteListeSupprRegleMapping.append("    ON norme.id_famille=fam.id_famille\n");
        requeteListeSupprRegleMapping.append("    AND regle.id_norme=norme.id_norme\n");
        requeteListeSupprRegleMapping.append("    WHERE fam.id_famille = '" + idFamille + "'");
        requeteListeSupprRegleMapping.append("  )");
        requeteListeSupprRegleMapping.append("    AND EXISTS (SELECT 1 FROM arc.ihm_norme norme INNER JOIN arc.ihm_famille fam");
        requeteListeSupprRegleMapping.append("      ON norme.id_famille=fam.id_famille");
        requeteListeSupprRegleMapping.append("      AND regle.id_norme=norme.id_norme");
        requeteListeSupprRegleMapping.append("      WHERE fam.id_famille = '"+idFamille+"')");
        /**
         * Sélection des règles à créer
         */
        StringBuilder requeteListeAddRegleMapping = new StringBuilder("INSERT INTO arc.ihm_mapping_regle (");
        requeteListeAddRegleMapping.append("id_regle");
        requeteListeAddRegleMapping.append(", id_norme");
        requeteListeAddRegleMapping.append(", validite_inf");
        requeteListeAddRegleMapping.append(", validite_sup");
        requeteListeAddRegleMapping.append(", version");
        requeteListeAddRegleMapping.append(", periodicite");
        requeteListeAddRegleMapping.append(", variable_sortie");
        requeteListeAddRegleMapping.append(", expr_regle_col");
        requeteListeAddRegleMapping.append(", commentaire)");
        requeteListeAddRegleMapping.append("\n  SELECT (SELECT max(id_regle) FROM arc.ihm_mapping_regle) + row_number() over ()");
        requeteListeAddRegleMapping.append(", norme.id_norme");
        requeteListeAddRegleMapping.append(", calendrier.validite_inf");
        requeteListeAddRegleMapping.append(", calendrier.validite_sup");
        requeteListeAddRegleMapping.append(", jdr.version");
        requeteListeAddRegleMapping.append(", norme.periodicite");
        requeteListeAddRegleMapping.append(", var.nom_variable_metier");
        requeteListeAddRegleMapping.append(", '" + FormatSQL.NULL + "'");
        requeteListeAddRegleMapping.append(", " + FormatSQL.NULL + "::text ");
        requeteListeAddRegleMapping.append("\n  FROM (SELECT DISTINCT id_famille, nom_variable_metier FROM arc."+IHM_MOD_VARIABLE_METIER+") var INNER JOIN arc.ihm_famille fam");
        requeteListeAddRegleMapping.append("\n    ON var.id_famille=fam.id_famille");
        requeteListeAddRegleMapping.append("\n  INNER JOIN arc.ihm_norme norme");
        requeteListeAddRegleMapping.append("\n    ON fam.id_famille=norme.id_famille");
        requeteListeAddRegleMapping.append("\n  INNER JOIN arc.ihm_calendrier calendrier");
        requeteListeAddRegleMapping.append("\n    ON calendrier.id_norme=norme.id_norme AND calendrier.periodicite=norme.periodicite");
        requeteListeAddRegleMapping.append("\n  INNER JOIN arc.ihm_jeuderegle jdr");
        requeteListeAddRegleMapping.append("\n    ON calendrier.id_norme=jdr.id_norme AND calendrier.periodicite=jdr.periodicite");
        requeteListeAddRegleMapping.append("\n      AND calendrier.validite_inf=jdr.validite_inf AND calendrier.validite_sup=jdr.validite_sup");
        requeteListeAddRegleMapping.append("\n  WHERE fam.id_famille = '" + idFamille + "'");
//        requeteListeAddRegleMapping.append("\n    AND lower(jdr.etat) NOT LIKE '%.prod'");
        requeteListeAddRegleMapping.append("\n    AND lower(jdr.etat) <> 'inactif'");
        requeteListeAddRegleMapping.append("\n    AND lower(calendrier.etat) = '1'");
        requeteListeAddRegleMapping.append("\n    AND NOT EXISTS (");
        requeteListeAddRegleMapping.append("\n      SELECT 1 FROM arc.ihm_mapping_regle regle");
        requeteListeAddRegleMapping.append("\n      WHERE regle.variable_sortie=var.nom_variable_metier");
        requeteListeAddRegleMapping.append("\n        AND regle.id_norme=norme.id_norme");
        requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
        requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
        requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
        requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
        requeteListeAddRegleMapping.append("\n    ) AND EXISTS (");
        requeteListeAddRegleMapping.append("\n      SELECT 1 FROM arc.ihm_mapping_regle regle");
        requeteListeAddRegleMapping.append("\n      WHERE regle.id_norme=norme.id_norme");
        requeteListeAddRegleMapping.append("\n        AND regle.validite_inf=calendrier.validite_inf");
        requeteListeAddRegleMapping.append("\n        AND regle.validite_sup=calendrier.validite_sup");
        requeteListeAddRegleMapping.append("\n        AND regle.periodicite=norme.periodicite");
        requeteListeAddRegleMapping.append("\n        AND regle.version=jdr.version");
        requeteListeAddRegleMapping.append("\n    )");
        StringBuilder requete = new StringBuilder();
        requete.append(requeteListeAddRegleMapping.toString() + ";\n");
        requete.append(requeteListeSupprRegleMapping.toString() + ";");
        return requete.toString();
    }

    /**
     * Détruit une variable métier dans la table de référence ihm_mod_variable_metier. Ne détruit pas les colonnes correspondantes dans les
     * tables d'environnement concernées.
     *
     * @param message
     * @param listContentBeforeUpdate
     *            Peut être à null
     */
    private String deleteVariableMetierWithoutSync(StringBuilder message, Map<String, ArrayList<String>> map,
            List<ArrayList<String>> arrayList, boolean onlyWhereBlank) {
        try {
            boolean drop = true;
            StringBuilder delete = new StringBuilder();
            /**
             * Pour chaque variable :<br/>
             * 1. Lister les tables<br/>
             * 2. Supprimer cette colonne des tables listées<br/>
             * 3. Supprimer cette variable*table de ihm_mod_variable_metier<br/>
             * 4. Supprimer la règle correspondante de ihm_mapping_regle
             */
            StringBuilder listeTable = new StringBuilder();
            for (int j = 0; (j < map.get("nom_variable_metier").size()) && drop; j++) {
                String nomVariable = map.get("nom_variable_metier").get(j);
                /**
                 * On prépare la liste des tables comportant effectivement la variable
                 */
                listeTable.setLength(0);
                /**
                 * Pour chaque table trouvée
                 */
                for (int i = numberOfColumnTableVariableMetier; (i < map.size()) && drop; i++) {
                    if (StringUtils.isBlank(arrayList.get(j).get(i)) || !onlyWhereBlank) {
                        listeTable.append("[" + this.viewVariableMetier.getHeadersDLabel().get(i) + "]");
                    }
                }
                delete.append("DELETE FROM arc."+IHM_MOD_VARIABLE_METIER+" WHERE id_famille='" + map.get("id_famille").get(j)
                        + "' AND nom_variable_metier='" + nomVariable + "'::text AND '" + listeTable + "' like '%['||nom_table_metier||']%';\n");
            }
            /**
             *
             */
            if (drop) {

                // UtilitaireDao.get("arc").executeRequest(null, delete);
                message.append("Toutes les variables sélectionnées ont été supprimées des tables sélectionnées.\n");
                return delete.toString();

            }

            message.append("Les variables sélectionnées n'ont pas été supprimées car la suppression d'au moins une variable pose problème.\n");
            if (map.get("nom_variable_metier").size() > 1) {
                message.append("Recommencez en supprimant une variable à la fois.\n");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return empty;
    }

    private boolean isMofificationOk(StringBuilder message, HashMap<String, ArrayList<String>> mapContentAfterUpdate) {
        return estCeQueLesNomsDeVariablesSontNonNuls(message, mapContentAfterUpdate)
               // && estCeQueLesIdentifiantsSontExclus(message, mapContentAfterUpdate)
                && estCeQueLeSchemaNeComportePasDeCycles(message, mapContentAfterUpdate);
    }

    private static boolean estCeQueLesNomsDeVariablesSontNonNuls(StringBuilder message, HashMap<String, ArrayList<String>> mapContentAfterUpdate) {
        for (int i = 0; i < mapContentAfterUpdate.get("nom_variable_metier").size(); i++) {
            String nomVariable = mapContentAfterUpdate.get("nom_variable_metier").get(i);
            if (nomVariable == null) {
                message.append("Une variable a un nom null.");
                return false;
            }
        }
        return true;
    }

    private boolean estCeQueLeSchemaNeComportePasDeCycles(StringBuilder message, HashMap<String, ArrayList<String>> mapContentAfterUpdate) {
        // TODO Auto-generated method stub
        return true;
    }

    private String mettreAJourInformationsVariables(VObject someViewVariableMetier) {
        StringBuilder requete = new StringBuilder();
        for (int i = 0; i < someViewVariableMetier.listContentAfterUpdate().size(); i++) {
            if (i > 0) {
                requete.append("\n");
            }
            StringBuilder requeteLocale = new StringBuilder("UPDATE arc."+IHM_MOD_VARIABLE_METIER+"");
            requeteLocale.append("\n  SET type_consolidation = '" + someViewVariableMetier.mapContentAfterUpdate().get("type_consolidation").get(i)
                    + "'");
            requeteLocale.append(",\n    description_variable_metier = '"
                    + someViewVariableMetier.mapContentAfterUpdate().get("description_variable_metier").get(i).replace(quote, quotequote) + "'");
            requeteLocale.append("\n  WHERE id_famille = '" + someViewVariableMetier.mapContentAfterUpdate().get("id_famille").get(i) + "'");
            requeteLocale.append("\n    AND nom_variable_metier = '"
                    + someViewVariableMetier.mapContentAfterUpdate().get("nom_variable_metier").get(i) + "'");
            requete.append(requeteLocale).append(";");
        }
        return requete.toString();
    }

    public static final void executeRequeteMiseAjourTableMetier(StringBuilder message, StringBuilder requete) {
        try {
            UtilitaireDao.get("arc").executeBlock(null, requete);
            message.append("Les règles correspondant aux variables supprimées dans les tables métier ont été supprimées.\n");
            message.append("Pensez également à valoriser les règles pour les variables nouvellement créées.\n");
        } catch (Exception ex) {
            ex.printStackTrace();
            message.append("Impossible de synchroniser les règles avec les familles de normes.");
        }
    }

    public final boolean isNomTableMetierValide(String nomTable) {
		return nomTable.matches("(?i)^"+TraitementPhase.MAPPING.toString().toLowerCase()+"_" + viewFamilleNorme.mapContentSelected().get("id_famille").get(0) + "_([a-z]|_)*[a-z]+_ok$");
    }

    @RequestMapping("/addTableMetier")
    public String addTableMetier() {
		if (isNomTableMetierValide(viewTableMetier.mapInputFields().get("nom_table_metier").get(0))) {
            this.vObjectService.insert(viewTableMetier);
        } else {
            setMessageNomTableMetierInvalide();
        }
        return generateDisplay(RESULT_SUCCESS);
    }

    private void setMessageNomTableMetierInvalide() {
        this.viewTableMetier.setMessage("Un nom de table doit respecter la syntaxe :\n\"mapping_"
                + this.viewFamilleNorme.mapContent().get("id_famille").get(0)
                + "_<identifiant>_ok\"\nOù <identifiant> est un ensemble de mots séparés par des underscores (\"_\")");
    }

    /**
     * @return the viewTableMetier
     */
    public final VObject getViewTableMetier() {
        return this.viewTableMetier;
    }

    /**
     * @param vObjectData
     *            the viewTableMetier to set
     */
    public final void setViewTableMetier(VObject vObjectData) {
        this.viewTableMetier = vObjectData;
    }

    /**
     * @return the viewVariableMetier
     */
    public final VObject getViewVariableMetier() {
        return this.viewVariableMetier;
    }

    /**
     * @param viewVariableMetier
     *            the viewVariableMetier to set
     */
    public final void setViewVariableMetier(VObject viewVariableMetier) {
        this.viewVariableMetier = viewVariableMetier;
    }

    /**
     * @return the viewFamilleNorme
     */
    public final VObject getViewFamilleNorme() {
        return this.viewFamilleNorme;
    }

    /**
     * @param vObjectData
     *            the viewFamilleNorme to set
     */
    public final void setViewFamilleNorme(VObject vObjectData) {
        this.viewFamilleNorme = vObjectData;
    }

    /**
     * @return the viewClient
     */
    public VObject getViewClient() {
        return this.viewClient;
    }

    /**
     * @param viewClient
     *            the viewClient to set
     */
    public void setViewClient(VObject viewClient) {
        this.viewClient = viewClient;
    }

}
