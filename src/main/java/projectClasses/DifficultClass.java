/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectClasses;

import java.lang.Thread.State;

/**
 *
 * @author Albin
 */
public class DifficultClass {
    private States state; 
    
    public DifficultClass() {
        setState(new Easy());
}

    public void setState(States state){
        this.state = state;
        this.state.setDifficult(this);
    }
}
