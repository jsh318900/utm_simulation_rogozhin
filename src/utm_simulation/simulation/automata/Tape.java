package utm_simulation.simulation.automata;

import java.util.Iterator;

/**
 * Represents a working tape in an automata
 * Tape is a DoubleLinkedList with apprpriate functionalities implemented for the simulations
 */

public class Tape implements Iterable<Character> {

    /*Inner Classes*/

    /**
     * Represents a cell in the working tape.
     * Contains a char type alphabet and pointers to its previous and next cells.
     */
    protected static class Cell{
        private char alphabet;
        private Cell previous;
        private Cell next;

        //Constructor
        public Cell(char alphabet, Cell previous, Cell next){
            this.alphabet = alphabet;
            setPrevious(previous);
            setNext(next);
        }

        //getter methods
        public char getAlphabet(){
            return alphabet;
        }

        public Cell getPrevious(){
            return previous;
        }

        public Cell getNext(){
            return next;
        }

        //setter methods
        public void setAlphabet(char alphabet){
            this.alphabet = alphabet;
        }

        public void setPrevious(Cell previous){
            if(previous != null)
                previous.setNext(this);
            this.previous = previous;
        }

        public void setNext(Cell next){
            if(next != null)
                next.setPrevious(this);
            this.next = next;
        }

    }

    /*Fields*/

    private final char BLANK;
    private Cell current;
    private Cell front;
    private Cell end;

    /*Constructors*/

    /**
     * Initializes a tape with given input and a blank symbol. The first cell is head by default
     * @param blank blank symbol of the tape
     * @param input the initial content of the tape.
     */
    public Tape(char blank, String input){
        this(blank,0, input);
    }

    /**
     * Initializes a Tape with its current cell at head_index and its content being the input.
     * @param blank blank symbol of the tape.
     * @param head_index the location of current cell
     * @param input the initial content of the tape
     * @throws NullPointerException if given String is null
     */
    public Tape(char blank, int head_index, String input){
        //setting blank symbol
        BLANK = blank;

        //Input check
        if(input.length() == 0){
            current = new Cell(BLANK, null, null);
            front = current;
            end = current;
            return;
        }else if(head_index < 0 || head_index >= input.length()){
            throw new StringIndexOutOfBoundsException("head_index: " + head_index + " is out of bounds");
        }

        //Initializing first cell
        this.front = new Cell(input.charAt(0), null, null);
        this.current = front; // initializing the current as front by default
        this.end = front;

        //Adding the rest
        for(int i = 1; i < input.length(); i++){
            this.end = new Cell(input.charAt(i), this.end, null);
            if(i == head_index) // setting the current with given head_index
                current = end;
        }
    }

    /*getter methods*/
    public char getBLANK(){
        return BLANK;
    }

    protected Cell getCurrent(){
        return current;
    }

    protected Cell getFront(){
        return front;
    }

    protected Cell getEnd(){
        return end;
    }

    /*setter methods*/
    protected void setCurrent(Cell current){
        this.current = current;
    }

    protected void setFront(Cell front){
        this.front = front;
    }

    protected void setEnd(Cell end){
        this.end = end;
    }

    /*Other methods*/

    /**
     * Reads the content of the current cell
     * @return the content of the current cell
     */
    public char read(){
        return getCurrent().getAlphabet();
    }

    /**
     * Writes over the content of the current cell
     * @param alphabet new alphabet for the current cell
     */
    public void write(char alphabet){
        getCurrent().setAlphabet(alphabet);
    }

    /**
     * Shifts the current position by given amount. If the given number is negative, the head moves to the left.
     * If the given number is positive, the head mover to the right. Otherwise, the head position does not change.
     * @param steps integer specifying how much the head should move.
     */
    public void shift(int steps){
        while(steps != 0){
            if(steps > 0){
                steps--;
                shiftToRight();
            }else{
                steps++;
                shiftToLeft();
            }
        }
    }

    /*Helper methods to shift*/

    private void shiftToLeft(){
        Cell temp = getCurrent().getPrevious();
        if(temp == null){
            temp = new Cell(BLANK, null, temp);
            setFront(temp);
        }
        setCurrent(temp);
    }

    private void shiftToRight(){
        Cell temp = getCurrent().getNext();
        if(temp == null){
            temp = new Cell(BLANK, temp, null);
            setEnd(temp);
        }
        setCurrent(temp);
    }

    /**
     * Appends given word to the end of the tape
     * @param word the String to add to the end of the tape
     */
    public void append(String word){
        Tape other = new Tape(BLANK, 0, word);
        append(other);
    }

    /**
     * Appends the given tape to the end.
     * @param other other tape to append.
     * @throws NullPointerException if given tape is null
     */
    protected void append(Tape other){
        getEnd().setNext(other.getFront());
        setEnd(other.getEnd());
    }

    /**
     * Default iterator iterates throught the entire content of the tape.
     * @return iterator default iterator of Tape.
     */
    public Iterator<Character> iterator(){
        Cell ptr = getFront();
        return new Iterator<Character>() {
            private Cell current = ptr;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Character next() {
                char next = current.getAlphabet();
                current = current.getNext();
                return next;
            }
        };
    }

    /**
     * returns the position of the head on the tape
     *
     * <p>Warning: This method is for gui purpose only and it will not trim the leading blank symbols.</p>
     *
     * @return the position of the head on the tape
     */
    public int headIndex(){
        return headIndexRecurse(0, getFront());
    }


    private int headIndexRecurse(int current, Cell ptr){
        if(ptr != getCurrent())
            return headIndexRecurse(++current, ptr.getNext());
        else
            return current;
    }

    /**
     * Creates a String showing content of the tape
     * @return String representation of this Tape.
     */
    public String toString(){
        StringBuilder result = new StringBuilder();
        for(char alphabet : this){
            result.append(alphabet);
        }
        return result.toString();
    }
}
