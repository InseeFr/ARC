package fr.insee.arc.web.gui.export.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.structure.GenericBean;

@Service
public class ServiceViewExport extends InteractorExport {
	   public String selectExport(Model model) {

			return generateDisplay(model, RESULT_SUCCESS);
	    }

	    public String addExport(Model model) {
	        this.vObjectService.insert(views.getViewExport());
	        return generateDisplay(model, RESULT_SUCCESS);
	    }

	    public String deleteExport(Model model) {
	         this.vObjectService.delete(views.getViewExport());
	        return generateDisplay(model, RESULT_SUCCESS);
	    }

	    public String updateExport(Model model) {
	        this.vObjectService.update(views.getViewExport());
	        return generateDisplay(model, RESULT_SUCCESS);
	    }

	    public String sortExport(Model model) {
	        this.vObjectService.sort(views.getViewExport());
	        return generateDisplay(model, RESULT_SUCCESS);
	    }

	  
	    public String startExport(Model model) {
	        
	    	try {
	    	ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
	    	requete.append("SELECT * FROM "+ getBacASable() +".export ");
	    	requete.append("WHERE file_name IN ("+requete.sqlListeOfValues(views.getViewExport().mapContentSelected().get("file_name"))+") ");
	    	
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
	    		views.getViewExport().setMessage("Export failed because of database query");
	    	}
	    	catch(IOException e)
	    	{
	    		views.getViewExport().setMessage("Export failed because of file system problem");
	    	}
	    	
	        return generateDisplay(model, RESULT_SUCCESS);
	    }
	
}
