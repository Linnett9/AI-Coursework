import org.logicng.datastructures.Assignment;
import org.logicng.datastructures.Tristate;
import org.logicng.formulas.Formula;
import org.logicng.formulas.FormulaFactory;
import org.logicng.formulas.Literal;
import org.logicng.formulas.Variable;
import org.logicng.solvers.MiniSat;
import org.logicng.solvers.SATSolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AgentC1 uses logical satisfiability (SAT) solving to make decisions in a Mosaic game.
 * It converts the current game state into Disjunctive Normal Form (DNF) sentences and uses a SAT solver to find solutions.
 */

public class AgentC1 {
    private Game game;
    private boolean verbose;
    private final FormulaFactory f = new FormulaFactory();
    private boolean [][] visited;
    private Formula dnfSentence;
    public AgentC1(Game game, boolean verbose) {
        this.game = game;
        this.verbose = verbose;
        this.visited = new boolean[game.size][game.size];
        this.dnfSentence =null;
    }

// converts the game state to DNF

    private Formula convertToDNFSentence() {
    List<Formula> allCellFormulas = new ArrayList<>();
    
    for (int r = 0; r < game.size; r++) {
        for (int c = 0; c < game.size; c++) {
            int clue = game.board[r][c];
            if (clue >= 0) {
                List<Formula> cellClauses = generateDNFClausesForCell(r, c, clue);
                // Wrap each cell's DNF clauses in an OR block
                Formula cellDnf = f.or(cellClauses);
                allCellFormulas.add(cellDnf); 
                if (verbose) {
              //      System.out.println("Cell (" + r + "," + c + ") DNF: " + cellDnf);
                }
            }
        }
    }
    
    // Combine all cell DNFs with an AND block
    Formula finalDnfSentence = f.and(allCellFormulas);
    if (verbose) {
       // System.out.println("Final DNF Sentence: " + finalDnfSentence);
    }
    return finalDnfSentence;
}

// Generates DNF clauses for single cells based on its clue and neighbors

private List<Formula> generateDNFClausesForCell(int row, int col, int clue) {
    List<int[]> neighbors = game.getNeighborsIncludingSelf(row, col);
    List<List<Literal>> validCombinations = getValidCombinations(neighbors, clue);
    
    List<Formula> cellClauses = new ArrayList<>();
    for (List<Literal> combination : validCombinations) {
        // For each combination, wrap the literals in an AND block
        cellClauses.add(f.and(combination.toArray(new Literal[0])));
    }
    
    return cellClauses;
}

// Gets valid combinations of literals that could satisfy the clue

private List<List<Literal>> getValidCombinations(List<int[]> neighbors, int clue) {
    List<List<Literal>> validCombinations = new ArrayList<>();
    generateCombinations(neighbors, clue, new ArrayList<>(), 0, validCombinations);
    return validCombinations;
}

    private Set<String> validCombinationsSet = new HashSet<>();

// Recusively generates combintations of literals that satisfy a clue    

    private void generateCombinations(List<int[]> neighbors, int clue, List<Literal> current, int start, List<List<Literal>> validCombinations) {
        if (clue == 0) {
            // Generate all combinations of painted and cleared cells
            List<Literal> currentCopy = new ArrayList<>(current);
            for (int[] coords : neighbors) {
                Variable var = f.variable("P" + coords[0] + "_" + coords[1]);
                if (!currentCopy.contains(var)) {
                    Literal negVar = var.negate(); // Get the negation of the variable
                    currentCopy.add(negVar);
                }
            }
            // Convert the combination to a string
            String combinationStr = currentCopy.toString();
            // Only add the combination if it does not already exist in validCombinationsSet
            if (!validCombinationsSet.contains(combinationStr)) {
                validCombinations.add(currentCopy);
                validCombinationsSet.add(combinationStr);
             //   System.out.println("Valid combination: " + currentCopy); // Print the current valid combination
            }
            return;
        }
    
        for (int i = start; i <= neighbors.size() - clue; i++) {
            int[] coords = neighbors.get(i);
            Variable var = f.variable("P" + coords[0] + "_" + coords[1]);
            current.add(var);
            generateCombinations(neighbors, clue - 1, current, i + 1, validCombinations);
            if (!current.isEmpty()) {
                current.remove(current.size() - 1);
            }
        }
    }

// Attempts to solve the game using final DNF sentence and SAT solver

public int playGame() {
    boolean changesMade;
    do {
        changesMade = false;
       this.dnfSentence = convertToDNFSentence();
        if (verbose) {
         //   System.out.println("DNF Sentence: " + dnfSentence);
        }
        SATSolver solver = MiniSat.miniSat(f);
        solver.add(dnfSentence);
        Tristate result = solver.sat();

        if (result == Tristate.TRUE) {
            Assignment model = solver.model();
            if (verbose) {
          //      System.out.println("SAT Solver Model: " + model);
            }
            changesMade = applySolutionToGameState(model);
        }
    } while (changesMade);
 //   System.out.println("Final DNF Sentence: " + dnfSentence);  // Print the final DNF sentence
        String boardForAgentA = convertBoardForAgentA();
        AgentA agentA = new AgentA(boardForAgentA);
        int finalStatus = agentA.determineFinalStatus();
    
        game.printBoard();
    
        return finalStatus;
    }

// Applies the solution found by the solver to the game state

    private boolean applySolutionToGameState(Assignment model) {
        boolean changesMade = false;
        for (Literal literal : model.literals()) {
            int row = Integer.parseInt(literal.name().substring(1, literal.name().indexOf("_")));
            int col = Integer.parseInt(literal.name().substring(literal.name().indexOf("_") + 1));
            if (literal.phase() && game.state[row][col] != Game.CLEARED) {
                game.state[row][col] = Game.PAINTED;
                changesMade = true;
                if (verbose) {
          //          System.out.println("Painting cell at (" + row + ", " + col + ") due to SAT model.");
                }
            } else if (!literal.phase() && game.state[row][col] != Game.PAINTED) {
                game.state[row][col] = Game.CLEARED;
                changesMade = true;
                if (verbose) {
             //       System.out.println("Clearing cell at (" + row + ", " + col + ") due to SAT model.");
                }
            }
        }
        return changesMade;
    }

// converts the game state into a string for status determination by AgentA

    public String convertBoardForAgentA() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < game.size; row++) {
            if (row > 0) sb.append(";");
            for (int col = 0; col < game.size; col++) {
                if (col > 0) sb.append(",");
                char stateChar = game.state[row][col] == Game.PAINTED ? '*' : game.state[row][col] == Game.CLEARED ? '_' : '.';
                sb.append(stateChar);
                int clue = game.board[row][col];
                sb.append(clue > -1 ? clue : "-");
            }
        }
        return sb.toString();
    }
}