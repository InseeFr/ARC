package fr.insee.arc.utils.sqlengine.model;

/**
 * 
 * Produit un couple formé de :<br/>
 * 1. L'idientifiant du thread courant.<br/>
 * 2. Un numéro séquentiel unique pour un thread donné.<br/>
 *
 */
public class TempNameSuffixShort implements IToken
{
//    private final String timestamp;
//    private final String alea;
    private final String name;
    private static ThreadLocal<Integer> rank = ThreadLocal.withInitial(() -> 0);

    public TempNameSuffixShort()
    {
//        this.timestamp = new Long(System.nanoTime()).toString();
//        this.alea = FormatSQL.randomNumber(4);
        this.name = new StringBuilder().append("$tmp$").toString();
    }
    
    @Override
    public String name()
    {
        return this.name;
    }

    public String toString()
    {
        return name();
    }
}
