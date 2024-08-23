import java.util.*;
import java.util.stream.Collectors;

public class PolarAStarSearch {
    // Node class for A* search
    static class Node {
        PolarGrid.PolarPoint point;  // The point in the grid this node represents
        Node parent;  // The node we came from
        double pathCost;  // g(n): Cost from start to this node
        double heuristicCost;  // h(n): Estimated cost from this node to the goal

        // Node constructor
        public Node(PolarGrid.PolarPoint point, Node parent, double pathCost, double heuristicCost) {
            this.point = point;
            this.parent = parent;
            this.pathCost = pathCost;
            this.heuristicCost = heuristicCost;
        }

        // Total cost based on heuristic and path cost
        public double totalCost() {
            return this.pathCost + this.heuristicCost;  // f(n) = g(n) + h(n)
        }
        
        // Convert the node to a simple string representation
        public String toSimpleString() {
            return String.format("(%d:%d)%.3f", point.distance, point.angle, totalCost());
        }
    }

    // Class to hold the results of the search
    public static class SearchResults {
        String path;  // The path found
        double cost;  // The cost of the path
        int nodesVisited;  // The number of nodes visited during the search
        List<String> frontierStates;  // The states of the frontier during the search

        // SearchResults constructor
        public SearchResults(String path, double cost, int nodesVisited, List<String> frontierStates) {
            this.path = path;
            this.cost = cost;
            this.nodesVisited = nodesVisited;
            this.frontierStates = frontierStates;
        }

        // Convert the search results to a string
        @Override
        public String toString() {
            return "Path: " + path + "\nCost: " + String.format("%.3f", cost) + "\nNodes Visited: " + nodesVisited + "\nFrontier States: " + frontierStates;
        }
    }
    
    // A* search algorithm
    public static SearchResults aStarSearch(PolarGrid grid) {
        // Comparator for sorting nodes by total cost
        Comparator<Node> byTotalCost = Comparator.comparingDouble(Node::totalCost);
        // The frontier of nodes to be explored, sorted by total cost
        TreeSet<Node> frontier = new TreeSet<>(byTotalCost);
        // Map from points to nodes for quick lookup
        Map<PolarGrid.PolarPoint, Node> pointToNode = new HashMap<>();
        // Set of points that have been explored
        Set<PolarGrid.PolarPoint> explored = new HashSet<>();
        // List of frontier states for recording the search
        List<String> frontierStates = new ArrayList<>();
        // Count of nodes visited
        int nodesVisited = 0;

        // Create the start node and add it to the frontier
        Node startNode = new Node(grid.getStart(), null, 0, heuristic(grid.getStart(), grid.getGoal()));
        frontier.add(startNode);
        pointToNode.put(startNode.point, startNode);
        frontierStates.add(frontierToString(frontier));

        // Main search loop
        while (!frontier.isEmpty()) {
            // Get the node with the lowest total cost
            Node current = frontier.pollFirst();
            // Mark the current node as explored
            explored.add(current.point);
            pointToNode.remove(current.point);
            nodesVisited++;

            // Check if the current node is the goal
            if (grid.getGoal().equals(current.point)) {
                // If it is, construct the path and return the search results
                return new SearchResults(constructPath(current), current.pathCost, nodesVisited, frontierStates);
            }

            // Explore the neighbors of the current node
            for (PolarGrid.PolarPoint neighbor : grid.getNeighbors(current.point)) {
                // Calculate the cost of the path through the current node to the neighbor
                double newPathCost = current.pathCost + grid.cost(current.point, neighbor);
                // Check if we've already created a node for this point
                Node existingNode = pointToNode.get(neighbor);

                // If the neighbor hasn't been explored yet and either it's new or we found a shorter path to it
                if (!explored.contains(neighbor) && (existingNode == null || newPathCost < existingNode.pathCost)) {
                    // Create a new node for the neighbor
                    Node newNode = new Node(neighbor, current, newPathCost, heuristic(neighbor, grid.getGoal()));
                    // If there was an existing node, remove it from the frontier
                    if (existingNode != null) {
                        frontier.remove(existingNode);
                        pointToNode.remove(neighbor);
                    }
                    // Add the new node to the frontier
                    frontier.add(newNode);
                    pointToNode.put(neighbor, newNode);
                }
            }
            // Record the state of the frontier
            frontierStates.add(frontierToString(frontier));
        }

        // If we've exhausted the frontier without finding the goal, the search failed
        return new SearchResults("fail", 0, nodesVisited, frontierStates);
    }

    // Check if the frontier contains a point
    private static boolean containsPoint(TreeSet<Node> frontier, PolarGrid.PolarPoint point) {
        return frontier.stream().anyMatch(node -> node.point.equals(point));
    }

    // Heuristic function for A* search
    private static double heuristic(PolarGrid.PolarPoint point, PolarGrid.PolarPoint goal) {
        // Calculate the Euclidean distance between the point and the goal in polar coordinates
        double dA = point.distance; // radial distance of point from origin
        double dB = goal.distance; // radial distance of goal from origin
        double thetaA = Math.toRadians(point.angle); // converting angle of point to radians
        double thetaB = Math.toRadians(goal.angle); // converting angle of goal to radians

        // Apply the Euclidean distance formula in polar coordinates
        double distance = Math.sqrt(dA * dA + dB * dB - 2 * dA * dB * Math.cos(thetaB - thetaA));
        return distance;
    }
    
    // Construct a path from the start to a given node
    private static String constructPath(Node node) {
        // Create a list to hold the path
        LinkedList<PolarGrid.PolarPoint> path = new LinkedList<>();
        Node current = node;
        // Go from the node to the start, adding each point to the front of the path
        while (current != null) {
            path.addFirst(current.point);
            current = current.parent;
        }
        // Convert the path to a string
        return path.stream()
                   .map(PolarGrid.PolarPoint::toSimpleString)
                   .collect(Collectors.joining(""));
    }
    
    // Convert the frontier to a string
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

        // Perform the search and print the results
        SearchResults results = aStarSearch(grid);
        System.out.println(results);
    }
}