package fr.insee.arc.core.service.thread;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

public class MultiThreadingTest {

	@Test
	public void dispatchFilesByNod_0ExecutorNods_allAllocatedTest()
	{
		List<String> listIdSource = Arrays.asList("file1", "file2");
		int startIndexOfExecutorNods=0;
		int numberOfExecutorNods=0;
		
		Map<Integer, List<Integer>> filesByNods = MultiThreading.dispatchFilesByNodId(listIdSource, startIndexOfExecutorNods,
				numberOfExecutorNods);
		
		// test if all files had been allocated to nod 0
		assertEquals(listIdSource.size(), filesByNods.get(0).size());
	}
	
	@Test
	public void dispatchFilesByNod_2ExecutorNods_allAllocatedTest()
	{
		List<String> listIdSource = Arrays.asList("file1", "file2", "file3", "file4", "file5");
		int startIndexOfExecutorNods=1;
		int numberOfExecutorNods=2;
		
		Map<Integer, List<Integer>> filesByNods = MultiThreading.dispatchFilesByNodId(listIdSource, startIndexOfExecutorNods,
				numberOfExecutorNods);
		
		// test if all files had been allocated to executor nods 1 and 2
		assertEquals(listIdSource.size(), filesByNods.get(1).size()+filesByNods.get(2).size());
	}
	
	@Test
	/**
	 * files must be allocate to a target nod with consistency according to their name
	 */
	public void dispatchFilesByNod_2ExecutorNods_consistencyTest()
	{
		List<String> listIdSource1 = Arrays.asList("file1", "file2", "file3", "file4", "file5");
		List<String> listIdSource2 = Arrays.asList("file2", "file4", "file3", "file5", "file1");
		
		int startIndexOfExecutorNods=1;
		int numberOfExecutorNods=2;
				
		Map<Integer, List<Integer>> filesByNods1 = MultiThreading.dispatchFilesByNodId(listIdSource1, startIndexOfExecutorNods,
				numberOfExecutorNods);
		Map<Integer, List<Integer>> filesByNods2 = MultiThreading.dispatchFilesByNodId(listIdSource2, startIndexOfExecutorNods,
				numberOfExecutorNods);
		
		// test if both of file list has been allocated to same executor nod when the number of total executor nods is the same
		
		List<String> filesInNods1OrderedByName = filesByNods1.get(1).stream().map(x->listIdSource1.get(x)).collect(Collectors.toList());
		List<String> filesInNods2OrderedByName= filesByNods2.get(1).stream().map(x->listIdSource2.get(x)).collect(Collectors.toList());
		
		Collections.sort(filesInNods1OrderedByName);
		Collections.sort(filesInNods2OrderedByName);
		
		assertEquals(filesInNods1OrderedByName,filesInNods2OrderedByName);
	}

}
