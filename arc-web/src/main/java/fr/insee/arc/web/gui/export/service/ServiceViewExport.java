package fr.insee.arc.web.gui.export.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import fr.insee.arc.utils.dao.UtilitaireDao;
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
			HashMap<String, ArrayList<String>> rules = dao.startExportRetrieve(viewExport);

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
			views.getViewExport().setMessage("Export failed because of database query");
		} catch (IOException e) {
			views.getViewExport().setMessage("Export failed because of file system problem");
		}

		return generateDisplay(model, RESULT_SUCCESS);
	}

	private void exportPlainText(String dirOut, HashMap<String, ArrayList<String>> h, int n)
			throws IOException, ArcException, SQLException {
		ArrayList<String> fileName = h.get("file_name");

		File fOut = new File(dirOut + File.separator + fileName.get(n));

		try (FileOutputStream fw = new FileOutputStream(fOut)) {
			try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fw, StandardCharsets.UTF_8))) {
				exportFile(h, n, bw, fw);
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
					exportFile(h, n, bw, fw);
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
					exportFile(h, n, bw, fw);
					gzos.flush();
				}
			}
		}
	}

	private void exportFile(HashMap<String, ArrayList<String>> h, int n, BufferedWriter bw, FileOutputStream fw)
			throws ArcException, IOException, SQLException {
		ArrayList<String> tablesToExport = h.get("table_to_export");
		ArrayList<String> headers = h.get("headers");
		ArrayList<String> nulls = h.get("nulls");
		ArrayList<String> filterTable = h.get("filter_table");
		ArrayList<String> orderTable = h.get("order_table");
		ArrayList<String> howToExport = h.get("nomenclature_export");
		ArrayList<String> headersToScan = h.get("columns_array_header");
		ArrayList<String> valuesToScan = h.get("columns_array_value");

		HashMap<String, Integer> pos = new HashMap<>();
		ArrayList<String> headerLine = new ArrayList<>();

		h = dao.exportFileRetrieve(n, howToExport, tablesToExport, getBacASable());

		for (int i = 0; i < h.get("varbdd").size(); i++) {
			pos.put(h.get("varbdd").get(i), Integer.parseInt(h.get("pos").get(i)));
			headerLine.add(h.get("varbdd").get(i));
		}

		// write header line if required
		if (!StringUtils.isEmpty(headers.get(n))) {
			for (String o : headerLine) {
				bw.write(o + ";");
			}
			bw.write("\n");
		}

		int maxPos = Integer.parseInt(h.get("maxp").get(0));

		Connection c = UtilitaireDao.get(0).getDriverConnexion();
		c.setAutoCommit(false);

		Statement stmt = c.createStatement();
		stmt.setFetchSize(5000);

		try (ResultSet res = dao.exportFileFilteredOrdered(stmt, n, tablesToExport, filterTable, orderTable, getBacASable())) {
			ResultSetMetaData rsmd = res.getMetaData();

			ArrayList<String> output;
			String[] tabH;
			String[] tabV;
			String colName;
			while (res.next()) {
				// reinitialiser l'arraylist de sortie
				output = new ArrayList<String>();
				for (int k = 0; k < maxPos; k++) {
					output.add("");
				}

				boolean todo = false;
				tabH = null;
				tabV = null;
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					colName = rsmd.getColumnLabel(i).toLowerCase();

					todo = true;
					// cas ou on est dans un tableau
					if (todo && colName.equals(headersToScan.get(n))) {
						todo = false;
						tabH = (String[]) res.getArray(i).getArray();
					}
					if (todo && colName.equals(valuesToScan.get(n))) {
						todo = false;
						tabV = (String[]) res.getArray(i).getArray();
					}
					if (todo) {
						todo = false;
						if (pos.get(colName) != null) {
							// if nulls value musn't be quoted as "null" and element is null then don't write
							if (!(StringUtils.isEmpty(nulls.get(n)) && StringUtils.isEmpty(res.getString(i)))) {
								output.set(pos.get(colName), res.getString(i));
							}
						}
					}
				}

				// traitement des variables tableaux
				if (tabH != null && tabV != null) {
					for (int k = 0; k < tabH.length; k++) {
						if (pos.get(tabH[k].toLowerCase()) != null) {
							// if nulls value musn't be quoted as "null" and element is null then don't write
							if (!(StringUtils.isEmpty(nulls.get(n)) && StringUtils.isEmpty(tabV[k]))) {
								output.set(pos.get(tabH[k].toLowerCase()), tabV[k]);
							}
						}
					}
				}

				for (String o : output) {
					bw.write(o + ";");
				}
				bw.write("\n");
			}
		}
		c.close();
		bw.flush();
		fw.flush();

	}

}
