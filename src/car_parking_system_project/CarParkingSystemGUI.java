package car_parking_system_project;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class CarParkingSystemGUI extends JFrame {
    // --- MODERN COLOR PALETTE ---
    private static final Color COLOR_SIDEBAR = new Color(15, 23, 42);       // #0F172A Dark Slate
    private static final Color COLOR_SIDEBAR_HOVER = new Color(30, 41, 59); // #1E293B
    private static final Color COLOR_SIDEBAR_ACTIVE = new Color(37, 99, 235);// #2563EB Royal Blue
    private static final Color COLOR_BG = new Color(248, 250, 252);         // #F8FAFC Off-white
    private static final Color COLOR_CARD = new Color(255, 255, 255);       // #FFFFFF White
    private static final Color COLOR_BORDER = new Color(226, 232, 240);      // #E2E8F0 Light Gray
    private static final Color COLOR_PRIMARY = new Color(37, 99, 235);       // #2563EB Blue
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);      // #10B981 Emerald
    private static final Color COLOR_DANGER = new Color(225, 29, 72);        // #E11D48 Rose Red
    private static final Color COLOR_SECONDARY = new Color(71, 85, 105);     // #475569 Slate Gray
    private static final Color COLOR_TEXT_DARK = new Color(15, 23, 42);      // #0F172A
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // #64748B

    private static class WhiteCarIcon implements Icon {
        private final int width = 30;
        private final int height = 24;

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);

            // Car Roof / Cabin
            int[] xCabin = {x + 6, x + 10, x + 20, x + 24};
            int[] yCabin = {y + 11, y + 4, y + 4, y + 11};
            g2.fillPolygon(xCabin, yCabin, 4);

            // Car Body
            g2.fillRoundRect(x + 2, y + 10, 26, 8, 4, 4);

            // Cabin Window Cutout
            g2.setColor(COLOR_SIDEBAR);
            int[] xWin = {x + 8, x + 11, x + 19, x + 22};
            int[] yWin = {y + 10, y + 6, y + 6, y + 10};
            g2.fillPolygon(xWin, yWin, 4);

            // Wheels (White circle outer, dark center)
            g2.setColor(Color.WHITE);
            g2.fillOval(x + 5, y + 15, 6, 6);
            g2.fillOval(x + 18, y + 15, 6, 6);

            g2.setColor(COLOR_SIDEBAR);
            g2.fillOval(x + 7, y + 17, 2, 2);
            g2.fillOval(x + 20, y + 17, 2, 2);

            g2.dispose();
        }

        @Override
        public int getIconWidth() { return width; }

        @Override
        public int getIconHeight() { return height; }
    }

    private CardLayout contentCardLayout;
    private JPanel mainContentPanel;
    private JPanel sidebarPanel;

    private double totalEarnings;
    private int totalBookingsCount;
    private int totalHoursBooked;
    private int ticketCounter;

    private VehicleRegistration vehicleRegistration;
    private List<Register> registeredUsers;
    private List<ParkingSlot> parkingSlots;
    private List<ParkingTicket> ticketHistory;
    private Register currentUser;
    private boolean isLogin;

    // Stat Labels
    private JLabel statAvailVal, statOccupiedVal, statRevenueVal, statBookingsVal;
    private JLabel pageTitleLabel, currentUserLabel;
    private JPanel slotsGridContainer;
    private List<JButton> navButtons;

    public CarParkingSystemGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("Smart Car Parking Management System");
        setSize(1020, 740);
        setMinimumSize(new Dimension(950, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize Data
        totalEarnings = 0;
        totalBookingsCount = 0;
        totalHoursBooked = 0;
        ticketCounter = 1001;
        vehicleRegistration = new VehicleRegistration();
        registeredUsers = new ArrayList<>();
        ticketHistory = new ArrayList<>();
        parkingSlots = new ArrayList<>();
        navButtons = new ArrayList<>();
        isLogin = false;

        String[] slotNames = {"A1", "A2", "A3", "A4", "A5", "A6", "B1", "B2", "B3", "B4", "B5", "B6"};
        for (String name : slotNames) {
            parkingSlots.add(new ParkingSlot(name));
        }

        loadAllData();

        contentCardLayout = new CardLayout();
        mainContentPanel = new JPanel(contentCardLayout);
        mainContentPanel.setBackground(COLOR_BG);

        // Root Container
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(COLOR_BG);

        contentPanelAddScreens();

        add(rootPanel);
        cardLayoutShow("Login");
    }

    private void contentPanelAddScreens() {
        mainContentPanel.add(createLoginView(), "Login");
        mainContentPanel.add(createRegisterView(), "Register");
        mainContentPanel.add(createMainAppLayout(), "AppLayout");
    }

    private void cardLayoutShow(String screenName) {
        if (screenName.equals("Login") || screenName.equals("Register")) {
            getContentPane().removeAll();
            getContentPane().add(mainContentPanel);
            contentCardLayout.show(mainContentPanel, screenName);
        } else {
            getContentPane().removeAll();
            getContentPane().add(createMainAppLayout());
        }
        revalidate();
        repaint();
    }

    // MAIN APP LAYOUT (Sidebar + Top Bar + Content Area)
    private JPanel createMainAppLayout() {
        JPanel appLayout = new JPanel(new BorderLayout());
        appLayout.setBackground(COLOR_BG);

        // 1. LEFT SIDEBAR
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(COLOR_SIDEBAR);
        sidebarPanel.setPreferredSize(new Dimension(240, 700));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        // Brand Logo & Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel brandIcon = new JLabel(new WhiteCarIcon());

        JPanel brandTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        brandTextPanel.setOpaque(false);
        JLabel brandTitle = new JLabel("PARK SMART");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandTitle.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Facility Management");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        brandSub.setForeground(new Color(148, 163, 184));

        brandTextPanel.add(brandTitle);
        brandTextPanel.add(brandSub);
        brandPanel.add(brandIcon);
        brandPanel.add(brandTextPanel);

        sidebarPanel.add(brandPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Nav Section Title
        JLabel navHeading = new JLabel("  FACILITY OPERATIONS");
        navHeading.setFont(new Font("Segoe UI", Font.BOLD, 11));
        navHeading.setForeground(new Color(147, 197, 253));
        navHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(navHeading);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Nav Buttons
        navButtons.clear();
        JButton btnDash = createNavButton("📊  Dashboard Overview", "DashboardView");
        JButton btnEntry = createNavButton("🛃  Vehicle Check-In (Entry)", "CheckIn");
        JButton btnExit = createNavButton("🏁  Vehicle Check-Out (Exit)", "CheckOut");
        JButton btnVehicles = createNavButton("🚘  Vehicle Directory", "VehicleView");
        JButton btnSearch = createNavButton("🔍  Search & Lookup", "SearchView");
        JButton btnReports = createNavButton("📈  Reports & Analytics", "ReportView");
        JButton btnLogout = createNavButton("🚪  Logout Operator", "Logout");

        sidebarPanel.add(btnDash);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebarPanel.add(btnEntry);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebarPanel.add(btnExit);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebarPanel.add(btnVehicles);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebarPanel.add(btnSearch);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        sidebarPanel.add(btnReports);

        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(btnLogout);

        // 2. TOP HEADER BAR
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(COLOR_CARD);
        topHeader.setPreferredSize(new Dimension(800, 60));
        topHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        pageTitleLabel = new JLabel("Dashboard Overview");
        pageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pageTitleLabel.setForeground(COLOR_TEXT_DARK);

        currentUserLabel = new JLabel("Operator: " + (currentUser != null ? currentUser.getname() : "Admin"));
        currentUserLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        currentUserLabel.setForeground(COLOR_PRIMARY);

        topHeader.add(pageTitleLabel, BorderLayout.WEST);
        topHeader.add(currentUserLabel, BorderLayout.EAST);

        // 3. CENTER CONTENT AREA (CARD LAYOUT)
        JPanel centerContent = new JPanel(new CardLayout());
        centerContent.setBackground(COLOR_BG);

        centerContent.add(createDashboardScreen(), "DashboardView");
        centerContent.add(createSlotGridScreen(), "SlotGridView");
        centerContent.add(createVehicleScreen(), "VehicleView");
        centerContent.add(createReportScreen(), "ReportView");

        // Action Listeners for Nav
        btnDash.addActionListener(e -> switchAppScreen(centerContent, "DashboardView", "Dashboard Overview", btnDash));
        btnEntry.addActionListener(e -> openCheckInDialog(null));
        btnExit.addActionListener(e -> openSelectCheckoutDialog());
        btnVehicles.addActionListener(e -> switchAppScreen(centerContent, "VehicleView", "Vehicle Directory Management", btnVehicles));
        btnSearch.addActionListener(e -> openSearchDialog());
        btnReports.addActionListener(e -> switchAppScreen(centerContent, "ReportView", "System Reports & Analytics", btnReports));
        btnLogout.addActionListener(e -> {
            isLogin = false;
            currentUser = null;
            cardLayoutShow("Login");
        });

        setNavButtonActive(btnDash);

        appLayout.add(sidebarPanel, BorderLayout.WEST);
        appLayout.add(topHeader, BorderLayout.NORTH);
        appLayout.add(centerContent, BorderLayout.CENTER);

        return appLayout;
    }

    private JButton createNavButton(String text, String screenTarget) {
        JButton btn = new JButton(text);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(241, 245, 249));
        btn.setBackground(COLOR_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(216, 40));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.getBackground() != COLOR_SIDEBAR_ACTIVE) {
                    btn.setBackground(COLOR_SIDEBAR_HOVER);
                    btn.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.getBackground() != COLOR_SIDEBAR_ACTIVE) {
                    btn.setBackground(COLOR_SIDEBAR);
                    btn.setForeground(new Color(241, 245, 249));
                }
            }
        });

        navButtons.add(btn);
        return btn;
    }

    private void setNavButtonActive(JButton activeBtn) {
        for (JButton btn : navButtons) {
            btn.setBackground(COLOR_SIDEBAR);
            btn.setForeground(new Color(241, 245, 249));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        activeBtn.setBackground(COLOR_SIDEBAR_ACTIVE);
        activeBtn.setForeground(Color.WHITE);
        activeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    private void switchAppScreen(JPanel container, String cardName, String pageTitle, JButton navBtn) {
        CardLayout cl = (CardLayout) container.getLayout();
        cl.show(container, cardName);
        pageTitleLabel.setText(pageTitle);
        setNavButtonActive(navBtn);
        updateDashboardStats();
    }

    // DASHBOARD SCREEN
    private JPanel createDashboardScreen() {
        JPanel dashboard = new JPanel(new BorderLayout(20, 20));
        dashboard.setBackground(COLOR_BG);
        dashboard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Stats Cards Row
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 16, 0));
        statsGrid.setOpaque(false);

        statAvailVal = new JLabel("12");
        statOccupiedVal = new JLabel("0");
        statRevenueVal = new JLabel("PKR 0");
        statBookingsVal = new JLabel("0");

        statsGrid.add(createDashboardStatCard("AVAILABLE SLOTS", statAvailVal, "🟢", COLOR_SUCCESS));
        statsGrid.add(createDashboardStatCard("OCCUPIED SLOTS", statOccupiedVal, "🔴", COLOR_DANGER));
        statsGrid.add(createDashboardStatCard("TOTAL REVENUE", statRevenueVal, "💰", COLOR_PRIMARY));
        statsGrid.add(createDashboardStatCard("TOTAL BOOKINGS", statBookingsVal, "🎟️", COLOR_SECONDARY));

        // Center Action Cards
        JPanel centerGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        centerGrid.setOpaque(false);

        // Quick Operations Card
        JPanel quickActions = createCardPanel("⚡ Gate & Terminal Operations");

        JPanel quickBtnsPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        quickBtnsPanel.setOpaque(false);

        JButton btnCheckIn = createPrimaryButton("🛃 Vehicle Entry Check-In (Gate 1)", COLOR_SUCCESS);
        JButton btnCheckOut = createPrimaryButton("🏁 Vehicle Exit Check-Out (Gate 2)", COLOR_DANGER);
        JButton btnSearch = createPrimaryButton("🔍 Search Active Vehicles & Tickets", COLOR_SECONDARY);

        btnCheckIn.addActionListener(e -> openCheckInDialog(null));
        btnCheckOut.addActionListener(e -> openSelectCheckoutDialog());
        btnSearch.addActionListener(e -> openSearchDialog());

        quickBtnsPanel.add(btnCheckIn);
        quickBtnsPanel.add(btnCheckOut);
        quickBtnsPanel.add(btnSearch);

        quickActions.add(quickBtnsPanel, BorderLayout.CENTER);

        // Tariff Rates Summary Card
        JPanel ratesCard = createCardPanel("💵 Hourly Tariff Structure (Billed at Exit)");
        ratesCard.setLayout(new BoxLayout(ratesCard, BoxLayout.Y_AXIS));

        ratesCard.add(createRateRow("🛵 Motorbike / Scooter", "PKR 50.0 / hr", COLOR_SUCCESS));
        ratesCard.add(Box.createRigidArea(new Dimension(0, 10)));
        ratesCard.add(createRateRow("🚗 Car / Sedan", "PKR 100.0 / hr", COLOR_PRIMARY));
        ratesCard.add(Box.createRigidArea(new Dimension(0, 10)));
        ratesCard.add(createRateRow("🚚 SUV / Heavy Vehicle", "PKR 150.0 / hr", COLOR_SECONDARY));
        ratesCard.add(Box.createRigidArea(new Dimension(0, 10)));
        ratesCard.add(createRateRow("⚡ EV Charging Station", "PKR 200.0 / hr", COLOR_DANGER));

        centerGrid.add(quickActions);
        centerGrid.add(ratesCard);

        dashboard.add(statsGrid, BorderLayout.NORTH);
        dashboard.add(centerGrid, BorderLayout.CENTER);

        updateDashboardStats();
        return dashboard;
    }

    private JPanel createDashboardStatCard(String title, JLabel valLabel, String iconStr, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel iconLbl = new JLabel(iconStr, SwingConstants.CENTER);
        iconLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JPanel textPnl = new JPanel(new GridLayout(2, 1, 0, 2));
        textPnl.setOpaque(false);

        JLabel tLbl = new JLabel(title);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tLbl.setForeground(COLOR_TEXT_MUTED);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLabel.setForeground(accentColor);

        textPnl.add(tLbl);
        textPnl.add(valLabel);

        card.add(iconLbl, BorderLayout.WEST);
        card.add(textPnl, BorderLayout.CENTER);

        return card;
    }

    private JPanel createRateRow(String type, String rate, Color badgeColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel tLbl = new JLabel(type);
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tLbl.setForeground(COLOR_TEXT_DARK);

        JLabel rLbl = new JLabel(rate);
        rLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        rLbl.setForeground(badgeColor);

        row.add(tLbl, BorderLayout.WEST);
        row.add(rLbl, BorderLayout.EAST);
        return row;
    }

    private JPanel createCardPanel(String titleStr) {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        if (titleStr != null) {
            JLabel title = new JLabel(titleStr);
            title.setFont(new Font("Segoe UI", Font.BOLD, 16));
            title.setForeground(COLOR_TEXT_DARK);
            card.add(title, BorderLayout.NORTH);
        }
        return card;
    }

    private JPanel createGridCardPanel() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(30, 35, 30, 35)
        ));
        return card;
    }

    private JButton createPrimaryButton(String text, Color bg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                if (getModel().isPressed()) {
                    g2.setColor(bg.darker().darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(bg.darker());
                } else {
                    g2.setColor(bg);
                }

                g2.fillRoundRect(0, 0, w, h, 8, 8);

                // Clip text inside button box
                g2.setClip(0, 0, w, h);
                g2.setColor(Color.WHITE);

                Font baseFont = getFont();
                FontMetrics fm = g2.getFontMetrics(baseFont);
                int availableWidth = w - 16;

                if (fm.stringWidth(getText()) > availableWidth && availableWidth > 0) {
                    float scaledSize = baseFont.getSize2D() * ((float) availableWidth / fm.stringWidth(getText()));
                    if (scaledSize < 10.0f) scaledSize = 10.0f;
                    baseFont = baseFont.deriveFont(scaledSize);
                    fm = g2.getFontMetrics(baseFont);
                }

                g2.setFont(baseFont);
                int textWidth = fm.stringWidth(getText());
                int x = Math.max(8, (w - textWidth) / 2);
                int y = (h + fm.getAscent() - fm.getDescent()) / 2;

                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(240, 42));
        return btn;
    }

    private void updateDashboardStats() {
        int occupied = 0;
        for (ParkingSlot s : parkingSlots) {
            if (s.isOccupied()) occupied++;
        }
        int available = parkingSlots.size() - occupied;

        if (statAvailVal != null) statAvailVal.setText(String.valueOf(available));
        if (statOccupiedVal != null) statOccupiedVal.setText(String.valueOf(occupied));
        if (statRevenueVal != null) statRevenueVal.setText(String.format("PKR %.0f", totalEarnings));
        if (statBookingsVal != null) statBookingsVal.setText(String.valueOf(totalBookingsCount));
        if (currentUserLabel != null && currentUser != null) currentUserLabel.setText("Operator: " + currentUser.getname());
    }

    // VISUAL PARKING SLOT GRID SCREEN (FLOOR MAP)
    private JPanel createSlotGridScreen() {
        JPanel mainWrapper = new JPanel(new BorderLayout(16, 16));
        mainWrapper.setBackground(COLOR_BG);
        mainWrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        slotsGridContainer = new JPanel(new GridLayout(2, 6, 16, 16));
        slotsGridContainer.setOpaque(false);

        mainWrapper.add(slotsGridContainer, BorderLayout.CENTER);

        refreshSlotGridUI();
        return mainWrapper;
    }

    private void refreshSlotGridUI() {
        if (slotsGridContainer == null) return;
        slotsGridContainer.removeAll();

        for (ParkingSlot slot : parkingSlots) {
            JButton slotBtn = new JButton();
            slotBtn.setFocusPainted(false);
            slotBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (!slot.isOccupied()) {
                slotBtn.setBackground(COLOR_SUCCESS);
                slotBtn.setForeground(Color.WHITE);
                slotBtn.setText("<html><center><font size='4'><b>SLOT " + slot.getSlotId() + "</b></font><br><font size='2'>🟢 FREE</font></center></html>");
            } else {
                slotBtn.setBackground(COLOR_DANGER);
                slotBtn.setForeground(Color.WHITE);
                slotBtn.setText("<html><center><font size='4'><b>SLOT " + slot.getSlotId() + "</b></font><br><font size='2'>🔴 " + slot.getOccupiedVehiclePlate() + "</font></center></html>");
            }

            slotBtn.addActionListener(e -> {
                if (!slot.isOccupied()) {
                    openCheckInDialog(slot);
                } else {
                    openCheckOutDialog(slot);
                }
            });

            slotsGridContainer.add(slotBtn);
        }

        slotsGridContainer.revalidate();
        slotsGridContainer.repaint();
        updateDashboardStats();
    }

    // --- REAL-WORLD CHECK-IN (ENTRY GATE) WORKFLOW ---
    private void openCheckInDialog(ParkingSlot preSelectedSlot) {
        // Select Free Slot
        ParkingSlot targetSlot = preSelectedSlot;
        if (targetSlot == null) {
            List<String> freeSlotNames = new ArrayList<>();
            for (ParkingSlot s : parkingSlots) {
                if (!s.isOccupied()) freeSlotNames.add(s.getSlotId());
            }
            if (freeSlotNames.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All parking slots are currently occupied!", "Parking Facility Full", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String selectedSlotName = (String) JOptionPane.showInputDialog(this, "Select Available Parking Slot:", "Gate Check-In",
                    JOptionPane.QUESTION_MESSAGE, null, freeSlotNames.toArray(), freeSlotNames.get(0));
            if (selectedSlotName == null) return;
            for (ParkingSlot s : parkingSlots) {
                if (s.getSlotId().equals(selectedSlotName)) {
                    targetSlot = s;
                    break;
                }
            }
        }

        if (targetSlot == null || targetSlot.isOccupied()) return;

        // Enter Vehicle Number Plate
        String vehiclePlate = JOptionPane.showInputDialog(this, "Enter Vehicle Number Plate (e.g. ABC-1234):", "Check-In - Slot " + targetSlot.getSlotId(), JOptionPane.QUESTION_MESSAGE);
        if (vehiclePlate == null || vehiclePlate.trim().isEmpty()) return;
        vehiclePlate = vehiclePlate.trim().toUpperCase();

        // VALIDATION: Active Vehicle Duplication Protection
        for (ParkingSlot slot : parkingSlots) {
            if (slot.isOccupied() && slot.getOccupiedVehiclePlate().equalsIgnoreCase(vehiclePlate)) {
                JOptionPane.showMessageDialog(this, "Vehicle " + vehiclePlate + " is ALREADY parked in Slot " + slot.getSlotId() + "!\nDuplicate active check-in is not allowed.", "Duplicate Vehicle Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Select Vehicle Category / Dynamic Hourly Rate
        String[] categories = {
                "Car / Sedan (PKR 100 / hr)",
                "Bike / Motorbike (PKR 50 / hr)",
                "SUV / Heavy Vehicle (PKR 150 / hr)",
                "EV Charging Station (PKR 200 / hr)"
        };
        String selectedCategoryStr = (String) JOptionPane.showInputDialog(this, "Select Vehicle Category / Dynamic Rate:", "Check-In - Category",
                JOptionPane.QUESTION_MESSAGE, null, categories, categories[0]);
        if (selectedCategoryStr == null) return;

        String vehicleType = "Car";
        double ratePerHour = 100.0;
        if (selectedCategoryStr.contains("Bike")) { vehicleType = "Bike"; ratePerHour = 50.0; }
        else if (selectedCategoryStr.contains("SUV")) { vehicleType = "SUV"; ratePerHour = 150.0; }
        else if (selectedCategoryStr.contains("EV")) { vehicleType = "EV"; ratePerHour = 200.0; }

        long entryTimeMillis = System.currentTimeMillis();
        String ticketId = "TKT-" + (ticketCounter++);
        String entryTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(entryTimeMillis));

        // Create Ticket
        ParkingTicket ticket = new ParkingTicket(ticketId, targetSlot.getSlotId(), vehiclePlate, vehicleType, entryTimeMillis, ratePerHour);
        ticketHistory.add(ticket);

        // Assign Slot
        targetSlot.setOccupied(true);
        targetSlot.setOccupiedVehiclePlate(vehiclePlate);
        targetSlot.setOccupiedVehicleType(vehicleType);
        targetSlot.setTicketId(ticketId);
        targetSlot.setEntryTimeMillis(entryTimeMillis);

        saveTicketToFile(ticket);
        saveAllData();
        refreshSlotGridUI();

        // Display Entry Slip
        showEntrySlipPopup(ticket, entryTimeStr);
    }

    private void showEntrySlipPopup(ParkingTicket ticket, String entryTimeStr) {
        String slipText = String.format(
                "=========================================\n" +
                "        PARKING GATE ENTRY SLIP          \n" +
                "=========================================\n" +
                " Ticket ID      : %s\n" +
                " Assigned Slot  : Slot %s\n" +
                " Vehicle Plate  : %s\n" +
                " Vehicle Type   : %s\n" +
                " Hourly Tariff  : PKR %.2f / hr\n" +
                " Entry Time     : %s\n" +
                "-----------------------------------------\n" +
                " Note: Payment will be calculated upon   \n" +
                " exit based on actual duration parked.   \n" +
                "=========================================\n",
                ticket.getTicketId(), ticket.getSlotId(), ticket.getVehiclePlate(),
                ticket.getVehicleType(), ticket.getRatePerHour(), entryTimeStr
        );

        JTextArea textArea = new JTextArea(slipText);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Entry Slip - " + ticket.getTicketId(), JOptionPane.INFORMATION_MESSAGE);
    }

    // --- REAL-WORLD CHECK-OUT (EXIT GATE & DURATION BILLING) WORKFLOW ---
    private void openSelectCheckoutDialog() {
        List<String> occupiedSlots = new ArrayList<>();
        for (ParkingSlot s : parkingSlots) {
            if (s.isOccupied()) {
                occupiedSlots.add(String.format("Slot %s | %s (%s)", s.getSlotId(), s.getOccupiedVehiclePlate(), s.getTicketId()));
            }
        }

        if (occupiedSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "There are currently no parked vehicles to check out.", "No Active Parked Vehicles", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String selectedStr = (String) JOptionPane.showInputDialog(this, "Select Occupied Slot / Vehicle to Check Out:", "Gate Check-Out",
                JOptionPane.QUESTION_MESSAGE, null, occupiedSlots.toArray(), occupiedSlots.get(0));
        if (selectedStr == null) return;

        String slotId = selectedStr.split(" ")[1];
        for (ParkingSlot s : parkingSlots) {
            if (s.getSlotId().equals(slotId)) {
                openCheckOutDialog(s);
                break;
            }
        }
    }

    private void openCheckOutDialog(ParkingSlot slot) {
        long exitTimeMillis = System.currentTimeMillis();
        long durationMs = exitTimeMillis - slot.getEntryTimeMillis();

        // Duration in hours (Minimum 1 hour, ceil to next whole hour)
        int hours = (int) Math.max(1, Math.ceil(durationMs / (1000.0 * 3600.0)));

        // Find active ticket
        ParkingTicket activeTicket = null;
        for (ParkingTicket t : ticketHistory) {
            if (t.getTicketId().equals(slot.getTicketId()) && !t.isSettled()) {
                activeTicket = t;
                break;
            }
        }

        double ratePerHour = 100.0;
        if (activeTicket != null) ratePerHour = activeTicket.getRatePerHour();
        double totalBill = hours * ratePerHour;

        String entryTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(slot.getEntryTimeMillis()));
        String exitTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(exitTimeMillis));

        String confirmMsg = String.format(
                "CHECK-OUT CONFIRMATION - Slot %s\n\n" +
                "Vehicle Plate  : %s\n" +
                "Ticket ID      : %s\n" +
                "Entry Time     : %s\n" +
                "Exit Time      : %s\n" +
                "Duration Parked: %d Hour(s)\n" +
                "Hourly Rate    : PKR %.2f / hr\n\n" +
                "TOTAL BILL DUE : PKR %.2f\n\n" +
                "Confirm payment and release slot?",
                slot.getSlotId(), slot.getOccupiedVehiclePlate(), slot.getTicketId(),
                entryTimeStr, exitTimeStr, hours, ratePerHour, totalBill
        );

        int option = JOptionPane.showConfirmDialog(this, confirmMsg, "Confirm Check-Out - Slot " + slot.getSlotId(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {

            if (activeTicket != null) {
                activeTicket.settleTicket(exitTimeMillis, hours, totalBill);
            }

            totalEarnings += totalBill;
            totalBookingsCount++;
            totalHoursBooked += hours;

            // Release Slot
            slot.setOccupied(false);
            slot.setOccupiedVehiclePlate("");
            slot.setOccupiedVehicleType("");
            slot.setTicketId("");
            slot.setEntryTimeMillis(0);

            saveAllData();
            refreshSlotGridUI();

            // Display Final Settlement Invoice Receipt
            showFinalInvoicePopup(slot.getSlotId(), slot.getOccupiedVehiclePlate(), activeTicket != null ? activeTicket.getTicketId() : "TKT-LOG",
                    entryTimeStr, exitTimeStr, hours, ratePerHour, totalBill);
        }
    }

    private void showFinalInvoicePopup(String slotId, String plate, String ticketId, String entryStr, String exitStr, int hours, double rate, double total) {
        String invoiceText = String.format(
                "=========================================\n" +
                "       PARKING SETTLEMENT INVOICE        \n" +
                "=========================================\n" +
                " Ticket ID      : %s\n" +
                " Slot Released  : Slot %s\n" +
                " Vehicle Plate  : %s\n" +
                " Entry Time     : %s\n" +
                " Exit Time      : %s\n" +
                " Duration       : %d Hour(s)\n" +
                " Rate / Hour    : PKR %.2f\n" +
                "-----------------------------------------\n" +
                " TOTAL PAID     : PKR %.2f\n" +
                " STATUS         : SETTLED & PAID ✅\n" +
                "=========================================\n" +
                "    Thank you! Safe Journey Ahead! 🚗    \n",
                ticketId, slotId, plate, entryStr, exitStr, hours, rate, total
        );

        JTextArea textArea = new JTextArea(invoiceText);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Final Settlement Invoice - " + ticketId, JOptionPane.INFORMATION_MESSAGE);
    }

    private void openSearchDialog() {
        String query = JOptionPane.showInputDialog(this, "Enter Vehicle Plate, Model Name, or Ticket ID:", "Search & Lookup", JOptionPane.QUESTION_MESSAGE);
        if (query == null || query.trim().isEmpty()) return;

        query = query.trim();
        List<Vehicle> matches = vehicleRegistration.searchVehicles(query);
        StringBuilder sb = new StringBuilder();

        if (!matches.isEmpty()) {
            sb.append("🚘 Matching Vehicle Directory Records (").append(matches.size()).append("):\n");
            for (Vehicle v : matches) {
                sb.append("• ").append(v.toString()).append("\n");
            }
            sb.append("\n");
        }

        List<ParkingTicket> ticketMatches = new ArrayList<>();
        for (ParkingTicket t : ticketHistory) {
            if (t.getTicketId().equalsIgnoreCase(query) || t.getVehiclePlate().equalsIgnoreCase(query)) {
                ticketMatches.add(t);
            }
        }

        if (!ticketMatches.isEmpty()) {
            sb.append("🎟️ Matching Parking Tickets (").append(ticketMatches.size()).append("):\n");
            for (ParkingTicket t : ticketMatches) {
                sb.append("• ").append(t.getSummaryString()).append("\n");
            }
        }

        if (sb.length() == 0) {
            JOptionPane.showMessageDialog(this, "No record found matching: " + query, "Search Result", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, sb.toString(), "Search Result", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // VEHICLE MANAGEMENT SCREEN
    private JPanel createVehicleScreen() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel card = createCardPanel("🚘 Vehicle Inventory Directory");
        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);

        JTextField modelField = createStyledTextField(18);
        JTextField yearField = createStyledTextField(18);
        JTextField companyField = createStyledTextField(18);
        JTextField plateField = createStyledTextField(18);
        JTextField removePlateField = createStyledTextField(18);

        JButton addBtn = createPrimaryButton("Add Vehicle", COLOR_SUCCESS);
        JButton removeBtn = createPrimaryButton("Remove Vehicle", COLOR_DANGER);
        JButton showBtn = createPrimaryButton("Show All Registered Vehicles", COLOR_PRIMARY);

        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formGrid.add(new JLabel("Model Name:"), gbc); gbc.gridx = 1; formGrid.add(modelField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formGrid.add(new JLabel("Manufacture Year:"), gbc); gbc.gridx = 1; formGrid.add(yearField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formGrid.add(new JLabel("Company:"), gbc); gbc.gridx = 1; formGrid.add(companyField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formGrid.add(new JLabel("Number Plate:"), gbc); gbc.gridx = 1; formGrid.add(plateField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formGrid.add(addBtn, gbc);

        gbc.gridy++; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        formGrid.add(new JLabel("Remove Plate:"), gbc); gbc.gridx = 1; formGrid.add(removePlateField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        formGrid.add(removeBtn, gbc);

        gbc.gridy++; formGrid.add(showBtn, gbc);

        addBtn.addActionListener(e -> {
            String model = modelField.getText().trim();
            String yearStr = yearField.getText().trim();
            String company = companyField.getText().trim();
            String plate = plateField.getText().trim();

            if (model.isEmpty() || yearStr.isEmpty() || company.isEmpty() || plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all vehicle fields.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int year = Integer.parseInt(yearStr);
                if (year < 1886 || year > 2100) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid year.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (vehicleRegistration.addVehicle(model, year, company, plate)) {
                    modelField.setText(""); yearField.setText(""); companyField.setText(""); plateField.setText("");
                    saveAllData();
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric year.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        removeBtn.addActionListener(e -> {
            String plate = removePlateField.getText().trim();
            if (plate.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a Number Plate to remove.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (vehicleRegistration.removeVehicle(plate)) {
                removePlateField.setText("");
                saveAllData();
            }
        });

        showBtn.addActionListener(e -> {
            if (vehicleRegistration.getCount() == 0) {
                JOptionPane.showMessageDialog(this, "No vehicles registered yet.", "Vehicles List", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder("Registered Vehicles (" + vehicleRegistration.getCount() + "):\n\n");
            for (Vehicle v : vehicleRegistration.getVehicles()) {
                sb.append("• ").append(v.toString()).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(), "Vehicles List", JOptionPane.INFORMATION_MESSAGE);
        });

        card.add(formGrid, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // REPORTS SCREEN
    private JPanel createReportScreen() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(COLOR_BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel card = createCardPanel("📈 Performance Metrics & Audit Logs");
        JPanel grid = new JPanel(new GridLayout(2, 2, 16, 16));
        grid.setOpaque(false);

        JButton earningsBtn = createPrimaryButton("💵 Daily Earnings Report", COLOR_PRIMARY);
        JButton vehicleUsageBtn = createPrimaryButton("🚗 Vehicle Usage Report", COLOR_SUCCESS);
        JButton slotUsageBtn = createPrimaryButton("🅿️ Slot Usage Report", COLOR_SECONDARY);
        JButton historyBtn = createPrimaryButton("🎟️ Transaction History Log", COLOR_PRIMARY);

        grid.add(earningsBtn);
        grid.add(vehicleUsageBtn);
        grid.add(slotUsageBtn);
        grid.add(historyBtn);

        earningsBtn.addActionListener(e -> {
            String msg = String.format("Daily Earnings Summary:\n\n• Total Revenue Generated: PKR %.2f\n• Total Completed Bookings: %d\n• Total Parking Hours: %d hrs",
                    totalEarnings, totalBookingsCount, totalHoursBooked);
            JOptionPane.showMessageDialog(this, msg, "Daily Earnings", JOptionPane.INFORMATION_MESSAGE);
        });

        vehicleUsageBtn.addActionListener(e -> {
            int count = vehicleRegistration.getCount();
            String msg = String.format("Vehicle Usage Report:\n\n• Total Registered Vehicles: %d\n• Active Inventory Records: %d",
                    count, count);
            JOptionPane.showMessageDialog(this, msg, "Vehicle Usage", JOptionPane.INFORMATION_MESSAGE);
        });

        slotUsageBtn.addActionListener(e -> {
            int occupied = 0;
            for (ParkingSlot s : parkingSlots) {
                if (s.isOccupied()) occupied++;
            }
            double avgHours = totalBookingsCount > 0 ? (double) totalHoursBooked / totalBookingsCount : 0.0;
            String msg = String.format("Slot Usage Report:\n\n• Available Slots: %d / %d\n• Currently Occupied Slots: %d\n• Total Hours Booked: %d hrs\n• Avg Hours / Booking: %.1f hrs",
                    (parkingSlots.size() - occupied), parkingSlots.size(), occupied, totalHoursBooked, avgHours);
            JOptionPane.showMessageDialog(this, msg, "Slot Usage", JOptionPane.INFORMATION_MESSAGE);
        });

        historyBtn.addActionListener(e -> {
            if (ticketHistory.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No transaction history records found.", "Transaction History", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder("Transaction History Log (" + ticketHistory.size() + " tickets):\n\n");
            for (ParkingTicket t : ticketHistory) {
                sb.append("• ").append(t.getSummaryString()).append("\n");
            }
            JTextArea textArea = new JTextArea(sb.toString(), 15, 50);
            textArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Transaction History Log", JOptionPane.INFORMATION_MESSAGE);
        });

        card.add(grid, BorderLayout.CENTER);
        wrapper.add(card, BorderLayout.CENTER);
        return wrapper;
    }

    // LOGIN & REGISTER ISOLATED VIEWS
    private JPanel createLoginView() {
        JPanel card = createGridCardPanel();
        card.setPreferredSize(new Dimension(480, 500));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);

        JLabel iconHeader = new JLabel("🚗", SwingConstants.CENTER);
        iconHeader.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));

        JLabel title = new JLabel("PARK SMART PRO", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_DARK);

        JLabel subTitle = new JLabel("Sign in to manage parking facility operations", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(COLOR_TEXT_MUTED);

        JTextField emailField = createStyledTextField(20);
        JPasswordField passwordField = createStyledPasswordField(20);

        // Ensure minimum preferred size for fields
        emailField.setPreferredSize(new Dimension(240, 38));
        passwordField.setPreferredSize(new Dimension(240, 38));

        JButton loginButton = createPrimaryButton("Login to Dashboard", COLOR_PRIMARY);
        JButton registerButton = createPrimaryButton("Register New Operator", COLOR_SECONDARY);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        card.add(iconHeader, gbc);

        gbc.gridy++;
        card.add(title, gbc);

        gbc.gridy++;
        card.add(subTitle, gbc);

        gbc.gridy++; gbc.insets = new Insets(16, 10, 6, 10);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel emailLbl = new JLabel("Email Address:");
        emailLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        emailLbl.setForeground(COLOR_TEXT_DARK);
        card.add(emailLbl, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        card.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.insets = new Insets(6, 10, 6, 10); gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        passLbl.setForeground(COLOR_TEXT_DARK);
        card.add(passLbl, gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        card.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 10, 6, 10);
        card.add(loginButton, gbc);

        gbc.gridy++; gbc.insets = new Insets(6, 10, 10, 10);
        card.add(registerButton, gbc);

        loginButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both Email and Password.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Register matchedUser = null;
            for (Register user : registeredUsers) {
                Login login = new Login(email, password);
                if (login.login(user)) {
                    matchedUser = user;
                    break;
                }
            }

            if (matchedUser != null) {
                isLogin = true;
                currentUser = matchedUser;
                emailField.setText(""); passwordField.setText("");
                cardLayoutShow("AppLayout");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials or user not registered.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> contentCardLayout.show(mainContentPanel, "Register"));

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(COLOR_BG);
        wrap.add(card);
        return wrap;
    }

    private JPanel createRegisterView() {
        JPanel card = createGridCardPanel();
        card.setPreferredSize(new Dimension(500, 540));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);

        JLabel title = new JLabel("Operator Registration", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(COLOR_TEXT_DARK);

        JLabel subTitle = new JLabel("Register new parking operator credentials", SwingConstants.CENTER);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(COLOR_TEXT_MUTED);

        JTextField nameField = createStyledTextField(20);
        JTextField emailField = createStyledTextField(20);
        JPasswordField passwordField = createStyledPasswordField(20);
        JTextField phoneField = createStyledTextField(20);

        nameField.setPreferredSize(new Dimension(240, 38));
        emailField.setPreferredSize(new Dimension(240, 38));
        passwordField.setPreferredSize(new Dimension(240, 38));
        phoneField.setPreferredSize(new Dimension(240, 38));

        JRadioButton maleRb = new JRadioButton("Male", true);
        JRadioButton femaleRb = new JRadioButton("Female");
        maleRb.setOpaque(false); femaleRb.setOpaque(false);
        maleRb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        femaleRb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ButtonGroup genderGrp = new ButtonGroup();
        genderGrp.add(maleRb); genderGrp.add(femaleRb);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        genderPanel.setOpaque(false);
        genderPanel.add(maleRb); genderPanel.add(femaleRb);

        JButton submitButton = createPrimaryButton("Submit Registration", COLOR_SUCCESS);
        JButton backButton = createPrimaryButton("Back to Login", COLOR_SECONDARY);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        card.add(title, gbc);

        gbc.gridy++;
        card.add(subTitle, gbc);

        gbc.gridy++; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel nameLbl = new JLabel("Full Name:");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(nameLbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        card.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel emailLbl = new JLabel("Email Address:");
        emailLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(emailLbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        card.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel passLbl = new JLabel("Password:");
        passLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(passLbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        card.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel phoneLbl = new JLabel("Phone Number:");
        phoneLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(phoneLbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        card.add(phoneField, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel genderLbl = new JLabel("Gender:");
        genderLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(genderLbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        card.add(genderPanel, gbc);

        gbc.gridx = 0; gbc.gridy++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        gbc.insets = new Insets(16, 10, 6, 10);
        card.add(submitButton, gbc);

        gbc.gridy++; gbc.insets = new Insets(4, 10, 10, 10);
        card.add(backButton, gbc);

        submitButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String phone = phoneField.getText().trim();
            String gender = maleRb.isSelected() ? "Male" : "Female";

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            for (Register u : registeredUsers) {
                if (u.getemail().equalsIgnoreCase(email)) {
                    JOptionPane.showMessageDialog(this, "Email is already registered.", "Registration Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Register newUser = new Register();
            newUser.setname(name); newUser.setemail(email); newUser.setpassword(password);
            newUser.setphoneNumber(phone); newUser.setgender(gender);
            registeredUsers.add(newUser);
            saveAllData();

            JOptionPane.showMessageDialog(this, "Account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            nameField.setText(""); emailField.setText(""); passwordField.setText(""); phoneField.setText("");
            contentCardLayout.show(mainContentPanel, "Login");
        });

        backButton.addActionListener(e -> contentCardLayout.show(mainContentPanel, "Login"));

        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setBackground(COLOR_BG);
        wrap.add(card);
        return wrap;
    }

    private JTextField createStyledTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return tf;
    }

    private JPasswordField createStyledPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return pf;
    }

    // PERSISTENCE STORAGE
    private void saveTicketToFile(ParkingTicket ticket) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("parking_tickets.txt", true))) {
            writer.write(ticket.toFileFormat() + "\n");
        } catch (IOException ignored) {}
    }

    private void saveAllData() {
        try {
            try (BufferedWriter w = new BufferedWriter(new FileWriter("users_data.txt"))) {
                for (Register u : registeredUsers) {
                    w.write(u.getname() + ";" + u.getemail() + ";" + u.getpassword() + ";" + u.getphoneNumber() + ";" + u.getgender() + "\n");
                }
            }
            try (BufferedWriter w = new BufferedWriter(new FileWriter("vehicles_data.txt"))) {
                for (Vehicle v : vehicleRegistration.getVehicles()) {
                    w.write(v.getModelName() + ";" + v.getYear() + ";" + v.getCompany() + ";" + v.getNumberPlate() + "\n");
                }
            }
            try (BufferedWriter w = new BufferedWriter(new FileWriter("state_data.txt"))) {
                w.write(totalEarnings + ";" + totalBookingsCount + ";" + totalHoursBooked + ";" + ticketCounter + "\n");
                for (ParkingSlot s : parkingSlots) {
                    w.write(s.getSlotId() + ";" + s.isOccupied() + ";" + (s.getOccupiedVehiclePlate().isEmpty() ? "NONE" : s.getOccupiedVehiclePlate()) + ";" + (s.getOccupiedVehicleType().isEmpty() ? "NONE" : s.getOccupiedVehicleType()) + ";" + (s.getTicketId().isEmpty() ? "NONE" : s.getTicketId()) + ";" + s.getEntryTimeMillis() + "\n");
                }
            }
        } catch (IOException ignored) {}
    }

    private void loadAllData() {
        try {
            File uFile = new File("users_data.txt");
            if (uFile.exists()) {
                try (BufferedReader r = new BufferedReader(new FileReader(uFile))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        String[] parts = line.split(";");
                        if (parts.length >= 5) {
                            Register reg = new Register();
                            reg.setname(parts[0]); reg.setemail(parts[1]); reg.setpassword(parts[2]);
                            reg.setphoneNumber(parts[3]); reg.setgender(parts[4]);
                            registeredUsers.add(reg);
                        }
                    }
                }
            }

            File vFile = new File("vehicles_data.txt");
            if (vFile.exists()) {
                try (BufferedReader r = new BufferedReader(new FileReader(vFile))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        String[] parts = line.split(";");
                        if (parts.length >= 4) {
                            vehicleRegistration.addVehicleSilent(parts[0], Integer.parseInt(parts[1]), parts[2], parts[3]);
                        }
                    }
                }
            }

            File sFile = new File("state_data.txt");
            if (sFile.exists()) {
                try (BufferedReader r = new BufferedReader(new FileReader(sFile))) {
                    String line = r.readLine();
                    if (line != null) {
                        String[] parts = line.split(";");
                        if (parts.length >= 4) {
                            totalEarnings = Double.parseDouble(parts[0]);
                            totalBookingsCount = Integer.parseInt(parts[1]);
                            totalHoursBooked = Integer.parseInt(parts[2]);
                            ticketCounter = Integer.parseInt(parts[3]);
                        }
                    }
                    while ((line = r.readLine()) != null) {
                        String[] parts = line.split(";");
                        if (parts.length >= 6) {
                            for (ParkingSlot slot : parkingSlots) {
                                if (slot.getSlotId().equals(parts[0])) {
                                    slot.setOccupied(Boolean.parseBoolean(parts[1]));
                                    slot.setOccupiedVehiclePlate(parts[2].equals("NONE") ? "" : parts[2]);
                                    slot.setOccupiedVehicleType(parts[3].equals("NONE") ? "" : parts[3]);
                                    slot.setTicketId(parts[4].equals("NONE") ? "" : parts[4]);
                                    slot.setEntryTimeMillis(Long.parseLong(parts[5]));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CarParkingSystemGUI().setVisible(true));
    }
}

class ParkingSlot {
    private String slotId;
    private boolean isOccupied;
    private String occupiedVehiclePlate;
    private String occupiedVehicleType;
    private String ticketId;
    private long entryTimeMillis;

    public ParkingSlot(String slotId) {
        this.slotId = slotId;
        this.isOccupied = false;
        this.occupiedVehiclePlate = "";
        this.occupiedVehicleType = "";
        this.ticketId = "";
        this.entryTimeMillis = 0;
    }

    public String getSlotId() { return slotId; }
    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }
    public String getOccupiedVehiclePlate() { return occupiedVehiclePlate; }
    public void setOccupiedVehiclePlate(String vehiclePlate) { this.occupiedVehiclePlate = vehiclePlate; }
    public String getOccupiedVehicleType() { return occupiedVehicleType; }
    public void setOccupiedVehicleType(String vehicleType) { this.occupiedVehicleType = vehicleType; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public long getEntryTimeMillis() { return entryTimeMillis; }
    public void setEntryTimeMillis(long timeMs) { this.entryTimeMillis = timeMs; }
}

class ParkingTicket {
    private String ticketId;
    private String slotId;
    private String vehiclePlate;
    private String vehicleType;
    private long entryTimeMillis;
    private long exitTimeMillis;
    private int hours;
    private double ratePerHour;
    private double totalAmount;
    private boolean isSettled;

    public ParkingTicket(String ticketId, String slotId, String vehiclePlate, String vehicleType, long entryTimeMillis, double ratePerHour) {
        this.ticketId = ticketId;
        this.slotId = slotId;
        this.vehiclePlate = vehiclePlate;
        this.vehicleType = vehicleType;
        this.entryTimeMillis = entryTimeMillis;
        this.ratePerHour = ratePerHour;
        this.exitTimeMillis = 0;
        this.hours = 0;
        this.totalAmount = 0;
        this.isSettled = false;
    }

    public String getTicketId() { return ticketId; }
    public String getSlotId() { return slotId; }
    public String getVehiclePlate() { return vehiclePlate; }
    public String getVehicleType() { return vehicleType; }
    public long getEntryTimeMillis() { return entryTimeMillis; }
    public double getRatePerHour() { return ratePerHour; }
    public boolean isSettled() { return isSettled; }
    public int getHours() { return hours; }
    public double getTotalAmount() { return totalAmount; }

    public void settleTicket(long exitTimeMillis, int hours, double totalAmount) {
        this.exitTimeMillis = exitTimeMillis;
        this.hours = hours;
        this.totalAmount = totalAmount;
        this.isSettled = true;
    }

    public String getSummaryString() {
        String entryStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(entryTimeMillis));
        if (isSettled) {
            String exitStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(exitTimeMillis));
            return String.format("[%s] Slot %s | Vehicle %s (%s) | %d hrs | PKR %.2f | Entry: %s | Exit: %s",
                    ticketId, slotId, vehiclePlate, vehicleType, hours, totalAmount, entryStr, exitStr);
        } else {
            return String.format("[%s] Slot %s | Vehicle %s (%s) | ACTIVE | Entry: %s",
                    ticketId, slotId, vehiclePlate, vehicleType, entryStr);
        }
    }

    public String toFileFormat() {
        return String.format("%s;%s;%s;%s;%d;%d;%d;%.2f;%.2f;%b",
                ticketId, slotId, vehiclePlate, vehicleType, entryTimeMillis, exitTimeMillis, hours, ratePerHour, totalAmount, isSettled);
    }
}

class Vehicle {
    private String modelName;
    private int year;
    private String company;
    private String numberPlate;

    public Vehicle(String modelName, int year, String company, String numberPlate) {
        this.modelName = modelName;
        this.year = year;
        this.company = company;
        this.numberPlate = numberPlate;
    }

    public String getModelName() { return modelName; }
    public int getYear() { return year; }
    public String getCompany() { return company; }
    public String getNumberPlate() { return numberPlate; }

    @Override
    public String toString() {
        return "Model Name: " + modelName + ", Year: " + year + ", Company: " + company + ", Number Plate: " + numberPlate;
    }
}

class VehicleRegistration {
    private List<Vehicle> vehicles;

    public VehicleRegistration() {
        vehicles = new ArrayList<>();
    }

    public boolean addVehicle(String modelName, int year, String company, String numberPlate) {
        for (Vehicle v : vehicles) {
            if (v.getNumberPlate().equalsIgnoreCase(numberPlate)) {
                JOptionPane.showMessageDialog(null, "Vehicle with Number Plate '" + numberPlate + "' is already registered.", "Duplicate Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        vehicles.add(new Vehicle(modelName, year, company, numberPlate));
        JOptionPane.showMessageDialog(null, "Vehicle added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    public void addVehicleSilent(String modelName, int year, String company, String numberPlate) {
        vehicles.add(new Vehicle(modelName, year, company, numberPlate));
    }

    public boolean removeVehicle(String numberPlate) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getNumberPlate().equalsIgnoreCase(numberPlate)) {
                vehicles.remove(i);
                JOptionPane.showMessageDialog(null, "Vehicle removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            }
        }
        JOptionPane.showMessageDialog(null, "Vehicle with Number Plate '" + numberPlate + "' not found.", "Not Found", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public List<Vehicle> getVehicles() {
        return new ArrayList<>(vehicles);
    }

    public String[] getVehicleNumbers() {
        String[] vehicleNumbers = new String[vehicles.size()];
        for (int i = 0; i < vehicles.size(); i++) {
            vehicleNumbers[i] = vehicles.get(i).getNumberPlate();
        }
        return vehicleNumbers;
    }

    public List<Vehicle> searchVehicles(String query) {
        List<Vehicle> matches = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Vehicle v : vehicles) {
            if (v.getNumberPlate().toLowerCase().contains(lowerQuery) ||
                v.getModelName().toLowerCase().contains(lowerQuery) ||
                v.getCompany().toLowerCase().contains(lowerQuery)) {
                matches.add(v);
            }
        }
        return matches;
    }

    public int getCount() {
        return vehicles.size();
    }
}

class BookSlot {
    public void bookSlot(int hours) {
        JOptionPane.showMessageDialog(null, "Slot booked successfully for " + hours + " hour(s).", "Slot Booked", JOptionPane.INFORMATION_MESSAGE);
    }
}

class Payment {
    private double pricePerHour;

    public Payment(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public double generateBill(int hours) {
        return pricePerHour * hours;
    }
}

class Register {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private String gender;

    public void setname(String name) { this.name = name; }
    public void setemail(String email) { this.email = email; }
    public void setpassword(String password) { this.password = password; }
    public void setphoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setgender(String gender) { this.gender = gender; }

    public String getname() { return name; }
    public String getemail() { return email; }
    public String getpassword() { return password; }
    public String getphoneNumber() { return phoneNumber; }
    public String getgender() { return gender; }
}

class Login {
    private String email;
    private String password;

    public Login(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean login(Register registeredUser) {
        return registeredUser != null &&
               registeredUser.getemail().equalsIgnoreCase(email) &&
               registeredUser.getpassword().equals(password);
    }
}
