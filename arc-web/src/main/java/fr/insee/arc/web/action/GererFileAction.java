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
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.web.model.FileSystemManagementModel;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GererFileAction extends ArcAction<FileSystemManagementModel> {

	private static final String IS_DIRECTORY = "isdirectory";

	private static final String DIR_OUT = "dirOut";

	private static final String DIR_IN = "dirIn";

	private static final Logger LOGGER = LogManager.getLogger(GererFileAction.class);

	private static final String RESULT_SUCCESS = "jsp/gererFile.jsp";

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
		
		setDirIn(arcModel.getDirIn() == null ? 
				properties.getBatchParametersDirectory() : arcModel.getDirIn() );
		setDirOut(arcModel.getDirOut() == null ? 
				properties.getBatchParametersDirectory() : arcModel.getDirOut() );

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);	
	}
	
	@Override
	public void extraModelAttributes(Model model) {
		model.addAttribute(DIR_IN, dirIn);
		model.addAttribute(DIR_OUT, dirOut);
	}

	// private SessionMap session;
	// visual des Files
	public void initializeDirIn() {
		loggerDispatcher.debug("/* initializeDirIn */", LOGGER);
		HashMap<String, String> defaultInputFields = new HashMap<>();
		ArrayList<ArrayList<String>> listeFichier = getDirFiles(this.dirIn, this.viewDirIn);
		this.vObjectService.initializeByList(viewDirIn, listeFichier, defaultInputFields);
	}

	@RequestMapping("/selectFile")
	public String selectFile(Model model) {
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping({"/seeDirIn", "/selectDirIn"})
	public String seeDirIn(Model model) {
		Map<String,ArrayList<String>> m= viewDirIn.mapContentSelected();
		if (!m.isEmpty() && m.get(IS_DIRECTORY).get(0).equals("true"))  {
			this.dirIn= Paths.get(this.dirIn, m.get("filename").get(0)).toString() + File.separator;
			model.addAttribute(DIR_IN, this.dirIn);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortDirIn")
	public String sortDirIn(Model model) {
		return sortVobject(model, RESULT_SUCCESS, viewDirIn);
	}

	@RequestMapping("/transferDirIn")
	public String transferDirIn(Model model) {
		transfer(viewDirIn, this.dirIn, this.dirOut);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/copyDirIn")
	public String copyDirIn(Model model) {
		copy(viewDirIn, this.dirIn, this.dirOut);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/renameDirIn")
	public String renameDirIn(Model model) {
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
		return generateDisplay(model, RESULT_SUCCESS);
	}


	@RequestMapping("/addDirIn")
	public String addDirIn(Model model) {
		HashMap<String,ArrayList<String>> m=viewDirIn.mapInputFields();
		if (!m.isEmpty())
		{
			if (m.get("filename").get(0)!=null && !m.get("filename").get(0).trim().equals(""))
			{
				FileUtils.createDirectory(this.dirIn+m.get("filename").get(0).trim());
			}
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/deleteDirIn")
	public String delDirIn(Model model) {
		if (delete(viewDirIn, this.dirIn, this.dirOut))
		{
			this.dirIn=null;
			model.addAttribute(DIR_IN, this.dirIn);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}



	// private SessionMap session;
	// visual des Files
	public void initializeDirOut() {
		loggerDispatcher.debug("/* initializeDirOut */", LOGGER);
		HashMap<String, String> defaultInputFields = new HashMap<String, String>();

		ArrayList<ArrayList<String>> listeFichier = getDirFiles(this.dirOut, this.viewDirOut);

		this.vObjectService.initializeByList(viewDirOut, listeFichier, defaultInputFields);

	}

	@RequestMapping({"/selectDirOut", "/seeDirOut"})
	public String seeDirOut(Model model) {
		Map<String,ArrayList<String>> m=viewDirOut.mapContentSelected();

		if (!m.isEmpty()) {
			if(m.get(IS_DIRECTORY).get(0).equals("true")) {
				this.dirOut = Paths.get(this.dirOut, m.get("filename").get(0)).toString() + File.separator;
				model.addAttribute(DIR_OUT, this.dirOut);
			}
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortDirOut")
	public String sortDirOut(Model model) {
		this.vObjectService.sort(viewDirOut);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/transferDirOut")
	public String transferDirOut(Model model) {
		transfer(viewDirOut, this.dirOut, this.dirIn);
		return generateDisplay(model, RESULT_SUCCESS);
	}


	@RequestMapping("/copyDirOut")
	public String copyDirOut(Model model) {
		copy(viewDirOut, this.dirOut, this.dirIn);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/renameDirOut")
	public String renameDirOut(Model model) {
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
		return generateDisplay(model, RESULT_SUCCESS);
	}


	@RequestMapping("/addDirOut")
	public String addDirOut(Model model) {
		HashMap<String,ArrayList<String>> m=viewDirOut.mapInputFields();
		if (!m.isEmpty())
		{
			if (m.get("filename").get(0)!=null && !m.get("filename").get(0).trim().equals(""))
			{
				FileUtils.createDirectory(this.dirOut+m.get("filename").get(0).trim());
			}
		}


		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/deleteDirOut")
	public String delDirOut(Model model) {
		if (delete(viewDirOut, this.dirOut, this.dirIn))
		{
			this.dirOut=null;
			model.addAttribute(DIR_OUT, this.dirOut);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	
	private void transfer(VObject viewSource, String dirSource, String dirTarget)
	{
		Map<String, ArrayList<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty()) {
			for (String f : m.get("filename")) {
				File fileSource = Paths.get(dirSource, f).toFile();
				File fileTarget = Paths.get(dirTarget, f).toFile();
				if (!fileSource.renameTo(fileTarget))
				{
					String errorMessage = "An error occured while tranfering the file "+fileSource;
					loggerDispatcher.error(errorMessage, LOGGER);
					viewSource.setMessage(errorMessage);
				}
			}
		} else {
			for (File fileSource : new File(dirSource).listFiles()) {
				if (!fileSource.isDirectory()) {
					File fileTarget = Paths.get(dirTarget, fileSource.getName()).toFile();
					
					if (!fileSource.renameTo(fileTarget))
					{
						String errorMessage = "An error occured while tranfering the file "+fileSource;
						loggerDispatcher.error(errorMessage, LOGGER);
						viewSource.setMessage(errorMessage);
					}
					
				}
			}
		}
	}
	
	private boolean delete(VObject viewSource, String dirSource, String dirTarget) {

		Map<String, ArrayList<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty()) {
			for (String f : m.get("filename")) {
				File fileSource = new File(dirSource + f);
				if (fileSource.isFile()) {
					if (!fileSource.delete())
					{
						String errorMessage = "An error occured while deleting the file "+fileSource;
						loggerDispatcher.error(errorMessage, LOGGER);
						viewSource.setMessage(errorMessage);
					}
				}
			}
			return false;
		}
		
			File dirSourceFile = new File(dirSource);
			if (dirSourceFile.listFiles().length == 0) {
				FileUtils.delete(dirSource);
				return true;
			} else {
				for (File f : dirSourceFile.listFiles()) {
					if (f.isFile()) {
						if (!f.delete())
						{
							String errorMessage = "An error occured while deleting the file "+f;
							loggerDispatcher.error(errorMessage, LOGGER);
							viewSource.setMessage(errorMessage);
						}
					}
				}
				return false;
			}
	}
	
	private void copy(VObject viewSource, String dirSource, String dirTarget) {
		Map<String,ArrayList<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty())
		{
			for (String f:m.get("filename"))
			{
				File fileSource = new File(dirSource + f);
				File fileTarget = new File(dirTarget + f);
				try {
					Files.copy(fileSource.toPath(), fileTarget.toPath());
				} catch (IOException e) {
					String errorMessage = "An error occured while copying the file "+fileSource;
					loggerDispatcher.error(errorMessage, e, LOGGER);
					viewSource.setMessage(errorMessage);
				}
			}
		}
		else
		{
			for (File fileSource:new File(dirSource).listFiles())
			{
				if (!fileSource.isDirectory())
				{
					File fileTarget = Paths.get(dirTarget, fileSource.getName()).toFile();
					try {
						Files.copy(fileSource.toPath(), fileTarget.toPath());
					} catch (IOException e) {
						String errorMessage = "An error occured while copying the file "+fileSource;
						loggerDispatcher.error(errorMessage, e, LOGGER);
						viewSource.setMessage(errorMessage);
					}
				}
			}
		}
	}

	private ArrayList<ArrayList<String>> getDirFiles(String dirUri, VObject dirVobject) {
		File dirFile = Paths.get(dirUri).toFile();
		ArrayList<ArrayList<String>> listeFichier = new ArrayList<>();
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			listeFichier = errorFromDirectory();
		} else {
			listeFichier = getFilesFromDirectory(dirFile, dirVobject.mapFilterFields());
		}
		return listeFichier;
	}
	
	private void initializeArrayForDirVObject(ArrayList<ArrayList<String>> result)
	{
		VObjectService.addRowToVObjectList(result,"filename",IS_DIRECTORY);
		VObjectService.addRowToVObjectList(result,"text","text");
	}

	private ArrayList<ArrayList<String>> errorFromDirectory() {
		ArrayList<ArrayList<String>> result = new ArrayList<>();
		initializeArrayForDirVObject(result);
		VObjectService.addRowToVObjectList(result, "<Path not valid>", "false");
		return result;
	}

	private ArrayList<ArrayList<String>> getFilesFromDirectory(File dir, HashMap<String,ArrayList<String>> filter) {
		ArrayList<ArrayList<String>> result=new ArrayList<>();

		initializeArrayForDirVObject(result);

		int nb=0;


		if (filter==null) {
			filter=new  HashMap<>();
		}
		
		filter.putIfAbsent("filename", new ArrayList<>());
		filter.putIfAbsent(IS_DIRECTORY, new ArrayList<>());

		if (filter.get("filename").isEmpty()) {
			filter.get("filename").add("");
		} else if (filter.get("filename").get(0) == null) {
			filter.get("filename").set(0, "");
		}

		if (filter.get(IS_DIRECTORY).isEmpty()) {
			filter.get(IS_DIRECTORY).add("");
		} else if (filter.get(IS_DIRECTORY).get(0) == null) {
			filter.get(IS_DIRECTORY).set(0, "");
		}


		for (File f: dir.listFiles()) {
			boolean toInsert=true;
			if (!filter.get("filename").get(0).equals("") && !f.getName().contains(filter.get("filename").get(0))) {
				toInsert=false;
			}

			if (!filter.get(IS_DIRECTORY).get(0).equals("") && "true".startsWith(filter.get(IS_DIRECTORY).get(0)) && !f.isDirectory()) {
				toInsert=false;
			}

			if (!filter.get(IS_DIRECTORY).get(0).equals("") && "false".startsWith(filter.get(IS_DIRECTORY).get(0)) && f.isDirectory()) {
				toInsert=false;
			}

			if (toInsert) {
				VObjectService.addRowToVObjectList(result,f.getName(),""+f.isDirectory());
				nb++;
				if (nb>50) {
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