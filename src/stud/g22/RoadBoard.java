package stud.g22;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 六子棋博弈评估函数的 "路" (Road) 模型实现
 */
public class RoadBoard {
    // 19路棋盘，总路数为 924
    // 横向: 19 * (19-6+1) = 266
    // 纵向: 19 * (19-6+1) = 266
    // 左斜: 14 * 14 = 196 (对角线长度不同，需动态计算，总数是924)
    // 右斜: 196

    // 权重 (对应 1-5 子)
    // 6连子设为极大值
    private static final int[] WEIGHTS = {0, 17, 78, 141, 788, 1030, 10000000};

    // 预计算数据
    private static int[][] ROADS; // [924][6] 存储每条路包含的6个坐标索引
    private static int[][] POS_TO_ROADS; // [361][] 存储每个点属于哪些路
    private static boolean initialized = false;

    // 实例状态
    // roadCounts[i] 存储第 i 条路上的 {黑子数, 白子数}
    // 这是一个扁平数组，偶数位存黑子数，奇数位存白子数，即 roadState[i*2] = black, roadState[i*2+1] = white
    private int[] roadState;

    // 内部棋盘，用于快速查找空位
    private int[] boardMap; // 0:空, 1:黑, 2:白

    public RoadBoard() {
        if (!initialized) initStaticTables();
        roadState = new int[ROADS.length * 2]; // 默认为0
        boardMap = new int[361]; // 默认为0
    }

    // 预计算所有的路
    private static void initStaticTables() {
        List<int[]> roadList = new ArrayList<>();
        int size = 19;

        // 横向
        for (int r = 0; r < size; r++) {
            for (int c = 0; c <= size - 6; c++) {
                int[] road = new int[6];
                for (int k = 0; k < 6; k++) road[k] = r * size + (c + k);
                roadList.add(road);
            }
        }
        // 纵向
        for (int c = 0; c < size; c++) {
            for (int r = 0; r <= size - 6; r++) {
                int[] road = new int[6];
                for (int k = 0; k < 6; k++) road[k] = (r + k) * size + c;
                roadList.add(road);
            }
        }
        // 右斜 (South-East)
        for (int r = 0; r <= size - 6; r++) {
            for (int c = 0; c <= size - 6; c++) {
                int[] road = new int[6];
                for (int k = 0; k < 6; k++) road[k] = (r + k) * size + (c + k);
                roadList.add(road);
            }
        }
        // 左斜 (South-West)
        for (int r = 0; r <= size - 6; r++) {
            for (int c = 5; c < size; c++) {
                int[] road = new int[6];
                for (int k = 0; k < 6; k++) road[k] = (r + k) * size + (c - k);
                roadList.add(road);
            }
        }

        ROADS = roadList.toArray(new int[0][]);

        // 反向索引：点 -> 路
        List<Integer>[] tempPosMap = new List[361];
        for (int i = 0; i < 361; i++) tempPosMap[i] = new ArrayList<>();

        for (int i = 0; i < ROADS.length; i++) {
            for (int pos : ROADS[i]) {
                tempPosMap[pos].add(i);
            }
        }

        POS_TO_ROADS = new int[361][];
        for (int i = 0; i < 361; i++) {
            POS_TO_ROADS[i] = tempPosMap[i].stream().mapToInt(Integer::intValue).toArray();
        }

        initialized = true;
    }

    // 落子
    public void addStone(int idx, int colorType) { // colorType: 1=Black, 2=White
        boardMap[idx] = colorType;
        int[] affectedRoads = POS_TO_ROADS[idx];

        // 更新受影响的路的状态
        for (int roadId : affectedRoads) {
            if (colorType == 1) { // Black
                roadState[roadId * 2]++;
            } else { // White
                roadState[roadId * 2 + 1]++;
            }
        }
    }

    // 提子 (回溯用)
    public void removeStone(int idx, int colorType) {
        boardMap[idx] = 0;
        int[] affectedRoads = POS_TO_ROADS[idx];

        for (int roadId : affectedRoads) {
            if (colorType == 1) {
                roadState[roadId * 2]--;
            } else {
                roadState[roadId * 2 + 1]--;
            }
        }
    }

    public boolean isEmpty(int idx) {
        return boardMap[idx] == 0;
    }

    // 获取指定颜色的分数
    // Formula: Score = Sum(MyRoads) - Sum(EnemyRoads)
    public int evaluate(int myColorType) {
        int myScore = 0;
        int oppScore = 0;

        int oppColorType = (myColorType == 1) ? 2 : 1;

        // 遍历所有路
        // 注意：这种全遍历在搜索深层可能略慢，但对于 924 次简单的数组访问，Java 还是很快的
        // 进一步优化：可以在 addStone 时动态维护总分，但需要处理“路被堵死”时的分数回退，逻辑复杂。
        // 这里直接遍历 924 条路。

        for (int i = 0; i < ROADS.length; i++) {
            int b = roadState[i * 2];
            int w = roadState[i * 2 + 1];

            if (b > 0 && w > 0) continue; // 双方都有子，此路无效 (Dead Road)

            if (b > 0) {
                // 只有黑子
                int val = WEIGHTS[b];
                if (myColorType == 1) myScore += val;
                else oppScore += val;
            } else if (w > 0) {
                // 只有白子
                int val = WEIGHTS[w];
                if (myColorType == 2) myScore += val;
                else oppScore += val;
            }
        }

        // 系数：稍微侧重防守，避免被偷袭
        return myScore - (int) (oppScore * 1.2);
    }

    // 快速检查是否有空位（为了防止搜索填满棋盘出错）
    public List<Integer> getEmptyNeighbors(int radius) {
        Set<Integer> candidates = new HashSet<>();
        boolean hasPiece = false;
        int size = 19;

        for (int i = 0; i < 361; i++) {
            if (boardMap[i] != 0) {
                hasPiece = true;
                int r = i / size;
                int c = i % size;
                for (int dr = -radius; dr <= radius; dr++) {
                    for (int dc = -radius; dc <= radius; dc++) {
                        int nr = r + dr;
                        int nc = c + dc;
                        if (nr >= 0 && nr < size && nc >= 0 && nc < size) {
                            int nIdx = nr * size + nc;
                            if (boardMap[nIdx] == 0) candidates.add(nIdx);
                        }
                    }
                }
            }
        }

        if (!hasPiece) {
            candidates.add(19 * 9 + 9);
        }

        return new ArrayList<>(candidates);
    }

    // V1 风格：快速单点价值评估（用于候选点排序）
    public int evaluatePointSimple(int idx, int myColor) {
        int score = 0;
        int[] roads = POS_TO_ROADS[idx];
        int oppColor = (myColor == 1) ? 2 : 1;

        for (int roadId : roads) {
            int b = roadState[roadId * 2];
            int w = roadState[roadId * 2 + 1];

            // 进攻价值：如果是我的有效路，且加一颗子能升级
            if (myColor == 1) { // 我是黑
                if (w == 0) score += WEIGHTS[b + 1];
            } else { // 我是白
                if (b == 0) score += WEIGHTS[w + 1];
            }

            // 防守价值：如果是对方的有效路，堵住它价值巨大
            if (oppColor == 1) { // 敌是黑
                if (w == 0) score += WEIGHTS[b + 1]; // 堵住它的延伸
            } else { // 敌是白
                if (b == 0) score += WEIGHTS[w + 1];
            }
        }
        return score;
    }
}