package dragAndDrop;



import sample.Controller.mainMenuController;

import java.sql.ResultSet;
import static Database.Variables.user_id;
import static Database.Variables.match_id;
import static Database.Variables.db;
import static sample.Controller.mainMenuController.shutDownThread;


public class Matchmaking extends Thread {
    private static boolean gameStarted=false;
    private int player_id =user_id;


    public void shutDown(){
        gameStarted = true;
    }


    public void run(){
        //Click to search for games and join if available
        match_id = db.matchMaking_search(player_id);

        //if none available create own game
        if(match_id<0){
            match_id = db.createGame(player_id);
        }

        //wait for other players to join
        while(!gameStarted){

            try {
                Thread.currentThread().sleep(2000); //Polling only every 2 seconds.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameStarted = db.pollGameStarted(match_id);
            if(gameStarted){
                try {
                    mainMenuController.startGame = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("Game started");
            }

            if(shutDownThread){
                gameStarted=true;
            }
        }
    }

    public int getMatch_id(){
        return match_id;
    }

    public void abortGame(int player_id){
        db.abortMatch(player_id);
    }
}