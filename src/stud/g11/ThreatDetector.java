package stud.g11;

import core.board.Board;
import core.board.PieceColor;
import core.game.Move;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThreatDetector {

    // 找到立即获胜的走法
    public static Move findWinningMove(Board board, PieceColor color) {
        List<Integer> empties = BoardHelper.getAllEmptyPositions(board);
        if (empties.size() < 2) return null;

        // 优化搜索：优先检查己方棋子周围的空位
        Set<Integer> hotSpots = new HashSet<>();
        List<Integer> myPieces = BoardHelper.getPiecePositions(board, color);

        for (int piece : myPieces) {
            hotSpots.addAll(BoardHelper.getEmptyNeighbors(board, piece, 3));
        }

        // 如果热点太少，加入棋盘中心区域
        if (hotSpots.size() < 20) {
            int center = 9 * 19 + 9;
            hotSpots.addAll(BoardHelper.getEmptyNeighbors(board, center, 5));
        }

        // 转换为列表
        List<Integer> spots = new ArrayList<>(hotSpots);
        if (spots.size() > 150) {
            spots = spots.subList(0, 150); // 限制搜索范围
        }

        // 检查所有热点组合
        for (int i = 0; i < spots.size(); i++) {
            for (int j = i + 1; j < spots.size(); j++) {
                int idx1 = spots.get(i);
                int idx2 = spots.get(j);

                if (BoardHelper.formsSixWithTwo(board, idx1, idx2, color)) {
                    return new Move(idx1, idx2);
                }
            }
        }

        return null;
    }

    // 寻找防守走法
    public static Move findDefensiveMove(Board board, PieceColor myColor, PieceColor opponentColor) {
        // 先找对方是否有立即获胜的走法
        Move opponentWin = findWinningMove(board, opponentColor);
        if (opponentWin == null) {
            return null;
        }

        // 获取所有空位
        List<Integer> empties = BoardHelper.getAllEmptyPositions(board);

        // 尝试不同的防守策略
        Move bestDefense = null;
        int bestScore = -1;

        int checks = 0;
        int maxChecks = Math.min(500, empties.size() * 5);

        for (int i = 0; i < empties.size() && checks < maxChecks; i++) {
            for (int j = i + 1; j < empties.size() && checks < maxChecks; j++) {
                int idx1 = empties.get(i);
                int idx2 = empties.get(j);

                // 计算这个防守走法的得分
                int score = evaluateDefense(board, idx1, idx2, opponentWin, opponentColor);

                if (score > bestScore) {
                    bestScore = score;
                    bestDefense = new Move(idx1, idx2);
                }
                checks++;
            }
        }

        return bestDefense;
    }

    // 评估防守走法的效果
    private static int evaluateDefense(Board board, int defIdx1, int defIdx2,
                                       Move opponentThreat, PieceColor opponentColor) {
        int score = 0;

        // 如果防守占据了一个威胁点，得基础分
        if (defIdx1 == opponentThreat.index1() || defIdx1 == opponentThreat.index2() ||
                defIdx2 == opponentThreat.index1() || defIdx2 == opponentThreat.index2()) {
            score += 10;
        }

        // 检查这个防守是否能阻止对方获胜
        if (!BoardHelper.formsSixWithTwo(board, opponentThreat.index1(),
                opponentThreat.index2(), opponentColor)) {
            score += 20; // 成功阻止
        }

        // 防守位置的价值
        int row1 = defIdx1 / 19, col1 = defIdx1 % 19;
        int row2 = defIdx2 / 19, col2 = defIdx2 % 19;
        score += (int)(BoardHelper.getPositionValue(row1, col1) +
                BoardHelper.getPositionValue(row2, col2));

        return score;
    }

    // 检查对手是否有单步威胁（差一子获胜）
    public static boolean hasOneMoveThreat(Board board, PieceColor opponentColor) {
        return BoardHelper.isOneMoveAway(board, opponentColor);
    }
}