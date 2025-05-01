/*GUI Imports*/
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
/*Esper stuff*/
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/*Stuff for the buzzer audio*/
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class GUI {

    /* Controller and Esper engine*/
    // Get engine reference
    EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

    TPWSController controller = new TPWSController("TPWS_1",engine);

    /* widgets */
    private final JLabel timerVal = bigLabel();
    private final JLabel segVal = bigLabel();
    private final JLabel sigVal = badgeLabel(Color.GRAY);
    private final JProgressBar speedGauge = new JProgressBar(0, 200);

    public GUI() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
        }

        // Initialize default values
        timerVal.setText("0 s 000 ms");
        segVal.setText("--");
        sigVal.setText("--");
        speedGauge.setValue(0);
        speedGauge.setString("0 km/h");

        createGui();
    }

    /* ── builder helpers ── */
    private static JLabel bigLabel() {
        JLabel l = new JLabel("--", SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 28f));
        return l;
    }

    private static JLabel badgeLabel(Color c) {
        JLabel l = bigLabel();
        l.setOpaque(true);
        l.setBackground(c);
        l.setForeground(Color.WHITE);
        l.setBorder(new LineBorder(Color.DARK_GRAY, 2, true));
        return l;
    }

    /* ── GUI construction ── */
    private void createGui() {
        JFrame frame = new JFrame("Dashboard");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(20, 30, 20, 30));
        card.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.EAST;

        addRow(card, gbc, "Elapsed:", timerVal);
        addRow(card, gbc, "Segment:", segVal);

        speedGauge.setStringPainted(true);
        speedGauge.setFont(speedGauge.getFont().deriveFont(Font.PLAIN, 16f));
        addRow(card, gbc, "Speed:", speedGauge);

        addRow(card, gbc, "Signal:", sigVal);

        frame.setContentPane(card);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void addRow(JPanel p, GridBagConstraints gbc,
                               String caption, JComponent comp) {
        JLabel cap = new JLabel(caption, SwingConstants.RIGHT);
        cap.setFont(cap.getFont().deriveFont(Font.BOLD, 15f));

        gbc.gridy++;
        p.add(cap, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(comp, gbc);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
    }

    /* ── launcher ── */
    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.OFF);
        SwingUtilities.invokeLater(() -> {
            try {
                new GUI();
            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException  e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Example of how to update values (for demonstration)
     */
    public void updateTimer(String timeText) {
        timerVal.setText(timeText);
    }

    public void updateSegment(String segment) {
        segVal.setText(segment);
    }

    public void updateSpeed(int speedValue) {
        speedGauge.setValue(speedValue);
        speedGauge.setString(speedValue + " km/h");
    }

    public void updateSignal(String signalStatus) {
        sigVal.setText(signalStatus);

        Color signalColor = switch (signalStatus) {
            case "RED" -> new Color(180, 35, 35);
            case "YELLOW" -> new Color(200, 130, 0);   // colour-blind-safe amber
            case "GREEN" -> new Color(25, 140, 60);
            default -> Color.GRAY;
        };

        sigVal.setBackground(signalColor);
    }
}

