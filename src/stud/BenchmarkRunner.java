package stud;

import core.game.ui.Configuration;
import core.match.GameEvent;
import core.player.Player;

import java.util.ArrayList;

public class BenchmarkRunner {
    public static void main(String[] args) {
        String match = "g33-vs-g22";
        int games = 10;

        for (int i = 0; i < args.length; i++) {
            if ("--match".equals(args[i]) && i + 1 < args.length) {
                match = args[++i];
            } else if ("--games".equals(args[i]) && i + 1 < args.length) {
                games = Integer.parseInt(args[++i]);
            } else if ("--help".equals(args[i])) {
                printUsage();
                return;
            }
        }

        if (games <= 0) {
            throw new IllegalArgumentException("--games must be positive");
        }

        Configuration.GUI = false;

        GameEvent event = new GameEvent(match, createPlayers(match));
        event.carnivalRun(games);
        event.showResults();
    }

    private static ArrayList<Player> createPlayers(String match) {
        ArrayList<Player> players = new ArrayList<>();
        switch (match) {
            case "g33-vs-g22":
                players.add(new stud.g33.Connect6Engine());
                players.add(new stud.g22.AI());
                return players;
            case "ga-vs-manual":
                players.add(new stud.g33.GAG33Engine());
                players.add(new stud.g33.ManualG33Engine());
                return players;
            default:
                throw new IllegalArgumentException("Unknown match: " + match);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java stud.BenchmarkRunner [--match g33-vs-g22|ga-vs-manual] [--games N]");
    }
}
