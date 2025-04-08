package fr.insee.arc.core.service.global.thread;

import fr.insee.arc.core.service.global.scalability.ScalableConnection;

public interface IThread2<T> {

	
	abstract void configThread(ScalableConnection connexion, int currentIndice, T aApi);

	abstract ScalableConnection getConnexion();

	abstract Thread getT();
	
	abstract void start();

}
