package utm_simulation.simulation.encoder;

import utm_simulation.simulation.automata.Machine;
import utm_simulation.simulation.automata.TagSystem;
import utm_simulation.simulation.automata.TagSystem.TagSystemTransition;

import java.util.HashMap;


public class Encoder {

    public static String utm5_5_encode(TagSystem machine){

        HashMap<Character, Integer> mapNs = utm5_5_generateNs(machine);
        StringBuilder builder = new StringBuilder("1b1b");

        char[] symbols = new char[machine.getSymbols().length - 2];
        int i = 0;
        for(char symbol : machine.getSymbols()){
            if(symbol != 'a' && symbol != 'b')
                symbols[i++] = symbol;
        }

        for(i = symbols.length - 1; i >= 0; i--){
            TagSystemTransition t = machine.getTransition(symbols[i]).get(0);

            if(t.getType() == Machine.TransitionType.APPEND){
                String append = t.getAppend();
                builder.append("bb");
                for(int j = append.length() - 1; j >= 0; j--){
                    appendN1s(builder, mapNs.get(append.charAt(j)));
                    if(append.charAt(j) == 'a' || append.charAt(j) == 'b'){
                        builder.append("1b");
                    }else{
                        builder.append("1b1");
                    }
                }
            }
        }

        builder.append("bbb");

        for(i = 0; i < machine.getInput_tape().toString().length(); i++){
            appendN1s(builder, mapNs.get(machine.getInput_tape().toString().charAt(i)));
            if(i != machine.getInput_tape().toString().length() - 1)
                builder.append('c');
        }

        return builder.toString();
    }

    private static void appendN1s(StringBuilder builder, int n){
        int i = n;
        while(i > 0){
            builder.append('1');
            i--;
        }
    }

    /**
     * Returns corresponding N values based on utm5_5 encoding scheme
     * @param machine tagsystem for utm(5,5) to simulatie
     * @return integer array storing N values
     */
    private static HashMap<Character, Integer> utm5_5_generateNs(TagSystem machine){
        int a = -1,b = -1;
        int lastChar = -1;
        boolean firstChar = false;
        char[] symbols = machine.getSymbols();
        int[] numbers = new int[symbols.length];

        /*Retrieving indices of 'a' and 'b' in symbols array*/
        for(int i = 0; i < symbols.length; i ++){
            if(symbols[i] == 'a'){
                a = i;
            }else if(symbols[i] == 'b'){
                b = i;
            }else if(!firstChar){
                lastChar = i;
                firstChar = true;
            }
        }

        numbers[lastChar] = 3;
        numbers[b] = 1;
        for(int i = lastChar + 1; i < symbols.length; i ++){
            if(i == a || i == b)
                continue;
            TagSystemTransition t = machine.getTransition(symbols[lastChar]).get(0);
            numbers[i] = numbers[lastChar] + t.getAppend().length() + 4;
            lastChar = i;
        }
        numbers[a] = numbers[lastChar] + 2;

        HashMap<Character, Integer> numberMap = new HashMap<>();

        for(int i = 0; i < symbols.length; i++) {
            numberMap.put(symbols[i], numbers[i]);
        }

        return numberMap;
    }

}
