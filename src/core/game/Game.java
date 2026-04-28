package core.game;

import core.board.PieceColor;
import core.game.timer.GameTimer;
import core.game.timer.TimerFactory;
import core.game.ui.Configuration;
import core.game.ui.GameUI;
import core.game.ui.UiFactory;
import core.game.ui.connect6.BeautyGUI;
import core.player.Player;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public class Game extends Observable implements Observer, Runnable {
    private final Referee referee;
    private Thread me;
    boolean flag = true;

    //通用的Game类，通用于两个棋手，分别执两种不同颜色的棋子。
    public Game(Player first, Player second) {
        //这盘对局的裁判
        this.referee = new Referee(first, second);
        //先手方和后手方进入棋局
        first.playGame(this);
        second.playGame(this);
    }
    //开始对局Thread
    public Thread start() {
        this.me = new Thread(this);
        this.me.start();
        return this.me;
    }
    //对局过程
    public void run() {

        if (Configuration.GUI) {
            //裁判为本对局设置GUI
            this.referee.setUi(this);
        }

        //当前步数和当前走步
        int steps = 1;
        Move currMove = null;

        while (running()) {

            if (this.referee.gameOver()) {
                endingGame("F", null);
                break;
            }
            if (steps > Configuration.MAX_STEP) {
                endingGame("M", null);
                break;
            }

            // 当前棋手下一手棋
            Player currPlayer = this.referee.whoseMove();
            //System.out.println("[GAME] Current player: " + currPlayer.name() + " (" + currPlayer.getColor() + ")");
            //System.out.println("[GAME] Current move from opponent: " + currMove);

            currPlayer.startTimer();
            Move move = null;
            try {
                move = currPlayer.findMove(currMove);
            } catch (Exception ex) {
                //System.out.println("==========================================");
                //System.out.println("EXCEPTION CAUGHT in " + currPlayer.name());
                //System.out.println("Full stack trace:");
                ex.printStackTrace();
                //System.out.println("==========================================");

                endingGame("E", null);
                break;
            }
            currPlayer.stopTimer();

            // 如果因某一方超时，导致线程被强迫终止，则退出循环，终止线程的运行
            if (Thread.interrupted()) {
                break;
            }

            // 如果当前棋手给出的是合法走步
            //System.out.println("[GAME] Checking if move " + move + " is legal...");
            if (this.referee.legalMove(move)) {
                setChanged();
                notifyObservers(move);
                //System.out.println("[GAME] Move accepted");
            } else {
                //System.out.println("[GAME] ILLEGAL MOVE DETECTED: " + move);
                endingGame("N", move);
                break;
            }

            steps++;
            currMove = move;
            //System.out.println("[GAME] Step " + steps + " completed");
            //System.out.println("----------------------------------------");
        }
    }

    //Timer超时事件的监听者
    public void update(Observable arg0, Object arg1) {
        //如果是来自Timer超时，则终止当前对局
        if (arg1.equals("T")) {
            if (this.me != null)
                this.me.interrupt();
            endingGame("T", null);
        }
    }

    public boolean running() {
        return flag;
    }

    private void endingGame(String endReason, Move currMove){
        this.referee.endingGame(endReason, currMove);
        flag = false;
    }
}