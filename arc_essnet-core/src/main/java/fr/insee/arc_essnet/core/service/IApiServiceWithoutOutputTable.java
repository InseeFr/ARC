package fr.insee.arc_essnet.core.service;

import java.sql.Connection;

/**
 * All service phase classes that dont have previous phase table need to extends
 * that classes.
 * 
 * @author S4lwo8
 *
 */
public interface  IApiServiceWithoutOutputTable extends IPhaseService {


    default void deleteFinalTable(Connection connexion, String tablePilTemp, String tablePrevious,
	    String paramBatch) {
	return;

    }
}
