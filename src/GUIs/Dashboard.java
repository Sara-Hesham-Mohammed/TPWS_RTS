package GUIs;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Dashboard extends JFrame {
    private JPanel contentPane;
    private JProgressBar speedBar;

    /****** Labels *****/
    private JLabel brakeStatusLabel;
    private JLabel powerStatusLabel;
    private JLabel weatherStatusLabel;
    private JLabel signalStatusLabel;
    private JLabel segIdLabel;
    private JLabel segSpeedLimLabel;
    private JLabel segSignalLabel;
    private JLabel speedLabel;

    /**
     * The values
     **/
    private JLabel signalStatus;
    private JLabel brakeStatus;
    private JLabel powerStatus;
    private JLabel weatherStatus;
    private JLabel segId;
    private JLabel segSpeedLim;
    private JLabel segSignal;

    public Dashboard() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (Exception ignored) {
        }

        setTitle("Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeComponents();
        setupLayout();

        pack();
        setLocationRelativeTo(null);
        setMinimumSize(getSize());
    }

    private void initializeComponents() {

        brakeStatusLabel = createCaptionLabel("Brake Status:");
        powerStatusLabel = createCaptionLabel("Power Status:");
        weatherStatusLabel = createCaptionLabel("Weather Status:");
        signalStatusLabel = createCaptionLabel("Signal Status:");
        segIdLabel = createCaptionLabel("Segment ID:");
        segSpeedLimLabel = createCaptionLabel("Speed Limit:");
        segSignalLabel = createCaptionLabel("Signal:");
        speedLabel = createCaptionLabel("Speed");

        // Initialize value displays
        brakeStatus = bigLabel("--");
        powerStatus = bigLabel("--");
        weatherStatus = bigLabel("--");
        signalStatus = badgeLabel(Color.GRAY);
        segId = bigLabel("--");
        segSpeedLim = bigLabel("--");
        segSignal = badgeLabel(Color.GRAY);

        // Initialize speed bar
        speedBar = new JProgressBar(0, 200);
        speedBar.setStringPainted(true);
        speedBar.setString("0 km/h");
        speedBar.setFont(speedBar.getFont().deriveFont(Font.PLAIN, 16f));
    }

    private void setupLayout() {
        contentPane = new JPanel(new GridBagLayout());
        contentPane.setBorder(new EmptyBorder(20, 30, 20, 30));
        contentPane.setBackground(new Color(245, 245, 245));
        setContentPane(contentPane);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.anchor = GridBagConstraints.EAST;

        addRow(contentPane, gbc, "Speed:", speedBar);
        addRow(contentPane, gbc, "Segment ID:", segId);
        addRow(contentPane, gbc, "Speed Limit:", segSpeedLim);
        addRow(contentPane, gbc, "Signal:", segSignal);
        addRow(contentPane, gbc, "Brake Status:", brakeStatus);
        addRow(contentPane, gbc, "Power Status:", powerStatus);
        addRow(contentPane, gbc, "Weather Status:", weatherStatus);
        addRow(contentPane, gbc, "Signal Status:", signalStatus);
    }

    // Helper methods
    private static JLabel bigLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 28f));
        return l;
    }

    private static JLabel badgeLabel(Color c) {
        JLabel l = bigLabel("--");
        l.setOpaque(true);
        l.setBackground(c);
        l.setForeground(Color.WHITE);
        l.setBorder(new LineBorder(Color.DARK_GRAY, 2, true));
        return l;
    }

    private static JLabel createCaptionLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        return label;
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

    // Update methods
    public void updateSpeed(double speedValue) {
        SwingUtilities.invokeLater(() -> {
            speedBar.setValue((int) speedValue);
            speedBar.setString((int) speedValue + " km/h");
        });
    }

    public void updateSignalStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            signalStatus.setText(status);

            Color signalColor = switch (status.toUpperCase()) {
                case "RED" -> new Color(180, 35, 35);
                case "YELLOW" -> new Color(200, 130, 0);
                case "GREEN" -> new Color(25, 140, 60);
                default -> Color.GRAY;
            };

            signalStatus.setBackground(signalColor);
        });
    }

    public void updateSegmentSignal(String status) {
        SwingUtilities.invokeLater(() -> {
            segSignal.setText(status);

            Color signalColor = switch (status.toUpperCase()) {
                case "RED" -> new Color(180, 35, 35);
                case "YELLOW" -> new Color(200, 130, 0);
                case "GREEN" -> new Color(25, 140, 60);
                default -> Color.GRAY;
            };

            segSignal.setBackground(signalColor);
        });
    }

    public void updateSegmentId(String id) {
        SwingUtilities.invokeLater(() -> segId.setText(id));
    }

    public void updateSpeedLimit(String limit) {
        SwingUtilities.invokeLater(() -> segSpeedLim.setText(limit));
    }

    public void updateBrakeStatus(String status) {
        SwingUtilities.invokeLater(() -> brakeStatus.setText(status));
    }

    public void updatePowerStatus(String status) {
        SwingUtilities.invokeLater(() -> powerStatus.setText(status));
    }

    public void updateWeatherStatus(String status) {
        SwingUtilities.invokeLater(() -> weatherStatus.setText(status));
    }


    public void showGUI() {
        setVisible(true);
    }


    public void hideGUI() {
        setVisible(false);
    }
}