package fr.insee.arc.core.service;

public interface IApiService {

    public boolean initialize() ;
    public void finalizePhase() throws Exception;
    public abstract void process() throws Exception;
    
    

}
