package fr.insee.arc.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import fr.insee.arc.core.model.Norme;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.utils.LoggerDispatcher;


/**
 * Reads the file to find the norm and validity.
 * @author S4LWO8
 *
 */
public class FileReader {

    
    // todo : externalize reading rules
    /** Max amount of loops */
    public static final int LOOPS_LIMIT = 1;
    /** Number of lines per loop (OPTI) */
    public static final int READ_LINES_LIMIT = 50;

    private static final Logger LOGGER = Logger.getLogger(FileReader.class);
    private Connection connection;
    private List<Norme> normList;

    /** Returns a query (SELECT)  holding the idsource and some lines from the file.
     * @param idSource of the file
     * @param br opened file reader
     * @param loopNb current step
     * */
    private String readFileAsQuery(String idSource, BufferedReader br, int loopNb) throws Exception {
        LoggerDispatcher.info("** readFileAsQuery **", LOGGER);

        
        StringBuilder query=new StringBuilder();
        int idLine = loopNb * READ_LINES_LIMIT;
        String line = br.readLine();

        boolean start=true;
        while (line != null && idLine < (loopNb + 1) * READ_LINES_LIMIT) {
          if (start)
          {
            query.append("\nSELECT '"+idSource.replace("'", "''")+"'::text as id_source,"+ idLine +"::int as id_ligne,'"+line.replace("'", "''")+"'::text as ligne");
            start=false;
          }
          else
          {
              query.append("\nUNION ALL SELECT '"+idSource.replace("'", "''")+"',"+ idLine +",'"+line.replace("'", "''")+"'");
          }
          
          idLine++;
          if (idLine < (loopNb + 1) * READ_LINES_LIMIT) {
              line = br.readLine();
          }
       }
        return query.toString();

    }

    /** Finds the norm. Returns (by reference) the norm in normOk[] and the validity in validityOk[].
     * @throws IOException
     * @throws Exception if no norm or more than one norm is found */
    public void findFileNormAndValidity(String idSource, InputStream file, Norme[] normOk, String[] validityOk)
            throws Exception {
        try {
            LoggerDispatcher.info("** findFileNormAndValidity **", LOGGER);
            normOk[0] = new Norme();
            validityOk[0] = null;
            int loopNb = 0;
            InputStreamReader isr = new InputStreamReader(file);
            BufferedReader br = new BufferedReader(isr);
            // Loops looking for a norm.
            // As of now, only loops once (thrown exceptions in readFileAsQuery) to prevent
            // losing time reading large files without norm.
            while (normOk[0].getIdNorme() == null && loopNb < LOOPS_LIMIT) {
                findNormAndValidity(normOk, validityOk, readFileAsQuery(idSource, br, loopNb));
                loopNb++;
            }
            br.close();
            isr.close();
            if (normOk[0].getIdNorme() == null) {
                throw (new Exception("Zero norm match the expression"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Returns (by reference) the norm in normOk[] and the validity in validityOk[].
     * @param normOk
     * @param validityOk
     * @param fileAsQuery a query (SELECT)  holding the idsource and some lines from the file.
     * @throws SQLException
     * @throws Exception if no norm or more than one norm is found */
    private void findNormAndValidity(Norme[] normOk, String[] validityOk, String fileAsQuery) throws Exception {
        LoggerDispatcher.info("** findNormAndValidity **", LOGGER);

        StringBuilder query=new StringBuilder();
        query.append("\n WITH alias_table AS ("+fileAsQuery+" )");

        query.append("\n SELECT * FROM (");
        
        for (int i=0;i<normList.size();i++)
        {
            if (i>0)
            {
                query.append("\n UNION ALL ");
            }
            query.append("\n SELECT "+i+"::int as id_norme ");
            query.append("\n , ("+normList.get(i).getDefNorme()+" LIMIT 1)::text as norme ");
            query.append("\n , ("+normList.get(i).getDefValidite()+" LIMIT 1)::text as validite ");
        }
        query.append("\n ) vv ");
        query.append("\n where norme is not null ");
        
        
        ArrayList<ArrayList<String>> result =UtilitaireDao.get("arc").executeRequestWithoutMetadata(this.connection, query);
        if (result.size()>1)
        {
            throw new Exception("More than one norm match the expression");
        } else if (result.isEmpty())
        {
            throw new Exception("Zero norm match the expression");
        }

        normOk[0]=normList.get(Integer.parseInt(result.get(0).get(0)));
        validityOk[0]=result.get(0).get(2);
    }
    
    
    
    /**
     * @return the connexion
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * @param connexion
     *            the connexion to set
     */
    public void setConnection(Connection connexion) {
        this.connection = connexion;
    }

    /** Get the potential norms to check the file against.
     * @return the listNorme
     */
    public List<Norme> getNormList() {
        return normList;
    }

    /** Set the potential norms to check the file against.
     * @param normList
     *            the normList to set
     */
    public void setNormList(List<Norme> normList) {
        this.normList = normList;
    }

}
