package fr.insee.arc.core.dataobjects;

import fr.insee.arc.utils.dao.UtilitaireDao;

public enum ArcDatabase {

	META_DATA(0), COORDINATOR(1), EXECUTOR(2);

	private int index;

	private ArcDatabase(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	/** 
	 * number of executor nods equal to total number of nods minus the starting index for executor nods
	 * @return
	 */
	public static int numberOfExecutorNods()
	{
		int numberOfExecutorNodsWhenMultiNods = UtilitaireDao.get(0).numberOfNods() - ArcDatabase.EXECUTOR.getIndex();
		
		return numberOfExecutorNodsWhenMultiNods>0?numberOfExecutorNodsWhenMultiNods:0;
	}

	
}
