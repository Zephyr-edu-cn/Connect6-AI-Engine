package stud;

import stud.g33.RoadBoard;

import java.util.List;

public class SmokeDemo {
    public static void main(String[] args) {
        RoadBoard board = new RoadBoard();

        board.addStone(180, 1);
        board.addStone(181, 1);

        int roads = board.getRoadCount();
        List<Integer> neighbors = board.getEmptyNeighbors(2);
        int pointScore = board.evaluatePointSimple(182, 1);

        if (roads != 924) {
            throw new IllegalStateException("Expected 924 roads, got " + roads);
        }
        if (neighbors.isEmpty()) {
            throw new IllegalStateException("Expected non-empty neighbor candidates");
        }
        if (pointScore <= 0) {
            throw new IllegalStateException("Expected positive point score, got " + pointScore);
        }

        System.out.println("roads=" + roads);
        System.out.println("neighborCandidates=" + neighbors.size());
        System.out.println("pointScore=" + pointScore);
        System.out.println("smoke=PASS");
    }
}
