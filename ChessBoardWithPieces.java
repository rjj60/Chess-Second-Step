import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

/**
 * The ChessBoardWithPieces class represents a simple chess board GUI with drag-and-drop functionality
 * for moving chess pieces. The board allows users to drag and drop pieces to new positions, save/load
 * the board state, and detect when a king is captured to declare a winner.
 */
public class ChessBoardWithPieces {
    /** Unicode representations for white and black chess pieces, ordered as King, Queen, Rook, Bishop, Knight, and Pawn. */

    private static final String[] CHESS_PIECE_UNICODES = {
            "\u2654", "\u2655", "\u2656", "\u2657", "\u2658", "\u2659",
            "\u265A", "\u265B", "\u265C", "\u265D", "\u265E", "\u265F"
    };
    /**
     * 2D arrays representing the state of the chess board and GUI components:
     * - chessBoardGrid: Stores the Unicode representation of each piece's position on the board.
     * - labels: Stores JLabels for each square on the board to display the pieces.
     * - draggedPieceRow and draggedPieceCol: Track the row and column indices of the piece being dragged.
     */
    private static final String[][] chessBoardGrid = new String[8][8];
    private static final JLabel[][] labels = new JLabel[8][8];
    private static int draggedPieceRow = -1, draggedPieceCol = -1;

    /**
     * Main method that initializes and displays the chess board GUI.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        initializeBoard();
        JFrame frame = createFrame();
        JPanel chessBoardGridPanel = createChessBoardPanel();

        frame.add(chessBoardGridPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Creates the main JFrame for the chess board and sets up the menu bar.
     *
     * @return JFrame configured with the chess board title and menu bar.
     */
    private static JFrame createFrame() {
        JFrame frame = new JFrame("Chess Board");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");

        JMenuItem newGameItem = new JMenuItem("New Game");
        newGameItem.addActionListener(e -> resetBoard());

        JMenuItem saveGameItem = new JMenuItem("Save Game");
        saveGameItem.addActionListener(e -> saveGame());

        JMenuItem loadGameItem = new JMenuItem("Load Game");
        loadGameItem.addActionListener(e -> loadGame());

        gameMenu.add(newGameItem);
        gameMenu.add(saveGameItem);
        gameMenu.add(loadGameItem);
        menuBar.add(gameMenu);
        frame.setJMenuBar(menuBar);

        return frame;
    }

    /**
     * Creates a JPanel with an 8x8 grid layout to represent the chess board, with each square colored
     * and set up for drag-and-drop functionality.
     *
     * @return JPanel configured as the chess board.
     */
    private static JPanel createChessBoardPanel() {
        JPanel chessBoardGridPanel = new JPanel(new GridLayout(8, 8));
        chessBoardGridPanel.setPreferredSize(new Dimension(400, 400));

        Color lightColor = new Color(240, 217, 181);
        Color darkColor = new Color(181, 136, 99);

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JLabel square = new JLabel(getPieceUnicode(row, col));
                square.setFont(new Font("Serif", Font.BOLD, 32));
                square.setHorizontalAlignment(JLabel.CENTER);
                labels[row][col] = square;

                JPanel panelSquare = new JPanel(new BorderLayout());
                panelSquare.setBackground((row + col) % 2 == 0 ? lightColor : darkColor);
                makeDraggable(square, row, col);
                makeDroppable(square, row, col);
                panelSquare.add(square);
                chessBoardGridPanel.add(panelSquare);
            }
        }

        return chessBoardGridPanel;
    }

    /**
     * Resets the chess board to its initial configuration with pieces in their starting positions.
     */
    private static void resetBoard() {
        initializeBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                labels[row][col].setText(getPieceUnicode(row, col));
            }
        }
    }

    /**
     * Saves the current state of the chess board to a text file.
     */
    private static void saveGame() {
        try (FileWriter writer = new FileWriter("saved_game.txt")) {
            for (String[] row : chessBoardGrid) {
                writer.write(String.join(" ", row).replace("", ".") + "\n");
            }
            JOptionPane.showMessageDialog(null, "Game saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving the game: " + e.getMessage());
        }
    }

    /**
     * Loads the previously saved state of the chess board from a text file.
     */
    private static void loadGame() {
        try (BufferedReader reader = new BufferedReader(new FileReader("saved_game.txt"))) {
            for (int row = 0; row < 8; row++) {
                String[] pieces = reader.readLine().split(" ");
                for (int col = 0; col < 8; col++) {
                    chessBoardGrid[row][col] = pieces[col].equals(".") ? "" : pieces[col];
                    labels[row][col].setText(chessBoardGrid[row][col]);
                }
            }
            JOptionPane.showMessageDialog(null, "Game loaded successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error loading the game: " + e.getMessage());
        }
    }

    /**
     * Initializes the chess board with pieces in their starting positions.
     */
    private static void initializeBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                chessBoardGrid[row][col] = "";
            }
        }
        for (int col = 0; col < 8; col++) {
            chessBoardGrid[0][col] = getPieceUnicode(0, col);
            chessBoardGrid[7][col] = getPieceUnicode(7, col);
            chessBoardGrid[1][col] = CHESS_PIECE_UNICODES[5];
            chessBoardGrid[6][col] = CHESS_PIECE_UNICODES[11];
        }
    }

    /**
     * Sets up drag-and-drop functionality for a specific chess piece.
     *
     * @param label JLabel representing the piece to be dragged.
     * @param row   Row index of the piece.
     * @param col   Column index of the piece.
     */
    private static void makeDraggable(JLabel label, int row, int col) {
        label.setTransferHandler(new TransferHandler("text") {
            protected Transferable createTransferable(JComponent c) {
                draggedPieceRow = row;
                draggedPieceCol = col;
                return new StringSelection(label.getText());
            }

            public int getSourceActions(JComponent c) {
                return MOVE;
            }

            protected void exportDone(JComponent source, Transferable data, int action) {
                if (action == MOVE) {
                    label.setText("");
                    chessBoardGrid[draggedPieceRow][draggedPieceCol] = "";
                }
            }
        });

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JComponent comp = (JComponent) e.getSource();
                TransferHandler handler = comp.getTransferHandler();
                handler.exportAsDrag(comp, e, TransferHandler.MOVE);
            }
        });
    }

    /**
     * Enables a chess board square to accept a dropped piece and handles capturing logic.
     *
     * @param square JLabel representing the square to accept the dropped piece.
     * @param row    Row index of the destination square.
     * @param col    Column index of the destination square.
     */
    private static void makeDroppable(JLabel square, int row, int col) {
        new DropTarget(square, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                // Empty implementation, but required by DropTargetListener
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
                // Empty implementation, but required by DropTargetListener
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                // Empty implementation, but required by DropTargetListener
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    String data = (String) dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    if (isKingCaptured(row, col)) {
                        String winnerMessage = chessBoardGrid[row][col].equals(CHESS_PIECE_UNICODES[0]) ?
                                "Black wins! White King has been captured." :
                                "White wins! Black King has been captured.";
                        JOptionPane.showMessageDialog(null, winnerMessage);
                        System.exit(0); // Terminate the game
                    }
                    chessBoardGrid[row][col] = data;
                    labels[row][col].setText(data);
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    dtde.dropComplete(false);
                }
            }

            private static boolean isKingCaptured(int row, int col) {
                String piece = chessBoardGrid[row][col];
                return piece.equals(CHESS_PIECE_UNICODES[0]) || piece.equals(CHESS_PIECE_UNICODES[6]);
            }
        });
    }

    /**
     * Returns the Unicode representation of the chess piece at a given board position.
     *
     * @param row Row index on the chess board (0-7).
     * @param col Column index on the chess board (0-7).
     * @return Unicode string representing the chess piece, or an empty string if no piece is present.
     */
    private static String getPieceUnicode(int row, int col) {
        if (row == 0 || row == 7) {
            int offset = (row == 0) ? 0 : 6; // Offset for white pieces (0) or black pieces (6)
            return switch (col) {
                case 0, 7 -> CHESS_PIECE_UNICODES[2 + offset]; // Rooks
                case 1, 6 -> CHESS_PIECE_UNICODES[4 + offset]; // Knights
                case 2, 5 -> CHESS_PIECE_UNICODES[3 + offset]; // Bishops
                case 3 -> CHESS_PIECE_UNICODES[1 + offset]; // Queens
                default -> CHESS_PIECE_UNICODES[0 + offset]; // Kings
            };
        } else if (row == 1 || row == 6) {
            return CHESS_PIECE_UNICODES[(row == 1) ? 5 : 11]; // Pawns for white (row 1) or black (row 6)
        }
        return ""; // Empty square for other rows
    }
}
