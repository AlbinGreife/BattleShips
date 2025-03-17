package projectClasses;

/**
 *
 * @author Albin
 */
public interface State {
    
    void shoot();//cantidad de balas según dificultad
    void mapSize();//cambia el tamaño del gridpane según la dificultad
    void island();//debe recibir primero las posiciones de los barcos para luego cargar las islas según la dificultad
    void trickPosition();//devuelve disparo si no se acierta en barco
    
    void setDifficult(DifficultClass difficult);
}