package fr.insee.arc.core.service.global.dao;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.insee.arc.core.model.BatchMode;

public class ThreadOperationsTest {

	@Test
	public void checkPreviousPhaseDataDropConditionTest() {
		
		assertEquals(false,ThreadOperations.checkPreviousPhaseDataDropCondition(BatchMode.UNSET));
		assertEquals(false,ThreadOperations.checkPreviousPhaseDataDropCondition(BatchMode.KEEP_INTERMEDIATE_DATA));
		assertEquals(true,ThreadOperations.checkPreviousPhaseDataDropCondition(BatchMode.NORMAL));

	}

}
