package GUIs;
import Components.Timer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Modern screen for the TPWS Components.Timer actor.
 *
 *  • Big digital clock  00:01:23.456
 *  • Slim progress bar  (0-999 ms of current second)
 *  • Start / Stop / Reset buttons
 *  • Updates every 50 ms via the timer's tick-listener
 */
public class TimerPanel extends JPanel implements Components.Timer.TickListener {

    /* ── widgets ── */
    private final JLabel        bigClock = new JLabel("00:00:00.000", SwingConstants.CENTER);
    private final JProgressBar  msBar    = new JProgressBar(0, 999);

    /* ── domain object ── */
    private final Timer timer;

    /* ── constants ── */
    private static final long               PERIOD_MS  = 50;        // tick every 50 ms
    private static final DateTimeFormatter  CLOCK_FMT =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());

    /* ── ctor ── */
    public TimerPanel(Timer timer) {
        this.timer = timer;
        timer.addListener(this);

        setLookAndFeel();
        buildUi();
    }

    /* ── Nimbus L&F ── */
    private void setLookAndFeel() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) {}
    }

    /* ── GUIs.GUI construction ── */
    private void buildUi() {
        setLayout(new BorderLayout());

        /* card-style panel */
        JPanel card = new JPanel(new GridBagLayout());
        card.setBorder(new LineBorder(new Color(200,200,200), 1, true));
        card.setBackground(new Color(250, 250, 250));

        bigClock.setFont(bigClock.getFont().deriveFont(Font.BOLD, 36f));
        bigClock.setBorder(new EmptyBorder(10, 20, 10, 20));

        msBar.setStringPainted(true);
        msBar.setPreferredSize(new Dimension(260, 18));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(bigClock, BorderLayout.CENTER);
        center.add(msBar,   BorderLayout.SOUTH);

        /* control buttons */
        JButton start = new JButton("Start");
        JButton stop  = new JButton("Stop");
        JButton reset = new JButton("Reset");

        start.addActionListener(e -> timer.start(PERIOD_MS));
        stop .addActionListener(e -> timer.stop());
        reset.addActionListener(e -> { timer.reset(); updateDisplay(0); });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        btnPanel.setOpaque(false);
        btnPanel.add(start); btnPanel.add(stop); btnPanel.add(reset);

        card.add(center,   gbc(0,0, GridBagConstraints.HORIZONTAL, 1.0));
        card.add(btnPanel, gbc(0,1, GridBagConstraints.NONE,       0));

        add(card, BorderLayout.CENTER);
    }
    private static GridBagConstraints gbc(int x,int y,int fill,double weightY){
        GridBagConstraints g = new GridBagConstraints();
        g.gridx=x; g.gridy=y; g.fill=fill; g.weightx=1; g.weighty=weightY;
        return g;
    }

    /* ── listener callback ── */
    @Override
    public void onTick(long elapsedMs, java.time.Instant ts) {
        SwingUtilities.invokeLater(() -> updateDisplay(elapsedMs));
    }

    private void updateDisplay(long ms) {
        bigClock.setText(CLOCK_FMT.format(LocalTime.MIDNIGHT.plus(Duration.ofMillis(ms))));
        int sub = (int)(ms % 1000);
        msBar.setValue(sub);
        msBar.setString(sub + " ms");
    }

    /* ── demo launcher ── */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Timer t = new Timer();
            JFrame f = new JFrame("Components.Timer Screen");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.setContentPane(new TimerPanel(t));
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
/*ttt*/
            /* start automatically so the clock is live */
            t.start(PERIOD_MS);
/*jsfffsss*/
            /* clean shutdown of scheduler */
            f.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override public void windowClosing(java.awt.event.WindowEvent e) {
                    try (t) { /* AutoCloseable shuts down executor */ }
                    catch (Exception ignored) {}
                }
            });
        });
    }
}
