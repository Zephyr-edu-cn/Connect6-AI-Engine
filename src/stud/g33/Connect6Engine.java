package stud.g33;

import core.board.Board;
import core.board.PieceColor;
import core.game.Game;
import core.game.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 六子棋博弈引擎 V3 终极版
 * 特点：支持遗传算法权重注入、Alpha-Beta剪枝、威胁空间搜索 (TBS)
 */
public class Connect6Engine extends core.player.AI {

    private RoadBoard roadBoard;
    private int myColorInt; // 1:黑子, 2:白子
    private int oppColorInt;
    private int moveCount = 0;

    // 遗传算法优化的动态权重与个体识别码
    private int[] customWeights;
    private String customName = "G33-AlphaBot";

    // 搜索超参数：控制搜索树的深度与宽度
    private static final int MAX_DEPTH = 4;
    private static final int SEARCH_WIDTH = 14;

    /**
     * 默认构造函数：用于最终实战，加载文献推荐的基准权重
     */
//    public Connect6Engine() {
//        this.customWeights = RoadBoard.DEFAULT_WEIGHTS;
//    }
    public Connect6Engine() {
        // 经 GA 离线进化第 3 代取得峰值适应度的最优权重
        this.customWeights = new int[]{0, 14, 65, 157, 630, 1104, 10000000};
    }

    /**
     * GA 专用构造函数：允许遗传算法在训练时注入不同的“基因”（权重数组）
     *
     * @param weights 评估函数分量权重
     * @param name    个体唯一标识，用于裁判统计得分
     */
    public Connect6Engine(int[] weights, String name) {
        this.customWeights = weights;
        this.customName = name;
    }

    @Override
    public String name() {
        return this.customName;
    }

    @Override
    public void playGame(Game game) {
        super.playGame(game);

        // 初始化棋盘状态
        this.board = new Board();

        // 注入权重，实例化基于“路”的评估模型
        this.roadBoard = new RoadBoard(this.customWeights);

        this.moveCount = 0;
        this.myColorInt = (this.getColor() == PieceColor.BLACK) ? 1 : 2;
        this.oppColorInt = (myColorInt == 1) ? 2 : 1;

        // 同步开局状态：六子棋黑方首手落子中心 J10
        roadBoard.addStone(180, 1);
    }

    @Override
    public Move findNextMove(Move opponentMove) {
        // 1. 同步对方的落子状态到 RoadBoard
        if (opponentMove != null) {
            this.board.makeMove(opponentMove);
            roadBoard.addStone(opponentMove.index1(), oppColorInt);
            if (opponentMove.index1() != opponentMove.index2()) {
                roadBoard.addStone(opponentMove.index2(), oppColorInt);
            }
        }

        // 2. 紧急威胁与即时斩杀检查：若有一步胜或必堵点，不进入搜索
        Move emergencyMove = checkEmergency();
        if (emergencyMove != null) {
            updateMyState(emergencyMove);
            return emergencyMove;
        }

        // 3. 执行核心搜索：Alpha-Beta 剪枝与威胁空间搜索 (TBS)
        Move bestMove = alphaBetaWithTBS();

        // 4. 兜底策略：若搜索未返回结果，选择启发式最高分点
        if (bestMove == null) {
            bestMove = generateFallbackMove();
        }

        updateMyState(bestMove);
        return bestMove;
    }

    private void updateMyState(Move move) {
        roadBoard.addStone(move.index1(), myColorInt);
        if (move.index1() != move.index2()) {
            roadBoard.addStone(move.index2(), myColorInt);
        }
        moveCount++;
    }

    /**
     * 紧急威胁检测：扫描己方和对方的斩杀机会
     */
    private Move checkEmergency() {
        List<Integer> candidates = roadBoard.getEmptyNeighbors(2);
        candidates.sort((a, b) -> roadBoard.evaluatePointSimple(b, myColorInt) - roadBoard.evaluatePointSimple(a, myColorInt));
        int topN = Math.min(candidates.size(), 20);

        // 己方斩杀检查
        for (int i = 0; i < topN; i++) {
            for (int j = i + 1; j < topN; j++) {
                int p1 = candidates.get(i);
                int p2 = candidates.get(j);
                roadBoard.addStone(p1, myColorInt);
                roadBoard.addStone(p2, myColorInt);
                int score = roadBoard.evaluate(myColorInt);
                roadBoard.removeStone(p2, myColorInt);
                roadBoard.removeStone(p1, myColorInt);
                if (score > 9000000) return new Move(p1, p2);
            }
        }
        // 对方防堵检查
        for (int i = 0; i < topN; i++) {
            for (int j = i + 1; j < topN; j++) {
                int p1 = candidates.get(i);
                int p2 = candidates.get(j);
                roadBoard.addStone(p1, oppColorInt);
                roadBoard.addStone(p2, oppColorInt);
                int score = roadBoard.evaluate(oppColorInt);
                roadBoard.removeStone(p2, oppColorInt);
                roadBoard.removeStone(p1, oppColorInt);
                if (score > 9000000) return new Move(p1, p2);
            }
        }
        return null;
    }

    /**
     * 结合 TBS 的 Alpha-Beta 搜索框架
     */
    private Move alphaBetaWithTBS() {
        Set<Integer> threatSpace = roadBoard.getThreatSpace(); // 提取威胁空间
        List<Integer> candidates = new ArrayList<>(threatSpace);

        if (candidates.size() < 10) candidates = roadBoard.getEmptyNeighbors(2);
        candidates.sort((a, b) -> roadBoard.evaluatePointSimple(b, myColorInt) - roadBoard.evaluatePointSimple(a, myColorInt));

        int limit = Math.min(candidates.size(), SEARCH_WIDTH);
        Move bestMove = null;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // 生成候选走法组合并递归搜索
        for (int i = 0; i < limit; i++) {
            for (int j = i + 1; j < limit; j++) {
                int p1 = candidates.get(i);
                int p2 = candidates.get(j);
                roadBoard.addStone(p1, myColorInt);
                roadBoard.addStone(p2, myColorInt);
                int val = minLayer(MAX_DEPTH - 1, alpha, beta);
                roadBoard.removeStone(p2, myColorInt);
                roadBoard.removeStone(p1, myColorInt);

                if (val > alpha) {
                    alpha = val;
                    bestMove = new Move(p1, p2);
                    if (alpha > 5000000) return bestMove;
                }
            }
        }
        return bestMove;
    }

    private int minLayer(int depth, int alpha, int beta) {
        int score = roadBoard.evaluate(myColorInt);
        if (depth <= 0 || Math.abs(score) > 5000000) return score;

        List<Integer> candidates = roadBoard.getEmptyNeighbors(2);
        candidates.sort((a, b) -> roadBoard.evaluatePointSimple(b, oppColorInt) - roadBoard.evaluatePointSimple(a, oppColorInt));
        int limit = Math.min(candidates.size(), SEARCH_WIDTH);

        int minVal = Integer.MAX_VALUE;
        for (int i = 0; i < limit; i++) {
            for (int j = i + 1; j < limit; j++) {
                roadBoard.addStone(candidates.get(i), oppColorInt);
                roadBoard.addStone(candidates.get(j), oppColorInt);
                int val = maxLayer(depth - 1, alpha, beta);
                roadBoard.removeStone(candidates.get(j), oppColorInt);
                roadBoard.removeStone(candidates.get(i), oppColorInt);
                minVal = Math.min(minVal, val);
                beta = Math.min(beta, val);
                if (beta <= alpha) return minVal; // 剪枝
            }
        }
        return minVal;
    }

    private int maxLayer(int depth, int alpha, int beta) {
        int score = roadBoard.evaluate(myColorInt);
        if (depth <= 0 || Math.abs(score) > 5000000) return score;

        List<Integer> candidates = roadBoard.getEmptyNeighbors(2);
        candidates.sort((a, b) -> roadBoard.evaluatePointSimple(b, myColorInt) - roadBoard.evaluatePointSimple(a, myColorInt));
        int limit = Math.min(candidates.size(), SEARCH_WIDTH);

        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < limit; i++) {
            for (int j = i + 1; j < limit; j++) {
                roadBoard.addStone(candidates.get(i), myColorInt);
                roadBoard.addStone(candidates.get(j), myColorInt);
                int val = minLayer(depth - 1, alpha, beta);
                roadBoard.removeStone(candidates.get(j), myColorInt);
                roadBoard.removeStone(candidates.get(i), myColorInt);
                maxVal = Math.max(maxVal, val);
                alpha = Math.max(alpha, val);
                if (beta <= alpha) return maxVal; // 剪枝
            }
        }
        return maxVal;
    }

    private Move generateFallbackMove() {
        List<Integer> empties = roadBoard.getEmptyNeighbors(2);
        if (empties.size() < 2) return new Move(0, 1);
        empties.sort((a, b) -> roadBoard.evaluatePointSimple(b, myColorInt) - roadBoard.evaluatePointSimple(a, myColorInt));
        return new Move(empties.get(0), empties.get(1));
    }
}