package piece;

import main.GamePanel;

public class Pawn extends Piece{

    public Pawn(int color, int col, int row){
        super(color, col, row);

        if (color == GamePanel.WHITE){
            image = getImage("/piece/w-pawn");
        }
        else{
            image = getImage("/piece/b-pawn");
        }
    }

    public boolean canMove(int targetCol, int targetRow){
        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false){
            // Define the move value based on its color
            int moveValue;
            if(color == GamePanel.WHITE){
                moveValue = -1;
            }
            else{
                moveValue = 1;
            }

            // Check hitting piece
            hittingPiece = getHittingPiece(targetCol, targetRow);

            // 1 square movement
            if(targetCol == prevCol && targetRow == prevRow + moveValue && hittingPiece == null){
                return true;
            }

            // 2 squares movement
            if(targetCol == prevCol && targetRow == prevRow + moveValue * 2 && hittingPiece == null && moved == false &&
                    pieceIsOnStraightLine(targetCol, targetRow) == false){
                return true;
            }

            // Diagonal movement & capture if piece has a square digonally
            if(Math.abs(targetCol - prevCol) == 1 && targetRow == prevRow + moveValue && hittingPiece != null &&
                    hittingPiece.color != color){
                return true;
            }

        }
        return false;
    }
    
}
