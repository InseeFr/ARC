package fr.insee.arc.batch;

import fr.insee.arc.core.factory.ApiServiceFactory;
import fr.insee.arc.core.model.ServiceReporting;
import fr.insee.arc.core.model.TypeTraitementPhase;
import fr.insee.arc.utils.batch.Batch;

public class ReceiveBatch extends Batch {

    public ServiceReporting report;

    public ReceiveBatch(String... someArgs) {
        super(someArgs);
    }

    /**
     *
     * @param args
     *            {@code args[0]} : origin workspace<br/>
     *            {@code args[1]} : final workspace<br/>
     *            {@code args[2]} : root directory<br/>
     *            {@code args[3]} : maximum number of lines to process
     */
    public static void main(String[] args) {
        Batch batch = new ReceiveBatch(args);
        batch.execute();

    }

    @Override
    public void execute() {
        this.report = ApiServiceFactory.getService(TypeTraitementPhase.REGISTER.toString(), (String) this.args[0], (String) this.args[1],
                (String) this.args[2], (String) this.args[3], (String) this.args[4]).invokeApi();
    }

}
