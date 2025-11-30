package pro.eng.yui.mcpl.blindChase.lib.game;

public interface Game {
    
    void start();

    void end();
    
    void endForce();
    
    GameState getCurrentState();
}
