/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetrisgame;

import java.io.IOException;
import java.io.RandomAccessFile;
import javafx.application.Application;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
/**
 *
 * @author JAHNICS
 */
public class TetrisGame extends Application implements TetrisBrickConstants {
    GridPane mainGamePane = new GridPane();
    //an array of panes for the grids where cells are displayed
    Pane[][] grid = new Pane[20][13], nextBrickGrid = new Pane[4][4];
    TetrisBrick brick,nextBrick;
    BorderPane root = new BorderPane();
    Timeline animation;
    int bricksHeight = grid.length, score = 0;
    TextField scoref = new TextField();
    
    @Override
    public void start(Stage primaryStage) {
        gamePage();
        animation = new Timeline(new KeyFrame(Duration.millis(500), e-> {
            moveBrick();
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        
        Scene scene = new Scene(root, grid.length*20+2, grid.length*20);
        
        primaryStage.setTitle("Tetris Game");
        primaryStage.setScene(scene);
        primaryStage.setHeight(grid.length*20+2);
        primaryStage.setWidth(grid.length*20);
        primaryStage.setResizable(false);
        primaryStage.show();
        root.requestFocus();
    }
    
    //Setting the pane for game
    private void gamePage() {
        brick = new TetrisBrick((int)Math.round(1 + Math.random() * 5), grid[0].length);
        //initializing the main game pane 
        for (int row = 0; row < grid.length; row++) {
            for (int column = 0; column < grid[row].length; column++) {
                grid[row][column] = new Pane();
                grid[row][column].setPrefSize(20,20);
                grid[row][column].setStyle("-fx-background-color: azure");
                mainGamePane.add(grid[row][column], column, row);
            }
        }
        mainGamePane.setPrefSize(20 * grid[0].length, 20 * grid.length);
        mainGamePane.setStyle("-fx-border-color:black; -fx-border-width:2");
        //The controls pane
        VBox aside = new VBox();
        GridPane nextBrickPane = new GridPane();
        //initializing nextbrick pane
        for (int x = 0; x < nextBrickGrid.length; x++) {
            for (int y = 0; y < nextBrickGrid[x].length; y++) {
                nextBrickGrid[x][y] = new Pane();
                nextBrickGrid[x][y].setPrefSize(20,20);
                nextBrickGrid[x][y].setStyle("-fx-background-color: azure");
                nextBrickPane.add(nextBrickGrid[x][y], y, x);
            }
        }
        nextBrick = new TetrisBrick((int)Math.round(1 + Math.random() * 5), grid.length);
        addNextBrick();
        //Game progress
        GridPane info = new GridPane();
        info.add(new Text("Score: "), 0,0);
        scoref.setText("  " + score);
        scoref.setEditable(false);
        info.add(scoref, 1, 0);
        aside.getChildren().add(nextBrickPane);
        aside.getChildren().add(info);
        Text clu = new Text("Press Enter to start game.");
        aside.getChildren().add(clu);
        aside.setSpacing(5);
        aside.setPrefSize((grid.length-grid[0].length)*20, root.getHeight());
        aside.setStyle("-fx-background-color:floralwhite");
        //Game controls
        root.setOnKeyPressed(e -> {
            if (null != e.getCode()) switch (e.getCode()) {
                case ESCAPE:
                if (clu.getText().contains("pause")){
                    pauseGame();
                }
                case ENTER:
                    if (clu.getText().contains("start")) {
                        animation.play();
                        clu.setText("Press Esc to pause game.");
                    }
                    break;
                case RIGHT:
                    shiftBrick(0);
                    break;
                case LEFT:
                    shiftBrick(1);
                    break;
                case UP: {
                    System.out.println("rotate");
                    if (canMove()){
                        animation.pause();
                    rotateBrick();}
                }
                    break;
                case DOWN: {
                    moveBrick();
                }
                default:
                    break;
            }
        });
        addNewBrick();
        root.setCenter(mainGamePane);
        root.setRight(aside);
        root.requestFocus();
    }
    //handle game pause
    public void pauseGame() {
        animation.pause();
        Node gamef = root.getCenter();
        Node contr = root.getRight();
        VBox gamePausePane = new VBox();
        //Play button
        Button playbtn = new Button("PLAY");
        playbtn.setOnAction(e -> {
           root.setRight(contr);
           gamef.setOpacity(1);
           animation.play();
           root.requestFocus();
        });
        //restart button
        Button restartbtn = new Button("RESTART");
        restartbtn.setOnAction(e -> {
            gamef.setOpacity(1);
            restartGame(); 
        });
        //exit Button
        Button exitbtn = new Button("EXIT");
        exitbtn.setOnAction(e -> {
           System.exit(0); 
        });
        gamePausePane.getChildren().addAll(playbtn, restartbtn, exitbtn);
        gamePausePane.setAlignment(Pos.CENTER);
        gamePausePane.setStyle("-fx-background-color:floralwhite");
        gamePausePane.setSpacing(5);
        gamePausePane.setPrefSize((grid.length-grid[0].length)*20, root.getHeight());
        gamef.setOpacity(0.5);
        root.setRight(gamePausePane);
    }
    //handle game over
    public void gameOver() {
        VBox gameOverPane = new VBox();
        //restart button
         Button restartBtn = new Button("RESTART");
         restartBtn.setOnAction(e -> {
            restartGame();
         });
         //exit button
         Button exitbtn = new Button("EXIT");
         exitbtn.setOnAction(e -> {
            System.exit(0); 
         });
         Text text = new Text();
         
         //Updating the scores
         RandomAccessFile inout;
         try {
            inout = new RandomAccessFile("hscore.ilovtech", "rw");
            int oldScore = 0;
            if (inout.length() > 0) {
               oldScore = inout.readInt();
                
            }
            
            if (score > oldScore) {
                text.setText("Congratulations! \nNew High Score: " + score);
                inout.seek(0);
                inout.writeInt(score);
            }
            else {
                text.setText("Total Score: " + score + "\nHigh Score: " + oldScore);
            }
         }
         catch (IOException ex) {
             
             ex.printStackTrace();
         }
         gameOverPane.getChildren().addAll(
        new Text("Game Over!"), text, restartBtn, exitbtn);
        
        gameOverPane.setStyle("-fx-background-color: darkgrey;");
        gameOverPane.setSpacing(20);
        gameOverPane.setAlignment(Pos.CENTER);
        root.getRight().setOpacity(0.1);
        root.getRight().disableProperty();
        root.setCenter(gameOverPane);
    }
    //Handle game restart
    public void restartGame() {
        root.getChildren().clear();
        score = 0;
        bricksHeight = grid.length;
        scoref.setText("  "+score);
        gamePage();
    }
    public void addNextBrick() {
        //remove the old brick
        for (int r = 0; r < nextBrickGrid.length; r++) {
            for (int c = 0; c < nextBrickGrid[r].length; c++) {
                if (!nextBrickGrid[r][c].getChildren().isEmpty())
                nextBrickGrid[r][c].getChildren().remove(0);
            }
        }
        switch (nextBrick.getType()) {
            case BOX: {
                nextBrickGrid[1][1].getChildren().add(nextBrick.get(0));
                nextBrickGrid[1][2].getChildren().add(nextBrick.get(1));
                nextBrickGrid[2][1].getChildren().add(nextBrick.get(2));
                nextBrickGrid[2][2].getChildren().add(nextBrick.get(3));
            }
            break;
            case LEFT_L: {
                nextBrickGrid[0][2].getChildren().add(nextBrick.get(0));
                nextBrickGrid[1][2].getChildren().add(nextBrick.get(1));
                nextBrickGrid[2][2].getChildren().add(nextBrick.get(2));
                nextBrickGrid[2][1].getChildren().add(nextBrick.get(3));
            }
            break;
            case RIGHT_L: {
                nextBrickGrid[0][1].getChildren().add(nextBrick.get(0));
                nextBrickGrid[1][1].getChildren().add(nextBrick.get(1));
                nextBrickGrid[2][1].getChildren().add(nextBrick.get(2));
                nextBrickGrid[2][2].getChildren().add(nextBrick.get(3));
            }
            break;
            case LEFT_Z: {
                nextBrickGrid[2][0].getChildren().add(nextBrick.get(0));
                nextBrickGrid[2][1].getChildren().add(nextBrick.get(1));
                nextBrickGrid[1][1].getChildren().add(nextBrick.get(2));
                nextBrickGrid[1][2].getChildren().add(nextBrick.get(3));
            }
            break;
            case RIGHT_Z: {
                nextBrickGrid[1][0].getChildren().add(nextBrick.get(0));
                nextBrickGrid[1][1].getChildren().add(nextBrick.get(1));
                nextBrickGrid[2][1].getChildren().add(nextBrick.get(2));
                nextBrickGrid[2][2].getChildren().add(nextBrick.get(3));
            }
            break;
            case STANDING: {
                nextBrickGrid[0][1].getChildren().add(nextBrick.get(0));
                nextBrickGrid[1][1].getChildren().add(nextBrick.get(1));
                nextBrickGrid[2][1].getChildren().add(nextBrick.get(2));
                nextBrickGrid[3][1].getChildren().add(nextBrick.get(3));
            }
            break;
        }
    }
    //rotating the brick
    public void rotateBrick() {
        
        switch (brick.getType()) {
            case RIGHT_L: {
                switch (brick.getOrientation()) {
                    case UP: {
                        rotateURL();
                    }
                    break;
                    case RIGHT: {
                        rotateRRL();
                    }
                    break;
                    case DOWN: {
                        rotateDRL();
                    }
                    break;
                    case LEFT: {
                        rotateLRL();
                    }
                    break;
                }
            }
            break;
            case LEFT_L: {
                switch (brick.getOrientation()) {
                    case UP: {
                        rotateULL();
                    }
                    break;
                    case RIGHT: {
                        rotateRLL();
                    }
                    break;
                    case DOWN: {
                        rotateDLL();
                    }
                    break;
                    case LEFT: {
                        rotateLLL();
                    }
                    break;
                }
            }
            break;
            case RIGHT_Z: {
                switch (brick.getOrientation()) {
                    case UP: {
                        rotateURZ();
                    }
                    break;
                    case RIGHT: {
                        rotateRRZ();
                    }
                    break;
                }
            }
            break;
            case LEFT_Z: {
                switch (brick.getOrientation()) {
                    case UP: {
                        rotateULZ();
                    }
                    break;
                    case RIGHT: {
                        rotateRLZ();
                    }
                    break;
                }
            }
            break;
            case STANDING: {
                if (brick.getOrientation() == UP) {
                    rotateUS();
                }
                else if (brick.getOrientation() == RIGHT) {
                    rotateRS();
                }
            }
            break;
            default: break;
        }
        animation.play();
    }
    public void rotateUS() {
        //proceeding if the brick is not at the edges
        if (brick.get(0).getColumn() +1 < grid[0].length && brick.get(0).getColumn() -1 > -1) {
            //The brick can rotate if its environment is clear
            boolean canRotate = grid[brick.get(3).getRow()][brick.get(3).getColumn()-1].getChildren().isEmpty()
                    && grid[brick.get(3).getRow()][brick.get(3).getColumn()+1].getChildren().isEmpty();
            if (brick.get(0).getRow() > -3)
                canRotate = grid[brick.get(2).getRow()][brick.get(2).getColumn()+1].getChildren().isEmpty();
            if (brick.get(0).getRow() > -2)
                canRotate = grid[brick.get(1).getRow()][brick.get(1).getColumn()+1].getChildren().isEmpty();
            if (brick.get(0).getRow() > -1)
                canRotate = grid[brick.get(0).getRow()][brick.get(0).getColumn()+1].getChildren().isEmpty();
            
            if (brick.get(3).getColumn()+2 < grid[0].length) {
            //Rotating while shifting to the right if there is space
                if (canRotate && grid[brick.get(3).getRow()][brick.get(3).getColumn()+2].getChildren().isEmpty()) {
                    brick.get(0).setRow(brick.get(3).getRow());
                    brick.get(0).setColumn(brick.get(3).getColumn() +2);
                    brick.get(0).setPos(true, true, true, false);
                    brick.get(1).setRow(brick.get(3).getRow());
                    brick.get(1).setColumn(brick.get(3).getColumn() +1);
                    brick.get(1).setPos(true, false, true, false);
                    brick.get(2).setPos(true, false, true, false);
                    brick.get(2).setRow(brick.get(3).getRow());
                    brick.get(3).setColumn(brick.get(3).getColumn() - 1);
                    brick.get(3).setPos(true, false, true, true);
                    for (int i =0; i < brick.size(); i++) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                    brick.setOrientation(RIGHT);
                }
                
                //Try left side
                else if (brick.get(3).getColumn()-2 >-1) {
                //Rotating while shifting to the left
                if (canRotate && grid[brick.get(3).getRow()][brick.get(3).getColumn()-2].getChildren().isEmpty()
                        && grid[brick.get(2).getRow()][brick.get(2).getColumn()-1].getChildren().isEmpty()) {
                    brick.get(0).setRow(brick.get(3).getRow());
                    brick.get(0).setColumn(brick.get(3).getColumn() +1);
                    brick.get(0).setPos(true, true, true, false);
                    brick.get(1).setRow(brick.get(3).getRow());
                    brick.get(1).setPos(true, false, true, false);
                    brick.get(2).setRow(brick.get(3).getRow());
                    brick.get(2).setColumn(brick.get(2).getColumn()-1);
                    brick.get(2).setPos(true, false, true, false);
                    brick.get(3).setColumn(brick.get(3).getColumn() - 2);
                    brick.get(3).setPos(true, false, true, true);
                    for (int i =0; i < brick.size(); i++) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                    brick.setOrientation(RIGHT);
                }
            }
            }
            //Try left if it is near to the edge
            else if (brick.get(3).getColumn()-2 >-1) {
                //Rotating while shifting to the left
                if (canRotate && grid[brick.get(3).getRow()][brick.get(3).getColumn()-2].getChildren().isEmpty()
                        && grid[brick.get(2).getRow()][brick.get(2).getColumn()-1].getChildren().isEmpty()) {
                    brick.get(0).setRow(brick.get(3).getRow());
                    brick.get(0).setColumn(brick.get(3).getColumn() +1);
                    brick.get(0).setPos(true, true, true, false);
                    brick.get(1).setRow(brick.get(3).getRow());
                    brick.get(1).setPos(true, false, true, false);
                    brick.get(2).setRow(brick.get(3).getRow());
                    brick.get(2).setColumn(brick.get(2).getColumn()-1);
                    brick.get(2).setPos(true, false, true, false);
                    brick.get(3).setColumn(brick.get(3).getColumn() - 2);
                    brick.get(3).setPos(true, false, true, true);
                    for (int i =0; i < brick.size(); i++) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                    brick.setOrientation(RIGHT);
                }
            }
        }
    }
    public void rotateRS() {
        if (brick.get(0).getRow() > 0) {
            //The brick can rotate if it's rotation environment is clear
            boolean canRotate = grid[brick.get(3).getRow()-1][brick.get(3).getColumn()].getChildren().isEmpty()
                    && grid[brick.get(2).getRow()-1][brick.get(2).getColumn()].getChildren().isEmpty()
                    && grid[brick.get(1).getRow()-1][brick.get(1).getColumn()].getChildren().isEmpty();
            if (brick.get(0).getRow() > 1)
                canRotate = grid[brick.get(2).getRow()-2][brick.get(2).getColumn()].getChildren().isEmpty()
                        && grid[brick.get(1).getRow()-2][brick.get(1).getColumn()].getChildren().isEmpty();
            if (brick.get(0).getRow() > 2)
                canRotate = grid[brick.get(1).getRow()-3][brick.get(1).getColumn()].getChildren().isEmpty();
            
            //rotating to the column of '1'
            if (canRotate) {
                //Removing all the cells from the display before rotating to hide those above row 0
                for (int i = 0; i < brick.size(); i++) {
                    grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().remove(0);
                }
                    brick.get(0).setRow(brick.get(0).getRow()-3);
                    brick.get(0).setColumn(brick.get(1).getColumn());
                    brick.get(0).setPos(false, true, true, true);
                    brick.get(1).setRow(brick.get(1).getRow()-2);
                    brick.get(1).setPos(false, true, false, true);
                    brick.get(2).setRow(brick.get(2).getRow()-1);
                    brick.get(2).setColumn(brick.get(1).getColumn());
                    brick.get(2).setPos(false, true, false, true);
                    brick.get(3).setColumn(brick.get(1).getColumn());
                    brick.get(3).setPos(true, true, false, true);
                    for (int i =0; i < brick.size(); i++) {
                        if (brick.get(i).getRow() > -1)
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                    brick.setOrientation(UP);
            }
        }
    }
    public void rotateULL() {
        
        if (brick.get(2).getColumn() + 1 < grid[0].length) {
            boolean canRotate = grid[brick.get(2).getRow()][brick.get(2).getColumn()+1].getChildren().isEmpty()
                    && grid[brick.get(2).getRow()+1][brick.get(2).getColumn()+1].getChildren().isEmpty();
            //rotating while extending to the right
            if (brick.get(0).getRow() < -1) {
                if (canRotate) {
                brick.get(0).setRow(brick.get(2).getRow()+1);
                brick.get(0).setColumn(brick.get(2).getColumn()+1);
                brick.get(0).setPos(true, true, true, false);
                brick.get(1).setRow(brick.get(2).getRow() +1);
                brick.get(1).setPos(true, false, true, false);
                brick.get(2).setColumn(brick.get(3).getColumn());
                brick.get(2).setRow(brick.get(3).getRow()+1);
                brick.get(2).setPos(true, false, false, true);
                brick.get(3).setPos(false, true, true, true);
                for (int i = 0; i < brick.size(); i++) {
                    if (i != 3) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                }
                brick.setOrientation(RIGHT);
            }
            }
            else if (brick.get(0).getRow() < 0 ) {
                if (canRotate
                    && grid[brick.get(1).getRow()][brick.get(1).getColumn()+1].getChildren().isEmpty() ) {
                brick.get(0).setRow(brick.get(2).getRow()+1);
                brick.get(0).setColumn(brick.get(2).getColumn()+1);
                brick.get(0).setPos(true, true, true, false);
                brick.get(1).setRow(brick.get(2).getRow() +1);
                brick.get(1).setPos(true, false, true, false);
                brick.get(2).setColumn(brick.get(3).getColumn());
                brick.get(2).setRow(brick.get(3).getRow()+1);
                brick.get(2).setPos(true, false, false, true);
                brick.get(3).setPos(false, true, true, true);
                for (int i = 0; i < brick.size(); i++) {
                    if (i != 3) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                }
                brick.setOrientation(RIGHT);
            }
            }
            else if (brick.get(0).getRow() > -1) {
                if (canRotate
                    && grid[brick.get(1).getRow()][brick.get(1).getColumn()+1].getChildren().isEmpty()
                    && grid[brick.get(0).getRow()][brick.get(0).getColumn()+1].getChildren().isEmpty()) {
                brick.get(0).setRow(brick.get(2).getRow()+1);
                brick.get(0).setColumn(brick.get(2).getColumn()+1);
                brick.get(0).setPos(true, true, true, false);
                brick.get(1).setRow(brick.get(2).getRow() +1);
                brick.get(1).setPos(true, false, true, false);
                brick.get(2).setColumn(brick.get(3).getColumn());
                brick.get(2).setRow(brick.get(3).getRow()+1);
                brick.get(2).setPos(true, false, false, true);
                brick.get(3).setPos(false, true, true, true);
                for (int i = 0; i < brick.size(); i++) {
                    if (i != 3) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                }
                brick.setOrientation(RIGHT);
            }
            }
        }
    }
    public void rotateRLL() {
        if (grid[brick.get(3).getRow()][brick.get(3).getColumn()+1].getChildren().isEmpty()) {
            brick.get(0).setRow(brick.get(2).getRow()+1);
            brick.get(0).setColumn(brick.get(2).getColumn());
            brick.get(0).setPos(true, true, false, true);
            brick.get(1).setColumn(brick.get(2).getColumn());
            brick.get(1).setPos(false, true, false, true);
            brick.get(2).setRow(brick.get(3).getRow());
            brick.get(2).setPos(false, false, true, true);
            brick.get(3).setColumn(brick.get(2).getColumn() + 1);
            brick.get(3).setPos(true, true, true, false);
                for (int i = 0; i < brick.size(); i++) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                }
            brick.setOrientation(DOWN);
        }
    }
    public void rotateDLL() {
         if (brick.get(3).getColumn() + 1 < grid[0].length) {
            if (grid[brick.get(2).getRow()][brick.get(2).getColumn() + 2].getChildren().isEmpty()
                    && grid[brick.get(1).getRow()][brick.get(1).getColumn() + 2].getChildren().isEmpty()
                    && grid[brick.get(0).getRow()][brick.get(0).getColumn() + 2].getChildren().isEmpty()) {
                brick.get(0).setColumn(brick.get(1).getColumn());
                brick.get(0).setRow(brick.get(1).getRow());
                brick.get(0).setPos(true, false, true, true);
                brick.get(1).setColumn(brick.get(3).getColumn());
                brick.get(1).setPos(true, false, true, false);
                brick.get(2).setRow(brick.get(0).getRow());
                brick.get(2).setColumn(brick.get(3).getColumn()+1);
                brick.get(2).setPos(false, true, true, false);
                brick.get(3).setRow(brick.get(2).getRow()+1);
                brick.get(3).setColumn(brick.get(2).getColumn());
                brick.get(3).setPos(true, true, false, true);
                for (int i = 0; i < brick.size(); i++) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                }
                brick.setOrientation(LEFT);
            }
        }
    }
    public void rotateLLL() {
        if (grid[brick.get(0).getRow()-1][brick.get(0).getColumn()].getChildren().isEmpty()
                && grid[brick.get(1).getRow()-1][brick.get(1).getColumn()].getChildren().isEmpty()
                && grid[brick.get(2).getRow()-1][brick.get(2).getColumn()].getChildren().isEmpty()) {
            brick.get(0).setRow(brick.get(2).getRow()-1);
            brick.get(0).setColumn(brick.get(2).getColumn());
            brick.get(0).setPos(false, true, true, true);
            brick.get(1).setColumn(brick.get(2).getColumn());
            brick.get(1).setPos(false, true, false, true);
            brick.get(2).setRow(brick.get(3).getRow());
            brick.get(2).setPos(true, false, true, false);
            brick.get(3).setColumn(brick.get(3).getColumn()-1);
            brick.get(3).setPos(true, false, true, true);
                for (int i = 0; i < brick.size(); i++) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                }
            brick.setOrientation(UP);
        }
    }
    public void rotateURL() {
        //making canRotate true if the brick is not fully displayed or has no cells inside
        boolean canRotate =brick.get(0).getRow() > -1? grid[brick.get(3).getRow()-1][brick.get(3).getColumn()].getChildren().isEmpty() &&
                grid[brick.get(3).getRow()-2][brick.get(3).getColumn()].getChildren().isEmpty(): true;
        
        if (brick.get(3).getColumn() + 1 < grid[0].length) {
            //rotating while stretching to the right
            if (canRotate && brick.get(0).getRow() < 0 
                    && grid[brick.get(2).getRow()][brick.get(2).getColumn()+2].getChildren().isEmpty()) {
                brick.get(0).setRow(brick.get(3).getRow());
                brick.get(0).setColumn(brick.get(0).getColumn()+2);
                brick.get(0).setPos(true, true, true, false);
                brick.get(1).setRow(brick.get(3).getRow());
                brick.get(1).setColumn(brick.get(3).getColumn());
                brick.get(1).setPos(true, false, true, false);
                brick.get(2).setPos(false, false, true, true);
                brick.get(3).setRow(brick.get(2).getRow()+1);
                brick.get(3).setColumn(brick.get(2).getColumn());
                brick.get(3).setPos(true, true, false, true);
                for (int i = 0; i < brick.size(); i++) {
                    if (brick.get(i).getRow() > -1 && i != 2) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                }
                brick.setOrientation(RIGHT);
            }
            else if (canRotate && grid[brick.get(1).getRow()][brick.get(1).getColumn()+2].getChildren().isEmpty() &&
                    grid[brick.get(2).getRow()][brick.get(2).getColumn()+2].getChildren().isEmpty()) {
                brick.get(0).setRow(brick.get(3).getRow());
                brick.get(0).setColumn(brick.get(0).getColumn()+2);
                brick.get(0).setPos(true, true, true, false);
                brick.get(1).setRow(brick.get(3).getRow());
                brick.get(1).setColumn(brick.get(3).getColumn());
                brick.get(1).setPos(true, false, true, false);
                brick.get(2).setPos(false, false, true, true);
                brick.get(3).setRow(brick.get(2).getRow()+1);
                brick.get(3).setColumn(brick.get(2).getColumn());
                brick.get(3).setPos(true, true, false, true);
                for (int i = 0; i < brick.size(); i++) {
                    if (brick.get(i).getRow() > -1 && i != 2) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                }
                brick.setOrientation(RIGHT);
            }
        }
        
        
    }
    public void rotateRRL() {
        if (grid[brick.get(3).getRow()+1][brick.get(3).getColumn()+1].getChildren().isEmpty()) {
            brick.get(0).setRow(brick.get(3).getRow()+1);
            brick.get(0).setColumn(brick.get(3).getColumn()+1);
            brick.get(0).setPos(true, true, false, true);
            brick.get(1).setRow(brick.get(3).getRow());
            brick.get(1).setPos(false, true, false, true);
            brick.get(2).setColumn(brick.get(1).getColumn());
            brick.get(2).setPos(false, true, true, false);
            brick.get(3).setRow(brick.get(2).getRow());
            brick.get(3).setPos(true, false, true, true);
                for (int i = 0; i < brick.size(); i++) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                }
            brick.setOrientation(DOWN);
        }
    }
    public void rotateDRL() {
        if (brick.get(0).getColumn() + 1 < grid[0].length) {
            if (grid[brick.get(2).getRow()][brick.get(2).getColumn() + 1].getChildren().isEmpty()
                    && grid[brick.get(1).getRow()][brick.get(1).getColumn() + 1].getChildren().isEmpty()
                    && grid[brick.get(0).getRow()][brick.get(0).getColumn() + 1].getChildren().isEmpty() 
                    && grid[brick.get(0).getRow()][brick.get(0).getColumn() - 1].getChildren().isEmpty()) {
                brick.get(0).setColumn(brick.get(3).getColumn());
                brick.get(0).setPos(true, false, true, true);
                brick.get(1).setRow(brick.get(0).getRow());
                brick.get(1).setPos(true, false, true, false);
                brick.get(2).setRow(brick.get(0).getRow());
                brick.get(2).setColumn(brick.get(2).getColumn()+1);
                brick.get(2).setPos(true, true, false, false);
                brick.get(3).setRow(brick.get(3).getRow()+1);
                brick.get(3).setColumn(brick.get(2).getColumn());
                brick.get(3).setPos(false, true, true, true);
                for (int i = 0; i < brick.size(); i++) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                }
                brick.setOrientation(LEFT);
            }
        }
    }
    public void rotateLRL() {
        if (grid[brick.get(0).getRow()-1][brick.get(0).getColumn()].getChildren().isEmpty()
                && grid[brick.get(1).getRow()+1][brick.get(1).getColumn()].getChildren().isEmpty()) {
            brick.get(0).setRow(brick.get(3).getRow());
            brick.get(0).setColumn(brick.get(1).getColumn());
            brick.get(0).setPos(false, true, true, true);
            brick.get(1).setPos(false, true, false, true);
            brick.get(2).setColumn(brick.get(1).getColumn());
            brick.get(2).setRow(brick.get(1).getRow()+1);
            brick.get(2).setPos(true, false, false, true);
            brick.get(3).setRow(brick.get(2).getRow());
            brick.get(3).setPos(true, true, true, false);
                for (int i = 0; i < brick.size(); i++) {
                    if (i != 1)
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                }
            brick.setOrientation(UP);
        }
    }
    public void rotateURZ() {
        //checking if the brick can rotate
                        boolean canRotate = brick.get(3).getRow()-1 > -1? grid[brick.get(3).getRow()-1][brick.get(3).getColumn()].getChildren().isEmpty() : true;
                        if (canRotate) {
                            //Rotating the brick
                            brick.get(0).setColumn(brick.get(3).getColumn());
                            brick.get(0).setPos(false, true, true, true);
                            brick.get(1).setColumn(brick.get(3).getColumn());
                            brick.get(1).setRow(brick.get(3).getRow());
                            brick.get(1).setPos(true, true, false, false);
                            brick.get(3).setRow(brick.get(2).getRow()+1);
                            brick.get(3).setColumn(brick.get(2).getColumn());
                            brick.get(3).setPos(true, true, false, true);
                            brick.get(2).setPos(false, false, true, true);
                            for (int i = 0; i < brick.size(); i++) {
                                if (i != 2 && brick.get(i).getRow() > -1)
                                grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                            }
                            brick.setOrientation(RIGHT);
                        }
    }
    public void rotateRRZ() {
        if (brick.get(3).getColumn()+2 < grid[0].length) if (grid[brick.get(3).getRow()][brick.get(3).getColumn()+2].getChildren().isEmpty()) {
                            brick.get(0).setColumn(brick.get(2).getColumn());
                            brick.get(0).setRow(brick.get(2).getRow());
                            brick.get(0).setPos(true, false, true, true);
                            brick.get(2).setColumn(brick.get(1).getColumn());
                            brick.get(2).setRow(brick.get(3).getRow());
                            brick.get(2).setPos(true, false, false, true);
                            brick.get(3).setColumn(brick.get(2).getColumn()+1);
                            brick.get(3).setPos(true, true, true, false);
                            brick.get(1).setPos(false, true, true, false);
                            for (int i = 0; i < brick.size(); i++) {
                                if (i != 1 && brick.get(i).getRow() > -1)
                                grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                            }
                            brick.setOrientation(UP);
                        }
        else if (brick.get(2).getColumn()-1 > -1) if (grid[brick.get(2).getRow()][brick.get(2).getColumn()-1].getChildren().isEmpty()) {
                            brick.get(0).setColumn(brick.get(2).getColumn()-1);
                            brick.get(0).setRow(brick.get(2).getRow());
                            brick.get(0).setPos(true, false, true, true);
                            brick.get(1).setColumn(brick.get(2).getColumn());
                            brick.get(1).setRow(brick.get(2).getRow());
                            brick.get(1).setPos(false, true, true, false);
                            brick.get(2).setRow(brick.get(3).getRow());
                            brick.get(2).setPos(true, false, false, true);
                            brick.get(3).setColumn(brick.get(2).getColumn()+1);
                            brick.get(3).setPos(true, true, true, false);
                            for (int i = 0; i < brick.size(); i++) {
                                if (brick.get(i).getRow() > -1)
                                grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                            }
                            brick.setOrientation(UP);
                        }
                        else if (brick.get(2).getColumn()-1 > -1) if (grid[brick.get(2).getRow()][brick.get(2).getColumn()-1].getChildren().isEmpty()) {
                            brick.get(0).setColumn(brick.get(2).getColumn()-1);
                            brick.get(0).setRow(brick.get(2).getRow());
                            brick.get(0).setPos(true, false, true, true);
                            brick.get(1).setColumn(brick.get(2).getColumn());
                            brick.get(1).setRow(brick.get(2).getRow());
                            brick.get(1).setPos(false, true, true, false);
                            brick.get(2).setRow(brick.get(3).getRow());
                            brick.get(2).setPos(true, false, false, true);
                            brick.get(3).setColumn(brick.get(2).getColumn()+1);
                            brick.get(3).setPos(true, true, true, false);
                            for (int i = 0; i < brick.size(); i++) {
                                if (brick.get(i).getRow() > -1)
                                grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                            }
                            brick.setOrientation(UP);
                        }
    }
    public void rotateULZ() {
        //checking if the brick can rotate
                        boolean canRotate = brick.get(0).getRow()-1 > -1? grid[brick.get(0).getRow()-1][brick.get(0).getColumn()].getChildren().isEmpty():true;
                        if (canRotate) {
                            //Rotating the brick
                            brick.get(3).setColumn(brick.get(0).getColumn());
                            brick.get(3).setPos(false, true, true, true);
                            brick.get(2).setColumn(brick.get(0).getColumn());
                            brick.get(2).setRow(brick.get(0).getRow());
                            brick.get(2).setPos(true, false, false, true);
                            brick.get(0).setRow(brick.get(1).getRow()+1);
                            brick.get(0).setColumn(brick.get(1).getColumn());
                            brick.get(0).setPos(true, true, false, true);
                            brick.get(1).setPos(false, true, true, false);
                            for (int i = 0; i < brick.size(); i++) {
                                if (i != 1 && brick.get(i).getRow() > -1)
                                grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                            }
                            brick.setOrientation(RIGHT);
                        }
    }
    public void rotateRLZ() {
        if (brick.get(3).getColumn()-2 > -1) {
            //rotating while shifting to the left
                            if (grid[brick.get(0).getRow()][brick.get(0).getColumn()-2].getChildren().isEmpty()) {
                                brick.get(3).setColumn(brick.get(1).getColumn());
                                brick.get(3).setRow(brick.get(1).getRow());
                                brick.get(3).setPos(true, true, true, false);
                                brick.get(1).setColumn(brick.get(2).getColumn());
                                brick.get(1).setRow(brick.get(0).getRow());
                                brick.get(1).setPos(true, true, false, false);
                                brick.get(0).setColumn(brick.get(2).getColumn()-1);
                                brick.get(0).setPos(true, false, true, true);
                                brick.get(2).setPos(false, false, true, true);
                                for (int i = 0; i < brick.size(); i++) {
                                    if (i != 2 && brick.get(i).getRow() > -1)
                                    grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                                }
                                brick.setOrientation(UP);
                            }
                            else if (brick.get(1).getColumn()+1 < grid[0].length) 
                            if(grid[brick.get(1).getRow()][brick.get(1).getColumn()+1].getChildren().isEmpty()) {
                                brick.get(3).setColumn(brick.get(1).getColumn()+1);
                                brick.get(3).setRow(brick.get(1).getRow());
                                brick.get(3).setPos(true, true, true, false);
                                brick.get(2).setColumn(brick.get(1).getColumn());
                                brick.get(2).setRow(brick.get(1).getRow());
                                brick.get(2).setPos(false, false, true, true);
                                brick.get(1).setRow(brick.get(0).getRow());
                                brick.get(1).setPos(true, true, false, false);
                                brick.get(0).setColumn(brick.get(1).getColumn()-1);
                                brick.get(0).setPos(true, false, true, true);
                                for (int i = 0; i < brick.size(); i++) {
                                    if (brick.get(i).getRow() > -1)
                                    grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                                }
                                brick.setOrientation(UP);
                            }
        }
        //Rotating while shifting to the right
                        else if (brick.get(1).getColumn()+1 < grid[0].length) 
                            if(grid[brick.get(1).getRow()][brick.get(1).getColumn()+1].getChildren().isEmpty()) {
                                brick.get(3).setColumn(brick.get(1).getColumn()+1);
                                brick.get(3).setRow(brick.get(1).getRow());
                                brick.get(3).setPos(true, true, true, false);
                                brick.get(2).setColumn(brick.get(1).getColumn());
                                brick.get(2).setRow(brick.get(1).getRow());
                                brick.get(2).setPos(false, false, true, true);
                                brick.get(1).setRow(brick.get(0).getRow());
                                brick.get(1).setPos(true, true, false, false);
                                brick.get(0).setColumn(brick.get(1).getColumn()-1);
                                brick.get(0).setPos(true, false, true, true);
                                for (int i = 0; i < brick.size(); i++) {
                                    if (brick.get(i).getRow() > -1)
                                    grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                                }
                                brick.setOrientation(UP);
                            }
    }
    
    //Shifting the brick to the requested side
    public void shiftBrick(int side) {
        if (side == 0) {
            //Checking if it can shift to right
            boolean canShift = true;
            for (int i = 0; i < brick.size(); i++) {
                if (brick.get(i).getRow() > -1) {
                    if (brick.get(i).getColumn() >= grid[brick.get(i).getRow()].length-1) {
                        canShift = false;
                        break;
                    }
                    if (brick.get(i).right && (!grid[brick.get(i).getRow()][brick.get(i).getColumn()+1].getChildren().isEmpty()))
                        canShift = false;
            
                }
            }
            //shifting if it can shift
            if (canShift) {
                for (int i = 0; i < brick.size(); i++) {
                    brick.get(i).setColumn(brick.get(i).getColumn()+1);
                    if (brick.get(i).getRow() > -1) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                }
            }
        }
        else if (side == 1) {
            //Checking if it can shift to left
            boolean canShift = true;
            for (int i = 0; i < brick.size(); i++) {
                if (brick.get(i).getRow() > -1) {
                    if (brick.get(i).getColumn() <= 0) {
                        canShift = false;
                        break;
                    }
                if (brick.get(i).left && (!grid[brick.get(i).getRow()][brick.get(i).getColumn()-1].getChildren().isEmpty()))
                    canShift = false;
                }
            }
            //shifting if it can shift
            if (canShift) {
                for (int i = 0; i < brick.size(); i++) {
                    brick.get(i).setColumn(brick.get(i).getColumn()-1);
                    if (brick.get(i).getRow() > -1) {
                        grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                    }
                }
            }
        }
    }
    //checking the grid if there is a row to be cleared
    private void checkGrid() {
        boolean clear;
        for (int x = grid.length-1; x >= bricksHeight; x--) {
            clear = true;
            for (int y = 0; y < grid[x].length; y++) {
                //setting clear to false if there is an empty grid cell
                if (grid[x][y].getChildren().isEmpty()) {
                    clear = false;
                    break;
                }
            }
            if (clear) {
                clearRow(x);
                //increamenting x since the height has been decreased
                x++;
            }
        }
    }
    //adding a newly created brick
    public void addNewBrick() {
        for (int i = 0; i < brick.size(); i++) {
                //Checking if the cell has to be displayed
                if (brick.get(i).getRow() > -1) {
                    //displaying at new location
                    grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                }
            }
    }
    public void clearRow(int row) {
        //Clearing the row
        for (int i = 0; i < grid[row].length; i++) {
            grid[row][i].getChildren().clear();
        }
        //droping the cells above the cleared row
        for (int x = row-1; x >= bricksHeight; x--) {
            for (int y = 0; y < grid[x].length; y++) {
                if (!grid[x][y].getChildren().isEmpty()) {
                    grid[x+1][y].getChildren().add(grid[x][y].getChildren().remove(0));
                }
            }
        }
        score += 2;
        scoref.setText("  " + score);
        bricksHeight++;
    }
    public boolean canMove() {
        //determining if the brick is free to move
        boolean canMove = true;
        for (int i = 0; i < brick.size(); i++) {
            if (brick.get(i).getRow() > -1)
                if (brick.get(i).bottom && (brick.get(i).getRow() >= grid.length-1 || 
                        !grid[brick.get(i).getRow()+1][brick.get(i).getColumn()].getChildren().isEmpty())) {
                    canMove = false;
                }
        }
        return canMove;
    }
    private void moveBrick() {
        //Move the block if it can move
        if (canMove()) {
            for (int i = 0; i < brick.size(); i++) {
                //changing the cell coordinates
                brick.get(i).setRow(brick.get(i).getRow() + 1);
                //Checking if the cell has to be displayed
                if (brick.get(i).getRow() > -1) {
                    //displaying at new location
                    grid[brick.get(i).getRow()][brick.get(i).getColumn()].getChildren().add(brick.get(i));
                }
            }
        }
        //leaving the block if it cannot move
        else {
            changeHeight();
            if (bricksHeight < 1) {
            animation.stop();
            gameOver();
            return;
        }
            //leaving the old brick and start a new one
            brick = nextBrick;
            nextBrick = new TetrisBrick((int)Math.round(1 + Math.random() * 5), grid[0].length);
            addNewBrick();
            addNextBrick();
            checkGrid();
        }
    }
    //changing the bricksHeight to the new value
    public void changeHeight() {
        for (int i = 0; i < brick.size(); i++) {
            if (brick.get(i).getRow() < bricksHeight) {
                bricksHeight = brick.get(i).getRow();
            }
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }   
}