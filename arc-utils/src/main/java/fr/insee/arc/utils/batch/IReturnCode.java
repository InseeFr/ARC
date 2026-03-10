package fr.insee.arc.utils.batch;

public interface IReturnCode
{
    public static final Integer STATUS_SUCCESS = 0;
    public static final Integer STATUS_SUCCESS_BUT_STOP_HERE = 100;
    public static final Integer STATUS_SUCCESS_FUNCTIONAL_WARNING = 200;
    public static final Integer STATUS_SUCCESS_TECHNICAL_WARNING = 201;
    public static final Integer STATUS_FAILURE_TECHNICAL_WARNING = 202;
    public static final Integer STATUS_FAILURE_FUNCTIONAL_WARNING = 203;
}