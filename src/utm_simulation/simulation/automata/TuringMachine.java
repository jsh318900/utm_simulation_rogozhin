package utm_simulation.simulation.automata;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class TuringMachine extends Machine{

    public static final class StateSymbolPair{
        private final int state;
        private final char symbol;

        public StateSymbolPair(int state, char symbol){
            this.state = state;
            this.symbol = symbol;
        }

        public int getState(){
            return state;
        }

        public char getSymbol(){
            return symbol;
        }

        @Override
        public boolean equals(Object obj){
            if(obj instanceof StateSymbolPair){
                return ((StateSymbolPair) obj).getState() == getState() && ((StateSymbolPair) obj).getSymbol() == getSymbol();
            }
            return false;
        }

        @Override
        public String toString(){
            return String.format("(%d, %s)", getState(), getSymbol());
        }

        @Override
        public int hashCode(){
            return toString().hashCode();
        }
    }

    public static class TuringTransition{
        private final StateSymbolPair stateSymbol;
        private final StateSymbolPair next;
        private final TransitionType type;
        private final int shift;


        public TuringTransition(int state, char symbol, int nextState, char nextSymbol, TransitionType type, int shift){
            stateSymbol = new StateSymbolPair(state, symbol);
            this.next = new StateSymbolPair(nextState, nextSymbol);
            this.type = type;
            this.shift = shift;
        }

        public int getState(){
            return stateSymbol.getState();
        }

        public char getSymbol(){
            return stateSymbol.getSymbol();
        }

        public int getNextState(){
            return next.getState();
        }

        public char getNextSymbol(){
            return next.getSymbol();
        }

        public TransitionType getType(){
            return type;
        }

        public int getShift(){
            return shift;
        }
    }

    private final int numStates;
    private int currentState;
    private HashMap<StateSymbolPair, ArrayList<TuringTransition>> transitions;

    protected TuringMachine(char blank, char[] symbols, int numStates, String input) {
        super(blank, symbols, input);
        this.numStates = numStates;
        this.currentState = 1;
    }

    public int getNumStates(){
        return numStates;
    }

    /**
     * Returns the number indicating the current state of the machine.
     * By default, 1 indicates the initial state and -1 indicates the halt state.
     * @return the number indicating the current state of the machine.
     */
    public int getCurrentState(){
        return currentState;
    }

    /**
     * Modifies the current state of the machine
     * @param currentState the new state of the machine
     * @throws IllegalArgumentException if given state does not exist.
     */
    protected void setCurrentState(int currentState){
        if(currentState <= 0 || currentState > getNumStates())
            throw new IllegalArgumentException("Given state does not exist");
        this.currentState = currentState;
    }

    public ArrayList<TuringTransition> getTransition(int state, char symbol){
        return new ArrayList<>(transitions.get(new StateSymbolPair(state, symbol)));
    }

    protected void setTransitions(HashMap<StateSymbolPair, ArrayList<TuringTransition>> transitions){
        this.transitions = transitions;
    }

    /**
     * Resets the machine with new input give and with initial state 1.
     * @param input new input given.
     */
    @Override
    public void reset(String input){
        super.reset(input);
        setCurrentState(1);
    }

    /**
     * Returns whether the next transition is a deterministic one
     *
     * @return whether the next transition is a deterministic one
     */
    @Override
    public boolean isDeterministic() {
        return getTransition(getCurrentState(), getInput_tape().read()).size() == 1;
    }

    /**
     * Runs the machine for one step. Should only be used for deterministic action
     *
     * @throws IllegalStateException         if the machine already halted.
     * @throws UnsupportedOperationException if called when next action is a non-deterministic one
     */
    @Override
    public void execute() throws IllegalStateException, UnsupportedOperationException {
        if(!isDeterministic()){
            throw new UnsupportedOperationException("Current operation requires a choice");
        }else if(getCurrentState() == -1){
            throw new IllegalStateException("The machine has already halted");
        }

        TuringTransition t = getTransition(getCurrentState(), getInput_tape().read()).get(0);
        execute(t);
    }

    /**
     * Runs the machine for one step. Should only be used for non-deterministic action
     *
     * @param choice specifying the choice
     * @throws IllegalStateException         if the machine already halted
     * @throws UnsupportedOperationException if called when next action is a deterministic one.
     */
    @Override
    public void execute(int choice) throws IllegalStateException, UnsupportedOperationException {
        if(isDeterministic()){
            throw new UnsupportedOperationException("Current operation requires a choice");
        }else if(getCurrentState() == -1){
            throw new IllegalStateException("The machine has already halted");
        }

        TuringTransition t = getTransition(getCurrentState(), getInput_tape().read()).get(choice);
        execute(t);
    }

    private void execute(TuringTransition t){
        switch(t.getType()){
            case STATE_TRANSITION -> {
                getInput_tape().write(t.getNextSymbol());
                getInput_tape().shift(t.getShift());
                setCurrentState(t.getNextState());
            }
            case HALT -> setCurrentState(-1);
            default -> {
                System.err.println("Fatal error when generating this machine");
                System.exit(-1);
            }
        }
    }

    public static TuringMachine buildMachine(String config){

        char[] symbols = null;
        char blank = (char) -1;
        String input = "";
        int numStates = 0;
        LinkedList<TuringTransition> transitions = new LinkedList<>();

        //Setting up XML parser
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in = null;

        try{
            in = new FileInputStream(config);
        }catch (FileNotFoundException e) {
            System.err.println("Given config file is not found.");
            System.exit(-1);
        }

        Iterator<Attribute> attributes;
        Attribute attribute;

        try{
            XMLEventReader reader = inputFactory.createXMLEventReader(in);
            LinkedList<Character> symbols_temp = new LinkedList<>();

            while(reader.hasNext()){
                XMLEvent event = reader.nextEvent();
                if(event.isStartElement()){
                    StartElement element = event.asStartElement();
                    String elementName = element.getName().getLocalPart();

                    switch(elementName){
                        case CLASS:

                            if(!element.asCharacters().getData().equals("TuringMachine")){
                                System.err.println("Invalid class value for Turing machine");
                                System.exit(-1);
                            }break;

                        case SYMBOL:
                            attributes = element.getAttributes();
                            if (attributes.hasNext()) {
                                attribute = attributes.next();
                                if(attribute.getName().toString().equals(TYPE) && attribute.getValue().equals("blank")){
                                    blank = element.getName().getLocalPart().charAt(0);
                                }
                            }
                            symbols_temp.add(element.asCharacters().getData().charAt(0));break;

                        case NUMSTATES:
                            numStates = Integer.parseInt(element.asCharacters().getData());break;

                        case TRANSITION:
                            attributes = element.getAttributes();
                            String type = null;
                            char symbol = ' ', nextSymbol = ' ', direction = ' ';
                            int state = -1, nextState = -1;
                            while(attributes.hasNext()){
                                attribute = attributes.next();
                                switch (attribute.getName().toString()) {
                                    case TYPE -> type = attribute.getValue();
                                    case STATE -> state = Integer.parseInt(attribute.getValue());
                                    case SYMBOL -> symbol = attribute.getValue().charAt(0);
                                    case NEWSTATE -> nextState = Integer.parseInt(attribute.getValue());
                                    case NEWSYMBOL -> nextSymbol = attribute.getValue().charAt(0);
                                    case DIRECTION -> direction = attribute.getValue().charAt(0);
                                    default -> {
                                        System.err.printf("Undefined xml tag: %s\n", attribute.getName().toString());
                                        System.exit(-1);
                                    }
                                }

                                if(type.equals(STATE_TRANSITION)){
                                    int shift = (direction == 'L') ? -1 : 1;
                                    transitions.add(new TuringTransition(state, symbol, nextState, nextSymbol, TransitionType.STATE_TRANSITION, shift));
                                }else if(type.equals(HALT)){
                                    transitions.add(new TuringTransition(state, symbol, -1, nextSymbol, TransitionType.HALT, 0));
                                }else{
                                    System.err.printf("Undefined xml attribute value: %s\n", type);
                                    System.exit(-1);
                                }
                            }break;
                        case INPUT: input = element.asCharacters().getData();
                    }
                }else if(event.isEndElement()){
                    EndElement element = event.asEndElement();
                    if(element.getName().getLocalPart().equals(SYMBOLS)){
                        symbols = new char[symbols_temp.size()];
                        int i = 0;
                        for(char s : symbols_temp)
                            symbols[i++] = s;
                    }
                }
            }
        }catch(XMLStreamException e){
            System.err.println("Error parsing the XML file");
            System.exit(-1);
        }

        HashMap<StateSymbolPair, ArrayList<TuringTransition>> transitionsFinal = new HashMap<>();
        HashMap<StateSymbolPair, LinkedList<TuringTransition>> tempTransitions = new HashMap<>();

        for(char symbol : symbols){
            for(int s = 1; s < numStates + 1; s++){
                tempTransitions.put(new StateSymbolPair(s, symbol), new LinkedList<>());
            }
        }

        for(TuringTransition t : transitions){
            StateSymbolPair pair = new StateSymbolPair(t.getState(), t.getSymbol());
            if(tempTransitions.containsKey(pair)){
                tempTransitions.get(pair).add(t);
            }else{
                System.err.printf("Undefined state or symbol used in transition: (%d, %s)\n", pair.getState(), pair.getSymbol());
                System.exit(-1);
            }
        }

        for(char symbol : symbols){
            for(int s = 1; s < numStates + 1; s++){
                StateSymbolPair pair = new StateSymbolPair(s, symbol);
                transitionsFinal.put(pair, new ArrayList<>(tempTransitions.get(pair)));
            }
        }

        TuringMachine machine = new TuringMachine(blank, symbols, numStates, input);
        machine.setTransitions(transitionsFinal);

        return machine;
    }
}