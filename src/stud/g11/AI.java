package stud.g11;

import core.board.Board;
import core.board.PieceColor;
import core.game.Game;
import core.game.Move;

import java.util.*;

public class AI extends core.player.AI {
    private Random rand = new Random();
    private int moveCount = 0;
    private List<Integer> emptyPositionsCache;

    @Override
    public Move findNextMove(Move opponentMove) {
        // 处理对手走法
        if (moveCount == 0 && opponentMove == null) {
            moveCount++;
            return firstMove();
        }

        if (opponentMove != null) {
            this.board.makeMove(opponentMove);
            moveCount++;
        }

        // V1策略
        Move move = findMoveV1();
        this.board.makeMove(move);
        moveCount++;
        return move;
    }

    private Move findMoveV1() {
        PieceColor myColor = this.getColor();
        PieceColor opponentColor = myColor.opposite();

        // 1. 立即获胜
        Move winMove = ThreatDetector.findWinningMove(board, myColor);
        if (winMove != null) {
            //System.out.println("find way to win");
            return winMove;
        }

        // 2. 防守对方立即获胜的威胁
        Move defenseMove = ThreatDetector.findDefensiveMove(board, myColor, opponentColor);
        if (defenseMove != null) {
            //System.out.println("find defensive move");
            return defenseMove;
        }

        // 3. 如果对手有单步威胁，进行预防性防守
        if (ThreatDetector.hasOneMoveThreat(board, opponentColor)) {
            Move preventiveMove = findPreventiveMove(myColor, opponentColor);
            if (preventiveMove != null) {
                //System.out.println("find preventive move");
                return preventiveMove;
            }
        }

        // 4. 进攻性走法
        Move offensiveMove = findOffensiveMove(myColor);
        if (offensiveMove != null) {
            //System.out.println("find offensive move");
            return offensiveMove;
        }

        // 5. 启发式走法
        return findHeuristicMove(myColor);
    }

    // 寻找预防性防守走法
    private Move findPreventiveMove(PieceColor myColor, PieceColor opponentColor) {
        List<Move> candidates = generateCandidateMoves(30);
        Move bestMove = null;
        double bestScore = -Double.MAX_VALUE;

        for (Move move : candidates) {
            double score = evaluatePreventiveMove(move, myColor, opponentColor);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    // 评估预防性走法
    private double evaluatePreventiveMove(Move move, PieceColor myColor, PieceColor opponentColor) {
        double score = 0;

        // 位置价值
        int row1 = move.index1() / 19, col1 = move.index1() % 19;
        int row2 = move.index2() / 19, col2 = move.index2() % 19;
        score += BoardHelper.getPositionValue(row1, col1);
        score += BoardHelper.getPositionValue(row2, col2);

        // 干扰对手潜在的威胁
        List<Integer> opponentPieces = BoardHelper.getPiecePositions(board, opponentColor);
        for (int piece : opponentPieces) {
            List<Integer> neighbors = BoardHelper.getEmptyNeighbors(board, piece, 2);
            for (int neighbor : neighbors) {
                if (neighbor == move.index1() || neighbor == move.index2()) {
                    score += 5; // 靠近对方棋子，干扰其发展
                }
            }
        }

        // 发展自己的棋子
        List<Integer> myPieces = BoardHelper.getPiecePositions(board, myColor);
        for (int piece : myPieces) {
            List<Integer> neighbors = BoardHelper.getEmptyNeighbors(board, piece, 2);
            for (int neighbor : neighbors) {
                if (neighbor == move.index1() || neighbor == move.index2()) {
                    score += 3; // 靠近己方棋子，加强连接
                }
            }
        }

        return score;
    }

    // 寻找进攻性走法
    private Move findOffensiveMove(PieceColor myColor) {
        List<Move> candidates = generateCandidateMoves(20);

        for (Move move : candidates) {
            // 检查是否能形成4连或以上
            if (countMaxConnectionAfterMove(move, myColor) >= 4) {
                return move;
            }
        }

        return null;
    }

    // 计算走法后的最大连接数
    private int countMaxConnectionAfterMove(Move move, PieceColor color) {
        // 创建临时棋盘
        PieceColor[][] tempBoard = BoardHelper.createBoardCopy(board);

        int row1 = move.index1() / 19, col1 = move.index1() % 19;
        int row2 = move.index2() / 19, col2 = move.index2() % 19;
        tempBoard[row1][col1] = color;
        tempBoard[row2][col2] = color;

        int maxConn = 0;

        // 检查两个落子点的连接情况
        for (int idx : new int[]{move.index1(), move.index2()}) {
            int r = idx / 19, c = idx % 19;
            for (int[] dir : BoardHelper.DIRECTIONS) {
                int count = 1; // 当前位置

                // 正向
                int nr = r + dir[0];
                int nc = c + dir[1];
                while (nr >= 0 && nr < 19 && nc >= 0 && nc < 19 && tempBoard[nr][nc] == color) {
                    count++;
                    nr += dir[0];
                    nc += dir[1];
                }

                // 反向
                nr = r - dir[0];
                nc = c - dir[1];
                while (nr >= 0 && nr < 19 && nc >= 0 && nc < 19 && tempBoard[nr][nc] == color) {
                    count++;
                    nr -= dir[0];
                    nc -= dir[1];
                }

                maxConn = Math.max(maxConn, count);
            }
        }

        return maxConn;
    }

    // 启发式走法选择
    private Move findHeuristicMove(PieceColor myColor) {
        List<Move> candidates = generateCandidateMoves(30);

        if (candidates.isEmpty()) {
            return generateRandomMove();
        }

        Move bestMove = candidates.get(0);
        double bestScore = evaluateHeuristicMove(bestMove, myColor);

        for (Move move : candidates) {
            double score = evaluateHeuristicMove(move, myColor);
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    // 评估启发式走法
    private double evaluateHeuristicMove(Move move, PieceColor myColor) {
        double score = 0;

        // 1. 位置价值
        int row1 = move.index1() / 19, col1 = move.index1() % 19;
        int row2 = move.index2() / 19, col2 = move.index2() % 19;
        score += BoardHelper.getPositionValue(row1, col1);
        score += BoardHelper.getPositionValue(row2, col2);

        // 2. 连接性得分
        score += evaluateConnectivity(move, myColor) * 10;

        // 3. 威胁性得分
        score += countMaxConnectionAfterMove(move, myColor) * 5;

        return score;
    }

    // 评估连接性
    private double evaluateConnectivity(Move move, PieceColor myColor) {
        double score = 0;
        List<Integer> myPieces = BoardHelper.getPiecePositions(board, myColor);

        // 计算与己方棋子的距离
        for (int piece : myPieces) {
            int pRow = piece / 19, pCol = piece % 19;

            // 到第一个子的距离
            int dist1 = Math.abs(pRow - (move.index1() / 19)) +
                    Math.abs(pCol - (move.index1() % 19));
            if (dist1 <= 3) {
                score += (4 - dist1);
            }

            // 到第二个子的距离
            int dist2 = Math.abs(pRow - (move.index2() / 19)) +
                    Math.abs(pCol - (move.index2() % 19));
            if (dist2 <= 3) {
                score += (4 - dist2);
            }
        }

        return score;
    }

    // 生成候选走法
    private List<Move> generateCandidateMoves(int maxMoves) {
        Set<Move> candidates = new HashSet<>();

        // 获取空位
        List<Integer> empties = getEmptyPositions();
        if (empties.size() < 2) {
            return new ArrayList<>();
        }

        // 收集热点位置
        Set<Integer> hotSpots = new HashSet<>();
        PieceColor myColor = this.getColor();

        // 1. 己方棋子周围的空位
        List<Integer> myPieces = BoardHelper.getPiecePositions(board, myColor);
        for (int piece : myPieces) {
            hotSpots.addAll(BoardHelper.getEmptyNeighbors(board, piece, 3));
        }

        // 2. 对方棋子周围的空位（防守）
        PieceColor opponentColor = myColor.opposite();
        List<Integer> opponentPieces = BoardHelper.getPiecePositions(board, opponentColor);
        for (int piece : opponentPieces) {
            hotSpots.addAll(BoardHelper.getEmptyNeighbors(board, piece, 2));
        }

        // 3. 如果热点太少，加入棋盘中心
        if (hotSpots.size() < 30) {
            int center = 9 * 19 + 9;
            hotSpots.addAll(BoardHelper.getEmptyNeighbors(board, center, 6));
        }

        // 4. 如果热点还是太少，使用所有空位
        if (hotSpots.size() < 20) {
            hotSpots.addAll(empties);
        }

        // 转换为列表
        List<Integer> spots = new ArrayList<>(hotSpots);

        // 生成候选走法
        for (int i = 0; i < spots.size() && candidates.size() < maxMoves; i++) {
            for (int j = i + 1; j < spots.size() && candidates.size() < maxMoves; j++) {
                int idx1 = spots.get(i);
                int idx2 = spots.get(j);
                candidates.add(new Move(idx1, idx2));
            }
        }

        // 如果候选走法不足，补充随机走法
        if (candidates.size() < maxMoves / 2) {
            while (candidates.size() < maxMoves) {
                candidates.add(generateRandomMove());
            }
        }

        return new ArrayList<>(candidates);
    }

    // 生成随机走法
    private Move generateRandomMove() {
        List<Integer> empties = getEmptyPositions();

        if (empties.size() < 2) {
            return new Move(0, 1);
        }

        int idx1 = empties.get(rand.nextInt(empties.size()));
        int idx2 = empties.get(rand.nextInt(empties.size()));
        while (idx2 == idx1) {
            idx2 = empties.get(rand.nextInt(empties.size()));
        }

        return new Move(idx1, idx2);
    }

    // 获取空位置（带缓存）
    private List<Integer> getEmptyPositions() {
        if (emptyPositionsCache == null) {
            emptyPositionsCache = BoardHelper.getAllEmptyPositions(board);
        }
        return emptyPositionsCache;
    }

    @Override
    public String name() {
        return "G11-V1";
    }

    @Override
    public void playGame(Game game) {
        super.playGame(game);
        board = new Board();
        moveCount = 0;
        rand = new Random();
        emptyPositionsCache = null;
    }
}