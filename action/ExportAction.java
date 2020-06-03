package fr.insee.arc_composite.web.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.tools.ant.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import fr.insee.arc_composite.core.model.IDbConstant;
import fr.insee.config.InseeConfig;
import fr.insee.siera.core.dao.UtilitaireDao;
import fr.insee.siera.core.structure.GenericBean;
import fr.insee.siera.core.util.FormatSQL;
import fr.insee.siera.textutils.IConstanteCaractere;
import fr.insee.siera.webutils.VObject;

@Component
@Results({ @Result(name = "success", location = "/jsp/gererExport.jsp"), @Result(name = "index", location = "/jsp/gererExport.jsp") })
public class ExportAction implements SessionAware, IDbConstant, IConstanteCaractere {
    @Override
    public void setSession(Map<String, Object> session) {
        this.viewExport.setMessage("");
    }

    public String export;
    public String env;
    private final String SESSION_EXPORT_ENV="EXPORT_ENV";
    
    public ArrayList<String> listEnv=new ArrayList<String>(Arrays.asList("ARC_BAS1","ARC_BAS2","ARC_BAS3","ARC_BAS4","ARC_BAS5","ARC_BAS6","ARC_BAS7","ARC_BAS8","ARC_PROD"));

    

    private static final Logger logger = Logger.getLogger(ExportAction.class);
    @Autowired
    @Qualifier("viewExport")
    VObject viewExport;

    @Autowired
    @Qualifier("viewFileExport")
    VObject viewFileExport;

    // pour charger un fichier CSV
        private String scope;

    public String sessionSyncronize() {
        this.viewExport.setActivation(this.scope);
        this.viewFileExport.setActivation(this.scope);
        
        Boolean defaultWhenNoScope = true;

        if (this.viewExport.getIsScoped()) {
            initializeExport();
            defaultWhenNoScope = false;
        }

        if (this.viewFileExport.getIsScoped()) {
            initializeFileExport();
            defaultWhenNoScope = false;
        }

        
        if (defaultWhenNoScope) {
            System.out.println("default");

            initializeExport();
            this.viewExport.setIsActive(true);
            this.viewExport.setIsScoped(true);
            
            initializeFileExport();
            this.viewFileExport.setIsActive(true);
            this.viewFileExport.setIsScoped(true);
        }
        return "success";

    }

    // private SessionMap session;
    // visual des Files
    public void initializeExport() {
		HttpSession session=ServletActionContext.getRequest().getSession();
    	
    	if (env==null)
    	{
            if (session.getAttribute(SESSION_EXPORT_ENV)==null)
            {
            	setEnv("ARC_BAS2");
            	session.setAttribute(SESSION_EXPORT_ENV,"ARC_BAS2");
            }
            else
            {
            	setEnv((String) session.getAttribute(SESSION_EXPORT_ENV));
            }
    	}
    	else
    	{
    		session.setAttribute(SESSION_EXPORT_ENV,env);
    	}
    	
        System.out.println("/* initializeExport */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();

        // création de la table d'export si elle n'existe pas
        StringBuilder query=new StringBuilder();
        query.append("CREATE SCHEMA IF NOT EXISTS "+this.env+"; " );
        query.append("\n CREATE TABLE IF NOT EXISTS "+this.env+".export"+" " );
        query.append("\n (file_name text, table_to_export text, nomenclature_export text, filter_table text, columns_array_header text, columns_array_value text, etat text); ");
        
        query.append(FormatSQL.tryQuery("ALTER TABLE "+this.env+".export add nulls text;"));
        query.append(FormatSQL.tryQuery("ALTER TABLE "+this.env+".export add headers text;"));
        query.append(FormatSQL.tryQuery("ALTER TABLE "+this.env+".export add order_table text;"));
        query.append(FormatSQL.tryQuery("ALTER TABLE "+this.env+".export add zip text;"));

        try {
			UtilitaireDao.get("arc").executeRequest(null, query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        this.viewExport.initialize("SELECT file_name, zip, table_to_export, headers, nulls, nomenclature_export, filter_table, order_table, columns_array_header, columns_array_value, etat  from "+this.env+".export", this.env+".export", defaultInputFields);

    }

    @Action(value = "/selectExport")
    public String selectExport() {
        return sessionSyncronize();
    }

    @Action(value = "/addExport")
    public String addExport() {
        this.viewExport.insert();
        return sessionSyncronize();
    }

    @Action(value = "/deleteExport")
    public String deleteExport() {
         this.viewExport.delete();
        return sessionSyncronize();
    }

    @Action(value = "/updateExport")
    public String updateExport() {
        this.viewExport.update();
        return sessionSyncronize();
    }

    @Action(value = "/sortExport")
    public String sortExport() {
        this.viewExport.sort();
        return sessionSyncronize();
    }

    @Action(value = "/startExport")
    public String startExport() throws Exception {
        
    	String fileSelected="";
    	if (!viewExport.mapContentSelected().isEmpty())
    	{
    		fileSelected="WHERE file_name IN ";
    		fileSelected+="('"+String.join("','", viewExport.mapContentSelected().get("file_name"))+"')";
    	}
    	
    	
    	// Requeter les export à réaliser
    	HashMap<String,ArrayList<String>> h=new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
				"select * from "+this.env+".export"+" "+fileSelected))
				.mapContent();

    	ArrayList<String> fileName=h.get("file_name");
    	ArrayList<String> zip=h.get("zip");
    	ArrayList<String> tablesToExport=h.get("table_to_export");
    	ArrayList<String> headers=h.get("headers");
    	ArrayList<String> nulls=h.get("nulls");
    	ArrayList<String> filterTable=h.get("filter_table");
    	ArrayList<String> orderTable=h.get("order_table");
    	ArrayList<String> howToExport=h.get("nomenclature_export");
    	ArrayList<String> headersToScan=h.get("columns_array_header");
    	ArrayList<String> valuesToScan=h.get("columns_array_value");

    	
    	System.out.println(nulls);
    	
    	// Initialiser le répertoire de sortie 	
    	String dirOut=initExportDir();
    	
    	
    	// itérer sur les exports à réaliser
    	for (int n=0;n<tablesToExport.size();n++)
    	{
    		UtilitaireDao.get("arc").executeRequest(null,"UPDATE "+this.env+".export set etat='EN COURS' where file_name='"+fileName.get(n)+"' ");

    		File fOut;
    		if (!StringUtils.isEmpty(zip.get(n)))
    		{
    			fOut=new File(dirOut+ File.separator +fileName.get(n)+".zip");
    		}
    		else
    		{
        		fOut=new File(dirOut+ File.separator +fileName.get(n));
    		}
    		FileOutputStream fw = new FileOutputStream(fOut);
   		
    		BufferedWriter bw;
    		ZipOutputStream zos=null;
    		
    		if (!StringUtils.isEmpty(zip.get(n)))
    		{
	    		zos = new ZipOutputStream(fw);
	    		ZipEntry ze = new ZipEntry(fileName.get(n));
	    		zos.putNextEntry(ze);
	   			bw = new BufferedWriter(new OutputStreamWriter(zos,"UTF-8"));
    		}
    		else
    		{
	   			bw = new BufferedWriter(new OutputStreamWriter(fw,"UTF-8"));
    		}
    		
    		
    		HashMap<String,Integer> pos= new HashMap<String,Integer>();
    		ArrayList<String> headerLine=new ArrayList<String>();
    		
    		// lire la table how to export pour voir comment on va s'y prendre
    		// L'objectif est de créer une hashmap de correspondance entre la variable et la position
    		h=new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
    				"SELECT lower(varbdd) as varbdd, pos::int-1 as pos, max(pos::int) over() as maxp FROM arc."+howToExport.get(n)+" order by pos::int "))
    				.mapContent();
    		
    		
    		for (int i=0;i<h.get("varbdd").size();i++)
    		{
    			pos.put(h.get("varbdd").get(i), Integer.parseInt(h.get("pos").get(i)));
    			headerLine.add(h.get("varbdd").get(i));
    		}
    		
    		// write header line if required
	    	if (!StringUtils.isEmpty(headers.get(n)))	
	    	{
		        for (String o:headerLine)
		        {
	    	        bw.write(o+";");   	        	
		        }
		        bw.write("\n");
	    	}
	    	
	    		    	
    		int maxPos=Integer.parseInt(h.get("maxp").get(0));
    		
    	
    		Connection c=UtilitaireDao.get("arc").getDriverConnexion();
    		c.setAutoCommit(false);
    		// if the 
    	    System.out.println("SELECT * FROM "+this.env+"."+tablesToExport.get(n)+" WHERE "+(StringUtils.isEmpty(filterTable.get(n))?"true":filterTable.get(n))+" "+(StringUtils.isEmpty(orderTable.get(n))?"":"ORDER BY "+orderTable.get(n)+" "));
    	    Statement stmt = c.createStatement();
    	    stmt.setFetchSize(5000);
    		ResultSet res=stmt.executeQuery("SELECT * FROM "+this.env+"."+tablesToExport.get(n)+" WHERE "+(StringUtils.isEmpty(filterTable.get(n))?"true":filterTable.get(n))+" "+(StringUtils.isEmpty(orderTable.get(n))?"":"ORDER BY "+orderTable.get(n)+" "));
            ResultSetMetaData rsmd=res.getMetaData();


            ArrayList<String> output;
            String[] tabH;
            String[] tabV;
            String colName;
            while (res.next())
            {
    	        // reinitialiser l'arraylist de sortie
    			output=new ArrayList<String>();
    			for (int k=0;k<maxPos;k++)
    			{
    				output.add("");
    			}
    			
    	        boolean todo=false;
    	        tabH=null;
    	        tabV=null;
    	        for (int i = 1; i <= rsmd.getColumnCount(); i++)
    	        {
    	        	colName=rsmd.getColumnLabel(i).toLowerCase();
    	        	
    	        	todo=true;
    	        	// cas ou on est dans un tableau
    	    			if (todo && colName.equals(headersToScan.get(n)))
    	    			{
    	    				todo=false;
    	    				tabH=(String[]) res.getArray(i).getArray();
    	    			}
    	    			if (todo && colName.equals(valuesToScan.get(n)))
    	    			{
    	    				todo=false;
    	    				tabV=(String[]) res.getArray(i).getArray();
    	    			}
    	    			if (todo)
    	    			{
    	    				todo=false;
    	    				if (pos.get(colName)!=null)
    	    				{
    	    					// if nulls value musn't be quoted as "null" and element is null then don't write
    	    					if (!( StringUtils.isEmpty(nulls.get(n)) && StringUtils.isEmpty(res.getString(i)) ))
    	    					{
    	    						output.set(pos.get(colName), res.getString(i));
    	    					}
    	    				}
    	    			}	
    	        }
    	        
    	        // traitement des variables tableaux
    	        if (tabH!=null && tabV!=null)
    	        {
    	        	for (int k=0;k<tabH.length;k++)
    	        	{
    	        		if (pos.get(tabH[k].toLowerCase())!=null)
    	        		{
	    					// if nulls value musn't be quoted as "null" and element is null then don't write
	    					if (!(StringUtils.isEmpty(nulls.get(n)) && StringUtils.isEmpty(tabV[k])))
	    					{
	    						output.set(pos.get(tabH[k].toLowerCase()), tabV[k]);
	    					}
    	        		}
    	        	}
    	        }
    	        
    	        for (String o:output)
    	        {
        	        bw.write(o+";");   	        	
    	        }
    	        bw.write("\n");
    	    }
            c.close();
            bw.flush();
            fw.flush();
            if (!StringUtils.isEmpty(zip.get(n)))
    		{
	            zos.flush();
	            zos.flush();
	            zos.closeEntry();
	            zos.close();
    		}    
            bw.close();
            fw.close();
            
            UtilitaireDao.get("arc").executeRequest(null,"UPDATE "+this.env+".export set etat=to_char(current_timestamp,'YYYY-MM-DD HH24:MI:SS') where file_name='"+fileName.get(n)+"' ");
    	}
    	
    	
        return sessionSyncronize();
    }

	public String initExportDir()
	{
		String repertoire = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire");
		String envDir = this.env.replace(".", "_").toUpperCase();
		String dirOut = repertoire + envDir + File.separator + "EXPORT";
		File f = new File(dirOut);
	
		if (!f.exists()) {
		    f.mkdir();
		}
		return dirOut;
	}
    
    // private SessionMap session;
    // visual des Files
    public void initializeFileExport() {
        System.out.println("/* initializeFileExport */");
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();

        String dirOut=initExportDir();

        ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(dirOut, this.viewFileExport.mapFilterFields());

        this.viewFileExport.initializeByList(listeFichier, defaultInputFields);

    }
    @Action(value = "/selectFileExport")
    public String selectFileExport() {
        System.out.println("selectFileExport " + this.scope);
        return sessionSyncronize();
    }

    @Action(value = "/seeFileExport")
    public String seeFileExport() {
        System.out.println("seeFileExport " + this.scope);

//        HashMap<String,ArrayList<String>> m=this.viewFileExport.mapContentSelected();
//        if (!m.isEmpty())
//        {
//            if(m.get("isdirectory").get(0).equals("true"))
//            {
//                this.FileExport=this.FileExport+m.get("filename").get(0)+File.separator;
//            }
//        }


        return sessionSyncronize();
    }

    @Action(value = "/sortFileExport")
    public String sortFileExport() {
        this.viewFileExport.sort();
        return sessionSyncronize();
    }

    @Action(value = "/deleteFileExport")
    public String deleteFileExport() {
    	String dirOut=initExportDir();
    	HashMap<String, ArrayList<String>> selection = this.viewFileExport.mapContentSelected();
    	System.out.println(selection);
    	if (!selection.isEmpty())
    	{
    		for (String s:selection.get("filename"))
    		{
    			FileUtils.delete(new File(dirOut + File.separator + s));
    		}
    	}
        return sessionSyncronize();
    }
    
    @Action(value = "/updateFileExport")
    public String updateFileExport() {
    	String dirOut=initExportDir();

    	 HashMap<String,ArrayList<String>> m=this.viewFileExport.mapContentBeforeUpdate();
         HashMap<String,ArrayList<String>> n=this.viewFileExport.mapContentAfterUpdate();

         if (!m.isEmpty())
         {
             for (int i=0; i<m.get("filename").size();i++)
             {
               File fileIn = new File(dirOut + File.separator + m.get("filename").get(i));
               File fileOut = new File(dirOut + File.separator + n.get("filename").get(i));
               fileIn.renameTo(fileOut);
             }
         }
       return sessionSyncronize();
    }
    
    @Action(value = "/downloadFileExport")
    public String downloadFileExport() {
    	HashMap<String, ArrayList<String>> selection = this.viewFileExport.mapContentSelected();
    	System.out.println(selection);
    	if (!selection.isEmpty())
    	{
    		StringBuilder requete=new StringBuilder();
    		boolean first=true;
    		for (String s:selection.get("filename"))
    		{
    			if (first)
    			{
    				first=false;
    			}
    			else
    			{
    				requete.append("\n UNION ALL ");
    			}
    			requete.append("SELECT '"+s+"' as nom_fichier ");
    		}
    		
        	String repertoire = InseeConfig.getConfig().getString("fr.insee.arc.batch.parametre.repertoire");
    		String envDir = this.env.replace(".", "_").toUpperCase();
    		String dirOut = repertoire + envDir;
    		
    		ArrayList<String> r=new ArrayList<String>(Arrays.asList("EXPORT"));
    		
            this.viewFileExport.downloadEnveloppe(requete.toString(), dirOut, r);

    	}
        return "none";

    }
    

    
//    this.viewArchiveBAS8.downloadEnveloppe(querySelection.toString(), chemin, listRepertoire);

    
    public ArrayList<ArrayList<String>> getFilesFromDirectory(String dir, HashMap<String,ArrayList<String>> filter2)
    {
        HashMap<String,ArrayList<String>> filter=filter2;
        ArrayList<ArrayList<String>> result=new ArrayList<ArrayList<String>>();

        ArrayList<String> entete = new ArrayList<String>();
        entete.add("filename");
        entete.add("isdirectory");
        result.add(entete);

        ArrayList<String> format = new ArrayList<String>();
        format.add("text");
        format.add("text");
        result.add(format);


//      if (dir.substring(dir.length()-1, dir.length()).equals("\\"))
//      {
//          System.out.println("yoooo");
//          dir=dir.substring(0,dir.length()-1);
//      }
//
        File files=new File(dir);
//      System.out.println(dir);
        int nb=0;


        // java de merde...
        if (filter==null)
        {
            filter=new  HashMap<String,ArrayList<String>>();
        }

        if (filter.isEmpty())
        {
            filter.put("filename", new ArrayList<String>());
            filter.put("isdirectory", new ArrayList<String>());
        }

        if (filter.get("filename")==null)
        {
            filter.put("filename", new ArrayList<String>());
        }

        if (filter.get("isdirectory")==null)
        {
            filter.put("isdirectory", new ArrayList<String>());
        }


        if (filter.get("filename").size()==0)
            {
                filter.get("filename").add("");
            }

        if (filter.get("isdirectory").size()==0)
            {
                filter.get("isdirectory").add("");
            }

        if (filter.get("filename").get(0)==null)
        {
            filter.get("filename").set(0, "");
        }

        if (filter.get("isdirectory").get(0)==null)
        {
            filter.get("isdirectory").set(0, "");
        }

        System.out.println(filter);


        for (File f:files.listFiles())
        {
            boolean toInsert=true;
            if (!filter.get("filename").get(0).equals("") && !f.getName().contains(filter.get("filename").get(0)))
                    {
                        toInsert=false;
                    }

            if (!filter.get("isdirectory").get(0).equals("") && "true".startsWith(filter.get("isdirectory").get(0)) && !f.isDirectory())
            {
                toInsert=false;
            }

            if (!filter.get("isdirectory").get(0).equals("") && "false".startsWith(filter.get("isdirectory").get(0)) && f.isDirectory())
            {
                toInsert=false;
            }

            if (toInsert)
            {
                ArrayList<String> fileAttribute = new ArrayList<String>();
                fileAttribute.add(f.getName());
                fileAttribute.add(""+f.isDirectory());
                result.add(fileAttribute);
                nb++;
                if (nb>50)
                {
                    break;
                }
            }
        }



        return result;

    }
    
    
    public VObject getViewExport() {
        return this.viewExport;
    }

    public void setViewExport(VObject viewExport) {
        this.viewExport = viewExport;
    }


    public VObject getViewFileExport() {
		return viewFileExport;
	}

	public void setViewFileExport(VObject viewFileExport) {
		this.viewFileExport = viewFileExport;
	}

	public String getExport() {
        return this.export;
    }

    public void setExport(String export) {
        this.export = export;
    }


    /**
     * @return the scope
     */
    public final String getScope() {
        return this.scope;
    }

    /**
     * @param scope
     *            the scope to set
     */
    public final void setScope(String scope) {
        this.scope = scope;
    }

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public ArrayList<String> getListEnv() {
		return listEnv;
	}

	public void setListEnv(ArrayList<String> listEnv) {
		this.listEnv = listEnv;
	}

    
    
}