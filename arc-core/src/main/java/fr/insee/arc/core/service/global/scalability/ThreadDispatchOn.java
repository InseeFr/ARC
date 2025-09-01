package fr.insee.arc.core.service.global.scalability;

public class ThreadDispatchOn extends Thread {

	
	private boolean errorInThread = false;

	public boolean isErrorInThread() {
		return errorInThread;
	}

	public void setErrorInThread(boolean errorInThread) {
		this.errorInThread = errorInThread;
	}

}
