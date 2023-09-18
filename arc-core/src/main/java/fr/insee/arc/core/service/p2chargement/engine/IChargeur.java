package fr.insee.arc.core.service.p2chargement.engine;

import fr.insee.arc.utils.exception.ArcException;

/*
 * Interface dont vont hériter les chargeurs de fichier
 */
public interface IChargeur {
   
    public void initialisation() throws ArcException;
     
    public void finalisation() throws ArcException;
    
    public void execution() throws ArcException;
    
    public void charger() throws ArcException;

    
    
    
}
