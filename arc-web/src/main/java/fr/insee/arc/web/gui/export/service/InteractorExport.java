package fr.insee.arc.web.gui.export.service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.export.dao.ExportDao;
import fr.insee.arc.web.gui.export.model.ModelExport;


@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorExport extends ArcWebGenericService<ModelExport,ExportDao>  {

	protected static final String RESULT_SUCCESS = "/jsp/gererExport.jsp";

	private static final Logger LOGGER = LogManager.getLogger(InteractorExport.class);

	@Autowired
	protected ModelExport views;
	

	@Override
	protected void putAllVObjects(ModelExport arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);
				
		views.setViewExport(this.vObjectService.preInitialize(arcModel.getViewExport()));
		views.setViewFileExport(this.vObjectService.preInitialize(arcModel.getViewFileExport()));
		
		putVObject(views.getViewExport(), t -> initializeExport(t));
		putVObject(views.getViewFileExport(), t -> initializeFileExport());
	}

    public void initializeExport(VObject viewExport) {
    	LoggerHelper.debug(LOGGER, "/* initializeExport */");
		dao.initializeViewExport(viewExport);
    }

 

	public String initExportDir()
	{
    	String repertoire = properties.getBatchParametersDirectory();
		String envDir =  getBacASable() .replace(".", "_").toUpperCase();
		Path dirOut = Paths.get(repertoire, envDir, "EXPORT");

		FileUtilsArc.createDirIfNotexist(dirOut.toFile());

		return dirOut.toString();
	}
    
    // visual des Files
    public void initializeFileExport() {
        HashMap<String, String> defaultInputFields = new HashMap<String, String>();

        String dirOut = initExportDir();

        ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(dirOut, this.views.getViewFileExport().mapFilterFields());

        this.vObjectService.initializeByList(views.getViewFileExport(), listeFichier, defaultInputFields);

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
    

	@Override
	public String getActionName() {
		return "export";
	}

    
    
}