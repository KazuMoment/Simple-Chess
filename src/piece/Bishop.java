package piece;

import main.GamePanel;

public class Bishop extends Piece{

    public Bishop(int color, int col, int row){
        super(color, col, row);

        if (color == GamePanel.WHITE){
            image = getImage("/piece/w-bishop");
        }
        else{
            image = getImage("/piece/b-bishop");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){
            if (Math.abs(targetCol - prevCol) == Math.abs(targetRow - prevRow)){
                if(isValidSquare(targetCol, targetRow) && pieceIsOnDiagonalLine(targetCol, targetRow) == false){
                    return true;
                }
            }
        }
        return false;
    }
    
}
