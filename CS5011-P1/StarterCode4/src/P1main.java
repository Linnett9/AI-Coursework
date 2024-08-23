import java.util.Scanner;

/*
 * Starter code
 * Example of Main Class 
 * CS5011 - P1
 * 
 * author: a.toniolo -- modified by 220034672
 */
/**
 * Entry point for the Mosaic game, handling agent selection and game execution.
 * This class interprets command-line arguments to choose the game agent,
 * accepts game specifications from the user, and oversees the game process.
 */

public class P1main {
    public static void main(String[] args) {
        boolean verbose = false;
        boolean agentBUsed = false;
        AgentB agentB = null; // Initialize AgentB to null
        if (args.length < 1) {
            System.out.println("usage: ./playMosaic.sh <A|B|C1|C2|C3|D> [verbose] [<any other param>]");
            System.exit(1);
        }
        if (args.length > 1 && args[1].equals("verbose")) {
            verbose = true;
            System.out.println("Verbose mode is on");
        }
        System.out.println("Please enter the game spec:");
        Scanner sc = new Scanner(System.in);
        String line = sc.nextLine();
        System.out.println(line);
        Game board = new Game();
        boolean parse = board.setGame(line);
        if (!parse) {
            System.out.println("Something went wrong with your game spec, please try again");
            System.exit(1);
        }

        System.out.println("Agent " + args[0] + " plays");
        System.out.println("Game:");
        board.printGame();
        System.out.println("Intitial view:");
        board.printBoard();

        System.out.println("Start!");

        int output = 0;

        // Initialize AgentB if needed for Agent B or C1
        if ("B".equals(args[0]) || "C1".equals(args[0])) {
            agentB = new AgentB(board);
            agentBUsed = true;
        }

        switch (args[0]) {
            case "A":
                AgentA agentA = new AgentA(line);
                output = agentA.determineFinalStatus();
                board.printBoard();
                break;

            case "B":
                boolean changesMade;
                do {
                    changesMade = false;
                    if (agentB.directClueApplication()) {
                        changesMade = true;
                    }
                    if (agentB.contradictionAvoidance()) {
                        changesMade = true;
                    }
                    if (agentB.completionStrategy()) {
                        changesMade = true;
                    }
                } while (changesMade);

                if (!agentB.finalizeAndValidateBoard()) {
                    board.printBoard();
                } else {
                    agentB.printCurrentGameState();
                }

                String boardStringForA = agentB.convertBoardForAgentA();
                AgentA agentAWithBState = new AgentA(boardStringForA);
                output = agentAWithBState.determineFinalStatus();
                break;

                case "C1":
    //System.out.println("Agent C1 is attempting to solve the puzzle using DNF strategies.");
    AgentC1 agentC1 = new AgentC1(board, verbose);

    int result = agentC1.playGame();

    if (result == 3) { // Assuming 3 indicates success
       // System.out.println("Puzzle solved by Agent C1.");
        String boardStringForC1 = agentC1.convertBoardForAgentA(); 
        AgentA agentAWithC1State = new AgentA(boardStringForC1);
        output = agentAWithC1State.determineFinalStatus(); 
       // System.out.println("Final Status determined by AgentA: " + output);
    //} else {
       // System.out.println("Agent C1 could not solve the puzzle.");
    }
    break;

            case "C2":
                System.out.println("Agent C2 is playing");
                //TODO: Implement Agent C2 logic
                break;

            case "C3":
                System.out.println("Agent C3 is playing");
                //TODO: Implement Agent C3 logic
                break;

            case "D":
                System.out.println("Agent D is playing");
                //TODO: Implement Agent D logic
                break;
        }

        switch (output) {
            case 0:
                System.out.println("Result: Game not terminated and incorrect");
                break;

            case 1:
                System.out.println("Result: Agent loses: Game terminated but incorrect");
                break;

            case 2:
                System.out.println("Result: Game not terminated but correct");
                break;

            case 3:
                System.out.println("Result: Agent wins: Game terminated and correct");
                break;

            default:
                System.out.println("Result: Unknown");
        }

        sc.close(); 
    }
}