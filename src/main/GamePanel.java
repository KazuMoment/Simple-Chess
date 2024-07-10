package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable{

    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    // Pieces
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promotionPieces = new ArrayList<>();
    Piece activePiece, checkingPiece;
    public static Piece castlingPiece;

    // Color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    // Booleans
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean checkmate;
    boolean stalemate;

    public GamePanel(){
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);
        setPieces();
        copyPieces(pieces, simPieces);
    }

    public void launchGame(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setPieces(){

        // White team
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new King(WHITE, 4, 7));
        pieces.add(new Queen(WHITE, 3, 7));

        // Black team
        pieces.add(new Pawn(BLACK, 0, 1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new King(BLACK, 4, 0));
        pieces.add(new Queen(BLACK, 3, 0));
    }

    public void testPromotion(){
        pieces.add(new Pawn(WHITE, 0, 3));
        pieces.add(new Pawn(BLACK, 5, 4));
    }

    public void testIllegal(){
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new King(WHITE, 3, 7));
        pieces.add(new King(BLACK, 0, 3));
        pieces.add(new Bishop(BLACK, 1, 4));
        pieces.add(new Queen(BLACK, 4, 5));
    }

    public void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target){
        target.clear();
        for (int i = 0; i < source.size(); i++){
            target.add(source.get(i));
        }
    }

    @Override
    public void run() {

        // Game Loop
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(gameThread != null){
            currentTime = System.nanoTime();
            
            delta += (currentTime - lastTime)/drawInterval;
            lastTime = currentTime;

            if (delta >= 1){
                update();
                repaint();
                delta--;
            }
        }

    }

    private void update(){

        if(promotion){
            promoting();
        }
        else if(checkmate == false && stalemate == false){
            // Mouse pressed
            if (mouse.pressed){
                if(activePiece == null){
                    // If there is no piece held, check if able to pick up a piece
                    for (Piece piece : simPieces){
                        // If mouse is on same team's piece, set it as activePiece
                        if (piece.color == currentColor &&
                            piece.col == mouse.x/Board.SQUARE_SIZE &&
                            piece.row == mouse.y/Board.SQUARE_SIZE)
                        activePiece = piece;
                    }
                }
                else{
                    // If already holding a piece, simulate move
                    simulate();
                }
            }

            // Mouse released
            if (mouse.pressed == false){
                if (activePiece != null){
                    if(validSquare){

                        // Move is confirmed

                        // Update piece list in case a piece has been captured
                        copyPieces(simPieces, pieces);
                        activePiece.updatePosition();
                        if (castlingPiece != null){
                            castlingPiece.updatePosition();
                        }

                        if (isKingInCheck() && isCheckmate()){
                            checkmate = true;
                        }
                        else if(isStalemate() && isKingInCheck() == false){
                            stalemate = true;
                        }
                        else{
                            if (canPromote()){
                                promotion = true;
                            }
                            else{
                                changePlayer();
                            }
                        }
                    }
                    else{
                        // The move is not valid so reset everything
                        copyPieces(pieces, simPieces);
                        activePiece.resetPosition();
                        activePiece = null;
                    }
                }
            }
        }


        
    }

    private void simulate(){

        canMove = false;
        validSquare = false;

        // Reset piece list in every loop
        copyPieces(pieces, simPieces);

        // Reset the castling piece's position
        if (castlingPiece != null){
            castlingPiece.col = castlingPiece.prevCol;
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
            castlingPiece = null;
        }

        // If piece is held, update piece position
        activePiece.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activePiece.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activePiece.col = activePiece.getCol(activePiece.x);
        activePiece.row = activePiece.getRow(activePiece.y);

        // Check if piece is hovering over a reachable square
        if (activePiece.canMove(activePiece.col, activePiece.row)){
            canMove = true;

            // If hitting a piece, remove piece
            if(activePiece.hittingPiece != null){
                simPieces.remove(activePiece.hittingPiece.getIndex());
            }

            checkCastling();
            
            if (isIllegal(activePiece) == false && opponentCanCaptureKing() == false){
                validSquare = true;
            }
        }
    }

    private boolean isIllegal(Piece king){
        if (king.type == Type.KING){
            for (Piece piece : simPieces){
                if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing(){

        Piece king = getKing(false);

        for (Piece piece : simPieces){
            if (piece.color != king.color && piece.canMove(king.col, king.row)){
                return true;
            }
        }

        return false;
    }

    private boolean isKingInCheck(){

        Piece king = getKing(true);

        if (activePiece.canMove(king.col, king.row)){
            checkingPiece = activePiece;
            return true;
        }
        else{
            checkingPiece = null;
        }

        return false;
    }

    private Piece getKing(boolean opponent){

        Piece king = null;
        
        for (Piece piece : simPieces){
            if(opponent){
                if (piece.type == Type.KING && piece.color != currentColor){
                    king = piece;
                }
            }
            else{
                if (piece.type == Type.KING && piece.color == currentColor){
                    king = piece;
                } 
            }
        }
        return king;
    }

    private boolean isCheckmate(){

        Piece king = getKing(true);

        if (kingCanMove(king)){
            return false;
        }
        else{
            // But you still have a chance!!! まだまだだ！
            // Check if you can block the attack with a piece

            // Check position of the checking piece and the king in check
            int colDiff = Math.abs(checkingPiece.col - king.col);
            int rowDiff = Math.abs(checkingPiece.row - king.row);

            if(colDiff == 0){
                // The checking piece is attacking vertically
                if(checkingPiece.row < king.row){
                    // The checking piece is above the king
                    for (int row = checkingPiece.row; row < king.row; row++){
                        for (Piece piece : simPieces){
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingPiece.col, row)){
                                return false;
                            }
                        }
                    }
                }
                if (checkingPiece.row > king.row){
                    // The checking piece is below the king
                    for (int row = checkingPiece.row; row > king.row; row--){
                        for (Piece piece : simPieces){
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingPiece.col, row)){
                                return false;
                            }
                        }
                    }
                }
            }
            else if(rowDiff == 0){
                // The checking piece is attacking horizontally
                if (checkingPiece.col < king.col){
                    // The checking piece is attacking from the left
                    for (int col = checkingPiece.col; col < king.col; col++){
                        for (Piece piece : simPieces){
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingPiece.row)){
                                return false;
                            }
                        }
                    }
                }
                if (checkingPiece.col > king.col){
                    // The checking piece is attacking from the right
                    for (int col = checkingPiece.col; col > king.col; col--){
                        for (Piece piece : simPieces){
                            if (piece != king && piece.color != currentColor && piece.canMove(col, checkingPiece.row)){
                                return false;
                            }
                        }
                    }
                }
            }
            else if(colDiff == rowDiff){
                // The checking piece is attacking diagonally
                if (checkingPiece.row < king.row){
                    // The checking piece is above the king
                    if(checkingPiece.col < king.col){
                        // The checking piece is in the upper left
                        for(int col = checkingPiece.col, row = checkingPiece.row; col < king.col; col++, row++){
                            for (Piece piece : simPieces){
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingPiece.col > king.col){
                        // The checking piece is in the upper right
                        for(int col = checkingPiece.col, row = checkingPiece.row; col >  king.col; col--, row++){
                            for (Piece piece : simPieces){
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if (checkingPiece.row > king.row){
                    // The checking piece is above the king
                    if(checkingPiece.col < king.col){
                        // The checking piece is in the lower left
                        for(int col = checkingPiece.col, row = checkingPiece.row; col < king.col; col++, row--){
                            for (Piece piece : simPieces){
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingPiece.col > king.col){
                        // The checking piece is in the lower right
                        for(int col = checkingPiece.col, row = checkingPiece.row; col > king.col; col--, row--){
                            for (Piece piece : simPieces){
                                if (piece != king && piece.color != currentColor && piece.canMove(col, row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            else{
                // The checking piece is a knight, cannot block it
            }
        }

        return true;
    }

    private boolean kingCanMove(Piece king){

        // Simulate if there is any square that the king can move to
        if(isValidMove(king, -1, -1)){return true;}
        if(isValidMove(king, 0, -1)){return true;}
        if(isValidMove(king, 1, -1)){return true;}
        if(isValidMove(king, -1, 0)){return true;}
        if(isValidMove(king, 1, 0)){return true;}
        if(isValidMove(king, -1, 1)){return true;}
        if(isValidMove(king, 0, 1)){return true;}
        if(isValidMove(king, 1, 1)){return true;}

        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus){

        boolean isValidMove = false;

        // Update the king's position for a second
        king.col += colPlus;
        king.row += rowPlus;

        if(king.canMove(king.col, king.row)){
            if (king.hittingPiece != null){
                simPieces.remove(king.hittingPiece.getIndex());
            }
            if (isIllegal(king) == false){
                isValidMove = true;
            }
        }

        // Reset king's position and restore the removed pieces
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;
    }

    private boolean isStalemate(){
        int count = 0;

        // Count the number of pieces
        for (Piece piece : simPieces){
            if (piece.color != currentColor){
                count++;
            }
        }

        // If only one piece (the king) is left
        if (count == 1){
            if (kingCanMove(getKing(true)) == false){
                return true;
            }
        }

        return false;
    }

    private void checkCastling(){

        if (castlingPiece != null){
            if (castlingPiece.col == 0){
                castlingPiece.col += 3;
            }
            else if (castlingPiece.col == 7){
                castlingPiece.col -= 2;
            }
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
        }

    }

    public void changePlayer(){
        if (currentColor == WHITE){
            currentColor = BLACK;
            // Reset black's two stepped status
            for (Piece piece : pieces){
                if (piece.color == BLACK){
                    piece.twoStepped = false;
                }
            }
        }
        else{
            currentColor = WHITE;
            // Reset white's two stepped status
            for (Piece piece : pieces){
                if (piece.color == WHITE){
                    piece.twoStepped = false;
                }
            }
        }
        activePiece = null;
    }

    private boolean canPromote(){

        if (activePiece.type == Type.PAWN){
            if (currentColor == WHITE && activePiece.row == 0 || currentColor == BLACK && activePiece.row == 7){
                promotionPieces.clear();
                promotionPieces.add(new Rook(currentColor, 9, 2));
                promotionPieces.add(new Knight(currentColor, 9, 3));
                promotionPieces.add(new Bishop(currentColor, 9, 4));
                promotionPieces.add(new Queen(currentColor, 9, 5));
                return true;
            }
        }
        return false;
    }

    private void promoting(){
        if (mouse.pressed){
            for (Piece piece : promotionPieces){
                if (piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE){
                    switch(piece.type){
                        case ROOK: simPieces.add(new Rook(currentColor, activePiece.col, activePiece.row)); break;
                        case KNIGHT: simPieces.add(new Knight(currentColor, activePiece.col, activePiece.row)); break;
                        case BISHOP: simPieces.add(new Bishop(currentColor, activePiece.col, activePiece.row)); break;
                        case QUEEN: simPieces.add(new Queen(currentColor, activePiece.col, activePiece.row)); break;
                        default: break;
                    }
                    simPieces.remove(activePiece.getIndex()); 
                    copyPieces(simPieces, pieces);
                    activePiece = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g;

        // Board
        board.draw(g2);

        // Pieces
        for(Piece p : simPieces){
            p.draw(g2);
        }

        if (activePiece != null){
            if (canMove){

                if (isIllegal(activePiece) || opponentCanCaptureKing()){
                    g2.setColor(Color.red);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));  
                }
                else{
                    g2.setColor(Color.white);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));  
                }

                              
            }
            // Draw activePiece in the end so not hidden by colored square
            activePiece.draw(g2);
        }

        // Status Messages
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
        g2.setColor(Color.white);

        if (promotion){
            g2.drawString("Promote to:", 840, 150);
            for (Piece piece : promotionPieces){
                g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row),
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        }

        else{
            if(currentColor == WHITE){
                g2.drawString("White's Turn", 840, 550);
                if (checkingPiece != null && checkingPiece.color == BLACK){
                    g2.setColor(Color.white);
                    g2.drawString("The King", 840, 650);
                    g2.drawString("is in check!", 840, 700);
                }
            }
            else{
                g2.drawString("Black's Turn", 840, 550);
                if (checkingPiece != null && checkingPiece.color == WHITE){
                    g2.setColor(Color.white);
                    g2.drawString("The King", 840, 100);
                    g2.drawString("is in check!", 840, 150);
                }
            }
        }
        if(checkmate){
            String s = "";
            if (currentColor == WHITE){
                s = "White Wins";
            }
            else{
                s = "Black Wins";
            }
            g2.setFont(new Font("Arial", Font.PLAIN, 90));
            g2.setColor(Color.green);
            g2.drawString(s, 200, 420);;
        }   
        if (stalemate){
            g2.setFont(new Font("Arial", Font.PLAIN, 90));
            g2.setColor(Color.yellow);
            g2.drawString("Stalemate", 200, 420);;
        }
    }   
}
