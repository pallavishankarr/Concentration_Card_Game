import java.util.ArrayList;
import java.util.Arrays;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


// represents a card
class Card {
  String rank;
  String suit;
  int value;
  boolean isFaceUp;
  boolean isMatched;

  Card(String rank, String suit, int value) {
    this.rank = rank;
    this.suit = suit;
    this.value = value;
    this.isFaceUp = false;
    this.isMatched = false;
  }

  // flips a card
  void flip() {
    this.isFaceUp = !this.isFaceUp;
  }

  // displays a card based on whether it is face up, face down, or matched
  WorldImage display() {
    if (this.isMatched) {
      return new RectangleImage(50, 70, OutlineMode.SOLID, Color.pink);
    } 
    
    else if (this.isFaceUp) {
      // card is face up, shows content
      WorldImage background = new RectangleImage(50, 70, OutlineMode.SOLID, Color.white);
      WorldImage outline = new RectangleImage(52, 72, OutlineMode.OUTLINE, Color.black);
      WorldImage content = new TextImage(this.rank + this.suit, 20, Color.black);
      // background and outline
      return content.overlayImages(background).overlayImages(outline);
    } 
    
    else {
      // card is face down
      return new RectangleImage(50, 70, OutlineMode.SOLID, Color.gray);
    }
  }
}

// represents a deck of cards
class Deck {
  ArrayList<Card> cards;

  Deck() {
    cards = new ArrayList<>();

    ArrayList<String> suits = new ArrayList<>(Arrays.asList("♣", "♦", "♥", "♠"));
    ArrayList<String> ranks = new ArrayList<>(
        Arrays.asList("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"));
    ArrayList<Integer> values = 
        new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13));

    for (String suit : suits) {
      for (int i = 0; i < ranks.size(); i++) {
        cards.add(new Card(ranks.get(i), suit, values.get(i)));
      }
    }

    // shuffle the deck
    shuffleDeck();
  }

  void shuffleDeck() {
    int n = cards.size();

    for (int i = 0; i < n; i++) {
      int randomIndex = i + (int) (Math.random() * (n - i));
      Card temp = cards.get(i);
      cards.set(i, cards.get(randomIndex));
      cards.set(randomIndex, temp);
    }
  }
}

// 
class Board extends World {
  ArrayList<ArrayList<Card>> grid;
  int score;
  Card firstCard;
  Card secondCard;
  boolean gameWon;
  boolean isFlipped;

  Board() {
    this.score = 26;
    this.grid = new ArrayList<>();
    this.firstCard = null;
    this.secondCard = null;
    this.gameWon = false;
    this.createBoard();
    this.isFlipped = false;
  }

  // Initializes the game board
  void createBoard() {
    Deck deck = new Deck();
    int index = 0;
    for (int i = 0; i < 4; i++) {
      ArrayList<Card> row = new ArrayList<>();
      for (int j = 0; j < 13; j++) {
        row.add(deck.cards.get(index));
        index++;
      }
      grid.add(row);
    }
  }

  @Override
  public WorldScene makeScene() {
    int cardWidth = 50;   
    int cardHeight = 70;  
    int xSpacing = 30;    
    int ySpacing = 50;    
    int rowCount = 4;     
    int columnCount = 13; 
    int startX = 80; 
    int startY = 80; 
    WorldScene scene = new WorldScene(1200, 600); 

    // place each card on the scene 
    for (int i = 0; i < rowCount; i++) {
      for (int j = 0; j < columnCount; j++) {
        // get the specific card
        Card card = this.grid.get(i).get(j);
        // calculate x and y position of card
        int xPosition = startX + j * (cardWidth + xSpacing);
        int yPosition = startY + i * (cardHeight + ySpacing);
        
        scene.placeImageXY(card.display(), xPosition, yPosition);
      }
    }

    scene.placeImageXY(new TextImage("Score: " + this.score, 20, Color.black), 1100, 20);

    if (gameWon) {
      scene.placeImageXY(new TextImage("You Win!", 50, Color.green), 600, 20);
    }

    return scene;
  }

  public void onMouseClicked(Posn posn) {
    int cardWidth = 50;   
    int cardHeight = 70;  
    int xSpacing = 30;    
    int ySpacing = 50;    
    int rowCount = 4;     
    int columnCount = 13; 
    int startX = 80; 
    int startY = 80; 

    for (int i = 0; i < rowCount; i++) {
      for (int j = 0; j < columnCount; j++) {
        int xPosition = startX + j * (cardWidth + xSpacing);
        int yPosition = startY + i * (cardHeight + ySpacing);

        // checks if the click is within the cards bounds
        if (posn.x >= xPosition && posn.x < xPosition + cardWidth &&
            posn.y >= yPosition && posn.y < yPosition + cardHeight) {

          Card clickedCard = this.grid.get(i).get(j);

          // if card is already face up or matched ignore
          if (clickedCard.isFaceUp || clickedCard.isMatched) {
            return;
          }

          // checks if cards match, if they don't flip them back 
          if (this.firstCard != null && this.secondCard != null) {
            if (this.firstCard.value != this.secondCard.value) {
              this.firstCard.flip();
              this.secondCard.flip();
            }
            // reset first and second card
            this.firstCard = null;
            this.secondCard = null;
          }

          // store first clicked card as firstCard
          if (this.firstCard == null) {
            this.firstCard = clickedCard;
            this.firstCard.flip();
          }

          // store second clicked card as secondCard
          else if (this.secondCard == null) {
            this.secondCard = clickedCard;
            this.secondCard.flip();

            // check if matched
            if (this.firstCard.value == this.secondCard.value) {
              // decrease score if matched
              this.score--;

              // if all pairs found
              if (this.score == 0) {
                this.gameWon = true; 
              }

              this.firstCard.isMatched = true;
              this.secondCard.isMatched = true;

              // reset first and second cards if not a match
              this.firstCard = null;
              this.secondCard = null;
            }
          }
        }
      }
    }
  }

  void resetBoard() {
    Deck deck = new Deck(); 
    this.grid = new ArrayList<>();
    int rowCount = 4;     
    int columnCount = 13; 
    int index = 0;
    
    for (int i = 0; i < rowCount; i++) {
      ArrayList<Card> row = new ArrayList<>();
      for (int j = 0; j < columnCount; j++) {
        row.add(deck.cards.get(index)); 
        index++;
      }
      this.grid.add(row);
    }

    this.score = 26;
    this.firstCard = null;
    this.secondCard = null;
    this.gameWon = false;
  }

  public void onKeyEvent(String key) {
    if (key.equals("r") || key.equals("R")) {
      resetBoard();
    }
  }
}

class ExamplesCard {

  public static void runExample() {
    Board board = new Board();

    board.bigBang(1200, 600, 0.1);
  }

  public void testCardFlipping(Tester t) {
    Card card = new Card("A", "♠", 1);
    t.checkExpect(card.isFaceUp, false); 
    card.flip();
    t.checkExpect(card.isFaceUp, true); 
  }

  public void testDeckShuffle(Tester t) {
    Deck deck = new Deck();
    ArrayList<Card> originalDeck = new ArrayList<>(deck.cards);
    deck.shuffleDeck();

    t.checkExpect(originalDeck.equals(deck.cards), false);
  }

  public static void main(String[] args) {
    runExample();
  }
}