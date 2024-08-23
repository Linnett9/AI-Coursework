/* Starter code
 * Game class: used to hold the board, parse the input, print the output
 * CS5011 - P1
 * 
 * author: a.toniolo  -- modified by 220034672
 */

import java.util.ArrayList;
import java.util.List;

public class Game {
	int[][] board;
	int[][] state;
	int[][] game;
	int size;
    public static final int COVERED = 0;
	public static final int CLEARED = 1;
    public static final int PAINTED = 2;
	/*
	 * board is the one the agent will play with 
	 * state indicates the status of the cells
	 * game is the hidden board, the one the agent has to be able to find
	 */


	public Game() {
		size=2;
		board=new int[size][size];
		state=new int[size][size];
		game=new int[size][size];
	}


	public boolean setGame(String map) {
		try {
			String[] lines=map.split(";"); //rows
			size=lines.length;
			board=new int[size][size];
			state=new int[size][size];
			game=new int[size][size];
			for(int r=0; r<size; r++) {
				String line=lines[r];
				String[] set=line.split(","); // columns
				int[] bl=new int[size];
				int[] sl=new int[size];
				int[] gl=new int[size];
				for(int c=0;c<size;c++) { 
					String ch=set[c];
					char i=ch.charAt(0); // status
					char e=ch.charAt(ch.length()-1); //paint
					String m=ch.substring(1,ch.length()-1); //clue
					int state=0;
					int paint=0;
					int clue=0;
					switch(i) {
					case '*':
						state=2;
						break;
					case '_':
						state=1;
						break;
					case '.':
						state=0;
					}
					if(m.equals("-")) {
						clue=-1;
					}else {
						clue=Integer.parseInt(m);
					}
					switch(e) {
					case '*':
						paint=1;
						break;
					case '_':
						paint=2;
						break;
					}
					bl[c]=clue;
					sl[c]=state;
					gl[c]=paint;
				}
				board[r]=bl;
				state[r]=sl;
				game[r]=gl;
				
			}
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		

	}
	public void printGame() {
		//this method is used to print the initial game view

		System.out.println();
		// first line
		System.out.format("%4s","   ");
		for (int c = 0; c < board[0].length; c++) {
			System.out.format("%4s",c);
		}
		System.out.println();
		// second line
		System.out.format("%5s","   ");
		for (int c = 0; c < board[0].length; c++) {
			System.out.print("--- ");// separator
		}
		System.out.println();
		// the board 
		for (int r = 0; r < board.length; r++) {
			System.out.print(" "+ r + "| ");// index+separator
			for (int c = 0; c < board[0].length; c++) {
				String code="";
				//print clues
				if(board[r][c]==-1) {
					code+=" ";
				}else {
					code+=board[r][c];
				}
				//print paint
				switch(game[r][c]) {
				case 1:
					code+="*";
					break;
				case 2:
					code+="_";
					break;
				}
				System.out.format("%4s",code);
			}
			System.out.println();
		}
		System.out.println();
	}


	public void printBoard() {
		//System.out.println("print board method called");
		System.out.println();
		// first line
		System.out.format("%4s", "   ");
		for (int c = 0; c < board[0].length; c++) {
			System.out.format("%4s", c);
		}
		System.out.println();
		// second line
		System.out.format("%5s", "   ");
		for (int c = 0; c < board[0].length; c++) {
			System.out.print("--- "); // separator
		}
		System.out.println();
		// the board
		for (int r = 0; r < board.length; r++) {
			System.out.print(" " + r + "| "); // index+separator
			for (int c = 0; c < board[0].length; c++) {
				String code = "";
				// Adjust code based on the state
				switch (state[r][c]) {
					case PAINTED:
						code += "*"; // Painted
						break;
					case COVERED:
						code += "."; // Covered
						break;
					case CLEARED:
						code += "_"; // Cleared
						break;
				}
				// Append clue if not -1
				code += (board[r][c] == -1) ? "-" : board[r][c];
				System.out.format("%4s", code);
			}
			System.out.println();
		}
		System.out.println();
	}
	
// PUBLIC HELPER METHODS 


public List<int[]> getNeighborsIncludingSelf(int row, int col) {
        List<int[]> neighbors = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int nRow = row + i;
                int nCol = col + j;
                // Ensure the neighbor is within the board boundaries
                if (nRow >= 0 && nRow < this.size && nCol >= 0 && nCol < this.size) {
                    neighbors.add(new int[]{nRow, nCol});
                }
            }
        }
        return neighbors;
    }
	public int countPaintedNeighbors(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborRow = row + i;
                int neighborCol = col + j;
                // Check only for out of bounds
                if (neighborRow < 0 || neighborRow >= size || neighborCol < 0 || neighborCol >= size) {
                    continue;
                }
                if (state[neighborRow][neighborCol] == Game.PAINTED) {
                    count++;
                }
            }
        }
        return count;
    }

    public int countCoveredNeighbors(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborRow = row + i;
                int neighborCol = col + j;
                // Check only for out of bounds
                if (neighborRow < 0 || neighborRow >= size || neighborCol < 0 || neighborCol >= size) {
                    continue;
                }
                if (state[neighborRow][neighborCol] == Game.COVERED) {
                    count++;
                }
            }
        }
        return count;
    }
	
	public boolean isValidCell(int row, int col) {
        return row >= 0 && row < size && col >= 0 && col < size;
    }
}

