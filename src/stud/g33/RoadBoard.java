package stud.g33;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 六子棋“路”(Road)模型盘面状态类
 * 核心逻辑：维护 924 条“路”的状态，支持增量更新与动态权重注入
 */
public class RoadBoard {
    // 默认权重基准，参考论文《六子棋博弈的评估函数》
    public static final int[] DEFAULT_WEIGHTS = {0, 17, 78, 141, 788, 1030, 10000000};

    // 实例变量：支持遗传算法动态注入权重
    private int[] weights;

    // 预计算全局静态数据：924条路及其反向索引
    private static int[][] ROADS; // [924][6]
    private static int[][] POS_TO_ROADS; // [361][] 每个点关联的路
    private static boolean initialized = false;

    // 当前局面的状态信息
    private int[] roadState; // 每条路的子数统计：[roadId*2]为黑子, [roadId*2+1]为白子
    private int[] boardMap;  // 19x19棋盘：0:空, 1:黑, 2:白

    /**
     * 默认构造函数：使用文献标准权重
     */
    public RoadBoard() {
        this(DEFAULT_WEIGHTS);
    }

    /**
     * GA专用构造函数：允许遗传算法注入变异后的基因权重
     */
    public RoadBoard(int[] customWeights) {
        if (!initialized) initStaticTables();
        this.weights = customWeights.clone();
        this.roadState = new int[ROADS.length * 2];
        this.boardMap = new int[361];
    }

    /**
     * 预计算 19x19 棋盘所有的“路”逻辑（静态初始化）
     */
    private static void initStaticTables() {
        List<int[]> roadList = new ArrayList<>();
        int size = 19;

        // 1. 横向扫描 (266条路)
        for (int r = 0; r < size; r++) {
            for (int c = 0; c <= size - 6; c++) {
                int[] road = new int[6];
                for (int k = 0; k < 6; k++) road[k] = r * size + (c + k);
                roadList.add(road);
            }
        }
        // 2. 纵向扫描 (266条路)
        for (int c = 0; c < size; c++) {
            for (int r = 0; r <= size - 6; r++) {
                int[] road = new int[6];
                for (int k = 0; k < 6; k++) road[k] = (r + k) * size + c;
                roadList.add(road);
            }
        }
        // 3. 右斜向 (196条路)
        for (int r = 0; r <= size - 6; r++) {
            for (int c = 0; c <= size - 6; c++) {
                int[] road = new int[6];
                for (int k = 0; k < 6; k++) road[k] = (r + k) * size + (c + k);
                roadList.add(road);
            }
        }
        // 4. 左斜向 (196条路)
        for (int r = 0; r <= size - 6; r++) {
            for (int c = 5; c < size; c++) {
                int[] road = new int[6];
                for (int k = 0; k < 6; k++) road[k] = (r + k) * size + (c - k);
                roadList.add(road);
            }
        }

        ROADS = roadList.toArray(new int[0][]);

        // 构建反向索引（点到路的映射），极速提升落子更新效率
        List<Integer>[] tempPosMap = new List[361];
        for (int i = 0; i < 361; i++) tempPosMap[i] = new ArrayList<>();
        for (int i = 0; i < ROADS.length; i++) {
            for (int pos : ROADS[i]) tempPosMap[pos].add(i);
        }
        POS_TO_ROADS = new int[361][];
        for (int i = 0; i < 361; i++) {
            POS_TO_ROADS[i] = tempPosMap[i].stream().mapToInt(Integer::intValue).toArray();
        }
        initialized = true;
    }

    /**
     * 增量落子更新
     */
    public void addStone(int idx, int colorType) {
        if (idx < 0 || idx >= 361) return;
        boardMap[idx] = colorType;
        for (int roadId : POS_TO_ROADS[idx]) {
            roadState[roadId * 2 + (colorType - 1)]++;
        }
    }

    /**
     * 增量回溯提子（Alpha-Beta 搜索必备）
     */
    public void removeStone(int idx, int colorType) {
        if (idx < 0 || idx >= 361) return;
        boardMap[idx] = 0;
        for (int roadId : POS_TO_ROADS[idx]) {
            roadState[roadId * 2 + (colorType - 1)]--;
        }
    }

    /**
     * 局面评估函数：计算博弈双方所有有效路的价值总和
     */
    public int evaluate(int myColorType) {
        int myScore = 0;
        int oppScore = 0;
        int oppColorType = (myColorType == 1) ? 2 : 1;

        for (int i = 0; i < ROADS.length; i++) {
            int b = roadState[i * 2];
            int w = roadState[i * 2 + 1];

            // 只要路上同时有双方棋子，该路即为“死路”，价值为0
            if (b > 0 && w > 0) continue;

            if (b > 0) {
                int val = weights[b];
                if (myColorType == 1) myScore += val; else oppScore += val;
            } else if (w > 0) {
                int val = weights[w];
                if (myColorType == 2) myScore += val; else oppScore += val;
            }
        }
        // 侧重防御系数，防止被对手瞬间杀局
        return myScore - (int)(oppScore * 1.1);
    }

    public boolean isEmpty(int idx) {
        return boardMap[idx] == 0;
    }

    /**
     * 启发式搜索：获取已有棋子周围的空位，缩小搜索宽度
     */
    public List<Integer> getEmptyNeighbors(int radius) {
        Set<Integer> candidates = new HashSet<>();
        boolean hasPiece = false;
        for (int i = 0; i < 361; i++) {
            if (boardMap[i] != 0) {
                hasPiece = true;
                int r = i / 19, c = i % 19;
                for (int dr = -radius; dr <= radius; dr++) {
                    for (int dc = -radius; dc <= radius; dc++) {
                        int nr = r + dr, nc = c + dc;
                        if (nr >= 0 && nr < 19 && nc >= 0 && nc < 19) {
                            int nIdx = nr * 19 + nc;
                            if (boardMap[nIdx] == 0) candidates.add(nIdx);
                        }
                    }
                }
            }
        }
        if (!hasPiece) candidates.add(180); // 无子时默认选择中心 J10
        return new ArrayList<>(candidates);
    }

    /**
     * 提取威胁空间：识别所有包含 3 颗子以上且无阻挡的路
     */
    public Set<Integer> getThreatSpace() {
        Set<Integer> threatSpace = new HashSet<>();
        for (int i = 0; i < ROADS.length; i++) {
            int b = roadState[i * 2];
            int w = roadState[i * 2 + 1];
            // 只考虑已有 3-5 子且无对手子阻挡的强力路
            if ((b >= 3 && w == 0) || (w >= 3 && b == 0)) {
                for (int pos : ROADS[i]) {
                    if (boardMap[pos] == 0) threatSpace.add(pos);
                }
            }
        }
        if (threatSpace.size() < 10) threatSpace.addAll(getEmptyNeighbors(2));
        return threatSpace;
    }

    /**
     * 单点启发式评分：用于 Alpha-Beta 搜索的节点排序
     */
    public int evaluatePointSimple(int idx, int myColor) {
        int score = 0;
        int oppColor = (myColor == 1) ? 2 : 1;
        for (int roadId : POS_TO_ROADS[idx]) {
            int b = roadState[roadId * 2];
            int w = roadState[roadId * 2 + 1];
            // 进攻与防守价值并重
            if (myColor == 1 && w == 0) score += weights[b + 1];
            else if (myColor == 2 && b == 0) score += weights[w + 1];
            if (oppColor == 1 && w == 0) score += weights[b + 1];
            else if (oppColor == 2 && b == 0) score += weights[w + 1];
        }
        return score;
    }

    // 供外部获取路状态
    public int getRoadCount() { return ROADS.length; }

    /**
     * 获取指定路上的黑白棋子数量
     * @return int[0]:黑子数, int[1]:白子数
     */
    public int[] getRoadState(int roadId) {
        return new int[]{roadState[roadId * 2], roadState[roadId * 2 + 1]};
    }

    /**
     * 获取指定路包含的 6 个格子索引坐标
     */
    public int[] getRoadIndices(int roadId) {
        return ROADS[roadId];
    }
}