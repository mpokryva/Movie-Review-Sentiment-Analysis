import javax.swing.*;

/**
 * Created by mpokr on 3/19/2017.
 */
public class KNNPanel extends JPanel {

    private JCheckBox useManhattanDistance;
    private JTextField K;
    private JCheckBox useTfIdf;
    private final String PANEL_TITLE = "K-NN Options";
    private JLabel titleArea;

    public KNNPanel() {
        useManhattanDistance = new JCheckBox();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        useManhattanDistance.setText("Use Manhattan distance (uses Euclidean if unchecked)");
        K = new JTextField();
        K.setText("Specify K");
        useTfIdf = new JCheckBox();
        useTfIdf.setText("Use tf-idf instead of unary weighting (tf)");
        titleArea = new JLabel(PANEL_TITLE);
        this.add(titleArea);
        this.add(useTfIdf);
        this.add(useManhattanDistance);
        this.add(K);
        this.setVisible(true);
    }

    public boolean getUseTfIdf() {
        return useTfIdf.isSelected();
    }

    public boolean shouldUseManhattan() {
        return useManhattanDistance.isSelected();
    }

    public int getK() {
        try {
            int KValue =  Integer.parseInt(K.getText());
            return KValue;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void disableComponents() {
        useTfIdf.setEnabled(false);
        useTfIdf.setEnabled(false);
        K.setEnabled(false);
    }


    public void enableComponents() {
        useTfIdf.setEnabled(true);
        useTfIdf.setEnabled(true);
        K.setEnabled(true);
    }
}
