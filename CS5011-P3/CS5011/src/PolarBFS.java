import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PolarBFS {

    // Inner class to represent a node in the search tree
    static class Node {
        PolarGrid.PolarPoint point; // The point this node represents
        Node parent; // The node we came from
        double pathCost; // The cost to reach this node
        int nodesVisited; // The number of nodes visited to reach this node

        public Node(PolarGrid.PolarPoint point, Node parent, double pathCost, int nodesVisited) {
            this.point = point;
            this.parent = parent;
            this.pathCost = pathCost;
            this.nodesVisited = nodesVisited;
        }

        // Convert the node to a simple string representation
        public String toSimpleString() {
            return "(" + point.distance + ":" + point.angle + ")";
        }
    }

    // Perform a breadth-first search on the given grid
    public static BFSResult bfs(PolarGrid grid) {
        long startTime = System.currentTimeMillis(); // Start timing the execution
        Queue<Node> frontier = new LinkedList<>(); // The frontier of nodes to explore
        Set<PolarGrid.PolarPoint> explored = new HashSet<>(); // The set of points we've already explored
        List<String> frontierStates = new ArrayList<>(); // The list of frontier states for debugging
        Map<PolarGrid.PolarPoint, Node> cameFrom = new HashMap<>(); // The map of where each point came from
        AtomicInteger nodesVisited = new AtomicInteger(0); // The number of nodes visited
        int maxFrontierSize = 0; // The maximum size of the frontier

        // Initialize the start node and add it to the frontier
        Node startNode = new Node(grid.getStart(), null, 0.0, 0);
        frontier.add(startNode);
        explored.add(grid.getStart());
        frontierStates.add(frontierToString(frontier));
        maxFrontierSize = frontier.size(); // Initialize max size tracking

        // While there are still nodes to explore
        while (!frontier.isEmpty()) {
            // Update the maximum frontier size
            if (frontier.size() > maxFrontierSize) {
                maxFrontierSize = frontier.size();
            }

            // Get the next node to explore
            Node current = frontier.poll();
            nodesVisited.incrementAndGet();  // Increment as we process each node
            PolarGrid.PolarPoint currentPoint = current.point;

            // If we've reached the goal, construct the result and return it
            if (currentPoint.equals(grid.getGoal())) {
                long endTime = System.currentTimeMillis(); // End timing
                return constructResult(current, cameFrom, grid, frontierStates, nodesVisited.get(), maxFrontierSize, endTime - startTime);
            }

            // Expand the current node
            expandNode(current, frontier, explored, cameFrom, grid, frontierStates);
        }

        // If we've exhausted the frontier without finding the goal, return a failure result
        long endTime = System.currentTimeMillis(); // End timing
        return new BFSResult(new Result("fail", 0.0, nodesVisited.get(), maxFrontierSize, endTime - startTime), frontierStates, explored);
    }

    // Expand a node by adding all its neighbours to the frontier
    private static void expandNode(Node current, Queue<Node> frontier, Set<PolarGrid.PolarPoint> explored,
                                   Map<PolarGrid.PolarPoint, Node> cameFrom, PolarGrid grid, List<String> frontierStates) {
        // Get the neighbours of the current point
        List<PolarGrid.PolarPoint> neighbors = grid.getNeighbors(current.point);
        // Sort the neighbours by distance and angle
        neighbors.sort(Comparator.comparingInt(PolarGrid.PolarPoint::getDistance).thenComparingInt(PolarGrid.PolarPoint::getAngle));

        boolean added = false;
        for (PolarGrid.PolarPoint neighbor : neighbors) {
            // If we haven't explored this neighbour yet
            if (!explored.contains(neighbor) && cameFrom.putIfAbsent(neighbor, current) == null) {
                // Add it to the explored set and the frontier
                explored.add(neighbor);
                frontier.add(new Node(neighbor, current, current.pathCost + grid.cost(current.point, neighbor), 0));
                added = true;
            }
        }

        // If we added any nodes to the frontier, add the current frontier state to the list
        if (added || !frontier.isEmpty()) {
            frontierStates.add(frontierToString(frontier));
        }
    }

    // Construct a result from the goal node and the other parameters
    private static BFSResult constructResult(Node goalNode, Map<PolarGrid.PolarPoint, Node> cameFrom, PolarGrid grid,
                                             List<String> frontierStates, int nodesVisited, int maxFrontierSize, long executionTime) {
        if (goalNode == null) {
            return new BFSResult(new Result("fail", 0.0, nodesVisited, maxFrontierSize, executionTime), frontierStates, cameFrom.keySet());
        }

        // Construct the path from the goal to the start
        LinkedList<PolarGrid.PolarPoint> path = new LinkedList<>();
        Node step = goalNode;
        double totalCost = 0;

        while (step != null) {
            path.addFirst(step.point);
            Node nextNode = cameFrom.get(step.point);
            if (nextNode != null) {
                totalCost += step.pathCost - nextNode.pathCost;
            }
            step = nextNode;
        }

        // Return the result
        return new BFSResult(new Result(pathToString(path), totalCost, nodesVisited, maxFrontierSize, executionTime), frontierStates, cameFrom.keySet());
    }

    // Convert the frontier to a string representation
    private static String frontierToString(Queue<Node> frontier) {
        return frontier.stream()
                .map(Node::toSimpleString)
                .collect(Collectors.joining(",", "[", "]"));
    }

    // Inner class to represent a result of the search
    static class Result {
        String path; // The path from the start to the goal
        double cost; // The cost of the path
        int nodesVisited; // The number of nodes visited
        int maxFrontierSize; // The maximum size of the frontier
        long executionTime; // The time it took to perform the search

        public Result(String path, double cost, int nodesVisited, int maxFrontierSize, long executionTime) {
            this.path = path;
            this.cost = cost;
            this.nodesVisited = nodesVisited;
            this.maxFrontierSize = maxFrontierSize;
            this.executionTime = executionTime;
        }

        // Convert the result to a string representation
        @Override
        public String toString() {
            return path + "\n" + String.format("%.3f", cost) + "\n" + nodesVisited + "\n" + "Max Frontier Size: " + maxFrontierSize + "\n" + "Execution Time: " + executionTime + "ms";
        }
    }

    // Inner class to represent a result of the BFS
    static class BFSResult {
        Result result; // The result of the search
        List<String> frontierStates; // The list of frontier states for debugging
        Set<PolarGrid.PolarPoint> explored; // The set of points that were explored

        public BFSResult(Result result, List<String> frontierStates, Set<PolarGrid.PolarPoint> explored) {
            this.result = result;
            this.frontierStates = frontierStates;
            this.explored = explored;
        }
    }

    // Convert the path list to the required string format
    private static String pathToString(List<PolarGrid.PolarPoint> path) {
        StringBuilder sb = new StringBuilder();
        for (PolarGrid.PolarPoint p : path) {
            if (sb.length() > 0) sb.append("");
            sb.append(p.toSimpleString());
        }
        return sb.toString();
    }
}