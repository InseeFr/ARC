package fr.insee.arc.core.service.p5mapping.bo.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import fr.insee.arc.core.service.p5mapping.bo.VariableMapping;
import fr.insee.arc.core.service.p5mapping.dao.MappingQueriesFactory;
import fr.insee.arc.utils.exception.ArcException;

public class RegleMappingGroupeTest {

	private RegleMappingGroupe rmg;

	private void initializeTest() {
		Set<String> anEnsembleIdentifiantRubriqueExistante = new HashSet<>();
		anEnsembleIdentifiantRubriqueExistante.add("i_r1");
		anEnsembleIdentifiantRubriqueExistante.add("i_r1");

		Set<String> anEnsembleNomRubriqueExistante = new HashSet<>();
		anEnsembleIdentifiantRubriqueExistante.add("v_r1");
		anEnsembleIdentifiantRubriqueExistante.add("v_r2");
		MappingQueriesFactory mappingQueriesFactory = new MappingQueriesFactory(null, "arc_bas2",
				anEnsembleIdentifiantRubriqueExistante, anEnsembleNomRubriqueExistante);

		VariableMapping vm = new VariableMapping(mappingQueriesFactory, "var_group", "text");
		this.rmg = new RegleMappingGroupe(mappingQueriesFactory, "{{1}{v_r1}}{{2}{v_r2}}", vm);
	}

	@Test
	public void deriverTest() throws ArcException {
		initializeTest();
		rmg.deriver();
		assertEquals(2, rmg.getEnsembleGroupes().size());
	}

	@Test
	public void getExpressionSQLTest() throws ArcException {
		assertThrows(ArcException.class, () -> {
			initializeTest();
			rmg.getExpressionSQL();
		});
	}

}
