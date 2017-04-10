package edu.carleton.comp2601.climbr;

/**
 * Created by Olivia on 2017-04-09.
 */

public class Step {
    String instruction;
    boolean timed;
    public Step(String s, boolean b){
        instruction = s;
        timed = b;
    }

    @Override
    public String toString(){
        return instruction.toString() ;
    }
}
