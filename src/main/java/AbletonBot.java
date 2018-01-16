import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;

/**
 * Created by Matt on 2017-12-04.
 */
public class AbletonBot {
    int savedX,savedY,soloX,soloY,trackCount,stemCount,pixelDiff,scrollX,scrollY=0;
    Robot bot;
    Color color;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    boolean mainLoopCondition = true;
    boolean hasScroll;
    Color BEFORE_SOLO_COLOR= new Color(141,141,141);
    Color SOLO_BORDER_COLOR= new Color(50, 50, 50);
    Color IN_SOLO_COLOR = new Color(153,153,153);
    Color OTHER_AFTER_SOLO_COLOR = new Color(122,122,122);
    Color ABLETON_BORDER_COLOR = new Color(118,118,118);
    //Double SCROLL_RATIO = 1.3333333333333;

    public AbletonBot() throws AWTException {
        this.bot = new Robot();
    }
    //TODO: Replace all color seraching with grabbing screens (particularly for looped color grabbing)
    //TODO: Make the scroll much bigger, currently 3 (Entails pixel diff growing in proportion)
    //TODO: More comments
    //Starts the auto-stemming process given the array of stem group colors.
    public void start(Color[]colors){
        bot.mouseWheel(-100);
        System.out.println("Starting job!");
        hasScroll = checkIfHasScroll();
        gettingTrueSoloLocation();

        bot.delay(2000);
        for(int j=0;j<stemCount;j++){


            mainLoopCondition = true;
            Coords latestSolo= new Coords(soloX,soloY);

            bot.mouseMove(latestSolo.x,latestSolo.y);

            robotMouseClick(latestSolo);
            robotMouseClick(latestSolo);
            bot.mouseWheel(-100);

            Color soloOnColor= new Color(40,131,201);
            Color realSoloColor = bot.getPixelColor(latestSolo.x,latestSolo.y);
            if(realSoloColor.equals(soloOnColor)){
                robotMouseClick(latestSolo);
            }
            else{
                robotMouseClick(latestSolo);
                robotMouseClick(latestSolo);
            }


            if(trackIsColor(latestSolo, colors[j])){
                //bot.delay(75);
                robotMouseClick(latestSolo);
                //bot.delay(75);
            }
            bot.delay(150);
            bot.keyPress(KeyEvent.VK_CONTROL);
            //for(int i=1;i<trackCount;i++){
            while(mainLoopCondition){

                latestSolo = findNextSolo(latestSolo);
                if(latestSolo.x==-1){
                    break;
                }
                if(trackIsColor(latestSolo, colors[j])){

                    robotMouseClick(latestSolo);

                }


            }
            bot.keyRelease(KeyEvent.VK_CONTROL);
            System.out.println("\t\tExporting stem group "+(j+1));
            export("Stems "+(j+1));
        }
        System.out.println("Finished job.");
        System.exit(0);
    }

    public boolean haveSoloLocation(){
        return soloX != 0;
    }
    private Robot getBot(){
        return this.bot;
    }
    public Color getColor(){
        return color;
    }
    public void setTrackCount(int count){
        this.trackCount = count;
    }
    public void setStemCount(int count){
        this.stemCount = count;
    }

    //Places a transluscent full screen JFrame used to capture the user's
    //next mouse click and save the color of the pixel clicked.
    public void colorClick(JButton button,Color[] colorArray,int i){
        final JFrame invisFrame = new JFrame();
        final JButton tempButton = button;
        final Color[] ca = colorArray;
        final int index = i;

        invisFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        invisFrame.setUndecorated(true);
        invisFrame.setVisible(true);

        invisFrame.setOpacity(0.10f);
        invisFrame.addMouseListener(new MouseAdapter() {// provides empty implementation of all
            // MouseListener`s methods, allowing us to
            // override only those which interests us
            @Override
            public void mousePressed(MouseEvent e) {
                System.out.print("clicked");
                int x = e.getX();
                int y = e.getY();
                savedX = x;
                savedY =y;
                invisFrame.setVisible(false);
                invisFrame.dispose();
                color = getBot().getPixelColor(x,y);
                //
                tempButton.setBackground(color);
                tempButton.setForeground(color);
                ca[index]=color;
            }
        });


    }

    public void gettingTrueSoloLocation(){
        Color currCol = bot.getPixelColor(soloX,soloY);
        Color prevCol;
        while(!currCol.equals(SOLO_BORDER_COLOR)){
            currCol=bot.getPixelColor(soloX,soloY);
            soloX+=1;
        }
        soloX=soloX-2;
    }
    //Places a transluscent full screen JFrame used to capture the user's
    //next mouse click and save the given 'first solo button' location.
    public void soloClick(){
        final JFrame invisFrame = new JFrame();

        invisFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        invisFrame.setUndecorated(true);
        invisFrame.setVisible(true);

        invisFrame.setOpacity(0.10f);
        invisFrame.addMouseListener(new MouseAdapter() {// provides empty implementation of all
            // MouseListener`s methods, allowing us to
            // override only those which interests us
            @Override
            public void mousePressed(MouseEvent e) {
                soloX  = e.getX();
                soloY = e.getY();


                invisFrame.setVisible(false);
                invisFrame.dispose();


            }
        });
    }

    //Boolean function which checks (given the track's solo button coords)
    //if the track's color is equal to the given color.
    public boolean trackIsColor(Coords soloCoords, Color color){
        Coords trackCoords = new Coords(savedX,soloCoords.y);
        Color trackColor = getTrackColor(trackCoords);
        return trackColor.equals(color);

    }

    //Given the coords of a track, find out its color by looking for the closest non grey pixel to the border.
    public Color getTrackColor(Coords trackCoords){
        Color prevCol=new Color(255,255,255);
        Color currCol=new Color(255,255,255);
        while(!currCol.equals(new Color(90,90,90))){

            prevCol=bot.getPixelColor(trackCoords.x-1,trackCoords.y);

            currCol=bot.getPixelColor(trackCoords.x,trackCoords.y);
            trackCoords.x+=1;

        }
        savedX= trackCoords.x-1;
        return prevCol;

    }

    //Boolean helper function for the'findNextSolo' function.
    public boolean soloColors(Color prevCol,Color currCol,Color nextCol){
        return(prevCol.equals(BEFORE_SOLO_COLOR))
                && currCol.equals(SOLO_BORDER_COLOR)
                && (nextCol.equals(IN_SOLO_COLOR)
                || nextCol.equals(OTHER_AFTER_SOLO_COLOR));
    }

    //Given the coords of the most recent found 'Solo button', will find the next.
    public Coords findNextSolo(Coords latestSolo){

        int pixelX = latestSolo.x;
        int pixelY = latestSolo.y;

        Color prevCol=new Color(255,255,255);
        Color currCol;
        Color nextCol;
        while(pixelY <screenSize.getHeight()){

            if(needToScroll(new Coords(pixelX,pixelY))){
                if(hasScroll) {
                    Coords beforeScroll = new Coords(pixelX, pixelY);
                    Coords afterScroll = scroll(beforeScroll, latestSolo);
                    if( afterScroll.equals(beforeScroll)){
                        return afterScroll;
                    }
                    pixelX = afterScroll.x;
                    pixelY = afterScroll.y;
                }
                else{
                    return new Coords(-1,-1);
                }

            }

            //Moving while looking
            //bot.mouseMove(pixelX,pixelY);
            currCol= bot.getPixelColor(pixelX,pixelY);

            //if 4 border pixels in a row:
            //  scroll down,
            //  count pixels scroll bar moved,
            //  return to latestSolo but accounting for pixelsMoved
            //Continue
            if (currCol.equals(SOLO_BORDER_COLOR)) {
                nextCol=bot.getPixelColor(pixelX,pixelY+3);
                if(soloColors(prevCol,currCol,nextCol)){

                    bot.mouseMove(pixelX,pixelY);
                    return new Coords(pixelX,pixelY);
                }
            }

            prevCol=currCol;
            pixelY += 1;
        }
        return new Coords(-1,-1);
    }

    public boolean checkIfHasScroll(){
        int x = soloX;
        int y = soloY;
        Color scrollColor = new Color(52,55,57);
        Color currCol = bot.getPixelColor(x,y);
        while(x <screenSize.getWidth()){
            x+=1;
            currCol = bot.getPixelColor(x,y);
            if(currCol.equals(scrollColor)){
                scrollX=x;
                scrollY=y;
                return true;
            }
        }
        //move to where scroll would be located on the x axis and search y axis
        x-=16;
        while(y <screenSize.getHeight()){
            y+=1;
            currCol = bot.getPixelColor(x,y);
            if(currCol.equals(scrollColor)){
                scrollX=x;
                scrollY=y;
                return true;
            }
        }
        while(y >1){
            y-=1;
            currCol = bot.getPixelColor(x,y);
            if(currCol.equals(scrollColor)){
                scrollX=x;
                scrollY=y;
                return true;
            }
        }
        return false;


    }
    //Scrolls down and returns to last solo button known adjusting for scroll
    public Coords scroll(Coords coord, Coords latestSolo){

        Coords originalLocation = new Coords(coord.x,coord.y);
        bot.keyRelease(KeyEvent.VK_CONTROL);
        bot.delay(100);

        int scrollDiff = getScrollDifference(coord);
        if(scrollDiff ==-1 || scrollDiff==0){
            mainLoopCondition = false;
            return coord;
        }
        bot.mouseMove(scrollX,scrollY);
        scrollY-=scrollDiff;
        bot.mouseMove(scrollX,scrollY);
        //bot.mouseWheel(3);

        //Hard Coded the amount for the bot to go up
        // in the tracks to account for the scroll.
        pixelDiff=70;


        bot.mouseMove(originalLocation.x,originalLocation.y-pixelDiff);
        bot.delay(5);
        bot.keyPress(KeyEvent.VK_CONTROL);
        return new Coords(originalLocation.x,originalLocation.y-pixelDiff);


    }

    public int getScrollDifference(Coords coord){
        Color scrollColor = new Color(52,55,57);
        Color endOfScrollColor = new Color(81,81,81);
        int x = scrollX;
        int y = scrollY;
        if(scrollY==215){
            bot.mouseMove(scrollX,scrollY);

        }

        //find scroll bar
        //Color currentColor=bot.getPixelColor(x,y);
//        while(!currentColor.equals(scrollColor)){
//            x+=1;
//            bot.mouseMove(x,y);
//            currentColor=bot.getPixelColor(x,y);
//        }
//        int scrollX=x;
        //Find end of scroll bar going down
        while(!bot.getPixelColor(x,y).equals(endOfScrollColor)){
            y+=1;
        }


        int newScrollY = y-1;
        bot.mouseMove(scrollX,newScrollY);

        bot.mouseWheel(3);
        int afterScrollx= scrollX;
        int afterScrolly= newScrollY;

        while(!bot.getPixelColor(afterScrollx,afterScrolly).equals(endOfScrollColor)){
            afterScrolly+=1;
            bot.mouseMove(afterScrollx,afterScrolly);
        }

        return newScrollY - afterScrolly;
    }
    //Checks if we are at the 4 pixel (50,50,50) border and need to scroll
    public boolean needToScroll(Coords coord){

        //TODO: make all colors global
        Color borderColor = new Color(90,90,90);


        Color pixelOne=bot.getPixelColor(coord.x,coord.y);
        if(!pixelOne.equals(borderColor)){
            return false;
        }
        Color pixelTwo=bot.getPixelColor(coord.x,coord.y+1);
        Color pixelThree= bot.getPixelColor(coord.x,coord.y+2);
        Color pixelFour = bot.getPixelColor(coord.x,coord.y+3);
        return pixelOne.equals(borderColor) &&
                pixelTwo.equals(borderColor) &&
               pixelThree.equals(borderColor) &&
                pixelFour.equals(borderColor);

    }
    //The Java Robot will:
    //1. Hit the appropriate hotkeys to export audio in Ableton
    //2. Follow through with the exporting the audio with the given exportName file name
    //3. Wait for the export to be finished before returning
    public void export(String exportName){


        bot.keyPress(KeyEvent.VK_CONTROL);
        bot.keyPress(KeyEvent.VK_SHIFT);
        bot.keyPress(KeyEvent.VK_R);
        bot.delay(100);
        bot.keyRelease(KeyEvent.VK_CONTROL);
        bot.keyRelease(KeyEvent.VK_SHIFT);
        bot.keyRelease(KeyEvent.VK_R);
        bot.delay(600);
        keyPress(KeyEvent.VK_ENTER);
        bot.delay(500);
        robotType(exportName);
        bot.delay(300);
        keyPress(KeyEvent.VK_ENTER);



        //TODO figure out how to handle overwriting.. default to yes for now, ask user for filename
        keyPress(KeyEvent.VK_LEFT);
        keyPress(KeyEvent.VK_ENTER);

        //TODO wait for export done
        bot.delay(5000);
    }

    //The Java Robot presses and releases the key corresponding to the given key code.
    public void keyPress(int code){
        bot.keyPress(code);
        bot.delay(100);
        bot.keyRelease(code);
    }

    //Function to have to java Robot 'type' the given text using the system clipboard.
    public void robotType(String text){
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);
        bot.keyPress(KeyEvent.VK_CONTROL);
        bot.keyPress(KeyEvent.VK_V);
        bot.keyRelease(KeyEvent.VK_V);
        bot.keyRelease(KeyEvent.VK_CONTROL);
    }

    //Function which uses the java Robot to click at given coords
    private void robotMouseClick(Coords coord) {

        bot.mouseMove(coord.x, coord.y);
        bot.delay(50);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.delay(50);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    //Class for passing around pixel coordinates
    public class Coords {
        int x;
        int y;
        public  Coords(int x,int y){
            this.x =x;
            this.y=y;
        }
        @Override
        public boolean equals(Object other){
            Coords toCompare = (Coords)other;
            return (toCompare.x == this.x) && (toCompare.y == this.y);
        }

    }
}
