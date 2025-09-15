package fr.insee.arc.core.service.global.scalability;

import java.util.List;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;

// sonar cannot detect that the runnables are defined in children classes
@SuppressWarnings("java:S2134")
public class ThreadDispatchOn extends Thread {

	
	private boolean errorInThread = false;

	public boolean isErrorInThread() {
		return errorInThread;
	}

	public void setErrorInThread(boolean errorInThread) {
		this.errorInThread = errorInThread;
	}
	
	public void finish() {
		try {
			this.join();
		} catch (InterruptedException e) {
			this.errorInThread = true;
			this.interrupt();
		}
	}
	
	
	/**
	 * finish a set of threads
	 * report error execption if any of them have failed
	 * @param threadsToJoin
	 * @throws ArcException
	 */
	public static void execute(List<ThreadDispatchOn> threadsToJoin) throws ArcException {

		// start threads
		for (ThreadDispatchOn t : threadsToJoin)
		{
			t.start();
		}

		// finish threads and report
		boolean errorInThreads = false;

		for (ThreadDispatchOn t:threadsToJoin)
		{
			t.finish();
			errorInThreads = errorInThreads || t.isErrorInThread();
		}
	
		if (errorInThreads)
		{
			throw new ArcException(ArcExceptionMessage.MULTITHREADING_DISPATCH_FAILED);
		}
		
	}


}
