package fr.insee.arc.web.gui.export.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.tools.ant.util.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.files.FileUtilsArc;

@Service
public class ServiceViewFileExport extends InteractorExport {


    public String selectFileExport(Model model) {
        return basicAction(model, RESULT_SUCCESS);
    }

    public String sortFileExport(Model model) {
        this.vObjectService.sort(views.getViewFileExport());
        return generateDisplay(model, RESULT_SUCCESS);
    }

    public String deleteFileExport(Model model) {
    	String dirOut=initExportDir();
    	HashMap<String, ArrayList<String>> selection = this.views.getViewFileExport().mapContentSelected();
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

    	 HashMap<String,ArrayList<String>> m=this.views.getViewFileExport().mapContentBeforeUpdate();
         HashMap<String,ArrayList<String>> n=this.views.getViewFileExport().mapContentAfterUpdate();

         if (!m.isEmpty())
         {
             for (int i=0; i<m.get("filename").size();i++)
             {
               File fileIn = new File(dirOut + File.separator + m.get("filename").get(i));
               File fileOut = new File(dirOut + File.separator + n.get("filename").get(i));
               
               if (!FileUtilsArc.renameTo(fileIn, fileOut))
               {
            	   this.views.getViewFileExport().setMessage("Le renommage a échoué");
               }
             }
         }
       return generateDisplay(model, RESULT_SUCCESS);
    }
    
    public String downloadFileExport(Model model, HttpServletResponse response) {
    	HashMap<String, ArrayList<String>> selection = this.views.getViewFileExport().mapContentSelected();
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
    		
    		ArrayList<String> r=new ArrayList<>(Arrays.asList("EXPORT"));
    		
            this.vObjectService.downloadEnveloppe(views.getViewFileExport(), response, requete, dirOut, r);

    	}
        return "none";

    }
    
	
}
