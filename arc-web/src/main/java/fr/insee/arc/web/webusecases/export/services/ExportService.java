package fr.insee.arc.web.webusecases.export.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tools.ant.util.FileUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.webusecases.ArcWebGenericService;
import fr.insee.arc.web.webusecases.export.models.ExportModel;


@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ExportService extends ArcWebGenericService<ExportModel>  {

	private static final String RESULT_SUCCESS = "/jsp/gererExport.jsp";

	private static final Logger LOGGER = LogManager.getLogger(ExportService.class);

    private VObject viewExport;

    private VObject viewFileExport;


	@Override
	protected void putAllVObjects(ExportModel arcModel) {
		setViewExport(this.vObjectService.preInitialize(arcModel.getViewExport()));
		setViewFileExport(this.vObjectService.preInitialize(arcModel.getViewFileExport()));
		
		putVObject(getViewExport(), t -> initializeExport());
		putVObject(getViewFileExport(), t -> initializeFileExport());
	}

    public void initializeExport() {
        HashMap<String, String> defaultInputFields = new HashMap<>();
        this.vObjectService.initialize(viewExport, new ArcPreparedStatementBuilder("SELECT file_name, zip, table_to_export, headers, nulls, filter_table, order_table, nomenclature_export, columns_array_header, columns_array_value, etat  from "+ getBacASable() +".export"),  getBacASable() +".export", defaultInputFields);
    }

    public String selectExport(Model model) {

		return generateDisplay(model, RESULT_SUCCESS);
    }

    public String addExport(Model model) {
        this.vObjectService.insert(viewExport);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String deleteExport(Model model) {
         this.vObjectService.delete(viewExport);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String updateExport(Model model) {
        this.vObjectService.update(viewExport);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String sortExport(Model model) {
        this.vObjectService.sort(viewExport);
        return generateDisplay(model, RESULT_SUCCESS);
    }

  
    public String startExport(Model model) {
        
    	try {
    	ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
    	requete.append("SELECT * FROM "+ getBacASable() +".export ");
    	requete.append("WHERE file_name IN ("+requete.sqlListeOfValues(viewExport.mapContentSelected().get("file_name"))+") ");
    	
    	// Requeter les export à réaliser
    	HashMap<String,ArrayList<String>> h=new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
    			requete))
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
    	
    	// Initialiser le répertoire de sortie 	
    	String dirOut=initExportDir();

    	// itérer sur les exports à réaliser
    	for (int n=0;n<tablesToExport.size();n++)
    	{
    		requete=new ArcPreparedStatementBuilder();
    		requete.append("UPDATE "+ getBacASable() +".export set etat="+requete.quoteText(TraitementEtat.ENCOURS.toString())+" where file_name="+requete.quoteText(fileName.get(n))+" ");
    		UtilitaireDao.get("arc").executeRequest(null,requete);

    		File fOut;
    		if (!StringUtils.isEmpty(zip.get(n)))
    		{
    			fOut=new File(dirOut+ File.separator +fileName.get(n)+".zip");
    		}
    		else
    		{
        		fOut=new File(dirOut+ File.separator +fileName.get(n));
    		}
    		try (FileOutputStream fw = new FileOutputStream(fOut))
    		{
	    		
	    		try (ZipOutputStream zos=!StringUtils.isEmpty(zip.get(n))?new ZipOutputStream(fw):null)
	    		{
	    		
			    		if (!StringUtils.isEmpty(zip.get(n)))
			    		{
				    		ZipEntry ze = new ZipEntry(fileName.get(n));
				    		zos.putNextEntry(ze);
				   		}
			    		
			    		try(BufferedWriter bw = !StringUtils.isEmpty(zip.get(n))?new BufferedWriter(new OutputStreamWriter(zos,"UTF-8")):new BufferedWriter(new OutputStreamWriter(fw,"UTF-8")))
			    		{
			    		
			    		HashMap<String,Integer> pos= new HashMap<String,Integer>();
			    		ArrayList<String> headerLine=new ArrayList<String>();
			    		
			    		// if columns,orders table is specified, get the information from database metadata
			    		String howToExportReworked;
			    		if (howToExport.get(n)==null)
			    		{
			    			howToExportReworked="(select column_name as varbdd, ordinal_position as pos from information_schema.columns where table_schema||'.'||table_name = '"+ getBacASable() .toLowerCase()+"."+tablesToExport.get(n)+"') ww ";
			    		}
			    		else
			    		{
			    			howToExportReworked="arc."+howToExport.get(n);
			    		}
			    		
			    		// lire la table how to export pour voir comment on va s'y prendre
			    		// L'objectif est de créer une hashmap de correspondance entre la variable et la position
			    		h=new GenericBean(UtilitaireDao.get("arc").executeRequest(null,
			    				new ArcPreparedStatementBuilder("SELECT lower(varbdd) as varbdd, pos::int-1 as pos, max(pos::int) over() as maxp FROM "+howToExportReworked+" order by pos::int ")))
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

			    		Statement stmt = c.createStatement();
			    	    stmt.setFetchSize(5000);
			    		
			    	    try (ResultSet res=stmt.executeQuery("SELECT * FROM "+ getBacASable() +"."+tablesToExport.get(n)+" WHERE "+(StringUtils.isEmpty(filterTable.get(n))?"true":filterTable.get(n))+" "+(StringUtils.isEmpty(orderTable.get(n))?"":"ORDER BY "+orderTable.get(n)+" ")))
			    	    {
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
			    	    }
			            c.close();
			            bw.flush();
			            fw.flush();
			    		
			            if (!StringUtils.isEmpty(zip.get(n)))
			    		{
				            zos.flush();
				            zos.closeEntry();
			    		} 
			        }
	    		}
	    		
    		}
    		
    		requete=new ArcPreparedStatementBuilder();
    		requete.append("UPDATE "+ getBacASable() +".export set etat=to_char(current_timestamp,'YYYY-MM-DD HH24:MI:SS') ");
    		requete.append("WHERE file_name="+requete.quoteText(fileName.get(n))+" ");
    		
            UtilitaireDao.get("arc").executeRequest(null,requete);
    	}
    	}
    	catch(SQLException e)
    	{
    		viewExport.setMessage("Export failed because of database query");
    	}
    	catch(IOException e)
    	{
    		viewExport.setMessage("Export failed because of file system problem");
    	}
    	
        return generateDisplay(model, RESULT_SUCCESS);
    }

	public String initExportDir()
	{
    	String repertoire = properties.getBatchParametersDirectory();
		String envDir =  getBacASable() .replace(".", "_").toUpperCase();
		Path dirOut = Paths.get(repertoire, envDir, "EXPORT");
		File f = dirOut.toFile();
	
		if (!f.exists()) {
		    f.mkdirs();
		}
		return dirOut.toString();
	}
    
    // visual des Files
    public void initializeFileExport() {
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();

        String dirOut = initExportDir();

        ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(dirOut, this.viewFileExport.mapFilterFields());

        this.vObjectService.initializeByList(viewFileExport, listeFichier, defaultInputFields);

    }

    public String selectFileExport(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    public String sortFileExport(Model model) {
        this.vObjectService.sort(viewFileExport);
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String deleteFileExport(Model model) {
    	String dirOut=initExportDir();
    	HashMap<String, ArrayList<String>> selection = this.viewFileExport.mapContentSelected();
    	loggerDispatcher.debug(selection, LOGGER);
    	if (!selection.isEmpty())
    	{
    		for (String s:selection.get("filename"))
    		{
    			FileUtils.delete(new File(dirOut + File.separator + s));
    		}
    	}
        return generateDisplay(model, RESULT_SUCCESS);
    }
    
    public String updateFileExport(Model model) {
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
       return generateDisplay(model, RESULT_SUCCESS);
    }
    
    public String downloadFileExport(Model model, HttpServletResponse response) {
    	HashMap<String, ArrayList<String>> selection = this.viewFileExport.mapContentSelected();
    	if (!selection.isEmpty())
    	{
    		ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();
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
    			requete.append("SELECT "+requete.quoteText(s)+" as nom_fichier ");
    		}
    		
        	String repertoire = properties.getBatchParametersDirectory();
       		String envDir =  getBacASable() .replace(".", "_").toUpperCase();
    		String dirOut = repertoire + envDir;
    		
    		ArrayList<String> r=new ArrayList<String>(Arrays.asList("EXPORT"));
    		
            this.vObjectService.downloadEnveloppe(viewFileExport, response, requete, dirOut, r);

    	}
        return "none";

    }
    
    
    private ArrayList<ArrayList<String>> getFilesFromDirectory(String dir, HashMap<String,ArrayList<String>> filter2)
    {
        HashMap<String,ArrayList<String>> filter=filter2;
        ArrayList<ArrayList<String>> result=new ArrayList<>();

        ArrayList<String> entete = new ArrayList<>();
        entete.add("filename");
        entete.add("isdirectory");
        result.add(entete);

        ArrayList<String> format = new ArrayList<>();
        format.add("text");
        format.add("text");
        result.add(format);

        File files=new File(dir);

        int nb=0;

        if (filter==null)
        {
            filter=new HashMap<>();
        }

        if (filter.isEmpty())
        {
            filter.put("filename", new ArrayList<>());
            filter.put("isdirectory", new ArrayList<>());
        }

        if (filter.get("filename")==null)
        {
            filter.put("filename", new ArrayList<>());
        }

        if (filter.get("isdirectory")==null)
        {
            filter.put("isdirectory", new ArrayList<>());
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

	@Override
	public String getActionName() {
		return "export";
	}

    
    
}