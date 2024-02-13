package fr.insee.arc.utils.minio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
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
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;
import okhttp3.OkHttpClient;

public class S3Template {

	public S3Template(String s3ApiUri, String bucket, String accessKey, String secretKey) {
		super();
		this.s3ApiUri = s3ApiUri;
		this.bucket = bucket;
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	private String s3ApiUri;
	private String bucket;
	private String accessKey;
	private String secretKey;

	private MinioClient minioClient;

	private OkHttpClient httpClient;

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
				e.printStackTrace();
			}
		}

		return minioClient;
	}

	private void buildMinioClient() throws KeyManagementException, NoSuchAlgorithmException {
		httpClient = new OkHttpClient().newBuilder().build();
		this.minioClient = MinioClient.builder().endpoint(s3ApiUri).credentials(accessKey, secretKey)
				.httpClient(httpClient).build();
		this.minioClient.ignoreCertCheck();
	}

	/**
	 * Crée un nouveau répertoire dans le bucket à l'emplacement donné
	 * 
	 * @param path le chemin du répertoire à créer (emplacement et nom)
	 * @throws ArcException
	 */
	public void createDirectory(String path) throws ArcException {
		try {
			getMinioClient()
					.putObject(PutObjectArgs.builder().bucket(bucket).object(path + (path.endsWith("/") ? "" : "/"))
							.stream(new ByteArrayInputStream(new byte[] {}), 0, -1).build()); // répertoire
			getMinioClient().putObject(
					PutObjectArgs.builder().bucket(bucket).object(path + (path.endsWith("/") ? "" : "/") + ".exists")
							.stream(new ByteArrayInputStream(new byte[] { 0x01 }), 1, -1).build()); // fichier .exists
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_WRITE_FAILED, path);
		}
	}

	/**
	 * Copie un objet d'un bucket dans un autre emplacement de ce bucket
	 * 
	 * @param pathFrom le chemin de l'objet à copier (emplacement et nom)
	 * @param pathTo   le chemin de la copie (emplacement et nom)
	 * @throws ArcException
	 */
	public void copy(String pathFrom, String pathTo) throws ArcException {
		try {
			getMinioClient().copyObject(CopyObjectArgs.builder().bucket(bucket).object(pathTo)
					.source(CopySource.builder().bucket(bucket).object(pathFrom).build()).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_COPY_FAILED, pathFrom, pathTo);
		}
	}

	/**
	 * Télécharge un objet d'un bucket vers un emplacement hors S3
	 * 
	 * @param pathFrom le chemin de l'objet à copier (emplacement et nom)
	 * @param fileTo   le fichier téléchargé (emplacement et nom)
	 * @throws ArcException
	 */
	public void download(String pathFrom, File fileTo) throws ArcException {
		try {
			getMinioClient().downloadObject(
					DownloadObjectArgs.builder().bucket(bucket).object(pathFrom).filename(fileTo.getPath()).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_COPY_FAILED, pathFrom, fileTo.getName());
		}
	}

	/**
	 * Uploade un fichier d'un emplacement hors S3 vers un emplacement du bucket
	 * 
	 * @param fileFrom le fichier à uploader (emplacement et nom)
	 * @param pathTo   le chemin vers lequel uploader (emplacement et nom)
	 * @throws ArcException
	 */
	public void upload(File fileFrom, String pathTo) throws ArcException {
		try {
			getMinioClient().uploadObject(
					UploadObjectArgs.builder().bucket(bucket).object(pathTo).filename(fileFrom.getPath()).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_COPY_FAILED, fileFrom.getName(), pathTo);
		}
	}

	/**
	 * Supprime un objet d'un bucket
	 * 
	 * @param path le chemin de l'objet à supprimer (emplacement et nom)
	 * @throws ArcException
	 */
	public void delete(String path) throws ArcException {
		try {
			getMinioClient().removeObject(RemoveObjectArgs.builder().bucket(bucket).object(path).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_DELETE_FAILED, path);
		}
	}

	public void delete(List<String> paths) throws ArcException {
		for (String path : paths) {
			delete(path);
		}
	}

	/**
	 * Supprime un répertoire d'un bucket avec son contenu
	 * 
	 * @param path le chemin du répertoire à supprimer (emplacement et nom)
	 * @throws ArcException
	 */
	public void deleteDirectory(String path) throws ArcException {
		path += (path.endsWith("/") ? "" : "/");
		delete(path);
		delete(listObjectsInDirectory(path));
	}

	/**
	 * Déplace un objet d'un bucket vers un nouvel emplacement dans le même bucket.
	 * Cette méthode peut être aussi utilisée pour renommer un objet.
	 * 
	 * @param pathFrom le chemin de l'objet à déplacer (emplacement et nom)
	 * @param pathTo   le nouveau chemin de l'objet (emplacement et nom)
	 * @throws ArcException
	 */
	public void move(String pathFrom, String pathTo) throws ArcException {
		copy(pathFrom, pathTo);
		delete(pathFrom);
	}

	/**
	 * Renvoie la taille en octets d'un objet d'un bucket
	 * 
	 * @param path le chemin de l'objet à mesurer (emplacement et nom)
	 * @return la taille en nombre d'octets de l'objet
	 * @throws ArcException
	 */
	public long size(String path) throws ArcException {

		StatObjectResponse statObject;
		try {
			statObject = getMinioClient().statObject(StatObjectArgs.builder().bucket(bucket).object(path).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, path);
		}

		return statObject.size();
	}

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
	 * @param path le chemin à vérifier (emplacement et nom)
	 * @return vrai si un objet existe avec ce chemin
	 * @throws ArcException
	 * @throws
	 */
	public boolean isExists(String path) throws ArcException {
		@SuppressWarnings("unused")
		StatObjectResponse statObject;
		boolean found;
		try {
			statObject = getMinioClient().statObject(StatObjectArgs.builder().bucket(bucket)
					.object(path + (path.endsWith("/") ? ".exists" : "")).build());
			found = true;
		} catch (ErrorResponseException e) {
			if (e.response().code() != 404) {
				throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, path);
			}
			found = false;
		} catch (InvalidKeyException | InsufficientDataException | InternalException | InvalidResponseException
				| NoSuchAlgorithmException | ServerException | XmlParserException | IllegalArgumentException
				| IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, path);
		}
		return found;
	}

	public List<String> listObjectsInDirectory(String path) throws ArcException {
		List<String> listNames = new ArrayList<>();
		Iterator<Result<Item>> listObject = getMinioClient()
				.listObjects(ListObjectsArgs.builder().bucket(bucket).prefix(path).build()).iterator();
		while (listObject.hasNext()) {
			try {
				listNames.add(listObject.next().get().objectName());
			} catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException
					| InternalException | InvalidResponseException | NoSuchAlgorithmException | ServerException
					| XmlParserException | IOException e) {

				throw new ArcException(ArcExceptionMessage.FILE_READ_FAILED, path);
			}
		}
		return listNames;
	}

	public void closeMinioClient() {
		httpClient.dispatcher().executorService().shutdown();
		httpClient.connectionPool().evictAll();
		this.minioClient = null;
	}

}
