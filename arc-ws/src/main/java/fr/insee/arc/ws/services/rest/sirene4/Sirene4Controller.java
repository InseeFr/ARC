package fr.insee.arc.ws.services.rest.sirene4;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.ApiInitialisationService;
import fr.insee.arc.core.service.ApiReceptionService;
import fr.insee.arc.core.service.engine.chargeur.ChargeurXmlComplexe;
import fr.insee.arc.core.service.engine.controle.ServiceJeuDeRegle;
import fr.insee.arc.core.service.engine.normage.NormageEngine;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.ws.services.rest.sirene4.view.AnomalieView;
import fr.insee.arc.ws.services.rest.sirene4.view.DeclarationAnomalieView;
import fr.insee.arc.ws.services.rest.sirene4.view.DeclarationIdentificationView;
import fr.insee.arc.ws.services.rest.sirene4.view.DeclarationView;
import fr.insee.arc.ws.services.rest.sirene4.view.EnteteAnomalieView;
import fr.insee.arc.ws.services.rest.sirene4.view.EnteteView;
import fr.insee.arc.ws.services.rest.sirene4.view.EnumCategorie;
import fr.insee.arc.ws.services.rest.sirene4.view.EnvoiView;


@RestController
public class Sirene4Controller {

	/** Début du nom de schéma pour chaque bac à sable.*/
	private static final String SCHEMA_BACASABLE = "arc_bas";


	@RequestMapping(value = "/hello")
	public ResponseEntity<String> sayHello() {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return new ResponseEntity<>("{\"msg\": \"Hello Sirene\"}", httpHeaders, HttpStatus.OK);
	}

	// http://qfsir4arclht04.ad.insee.intra/sir4arcws/generateSandbox?envModelId=4&envPoolSize=5
	// http://localhost:18080/arc-ws/generateSandbox?envModelId=4&envPoolSize=5
	@RequestMapping(value = "/generateSandbox")
	public ResponseEntity<String> updateSandbox(@RequestParam int envModelId, @RequestParam int envPoolSize) {

		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		final int MIN_SANDBOX_ID = 9001;
		final int MAX_POOL_SIZE = 99;
		String envModel = SCHEMA_BACASABLE + envModelId;

		StringBuilder requete;
//		String repertoire = PropertiesHandler.getInstance().getBatchParametersDirectory();
		String repertoire = PropertiesHandler.getInstance().getBatchParametersDirectory();
		try {

			// on detruit tous les sandbox existants
			for (int i = MIN_SANDBOX_ID; i <= MIN_SANDBOX_ID + MAX_POOL_SIZE; i++) {
				String env = SCHEMA_BACASABLE + i;

				requete = new StringBuilder();
				requete.append("DROP SCHEMA IF EXISTS " + env + " CASCADE;");
				UtilitaireDao.get("arc").executeImmediate(null, requete);

				File f = new File(repertoire + File.separator + env.toUpperCase());
				deleteDirectory(f);
			}

			// récupération des tables de l'environnement modele
			GenericBean gb = new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
					"select tablename from pg_tables where schemaname='" + envModel + "'"));
			List<String> tables = gb.mapContent().get("tablename");

			// on crée tous les sandbox demandés
			for (int i = MIN_SANDBOX_ID; i < MIN_SANDBOX_ID + envPoolSize; i++) {
				String env = SCHEMA_BACASABLE + i;

				// création du systeme de fichier
				new ApiReceptionService(TraitementPhase.RECEPTION.toString(), "arc.ihm", env, repertoire,
						Integer.valueOf(TraitementPhase.RECEPTION.getNbLigneATraiter()))
						.moveClientFiles(0,TraitementPhase.RECEPTION.getNbLigneATraiter());


				requete = new StringBuilder();
				requete.append("\n /* '" + env + "' */ ");
				requete.append("\n INSERT INTO arc.service_env_pool values('" + env + "');");
				requete.append("CREATE SCHEMA " + env + ";");
				for (String t : tables) {
					requete.append("\n CREATE UNLOGGED table " + env + "." + t + " as select * from " + envModel + "."
							+ t + ";");
				}
				UtilitaireDao.get("arc").executeImmediate(null, requete);

				ApiInitialisationService.clearPilotageAndDirectories(repertoire, env);
				ApiInitialisationService service = new ApiInitialisationService(
						TraitementPhase.INITIALISATION.toString(), "arc.ihm", env, repertoire,
						TraitementPhase.INITIALISATION.getNbLigneATraiter());
				try {
					service.resetEnvironnement();
				} finally {
					service.finaliser();
				}

				requete = new StringBuilder();
				requete.append("DROP TRIGGER tg_pilotage_fichier_calcul ON " + env + ".pilotage_fichier;");
				requete.append("DROP TRIGGER tg_pilotage_fichier_fin ON " + env + ".pilotage_fichier;");
				requete.append("DROP INDEX IF EXISTS " + env + ".idx1_pilotage_fichier;");
				requete.append("DROP INDEX IF EXISTS " + env + ".idx2_pilotage_fichier;");
				requete.append("DROP INDEX IF EXISTS " + env + ".idx2_pilotage_fichier;");
				requete.append("DROP INDEX IF EXISTS " + env + ".idx3_pilotage_fichier;");
				requete.append("DROP INDEX IF EXISTS " + env + ".idx4_pilotage_fichier;");
				requete.append("DROP INDEX IF EXISTS " + env + ".idx5_pilotage_fichier;");
				requete.append("DROP INDEX IF EXISTS " + env + ".idx6_pilotage_fichier;");

				try {
					UtilitaireDao.get("arc").executeImmediate(null, requete);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				File f = new File(repertoire + File.separator + env.toUpperCase());
				deleteDirectory(f);
				
			}

		} catch (Exception e) {
			return new ResponseEntity<>("{\"msg\": \"Error : " + e + "\"}", httpHeaders, HttpStatus.OK);
		}
		return new ResponseEntity<>("{\"msg\": \"Service sandboxes had been reset and generated\"}", httpHeaders,
				HttpStatus.OK);
	}

	@RequestMapping(value = "/liasse2/{version}", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EnvoiView> validateLiasse2(@PathVariable String version, @RequestBody(required = false) String xml) {
		try (Connection c = UtilitaireDao.get("arc").getDriverConnexion())
		{

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final EnvoiView envoiView = new EnvoiView();
		return ResponseEntity.status(HttpStatus.OK).body(envoiView);

	}

	@RequestMapping(value = "/liasse/{version}", consumes = MediaType.APPLICATION_XML_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EnvoiView> validateLiasse(@PathVariable String version, @RequestBody(required = false) String xml) {
		StringBuilder requete = new StringBuilder();
		String fileName = "f.xml";
		String periodicite = "A";
		String validite = "2020-01-01";

		final EnvoiView envoiView = new EnvoiView();

		try (Connection c = UtilitaireDao.get("arc").getDriverConnexion())
		{
		
			String env = "arc_bas4";
			try {

				// register file
				String jointure;
				String tableRegleChargement=env+".chargement_regle";
				
				try (InputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));) {
					ChargeurXmlComplexe chargeur = new ChargeurXmlComplexe(c, fileName, inputStream, "A", version,
							periodicite, validite, tableRegleChargement);
					chargeur.executeEngine();
//					jointure = chargeur.jointure.replace("''", "'");
				}
				
//				HashMap<String, ArrayList<String>> pil = new HashMap<>();
//				pil.put("id_source", new ArrayList<String>(Arrays.asList(fileName)));
//				pil.put("id_norme", new ArrayList<String>(Arrays.asList(version)));
//				pil.put("validite", new ArrayList<String>(Arrays.asList(validite)));
//				pil.put("periodicite", new ArrayList<String>(Arrays.asList(periodicite)));
//				pil.put("jointure", new ArrayList<String>(Arrays.asList(jointure)));
//
//				HashMap<String, ArrayList<String>> regle = new HashMap<>();
//				regle.put("id_regle", new ArrayList<String>());
//				regle.put("id_norme", new ArrayList<String>());
//				regle.put("periodicite", new ArrayList<String>());
//				regle.put("validite_inf", new ArrayList<String>());
//				regle.put("validite_sup", new ArrayList<String>());
//				regle.put("id_classe", new ArrayList<String>());
//				regle.put("rubrique", new ArrayList<String>());
//				regle.put("rubrique_nmcl", new ArrayList<String>());
//
//				HashMap<String, ArrayList<String>> rubriqueUtiliseeDansRegles = new HashMap<>();
//				rubriqueUtiliseeDansRegles.put("var", new ArrayList<String>());
//
//				NormageEngine normage = new NormageEngine(c, pil, regle, rubriqueUtiliseeDansRegles, "A", "B", null);
//				normage.executeEngine();
//
//				requete = new StringBuilder();
//				requete.append(
//						"CREATE TEMPORARY TABLE c as select *, '0'::text collate \"C\" as controle, null::text[] collate \"C\" as brokenrules from b;");
//				UtilitaireDao.get("arc").executeImmediate(c, requete);
//
//				ServiceJeuDeRegle sjdr = new ServiceJeuDeRegle(env + ".controle_regle");
//
//				// Récupération des règles de controles associées aux jeux de règle
//				JeuDeRegle jdr = new JeuDeRegle();
//
//				sjdr.fillRegleControle(c, jdr, env + ".controle_regle", "c");
//				sjdr.executeJeuDeRegle(c, jdr, "c", normage.structure);
//
//				
//				envoiView.setDateHeureReception(new Date());
//				envoiView.setVersionConformite(version);
//				
//				// retrouver la table des données controlées pour requeter
//				String tableControle="c";
//				String tableRubriqueEntete=env+".nmcl_rubriqueentete_v001";
//				String tableRegleControle=env+".controle_regle";
//
//				// récupérer les résultats
//				requete=new StringBuilder();
//				requete.append("\n with tmp_count as (SELECT count(distinct(i_liasse)) as nb_liasse from "+tableControle+")");
//				requete.append("\n , tmp_rules as (select  distinct unnest(brokenrules)::int as brokenrules from "+tableControle+")");
//				requete.append("\n , tmp_regle as ( ");
//				requete.append("\n  SELECT id_regle, id_classe, rubrique_pere, rubrique_fils, rubrique as entete");
//				requete.append("\n  FROM "+tableRegleControle+" a");
//				requete.append("\n  LEFT OUTER JOIN "+tableRubriqueEntete+" b ON (lower(a.rubrique_pere) like '_\\_'||lower(b.rubrique) or lower(a.rubrique_fils) like '_\\_'||lower(b.rubrique))");
//				requete.append("\n  ");
//				requete.append("\n  WHERE id_norme='"+version+"'");
//				requete.append("\n  AND EXISTS (SELECT from tmp_rules b where a.id_regle=b.brokenrules)");
//				requete.append("\n )");
//				requete.append("\n , tmp_declaration as (");
//				requete.append("\n SELECT distinct v_c02 as numero_declaration, regexp_replace(string_agg(v_c10_1,' ') over (partition by v_c02 order by i_c10_1),'([^ ]+)( \\1)*','\\1','g') as evenementDeclaration ");
//				requete.append("\n from "+tableControle+" b ");
//				requete.append("\n )");
//				requete.append("\n  , tmp_data as (");
//				requete.append("\n 	SELECT * from (");
//				requete.append("\n 	SELECT distinct v_c02 as numero_declaration, regexp_replace(string_agg(v_c10_1,' ') over (partition by v_c02 order by i_c10_1),'([^ ]+)( \\1)*','\\1','g') as evenement_declaration, brokenrules");
//				requete.append("\n 		 FROM "+tableControle+" b");
//				requete.append("\n 		 where exists (select from "+tableControle+" b where brokenrules is not null)");
//				requete.append("\n 	) vv	 where brokenrules is not null");
//				requete.append("\n 	 )");
//				requete.append("\n ");
//				requete.append("\n  SELECT 1 as type_reponse, nb_liasse::text, null as numero_declaration, null as evenement_declaration, null as code, null as categorie, null as id_regle, null as message from tmp_count");
//				requete.append("\n ");
//				requete.append("\n  UNION ALL");
//				requete.append("\n  SELECT 2, null, null, null, id_classe, 'BLOQUANT', id_regle, ");
//				requete.append("\n  'Echec du contrôle de '||id_classe||case when rubrique_fils is null then ' sur '||substring(rubrique_pere,3) else ' entre '||substring(rubrique_pere,3)||' et '||substring(rubrique_fils,3) end");
//				requete.append("\n  FROM tmp_regle a where entete is not null");
//				requete.append("\n ");
//				requete.append("\n  UNION ALL");
//				requete.append("\n 	(");
//				requete.append("\n  SELECT * FROM (");
//				requete.append("\n  SELECT 3 as type_reponse, null, numero_declaration, evenementDeclaration, null, null, null, null from tmp_declaration a ");
//				requete.append("\n  UNION ALL ");
//				requete.append("\n  SELECT 4, null, numero_declaration, evenement_declaration, id_classe, 'BLOQUANT', id_regle, ");
//				requete.append("\n  'Echec du contrôle de '||id_classe||case when rubrique_fils is null then ' sur '||substring(rubrique_pere,3) else ' entre '||substring(rubrique_pere,3)||' et '||substring(rubrique_fils,3) end");
//				requete.append("\n  FROM tmp_regle a");
//				requete.append("\n  , lateral (");
//				requete.append("\n  select distinct numero_declaration, evenement_declaration from tmp_data b");
//				requete.append("\n 		WHERE b.brokenrules @>  array[a.id_regle::text]");
//				requete.append("\n  ) v");
//				requete.append("\n   where entete is null");
//				requete.append("\n   ) w");
//				requete.append("\n order by numero_declaration, type_reponse");
//				requete.append("\n  )");
//
//				
//
//				HashMap<String,ArrayList<String>> mapResultat=new GenericBean(UtilitaireDao.get("arc").executeRequest(c,
//						requete)).mapContent();
//				
//
//				List<AnomalieView> anomaliesEntete = new ArrayList<>();
//				EnteteAnomalieView enteteAnomalieView=new EnteteAnomalieView();
//				enteteAnomalieView.setAnomalies(anomaliesEntete);
//		    	EnteteView enteteView = new EnteteView();
//				enteteView.setEnteteAnomalie(enteteAnomalieView);
//				envoiView.setEntete(enteteView);
//
//				DeclarationIdentificationView declarationIdentificationView=null;
//				DeclarationView declarationView=null;
//				DeclarationAnomalieView declarationAnomalieView=null;
//				List<AnomalieView> anomaliesDeclaration =null;
//						
//				List<DeclarationView> declarations =new ArrayList<>();
//				envoiView.setDeclarations(declarations);
//
//				for (int i=0;i<mapResultat.get("type_reponse").size();i++)
//				{
//					String typeReponse = mapResultat.get("type_reponse").get(i);
//					if (typeReponse.equals("1"))
//					{
//						envoiView.setNombreDeclarations(Integer.parseInt(mapResultat.get("nb_liasse").get(i)));
//					}
//					if (typeReponse.equals("2"))
//					{
//						anomaliesEntete.add(new AnomalieView(mapResultat.get("code").get(i),
//								EnumCategorie.valueOf(mapResultat.get("categorie").get(i)),
//								mapResultat.get("message").get(i)
//								, Integer.parseInt(mapResultat.get("id_regle").get(i)), 0));
//					}
//					if (typeReponse.equals("3"))
//					{
//
//						if (declarationIdentificationView==null
//								|| !declarationIdentificationView.getNumeroDeclaration()
//								.equals(mapResultat.get("numero_declaration").get(i)))
//						{
//							
//						
//							declarationView=new DeclarationView();
//							declarationIdentificationView=new DeclarationIdentificationView();
//							declarationIdentificationView.setNumeroDeclaration(mapResultat.get("numero_declaration").get(i));
//							declarationIdentificationView.setEvenementDeclaration(mapResultat.get("evenement_declaration").get(i));
//							
//							declarationAnomalieView=new DeclarationAnomalieView();
//							anomaliesDeclaration=new ArrayList<AnomalieView>();
//							declarationAnomalieView.setAnomalies(anomaliesDeclaration);
//
//							declarationView.setDeclarationIdentification(declarationIdentificationView);
//							declarationView.setDeclarationAnomalie(declarationAnomalieView);
//							declarations.add(declarationView);
//
//						}
//					}
//					if (typeReponse.equals("4"))
//					{
//						anomaliesDeclaration.add(new AnomalieView(
//								mapResultat.get("code").get(i)
//								, EnumCategorie.valueOf(mapResultat.get("categorie").get(i))
//								, mapResultat.get("message").get(i)
//								, Integer.parseInt(mapResultat.get("id_regle").get(i)), 0));
//					}
//				}


			} finally {
				// asynchron : clean and release sanbdox
//				new Thread() {
//					public void run() {
//
//						StringBuilder requete = new StringBuilder();
//						requete.append("DELETE FROM arc.service_env_locked where id_env='" + env + "';");
//						requete.append("vacuum freeze arc.service_env_locked;");
//
//						try {
//							UtilitaireDao.get("arc").executeImmediate(null, requete);
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//
//					}
//				}.start();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseEntity.status(HttpStatus.OK).body(envoiView);

	}

	/** Supprime le répertoire s'il existe. */
	private void deleteDirectory(File f) throws IOException {
		if (f.exists()) {
			FileUtils.deleteDirectory(f);
		}
	}

//	public String ChooseAndLockSandbox(Connection c) {
//		StringBuilder requete = new StringBuilder();
//		requete.append("\n WITH TMP_INSERT as ( ");
//		requete.append("\n INSERT INTO arc.service_env_locked ");
//		requete.append("\n SELECT id_env from arc.service_env_pool a ");
//		requete.append("\n WHERE NOT EXISTS (SELECT FROM arc.service_env_locked b WHERE a.id_env=b.id_env) ");
//		requete.append("\n LIMIT 1");
//		requete.append("\n RETURNING id_env )");
//		requete.append("\n SELECT id_env FROM tmp_insert; ");
//
//		do {
//
//			try {
//				String env = UtilitaireDao.get("arc").getString(c, requete);
//				if (env != null) {
//					return env;
//				}
//			} catch (Exception e) {
//			}
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//
//			}
//
//		} while (true);
//	}

}
