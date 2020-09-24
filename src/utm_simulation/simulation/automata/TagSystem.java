package utm_simulation.simulation.automata;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 */
public class TagSystem extends Machine{

    /**
     * Represents a transition rule defined for a symbol.
     */
    public static class TagSystemTransition{
        private final char symbol;
        private final TransitionType type;
        private final String append;

        /**
         * Creates a representation of a tagsystem transition defined for a given symbol.
         * @param type the type of transition. must be either {@link utm_simulation.simulation.automata.Machine.TransitionType HALT}
         *             or {@link utm_simulation.simulation.automata.Machine.TransitionType APPEND}
         * @param symbol the symbol which produces the rule
         * @param append String to append if its type is {@link utm_simulation.simulation.automata.Machine.TransitionType APPEND}. Ignored otherwise.
         * @throws IllegalArgumentException if given type is invalid.
         */
        public TagSystemTransition(TransitionType type, char symbol, String append){
            if(!type.equals(TransitionType.APPEND) || !type.equals(TransitionType.HALT))
                throw new IllegalArgumentException("Invalid transition type for tag system.");
            this.symbol = symbol;
            this.append = append;
            this.type = type;
        }

        /**
         * Returns the symbol associated with this transition
         * @return the symbol associated with this transition
         */
        public char getSymbol(){
            return symbol;
        }

        /**
         * Returns the type of this transition
         * @return the type of this transition
         */
        public TransitionType getType(){
            return type;
        }

        /**
         * Returns the String to append when applicable. Otherwise, it will return an arbitrary String.
         * @return the String to append
         */
        public String getAppend(){
            return append;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString(){
            if(getType().equals(TransitionType.APPEND))
                return "type:" + getType() + " symbol:" + getSymbol() + " value:" + getAppend();
            else
                return "type:" + getType() + " symbol:" + getSymbol();
        }
    }

    private final int deletionNumber;
    private boolean halted = false;
    private HashMap<Character, ArrayList<TagSystemTransition>> transitions;

    protected TagSystem(int deletionNumber, char blank, char[] symbols, String input) {
        super(blank, symbols, input);
        this.deletionNumber = deletionNumber;
    }

    /**
     * Returns the deletion number of this tagsystem.
     * @return the deletion number of this tagsystem.
     */
    public int getDeletionNumber(){
        return deletionNumber;
    }

    /**
     * Returns a copy of all transition rules defined for the given symbol.
     * Any operation on returned ArrayList does not modify the original one.
     * @param symbol associated symbol to returned rules.
     * @return all transition rules defined for the given symbol.
     */
    public ArrayList<TagSystemTransition> getTransition(char symbol){
        return new ArrayList<>(transitions.get(symbol));
    }

    protected void setTransitions(HashMap<Character, ArrayList<TagSystemTransition>> transitions){
        this.transitions = transitions;
    }

    /**
     * Returns whether the machine has halted.
     * @return whether the machine has halted.
     */
    public boolean isHalted() {
        return halted;
    }

    protected void halt(){
        halted = true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDeterministic() {
        return (getTransition(getInput_tape().read()).size() == 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws IllegalStateException, UnsupportedOperationException {
        if(!isDeterministic()) {
            throw new UnsupportedOperationException("The machine currently requires a choice to proceed");
        }else if(isHalted()){
            throw new IllegalStateException("The machine has already terminated.");
        }

        TagSystemTransition t = getTransition(getInput_tape().read()).get(0);
        execute(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(int choice) throws IllegalStateException, UnsupportedOperationException {
        if(isDeterministic()) {
            throw new UnsupportedOperationException("The machine does not require a choice to proceed");
        }else if(isHalted()){
            throw new IllegalStateException("The machine has already terminated.");
        }

        TagSystemTransition t = getTransition(getInput_tape().read()).get(choice);
        execute(t);
    }

    protected void execute(TagSystemTransition t){
        switch (t.getType()) {
            case APPEND -> {
                getInput_tape().append(t.getAppend());
                int delete = getDeletionNumber();
                while (delete > 0) {
                    getInput_tape().write(getInput_tape().getBLANK());
                    getInput_tape().shift(1);
                    delete--;
                }
            }
            case HALT -> halt();
            default -> {
                System.err.println("Fatal error when generating this tagsystem");
                System.exit(-1);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset(String input){
        super.reset(input);
        halted = false;
    }

    /**
     * Method used to generate a TagSystem instance with given path to config file.
     * This method is the only way to generate TagSystem instance in this library.
     *
     * @param config path to the config xml file.
     * @return the TagSystem instance defined by given config file.
     */
    public static TagSystem buildMachine(String config){

        //Declaring variables to parse
        char[] symbols = null;
        LinkedList<TagSystemTransition> transitions = new LinkedList<>();
        String input = "";
        char blank = (char) -1;
        int deletionNumber = 2;

        //Setting up XML parser
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in = null;
        try {
            in = new FileInputStream(config);
        } catch (FileNotFoundException e) {
            System.err.println("Given config file is not found.");
            System.exit(-1);
        }
        Iterator<Attribute> attributes;
        Attribute attribute;
        try {
            XMLEventReader reader = inputFactory.createXMLEventReader(in);
            LinkedList<Character> symbols_temp = new LinkedList<>();
            while(reader.hasNext()){
                XMLEvent event = reader.nextEvent();
                if(event.isStartElement()){
                    StartElement element = event.asStartElement();
                    String elementName = element.getName().getLocalPart();
                    switch(elementName){
                        case MACHINE: //check whether the xml is valid TagSystem configuration.
                            attributes = element.getAttributes();
                            if(attributes.hasNext()){
                                attribute = attributes.next();
                                if(attribute.getName().toString().equals(TYPE) && !attribute.getValue().equals("non-universal")) {
                                    System.err.println("Invalid tag for Tagsystem simulation");
                                    System.exit(-1);
                                }
                            }break;

                        case CLASS: //check whether the xml is valid TagSystem configuration.
                            if(!element.asCharacters().getData().equals("TagSystem")){
                                System.err.println("Invalid class value for tagsystem simulation");
                                System.exit(-1);
                            }break;

                        case DELETIONNUMBER:
                            deletionNumber = Integer.parseInt(element.asCharacters().getData());

                        case SYMBOL: //add symbol to temporary list
                            attributes = element.getAttributes();
                            if(attributes.hasNext()){
                                attribute = attributes.next();
                                if(attribute.getName().toString().equals(TYPE) && attribute.getValue().equals("blank")){
                                    blank = element.getName().getLocalPart().charAt(0);
                                }
                            }
                            symbols_temp.add(element.asCharacters().getData().charAt(0));break;

                        case TRANSITION:
                            attributes = element.getAttributes();
                            String type = null;
                            char symbol = (char)-1;
                            while(attributes.hasNext()){
                                attribute = attributes.next();
                                if(attribute.getName().toString().equals(TYPE))
                                    type = attribute.getValue();
                                else if(attribute.getName().toString().equals(SYMBOL))
                                    symbol = attribute.getValue().charAt(0);
                                else{
                                    System.err.printf("Unexpected tag in transition definition: %s\n", attribute.getName().toString());
                                    System.exit(-1);
                                }
                            }
                            if(type.equals(APPEND)){
                                transitions.add(new TagSystemTransition(TransitionType.APPEND, symbol, element.asCharacters().getData()));
                            }else if(type.equals(HALT)){
                                transitions.add(new TagSystemTransition(TransitionType.HALT, symbol, ""));
                            }else{
                                System.err.printf("Unexpected transition type for tagsystem: %s\n", type);
                                System.exit(-1);
                            }break;

                        case INPUT:
                            input = element.asCharacters().getData();
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
        } catch (XMLStreamException e) {
            System.err.println("Invalid xml file.");
            System.exit(-1);
        }

        HashMap<Character, ArrayList<TagSystemTransition>> transitionsFinal = new HashMap<>();
        HashMap<Character, LinkedList<TagSystemTransition>> tempTransitions = new HashMap<>();

        for(char symbol: symbols){
            tempTransitions.put(symbol, new LinkedList<>());
        }

        for(TagSystemTransition t : transitions){
            if(tempTransitions.containsKey(t.getSymbol())){
                tempTransitions.get(t.getSymbol()).add(t);
            }else{
                System.err.println("Undefined symbol used in transition");
            }
        }

        for(char symbol : symbols){
            transitionsFinal.put(symbol, new ArrayList<>(tempTransitions.get(symbol)));
        }

        TagSystem machine  = new TagSystem(deletionNumber, blank, symbols, input);
        machine.setTransitions(transitionsFinal);
        return machine;
    }
}