package fr.insee.arc.core.service.p1reception.registerfiles.dao;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import fr.insee.arc.core.dataobjects.ArcPreparedStatementBuilder;
import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.core.dataobjects.ViewEnum;
import fr.insee.arc.core.model.TraitementEtat;
import fr.insee.arc.core.model.TraitementOperationFichier;
import fr.insee.arc.core.model.TraitementPhase;
import fr.insee.arc.core.model.TraitementTypeFichier;
import fr.insee.arc.core.service.global.bo.ArcDateFormat;
import fr.insee.arc.core.service.global.bo.Sandbox;
import fr.insee.arc.core.service.global.dao.TableOperations;
import fr.insee.arc.core.service.p1reception.provider.DirectoriesReception;
import fr.insee.arc.core.service.p1reception.provider.DirectoryPath;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FileDescriber;
import fr.insee.arc.core.service.p1reception.registerarchive.bo.FilesDescriber;
import fr.insee.arc.core.service.p1reception.registerfiles.provider.ContainerName;
import fr.insee.arc.utils.dao.SQL;
import fr.insee.arc.utils.dao.UtilitaireDao;
import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.files.CompressedUtils;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.structure.GenericBean;
import fr.insee.arc.utils.utils.FormatSQL;
import fr.insee.arc.utils.utils.ManipString;

public class FileRegistrationDao {

	public FileRegistrationDao(Sandbox sandbox, String tablePilTemp) {
		this.sandbox = sandbox;
		this.tablePilTemp = tablePilTemp;
		this.tablePil = ViewEnum.PILOTAGE_FICHIER.getFullName(sandbox.getSchema());
		this.directories = new DirectoriesReception(sandbox);
	}

	private Sandbox sandbox;
	private String tablePilTemp;
	private String tablePil;
	private DirectoriesReception directories;

	public void createTemporaryResultTable() throws ArcException {
		StringBuilder requete = new StringBuilder();
		requete.append(FormatSQL.dropTable(this.tablePilTemp));
		requete.append(TableOperations.creationTableResultat(this.tablePil, this.tablePilTemp));
		UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), requete);

	}

	public void execQueryRegisterFiles(FilesDescriber archiveContent) throws ArcException {

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		String dirIn = directories.getDirectoryReceptionEnCours();

		for (FileDescriber f : archiveContent.getFilesAttribute()) {

			f.setContainerNewName(ContainerName.buildContainerName(f.getContainerName()));

			// archive correcte dans ok
			if (f.getTypeOfFile().equals(TraitementTypeFichier.DA)) {
				queryInsertFileDescriberInPilotage(requete, f);
			}

			// archive correcte dans ok
			if (f.getTypeOfFile().equals(TraitementTypeFichier.A)) {
				String dirOut = DirectoryPath.directoryReceptionEtat(directories.getDirectoryRoot(),
						sandbox.getSchema(), f.getEtat());
				FileUtilsArc.deplacerFichier(dirIn, dirOut, f.getContainerName(), f.getContainerNewName());
			}

			// archive corrompue vers ko
			if (f.getTypeOfFile().equals(TraitementTypeFichier.AC)) {
				String dirOut = DirectoryPath.directoryReceptionEtat(this.directories.getDirectoryRoot(),
						sandbox.getSchema(), f.getEtat());
				FileUtilsArc.deplacerFichier(dirIn, dirOut, f.getContainerName(), f.getContainerNewName());
				queryInsertFileDescriberInPilotage(requete, f);
			}

			// pour les fichier seul, on en fait une archive
			if (f.getTypeOfFile().equals(TraitementTypeFichier.D)) {
				String dirOut = DirectoryPath.directoryReceptionEtatOK(this.directories.getDirectoryRoot(),
						sandbox.getSchema());
				File fileIn = new File(dirIn + File.separator + f.getFileName());
				File fileOut = new File(dirOut + File.separator + f.getContainerNewName());

				if (fileOut.exists()) {
					FileUtilsArc.delete(fileOut);
				}
				CompressedUtils.generateTarGzFromFile(fileIn, fileOut,
						ManipString.substringAfterFirst(fileIn.getName(), "_"));
				FileUtilsArc.delete(fileIn);
				queryInsertFileDescriberInPilotage(requete, f);

			}
			
			if (requete.getParameters().size()>FormatSQL.MAXIMUM_NUMBER_OF_BIND_IN_PREPARED_STATEMENT)
			{
				requete.build(SQL.END_QUERY, SQL.COMMIT, SQL.END_QUERY);
				UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), requete);
				requete = new ArcPreparedStatementBuilder();
			}
			
		}
		requete.append(SQL.END_QUERY);

		queryUpdateArchiveWithoutFileName(requete);

		UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), requete);

	}

	/**
	 * build query that inserts the file attributes in pilotage table
	 * 
	 * @param requete
	 * @param f
	 */
	private void queryInsertFileDescriberInPilotage(ArcPreparedStatementBuilder requete, FileDescriber f) {
		String originalContainer = f.getContainerName();
		String newContainer = f.getContainerNewName();
		String virtualContainer = f.getVirtualContainer();
		String fileName = f.getFileName();
		TraitementEtat etat = f.getEtat();
		String rapport = f.getReport();

		Date d = new Date();

		// si ko, etape vaut 2
		String etape = etat.equals(TraitementEtat.KO) ? "2" : "1";

		if (requete.length() == 0) {
			requete.append("INSERT INTO " + tablePilTemp + " ");
			requete.append("(o_container, container, v_container, " + ColumnEnum.ID_SOURCE.getColumnName()
					+ ", date_entree,phase_traitement,etat_traitement,date_traitement, rapport, nb_enr, etape) VALUES ");
		} else {
			requete.append("\n,");
		}
		requete.append(" (");
		requete.appendText(originalContainer);
		requete.append(",").appendText(newContainer);
		requete.append(",").appendText(virtualContainer);
		requete.append(",").appendText(fileName);
		requete.append(",").appendText(
				new SimpleDateFormat(ArcDateFormat.DATE_HOUR_FORMAT_CONVERSION.getApplicationFormat()).format(d));
		requete.append(",").appendText(TraitementPhase.RECEPTION.toString());
		requete.append(",").appendText("{" + etat + "}");
		requete.append(",to_timestamp(")
			.appendText(new SimpleDateFormat(ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getApplicationFormat()).format(d))
			.append(",").appendText(ArcDateFormat.TIMESTAMP_FORMAT_CONVERSION.getDatastoreFormat())
		.append(")");
		requete.append(",").appendText(rapport);
		requete.append(",1");
		requete.append("," + etape);
		requete.append(") ");
	}

	/**
	 * Build query to update query without filename
	 * 
	 * @param requete
	 */
	private void queryUpdateArchiveWithoutFileName(ArcPreparedStatementBuilder requete) {
		// pb des archives sans nom de fichier
		requete.append("UPDATE " + this.tablePilTemp + " set " + ColumnEnum.ID_SOURCE.getColumnName() + "='' where "
				+ ColumnEnum.ID_SOURCE.getColumnName() + " is null; ");
	}

	/**
	 * Build query to the find files in pilotage table that are marked as replayed and also found among the new received files
	 * to be replayed
	 * 
	 * @return
	 */
	private ArcPreparedStatementBuilder querySelectFilesMarkedToReplay() {
		ArcPreparedStatementBuilder query = new ArcPreparedStatementBuilder();
		query.build(SQL.SELECT, ColumnEnum.ID_SOURCE.getColumnName());
		query.build(SQL.FROM, "(");
		query.build(SQL.SELECT, SQL.DISTINCT, ColumnEnum.ID_SOURCE.getColumnName());
		query.build(SQL.FROM, this.tablePil);
		query.build(SQL.WHERE, ColumnEnum.TO_DELETE.getColumnName(), "=", query.quoteText(TraitementOperationFichier.R.getDbValue()));
		query.build(") a");
		query.build(SQL.WHERE, SQL.EXISTS);
		query.build("(", SQL.SELECT, SQL.FROM, this.tablePilTemp, " b ");
		query.build(SQL.WHERE, ColumnEnum.ID_SOURCE.alias("a"), "=", ColumnEnum.ID_SOURCE.alias("b"));
		query.build(")");
		
		return query;
	}

	/**
	 * return the list of files to be replayed
	 * 
	 * @return
	 * @throws ArcException
	 */
	public List<String> execQuerySelectFilesMarkedToReplay() throws ArcException {
		return new GenericBean(UtilitaireDao.get(0).executeRequest(sandbox.getConnection(),
				querySelectFilesMarkedToReplay()))
				.getColumnValues(ColumnEnum.ID_SOURCE.getColumnName());
	}

	/**
	 * Delete from the pilotage table the files registered and which are meant to be
	 * replayed
	 * 
	 * @param idSourceMarkedToReplay
	 * @throws ArcException
	 */
	public void execQueryDeleteFilesMarkedToReplay(List<String> idSourceMarkedToReplay) throws ArcException {

		if (idSourceMarkedToReplay.isEmpty()) {
			return;
		}
		StringBuilder requete = new StringBuilder();
		// marque les fichiers à effacer (ils vont etre rechargés)
		requete.append("CREATE TEMPORARY TABLE a_rejouer " + FormatSQL.WITH_NO_VACUUM + " AS ");
		requete.append(querySelectFilesMarkedToReplay());
		requete.append(";");

		// effacer de la table pilotage des to_delete à R
		requete.append("DELETE FROM " + this.tablePil + " a using a_rejouer b where a."
				+ ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName() + "; ");
		UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), requete);
	}

	/**
	 * insert registered files into the global pilotage table
	 * 
	 * @throws ArcException
	 */
	public void execQueryInsertRegisteredFilesInPilotage() throws ArcException {

		StringBuilder requete = new StringBuilder();
		requete.append("INSERT INTO " + this.tablePil + " select * from " + this.tablePilTemp + "; \n");
		requete.append("DISCARD TEMP; \n");
		UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), requete);
	}

	/**
	 * insert registered files into the temporary pilotage table
	 * 
	 * @param registeredFile
	 * @throws ArcException
	 */
	public void execQueryTemporaryInsertRegisteredFiles(FilesDescriber registeredFile) throws ArcException {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		
		boolean first = true;
		// insertion des fichiers enregitrés dans la table tablePilTemp
		for (FileDescriber f : registeredFile.getFilesAttribute()) {
			if (f.getFileName() != null) {
				if (first)
				{
					requete.append("insert into " + this.tablePilTemp + " (container, "
							+ ColumnEnum.ID_SOURCE.getColumnName() + ") values (" + requete.quoteText(f.getContainerName())
							+ "," + requete.quoteText(f.getFileName()) + ")");
					first = false;
				}
				else
				{
					requete.append(",(" + requete.quoteText(f.getContainerName())+ "," + requete.quoteText(f.getFileName()) + ")");
				}
			}
			if (requete.getParameters().size()>FormatSQL.MAXIMUM_NUMBER_OF_BIND_IN_PREPARED_STATEMENT)
			{
				requete.build(SQL.END_QUERY, SQL.COMMIT, SQL.END_QUERY);
				UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), requete);			
				requete = new ArcPreparedStatementBuilder();
				first = true;
			}
		}
		UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(), requete);
	}

	/**
	 * find duplicate files
	 * 
	 * @return
	 * @throws ArcException
	 */
	public List<String> execQueryFindDuplicateFiles() throws ArcException {
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		// bloc recherche de doublon dans les fichiers recus
		requete.append("select container, " + ColumnEnum.ID_SOURCE.getColumnName() + " FROM " + this.tablePilTemp
				+ " where " + ColumnEnum.ID_SOURCE.getColumnName() + " in ( ");
		requete.append("select distinct " + ColumnEnum.ID_SOURCE.getColumnName() + " from ( ");
		requete.append("select " + ColumnEnum.ID_SOURCE.getColumnName() + ", count(1) over (partition by "
				+ ColumnEnum.ID_SOURCE.getColumnName() + ") as n from " + this.tablePilTemp + " ");
		requete.append(") ww where n>1 ");
		requete.append(") ");
		// bloc recherche des doublons de fichiers entre les fichiers recus et les
		// fichiers présent dans la table de pilotage qui ne sont pas marqués à rejouer
		requete.append("UNION ");
		requete.append(
				"SELECT container, " + ColumnEnum.ID_SOURCE.getColumnName() + " from " + this.tablePilTemp + " a ");
		requete.append("where exists (select 1 from " + this.tablePil + " b where a."
				+ ColumnEnum.ID_SOURCE.getColumnName() + "=b." + ColumnEnum.ID_SOURCE.getColumnName() + ") \n");
		requete.append("and a." + ColumnEnum.ID_SOURCE.getColumnName() + " not in (select distinct "
				+ ColumnEnum.ID_SOURCE.getColumnName());
		requete.append(" from " + this.tablePil + " b");
		requete.append(" where b.to_delete="+requete.quoteText(TraitementOperationFichier.R.getDbValue())+") ;\n");

		// récupérer les doublons pour mettre à jour le dispatcher
		return new GenericBean(
				UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), requete))
				.getColumnValues(ColumnEnum.ID_SOURCE.getColumnName());
	}

	
	/**
	 * In the received files, found the ones that are marked to be replayed
	 * Return their container name and their file name in the container  
	 * @param listContainerARejouer
	 * @param listIdsourceARejouer
	 * @throws ArcException
	 */
	public void execQueryFindFilesMarkedAsReplay(List<String> listContainerARejouer, List<String> listIdsourceARejouer)
			throws ArcException {

		// on ignore les doublons pour les fichiers à rejouer
		// on recrée un nouvelle liste en ne lui ajoutant pas ces doublons à ignorer
		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();

		requete.build(SQL.SELECT, "container", ",");
		requete.build("container||'" + File.separator + "'||" + ColumnEnum.ID_SOURCE.getColumnName(), SQL.AS, ColumnEnum.ID_SOURCE.getColumnName());
		requete.build(SQL.FROM, this.tablePilTemp, " a ");
		requete.build(SQL.WHERE, SQL.EXISTS, "(");
		requete.build(	SQL.SELECT, SQL.FROM, this.tablePil, " b ");
		requete.build(	SQL.WHERE, ColumnEnum.TO_DELETE.getColumnName(), "=", requete.quoteText(TraitementOperationFichier.R.getDbValue()));
		requete.build(	SQL.AND, ColumnEnum.ID_SOURCE.alias("a"), "=", ColumnEnum.ID_SOURCE.alias("b"));
		requete.build(")");

		GenericBean m = new GenericBean(UtilitaireDao.get(0).executeRequest(this.sandbox.getConnection(),
				requete));

		if (!m.isEmpty()) {
			listContainerARejouer.addAll(m.getColumnValues(ColumnEnum.CONTAINER.getColumnName()));
			listIdsourceARejouer.addAll(m.getColumnValues(ColumnEnum.ID_SOURCE.getColumnName()));
		}


		
	}

	public void execQueryInsertCorruptedArchiveInPilotage(FilesDescriber content) throws ArcException {

		ArcPreparedStatementBuilder requete = new ArcPreparedStatementBuilder();
		// insertion des fichiers d'archive corrompue dans la table
		// tablePilTemp
		// on doit aussi leur donner un numéro
		for (FileDescriber z : content.getFilesAttribute()) {
			if (z.getTypeOfFile().equals(TraitementTypeFichier.AC)) {
				requete.append("insert into " + this.tablePilTemp + " (container, "
						+ ColumnEnum.ID_SOURCE.getColumnName() + ") values (" + requete.quoteText(z.getContainerName())
						+ "," + requete.quoteText(z.getFileName()) + "); \n");
			}
		}
		UtilitaireDao.get(0).executeImmediate(this.sandbox.getConnection(), requete);
	}

	public void execQueryVersionDuplicateArchives(List<String> listContainerDoublons,
			List<String> listVersionContainerDoublons) throws ArcException {
		StringBuilder requete = new StringBuilder();

		requete.append("select container ");
		requete.append(" , coalesce((select max(v_container::integer)+1 from  " + this.tablePil
				+ " b where a.container=b.o_container),1)::text as v_container ");
		requete.append(
				"from (select distinct container from " + this.tablePilTemp + " where container is not null) a ");

		GenericBean m = new GenericBean(
				UtilitaireDao.get(0).executeRequest(sandbox.getConnection(), new ArcPreparedStatementBuilder(requete)));
		listContainerDoublons.addAll(m.getColumnValues(ColumnEnum.CONTAINER.getColumnName()));
		listVersionContainerDoublons.addAll(m.getColumnValues(ColumnEnum.V_CONTAINER.getColumnName()));

	}

}
