package fr.insee.arc.web.action;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.store.fs.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;
import fr.insee.arc.web.model.FileSystemManagementModel;
import fr.insee.arc.web.util.VObject;

@Controller
public class GererFileAction extends ArcAction<FileSystemManagementModel> {

	private static final Logger LOGGER = LogManager.getLogger(GererFileAction.class);

	private static final String RESULT_SUCCESS = "jsp/gererFile.jsp";

	@Autowired
	public PropertiesHandler PROPERTIES;

	public static String REPERTOIRE_EFFACABLE="TO_DELETE";

	private VObject viewDirIn;

	private VObject viewDirOut;

	private String dirIn;

	private String dirOut;


	@Override
	public String getActionName() {
		return "fileSystemManagement";
	}

	@Override
	public void putAllVObjects(FileSystemManagementModel arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);

		setViewDirIn(vObjectService.preInitialize(arcModel.getViewDirIn()));
		setViewDirOut(vObjectService.preInitialize(arcModel.getViewDirOut()));

		putVObject(getViewDirIn(), t -> initializeDirIn());
		putVObject(getViewDirOut(), t -> initializeDirOut());

		setDirIn(arcModel.getDirIn());
		setDirOut(arcModel.getDirOut());

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);	
	}

	// private SessionMap session;
	// visual des Files
	public void initializeDirIn() {
		System.out.println("/* initializeDirIn */");
		HashMap<String, String> defaultInputFields = new HashMap<>();

		if (this.dirIn==null) {
			this.dirIn=PROPERTIES.getBatchParametersDirectory();
		}

		ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirIn, viewDirIn.mapFilterFields());

		this.vObjectService.initializeByList(viewDirIn, listeFichier, defaultInputFields);

	}


	@RequestMapping("/selectFile")
	public String selectFile() {
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/selectDirIn")
	public String selectDirIn() {
		return seeDirIn();
	}

	@RequestMapping("/seeDirIn")
	public String seeDirIn() {
		Map<String,ArrayList<String>> m=viewDirIn.mapContentSelected();
		if (!m.isEmpty()) {
			if(m.get("isdirectory").get(0).equals("true"))  {
				this.dirIn= Paths.get(this.dirIn, m.get("filename").get(0)).toString() + File.separator;
			}
		}


		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/sortDirIn")
	public String sortDirIn() {
		this.vObjectService.sort(viewDirIn);
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/transferDirIn")
	public String transferDirIn() {
		Map<String,ArrayList<String>> m=viewDirIn.mapContentSelected();
		if (!m.isEmpty())
		{
			for (String f:m.get("filename"))
			{
				File fileIn = new File(this.dirIn + f);
				File fileOut = new File(this.dirOut + f);
				fileIn.renameTo(fileOut);
			}
		}
		else
		{
			m=viewDirIn.mapContent();
			while (!m.isEmpty())
			{
				for (int i=0;i<m.get("filename").size();i++)
				{
					if (m.get("isdirectory").get(i).equals("false"))
					{
						File fileIn = new File(this.dirIn + m.get("filename").get(i));
						File fileOut = new File(this.dirOut + m.get("filename").get(i));
						fileIn.renameTo(fileOut);
					}
				}
				ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirIn, viewDirIn.mapFilterFields());
				this.vObjectService.initializeByList(viewDirIn, listeFichier, new HashMap<>());
				m=viewDirIn.mapContent();
			}
		}
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/copyDirIn")
	public String copyDirIn() {
		Map<String,ArrayList<String>> m=viewDirIn.mapContentSelected();
		if (!m.isEmpty())
		{
			for (String f:m.get("filename"))
			{
				File fileIn = new File(this.dirIn + f);
				File fileOut = new File(this.dirOut + f);
				try {
					Files.copy(fileIn.toPath(), fileOut.toPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
		{
			m=viewDirIn.mapContent();
			while (!m.isEmpty())
			{
				for (int i=0;i<m.get("filename").size();i++)
				{
					if (m.get("isdirectory").get(i).equals("false"))
					{
						File fileIn = new File(this.dirIn + m.get("filename").get(i));
						File fileOut = new File(this.dirOut + m.get("filename").get(i));
						try {
							Files.copy(fileIn.toPath(), fileOut.toPath());
						} catch (IOException e) {
						}
					}
				}
				ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirIn, viewDirIn.mapFilterFields());
				this.vObjectService.initializeByList(viewDirIn, listeFichier, new HashMap<>());
				m=viewDirIn.mapContent();
			}
		}
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/renameIn")
	public String renameIn() {
		Map<String,ArrayList<String>> m=viewDirIn.mapContentSelected();
		Map<String,ArrayList<String>> n=viewDirIn.mapContentSelected();

		if (!m.isEmpty())
		{
			for (int i=0; i<m.get("filename").size();i++)
			{
				File fileIn = new File(this.dirIn + m.get("filename").get(i));
				File fileOut = new File(this.dirIn + n.get("filename").get(i));
				fileIn.renameTo(fileOut);
			}
		}
		return generateDisplay(RESULT_SUCCESS);
	}


	@RequestMapping("/addDirIn")
	public String addDirIn() {
		HashMap<String,ArrayList<String>> m=viewDirIn.mapInputFields();
		if (!m.isEmpty())
		{
			if (m.get("filename").get(0)!=null && !m.get("filename").get(0).trim().equals(""))
			{
				FileUtils.createDirectory(this.dirIn+m.get("filename").get(0).trim());
			}
		}
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/deleteDirIn")
	public String delDirIn() {


		if (!this.dirIn.contains(REPERTOIRE_EFFACABLE))
		{
			File f=new File(this.dirIn);
			if (f.listFiles().length==0)
			{
				FileUtils.delete(this.dirIn);
				this.dirIn=null;
			}
			else
			{
				this.viewDirIn.setMessage("Le répertoire doit être vide ou contenir "+REPERTOIRE_EFFACABLE+" pour etre effaçable. ");
			}
		}
		else
		{
			FileUtils.deleteRecursive(this.dirIn, true);
			this.dirIn=null;
		}
		return generateDisplay(RESULT_SUCCESS);
	}


	// private SessionMap session;
	// visual des Files
	public void initializeDirOut() {
		System.out.println("/* initializeDirOut */");
		HashMap<String, String> defaultInputFields = new HashMap<String, String>();


		if (this.dirOut==null)
		{
			this.dirOut=PROPERTIES.getBatchParametersDirectory();
		}

		ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirOut, viewDirOut.mapFilterFields());

		this.vObjectService.initializeByList(viewDirOut, listeFichier, defaultInputFields);

	}

	@RequestMapping("/selectDirOut")
	public String selectDirOut() {
		return seeDirOut();
	}

	@RequestMapping("/seeDirOut")
	public String seeDirOut() {
		Map<String,ArrayList<String>> m=viewDirOut.mapContentSelected();

		//        System.out.println(m);
		if (!m.isEmpty()) {
			if(m.get("isdirectory").get(0).equals("true")) {
				this.dirOut= Paths.get(this.dirOut, m.get("filename").get(0)).toString() + File.separator;
			}
		}

		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/sortDirOut")
	public String sortDirOut() {
		this.vObjectService.sort(viewDirOut);
		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/transferDirOut")
	public String transferDirOut() {
		Map<String,ArrayList<String>> m=viewDirOut.mapContentSelected();
		if (!m.isEmpty())
		{
			for (String f:m.get("filename"))
			{
				File fileIn = new File(this.dirOut + f);
				File fileOut = new File(this.dirIn + f);
				fileIn.renameTo(fileOut);
			}
		}
		else
		{

			m=viewDirOut.mapContent();
			while (!m.isEmpty())
			{
				for (int i=0;i<m.get("filename").size();i++)
				{
					if (m.get("isdirectory").get(i).equals("false"))
					{
						File fileIn = new File(this.dirOut + m.get("filename").get(i));
						File fileOut = new File(this.dirIn + m.get("filename").get(i));
						fileIn.renameTo(fileOut);
					}
				}
				ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirOut, viewDirOut.mapFilterFields());
				this.vObjectService.initializeByList(viewDirOut, listeFichier, new HashMap<>());
				m=viewDirOut.mapContent();
			}
		}
		return generateDisplay(RESULT_SUCCESS);
	}


	@RequestMapping("/copyDirOut")
	public String copyDirOut() {
		Map<String,ArrayList<String>> m=viewDirOut.mapContentSelected();
		if (!m.isEmpty())
		{
			for (String f:m.get("filename"))
			{
				File fileIn = new File(this.dirOut + f);
				File fileOut = new File(this.dirIn + f);
				try {
					Files.copy(fileIn.toPath(), fileOut.toPath());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
		{
			m=viewDirOut.mapContent();
			while (!m.isEmpty())
			{
				for (int i=0;i<m.get("filename").size();i++)
				{
					if (m.get("isdirectory").get(i).equals("false"))
					{
						File fileIn = new File(this.dirOut + m.get("filename").get(i));
						File fileOut = new File(this.dirIn + m.get("filename").get(i));
						try {
							Files.copy(fileIn.toPath(), fileOut.toPath());
						} catch (IOException e) {}
					}
				}
				ArrayList<ArrayList<String>> listeFichier = getFilesFromDirectory(this.dirOut, viewDirOut.mapFilterFields());
				this.vObjectService.initializeByList(viewDirOut, listeFichier, new HashMap<>());
				m=viewDirOut.mapContent();
			}
		}
		return generateDisplay(RESULT_SUCCESS);
	}


	@RequestMapping("/renameOut")
	public String renameOut() {
		Map<String,ArrayList<String>> m=viewDirOut.mapContentSelected();
		Map<String,ArrayList<String>> n=viewDirOut.mapContentSelected();

		if (!m.isEmpty())
		{
			for (int i=0; i<m.get("filename").size();i++)
			{
				File fileIn = new File(this.dirOut + m.get("filename").get(i));
				File fileOut = new File(this.dirOut + n.get("filename").get(i));
				fileIn.renameTo(fileOut);
			}
		}
		return generateDisplay(RESULT_SUCCESS);
	}


	@RequestMapping("/addDirOut")
	public String addDirOut() {
		HashMap<String,ArrayList<String>> m=viewDirOut.mapInputFields();
		if (!m.isEmpty())
		{
			if (m.get("filename").get(0)!=null && !m.get("filename").get(0).trim().equals(""))
			{
				FileUtils.createDirectory(this.dirOut+m.get("filename").get(0).trim());
			}
		}


		return generateDisplay(RESULT_SUCCESS);
	}

	@RequestMapping("/deleteDirOut")
	public String delDirOut() {


		if (!this.dirOut.contains(REPERTOIRE_EFFACABLE))
		{
			File f=new File(this.dirOut);
			if (f.listFiles().length==0)
			{
				FileUtils.delete(this.dirOut);
				this.dirOut=null;
			}
			else
			{
				this.viewDirOut.setMessage("Le répertoire doit être vide ou contenir "+REPERTOIRE_EFFACABLE+" pour etre effaçable. ");
			}
		}
		else
		{
			FileUtils.deleteRecursive(this.dirOut, true);
			this.dirOut=null;
		}
		return generateDisplay(RESULT_SUCCESS);
	}

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

	public VObject getViewDirIn() {
		return this.viewDirIn;
	}

	public void setViewDirIn(VObject viewDirIn) {
		this.viewDirIn = viewDirIn;
	}

	public VObject getViewDirOut() {
		return viewDirOut;
	}

	public void setViewDirOut(VObject viewDirOut) {
		this.viewDirOut = viewDirOut;
	}

	public String getDirIn() {
		return this.dirIn;
	}

	public void setDirIn(String dirIn) {
		this.dirIn = dirIn;
	}

	public String getDirOut() {
		return this.dirOut;
	}

	public void setDirOut(String dirOut) {
		this.dirOut = dirOut;
	}

}