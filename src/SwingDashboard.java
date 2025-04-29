import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Swing dashboard that auto-updates via push callbacks.
 */
public class SwingDashboard {

    /* ── widgets ── */
    private final JLabel timerVal = bigLabel();
    private final JLabel segVal   = bigLabel();
    private final JLabel sigVal   = badgeLabel(Color.GRAY);
    private final JProgressBar speedGauge = new JProgressBar(0, 200);

    /* ── core objects ── */
    private final Timer                stopwatch = new Timer();
    private final TrackSideTransmitter tx        =
            new TrackSideTransmitter("01", "S-1A", 100, "GREEN", 3);

    /* ── ctor ── */
    public SwingDashboard() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) {}

        wireListeners();
        tx.start();
        stopwatch.start(250);

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

    /* ── listener wiring (push model) ── */
    private void wireListeners() {

        /* elapsed time every 250 ms */
        stopwatch.addListener((elapsed, ts) ->
                SwingUtilities.invokeLater(() ->
                        timerVal.setText(formatTime(elapsed))));

        /* transmitter pushes field updates */
        tx.addListener((key, value, ts) -> SwingUtilities.invokeLater(() -> {
            switch (key) {
                case "segment" -> segVal.setText(value.toString());

                case "speedLimit" -> {
                    int v = (Integer) value;
                    speedGauge.setValue(v);
                    speedGauge.setString(v + " km/h");
                }

                case "signal" -> {
                    String s = value.toString();
                    sigVal.setText(s);
                    sigVal.setBackground(switch (s) {
                        case "RED"    -> new Color(180, 35, 35);
                        case "YELLOW" -> new Color(200, 130, 0);   // colour-blind-safe amber
                        case "GREEN"  -> new Color( 25, 140, 60);
                        default       -> Color.GRAY;
                    });
                }
            }
        }));
    }

    /* ── GUI construction ── */
    private void createGui() {
        JFrame frame = new JFrame("TPWS Dashboard");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(20, 30, 20, 30));
        card.setBackground(new Color(245, 245, 245));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(10, 15, 10, 15);
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

        /* ensure background threads shut down */
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                try (tx; stopwatch) { /* try-with-resources closes both */ }
                catch (Exception ignored) {}
            }
        });
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
        gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE;
    }

    /* ── helpers ── */
    private static String formatTime(long ms) {
        long sec = ms / 1000;
        long rem = ms % 1000;
        return "%d s %03d ms".formatted(sec, rem);
    }

    /* ── launcher ── */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SwingDashboard::new);
    }
}
