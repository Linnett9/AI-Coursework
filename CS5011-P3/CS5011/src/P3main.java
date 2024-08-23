import java.util.List;

public class P3main {

    // Accepts test arguments
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: java P3Main <DFS|BFS|AStar|BestF|SMAStar|...> <N> <ds:as> <dg:ag> [<param>]");
            System.exit(1);
        }

        // Print initial information --- please do not delete
        System.out.println("World: Oedipus " + args[1]);
        System.out.println("Departure airport -- Start: " + args[2]);
        System.out.println("Destination airport -- Goal: " + args[3]);
        System.out.println("Search algorithm: " + args[0]);
        System.out.println();

        // Run the search algorithm
        runSearch(args[0], Integer.parseInt(args[1]), args[2], args[3]);
    }

    private static void runSearch(String algo, int size, String start, String goal) {
        PolarGrid.PolarPoint startPoint = parsePoint(start);
        PolarGrid.PolarPoint goalPoint = parsePoint(goal);
        PolarGrid grid = new PolarGrid(size, startPoint, goalPoint);

        switch (algo) {
            case "BFS":
                PolarBFS.BFSResult bfsResult = PolarBFS.bfs(grid);
               bfsResult.frontierStates.forEach(System.out::println);
               if (bfsResult.result.path.equals("fail")) {
                   System.out.println("fail");
                   System.out.println(bfsResult.result.nodesVisited);
               } else {
                   System.out.println(bfsResult.result.path);
                   System.out.println(String.format("%.3f", bfsResult.result.cost));
                   System.out.println(bfsResult.result.nodesVisited);
               }
               break;
               case "DFS":
               PolarDFS.DFSResult dfsResult = PolarDFS.dfs(grid);
               dfsResult.frontierStates.forEach(System.out::println);
               if ("fail".equals(dfsResult.result.path)) {
                   System.out.println("fail");
                   System.out.println(dfsResult.result.nodesVisited);
               } else {
                   System.out.println(dfsResult.result.path);
                   System.out.println(String.format("%.3f", dfsResult.result.cost));
                   System.out.println(dfsResult.result.nodesVisited);
               }
               break;
               case "BestF":
    PolarBestFirstSearch.SearchResults bestFResult = PolarBestFirstSearch.bestFirstSearch(grid);
    bestFResult.frontierStates.forEach(System.out::println);
    if ("fail".equals(bestFResult.path)) {
        System.out.println("fail");
        System.out.println(bestFResult.nodesVisited);
    } else {
        System.out.println(bestFResult.path);
        System.out.println(String.format("%.3f", bestFResult.cost));
        System.out.println(bestFResult.nodesVisited);
    }
    break;

    case "AStar":
    PolarAStarSearch.SearchResults aStarResult = PolarAStarSearch.aStarSearch(grid);
    aStarResult.frontierStates.forEach(System.out::println);
    if ("fail".equals(aStarResult.path)) {
        System.out.println("fail");
        System.out.println(aStarResult.nodesVisited);
    } else {
        System.out.println(aStarResult.path);
        System.out.println(String.format("%.3f", aStarResult.cost));
        System.out.println(aStarResult.nodesVisited);
    }
    break;
    case "SMAStar":
    PolarSMAStarSearch.SearchResults smaStarResult = PolarSMAStarSearch.smaStarSearch(grid, grid.getSize());
    smaStarResult.frontierStates.forEach(System.out::println);
    if ("fail".equals(smaStarResult.path) || smaStarResult.cost >= 10000.000) {
        System.out.println("fail");
        System.out.println(smaStarResult.nodesVisited);
    } else {
        System.out.println(smaStarResult.path);
        System.out.println(String.format("%.3f", smaStarResult.cost));
        System.out.println(smaStarResult.nodesVisited);
    }
    break;


            default:
                System.out.println("Algorithm not implemented");
                break;
        }
    }

    // Method to parse a point string in the format "(radius:angle)" to a PolarPoint
    private static PolarGrid.PolarPoint parsePoint(String pointString) {
        String[] parts = pointString.replace("(", "").replace(")", "").split(":");
        int radius = Integer.parseInt(parts[0]);
        int angle = Integer.parseInt(parts[1]);
        return new PolarGrid.PolarPoint(radius, angle);
    }
}