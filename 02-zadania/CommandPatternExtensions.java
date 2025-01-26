
// Interfejs dla polecenia
interface ICommand {
    void execute();
    void undo();
}

// Klasa reprezentująca pojedynczy ruch na szachownicy
class MoveCommand implements ICommand {
    private final ChessBoard board;
    private final int fromRow, fromCol, toRow, toCol;
    private ChessPiece capturedPiece;

    public MoveCommand(ChessBoard board, int fromRow, int fromCol, int toRow, int toCol) {
        this.board = board;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
    }

    @Override
    public void execute() {
        capturedPiece = board.getPiece(toRow, toCol); // Zapamiętanie zbitej figury
        board.movePiece(fromRow, fromCol, toRow, toCol); // Wykonanie ruchu
    }

    @Override
    public void undo() {
        board.movePiece(toRow, toCol, fromRow, fromCol); // Cofnięcie ruchu
        board.setPiece(toRow, toCol, capturedPiece);    // Przywrócenie zbitej figury
    }
}

// Klasa zarządzająca historią poleceń
class CommandManager {
    private final Stack<ICommand> executedCommands = new Stack<>();
    private final Stack<ICommand> undoneCommands = new Stack<>();

    public void executeCommand(ICommand command) {
        command.execute();
        executedCommands.push(command);
        undoneCommands.clear(); // Czyszczenie stosu cofniętych poleceń
    }

    public void undo() {
        if (!executedCommands.isEmpty()) {
            ICommand command = executedCommands.pop();
            command.undo();
            undoneCommands.push(command);
        }
    }

    public void redo() {
        if (!undoneCommands.isEmpty()) {
            ICommand command = undoneCommands.pop();
            command.execute();
            executedCommands.push(command);
        }
    }
}
