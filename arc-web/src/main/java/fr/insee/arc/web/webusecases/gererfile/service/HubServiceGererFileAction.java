package fr.insee.arc.web.webusecases.gererfile.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.web.util.VObject;
import fr.insee.arc.web.util.VObjectService;
import fr.insee.arc.web.webusecases.ArcWebGenericService;
import fr.insee.arc.web.webusecases.gererfile.model.ModelGererFile;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HubServiceGererFileAction extends ArcWebGenericService<ModelGererFile> {

	protected static final String IS_DIRECTORY = "isdirectory";

	protected static final String DIR_OUT = "dirOut";

	protected static final String DIR_IN = "dirIn";

	private static final Logger LOGGER = LogManager.getLogger(HubServiceGererFileAction.class);

	protected static final String RESULT_SUCCESS = "jsp/gererFile.jsp";

	// magic word that must b e found in file or directory name to proceed for deletion
	private static final String DELETABLE="DELETE";
	
	// view column name containing the filename
	protected static final String VC_FILENAME ="filename";
	
	// private max number of files shown FROM directory
	private static final int MAX_NUMBER_OF_FILES_SHOWN_FROM_DIRECTORY=1000;

	@Autowired
	protected ModelGererFile views;

	@Override
	public String getActionName() {
		return "fileSystemManagement";
	}

	@Override
	public void putAllVObjects(ModelGererFile arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);

		views.setViewDirIn(vObjectService.preInitialize(arcModel.getViewDirIn()));
		views.setViewDirOut(vObjectService.preInitialize(arcModel.getViewDirOut()));

		putVObject(views.getViewDirIn(), t -> initializeDirIn());
		putVObject(views.getViewDirOut(), t -> initializeDirOut());
		
		views.setDirIn(arcModel.getDirIn() == null ? 
				properties.getBatchParametersDirectory() : arcModel.getDirIn() );
		views.setDirOut(arcModel.getDirOut() == null ? 
				properties.getBatchParametersDirectory() : arcModel.getDirOut() );

		loggerDispatcher.debug("putAllVObjects() end", LOGGER);	
	}
	
	@Override
	public void extraModelAttributes(Model model) {
		model.addAttribute(DIR_IN, views.getDirIn());
		model.addAttribute(DIR_OUT, views.getDirOut());
	}

	// visual des Files
	public void initializeDirIn() {
		loggerDispatcher.debug("/* initializeDirIn */", LOGGER);
		HashMap<String, String> defaultInputFields = new HashMap<>();
		ArrayList<ArrayList<String>> listeFichier = getDirFiles(views.getDirIn(), views.getViewDirIn());
		this.vObjectService.initializeByList(views.getViewDirIn(), listeFichier, defaultInputFields);
	}



	// visual des Files
	public void initializeDirOut() {
		loggerDispatcher.debug("/* initializeDirOut */", LOGGER);
		HashMap<String, String> defaultInputFields = new HashMap<>();

		ArrayList<ArrayList<String>> listeFichier = getDirFiles(views.getDirOut(), views.getViewDirOut());

		this.vObjectService.initializeByList(views.getViewDirOut(), listeFichier, defaultInputFields);

	}

	
	/**
	 * Transfer the files selected in the view viewSource from the source directory to the target directory
	 * @param viewSource
	 * @param dirSource
	 * @param dirTarget
	 */
	protected void transfer(VObject viewSource, String dirSource, String dirTarget)
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
	protected boolean delete(VObject viewSource, String dirSource) {
		// safeguard : only file or directory containing this magic word will be able to be deleted	
		Map<String, ArrayList<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty()) {
			for (String f : m.get(VC_FILENAME)) {
				File fileSource = new File(dirSource + f);
				if (fileSource.isFile()) {
					if (!fileSource.getName().contains(DELETABLE))
					{
						String errorMessage = "file name must contain the word DELETE to be deleted : "+fileSource;
						viewSource.setMessage(errorMessage);
					}
					else
					{
						try {
							Files.delete(fileSource.toPath());
						} catch (IOException e) {
							String errorMessage = "An error occured while deleting the file "+fileSource;
							viewSource.setMessage(errorMessage);						}
					}
				}
				else
				{
					String errorMessage = "The selected directory cannot be deleted this way. Go under the directory to delete it."+fileSource;
					viewSource.setMessage(errorMessage);
				}
			}
			return false;
		}
		
		if (!dirSource.contains(DELETABLE))
		{
			String errorMessage = "directory name must contain the word DELETE to be deleted "+dirSource;
			viewSource.setMessage(errorMessage);

			return false;
		}
		

		File dirSourceFile = new File(dirSource);
		if (dirSourceFile.listFiles().length == 0) {
			try {
				Files.delete(dirSourceFile.toPath());
			} catch (IOException e) {
				String errorMessage = "An error occured while deleting the directory " + dirSource;
				viewSource.setMessage(errorMessage);
			}
			return true;
		} else {
			for (File f : dirSourceFile.listFiles()) {
				if (f.isFile()) {
					try {
						Files.delete(f.toPath());
					} catch (IOException e) {
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
	protected void rename(VObject viewSource, String dirSource) {
		
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
	
	/**
	 * download a set of selected file
	 * @param response
	 * @param viewSource
	 * @param dirSource
	 */
	protected void download(HttpServletResponse response, VObject viewSource, String dirSource) {
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
	
	protected void copy(VObject viewSource, String dirSource, String dirTarget) {
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
	protected void createDirectory(VObject viewSource, String dirSource)
	{
		HashMap<String,ArrayList<String>> m=viewSource.mapInputFields();
		if (!m.isEmpty())
		{
			if (m.get(VC_FILENAME).get(0)!=null && !m.get(VC_FILENAME).get(0).trim().equals(""))
			{
				Path directoryPath = Paths.get(dirSource+m.get(VC_FILENAME).get(0).trim());
				try {
					Files.createDirectory(directoryPath);
				} catch (IOException e) {
					viewSource.setMessage("directory creation failed "+directoryPath);
				}
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

}