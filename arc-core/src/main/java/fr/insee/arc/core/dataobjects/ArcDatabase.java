package fr.insee.arc.core.dataobjects;

public enum ArcDatabase {

	META_DATA(0), COORDINATOR(1), EXECUTOR(2);

	private int index;

	private ArcDatabase(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

}
