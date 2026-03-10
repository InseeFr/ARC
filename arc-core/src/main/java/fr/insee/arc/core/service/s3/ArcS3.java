package fr.insee.arc.core.service.s3;

import fr.insee.arc.utils.minio.S3Template;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

public class ArcS3 {

	public static final S3Template INPUT_BUCKET = new S3Template( //
			PropertiesHandler.getInstance().getS3InputApiUri(), //
			PropertiesHandler.getInstance().getS3InputBucket(), //
			PropertiesHandler.getInstance().getS3InputDirectory(), //
			PropertiesHandler.getInstance().getS3InputAccess(), //
			PropertiesHandler.getInstance().getS3InputSecret() //
	);

	public static final S3Template OUTPUT_BUCKET = new S3Template( //
			PropertiesHandler.getInstance().getS3OutputApiUri(), //
			PropertiesHandler.getInstance().getS3OutputBucket(), //
			PropertiesHandler.getInstance().getS3OutputDirectory(), //
			PropertiesHandler.getInstance().getS3OutputAccess(), //
			PropertiesHandler.getInstance().getS3OutputSecret() //
			);

}
