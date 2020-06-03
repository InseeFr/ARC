package fr.insee.arc.core.service.engine.chargeur;


/*
 * Interface dont vont hériter les chargeurs de fichier
 */
public interface IChargeur {
   
    public void initialisation();
     
    public void finalisation();
    
    public void excecution() throws Exception;
    
    public void charger() throws Exception;

    
    
    
}
