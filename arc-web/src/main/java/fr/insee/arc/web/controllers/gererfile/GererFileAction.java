package fr.insee.arc.web.controllers.gererfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.store.fs.FileUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.model.FileSystemManagementModel;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;
import fr.insee.arc.web.webusecases.ArcWebGenericService;

@Controller
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class GererFileAction extends ArcWebGenericService<FileSystemManagementModel> {

	private static final String IS_DIRECTORY = "isdirectory";

	private static final String DIR_OUT = "dirOut";

	private static final String DIR_IN = "dirIn";

	private static final Logger LOGGER = LogManager.getLogger(GererFileAction.class);

	private static final String RESULT_SUCCESS = "jsp/gererFile.jsp";

	// magic word that must b e found in file or directory name to proceed for deletion
	private static final String DELETABLE="DELETE";
	
	// view column name containing the filename
	private static final String VC_FILENAME ="filename";
	
	// private max number of files shown FROM directory
	private static final int MAX_NUMBER_OF_FILES_SHOWN_FROM_DIRECTORY=50;


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
			this.dirIn= Paths.get(this.dirIn, m.get(VC_FILENAME).get(0)).toString() + File.separator;
			model.addAttribute(DIR_IN, this.dirIn);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortDirIn")
	public String sortDirIn(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewDirIn);
	}

	@RequestMapping("/transferDirIn")
	public String transferDirIn(Model model) {
		transfer(this.viewDirIn, this.dirIn, this.dirOut);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/copyDirIn")
	public String copyDirIn(Model model) {
		copy(this.viewDirIn, this.dirIn, this.dirOut);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/updateDirIn")
	public String updateDirIn(Model model) {
		rename(this.viewDirIn,this.dirIn);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/addDirIn")
	public String addDirIn(Model model) {
		createDirectory(this.viewDirIn,this.dirIn);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/deleteDirIn")
	public String delDirIn(Model model) {
		if (delete(this.viewDirIn, this.dirIn))
		{
			this.dirIn=properties.getBatchParametersDirectory();
			model.addAttribute(DIR_IN, this.dirIn);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/downloadDirIn")
	public String downloadDirIn(Model model, HttpServletResponse response) {
		download(response, this.viewDirIn, this.dirIn);
        return "none";
	}


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
				this.dirOut = Paths.get(this.dirOut, m.get(VC_FILENAME).get(0)).toString() + File.separator;
				model.addAttribute(DIR_OUT, this.dirOut);
			}
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/sortDirOut")
	public String sortDirOut(Model model) {
		return sortVobject(model, RESULT_SUCCESS, this.viewDirOut);
	}

	@RequestMapping("/transferDirOut")
	public String transferDirOut(Model model) {
		transfer(this.viewDirOut, this.dirOut, this.dirIn);
		return generateDisplay(model, RESULT_SUCCESS);
	}


	@RequestMapping("/copyDirOut")
	public String copyDirOut(Model model) {
		copy(this.viewDirOut, this.dirOut, this.dirIn);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/updateDirOut")
	public String updateDirOut(Model model) {
		rename(this.viewDirOut,this.dirOut);
		return generateDisplay(model, RESULT_SUCCESS);
	}


	@RequestMapping("/addDirOut")
	public String addDirOut(Model model) {
		createDirectory(this.viewDirOut,this.dirOut);
		return generateDisplay(model, RESULT_SUCCESS);
	}

	@RequestMapping("/deleteDirOut")
	public String delDirOut(Model model) {
		if (delete(this.viewDirOut, this.dirOut))
		{
			this.dirOut=properties.getBatchParametersDirectory();
			model.addAttribute(DIR_OUT, this.dirOut);
		}
		return generateDisplay(model, RESULT_SUCCESS);
	}

	
	@RequestMapping("/downloadDirOut")
	public String downloadDirOut(Model model, HttpServletResponse response) {
		download(response, this.viewDirOut, this.dirOut);
        return "none";
	}
	
	
	/**
	 * Transfer the files selected in the view viewSource from the source directory to the target directory
	 * @param viewSource
	 * @param dirSource
	 * @param dirTarget
	 */
	private void transfer(VObject viewSource, String dirSource, String dirTarget)
	{
		Map<String, ArrayList<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty()) {
			for (String f : m.get(VC_FILENAME)) {
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
	
	/**
	 * Delete the file or folder selected by the users in the view
	 * the name of files or folders to be deleted must contain a magic word to proceed
	 * @param viewSource
	 * @param dirSource
	 * @return
	 */
	private boolean delete(VObject viewSource, String dirSource) {
		// safeguard : only file or directory containing this magic word will be able to be deleted	
		Map<String, ArrayList<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty()) {
			for (String f : m.get(VC_FILENAME)) {
				File fileSource = new File(dirSource + f);
				if (fileSource.isFile()) {
					if (!fileSource.getName().contains(DELETABLE))
					{
						String errorMessage = "file must contain the word DELETE to be deleted : "+fileSource;
						viewSource.setMessage(errorMessage);
					}
					else
					{
						if (!fileSource.delete())
						{
							String errorMessage = "An error occured while deleting the file "+fileSource;
							viewSource.setMessage(errorMessage);
						}
					}
				}
			}
			return false;
		}
		
		if (!dirSource.contains(DELETABLE))
		{
			return false;
		}
		
		File dirSourceFile = new File(dirSource);
		if (dirSourceFile.listFiles().length == 0) {
			FileUtils.delete(dirSource);
			return true;
		} else {
			for (File f : dirSourceFile.listFiles()) {
				if (f.isFile()) {
					if (!f.delete()) {
						String errorMessage = "An error occured while deleting the file " + f;
						viewSource.setMessage(errorMessage);
					}
				}
			}
			return false;
		}
	}
	

	/**
	 * Rename a file or directory according to the changed made by user in view
	 * @param model
	 * @return
	 */
	private void rename(VObject viewSource, String dirSource) {
		
		Map<String,ArrayList<String>> m0=viewSource.mapContentBeforeUpdate();
		Map<String,ArrayList<String>> m1=viewSource.mapContentAfterUpdate();
		
		for (int i=0; i<m0.get(VC_FILENAME).size();i++)
		{
			File fileIn = new File(dirSource+ m0.get(VC_FILENAME).get(i));
			File fileOut = new File(dirSource + m1.get(VC_FILENAME).get(i));
			if (!fileIn.renameTo(fileOut))
			{
				viewSource.setMessage("Rename operation failed. Check if filesystem isn't locked");
			}
		}
	}
	
	
	public void download(HttpServletResponse response, VObject viewSource, String dirSource) {
		HashMap<String, ArrayList<String>> selection = viewSource.mapContentSelected();
    	if (!selection.isEmpty())
    	{

    		ArcPreparedStatementBuilder requete=new ArcPreparedStatementBuilder();
    		boolean first=true;
    		for (String s:selection.get(VC_FILENAME))
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
    		
    		ArrayList<String> r=new ArrayList<>(Arrays.asList("/"));

        this.vObjectService.downloadEnveloppe(viewSource, response, requete, dirSource, r);
	}
	}
	
	private void copy(VObject viewSource, String dirSource, String dirTarget) {
		Map<String,ArrayList<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty())
		{
			for (String f:m.get(VC_FILENAME))
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

	/**
	 * Create a new directory according to the name provided by the user in the source view
	 * @param viewSource
	 * @param dirSource
	 */
	private void createDirectory(VObject viewSource, String dirSource)
	{
		HashMap<String,ArrayList<String>> m=viewSource.mapInputFields();
		if (!m.isEmpty())
		{
			if (m.get(VC_FILENAME).get(0)!=null && !m.get(VC_FILENAME).get(0).trim().equals(""))
			{
				FileUtils.createDirectory(dirSource+m.get(VC_FILENAME).get(0).trim());
			}
		}
	}
	
	/**
	 * Return the files and directories from a parent directory and according to the view filters
	 * @param dirUri
	 * @param dirVobject
	 * @return
	 */
	private ArrayList<ArrayList<String>> getDirFiles(String dirUri, VObject dirVobject) {
		File dirFile = Paths.get(dirUri).toFile();
		ArrayList<ArrayList<String>> listeFichier;
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			listeFichier = errorFromDirectory();
		} else {
			listeFichier = getFilesFromDirectory(dirFile, dirVobject.mapFilterFields());
		}
		return listeFichier;
	}
	
	private void initializeArrayForDirVObject(ArrayList<ArrayList<String>> result)
	{
		VObjectService.addRowToVObjectList(result,VC_FILENAME,IS_DIRECTORY);
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
		
		filter.putIfAbsent(VC_FILENAME, new ArrayList<>());
		filter.putIfAbsent(IS_DIRECTORY, new ArrayList<>());

		if (filter.get(VC_FILENAME).isEmpty()) {
			filter.get(VC_FILENAME).add("");
		} else if (filter.get(VC_FILENAME).get(0) == null) {
			filter.get(VC_FILENAME).set(0, "");
		}

		if (filter.get(IS_DIRECTORY).isEmpty()) {
			filter.get(IS_DIRECTORY).add("");
		} else if (filter.get(IS_DIRECTORY).get(0) == null) {
			filter.get(IS_DIRECTORY).set(0, "");
		}


		for (File f: dir.listFiles()) {
			boolean toInsert=true;
			if (!filter.get(VC_FILENAME).get(0).equals("") && !f.getName().contains(filter.get(VC_FILENAME).get(0))) {
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
				if (nb>MAX_NUMBER_OF_FILES_SHOWN_FROM_DIRECTORY) {
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