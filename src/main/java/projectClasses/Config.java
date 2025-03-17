/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectClasses;

/**
 *
 * @author Albin
 */
public class Config {
    private static String selectedDifficult;

    public static String getSelectedDifficult() {
        return selectedDifficult;
    }

    public static void setSelectedDifficult(String difficult) {
        selectedDifficult = difficult;
    }
}
