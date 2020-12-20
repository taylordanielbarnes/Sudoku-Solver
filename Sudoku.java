/*
 * Taylor Barnes
 * 
 * Sudoku Algorithm
 * Sets up a soduku puzzle based on user input
 * User inputs 81 numbers separated by spaces or newlines
 * Blank spaces are represented by -1
 * Feeds all blank squares through a queue and solves them one by one
 * The algorithm used is capable of solving easy, medium, or hard puzzles
 * It cannot solve all expert level puzzles, however
 */

import java.util.*;
import java.io.*;
import java.lang.Boolean;
import java.util.BitSet;



public class Sudoku {

	public static void main(String[] args) {
		
		//take in all user input at once
		BufferedReader stdin = null;
		ArrayList<String> strArr = new ArrayList<String>();  
		String[] tokArr = new String[100];
		String line1 = "";
		try {
			stdin = new BufferedReader(new InputStreamReader(System.in));
			line1 = stdin.readLine();
			while(!line1.equals("null")) { //change to line1 != NULL if reading from file
				tokArr = line1.split(" ", 0);
				for(int k = 0; k < tokArr.length; k++) {
					strArr.add(tokArr[k]);
				}
				line1 = stdin.readLine();
			}
		} catch (Exception e) {}
		
		
		//declare rows/boxes/columns
		Square[][] squares = new Square[9][9];
		Row[] rows = new Row[9];
		Column[] columns = new Column[9];
		Box[] boxes = new Box[9];
		for(int i = 0; i < 9; i++) {
			Row r = new Row();
			Column c = new Column();
			Box b = new Box();
			rows[i] = r;
			columns[i] = c;
			boxes[i] = b;
		}
		
		//populate sudoku grid
		int k = 0;
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 9; j++) {
				int x = ((i / 3) * 3) + (j / 3);
				int y = ((i % 3) * 3) + (j % 3);
				
				Square s = new Square(Integer.parseInt(strArr.get(k++)), i, j, x);
				squares[i][j] = s;
				
				rows[i].addSquare(s, j);
				columns[j].addSquare(s, i);
				boxes[x].addSquare(s, y);
			}
		}
		
		//create queue
		LinkedList<Square> Q = new LinkedList<>();
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 9; j++) {
				if(!squares[i][j].isSolved()) {
					Q.addLast(squares[i][j]);
				}
			}
		}
		
		//traverse queue until the puzzle is solved
		int timeout = 5000; //or until puzzle times out
		while(Q.size() > 0 && timeout > 0) {
			Square current = Q.removeFirst();
			
			if(!current.isSolved())
				current.checkRow(rows[current.x]);
			if(!current.isSolved())
				current.checkColumn(columns[current.y]);
			if(!current.isSolved())
				current.checkBox(boxes[current.z]);
				
			if(!current.isSolved())
				current.complexCheckRow(rows[current.x]);
			if(!current.isSolved())
				current.complexCheckColumn(columns[current.y]);
			if(!current.isSolved())
				current.complexCheckBox(boxes[current.z]);
			
			//refill queue if not yet solved
			if(!current.isSolved())
				Q.add(current);
			
			timeout--;
		}
		
		
		//print squares
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 9; j++) {
				if(squares[i][j].solution > 0)
					System.out.printf("%2d ", squares[i][j].solution);
				else
					System.out.printf(" _ ");
			}
			System.out.println();
		}
	}
}



/*
 * Square
 * 
 * Individual square in the 9x9 sudoku puzzle
 * Has a solution 1-9
 * Or a list of possible solutions
 */
class Square {
	BitSet poss = new BitSet(9);
	public int solution;
	int numPossible;
	public int x, y, z;
	
	Square(int a, int xx, int yy, int zz) { //a is the given solution
		x = xx;
		y = yy;
		z = zz;
		if(a > 0) { //if a real solution is given
			solve(a);
		}
		else { //if no real solution is given
			poss.set(0, 9); //set all possibilities true
			solution = -1;
			numPossible = 9;
		}
	}
	
	public Boolean isSolved() {
		return (numPossible == 1);
	}
	
	public void solve(int a) { //version with an explicit solution
		poss.clear(); //set all possibilities false
		poss.set(a - 1); //except for the one truth
		solution = a;
		numPossible = 1;
	}
	public void solve() { //version with an implicit solution
		for(int i = 0; i < 9; i++) {
			if(poss.get(i)) {
				solution = i + 1;
				break;
			}
		}
	}
	
	//check the square's row/column/box for new eliminations based on concrete solutions
	public void checkRow(Row r) {
		int elimVal;
		for(int i = 0; i < 9; i++) {
			elimVal = r.squares[i].solution;
			if(elimVal > 0 && poss.get(elimVal - 1)) //if a solution exists and it is still a possibility in this square
				eliminate(elimVal);
		}
	}
	public void checkColumn(Column c) {
		int elimVal;
		for(int i = 0; i < 9; i++) {
			elimVal = c.squares[i].solution;
			if(elimVal > 0 && poss.get(elimVal - 1)) //if a solution exists and it is still a possibility in this square
				eliminate(elimVal);
		}
	}
	public void checkBox(Box b) {
		int elimVal;
		for(int i = 0; i < 9; i++) {
			elimVal = b.squares[i].solution;
			if(elimVal > 0 && poss.get(elimVal - 1)) //if a solution exists and it is still a possibility in this square
				eliminate(elimVal);
		}
	}
	
	//check the square's row/column/box for new eliminations based on solutions found by the complex algorithm
	public void complexCheckRow(Row r) {
		BitSet temp1 = new BitSet(9);
		temp1.or(poss);
		int tick = 0;
		
		for(int i = 0; i < 9; i++) {
			BitSet temp2 = new BitSet(9);
			temp2.or(r.squares[i].poss);
			temp2.andNot(temp1);
			if(temp2.isEmpty()) { //if temp2's possibilities are restricted to the same possibilities as temp1
				tick++;
			}
		}
		if(tick == numPossible) {
			for(int i = 0; i < 9; i++) {
				BitSet temp2 = new BitSet(9);
				temp2.or(r.squares[i].poss);
				temp2.andNot(temp1);
				if(!temp2.isEmpty()) { //if temp2's possibilities are restricted to the same possibilities as temp1
					for(int j = 0; j < 9; j++) {
						if(r.squares[i].poss.get(j) && this.poss.get(j)) {
							r.squares[i].eliminate(j + 1);
						}
					}
				}
			}
		}
	}
	public void complexCheckColumn(Column c) {
		BitSet temp1 = new BitSet(9);
		temp1.or(poss);
		int tick = 0;
		
		for(int i = 0; i < 9; i++) {
			BitSet temp2 = new BitSet(9);
			temp2.or(c.squares[i].poss);
			temp2.andNot(temp1);
			if(temp2.isEmpty()) { //if temp2's possibilities are restricted to the same possibilities as temp1
				tick++;
			}
		}
		if(tick == numPossible) {
			for(int i = 0; i < 9; i++) {
				BitSet temp2 = new BitSet(9);
				temp2.or(c.squares[i].poss);
				temp2.andNot(temp1);
				if(!temp2.isEmpty()) { //if temp2's possibilities are restricted to the same possibilities as temp1
					for(int j = 0; j < 9; j++) {
						if(c.squares[i].poss.get(j) && this.poss.get(j)) {
							c.squares[i].eliminate(j + 1);
						}
					}
				}
			}
		}
	}
	public void complexCheckBox(Box b) {
		BitSet temp1 = new BitSet(9);
		temp1.or(poss);
		int tick = 0;
		
		for(int i = 0; i < 9; i++) {
			BitSet temp2 = new BitSet(9);
			temp2.or(b.squares[i].poss);
			temp2.andNot(temp1);
			if(temp2.isEmpty()) { //if temp2's possibilities are restricted to the same possibilities as temp1
				tick++;
			}
		}
		if(tick == numPossible) {
			for(int i = 0; i < 9; i++) {
				BitSet temp2 = new BitSet(9);
				temp2.or(b.squares[i].poss);
				temp2.andNot(temp1);
				if(!temp2.isEmpty()) { //if temp2's possibilities are restricted to the same possibilities as temp1
					for(int j = 0; j < 9; j++) {
						if(b.squares[i].poss.get(j) && this.poss.get(j)) {
							b.squares[i].eliminate(j + 1);
						}
					}
				}
			}
		}
	}
	
	public void eliminate(int a) { //eliminate a possible solution
		if(!this.isSolved()) {
			poss.clear(a - 1);
			numPossible--;
			
			if(this.isSolved()) {
				this.solve();				
			}
		}
	}
	
	public String toString() {
		String str = "(" + x + ", " + y + ")";
		return str;
	}
}

class Row {
	public Square[] squares = new Square[9];
	
	public void addSquare(Square s, int pos) {
		squares[pos] = s;
	}
}

class Column {
	public Square[] squares = new Square[9];
	
	public void addSquare(Square s, int pos) {
		squares[pos] = s;
	}
}

class Box {
	public Square[] squares = new Square[9];
	
	public void addSquare(Square s, int pos) {
		squares[pos] = s;
	}
}