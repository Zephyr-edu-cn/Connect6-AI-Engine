package core.game;

import core.player.Player;

import java.time.LocalDate;
import java.util.ArrayList;

public class GameResult {
    private final Player first;     //黑方
    private final Player second;     //白方
    private final String winner;    //胜方
    private final LocalDate date = LocalDate.now(); //对战时间
    private final ArrayList<Move> moves;

    public Player getFirst() {
        return first;
    }

    public Player getSecond() {
        return second;
    }

    private final int steps;
    private final String endReason; //获胜原因

    public GameResult(Player first, Player second, String winner,
                      int steps, String endReason, ArrayList<Move> moves) {
        this.first = first;
        this.second = second;
        this.winner = winner;
        this.steps = steps;
        this.endReason = endReason;
        this.moves = moves;
    }

    /**
     * 棋手name在本次对局中的得分
     * 胜：2分；平：1分；负：0分
     * @param name
     * @return
     */
    public int score(String name) {
        if ("NONE".equals(this.winner))
            return 1;
        if (name.equals(this.winner)) {
            return 2;
        }
        return 0;
    }

    /**
     * 获取棋手在本次对局中的对手
     * @param player
     * @return name的对手的名字
     */
    public Player getOpponent(Player player){
        if (this.first.equals(player)){
            return this.second;
        }
        return this.first;
    }

    public String toString() {
        return "\t" + this.second.fullName() + "\n\t" + this.first.fullName() + "\n\t胜方：" + this.winner + "\n\t步数：" + this.steps + "\n\t结束原因：" + this.endReason + "\n";
    }

    public void save(String eventName){
        System.out.println(eventName + "_" + this.second.name() + "vs" + this.first.name());
    }

    public boolean isOppenent(Player player, Player opponent) {
        return getOpponent(player).name().equals(opponent.name());
    }
}
