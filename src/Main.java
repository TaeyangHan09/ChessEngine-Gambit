public class Main
{
    public static void main(String[] args)
    {
        Board board = Board.fromFen(Board.STARTING_POSITION);
        int score = Evaluator.evaluate(board);
        System.out.println("Start position score: " + score);
    }
}