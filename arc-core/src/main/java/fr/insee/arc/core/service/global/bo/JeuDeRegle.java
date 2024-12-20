package fr.insee.arc.core.service.global.bo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.utils.LoggerHelper;


public class JeuDeRegle {

	private static final Logger LOGGER = LogManager.getLogger(JeuDeRegle.class);

    
    private String idNorme;
    private String periodicite;
    private Date validiteInf;
    private Date validiteSup;
    private String version;

    private List<RegleControleEntity> listRegleControle;

    private String etat;

    public JeuDeRegle(String idNorme, String periodicite, String validiteInf, String validiteSup, String version) throws ParseException {
        this(idNorme, periodicite, new SimpleDateFormat(ArcDateFormat.DATE_FORMAT_CONVERSION.getApplicationFormat()).parse(validiteInf), new SimpleDateFormat(ArcDateFormat.DATE_FORMAT_CONVERSION.getApplicationFormat()).parse(validiteSup), version);
    }

    public JeuDeRegle(String idNorme, String periodicite, Date validiteInf, Date validiteSup, String version) {
        super();
        this.idNorme = idNorme;
        this.periodicite = periodicite;
        this.validiteInf = validiteInf;
        this.validiteSup = validiteSup;
        this.version = version;
        this.listRegleControle = new ArrayList<>();
    }

    // Getter et setter

    public JeuDeRegle() {
    }

    public String getIdNorme() {
        return this.idNorme;
    }

    public void setIdNorme(String idNorme) {
        this.idNorme = idNorme;
    }

    public String getPeriodicite() {
        return this.periodicite;
    }

    public void setPeriodicite(String periodicite) {
        this.periodicite = periodicite;
    }

    public Date getValiditeInf() {
        return this.validiteInf;
    }

    public String getValiditeInfString() {
        return new SimpleDateFormat(ArcDateFormat.DATE_FORMAT_CONVERSION.getApplicationFormat()).format(this.validiteInf);
    }

    public void setValiditeInf(Date validiteInf) {
        this.validiteInf = validiteInf;
    }

    public void setValiditeInfString(String validiteInf, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            java.sql.Date dateSql = new java.sql.Date(formatter.parse(validiteInf).getTime());
            this.validiteInf = dateSql;
        } catch (ParseException e) {
            LoggerHelper.errorAsComment(LOGGER, "JeuDeRegle.setValiditeInfString - the validité sup "+validiteInf+" isn't at date format "+format);
        }
    }

    public Date getValiditeSup() {
        return this.validiteSup;
    }

    public String getValiditeSupString() {
        return new SimpleDateFormat(ArcDateFormat.DATE_FORMAT_CONVERSION.getApplicationFormat()).format(this.validiteSup);
    }

    public void setValiditeSup(Date validiteSup) {
        this.validiteSup = validiteSup;
    }

    public void setValiditeSupString(String validiteSup, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            java.sql.Date dateSql = new java.sql.Date(formatter.parse(validiteSup).getTime());
            this.validiteSup = dateSql;
        } catch (ParseException e) {
            LoggerHelper.errorAsComment(LOGGER, "JeuDeRegle.setValiditeSupString - the validité sup "+validiteSup+" isn't at date format "+format);
        }
    }

    public List<RegleControleEntity> getListRegleControle() {
        return this.listRegleControle;
    }

    public void setListRegleControle(List<RegleControleEntity> listRegleControle) {
        this.listRegleControle = listRegleControle;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "JeuDeRegle [idNorme=" + this.idNorme + ", periodicite=" + this.periodicite + ", validiteInf=" + this.validiteInf + ", validiteSup="
                + this.validiteSup + ", version=" + this.version + "]";
    }

    public ArcPreparedStatementBuilder getSqlEquals() {
    	ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();
    	requete
		.append("id_norme = " + requete.quoteText(this.idNorme))
        .append("\n  AND validite_inf = " + requete.quoteText(this.getValiditeInfString())  + "::date")
        .append("\n  AND validite_sup = " + requete.quoteText(this.getValiditeSupString()) + "::date")
    	.append("\n  AND periodicite = " + requete.quoteText(this.getPeriodicite()))
		.append("\n  AND version = " + requete.quoteText(this.getVersion()));
    	
        return requete;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.idNorme == null) ? 0 : this.idNorme.hashCode());
        result = prime * result + ((this.periodicite == null) ? 0 : this.periodicite.hashCode());
        result = prime * result + ((this.validiteInf == null) ? 0 : this.validiteInf.hashCode());
        result = prime * result + ((this.validiteSup == null) ? 0 : this.validiteSup.hashCode());
        result = prime * result + ((this.version == null) ? 0 : this.version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof JeuDeRegle)) {
            return false;
        }
        JeuDeRegle other = (JeuDeRegle) obj;
        if (this.idNorme == null) {
            if (other.idNorme != null) {
                return false;
            }
        } else if (!this.idNorme.equals(other.idNorme)) {
            return false;
        }
        if (this.periodicite == null) {
            if (other.periodicite != null) {
                return false;
            }
        } else if (!this.periodicite.equals(other.periodicite)) {
            return false;
        }
        if (this.validiteInf == null) {
            if (other.validiteInf != null) {
                return false;
            }
        } else if (!this.validiteInf.equals(other.validiteInf)) {
            return false;
        }
        if (this.validiteSup == null) {
            if (other.validiteSup != null) {
                return false;
            }
        } else if (!this.validiteSup.equals(other.validiteSup)) {
            return false;
        }
        if (this.version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

    public void setEtat(String zeEtat) {
        this.etat = zeEtat;
    }

    /**
     * @return the etat
     */
    public String getEtat() {
        return etat;
    }

}
