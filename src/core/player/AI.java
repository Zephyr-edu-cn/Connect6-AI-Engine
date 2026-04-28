package core.player;

import core.board.Board;
import core.board.PieceColor;
import core.game.Move;

import java.util.Random;

public abstract class AI extends Player {
	
	public AI() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public final boolean isManual() {
		// TODO Auto-generated method stub
		return false;
	}

	public final Move firstMove() {
		Random rand = new Random();
		int index = rand.nextInt(firstMoves.length);
		return firstMoves[index];
	}

	public final Move findMove(Move opponentMove) throws Exception{
		//System.out.println("[CORE AI] " + this.name() + " findMove called");
		//System.out.println("[CORE AI] My color: " + this.getColor());
		//System.out.println("[CORE AI] opponentMove: " + opponentMove);
		//System.out.println("[CORE AI] board is null? " + (this.board == null));

		if (opponentMove == null) {
			//System.out.println("[CORE AI] No opponent move, calling firstMove()");
			Move move = firstMove();
			//System.out.println("[CORE AI] First move: " + move);
			this.board.makeMove(move);
			return move;
		}

		//System.out.println("[CORE AI] Calling findNextMove");
		return findNextMove(opponentMove);
	}

	protected abstract Move findNextMove(Move opponentMove);

	//开局库
	private static Move[] firstMoves = {
			new Move('J', 'K', 'K', 'I'), // 定式1
			new Move('I', 'K', 'L', 'J'), // 定式2
			new Move('I', 'K', 'L', 'K'), // 定式3
			new Move('I', 'K', 'J', 'L'), // 定式4
			new Move('J', 'K', 'K', 'J'), // 定式5
			new Move('I', 'K', 'K', 'L'), // 定式6
			new Move('J', 'L', 'L', 'J'), // 定式7
			new Move('J', 'K', 'K', 'K'), // 定式8
			new Move('J', 'L', 'L', 'L'), // 定式9
			new Move('K', 'I', 'I', 'K'), // 定式10
			new Move('J', 'K', 'L', 'J'), // 定式11
			new Move('K', 'H', 'J', 'K'), // 定式12
			new Move('L', 'H', 'J', 'L'), // 定式13
			new Move('J', 'I', 'J', 'K'), // 定式14
			new Move('I', 'L', 'K', 'L'), // 定式15
			new Move('J', 'H', 'J', 'L'), // 定式16
			new Move('J', 'L', 'L', 'K'), // 定式17
			new Move('J', 'K', 'K', 'L'), // 定式18
			new Move('L', 'I', 'J', 'K'), // 定式19
			new Move('I', 'K', 'L', 'K'), // 定式20
			new Move('J', 'L', 'K', 'L'), // 定式21
			new Move('L', 'I', 'I', 'K'), // 定式22
			new Move('I', 'K', 'I', 'L'), // 定式23
			new Move('L', 'H', 'J', 'K'), // 定式24
			new Move('J', 'K', 'L', 'K'), // 定式25
			new Move('J', 'K', 'J', 'L') // 定式26

			// 定式9, 3, 2, 8, 12, 19, 20, 23, 后手方赢了。
			
			// MaxDepth = 2: 黑胜：26, 9, 3, 4(42), 5, 6, 10, 11
			//               白胜：2, 1, 8(43)
	};

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}

	protected Board board;

	@Override
	public String fullName(){
		if (_myColor == PieceColor.BLACK) return "黑方：" + name();
		if (_myColor == PieceColor.WHITE) return "白方：" + name();
		return "Empty";
	}
}