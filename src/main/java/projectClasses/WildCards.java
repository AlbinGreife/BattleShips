package projectClasses;

import java.util.*;

/**
 * Clase que gestiona los comodines (wild cards) del jugador.
 * Solo se puede activar un comodín por turno.
 */
public class WildCards {
    // Tiempos de reutilización y uso
    private static int turnCounter = 0;
    private static int lastDoubleShotTurn = Integer.MIN_VALUE;
    private static boolean tripleShotUsed = false;
    private static int shieldAvailableAtTurn = 3;

    // Estados de activación durante un turno
    private static boolean doubleShotActive = false;
    private static boolean tripleShotActive = false;
    private static boolean shieldActive = false;
    private static int shieldRow = -1;
    private static int shieldCol = -1;

    /**
     * Debe invocarse al inicio de cada turno del jugador.
     */
    public static void onPlayerTurnStart() {
        turnCounter++;
        // Reset de comodines activos cada turno
        doubleShotActive = false;
        tripleShotActive = false;
        shieldActive = false;
    }

    /**
     * Intenta activar el comodín de tiro doble.
     * @return true si se activó correctamente
     */
    public static boolean activateDoubleShot() {
        if (turnCounter - lastDoubleShotTurn >= 3) {
            doubleShotActive = true;
            lastDoubleShotTurn = turnCounter;
            return true;
        }
        return false;
    }

    /**
     * Intenta activar el comodín de tiro triple.
     * @return true si se activó correctamente
     */
    public static boolean activateTripleShot() {
        if (!tripleShotUsed) {
            tripleShotActive = true;
            tripleShotUsed = true;
            return true;
        }
        return false;
    }

    /**
     * Intenta activar el comodín de escudo.
     * Solo disponible cada 3 turnos.
     * @return true si se activó correctamente
     */
    public static boolean activateShield(int row, int col) {
        if (turnCounter >= shieldAvailableAtTurn) {
            shieldActive = true;
            shieldRow = row;
            shieldCol = col;
            shieldAvailableAtTurn = turnCounter + 3;
            return true;
        }
        return false;
    }

    // Getters de estados
    public static boolean isDoubleShotActive() { return doubleShotActive; }
    public static boolean isTripleShotActive() { return tripleShotActive; }
    public static boolean isShieldActive() { return shieldActive; }
    public static int getShieldRow() { return shieldRow; }
    public static int getShieldCol() { return shieldCol; }
    public static int getTurnCounter() { return turnCounter; }
    public static int getNextShieldAvailableTurn() { return shieldAvailableAtTurn; }

    /**
     * Determina cuántos disparos permite este turno.
     */
    public static int getShotsThisTurn(int baseShots) {
        if (doubleShotActive) {
            return 2;
        } else if (tripleShotActive) {
            return 3;
        }
        return baseShots;
    }

    /**
     * Si tripleShotActive, salta los próximos 2 turnos de jugador.
     */
    public static int getSkippedEnemyTurns() {
        return tripleShotActive ? 2 : 0;
    }
}
