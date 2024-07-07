package piece;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Board;
import main.GamePanel;

public class Piece {

    BufferedImage image;
    public int x, y;
    public int col, row, prevCol, prevRow;
    public int color;
    public Piece hittingPiece;
    public boolean moved;



    public Piece(int color, int col, int row){
        this.color = color;
        this.col = col;
        this.row = row;
        x = getX(col);
        y = getY(row);
        prevCol = col;
        prevRow = row;
    }

    public BufferedImage getImage(String imagePath){
        BufferedImage image = null;

        try{ 
            image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
        }catch(IOException e){
            e.printStackTrace();
        }
        return image;
    }

    public int getX(int col){
        return col * Board.SQUARE_SIZE;
    }
    public int getY(int row){
        return row * Board.SQUARE_SIZE;
    }

    public int getCol(int x){
        return (x + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }
    
    public int getIndex(){
        for(int index = 0; index < GamePanel.simPieces.size(); index++){
            if (GamePanel.simPieces.get(index) == this){
                return index;
            }
        }
        return 0; 
    }

    public int getRow(int y){
        return (y + Board.HALF_SQUARE_SIZE)/Board.SQUARE_SIZE;
    }

    public void updatePosition(){
        x = getX(col);
        y = getY(row);
        prevCol = getCol(x);
        prevRow = getRow(y);
        moved = true;
    }

    public void resetPosition(){
        col = prevCol;
        row = prevRow;
        x = getX(col);
        y = getY(row);
    }

    public boolean canMove(int targetCol, int targetRow){
        return false;
    }

    public boolean isWithinBoard(int targetCol, int targetRow){
        if (targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7){
            return true;
        }
        return false;    
    }

    public boolean isSameSquare(int targetCol, int targetRow){
        if (targetCol == prevCol && targetRow == prevRow){
            return true;
        }
        return false;
    }

    public Piece getHittingPiece(int targetCol, int targetRow){
        for (Piece piece : GamePanel.simPieces){
            if (piece.col == targetCol && piece.row == targetRow && piece != this){
                return piece;
            }
        }
        return null;
    }

    public boolean isValidSquare(int targetCol,  int targetRow){
        hittingPiece = getHittingPiece(targetCol, targetRow);

        if (hittingPiece == null){
            return true;
        }
        else{
            if(hittingPiece.color != this.color){
                return true;
            }
            else{
                hittingPiece = null;
            }
        }
        return false;
    }

    public boolean pieceIsOnStraightLine(int targetCol, int targetRow){

        // When moving to the left
        for (int c = prevCol - 1; c > targetCol; c--){
            for(Piece piece : GamePanel.simPieces){
                if(piece.col == c && piece.row == targetRow){
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        // When moving to the right
        for (int c = prevCol + 1; c < targetCol; c++){
            for(Piece piece : GamePanel.simPieces){
                if(piece.col == c && piece.row == targetRow){
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        // When moving up
        for (int r = prevRow - 1; r > targetRow; r--){
            for(Piece piece : GamePanel.simPieces){
                if(piece.col == targetCol && piece.row == r){
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        // When moving down
        for (int r = prevRow + 1; r < targetRow; r++){
            for(Piece piece : GamePanel.simPieces){
                if(piece.col == targetCol && piece.row == r){
                    hittingPiece = piece;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean pieceIsOnDiagonalLine(int targetCol, int targetRow){
        if (targetRow < prevRow){

            // Up left
            for (int c = prevCol - 1; c > targetCol; c--){
                int diff = Math.abs(c - prevCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == prevRow - diff){
                        hittingPiece = piece;
                        return true;
                    }
                }
            }

            // Up right
            for (int c = prevCol + 1; c < targetCol; c++){
                int diff = Math.abs(c - prevCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == prevRow - diff){
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
        }

        if (targetRow > prevRow){

            // Down left
            for (int c = prevCol - 1; c > targetCol; c--){
                int diff = Math.abs(c - prevCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == prevRow + diff){
                        hittingPiece = piece;
                        return true;
                    }
                }
            }

            for (int c = prevCol + 1; c < targetCol; c++){
                int diff = Math.abs(c - prevCol);
                for(Piece piece : GamePanel.simPieces){
                    if(piece.col == c && piece.row == prevRow + diff){
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void draw(Graphics2D g2){
        g2.drawImage(image, x, y, Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
    }

    
}
