package fr.insee.arc.core.service.p1reception.registerfiles.provider;

import fr.insee.arc.utils.files.CompressionExtension;
import fr.insee.arc.utils.utils.ManipString;

public class ContainerName {

	
	
	public static String buildContainerName(String container) {
		String newContainerName = "";
		newContainerName = "";
		if (container.endsWith(CompressionExtension.TAR_GZ.getFileExtension())) {
			newContainerName = normalizeContainerName(container, CompressionExtension.TAR_GZ.getFileExtension());
		} else if (container.endsWith(CompressionExtension.TGZ.getFileExtension())) {
			newContainerName = normalizeContainerName(container, CompressionExtension.TGZ.getFileExtension());
		} else if (container.endsWith(CompressionExtension.ZIP.getFileExtension())) {
			newContainerName = normalizeContainerName(container, CompressionExtension.ZIP.getFileExtension());
		} else if (container.endsWith(CompressionExtension.GZ.getFileExtension())) {
			newContainerName = normalizeContainerName(container, CompressionExtension.GZ.getFileExtension());
		}
		return newContainerName;
	}

	private static String normalizeContainerName(String container, String extension) {
		return ManipString.substringBeforeLast(container, extension) + extension;
	}
	
	
}
