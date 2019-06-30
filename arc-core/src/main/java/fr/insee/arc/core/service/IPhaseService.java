package fr.insee.arc.core.service;

import java.sql.Connection;

public interface IPhaseService {

    
    public boolean checkTodo(String tablePil, String phaseAncien, String phaseNouveau) ;
    
    public void register(Connection connexion, String phase, String tablePil, String tablePilTemp, Integer nbEnr);
    
    public void deleteFinalTable(Connection connexion, String tablePilTemp, String tablePrevious, String paramBatch);
    
}
