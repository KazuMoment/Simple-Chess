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
    Piece activePiece;

    // Color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    // Booleans
    boolean canMove;
    boolean validSquare;

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
        // Placeholder ----v
        // pieces.add(new Queen(WHITE, 4, 4));
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
                    changePlayer();
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

    private void simulate(){

        canMove = false;
        validSquare = false;

        // Reset piece list in every loop
        copyPieces(pieces, simPieces);

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

            validSquare = true;
        }
    }

    public void changePlayer(){
        if (currentColor == WHITE){
            currentColor = BLACK;
        }
        else{
            currentColor = WHITE;
        }
        activePiece = null;
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
                g2.setColor(Color.white);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                     Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));                
            }
            // Draw activePiece in the end so not hidden by colored square
            activePiece.draw(g2);
        }

        // Status Messages
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
        g2.setColor(Color.white);

        if(currentColor == WHITE){
            g2.drawString("White's Turn", 840, 550);
        }
        else{
            g2.drawString("Black's Turn", 840, 550);
        }
    }   
}
