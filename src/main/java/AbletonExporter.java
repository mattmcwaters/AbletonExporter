import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Created by Matt on 2017-12-01.
 */
public class AbletonExporter  {
    static AbletonBot abletonBot;
    static AbletonExporter ae;
    static JFrame stemFrame,buttonFrame;


    int stemCount;
    Color[] colorArray;
    JSpinner stemSpinner,trackCountSpinner;


    //Driver
    public static void main(String[] args) throws AWTException {
        ae  = new AbletonExporter();
        abletonBot  = new AbletonBot();
        ae.firstFrame();

    }


    //Settings frame
    public void firstFrame(){

        //Create the frame.
        stemFrame = new JFrame("Choose how many stem groups to export");

        //Optional: What happens when the frame closes?
        stemFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //Frame and panel
        stemFrame.setMinimumSize(new Dimension(600, 300));
        stemFrame.setLocationRelativeTo(null);
        JPanel spinnerPanel  = new JPanel();

        //Spinners
        SpinnerModel stemSpinnerModel = new SpinnerNumberModel(2,2,40,1);
        stemSpinner =  new JSpinner(stemSpinnerModel);
        spinnerPanel.add(stemSpinner);


//        SpinnerModel trackCountSpinnerModel = new SpinnerNumberModel(1,1,200,1);
//        trackCountSpinner =  new JSpinner(trackCountSpinnerModel);
//        spinnerPanel.add(trackCountSpinner);
          stemFrame.add(spinnerPanel);


        //TODO: ui to indicate what spinners are for

        stemFrame.setLayout(new GridLayout(1,1,2,2));
        JButton b = new JButton("Choose Stem Group Colors");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(b);
        stemFrame.add(buttonPanel);


        stemFrame.setVisible(true);
        b.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                stemFrame.setVisible(false);
                //abletonBot.setTrackCount((Integer) trackCountSpinner.getValue());

                stemCount = (Integer)stemSpinner.getValue();
                abletonBot.setStemCount(stemCount);
                colorArray = new Color[stemCount];
                ae.secondFrame();
            }
        });

    }

    //Main frame
    public void secondFrame(){
        final ArrayList<JButton> buttonArrayList = new ArrayList<JButton>();

        //Create the frame.
         buttonFrame = new JFrame("Choose your stem group Colors!");

        //Optional: What happens when the frame closes?
        buttonFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //Frame and panels
        buttonFrame.setMinimumSize(new Dimension(600, 300));
        buttonFrame.setLocationRelativeTo(null);
        JPanel buttonPanel  = new JPanel();
        JPanel goPanel = new JPanel();

        JButton goButton = new JButton("Start");

        goButton.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                boolean allColorsSelected=ae.colorArrayFull();
                if(allColorsSelected && abletonBot.haveSoloLocation()){
                    buttonFrame.setVisible(false);
                    //buttonFrame.dispose();
                    abletonBot.start(colorArray);
                }
                else if(!allColorsSelected){
                    JOptionPane.showMessageDialog( buttonFrame,
                            "Make sure every color has been selected!");
                }
                if(!abletonBot.haveSoloLocation()){
                    JOptionPane.showMessageDialog( buttonFrame,
                            "Make sure to tell me where your first track's solo button is located!");
                }
            }
        });
        goPanel.add(goButton);

        JButton soloButton = new JButton("First track's Solo Button Location");
        soloButton.addActionListener( new ActionListener(){
            public void actionPerformed(ActionEvent e)
            {
                abletonBot.soloClick();
            }
        });

        for(int i=0;i<stemCount;i++){
            //Button
            final JButton tempButton = new JButton("Color"+" "+ (i+1));
            tempButton.setVerticalTextPosition(AbstractButton.BOTTOM);
            tempButton.setHorizontalTextPosition(AbstractButton.CENTER);
            tempButton.setPreferredSize(new Dimension(100,50));
            //action
            tempButton.addActionListener( new ActionListener(){
                public void actionPerformed(ActionEvent e)
                {

                   //TODO: sort out how to array the colors
                    abletonBot.colorClick(tempButton,colorArray,buttonArrayList.indexOf(tempButton));
                    //colorArray[i] = tempColor

                }
            });

            //ButtonPanel
            buttonPanel.add(tempButton);
            buttonArrayList.add(tempButton);
            buttonPanel.add(tempButton);
        }

        goPanel.add(soloButton);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setTopComponent(buttonPanel);
        split.setBottomComponent(goPanel);
        split.setDividerLocation(0.8);
        buttonFrame.add(split);

        buttonFrame.setVisible(true);
    }

    private boolean colorArrayFull(){
        for(Color c : colorArray){
            if(c==null){
                return false;
            }
        }
        return true;
    }
}
