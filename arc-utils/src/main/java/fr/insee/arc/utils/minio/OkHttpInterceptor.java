package fr.insee.arc.utils.minio;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerHelper;
import okhttp3.Interceptor;
import okhttp3.Response;

public class OkHttpInterceptor implements Interceptor {

	private static final Logger LOGGER = LogManager.getLogger(S3Template.class);
	
	
	public OkHttpInterceptor() {
	}

	@Override
	public Response intercept(Chain chain) throws IOException {

		Response response = chain.proceed(chain.request());

		if (!response.isSuccessful() && response.code()!=404) {		
			LoggerHelper.error(LOGGER, "Error okhttp : " + response);
		}

		return response;
	}

}