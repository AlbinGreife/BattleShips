package projectClasses;

import controllers.SecondWindowController;
import projectClasses.Config;
import java.util.*;

/**
 * Clase que gestiona la lógica del juego por turnos entre el jugador y Skynet.
 */
public class Game {
    private static boolean playerTurn;
    private static boolean gameOver;
    
    private static List<String> notifications = new ArrayList<>();

    private static int gridSize;
    private static int playerShots;
    private static int enemyShots;
    private static boolean extraRound;
    private static int extraShotsEach;

    private static String[][] playerCellInfo;
    private static String[][] enemyCellInfo;

    private static boolean[][] targetHits;
    private static boolean[][] userHits;

    private static Map<Integer, Integer> shipCellsCount;
    private static Map<Integer, Integer> shipHits;
    private static Map<Integer, Integer> playerShipCellsCount;
    private static Map<Integer, Integer> playerShipHits;

    // IA de Skynet
    private static Deque<int[]> targetQueue;
    private static int[] firstHit;
    private static int[] lastHit;
    private static int dirRow;
    private static int dirCol;
    private static boolean directionLocked;
    private static String winner = null;

    /**
     * Inicializa el juego y prepara estructuras.
     */
    public static void startGame() {
        SecondWindowController.ensureBoardGenerated();
        gridSize = Config.getGridSize();
        playerShots = Config.getShots();
        enemyShots = Config.getShots();
        extraRound = false;
        extraShotsEach = 3;

        playerCellInfo = Config.getCellInfo();
        enemyCellInfo = SecondWindowController.getSavedEnemyCellInfo();

        targetHits = new boolean[gridSize][gridSize];
        userHits = new boolean[gridSize][gridSize];

        // Seguimiento de barcos enemigos
        shipCellsCount = new HashMap<>();
        shipHits = new HashMap<>();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                String info = enemyCellInfo[r][c];
                if (info != null && info.startsWith("ship:")) {
                    int id = Integer.parseInt(info.split(":")[1]);
                    shipCellsCount.put(id, shipCellsCount.getOrDefault(id, 0) + 1);
                }
            }
        }
        for (Integer id : shipCellsCount.keySet()) shipHits.put(id, 0);

        // Seguimiento de barcos del jugador (para IA)
        playerShipCellsCount = new HashMap<>();
        playerShipHits = new HashMap<>();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                String info = playerCellInfo[r][c];
                if (info != null && info.startsWith("ship:")) {
                    int id = Integer.parseInt(info.split(":")[1]);
                    playerShipCellsCount.put(id, playerShipCellsCount.getOrDefault(id, 0) + 1);
                }
            }
        }
        for (Integer id : playerShipCellsCount.keySet()) playerShipHits.put(id, 0);

        // AI targeting
        targetQueue = new ArrayDeque<>();
        firstHit = null;
        lastHit = null;
        directionLocked = false;

        playerTurn = true;
        gameOver = false;
    }

    /**
     * Lógica para el turno del jugador: dispara a (row, col).
     */
    public static void playerTurn(int row, int col) {
        
        notifications.clear();
        WildCards.onPlayerTurnStart();
        
        if (gameOver || !playerTurn) return;

        int currentShots = extraRound ? extraShotsEach : playerShots;
        if (currentShots <= 0) {
            System.out.println("No quedan tiros disponibles.");
            return;
        }
        if (row < 0 || row >= gridSize || col < 0 || col >= gridSize) {
            System.out.println("Coordenadas fuera de rango.");
            return;
        }
        if (targetHits[row][col]) {
            System.out.println("Ya has disparado en esa posición.");
            return;
        }

        // Registrar disparo
        targetHits[row][col] = true;
        if (!extraRound) playerShots--; else extraShotsEach--;

        String info = enemyCellInfo[row][col];
        if (info == null) {
            notifications.add("Agua");
        } else if (info.equals("island")) {
            notifications.add("Objetivo Alcanzado");
            // manejo de islas (similar a versiones previas)
        } else if (info.startsWith("ship:")) {
            notifications.add("Objetivo Alcanzado");
            int id = Integer.parseInt(info.split(":")[1]);
            int hits = shipHits.merge(id, 1, Integer::sum);
            if (hits == shipCellsCount.get(id)) {
                notifications.add("Hundido");
            }
        } else if (info.equals("trap")) {
            notifications.add("¡Trampa!");
            // Refleja el disparo en tu propio tablero
            userHits[row][col] = true;
        }

        if (checkWinCondition()) return;
        handleShotsExhausted();
        playerTurn = false;
        enemyTurn();
    }

    /**
     * Lógica para el turno de Skynet con IA de hundimiento de barcos.
     */
/**
 * Lógica para el turno de Skynet con IA de hundimiento de barcos y manejo de trampas.
 */
private static void enemyTurn() {
    if (gameOver) return;

    int row, col;
    // Decidir posición de disparo: o bien de la cola, o aleatoria
    if (!targetQueue.isEmpty()) {
        int[] next = targetQueue.poll();
        row = next[0];
        col = next[1];
    } else {
        Random rnd = new Random();
        do {
            row = rnd.nextInt(gridSize);
            col = rnd.nextInt(gridSize);
        } while (userHits[row][col]);
    }

    // Registrar disparo
    userHits[row][col] = true;
    enemyShots--;
    String info = playerCellInfo[row][col];

    if (info != null && info.startsWith("ship:")) {
        // Impacto en barco
        notifications.add("dañado en (" + row + "," + col + ")");
        int id = Integer.parseInt(info.split(":")[1]);
        int hits = playerShipHits.merge(id, 1, Integer::sum);

        if (hits == playerShipCellsCount.get(id)) {
            notifications.add("hundido barco " + id);
            // Reset IA targeting tras hundir
            targetQueue.clear();
            firstHit = null;
            lastHit = null;
            directionLocked = false;
        } else {
            // Lógica para seguir hundiendo ese barco...
            if (firstHit == null) {
                firstHit = new int[]{row, col};
                lastHit  = new int[]{row, col};
                int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
                for (int[] d : dirs) {
                    int nr = row + d[0], nc = col + d[1];
                    if (nr>=0 && nr<gridSize && nc>=0 && nc<gridSize && !userHits[nr][nc]) {
                        targetQueue.add(new int[]{nr, nc});
                    }
                }
            } else if (!directionLocked) {
                dirRow = row - firstHit[0];
                dirCol = col - firstHit[1];
                directionLocked = true;
                lastHit = new int[]{row, col};
                // Priorizar siguiente en la misma dirección
                int nr = row + dirRow, nc = col + dirCol;
                targetQueue.clear();
                if (nr>=0 && nr<gridSize && nc>=0 && nc<gridSize && !userHits[nr][nc]) {
                    targetQueue.addFirst(new int[]{nr, nc});
                }
            } else {
                lastHit = new int[]{row, col};
                int nr = row + dirRow, nc = col + dirCol;
                if (nr>=0 && nr<gridSize && nc>=0 && nc<gridSize && !userHits[nr][nc]) {
                    targetQueue.addFirst(new int[]{nr, nc});
                }
            }
        }

    } else if ("trap".equals(info)) {
        // Caer en trampa: se refleja en el tablero del jugador y se notifica
        notifications.add("Skynet cae en trampa en (" + row + "," + col + ")");
        // userHits[row][col] ya está marcado arriba para pintar en negro

    } else {
        // Agua / fallo
        notifications.add("Skynet falla en (" + row + "," + col + ")");
        // Si está siguiendo dirección, invierte y prueba
        if (directionLocked && firstHit != null) {
            dirRow = -dirRow;
            dirCol = -dirCol;
            targetQueue.clear();
            int nr = firstHit[0] + dirRow, nc = firstHit[1] + dirCol;
            if (nr>=0 && nr<gridSize && nc>=0 && nc<gridSize && !userHits[nr][nc]) {
                targetQueue.addFirst(new int[]{nr, nc});
            }
        }
    }

    // Verificar si Skynet gana
    if (checkLoseCondition()) return;

    // Devolver el turno al jugador
    playerTurn = true;
}


    private static boolean checkWinCondition() {
        for (int r = 0; r < gridSize; r++)
            for (int c = 0; c < gridSize; c++)
                if (enemyCellInfo[r][c]!=null && enemyCellInfo[r][c].startsWith("ship:") && !targetHits[r][c])
                    return false;
        gameOver = true;
        //System.out.println("¡Felicidades! Has ganado el juego.");
        winner = Config.getPlayerName();
        return true;
    }

    private static boolean checkLoseCondition() {
        for (int r = 0; r < gridSize; r++)
            for (int c = 0; c < gridSize; c++)
                if (playerCellInfo[r][c]!=null && playerCellInfo[r][c].startsWith("ship:") && !userHits[r][c])
                    return false;
        gameOver = true;
        winner = "Skynet";
        //System.out.println("Skynet ha ganado. Lo siento.");
        return true;
    }

    private static void handleShotsExhausted() {
        if (!extraRound && playerShots==0 && enemyShots==0 && !gameOver) {
            int playerHitCount = shipHits.values().stream().mapToInt(Integer::intValue).sum();
            int enemyHitCount  = playerShipHits.values().stream().mapToInt(Integer::intValue).sum();
            if (playerHitCount != enemyHitCount) {
                gameOver = true;
                if (playerHitCount>enemyHitCount)
                    System.out.println("¡Felicidades! Has ganado por más impactos.");
                else
                    System.out.println("Skynet gana por más impactos.");
            } else {
                extraRound = true;
                extraShotsEach = 3;
                System.out.println("Empate. 3 tiros extra para cada uno.");
            }
        }
    }

    // Getters
    public static boolean isPlayerTurn()    { 
        return playerTurn; 
    }
    public static boolean isGameOver()      { 
        return gameOver; 
    }
    public static int getPlayerShots()      { 
        return extraRound ? extraShotsEach : playerShots; 
    }
    public static int getEnemyShots()       { 
        return enemyShots; 
    }
    public static boolean[][] getTargetHits() { 
        return targetHits; 
    }
    public static boolean[][] getUserHits() {
       return userHits;
    }
    public static List<String> getNotifications() {
       return notifications;
    }
    public static String getWinner() {
      return winner;
}

}
