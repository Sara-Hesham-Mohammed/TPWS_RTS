package GUIs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AlertGUI extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JLabel warningLabel;
    private JLabel warningText;

    public AlertGUI() {
        setTitle("Alert");
        setSize(300, 200);
        setLocationRelativeTo(null); // center on screen
        setModal(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // Initialize contentPane
        contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        setContentPane(contentPane);

        /******* Components *******/
        warningLabel = new JLabel("Warning");
        warningLabel.setFont(new Font("Arial", Font.BOLD, 16));
        warningLabel.setForeground(Color.RED);
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        warningText = new JLabel();
        warningText.setMaximumSize(new Dimension(260, 80));
        warningText.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonOK = new JButton("OK");
        buttonCancel = new JButton("Cancel");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(buttonOK);
        buttonPanel.add(buttonCancel);

        /******* Layout *******/
        contentPane.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPane.add(warningLabel);
        contentPane.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPane.add(warningText);
        contentPane.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPane.add(buttonPanel);

        /******* Event Handlers *******/
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        hideMessage();
        dispose();
    }

    private void onCancel() {
        hideMessage();
        dispose();
    }

    public void showMessage(String message) {
        warningText.setText(message);
        setVisible(true);
    }

    public void hideMessage() {
        setVisible(false);
    }
}
