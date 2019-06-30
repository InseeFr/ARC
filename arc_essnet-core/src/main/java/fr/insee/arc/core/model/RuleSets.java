package fr.insee.arc.core.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import fr.insee.arc.core.util.EDateFormat;
import fr.insee.arc.utils.utils.LoggerDispatcher;

/**
 * 
 * @author S4lwo8
 *
 */
public class RuleSets {

    private static final Logger LOGGER = Logger.getLogger(RuleSets.class);

    private String idNorme;
    private String periodicite;
    private Date validiteInf;
    private Date validiteSup;
    private String version;

    private List<RegleControleEntity> listRegleControle;

    private String etat;

    public RuleSets(String idNorme, String periodicite, String validiteInf, String validiteSup, String version)
	    throws ParseException {
	this(idNorme, periodicite, new SimpleDateFormat(EDateFormat.SIMPLE_DATE_FORMAT_IHM.getValue()).parse(validiteInf),
		new SimpleDateFormat(EDateFormat.SIMPLE_DATE_FORMAT_IHM.getValue()).parse(validiteSup), version);
    }

    public RuleSets(String idNorme, String periodicite, Date validiteInf, Date validiteSup, String version) {
	super();
	this.idNorme = idNorme;
	this.periodicite = periodicite;
	this.validiteInf = validiteInf;
	this.validiteSup = validiteSup;
	this.version = version;
	this.listRegleControle = new ArrayList<>();
    }

    // Getter et setter

    public RuleSets() {
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
	SimpleDateFormat formatter = new SimpleDateFormat(EDateFormat.SIMPLE_DATE_FORMAT_IHM.getValue());
	return formatter.format(this.validiteInf);
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
	    LoggerDispatcher.error("Error when parsing the validity inf date",e, LOGGER);
	}
    }

    public Date getValiditeSup() {
	return this.validiteSup;
    }

    public String getValiditeSupString() {
	SimpleDateFormat formatter = new SimpleDateFormat(EDateFormat.SIMPLE_DATE_FORMAT_IHM.getValue());
	return formatter.format(this.validiteSup);
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
	    LoggerDispatcher.error("Error when parsing the validity sup date",e, LOGGER);
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
	return "JeuDeRegle [idNorme=" + this.idNorme + ", periodicite=" + this.periodicite + ", validiteInf="
		+ this.validiteInf + ", validiteSup=" + this.validiteSup + ", version=" + this.version + "]";
    }

    public String getSqlEquals() {
	return new StringBuilder("id_norme = '" + this.idNorme + "'")//
		.append("\n  AND validite_inf = '" + this.getValiditeInfString() + "'")//
		.append("\n  AND validite_sup = '" + this.getValiditeSupString() + "'")//
		.append("\n  AND periodicite = '" + this.getPeriodicite() + "'")//
		.append("\n  AND version = '" + this.getVersion() + "'")//
		.toString();
    }

    public String getSqlEquals(String alias) {
	return new StringBuilder(alias + ".id_norme = '" + this.idNorme + "'")//
		.append("\n  AND " + alias + ".validite_inf = '" + this.getValiditeInfString() + "'")//
		.append("\n  AND " + alias + ".validite_sup = '" + this.getValiditeSupString() + "'")//
		.append("\n  AND " + alias + ".periodicite = '" + this.getPeriodicite() + "'")//
		.append("\n  AND " + alias + ".version = '" + this.getVersion() + "'")//
		.toString();
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
	if (!(obj instanceof RuleSets)) {
	    return false;
	}
	RuleSets other = (RuleSets) obj;
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
