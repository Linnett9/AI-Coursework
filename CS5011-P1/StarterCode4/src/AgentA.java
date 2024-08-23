/**
 * Represents an agent that evaluates the state of a Mosaic game board and determines its final status.
 * This agent assesses whether all clues match with the state of the board without making any moves.
 */

public class AgentA {
    private static final char PAINTED = '*';
    private static final char CLEARED = '_';
    private static final char COVERED = '.';
    private static final char NO_CLUE = '-';
// 2D array representing the game board
    private final Cell[][] board;
    private final int rows;
    private final int cols;

    // Represents each cell of the Mosaic board
    private static class Cell {
        char state;
        int clue;

        Cell(char state, int clue) {
            this.state = state;
            this.clue = clue;
        }
    }

/**
     * Constructs an AgentA with a specified board configuration.
     * @param boardString The string representation of the board's initial state and clues.
     */

    public AgentA(String boardString) {
      //  System.out.println("Initializing AgentA with boardString: " + boardString);
        String[] rowStrings = boardString.split(";");
        this.rows = rowStrings.length;
        this.cols = rowStrings[0].split(",").length;
       // System.out.println("Board dimensions: " + this.rows + " rows, " + this.cols + " columns");
        this.board = new Cell[this.rows][this.cols];
        
        for (int i = 0; i < this.rows; i++) {
            String[] cellStrings = rowStrings[i].split(",");
            for (int j = 0; j < this.cols; j++) {
                String cellStr = cellStrings[j];
                char state = cellStr.charAt(0);
                String clueStr = cellStr.substring(1, cellStr.length() - 1); // Extract clue from the string
                if (clueStr.length() == 0) {
                    clueStr = cellStr.substring(1); // If no painting state, the clue is from index 1
                }
                int clue;
                try {
                    clue = clueStr.equals("-") ? -1 : Integer.parseInt(clueStr);
                } catch (NumberFormatException e) {
                 //   System.out.println("Error parsing cell: " + cellStr + " | State: " + state + " | ClueStr: " + clueStr);
                    throw e;
                }
                this.board[i][j] = new Cell(state, clue);
               // System.out.println("Created cell at (" + i + ", " + j + ") with state: " + state + " and clue: " + clue);
            }
        }
    }
    /**
     * Determines the final status of the game by evaluating if all clues are consistent and all cells are processed.
     * @return An integer representing the final status code of the game.
     */

    public int determineFinalStatus() {
        //System.out.println("Determining final status...");
        boolean allCellsProcessed = true;
        boolean allCluesConsistent = true;
    
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                Cell cell = this.board[i][j];
    
                if (cell.state == COVERED) {
                    allCellsProcessed = false;
                  //  System.out.println("Cell at (" + i + ", " + j + ") is still covered.");
                    continue;
                }
    
                int paintedCount = countAdjacentPainted(i, j);
                int coveredCount = countAdjacentCovered(i, j);
    
                // If clue is -1, it means there is no specific clue, so we should not check for inconsistency
                if (cell.clue != -1) {
                    // If all neighboring cells are determined and the painted count does not match the clue, it's inconsistent
                    if ((coveredCount == 0 && cell.clue != paintedCount) || (paintedCount > cell.clue)) {
                        allCluesConsistent = false;
                     //   System.out.println("Inconsistency found at cell (" + i + ", " + j + "). Clue: " + cell.clue + ", Painted count: " + paintedCount + ", Covered count: " + coveredCount);
                    }
                }
            }
        }
    
        if (!allCellsProcessed) {
           // System.out.println("Not all cells processed. All clues consistent: " + allCluesConsistent);
            return allCluesConsistent ? 2 : 0;
        } else {
           // System.out.println("All cells processed. All clues consistent: " + allCluesConsistent);
            return allCluesConsistent ? 3 : 1;
        }
    }
     /**
     * Counts the number of adjacent painted/covered cells for a given cell.
     * @param row The row index of the cell.
     * @param col The column index of the cell.
     * @return The count of adjacent painted cells.
     */

    private int countAdjacentPainted(int row, int col) {
     // System.out.println("Counting adjacent painted cells for (" + row + ", " + col + ")...");
        int count = 0;
        // Include the cell itself if it's painted
        if (this.board[row][col].state == PAINTED) {
            count++;
        }
        // Then check the surrounding cells
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // skip the cell itself in the loop
                int newRow = row + i;
                int newCol = col + j;
                if (newRow >= 0 && newRow < this.rows && newCol >= 0 && newCol < this.cols) {
                    if (this.board[newRow][newCol].state == PAINTED) {
                        count++;
                    }
                }
            }
        }
       // System.out.println("Count of adjacent painted cells for (" + row + ", " + col + "): " + count);
        return count;
    }
    
    public int countAdjacentCovered(int row, int col) {
      //  System.out.println("Counting adjacent covered cells for (" + row + ", " + col + ")...");
        int count = 0;
        // Check the surrounding cells
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // Skip the cell itself
                if (i == 0 && j == 0) continue;
                int newRow = row + i;
                int newCol = col + j;
                // Check the cell is within the bounds of the board
                if (newRow >= 0 && newRow < this.rows && newCol >= 0 && newCol < this.cols) {
                    // Increase count if the cell is covered
                    if (this.board[newRow][newCol].state == COVERED) {
                        count++;
                    }
                }
            }
        }
       // System.out.println("Count of adjacent covered cells for (" + row + ", " + col + "): " + count);
        return count;
    }
    /**
     * Converts the current board state into a string representation.
     * @return A string representing the current state of the board.
     */
    
    public String getBoardAsString() {
      //  System.out.println("Getting board as string...");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            if (i > 0) {
                sb.append(";"); // row delimiter
            }
            for (int j = 0; j < cols; j++) {
                if (j > 0) {
                    sb.append(","); // column delimiter
                }
                Cell cell = board[i][j];
                sb.append(cell.state); // append the cell state
                sb.append(cell.clue == -1 ? NO_CLUE : cell.clue); // append the clue or NO_CLUE char
            }
        }
     //   System.out.println("Board as string: " + sb.toString());
        return sb.toString();
    }
   
    public static void main(String[] args) {
        // Test with a provided board string
        AgentA agent = new AgentA(".2_,.-*,.4*;.-_,.6*,.-*;.3*,.-*,.-_");
        String boardString = agent.getBoardAsString();
        System.out.println("Board as String: " + boardString);

        int finalStatus = agent.determineFinalStatus();
        System.out.println("Final Status: " + finalStatus);
    }
}



