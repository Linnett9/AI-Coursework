import java.util.*;
import java.util.stream.Collectors;

public class PolarSMAStarSearch {
    static int maxMemory; // Define maxMemory 

    static class Node {
        PolarGrid.PolarPoint point;
        Node parent;
        double pathCost;
        double heuristicCost;
        int depth;
        boolean isLeaf;
        List<Node> forgottenChildren;

        public Node(PolarGrid.PolarPoint point, Node parent, double pathCost, double heuristicCost, int depth) {
            this.point = point;
            this.parent = parent;
            this.pathCost = pathCost;
            this.heuristicCost = heuristicCost;
            this.depth = depth;
            this.isLeaf = true;
            this.forgottenChildren = new ArrayList<>();
            // Check and apply high heuristic cost if depth exceeds maxMemory within Node's constructor or any method
            if (this.depth > PolarSMAStarSearch.maxMemory) {
                this.heuristicCost = 10000.000; // Set high cost directly 
            }
        }

        public double totalCost() {
            return this.pathCost + this.heuristicCost;
        }

        public String toSimpleString() {
            return String.format("(%d:%d)%.3f", point.distance, point.angle, totalCost());
        }
    }

    public static class SearchResults {
        String path;
        double cost;
        int nodesVisited;
        List<String> frontierStates;

        public SearchResults(String path, double cost, int nodesVisited, List<String> frontierStates) {
            this.path = path;
            this.cost = cost;
            this.nodesVisited = nodesVisited;
            this.frontierStates = frontierStates;
        }

        @Override
        public String toString() {
            return "Path: " + path + "\nCost: " + String.format("%.3f", cost) + "\nNodes Visited: " + nodesVisited + "\nFrontier States: " + frontierStates;
        }
    }

    public static SearchResults smaStarSearch(PolarGrid grid, int memoryLimit) {
        maxMemory = memoryLimit; // Set maxMemory based on passed parameter
        Comparator<Node> byTotalCost = Comparator.comparingDouble(Node::totalCost);
        TreeSet<Node> frontier = new TreeSet<>(byTotalCost);
        int nodesVisited = 0;

        Node startNode = new Node(grid.getStart(), null, 0, heuristic(grid.getStart(), grid.getGoal()), 0);
        frontier.add(startNode);
        List<String> frontierStates = new ArrayList<>();
        frontierStates.add(frontierToString(frontier));

        while (!frontier.isEmpty()) {
            if (frontier.size() > maxMemory) {
                removeWorstLeaf(frontier);
            }

            Node current = frontier.pollFirst();
            nodesVisited++;

            if (grid.getGoal().equals(current.point)) {
                return new SearchResults(constructPath(current), current.pathCost, nodesVisited, frontierStates);
            }

            expand(current, frontier, grid);
            updateLeafStatus(current, frontier);
            frontierStates.add(frontierToString(frontier));
        }

        return new SearchResults("fail", 0, nodesVisited, frontierStates);
    }

    private static void expand(Node node, TreeSet<Node> frontier, PolarGrid grid) {
        for (PolarGrid.PolarPoint neighbor : grid.getNeighbors(node.point)) {
            double newPathCost = node.pathCost + grid.cost(node.point, neighbor);
            int newDepth = node.depth + 1;
            double newHeuristicCost = heuristic(neighbor, grid.getGoal());
            if (newDepth > maxMemory) {
                newHeuristicCost = 10000.000; // Apply high heuristic cost if new depth exceeds maxMemory
            }
            Node newNode = new Node(neighbor, node, newPathCost, newHeuristicCost, newDepth);
            newNode.isLeaf = true;
            frontier.add(newNode);
            node.isLeaf = false;
        }
    }

    private static void removeWorstLeaf(TreeSet<Node> frontier) {
        Node worst = frontier.stream()
                             .filter(n -> n.isLeaf && n.depth >= maxMemory) 
                             .max(Comparator.comparingDouble(Node::totalCost))
                             .orElse(null);
        if (worst != null) {
            frontier.remove(worst);
        }
    }

    private static void updateLeafStatus(Node node, TreeSet<Node> frontier) {
        node.isLeaf = frontier.stream().noneMatch(n -> n.parent == node);
    }

    private static double heuristic(PolarGrid.PolarPoint point, PolarGrid.PolarPoint goal) {
        double dA = point.distance;
        double dB = goal.distance;
        double thetaA = Math.toRadians(point.angle);
        double thetaB = Math.toRadians(goal.angle);
        return Math.sqrt(dA * dA + dB * dB - 2 * dA * dB * Math.cos(thetaB - thetaA));
    }

    private static String constructPath(Node node) {
        LinkedList<PolarGrid.PolarPoint> path = new LinkedList<>();
        Node current = node;
        while (current != null) {
            path.addFirst(current.point);
            current = current.parent;
        }
        return path.stream()
                   .map(PolarGrid.PolarPoint::toSimpleString)
                   .collect(Collectors.joining(""));
    }

    private static String frontierToString(TreeSet<Node> frontier) {
        return frontier.stream()
                       .map(node -> String.format("(%d:%d)%.3f", node.point.distance, node.point.angle, node.totalCost()))
                       .collect(Collectors.joining(",", "[", "]"));
    }
}