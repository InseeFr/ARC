package fr.insee.arc.utils.utils;

import fr.insee.arc.utils.textUtils.IConstanteCaractere;

public class TemporaryToken implements IConstanteCaractere {

	private long token;

	public TemporaryToken() {
		super();
		this.token = generateTemporaryToken();
	}
	
	public long getToken() {
		return token;
	}


	private static final long generateTemporaryToken()
	{
		return Thread.currentThread().getId();
	}

}
