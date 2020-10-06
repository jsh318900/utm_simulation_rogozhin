package utm_simulation.simulation;


import utm_simulation.simulation.automata.TagSystem;
import utm_simulation.simulation.automata.TuringMachine;
import utm_simulation.simulation.encoder.Encoder;

/**
 * Runs simple simulation of utm(5,5) where it prints the content of the tape
 * for each time stamp.
 *
 */
public class SimpleSimulation {



    public static void main(String[] args){
        TuringMachine machine = TuringMachine.buildMachine(args[0]);
        TagSystem tagsystem = TagSystem.buildMachine(args[1]);

        String input = Encoder.utm5_5_encode(tagsystem);

        int headIndex = -1;

        for(int i = input.length() - 1; i >= 0 && headIndex < 0; i--){
            if(input.startsWith("bbb", i)){
                headIndex = i + 3;
            }
        }

        machine.reset(input, headIndex);

        do {
            System.out.println(machine.toString());
            machine.execute();
        }while(machine.getCurrentState() != -1);
    }

}