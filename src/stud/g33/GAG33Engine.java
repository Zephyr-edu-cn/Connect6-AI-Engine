package stud.g33;

/**
 * GA-tuned V3 engine using the best chromosome found in offline tuning.
 */
public class GAG33Engine extends Connect6Engine {

    private static final int[] GA_WEIGHTS = {
            0, 14, 65, 157, 630, 1104, 10000000
    };

    public GAG33Engine() {
        super(GA_WEIGHTS, "GA-G33");
    }
}