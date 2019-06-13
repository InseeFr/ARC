package fr.insee.arc_essnet.core.service;

import java.sql.Connection;

import fr.insee.arc_essnet.core.service.thread.AbstractThreadService;

/**
 * All service phase classes that  have previous phase table need to extends that classes.
 * 
 * @author S4lwo8
 *
 */

public abstract interface IApiServiceWithOutputTable extends IPhaseService{

    /**
     * FIXME
     */
    public default void deleteFinalTable(Connection connexion, String tablePilTemp, String tablePrevious,
	    String paramBatch) {
	 AbstractThreadService.deleteTodo(connexion, tablePilTemp, tablePrevious, paramBatch);

    }


}
