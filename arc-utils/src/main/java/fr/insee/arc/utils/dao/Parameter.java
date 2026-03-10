package fr.insee.arc.utils.dao;

public class Parameter<V> {
	
	private V value;
	
	private ParameterType type;
	
	public Parameter(V value, ParameterType type) {
		super();
		this.value = value;
		this.type = type;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}

	public ParameterType getType() {
		return type;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}
	
}
