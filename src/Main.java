import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

// Inicialização do jogo
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessFrame::new);
    }
}

class ChessFrame extends JFrame {
    private final BoardPanel boardPanel;
    private final JLabel statusBar;

    // Construção da janela do jogo
    ChessFrame() {
        super("Jogo de Xadrez, versão 1.0 - Feito por Felipe"); // Título da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        boardPanel = new BoardPanel(); // Painel com o tabuleiro
        statusBar = new JLabel(); // Barra inferior com a vez atual
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        add(boardPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        // Atualiza a barra de status com base no turno atual (Brancas/Negras)
        boardPanel.setStatusListener(turn -> statusBar.setText(
                "Vez: " + (turn == PieceColor.WHITE ? "Brancas" : "Negras")
        ));
        // Texto inicial da barra
        statusBar.setText("Vez: " +
                (boardPanel.getTurn() == PieceColor.WHITE ? "Brancas" : "Negras"));

        pack();                           // Ajusta a janela ao conteúdo
        setLocationRelativeTo(null);      // Centraliza na tela
        setVisible(true);                 // Torna a janela visível
    }
}

class BoardPanel extends JPanel implements MouseListener {
    private static final int TILE_SIZE = 80; // Tamanho de cada quadrado do tabuleiro em pixels
    private static final int BOARD_SIZE = 8; // Número de linhas e colunas do tabuleiro

    // Matriz de peças, lista de movimentos legais, seleção atual e turno
    private final Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];
    private final List<Point> legalMoves = new ArrayList<>();
    private Point selected = null;
    private PieceColor turn = PieceColor.WHITE;
    private java.util.function.Consumer<PieceColor> statusListener = t -> {};

    BoardPanel() {
        setPreferredSize(new Dimension(TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE));
        setBackground(Color.DARK_GRAY);
        setFocusable(true);
        addMouseListener(this);
        initBoard(); // Inicializa as peças no tabuleiro
    }

    // Permite que o ChessFrame receba notificações de mudança de turno
    void setStatusListener(java.util.function.Consumer<PieceColor> listener) {
        this.statusListener = listener != null ? listener : t -> {};
    }

    public PieceColor getTurn() {
        return turn;  // Retorna de quem é a vez: WHITE (Brancas) ou BLACK (Pretas)
    }

    private void initBoard() {
        // Peças pretas na linha superior
        board[0][0] = new Piece(PieceType.ROOK, PieceColor.BLACK);
        board[0][1] = new Piece(PieceType.KNIGHT, PieceColor.BLACK);
        board[0][2] = new Piece(PieceType.BISHOP, PieceColor.BLACK);
        board[0][3] = new Piece(PieceType.QUEEN, PieceColor.BLACK);
        board[0][4] = new Piece(PieceType.KING, PieceColor.BLACK);
        board[0][5] = new Piece(PieceType.BISHOP, PieceColor.BLACK);
        board[0][6] = new Piece(PieceType.KNIGHT, PieceColor.BLACK);
        board[0][7] = new Piece(PieceType.ROOK, PieceColor.BLACK);
        for (int c = 0; c < BOARD_SIZE; c++)
            board[1][c] = new Piece(PieceType.PAWN, PieceColor.BLACK);

        // Peças brancas na base
        for (int c = 0; c < BOARD_SIZE; c++)
            board[6][c] = new Piece(PieceType.PAWN, PieceColor.WHITE);

        board[7][0] = new Piece(PieceType.ROOK, PieceColor.WHITE);
        board[7][1] = new Piece(PieceType.KNIGHT, PieceColor.WHITE);
        board[7][2] = new Piece(PieceType.BISHOP, PieceColor.WHITE);
        board[7][3] = new Piece(PieceType.QUEEN, PieceColor.WHITE);
        board[7][4] = new Piece(PieceType.KING, PieceColor.WHITE);
        board[7][5] = new Piece(PieceType.BISHOP, PieceColor.WHITE);
        board[7][6] = new Piece(PieceType.KNIGHT, PieceColor.WHITE);
        board[7][7] = new Piece(PieceType.ROOK, PieceColor.WHITE);
    }

    // Desenha o tabuleiro e as peças na tela:
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Desenha o tabuleiro em padrão quadriculado
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                boolean light = (r + c) % 2 == 0;
                g2.setColor(light ? Color.WHITE : Color.BLACK);
                g2.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // Destaque da peça selecionada e seus movimentos possíveis
        if (selected != null) {
            g2.setColor(new Color(255, 255, 0, 120));
            g2.fillRect(selected.y * TILE_SIZE, selected.x * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            g2.setColor(new Color(0, 255, 0, 120));
            for (Point p : legalMoves) {
                int x = p.y * TILE_SIZE, y = p.x * TILE_SIZE, pad = TILE_SIZE / 4;
                g2.fillOval(x + pad, y + pad, TILE_SIZE - pad * 2, TILE_SIZE - pad * 2);
            }
        }

        // Desenha as peças em Unicode com sombra
        g2.setFont(new Font("SansSerif", Font.PLAIN, (int)(TILE_SIZE * 0.75)));
        FontMetrics fm = g2.getFontMetrics();
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Piece piece = board[r][c];
                if (piece != null) {
                    String s = piece.unicode();
                    int textW = fm.stringWidth(s), textH = fm.getAscent();
                    int x = c * TILE_SIZE + (TILE_SIZE - textW) / 2;
                    int y = r * TILE_SIZE + (TILE_SIZE + textH) / 2 - 6;

                    Color pieceColor = piece.color == PieceColor.WHITE
                            ? new Color(245, 245, 245)
                            : new Color(18, 18, 18);
                    Color shadowColor = piece.color == PieceColor.WHITE
                            ? new Color(0, 0, 0, 180)
                            : new Color(255, 255, 255, 180);

                    g2.setColor(shadowColor);
                    g2.drawString(s, x + 2, y + 2);
                    g2.setColor(pieceColor);
                    g2.drawString(s, x, y);
                }
            }
        }

        // Coordenadas do tabuleiro (a–h / 1–8)
        g2.setColor(new Color(0, 0, 0, 140));
        g2.setFont(getFont().deriveFont(Font.PLAIN, 12));
        for (int c = 0; c < BOARD_SIZE; c++)
            g2.drawString(String.valueOf((char)('a' + c)),
                    c * TILE_SIZE + 4, BOARD_SIZE * TILE_SIZE - 4);
        for (int r = 0; r < BOARD_SIZE; r++)
            g2.drawString(String.valueOf(BOARD_SIZE - r),
                    2, r * TILE_SIZE + 14);

        g2.dispose();
    }

    // Faz a intereçao das das peças com o mouse
    @Override
    public void mousePressed(MouseEvent e) {
        handleClick(e); // Quando o mouse é pressionado, chamamos a função que trata o clique
    }
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    private void handleClick(MouseEvent e) {
        int c = e.getX() / TILE_SIZE, r = e.getY() / TILE_SIZE;
        if (!inBounds(r, c)) return;

        if (selected == null) {
            Piece piece = board[r][c];
            if (piece != null && piece.color == turn) {
                selected = new Point(r, c);
                recomputeLegalMoves();
                repaint();
            }
        } else {
            Piece target = board[r][c];
            if (target != null && target.color == turn) {
                selected = new Point(r, c);
                recomputeLegalMoves();
                repaint();
                return;
            }
            boolean isLegal = legalMoves.stream().anyMatch(p -> p.x == r && p.y == c);
            if (isLegal) {
                move(selected.x, selected.y, r, c);
                selected = null;
                legalMoves.clear();
                repaint();
            } else {
                selected = null;
                legalMoves.clear();
                repaint();
            }
        }
    }

    private void move(int r0, int c0, int r1, int c1) {
        Piece p = board[r0][c0];
        board[r1][c1] = p;
        board[r0][c0] = null;

        // Promoção para rainha
        if (p.type == PieceType.PAWN) {
            if (p.color == PieceColor.WHITE && r1 == 0)
                board[r1][c1] = new Piece(PieceType.QUEEN, PieceColor.WHITE);
            if (p.color == PieceColor.BLACK && r1 == 7)
                board[r1][c1] = new Piece(PieceType.QUEEN, PieceColor.BLACK);
        }

        // Alterna o turno e atualiza a barra
        turn = turn.opposite();
        statusListener.accept(turn);

        // Verifica xeque-mate
        if (isCheckmate(turn)) {
            String msg = (turn == PieceColor.WHITE ? "Pretas" : "Brancas") +
                    " venceram por xeque-mate!";
            JOptionPane.showMessageDialog(this, msg, "Xeque-mate",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Recalcula os movimentos legais da peça selecionada
    private void recomputeLegalMoves() {
        legalMoves.clear();
        if (selected == null) return;
        int r = selected.x, c = selected.y;
        Piece p = board[r][c];
        if (p == null) return;
        switch (p.type) {
            case PAWN:
                computePawnMoves(r, c, p.color);
                break;
            case KNIGHT:
                addKnightMoves(r, c, p.color);
                break;
            case BISHOP:
                addSlidingMoves(r, c, p.color, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
                break;
            case ROOK:
                addSlidingMoves(r, c, p.color, new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
                break;
            case QUEEN:
                addSlidingMoves(r, c, p.color, new int[][]{
                        {1,1},{1,-1},{-1,1},{-1,-1},{1,0},{-1,0},{0,1},{0,-1}
                });
                break;
            case KING:
                addKingMoves(r, c, p.color);
                break;
        }
        // Filtro: captura de rei e movimentos que deixam em xeque
        Iterator<Point> it = legalMoves.iterator();
        while (it.hasNext()) {
            Point mv = it.next();
            Piece target = board[mv.x][mv.y];
            if (target != null && target.type == PieceType.KING) {
                it.remove();
                continue;
            }
            if (wouldLeaveKingInCheck(r, c, mv.x, mv.y)) {
                it.remove();
            }
        }
    }

    // Verifica se o jogador de vez não tem movimentos legais e está em xeque
    private boolean isCheckmate(PieceColor currentTurn) {
        if (!isInCheck(currentTurn)) return false;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Piece piece = board[r][c];
                if (piece != null && piece.color == currentTurn) {
                    if (!computeLegalMoves(r, c).isEmpty()) return false;
                }
            }
        }
        return true;
    }

    // Verifica se o rei da cor está em xeque
    private boolean isInCheck(PieceColor color) {
        Point kingPos = findKing(color, board);
        return kingPos != null &&
                isSquareAttacked(kingPos.x, kingPos.y, color.opposite(), board);
    }

    // Computa movimentos legais válidos de (r,c) sem alterar seleção
    private List<Point> computeLegalMoves(int r, int c) {
        List<Point> moves = new ArrayList<>();
        Piece piece = board[r][c];
        if (piece == null) return moves;
        switch (piece.type) {
            case PAWN:
                computePawnMoves(r, c, piece.color, moves); break;
            case KNIGHT:
                addKnightMoves(r, c, piece.color, moves); break;
            case BISHOP:
                addSlidingMoves(r, c, piece.color,
                        new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}}, moves); break;
            case ROOK:
                addSlidingMoves(r, c, piece.color,
                        new int[][]{{1,0},{-1,0},{0,1},{0,-1}}, moves); break;
            case QUEEN:
                addSlidingMoves(r, c, piece.color,
                        new int[][]{
                                {1,1},{1,-1},{-1,1},{-1,-1},{1,0},{-1,0},{0,1},{0,-1}
                        }, moves); break;
            case KING:
                addKingMoves(r, c, piece.color, moves); break;
        }
        // Remove movimentos que deixam o rei em xeque
        moves.removeIf(mv -> wouldLeaveKingInCheck(r, c, mv.x, mv.y));
        return moves;
    }

    // Métodos de geração de movimentos
    private void computePawnMoves(int r, int c, PieceColor color, List<Point> m) {
        int dir = (color == PieceColor.WHITE) ? -1 : 1;
        int startRow = (color == PieceColor.WHITE) ? 6 : 1;
        int nextR = r + dir;
        if (inBounds(nextR, c) && board[nextR][c] == null) m.add(new Point(nextR, c));
        int twoR = r + 2 * dir;
        if (r == startRow && inBounds(nextR, c) && board[nextR][c] == null &&
                inBounds(twoR, c) && board[twoR][c] == null) m.add(new Point(twoR, c));
        for (int d : new int[]{-1, 1}) {
            int cr = r + dir, cc = c + d;
            if (inBounds(cr, cc) && board[cr][cc] != null &&
                    board[cr][cc].color != color) {
                m.add(new Point(cr, cc));
            }
        }
    }

    // Calculam os movimentos do cavalo
    private void addKnightMoves(int r, int c, PieceColor color, List<Point> m) {
        for (int[] j : new int[][]{{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}}) {
            int rr = r + j[0], cc = c + j[1];
            if (!inBounds(rr, cc)) continue;
            Piece t = board[rr][cc];
            if (t == null || t.color != color) m.add(new Point(rr, cc));
        }
    }

    // Calculam os movimentos do rei
    private void addKingMoves(int r, int c, PieceColor color, List<Point> m) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr, cc = c + dc;
                if (!inBounds(rr, cc)) continue;
                Piece t = board[rr][cc];
                if (t == null || t.color != color) m.add(new Point(rr, cc));
            }
        }
    }

    // movimentos das peças que deslizam pelo tabuleiro: bispo, torre e rainha.
    private void addSlidingMoves(int r, int c, PieceColor color,
                                 int[][] dirs, List<Point> m) {
        for (int[] d : dirs) {
            int rr = r + d[0], cc = c + d[1];
            while (inBounds(rr, cc)) {
                Piece t = board[rr][cc];
                if (t == null) {
                    m.add(new Point(rr, cc));
                } else {
                    if (t.color != color) m.add(new Point(rr, cc));
                    break;
                }
                rr += d[0]; cc += d[1];
            }
        }
    }

    // Métodos que usam a lista legalMoves padrão do tabuleiro
    private void computePawnMoves(int r, int c, PieceColor color) {
        computePawnMoves(r, c, color, legalMoves); // Calcula movimentos legais do peão
    }
    private void addKnightMoves(int r, int c, PieceColor color) {
        addKnightMoves(r, c, color, legalMoves); // Calcula movimentos do cavalo
    }
    private void addKingMoves(int r, int c, PieceColor color) {
        addKingMoves(r, c, color, legalMoves); // Calcula movimentos do rei
    }
    private void addSlidingMoves(int r, int c, PieceColor color, int[][] dirs) {
        addSlidingMoves(r, c, color, dirs, legalMoves); // Calcula movimentos de peças que deslizam (torre, bispo, rainha)
    }

    // Verifica se um movimento deixaria o rei em xeque
    private boolean wouldLeaveKingInCheck(int fromR, int fromC, int toR, int toC) {
        Piece moving = board[fromR][fromC];
        if (moving == null) return true; // Sem peça → movimento ilegal
        Piece[][] copy = copyBoard(board); // Cópia do tabuleiro
        copy[toR][toC] = copy[fromR][fromC]; // Move a peça na cópia
        copy[fromR][fromC] = null;
        Point kingPos = findKing(moving.color, copy); // Posição do rei da cor
        return kingPos == null || isSquareAttacked(
                kingPos.x, kingPos.y, moving.color.opposite(), copy); // Retorna true se o rei fica em xeque
    }

    // Cria cópia do tabuleiro
    private Piece[][] copyBoard(Piece[][] src) {
        Piece[][] c = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int s = 0; s < BOARD_SIZE; s++) {
                Piece p = src[r][s];
                c[r][s] = (p != null) ? new Piece(p.type, p.color) : null;
            }
        }
        return c;
    }

    // Procura o rei de determinada pela cor
    private Point findKing(PieceColor color, Piece[][] b) {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                Piece p = b[r][c];
                if (p != null && p.type == PieceType.KING && p.color == color)
                    return new Point(r, c);
            }
        }
        return null;
    }

    // Verifica se uma casa está sendo atacada pelo adversário
    private boolean isSquareAttacked(int r, int c, PieceColor attacker, Piece[][] b) {
        // Checa ataques de peões
        int pawnDir = (attacker == PieceColor.WHITE) ? -1 : 1;
        int pr = r - pawnDir;
        for (int dc : new int[]{-1, 1}) {
            int pc = c + dc;
            if (inBounds(pr, pc)) {
                Piece p = b[pr][pc];
                if (p != null && p.type == PieceType.PAWN && p.color == attacker) return true;
            }
        }

        // Checa ataques de cavalos
        for (int[] j : new int[][]{{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}}) {
            int rr = r + j[0], cc = c + j[1];
            if (inBounds(rr, cc)) {
                Piece p = b[rr][cc];
                if (p != null && p.type == PieceType.KNIGHT && p.color == attacker)
                    return true;
            }
        }

        // Checa ataques do rei
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = r + dr, cc2 = c + dc;
                if (inBounds(rr, cc2)) {
                    Piece p = b[rr][cc2];
                    if (p != null && p.type == PieceType.KING && p.color == attacker)
                        return true;
                }
            }
        }

        // Checa ataques de peças deslizantes (torre, bispo, rainha)
        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
        for (int[] d : dirs) {
            int rr = r + d[0], cc2 = c + d[1];
            while (inBounds(rr, cc2)) {
                Piece p = b[rr][cc2];
                if (p != null) {
                    if (p.color == attacker) {
                        if (Math.abs(d[0]) == Math.abs(d[1])) { // diagonal → bispo ou rainha
                            if (p.type == PieceType.BISHOP || p.type == PieceType.QUEEN)
                                return true;
                        } else { // reta → torre ou rainha
                            if (p.type == PieceType.ROOK || p.type == PieceType.QUEEN)
                                return true;
                        }
                    }
                    break; // outra peça bloqueia
                }
                rr += d[0]; cc2 += d[1];
            }
        }

        return false;
    }

    // Verifica se a posição está dentro do tabuleiro
    private boolean inBounds(int r, int c) {
        return r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE;
    }
}
// Tipos de peças do xadrez
enum PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

// Cores das peças
enum PieceColor {
    WHITE, BLACK;
    public PieceColor opposite() { return this == WHITE ? BLACK : WHITE; }
}

// Classe que representa uma peça do tabuleiro
class Piece {
    final PieceType type;
    final PieceColor color;
    Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
    }
    // Utiliza Unicode para renderizar as peças gráficas
    String unicode() {
        switch (type) {
            case KING:   return color == PieceColor.WHITE ? "♔" : "♚";
            case QUEEN:  return color == PieceColor.WHITE ? "♕" : "♛";
            case ROOK:   return color == PieceColor.WHITE ? "♖" : "♜";
            case BISHOP: return color == PieceColor.WHITE ? "♗" : "♝";
            case KNIGHT: return color == PieceColor.WHITE ? "♘" : "♞";
            case PAWN:   return color == PieceColor.WHITE ? "♙" : "♟";
        }
        return "?";
    }
}
