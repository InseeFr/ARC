package fr.insee.arc.core.service.global.thread;

public class ThreadConstant {
	
	private ThreadConstant() {
		throw new IllegalStateException("Utility class");
	}
	
		// anti-spam delay when thread chain error
	public static final int PREVENT_ERROR_SPAM_DELAY = 100;

}
