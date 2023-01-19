package fr.insee.arc.core.service.thread;

public interface ArcThread<T> {

	
	abstract void configThread(ScalableConnection connexion, int currentIndice, T aApi);

	abstract ScalableConnection getConnexion();

	abstract Thread getT();
	
	abstract void start();

}
