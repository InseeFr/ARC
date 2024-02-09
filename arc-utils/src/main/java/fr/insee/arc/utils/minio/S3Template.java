package fr.insee.arc.utils.minio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
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
		this.minioClient = MinioClient.builder().endpoint(s3ApiUri).credentials(accessKey, secretKey).httpClient(httpClient).build();
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
							.stream(new ByteArrayInputStream(new byte[] {}), 0, -1).build());
		} catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException
				| InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException
				| IllegalArgumentException | IOException e) {

			throw new ArcException(ArcExceptionMessage.FILE_WRITE_FAILED, path);
		}
	}

	public void closeMinioClient()
	{
		httpClient.dispatcher().executorService().shutdown();
		this.minioClient=null;
	}
	
}
