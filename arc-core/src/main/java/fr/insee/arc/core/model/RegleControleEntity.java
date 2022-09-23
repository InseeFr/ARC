package fr.insee.arc.core.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.insee.arc.utils.dao.AbstractEntity;
import fr.insee.arc.utils.format.Format;

public class RegleControleEntity extends AbstractEntity {

    private static final String colIdNorme = "id_norme";
    private static final String colPeriodicite = "periodicite";
    private static final String colValiditeInf = "validite_inf";
    private static final String colValiditeSup = "validite_sup";
    private static final String colVersion = "version";
    private static final String colIdClasse = "id_classe";
    private static final String colRubriquePere = "rubrique_pere";
    private static final String colRubriqueFils = "rubrique_fils";
    private static final String colBorneInf = "borne_inf";
    private static final String colBorneSup = "borne_sup";
    private static final String colCondition = "condition";
    private static final String colPreAction = "pre_action";
    private static final String colIdRegle = "id_regle";
    private static final String colTodo = "todo";
    private static final String colCommentaire = "commentaire";
    private static final String colBlockingThreshold = "blocking_threshold";
    private static final String colErrorRowProcessing = "error_row_processing";

    private static final Set<String> colNames = new HashSet<String>() {
        /**
         *
         */
        private static final long serialVersionUID = 3677223986776708059L;

        {
            add(colIdNorme);
            add(colPeriodicite);
            add(colValiditeInf);
            add(colValiditeSup);
            add(colVersion);
            add(colIdClasse);
            add(colRubriquePere);
            add(colRubriqueFils);
            add(colBorneInf);
            add(colBorneSup);
            add(colCondition);
            add(colPreAction);
            add(colIdRegle);
            add(colTodo);
            add(colCommentaire);
            add(colBlockingThreshold);
            add(colErrorRowProcessing);

        }
    };

    public RegleControleEntity() {
        super();
    }

    public RegleControleEntity(List<String> someNames, List<String> someValues) {
        super(someNames, someValues);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.getBorneInf() == null) ? 0 : this.getBorneInf().hashCode());
        result = prime * result + ((this.getBorneSup() == null) ? 0 : this.getBorneSup().hashCode());
        result = prime * result + ((this.getCommentaire() == null) ? 0 : this.getCommentaire().hashCode());
        result = prime * result + ((this.getCondition() == null) ? 0 : this.getCondition().hashCode());
        result = prime * result + ((this.getIdClasse() == null) ? 0 : this.getIdClasse().hashCode());
        result = prime * result + ((this.getIdRegle() == null) ? 0 : this.getIdRegle().hashCode());
        result = prime * result + ((this.getPreAction() == null) ? 0 : this.getPreAction().hashCode());
        result = prime * result + ((this.getRubriqueFils() == null) ? 0 : this.getRubriqueFils().hashCode());
        result = prime * result + ((this.getRubriquePere() == null) ? 0 : this.getRubriquePere().hashCode());
        result = prime * result + ((this.getBlockingThreshold() == null) ? 0 : this.getBlockingThreshold().hashCode());
        result = prime * result + ((this.getErrorRowProcessing() == null) ? 0 : this.getErrorRowProcessing().hashCode());

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
        if (!(obj instanceof RegleControleEntity)) {
            return false;
        }
        RegleControleEntity other = (RegleControleEntity) obj;
        if (this.getBorneInf() == null) {
            if (other.getBorneInf() != null) {
                return false;
            }
        } else if (!this.getBorneInf().equals(other.getBorneInf())) {
            return false;
        }
        if (this.getBorneSup() == null) {
            if (other.getBorneSup() != null) {
                return false;
            }
        } else if (!this.getBorneSup().equals(other.getBorneSup())) {
            return false;
        }
        if (this.getCommentaire() == null) {
            if (other.getCommentaire() != null) {
                return false;
            }
        } else if (!this.getCommentaire().equals(other.getCommentaire())) {
            return false;
        }
        if (this.getCondition() == null) {
            if (other.getCondition() != null) {
                return false;
            }
        } else if (!this.getCondition().equals(other.getCondition())) {
            return false;
        }
        if (this.getIdClasse() == null) {
            if (other.getIdClasse() != null) {
                return false;
            }
        } else if (!this.getIdClasse().equals(other.getIdClasse())) {
            return false;
        }
        if (this.getIdRegle() == null) {
            if (other.getIdRegle() != null) {
                return false;
            }
        } else if (!this.getIdRegle().equals(other.getIdRegle())) {
            return false;
        }
        if (this.getPreAction() == null) {
            if (other.getPreAction() != null) {
                return false;
            }
        } else if (!this.getPreAction().equals(other.getPreAction())) {
            return false;
        }
        if (this.getRubriqueFils() == null) {
            if (other.getRubriqueFils() != null) {
                return false;
            }
        } else if (!this.getRubriqueFils().equals(other.getRubriqueFils())) {
            return false;
        }
        if (this.getRubriquePere() == null) {
            if (other.getRubriquePere() != null) {
                return false;
            }
        } else if (!this.getRubriquePere().equals(other.getRubriquePere())) {
            return false;
        }
        if (this.getBlockingThreshold() == null) {
            if (other.getBlockingThreshold() != null) {
                return false;
            }
        } else if (!this.getBlockingThreshold().equals(other.getBlockingThreshold())) {
            return false;
        }
        if (this.getErrorRowProcessing() == null) {
            if (other.getErrorRowProcessing() != null) {
                return false;
            }
        } else if (!this.getErrorRowProcessing().equals(other.getErrorRowProcessing())) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return
     */
    public String getIdNorme() {
        return this.getMap().get(colIdNorme);
    }

    /**
     * @param idNorme
     *            the idNorme to set
     */
    public void setIdNorme(String idNorme) {
        this.getMap().put(colIdNorme, idNorme);
    }

    /**
     *
     * @return
     */
    public String getPeriodicite() {
        return this.getMap().get(colPeriodicite);
    }

    /**
     * @param periodicite
     *            the periodicite to set
     */
    public void setPeriodicite(String periodicite) {
        this.getMap().put(colPeriodicite, periodicite);
    }

    /**
     *
     * @return
     */
    public String getValiditeInf() {
        return this.getMap().get(colValiditeInf);
    }

    /**
     * @param validiteInf
     *            the validiteInf to set
     */
    public void setValiditeInf(String validiteInf) {
        this.getMap().put(colValiditeInf, validiteInf);
    }

    /**
     *
     * @return
     */
    public String getValiditeSup() {
        return this.getMap().get(colValiditeSup);
    }

    /**
     * @param validiteSup
     *            the validiteSup to set
     */
    public void setValiditeSup(String validiteSup) {
        this.getMap().put(colValiditeSup, validiteSup);
    }

    /**
     *
     * @return
     */
    public String getVersion() {
        return this.getMap().get(colVersion);
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(String version) {
        this.getMap().put(colVersion, version);
    }

    /**
     *
     * @return
     */
    public String getIdClasse() {
        return this.getMap().get(colIdClasse);
    }

    /**
     * @param idClasse
     *            the idClasse to set
     */
    public void setIdClasse(String idClasse) {
        this.getMap().put(colIdClasse, idClasse);
    }

    /**
     *
     * @return
     */
    public String getRubriquePere() {
        return Format.toUpperCase(this.getMap().get(colRubriquePere));
    }

    /**
     * @param rubriquePere
     *            the rubriquePere to set
     */
    public void setRubriquePere(String rubriquePere) {
        this.getMap().put(colRubriquePere, rubriquePere);
    }

    /**
     *
     * @return
     */
    public String getRubriqueFils() {
        return Format.toUpperCase(this.getMap().get(colRubriqueFils));
    }

    /**
     * @param rubriqueFils
     *            the rubriqueFils to set
     */
    public void setRubriqueFils(String rubriqueFils) {
        this.getMap().put(colRubriqueFils, rubriqueFils);
    }

    /**
     *
     * @return
     */
    public String getBorneInf() {
        return this.getMap().get(colBorneInf);
    }

    /**
     * @param borneInf
     *            the borneInf to set
     */
    public void setBorneInf(String borneInf) {
        this.getMap().put(colBorneInf, borneInf);
    }

    /**
     *
     * @return
     */
    public String getBorneSup() {
        return this.getMap().get(colBorneSup);
    }

    /**
     * @param borneSup
     *            the borneSup to set
     */
    public void setBorneSup(String borneSup) {
        this.getMap().put(colBorneSup, borneSup);
    }

    /**
     *
     * @return
     */
    public String getCondition() {
        return this.getMap().get(colCondition);
    }

    /**
     * @param condition
     *            the condition to set
     */
    public void setCondition(String condition) {
        this.getMap().put(colCondition, condition);
    }

    /**
     *
     * @return
     */
    public String getPreAction() {
        return this.getMap().get(colPreAction);
    }

    /**
     * @param preAction
     *            the preAction to set
     */
    public void setPreAction(String preAction) {
        this.getMap().put(colPreAction, preAction);
    }

    /**
     *
     * @return
     */
    public String getIdRegle() {
        return this.getMap().get(colIdRegle);
    }

    /**
     * @param idRegle
     *            the idRegle to set
     */
    public void setIdRegle(String idRegle) {
        this.getMap().put(colIdRegle, idRegle);
    }

    /**
     *
     * @return
     */
    public String getTodo() {
        return this.getMap().get(colTodo);
    }

    /**
     * @param todo
     *            the todo to set
     */
    public void setTodo(String todo) {
        this.getMap().put(colTodo, todo);
    }

    /**
     *
     * @return
     */
    public String getCommentaire() {
        return this.getMap().get(colCommentaire);
    }

    /**
     * @param commentaire
     *            the commentaire to set
     */
    public void setCommentaire(String commentaire) {
        this.getMap().put(colCommentaire, commentaire);
    }

    /**
    *
    * @return
    */
   public String getBlockingThreshold() {
       return this.getMap().get(colBlockingThreshold);
   }

   /**
    * @param commentaire
    *            the commentaire to set
    */
   public void setBlockingThreshold(String blockingThreshold) {
       this.getMap().put(colBlockingThreshold, blockingThreshold);
   }
   
   /**
   *
   * @return
   */
  public String getErrorRowProcessing() {
      return this.getMap().get(colErrorRowProcessing);
  }

  /**
   * @param commentaire
   *            the commentaire to set
   */
  public void setErrorRowProcessing(String errorRowProcessing) {
      this.getMap().put(colErrorRowProcessing, errorRowProcessing);
  }
   
   
    
    @Override
    public Set<String> colNames() {
        return colNames;
    }

}
