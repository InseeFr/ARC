package fr.insee.arc.web.gui.export.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.web.gui.all.util.VObject;

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
			// Requêter les exports à réaliser
			VObject viewExport = views.getViewExport();
			dao.setSelectedRecords(viewExport.mapContentSelected());
			HashMap<String, ArrayList<String>> rules = dao.startExportRetrieve();

			ArrayList<String> fileName = rules.get("file_name");
			ArrayList<String> zip = rules.get("zip");

			// Initialiser le répertoire de sortie
			String dirOut = initExportDir();

			// Itérer sur les exports à réaliser
			for (int fileIndex = 0; fileIndex < fileName.size(); fileIndex++) {
				
				dao.startExportUpdateState(fileName, fileIndex, false);

				switch (zip.get(fileIndex)) {
				case "1": // Zip
					exportZip(dirOut, rules, fileIndex);
					break;
				case "2": // GZ
					exportGz(dirOut, rules, fileIndex);
					break;
				default: // NA ou non renseigné
					exportPlainText(dirOut, rules, fileIndex);
				}

				dao.startExportUpdateState(fileName, fileIndex, true);
			}
		} catch (ArcException | SQLException e) {
			views.getViewExport().setMessage("export.error.database");
		} catch (IOException e) {
			views.getViewExport().setMessage("export.error.filesystem");
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	private void exportPlainText(String dirOut, HashMap<String, ArrayList<String>> h, int n)
			throws IOException, ArcException, SQLException {
		ArrayList<String> fileName = h.get("file_name");

		File fOut = new File(dirOut + File.separator + fileName.get(n));

		try (FileOutputStream fw = new FileOutputStream(fOut)) {
			try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fw, StandardCharsets.UTF_8))) {
				dao.exportFile(h, n, bw, fw);
			}
		}
	}

	private void exportZip(String dirOut, HashMap<String, ArrayList<String>> h, int n)
			throws IOException, ArcException, SQLException {
		ArrayList<String> fileName = h.get("file_name");

		File fOut = new File(dirOut + File.separator + fileName.get(n) + ".zip");

		try (FileOutputStream fw = new FileOutputStream(fOut)) {
			try (ZipOutputStream zos = new ZipOutputStream(fw)) {
				ZipEntry ze = new ZipEntry(fileName.get(n));
				zos.putNextEntry(ze);

				try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8))) {
					dao.exportFile(h, n, bw, fw);
					zos.flush();
					zos.closeEntry();
				}
			}
		}
	}

	private void exportGz(String dirOut, HashMap<String, ArrayList<String>> h, int n)
			throws IOException, ArcException, SQLException {
		ArrayList<String> fileName = h.get("file_name");

		File fOut = new File(dirOut + File.separator + fileName.get(n) + ".gz");

		try (FileOutputStream fw = new FileOutputStream(fOut)) {
			try (GZIPOutputStream gzos = new GZIPOutputStream(fw)) {
				try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(gzos, StandardCharsets.UTF_8))) {
					dao.exportFile(h, n, bw, fw);
					gzos.flush();
				}
			}
		}
	}

}
