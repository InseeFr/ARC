package fr.insee.arc.utils.dao;

import java.util.ArrayList;
import java.util.List;

public class PreparedStatementParameters {

private List<String> parameters=new ArrayList<String>();

public int quoteText(String s)
{
	parameters.add(s);
	return parameters.size();
}

public List<String> getParameters() {
	return parameters;
}

}
