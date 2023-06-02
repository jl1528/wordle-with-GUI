package gurdle.gui;

import gurdle.CharChoice;
import gurdle.Model;
import gurdle.ptui.Turdle;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.AudioClip;
import util.Observer;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.*;

/**
 * The graphical user interface to the Wordle game model in
 * {@link Model}.
 *
 * @author Jonathan Luo
 */
public class Gurdle extends Application
        implements Observer< Model, String > {
    /** View/Controller access to model*/
    private Model model;
    /** current row*/
    private int cursorRow = 0;
    /** current column*/
    private int cursorCol = 0;
    /** text at the top of the gurdle game*/
    private Label text;
    /** the label for the amount of guesses the usermade*/
    private Label guess;
    /** Used to prevent this class displaying any info before the UI has been completely set up.*/
    private boolean initialized;
    /** arraylist of characters for a single guess*/
    private ArrayList<Character> charList = new ArrayList<>();
    /** the map of characters to its buttons */
    private final Map<Character, Button> buttonMap = new HashMap<>();
    /** the 2d array of labels */
    private Label[][] labels;

    /**
     * Create the Wordle model and register this object as an observer
     * of it. If there was a command line argument, use that as the first
     * secret word.
     * @throws Exception
     */
    @Override public void init() throws Exception{
        model = new Model();
        model.addObserver(this);
        List<String> strings = super.getParameters().getRaw();
        gameStart(strings, this.model);
        //create label 2d array with size of 6 and 5
        labels = new Label[Model.NUM_TRIES][Model.WORD_SIZE];

    }

    /**
     * starts the game with arguments or file
     * @param strings list of words to use
     * @param model the observed subject of this observer
     * @throws Exception
     */
    public static void gameStart(List<String> strings, Model model) throws Exception {
        if ( strings.size() == 1 ) {
            final String firstWord = strings.get( 0 );
            if ( firstWord.length() == Model.WORD_SIZE ) {
                model.newGame( firstWord );
            }
            else {
                throw new Exception(
                        String.format(
                                "\"%s\" is not the required word length (%d)." +
                                        System.lineSeparator(),
                                firstWord, Model.WORD_SIZE
                        )
                );
            }
        }
        else {
            model.newGame();
        }
    }

    /**
     * the action for when user presses backspace
     * (old implementation of backspace IGNORE THIS)
     */
    public void backspaceAction(){
        if(!labels[cursorRow][cursorCol].getText().equals("") && cursorCol ==4){
            labels[cursorRow][cursorCol].setText("");
            charList.remove(cursorCol);
        }
        else if(cursorCol !=0 ) {
            labels[cursorRow][cursorCol-1].setText("");
            charList.remove(cursorCol-1);
            if(cursorCol!=0){cursorCol--;}
        }
    }

    /**
     * a helper function that does the action of resetting the grid and keyboard
     */
    public void newGame(){
        for(int r =0; r<Model.NUM_TRIES; r++) {  // loop trough rows
            for (int c = 0; c < Model.WORD_SIZE; c++){  // loop trough columns
                //reset the labels
                labels[r][c].setText("");
                labels[r][c].setStyle("""
                        -fx-font: 25px Menlo;
                        -fx-padding: 20;
                        -fx-border-style: solid inside;
                        -fx-border-width: 2;
                        -fx-border-insets: 5;
                        -fx-border-radius: 2;
                        -fx-border-color: lightgrey;
                        """);
            }
        }
        //reset the keyboard
        for(Button button:buttonMap.values()){
            button.setStyle(null);
        }
        //reset the char list
        charList= new ArrayList<>();
    }

    /**
     * create the gui and all of its buttons along with its repective actions
     * @param mainStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     */
    @Override
    public void start( Stage mainStage ) {
        initialized = true;
        mainStage.setTitle("Gurdle");
        BorderPane borderPane;
        Scene scene = new Scene( borderPane = new BorderPane());

        // top of border pane
        HBox topLabels = new HBox(100);
        guess = new Label("#guesses: " + this.model.numAttempts() );
        text = new Label("Make a guess!");
        Label secret = new Label("secret: ?");
        topLabels.getChildren().addAll(guess, text,secret);
        borderPane.setTop(topLabels);

        // center of border pane
        GridPane word = new GridPane();
        word.setHgap(5);
        word.setVgap(5);

        // loop through rows and columns to create labels at each position in the 2d array
        for(int row = 0; row < Model.NUM_TRIES; row++){
            for(int col = 0; col<Model.WORD_SIZE; col++){
                Label label = new Label("");
                labels[row][col] = label;
                label.setStyle( """
                            -fx-font: 25px Menlo;
                            -fx-padding: 20;
                            -fx-border-style: solid inside;
                            -fx-border-width: 2;
                            -fx-border-insets: 5;
                            -fx-border-radius: 2;
                            -fx-border-color: lightgray;
                """);
                word.add(label, col, row);  // add labels to gripper

            }
        }
        word.setAlignment(Pos.CENTER);  // center the grid pane
        borderPane.setCenter(word);

        // bottom of border pane
        BorderPane bottomButton = new BorderPane();
        borderPane.setBottom(bottomButton);
        BorderPane keyBoard = new BorderPane();  // create keyboard

        // create top of keyboard
        HBox topKeys = new HBox();
        Character[] topLetters = {'Q','W','E','R','T','Y','U','I','O','P'};
        for(int i = 0; i<10; i++){
            String s = ""+topLetters[i];
            Button button = new Button(s);
            buttonMap.put(topLetters[i], button);
            int finalI = i;
            button.setOnAction(event -> {if(cursorCol!=4){cursorCol++;} charList.add(topLetters[finalI]);
                model.enterNewGuessChar(topLetters[finalI]);});
            topKeys.getChildren().addAll(button);
        }
        keyBoard.setTop(topKeys);
        topKeys.setAlignment(Pos.CENTER);

        //create middle of keyboard
        HBox midKeys = new HBox();
        Character[] midLetters = {'A','S','D','F','G','H','J','K','L'};
        for(int i = 0; i<9; i++){
            String s = ""+midLetters[i];
            Button button = new Button(s);
            buttonMap.put(midLetters[i], button);
            int finalI = i;
            button.setOnAction(event -> {if(cursorCol!=4){cursorCol++;}charList.add(midLetters[finalI]);
                model.enterNewGuessChar(midLetters[finalI]);});
            midKeys.getChildren().addAll(button);
        }
        keyBoard.setCenter(midKeys);
        midKeys.setAlignment(Pos.CENTER);

        // create bottom of keyboard
        HBox bottomKeys = new HBox();
        Character[] bottomLetters = {'Z','X','C','V','B','N','M'};
        for(int i = 0; i<7; i++){
            String s = ""+ bottomLetters[i];
            Button button = new Button(s);
            buttonMap.put(bottomLetters[i], button);
            int finalI = i;
            button.setOnAction(event -> {if(cursorCol!=4){cursorCol++;}charList.add(bottomLetters[finalI]);
                model.enterNewGuessChar(bottomLetters[finalI]);});
            bottomKeys.getChildren().addAll(button);
        }
        keyBoard.setBottom(bottomKeys);
        bottomKeys.setAlignment(Pos.CENTER);
        bottomButton.setCenter(keyBoard);

        // create game options buttons
        HBox options = new HBox(10);
        Button enter = new Button("ENTER");
        enter.setOnAction(event -> {model.confirmGuess();});
        Button backspace = new Button("BACKSPACE");
        //backspace does not actually backspace ;(
        AudioClip backAudio = new AudioClip(this.getClass().getResource("Backspce.wav").toString());
        backspace.setOnAction(event -> {backAudio.play();});
        enter.setAlignment(Pos.BOTTOM_CENTER);
        bottomButton.setLeft(enter);
        bottomButton.setRight(backspace);
        bottomButton.setBottom(options);

        // make buttons for new game and cheat
        Button newGame = new Button("NEW GAME");
        Button cheat = new Button("CHEAT");
        cheat.setOnAction((event -> {secret.setText("secret: " + this.model.secret());}));
        newGame.setOnAction(event -> {model.newGame();secret.setText("secret: ?");newGame();cursorCol =0;cursorRow=0;});
        options.getChildren().addAll(newGame, cheat);
        options.setAlignment(Pos.CENTER);

        mainStage.setScene( scene );
        mainStage.show();
    }

    /**
     * Query the model to find out what's going on and display the current
     * state of all the legitimate guesses. determine the state of the game after each guess
     * @param model the object that wishes to inform this object
     *                about something that has happened.
     * @param message    optional data the model can send to the observer
     */
    @Override
    public void update( Model model, String message ) {
        if(!this.initialized){
            return;
        }
        guess.setText("#guesses: " + model.numAttempts());
        // loop trough rows and columns to get Char Choice at a position and change its
        // respective label and keyboard button colors to the colors preprinting to its position based on secret word
        for ( int guessNum = 0; guessNum < Model.NUM_TRIES; ++guessNum ) {
            for ( int charPos = 0; charPos < Model.WORD_SIZE; ++charPos ) {
                CharChoice cc = model.get( guessNum, charPos );
                labels[guessNum][charPos].setText(cc.toString());
                final char ch = cc.getChar();
                final CharChoice.Status ccStatus = cc.getStatus();
                if(ccStatus == CharChoice.Status.WRONG){
                    buttonMap.get(ch).setStyle("-fx-background-color: gray; ");
                    labels[guessNum][charPos].setStyle( """
                            -fx-font: 25px Menlo;
                            -fx-background-color: grey;
                            -fx-padding: 20;
                            -fx-text-fill: white;
                            """);
                }
                if(ccStatus ==CharChoice.Status.RIGHT_POS){
                    buttonMap.get(ch).setStyle("-fx-background-color: lightgreen; ");
                    labels[guessNum][charPos].setStyle( """
                            -fx-font: 25px Menlo;
                            -fx-background-color: lightgreen;
                            -fx-padding: 20;
                            -fx-text-fill: white;
                            """);
                }
                if(ccStatus ==CharChoice.Status.WRONG_POS){
                    buttonMap.get(ch).setStyle("-fx-background-color: orange; ");
                    labels[guessNum][charPos].setStyle( """
                            -fx-font: 25px Menlo;
                            -fx-background-color:orange;
                            -fx-padding: 20;
                            -fx-text-fill: white;
                            """);}
            }
        }
        // change the text at top of the application, based on game state
        final Model.GameState gamestate = model.gameState();
        if ( gamestate == Model.GameState.WON ) {
            text.setText("You Won!! :))");
            AudioClip win = new AudioClip(this.getClass().getResource("win.wav").toString());
            win.play();

        }
        if ( gamestate == Model.GameState.LOST ) {
            text.setText("You Lost ... :((, secrete: " + this.model.secret());
        }
        if(gamestate == Model.GameState.ILLEGAL_WORD){
            text.setText("Illegal word entered");
        }
        if(gamestate == Model.GameState.ONGOING){
            text.setText("Make a guess!");
        }

    }

    /**
     * start up the gui application
     * @param args a single, optional word, to use as the first secret word
     */
    public static void main( String[] args ) {
        if ( args.length > 1 ) {
            System.err.println( "Usage: java Gurdle [1st-secret-word]" );
        }
        Application.launch( args );
    }
}
