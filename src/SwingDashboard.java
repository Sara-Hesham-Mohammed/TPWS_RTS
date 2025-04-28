import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Nicer-looking, fully-automatic TPWS dashboard (no buttons).
 * Depends only on TrackSideTransmitter.java and Stopwatch inner class.
 */
public class SwingDashboard {

    /* ---------- live labels & widgets ---------- */
    private final JLabel timerVal = bigLabel();
    private final JLabel segVal   = bigLabel();
    private final JLabel sigVal   = badgeLabel(Color.GRAY);
    private final JProgressBar speedGauge = new JProgressBar(0, 200);

    /* ---------- core objects ---------- */
    private final Stopwatch            stopwatch = new Stopwatch();
    private final TrackSideTransmitter tx        =
            new TrackSideTransmitter("TX-01", "S-1A", 100, "GREEN", 3);

    /* ================================================================ */
    public SwingDashboard() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) { /* fallback L&F will be used */ }

        new Thread(tx, "TxLoop").start();
        stopwatch.start();

        createGui();
        startUiTicker();
    }

    /* ---------- helper: big value label ---------- */
    private static JLabel bigLabel() {
        JLabel l = new JLabel("--", SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 28f));
        return l;
    }

    /* ---------- helper: coloured badge ---------- */
    private static JLabel badgeLabel(Color c) {
        JLabel l = bigLabel();
        l.setOpaque(true);
        l.setBackground(c);
        l.setForeground(Color.WHITE);
        l.setBorder(new LineBorder(Color.DARK_GRAY, 2, true));
        return l;
    }

    /* ---------- GUI construction ---------- */
    private void createGui() {
        JFrame frame = new JFrame("TPWS Dashboard");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        /* --- pretty card panel --- */
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(20, 30, 20, 30));
        card.setBackground(new Color(245, 245, 245));
        card.setOpaque(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.EAST;

        addRow(card, gbc, "Elapsed:", timerVal);
        addRow(card, gbc, "Segment:", segVal);

        speedGauge.setStringPainted(true);
        speedGauge.setValue(0);
        speedGauge.setFont(speedGauge.getFont().deriveFont(Font.PLAIN, 16f));
        addRow(card, gbc, "Speed:", speedGauge);

        addRow(card, gbc, "Signal:", sigVal);

        frame.setContentPane(card);
        frame.pack();
        frame.setMinimumSize(frame.getSize());      // prevent collapse
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
        gbc.gridx = 0; gbc.fill = GridBagConstraints.NONE;
    }

    /* ---------- periodic UI refresh ---------- */
    private void startUiTicker() {
        new Timer("UI", true).scheduleAtFixedRate(
                new TimerTask() { @Override public void run() {
                    SwingUtilities.invokeLater(SwingDashboard.this::refresh);
                }}, 0, 250);
    }

    private void refresh() {
        timerVal.setText(stopwatch.elapsedMs() + " ms");
        segVal.setText(tx.getSegmentIdentifier());

        speedGauge.setValue(tx.getSpeedLimit());
        speedGauge.setString(tx.getSpeedLimit() + " km/h");

        sigVal.setText(tx.getSignalStatus());
        switch (tx.getSignalStatus()) {
            case "RED"    -> sigVal.setBackground(new Color(200,30,30));
            case "YELLOW" -> sigVal.setBackground(new Color(240,180,20));
            case "GREEN"  -> sigVal.setBackground(new Color(30,170,30));
            default       -> sigVal.setBackground(Color.GRAY);
        }
    }

    /* ---------- launcher ---------- */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(SwingDashboard::new);
    }
}

/* tiny helper class: no clash with java.util.Timer */
class Stopwatch {
    private long start;
    void start()           { start = System.currentTimeMillis(); }
    long elapsedMs()       { return System.currentTimeMillis() - start; }
}
