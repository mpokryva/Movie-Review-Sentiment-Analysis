import javax.swing.*;

/**
 * Created by mpokr on 3/19/2017.
 */
public class ResultsPanel extends  JPanel {


    private JTextArea KNNArea;
    private JTextArea rocchioArea;
    private final String PANEL_TITLE = "Results";
    private JLabel titleArea;

    public ResultsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        titleArea = new JLabel(PANEL_TITLE);
        this.add(titleArea);
        KNNArea = new JTextArea();
        rocchioArea = new JTextArea();
        this.add(KNNArea);
        this.add(rocchioArea);
        setVisible(false);
    }

    protected void setKNNAreaText(String text) {
        KNNArea.setText(text);
        setVisible(true);
        repaint();
    }

    public void setRocchioAreaText(String text) {
        rocchioArea.setText(text);
        setVisible(true);
        repaint();
    }


}

