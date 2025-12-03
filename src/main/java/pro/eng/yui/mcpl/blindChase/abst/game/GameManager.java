package pro.eng.yui.mcpl.blindChase.abst.game;

import pro.eng.yui.mcpl.blindChase.lib.game.Game;

public class GameManager {

    private static GameManager instance;
    private GameManager() {
        // singleton
    }
    public static GameManager getGameManager() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }
    
    public Game game;
    
}
