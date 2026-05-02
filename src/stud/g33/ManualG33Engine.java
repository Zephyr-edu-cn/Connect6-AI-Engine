package stud.g33;

/**
 * Hand-tuned V3 engine from the original report.
 */
public class ManualG33Engine extends Connect6Engine {

    private static final int[] MANUAL_WEIGHTS = {
            0, 20, 90, 160, 850, 1100, 10000000
    };

    public ManualG33Engine() {
        super(MANUAL_WEIGHTS, "Manual-G33");
    }
}