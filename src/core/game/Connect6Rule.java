package core.game;

import static core.board.PieceColor.EMPTY;

import core.board.Board;
import core.board.PieceColor;

/**
 * @author 朱琳
 *
 */
public class Connect6Rule {
    Board _board;
    public static final int[][] FORWARD = { { 0, 1 }, { 1, 0 }, { 1, 1 }, { 1, -1 } };

    public Connect6Rule(Board _board){
        this._board = _board;
    }
    // 是否合法走步
    public boolean legalMove(Move mov) {
        return Move.validSquare(mov.index1()) && _board.get(mov.index1()) == EMPTY && Move.validSquare(mov.index2())
                && _board.get(mov.index2()) == EMPTY && mov.index1() != mov.index2(); // 同一走步的两个棋子落在不同点
    }

    public boolean gameOver() {
        if (_board.getMoveList().isEmpty())
            return false;
        Move lastMove = _board.getMoveList().get(_board.getMoveList().size() - 1);

        return isWin(lastMove.col0(), lastMove.row0()) || isWin(lastMove.col1(), lastMove.row1());
    }

    // 棋手在点(col，row)上落子后，该棋手是否获胜
    private boolean isWin(char col, char row) {

        for (int dir = 0; dir < 4; dir++) {
            // 从当前点沿dir方向，连续相同颜色的点的个数，包括当前点
            int len = lengthConnected(col, row, dir, 6);
            // 已经六连
            if (len == 6) {
                return true;
            } else {
                // 否则，从当前点，沿dir方向的反方向，退后（6-len）个点的位置
                char startCol = (char) (col - FORWARD[dir][0] * (6 - len));
                char startRow = (char) (row - FORWARD[dir][1] * (6 - len));
                // 如果不是合法的点，则继续查看下一个方向
                if (!Move.validSquare(startCol, startRow)) {
                    continue;
                }
                int tempLen = 6 - len;
                len = lengthConnected(startCol, startRow, dir, tempLen);
                // 如果连六
                if (len == tempLen) {
                    return true;
                }
            }
        }
        return false;
    }

    // 从点(startCol, startRow)开始，沿着方向dir向前的len个位置，连续与该点颜色相同的棋子的个数
    // 该个数 <= len.
    private int lengthConnected(char startCol, char startRow, int dir, int len) {

        int myLen = 0;
        // 点(startCol, startRow)的棋子的颜色
        PieceColor myColor = _board.whoseMove().opposite();
        for (int j = 0; j < len; j++) {
            char tempCol = (char) (startCol + FORWARD[dir][0] * j);
            char tempRow = (char) (startRow + FORWARD[dir][1] * j);
            if (Move.validSquare(tempCol, tempRow) && _board.get(tempCol, tempRow) == myColor) {
                myLen++;
            } else {
                break;
            }
        }
        return myLen;
    }
}