import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Represents a card and it's associated image. This class is a subclass of PokerCard.java
 * and calls the super class in its constructor. 
 * 
 * @author martin
 *
 */
public class GraphicalCard extends PokerCard {
	
	private Image cardImage; // The image of the card.
	
	/**
	 * Constructs a graphical card with a value and suit. Also constructs the string needed for the image so the card knows which
	 * image and its location.
	 * @param value The cards value(2 to 14).
	 * @param suit The cards suit(0 to 4).
	 */
	public GraphicalCard(int value, int suit) {
		super(value, suit);
		
		// get suit as string
		char charSuit = this.getSuitAsString().charAt(0);
		String s = Character.toString(charSuit);
		
		// get value as string
		String v;
		if(this.getValue() >= 2 && this.getValue() <= 10)
			v = this.getValueAsString();
		else
			v = Character.toString(this.getValueAsString().charAt(0));
		 
		// combine suit and string to get the card image
		String card = "cards/" + (s+v) + ".jpg";
		cardImage = new Image(card);
	}
	
	/**
	 * Draws the card on the GraphicsContext at the specified x and y values.
	 * @param g The GraphicsContext being drawn on.
	 * @param x The upper left corner x-coordinate.
	 * @param y The upper left corner y-coordinate.
	 */
	public void drawCard(GraphicsContext g, double x, double y) {
		g.drawImage(cardImage, x, y);
	}
}
