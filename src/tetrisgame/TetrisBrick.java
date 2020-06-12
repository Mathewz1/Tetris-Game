/*
 This class provides a brick for the tetris game.
The brick is made up of cells. Each cell has row and column coordinates and the boolean property 'down'
A brick is an arraylist of cells.
To construct an instance of TetrisBrick you have to indicate the type of the brick which
are defined in the TetrisBrickTypes interface.
 */
package tetrisgame;

/**
 *
 * @author JAHNICS
 */

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;

public class TetrisBrick extends ArrayList<Cell> implements TetrisBrickConstants {
    private int type;
    private int orientation;
    TetrisBrick(int type, int gridWidth) {
        this.type = type;
        orientation = UP;
        int center = gridWidth/2 + 1;
        //Creating required type brickS
        switch (type) {
            case BOX: createB(center);
            break;
            case LEFT_L: createLL(center);
            break;
            case RIGHT_L: createRL(center);
            break;
            case RIGHT_Z: createRZ(center);
            break;
            case LEFT_Z: createLZ(center);
            break;
            case STANDING: createS(center);
            break;
        }
        
    }
    public int getOrientation() {
        return this.orientation;
    }
    public void setOrientation(int ori) {
        this.orientation = ori;
    }
    private void createRZ(int center) {
        this.add(new Cell(-1, center-1, true, false, true, true));
        this.add(new Cell(-1, center, false, true, true, false));
        this.add(new Cell(0, center, true, false, false, true));
        this.add(new Cell(0, center+1, true, true, true, false));
    }
    private void createLZ(int center) {
        this.add(new Cell(0, center-1, true, false, true, true));
        this.add(new Cell(0, center, true, true, false, false));
        this.add(new Cell(-1, center, false, false, true, true));
        this.add(new Cell(-1, center+1, true, true, true, false));
    }
    private void createRL(int center) {
        this.add(new Cell(-2, center, false, true, true, true));
        this.add(new Cell(-1, center, false, true, false, true));
        this.add(new Cell(0, center, true, false, false, true));
        this.add(new Cell(0, center+1, true, true, true, false));
    }
    private void createLL(int center) {
        this.add(new Cell(-2, center, false, true, true, true));
        this.add(new Cell(-1, center, false, true, false, true));
        this.add(new Cell(0, center, true, true, false, false));
        this.add(new Cell(0, center-1, true, false, true, true));
    }
    
    private void createS(int center) {
        this.add(new Cell(-3, center, false, true, true, true));
        this.add(new Cell(-2, center, false, true, false, true));
        this.add(new Cell(-1, center, false, true, false, true));
        this.add(new Cell(0, center, true, true, false, true));
    }
    
    private void createB(int center) {
        this.add(new Cell(0, center-1, true, false, false, true));
        this.add(new Cell(0, center, true, true, false, false));
        this.add(new Cell(-1, center-1, false, false, true, true));
        this.add(new Cell(-1, center, false, true, true, false));
       
    }
    public int getType() {
        return this.type;
    }
}
    class Cell extends Rectangle{
        //location of a cell
        private int row, column;
        public boolean bottom, right, top, left;
        
        Cell(int row, int column, boolean bottom, boolean right, boolean top, boolean left) {
            this.bottom = bottom;
            this.top = top;
            this.left = left;
            this.right = right;
            //initializing the location
            this.row = row;
            this.column = column;
            //Initialiing the cell
            this.setStroke(Color.ALICEBLUE);
            this.setFill(Color.CADETBLUE);
            this.setWidth(20);
            this.setHeight(20);
        }
        public void setPos(boolean bottom, boolean right, boolean top, boolean left) {
            this.bottom = bottom;
            this.top = top;
            this.left = left;
            this.right = right;
        }
        
        //Getter and setter methods
        public int getRow() {
            return this.row;
        }
        public int getColumn() {
            return this.column;
        }
        public void setRow(int row) {
            this.row = row;
        }
        public void setColumn(int column) {
            this.column = column;
        }
    }

