//  ██████╗ ██╗███╗   ██╗ █████╗ ██████╗ ██╗   ██╗    ██╗    ██╗ █████╗ ██████╗ ███████╗ █████╗ ██████╗ ███████╗  //
//  ██╔══██╗██║████╗  ██║██╔══██╗██╔══██╗╚██╗ ██╔╝    ██║    ██║██╔══██╗██╔══██╗██╔════╝██╔══██╗██╔══██╗██╔════╝  //
//  ██████╔╝██║██╔██╗ ██║███████║██████╔╝ ╚████╔╝     ██║ █╗ ██║███████║██████╔╝█████╗  ███████║██████╔╝█████╗    //
//  ██╔══██╗██║██║╚██╗██║██╔══██║██╔══██╗  ╚██╔╝      ██║███╗██║██╔══██║██╔══██╗██╔══╝  ██╔══██║██╔══██╗██╔══╝    //
//  ██████╔╝██║██║ ╚████║██║  ██║██║  ██║   ██║       ╚███╔███╔╝██║  ██║██║  ██║██║     ██║  ██║██║  ██║███████╗  //
//  ╚═════╝ ╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝        ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝  //
//                                                                                                                //

//   ######## ########    ###    ##     ##       ##   ########     //
//      ##    ##         ## ##   ###   ###     ####   ##    ##     //
//      ##    ##        ##   ##  #### ####       ##       ##       //
//      ##    ######   ##     ## ## ### ##       ##      ##        //
//      ##    ##       ######### ##     ##       ##     ##         //
//      ##    ##       ##     ## ##     ##       ##     ##         //
//      ##    ######## ##     ## ##     ##     ######   ##         //


package gameplay;

import Runnables.RunnableInterface;
import com.jfoenix.controls.JFXButton;
import database.Variables;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.SQLException;
import static database.Variables.*;
import java.util.ArrayList;
import java.util.Collections;
import javafx.geometry.Orientation;
import menus.Main;

public class GameLogic extends Application {
  ////BOARD SIZE CONTROLS////
  private static final int boardSize = 7;      //sets the number of tiles en each direction of the grid
  public static final int tileSize = 100;      //sets size of each tile on the grid
  private static final int playerSideSize = 2; //Used to set width of the placement area

  ////SCENE ELEMENTS////
  private Grid grid = new Grid(boardSize, boardSize);
  private Pane root;
  private Label description = new Label();                //description label for the selected unit
  private Label turnCounter = new Label("TURN: " + turn); //describe which turn it is

  private Thread waitTurnThread;

  ////WINDOW SIZE////
  private final int windowWidth = 1920;
  private final int windowHeight = 1080;

  ////SIZE VARIABLES////
  private final int buttonWidth = 150;
  private final int buttonHeight = 75;

  ////PANE PADDINGS////
  //GRID//
  private final int gridXPadding = 300;
  private final int gridYPadding = 100;
  //PLACMENT PHASE SIDE PANEL//
  private final int recruitXPadding = gridXPadding + tileSize*boardSize + 150;
  private final int recruitYPadding = 150;
  private final int placementButtonXPadding = 150;
  private final int placementButtonYPadding = 500;
  //MOVEMENT AND ATTACK PHASE SIDE PANEL//
  private final int sidePanelXPadding = gridXPadding + tileSize*boardSize + 150;
  private final int sidePanelYPadding = 150;
  private final int descriptionXPadding = 0;
  private final int descriptionYPadding = 0;
  private final int turnCounterXPadding = 150;
  private final int turnCounterYPadding = 0;
  private final int endTurnButtonXPadding = 150;
  private final int endTurnButtonYPadding = 500;
  private final int surrenderButtonXPadding = 150;
  private final int surrenderButtonYPadding = 580;

  ////GAME CONTROL VARIABLES////
  private boolean unitSelected = false;
  private int moveCounter = 0;                                         // Counter for movement phase.
  private int attackCount = 0;                                        // Counter for attack phase.
  private Unit selectedUnit;
  private ArrayList<Move> movementList = new ArrayList<>();           //Keeps track of the moves made for the current turn.
  private ArrayList<Attack> attackList = new ArrayList<>();           //Keeps track of the attacks made for the current turn.
  private ArrayList<Move> importedMovementList = new ArrayList<>();   //Keeps track of the moves made during the opponents turn
  private ArrayList<Attack> importedAttackList = new ArrayList<>();   //Keeps track of the attacks made during the opponents turn
  private boolean movementPhase = true;                               //Controls if the player is in movement or attack phase
  private UnitGenerator unitGenerator = new UnitGenerator();
  ArrayList<PieceSetup> setupPieces;

  ////STYLING////
  private String gameTitle = "PAPER LEGION";
  private String descriptionFont = "-fx-font-family: 'Arial Black'";
  private String buttonBackgroundColor = "-fx-background-color: #000000";
  private String turnCounterFontSize = "-fx-font-size: 32px";
  private Paint selectionOutlineColor = Color.RED;
  private Paint buttonTextColor = Color.WHITE;
  private Paint movementHighlightColor = Color.GREENYELLOW;
  private Paint attackHighlightColor = Color.DARKRED;
  private Paint untargetableTileColor = Color.color(155.0/255.0, 135.0/255.0, 65.0/255.0);

  public void start(Stage window) throws Exception {
    // Sets static variables for players and opponent id.
    db.getPlayers();

    SetUp setUp = new SetUp();
    setUp.importUnitTypes();

    root = new Pane();
    Scene scene = new Scene(root, windowWidth, windowHeight);

    Pane gridPane = createGrid(); //creates the grid

    placementPhaseStart(); //starts the placement phase


    window.setTitle(gameTitle);
    window.setScene(scene);
    window.show();
  }

  private void placementPhaseStart() {
    Pane recruitPane = createRecruitPane();

    JFXButton finishedPlacingButton = new JFXButton("Finished placing units"); //creates a button for ending the placementphase
    finishedPlacingButton.setMinSize(buttonWidth, buttonHeight);
    finishedPlacingButton.setTextFill(buttonTextColor);
    finishedPlacingButton.setStyle(buttonBackgroundColor);
    finishedPlacingButton.setLayoutX(placementButtonXPadding);
    finishedPlacingButton.setLayoutY(placementButtonYPadding);

    recruitPane.getChildren().add(finishedPlacingButton);

    int playerSideTop, playerSideBottom; //sets paddings depending on player side (to the coloring of the boardtiles as well as untargetability)
    if (user_id == player1) {
      playerSideTop = playerSideSize;
      playerSideBottom = 0;
    } else {
      playerSideTop = 0;
      playerSideBottom = playerSideSize;
    }

    for (int i = playerSideTop; i < boardSize - playerSideBottom; i++) { //colors and makes tiles untargetable
      for (int j = 0; j < boardSize; j++) {
        grid.tileList[i][j].setFill(untargetableTileColor);
        grid.tileList[i][j].setUntargetable();
      }
    }

    finishedPlacingButton.setOnAction(event -> { //when button pressed, finishes the placementphase
      placementPhaseFinished(recruitPane);
    });
  }

  private void placementPhaseFinished(Pane recruitPane) {
    root.getChildren().remove(recruitPane); //removes recruitmentpane with all necessities tied to placementphase

    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        grid.tileList[i][j].setFill(Color.WHITE); //sets the grid all white again, and sets all tiles untargetable for dragboard as a safety measure
        grid.tileList[i][j].setUntargetable();
      }
    }

    ArrayList<Unit> exportUnitList = new ArrayList<>();
    ArrayList<Integer> exportPositionXList = new ArrayList<>();
    ArrayList<Integer> exportPositionYList = new ArrayList<>();

    for (int i = 0; i < grid.tileList.length; i++) {
      for (int j = 0; j < grid.tileList[i].length; j++) {

        if(grid.tileList[i][j].getUnit() != null){
          exportUnitList.add(grid.tileList[i][j].getUnit());
          exportPositionXList.add(j);
          exportPositionYList.add(i);
        }
      }
    }

    if (exportUnitList != null){
      db.exportPlacementUnits(exportUnitList, exportPositionXList, exportPositionYList);
    }
  }

  private void movementActionPhaseStart() {
    Pane sidePanel = createSidePanel();

    JFXButton endTurnButton = new JFXButton("End turn");
    endTurnButton.setMinSize(buttonWidth, buttonHeight);
    endTurnButton.setTextFill(buttonTextColor);
    endTurnButton.setStyle(buttonBackgroundColor);
    endTurnButton.setLayoutX(endTurnButtonXPadding);
    endTurnButton.setLayoutY(endTurnButtonYPadding);

    JFXButton surrenderButton = new JFXButton("Surrender");
    surrenderButton.setMinSize(buttonWidth, buttonHeight);
    surrenderButton.setTextFill(buttonTextColor);
    surrenderButton.setStyle(buttonBackgroundColor);
    surrenderButton.setLayoutX(surrenderButtonXPadding);
    surrenderButton.setLayoutY(surrenderButtonYPadding);

    sidePanel.getChildren().addAll(endTurnButton, surrenderButton);

    //If you are player 2. Start polling the database for next turn.
    if (!yourTurn) {
      endTurnButton.setText("Waiting for other player");
      waitForTurn(endTurnButton);
    } else {
      //Enters turn 1 into database.
      db.sendTurn(turn);
    }

    endTurnButton.setOnAction(event -> {
      endTurn(endTurnButton);
    });

    surrenderButton.setOnAction(event -> {
      surrender();
    });

    grid.addEventHandler(event -> {

    });
  }

  private void endTurn(JFXButton endTurnButton) {
    if (yourTurn) {

      //Increments turn. Opponents Turn.
      turn++;

      turnCounter.setText("TURN: " + turn);
      endTurnButton.setText("Waiting for other player");
      yourTurn = false;


      ////SEND MOVEMENT////

      if (movementList.size() != 0) {
        System.out.println("SENDING MOVE LIST!");
        db.exportMoveList(movementList); //when we use movement table use this
        ////Old method////
        //db.exportPieceMoveList(movementList);
        movementList = new ArrayList<>(); //Resets the movementList for the next turn.
      }


      /////SEND ATTACKS////

      if(attackList.size() != 0){
        db.exportAttackList(attackList);
        attackList = new ArrayList<>(); //Resets the attackList for the next turn.
      }

      // Finds every enemy unit that was damaged and sends their new info the database.


      //Add the next turn into the database.
      db.sendTurn(turn);

      //de-selects the currently selected unit
      deselect();

      //Resets hasAttackedThisTurn for all units
      for (int i = 0; i < grid.tileList.length; i++) {
        for (int j = 0; j < grid.tileList[i].length; j++) {
          grid.tileList[i][j].getUnit().setHasAttackedThisTurn(false);
        }
      }

      //Check if you have won
      checkForGameOver();

      //Wait for you next turn
      waitForTurn(endTurnButton);
    }
  }

  private void waitForTurn(JFXButton endTurnButton) {

    // Runnable lambda implementation for turn waiting with it's own thread
    RunnableInterface waitTurnRunnable = new RunnableInterface() {
      private boolean doStop = false;

      @Override
      public void run() {
        while(keepRunning()){
          try {
            while (!yourTurn) {
              System.out.println("Sleeps thread " + Thread.currentThread());
              Thread.sleep(1000);
              //When player in database matches your own user_id it is your turn again.
              System.out.println("Whose turn is it? " + db.getTurnPlayer());
              if (db.getTurnPlayer() == user_id) {
                System.out.println("yourTurn changes");
                yourTurn = true;
                this.doStop();
              }
            }


            //What will happen when it is your turn again.

            //Increments turn. Back to your turn.
            Platform.runLater(()->{

              setUpNewTurn(endTurnButton);

            });

            movementPhase = true;

          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }

      @Override
      public synchronized void doStop(){
        this.doStop = true;
      }

      @Override
      public synchronized boolean keepRunning(){
        return !this.doStop;
      }
    };

    waitTurnThread = new Thread(waitTurnRunnable);
    waitTurnThread.start();
  }

  private void setUpNewTurn(JFXButton endTurnButton){
    deselect();
    selectedUnit = null;
    turn++;
    turnCounter.setText("TURN: " + turn);
    endTurnButton.setText("End turn");

    importedMovementList = db.importMoveList(turn-1, match_id);
    importedAttackList = db.importAttackList(turn-1, match_id, opponent_id);

    System.out.println("importedAttackList size is: " + importedAttackList.size());

    ////EXECUTES MOVES FROM OPPONENTS TURN////
    for (int i = 0; i < importedMovementList.size(); i++) {

      //gets the unit from the tile and removes it from the same tile
      Unit movingUnit = grid.tileList[importedMovementList.get(i).getStartPosY()][importedMovementList.get(i).getStartPosX()].getUnit();
      grid.tileList[importedMovementList.get(i).getStartPosY()][importedMovementList.get(i).getStartPosX()].removeUnit();
      //adds the unit to new tile
      grid.tileList[importedMovementList.get(i).getEndPosY()][importedMovementList.get(i).getEndPosX()].setUnit(movingUnit);
    }

    ////EXECUTES ATTACKS FROM OPPONENTS TURN////
    for (int i = 0; i < importedAttackList.size(); i++) {

      for (int j = 0; j < grid.tileList.length; j++) {
        for (int k = 0; k < grid.tileList[j].length; k++) {
          if(!grid.tileList[j][k].getUnit().getEnemy() && grid.tileList[j][k].getUnit().getPieceId() == importedAttackList.get(i).getReceiverPieceID()){
            System.out.println(importedAttackList.get(i).getDamage());

            System.out.println("DOING AN ATTACK!" + grid.tileList[j][k].getUnit().getHp());

            grid.tileList[j][k].getUnit().takeDamage(importedAttackList.get(i).getDamage());

            //If units health is zero. Remove it from the board.
            if (grid.tileList[j][k].getUnit().getHp() <= 0) {
              //TODO legg til at uniten blir skada inn i databasen med en gang, før den blir slettet. (sett hp 0)
              grid.tileList[j][k].removeUnit();
            }
          }
        }

      }
    }

    checkForGameOver();
  }

  private void surrender() {
    //TODO: Surrender
  }

  private void checkForGameOver() {
    String win_loseText;
    String gameSummary = "";
    int loser = -1;
    int eliminationResult = checkForEliminationVictory();
    int surrenderResult = db.checkForSurrender();

    if (eliminationResult != -1) {
      gameSummary = "The game ended after a player's unit were all eliminated after " + turn + " turns\n";
      loser = eliminationResult;
    } else if (surrenderResult == user_id || surrenderResult == opponent_id) {
      gameSummary = "The game ended after a player surrendered the match after " + turn + " turns\n";
      loser = surrenderResult;
    }

    if (loser != -1) {
      //Game is won or lost.
      gameCleanUp();
      //Open alert window.
      Stage winner_alert = new Stage();
      winner_alert.initModality(Modality.APPLICATION_MODAL);
      winner_alert.setTitle("Game over!");

      Text winnerTextHeader = new Text();
      Text winnerText = new Text();
      winnerTextHeader.setStyle("-fx-font-size:32px;");
      winnerTextHeader.setBoundsType(TextBoundsType.VISUAL);
      //db.incrementGamesPlayed();

      if (loser == user_id) {
        win_loseText = "You Lose!\n";
      } else if (loser == opponent_id){
        win_loseText = "You Win!\n";
      } else {
        win_loseText = "Something went wrong\n";
      }

      winnerTextHeader.setText(win_loseText);
      winnerText.setText(gameSummary);

      JFXButton endGameBtn = new JFXButton("Return to menu");

      endGameBtn.setOnAction(event -> {
        String fxmlDir = "/menus/View/mainMenu.fxml";
        Parent root = null;
        try {
          root = FXMLLoader.load(this.getClass().getResource(fxmlDir));
        } catch (IOException e) {
          e.printStackTrace();
          System.out.println("load failed");
        }
        winner_alert.close();
        Main.window.setScene(new Scene(root));
      });

      VBox content = new VBox();
      content.setAlignment(Pos.CENTER);
      content.setSpacing(20);
      content.getChildren().addAll(winnerTextHeader, winnerText, endGameBtn);
      Scene scene = new Scene(content, 450, 200);
      winner_alert.initStyle(StageStyle.UNDECORATED);
      winner_alert.setScene(scene);
      winner_alert.showAndWait();
    }
  }

  private int checkForEliminationVictory() {
    int yourPieces = 0;
    int opponentsPieces = 0;
    //Goes through all units and counts how many are alive for each player.
    for (int i = 0; i < grid.tileList.length; i++) {
      for (int j = 0; j < grid.tileList[i].length; j++) {
        if (grid.tileList[i][j].getUnit() != null) {
          if (grid.tileList[i][j].getUnit().getEnemy()) {
            opponentsPieces++;
          } else {
            yourPieces++;
          }
        }
      }
    }
    System.out.println("YourPiceces " + yourPieces + " opponent: " + opponentsPieces);
    if (yourPieces == 0) {
      return 1;
    } else if (opponentsPieces == 0) {
      return 0;
    } else {
      return -1;
    }
  }

  private void gameCleanUp() {

    //Stuff that need to be closed or reset. Might not warrant its own method.
    if (Variables.waitTurnThread.isAlive()) {
      Variables.waitTurnThread.stop();
    }

    //Sets turns back to 1 for next match.
    turn = 1;
    match_id = -1;
    player1 = -1;
    player2 = -1;
    opponent_id = -1;
  }

  private int getPosXFromEvent(MouseEvent event) {
    return (int)Math.ceil((event.getX() - gridXPadding) / tileSize);
  }

  private int getPosYFromEvent(MouseEvent event) {
    return (int)Math.ceil((event.getY() - gridYPadding) / tileSize);
  }

  private void select() {

  }

  private void deselect() {

  }

  private Pane createGrid() { //adds grid and styles it
    Pane gridPane = new Pane();

    gridPane.getChildren().add(grid);

    gridPane.setLayoutX(gridXPadding);
    gridPane.setLayoutY(gridYPadding);
    root.getChildren().add(gridPane);

    return gridPane;
  }

  private Pane createRecruitPane() { //adds unit selector/recruiter and styles it
    Pane unitPane = new Pane();
    FlowPane units = new FlowPane(Orientation.HORIZONTAL, 5, 5);

    units.setMinWidth(520);

    for (int i = 0; i < SetUp.unitTypeList.size(); i++) {
      RecruitTile tile = new RecruitTile(tileSize, tileSize, unitGenerator.newRecruit(SetUp.unitTypeList.get(i)));
      units.getChildren().add(tile);
    }
    unitPane.getChildren().add(units);

    unitPane.setLayoutX(recruitXPadding);
    unitPane.setLayoutY(recruitYPadding);
    root.getChildren().add(unitPane);

    return unitPane;
  }

  private Pane createSidePanel() { //creates the side panel for movement/attack phase
    Pane sidePanel = new Pane();

    description.setStyle(descriptionFont);
    description.setLayoutX(descriptionXPadding);
    description.setLayoutY(descriptionYPadding);
    description.setVisible(false);

    turnCounter.setStyle(turnCounterFontSize);
    turnCounter.setLayoutX(descriptionXPadding);
    turnCounter.setLayoutY(descriptionYPadding);

    sidePanel.getChildren().addAll(description, turnCounter);

    sidePanel.setLayoutX(sidePanelXPadding);
    sidePanel.setLayoutY(sidePanelYPadding);
    root.getChildren().add(sidePanel);

    return sidePanel;
  }
}
