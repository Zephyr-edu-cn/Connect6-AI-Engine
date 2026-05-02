package stud;

import core.game.timer.StopwatchCPU;
import core.game.ui.Configuration;
import core.match.GameEvent;
import core.player.Player;

import java.util.ArrayList;

/**
 * Evaluation entry for Manual-G33 vs GA-G33.
 *
 * This tester compares the hand-tuned V3 weights with the GA-tuned weights
 * under the same Connect6 engine architecture.
 */
public class ManualVsGATester {

    public static void main(String[] args) {
        StopwatchCPU timer = new StopwatchCPU();

        Configuration.GUI = false;

        GameEvent event = new GameEvent("Manual-G33 vs GA-G33", createPlayers());

        // For two players:
        // carnivalRun(500) means 500 games for this pair,
        // usually balanced by first/second player internally.
        event.carnivalRun(1000);

        event.showResults();

        double elapsedTime = timer.elapsedTime();
        System.out.printf("%.4f%n", elapsedTime);
    }

    private static ArrayList<Player> createPlayers() {
        ArrayList<Player> players = new ArrayList<>();

        players.add(new stud.g33.ManualG33Engine());
        players.add(new stud.g33.GAG33Engine());

        return players;
    }
}