import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PolarDFS {

    // Inner class to represent a node in the graph
    static class Node {
        PolarGrid.PolarPoint point;  // The point in the polar grid this node represents
        Node parent;  // The node we came from to get here
        double pathCost;  // The cost of the path from the start node to this node
        int nodesVisited;  // The number of nodes visited to get to this node

        // Constructor for the Node class
        public Node(PolarGrid.PolarPoint point, Node parent, double pathCost, int nodesVisited) {
            this.point = point;
            this.parent = parent;
            this.pathCost = pathCost;
            this.nodesVisited = nodesVisited;
        }

        // Method to convert the node to a simple string representation
        public String toSimpleString() {
            return "(" + point.distance + ":" + point.angle + ")";
        }
    }

    // Method to perform a depth-first search (DFS) on the polar grid
    public static DFSResult dfs(PolarGrid grid) {
        long startTime = System.currentTimeMillis();  // Start timing the execution
        Deque<Node> stack = new ArrayDeque<>();  // Use a stack for DFS
        Set<PolarGrid.PolarPoint> explored = new HashSet<>();  // Set to keep track of explored nodes
        List<String> frontierStates = new ArrayList<>();  // List to keep track of the frontier states
        Map<PolarGrid.PolarPoint, Node> cameFrom = new HashMap<>();  // Map to keep track of where each node came from
        AtomicInteger nodesVisited = new AtomicInteger(0);  // Atomic integer to keep track of the number of nodes visited
        int maxFrontierSize = 0;  // Variable to track the maximum size of the frontier

        // Create the start node and add it to the stack and explored set
        Node startNode = new Node(grid.getStart(), null, 0.0, 0);
        stack.push(startNode);
        explored.add(grid.getStart());
        frontierStates.add(stackToString(stack));

        // Main loop for the DFS
        while (!stack.isEmpty()) {
            Node current = stack.pop();  // Pop the current node from the stack
            nodesVisited.incrementAndGet();  // Increment the number of nodes visited
            maxFrontierSize = Math.max(maxFrontierSize, stack.size());  // Update the maximum frontier size

            // If the current node is the goal, construct the result and return it
            PolarGrid.PolarPoint currentPoint = current.point;
            if (currentPoint.equals(grid.getGoal())) {
                long endTime = System.currentTimeMillis();  // End timing the execution
                return constructResult(current, cameFrom, grid, frontierStates, nodesVisited.get(), endTime - startTime, maxFrontierSize);
            }

            // Expand the current node
            expandNode(current, stack, explored, cameFrom, grid, frontierStates);
        }

        // If the stack is empty and we haven't found the goal, return a failure result
        long endTime = System.currentTimeMillis();  // End timing the execution
        return new DFSResult(new Result("fail", 0.0, nodesVisited.get()), frontierStates, explored, endTime - startTime, maxFrontierSize);
    }

    // Method to expand a node
    private static void expandNode(Node current, Deque<Node> stack, Set<PolarGrid.PolarPoint> explored,
                                   Map<PolarGrid.PolarPoint, Node> cameFrom, PolarGrid grid, List<String> frontierStates) {
        // Get the neighbors of the current node and sort them
        List<PolarGrid.PolarPoint> neighbors = grid.getNeighbors(current.point);
        neighbors.sort(Comparator.comparingInt(PolarGrid.PolarPoint::getDistance).thenComparingInt(PolarGrid.PolarPoint::getAngle));
        Collections.reverse(neighbors);  // Reverse to maintain correct order when pushing to stack

        // For each neighbor, if it hasn't been explored yet, add it to the stack and explored set
        for (PolarGrid.PolarPoint neighbor : neighbors) {
            if (!explored.contains(neighbor) && cameFrom.putIfAbsent(neighbor, current) == null) {
                explored.add(neighbor);
                stack.push(new Node(neighbor, current, current.pathCost + grid.cost(current.point, neighbor), 0));
            }
        }
        // If the stack is not empty, add its string representation to the frontier states
        if (!stack.isEmpty()) {
            frontierStates.add(stackToString(stack));
        }
    }

    // Method to construct the result of the DFS
    private static DFSResult constructResult(Node goalNode, Map<PolarGrid.PolarPoint, Node> cameFrom, PolarGrid grid,
                                             List<String> frontierStates, int nodesVisited, long executionTime, int maxFrontierSize) {
        LinkedList<PolarGrid.PolarPoint> path = new LinkedList<>();  // List to store the path from the start to the goal
        Node step = goalNode;  // Start with the goal node
        double totalCost = 0;  // Variable to store the total cost of the path

        // Construct the path by stepping back from the goal to the start
        while (step != null) {
            path.addFirst(step.point);  // Add the current node to the front of the path
            Node nextNode = cameFrom.get(step.point);  // Get the node we came from
            if (nextNode != null) {
                totalCost += step.pathCost - nextNode.pathCost;  // Update the total cost
            }
            step = nextNode;  // Move to the next node
        }

        // Return the result of the DFS
        return new DFSResult(new Result(pathToString(path), totalCost, nodesVisited), frontierStates, cameFrom.keySet(), executionTime, maxFrontierSize);
    }

       // Method to convert a stack of nodes to a string representation
       private static String stackToString(Deque<Node> stack) {
        // Use Java 8 streams to map each node to its simple string representation,
        // then collect them into a string, separated by commas and enclosed in brackets
        return stack.stream()
                .map(Node::toSimpleString)
                .collect(Collectors.joining(",", "[", "]"));
    }

    // Inner class to represent the result of a search
    static class Result {
        String path;  // The path from the start to the goal
        double cost;  // The cost of the path
        int nodesVisited;  // The number of nodes visited during the search

        // Constructor for the Result class
        public Result(String path, double cost, int nodesVisited) {
            this.path = path;
            this.cost = cost;
            this.nodesVisited = nodesVisited;
        }

        // Method to convert the result to a string representation
        @Override
        public String toString() {
            // The string representation includes the path, cost (formatted to 3 decimal places), and number of nodes visited
            return path + "\n" + String.format("%.3f", cost) + "\n" + nodesVisited;
        }
    }

    // Inner class to represent the result of a depth-first search (DFS)
    static class DFSResult {
        Result result;  // The result of the search
        List<String> frontierStates;  // The states of the frontier during the search
        Set<PolarGrid.PolarPoint> explored;  // The points that were explored during the search
        long executionTime;  // The time it took to execute the search
        int maxFrontierSize;  // The maximum size of the frontier during the search

        // Constructor for the DFSResult class
        public DFSResult(Result result, List<String> frontierStates, Set<PolarGrid.PolarPoint> explored, long executionTime, int maxFrontierSize) {
            this.result = result;
            this.frontierStates = frontierStates;
            this.explored = explored;
            this.executionTime = executionTime;
            this.maxFrontierSize = maxFrontierSize;
        }
    }

    // Method to convert a path to a string representation
    private static String pathToString(List<PolarGrid.PolarPoint> path) {
        StringBuilder sb = new StringBuilder();  // Use a StringBuilder for efficiency
        // For each point in the path, append its simple string representation to the StringBuilder
        for (PolarGrid.PolarPoint p : path) {
            if (sb.length() > 0) sb.append("");  // If this is not the first point, append a space before it
            sb.append(p.toSimpleString());
        }
        // Convert the StringBuilder to a string and return it
        return sb.toString();
    }
}