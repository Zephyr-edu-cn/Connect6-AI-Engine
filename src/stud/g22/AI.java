package stud.g22;

import core.board.PieceColor;
import core.game.Game;
import core.game.Move;

import java.util.ArrayList;
import java.util.List;

public class AI extends core.player.AI {

    private RoadBoard roadBoard;
    private int myColorInt; // 1:Black, 2:White
    private int oppColorInt;
    private int moveCount = 0;

    // 搜索参数
    private static final int MAX_DEPTH = 2;
    private static final int SEARCH_WIDTH = 12; // 候选点宽度

    @Override
    public String name() {
        return "G22-V2-RoadPaper";
    }

    @Override
    public void playGame(Game game) {
        System.out.println("[DEBUG] AI " + name() + " playGame called, color: " + this.getColor());

        // 先调用父类方法
        super.playGame(game);

        // 初始化父类的board
        if (this.board == null) {
            this.board = new core.board.Board("G22-V2-RoadPaper");
            this.board.clear(); // 这会设置中心黑子
            System.out.println("[DEBUG] Created and initialized parent board");
        }

        // 初始化自己的RoadBoard
        roadBoard = new RoadBoard();
        moveCount = 0;
        myColorInt = (this.getColor() == PieceColor.BLACK) ? 1 : 2;
        oppColorInt = (myColorInt == 1) ? 2 : 1;

        // 添加中心黑子（六子棋开局规则）
        int centerIndex = 9 * 19 + 9; // (J,J) 的索引
        roadBoard.addStone(centerIndex, 1); // 1=Black

        // 注意：如果自己是白方，这个黑子是对手的
        // 如果自己是黑方，这个黑子是自己（初始）的

        System.out.println("[DEBUG] Added center black stone at index " + centerIndex + " to RoadBoard");
        System.out.println("[DEBUG] playGame completed");
        System.out.println("[DEBUG] Parent board is null? " + (this.board == null));
        System.out.println("[DEBUG] RoadBoard is null? " + (roadBoard == null));
    }

    @Override
    public Move findNextMove(Move opponentMove) {
        System.out.println("[G11 AI] ========== findNextMove called ==========");
        System.out.println("[G11 AI] moveCount: " + moveCount);
        System.out.println("[G11 AI] opponentMove: " + opponentMove);

        // 1. 先同步状态（确保RoadBoard与实际棋盘一致）
        syncBoardToRoadBoard();

        // 2. 判断是否是白方第一手
        if (moveCount == 0 && opponentMove == null) {
            System.out.println("[G11 AI] White first move case");
            Move opening = super.firstMove();
            updateMyMove(opening);
            return opening;
        }

        // 3. 紧急威胁检查
        Move emergencyMove = checkEmergency();
        if (emergencyMove != null) {
            System.out.println("[G11 AI] Emergency move found: " + emergencyMove);
            updateMyMove(emergencyMove);
            return emergencyMove;
        }

        // 4. Alpha-Beta搜索
        System.out.println("[G11 AI] Starting alphaBetaSearch...");
        Move bestMove = alphaBetaSearch();

        // 5. 兜底逻辑
        if (bestMove == null) {
            System.out.println("[G11 AI] No move from alphaBetaSearch, using fallback");
            bestMove = generateFallbackMove();
        }

        System.out.println("[G11 AI] Selected move: " + bestMove);
        updateMyMove(bestMove);
        return bestMove;
    }

    // 生成兜底走法
    private Move generateFallbackMove() {
        List<Integer> empties = roadBoard.getEmptyNeighbors(2);

        // 过滤出真正为空的位置
        List<Integer> trueEmpties = new ArrayList<>();
        for (int idx : empties) {
            if (roadBoard.isEmpty(idx)) {
                trueEmpties.add(idx);
            }
        }

        if (trueEmpties.size() >= 2) {
            // 选择两个价值较高的空位
            trueEmpties.sort((a, b) ->
                    roadBoard.evaluatePointSimple(b, myColorInt) -
                            roadBoard.evaluatePointSimple(a, myColorInt));
            System.out.println("[G11 AI] Fallback move from top positions: " + trueEmpties.get(0) + ", " + trueEmpties.get(1));
            return new Move(trueEmpties.get(0), trueEmpties.get(1));
        } else {
            // 极端情况
            List<Integer> allEmpties = new ArrayList<>();
            for (int i = 0; i < 361; i++) {
                if (roadBoard.isEmpty(i)) allEmpties.add(i);
            }
            if (allEmpties.size() >= 2) {
                System.out.println("[G11 AI] Fallback move from all empties: " + allEmpties.get(0) + ", " + allEmpties.get(1));
                return new Move(allEmpties.get(0), allEmpties.get(1));
            }
            System.out.println("[G11 AI] Emergency fallback move: 0, 1");
            return new Move(0, 1);
        }
    }

    private void updateMyMove(Move move) {
        System.out.println("[G11 AI] updateMyMove: " + move);

        // 更新RoadBoard
        roadBoard.addStone(move.index1(), myColorInt);
        if (move.index1() != move.index2()) {
            roadBoard.addStone(move.index2(), myColorInt);
        }

        // 更新父类的Board（确保一致性）
        if (this.board != null) {
            this.board.makeMove(move);
        }

        moveCount++;
        System.out.println("[G11 AI] moveCount after update: " + moveCount);
    }

    // --- V1 逻辑: 必杀与必防 ---
    private Move checkEmergency() {
        List<Integer> candidates = roadBoard.getEmptyNeighbors(2);

        // 使用 RoadBoard 的 evaluatePointSimple 进行贪心排序
        candidates.sort((a, b) -> roadBoard.evaluatePointSimple(b, myColorInt) - roadBoard.evaluatePointSimple(a, myColorInt));

        int topN = Math.min(candidates.size(), 20);

        // A. 检查己方是否有一步制胜 (连6)
        for(int i = 0; i < topN; i++) {
            for(int j = i + 1; j < topN; j++) {
                int p1 = candidates.get(i);
                int p2 = candidates.get(j);

                // 验证位置为空
                if (!roadBoard.isEmpty(p1) || !roadBoard.isEmpty(p2)) {
                    continue;
                }

                roadBoard.addStone(p1, myColorInt);
                roadBoard.addStone(p2, myColorInt);
                int score = roadBoard.evaluate(myColorInt);
                roadBoard.removeStone(p2, myColorInt);
                roadBoard.removeStone(p1, myColorInt);

                if (score > 9000000) return new Move(p1, p2); // 发现绝杀
            }
        }

        // B. 检查对方是否有一步制胜 (必须防守)
        // 模拟对方下子，如果对方能赢，必须抢占
        for(int i = 0; i < topN; i++) {
            for(int j = i + 1; j < topN; j++) {
                int p1 = candidates.get(i);
                int p2 = candidates.get(j);

                // 验证位置为空
                if (!roadBoard.isEmpty(p1) || !roadBoard.isEmpty(p2)) {
                    continue;
                }

                roadBoard.addStone(p1, oppColorInt);
                roadBoard.addStone(p2, oppColorInt);
                int score = roadBoard.evaluate(oppColorInt); // 以对方视角看分
                roadBoard.removeStone(p2, oppColorInt);
                roadBoard.removeStone(p1, oppColorInt);

                if (score > 9000000) {
                    return new Move(p1, p2); // 阻断对方绝杀
                }
            }
        }
        return null;
    }

    // --- V2 逻辑: Alpha-Beta 搜索 ---

    private Move alphaBetaSearch() {
        System.out.println("[G11 AI] alphaBetaSearch called, moveCount=" + moveCount);

        List<Integer> rawCandidates = roadBoard.getEmptyNeighbors(2);
        if (rawCandidates.isEmpty()) {
            System.out.println("[G11 AI] No candidates found, returning null");
            return null;
        }

        // 对候选点进行排序
        rawCandidates.sort((a, b) -> roadBoard.evaluatePointSimple(b, myColorInt) - roadBoard.evaluatePointSimple(a, myColorInt));

        int limit = Math.min(rawCandidates.size(), SEARCH_WIDTH);
        System.out.println("[G11 AI] Number of candidates: " + rawCandidates.size() + ", limit to: " + limit);

        Move bestMove = null;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int i = 0; i < limit; i++) {
            for (int j = i + 1; j < limit; j++) {
                int p1 = rawCandidates.get(i);
                int p2 = rawCandidates.get(j);

                // 验证位置为空
                if (!roadBoard.isEmpty(p1) || !roadBoard.isEmpty(p2)) {
                    continue;
                }

                // 模拟下棋
                roadBoard.addStone(p1, myColorInt);
                roadBoard.addStone(p2, myColorInt);

                int val = minLayer(MAX_DEPTH - 1, alpha, beta);

                // 回溯
                roadBoard.removeStone(p2, myColorInt);
                roadBoard.removeStone(p1, myColorInt);

                if (val > alpha) {
                    alpha = val;
                    bestMove = new Move(p1, p2);
                    System.out.println("[G11 AI] New best move: " + bestMove + " with score " + val);
                }
            }
        }

        if (bestMove == null && limit >= 2) {
            // 如果没有找到有效走步，使用前两个候选点
            System.out.println("[G11 AI] No valid move found in search, using first two candidates");
            int p1 = rawCandidates.get(0);
            int p2 = rawCandidates.get(1);
            if (roadBoard.isEmpty(p1) && roadBoard.isEmpty(p2)) {
                bestMove = new Move(p1, p2);
            }
        }

        return bestMove;
    }

    private int minLayer(int depth, int alpha, int beta) {
        int score = roadBoard.evaluate(myColorInt);
        if (depth <= 0 || Math.abs(score) > 5000000) {
            return score;
        }

        List<Integer> rawCandidates = roadBoard.getEmptyNeighbors(2);
        // 对手视角排序
        rawCandidates.sort((a, b) -> roadBoard.evaluatePointSimple(b, oppColorInt) - roadBoard.evaluatePointSimple(a, oppColorInt));

        int limit = Math.min(rawCandidates.size(), SEARCH_WIDTH);
        int minVal = Integer.MAX_VALUE;

        for (int i = 0; i < limit; i++) {
            for (int j = i + 1; j < limit; j++) {
                int p1 = rawCandidates.get(i);
                int p2 = rawCandidates.get(j);

                // 验证位置为空
                if (!roadBoard.isEmpty(p1) || !roadBoard.isEmpty(p2)) {
                    continue;
                }

                roadBoard.addStone(p1, oppColorInt);
                roadBoard.addStone(p2, oppColorInt);

                int val = maxLayer(depth - 1, alpha, beta);

                roadBoard.removeStone(p2, oppColorInt);
                roadBoard.removeStone(p1, oppColorInt);

                minVal = Math.min(minVal, val);
                beta = Math.min(beta, val);
                if (beta <= alpha) {
                    return minVal;
                }
            }
        }
        return minVal;
    }

    private int maxLayer(int depth, int alpha, int beta) {
        int score = roadBoard.evaluate(myColorInt);
        if (depth <= 0 || Math.abs(score) > 5000000) {
            return score;
        }

        List<Integer> rawCandidates = roadBoard.getEmptyNeighbors(2);
        rawCandidates.sort((a, b) -> roadBoard.evaluatePointSimple(b, myColorInt) - roadBoard.evaluatePointSimple(a, myColorInt));

        int limit = Math.min(rawCandidates.size(), SEARCH_WIDTH);
        int maxVal = Integer.MIN_VALUE;

        for (int i = 0; i < limit; i++) {
            for (int j = i + 1; j < limit; j++) {
                int p1 = rawCandidates.get(i);
                int p2 = rawCandidates.get(j);

                // 验证位置为空
                if (!roadBoard.isEmpty(p1) || !roadBoard.isEmpty(p2)) {
                    continue;
                }

                roadBoard.addStone(p1, myColorInt);
                roadBoard.addStone(p2, myColorInt);

                int val = minLayer(depth - 1, alpha, beta);

                roadBoard.removeStone(p2, myColorInt);
                roadBoard.removeStone(p1, myColorInt);

                maxVal = Math.max(maxVal, val);
                alpha = Math.max(alpha, val);
                if (beta <= alpha) {
                    return maxVal;
                }
            }
        }
        return maxVal;
    }

    private void syncBoardToRoadBoard() {
        System.out.println("[G11 AI] Syncing parent board state to RoadBoard");

        // 清空并重建RoadBoard
        roadBoard = new RoadBoard();

        // 从父类的board中读取所有棋子，并添加到RoadBoard
        int myStones = 0;
        for (int i = 0; i < 361; i++) {
            core.board.PieceColor piece = this.board.get(i);
            if (piece == core.board.PieceColor.BLACK) {
                roadBoard.addStone(i, 1);
                if (myColorInt == 1) myStones++; // 黑方AI数黑子
            } else if (piece == core.board.PieceColor.WHITE) {
                roadBoard.addStone(i, 2);
                if (myColorInt == 2) myStones++; // 白方AI数白子
            }
        }

        // 正确计算moveCount：每步下2子，所以除以2，但要排除初始中心黑子
        if (myColorInt == 1) { // 黑方
            // 黑方：初始有一个中心黑子，所以要减1
            moveCount = Math.max(0, (myStones - 1) / 2);
        } else { // 白方
            // 白方：初始没有白子
            moveCount = myStones / 2;
        }

        System.out.println("[G11 AI] Sync completed, moveCount=" + moveCount);
    }
}