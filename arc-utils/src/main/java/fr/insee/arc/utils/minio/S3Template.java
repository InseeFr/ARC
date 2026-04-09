package fr.insee.arc.utils.minio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import fr.insee.arc.utils.files.FileUtilsArc;
import fr.insee.arc.utils.utils.LoggerHelper;
import fr.insee.arc.utils.utils.ManipString;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.DownloadObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.UploadObjectArgs;
import io.minio.credentials.AwsEnvironmentProvider;
import io.minio.credentials.MinioEnvironmentProvider;
import io.minio.credentials.Provider;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.http.HttpUtils;
import io.minio.messages.Item;
import okhttp3.OkHttpClient;

public class S3Template {

	// for minio authentification type
	private static final String MINIO_ACCESS_KEY = "MINIO_ACCESS_KEY";
	private static final String MINIO_SECRET_KEY = "MINIO_SECRET_KEY";

	// for aws authentification type
	private static final String AWS_SESSION_TOKEN = "AWS_SESSION_TOKEN";
	private static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
	private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";

	private static final Logger LOGGER = LogManager.getLogger(S3Template.class);

	private static final String EXISTS_FILE = ".exists";

	protected static final long DEFAULT_CONNECTION_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

	private static final int MAX_ERROR_COUNT = 10;
	private int errorCount = 0;

	public S3Template(String s3ApiUri, String bucket, String directory, String accessKey, String secretKey) {
		super();
		this.s3ApiUri = s3ApiUri;
		this.bucket = bucket;
		this.directory = directory;

		if (isS3Off()) {
			return;
		}

		System.setProperty(MINIO_ACCESS_KEY, accessKey);
		System.setProperty(MINIO_SECRET_KEY, secretKey);

	}

	public S3Template(String s3ApiUri, String bucket, String directory, String accessKey, String secretKey,
			String sessionToken) {
		this.s3ApiUri = s3ApiUri;
		this.bucket = bucket;
		this.directory = directory;

		if (isS3Off()) {
			return;
		}

		System.setProperty(AWS_ACCESS_KEY_ID, accessKey);
		System.setProperty(AWS_SECRET_ACCESS_KEY, secretKey);
		System.setProperty(AWS_SESSION_TOKEN, sessionToken);

	}

	private String s3ApiUri;
	private String bucket;
	private String directory;

	// client http used by minio
	private OkHttpClient httpClient;

	// client minio
	private MinioClient minioClient;

	/**
	 * test if s3 in turned off
	 * 
	 * @return
	 */
	public boolean isS3Off() {
		return s3ApiUri.isEmpty();
	}

	/**
	 * Récupère le minioClient en le recréant si nécessaire.
	 * 
	 * @return minioClient
	 */
	public MinioClient getMinioClient() {
		if (this.minioClient == null) {
			try {
				buildMinioClient();
			} catch (KeyManagementException | NoSuchAlgorithmException e) {
				LoggerHelper.error(LOGGER, e);
			}
		}

		return minioClient;
	}

	private void buildMinioClient() throws KeyManagementException, NoSuchAlgorithmException {

		httpClient = newDefaultHttpClient();

		Provider credentialProvider = (System.getProperty(AWS_ACCESS_KEY_ID) == null) ? new MinioEnvironmentProvider()
				: new AwsEnvironmentProvider();

		this.minioClient = MinioClient.builder().endpoint(s3ApiUri).credentialsProvider(credentialProvider)
				.httpClient(httpClient).build();

		this.minioClient.ignoreCertCheck();
	}

	private static OkHttpClient newDefaultHttpClient() {

		OkHttpClient httpClient = new OkHttpClient().newBuilder()
				.connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
				.writeTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
				.readTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS).addInterceptor(new OkHttpInterceptor())
				.build();

		String filename = System.getenv("SSL_CERT_FILE");
		if (filename != null && !filename.isEmpty()) {
			try {
				httpClient = HttpUtils.enableExternalCertificates(httpClient, filename);
			} catch (GeneralSecurityException | IOException e) {
				throw new RuntimeException(e);
			}
		}
		return httpClient;
	}

	/**
	 * Crée un nouveau répertoire dans le bucket à l'emplacement donné
	 * 
	 * @param path le chemin du répertoire à créer
	 * @throws ArcException
	 */
	public void createDirectory(String path) throws ArcException {
		createDirectoryRecursively(absoluteDirectoryPath(path));
	}

	/**
	 * Crée un nouveau répertoire (et ses sous-répertoires jusqu'à la racine) dans
	 * le bucket à l'emplacement donné
	 * 
	 * @param path le chemin du répertoire à créer
	 * @throws ArcException
	 */
	private void createDirectoryRecursively(String path) throws ArcException {

		if (isS3Off())
			return;

		path = asDirectory(path); // ajouté au path si manquant
		int indexSecondLastSubstr = path.lastIndexOf("/", path.length() - 2); // index de l'avant dernier "/"
		String parentPath = asDirectory(path.substring(0, indexSecondLastSubstr + 1));
		if (!parentPath.equals(asDirectory(this.directory))) {
			createDirectoryRecursively(parentPath); // crée les répertoires parents
		}
		try {
			if (!isExists(path)) {
				getMinioClient().putObject(PutObjectArgs.builder().bucket(bucket).object(path + EXISTS_FILE)
						.stream(new ByteArrayInputStream(new byte[] { 0x01 }), 1, -1).build()); // fichier .exists
			}
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_WRITE_FAILED, path);
		}
	}

	/**
	 * normalize path given :\ is replace by s3 separator / double s3 path separator
	 * // is replaced by single s3 path separator /
	 * 
	 * @param path
	 * @return
	 */
	static String normalizePath(String path) {
		if (path == null || path.isBlank())
			return "";
		String reworkedPath = path.replace("\\", "/").replace("//", "/");
		reworkedPath = reworkedPath.charAt(0) == '/' ? reworkedPath.substring(1) : reworkedPath;
		return reworkedPath;
	}

	/**
	 * directory are being added an s3 path separator / at the end
	 * 
	 * @param path
	 * @return
	 */
	private static String asDirectory(String path) {
		String normalizedPath = normalizePath(path);
		return normalizedPath + (normalizedPath.endsWith("/") ? "" : "/");
	}

	/**
	 * path for files if the path provided is a directory with s3 path separator, it
	 * adds the directory indicator file .exists
	 * 
	 * @param path
	 * @return
	 */
	private static String asFile(String path) {
		String normalizedPath = normalizePath(path);
		return normalizedPath + (normalizedPath.endsWith("/") ? EXISTS_FILE : "");
	}

	/**
	 * adds root directory to path to make it absolute
	 * 
	 * @param path
	 * @return
	 */
	public String absolutePath(String path) {
		String normalizedPath = normalizePath(path);
		String normalizedDirectory = normalizePath(this.directory);
		if (normalizedDirectory == null || normalizedPath.startsWith(normalizedDirectory)) {
			return normalizedPath;
		}
		return normalizedDirectory + "/" + normalizedPath;
	}

	private String absoluteDirectoryPath(String path) {
		return absolutePath(asDirectory(path));
	}

	private String absoluteFilePath(String path) {
		return absolutePath(asFile(path));
	}

	private void exitWhenTooManyErrorsOnS3Operation(String pathFrom, String pathTo) throws ArcException {
		if (errorCount > MAX_ERROR_COUNT) {
			errorCount = 0;
			throw new ArcException(ArcExceptionMessage.FILE_S3_OPERATION_FAILED, pathFrom, pathTo);
		}
	}

	/**
	 * Copie un objet d'un bucket dans un autre emplacement de ce bucket
	 * 
	 * @param pathFrom le chemin de l'objet à copier
	 * @param pathTo   le chemin de la copie
	 * @throws ArcException
	 */
	public void copy(String pathFrom, String pathTo) throws ArcException {

		if (isS3Off())
			return;

		exitWhenTooManyErrorsOnS3Operation(pathFrom, pathTo);

		try {
			getMinioClient().copyObject(CopyObjectArgs.builder().bucket(bucket).object(absolutePath(pathTo))
					.source(CopySource.builder().bucket(bucket).object(absolutePath(pathFrom)).build()).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			LoggerHelper.error(LOGGER, e);
			copyRetry(pathFrom, pathTo);
		}

		if (!isExists(pathTo)) {
			copyRetry(pathFrom, pathTo);
		} else {
			LoggerHelper.warn(LOGGER, pathTo + " has been copied");
		}

	}

	private void copyRetry(String pathFrom, String pathTo) throws ArcException {
		errorCount++;
		new ArcException(ArcExceptionMessage.FILE_COPY_FAILED_RETRY, pathFrom, pathTo, errorCount)
				.logMessageException();
		closeMinioClient();
		copy(pathFrom, pathTo);
	}

	/**
	 * Copie un répertoire et son contenu d'un bucket dans un autre emplacement de
	 * ce bucket
	 *
	 * @param pathFrom le chemin du répertoire à copier
	 * @param pathTo   le chemin de la copie
	 * @throws ArcException
	 */
	public void copyDirectory(String pathFrom, String pathTo) throws ArcException {
		for (String fileFrom : listObjectsInDirectory(pathFrom, true, true, false)) {
			String fileTo = fileFrom.replace(pathFrom, pathTo);
			copy(fileFrom, fileTo);
		}
	}

	/**
	 * Télécharge un objet d'un bucket vers un emplacement hors S3
	 * 
	 * @param sourceS3Path   le chemin de l'objet à copier
	 * @param targetFilePath le fichier téléchargé
	 * @throws ArcException
	 */
	public void download(String sourceS3Path, String targetFilePath) throws ArcException {

		if (isS3Off())
			return;

		File targetFile = new File(targetFilePath);

		exitWhenTooManyErrorsOnS3Operation(sourceS3Path, targetFile.getAbsolutePath());

		// delete file if it already exists
		if (targetFile.exists()) {
			FileUtilsArc.delete(targetFile);
		}

		try {
			getMinioClient().downloadObject(DownloadObjectArgs.builder().bucket(bucket)
					.object(absolutePath(sourceS3Path)).filename(targetFile.getPath()).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			LoggerHelper.error(LOGGER, e);
			downloadRetry(sourceS3Path, targetFilePath);
		}

		// check if file exists
		if (!targetFile.exists()) {
			downloadRetry(sourceS3Path, targetFilePath);
		} else {
			LoggerHelper.warn(LOGGER, sourceS3Path + " has been downloaded");
		}

	}

	public void downloadRetry(String sourceS3Path, String targetFilePath) throws ArcException {
		errorCount++;
		new ArcException(ArcExceptionMessage.FILE_COPY_FAILED_RETRY, sourceS3Path, targetFilePath, errorCount)
				.logMessageException();
		closeMinioClient();
		download(sourceS3Path, targetFilePath);
	}

	public void downloadToDirectory(String sourceS3Path, String targetDirectoryPath) throws ArcException {

		if (isS3Off())
			return;

		String targetFilePath = targetDirectoryPath + File.separator
				+ ManipString.substringAfterLast(sourceS3Path, "/");

		download(sourceS3Path, targetFilePath);

	}

	/**
	 * Uploade un fichier d'un emplacement hors S3 vers un emplacement du bucket
	 * 
	 * @param fileFrom le fichier à uploader
	 * @param pathTo   le chemin vers lequel uploader
	 * @throws ArcException
	 */
	public void upload(File fileFrom, String pathTo) throws ArcException {

		if (isS3Off())
			return;

		exitWhenTooManyErrorsOnS3Operation(fileFrom.getAbsolutePath(), pathTo);

		try {
			getMinioClient().uploadObject(UploadObjectArgs.builder().bucket(bucket).object(absolutePath(pathTo))
					.filename(fileFrom.getPath()).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			LoggerHelper.error(LOGGER, e);
			uploadRetry(fileFrom, pathTo);
		}

		if (!isExists(pathTo)) {
			uploadRetry(fileFrom, pathTo);
		} else {
			LoggerHelper.warn(LOGGER, pathTo + " has been uploaded");
		}

	}

	private void uploadRetry(File fileFrom, String pathTo) throws ArcException {
		errorCount++;
		new ArcException(ArcExceptionMessage.FILE_COPY_FAILED_RETRY, fileFrom.getAbsolutePath(), pathTo, errorCount)
				.logMessageException();
		closeMinioClient();
		upload(fileFrom, pathTo);
	}

	/**
	 * Supprime un objet d'un bucket
	 * 
	 * @param path le chemin de l'objet à supprimer
	 * @throws ArcException
	 */
	public void delete(String path) throws ArcException {

		if (isS3Off())
			return;

		exitWhenTooManyErrorsOnS3Operation(path, path);

		try {
			getMinioClient().removeObject(RemoveObjectArgs.builder().bucket(bucket).object(absolutePath(path)).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {
			LoggerHelper.error(LOGGER, e);
			deleteRetry(path);
		}

		if (isExists(path)) {
			deleteRetry(path);
		} else {
			LoggerHelper.warn(LOGGER, path + " has been deleted");
		}

	}

	private void deleteRetry(String path) throws ArcException {
		errorCount++;
		new ArcException(ArcExceptionMessage.FILE_DELETE_FAILED_RETRY, path, errorCount).logMessageException();
		closeMinioClient();
		delete(path);
	}

	/**
	 * Supprime une liste d'objets
	 * 
	 * @param paths le chemin des objets à supprimer (emplacements et noms)
	 * @throws ArcException
	 */
	public void delete(List<String> paths) throws ArcException {
		for (String path : paths) {
			delete(path);
		}
	}

	/**
	 * Supprime un répertoire d'un bucket avec son contenu
	 * 
	 * @param path le chemin du répertoire à supprimer
	 * @throws ArcException
	 */
	public void deleteDirectory(String path) throws ArcException {
		delete(listObjectsInDirectory(path, true, true, false));
	}

	/**
	 * Déplace un objet d'un bucket vers un nouvel emplacement dans le même bucket.
	 * Cette méthode peut être aussi utilisée pour renommer un objet.
	 * 
	 * @param pathFrom le chemin de l'objet à déplacer
	 * @param pathTo   le nouveau chemin de l'objet
	 * @throws ArcException
	 */
	public void move(String pathFrom, String pathTo) throws ArcException {
		copy(pathFrom, pathTo);
		delete(pathFrom);
	}

	/**
	 * Déplace un répertoire et son contenu d'un bucket vers un nouvel emplacement
	 * dans le même bucket. Cette méthode peut être aussi utilisée pour renommer un
	 * répertoire.
	 *
	 * @param pathFrom le chemin du répertoire à déplacer
	 * @param pathTo   le nouveau chemin du répertoire
	 * @throws ArcException
	 */
	public void moveDirectory(String pathFrom, String pathTo) throws ArcException {
		copyDirectory(pathFrom, pathTo);
		deleteDirectory(pathFrom);
	}

	/**
	 * Renvoie la taille en octets d'un objet d'un bucket
	 * 
	 * @param path le chemin de l'objet à mesurer
	 * @return la taille en nombre d'octets de l'objet
	 * @throws ArcException
	 */
	public long size(String path) throws ArcException {
		if (isDirectory(path)) {
			return size(listObjectsInDirectory(path, true, true, false));
		}
		StatObjectResponse statObject;
		try {
			statObject = getMinioClient()
					.statObject(StatObjectArgs.builder().bucket(bucket).object(absoluteFilePath(path)).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, path);
		}

		return statObject.size();
	}

	/**
	 * Renvoie la taille totale en octets d'une liste d'objets
	 * 
	 * @param paths le chemin des objets à mesurer (emplacements et noms)
	 * @return la taille en nombre d'octets des objets
	 * @throws ArcException
	 */
	public long size(List<String> paths) throws ArcException {
		long sizeTotal = 0;
		for (String path : paths) {
			sizeTotal += size(path);
		}
		return sizeTotal;
	}

	/**
	 * Vérifie qu'un objet existe dans le bucket au chemin donné
	 * 
	 * @param path le chemin à vérifier
	 * @return vrai si un objet existe avec ce chemin
	 * @throws ArcException
	 */
	public boolean isExists(String path) {
		boolean found = false;
		try {
			@SuppressWarnings("unused")

			StatObjectResponse statObject = getMinioClient()
					.statObject(StatObjectArgs.builder().bucket(bucket).object(absoluteFilePath(path)).build());
			found = true;
		} catch (ErrorResponseException e) {
			int errorCode = e.response().code();
			if (errorCode != 404) {
				LoggerHelper.error(LOGGER, e, "Erreur lors de l'interrogation S3. Code HTTP : ", errorCode);
			}
		} catch (InvalidKeyException | InsufficientDataException | InternalException | InvalidResponseException
				| NoSuchAlgorithmException | ServerException | XmlParserException | IllegalArgumentException
				| IOException e) {

			LoggerHelper.error(LOGGER, e, "Erreur lors de l'interrogation S3");
		}
		return found;
	}

	/**
	 * Indique si le chemin en entrée est un répertoire qui existe.
	 * 
	 * @param path le chemin à tester
	 * @return vrai si le chemin est celui d'un répertoire qui existe
	 * @throws ArcException
	 */
	public boolean isDirectory(String path) {
		return isExists(path) && path.endsWith("/");
	}

	/**
	 * Liste l'ensemble des objets présents dans un répertoire
	 * 
	 * @param path           le chemin du répertoire
	 * @param isRecursive    si true, inclut les objets des sous-répertoires
	 * @param includeExists  si true, inclut les fichiers .exists
	 * @param includeSubdirs si true, inclut les noms des sous-répertoires
	 * @return la liste des objets du répertoire
	 * @throws ArcException
	 */
	public List<String> listObjectsInDirectory(String path, Boolean isRecursive, Boolean includeExists,
			Boolean includeSubdirs) throws ArcException {

		if (isS3Off())
			return new ArrayList<>();

		List<String> listNames = new ArrayList<>();
		Iterator<Result<Item>> listObject = getMinioClient().listObjects(ListObjectsArgs.builder().bucket(bucket)
				.prefix(absoluteDirectoryPath(path)).recursive(isRecursive).build()).iterator();
		while (listObject.hasNext()) {
			try {
				String nextObject = listObject.next().get().objectName();
				if ((includeExists || !nextObject.endsWith(EXISTS_FILE))
						&& (includeSubdirs || !nextObject.endsWith("/"))) {
					listNames.add(nextObject);
				}
			} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
					| InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
					| XmlParserException | IOException e) {

				throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, path);
			}
		}
		return listNames;
	}

	/**
	 * Liste l'ensemble des objets présents dans un répertoire, en excluant le
	 * contenu des sous-répertoires
	 * 
	 * @param path le chemin du répertoire
	 * @return la liste des objets et sous-répertoires du répertoire
	 * @throws ArcException
	 */
	public List<String> listObjectsInDirectory(String path) throws ArcException {
		return listObjectsInDirectory(path, false, false, false);
	}

	public void closeMinioClient() {
		if (isS3Off())
			return;
		httpClient.dispatcher().executorService().shutdown();
		httpClient.connectionPool().evictAll();
		try {
			if (httpClient.cache() != null) {
				httpClient.cache().close();
			}
		} catch (IOException e) {
			LoggerHelper.error(LOGGER, e);
		}
		this.minioClient = null;
	}

}
