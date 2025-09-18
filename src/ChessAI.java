import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.awt.Point;

public class ChessAI {
    private final Random random = new Random();

    // Retorna um movimento aleatório válido para aiColor usando o BoardPanel para gerar jogadas
    public Move getBestMove(Piece[][] board, PieceColor aiColor, BoardPanel panel) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.color == aiColor) {
                    List<Point> legal = panel.computeLegalMoves(r, c);
                    for (Point mv : legal) {
                        moves.add(new Move(r, c, mv.x, mv.y));
                    }
                }
            }
        }
        if (moves.isEmpty()) return null;
        return moves.get(random.nextInt(moves.size()));
    }
}