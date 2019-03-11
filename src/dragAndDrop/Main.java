
package dragAndDrop;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;



public class Main extends Application {
    static Tile[][] liste = new Tile[6][6];
    static Piece[][] piecesListe = new Piece[6][6];
    static int enemyhealth = 100;
    static Rectangle enemy = new Rectangle(100,100);

    private static final int tileSize = 100;
    private double oldPosX;
    private double oldPosY;


    //Har alle tiles i seg.
    GridPane ins = new GridPane();
    public void setOldPos(double x, double y){
        this.oldPosX = x;
        this.oldPosY = y;
    }



    @Override
    public void start(Stage primaryStage) throws Exception{
        Stage window = primaryStage;
        Scene scene1;

        StackPane sp = new StackPane();
        sp.setAlignment(Pos.BASELINE_LEFT);

        Grid testGrid = new Grid(7,7);
        ins.getChildren().add(testGrid.gp);


        //Bruker denne som spiller brikke foreløpig.
        Piece tile = new Piece(100,100);


        //Når musen klikkes utforbi spillerbrikken endres fargen tilbake til normalt.
        ins.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            tile.setStroke(Color.BLACK);
        });










        enemy.setTranslateY(500);



        //Legger alle tiles til i stackpane.
        sp.getChildren().addAll(ins, enemy,tile);

        Piece test = new Piece(100,100);

        test.setTranslateX(500);
        sp.getChildren().add(test);




            tile.addEventHandler(MouseEvent.MOUSE_CLICKED, e ->{
                setOldPos(tile.getTranslateX(), tile.getTranslateY() );
                tile.setStrokeType(StrokeType.INSIDE);
                tile.setStrokeWidth(3);
                tile.setStroke(Color.RED);


                enemy.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    enemyhealth -= 40;
                    System.out.println(enemyhealth);
                    if(enemyhealth<=0){
                        sp.getChildren().removeAll(enemy);
                        // må og fjernes fra eventuelle lister denne fienden kan ligge i.
                    }
                    ;
                });
            });
            
            //Mouse hovered over spillerbrikke så blir fargen på brikken rød.
            tile.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, event -> {
                tile.setFill(Color.RED);
            });

            //Når musen flyttes over grid og vekk fra spillerbrikke så blir orginalfarge satt tilbake igjen.
            ins.addEventHandler(MouseEvent.MOUSE_ENTERED_TARGET, event -> {
                tile.setFill(Color.BLACK);
            });


            //Drar spillerbrikke når mus blir holdt inne. 
            tile.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
                double rectPosX = tile.getLayoutX()+ tile.getWidth();
                double rectPosY = tile.getLayoutY() + tile.getHeight();
                double precRectPosX = tile.getLayoutX()+ tile.getWidth()/2;
                double precRectPosY = tile.getLayoutY() + tile.getHeight()/2;

                double posX = event.getSceneX();
                double posY =event.getSceneY();
                double movementX = posX-rectPosX;
                double movementY = posY-rectPosY;

                double movementPrecX = posX-precRectPosX;
                double movementPrecY = posY-precRectPosY;

                double a = (int) (Math.ceil(movementX/100.0))*100; // Runder til nærmeste 100 for snap to grid funksjonalitet
                double b = (int) (Math.ceil(movementY/100.0))*100; // Runder til nærmeste 100 for snap to grid funksjonalitet

                //System.out.println(oldPosX + "   " + oldPosY);
                tile.setTranslateX(movementPrecX);
                tile.setTranslateY(movementPrecY);

                tile.addEventHandler(MouseEvent.MOUSE_RELEASED, e ->{
                    //Hvis avstanden er 2 eller mindre
                    if(!(Math.abs(a/100-oldPosX/100)>2) && (!(Math.abs(b/100-oldPosY/100)>2))){
                        tile.setTranslateX(a);
                        tile.setTranslateY(b);
                    //Hvis avstanden er stører enn 2, flyttes ikke brikken.
                    } else{
                        tile.setTranslateX(oldPosX);
                        tile.setTranslateY(oldPosY);
                    }

                });

                //En måte å holde oversikt på posisjonen til spillerbrikken på. Oppgitt i 0,1, -- 1,2 osv. passer med array.
                //System.out.println("PosX: " + (int)tile.getTranslateX()/100 + " PosY: " + (int) tile.getTranslateY()/100);

                //tile.setTranslateY(posX-rectPosX);
                //tile.setTranslateY(posY-rectPosY);
            });





        scene1 = new Scene(sp, 500,400);

        window.setTitle("Hello World");
        window.setScene(scene1);
        window.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
