package fr.insee.arc.ws.services.importServlet.bo;

import java.net.InetAddress;
import java.net.UnknownHostException;

import fr.insee.arc.utils.exception.ArcException;
import fr.insee.arc.utils.exception.ArcExceptionMessage;
import jakarta.servlet.http.HttpServletRequest;

public class RemoteHost {

	public RemoteHost(HttpServletRequest request) throws ArcException {
		super();
		try {
			this.name = InetAddress.getByName(request.getRemoteHost()).getHostName();
		} catch (UnknownHostException e) {
			throw new ArcException(ArcExceptionMessage.HOST_NOT_RESOLVED);
		}
		
		this.secure = request.isSecure();
	}

	private boolean secure;
	
	private String name;

	public String getName() {
		return name;
	}

	public boolean isSecure() {
		return secure;
	}
	
	
	
}
