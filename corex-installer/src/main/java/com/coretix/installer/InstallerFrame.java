package com.coretix.installer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

public final class InstallerFrame extends JFrame {
    private final JTextField hostField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("3306");
    private final JTextField databaseField = new JTextField();
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<InstallTarget> targetCombo = new JComboBox<>(InstallTarget.values());
    private final JButton installButton = new JButton("Install");
    private final JTextArea logArea = new JTextArea();
    private final JTextArea sqlPreviewArea = new JTextArea();
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel statusLabel = new JLabel("Waiting to start");

    public InstallerFrame() {
        super("CoreX Database Installer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 720));
        setLocationByPlatform(true);
        setLayout(new BorderLayout(12, 12));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildFooterPanel(), BorderLayout.SOUTH);

        installButton.addActionListener(event -> startInstall());
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Database Connection"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addField(panel, gbc, 0, "Host", hostField);
        addField(panel, gbc, 1, "Port", portField);
        addField(panel, gbc, 2, "Database Name", databaseField);
        addField(panel, gbc, 3, "Username", usernameField);
        addField(panel, gbc, 4, "Password", passwordField);
        addField(panel, gbc, 5, "Install Target", targetCombo);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 6;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        panel.add(new JLabel("<html>Select the target and provide the MySQL account that can create databases and execute schema scripts.</html>"), gbc);

        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private JPanel buildCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        sqlPreviewArea.setEditable(false);
        sqlPreviewArea.setLineWrap(false);

        JScrollPane logPane = new JScrollPane(logArea);
        logPane.setBorder(BorderFactory.createTitledBorder("Execution Log"));

        JScrollPane sqlPane = new JScrollPane(sqlPreviewArea);
        sqlPane.setBorder(BorderFactory.createTitledBorder("Current SQL"));
        sqlPane.setPreferredSize(new Dimension(460, 420));

        panel.add(logPane, BorderLayout.CENTER);
        panel.add(sqlPane, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildFooterPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));

        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(installButton, BorderLayout.EAST);

        return panel;
    }

    private void startInstall() {
        DbInstallConfig config;
        try {
            config = buildConfig();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        installButton.setEnabled(false);
        logArea.setText("");
        sqlPreviewArea.setText("");
        progressBar.setValue(0);
        progressBar.setString("0%");
        statusLabel.setText("Starting installation...");

        SwingWorker<Void, UiEvent> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                DbInstallerService service = new DbInstallerService();
                service.install(config, new InstallListener() {
                    @Override
                    public void onStart(int totalSteps) {
                        publish(UiEvent.start(totalSteps));
                    }

                    @Override
                    public void onStepStarted(int stepNumber, int totalSteps, InstallStep step) {
                        publish(UiEvent.stepStarted(stepNumber, totalSteps, step));
                    }

                    @Override
                    public void onProgress(int completedSteps, int totalSteps, InstallStep currentStep) {
                        publish(UiEvent.progress(completedSteps, totalSteps, currentStep));
                    }

                    @Override
                    public void onMessage(String message) {
                        publish(UiEvent.message(message));
                    }

                    @Override
                    public void onComplete() {
                        publish(UiEvent.complete());
                    }
                });
                return null;
            }

            @Override
            protected void process(List<UiEvent> chunks) {
                for (UiEvent event : chunks) {
                    handleUiEvent(event);
                }
            }

            @Override
            protected void done() {
                installButton.setEnabled(true);
                try {
                    get();
                    statusLabel.setText("Installation completed");
                    JOptionPane.showMessageDialog(InstallerFrame.this, "Database installation completed.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    String message = ex.getCause() == null ? ex.getMessage() : ex.getCause().getMessage();
                    statusLabel.setText("Installation failed");
                    appendLog("ERROR: " + message);
                    JOptionPane.showMessageDialog(InstallerFrame.this, message, "Installation Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void handleUiEvent(UiEvent event) {
        switch (event.type()) {
            case START -> {
                progressBar.setMaximum(Math.max(1, event.totalSteps()));
                progressBar.setValue(0);
                progressBar.setString("0%");
                appendLog("Loaded " + event.totalSteps() + " SQL files.");
            }
            case STEP_STARTED -> {
                InstallStep step = event.step();
                if (step != null) {
                    int current = event.completedSteps();
                    int total = Math.max(1, event.totalSteps());
                    statusLabel.setText("Running " + current + " of " + total + ": " + step.getDisplayName());
                    sqlPreviewArea.setText(step.getSql());
                    sqlPreviewArea.setCaretPosition(0);
                }
            }
            case PROGRESS -> {
                int completed = event.completedSteps();
                int total = Math.max(1, event.totalSteps());
                progressBar.setMaximum(total);
                progressBar.setValue(completed);
                int percent = (int) Math.round((completed * 100.0) / total);
                progressBar.setString(percent + "%");

                InstallStep step = event.step();
                if (step != null) {
                    statusLabel.setText("Completed " + completed + " of " + total + ": " + step.getDisplayName());
                }
            }
            case MESSAGE -> appendLog(event.message());
            case COMPLETE -> {
                progressBar.setValue(progressBar.getMaximum());
                progressBar.setString("100%");
                statusLabel.setText("Finalizing");
            }
        }
    }

    private void appendLog(String message) {
        logArea.append(message + System.lineSeparator());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private DbInstallConfig buildConfig() {
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();
        String databaseName = databaseField.getText().trim();
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        InstallTarget target = (InstallTarget) targetCombo.getSelectedItem();

        if (host.isEmpty()) {
            throw new IllegalArgumentException("Host is required.");
        }
        if (databaseName.isEmpty()) {
            throw new IllegalArgumentException("Database name is required.");
        }
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (target == null) {
            throw new IllegalArgumentException("Install target is required.");
        }

        int port;
        try {
            port = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port must be a valid number.");
        }

        return new DbInstallConfig(host, port, databaseName, username, password, target);
    }

    public static void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    public static void showWindow() {
        installLookAndFeel();
        SwingUtilities.invokeLater(() -> {
            InstallerFrame frame = new InstallerFrame();
            frame.setVisible(true);
        });
    }
}
