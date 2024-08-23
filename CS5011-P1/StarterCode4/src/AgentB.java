import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Represents AgentB, capable of applying strategies to solvethe state of a Mosaic game.
 */

public class AgentB {
    private Game game;

    public AgentB(Game game) {
        this.game = game;
    }
// HOW TO MAKE A MOVE EXAMPLE
  /*   public void makeMove() {
        // Example: Change the state of a specific cell
        // For demonstration, let's change the state of cell at (0, 0) to PAINTED
        int row = 0; // Row of the cell to change
        int col = 0; // Column of the cell to change
    
        System.out.println("Before move, state of cell at (" + row + ", " + col + "): " + game.state[row][col]);
    
        // Check if the cell is covered, then paint it
        if (game.state[row][col] == Game.COVERED) {
            game.state[row][col] = Game.PAINTED;
            System.out.println("Cell at (" + row + ", " + col + ") painted.");
        }
    
        System.out.println("After move, state of cell at (" + row + ", " + col + "): " + game.state[row][col]);
    
    }


    /* */
    // Method to display the current state of the game
    public void printCurrentGameState() {
        game.printBoard();
    }

// Applies direct clue application method, eg if clue of 4 has only 4 neighbours including itself, all should be painted. 

    public boolean directClueApplication() {
        boolean changesMade = false;
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.board[row][col] > -1) {
                    int clue = game.board[row][col];
                    List<int[]> neighbors = game.getNeighborsIncludingSelf(row, col);
                    int coveredCount = 0;
                    int paintedCount = 0;
                    for (int[] neighbor : neighbors) {
                        if (game.state[neighbor[0]][neighbor[1]] == Game.COVERED) {
                            coveredCount++;
                        } else if (game.state[neighbor[0]][neighbor[1]] == Game.PAINTED) {
                            paintedCount++;
                        }
                    }
                    if (clue == coveredCount + paintedCount) {
                        for (int[] neighbor : neighbors) {
                            if (game.state[neighbor[0]][neighbor[1]] == Game.COVERED) {
                                game.state[neighbor[0]][neighbor[1]] = Game.PAINTED;
                                changesMade = true;
                            //    System.out.println(" DCA: Painted cell at (" + neighbor[0] + ", " + neighbor[1] + ")");
                            }
                        }
                    }
                }
            }
        }
        return changesMade;
    }
    
    // Applies completion strategy - clears cells when clue is satisfied. 

    public boolean completionStrategy() {
        boolean changesMade = false;
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.board[row][col] > -1) {
                    int clue = game.board[row][col];
                    int paintedCount = game.countPaintedNeighbors(row, col);

                    // If the number of painted neighbors matches the clue, clear the covered neighbors
                    if (paintedCount == clue) {
                        List<int[]> neighbors = game.getNeighborsIncludingSelf(row, col);
                        for (int[] neighbor : neighbors) {
                            if (game.state[neighbor[0]][neighbor[1]] == Game.COVERED) {
                                game.state[neighbor[0]][neighbor[1]] = Game.CLEARED;
                                changesMade = true;
                             //   System.out.println("CS :Cleared cell at (" + neighbor[0] + ", " + neighbor[1] + ")");
                            }
                        }
                    }
                }
            }
        }
        return changesMade;
    }

// Applies contradiction avoidance - Ensures no moves would create a contradiction of the games rules. 

    public boolean contradictionAvoidance() {
        boolean changesMade = false;
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.state[row][col] == Game.COVERED) {
                    List<int[]> neighbors = game.getNeighborsIncludingSelf(row, col); 
                    boolean canPaint = true;
    
                    // Check all neighbors with clues to see if we can paint this cell
                    for (int[] neighbor : neighbors) {
                        int clue = game.board[neighbor[0]][neighbor[1]];
                        int paintedNeighbors = game.countPaintedNeighbors(neighbor[0], neighbor[1]);
                        int coveredNeighbors = game.countCoveredNeighbors(neighbor[0], neighbor[1]);
    
                        // If painting this cell would satisfy the neighbor's clue
                        if (clue == paintedNeighbors + 1) {
                            continue;
                        }
                        // If painting this cell would cause the neighbor's clue to be unsatisfiable
                        else if (clue < paintedNeighbors + 1 || clue > paintedNeighbors + coveredNeighbors) {
                            canPaint = false;
                            break;
                        }
                    }
    
                    if (canPaint) {
                        game.state[row][col] = Game.PAINTED;
                        changesMade = true;
                    //    System.out.println(" CA: Painted cell at (" + row + ", " + col + ")");
                    }
                }
            }
        }
        return changesMade;
    }
    
   // Validates the board against the clues 

    public boolean validateFinalBoard() {
       for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.board[row][col] > -1) { // Cell has a clue
                    int clue = game.board[row][col];
                    int paintedCount = game.countPaintedNeighbors(row, col);
                    // Check if painted count satisfies the clue
                    if (paintedCount != clue) {
                     //   System.out.println("Unsatisfied clue found at cell (" + row + ", " + col + ")");
                        return false; // Found an unsatisfied clue
                    }
                }
            }
        } 
        return true; // No unsatisfied clues found
    }

    public boolean finalizeAndValidateBoard() {
        return validateFinalBoard();
    }

    // Converts board for status determination in AgentA
    
    public String convertBoardForAgentA() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < game.size; row++) {
            if (row > 0) {
                sb.append(";"); // Delimiter between rows
            }
            for (int col = 0; col < game.size; col++) {
                if (col > 0) {
                    sb.append(","); // Delimiter between columns within a row
                }
                // Determine the state of the cell
                char stateChar = '.';
                if (game.state[row][col] == Game.PAINTED) {
                    stateChar = '*';
                } else if (game.state[row][col] == Game.CLEARED) {
                    stateChar = '_';
                }
                
                // Append the state character
                sb.append(stateChar);
    
                // Append the clue if present, or a dash if not
                int clue = game.board[row][col];
                if (clue > -1) {
                    sb.append(clue);
                } else {
                    sb.append("-");
                }
            }
        }
        return sb.toString();
    }
    
    public static void main(String[] args) {
        System.out.println("Please enter the game spec:");
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        Game game = new Game();
        boolean parseSuccess = game.setGame(line);

        if (!parseSuccess) {
            System.out.println("Failed to parse game spec. Exiting.");
            return;
        }

        System.out.println("Initial game state:");
        game.printBoard();

        AgentB agentB = new AgentB(game);
       
        agentB.printCurrentGameState();
    }
}
































/*
 * The below code represents unused and non-working code - some with some good applications but unneeded for this Agent. 
 * For example: Clue Summation, Safe opening, and neighbor clue integration
 * 
 * Additionally, I implemented a bit of a brute force search at one point before realising it was against the rules for this agent
 * which may have been applicable for part D
 * 
 */




/* 

STRATEGIES: public boolean contradictionAvoidance() {
        boolean changesMade = false;
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.board[row][col] > -1) { // Cell has a clue
                    int clue = game.board[row][col];
                    int paintedCount = countPaintedNeighbors(row, col);
                    int coveredCount = countCoveredNeighbors(row, col);
                    // If the clue number is equal to the number of painted neighbors, then any additional covered neighbors must be cleared
                    if (clue == paintedCount && coveredCount > 0) {
                        clearCoveredNeighbors(row, col);
                        changesMade = true;
                        System.out.println("Cleared covered neighbors of cell (" + row + ", " + col + ") to avoid contradiction");
                    }
                }
            }
        }
        return changesMade;
    }
    public boolean completionStrategy() {
        boolean changesMade = false;
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.board[row][col] > -1) { // Cell has a clue
                    int clue = game.board[row][col];
                    int paintedCount = countPaintedNeighbors(row, col);
                    int coveredCount = countCoveredNeighbors(row, col);
                    // If the clue number is equal to the number of painted neighbors, then any additional covered neighbors must be cleared
                    if (clue == paintedCount && coveredCount > 0) {
                        clearCoveredNeighbors(row, col);
                        changesMade = true;
                        System.out.println("Cleared covered neighbors of cell (" + row + ", " + col + ") due to completion strategy");
                    }
                }
            }
        }
        return changesMade;
    }
    public boolean safeOpening() {
        boolean changesMade = false;
    
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                // If the clue is 0, all neighbors can be safely cleared.
                if (game.board[row][col] == 0) {
                    List<int[]> coveredNeighbors = getCoveredNeighbors(row, col);
                    for (int[] neighbor : coveredNeighbors) {
                        game.state[neighbor[0]][neighbor[1]] = Game.CLEARED;
                        changesMade = true;
                    }
                }
            }
        }
    
        return changesMade;
    }
    
    public boolean clueSummation() {
        boolean changesMade = false;
    
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                // Look for a cell with a clue greater than 0 (as it must have at least one painted neighbor).
                if (game.board[row][col] > 0) {
                    List<int[]> neighborsWithClueOne = getNeighborsWithClueOne(row, col);
                    for (int[] neighbor : neighborsWithClueOne) {
                        int[] sharedCoveredCell = getSharedCoveredCell(row, col, neighbor[0], neighbor[1]);
                        if (sharedCoveredCell != null) {
                            game.state[sharedCoveredCell[0]][sharedCoveredCell[1]] = Game.PAINTED;
                            changesMade = true;
                        }
                    }
                }
            }
        }
    
        return changesMade;
    }
    public boolean neighborClueIntegration() {
        boolean changesMade = false;
    
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                // Proceed only if the current cell has a clue of 1.
                if (game.board[row][col] == 1) {
                    // Get all covered neighbors for the current cell.
                    List<int[]> coveredNeighbors = getCoveredNeighbors(row, col);
                    
                    // Check each covered neighbor to see if it is also a neighbor
                    // to another cell with a clue of 1.
                    for (int[] coveredNeighbor : coveredNeighbors) {
                        if (isSharedByAnotherClueOne(coveredNeighbor, row, col)) {
                            // Paint the shared covered cell
                            game.state[coveredNeighbor[0]][coveredNeighbor[1]] = Game.PAINTED;
                            changesMade = true;
                        }
                    }
                }
            }
        }
    
        return changesMade;
    }
    
    private boolean isSharedByAnotherClueOne(int[] coveredNeighbor, int originalRow, int originalCol) {
        // Check all cells around the covered neighbor for another clue of 1.
        List<int[]> neighbors = getNeighborsIncludingSelf(coveredNeighbor[0], coveredNeighbor[1]);
        
        for (int[] neighbor : neighbors) {
            // Skip the original cell with the clue of 1.
            if (neighbor[0] == originalRow && neighbor[1] == originalCol) continue;
    
            // If another cell with a clue of 1 is found, return true.
            if (game.board[neighbor[0]][neighbor[1]] == 1) {
                return true;
            }
        }
    
        return false;
    }
    private List<int[]> getNeighborsWithClueOne(int row, int col) {
        List<int[]> neighbors = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborRow = row + i;
                int neighborCol = col + j;
                // Check only for out of bounds and the cell itself
                if (neighborRow < 0 || neighborRow >= game.size || neighborCol < 0 || neighborCol >= game.size || (i == 0 && j == 0)) {
                    continue;
                }
                if (game.board[neighborRow][neighborCol] == 1) {
                    neighbors.add(new int[]{neighborRow, neighborCol});
                }
            }
        }
        return neighbors;
    }
    
    private int[] getSharedCoveredCell(int row1, int col1, int row2, int col2) {
        List<int[]> coveredNeighbors1 = getCoveredNeighbors(row1, col1);
        List<int[]> coveredNeighbors2 = getCoveredNeighbors(row2, col2);
        for (int[] cell1 : coveredNeighbors1) {
            for (int[] cell2 : coveredNeighbors2) {
                if (cell1[0] == cell2[0] && cell1[1] == cell2[1]) {
                    return cell1; // Found a shared covered cell
                }
            }
        }
        return null; // No shared covered cell found
    }
private int countPaintedNeighbors(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborRow = row + i;
                int neighborCol = col + j;
                // Check only for out of bounds
                if (neighborRow < 0 || neighborRow >= game.size || neighborCol < 0 || neighborCol >= game.size) {
                    continue;
                }
                if (game.state[neighborRow][neighborCol] == Game.PAINTED) {
                    count++;
                    System.out.println("Found painted cell at (" + neighborRow + ", " + neighborCol + ")");
                }
            }
        }
        return count;
    }
    private int countCoveredNeighbors(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborRow = row + i;
                int neighborCol = col + j;
                // Check only for out of bounds
                if (neighborRow < 0 || neighborRow >= game.size || neighborCol < 0 || neighborCol >= game.size) {
                    continue;
                }
                if (game.state[neighborRow][neighborCol] == Game.COVERED) {
                    count++;
                }
            }
        }
        return count;
    }
    private List<int[]> getCoveredNeighbors(int row, int col) {
        List<int[]> coveredNeighbors = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborRow = row + i;
                int neighborCol = col + j;
                // Check only for out of bounds
                if (neighborRow < 0 || neighborRow >= game.size || neighborCol < 0 || neighborCol >= game.size) {
                    continue;
                }
                if (game.state[neighborRow][neighborCol] == Game.COVERED) {
                    coveredNeighbors.add(new int[]{neighborRow, neighborCol});
                }
            }
        }
        return coveredNeighbors;
    }
    private void clearCoveredNeighbors(int row, int col) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborRow = row + i;
                int neighborCol = col + j;
                // Check only for out of bounds
                if (neighborRow < 0 || neighborRow >= game.size || neighborCol < 0 || neighborCol >= game.size) {
                    continue;
                }
                if (game.state[neighborRow][neighborCol] == Game.COVERED) {
                    game.state[neighborRow][neighborCol] = Game.CLEARED;
                    System.out.println("Cleared cell (" + neighborRow + ", " + neighborCol + ")");
                }
            }
        }
    }

   

 
    private List<int[]> getNeighborsIncludingSelf(int row, int col) {
        List<int[]> neighbors = new ArrayList<>();
    
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nRow = row + i;
                int nCol = col + j;
                // Ensure within bounds
                if (nRow >= 0 && nRow < game.size && nCol >= 0 && nCol < game.size) {
                    neighbors.add(new int[]{nRow, nCol});
                    System.out.println("Added neighbor at (" + nRow + ", " + nCol + ")");
                }
            }
        }
    
        return neighbors;
    }
    
    public void finalizeBoard() {
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.state[row][col] == Game.COVERED) {
                    game.state[row][col] = Game.CLEARED;
                }
            }
        }
    }
    public boolean validateFinalBoard() {
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.board[row][col] > -1) { // Cell has a clue
                    int clue = game.board[row][col];
                    int paintedCount = countPaintedNeighbors(row, col);
                    // Check if painted count satisfies the clue
                    if (paintedCount != clue) {
                        System.out.println("Unsatisfied clue found at cell (" + row + ", " + col + ")");
                        return false; // Found an unsatisfied clue
                    }
                }
            }
        }
        return true; // No unsatisfied clues found
    }
    public boolean finalizeAndValidateBoard() {
        finalizeBoard();
        return validateFinalBoard();
    }
    public String convertBoardForAgentA() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < game.size; i++) {
            for (int j = 0; j < game.size; j++) {
                // Append state symbol
                switch (game.state[i][j]) {
                    case Game.COVERED:
                        sb.append('.'); // Assuming COVERED means not yet processed by AgentB
                        break;
                    case Game.PAINTED:
                        sb.append('*');
                        break;
                    case Game.CLEARED:
                        sb.append('_');
                        break;
                    default:
                        sb.append('.'); // Default case if none of the above
                }
                // Append clue or NO_CLUE symbol
                if (game.board[i][j] >= 0) {
                    sb.append(game.board[i][j]); // Append the clue
                } else {
                    sb.append('-'); // No clue present
                }
                if (j < game.size - 1) sb.append(","); // Column delimiter
            }
            if (i < game.size - 1) sb.append(";"); // Row delimiter
        }
        return sb.toString();
    }













import java.util.ArrayList;
import java.util.List;

public class AgentB {
    private Game game;
    private static final int COVERED = 0;
    private static final int CLEARED = 1;
    private static final int PAINTED = 2;

    public AgentB(Game game) {
        this.game = game;
    }

    public int makeMovesAndDetermineStatus() {
        boolean changesMade;
        do {
            changesMade = directClueApplication();
        } while (changesMade);

        printBoardState();
        return determineFinalStatus();
    }
    
    private String getBoardAsString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < game.size; i++) {
            if (i > 0) {
                sb.append(";"); // Delimiter between rows
            }
            for (int j = 0; j < game.size; j++) {
                // Convert state to the appropriate character representation
                char stateChar = (game.state[i][j] == 1) ? '*' :
                                 (game.state[i][j] == 2) ? '_' : '.';
    
                // Convert clue to string, using "-" for no clue (-1)
                String clueStr = (game.board[i][j] == -1) ? "-" : Integer.toString(game.board[i][j]);
                
                // Append the state and clue to the string builder
                sb.append(stateChar).append(clueStr);
                if (j < game.size - 1) {
                    sb.append(","); // Delimiter between columns
                }
            }
        }
        return sb.toString();
    }
    public boolean directClueApplication() {
        System.out.println("Running directClueApplication...");
        boolean changesMade = false;
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.board[row][col] != -1) {
                    int totalNeighbors = countTotalNeighborsIncludingSelf(row, col);
                    System.out.println("Cell (" + row + "," + col + ") - Clue: " + game.board[row][col] + ", Total Neighbors (incl. self): " + totalNeighbors);
                    if (totalNeighbors == game.board[row][col]) {
                        boolean painted = paintAllNeighbors(row, col);
                        if (painted) {
                            changesMade = true;
                        }
                    }
                }
            }
        }
        System.out.println("directClueApplication changesMade: " + changesMade);
        return changesMade;
    }

    
    private int countTotalNeighborsIncludingSelf(int row, int col) {
        System.out.println("Running countTotalNeighborsIncludingSelf for cell (" + row + "," + col + ")...");
        int count = 1; // Start with 1 to include the cell itself
        List<int[]> neighbors = getNeighbors(row, col);
        System.out.println("Total neighbors (incl. self) for cell (" + row + "," + col + "): " + count);
        return count;
    }
    
    private void printBoardState() {
        System.out.println("Current Board State:");
        for (int i = 0; i < game.size; i++) {
            for (int j = 0; j < game.size; j++) {
                System.out.print(game.state[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    private List<int[]> getNeighbors(int row, int col) {
        List<int[]> neighbors = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborRow = row + i;
                int neighborCol = col + j;
                // Skip the cell itself
                if (i == 0 && j == 0) continue;
                if (isValidPosition(neighborRow, neighborCol)) {
                    neighbors.add(new int[]{neighborRow, neighborCol});
                }
            }
        }
        return neighbors;
    }
    
    
    
    private boolean isValidPosition(int row, int col) {
        boolean valid = row >= 0 && row < game.size && col >= 0 && col < game.size;
        //System.out.println("Position (" + row + ", " + col + ") is valid: " + valid);
        return valid;
    }
    
    private boolean paintAllNeighbors(int row, int col) {
        System.out.println("Running paintAllNeighbors for cell (" + row + "," + col + ")...");
        boolean changesMade = false;
        List<int[]> neighbors = getNeighbors(row, col);
        for (int[] neighbor : neighbors) {
            int nRow = neighbor[0];
            int nCol = neighbor[1];
            if (game.state[nRow][nCol] == COVERED) {
                game.state[nRow][nCol] = PAINTED;
                changesMade = true;
            }
        }
        System.out.println("paintAllNeighbors changesMade: " + changesMade);
        return changesMade;
    }
    
    
    
    private int countCoveredNeighbors(int row, int col) {
        int count = 0;
        List<int[]> neighbors = getNeighbors(row, col);
        for (int[] neighbor : neighbors) {
            if (game.state[neighbor[0]][neighbor[1]] == COVERED) {
                count++;
            }
        }
        System.out.println("Covered neighbors for cell (" + row + ", " + col + "): " + count);
        return count;
    }
    private int determineFinalStatus() {
        boolean allCellsProcessed = true;
        boolean allCluesConsistent = true;
    
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.state[row][col] == COVERED) {
                    allCellsProcessed = false;
                    continue;
                }
    
                int paintedCount = countAdjacentPainted(row, col);
                if ((game.board[row][col] != -1 && game.board[row][col] != paintedCount)
                        || (game.board[row][col] == 0 && paintedCount > 0)) {
                    allCluesConsistent = false;
                }
            }
        }
    
        if (!allCellsProcessed) {
            return allCluesConsistent ? 2 : 0;
        } else {
            return allCluesConsistent ? 3 : 1;
        }
    }
    

private int countAdjacentPainted(int row, int col) {
    int count = 0;
    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            int newRow = row + i;
            int newCol = col + j;
            if (isValidPosition(newRow, newCol) && game.state[newRow][newCol] == PAINTED) {
                count++;
            }
        }
    }
    return count;
}
}









/*import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgentB {
    private Game game;
    private AgentA agentA;
    private static final char COVERED = '.';
    private static final char CLEARED = '_';
    private static final char PAINTED = '*';


    public AgentB(Game game) {
        this.game = game;
        String boardString = getBoardAsString(); // Convert the game object to string
        this.agentA = new AgentA(boardString); // Instantiate AgentA with the string
    }
    public int makeMovesAndDetermineStatus() {
        boolean changesMade;
        do {
            changesMade = false;
    
            // Apply each SPS strategy
            changesMade |= directClueApplication(); // |= is used to track if any changes were made
            changesMade |= contradictionAvoidance();
            changesMade |= contradictionAvoidance();
            changesMade |= completion();
            changesMade |= neighborClueIntegration();
            changesMade |= safeOpening();
            changesMade |= clueSummation();
            // ... apply other strategies
            updateAgentA();
        } while (changesMade);
    
        return agentA.determineFinalStatus(); // Call the method from AgentA
    }
    

    private boolean directClueApplication() {
        boolean changesMade = false;
        System.out.println("Starting directClueApplication");
        for (int row = 0; row < game.size; row++) {
            for (int col = 0; col < game.size; col++) {
                if (game.state[row][col] == COVERED && game.board[row][col] != -1) {
                    int coveredNeighbors = countCoveredNeighbors(row, col);
                    System.out.println("Cell (" + row + "," + col + ") - State: " + game.state[row][col] + ", Clue: " + game.board[row][col] + ", Covered Neighbors: " + coveredNeighbors);
                    if (game.board[row][col] == coveredNeighbors) {
                        System.out.println("Painting all neighbors of cell (" + row + ", " + col + ")");
                        paintAllNeighbors(row, col);
                        changesMade = true;
                    }
                }
            }
        }
        if (changesMade) {
            updateAgentA();
            System.out.println("Board updated in directClueApplication");
        }
        printBoardState(); 
        return changesMade;
    }
    
    private void printBoardState() {
        System.out.println("Current Board State:");
        for (int i = 0; i < game.size; i++) {
            for (int j = 0; j < game.size; j++) {
                System.out.print(game.state[i][j] + " ");
            }
            System.out.println(); 
        }
    }
    
// Helper method to get the neighbors of a cell, including the cell itself
private List<int[]> getNeighbors(int row, int col) {
    List<int[]> neighbors = new ArrayList<>();
    for (int i = -1; i <= 1; i++) {
        for (int j = -1; j <= 1; j++) {
            int neighborRow = row + i;
            int neighborCol = col + j;
            if (isValidPosition(neighborRow, neighborCol)) {
                neighbors.add(new int[]{neighborRow, neighborCol});
            }
        }
    }
    System.out.println("Neighbors of cell (" + row + ", " + col + "): " + neighbors);
    return neighbors;
}

// Helper method to get shared neighbors between two cells
private List<int[]> getSharedNeighbors(int row1, int col1, int row2, int col2) {
    List<int[]> sharedNeighbors = new ArrayList<>();

    // Get neighbors of the first cell
    List<int[]> neighbors1 = getNeighbors(row1, col1);
    // Get neighbors of the second cell
    List<int[]> neighbors2 = getNeighbors(row2, col2);

    // Check for common neighbors
    for (int[] n1 : neighbors1) {
        for (int[] n2 : neighbors2) {
            if (Arrays.equals(n1, n2)) {
                sharedNeighbors.add(n1);
            }
        }
    }
    System.out.println("Shared neighbors of cells (" + row1 + ", " + col1 + ") and (" + row2 + ", " + col2 + "): " + sharedNeighbors);
    return sharedNeighbors;
}

    // Refactor countCoveredNeighbors to use getNeighbors
private int countCoveredNeighbors(int row, int col) {
    int count = 0;
    List<int[]> neighbors = getNeighbors(row, col);
    for (int[] neighbor : neighbors) {
        if (game.state[neighbor[0]][neighbor[1]] == COVERED) {
            count++;
        }
    }
    return count;
}

// Refactor countPaintedNeighbors to use getNeighbors
private int countPaintedNeighbors(int row, int col) {
    int count = 0;
    List<int[]> neighbors = getNeighbors(row, col);
    for (int[] neighbor : neighbors) {
        if (game.state[neighbor[0]][neighbor[1]] == PAINTED) {
            count++;
        }
    }
    return count;
}

    
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < game.size && col >= 0 && col < game.size;
    }
    
    private boolean paintAllNeighbors(int row, int col) {
        boolean changesMade = false; // Declare changesMade within the method scope
        List<int[]> neighbors = getNeighbors(row, col); 
        for (int[] neighbor : neighbors) {
            if (game.state[neighbor[0]][neighbor[1]] == COVERED) {
                game.state[neighbor[0]][neighbor[1]] = PAINTED;
                changesMade = true;
            }
        }
        if (changesMade) {
            updateAgentA();
        }
        return changesMade;
    }
    
        private boolean contradictionAvoidance() {
            boolean changesMade = false;
            
            for (int row = 0; row < game.size; row++) {
                for (int col = 0; col < game.size; col++) {
                    // Check if the cell contains a clue
                    if (game.board[row][col] != -1) {
                        int clue = game.board[row][col];
                        int coveredNeighbors = countCoveredNeighbors(row, col);
                        int paintedNeighbors = countPaintedNeighbors(row, col);
                        
                        // If the number of painted neighbors plus the number of covered neighbors is greater than the clue
                        // then at least one covered neighbor must be cleared
                        if (paintedNeighbors + coveredNeighbors > clue) {
                            changesMade |= clearExtraCoveredCells(row, col, clue - paintedNeighbors);
                        }
                    }
                }
                if (changesMade) {
                    updateAgentA();
                }
            }
            
            return changesMade;
        }
        private boolean completion() {
            boolean changesMade = false;
            for (int row = 0; row < game.size; row++) {
                for (int col = 0; col < game.size; col++) {
                    int clue = game.board[row][col];
                    if (clue >= 0) {
                        int paintedNeighbors = countPaintedNeighbors(row, col);
                        int coveredNeighbors = countCoveredNeighbors(row, col);
                        if (paintedNeighbors == clue) {
                            // Since the number of painted cells equals the clue, all covered cells must be cleared
                            clearAllCoveredNeighbors(row, col);
                            changesMade = true;
                        } else if (paintedNeighbors + coveredNeighbors == clue) {
                            // If the sum of painted and covered neighbors equals the clue, all covered must be painted
                            paintAllCoveredNeighbors(row, col);
                            changesMade = true;
                        }
                    }
                }
            }
            if (changesMade) {
                updateAgentA();
            }
            return changesMade;
        }
        private boolean clearAllCoveredNeighbors(int row, int col) {
            boolean changesMade = false;
            List<int[]> neighbors = getNeighbors(row, col);
            for (int[] neighbor : neighbors) {
                if (game.state[neighbor[0]][neighbor[1]] == COVERED) {
                    game.state[neighbor[0]][neighbor[1]] = CLEARED; 
                    changesMade = true;
                }
            }
            if (changesMade) {
                updateAgentA();
            }
            return changesMade;
        }
        
        private boolean paintAllCoveredNeighbors(int row, int col) {
            boolean changesMade = false;
            List<int[]> neighbors = getNeighbors(row, col);
            for (int[] neighbor : neighbors) {
                if (game.state[neighbor[0]][neighbor[1]] == COVERED) {
                    game.state[neighbor[0]][neighbor[1]] = PAINTED; 
                    changesMade = true;
                }
            }
            if (changesMade) {
                updateAgentA();
            }
            return changesMade;
        }
private boolean clearExtraCoveredCells(int row, int col, int cellsToClear) {
    boolean changesMade = false;
    List<int[]> neighbors = getNeighbors(row, col);
    for (int[] neighbor : neighbors) {
        if (game.state[neighbor[0]][neighbor[1]] == COVERED && cellsToClear > 0) {
            game.state[neighbor[0]][neighbor[1]] = CLEARED;
            changesMade = true;
            cellsToClear--;
        }
    }
    if (changesMade) {
        updateAgentA(); // Synchronize with AgentA
    }
    return changesMade;
}
private boolean clueSummation() {
    boolean changesMade = false;
    // Iterate through all cells
    for (int row = 0; row < game.size; row++) {
        for (int col = 0; col < game.size; col++) {
            // Check if the cell has a clue
            int clue = game.board[row][col];
            if (clue >= 0 && game.state[row][col] != COVERED) {
                // Iterate through all neighbors of the cell
                List<int[]> neighbors = getNeighbors(row, col);
                for (int[] neighbor : neighbors) {
                    int nRow = neighbor[0];
                    int nCol = neighbor[1];
                    // Check if the neighbor cell has a clue and is not the same cell
                    int neighborClue = game.board[nRow][nCol];
                    if ((nRow != row || nCol != col) && neighborClue >= 0 && game.state[nRow][nCol] != COVERED) {
                        // Find shared neighbors and apply clue summation strategy
                        List<int[]> sharedNeighbors = getSharedNeighbors(row, col, nRow, nCol);
                        int sumClues = clue + neighborClue;
                        int paintedNeighbors = countPaintedNeighbors(row, col) + countPaintedNeighbors(nRow, nCol) - countPaintedNeighborsShared(row, col, nRow, nCol);
                        int remaining = sumClues - paintedNeighbors;
                        if (remaining == sharedNeighbors.size()) {
                            // Paint or clear the shared neighbors based on the game's rules
                            for (int[] sharedNeighbor : sharedNeighbors) {
                                // Example action: paint the shared neighbor
                                int sRow = sharedNeighbor[0];
                                int sCol = sharedNeighbor[1];
                                if (game.state[sRow][sCol] == COVERED) {
                                    game.state[sRow][sCol] = PAINTED;
                                    changesMade = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        if (changesMade) {
            updateAgentA();
        }
    }
    return changesMade;
}

// Helper method to count painted neighbors shared between two cells
private int countPaintedNeighborsShared(int row1, int col1, int row2, int col2) {
    int count = 0;
    List<int[]> sharedNeighbors = getSharedNeighbors(row1, col1, row2, col2);
    for (int[] neighbor : sharedNeighbors) {
        if (game.state[neighbor[0]][neighbor[1]] == PAINTED) {
            count++;
        }
    }
    return count;
}

private boolean safeOpening() {
    boolean changesMade = false;
    for (int row = 0; row < game.size; row++) {
        for (int col = 0; col < game.size; col++) {
            // If the clue is 0, all neighbors including the cell itself must be cleared
            if (game.board[row][col] == 0) {
                clearAllCoveredNeighbors(row, col);
                changesMade = true;
            }
            if (changesMade) { 
                updateAgentA();
            }
        }
    }
    return changesMade;
}
private boolean neighborClueIntegration() {
    boolean changesMade = false;
    // Loop through all cells in the grid
    for (int row = 0; row < game.size; row++) {
        for (int col = 0; col < game.size; col++) {
            // Check if this cell has a clue of 1
            if (game.board[row][col] == 1) {
                List<int[]> neighbors = getNeighbors(row, col); // Including the cell itself
                // Iterate over neighbors to find another clue of 1
                for (int[] neighbor : neighbors) {
                    int nRow = neighbor[0];
                    int nCol = neighbor[1];
                    // Skip the cell itself when checking for a neighbor with a clue of 1
                    if ((nRow != row || nCol != col) && game.board[nRow][nCol] == 1) {
                        List<int[]> sharedNeighbors = getSharedNeighbors(row, col, nRow, nCol);
                        // If there's exactly one shared covered neighbor, paint it
                        for (int[] sharedNeighbor : sharedNeighbors) {
                            int sRow = sharedNeighbor[0];
                            int sCol = sharedNeighbor[1];
                            // Check if the shared neighbor is covered, and if so, paint it
                            if (game.state[sRow][sCol] == COVERED) {
                                game.state[sRow][sCol] = PAINTED; 
                                changesMade = true;
                            }
                        }
                    }
                }
            }
        }
        if (changesMade) {
            updateAgentA();
        }
    }
    return changesMade;
}

private String getBoardAsString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < game.size; i++) {
        if (i > 0) {
            sb.append(";"); // Delimiter between rows
        }
        for (int j = 0; j < game.size; j++) {
            // Convert state to the appropriate character representation
            char stateChar = (game.state[i][j] == 1) ? '*' :
                             (game.state[i][j] == 2) ? '_' : '.';

            // Convert clue to string, using "-" for no clue (-1)
            String clueStr = (game.board[i][j] == -1) ? "-" : Integer.toString(game.board[i][j]);
            
            // Append the state and clue to the string builder
            sb.append(stateChar).append(clueStr);
            if (j < game.size - 1) {
                sb.append(","); // Delimiter between columns
            }
        }
    }
    return sb.toString();
}
private void updateAgentA() {
    String boardString = getBoardAsString();
    agentA = new AgentA(boardString);
}

/* */