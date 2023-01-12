package fr.insee.arc.core.service.thread;

import java.sql.Connection;

public interface ArcThread<T> {

	
	abstract void configThread(Connection connexion, int currentIndice, T aApi);

	abstract Connection getConnexion();

	abstract Thread getT();
	
	abstract void start();

}
