package utm_simulation.simulation.automata;

import java.util.Arrays;

public abstract class Machine{

    /**Reserved state for Halt, set to -1.*/
    public static final int HALT = -1;

    private final char[] symbols;
    private final int numStates;
    private final Transition transition;
    private int currentState;
    private Tape input_tape;

    public Machine(char[] symbols, int numStates, Transition transition, String input){
        this.symbols = Arrays.copyOf(symbols, symbols.length);
        this.numStates = numStates;
        this.transition = transition;
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
     * Returns the number of states of the machine
     * @return the number of states of the machine
     */
    public int getNumStates(){
        return numStates;
    }

    /**
     * Returns the Transition function object for the machine
     * @return the Transition function object for the machine
     */
    protected Transition getTransition(){
        return transition;
    }

    /**
     * Returns the number indicating the current state of the machine.
     * @return the number indicating the current state of the machine.
     */
    public int getCurrentState(){
        return currentState;
    }

    /**
     * Returns the working tape instance
     * @return the working tape instance
     */
    public Tape getInput_tape(){
        return input_tape;
    }

    /**
     * Modifies the current state of the machine
     * @param currentState the new state of the machine
     * @throws IllegalArgumentException if given state does not exist.
     */
    public void setCurrentState(int currentState){
        if(currentState < -1 && currentState > getNumStates())
            throw new IllegalArgumentException("Given state does not exist");
        this.currentState = currentState;
    }

    /**
     * Runs the machine for one step.
     * @throws IllegalStateException if the machine already halted.
     */
    public abstract void execute() throws IllegalStateException;

    /**
     * Returns the String representation of the machine
     * @return the String representation of the machine.
     */
    @Override
    public abstract String toString();
}