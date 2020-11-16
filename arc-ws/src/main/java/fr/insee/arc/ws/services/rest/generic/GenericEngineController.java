package fr.insee.arc.ws.services.rest.generic;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.arc.core.dao.JeuDeRegleDao;
import fr.insee.arc.core.model.JeuDeRegle;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.service.engine.chargeur.ChargeurXmlComplexe;
import fr.insee.arc.core.service.engine.controle.ServiceJeuDeRegle;
import fr.insee.arc.core.service.engine.mapping.RegleMappingFactory;
import fr.insee.arc.core.service.engine.mapping.RequeteMapping;
import fr.insee.arc.core.service.engine.mapping.ServiceMapping;
import fr.insee.arc.core.service.engine.normage.NormageEngine;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.ws.services.rest.generic.pojo.GenericPojo;
import fr.insee.arc.ws.services.rest.generic.view.ReturnView;

@RestController
public class GenericEngineController {
	
	@Autowired
	private LoggerDispatcher loggerDispatcher;
	
    private static final Logger LOGGER = LogManager.getLogger(GenericEngineController.class);
	
	@RequestMapping(value = "/execute/engine/{serviceName}/{serviceId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> executeEngineClient(
			@PathVariable String serviceName,
			@PathVariable int serviceId,
			@RequestBody(required = true) GenericPojo p
	)
	{
		Date firstContactDate=new Date();
		ReturnView r=new ReturnView();
		
		String identifiantLog = "(" + serviceName + ", " + serviceId + ")";
		
		loggerDispatcher.info(identifiantLog + " received", LOGGER);

		try (Connection c = UtilitaireDao.get("arc").getDriverConnexion()) {
			
			GenericRulesDao.fillRules(c, p, serviceName, serviceId);
			
			StringBuilder requete;
			
			loggerDispatcher.info(identifiantLog + " launching phases", LOGGER);
			String env = p.sandbox;


			String structure = "";
				for (int i = 2; i <= Integer.parseInt(p.targetPhase); i++) {

					switch (TraitementPhase.getPhase(i)) {
					case CHARGEMENT:
						// register file
						String tableRegleChargement = env + ".chargement_regle";

						try (InputStream inputStream = new ByteArrayInputStream(
								p.fileContent.getBytes(StandardCharsets.UTF_8));) {
							ChargeurXmlComplexe chargeur = new ChargeurXmlComplexe(c, p.fileName, inputStream, currentTemporaryTable(i),
									p.norme, p.periodicite, p.validite, tableRegleChargement);
							chargeur.executeEngine();
							structure = chargeur.jointure.replace("''", "'");
						}
						break;
					case NORMAGE:
						HashMap<String, ArrayList<String>> pil = new HashMap<>();
						pil.put("id_source", new ArrayList<String>(Arrays.asList(p.fileName)));
						pil.put("id_norme", new ArrayList<String>(Arrays.asList(p.norme)));
						pil.put("validite", new ArrayList<String>(Arrays.asList(p.validite)));
						pil.put("periodicite", new ArrayList<String>(Arrays.asList(p.periodicite)));
						pil.put("jointure", new ArrayList<String>(Arrays.asList(structure)));

						HashMap<String, ArrayList<String>> regle = new HashMap<>();
						regle.put("id_regle", new ArrayList<String>());
						regle.put("id_norme", new ArrayList<String>());
						regle.put("periodicite", new ArrayList<String>());
						regle.put("validite_inf", new ArrayList<String>());
						regle.put("validite_sup", new ArrayList<String>());
						regle.put("id_classe", new ArrayList<String>());
						regle.put("rubrique", new ArrayList<String>());
						regle.put("rubrique_nmcl", new ArrayList<String>());

						HashMap<String, ArrayList<String>> rubriqueUtiliseeDansRegles = new HashMap<>();
						rubriqueUtiliseeDansRegles.put("var", new ArrayList<String>());

						NormageEngine normage = new NormageEngine(c, pil, regle, rubriqueUtiliseeDansRegles, previousTemporaryTable(i), currentTemporaryTable(i),
								null);
						normage.executeEngine();
						structure = normage.structure;

						break;
					case CONTROLE:
						requete = new StringBuilder();
						requete.append(
								"CREATE TEMPORARY TABLE "+currentTemporaryTable(i)+" as select *, '0'::text collate \"C\" as controle, null::text[] collate \"C\" as brokenrules from "+previousTemporaryTable(i)+";");
						UtilitaireDao.get("arc").executeImmediate(c, requete);

						ServiceJeuDeRegle sjdr = new ServiceJeuDeRegle(env + ".controle_regle");

						// Récupération des règles de controles associées aux jeux de règle
						JeuDeRegle jdr = new JeuDeRegle();

						sjdr.fillRegleControle(c, jdr, env + ".controle_regle", currentTemporaryTable(i));
						sjdr.executeJeuDeRegle(c, jdr, currentTemporaryTable(i), structure);
						break;
					case FILTRAGE:
						UtilitaireDao.get("arc").executeImmediate(c, "CREATE TEMPORARY TABLE "+currentTemporaryTable(i)+" as select * from "+previousTemporaryTable(i)+";");
						//TODO
						break;
					case MAPPING:
						UtilitaireDao.get("arc").executeImmediate(c, "CREATE TEMPORARY TABLE "+currentTemporaryTable(i)+" as select * from "+previousTemporaryTable(i)+";");
						String tableTempFiltrageOk = previousTemporaryTable(i);
						List<JeuDeRegle> listeJeuxDeRegles = JeuDeRegleDao.recupJeuDeRegle(c, tableTempFiltrageOk, env + ".mapping_regle");
						ServiceMapping serviceMapping = new ServiceMapping();
						RegleMappingFactory regleMappingFactory = serviceMapping.construireRegleMappingFactory(c, env, tableTempFiltrageOk, "v_");
						String idFamille = serviceMapping.fetchIdFamille(c, listeJeuxDeRegles.get(0), env + ".norme");

			            RequeteMapping requeteMapping = new RequeteMapping(c, regleMappingFactory, idFamille, listeJeuxDeRegles.get(0),
			                        env, tableTempFiltrageOk, 0);
			            requeteMapping.construire();
			            UtilitaireDao.get("arc").executeBlock(c, requeteMapping.requeteCreationTablesTemporaires());

			            StringBuilder req = new StringBuilder();
			            req.append(requeteMapping.getRequete(p.fileName, false));
			            UtilitaireDao.get("arc").executeBlock(c,"set enable_nestloop=off;"+req.toString()+"set enable_nestloop=on;");
			            req.setLength(0);

		                StringBuilder requeteMAJFinale = new StringBuilder();
		                requeteMAJFinale.append(requeteMapping.requeteTransfertVersTablesMetierDefinitives());
		                UtilitaireDao.get("arc").executeBlock(c, requeteMAJFinale);

		                UtilitaireDao.get("arc").dropTable(c, requeteMapping.tableauNomsTablesTemporaires());
						break;

					default:
						
						break;

					}

				}
				
				GenericRulesDao.buildResponse(c, p, r, firstContactDate);

		} catch (Exception e) {
			loggerDispatcher.error(identifiantLog, e, LOGGER);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(r);
		}
		loggerDispatcher.info(identifiantLog + " done", LOGGER);
		return ResponseEntity.status(HttpStatus.OK).body(r);

	}
	
	@RequestMapping(value = "/execute/engine/{serviceName}/{serviceId}/{sandbox}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ReturnView> executeEngineClient(
			@PathVariable String serviceName,
			@PathVariable int serviceId,
			@PathVariable int sandbox,
			@RequestBody(required = true) GenericPojo p
	)
	{
		p.sandbox="arc_"+sandbox;
		return  executeEngineClient(serviceName,serviceId,p);
	}

	
//	@RequestMapping(value = "/execute/engine/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<ReturnView> executeEngine(
//			@RequestBody(required = true) GenericPojo p
//	)
//	{
//
//
//		Date receptionDate=new Date();
//		p.fileName = p.fileName == null ? "f.xml" : p.fileName;
//		ReturnView r=new ReturnView();
//
//		try (Connection c = UtilitaireDao.get("arc").getDriverConnexion()) {
//
//			String env = p.sandbox;
//
//
//				for (int i = 2; i <= Integer.parseInt(p.targetPhase); i++) {
//					String structure = "";
//
//					switch (TraitementPhase.getPhase(i)) {
//					case CHARGEMENT:
//						// register file
//						String tableRegleChargement = env + ".chargement_regle";
//
//						try (InputStream inputStream = new ByteArrayInputStream(
//								p.fileContent.getBytes(StandardCharsets.UTF_8));) {
//							ChargeurXmlComplexe chargeur = new ChargeurXmlComplexe(c, p.fileName, inputStream, currentTemporaryTable(i),
//									p.version, p.periodicite, p.validite, tableRegleChargement);
//							chargeur.executeEngine();
//							structure = chargeur.jointure.replace("''", "'");
//						}
//						break;
//					case NORMAGE:
//						HashMap<String, ArrayList<String>> pil = new HashMap<>();
//						pil.put("id_source", new ArrayList<String>(Arrays.asList(p.fileName)));
//						pil.put("id_norme", new ArrayList<String>(Arrays.asList(p.version)));
//						pil.put("validite", new ArrayList<String>(Arrays.asList(p.validite)));
//						pil.put("periodicite", new ArrayList<String>(Arrays.asList(p.periodicite)));
//						pil.put("jointure", new ArrayList<String>(Arrays.asList(structure)));
//
//						HashMap<String, ArrayList<String>> regle = new HashMap<>();
//						regle.put("id_regle", new ArrayList<String>());
//						regle.put("id_norme", new ArrayList<String>());
//						regle.put("periodicite", new ArrayList<String>());
//						regle.put("validite_inf", new ArrayList<String>());
//						regle.put("validite_sup", new ArrayList<String>());
//						regle.put("id_classe", new ArrayList<String>());
//						regle.put("rubrique", new ArrayList<String>());
//						regle.put("rubrique_nmcl", new ArrayList<String>());
//
//						HashMap<String, ArrayList<String>> rubriqueUtiliseeDansRegles = new HashMap<>();
//						rubriqueUtiliseeDansRegles.put("var", new ArrayList<String>());
//
//						NormageEngine normage = new NormageEngine(c, pil, regle, rubriqueUtiliseeDansRegles, previousTemporaryTable(i), currentTemporaryTable(i),
//								null);
//						normage.executeEngine();
//						structure = normage.structure;
//
//						break;
//					case CONTROLE:
//
//						StringBuilder requete = new StringBuilder();
//						requete.append(
//								"CREATE TEMPORARY TABLE "+currentTemporaryTable(i)+" as select *, '0'::text collate \"C\" as controle, null::text[] collate \"C\" as brokenrules from "+previousTemporaryTable(i)+";");
//						UtilitaireDao.get("arc").executeImmediate(c, requete);
//
//						ServiceJeuDeRegle sjdr = new ServiceJeuDeRegle(env + ".controle_regle");
//
//						// Récupération des règles de controles associées aux jeux de règle
//						JeuDeRegle jdr = new JeuDeRegle();
//
//						sjdr.fillRegleControle(c, jdr, env + ".controle_regle", currentTemporaryTable(i));
//						sjdr.executeJeuDeRegle(c, jdr, currentTemporaryTable(i), structure);
//						break;
//
//					default:
//						break;
//
//					}
//
//				}
//
//
//				// response build
//				r.setReceptionTime(receptionDate);
//				r.setReturnTime(new Date());
//
//				DataSetView ds=new DataSetView();
//				ds.setDatasetId(1);
//				ds.setDatasetName("requête 1");
//
//				GenericBean gb=new GenericBean(UtilitaireDao.get("arc").executeRequest(c, p.query));
//				ds.setContent(gb.mapRecord());
//
//				r.setDataSetView(Arrays.asList(ds));
//
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return ResponseEntity.status(HttpStatus.OK).body(r);
//
//	}
//
	
	// les tables temporaires des phases respectives valent a,b,c,d ...
	// important de rester sur un octet pour les performances
	private static int temporaryTableAsciiStart=97;

	
	private String generateTemporaryTableName(int i)
	{
		return String.valueOf(((char) (temporaryTableAsciiStart + i)));
	}
	
	private String currentTemporaryTable(int i)
	{
		return generateTemporaryTableName(i);
	}
	
	
	private String previousTemporaryTable(int i)
	{
		return generateTemporaryTableName(i-1);
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