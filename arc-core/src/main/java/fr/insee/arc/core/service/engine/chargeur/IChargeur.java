package fr.insee.arc.core.service.engine.chargeur;

import fr.insee.arc.utils.exception.ArcException;

/*
 * Interface dont vont hériter les chargeurs de fichier
 */
public interface IChargeur {
   
    public void initialisation();
     
    public void finalisation();
    
    public void execution() throws ArcException;
    
    public void charger() throws ArcException;

    
    
    
}
