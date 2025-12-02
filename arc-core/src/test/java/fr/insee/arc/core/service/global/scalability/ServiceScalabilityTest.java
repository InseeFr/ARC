package fr.insee.arc.core.service.global.scalability;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insee.arc.utils.consumer.ThrowingConsumer;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.query.InitializeQueryTest;

public class ServiceScalabilityTest extends InitializeQueryTest {

	@Test
	public void testDispatchOnNods() throws SQLException, ArcException {

		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	    System.setOut(new PrintStream(outContent));
	    
		buildPropertiesWithTwoExecutors(null);
		
		// coordinator thread will write coordinator
		ThrowingConsumer<Connection> onCoordinator = c -> {
			System.out.println("coordinator");
		};

		// executor threads will write executor
		ThrowingConsumer<Connection> onExecutor = executorConnection -> {
			System.out.println("executor");
		};

		// start threads
		ServiceScalability.dispatchOnNods(c, onCoordinator, onExecutor);

		List<String> expected = Arrays.asList("coordinator","executor", "executor");
		List<String> result = Arrays.asList(outContent.toString().split(System.lineSeparator()));
		
		assertTrue(expected.containsAll(result) && result.containsAll(expected));
			
	}

}
