package fr.insee.arc.utils.batch;

/**
 *
 * @author QV47IK
 *
 */
public abstract class Batch implements IReturnCode {
    protected Object[] args;

    protected Batch(Object[] someArgs) {
        this.args = someArgs;
    }

    public abstract void execute();
}
