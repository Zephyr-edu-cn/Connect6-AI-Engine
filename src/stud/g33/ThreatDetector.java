package stud.g33;

import core.game.Move;
import java.util.*;

/**
 * 威胁探测器：专门负责识别局面的即时胜着与必杀威胁
 * 作用：为搜索树提供“急剧剪枝”依据，并辅助 TBS 确定候选点
 */
public class ThreatDetector {

    /**
     * 在全局 924 条路中寻找能够直接成 6 的“必胜着”
     * @param rb 盘面路表模型
     * @param color 待检查的棋手颜色
     * @return 必胜走法，若无则返回 null
     */
    public static Move findWinningMove(RoadBoard rb, int color) {
        for (int i = 0; i < rb.getRoadCount(); i++) {
            int[] state = rb.getRoadState(i); // 获取该路黑白子数 [0]:黑, [1]:白
            int myStones = (color == 1) ? state[0] : state[1];
            int oppStones = (color == 1) ? state[1] : state[0];

            // 规则：路中无对手棋子，且己方已有 4 或 5 颗子
            if (oppStones == 0 && myStones >= 4) {
                int[] indices = rb.getRoadIndices(i); // 获取该路覆盖的 6 个坐标
                List<Integer> empties = new ArrayList<>();
                for (int idx : indices) {
                    if (rb.isEmpty(idx)) empties.add(idx);
                }

                // 剩余空位补齐即可达成 6 连
                if (!empties.isEmpty()) {
                    if (empties.size() == 1) {
                        // 差一子：补齐该子，另一子下在邻居点
                        return new Move(empties.get(0), rb.getEmptyNeighbors(1).get(0));
                    } else if (empties.size() == 2) {
                        // 差二子：直接补齐这两子
                        return new Move(empties.get(0), empties.get(1));
                    }
                }
            }
        }
        return null;
    }

    /**
     * 寻找对方的即时威胁，并生成强力的阻断性防守走法
     */
    public static Move findDefensiveMove(RoadBoard rb, int myColor) {
        int oppColor = (myColor == 1) ? 2 : 1;
        // 逻辑：对方能赢的地方，就是我必须防守的地方
        return findWinningMove(rb, oppColor);
    }

    /**
     * 识别局面是否存在“单步威胁”：即对方只需再下一子即可形成 5 连（VCF 预警）
     */
    public static boolean hasOneMoveThreat(RoadBoard rb, int oppColor) {
        for (int i = 0; i < rb.getRoadCount(); i++) {
            int[] state = rb.getRoadState(i);
            int oppStones = (oppColor == 1) ? state[0] : state[1];
            int myStones = (oppColor == 1) ? state[1] : state[0];

            // 对方已有 5 子且我方未堵塞该路
            if (myStones == 0 && oppStones == 5) return true;
        }
        return false;
    }
}