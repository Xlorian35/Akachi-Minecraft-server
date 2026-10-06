package akachi.launcher;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.DefaultListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.awt.image.BufferedImage;
import java.awt.geom.Path2D;
import javax.imageio.ImageIO;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.net.httpserver.HttpServer;

import static java.awt.GridBagConstraints.WEST;

public final class AkachiLauncher {
    private static final String REPOSITORY = "Xlorian35/Akachi-Minecraft-server";
    private static final String BRANCH = "main";
    private static final String MINECRAFT_VERSION = "1.20.1";
    private static final String FORGE_VERSION = "47.4.26";
    private static final String FORGE_PROFILE = MINECRAFT_VERSION + "-forge-" + FORGE_VERSION;
    private static final String FORGE_INSTALLER_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/"
            + MINECRAFT_VERSION + "-" + FORGE_VERSION + "/forge-" + MINECRAFT_VERSION + "-" + FORGE_VERSION
            + "-installer.jar";
    private static final String FORGE_INSTALLER_SHA1 = "27b38c97615d138101caa13c01e6bfda6176dea3";
    private static final int OAUTH_CALLBACK_PORT = 43821;
    private static final String OAUTH_CALLBACK_PATH = "/auth/callback";
    private static final String OAUTH_CALLBACK_URL = "http://127.0.0.1:" + OAUTH_CALLBACK_PORT + OAUTH_CALLBACK_PATH;
    // Fill with the project's public URL and publishable key; never put a secret/service_role key here.
    private static final String SUPABASE_URL = "https://cbjtejlgkymaoedvuong.supabase.co";
    private static final String SUPABASE_PUBLISHABLE_KEY = "sb_publishable_uLCNckMO7E3-N6JFYEmZ_A_toGH390_";
    // Fill these once with the Akachi server's Java address. Players won't enter an address in the launcher.
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 25565;
    private static final boolean PREVIEW_MODE = Boolean.getBoolean("akachi.preview");
    private static final Path AKACHI_DIRECTORY = appDataDirectory();
    private static final Path MINECRAFT_DIRECTORY = AKACHI_DIRECTORY.resolve("Minecraft");
    private static final Color BACKGROUND = new Color(15, 19, 25);
    private static final Color CARD = new Color(25, 31, 40);
    private static final Color CARD_ALT = new Color(31, 38, 48);
    private static final Color TEXT = new Color(238, 205, 208);
    private static final Color MUTED = new Color(181, 126, 132);
    private static final Color GREEN = new Color(172, 49, 61);
    private static final Color BLUE = new Color(195, 79, 89);
    private static final Color RED = new Color(224, 83, 94);
    private static HttpClient httpClient() {
        return HttpClientHolder.INSTANCE;
    }

    private static final class HttpClientHolder {
        private static final HttpClient INSTANCE = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(12))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    private final JFrame frame = new JFrame("Akachi Launcher");
    private final JLabel serverAddress = new JLabel(serverDisplay());
    private final JLabel serverStatus = new JLabel("Sunucu adresini gir", SwingConstants.RIGHT);
    private final JLabel playersStatus = new JLabel("", SwingConstants.RIGHT);
    private final JLabel modStatus = new JLabel("GitHub deposundaki modlar hazır olduğunda buradan indir.");
    private final JLabel footerStatus = new JLabel("Minecraft " + MINECRAFT_VERSION + " · Forge " + FORGE_VERSION);
    private final JButton pingButton = actionButton("Sunucuyu kontrol et", false);
    private final JButton syncButton = actionButton("GitHub'dan modları indir", false);
    private final JButton launchButton = actionButton("SÜRÜMÜ KUR VE AÇ", true);
    private final JLabel authStatus = new JLabel("Supabase bağlantısı hazırlanıyor.");
    private final JTextField authUsername = new JTextField();
    private final JTextField authEmail = new JTextField();
    private final JPasswordField authPassword = new JPasswordField();
    private final JButton signInButton = actionButton("Giriş Yap", true);
    private final JButton signUpButton = actionButton("Kaydol", false);
    private final JButton discordButton = discordLoginButton();
    private final JButton signOutButton = actionButton("Çıkış Yap", false);
    private JPanel authCard;
    private boolean authenticated;
    private String signedInEmail = "";
    private String currentAccessToken = "";
    private String currentGameUsername = "";
    private final AssetPanel optionalModAssets = new AssetPanel("optional-mods", "İsteğe bağlı modlar", "jar");
    private final AssetPanel resourcePackAssets = new AssetPanel("resource-packs", "Texture pack'ler", "zip");
    private final AssetPanel shaderPackAssets = new AssetPanel("shader-packs", "Shader pack'ler", "zip");
    private int ramGb = 4;
    private String graphicsPreset = "Orta";
    private Rectangle restoreBounds;

    private AkachiLauncher() {
        setAppIcon();
        try {
            Files.createDirectories(AKACHI_DIRECTORY);
            Files.createDirectories(AKACHI_DIRECTORY.resolve("Launcher"));
            Files.createDirectories(MINECRAFT_DIRECTORY);
        } catch (IOException ex) {
            footerStatus.setText("Akachi klasörü oluşturulamadı: " + ex.getMessage());
        }
        loadSettings();
        refreshAuthCard();
        launchButton.setEnabled(false);
        buildWindow();
        if (!PREVIEW_MODE && !SERVER_HOST.isBlank()) {
            checkServer();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Swing's default look and feel is an acceptable fallback.
            }
            new AkachiLauncher();
        });
    }

    private void showSettings() {
        JDialog dialog = new JDialog(frame, "Akachi Launcher Ayarları", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(24, 26, 20, 26));

        JLabel title = new JLabel("Ayarlar");
        styleLabel(title, TEXT, 22, true);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel subtitle = new JLabel("Akachi oyun başlatma tercihleri");
        styleLabel(subtitle, MUTED, 12, false);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel ramRow = new JPanel(new BorderLayout(16, 0));
        ramRow.setOpaque(false);
        ramRow.setBorder(new EmptyBorder(24, 0, 8, 0));
        JLabel ramLabel = new JLabel("Minecraft için ayrılacak RAM");
        styleLabel(ramLabel, TEXT, 14, true);
        JSpinner ramSpinner = new JSpinner(new SpinnerNumberModel(ramGb, 2, 16, 1));
        ramSpinner.setPreferredSize(new Dimension(92, 34));
        ramSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ramRow.add(ramLabel, BorderLayout.CENTER);
        ramRow.add(ramSpinner, BorderLayout.EAST);

        JLabel ramHelp = new JLabel("GB · Forge profili bulunduğunda resmi Minecraft Launcher'a uygulanır.");
        styleLabel(ramHelp, MUTED, 11, false);
        ramHelp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel graphicsRow = new JPanel(new BorderLayout(16, 0));
        graphicsRow.setOpaque(false);
        graphicsRow.setBorder(new EmptyBorder(22, 0, 8, 0));
        JLabel graphicsLabel = new JLabel("Grafik / FPS ön ayarı");
        styleLabel(graphicsLabel, TEXT, 14, true);
        JComboBox<String> graphicsSelector = new JComboBox<>(new String[] {"Yüksek", "Orta", "Düşük", "Ultra düşük"});
        graphicsSelector.setSelectedItem(graphicsPreset);
        graphicsSelector.setPreferredSize(new Dimension(150, 34));
        graphicsSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        graphicsRow.add(graphicsLabel, BorderLayout.CENTER);
        graphicsRow.add(graphicsSelector, BorderLayout.EAST);
        JLabel graphicsHelp = new JLabel("Oyun kapalıyken kaydet; sonraki açılışta uygular.");
        styleLabel(graphicsHelp, MUTED, 11, false);
        graphicsHelp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(22, 0, 0, 0));
        JButton cancel = actionButton("İptal", false);
        JButton save = actionButton("Kaydet", true);
        cancel.addActionListener(e -> dialog.dispose());
        save.addActionListener(e -> {
            ramGb = (Integer) ramSpinner.getValue();
            graphicsPreset = (String) graphicsSelector.getSelectedItem();
            try {
                saveSettings();
                boolean applied = applyRamSettingToForgeProfile();
                applyGraphicsPreset();
                footerStatus.setText(applied
                        ? graphicsPreset + " grafik ayarı ve " + ramGb + " GB RAM kaydedildi"
                        : "Grafikler kaydedildi; Forge profili oluşunca RAM ayarı uygulanacak");
                dialog.dispose();
            } catch (IOException ex) {
                showMessage("Ayarlar kaydedilemedi.\n\n" + ex.getMessage());
            }
        });
        buttons.add(cancel);
        buttons.add(save);

        content.add(title);
        content.add(Box.createVerticalStrut(5));
        content.add(subtitle);
        content.add(ramRow);
        content.add(ramHelp);
        content.add(graphicsRow);
        content.add(graphicsHelp);
        content.add(buttons);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(470, dialog.getHeight()));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void loadSettings() {
        Path settings = AKACHI_DIRECTORY.resolve("Launcher").resolve("settings.properties");
        if (!Files.isRegularFile(settings)) return;
        Properties values = new Properties();
        try (InputStream in = Files.newInputStream(settings)) {
            values.load(in);
            int savedRam = Integer.parseInt(values.getProperty("ramGb", "4"));
            ramGb = Math.max(2, Math.min(16, savedRam));
            String savedPreset = values.getProperty("graphicsPreset", "Orta");
            if (List.of("Yüksek", "Orta", "Düşük", "Ultra düşük").contains(savedPreset)) graphicsPreset = savedPreset;
        } catch (Exception ignored) {
            ramGb = 4;
        }
    }

    private void saveSettings() throws IOException {
        Path settings = AKACHI_DIRECTORY.resolve("Launcher").resolve("settings.properties");
        Files.createDirectories(settings.getParent());
        Properties values = new Properties();
        values.setProperty("ramGb", Integer.toString(ramGb));
        values.setProperty("graphicsPreset", graphicsPreset);
        try (var out = Files.newOutputStream(settings, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            values.store(out, "Akachi Launcher settings");
        }
    }

    private void applyGraphicsPreset() throws IOException {
        Map<String, String> preset = new LinkedHashMap<>();
        switch (graphicsPreset) {
            case "Yüksek" -> {
                preset.put("graphicsMode", "2");
                preset.put("renderDistance", "12");
                preset.put("simulationDistance", "8");
                preset.put("entityDistanceScaling", "1.0");
                preset.put("clouds", "2");
                preset.put("particles", "0");
                preset.put("maxFps", "240");
                preset.put("enableVsync", "false");
                preset.put("entityShadows", "true");
                preset.put("ao", "true");
                preset.put("biomeBlendRadius", "2");
                preset.put("mipmapLevels", "4");
            }
            case "Düşük" -> {
                preset.put("graphicsMode", "0");
                preset.put("renderDistance", "6");
                preset.put("simulationDistance", "4");
                preset.put("entityDistanceScaling", "0.75");
                preset.put("clouds", "0");
                preset.put("particles", "1");
                preset.put("maxFps", "120");
                preset.put("enableVsync", "false");
                preset.put("entityShadows", "false");
                preset.put("ao", "false");
                preset.put("biomeBlendRadius", "0");
                preset.put("mipmapLevels", "0");
            }
            case "Ultra düşük" -> {
                preset.put("graphicsMode", "0");
                preset.put("renderDistance", "4");
                preset.put("simulationDistance", "3");
                preset.put("entityDistanceScaling", "0.5");
                preset.put("clouds", "0");
                preset.put("particles", "2");
                preset.put("maxFps", "240");
                preset.put("enableVsync", "false");
                preset.put("entityShadows", "false");
                preset.put("ao", "false");
                preset.put("biomeBlendRadius", "0");
                preset.put("mipmapLevels", "0");
            }
            default -> {
                preset.put("graphicsMode", "1");
                preset.put("renderDistance", "8");
                preset.put("simulationDistance", "6");
                preset.put("entityDistanceScaling", "0.9");
                preset.put("clouds", "1");
                preset.put("particles", "1");
                preset.put("maxFps", "120");
                preset.put("enableVsync", "false");
                preset.put("entityShadows", "false");
                preset.put("ao", "true");
                preset.put("biomeBlendRadius", "1");
                preset.put("mipmapLevels", "2");
            }
        }
        Path options = MINECRAFT_DIRECTORY.resolve("options.txt");
        List<String> lines = Files.isRegularFile(options)
                ? new ArrayList<>(Files.readAllLines(options, StandardCharsets.UTF_8)) : new ArrayList<>();
        Set<String> written = new HashSet<>();
        List<String> updated = new ArrayList<>();
        for (String line : lines) {
            int split = line.indexOf(':');
            String key = split > 0 ? line.substring(0, split) : "";
            if (preset.containsKey(key)) {
                updated.add(key + ":" + preset.get(key));
                written.add(key);
            } else updated.add(line);
        }
        for (Map.Entry<String, String> option : preset.entrySet()) {
            if (!written.contains(option.getKey())) updated.add(option.getKey() + ":" + option.getValue());
        }
        Files.createDirectories(MINECRAFT_DIRECTORY);
        Files.write(options, updated, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    private boolean applyRamSettingToForgeProfile() throws IOException {
        Path profileFile = MINECRAFT_DIRECTORY.resolve("launcher_profiles.json");
        if (!Files.isRegularFile(profileFile)) return false;
        String json = Files.readString(profileFile, StandardCharsets.UTF_8);
        Matcher profilesMatcher = Pattern.compile("\\\"profiles\\\"\\s*:\\s*\\{").matcher(json);
        if (!profilesMatcher.find()) return false;
        int mapOpen = profilesMatcher.end() - 1;
        int mapClose = matchingBrace(json, mapOpen);
        if (mapClose < 0) return false;

        int cursor = mapOpen + 1;
        while (cursor < mapClose) {
            while (cursor < mapClose && (Character.isWhitespace(json.charAt(cursor)) || json.charAt(cursor) == ',')) cursor++;
            if (cursor >= mapClose || json.charAt(cursor) != '\"') break;
            int keyEnd = jsonStringEnd(json, cursor);
            if (keyEnd < 0) return false;
            int valueStart = keyEnd + 1;
            while (valueStart < mapClose && Character.isWhitespace(json.charAt(valueStart))) valueStart++;
            if (valueStart >= mapClose || json.charAt(valueStart) != ':') return false;
            valueStart++;
            while (valueStart < mapClose && Character.isWhitespace(json.charAt(valueStart))) valueStart++;
            if (valueStart >= mapClose || json.charAt(valueStart) != '{') return false;
            int profileEnd = matchingBrace(json, valueStart);
            if (profileEnd < 0 || profileEnd > mapClose) return false;
            String profile = json.substring(valueStart, profileEnd + 1);
            if (FORGE_PROFILE.equals(jsonText(profile, "lastVersionId"))) {
                String args = jsonText(profile, "javaArgs");
                Matcher heap = Pattern.compile("-Xmx\\d+[mMgG]").matcher(args);
                String updatedArgs = heap.find() ? heap.replaceFirst("-Xmx" + ramGb + "G") : "-Xmx" + ramGb + "G " + args;
                Matcher argsField = Pattern.compile("\\\"javaArgs\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"").matcher(profile);
                String updatedProfile;
                if (argsField.find()) {
                    updatedProfile = profile.substring(0, argsField.start()) + "\"javaArgs\":\""
                            + escapeJson(updatedArgs) + "\"" + profile.substring(argsField.end());
                } else {
                    int insertAt = profile.length() - 1;
                    int lastContent = insertAt - 1;
                    while (lastContent >= 0 && Character.isWhitespace(profile.charAt(lastContent))) lastContent--;
                    String prefix = profile.substring(0, lastContent + 1);
                    String comma = lastContent >= 0 && profile.charAt(lastContent) != '{' ? "," : "";
                    updatedProfile = prefix + comma + "\"javaArgs\":\"" + escapeJson(updatedArgs) + "\""
                            + profile.substring(lastContent + 1);
                }
                String updatedJson = json.substring(0, valueStart) + updatedProfile + json.substring(profileEnd + 1);
                Path temporary = profileFile.resolveSibling("launcher_profiles.json.akachi-tmp");
                Files.writeString(temporary, updatedJson, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                try {
                    Files.move(temporary, profileFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException ignored) {
                    Files.move(temporary, profileFile, StandardCopyOption.REPLACE_EXISTING);
                }
                return true;
            }
            cursor = profileEnd + 1;
        }
        return false;
    }

    private static int matchingBrace(String value, int open) {
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        for (int i = open; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (ch == '\\') escaped = true;
                else if (ch == '\"') inString = false;
            } else if (ch == '\"') inString = true;
            else if (ch == '{') depth++;
            else if (ch == '}' && --depth == 0) return i;
        }
        return -1;
    }

    private static int jsonStringEnd(String value, int start) {
        boolean escaped = false;
        for (int i = start + 1; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (escaped) escaped = false;
            else if (ch == '\\') escaped = true;
            else if (ch == '\"') return i;
        }
        return -1;
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void buildWindow() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(1500, Math.max(1000, screen.width - 60));
        int height = Math.min(1000, Math.max(720, screen.height - 80));
        frame.setMinimumSize(new Dimension(Math.min(1200, width), Math.min(820, height)));
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);

        JPanel page = new JPanel(new BorderLayout(0, 22));
        page.setBackground(BACKGROUND);
        page.setBorder(new EmptyBorder(14, 26, 18, 26));
        page.add(buildHeader(), BorderLayout.NORTH);

        JPanel sections = new JPanel();
        sections.setOpaque(false);
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
        JPanel topRow = new JPanel(new GridBagLayout());
        topRow.setOpaque(false);
        GridBagConstraints topCell = new GridBagConstraints();
        topCell.gridy = 0;
        topCell.weightx = 0.5;
        topCell.fill = GridBagConstraints.HORIZONTAL;
        topCell.anchor = GridBagConstraints.NORTHWEST;
        topCell.insets = new Insets(0, 0, 0, 8);
        topRow.add(buildAssetSection("TEXTURE PACK", "İstediğin kaynak paketlerini indir; oyunda Kaynak Paketleri menüsünden etkinleştir.", resourcePackAssets), topCell);
        topCell.gridx = 1;
        topCell.insets = new Insets(0, 8, 0, 0);
        topRow.add(buildAuthCard(), topCell);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        sections.add(topRow);
        sections.add(Box.createVerticalStrut(26));

        JPanel cards = new JPanel(new GridBagLayout());
        cards.setOpaque(false);
        GridBagConstraints cell = new GridBagConstraints();
        // Keep mods and shaders down the left; put the game version in the
        // open lower-right area. Server details live in the header.
        cell.weightx = 0.5;
        cell.anchor = GridBagConstraints.NORTHWEST;
        cell.fill = GridBagConstraints.HORIZONTAL;
        cell.gridx = 0;
        cell.gridy = 0;
        cell.insets = new Insets(0, 0, 14, 8);
        cards.add(buildOptionalModsCard(), cell);
        cell.gridx = 0;
        cell.gridy = 1;
        cell.insets = new Insets(0, 0, 14, 8);
        cards.add(buildAssetSection("SHADER PACK", "Shader arşivlerini indir. Forge 1.20.1'de kullanmak için Oculus gibi uyumlu bir mod gerekir.", shaderPackAssets), cell);
        cell.gridx = 1;
        cell.fill = GridBagConstraints.NONE;
        cell.anchor = GridBagConstraints.NORTHEAST;
        cell.insets = new Insets(22, 8, 14, 24);
        cards.add(buildGameCard(), cell);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);
        sections.add(cards);
        sections.add(Box.createVerticalStrut(8));

        JScrollPane body = new JScrollPane(sections, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        body.setOpaque(false);
        body.getViewport().setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder());
        body.getVerticalScrollBar().setUnitIncrement(22);
        body.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        page.add(body, BorderLayout.CENTER);
        page.add(buildFooter(), BorderLayout.SOUTH);

        frame.setContentPane(page);
        frame.setVisible(true);
        if (!PREVIEW_MODE) {
            optionalModAssets.refreshInventory();
            resourcePackAssets.refreshInventory();
            shaderPackAssets.refreshInventory();
        }
    }

    private JPanel buildAuthCard() {
        authCard = cardPanel();
        authCard.setLayout(new BoxLayout(authCard, BoxLayout.Y_AXIS));
        authCard.setPreferredSize(new Dimension(410, 338));
        authCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));
        signInButton.addActionListener(e -> authenticateWithEmail(false));
        signUpButton.addActionListener(e -> authenticateWithEmail(true));
        discordButton.addActionListener(e -> authenticateWithDiscord());
        signOutButton.addActionListener(e -> {
            authenticated = false;
            signedInEmail = "";
            currentAccessToken = "";
            currentGameUsername = "";
            try { Files.deleteIfExists(authFilePath()); }
            catch (IOException ignored) { }
            launchButton.setEnabled(false);
            footerStatus.setText("Önce Akachi hesabınla giriş yap.");
            refreshAuthCard();
        });
        refreshAuthCard();
        return authCard;
    }

    private void refreshAuthCard() {
        if (authCard == null) {
            authStatus.setText(authConfigured() ? "Giriş yap veya hesap oluştur." : "Supabase bağlantısı uygulamaya ekleniyor.");
            return;
        }
        authCard.removeAll();
        JLabel title = new JLabel("AKACHI HESABI");
        styleLabel(title, GREEN, 11, true);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        authCard.add(title);
        authCard.add(Box.createVerticalStrut(6));

        if (authenticated) {
            JLabel account = new JLabel("Giriş yapıldı: " + currentGameUsername);
            styleLabel(account, TEXT, 13, true);
            account.setAlignmentX(Component.LEFT_ALIGNMENT);
            authCard.add(account);
            authCard.add(Box.createVerticalStrut(10));
            authStatus.setText(signedInEmail + " · Minecraft adı hesaba bağlandı");
            styleLabel(authStatus, GREEN, 11, false);
            authStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
            authCard.add(authStatus);
            authCard.add(Box.createVerticalStrut(8));
            signOutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            authCard.add(signOutButton);
        } else {
            JLabel caption = new JLabel("Kaydolurken e-posta, şifre ve Minecraft oyun adını seç.");
            styleLabel(caption, TEXT, 12, false);
            caption.setAlignmentX(Component.LEFT_ALIGNMENT);
            authCard.add(caption);
            authCard.add(Box.createVerticalStrut(7));

            authUsername.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            authUsername.setToolTipText("Minecraft oyun adı · 3–16 harf, rakam veya _");
            styleInput(authUsername);
            authUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JLabel gameNameLabel = new JLabel("Minecraft kullanıcı adı · yalnızca kayıtta");
            styleLabel(gameNameLabel, MUTED, 10, false);
            gameNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            authCard.add(gameNameLabel);
            authCard.add(Box.createVerticalStrut(3));
            authCard.add(authUsername);
            authCard.add(Box.createVerticalStrut(6));
            authEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            authEmail.setToolTipText("E-posta adresi");
            styleInput(authEmail);
            authEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JLabel emailLabel = new JLabel("E-posta");
            styleLabel(emailLabel, MUTED, 10, false);
            emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            authCard.add(emailLabel);
            authCard.add(Box.createVerticalStrut(3));
            authCard.add(authEmail);
            authCard.add(Box.createVerticalStrut(6));
            authPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            authPassword.setToolTipText("Şifre");
            styleInput(authPassword);
            authPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JLabel passwordLabel = new JLabel("Şifre");
            styleLabel(passwordLabel, MUTED, 10, false);
            passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            authCard.add(passwordLabel);
            authCard.add(Box.createVerticalStrut(3));
            authCard.add(authPassword);
            authCard.add(Box.createVerticalStrut(7));

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
            actions.setOpaque(false);
            actions.add(signInButton);
            actions.add(signUpButton);
            authCard.add(actions);
            authCard.add(Box.createVerticalStrut(6));
            discordButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            discordButton.setText("Discord ile giriş yap");
            authCard.add(discordButton);

            authStatus.setText(authConfigured()
                    ? "Yeni hesaplarda e-posta doğrulaması istenebilir."
                    : "Supabase bağlantısı yapılandırılmadı.");
            styleLabel(authStatus, authConfigured() ? MUTED : RED, 10, false);
            authStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
            authCard.add(Box.createVerticalStrut(5));
            authCard.add(authStatus);
            boolean enabled = authConfigured();
            authUsername.setEnabled(enabled);
            authEmail.setEnabled(enabled);
            authPassword.setEnabled(enabled);
            signInButton.setEnabled(enabled);
            signUpButton.setEnabled(enabled);
            discordButton.setEnabled(enabled);
        }
        authCard.revalidate();
        authCard.repaint();
    }

    private void authenticateWithEmail(boolean signUp) {
        if (!authConfigured()) {
            setAuthMessage("Supabase bağlantısı henüz uygulamaya eklenmedi.", RED);
            return;
        }
        String email = authEmail.getText().trim();
        String password = new String(authPassword.getPassword());
        String username = authUsername.getText().trim();
        if (email.isBlank() || password.isBlank()) {
            setAuthMessage("E-posta ve şifre alanlarını doldur.", RED);
            return;
        }
        if (signUp && !validGameUsername(username)) {
            setAuthMessage("Minecraft oyun adı 3–16 karakter olmalı; yalnızca harf, rakam ve _ kullan.", RED);
            return;
        }
        setAuthBusy(true, signUp ? "Hesap oluşturuluyor…" : "Giriş yapılıyor…");
        new SwingWorker<AuthResult, Void>() {
            @Override protected AuthResult doInBackground() throws Exception {
                String endpoint = signUp ? "/auth/v1/signup" : "/auth/v1/token?grant_type=password";
                String payload = "{\"email\":" + jsonQuote(email) + ",\"password\":" + jsonQuote(password)
                        + (signUp ? ",\"data\":{\"minecraft_username\":" + jsonQuote(username) + "}" : "") + "}";
                return sendAuthRequest(endpoint, payload);
            }

            @Override protected void done() {
                try {
                    AuthResult result = get();
                    if (!result.accessToken().isBlank()) {
                        authPassword.setText("");
                        continueAuthentication(result, signUp ? username : "");
                    } else {
                        setAuthBusy(false, signUp
                                ? "Kayıt oluşturuldu; e-postanı doğrula, sonra giriş yap."
                                : "Oturum açılamadı. E-posta doğrulaması gerekiyorsa gelen kutunu kontrol et.");
                    }
                } catch (Exception ex) {
                    setAuthBusy(false, rootMessage(ex));
                }
            }
        }.execute();
    }

    private void authenticateWithDiscord() {
        if (!authConfigured()) {
            setAuthMessage("Supabase bağlantısı henüz uygulamaya eklenmedi.", RED);
            return;
        }
        setAuthBusy(true, "Discord güvenli giriş sayfası açılıyor…");
        new SwingWorker<AuthResult, Void>() {
            @Override protected AuthResult doInBackground() throws Exception {
                return beginDiscordOAuth();
            }

            @Override protected void done() {
                try {
                    AuthResult result = get();
                    continueAuthentication(result, "");
                } catch (Exception ex) {
                    setAuthBusy(false, rootMessage(ex));
                }
            }
        }.execute();
    }

    private void continueAuthentication(AuthResult result, String suggestedUsername) {
        if (result.accessToken().isBlank() || result.userId().isBlank()) {
            setAuthBusy(false, "Oturum bilgisi alınamadı. Tekrar giriş yap.");
            return;
        }
        setAuthBusy(true, "Minecraft kullanıcı adın kontrol ediliyor…");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return fetchMinecraftUsername(result);
            }

            @Override protected void done() {
                try {
                    String existing = get();
                    if (!existing.isBlank()) {
                        finishAuthentication(result, existing);
                        return;
                    }
                    promptForMinecraftUsername(result, suggestedUsername);
                } catch (Exception ex) {
                    setAuthBusy(false, "Hesap profili okunamadı: " + rootMessage(ex));
                }
            }
        }.execute();
    }

    private void promptForMinecraftUsername(AuthResult result, String suggestedUsername) {
        String username = JOptionPane.showInputDialog(frame,
                "Bu Akachi hesabına bağlanacak Minecraft oyun adını seç.\n3–16 harf, rakam veya _ kullan.",
                suggestedUsername.isBlank() ? "Minecraft oyun adı" : suggestedUsername);
        if (username == null) {
            setAuthBusy(false, "Sunucuya girmek için Minecraft oyun adını bağlamalısın.");
            return;
        }
        username = username.trim();
        if (!validGameUsername(username)) {
            setAuthBusy(false, "Oyun adı geçersiz. 3–16 harf, rakam veya _ kullan.");
            promptForMinecraftUsername(result, suggestedUsername);
            return;
        }
        String selectedUsername = username;
        setAuthBusy(true, "Minecraft oyun adı Akachi hesabına bağlanıyor…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                createMinecraftProfile(result, selectedUsername);
                return null;
            }

            @Override protected void done() {
                try {
                    get();
                    finishAuthentication(result, selectedUsername);
                } catch (Exception ex) {
                    setAuthBusy(false, "Bu oyun adı alınamadı veya hesaba bağlanamadı: " + rootMessage(ex));
                }
            }
        }.execute();
    }

    private String fetchMinecraftUsername(AuthResult result) throws Exception {
        String endpoint = "/rest/v1/akachi_profiles?select=minecraft_username&user_id=eq." + result.userId();
        String body = sendSupabaseRest("GET", endpoint, result.accessToken(), "");
        return jsonText(body, "minecraft_username");
    }

    private void createMinecraftProfile(AuthResult result, String username) throws Exception {
        String body = "{\"user_id\":" + jsonQuote(result.userId()) + ",\"minecraft_username\":" + jsonQuote(username) + "}";
        sendSupabaseRest("POST", "/rest/v1/akachi_profiles", result.accessToken(), body);
    }

    private String sendSupabaseRest(String method, String endpoint, String accessToken, String payload) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(normalizedSupabaseUrl() + endpoint))
                .timeout(Duration.ofSeconds(25))
                .header("apikey", SUPABASE_PUBLISHABLE_KEY)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
        if ("GET".equals(method)) builder.GET();
        else builder.header("Prefer", "return=minimal").POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8));
        HttpResponse<String> response = httpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String detail = jsonText(response.body(), "message");
            if (detail.isBlank()) detail = jsonText(response.body(), "details");
            if (detail.isBlank()) detail = jsonText(response.body(), "hint");
            throw new IOException(detail.isBlank() ? "Akachi profili isteği başarısız (HTTP " + response.statusCode() + ")." : detail);
        }
        return response.body();
    }

    private void finishAuthentication(AuthResult result, String username) {
        try {
            Files.createDirectories(MINECRAFT_DIRECTORY);
            String authJson = "{\"access_token\":" + jsonQuote(result.accessToken())
                    + ",\"minecraft_username\":" + jsonQuote(username) + "}";
            Files.writeString(authFilePath(), authJson, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            authenticated = true;
            signedInEmail = result.email().isBlank() ? "Akachi hesabı" : result.email();
            currentAccessToken = result.accessToken();
            currentGameUsername = username;
            launchButton.setEnabled(true);
            footerStatus.setText("Giriş yapıldı · Minecraft adı: " + username);
            refreshAuthCard();
        } catch (IOException ex) {
            setAuthBusy(false, "Oyun hesabı kaydedilemedi: " + ex.getMessage());
        }
    }

    private static boolean validGameUsername(String username) {
        return username != null && username.matches("[A-Za-z0-9_]{3,16}");
    }

    private static Path authFilePath() {
        return MINECRAFT_DIRECTORY.resolve("akachi-auth.json");
    }

    private AuthResult beginDiscordOAuth() throws Exception {
        String verifier = randomUrlToken(48);
        String challenge = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));
        CompletableFuture<Map<String, String>> callback = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), OAUTH_CALLBACK_PORT), 0);
        server.createContext(OAUTH_CALLBACK_PATH, exchange -> {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            String message = query.containsKey("error")
                    ? "Discord girişi tamamlanmadı. Bu sekmeyi kapatabilirsin."
                    : "Discord girişi tamamlandı. Bu sekmeyi kapatıp Akachi Launcher'a dön.";
            byte[] page = ("<!doctype html><meta charset='utf-8'><title>Akachi Launcher</title>"
                    + "<body style='font:16px Segoe UI;background:#0f1319;color:#eed0d3;padding:48px'>" + message + "</body>")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, page.length);
            try (var response = exchange.getResponseBody()) { response.write(page); }
            callback.complete(query);
        });
        server.start();
        try {
            String redirect = URLEncoder.encode(OAUTH_CALLBACK_URL, StandardCharsets.UTF_8);
            String authorize = normalizedSupabaseUrl() + "/auth/v1/authorize?provider=discord&redirect_to=" + redirect
                    + "&code_challenge=" + URLEncoder.encode(challenge, StandardCharsets.UTF_8)
                    + "&code_challenge_method=s256";
            if (!Desktop.isDesktopSupported()) throw new IOException("Bu bilgisayarda varsayılan tarayıcı açılamadı.");
            Desktop.getDesktop().browse(URI.create(authorize));
            Map<String, String> result = callback.get(3, TimeUnit.MINUTES);
            if (result.containsKey("error")) {
                throw new IOException(result.getOrDefault("error_description", result.get("error")));
            }
            String code = result.get("code");
            if (code == null || code.isBlank()) throw new IOException("Discord dönüşünde giriş kodu bulunamadı.");
            return sendAuthRequest("/auth/v1/token?grant_type=pkce",
                    "{\"auth_code\":" + jsonQuote(code) + ",\"code_verifier\":" + jsonQuote(verifier) + "}");
        } finally {
            server.stop(0);
        }
    }

    private AuthResult sendAuthRequest(String endpoint, String payload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(normalizedSupabaseUrl() + endpoint))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("apikey", SUPABASE_PUBLISHABLE_KEY)
                .header("User-Agent", "AkachiLauncher/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8)).build();
        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String body = response.body();
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String detail = jsonText(body, "msg");
            if (detail.isBlank()) detail = jsonText(body, "message");
            if (detail.isBlank()) detail = jsonText(body, "error_description");
            if (detail.isBlank()) detail = jsonText(body, "error");
            throw new IOException(detail.isBlank() ? "Supabase isteği başarısız (HTTP " + response.statusCode() + ")." : detail);
        }
        return new AuthResult(jsonText(body, "access_token"), jsonText(body, "refresh_token"),
                jsonText(body, "email"), jsonText(body, "id"));
    }

    private void setAuthBusy(boolean busy, String message) {
        signInButton.setEnabled(!busy && authConfigured());
        signUpButton.setEnabled(!busy && authConfigured());
        discordButton.setEnabled(!busy && authConfigured());
        authStatus.setText(message);
        authStatus.setForeground(busy ? BLUE : RED);
        launchButton.setEnabled(authenticated);
    }

    private void setAuthMessage(String message, Color color) {
        authStatus.setText(message);
        authStatus.setForeground(color);
    }

    private boolean authConfigured() {
        return SUPABASE_URL.startsWith("https://") && !SUPABASE_PUBLISHABLE_KEY.isBlank();
    }

    private String normalizedSupabaseUrl() {
        String value = SUPABASE_URL.trim();
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return value;
    }

    private static String randomUrlToken(int bytes) {
        byte[] value = new byte[bytes];
        new SecureRandom().nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> values = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return values;
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            values.put(key, value);
        }
        return values;
    }

    private static String jsonQuote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n").replace("\t", "\\t") + "\"";
    }

    private static void styleInput(JTextField field) {
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBackground(CARD_ALT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(58, 69, 84)), new EmptyBorder(6, 9, 6, 9)));
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setColor(new Color(20, 23, 31, 235));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g.setColor(new Color(255, 255, 255, 18));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(7, 14, 7, 8));
        header.setPreferredSize(new Dimension(0, 72));
        JPanel identity = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0));
        identity.setOpaque(false);
        Image brandImage = loadBrandImage(42);
        if (brandImage != null) identity.add(new JLabel(new ImageIcon(brandImage)));
        JLabel brand = new JLabel("Akachi Launcher");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(RED);
        JLabel version = new JLabel("LAUNCHER  ·  BETA");
        version.setFont(new Font("Segoe UI", Font.BOLD, 11));
        version.setForeground(MUTED);
        identity.add(brand);
        header.add(identity, BorderLayout.WEST);
        JPanel serverInfo = buildServerHeaderInfo();
        header.add(serverInfo, BorderLayout.CENTER);
        JPanel windowControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        windowControls.setOpaque(false);
        version.setBorder(new EmptyBorder(0, 0, 0, 12));
        JButton settings = windowButton("⚙", false);
        settings.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 17));
        settings.setToolTipText("Ayarlar");
        settings.addActionListener(e -> showSettings());
        windowControls.add(settings);
        windowControls.add(version);
        JButton minimize = windowButton("—", false);
        minimize.setToolTipText("Küçült");
        minimize.addActionListener(e -> frame.setState(JFrame.ICONIFIED));
        JButton maximize = windowButton("□", false);
        maximize.setToolTipText("Büyüt / geri al");
        maximize.addActionListener(e -> toggleMaximized());
        JButton close = windowButton("×", true);
        close.setToolTipText("Kapat");
        close.addActionListener(e -> System.exit(0));
        windowControls.add(minimize);
        windowControls.add(maximize);
        windowControls.add(close);
        header.add(windowControls, BorderLayout.EAST);
        installWindowDragging(header, identity, brand, version, serverInfo);
        return header;
    }

    private JPanel buildServerHeaderInfo() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 22, 0, 12));

        JPanel details = new JPanel();
        details.setOpaque(false);
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));

        JPanel statusLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        statusLine.setOpaque(false);
        JLabel title = new JLabel("AKACHI SUNUCUSU");
        styleLabel(title, GREEN, 10, true);
        serverStatus.setHorizontalAlignment(SwingConstants.LEFT);
        styleLabel(serverStatus, GREEN, 11, true);
        styleLabel(playersStatus, MUTED, 11, false);
        statusLine.add(title);
        statusLine.add(serverStatus);
        statusLine.add(playersStatus);
        if (PREVIEW_MODE) {
            serverStatus.setText("Önizleme");
            playersStatus.setText("canlı durum yüklenmedi");
        }

        styleLabel(serverAddress, TEXT, 12, true);
        statusLine.setAlignmentX(Component.LEFT_ALIGNMENT);
        serverAddress.setAlignmentX(Component.LEFT_ALIGNMENT);
        details.add(statusLine);
        details.add(Box.createVerticalStrut(4));
        details.add(serverAddress);
        panel.add(details, BorderLayout.CENTER);

        pingButton.setText("Durumu yenile");
        pingButton.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        pingButton.setToolTipText("Sunucu durumunu yenile");
        pingButton.setPreferredSize(new Dimension(98, 28));
        pingButton.addActionListener(e -> checkServer());
        statusLine.add(pingButton);
        installWindowDragging(details, statusLine, title, serverStatus, playersStatus, serverAddress);
        return panel;
    }

    private JButton windowButton(String label, boolean isClose) {
        JButton button = new JButton(label);
        button.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 17));
        button.setForeground(isClose ? new Color(235, 205, 208) : MUTED);
        button.setBackground(new Color(30, 34, 43));
        button.setPreferredSize(new Dimension(36, 32));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                button.setOpaque(true);
                button.setBackground(isClose ? new Color(155, 42, 55) : new Color(55, 40, 47));
                button.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                button.setOpaque(false);
                button.repaint();
            }
        });
        return button;
    }

    private void toggleMaximized() {
        if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            frame.setExtendedState(JFrame.NORMAL);
            if (restoreBounds != null) frame.setBounds(restoreBounds);
            else {
                frame.setSize(1500, 1000);
                frame.setLocationRelativeTo(null);
            }
        } else {
            restoreBounds = frame.getBounds();
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }

    private void installWindowDragging(Component... components) {
        for (Component component : components) {
            Point[] press = {null};
            component.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        toggleMaximized();
                        press[0] = null;
                    } else if (frame.getExtendedState() == JFrame.NORMAL) {
                        press[0] = e.getLocationOnScreen();
                    }
                }
            });
            component.addMouseMotionListener(new MouseAdapter() {
                @Override public void mouseDragged(MouseEvent e) {
                    if (press[0] == null || frame.getExtendedState() != JFrame.NORMAL) return;
                    Point now = e.getLocationOnScreen();
                    frame.setLocation(frame.getX() + now.x - press[0].x, frame.getY() + now.y - press[0].y);
                    press[0] = now;
                }
            });
        }
    }

    private void setAppIcon() {
        Image icon = loadBrandImage(64);
        if (icon != null) frame.setIconImage(icon);
    }

    private Image loadBrandImage(int size) {
        try (InputStream input = AkachiLauncher.class.getResourceAsStream("/akachi/launcher/akachi.jpg")) {
            if (input == null) return null;
            BufferedImage image = ImageIO.read(input);
            return image == null ? null : image.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        } catch (IOException ignored) {
            return null;
        }
    }

    private JPanel buildWelcome() {
        JPanel welcome = new JPanel();
        welcome.setOpaque(false);
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Akachi'ye hoş geldin.");
        title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Sunucu hazır. Modlarını güncelle ve oyuna katıl.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);
        welcome.add(title);
        welcome.add(Box.createVerticalStrut(5));
        welcome.add(subtitle);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        return welcome;
    }

    private JPanel buildServerCard() {
        JPanel card = cardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints c = constraints();
        addLabel(card, c, "AKACHI SUNUCUSU", 0, 0);

        JPanel statusBlock = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        statusBlock.setOpaque(false);
        serverStatus.setForeground(MUTED);
        serverStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        playersStatus.setForeground(MUTED);
        playersStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusBlock.add(serverStatus);
        statusBlock.add(playersStatus);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        card.add(statusBlock, c);

        JLabel addressLabel = new JLabel("Sunucu adresi");
        styleLabel(addressLabel, MUTED, 12, false);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.anchor = WEST;
        c.insets = new Insets(17, 0, 0, 10);
        card.add(addressLabel, c);

        styleLabel(serverAddress, TEXT, 14, true);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.insets = new Insets(17, 0, 0, 0);
        card.add(serverAddress, c);

        pingButton.setText("Durumu yenile");
        pingButton.setEnabled(!SERVER_HOST.isBlank());
        pingButton.addActionListener(e -> checkServer());
        c.gridx = 1;
        c.gridy = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(12, 0, 0, 0);
        card.add(pingButton, c);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Keep this card within its GridBag column. A large preferred width
        // made the entire page wider than the window and clipped the right edge.
        card.setPreferredSize(new Dimension(410, 132));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 132));
        return card;
    }

    private JPanel buildOptionalModsCard() {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("MOD MANAGER · İSTEĞE BAĞLI EK MODLAR");
        styleLabel(title, GREEN, 11, true);
        JLabel description = new JLabel("Listeden seçtiklerini kurabilir veya kaldırabilirsin.");
        styleLabel(description, TEXT, 14, false);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(description);
        card.add(Box.createVerticalStrut(8));
        card.add(optionalModAssets);
        card.add(Box.createVerticalStrut(14));

        JLabel requiredTitle = new JLabel("AKACHI SUNUCU MODLARI");
        styleLabel(requiredTitle, GREEN, 11, true);
        JLabel requiredDescription = new JLabel("Sunucuya girmek için gereken mod paketini güncelle.");
        styleLabel(requiredDescription, TEXT, 13, false);
        modStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modStatus.setForeground(MUTED);
        card.add(requiredTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(requiredDescription);
        card.add(Box.createVerticalStrut(7));
        card.add(modStatus);
        card.add(Box.createVerticalStrut(10));
        syncButton.setText("Sunucu modlarını güncelle");
        syncButton.addActionListener(e -> syncMods());
        syncButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(syncButton);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JPanel buildAssetSection(String titleText, String descriptionText, AssetPanel assets) {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(titleText);
        styleLabel(title, GREEN, 11, true);
        JLabel description = new JLabel("<html><body style='width:360px'>" + descriptionText + "</body></html>");
        styleLabel(description, TEXT, 13, false);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(description);
        card.add(Box.createVerticalStrut(8));
        card.add(assets);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JPanel buildGameCard() {
        JPanel card = cardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel versionLabel = new JLabel("OYUN SÜRÜMÜ");
        styleLabel(versionLabel, GREEN, 11, true);
        card.add(versionLabel);
        card.add(Box.createVerticalStrut(12));
        JLabel fixedVersion = new JLabel(MINECRAFT_VERSION + " · Forge " + FORGE_VERSION);
        styleLabel(fixedVersion, TEXT, 14, true);
        card.add(fixedVersion);
        card.add(Box.createVerticalStrut(18));

        JLabel directoryLabel = new JLabel("MINECRAFT OYUN KLASÖRÜ");
        styleLabel(directoryLabel, MUTED, 11, true);
        card.add(directoryLabel);
        card.add(Box.createVerticalStrut(8));
        JLabel directoryPath = new JLabel("<html><body style='width:310px'>" + MINECRAFT_DIRECTORY + "</body></html>");
        styleLabel(directoryPath, TEXT, 11, false);
        card.add(directoryPath);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setPreferredSize(new Dimension(370, 156));
        card.setMaximumSize(new Dimension(370, 156));
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footerStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footerStatus.setForeground(MUTED);
        footer.add(footerStatus, BorderLayout.WEST);
        JLabel repository = new JLabel("GitHub  ·  " + REPOSITORY);
        repository.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        repository.setForeground(MUTED);
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightControls.setOpaque(false);
        launchButton.setPreferredSize(new Dimension(218, 42));
        launchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        launchButton.setForeground(new Color(238, 190, 195));
        launchButton.setBackground(new Color(83, 31, 39));
        launchButton.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        launchButton.setOpaque(false);
        launchButton.setContentAreaFilled(false);
        launchButton.setBorderPainted(false);
        launchButton.setUI(new BasicButtonUI() {
            @Override public void update(Graphics graphics, javax.swing.JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                JButton button = (JButton) component;
                int inset = 1;
                Color fill = button.getModel().isPressed() ? new Color(105, 38, 48)
                        : button.getModel().isRollover() ? new Color(98, 35, 45) : new Color(75, 29, 37);
                g.setColor(fill);
                g.fillRoundRect(inset, inset, component.getWidth() - inset * 2 - 1,
                        component.getHeight() - inset * 2 - 1, 14, 14);
                g.setColor(new Color(137, 54, 65));
                g.drawRoundRect(inset, inset, component.getWidth() - inset * 2 - 1,
                        component.getHeight() - inset * 2 - 1, 14, 14);
                super.paint(g, component);
                g.dispose();
            }
        });
        launchButton.addActionListener(e -> installAndOpenMinecraft());
        rightControls.add(repository);
        rightControls.add(launchButton);
        footer.add(rightControls, BorderLayout.EAST);
        return footer;
    }

    private void checkServer() {
        String host = SERVER_HOST.trim();
        if (host.isBlank()) {
            serverStatus.setForeground(MUTED);
            serverStatus.setText("Adres ekleniyor");
            playersStatus.setText("");
            return;
        }
        int port = SERVER_PORT;
        serverStatus.setForeground(BLUE);
        serverStatus.setText("Kontrol ediliyor…");
        playersStatus.setText("");
        pingButton.setEnabled(false);

        new SwingWorker<ServerInfo, Void>() {
            @Override protected ServerInfo doInBackground() throws Exception {
                return pingMinecraftServer(host, port);
            }

            @Override protected void done() {
                pingButton.setEnabled(true);
                try {
                    ServerInfo info = get();
                    serverStatus.setForeground(GREEN);
                    serverStatus.setText("Çevrimiçi");
                    playersStatus.setText(info.online + "/" + info.maxPlayers + " oyuncu · " + info.latencyMs + " ms");
                    footerStatus.setText(info.motd.isBlank()
                            ? "Minecraft " + MINECRAFT_VERSION + " · Forge " + FORGE_VERSION : info.motd);
                } catch (Exception ex) {
                    serverStatus.setForeground(RED);
                    serverStatus.setText("Çevrimdışı");
                    playersStatus.setText("");
                    footerStatus.setText("Sunucu çevrimdışı veya bu bilgisayardan ulaşılamıyor");
                }
            }
        }.execute();
    }

    private void syncMods() {
        Path destination = MINECRAFT_DIRECTORY.resolve("mods");
        syncButton.setEnabled(false);
        modStatus.setForeground(BLUE);
        modStatus.setText("GitHub deposu kontrol ediliyor…");
        new SwingWorker<Integer, String>() {
            @Override protected Integer doInBackground() throws Exception {
                return downloadRepositoryMods(destination, this::publish);
            }

            @Override protected void process(List<String> messages) {
                if (!messages.isEmpty()) modStatus.setText(messages.get(messages.size() - 1));
            }

            @Override protected void done() {
                syncButton.setEnabled(true);
                try {
                    int count = get();
                    modStatus.setForeground(GREEN);
                    modStatus.setText(count + " mod dosyası güncel");
                } catch (Exception ex) {
                    modStatus.setForeground(RED);
                    modStatus.setText("İndirme başarısız. Değişiklikleri GitHub'a Push ettiğini kontrol et.");
                    showMessage("Modlar indirilemedi. Repo herkese açık mı ve mods/ klasörü GitHub'a gönderildi mi?\n\n"
                            + rootMessage(ex));
                }
            }
        }.execute();
    }

    private int downloadRepositoryMods(Path destination, java.util.function.Consumer<String> progress) throws Exception {
        URI api = URI.create("https://api.github.com/repos/" + REPOSITORY + "/contents/mods?ref=" + BRANCH);
        HttpRequest request = HttpRequest.newBuilder(api)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "AkachiLauncher/0.1")
                .GET().build();
        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("GitHub API HTTP " + response.statusCode());
        }

        List<RemoteFile> files = parseRepositoryFiles(response.body());
        if (files.isEmpty()) throw new IOException("GitHub'da indirilebilir .jar modu bulunamadı.");
        Files.createDirectories(destination);
        int index = 0;
        for (RemoteFile file : files) {
            index++;
            Path target = destination.resolve(file.name).normalize();
            if (!target.getParent().equals(destination.normalize())) continue;
            if (Files.isRegularFile(target) && gitBlobSha(target).equalsIgnoreCase(file.sha)) {
                progress.accept(index + "/" + files.size() + " · güncel: " + file.name);
                continue;
            }

            progress.accept(index + "/" + files.size() + " · indiriliyor: " + file.name);
            Path temporary = destination.resolve(file.name + ".part");
            HttpRequest download = HttpRequest.newBuilder(URI.create(file.downloadUrl))
                    .timeout(Duration.ofMinutes(3))
                    .header("User-Agent", "AkachiLauncher/0.1")
                    .GET().build();
            HttpResponse<InputStream> downloadResponse = httpClient().send(download, HttpResponse.BodyHandlers.ofInputStream());
            if (downloadResponse.statusCode() != 200) {
                downloadResponse.body().close();
                throw new IOException("Mod indirilemedi: " + file.name + " (HTTP " + downloadResponse.statusCode() + ")");
            }
            try (InputStream in = downloadResponse.body()) {
                Files.copy(in, temporary, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!gitBlobSha(temporary).equalsIgnoreCase(file.sha)) {
                Files.deleteIfExists(temporary);
                throw new IOException("İndirilen dosyanın bütünlük kontrolü başarısız: " + file.name);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return files.size();
    }

    private List<RemoteAsset> fetchRemoteAssets(String folder, String extension) throws Exception {
        URI api = URI.create("https://api.github.com/repos/" + REPOSITORY + "/contents/" + folder + "?ref=" + BRANCH);
        HttpRequest request = HttpRequest.newBuilder(api)
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "AkachiLauncher/0.1")
                .GET().build();
        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() == 404) return List.of();
        if (response.statusCode() != 200) throw new IOException("GitHub API HTTP " + response.statusCode());
        List<RemoteAsset> assets = new ArrayList<>();
        for (String object : topLevelObjects(response.body())) {
            Matcher matcher = Pattern.compile("\"([A-Za-z_]+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(object);
            Map<String, String> values = new java.util.HashMap<>();
            while (matcher.find()) values.put(matcher.group(1), unescapeJson(matcher.group(2)));
            String name = values.get("name");
            if (!"file".equals(values.get("type")) || name == null || !name.toLowerCase(Locale.ROOT).endsWith("." + extension)) continue;
            String downloadUrl = values.get("download_url");
            String sha = values.get("sha");
            if (downloadUrl != null && sha != null) assets.add(new RemoteAsset(name, downloadUrl, sha));
        }
        assets.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        return assets;
    }

    private int installAssets(List<RemoteAsset> assets, Path destination, java.util.function.Consumer<String> progress) throws Exception {
        Files.createDirectories(destination);
        int count = 0;
        for (RemoteAsset asset : assets) {
            Path target = destination.resolve(asset.name).normalize();
            if (!target.getParent().equals(destination.normalize())) throw new IOException("Geçersiz dosya adı: " + asset.name);
            if (Files.isRegularFile(target) && gitBlobSha(target).equalsIgnoreCase(asset.sha)) {
                progress.accept("Güncel: " + asset.name);
                count++;
                continue;
            }
            progress.accept("İndiriliyor: " + asset.name);
            Path temporary = destination.resolve(asset.name + ".part");
            HttpRequest request = HttpRequest.newBuilder(URI.create(asset.downloadUrl))
                    .timeout(Duration.ofMinutes(5))
                    .header("User-Agent", "AkachiLauncher/0.1")
                    .GET().build();
            HttpResponse<InputStream> response = httpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                response.body().close();
                throw new IOException("Dosya indirilemedi: " + asset.name + " (HTTP " + response.statusCode() + ")");
            }
            try (InputStream in = response.body()) {
                Files.copy(in, temporary, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!gitBlobSha(temporary).equalsIgnoreCase(asset.sha)) {
                Files.deleteIfExists(temporary);
                throw new IOException("İndirilen dosyanın doğrulaması başarısız: " + asset.name);
            }
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            count++;
        }
        return count;
    }

    private static List<RemoteFile> parseRepositoryFiles(String json) {
        List<String> objects = topLevelObjects(json);
        List<RemoteFile> files = new ArrayList<>();
        Set<String> seenBlobs = new HashSet<>();
        Pattern field = Pattern.compile("\"([A-Za-z_]+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        for (String object : objects) {
            Matcher matcher = field.matcher(object);
            Map<String, String> values = new java.util.HashMap<>();
            while (matcher.find()) values.put(matcher.group(1), unescapeJson(matcher.group(2)));
            String name = values.get("name");
            String type = values.get("type");
            String downloadUrl = values.get("download_url");
            String sha = values.get("sha");
            if (name == null || !"file".equals(type) || downloadUrl == null || sha == null) continue;
            String lower = name.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".jar") && seenBlobs.add(sha)) files.add(new RemoteFile(name, downloadUrl, sha));
        }
        return files;
    }

    private static List<String> topLevelObjects(String json) {
        List<String> result = new ArrayList<>();
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        int start = -1;
        for (int i = 0; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (ch == '\\') escaped = true;
                else if (ch == '"') inString = false;
                continue;
            }
            if (ch == '"') inString = true;
            else if (ch == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (ch == '}' && depth > 0) {
                depth--;
                if (depth == 0 && start >= 0) result.add(json.substring(start, i + 1));
            }
        }
        return result;
    }

    private static String unescapeJson(String value) {
        return value.replace("\\/", "/").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private void installAndOpenMinecraft() {
        if (!authenticated) {
            footerStatus.setText("Oyunu açmak için önce Akachi hesabınla giriş yap.");
            return;
        }
        launchButton.setEnabled(false);
        footerStatus.setText("Forge " + MINECRAFT_VERSION + "-" + FORGE_VERSION + " kontrol ediliyor…");
        Path gameDirectory = MINECRAFT_DIRECTORY;

        new SwingWorker<Boolean, String>() {
            @Override protected Boolean doInBackground() throws Exception {
                return ensureForgeInstalled(gameDirectory, this::publish);
            }

            @Override protected void process(List<String> messages) {
                if (!messages.isEmpty()) footerStatus.setText(messages.get(messages.size() - 1));
            }

            @Override protected void done() {
                launchButton.setEnabled(authenticated);
                try {
                    get();
                    footerStatus.setText("Minecraft " + MINECRAFT_VERSION + " · Forge " + FORGE_VERSION + " hazır");
                try {
                    applyRamSettingToForgeProfile();
                } catch (IOException profileError) {
                    footerStatus.setText("RAM ayarı uygulanamadı; Minecraft Launcher ayarlarını kontrol et");
                }
                    openMinecraftLauncher();
                } catch (Exception ex) {
                    footerStatus.setText("Forge kurulumu başarısız oldu");
                    showMessage("Forge " + MINECRAFT_VERSION + "-" + FORGE_VERSION + " kurulamadı.\n\n"
                            + rootMessage(ex));
                }
            }
        }.execute();
    }

    private boolean ensureForgeInstalled(Path gameDirectory, java.util.function.Consumer<String> progress) throws Exception {
        Path profileJson = gameDirectory.resolve("versions").resolve(FORGE_PROFILE).resolve(FORGE_PROFILE + ".json");
        if (Files.isRegularFile(profileJson)) {
            progress.accept("Forge " + MINECRAFT_VERSION + "-" + FORGE_VERSION + " zaten kurulu.");
            return true;
        }

        Files.createDirectories(gameDirectory);
        Path installer = MINECRAFT_DIRECTORY.resolve("cache")
                .resolve("forge-" + MINECRAFT_VERSION + "-" + FORGE_VERSION + "-installer.jar");
        Files.createDirectories(installer.getParent());
        if (!Files.isRegularFile(installer) || !fileSha1(installer).equalsIgnoreCase(FORGE_INSTALLER_SHA1)) {
            progress.accept("Resmi Forge yükleyicisi indiriliyor…");
            HttpRequest request = HttpRequest.newBuilder(URI.create(FORGE_INSTALLER_URL))
                    .timeout(Duration.ofMinutes(3))
                    .header("User-Agent", "AkachiLauncher/0.1")
                    .GET().build();
            HttpResponse<InputStream> response = httpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                response.body().close();
                throw new IOException("Forge yükleyicisi indirilemedi (HTTP " + response.statusCode() + ").");
            }
            try (InputStream in = response.body()) {
                Files.copy(in, installer, StandardCopyOption.REPLACE_EXISTING);
            }
            if (!fileSha1(installer).equalsIgnoreCase(FORGE_INSTALLER_SHA1)) {
                Files.deleteIfExists(installer);
                throw new IOException("Forge yükleyicisinin SHA-1 doğrulaması başarısız.");
            }
        }

        progress.accept("Forge kuruluyor; gerekli Minecraft dosyaları indiriliyor…");
        ensureForgeLauncherProfile(gameDirectory);
        Process process = new ProcessBuilder(javaExecutable(), "-jar", installer.toString(),
                "--installClient", gameDirectory.toString())
                .directory(gameDirectory.toFile())
                .redirectErrorStream(true)
                .start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) progress.accept(trimmed.length() > 90 ? trimmed.substring(0, 90) + "…" : trimmed);
            }
        }
        int exitCode = process.waitFor();
        if (exitCode != 0 || !Files.isRegularFile(profileJson)) {
            throw new IOException("Forge Installer kurulumu tamamlayamadı (çıkış kodu " + exitCode + ").");
        }
        return true;
    }

    private static void ensureForgeLauncherProfile(Path gameDirectory) throws IOException {
        Path standardProfile = gameDirectory.resolve("launcher_profiles.json");
        Path storeProfile = gameDirectory.resolve("launcher_profiles_microsoft_store.json");
        if (Files.isRegularFile(standardProfile) || Files.isRegularFile(storeProfile)) return;

        // Forge's official client installer requires one of these files before it
        // will download the vanilla client and install its Forge profile. A fresh
        // Akachi game directory has neither, so seed a minimal launcher profile.
        String initialProfile = """
                {
                  "profiles": {},
                  "settings": {
                    "crashAssistance": false,
                    "enableAdvanced": false,
                    "enableAnalytics": false,
                    "keepLauncherOpen": false,
                    "showGameLog": false,
                    "showMenu": false,
                    "soundOn": false
                  },
                  "version": 3
                }
                """;
        Files.writeString(standardProfile, initialProfile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
    }

    private static String javaExecutable() {
        Path java = Path.of(System.getProperty("java.home"), "bin", "java.exe");
        return Files.isRegularFile(java) ? java.toString() : "java";
    }

    private static String fileSha1(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private void openMinecraftLauncher() {
        try {
            Path executable = findMinecraftLauncher();
            if (executable != null) {
                new ProcessBuilder(executable.toString(), "--workDir", MINECRAFT_DIRECTORY.toString()).start();
            } else {
                new ProcessBuilder("MinecraftLauncher.exe", "--workDir", MINECRAFT_DIRECTORY.toString()).start();
            }
            JOptionPane.showMessageDialog(frame,
                    "Minecraft Launcher açıldı. Forge " + MINECRAFT_VERSION + "-" + FORGE_VERSION
                            + " profilini seçip Microsoft hesabınla oyunu başlat.",
                    "Akachi Launcher", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            try {
                if (!Desktop.isDesktopSupported()) throw new IOException("Minecraft Launcher bulunamadı.");
                Desktop.getDesktop().browse(URI.create("minecraft://"));
                showMessage("Forge profili " + MINECRAFT_DIRECTORY + " içine kuruldu. Minecraft Launcher'da oyun klasörü olarak bu yolu seçip Forge "
                        + MINECRAFT_VERSION + "-" + FORGE_VERSION + " profilini başlat.");
            } catch (Exception fallbackError) {
                showMessage("Forge profili kuruldu. Minecraft Launcher'ı açıp oyun klasörü olarak "
                        + MINECRAFT_DIRECTORY + " yolunu ve Forge " + MINECRAFT_VERSION + "-" + FORGE_VERSION
                        + " profilini seç.");
            }
        }
    }

    private static Path findMinecraftLauncher() {
        List<Path> candidates = new ArrayList<>();
        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        String programFiles = System.getenv("ProgramFiles");
        String localAppData = System.getenv("LOCALAPPDATA");
        if (programFilesX86 != null) candidates.add(Path.of(programFilesX86, "Minecraft Launcher", "MinecraftLauncher.exe"));
        if (programFiles != null) candidates.add(Path.of(programFiles, "Minecraft Launcher", "MinecraftLauncher.exe"));
        if (localAppData != null) candidates.add(Path.of(localAppData, "Microsoft", "WindowsApps", "MinecraftLauncher.exe"));
        return candidates.stream().filter(Files::isRegularFile).findFirst().orElse(null);
    }

    private static Path appDataDirectory() {
        String appData = System.getenv("APPDATA");
        Path root = appData == null
                ? Path.of(System.getProperty("user.home"), "AppData", "Roaming")
                : Path.of(appData);
        return root.resolve("Akachi Launcher");
    }

    private static String serverDisplay() {
        return SERVER_HOST.isBlank() ? "Sunucu adresi eklenecek" : SERVER_HOST + ":" + SERVER_PORT;
    }

    private static ServerInfo pingMinecraftServer(String host, int port) throws Exception {
        long started = System.nanoTime();
        try (java.net.Socket socket = new java.net.Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3500);
            socket.setSoTimeout(3500);
            var out = socket.getOutputStream();
            ByteArrayOutputStream handshake = new ByteArrayOutputStream();
            writeVarInt(handshake, 0);
            writeVarInt(handshake, -1);
            writeString(handshake, host);
            handshake.write((port >>> 8) & 0xff);
            handshake.write(port & 0xff);
            writeVarInt(handshake, 1);
            writeVarInt(out, handshake.size());
            handshake.writeTo(out);
            writeVarInt(out, 1);
            writeVarInt(out, 0);
            out.flush();

            InputStream in = socket.getInputStream();
            readVarInt(in); // Status packet length.
            int packetId = readVarInt(in);
            if (packetId != 0) throw new IOException("Beklenmeyen sunucu yanıtı.");
            int jsonLength = readVarInt(in);
            byte[] jsonBytes = in.readNBytes(jsonLength);
            if (jsonBytes.length != jsonLength) throw new IOException("Sunucu yanıtı eksik.");
            String json = new String(jsonBytes, StandardCharsets.UTF_8);
            int online = jsonNumber(json, "online", 0);
            int max = jsonNumber(json, "max", 0);
            String motd = jsonText(json, "description");
            long latency = (System.nanoTime() - started) / 1_000_000;
            return new ServerInfo(online, max, motd, latency);
        }
    }

    private static int jsonNumber(String json, String key, int fallback) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)").matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : fallback;
    }

    private static String jsonText(String json, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"").matcher(json);
        return matcher.find() ? unescapeJson(matcher.group(1)) : "";
    }

    private static void writeString(ByteArrayOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static void writeVarInt(java.io.OutputStream out, int value) throws IOException {
        while ((value & 0xFFFFFF80) != 0) {
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.write(value & 0x7F);
    }

    private static int readVarInt(InputStream in) throws IOException {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            int raw = in.read();
            if (raw == -1) throw new IOException("Sunucu bağlantıyı kapattı.");
            read = (byte) raw;
            result |= (read & 0x7F) << (7 * numRead);
            numRead++;
            if (numRead > 5) throw new IOException("Geçersiz sunucu yanıtı.");
        } while ((read & 0x80) != 0);
        return result;
    }

    private static String gitBlobSha(Path file) throws Exception {
        long size = Files.size(file);
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(("blob " + size + "\0").getBytes(StandardCharsets.UTF_8));
        try (InputStream in = Files.newInputStream(file)) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static JPanel cardPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(new Color(CARD.getRed(), CARD.getGreen(), CARD.getBlue(), 218));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g.setColor(new Color(166, 94, 105, 65));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(19, 20, 19, 20));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private static GridBagConstraints constraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = WEST;
        return c;
    }

    private static void addLabel(JPanel panel, GridBagConstraints c, String value, int x, int y) {
        JLabel label = new JLabel(value);
        styleLabel(label, GREEN, 11, true);
        c.gridx = x;
        c.gridy = y;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = WEST;
        c.insets = new Insets(0, 0, 0, 10);
        panel.add(label, c);
    }

    private static void styleLabel(JLabel label, Color color, int size, boolean bold) {
        label.setForeground(color);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
    }

    private final class AssetPanel extends JPanel {
        private final String repositoryFolder;
        private final String title;
        private final String extension;
        private final Path installDirectory;
        private final JPanel rows = new JPanel();
        private final JLabel status = new JLabel("Depo listesi yükleniyor…");
        private final List<JCheckBox> choices = new ArrayList<>();
        private final JButton refreshButton = actionButton("Yenile", false);
        private final JButton installButton = actionButton("Kur / güncelle", true);
        private final JButton removeButton = actionButton("Kaldır", false);

        private AssetPanel(String repositoryFolder, String title, String extension) {
            this.repositoryFolder = repositoryFolder;
            this.title = title;
            this.extension = extension;
            this.installDirectory = switch (repositoryFolder) {
                case "optional-mods" -> MINECRAFT_DIRECTORY.resolve("mods");
                case "resource-packs" -> MINECRAFT_DIRECTORY.resolve("resourcepacks");
                default -> MINECRAFT_DIRECTORY.resolve("shaderpacks");
            };
            setOpaque(false);
            setLayout(new BorderLayout(0, 6));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(410, 112));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));

            rows.setOpaque(false);
            rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
            JScrollPane listScroll = new JScrollPane(rows, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            listScroll.setPreferredSize(new Dimension(410, 46));
            listScroll.setBorder(BorderFactory.createLineBorder(new Color(58, 69, 84)));
            listScroll.setOpaque(false);
            listScroll.getViewport().setBackground(CARD);
            listScroll.getVerticalScrollBar().setUnitIncrement(20);
            add(listScroll, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new BorderLayout(8, 0));
            bottom.setOpaque(false);
            styleLabel(status, MUTED, 11, false);
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            buttons.setOpaque(false);
            refreshButton.addActionListener(e -> refreshInventory());
            installButton.addActionListener(e -> installSelected());
            removeButton.addActionListener(e -> removeSelected());
            refreshButton.setToolTipText("GitHub deposunu yenile");
            installButton.setToolTipText("Seçilen dosyaları kur veya güncelle");
            removeButton.setToolTipText("Seçilen dosyaları oyundan kaldır");
            buttons.add(refreshButton);
            buttons.add(installButton);
            buttons.add(removeButton);
            bottom.add(status, BorderLayout.CENTER);
            bottom.add(buttons, BorderLayout.EAST);
            add(bottom, BorderLayout.SOUTH);
            showEmptyRow("Depo listesi yükleniyor…");
            if (PREVIEW_MODE) {
                showEmptyRow("Önizleme: Depo dosyaları burada listelenecek.");
                status.setText("Önizleme");
            }
        }

        private void refreshInventory() {
            refreshButton.setEnabled(false);
            setStatus("" + title + " GitHub'dan alınıyor…", BLUE);
            new SwingWorker<List<RemoteAsset>, Void>() {
                @Override protected List<RemoteAsset> doInBackground() throws Exception {
                    return fetchRemoteAssets(repositoryFolder, extension);
                }

                @Override protected void done() {
                    refreshButton.setEnabled(true);
                    try {
                        List<RemoteAsset> assets = get();
                        rows.removeAll();
                        choices.clear();
                        if (assets.isEmpty()) {
                            showEmptyRow("Bu bölüm ." + extension + " dosyalarını okur; repoda " + repositoryFolder + "/ klasörüne ekle.");
                            setStatus("GitHub deposunda ." + extension + " bulunamadı · ekledikten sonra Push origin yap", MUTED);
                        } else {
                            for (RemoteAsset asset : assets) {
                                boolean installed = Files.isRegularFile(installDirectory.resolve(asset.name));
                                JCheckBox check = new JCheckBox(asset.name + (installed ? "   ·   kurulu" : ""));
                                check.putClientProperty("asset", asset);
                                check.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                                check.setForeground(TEXT);
                                check.setBackground(CARD);
                                check.setOpaque(true);
                                check.setFocusPainted(false);
                                check.setBorder(new EmptyBorder(3, 8, 3, 8));
                                check.setAlignmentX(Component.LEFT_ALIGNMENT);
                                choices.add(check);
                                rows.add(check);
                            }
                            setStatus(assets.size() + " dosya listelendi · kurmak veya kaldırmak için seç", MUTED);
                        }
                        rows.revalidate();
                        rows.repaint();
                    } catch (Exception ex) {
                        showEmptyRow("GitHub deposuna bağlanılamadı.");
                        setStatus("Liste alınamadı: " + rootMessage(ex), RED);
                    }
                }
            }.execute();
        }

        private void installSelected() {
            List<RemoteAsset> selected = selectedAssets();
            if (selected.isEmpty()) {
                setStatus("Önce kurulacak dosyaları işaretle.", MUTED);
                return;
            }
            setButtonsEnabled(false);
            new SwingWorker<Integer, String>() {
                @Override protected Integer doInBackground() throws Exception {
                    return installAssets(selected, installDirectory, this::publish);
                }
                @Override protected void process(List<String> messages) {
                    if (!messages.isEmpty()) setStatus(messages.get(messages.size() - 1), BLUE);
                }
                @Override protected void done() {
                    setButtonsEnabled(true);
                    try {
                        setStatus(get() + " dosya kuruldu / güncellendi", GREEN);
                        refreshInventory();
                    } catch (Exception ex) {
                        setStatus("Kurulum başarısız: " + rootMessage(ex), RED);
                    }
                }
            }.execute();
        }

        private void removeSelected() {
            List<RemoteAsset> selected = selectedAssets();
            if (selected.isEmpty()) {
                setStatus("Önce kaldırılacak dosyaları işaretle.", MUTED);
                return;
            }
            int removed = 0;
            try {
                for (RemoteAsset asset : selected) {
                    Path target = installDirectory.resolve(asset.name).normalize();
                    if (target.getParent().equals(installDirectory.normalize()) && Files.deleteIfExists(target)) removed++;
                }
                setStatus(removed + " dosya kaldırıldı", GREEN);
                refreshInventory();
            } catch (IOException ex) {
                setStatus("Dosyalar kaldırılamadı: " + ex.getMessage(), RED);
            }
        }

        private List<RemoteAsset> selectedAssets() {
            List<RemoteAsset> selected = new ArrayList<>();
            for (JCheckBox check : choices) {
                if (check.isSelected()) selected.add((RemoteAsset) check.getClientProperty("asset"));
            }
            return selected;
        }

        private void showEmptyRow(String message) {
            rows.removeAll();
            JLabel empty = new JLabel(message);
            styleLabel(empty, MUTED, 11, false);
            empty.setBorder(new EmptyBorder(10, 8, 10, 8));
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            rows.add(empty);
            rows.revalidate();
            rows.repaint();
        }

        private void setStatus(String text, Color color) {
            status.setText(text);
            status.setForeground(color);
        }

        private void setButtonsEnabled(boolean enabled) {
            refreshButton.setEnabled(enabled);
            installButton.setEnabled(enabled);
            removeButton.setEnabled(enabled);
        }
    }

    private static JButton actionButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, primary ? 14 : 12));
        button.setForeground(primary ? new Color(255, 235, 237) : TEXT);
        button.setBackground(primary ? GREEN : CARD_ALT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? GREEN : new Color(58, 69, 84)),
                new EmptyBorder(primary ? 12 : 8, primary ? 17 : 12, primary ? 12 : 8, primary ? 17 : 12)));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static JButton discordLoginButton() {
        JButton button = actionButton("Discord ile giriş yap", false);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(88, 101, 242));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(76, 88, 220)),
                new EmptyBorder(8, 12, 8, 14)));
        button.setIcon(new ImageIcon(createDiscordMark()));
        button.setIconTextGap(9);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setPreferredSize(new Dimension(230, 38));
        return button;
    }

    private static BufferedImage createDiscordMark() {
        BufferedImage image = new BufferedImage(22, 18, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        Path2D mark = new Path2D.Float();
        mark.moveTo(5.0, 3.3);
        mark.curveTo(8.0, 2.0, 14.0, 2.0, 17.0, 3.3);
        mark.curveTo(19.5, 6.0, 20.1, 10.2, 19.0, 13.4);
        mark.curveTo(17.5, 14.8, 15.6, 15.6, 13.7, 15.7);
        mark.lineTo(12.3, 13.8);
        mark.curveTo(11.0, 14.1, 9.8, 14.1, 8.5, 13.8);
        mark.lineTo(7.1, 15.7);
        mark.curveTo(5.2, 15.5, 3.4, 14.7, 2.0, 13.4);
        mark.curveTo(0.9, 10.2, 1.5, 6.0, 5.0, 3.3);
        mark.closePath();
        g.fill(mark);
        g.setColor(new Color(88, 101, 242));
        g.fillOval(6, 7, 4, 4);
        g.fillOval(12, 7, 4, 4);
        g.dispose();
        return image;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(frame, message, "Akachi Launcher", JOptionPane.INFORMATION_MESSAGE);
    }

    private static String rootMessage(Exception exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }

    private record AuthResult(String accessToken, String refreshToken, String email, String userId) {}
    private record RemoteFile(String name, String downloadUrl, String sha) {}
    private record RemoteAsset(String name, String downloadUrl, String sha) {}
    private record ServerInfo(int online, int maxPlayers, String motd, long latencyMs) {}
}
