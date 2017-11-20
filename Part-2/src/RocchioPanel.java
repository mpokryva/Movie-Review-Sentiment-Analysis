import javax.swing.*;

/**
 * Created by mpokr on 3/19/2017.
 */
public class RocchioPanel extends JPanel {

    private JCheckBox useTfIdf;
    private final String PANEL_TITLE = "Rocchio Classifier Options";
    private JLabel titleArea;
    private JCheckBox useManhattanDistance;

    public RocchioPanel() {
        useTfIdf = new JCheckBox();
        useManhattanDistance = new JCheckBox();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        useTfIdf.setText("Use tf-idf instead of unary weighting (tf).");
        useManhattanDistance.setText("Use Manhattan distance (uses Euclidean if unchecked)");
        titleArea = new JLabel(PANEL_TITLE);
        this.add(titleArea);
        this.add(useTfIdf);
        this.add(useManhattanDistance);
        this.setVisible(true);
    }

    public boolean shouldUseTfIdf() {
        return useTfIdf.isSelected();
    }

    public boolean shouldUseManhattan() {
        return useManhattanDistance.isSelected();
    }

    public void disableComponents() {
        useTfIdf.setEnabled(false);
    }

    public void enableComponents() {
        useTfIdf.setEnabled(true);
    }
}
