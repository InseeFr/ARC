package fr.insee.arc.core.service.global.thread;

import fr.insee.arc.core.service.global.scalability.ScalableConnection;

public interface IThread<T> {
	
	abstract void configThread(ScalableConnection connexion, int currentIndice, T aApi);

	abstract Thread getT();
	
	abstract void start();

}
