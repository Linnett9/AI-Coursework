import java.util.*;
import java.util.stream.Collectors;

public class PolarBestFirstSearch {
    // Inner class representing a node in the search tree
    static class Node {
        PolarGrid.PolarPoint point;  // The point in the grid this node represents
        Node parent;  // The node we came from
        double pathCost;  // The cost to reach this node
        double heuristicCost;  // The estimated cost to reach the goal from this node
        
        // Node constructor
        public Node(PolarGrid.PolarPoint point, Node parent, double pathCost, double heuristicCost) {
            this.point = point;
            this.parent = parent;
            this.pathCost = pathCost;
            this.heuristicCost = heuristicCost;
        }

        // Method to get the total cost of a node
        public double totalCost() {
            return this.heuristicCost;
        }
        
        // Method to get a simple string representation of a node
        public String toSimpleString() {
            return String.format("(%d:%d)%.3f", point.distance, point.angle, this.heuristicCost);
        }
    }

    // Inner class representing the results of a search
    public static class SearchResults {
        String path;  // The path taken
        double cost;  // The total cost of the path
        int nodesVisited;  // The number of nodes visited during the search
        List<String> frontierStates;  // The states of the frontier during the search

        // SearchResults constructor
        public SearchResults(String path, double cost, int nodesVisited, List<String> frontierStates) {
            this.path = path;
            this.cost = cost;
            this.nodesVisited = nodesVisited;
            this.frontierStates = frontierStates;
        }

        // Method to get a string representation of the search results
        @Override
        public String toString() {
            return "Path: " + path + "\nCost: " + String.format("%.3f", cost) + "\nNodes Visited: " + nodesVisited + "\nFrontier States: " + frontierStates;
        }
    }
    
    // Method to perform a best-first search on a grid
    public static SearchResults bestFirstSearch(PolarGrid grid) {
        // Comparator for sorting nodes by heuristic cost
        Comparator<Node> byHeuristic = Comparator.comparingDouble(Node::totalCost);
        // The frontier of the search, sorted by heuristic cost
        TreeSet<Node> frontier = new TreeSet<>(byHeuristic);
        // The set of points that have been explored
        Set<PolarGrid.PolarPoint> explored = new HashSet<>();
        // The list of frontier states during the search
        List<String> frontierStates = new ArrayList<>();
        // The number of nodes visited during the search
        int nodesVisited = 0;
    
        // Create the start node and add it to the frontier
        Node startNode = new Node(grid.getStart(), null, 0, heuristic(grid.getStart(), grid.getGoal()));
        frontier.add(startNode);
        // Log the initial state of the frontier
        frontierStates.add(frontierToString(frontier)); 
    
        // While there are nodes in the frontier
        while (!frontier.isEmpty()) {
            // Get the node with the lowest heuristic cost
            Node current = frontier.pollFirst();
            nodesVisited++;
            // Mark the current point as explored
            explored.add(current.point);
    
            // If the current point is the goal
            if (grid.getGoal().equals(current.point)) {
                // Return the results of the search
                return new SearchResults(constructPath(current), current.pathCost, nodesVisited, frontierStates);
            }
    
            // For each neighbor of the current point
            for (PolarGrid.PolarPoint neighbor : grid.getNeighbors(current.point)) {
                // If the neighbor has not been explored and is not in the frontier
                if (!explored.contains(neighbor) && !containsPoint(frontier, neighbor)) {
                    // Create a new node for the neighbor and add it to the frontier
                    Node nextNode = new Node(neighbor, current, current.pathCost + grid.cost(current.point, neighbor),
                                             heuristic(neighbor, grid.getGoal()));
                    frontier.add(nextNode);
                }
            }
            // Log the state of the frontier after expanding the current node
            frontierStates.add(frontierToString(frontier)); 
        }
        
        // If no path was found, return a failure result
        return new SearchResults("fail", 0, nodesVisited, frontierStates);
    }
    
    // Method to check if a point is in the frontier
    private static boolean containsPoint(TreeSet<Node> frontier, PolarGrid.PolarPoint point) {
        return frontier.stream().anyMatch(node -> node.point.equals(point));
    }

    // Method to calculate the heuristic cost of a point
    private static double heuristic(PolarGrid.PolarPoint point, PolarGrid.PolarPoint goal) {
        // Calculate the radial distances and angles of the point and goal
        double dA = point.distance; 
        double dB = goal.distance; 
        double thetaA = Math.toRadians(point.angle); 
        double thetaB = Math.toRadians(goal.angle); 
    
        // Calculate the Euclidean distance between the point and goal in polar coordinates
        double distance = Math.sqrt(dA * dA + dB * dB - 2 * dA * dB * Math.cos(thetaB - thetaA));
        return distance;
    }
    
    // Method to construct a path from a node to the start node
    private static String constructPath(Node node) {
        LinkedList<PolarGrid.PolarPoint> path = new LinkedList<>();
        Node current = node;
        // While there is a node
        while (current != null) {
            // Add the node's point to the start of the path
            path.addFirst(current.point);
            // Move to the parent node
            current = current.parent;
        }
        // Convert the path to a string
        return path.stream()
                   .map(PolarGrid.PolarPoint::toSimpleString)
                   .collect(Collectors.joining(""));  
    }
    
    // Method to get a string representation of the frontier
    private static String frontierToString(TreeSet<Node> frontier) {
        return frontier.stream()
                       .map(Node::toSimpleString)
                       .collect(Collectors.joining(",", "[", "]"));
    }

    // Main method for testing
    public static void main(String[] args) {
        // Create a grid and perform a search on it
        PolarGrid.PolarPoint start = new PolarGrid.PolarPoint(0, 0);
        PolarGrid.PolarPoint goal = new PolarGrid.PolarPoint(5, 90);
        PolarGrid grid = new PolarGrid(10, start, goal);
        
        SearchResults results = bestFirstSearch(grid);
        // Print the results of the search
        System.out.println(results);
    }
}