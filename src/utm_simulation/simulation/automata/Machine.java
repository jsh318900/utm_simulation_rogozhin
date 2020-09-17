package utm_simulation.simulation.automata;

import java.util.Arrays;

public abstract class Machine{

    /*Constant Strings for xml parsing*/
    public static final String MACHINE = "Machine";
    public static final String CLASS = "Class";
    public static final String SYMBOLS = "Symbols";
    public static final String SYMBOL = "Symbol";
    public static final String TRANSITIONS = "Transitions";
    public static final String TRANSITION = "Transition";
    public static final String INPUT = "Input";
    public static final String NUMSTATES = "NumStates";
    public static final String TYPE = "Type";
    public static final String STATE = "State";
    public static final String STATE_TRANSITION = "state_transition";
    public static final String APPEND = "append";
    public static final String HALT = "halt";
    public static final String NEWSTATE = "NewState";
    public static final String NEWSYMBOL = "NewSymbol";
    public static final String DIRECTION = "Direction";

    private final char[] symbols;
    private Tape input_tape;

    /**
     * Creates a machine with given information
     * @param symbols set of characters used by the machine and tape
     * @param input the initial input to the tape
     */
    public Machine(char[] symbols, String input){
        this.symbols = Arrays.copyOf(symbols, symbols.length);
        input_tape = new Tape(symbols[0], input);
    }

    /**
     * Returns array of tape symbols of the machine.
     * The first symbol is always the blank tape symbol.
     * @return array of tape symbols of the machine.
     */
    public char[] getSymbols(){
        return Arrays.copyOf(symbols, symbols.length);
    }

    /**
     * Returns the working tape instance
     * @return the working tape instance
     */
    public Tape getInput_tape(){
        return input_tape;
    }

    protected void setInput_tape(Tape tape){
        input_tape = tape;
    }

    /**
     * Returns whether the next transition is a deterministic one
     * @return whether the next transition is a deterministic one
     */
    public abstract boolean isDeterministic();

    /**
     * Resets the machine with new input given
     * @param input new input given.
     */
    public void reset(String input){
        setInput_tape(new Tape(getInput_tape().getBLANK(), input));
    }

    /**
     * Runs the machine for one step. Should only be used for deterministic action
     * @throws IllegalStateException if the machine already halted.
     * @throws UnsupportedOperationException if called when next action is a non-deterministic one
     */
    public abstract void execute() throws IllegalStateException, UnsupportedOperationException;

    /**
     * Runs the machine for one step. Should only be used for non-deterministic action
     * @param choice specifying the choice
     * @throws IllegalStateException if the machine already halted
     * @throws UnsupportedOperationException if called when next action is a deterministic one.
     */
    public abstract void execute(int choice) throws IllegalStateException, UnsupportedOperationException;

}