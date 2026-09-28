public class Main{
    public static void main(String[] args){
        Board board = Board.fromFen(Board.STARTING_POSITION);
        System.out.println(board);
        System.out.println(board.toFen());
    }
}