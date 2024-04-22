package fr.insee.arc.core.service.p2chargement.bo;

import java.util.Arrays;

import fr.insee.arc.core.dataobjects.ColumnEnum;
import fr.insee.arc.utils.textUtils.FastList;


/**
 * Build a column correspondance between short and large name as we cannot use copy for  xml loader
 * @author FY2QEQ
 *
 */
public class XMLColumns {

	// custom fastlist
	// act as bidirectionnal map
	public static final FastList<ColumnEnum> tempTableAColumnsLongName = new FastList<>(Arrays.asList(
			ColumnEnum.ID_SOURCE
			, ColumnEnum.ID_SAX
			, ColumnEnum.DATE_INTEGRATION
			, ColumnEnum.ID_NORME
			, ColumnEnum.PERIODICITE
			, ColumnEnum.VALIDITE));
	
	public static final FastList<ColumnEnum> tempTableAColumnsShortName = new FastList<>(
			Arrays.asList(ColumnEnum.M0, ColumnEnum.M1, ColumnEnum.M2, ColumnEnum.M3, ColumnEnum.M4, ColumnEnum.M5));

	public static String getShort(ColumnEnum c)
	{
		return tempTableAColumnsShortName
				.get(tempTableAColumnsLongName.indexOf(c)).toString();
	}
	
}
