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

  //ENHANCEMENT: checks if two cards are a match based on rank and color
  boolean isMatch(Card other) {
    // check if both cards are red suits and have the same rank
    if ((this.suit.equals("♥") || this.suit.equals("♦")) &&
        (other.suit.equals("♥") || other.suit.equals("♦")) &&
        this.rank.equals(other.rank)) {
      return true;
    }

    // check if both cards are black suits and have the same rank
    return (this.suit.equals("♠") || this.suit.equals("♣")) &&
        (other.suit.equals("♠") || other.suit.equals("♣")) &&
        this.rank.equals(other.rank);
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

      // ENHANCEMENT: sets the red suits red
      if (this.suit.equals("♥") || this.suit.equals("♦")) {
        // heart and diamond is red
        return new TextImage(this.rank + this.suit, 20, Color.red)
            .overlayImages(background).overlayImages(outline);
      } 

      else {
        // club and spade is black
        return new TextImage(this.rank + this.suit, 20, Color.black)
            .overlayImages(background).overlayImages(outline);
      }
    }

    // card is face down
    return new RectangleImage(50, 70, OutlineMode.SOLID, Color.gray);

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
        int xPosition = startX + (j * (cardWidth + xSpacing));
        int yPosition = startY + (i * (cardHeight + ySpacing));

        // checks if the click is within the cards bounds
        if (posn.x >= (xPosition - (cardWidth / 2)) && posn.x < (xPosition + (cardWidth / 2)) &&
            posn.y >= (yPosition - (cardHeight / 2)) && posn.y < (yPosition + (cardHeight / 2))) {

          Card clickedCard = this.grid.get(i).get(j);

          // if card is already face up or matched ignore
          if (clickedCard.isFaceUp || clickedCard.isMatched) {
            return;
          }

          // checks if cards match, if they don't flip them back 
          if (this.firstCard != null && this.secondCard != null) {
            if (!this.firstCard.isMatch(this.secondCard)) {
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
  Card redFaceUp;
  Card faceDown;
  Deck testDeck;
  Board testBoard;

  // initialize test data
  void initData() {
    redFaceUp = new Card("A", "♥", 1);
    redFaceUp.flip(); 

    faceDown = new Card("2", "♦", 2);

    testDeck = new Deck();
    testBoard = new Board();
  }

  // test card flipping method
  void testCardFlipping(Tester t) {
    initData();

    // flipping a face-down card
    t.checkExpect(faceDown.isFaceUp, false);
    faceDown.flip();
    t.checkExpect(faceDown.isFaceUp, true);

    // flipping a face-up card
    redFaceUp.flip();
    t.checkExpect(redFaceUp.isFaceUp, false);

    // flipping the same card multiple times
    redFaceUp.flip(); // back to face-up
    t.checkExpect(redFaceUp.isFaceUp, true);
    redFaceUp.flip(); // back to face-down
    t.checkExpect(redFaceUp.isFaceUp, false);

    // flipping a matched card 
    redFaceUp.isMatched = true;
    redFaceUp.flip();
    t.checkExpect(redFaceUp.isFaceUp, true); 
    t.checkExpect(redFaceUp.isMatched, true); 
  }

  void testBigBang(Tester t) {
    Board board = new Board();

    board.bigBang(1200, 600, 0.1);
  }
  
// test the isMatch based on color and rank 
void testIsMatch(Tester t) {
  initData();

  // cards for testing
  Card redAce1 = new Card("A", "♥", 1);  
  Card redAce2 = new Card("A", "♦", 1);  
  Card redTwo = new Card("2", "♥", 2);   
  Card blackAce1 = new Card("A", "♠", 1); 
  Card blackAce2 = new Card("A", "♣", 1); 
  Card blackTwo = new Card("2", "♠", 2);  
  Card redKing = new Card("K", "♦", 3);   
  Card blackKing = new Card("K", "♠", 3); 

  
  // same rank and color group
  t.checkExpect(redAce1.isMatch(redAce2), true); 
  t.checkExpect(blackAce1.isMatch(blackAce2), true); 

  // different rank, same color group
  t.checkExpect(redAce1.isMatch(redTwo), false); 
  t.checkExpect(blackAce1.isMatch(blackTwo), false); 
  
  // same rank, different color group
  t.checkExpect(redAce1.isMatch(blackAce1), false); 
  t.checkExpect(redTwo.isMatch(blackTwo), false);

  // different rank, different color group
  t.checkExpect(redAce1.isMatch(blackKing), false); 
  t.checkExpect(blackTwo.isMatch(redKing), false); 

  // same card comparison
  t.checkExpect(redAce1.isMatch(redAce1), true); 
  t.checkExpect(blackTwo.isMatch(blackTwo), true); 
}




}
