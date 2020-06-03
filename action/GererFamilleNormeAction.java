package fr.insee.arc_composite.web.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc_composite.core.model.IDbConstant;
import fr.insee.arc_composite.web.model.ViewFamilleNorme;
import fr.insee.arc_composite.web.model.ViewTableMetier;
import fr.insee.arc_composite.web.model.ViewVariableMetier;
import fr.insee.arc_composite.web.util.ArcStringUtils;
import fr.insee.siera.core.dao.UtilitaireDao;
import fr.insee.siera.core.util.FormatSQL;
import fr.insee.siera.core.util.ManipString;
import fr.insee.siera.sqlgen.requests.implementations.GenericRequest;
import fr.insee.siera.sqlgen.requests.implementations.GenericRequestFactory;
import fr.insee.siera.textutils.IConstanteCaractere;
import fr.insee.siera.webutils.ConstantVObject.ColumnRendering;
import fr.insee.siera.webutils.VObject;
import fr.insee.siera.webutils.VObject.LineObject;

@Component
@Results({ @Result(name = "success", location = "/jsp/gererFamilleNorme.jsp"), @Result(name = "index", location = "/jsp/index.jsp") })
public class GererFamilleNormeAction implements SessionAware, IConstanteCaractere, IDbConstant {


    private static final String IHM_MOD_VARIABLE_METIER = "ihm_mod_variable_metier";

    private static final Logger LOGGER = Logger.getLogger(GererFamilleNormeAction.class);
    private static final int numberOfColumnTableVariableMetier = 5;
    @Autowired
    @Qualifier("viewFamilleNorme")
    private VObject viewFamilleNorme;
    @Autowired
    @Qualifier("viewClient")
    private VObject viewClient;
    @Autowired
    @Qualifier("viewTableMetier")
    private VObject viewTableMetier;
    @Autowired
    @Qualifier("viewVariableMetier")
    private VObject viewVariableMetier;
    private String scope;

    @Override
    public void setSession(Map<String, Object> session) {
        this.viewFamilleNorme.setMessage("");
        this.viewTableMetier.setMessage("");
        this.viewVariableMetier.setMessage("");
    }

    public String sessionSyncronize() {
        this.viewFamilleNorme.setActivation(this.scope);
        this.viewClient.setActivation(this.scope);
        this.viewTableMetier.setActivation(this.scope);
        this.viewVariableMetier.setActivation(this.scope);
        Boolean defaultWhenNoScope = true;
        if (this.viewFamilleNorme.getIsScoped()) {
            initializeFamilleNorme();
            defaultWhenNoScope = false;
        }
        if (this.viewClient.getIsScoped()) {
            initializeClient();
            defaultWhenNoScope = false;
        }
        if (this.viewTableMetier.getIsScoped()) {
            initializeTableMetier();
            defaultWhenNoScope = false;
        }
        if (this.viewVariableMetier.getIsScoped()) {
            initializeVariableMetier();
            defaultWhenNoScope = false;
        }
        if (defaultWhenNoScope) {
            initializeFamilleNorme();
            this.viewFamilleNorme.setIsActive(true);
            this.viewFamilleNorme.setIsScoped(true);
        }

        return "success";
    }

    /*
     * FAMILLES DE NORMES
     */
    private void initializeFamilleNorme() {
        System.out.println("/* initializeFamilleNorme */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();
        this.viewFamilleNorme.initialize("select id_famille from arc.ihm_famille order by id_famille", "arc.ihm_famille", defaultInputFields);
    }

    @Action(value = "/selectFamilleNorme")
    public String selectFamilleNorme() {
        return sessionSyncronize();
    }

    @Action(value = "/addFamilleNorme")
    public String addFamilleNorme() {
        this.viewFamilleNorme.insert();
        return sessionSyncronize();
    }

    @Action(value = "/deleteFamilleNorme")
    public String deleteFamilleNorme() {
        this.viewFamilleNorme.delete();
        return sessionSyncronize();
    }

    @Action(value = "/updateFamilleNorme")
    public String updateFamilleNorme() {
        this.viewFamilleNorme.update();
        return sessionSyncronize();
    }

    @Action(value = "/sortFamilleNorme")
    public String sortFamilleNorme() {
        this.viewFamilleNorme.sort();
        return sessionSyncronize();
    }

    /*
     * CLIENT
     */
    public void initializeClient() {
        try {
            System.out.println("/* initializeClient */");
            HashMap<String, ArrayList<String>> selection = this.viewFamilleNorme.mapContentSelected();
            if (!selection.isEmpty()) {

                StringBuilder requete = new StringBuilder("SELECT id_famille, id_application FROM arc.ihm_client WHERE id_famille='"
                        + selection.get("id_famille").get(0) + "'");

                HashMap<String, String> defaultInputFields = new HashMap<String, String>();
                defaultInputFields.put("id_famille", selection.get("id_famille").get(0));

                // defaultInputFields.put("id_famille", this.viewFamilleNorme.mapContentSelected().get("id_famille").get(0));
                this.viewClient.initialize(requete.toString(), "arc.ihm_client", defaultInputFields);
            } else {
                this.viewClient.destroy();

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Action(value = "/selectClient")
    public String selectClient() {
        System.out.println("selectClient " + this.scope);
        return sessionSyncronize();
    }

    @Action(value = "/addClient")
    public String addClient() {
        this.viewClient.insert();
        return sessionSyncronize();
    }

    /**
     * Suppression de Client Regle de gestion : impossible de supprimer une Client active
     *
     * @return
     */
    @Action(value = "/deleteClient")
    public String deleteClient() {
        HashMap<String, ArrayList<String>> selection = this.viewClient.mapContentSelected();
        if (!selection.isEmpty()) {
            this.viewClient.delete();
        }
        return sessionSyncronize();
    }

    //TODO : les lignes ne sont pas modifiables via l'IHM : méthode jamais appelable ?
    @Action(value = "/updateClient")
    public String updateClient() {
        this.viewClient.update();
        return sessionSyncronize();
    }

    @Action(value = "/sortClient")
    public String sortClient() {
        this.viewClient.sort();
        return sessionSyncronize();
    }

    /*
     * TABLES METIER
     */
    public void initializeTableMetier() {
        try {
            System.out.println("/* initializeTableMetier */");
            HashMap<String, ArrayList<String>> selection = this.viewFamilleNorme.mapContentSelected();
            if (!selection.isEmpty()) {
                HashMap<String, String> type = this.viewFamilleNorme.mapHeadersType();
                StringBuilder requete = new StringBuilder();
                requete.append("select * from arc.ihm_mod_table_metier");
                requete.append(" where id_famille" + ManipString.sqlEqual(selection.get("id_famille").get(0), type.get("id_famille")));
                HashMap<String, String> defaultInputFields = new HashMap<String, String>();
                defaultInputFields.put("id_famille", selection.get("id_famille").get(0));
                // defaultInputFields.put("nom_table_metier", selection.get("nom_table_metier").get(0));
                // defaultInputFields.put("description_table_metier", selection.get("description_table_metier").get(0));
                // this.viewTableMetier.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewTableMetier.getSessionName()));
                this.viewTableMetier.initialize(requete.toString(), "arc.ihm_mod_table_metier", defaultInputFields);
            } else {
                this.viewTableMetier.destroy();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Action(value = "/selectTableMetier")
    public String selectTableMetier() {
        return sessionSyncronize();
    }

    @Action(value = "/deleteTableMetier")
    public String deleteTableMetier() {
        StringBuilder message = new StringBuilder();
        if (deleteTableMetierWithoutSync(message)) {
            this.viewTableMetier.delete();
        }
        this.viewTableMetier.setMessage(message.toString());
        return sessionSyncronize();
    }

    @Action(value = "/sortTableMetier")
    public String sortTableMetier() {
        this.viewTableMetier.sort();
        return sessionSyncronize();
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

    @Action(value = "/addTableMetier")
    public String addTableMetier() {
        String nomTableMetier = this.viewTableMetier.getInputFieldFor("nom_table_metier");
		if (isNomTableMetierValide(nomTableMetier)) {
	        nomTableMetier = ArcStringUtils.cleanUpVariable(nomTableMetier);
			this.viewTableMetier.setInputFieldFor("nom_table_metier", nomTableMetier);
            this.viewTableMetier.insert();
        } else {
            setMessageNomTableMetierInvalide();
        }
        return sessionSyncronize();
    }

    //TODO : les lignes ne sont pas modifiables via l'IHM : méthode jamais appelable ?
    @Action(value = "/updateTableMetier")
    public String updateTableMetier() {
        if (isNomTableMetierValide(this.viewTableMetier.mapInputFields().get("nom_table_metier").get(0))) {
            StringBuilder message = new StringBuilder();
            this.deleteTableMetierWithoutSync(message);
            this.viewTableMetier.insert();
        } else {
            setMessageNomTableMetierInvalide();
        }
        return sessionSyncronize();
    }

    private void setMessageNomTableMetierInvalide() {
        this.viewTableMetier.setMessage("Un nom de table doit respecter la syntaxe :\n\"mapping_"
                + this.viewFamilleNorme.mapContent().get("id_famille").get(0)
                + "_<identifiant>_ok\"\nOù <identifiant> est un ensemble de mots séparés par des underscores (\"_\")");
    }

    private boolean deleteTableMetierWithoutSync(StringBuilder message) {
        System.out.println("Destruction de la table");
        boolean drop = true;
        
        HashMap<String,ArrayList<String>> content=this.viewTableMetier.mapContentSelected();
        
        for (int i = 0; (i < content.get("nom_table_metier").size()) && drop; i++) {
//            String nomVeridique = "arc.prod_" + this.viewTableMetier.mapContentSelected().get("nom_table_metier").get(i);
//            if (UtilitaireDao.get("arc").isTableExiste(null, nomVeridique)) {
//                drop = (UtilitaireDao.get("arc").getCount(null, nomVeridique) == 0);
//            }
            
        	// la condition pour dropper est plutot la suivante
        	// on ne doit plus avoir de variable dans la table arc.ihm_mod_variable_metier pour la famille et la table a dropper
        	// le drop de la table en elle meme est faite à l'initialisation !!!!!!!!!!!!!
        	
        	drop = (UtilitaireDao.get("arc").getInt(null, "SELECT count(1) from "+this.viewVariableMetier.getTable()
        					+" where id_famille='"+content.get("id_famille").get(i)+"' "
        					+ "and nom_table_metier='"+content.get("nom_table_metier").get(i)+"' ")
        					==0);
        	
        }
        if (drop) {
            UtilitaireDao.get("arc").dropTable(null, this.viewTableMetier.mapContentSelected().get("nom_table_metier").toArray(new String[0]));
            message.append("Les tables sont supprimées avec succès.");
            return true;
        } else {
            message.append("La table ne doit plus avoir de colonne pour pouvoir etre supprimée");
            if (this.viewTableMetier.mapContentSelected().get("nom_table_metier").size() > 1) {
                message.append("\nRecommencez en supprimant une table à la fois.");
            }
        }
        return false;
    }

    /*
     * VARIABLES METIER
     */
    public static final HashMap<String, ColumnRendering> getInitialRendering(List<String> aVariableListe) {
        HashMap<String, ColumnRendering> returned = new HashMap<String, ColumnRendering>();
        String size = "100px";
        String type = "text";
        String query = null;
        for (int i = 0; i < aVariableListe.size(); i++) {
            System.out.println(aVariableListe.get(i).replaceAll("^mapping_[^_]*_", "").replaceAll("_ok$", "").toLowerCase());
            returned.put(aVariableListe.get(i),
                    new ColumnRendering(true, aVariableListe.get(i).replaceAll("^mapping_[^_]*_", "").replaceAll("_ok$", "").toLowerCase(), size,
                            type, query, false));
        }
        return returned;
    }

    public void initializeVariableMetier() {
        List<String> listeTableFamille = getListeTableMetierFamille(this.viewFamilleNorme.mapContentSelected().get("id_famille").get(0));
        HashMap<String, ColumnRendering> rendering = getInitialRenderingViewVariableMetier(new HashMap<String, ColumnRendering>());
        rendering.putAll(getInitialRendering(listeTableFamille));
        this.viewVariableMetier.initialiserColumnRendering(rendering);
        try {
            System.out.println("/* initializeVariableMetier */");
            StringBuilder requete = getRequeteListeVariableMetierTableMetier(listeTableFamille,
                    this.viewFamilleNorme.mapContentSelected().get("id_famille").get(0));
            HashMap<String, String> defaultInputFields = new HashMap<String, String>();
            defaultInputFields.put("id_famille", this.viewFamilleNorme.mapContentSelected().get("id_famille").get(0));
            // this.viewVariableMetier.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewVariableMetier.getSessionName()));
            this.viewVariableMetier.initialize(requete.toString(), "arc."+IHM_MOD_VARIABLE_METIER, defaultInputFields);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static HashMap<String, ColumnRendering> getInitialRenderingViewVariableMetier(HashMap<String, ColumnRendering> returned) {
        returned.put("id_famille", new ColumnRendering(false, "Id.", "20px", "text", null, false));
        returned.put("nom_variable_metier", new ColumnRendering(true, "Nom de la variable", "200px", "text", null, false));
        returned.put("description_variable_metier", new ColumnRendering(true, "Description", "200px", "text", null, false));
        returned.put("type_variable_metier", new ColumnRendering(true, "Type de la variable", "100px", "select",
                "SELECT nom_type id, nom_type val FROM arc.ext_mod_type_autorise ORDER BY nom_type", true));
        returned.put("type_consolidation", new ColumnRendering(true, "Type consolidation", "200px", "text", null, false));
        return returned;
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
                    + "'||']%' then 'oui' else '' end " + listeTableMetier.get(i));
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

    @Action(value = "/selectVariableMetier")
    public String selectVariableMetier() {
        return sessionSyncronize();
    }

    @Action(value = "/addVariableMetier")
    public String addVariableMetier() {
        StringBuilder message = new StringBuilder();
        StringBuilder bloc = new StringBuilder();
        bloc.append(addNonExistingVariableMetierWithoutSync(message));
        bloc.append(synchronizeRegleWithVariableMetier(message, this.viewFamilleNorme.mapContentSelected().get("id_famille").get(0)));
        executeRequeteMiseAjourTableMetier(message, bloc);
        this.viewVariableMetier.setMessage(message.toString());
        return sessionSyncronize();
    }

    @Action(value = "/sortVariableMetier")
    public String sortVariableMetier() {
        this.viewVariableMetier.sort();
        return sessionSyncronize();
    }

    @Action(value = "/deleteVariableMetier")
	public String deleteVariableMetier() {
	    StringBuilder message = new StringBuilder();
	    StringBuilder bloc = new StringBuilder();
	    bloc.append(deleteVariableMetierWithoutSync(message, this.viewVariableMetier.mapContentSelected(),
	            this.viewVariableMetier.listContentSelected(), false));
	    bloc.append(synchronizeRegleWithVariableMetier(message, this.viewFamilleNorme.mapContentSelected().get("id_famille").get(0)));
	    executeRequeteMiseAjourTableMetier(message, bloc);
	    this.viewVariableMetier.setMessage(message.toString());
	    return sessionSyncronize();
	}

	@Action(value = "/updateVariableMetier")
	public String updateVariableMetier() {
		
		try {
		
	    StringBuilder message = new StringBuilder();
	    StringBuilder requete = new StringBuilder();
	    
	    HashMap <String,ArrayList<String>> mBefore=this.viewVariableMetier.mapContentBeforeUpdate();
	    ArrayList<ArrayList<String>> lBefore=this.viewVariableMetier.listContentBeforeUpdate();

	    for (LineObject line : this.viewVariableMetier.getContent().getT()) {
	    	int indexOfVar = this.viewVariableMetier.getHeadersDLabel().indexOf("nom_variable_metier");
	    	line.getD().set(indexOfVar, ArcStringUtils.cleanUpVariable(line.getD().get(indexOfVar)));
	    }
	    
	    HashMap <String,ArrayList<String>> mAfter=this.viewVariableMetier.mapContentAfterUpdate();
	    ArrayList<ArrayList<String>> lAfter=this.viewVariableMetier.listContentAfterUpdate();
	    
	    // partie 1 : update nom de variable
        // créer une map des noms avant aprés pour modifier les règles et les tables
	    ArrayList<String> namesAfter = mAfter.get("nom_variable_metier");
		for (int i=0; i< namesAfter.size(); i++)
        {

	    	String nameAfter = namesAfter.get(i);
        	String nameBefore = mBefore.get("nom_variable_metier").get(i);
			if (!nameBefore.equals(nameAfter))
        	{
        		// mise à jour du nom de la variable dans la table métier
    	    	requete.append("\n");
    	    	requete.append("update arc.ihm_mod_variable_metier set nom_variable_metier='"+nameAfter+"' ");
    	    	requete.append("where nom_variable_metier='"+nameBefore+"' ");
    	    	requete.append("and id_famille='"+mAfter.get("id_famille").get(i)+"'; ");
    	    	
        		// mise à jour du nom de la variable dans la table de règle
    	    	requete.append("\n");
    	    	requete.append("update arc.ihm_mapping_regle a set variable_sortie='"+nameAfter+"' ");
    	    	requete.append("where variable_sortie='"+nameBefore+"' ");
    	    	requete.append("and exists (select from arc.ihm_norme b where a.id_norme=b.id_norme and b.id_famille='"+mAfter.get("id_famille").get(i)+"'); ");
    	    	

    	    	// mise à jour du nom de la variable dans les tables des environnements
                StringBuilder requeteListeEnvironnement = new StringBuilder("SELECT distinct replace(id,'.','_') FROM arc.ext_etat_jeuderegle where isenv");
                List<String> listeEnvironnement = UtilitaireDao.get("arc").getList(null, requeteListeEnvironnement, new ArrayList<String>());
                
                for (String envName : listeEnvironnement) {
                	for (int k = numberOfColumnTableVariableMetier; k < mBefore.size(); k++) {
                		String nomVeridique = envName + "." + this.viewVariableMetier.getHeadersDLabel().get(k);

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
                				if (UtilitaireDao.get("arc").isColonneExiste(null, nomVeridique, nameBefore)) {

                					requete.append("\n");
                					requete.append("ALTER TABLE "+nomVeridique+" RENAME "+nameBefore+" TO "+nameAfter+";");

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
	        requete.append(synchronizeRegleWithVariableMetier(message, this.viewFamilleNorme.mapContentSelected().get("id_famille").get(0)));
	        executeRequeteMiseAjourTableMetier(message, requete);
	    }

	    this.viewVariableMetier.setMessage(message.toString());
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
		    this.viewVariableMetier.setMessage(e.getMessage());
		}
	    return sessionSyncronize();
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
            ArrayList<String> inputFields = this.viewVariableMetier.getInputFields();
			for (int i = numberOfColumnTableVariableMetier; i < inputFields.size(); i++) {
                if (StringUtils.isNotBlank(inputFields.get(i)) && inputFields.get(i).equals("oui")) {
                	
                	// au moins une table est resnseignée
                	blank=false;
                	
                    String nomVariableMetier = this.viewVariableMetier.getInputFieldFor("nom_variable_metier");
                    this.viewVariableMetier.setInputFieldFor("nom_variable_metier", ArcStringUtils.cleanUpVariable(nomVariableMetier));
                	
                    if (checkIsValide(inputFields)) {
                        requete.append("INSERT INTO arc."+IHM_MOD_VARIABLE_METIER+" (");
                        StringBuilder values = new StringBuilder();
                        for (int j = 0; j < numberOfColumnTableVariableMetier; j++) {
                            if (j > 0) {
                                requete.append(", ");
                                values.append(", ");
                            }
                            requete.append(this.viewVariableMetier.getHeadersDLabel().get(j));
                            values.append("'" + inputFields.get(j) + "'::"
                                    + this.viewVariableMetier.getHeadersDType().get(j));
                        }
                        requete.append(", nom_table_metier) VALUES (" + values.append(", '" + this.viewVariableMetier.getHeadersDLabel().get(i))
                                + "'::text);\n");
                    } else {
                        message.append("La variable "
                                + inputFields.get(1)
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

    private static boolean checkIsValide(ArrayList<String> inputFields) {
        StringBuilder requete = new StringBuilder("SELECT count(1) FROM arc."+IHM_MOD_VARIABLE_METIER+"\n")//
                .append("WHERE id_famille='" + inputFields.get(0) + "'\n")//
                .append("  AND nom_variable_metier='" + inputFields.get(1) + "';");
        return UtilitaireDao.get("arc").getInt(null, requete) == 0;
    }

    

    // @Deprecated
    // public void initializeVariableMetierFilsTableMetier() {
    // try {
    // System.out.println("initializeVariableMetier");
    // HashMap<String, ArrayList<String>> selection = this.viewTableMetier.mapContentSelected();
    // if (!selection.isEmpty()) {
    //
    // // System.out.println(selection);
    //
    // HashMap<String, String> type = this.viewTableMetier.mapHeadersType();
    // StringBuilder requete = new StringBuilder();
    // requete.append("select id_famille, nom_table_metier, nom_variable_metier, description_variable_metier, type_variable_metier");
    // /*
    // * , type_variable_metier description_type");
    // */
    // requete.append(" from arc."+IHM_MOD_VARIABLE_METIER+"");
    // requete.append(" where id_famille" + ManipString.sqlEqual(selection.get("id_famille").get(0), type.get("id_famille")));
    // requete.append(" AND nom_table_metier" + ManipString.sqlEqual(selection.get("nom_table_metier").get(0),
    // type.get("nom_table_metier")));
    // HashMap<String, String> defaultInputFields = new HashMap<String, String>();
    // // this.viewVariableMetier.setColumnRendering(ArcConstantVObjectGetter.columnRender.get(this.viewVariableMetier.getSessionName()));
    // this.viewVariableMetier.initialize(requete.toString(), "arc."+IHM_MOD_VARIABLE_METIER+"", defaultInputFields);
    // } else {
    // this.viewVariableMetier.destroy();
    // }
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // }
    // }

    /**
     * Ajoute une variable métier à des tables par UPDATE (la variable existe déjà)
     *
     * @param message
     */
    private String addExistingVariableMetierWithoutSync(StringBuilder message, ArrayList<ArrayList<String>> listContent) {
        try {
            StringBuilder requete = new StringBuilder();
            /**
             * Pour chaque ligne à UPDATE
             */
            for (int i = 0; i < listContent.size(); i++) {
                /**
                 * Et pour l'ensemble des tables métier
                 */
                for (int j = numberOfColumnTableVariableMetier; j < this.viewVariableMetier.mapContentAfterUpdate(i).size(); j++) {
                    /**
                     * Si une variable est à "oui" pour cette table alors qu'elle n'y était pas...
                     */
                    if (StringUtils.isNotBlank(listContent.get(i).get(j))
                            && StringUtils.isBlank(this.viewVariableMetier.listContentBeforeUpdate().get(i).get(j))) {
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
        requeteListeAddRegleMapping.append(", 'règle générée automatiquement lors de la création de cette variable'");
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
    private String deleteVariableMetierWithoutSync(StringBuilder message, HashMap<String, ArrayList<String>> mapContent,
            ArrayList<ArrayList<String>> listContent, boolean onlyWhereBlank) {
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
            for (int j = 0; (j < mapContent.get("nom_variable_metier").size()) && drop; j++) {
                String nomVariable = mapContent.get("nom_variable_metier").get(j);
                /**
                 * On prépare la liste des tables comportant effectivement la variable
                 */
                listeTable.setLength(0);
                /**
                 * Pour chaque table trouvée
                 */
                for (int i = numberOfColumnTableVariableMetier; (i < mapContent.size()) && drop; i++) {
                    if (StringUtils.isBlank(listContent.get(j).get(i)) || !onlyWhereBlank) {
                        listeTable.append("[" + this.viewVariableMetier.getHeadersDLabel().get(i) + "]");
                    }
                }
                delete.append("DELETE FROM arc."+IHM_MOD_VARIABLE_METIER+" WHERE id_famille='" + mapContent.get("id_famille").get(j)
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
            if (mapContent.get("nom_variable_metier").size() > 1) {
                message.append("Recommencez en supprimant une variable à la fois.\n");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return empty;
    }

    private boolean isMofificationOk(StringBuilder message, HashMap<String, ArrayList<String>> mapContentAfterUpdate) {
        return estCeQueLesNomsDeVariablesSontNonNuls(message, mapContentAfterUpdate)
                && estCeQueLesIdentifiantsSontExclus(message, mapContentAfterUpdate)
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

    /**
     * les variable commençant par "id_" sont des identifiants techniques qui doivent etre exclus
     * @param message
     * @param mapContentAfterUpdate
     * @return
     */
    private static boolean estCeQueLesIdentifiantsSontExclus(StringBuilder message, HashMap<String, ArrayList<String>> mapContentAfterUpdate) {
        for (int i = 0; i < mapContentAfterUpdate.get("nom_variable_metier").size(); i++) {
            String nomVariable = mapContentAfterUpdate.get("nom_variable_metier").get(i);
            String typeConsolidation = mapContentAfterUpdate.get("type_consolidation").get(i);
            
	          if (nomVariable.startsWith("id_") && !typeConsolidation.equalsIgnoreCase("{exclus}")) {
	          message.append("La variable identifiante technique " + nomVariable + " doit avoir type_consolidation = \"{exclus}\".");
	          return false;
	      }
            

//	            String nomFamille = mapContentAfterUpdate.get("id_famille").get(i);
//            Set <String> keys=mapContentAfterUpdate.keySet();
//
//            for (String s:keys)
//            {
//            	if (!s.equals("id_famille")
//            			&& !s.equals("nom_variable_metier")
//            			&& !s.equals("type_variable_metier")
//            			&& !s.equals("description_variable_metier")
//            			&& !s.equals("type_consolidation")
//            			)
//            	{
//
//                    String nomTableCourt =
//                    		ManipString.substringAfterFirst(ManipString.substringBeforeLast(s,underscore),underscore)
//                    		.substring(nomFamille.length()+1)
//                    		;
//
//
//
//                    if (nomVariable.equals("id_"+nomTableCourt) && !typeConsolidation.equalsIgnoreCase("{exclus}")) {
//                        message.append("La variable identifiante technique " + nomVariable + " doit avoir type_consolidation = \"{exclus}\".");
//                        return false;
//                    }
//
//            	}
//
//            }
            
            

        }
        return true;
    }

    private boolean estCeQueLeSchemaNeComportePasDeCycles(StringBuilder message, HashMap<String, ArrayList<String>> mapContentAfterUpdate) {
        // TODO Auto-generated method stub
        return true;
    }

    private static String mettreAJourInformationsVariables(VObject someViewVariableMetier) {
        StringBuilder requete = new StringBuilder();
        System.out.println("----------> " + someViewVariableMetier.mapContentAfterUpdate());
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
        return nomTable != null && nomTable.matches("(?i)^mapping_" + this.viewFamilleNorme.mapContentSelected().get("id_famille").get(0) + "_([a-z]|_)*[a-z]+_ok$");
    }

	private void deleteVariableMetierFilsDeTableMetierWithoutSync(StringBuilder message) {
        boolean drop = true;
        StringBuilder delete = new StringBuilder("BEGIN;");
        GenericRequest grDelete = GenericRequestFactory.as(new StringBuilder("ALTER TABLE {:table} DROP COLUMN {:column};"));
        for (int i = 0; (i < this.viewTableMetier.mapContentSelected().get("nom_table_metier").size()) && drop; i++) {
            String nomVeridique = "arc.prod_" + this.viewTableMetier.mapContentSelected().get("nom_table_metier").get(i);
            if (UtilitaireDao.get("arc").isTableExiste(null, nomVeridique)) {
                drop = (UtilitaireDao.get("arc").getCount(null, nomVeridique) == 0);
                grDelete.setName("{:table}", nomVeridique);
                for (int j = 0; (i < this.viewVariableMetier.mapContentSelected().get("nom_variable_metier").size()) && drop; j++) {
                    String nomVariable = this.viewVariableMetier.mapContentSelected().get("nom_variable_metier").get(j);
                    if (UtilitaireDao.get("arc").isColonneExiste(null, nomVeridique, nomVariable)) {
                        StringBuilder requete = new StringBuilder("SELECT count(1) count FROM " + nomVeridique + " WHERE " + nomVariable
                                + " IS NOT NULL");
                        drop = (UtilitaireDao.get("arc").getInt(null, requete) == 0);
                        if (drop) {
                            grDelete.setName("{:column}", nomVariable);
                            delete.append(grDelete.toString());
                        }
                    }
                }
            }
        }
        if (drop) {
            try {
                UtilitaireDao.get("arc").executeImmediate(null, delete.append("END;").toString());
                this.viewVariableMetier.delete();
                message.append("Toutes les variables sélectionnées ont été supprimées des tables sélectionnées.");
            } catch (SQLException ex) {
                drop = false;
                ex.printStackTrace();
            }
        }
        if (!drop) {
            message.append("Les variables sélectionnées n'ont pas été supprimées car la suppression d'au moins une variable pose problème.");
            if (this.viewVariableMetier.mapContentSelected().get("nom_variable_metier").size() > 1) {
                message.append("\nRecommencez en supprimant une variable à la fois.");
            }
        }
    }

    /**
     * @return the viewTableMetier
     */
    public final VObject getViewTableMetier() {
        return this.viewTableMetier;
    }

    /**
     * @param viewTableMetier
     *            the viewTableMetier to set
     */
    public final void setViewTableMetier(ViewTableMetier viewTableMetier) {
        this.viewTableMetier = viewTableMetier;
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
    public final void setViewVariableMetier(ViewVariableMetier viewVariableMetier) {
        this.viewVariableMetier = viewVariableMetier;
    }

    public String getScope() {
        return this.scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * @return the viewFamilleNorme
     */
    public final VObject getViewFamilleNorme() {
        return this.viewFamilleNorme;
    }

    /**
     * @param viewFamilleNorme
     *            the viewFamilleNorme to set
     */
    public final void setViewFamilleNorme(ViewFamilleNorme viewFamilleNorme) {
        this.viewFamilleNorme = viewFamilleNorme;
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
