package utm_simulation.simulation.automata;

/**
 * Represents a transition function of a machine
 */
public interface Transition {

    /**
     * returns whether mapped transition is deterministic
     * @param state state of the machine
     * @param alphabet alphabet of the machine
     * @return whether mapped transition is deterministic
     */
    boolean isDeterministic(int state, char alphabet);

    /**
     * used to define a deterministic transitions for given state and alpahbet.
     * @param state state of the machine
     * @param alphabet alphabet of the machine
     * @throws UnsupportedOperationException if given state and alphabet map to a non-deterministic transition.
     */
    void execute(int state, char alphabet) throws UnsupportedOperationException;

    /**
     * used to define a non-deterministic transitions for given state and alphabet.
     * @param state state of the machine
     * @param alphabet alphabet of the machine
     * @param choice specifying the choice
     * @throws UnsupportedOperationException if given state and alphabet map to a deterministic transition.
     * @throws IllegalArgumentException if given choice is not applicable.
     */
    void execute(int state, char alphabet, int choice) throws UnsupportedOperationException, IllegalArgumentException;

}
