package ai.montecarloheuristic10;

import java.util.List;
import java.util.Random;

import util.Tuple;
import core.Board;
import core.Rules;

/**
 * Classe relativa al Montecarloh10 che estrapola file e tuple dai records
 * @author Mina
 *
 */

public class MonteCarloH10 {
	private Board board;
	private String color;
	private Root root;
	private int allMovesNumber;
	private double c;
	
//	public void test() throws Exception {
//		defaultPolicy(this.root.getRoot(), this.board);
//		board.draw();
//	}
	
	/**
	 * 
	 * @param board
	 * @param color
	 */
	public MonteCarloH10(Board board, String color, int moveNumber, 
			int allMovesNumber) {
		this.board = board;
		this.color = color;
		this.allMovesNumber = allMovesNumber;
		this.root = new Root(new Node(null, null, color, board, moveNumber, 
				this.color));
		int twoh10 = 2;
		this.c = 1 / Math.sqrt(twoh10);
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public Tuple<Integer, Integer> uct(int n) {
		while(n > 0) {
//			System.out.println(n);
			Board tempBoard = this.board.duplicate();
			Node node = null;
			try {
				node = treePolicy(root.getRoot(), tempBoard);
			} catch (Exception e) {
				System.out.println("TREE_POLICY");
				System.exit(0);
			}
			String delta = null;
			try {
				delta = defaultPolicy(node, tempBoard);
			} catch (Exception e) {
				System.out.println("DEFAULT_POLICY");
				System.exit(0);
			}
			back_up(node, delta);
			--n;
		}
		return bestChild(root.getRoot(), 0).getMove();
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 * @throws Exception 
	 */
	private Node treePolicy(Node treePolicynode, Board boardtreePolicy) {
		int numberNode = treePolicynode.getMoveNumber() ;
		while(numberNode < this.allMovesNumber) {
			if(treePolicynode.getUntriedMoves().size() != 0) {
				Node newNode =  treePolicynode.expand(boardtreePolicy, this.color);
				numberNode = treePolicynode.getMoveNumber() ;
				return newNode;
			} else {
				String color = treePolicynode.getColor();
				try {
					treePolicynode = bestChild(treePolicynode, this.c);
				} catch(Exception e) {
					//node is a terminal state.
					return treePolicynode;
				}
				try {
					boardtreePolicy.makeMove(treePolicynode.getMove(), color);
				} catch (Exception e) {
					System.out.println("EXception when making move");
					System.out.println(">>" + treePolicynode.getParent().getMoveNumber());
					System.exit(0);
				}
			}
		}
		return treePolicynode;
	}
	
	/**
	 * This method conducts self-played game with random moves until the board
	 * is completely fulfilled. 
	 * @param node Node from which simulation takes place.
	 * @param board Board that serves as a starting point.
	 * @return winning side: "w" for white, "b" for black, "0" for draw.
	 * @throws Exception ???
	 */
	private String defaultPolicy(Node node_policy, Board board_policy) throws Exception {
		Random generator = new Random();
		String color = node_policy.getColor();
		int moveNumber = node_policy.getMoveNumber();
		String w = "w";
		int tenh10 = 10;
		while(moveNumber < this.allMovesNumber) {
			List<Tuple<Integer, Integer>> listValidMoves;
			if(color.equals(this.color)) {
				listValidMoves = board_policy.heuristic_bestX_moves(color, tenh10);
			} else {
				listValidMoves = board_policy.getListValidMoves();
			}
			board_policy.makeMove(listValidMoves.get(generator.nextInt(listValidMoves.size())), color);
			if(color.equals(w)) {
				color = "b";
			} else {
				color = "w";
			}
			++moveNumber;
		}
		return Rules.calculateScore(board_policy);
	}
	
	/**
	 * This method back propagates the outcome of the simulation throughout the 
	 * game tree until it reaches root.
	 * @param node Leaf node which originate the simulation.
	 * @param delta The outcome of the simulation (w/b/0) -- which side came out 
	 * as a winner, or was there a draw.
	 */
//	private void backUp(Node node, String delta) {
//		double value;
//		
//		if(delta.equals("0")) {
//			value = .5;
//		} else if(node.getColor().equals(delta)) {
//			value = 0;
//		} else {
//			value = 1;
//		}
//		
//		while(node != null) {
//			node.updateValue(value);
//			node.updateVisit();
//			
//			if(value == 1) {
//				value = (value + 1) % 2;
//			}
//			
//			node = node.getParent();
//		}
//	}
	
//	private void backUp2(Node node, String delta) {
//		double value;
//		if(delta.equals("0")) {
//			value = .5;
//		} else if(delta.equals("w")) {
//			value = 1;
//		} else {
//			value = 0;
//		}
//		
//		while(node != null) {
//			node.updateValue(value);
//			node.updateVisit();
//			node = node.getParent();
//		}
//	}
	
	/**
	 * Propagate information whether MC won or not.
	 * @param node
	 * @param delta
	 */
	private void back_up(Node node, String delta) {
		double value;
		String zero = "0";
		
		if(delta.equals(zero)) {
			value = .5;
		} else if(delta.equals(node.getColor())) {
			value = 0;
		} else {
			value = 1;
		}
		
		while(node != null) {
			node.updateValue(value);
			node.updateVisit();
			
			if(value != .5) {
				value = (value + 1) % 2;
			}
			
			node = node.getParent();
		}
	}
	
//	private void printTree() {
//		Node node = this.root.getRoot();
//		int i = 1;
//		for(Node child : node.getChildren()) {
//			System.out.println("Child # " + i);
//			i++;
//			System.out.println("move: " + child.getMove().toString());
//			System.out.println("Value: " + child.getValue());
//			System.out.println("Visit: " + child.getVisit());
//		}
//	}
	
	/**
	 * Select the best child based on evaluation function (UCT).
	 */
	private Node bestChild(Node node_nbc, double bc_c) {
		Node bestChild = null;
		double tempScore = -1;
		for(Node child: node_nbc.getChildren()) {
			double score = (child.getValue() / child.getVisit()) + 
					(bc_c * Math.sqrt((2 * Math.log(node_nbc.getVisit())) / 
							(child.getVisit())));
			if(score >= tempScore) {
				bestChild = child;
				tempScore = score;
			}
		}
		return bestChild;
	}
}