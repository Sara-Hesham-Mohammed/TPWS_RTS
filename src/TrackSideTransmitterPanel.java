import Components.TrackSideTransmitter;

import javax.swing.*;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Pure display panel for a Components.TrackSideTransmitter.
 * Adds itself as a listener and refreshes labels automatically.
 */
public class TrackSideTransmitterPanel extends JPanel
        implements TrackSideTransmitter.Listener {

    /* ── live labels ── */
    private final JLabel segLbl = new JLabel("--");
    private final JLabel limLbl = new JLabel("--");
    private final JLabel sigLbl = new JLabel("--");
    private final JLabel tsLbl  = new JLabel("--");

    private final DateTimeFormatter tf =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());

    public TrackSideTransmitterPanel(TrackSideTransmitter tx) {
        tx.addListener(this);

        setLayout(new GridLayout(4, 2, 10, 6));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(new JLabel("Segment:"));        add(segLbl);
        add(new JLabel("Speed limit:"));    add(limLbl);
        add(new JLabel("Signal:"));         add(sigLbl);
        add(new JLabel("Last broadcast:")); add(tsLbl);
    }

    @Override
    public void onBroadcast(String key, Object val, java.time.Instant ts) {
        SwingUtilities.invokeLater(() -> {
            switch (key) {
                /* new key names from the refactored transmitter */
                case "segment", "segmentIdentifier" -> segLbl.setText(val.toString());
                case "speedLimit"                   -> limLbl.setText(val + " km/h");
                case "signal", "signalStatus"       -> sigLbl.setText(val.toString());
            }
            tsLbl.setText(tf.format(ts));
        });
    }

    /* ── quick demo launcher ── */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // transmitter auto-changes every 3 s
            TrackSideTransmitter tx =
                    new TrackSideTransmitter("TX-01", "S-1A", 100, "GREEN", 3);
            tx.start();   // <-- internal scheduler handles threading

            JFrame f = new JFrame("Track-Side Transmitter Screen (auto)");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.setContentPane(new TrackSideTransmitterPanel(tx));
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);

            /* tidy up background scheduler when window closes */
            f.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) {
                    try (tx) { /* AutoCloseable */ } catch (Exception ignored) {}
                }
            });
        });
    }
}
