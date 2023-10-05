package fr.insee.arc.utils.utils;

import fr.insee.arc.utils.textUtils.IConstanteCaractere;

public class TemporaryToken implements IConstanteCaractere {

	private String token;

	public TemporaryToken() {
		super();
		this.token = generateTemporaryToken();
	}
	
	public String getToken() {
		return token;
	}


	private static final String generateTemporaryToken()
	{
		String l = System.currentTimeMillis() + "";
		// on prend que les 10 derniers chiffres (durrée de vie : 6 mois)
		l = l.substring(l.length() - 10);
		// on inverse la chaine de caractere pour avoir les millisecondes en
		// premier en cas de troncature
		l = new StringBuffer(l).reverse().toString();
		return new StringBuilder().append(l).append(dollar).append(randomNumber(4)).toString();
	}
	

	/**
	 *
	 * @return Un nombre aléatoire d'une certaine précision
	 */
	public static final String randomNumber(int precision) {
		String rn = ((int) Math.floor((Math.random() * (Math.pow(10, precision))))) + "";
		return ManipString.padLeft(rn, "0", precision);
	}
	
}
