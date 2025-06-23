package fr.insee.arc.web.gui.file.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.WebApplicationContext;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.web.gui.all.service.ArcWebGenericService;
import fr.insee.arc.web.gui.all.util.VObject;
import fr.insee.arc.web.gui.all.util.VObjectService;
import fr.insee.arc.web.gui.file.dao.FileDao;
import fr.insee.arc.web.gui.file.model.ModelFile;

@Service
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class InteractorFile extends ArcWebGenericService<ModelFile, FileDao> {

	protected static final String IS_DIRECTORY = "isdirectory";

	protected static final String DIR_OUT = "dirOut";

	protected static final String DIR_IN = "dirIn";

	private static final Logger LOGGER = LogManager.getLogger(InteractorFile.class);

	protected static final String RESULT_SUCCESS = "jsp/gererFile.jsp";

	// magic word that must b e found in file or directory name to proceed for
	// deletion
	private static final String DELETABLE = "DELETE";

	// view column name containing the filename
	protected static final String VC_FILENAME = "filename";

	// private max number of files shown FROM directory
	private static final int MAX_NUMBER_OF_FILES_SHOWN_FROM_DIRECTORY = 1000;

	@Autowired
	protected ModelFile views;

	@Override
	public String getActionName() {
		return "fileSystemManagement";
	}

	@Override
	public void putAllVObjects(ModelFile arcModel) {
		loggerDispatcher.debug("putAllVObjects()", LOGGER);

		views.setViewDirIn(vObjectService.preInitialize(arcModel.getViewDirIn()));
		views.setViewDirOut(vObjectService.preInitialize(arcModel.getViewDirOut()));

		putVObject(views.getViewDirIn(), t -> initializeDirIn());
		putVObject(views.getViewDirOut(), t -> initializeDirOut());

		views.setDirIn(arcModel.getDirIn() == null ? properties.getBatchParametersDirectory() : arcModel.getDirIn());
		views.setDirOut(arcModel.getDirOut() == null ? properties.getBatchParametersDirectory() : arcModel.getDirOut());

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
		Map<String, String> defaultInputFields = new HashMap<>();
		List<List<String>> listeFichier = getDirFiles(views.getDirIn(), views.getViewDirIn());
		this.vObjectService.initializeByList(views.getViewDirIn(), listeFichier, defaultInputFields);
	}

	// visual des Files
	public void initializeDirOut() {
		loggerDispatcher.debug("/* initializeDirOut */", LOGGER);
		Map<String, String> defaultInputFields = new HashMap<>();

		List<List<String>> listeFichier = getDirFiles(views.getDirOut(), views.getViewDirOut());

		this.vObjectService.initializeByList(views.getViewDirOut(), listeFichier, defaultInputFields);

	}

	/**
	 * Transfer the files selected in the view viewSource from the source directory
	 * to the target directory
	 * 
	 * @param viewSource
	 * @param dirSource
	 * @param dirTarget
	 */
	protected void transfer(VObject viewSource, String dirSource, String dirTarget) {
		Map<String, List<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty()) {
			for (String f : m.get(VC_FILENAME)) {
				File fileSource = Paths.get(dirSource, f).toFile();
				File fileTarget = Paths.get(dirTarget, f).toFile();

				try {
					FileUtilsArc.renameTo(fileSource, fileTarget);
				} catch (ArcException e) {
					viewSource.setMessage("fileManagement.transfer.error");
					viewSource.setMessageArgs(fileSource);
				}
			}
		} else {
			for (File fileSource : new File(dirSource).listFiles()) {
				if (!fileSource.isDirectory()) {
					File fileTarget = Paths.get(dirTarget, fileSource.getName()).toFile();

					try {
						FileUtilsArc.renameTo(fileSource, fileTarget);
					} catch (ArcException e) {
						viewSource.setMessage("fileManagement.transfer.error");
						viewSource.setMessageArgs(fileSource);
					}
				}
			}
		}
	}

	/**
	 * Delete the file or folder selected by the users in the view the name of files
	 * or folders to be deleted must contain a magic word to proceed
	 * 
	 * @param viewSource
	 * @param dirSource
	 * @return
	 */
	protected boolean delete(VObject viewSource, String dirSource) {
		// safeguard : only file or directory containing this magic word will be able to
		// be deleted
		Map<String, List<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty()) {
			for (String f : m.get(VC_FILENAME)) {
				File fileSource = new File(dirSource + f);
				if (fileSource.isFile()) {
					if (!fileSource.getName().contains(DELETABLE)) {
						viewSource.setMessage("fileManagement.delete.error.fileName");
						viewSource.setMessageArgs(fileSource);
					} else {
						try {
							Files.delete(fileSource.toPath());
						} catch (IOException e) {
							viewSource.setMessage("fileManagement.delete.error.file");
							viewSource.setMessageArgs(fileSource);
						}
					}
				} else {
					viewSource.setMessage("fileManagement.delete.directory");
					viewSource.setMessageArgs(fileSource);
				}
			}
			return false;
		}

		if (!dirSource.contains(DELETABLE)) {
			viewSource.setMessage("fileManagement.delete.error.directoryName");
			viewSource.setMessageArgs(dirSource);

			return false;
		}

		File dirSourceFile = new File(dirSource);
		if (dirSourceFile.listFiles().length == 0) {
			try {
				Files.delete(dirSourceFile.toPath());
			} catch (IOException e) {
				viewSource.setMessage("fileManagement.delete.error.directory");
				viewSource.setMessageArgs(dirSource);
			}
			return true;
		} else {
			for (File f : dirSourceFile.listFiles()) {
				if (f.isFile()) {
					try {
						Files.delete(f.toPath());
					} catch (IOException e) {
						viewSource.setMessage("fileManagement.delete.error.file");
						viewSource.setMessageArgs(f);
					}
				}
			}
			return false;
		}
	}

	/**
	 * Rename a file or directory according to the changed made by user in view
	 * 
	 * @param model
	 * @return
	 */
	protected void rename(VObject viewSource, String dirSource) {

		Map<String, List<String>> m0 = viewSource.mapContentBeforeUpdate();
		Map<String, List<String>> m1 = viewSource.mapContentAfterUpdate();

		for (int i = 0; i < m0.get(VC_FILENAME).size(); i++) {
			File fileIn = new File(dirSource + m0.get(VC_FILENAME).get(i));
			File fileOut = new File(dirSource + m1.get(VC_FILENAME).get(i));

			try {
				FileUtilsArc.renameTo(fileIn, fileOut);
			} catch (ArcException e) {
				viewSource.setMessage("fileManagement.rename.error");

			}
		}
	}

	/**
	 * download a set of selected file
	 * 
	 * @param response
	 * @param viewSource
	 * @param dirSource
	 */
	protected void download(HttpServletResponse response, VObject viewSource, String dirSource) {
		Map<String, List<String>> selection = viewSource.mapContentSelected();
		if (!selection.isEmpty()) {

			ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
			boolean first = true;
			for (String s : selection.get(VC_FILENAME)) {
				if (first) {
					first = false;
				} else {
					requete.append("\n UNION ALL ");
				}
				requete.append("SELECT " + requete.quoteText(s) + "::text as nom_fichier ");
			}

			List<String> r = new ArrayList<>(Arrays.asList("/"));

			this.vObjectService.downloadEnveloppe(viewSource, response, requete, dirSource, r);
		}
	}

	protected void copy(VObject viewSource, String dirSource, String dirTarget) {
		Map<String, List<String>> m = viewSource.mapContentSelected();
		if (!m.isEmpty()) {
			for (String f : m.get(VC_FILENAME)) {
				File fileSource = new File(dirSource + f);
				File fileTarget = new File(dirTarget + f);
				try {
					Files.copy(fileSource.toPath(), fileTarget.toPath());
				} catch (IOException e) {
					new ArcException(e, ArcExceptionMessage.FILE_COPY_FAILED).logFullException();
					viewSource.setMessage("fileManagement.copy.error");
					viewSource.setMessageArgs(fileSource);
				}
			}
		} else {
			for (File fileSource : new File(dirSource).listFiles()) {
				if (!fileSource.isDirectory()) {
					File fileTarget = Paths.get(dirTarget, fileSource.getName()).toFile();
					try {
						Files.copy(fileSource.toPath(), fileTarget.toPath());
					} catch (IOException e) {
						loggerDispatcher.error("fileManagement.copy.error" + fileSource, e, LOGGER);
						viewSource.setMessage("fileManagement.copy.error");
						viewSource.setMessageArgs(fileSource);
					}
				}
			}
		}
	}

	/**
	 * Create a new directory according to the name provided by the user in the
	 * source view
	 * 
	 * @param viewSource
	 * @param dirSource
	 */
	protected void createDirectory(VObject viewSource, String dirSource) {
		Map<String, List<String>> m = viewSource.mapInputFields();
		if (!m.isEmpty()) {
			if (m.get(VC_FILENAME).get(0) != null && !m.get(VC_FILENAME).get(0).trim().equals("")) {
				Path directoryPath = Paths.get(dirSource + m.get(VC_FILENAME).get(0).trim());
				try {
					Files.createDirectory(directoryPath);
				} catch (IOException e) {
					viewSource.setMessage("fileManagement.createDirectory.error");
					viewSource.setMessageArgs(directoryPath);
				}
			}
		}
	}

	/**
	 * Return the files and directories from a parent directory and according to the
	 * view filters
	 * 
	 * @param dirUri
	 * @param dirVobject
	 * @return
	 */
	private List<List<String>> getDirFiles(String dirUri, VObject dirVobject) {
		File dirFile = Paths.get(dirUri).toFile();
		List<List<String>> listeFichier;
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			listeFichier = errorFromDirectory();
		} else {
			listeFichier = getFilesFromDirectory(dirFile, dirVobject.mapFilterFields());
		}
		return listeFichier;
	}

	private void initializeArrayForDirVObject(List<List<String>> result) {
		VObjectService.addRowToVObjectList(result, VC_FILENAME, IS_DIRECTORY);
		VObjectService.addRowToVObjectList(result, "text", "text");
	}

	private List<List<String>> errorFromDirectory() {
		List<List<String>> result = new ArrayList<>();
		initializeArrayForDirVObject(result);
		VObjectService.addRowToVObjectList(result, "<Path not valid>", "false");
		return result;
	}

	private List<List<String>> getFilesFromDirectory(File dir, Map<String, List<String>> filter) {
		List<List<String>> result = new ArrayList<>();

		initializeArrayForDirVObject(result);

		int nb = 0;

		if (filter == null) {
			filter = new HashMap<>();
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

		for (File f : dir.listFiles()) {
			boolean toInsert = true;
			if (!filter.get(VC_FILENAME).get(0).equals("") && !f.getName().contains(filter.get(VC_FILENAME).get(0))) {
				toInsert = false;
			}

			if (!filter.get(IS_DIRECTORY).get(0).equals("") && "true".startsWith(filter.get(IS_DIRECTORY).get(0))
					&& !f.isDirectory()) {
				toInsert = false;
			}

			if (!filter.get(IS_DIRECTORY).get(0).equals("") && "false".startsWith(filter.get(IS_DIRECTORY).get(0))
					&& f.isDirectory()) {
				toInsert = false;
			}

			if (toInsert) {
				VObjectService.addRowToVObjectList(result, f.getName(), "" + f.isDirectory());
				nb++;
				if (nb > MAX_NUMBER_OF_FILES_SHOWN_FROM_DIRECTORY) {
					break;
				}
			}
		}

		return result;

	}

}