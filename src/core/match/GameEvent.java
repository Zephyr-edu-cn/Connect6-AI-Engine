package core.match;

import core.game.Game;
import core.game.GameResult;
import core.game.ui.Configuration;
import core.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public class GameEvent {
    private final String name;
    private final ArrayList<Player> players;
    private final ArrayList<Match> matches;

    public String getName() {
        return this.name;
    }

    public GameEvent(String name) {
        this.players = new ArrayList<>();
        this.matches = new ArrayList<>();
        this.name = name;

        String[] split = Configuration.PLAYER_IDs.split (",");
        //然后利用Lambda表达式进行类型转换即可
        int[] ids = Arrays.asList(split).stream().mapToInt(Integer::parseInt).toArray();
        for (int i : ids){
            addPlayer(getPlayerById(i));
        }
    }

    public GameEvent(String name, ArrayList<Player> players) {
        this.players = players;
        this.matches = new ArrayList<>();
        this.name = name;
    }

    private static Player getPlayerByName(String name){
        Class[] parameterType = null;
        try {
            return (Player) Class.forName(name).getDeclaredConstructor(parameterType).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Player getPlayerById(int id) {
        String name = "stud.g" + String.format("%02d", id) + ".AI";
        return getPlayerByName(name);
    }

    /**
     * 为该项赛事添加参赛棋手
     * @param player 参赛棋手
     */
    public void addPlayer(Player player) {
        this.players.add(player);
    }

    public ArrayList<Player> getPlayers() {
        return this.players;
    }

    /**
     * 获取赛事的所有对局
     * @return 赛事的所有对局
     */
    private ArrayList<Game> getGames() {
        ArrayList<Game> games = new ArrayList<>();

        Iterator<Match> itrMatch = this.matches.iterator();
        while (itrMatch.hasNext()) {
            games.addAll(itrMatch.next().getGames());
        }

        return games;
    }
    //
    public void hostGames(int hostId) throws CloneNotSupportedException {
        ArrayList<Game> games = new ArrayList<>();
        Player host = getHost(hostId);
        for (Player player : this.players) {
            if (!player.equals(host))
                games.add(new Game((Player) host.clone(), (Player)player.clone()));
        }
        runGames(games);
    }

    /**
     * 按照单循环为所有的参赛棋手安排比赛。每场比赛(Match)进行gameNumbers个对局(Game)
     * @param gameNumbers   每场比赛的对局数
     */
    public void carnivalRun(int gameNumbers) {
        this.matches.clear();
        int size = this.players.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++)
                this.matches.add(new Match(gameNumbers, this.players.get(i), this.players.get(j)));
        }
        runGames(getGames());
        Collections.sort(this.players);
    }

    public void runGames(ArrayList<Game> games) {
        for (Game game : games) {
            game.start();
            while (game.running()){
                ;
            }
        }
    }

    /**
     * 显示本次赛事的所有棋手的比赛结果
     */
    public void showResults() {
        gameStatistics();
        totalStatistics();
        System.out.println();
        System.out.println("===========棋手得分明细 (WSUM: 先手得分，BSUM：后手得分，TSUM：棋手的总得分)===========");
        scoreStatistics();
    }

    /**
     * 棋手之间的对局结果统计
     */
    public void gameStatistics() {
        for (int i = 0; i < this.players.size(); i++) {
            System.out.println("=========================================================================");
            int[] sScores = new int[2]; //棋手i的先后手总得分：
            int[] sStatics = new int[3];
            for (int j = 0; j < this.players.size(); j++) {
                //每个棋手与其对手
                if (i != j) {
                    //获得棋手i与棋手j的对弈结果统计[2][3], [0][3]先手的结果, [1][3]后手的结果
                    int[][] statistics = players.get(i).getGameStatistics(this.players.get(j));
                    if (statistics == null) return;
                    int[] scores = new int[2];
                    for (int k = 0; k < 2; k++) {
                        scores[k] = statistics[k][2] * 2 + statistics[k][1];
                        sScores[k] += scores[k];
                        for (int l = 0; l < 3; l++){
                            sStatics[l] += statistics[k][l];
                        }
                    }

                    //向控制台输出对弈结果
                    System.out.println("\tGame Statistics (" + players.get(i).name() + " vs " + players.get(j).name() + "): " + format(scores[0] + scores[1]));
                    System.out.println("\t\t先手：\twin: " + format(statistics[0][2]) + ", " +
                                                "lose: " + format(statistics[0][0]) + ", " +
                                                "draw: " + format(statistics[0][1]) + ", " +
                                                "得分: " + format(scores[0]));
                    System.out.println("\t\t后手：\twin: " + format(statistics[1][2]) + ", " +
                                                "lose: " + format(statistics[1][0]) + ", " +
                                                "draw: " + format(statistics[1][1]) + ", " +
                                                "得分: " + format(scores[1]));
                    System.out.println("\t\t合计：\twin: " + format(statistics[0][2] + statistics[1][2]) + ", " +
                                                "lose: " + format(statistics[0][0] + statistics[1][0]) + ", " +
                                                "draw: " + format(statistics[0][1] + statistics[1][1]) + ", " +
                                                "得分: " + format(scores[0] + scores[1]));
                }
            }
            System.out.println("----------------------------------------------------------------------");
            System.out.println("\t " + players.get(i).name() + "总计：\twin: " + format(sStatics[2]) + ", " +
                    "lose: " + format(sStatics[0]) + ", " +
                    "draw: " + format(sStatics[1]) + ", " +
                    "得分: " + format(sScores[0] + sScores[1]));
            System.out.println();
        }
    }

    private String format(int scores){
            return String.format("%5d", scores);
    }

    /**
     * 棋手之间的对局结果统计
     */
    public void totalStatistics() {
        System.out.println("================================棋手总战绩================================");
        for (int i = 0; i < this.players.size(); i++) {
            int[] sScores = new int[2]; //棋手i的先后手总得分：
            int[] sStatics = new int[3];
            for (int j = 0; j < this.players.size(); j++) {
                //每个棋手与其对手
                if (i != j) {
                    //获得棋手i与棋手j的对弈结果统计[2][3], [0][3]先手的结果, [1][3]后手的结果
                    int[][] statistics = players.get(i).getGameStatistics(this.players.get(j));
                    if (statistics == null) return;
                    int[] scores = new int[2];
                    for (int k = 0; k < 2; k++) {
                        scores[k] = statistics[k][2] * 2 + statistics[k][1];
                        sScores[k] += scores[k];
                        for (int l = 0; l < 3; l++) {
                            sStatics[l] += statistics[k][l];
                        }
                    }
                }
            }
            System.out.println("\t " + players.get(i).name() + "总计：\twin: " + format(sStatics[2]) + ", " +
                    "lose: " + format(sStatics[0]) + ", " +
                    "draw: " + format(sStatics[1]) + ", " +
                    "得分: " + format(sScores[0] + sScores[1]));
        }
    }

    /**
     * 棋手之间的对局结果统计
     */
    public void scoreStatistics() {

        System.out.print("         ");
        for (int i = 0; i < this.players.size(); i++){
            System.out.printf("%5s, ", this.players.get(i).name());
        }
        System.out.printf("%5s\n", "|WSUM");
        System.out.print("\t-----");
        for (int i = 0; i <= this.players.size(); i++){
            System.out.print("-------");
        }
        System.out.println();

        int[][] sScores = new int[2][this.players.size()]; //棋手先后手分别得分：[0]所有棋手的先手得分， [1]后手得分

        for (int i = 0; i < this.players.size(); i++) {
            System.out.printf("\t%5s", this.players.get(i).name() + "|");
            int total = 0;
            for (int j = 0; j < this.players.size(); j++){
                //每个棋手与其对手
                if (i != j) {
                    //获得棋手i与棋手j的对弈结果统计[2][3],[0]先手的结果, [1]后手的结果
                    int[][] statistics = players.get(i).getGameStatistics(this.players.get(j));
                    if (statistics == null) return;

                    //向控制台输出对弈结果
                    int scores = statistics[0][2] * 2 + statistics[0][1];
                    System.out.printf("%5d, ",scores);
                    sScores[0][i] += scores;
                    sScores[1][i] += statistics[1][2] * 2 + statistics[1][1];
                }
                else
                    System.out.print("     , ");
            }
            //棋手i先手总分
            System.out.printf("|%5d", sScores[0][i]);
            System.out.println();
        }

        //打印每位棋手的后手得分
        System.out.printf("\t%5s", "BSUM|");
        for (int i = 0; i < this.players.size(); i++){
            System.out.printf("%5d, ", sScores[1][i]);
        }
        System.out.printf("|%4s", " -");
        System.out.println();

        //打印每位棋手的总得分
        System.out.printf("\t%5s", "TSUM|");
        for (int i = 0; i < this.players.size(); i++){
            System.out.printf("%5d, ", sScores[0][i] + sScores[1][i] );
        }
        System.out.printf("|%4s", " -");
        System.out.println();
    }


    private Player getHost(int hostId){
        for (Player player : this.players){
            if (player.name().equals(String.format("G%02d", hostId))){
                return player;
            }
        }
        return null;
    }

    /**
     * 主场AI和其他AI的对战结果
     * @param hostId
     */
    public void showHostResults(int hostId) {
        Player host = getHost(hostId);
        System.out.println(host.name() + "和" + getPlayerNames() + "的对战结果如下：");
        for (GameResult result : host.gameResults())
            //result.save(this.name.trim() + "");
            System.out.println(result);

        System.out.print("   ");
        for (int i = 0; i < this.players.size(); i++){
            System.out.printf("%5s,", this.players.get(i).name());
        }
        System.out.println();
        System.out.printf("%5s", host.name() + ",");
        for (int j = 0; j < this.players.size(); j++) {
            if (!host.equals(this.players.get(j))) {
                int[][] statistics = host.getGameStatistics(this.players.get(j));
                if (statistics == null) return;
                System.out.printf("%3d", statistics[0][2] * 2 + statistics[0][1]);

            } else {
                System.out.print("   ");
            }
            if (j < this.players.size() - 1) System.out.print(",  ");
        }
        System.out.println();
    }

    private String getPlayerNames() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        int i = 0;
        for (i = 0; i < players.size() - 1; i++){
            sb.append(players.get(i).name() + " ");
        }
        sb.append(players.get(i).name() + ")");
        return sb.toString();
    }
}
