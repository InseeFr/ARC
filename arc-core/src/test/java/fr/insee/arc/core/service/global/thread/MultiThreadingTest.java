package fr.insee.arc.core.service.global.thread;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MultiThreadingTest {

	@Test
	public void dispatchFilesByNod_0ExecutorNods_allAllocatedTest()
	{
		List<String> listIdSource = Arrays.asList("file1", "file2");
		int startIndexOfExecutorNods=0;
		int numberOfExecutorNods=0;
		
		Map<Integer, List<Integer>> filesByNods = MultiThreading.dispatchFilesByNod(listIdSource, startIndexOfExecutorNods,
				numberOfExecutorNods, false);
		
		// test if all files had been allocated to nod 0
		assertEquals(listIdSource.size(), filesByNods.get(0).size());
	}
	
	@Test
	public void dispatchFilesByNod_2ExecutorNods_allAllocatedTest()
	{
		List<String> listIdSource = Arrays.asList("file1", "file2", "file3", "file4", "file5");
		int startIndexOfExecutorNods=1;
		int numberOfExecutorNods=2;
		
		Map<Integer, List<Integer>> filesByNods = MultiThreading.dispatchFilesByNod(listIdSource, startIndexOfExecutorNods,
				numberOfExecutorNods,true);
		
		// test if all files had been allocated to executor nods 1 and 2
		assertEquals(listIdSource.size(), filesByNods.get(1).size()+filesByNods.get(2).size());
	}
	
	@Test
	/**
	 * files must be allocate to a target nod with consistency according to their name
	 */
	void dispatchFilesByNod_2ExecutorNods_fifoNonDeterministicTest()
	{
		List<String> listIdSource = Arrays.asList("file1", "file2", "file3", "file4", "file5");
		
		int startIndexOfExecutorNods=1;
		int numberOfExecutorNods=2;
				
		Map<Integer, List<Integer>> filesByNods1 = MultiThreading.dispatchFilesByNod(listIdSource, startIndexOfExecutorNods,
				numberOfExecutorNods,true);
		
		// test if both of file list has been allocated to same executor nod when the number of total executor nods is the same
		
		List<String> filesInNods1OrderedByName = filesByNods1.get(1).stream().map(listIdSource::get).toList();
		List<String> filesInNods2OrderedByName= filesByNods1.get(2).stream().map(listIdSource::get).toList();

		assertEquals(Arrays.asList("file1","file3","file5"),filesInNods1OrderedByName);
		assertEquals(Arrays.asList("file2","file4"),filesInNods2OrderedByName);
	}

	public void dispatchFilesByNod_2ExecutorNods_NameHashDeterministicTest()
	{
		List<String> listIdSource = Arrays.asList("file1", "file2", "file3", "file4", "file5");
		
		int startIndexOfExecutorNods=1;
		int numberOfExecutorNods=2;
				
		Map<Integer, List<Integer>> filesByNods1 = MultiThreading.dispatchFilesByNod(listIdSource, startIndexOfExecutorNods,
				numberOfExecutorNods,true);
		
		// test if both of file list has been allocated to same executor nod when the number of total executor nods is the same
		
		List<String> filesInNods1OrderedByName = filesByNods1.get(1).stream().map(listIdSource::get).toList();
		List<String> filesInNods2OrderedByName= filesByNods1.get(2).stream().map(listIdSource::get).toList();

		assertEquals(Arrays.asList("file2","file4"), filesInNods1OrderedByName);
		assertEquals(Arrays.asList("file1","file3","file5"),filesInNods2OrderedByName);
	}
	
}
