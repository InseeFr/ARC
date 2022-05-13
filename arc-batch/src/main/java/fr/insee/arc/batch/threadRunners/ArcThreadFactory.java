package fr.insee.arc.batch.threadRunners;

import java.util.HashMap;

import fr.insee.arc.batch.threadRunners.parameter.ParameterKey;
import fr.insee.arc.batch.threadRunners.template.ThreadRunnerTemplate;
import fr.insee.arc.batch.unitaryLauncher.ChargerBatch;
import fr.insee.arc.batch.unitaryLauncher.ComputeBatchArgs;
import fr.insee.arc.batch.unitaryLauncher.ControlerBatch;
import fr.insee.arc.batch.unitaryLauncher.FiltrerBatch;
import fr.insee.arc.batch.unitaryLauncher.InitialiserBatch;
import fr.insee.arc.batch.unitaryLauncher.MapperBatch;
import fr.insee.arc.batch.unitaryLauncher.NormerBatch;
import fr.insee.arc.batch.unitaryLauncher.RecevoirBatch;
import fr.insee.arc.core.model.TraitementPhase;

public class ArcThreadFactory extends ThreadRunnerTemplate {

	public ArcThreadFactory(HashMap<String, String> mapParam, TraitementPhase phase) {
		initializeThreadRunnerTemplate(mapParam, phase);
	}

	@Override
	public void run()
	{
		execute();
	}
	
	
	public void execute() {

		switch (getPhaseName()) {
		case INITIALISATION:
			InitialiserBatch i = new InitialiserBatch(
					ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_SIZE_RECEPTION));
			i.execute();
			setReport(i.report);
			break;

		case RECEPTION:
			RecevoirBatch r = new RecevoirBatch(
					ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_SIZE_RECEPTION));
			r.execute();
			setReport(r.report);
			break;

		case CHARGEMENT:
			new ChargerBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_TO_LOAD))
					.execute();
			break;

		case NORMAGE:
			new NormerBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE))
					.execute();
			break;

		case CONTROLE:
			new ControlerBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE))
					.execute();
			break;
			
		case FILTRAGE:
			new FiltrerBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE))
					.execute();
			break;

		case MAPPING:
			new MapperBatch(ComputeBatchArgs.batchArgs(this.getMapParam(), ParameterKey.KEY_FOR_MAX_FILES_PER_PHASE))
					.execute();
			break;

		default:
			break;
		}

	}

}
