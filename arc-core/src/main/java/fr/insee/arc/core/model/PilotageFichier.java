package fr.insee.arc.core.model;

import java.text.ParseException;
import java.util.Date;

public class PilotageFichier {

    private String idSource;
    private JeuDeRegle jeuDeRegle;

    public PilotageFichier(String idSource, JeuDeRegle jeuDeRegle) {
        super();
        this.idSource = idSource;
        this.jeuDeRegle = jeuDeRegle;
    }

    /**
     *
     * @param idSource
     * @param idNorme
     * @param periodicite
     * @param validiteInf
     * @param validiteSup
     * @param version
     * @throws ParseException
     */
    public PilotageFichier(String idSource, String idNorme, String periodicite, String validiteInf, String validiteSup, String version)
            throws ParseException {
        this(idSource, new JeuDeRegle(idNorme, periodicite, validiteInf, validiteSup, version));
    }

    /**
     *
     * @param idSource
     * @param idNorme
     * @param periodicite
     * @param validiteInf
     * @param validiteSup
     * @param version
     */
    public PilotageFichier(String idSource, String idNorme, String periodicite, Date validiteInf, Date validiteSup, String version) {
        this(idSource, new JeuDeRegle(idNorme, periodicite, validiteInf, validiteSup, version));
    }

    /**
     * @return the idSource
     */
    public String getIdSource() {
        return this.idSource;
    }

    /**
     * @return the jeuDeRegle
     */
    public JeuDeRegle getJeuDeRegle() {
        return this.jeuDeRegle;
    }

}
