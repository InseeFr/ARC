package fr.insee.arc.core.service.global.scalability;


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

}
