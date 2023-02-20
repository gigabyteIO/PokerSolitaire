import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * A solitaire card game in which the user tries to get good poker
 * hands in each row, column, and diagonal of a 5-by-5 grid of cards.
 * The user sees one card at a time and must place that card on the
 * grid by clicking one of the empty grid positions.  The game is
 * over when all 25 grid positions have been filled.
 */
public class PokerSolitaire extends Application {

	private static final int CARD_WIDTH = 90;   // Each card image is 90 pixels wide.
	private static final int CARD_HEIGHT = 126; // Each card image is 126 pixels tall.

	private Canvas canvas;     // The canvas on which the game is played.
	private GraphicsContext g; // A graphics context for drawing on the canvas.
	
	private GraphicalDeck deck; 	// The deck of cards, 52 cards or 54 with jokers.
	private GraphicalCard[][] grid; // The 5x5 grid board that holds the cards.
	private GraphicalCard currentCard; // The current card that needs to be placed.
	private int cardsPlaced; 		// Keeps track of number of cards placed, when this is 25 the game is over.
	
	private int totalPoints;		// Keeps track of the total points that the player has scored between all hands.
	private int highScore;			// Keeps track of the highest score the player has gotten so far in a game.

	private Button newGameButton;			// Disabled initially until the game is over.
	
	private PokerRank ranker;		// Scores the hands in each row, column, and diagonal.
	
	private final int[] points = { 0, 1, 2, 3, 4, 6, 9, 25, 50, 250 }; // Points associated with hand rank (0 to 9)
	
	/* Holds the scores for the rows/columns/diagonals */
	private String[] rowScores = new String[5];
	private String[] colScores = new String[5];
	private String[] diagScores = new String[2];
	
	/**
	 *  Draw the game board, showing the grid of cards and the next card
	 *  that the user must play.
	 */
	private void draw() {
		g.setFill(Color.GREEN);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.setFill(Color.BEIGE);
		g.setStroke(Color.SIENNA);
		g.setLineWidth(2);
		// Draw the 5-by-5 grid of cards.
		for (int row = 0; row < 5; row++) {
			int y = 20 + row*(CARD_HEIGHT + 20);  // y-coordinate for cards in this row
			System.out.println("y: " + y);
			for (int col = 0; col < 5; col++) {
				int x = 20 + col*(CARD_WIDTH + 20);  // x-coordinate for cards in this column
				System.out.println("x: " + x);
				g.fillRect(x, y, CARD_WIDTH, CARD_HEIGHT); // draw empty space
				g.strokeRect(x - 1, y - 1, CARD_WIDTH + 2, CARD_HEIGHT + 2); // draw a border
			}
			System.out.println();
		}
		
		// Draw the cards
		for (int row = 0; row < 5; row++) {
			int y = 20 + row*(CARD_HEIGHT + 20);
			
			for (int col = 0; col < 5; col++) {
				int x = 20 + col*(CARD_WIDTH + 20);
				
				if(grid[row][col] != null)
					grid[row][col].drawCard(g, x, y);
			}
		}
		
		// Draw the next available card, labeled "Next Card".
		g.strokeRect(630 - 1, 50 - 1, CARD_WIDTH + 2, CARD_HEIGHT + 2); // draw a border
		g.setFill(Color.BEIGE);
		g.setFont(Font.font(20));
		g.fillText("Next Card", 625, 35);
		
		// This means it's the end of the game.
		if(cardsPlaced == 25) {
			Image cardBack = new Image("cards/back.jpg");
			g.drawImage(cardBack, 630, 50);
			g.setStroke(Color.BROWN);
			g.strokeText("Game", 645, CARD_HEIGHT - 50);
			g.strokeText("over", 653, CARD_HEIGHT - 30);
			newGameButton.setDisable(false);						
			if(totalPoints > highScore)
	    		highScore = totalPoints;		
		}
		// This means it's NOT the end of the game.
		else {
			// draw next card
			currentCard = deck.dealCard();
			currentCard.drawCard(g, 630, 50);
			cardsPlaced++;
		}
		
		int x, y;
		x = 600;
		y = CARD_HEIGHT + 100;
		/* Draw the text displaying the hands and points in rows/columns/diagonals */	
		g.setFill(Color.WHITE);
		for(int i = 0; i < rowScores.length; i++) {
			g.fillText(rowScores[i], x, y);
			y = y + 40;
		}
		for(int i = 0; i < colScores.length; i++) {
			g.fillText(colScores[i], x, y);
			y = y + 40;
		}
		for(int i = 0; i < diagScores.length; i++) {
			g.fillText(diagScores[i], x, y);
			y = y + 40;
		}
		
		/* Draw the total points at bottom of canvas */
		g.fillText("Total Points: " + totalPoints, x, y + 10);
		
		/* Draw high score box */
		g.setFill(Color.BLUE);
		g.fillRect(800, 50, 120, 80);
		g.setStroke(Color.BLACK);
		g.strokeRect(800 - 1, 50 - 1, 120, 80);
		
		g.setFill(Color.RED);
		g.fillText("High Score", 805, 70);
		
		g.setFill(Color.WHITE);
		g.fillText(Integer.toString(highScore), 848, 105);
		
		
		System.out.println(currentCard.getSuitAsString() + currentCard.getValueAsString());
	}


	/**
	 * Set up and show the window for the program.
	 */
	public void start( Stage stage ) {
		canvas = new Canvas(1000,750);
		g = canvas.getGraphicsContext2D();
		
		deck = new GraphicalDeck();
		deck.shuffle();
		grid = new GraphicalCard[5][5];
		ranker = new PokerRank();
		cardsPlaced = 0;
		totalPoints = 0;
		highScore = 0;
		score();
		
		canvas.setOnMousePressed( evt -> doMouseDown(evt.getX(), evt.getY()) );
		
		BorderPane content = new BorderPane();
		content.setCenter(canvas);
		content.setBottom(makeBottom());
		Scene scene = new Scene(content);
		stage.setScene(scene);
		stage.setTitle("Poker Solitaire");
		stage.setResizable(false);
		stage.show();
		draw();
	}

	/**
	 * This method scores the poker hands in each row, column, and the two diagonals.
	 * It updates the labels associated with each, as well as the total points.
	 */
	private void score() {
		
		// Rows
		for(int row = 0; row < 5; row++) {
			for(int col = 0; col < 5; col++) {
				
				if(grid[row][col] != null)
					ranker.add(grid[row][col]);	
			}
			if(row == 0) {
				rowScores[row] = "Row 1: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";
				totalPoints += points[ranker.getHandType()];
			}
			else if(row == 1) {
				rowScores[row] = "Row 2: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";	
				totalPoints += points[ranker.getHandType()];
			}
			else if(row == 2) {
				rowScores[row] = "Row 3: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";
				totalPoints += points[ranker.getHandType()];
			}
			else if(row == 3) {
				rowScores[row] = "Row 4: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";
				totalPoints += points[ranker.getHandType()];
			}
			else {
				rowScores[row] = "Row 5: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";
				totalPoints += points[ranker.getHandType()];
			}
			
			ranker.clear();
		}
		
		// Columns
		for(int col = 0; col < 5; col++) {
			for(int row = 0; row < 5; row++) {
				
				if(grid[row][col] != null)
					ranker.add(grid[row][col]);
			}
			if(col == 0) {
				colScores[col] = "Column 1: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";
				totalPoints += points[ranker.getHandType()];
			}
			else if(col == 1) {
				colScores[col] = "Column 2: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";	
				totalPoints += points[ranker.getHandType()];
			}
			else if(col == 2) {
				colScores[col] = "Column 3: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";
				totalPoints += points[ranker.getHandType()];
			}
			else if(col == 3) {
				colScores[col] = "Column 4: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";	
				totalPoints += points[ranker.getHandType()];
			}
			else {
				colScores[col] = "Column 5: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";
				totalPoints += points[ranker.getHandType()];
			}
			
			ranker.clear();
		}
		
		// Diagonal one
		for(int i = 0; i < 5; i++) {
			if(grid[i][i] != null)
				ranker.add(grid[i][i]);
		}
		
		diagScores[0] = "Diagonal 1: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";
		totalPoints += points[ranker.getHandType()];
		ranker.clear();
		
		// Diagonal two
		int j = 4;
		for(int i = 0; i < 5; i++) {
			if(grid[i][j] != null)
				ranker.add(grid[i][j]);
			j--;
		}
		diagScores[1] = "Diagonal 2: " + ranker.getHandTypeAsString() + " (" + points[ranker.getHandType()] + " points)";
		totalPoints += points[ranker.getHandType()];
		ranker.clear();
	}

	/**
     * Handles mouse clicks and gets the row and column of the click. 
     * If it's a valid click, it places the current card in the correct 
     * position and redraws the board.
     * @param x x-coordinate of mouse click.
     * @param y y-coordinate of mouse click.
     */
    private void doMouseDown(double x, double y) {
    	int row = getRow(y);
    	int col = getCol(x);
    	
    	if(isValid(row, col)) {
    		grid[row][col] = currentCard;
    		totalPoints = 0; // must reset this every click 
    		score();
    		draw();
    	}
    	
//    	if(isValid(getRow(y), getCol(x))) {
//    		System.out.println("That was a valid click.");
//    		System.out.println("Pressed row: " + getRow(y));
//        	System.out.println("Pressed col: " + getCol(x));
//    	}
//    	else {
//    		System.out.println("Invalid click. Please click in one of the unused boxes.");
//    	}
	}
    
    /**
     * Creates the bottom panel that holds the button for playing a new game and sets up Action listeners.
     * @return An HBox containing the buttons to control the game.
     */
    private HBox makeBottom() {
		
    	newGameButton = new Button("New Game");
    	newGameButton.setDisable(true);
    	
    	newGameButton.setOnAction( e -> {
    		doNewGame();
    	});
    	
    	HBox bottomBar = new HBox(newGameButton);
    	bottomBar.setAlignment(Pos.CENTER);
    	bottomBar.setStyle( // CSS styling for the HBox
                "-fx-padding: 5px; -fx-border-color: black; -fx-background-color: brown" );
    	
    	return bottomBar;	
    }
    
    /**
     * Setups variables for a new game and clears the board.
     */
    private void doNewGame() {
    	deck.shuffle();
    	
    	// clears the board
    	for(int row = 0; row < 5; row++) {
    		for(int col = 0; col < 5; col++) {
    			grid[row][col] = null;
    		}
    	}
    	
    	// Resets cards placed and hand scoring.
    	cardsPlaced = 0;
    	totalPoints = 0;
    	score();
    	newGameButton.setDisable(true);
    	draw();
    }
    
    /**
     * Returns the row number given a y-coordinate.
     * @param y The y-coordinate of the mouse press.
     * @return The row number of the click, -1 if not in a row.
     */
    private int getRow(double y) {
    	int row = -1; // returns -1 if outside row bounds;
    	
    	if(y > 20 && y < (20 + CARD_HEIGHT))
    		row = 0;
    	else if(y > 166 && y < (166 + CARD_HEIGHT))
    		row = 1;
    	else if(y > 312 && y < (312 + CARD_HEIGHT))
    		row = 2;
    	else if(y > 458 && y < (458 + CARD_HEIGHT))
    		row = 3;
    	else if(y > 604 && y < (604 + CARD_HEIGHT))
    		row = 4;
    	
    	return row;
    }
    
    /**
     * Returns the column number given a x-coordinate.
     * @param x The x-coordinate of the mouse press.
     * @return The column number of the click, -1 if not in a column. 
     */
    private int getCol(double x) {
    	int col = -1; // returns -1 if outside column bounds;
    	
    	if(x > 20 && x < (20 + CARD_WIDTH)) 
    		col = 0;
    	else if(x > 130 && x < (130 + CARD_WIDTH))
    		col = 1;
    	else if(x > 240 && x < (240 + CARD_WIDTH))
    		col = 2;
    	else if(x > 350 && x < (350 + CARD_WIDTH))
    		col = 3;
    	else if(x > 460 && x < (460 + CARD_WIDTH))
    		col = 4;
    	
    	return col;
    }
    
    /**
     * Determines if a given click is valid based on row and column number.
     * @param row The row.
     * @param col The column.
     * @return True if its a valid row and column number and not occupied by a current card.
     */
    private boolean isValid(int row, int col) {
    	boolean isValid;
    	
    	if(row == -1 || col == -1 || grid[row][col] != null)
    		isValid = false;
    	else
    		isValid = true;
    	
    	return isValid;   	
    }
	
	/**
	 * Launch the application by calling its start() method.
	 */
	public static void main(String[] args) {
		launch(); // (does not return; program ends when user closes the window)
	}

} // end class PokerSolitaire