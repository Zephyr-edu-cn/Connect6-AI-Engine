package stud.g11;

import core.board.Board;
import core.board.PieceColor;
import core.game.Move;

import java.util.ArrayList;
import java.util.List;

public class BoardHelper {
    // 方向数组：水平、垂直、正斜线、反斜线
    public static final int[][] DIRECTIONS = {
            {0, 1},   // 水平 →
            {1, 0},   // 垂直 ↓
            {1, 1},   // 正斜线 ↘
            {1, -1}   // 反斜线 ↙
    };

    // 创建棋盘副本
    public static PieceColor[][] createBoardCopy(Board board) {
        PieceColor[][] copy = new PieceColor[19][19];
        for (int r = 0; r < 19; r++) {
            for (int c = 0; c < 19; c++) {
                int idx = r * 19 + c;
                char colChar = Move.col(idx);
                char rowChar = Move.row(idx);
                copy[r][c] = board.get(colChar, rowChar);
            }
        }
        return copy;
    }

    // 检查两个子落下后是否形成六连
    public static boolean formsSixWithTwo(Board board, int idx1, int idx2, PieceColor color) {
        PieceColor[][] tempBoard = createBoardCopy(board);

        int row1 = idx1 / 19, col1 = idx1 % 19;
        int row2 = idx2 / 19, col2 = idx2 % 19;

        // 放置两个棋子
        tempBoard[row1][col1] = color;
        tempBoard[row2][col2] = color;

        return checkBoardForSix(tempBoard, color);
    }

    // 检查棋盘上是否存在六连
    private static boolean checkBoardForSix(PieceColor[][] board, PieceColor color) {
        for (int r = 0; r < 19; r++) {
            for (int c = 0; c < 19; c++) {
                if (board[r][c] == color) {
                    for (int[] dir : DIRECTIONS) {
                        int count = countInDirection(board, r, c, dir[0], dir[1], color);
                        if (count >= 6) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // 计算某个方向上的连续棋子数
    private static int countInDirection(PieceColor[][] board, int startRow, int startCol,
                                        int dx, int dy, PieceColor color) {
        int count = 1;
        int row = startRow + dx;
        int col = startCol + dy;

        while (row >= 0 && row < 19 && col >= 0 && col < 19 && board[row][col] == color) {
            count++;
            row += dx;
            col += dy;
        }

        row = startRow - dx;
        col = startCol - dy;
        while (row >= 0 && row < 19 && col >= 0 && col < 19 && board[row][col] == color) {
            count++;
            row -= dx;
            col -= dy;
        }

        return count;
    }

    // 获取所有空位置
    public static List<Integer> getAllEmptyPositions(Board board) {
        List<Integer> empties = new ArrayList<>();
        for (int i = 0; i < 361; i++) {
            char col = Move.col(i);
            char row = Move.row(i);
            if (board.get(col, row) == PieceColor.EMPTY) {
                empties.add(i);
            }
        }
        return empties;
    }

    // 获取指定位置周围的空邻居
    public static List<Integer> getEmptyNeighbors(Board board, int centerIndex, int radius) {
        List<Integer> neighbors = new ArrayList<>();
        int centerRow = centerIndex / 19;
        int centerCol = centerIndex % 19;

        for (int dr = -radius; dr <= radius; dr++) {
            for (int dc = -radius; dc <= radius; dc++) {
                if (dr == 0 && dc == 0) continue;

                int newRow = centerRow + dr;
                int newCol = centerCol + dc;

                if (newRow >= 0 && newRow < 19 && newCol >= 0 && newCol < 19) {
                    int idx = newRow * 19 + newCol;
                    if (board.get(Move.col(idx), Move.row(idx)) == PieceColor.EMPTY) {
                        neighbors.add(idx);
                    }
                }
            }
        }
        return neighbors;
    }

    // 获取指定颜色的所有棋子位置
    public static List<Integer> getPiecePositions(Board board, PieceColor color) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < 361; i++) {
            char col = Move.col(i);
            char row = Move.row(i);
            if (board.get(col, row) == color) {
                positions.add(i);
            }
        }
        return positions;
    }

    // 计算位置价值（中心价值高）
    public static double getPositionValue(int row, int col) {
        double distanceFromCenter = Math.sqrt(Math.pow(row - 9, 2) + Math.pow(col - 9, 2));
        return 10.0 - distanceFromCenter / 3.0;
    }

    // 检查单步威胁：下一个子就能形成5连（差一子获胜）
    public static boolean isOneMoveAway(Board board, PieceColor color) {
        List<Integer> empties = getAllEmptyPositions(board);

        // 检查所有空位
        for (int idx : empties) {
            // 模拟放置一个棋子
            PieceColor[][] tempBoard = createBoardCopy(board);
            int row = idx / 19;
            int col = idx % 19;
            tempBoard[row][col] = color;

            // 检查是否形成5连（差一子获胜）
            for (int r = 0; r < 19; r++) {
                for (int c = 0; c < 19; c++) {
                    if (tempBoard[r][c] == color) {
                        for (int[] dir : DIRECTIONS) {
                            int count = countInDirection(tempBoard, r, c, dir[0], dir[1], color);
                            if (count >= 5) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}