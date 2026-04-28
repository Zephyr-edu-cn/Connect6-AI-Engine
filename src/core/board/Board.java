package core.board;

import static core.game.Move.*;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;

import core.game.Game;
import core.game.Move;
import core.game.timer.GameTimer;
import core.game.ui.GameUI;


/** A Connect6 board.  The squares are labeled by column (a char value between
 *  'A' and 'S') and row (also a char value between 'A' and 'S').
 *  
 *  Moves on this board are denoted by Moves.
 *  @author Jianliang Xu
 */
public class Board implements GameUI{
    public static final int[][] FORWARD = new int[][]{{0, 1}, {1, 0}, {1, 1}, {1, -1}};
    private PieceColor _whoseMove;
    private final PieceColor[] _board = new PieceColor[361];
    private final ArrayList<Move> moveList = new ArrayList();
    private boolean _gameOver;
    private String title;
    public Board() {
        this.clear();
    }
    public Board(String title){
        this();
        this.title = title;
    }
    /** A copy of B. */
    public Board(Board b) {
        internalCopy(b);
    }
    public PieceColor[] get_board() {
		return _board;
	}
    /** Copy B into me. */
    private void internalCopy(Board b) {
        _gameOver = b.gameOver();
        _whoseMove = b.whoseMove();
        for (int i = 0; i < SIDE * SIDE; i++) {
            _board[i] = b.get(i);
        }
    }
    public void clear() {
        this._whoseMove = PieceColor.WHITE;

        for(int i = 0; i < 361; ++i) {
            this._board[i] = PieceColor.EMPTY;
        }

        this._board[Move.index('J', 'J')] = PieceColor.BLACK;
        this.moveList.clear();
    }

    public boolean gameOver() {
        if (this.moveList.isEmpty()) {
            return false;
        } else {
            Move lastMove = (Move)this.moveList.get(this.moveList.size() - 1);
            return this.isWin(lastMove.col0(), lastMove.row0()) || this.isWin(lastMove.col1(), lastMove.row1());
        }
    }

    private int lengthConnected(char startCol, char startRow, int dir, int len) {
        int myLen = 0;
        PieceColor myColor = this._whoseMove.opposite();

        for(int j = 0; j < len; ++j) {
            char tempCol = (char)(startCol + FORWARD[dir][0] * j);
            char tempRow = (char)(startRow + FORWARD[dir][1] * j);
            if (!Move.validSquare(tempCol, tempRow) || this.get(tempCol, tempRow) != myColor) {
                break;
            }

            ++myLen;
        }

        return myLen;
    }

    private boolean isWin(char col, char row) {
        for(int dir = 0; dir < 4; ++dir) {
            int len = this.lengthConnected(col, row, dir, 6);
            if (len == 6) {
                return true;
            }

            char startCol = (char)(col - FORWARD[dir][0] * (6 - len));
            char startRow = (char)(row - FORWARD[dir][1] * (6 - len));
            if (Move.validSquare(startCol, startRow)) {
                int tempLen = 6 - len;
                len = this.lengthConnected(startCol, startRow, dir, tempLen);
                if (len == tempLen) {
                    return true;
                }
            }
        }

        return false;
    }

    public PieceColor get(char c, char r) {
        assert Move.validSquare(c, r);

        return this._board[Move.index(c, r)];
    }

    public PieceColor get(int k) {
        assert Move.validSquare(k);

        return this._board[k];
    }

    protected void set(char c, char r, PieceColor v) {
        assert Move.validSquare(c, r);

        this.set(Move.index(c, r), v);
    }

    protected void set(int k, PieceColor v) {
        assert Move.validSquare(k);

        this._board[k] = v;
    }
    public ArrayList<Move> getMoveList() {
		return moveList;
	}
    public boolean legalMove(Move mov) {
        //System.out.println("[BOARD] Checking move: " + mov);
        //System.out.println("[BOARD] Index1 valid? " + Move.validSquare(mov.index1()));
        //System.out.println("[BOARD] Index2 valid? " + Move.validSquare(mov.index2()));
        //System.out.println("[BOARD] Position " + mov.index1() + " is empty? " + (this.get(mov.index1()) == PieceColor.EMPTY));
        //System.out.println("[BOARD] Position " + mov.index2() + " is empty? " + (this.get(mov.index2()) == PieceColor.EMPTY));
        //System.out.println("[BOARD] Indexes same? " + (mov.index1() == mov.index2()));

        boolean legal = Move.validSquare(mov.index1()) &&
                this.get(mov.index1()) == PieceColor.EMPTY &&
                Move.validSquare(mov.index2()) &&
                this.get(mov.index2()) == PieceColor.EMPTY &&
                mov.index1() != mov.index2();

        //System.out.println("[BOARD] Move is legal? " + legal);
        return legal;
    }

    public PieceColor whoseMove() {
        return this._whoseMove;
    }

    public void makeMove(char c0, char r0, char c1, char r1) {
        this.makeMove(new Move(c0, r0, c1, r1));
    }

    public void makeMove(Move mov) {
        assert this.legalMove(mov);

        this.moveList.add(mov);
        this.set(mov.col0(), mov.row0(), this._whoseMove);
        this.set(mov.col1(), mov.row1(), this._whoseMove);
        this._whoseMove = this._whoseMove.opposite();
    }

    public void undo() {
        Move mov = (Move)this.moveList.remove(this.moveList.size() - 1);
        this.undo(mov);
        this._whoseMove = this._whoseMove.opposite();
    }

    public void undo(Move mov) {
        this.set(mov.col0(), mov.row0(), PieceColor.EMPTY);
        this.set(mov.col1(), mov.row1(), PieceColor.EMPTY);
    }

    public String toString() {
        return this.toString(false);
    }

    public String toString(boolean legend) {
        StringBuilder strBuff = new StringBuilder();
        strBuff.append("   ");

        int i;
        for(i = 0; i < 8; ++i) {
            strBuff.append((char)(65 + i));
        }
        for(i = 9; i < 20; ++i) {
            strBuff.append((char)(65 + i));
        }
        strBuff.append("\n");

        for (i = 0; i < 19; i++){
            strBuff.append(String.format("%02d ", 19-i));
            for (int j = 0; j < 19; j++){
                int index = j * 19 + i;
                if (this._board[index] == PieceColor.EMPTY) {
                    strBuff.append("-");
                } else if (this._board[index] == PieceColor.BLACK) {
                    strBuff.append("x");
                } else {
                    strBuff.append("o");
                }
            }
            strBuff.append("\n");
        }

        return strBuff.toString();
    }

    public void draw() {
        //System.out.println("******(" +  title + ")******");
        //System.out.print(this.toString(true));
    }

    public void update(Observable arg0, Object arg1) {
        if (arg0 instanceof Game) {
            this.makeMove((Move) arg1);
            this.draw();
        }
        else if (arg0 instanceof GameTimer){
            //重新显示Timer的数据
            if (((String)arg1).equals("R")){

            }
        }
    }

    @Override
    public void setTimer(GameTimer bTimer, GameTimer wTimer) {
        this.bTimer = bTimer;
        this.wTimer = wTimer;
        bTimer.addObserver(this);
        wTimer.addObserver(this);
    }
    private GameTimer bTimer, wTimer;
}
