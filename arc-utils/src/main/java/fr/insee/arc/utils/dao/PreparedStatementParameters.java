package fr.insee.arc.utils.dao;

import java.util.ArrayList;
import java.util.List;

public class PreparedStatementParameters {

private List<String> parameters=new ArrayList<String>();

public String quoteText(String s)
{
	parameters.add(s);
	return "?";
}

public List<String> getParameters() {
	return parameters;
}

}
