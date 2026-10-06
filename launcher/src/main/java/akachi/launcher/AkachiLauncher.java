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
import javax.swing.JProgressBar;
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
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.sun.net.httpserver.HttpServer;

import static java.awt.GridBagConstraints.WEST;

public final class AkachiLauncher {
    private static final String REPOSITORY = "Xlorian35/Akachi-Minecraft-server";
    private static final String BRANCH = "main";
    private static final String LAUNCHER_VERSION = "V0.7.7";
    private static final String UPDATE_BUILD = "V0.7.7";
    private static final String VERSION_URL = "https://raw.githubusercontent.com/" + REPOSITORY + "/" + BRANCH + "/launcher/version.txt";
    private static final String SETUP_DOWNLOAD_URL = "https://raw.githubusercontent.com/" + REPOSITORY + "/" + BRANCH + "/AkachiLauncherSetup.exe";
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
    private final JLabel modStatus = new JLabel("Gerekli modlar ve grafik bileşenleri oyuna girerken otomatik eşitlenir.");
    private final JLabel footerStatus = new JLabel("Minecraft " + MINECRAFT_VERSION + " · Forge " + FORGE_VERSION);
    private final JProgressBar downloadProgress = new JProgressBar(0, 100);
    private final JButton pingButton = actionButton("Sunucuyu kontrol et", false);
    private final JButton syncButton = actionButton("Modları şimdi eşitle", false);
    private final JButton launchButton = actionButton("Oyunu kur ve aç", true);
    private final JButton updateButton = actionButton("Güncelle", false);
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
        updateLaunchButtonLabel();
        loadSettings();
        refreshAuthCard();
        launchButton.setEnabled(false);
        buildWindow();
        if (!PREVIEW_MODE) restoreSavedSession();
        if (!PREVIEW_MODE && !SERVER_HOST.isBlank()) {
            checkServer();
        }
        if (!PREVIEW_MODE) checkForLauncherUpdate();
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

        JLabel ramHelp = new JLabel("GB · Doğrudan Minecraft başlatılırken uygulanır.");
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

        JPanel updateRow = new JPanel(new BorderLayout(12, 0));
        updateRow.setOpaque(false);
        updateRow.setBorder(new EmptyBorder(22, 0, 8, 0));
        JButton checkUpdates = actionButton("Güncellemeleri denetle", false);
        JLabel updateStatus = new JLabel("Sürüm " + LAUNCHER_VERSION);
        styleLabel(updateStatus, MUTED, 11, false);
        checkUpdates.addActionListener(e -> checkForLauncherUpdate(updateStatus, checkUpdates));
        updateRow.add(checkUpdates, BorderLayout.WEST);
        updateRow.add(updateStatus, BorderLayout.CENTER);

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
                applyGraphicsPreset();
                footerStatus.setText(graphicsPreset + " grafik ayarı ve " + ramGb + " GB RAM kaydedildi");
                dialog.dispose();
            } catch (IOException ex) {
                showMessage("Ayarlar kaydedilemedi.\n\n" + ex.getMessage());
            }
        });
        buttons.add(cancel);
        buttons.add(save);

        JPanel uninstallSection = new JPanel();
        uninstallSection.setOpaque(false);
        uninstallSection.setLayout(new BoxLayout(uninstallSection, BoxLayout.Y_AXIS));
        uninstallSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(73, 44, 50)),
                new EmptyBorder(18, 0, 0, 0)));
        JLabel uninstallTitle = new JLabel("UYGULAMAYI KALDIR");
        styleLabel(uninstallTitle, RED, 11, true);
        uninstallTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel uninstallHint = new JLabel("Launcher ve oturum dosyaları silinir; Minecraft klasörün korunur.");
        styleLabel(uninstallHint, MUTED, 11, false);
        uninstallHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        JCheckBox deleteMinecraft = new JCheckBox("Minecraft dosyalarını ve dünyaları da sil");
        deleteMinecraft.setOpaque(false);
        deleteMinecraft.setForeground(TEXT);
        deleteMinecraft.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deleteMinecraft.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton uninstall = actionButton("Uygulamayı kökten sil", true);
        uninstall.setForeground(Color.WHITE);
        uninstall.setBackground(new Color(132, 34, 46));
        uninstall.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(161, 43, 57)), new EmptyBorder(9, 14, 9, 14)));
        uninstall.setAlignmentX(Component.LEFT_ALIGNMENT);
        uninstall.addActionListener(e -> confirmAndUninstall(deleteMinecraft.isSelected(), dialog));
        uninstallSection.add(uninstallTitle);
        uninstallSection.add(Box.createVerticalStrut(5));
        uninstallSection.add(uninstallHint);
        uninstallSection.add(Box.createVerticalStrut(4));
        uninstallSection.add(deleteMinecraft);
        uninstallSection.add(Box.createVerticalStrut(7));
        uninstallSection.add(uninstall);
        uninstallSection.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(title);
        content.add(Box.createVerticalStrut(5));
        content.add(subtitle);
        content.add(ramRow);
        content.add(ramHelp);
        content.add(graphicsRow);
        content.add(graphicsHelp);
        content.add(updateRow);
        content.add(buttons);
        content.add(Box.createVerticalStrut(18));
        content.add(uninstallSection);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(470, dialog.getHeight()));
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void confirmAndUninstall(boolean removeMinecraftData, JDialog settingsDialog) {
        String details = removeMinecraftData
                ? "Akachi Launcher, kayıtlı oturumun ve Minecraft klasöründeki oyun dosyaları, modlar ve dünyalar kalıcı olarak silinecek. Bu işlem geri alınamaz."
                : "Akachi Launcher ve kayıtlı oturum dosyaları kaldırılacak. Minecraft oyun dosyaların ve dünyaların korunacak.";
        int choice = JOptionPane.showConfirmDialog(settingsDialog, details,
                "Akachi Launcher'ı kaldır", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        if (removeMinecraftData) {
            int finalChoice = JOptionPane.showConfirmDialog(settingsDialog,
                    "Minecraft klasöründeki dünyalar da silinecek. Gerçekten devam edilsin mi?",
                    "Minecraft verilerini de sil", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
            if (finalChoice != JOptionPane.YES_OPTION) return;
        }

        try {
            Path appRoot = AKACHI_DIRECTORY.toAbsolutePath().normalize();
            Path expectedRoot = appDataDirectory().toAbsolutePath().normalize();
            Path launcherDirectory = appRoot.resolve("Launcher").normalize();
            Path minecraftDirectory = appRoot.resolve("Minecraft").normalize();
            if (!appRoot.equals(expectedRoot)
                    || !launcherDirectory.getParent().equals(appRoot)
                    || !minecraftDirectory.getParent().equals(appRoot)
                    || Files.isSymbolicLink(appRoot)
                    || Files.isSymbolicLink(launcherDirectory)
                    || Files.isSymbolicLink(minecraftDirectory)) {
                throw new IOException("Uygulama klasörü doğrulanamadı; hiçbir dosya silinmedi.");
            }

            String appData = System.getenv("APPDATA");
            Path startMenu = (appData == null ? Path.of(System.getProperty("user.home"), "AppData", "Roaming") : Path.of(appData))
                    .resolve("Microsoft").resolve("Windows").resolve("Start Menu").resolve("Programs")
                    .resolve("Akachi Launcher.lnk");
            Path scriptPath = Path.of(System.getProperty("java.io.tmpdir"),
                    "akachi-uninstall-" + java.util.UUID.randomUUID() + ".ps1");
            String script = "$ErrorActionPreference = 'Stop'\n"
                    + "$launcherProcessId = " + ProcessHandle.current().pid() + "\n"
                    + "$appRoot = [IO.Path]::GetFullPath(" + powershellLiteral(appRoot.toString()) + ")\n"
                    + "$launcherDir = [IO.Path]::GetFullPath(" + powershellLiteral(launcherDirectory.toString()) + ")\n"
                    + "$gameDir = [IO.Path]::GetFullPath(" + powershellLiteral(minecraftDirectory.toString()) + ")\n"
                    + "$expectedRoot = [IO.Path]::GetFullPath((Join-Path $env:APPDATA 'Akachi Launcher'))\n"
                    + "if (![string]::Equals($appRoot.TrimEnd([char]92), $expectedRoot.TrimEnd([char]92), [StringComparison]::OrdinalIgnoreCase)) { exit 2 }\n"
                    + "if (![string]::Equals([IO.Path]::GetDirectoryName($launcherDir), $appRoot, [StringComparison]::OrdinalIgnoreCase) -or [IO.Path]::GetFileName($launcherDir) -ne 'Launcher') { exit 3 }\n"
                    + "if (![string]::Equals([IO.Path]::GetDirectoryName($gameDir), $appRoot, [StringComparison]::OrdinalIgnoreCase) -or [IO.Path]::GetFileName($gameDir) -ne 'Minecraft') { exit 4 }\n"
                    + "Wait-Process -Id $launcherProcessId -ErrorAction SilentlyContinue\n"
                    + "$desktopDir = [Environment]::GetFolderPath([Environment+SpecialFolder]::DesktopDirectory)\n"
                    + "$shortcuts = @(" + powershellLiteral(startMenu.toString()) + ", (Join-Path $desktopDir 'Akachi Launcher.lnk'))\n"
                    + "foreach ($shortcut in $shortcuts) { if (Test-Path -LiteralPath $shortcut) { Remove-Item -LiteralPath $shortcut -Force } }\n"
                    + "if (Test-Path -LiteralPath $launcherDir) { Remove-Item -LiteralPath $launcherDir -Recurse -Force }\n"
                    + (removeMinecraftData
                    ? "if (Test-Path -LiteralPath $gameDir) { Remove-Item -LiteralPath $gameDir -Recurse -Force }\n"
                    : "$authFile = Join-Path $gameDir 'akachi-auth.json'; if (Test-Path -LiteralPath $authFile) { Remove-Item -LiteralPath $authFile -Force }\n")
                    + "if ((Test-Path -LiteralPath $appRoot) -and @(Get-ChildItem -LiteralPath $appRoot -Force).Count -eq 0) { Remove-Item -LiteralPath $appRoot -Force }\n"
                    + "Remove-Item -LiteralPath $PSCommandPath -Force -ErrorAction SilentlyContinue\n";
            Files.writeString(scriptPath, script, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            new ProcessBuilder("powershell.exe", "-NoProfile", "-ExecutionPolicy", "Bypass",
                    "-WindowStyle", "Hidden", "-File", scriptPath.toString()).start();
            settingsDialog.dispose();
            frame.dispose();
            System.exit(0);
        } catch (IOException ex) {
            showMessage("Launcher kaldırılamadı.\n\n" + ex.getMessage());
        }
    }

    private static String powershellLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
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
        JPanel columns = new JPanel(new GridBagLayout());
        columns.setOpaque(false);

        JPanel leftColumn = new JPanel();
        leftColumn.setOpaque(false);
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.add(buildAssetSection("TEXTURE PACK", "İstediğin kaynak paketlerini indir; oyunda Kaynak Paketleri menüsünden etkinleştir.", resourcePackAssets));
        leftColumn.add(Box.createVerticalStrut(16));
        leftColumn.add(buildOptionalModsCard());
        leftColumn.add(Box.createVerticalStrut(16));
        leftColumn.add(buildAssetSection("SHADER PACK", "Shader arşivlerini indir. Forge 1.20.1'de kullanmak için Oculus gibi uyumlu bir mod gerekir.", shaderPackAssets));
        leftColumn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel rightColumn = new JPanel();
        rightColumn.setOpaque(false);
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.add(buildAuthCard());
        rightColumn.add(Box.createVerticalStrut(16));
        rightColumn.add(buildGameCard());
        rightColumn.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints leftCell = new GridBagConstraints(0, 0, 1, 1, 0.54, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 10), 0, 0);
        GridBagConstraints rightCell = new GridBagConstraints(1, 0, 1, 1, 0.46, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL,
                new Insets(0, 10, 0, 0), 0, 0);
        columns.add(leftColumn, leftCell);
        columns.add(rightColumn, rightCell);
        columns.setAlignmentX(Component.LEFT_ALIGNMENT);
        sections.add(columns);
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
            try { Files.deleteIfExists(sessionFilePath()); }
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
            authStatus.setText(maskEmail(signedInEmail) + " · Minecraft adı hesaba bağlandı");
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
        try {
            persistAuthSession(result);
        } catch (IOException ex) {
            setAuthBusy(false, "Oturum bu bilgisayarda hatırlanamadı: " + ex.getMessage());
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
        JDialog dialog = new JDialog(frame, "Minecraft adını bağla", true);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel surface = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(BACKGROUND);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g.setColor(new Color(166, 94, 105, 100));
                g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        surface.setOpaque(false);
        surface.setBorder(new EmptyBorder(1, 1, 1, 1));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, 17, 12, 12));
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        brand.setOpaque(false);
        Image logo = loadBrandImage(28);
        if (logo != null) brand.add(new JLabel(new ImageIcon(logo)));
        JLabel brandName = new JLabel("Akachi Launcher");
        styleLabel(brandName, TEXT, 13, true);
        brand.add(brandName);
        JButton close = new JButton("×");
        close.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        close.setForeground(MUTED);
        close.setContentAreaFilled(false);
        close.setBorder(BorderFactory.createEmptyBorder(0, 8, 2, 4));
        close.setFocusPainted(false);
        close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        close.addActionListener(e -> dialog.dispose());
        header.add(brand, BorderLayout.CENTER);
        header.add(close, BorderLayout.EAST);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(10, 25, 23, 25));
        JLabel eyebrow = new JLabel("HESAP KURULUMU  ·  1 / 1");
        styleLabel(eyebrow, GREEN, 10, true);
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Minecraft adını bağla");
        styleLabel(title, TEXT, 21, true);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel description = new JLabel("Bu ad Akachi hesabınla eşleşecek ve sunucu girişinde kullanılacak.");
        styleLabel(description, MUTED, 12, false);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel fieldLabel = new JLabel("MINECRAFT OYUN ADI");
        styleLabel(fieldLabel, TEXT, 10, true);
        fieldLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JTextField usernameField = new JTextField(suggestedUsername);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        styleInput(usernameField);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hint = new JLabel("3–16 karakter · İngilizce harf, rakam veya alt çizgi");
        styleLabel(hint, MUTED, 11, false);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel error = new JLabel(" ");
        styleLabel(error, RED, 11, false);
        error.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = actionButton("Daha sonra", false);
        JButton confirm = actionButton("Adı bağla", true);
        buttons.add(cancel);
        buttons.add(confirm);
        buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttons.getPreferredSize().height));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(eyebrow);
        content.add(Box.createVerticalStrut(8));
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(description);
        content.add(Box.createVerticalStrut(22));
        content.add(fieldLabel);
        content.add(Box.createVerticalStrut(7));
        content.add(usernameField);
        content.add(Box.createVerticalStrut(7));
        content.add(hint);
        content.add(Box.createVerticalStrut(2));
        content.add(error);
        content.add(Box.createVerticalStrut(12));
        content.add(buttons);
        surface.add(header, BorderLayout.NORTH);
        surface.add(content, BorderLayout.CENTER);
        dialog.setContentPane(surface);
        dialog.setSize(510, 330);
        dialog.setLocationRelativeTo(frame);

        final String[] chosenUsername = {null};
        Runnable submit = () -> {
            String value = usernameField.getText().trim();
            if (!validGameUsername(value)) {
                error.setText("Geçerli bir oyun adı gir: 3–16 harf, rakam veya _.");
                usernameField.requestFocusInWindow();
                return;
            }
            chosenUsername[0] = value;
            dialog.dispose();
        };
        confirm.addActionListener(e -> submit.run());
        usernameField.addActionListener(e -> submit.run());
        cancel.addActionListener(e -> dialog.dispose());
        dialog.getRootPane().setDefaultButton(confirm);
        usernameField.selectAll();
        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
        dialog.setVisible(true);

        String username = chosenUsername[0];
        if (username == null) {
            setAuthBusy(false, "Sunucuya girmek için Minecraft oyun adını bağlamalısın.");
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
            persistAuthSession(result);
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

    private static String maskEmail(String email) {
        if (email == null) return "";
        int at = email.indexOf('@');
        if (at <= 0 || at == email.length() - 1) return email;
        String localPart = email.substring(0, at);
        int visibleCharacters = Math.min(2, localPart.length());
        int hiddenCharacters = Math.max(2, localPart.length() - visibleCharacters);
        return localPart.substring(0, visibleCharacters) + "*".repeat(hiddenCharacters) + email.substring(at);
    }

    private static Path authFilePath() {
        return MINECRAFT_DIRECTORY.resolve("akachi-auth.json");
    }

    private static Path sessionFilePath() {
        return AKACHI_DIRECTORY.resolve("Launcher").resolve("session.json");
    }

    private void persistAuthSession(AuthResult result) throws IOException {
        if (result.refreshToken().isBlank()) return;
        Files.createDirectories(sessionFilePath().getParent());
        String session = "{\"refresh_token\":" + jsonQuote(result.refreshToken()) + "}";
        Files.writeString(sessionFilePath(), session, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        restrictSessionFilePermissions();
    }

    private void restrictSessionFilePermissions() {
        try {
            AclFileAttributeView acl = Files.getFileAttributeView(sessionFilePath(), AclFileAttributeView.class);
            if (acl == null) return;
            AclEntry ownerOnly = AclEntry.newBuilder()
                    .setType(AclEntryType.ALLOW)
                    .setPrincipal(acl.getOwner())
                    .setPermissions(EnumSet.allOf(AclEntryPermission.class))
                    .build();
            acl.setAcl(List.of(ownerOnly));
        } catch (IOException | UnsupportedOperationException ignored) {
            // AppData's per-user permissions remain the fallback on filesystems without ACL support.
        }
    }

    private void restoreSavedSession() {
        Path sessionPath = sessionFilePath();
        if (!Files.isRegularFile(sessionPath)) return;
        final String refreshToken;
        try {
            refreshToken = jsonText(Files.readString(sessionPath, StandardCharsets.UTF_8), "refresh_token");
        } catch (IOException ex) {
            setAuthMessage("Kaydedilmiş oturum okunamadı; tekrar giriş yap.", RED);
            return;
        }
        if (refreshToken.isBlank()) {
            setAuthMessage("Kaydedilmiş oturum geçersiz; tekrar giriş yap.", RED);
            return;
        }

        setAuthBusy(true, "Kaydedilmiş oturum açılıyor…");
        new SwingWorker<AuthResult, Void>() {
            @Override protected AuthResult doInBackground() throws Exception {
                String payload = "{\"refresh_token\":" + jsonQuote(refreshToken) + "}";
                return sendAuthRequest("/auth/v1/token?grant_type=refresh_token", payload);
            }

            @Override protected void done() {
                try {
                    AuthResult result = get();
                    if (result.accessToken().isBlank() || result.userId().isBlank()) {
                        throw new IOException("Yenilenen oturum bilgisi eksik.");
                    }
                    continueAuthentication(result, "");
                } catch (Exception ex) {
                    setAuthBusy(false, "Oturum yenilenemedi. Tekrar giriş yapabilirsin.");
                }
            }
        }.execute();
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
        JLabel version = new JLabel("LAUNCHER  ·  " + LAUNCHER_VERSION);
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
        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        downloadProgress.setStringPainted(true);
        downloadProgress.setString("0%");
        downloadProgress.setFont(new Font("Segoe UI", Font.BOLD, 10));
        downloadProgress.setForeground(new Color(238, 205, 208));
        downloadProgress.setBackground(CARD_ALT);
        downloadProgress.setBorderPainted(false);
        downloadProgress.setPreferredSize(new Dimension(300, 16));
        downloadProgress.setMaximumSize(new Dimension(300, 16));
        downloadProgress.setVisible(false);
        statusPanel.add(footerStatus);
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(downloadProgress);
        footer.add(statusPanel, BorderLayout.WEST);
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
        updateButton.setPreferredSize(new Dimension(112, 42));
        updateButton.setVisible(false);
        updateButton.addActionListener(e -> downloadAndInstallUpdate());
        launchButton.addActionListener(e -> installAndOpenMinecraft());
        rightControls.add(repository);
        rightControls.add(updateButton);
        rightControls.add(launchButton);
        footer.add(rightControls, BorderLayout.EAST);
        return footer;
    }

    private void checkForLauncherUpdate() {
        checkForLauncherUpdate(null, null);
    }

    private void checkForLauncherUpdate(JLabel resultLabel, JButton triggerButton) {
        if (triggerButton != null) {
            triggerButton.setEnabled(false);
            resultLabel.setText("Sürüm denetleniyor…");
            resultLabel.setForeground(BLUE);
        }
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                URI uri = URI.create(VERSION_URL + "?check=" + System.currentTimeMillis());
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofSeconds(12))
                        .header("User-Agent", "AkachiLauncher/" + UPDATE_BUILD)
                        .header("Cache-Control", "no-cache")
                        .GET().build();
                HttpResponse<String> response = httpClient().send(request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                if (response.statusCode() != 200) throw new IOException("Sürüm bilgisi alınamadı.");
                return response.body().trim();
            }

            @Override protected void done() {
                if (triggerButton != null) triggerButton.setEnabled(true);
                try {
                    String latestVersion = get();
                    if (compareVersions(latestVersion, UPDATE_BUILD) > 0) {
                        updateButton.setText("Güncelleme var");
                        updateButton.setVisible(true);
                        footerStatus.setText("Akachi Launcher " + latestVersion + " güncellemesi hazır");
                        if (resultLabel != null) {
                            resultLabel.setText("Yeni sürüm bulundu: " + latestVersion);
                            resultLabel.setForeground(GREEN);
                        }
                        frame.revalidate();
                    } else if (resultLabel != null) {
                        resultLabel.setText("Launcher güncel · " + LAUNCHER_VERSION);
                        resultLabel.setForeground(GREEN);
                    }
                } catch (Exception ex) {
                    if (resultLabel != null) {
                        resultLabel.setText("Güncelleme kontrol edilemedi");
                        resultLabel.setForeground(RED);
                    }
                    // Update checks are best-effort and must not block normal launcher use.
                }
            }
        }.execute();
    }

    private void downloadAndInstallUpdate() {
        updateButton.setEnabled(false);
        updateButton.setText("İndiriliyor…");
        Path updateDirectory = AKACHI_DIRECTORY.resolve("Launcher").resolve("updates");
        Path installer = updateDirectory.resolve("AkachiLauncherSetup.exe");
        Path temporary = updateDirectory.resolve("AkachiLauncherSetup.exe.part");

        new SwingWorker<Path, String>() {
            @Override protected Path doInBackground() throws Exception {
                Files.createDirectories(updateDirectory);
                URI uri = URI.create(SETUP_DOWNLOAD_URL + "?download=" + System.currentTimeMillis());
                HttpRequest request = HttpRequest.newBuilder(uri)
                        .timeout(Duration.ofMinutes(5))
                        .header("User-Agent", "AkachiLauncher/" + UPDATE_BUILD)
                        .GET().build();
                HttpResponse<InputStream> response = httpClient().send(request,
                        HttpResponse.BodyHandlers.ofInputStream());
                if (response.statusCode() != 200) {
                    response.body().close();
                    throw new IOException("Güncelleme indirilemedi (HTTP " + response.statusCode() + ").");
                }
                long total = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
                long downloaded = 0;
                try (InputStream in = response.body(); var out = Files.newOutputStream(temporary,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                    byte[] buffer = new byte[64 * 1024];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        downloaded += read;
                        if (downloaded % (4 * 1024 * 1024) < buffer.length) {
                            publish(total > 0
                                    ? "Güncelleme indiriliyor · " + (downloaded * 100 / total) + "%"
                                    : "Güncelleme indiriliyor…");
                        }
                    }
                }
                if (Files.size(temporary) < 10_000_000L) {
                    Files.deleteIfExists(temporary);
                    throw new IOException("İndirilen setup dosyası eksik görünüyor.");
                }
                try {
                    Files.move(temporary, installer, StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(temporary, installer, StandardCopyOption.REPLACE_EXISTING);
                }
                return installer;
            }

            @Override protected void process(List<String> messages) {
                if (!messages.isEmpty()) footerStatus.setText(messages.get(messages.size() - 1));
            }

            @Override protected void done() {
                try {
                    Path downloadedInstaller = get();
                    footerStatus.setText("Güncelleme kuruluyor · launcher yeniden açılacak");
                    new ProcessBuilder(downloadedInstaller.toString())
                            .directory(downloadedInstaller.getParent().toFile())
                            .start();
                    frame.dispose();
                    System.exit(0);
                } catch (Exception ex) {
                    updateButton.setEnabled(true);
                    updateButton.setText("Güncelleme var");
                    footerStatus.setText("Güncelleme başarısız: " + rootMessage(ex));
                }
            }
        }.execute();
    }

    private static int compareVersions(String first, String second) {
        String[] left = first.trim().replaceFirst("^[vV]", "").split("\\.");
        String[] right = second.trim().replaceFirst("^[vV]", "").split("\\.");
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int a = i < left.length ? versionPart(left[i]) : 0;
            int b = i < right.length ? versionPart(right[i]) : 0;
            if (a != b) return Integer.compare(a, b);
        }
        return 0;
    }

    private static int versionPart(String value) {
        Matcher matcher = Pattern.compile("^\\s*(\\d+)").matcher(value);
        if (!matcher.find()) return 0;
        try { return Integer.parseInt(matcher.group(1)); }
        catch (NumberFormatException ignored) { return 0; }
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
                int count = downloadRepositoryMods(destination, this::publish);
                disableOptiFine(destination, this::publish);
                return count;
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
        if (files.isEmpty()) {
            throw new IOException("GitHub'da indirilebilir .jar modu bulunamadı.");
        }
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

    private void disableOptiFine(Path modsDirectory, java.util.function.Consumer<String> progress) throws IOException {
        if (!Files.isDirectory(modsDirectory)) return;
        try (var files = Files.newDirectoryStream(modsDirectory, "OptiFine*.jar")) {
            for (Path optiFine : files) {
                Path backup = optiFine.resolveSibling(optiFine.getFileName() + ".disabled");
                int suffix = 1;
                while (Files.exists(backup)) {
                    backup = optiFine.resolveSibling(optiFine.getFileName() + ".disabled-" + suffix++);
                }
                Files.move(optiFine, backup);
                progress.accept("OptiFine/Posture çakışması önlendi; yedek: " + backup.getFileName());
            }
        }
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
        downloadProgress.setIndeterminate(true);
        downloadProgress.setValue(0);
        downloadProgress.setString("…");
        downloadProgress.setVisible(true);
        footerStatus.setText("Forge " + MINECRAFT_VERSION + "-" + FORGE_VERSION + " kontrol ediliyor…");
        Path gameDirectory = MINECRAFT_DIRECTORY;

        String username = currentGameUsername;
        new SwingWorker<GameLaunchConfig, String>() {
            @Override protected GameLaunchConfig doInBackground() throws Exception {
                publish("Depodaki zorunlu modlar kontrol ediliyor…");
                int modCount = downloadRepositoryMods(gameDirectory.resolve("mods"), this::publish);
                disableOptiFine(gameDirectory.resolve("mods"), this::publish);
                publish(modCount + " mod dosyası Minecraft klasörüne eşitlendi.");
                ensureForgeInstalled(gameDirectory, this::publish);
                return prepareDirectLaunch(gameDirectory, username, this::publish);
            }

            @Override protected void process(List<String> messages) {
                for (String message : messages) {
                    if (message.startsWith("@PROGRESS:")) {
                        String[] parts = message.split(":", 3);
                        if (parts.length == 3) {
                            try {
                                int percent = Math.max(0, Math.min(100, Integer.parseInt(parts[1])));
                                downloadProgress.setIndeterminate(false);
                                downloadProgress.setValue(percent);
                                downloadProgress.setString(percent + "%");
                                footerStatus.setText(parts[2]);
                            } catch (NumberFormatException ignored) { }
                        }
                    } else {
                        footerStatus.setText(message);
                    }
                }
            }

            @Override protected void done() {
                try {
                    GameLaunchConfig config = get();
                    downloadProgress.setVisible(false);
                    updateLaunchButtonLabel();
                    Process game = new ProcessBuilder(config.command)
                            .directory(gameDirectory.toFile())
                            .redirectErrorStream(true)
                            .redirectOutput(ProcessBuilder.Redirect.appendTo(config.logFile.toFile()))
                            .start();
                    launchButton.setEnabled(false);
                    footerStatus.setText("Minecraft " + MINECRAFT_VERSION + " · Forge " + FORGE_VERSION + " açıldı.");
                    CompletableFuture.runAsync(() -> {
                        int exitCode;
                        try { exitCode = game.waitFor(); }
                        catch (InterruptedException interrupted) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        SwingUtilities.invokeLater(() -> {
                            launchButton.setEnabled(authenticated);
                            footerStatus.setText(exitCode == 0
                                    ? "Minecraft kapandı."
                                    : "Minecraft açılamadı (" + exitCode + "). Günlük: " + config.logFile);
                        });
                    });
                } catch (Exception ex) {
                    downloadProgress.setVisible(false);
                    launchButton.setEnabled(authenticated);
                    footerStatus.setText("Minecraft başlatılamadı");
                    showMessage("Minecraft " + MINECRAFT_VERSION + " başlatılamadı.\n\n"
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

    private GameLaunchConfig prepareDirectLaunch(Path gameDirectory, String username,
                                                   java.util.function.Consumer<String> progress) throws Exception {
        if (username == null || username.isBlank()) throw new IOException("Akachi hesabına bir Minecraft adı bağla.");
        Path vanillaDirectory = gameDirectory.resolve("versions").resolve(MINECRAFT_VERSION);
        Path vanillaJsonFile = vanillaDirectory.resolve(MINECRAFT_VERSION + ".json");
        Map<String, Object> vanilla = loadVanillaMetadata(gameDirectory, vanillaJsonFile, progress);

        Map<String, Object> client = object(object(vanilla, "downloads"), "client");
        Path clientJar = vanillaDirectory.resolve(MINECRAFT_VERSION + ".jar");
        downloadVerified(string(client, "url"), clientJar, string(client, "sha1"), number(client, "size"), progress,
                "Minecraft istemcisi indiriliyor…");

        Path librariesDirectory = gameDirectory.resolve("libraries");
        downloadLibraries(list(vanilla, "libraries"), librariesDirectory, progress);

        Map<String, Object> assetIndex = object(vanilla, "assetIndex");
        String assetIndexId = string(assetIndex, "id");
        if (assetIndexId.isBlank()) throw new IOException("Minecraft kaynak paketi indeksi bulunamadı.");
        Path assetsDirectory = gameDirectory.resolve("assets");
        Path assetIndexFile = assetsDirectory.resolve("indexes").resolve(assetIndexId + ".json");
        downloadVerified(string(assetIndex, "url"), assetIndexFile, string(assetIndex, "sha1"),
                number(assetIndex, "size"), progress, "Minecraft kaynak paketi listesi indiriliyor…");
        downloadAssets(assetIndexFile, assetsDirectory, progress);

        Map<String, Object> forge = readJsonObject(gameDirectory.resolve("versions").resolve(FORGE_PROFILE)
                .resolve(FORGE_PROFILE + ".json"));
        downloadLibraries(list(forge, "libraries"), librariesDirectory, progress);
        Path nativesDirectory = gameDirectory.resolve("versions").resolve(FORGE_PROFILE).resolve("natives");
        extractWindowsNatives(librariesDirectory, nativesDirectory);

        LinkedHashSet<Path> classpathEntrySet = new LinkedHashSet<>();
        addLibraryArtifacts(classpathEntrySet, list(vanilla, "libraries"), librariesDirectory);
        addLibraryArtifacts(classpathEntrySet, list(forge, "libraries"), librariesDirectory);
        String forgeArtifactVersion = MINECRAFT_VERSION + "-" + FORGE_VERSION;
        Path forgeClientJar = librariesDirectory.resolve("net/minecraftforge/forge/")
                .resolve(forgeArtifactVersion).resolve("forge-" + forgeArtifactVersion + "-client.jar");
        if (Files.isRegularFile(forgeClientJar)) classpathEntrySet.add(forgeClientJar);
        List<Path> classpathEntries = new ArrayList<>(classpathEntrySet);
        Path minecraftClientLibraries = librariesDirectory.resolve("net/minecraft/client");
        boolean transformedForgeClientPresent = false;
        if (Files.isDirectory(minecraftClientLibraries)) {
            try (var paths = Files.walk(minecraftClientLibraries)) {
                transformedForgeClientPresent = paths.filter(Files::isRegularFile)
                        .anyMatch(path -> path.getFileName().toString().matches(
                                "client-" + java.util.regex.Pattern.quote(MINECRAFT_VERSION)
                                        + "-[^-]+-srg\\.jar"));
            }
        }
        // Forge installs a remapped client-srg jar beside the normal Minecraft client.
        // Forge discovers that jar itself; adding the vanilla jar here creates two
        // modules containing the same net.minecraft packages and aborts startup.
        if (!transformedForgeClientPresent) classpathEntries.add(clientJar);
        String classpath = classpathEntries.stream().map(Path::toString)
                .collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator));

        String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8))
                .toString();
        List<String> command = new ArrayList<>();
        command.add(javaExecutable());
        command.add("-Xmx" + Math.max(2, ramGb) + "G");
        command.add("-Djava.library.path=" + nativesDirectory);
        command.add("-Dminecraft.launcher.brand=AkachiLauncher");
        command.add("-Dminecraft.launcher.version=" + LAUNCHER_VERSION);

        Map<String, Object> arguments = object(forge, "arguments");
        List<String> jvmArguments = strings(arguments.get("jvm"));
        if (jvmArguments.isEmpty()) throw new IOException("Forge başlatma ayarları eksik.");
        Map<String, String> substitutions = Map.of(
                "${library_directory}", librariesDirectory.toString(),
                "${classpath_separator}", java.io.File.pathSeparator,
                "${version_name}", FORGE_PROFILE,
                "${natives_directory}", nativesDirectory.toString(),
                "${game_directory}", gameDirectory.toString(),
                "${assets_root}", assetsDirectory.toString(),
                "${assets_index_name}", assetIndexId,
                "${classpath}", classpath,
                "${launcher_name}", "AkachiLauncher",
                "${launcher_version}", LAUNCHER_VERSION);
        for (String argument : jvmArguments) {
            String expanded = expandArgument(argument, substitutions);
            // Forge's installer leaves these build-time-only jars in the shared
            // libraries directory. BootstrapLauncher otherwise treats them as
            // runtime modules, where FART's bundled ASM conflicts with Forge's ASM.
            if (expanded.startsWith("-DignoreList=")) {
                expanded += ",ForgeAutoRenamingTool,animal-sniffer-annotations";
            }
            command.add(expanded);
        }
        command.add("-cp");
        command.add(classpath);
        command.add(string(forge, "mainClass"));

        addGameArgument(command, "--username", username);
        addGameArgument(command, "--version", FORGE_PROFILE);
        addGameArgument(command, "--gameDir", gameDirectory.toString());
        addGameArgument(command, "--assetsDir", assetsDirectory.toString());
        addGameArgument(command, "--assetIndex", assetIndexId);
        addGameArgument(command, "--uuid", uuid);
        addGameArgument(command, "--accessToken", "0");
        addGameArgument(command, "--clientId", "0");
        addGameArgument(command, "--xuid", "0");
        addGameArgument(command, "--userType", "legacy");
        addGameArgument(command, "--versionType", "release");
        addGameArgument(command, "--userProperties", "{}");
        for (String argument : strings(object(forge, "arguments").get("game"))) command.add(argument);

        Path logFile = gameDirectory.resolve("logs").resolve("akachi-launcher.log");
        Files.createDirectories(logFile.getParent());
        progress.accept("Minecraft " + MINECRAFT_VERSION + " doğrudan başlatılmaya hazır.");
        return new GameLaunchConfig(command, logFile);
    }

    private Map<String, Object> loadVanillaMetadata(Path gameDirectory, Path versionFile,
                                                     java.util.function.Consumer<String> progress) throws Exception {
        if (!Files.isRegularFile(versionFile)) {
            progress.accept("Minecraft " + MINECRAFT_VERSION + " sürüm bilgisi indiriliyor…");
            String manifestText = downloadText("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
            Map<String, Object> manifest = parseJsonObject(manifestText);
            Map<String, Object> selected = null;
            for (Object entry : list(manifest, "versions")) {
                Map<String, Object> candidate = asObject(entry);
                if (MINECRAFT_VERSION.equals(string(candidate, "id"))) {
                    selected = candidate;
                    break;
                }
            }
            if (selected == null) throw new IOException("Resmi Minecraft sürüm bilgisi bulunamadı.");
            String metadataUrl = string(selected, "url");
            String metadata = downloadText(metadataUrl);
            Files.createDirectories(versionFile.getParent());
            Files.writeString(versionFile, metadata, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }
        Map<String, Object> version = readJsonObject(versionFile);
        if (!MINECRAFT_VERSION.equals(string(version, "id"))) {
            throw new IOException("Minecraft sürüm dosyası geçersiz.");
        }
        return version;
    }

    private void downloadLibraries(List<Object> libraries, Path root,
                                   java.util.function.Consumer<String> progress) throws Exception {
        for (Object value : libraries) {
            Map<String, Object> library = asObject(value);
            if (!libraryAllowedOnWindows(library)) continue;
            Map<String, Object> downloads = object(library, "downloads");
            Map<String, Object> artifact = object(downloads, "artifact");
            if (!artifact.isEmpty()) {
                Path destination = safeChild(root, string(artifact, "path"));
                downloadVerified(string(artifact, "url"), destination, string(artifact, "sha1"),
                        number(artifact, "size"), progress, "Minecraft kitaplıkları indiriliyor…");
            }
            Map<String, Object> natives = object(library, "natives");
            String nativeClassifier = string(natives, "windows").replace("${arch}",
                    System.getProperty("os.arch", "").contains("64") ? "64" : "32");
            Map<String, Object> classifiers = object(downloads, "classifiers");
            Map<String, Object> nativeArtifact = object(classifiers, nativeClassifier);
            if (!nativeArtifact.isEmpty()) {
                Path destination = safeChild(root, string(nativeArtifact, "path"));
                downloadVerified(string(nativeArtifact, "url"), destination, string(nativeArtifact, "sha1"),
                        number(nativeArtifact, "size"), progress, "Minecraft Windows kitaplıkları indiriliyor…");
            }
        }
    }

    private static void addLibraryArtifacts(Set<Path> classpath, List<Object> libraries, Path root)
            throws IOException {
        for (Object value : libraries) {
            Map<String, Object> library = asObject(value);
            if (!libraryAllowedOnWindows(library)) continue;
            Map<String, Object> artifact = object(object(library, "downloads"), "artifact");
            String relativePath = string(artifact, "path");
            if (relativePath.isBlank()) continue;
            Path jar = safeChild(root, relativePath);
            if (Files.isRegularFile(jar)) classpath.add(jar);
        }
    }

    private void downloadAssets(Path indexFile, Path assetsDirectory,
                                java.util.function.Consumer<String> progress) throws Exception {
        Map<String, Object> index = readJsonObject(indexFile);
        Map<String, Object> objects = object(index, "objects");
        List<Future<?>> pending = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(10, runnable -> {
            Thread thread = new Thread(runnable, "akachi-assets-download");
            thread.setDaemon(true);
            return thread;
        });
        AtomicInteger completed = new AtomicInteger();
        int totalAssets = Math.max(1, objects.size());
        progress.accept("@PROGRESS:0:Minecraft dosyaları hazırlanıyor…");
        try {
            for (Object value : objects.values()) {
                Map<String, Object> asset = asObject(value);
                String hash = string(asset, "hash");
                if (!hash.matches("[0-9a-fA-F]{40}")) continue;
                Path destination = assetsDirectory.resolve("objects").resolve(hash.substring(0, 2)).resolve(hash);
                long size = number(asset, "size");
                if (Files.isRegularFile(destination) && Files.size(destination) == size) {
                    int count = completed.incrementAndGet();
                    progress.accept("@PROGRESS:" + (count * 100 / totalAssets)
                            + ":Minecraft dosyaları denetleniyor…");
                    continue;
                }
                pending.add(executor.submit(() -> {
                    try {
                        downloadVerified("https://resources.download.minecraft.net/" + hash.substring(0, 2) + "/" + hash,
                                destination, hash, size, progress, "Minecraft dosyaları indiriliyor…");
                        int count = completed.incrementAndGet();
                        progress.accept("@PROGRESS:" + (count * 100 / totalAssets)
                                + ":Minecraft dosyaları indiriliyor · " + count + "/" + objects.size());
                    } catch (Exception ex) {
                        throw new java.util.concurrent.CompletionException(ex);
                    }
                }));
            }
            for (Future<?> future : pending) future.get();
        } finally {
            executor.shutdownNow();
        }
        progress.accept("@PROGRESS:100:Minecraft dosyaları hazır.");
        progress.accept("Minecraft kaynak dosyaları hazır.");
    }

    private static boolean libraryAllowedOnWindows(Map<String, Object> library) {
        List<Object> rules = list(library, "rules");
        if (rules.isEmpty()) return true;
        boolean allowed = false;
        for (Object value : rules) {
            Map<String, Object> rule = asObject(value);
            Map<String, Object> os = object(rule, "os");
            String name = string(os, "name");
            if (name.isBlank() || "windows".equalsIgnoreCase(name)) {
                allowed = "allow".equalsIgnoreCase(string(rule, "action"));
            }
        }
        return allowed;
    }

    private static void extractWindowsNatives(Path librariesDirectory, Path nativesDirectory) throws IOException {
        Files.createDirectories(nativesDirectory);
        try (var paths = Files.walk(librariesDirectory)) {
            for (Path archive : paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().contains("natives-windows")
                            && path.getFileName().toString().endsWith(".jar")).toList()) {
                try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(archive))) {
                    ZipEntry entry;
                    while ((entry = zip.getNextEntry()) != null) {
                        if (entry.isDirectory()) continue;
                        String name = entry.getName();
                        if (name.startsWith("META-INF/")) continue;
                        Path output = nativesDirectory.resolve(name).normalize();
                        if (!output.startsWith(nativesDirectory.normalize())) {
                            throw new IOException("Minecraft yerel kitaplığında geçersiz dosya yolu var.");
                        }
                        Files.createDirectories(output.getParent());
                        Files.copy(zip, output, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

    private static Path safeChild(Path root, String relative) throws IOException {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path child = normalizedRoot.resolve(relative.replace('/', java.io.File.separatorChar)).normalize();
        if (!child.startsWith(normalizedRoot)) throw new IOException("Minecraft kitaplığında geçersiz dosya yolu var.");
        return child;
    }

    private void downloadVerified(String url, Path destination, String sha1, long expectedSize,
                                  java.util.function.Consumer<String> progress, String message) throws Exception {
        if (url == null || url.isBlank()) throw new IOException("Minecraft indirme adresi eksik.");
        if (Files.isRegularFile(destination)) {
            boolean sizeMatches = expectedSize <= 0 || Files.size(destination) == expectedSize;
            boolean hashMatches = sha1 == null || sha1.isBlank() || fileSha1(destination).equalsIgnoreCase(sha1);
            if (sizeMatches && hashMatches) return;
        }
        Files.createDirectories(destination.getParent());
        Path temporary = destination.resolveSibling(destination.getFileName() + ".akachi-part");
        progress.accept(message);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(3))
                .header("User-Agent", "AkachiLauncher/" + UPDATE_BUILD).GET().build();
        HttpResponse<InputStream> response = httpClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() != 200) {
            response.body().close();
            throw new IOException("Minecraft dosyası indirilemedi (HTTP " + response.statusCode() + ").");
        }
        try (InputStream input = response.body(); var output = Files.newOutputStream(temporary,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            input.transferTo(output);
        } catch (Exception ex) {
            Files.deleteIfExists(temporary);
            throw ex;
        }
        if (expectedSize > 0 && Files.size(temporary) != expectedSize
                || sha1 != null && !sha1.isBlank() && !fileSha1(temporary).equalsIgnoreCase(sha1)) {
            Files.deleteIfExists(temporary);
            throw new IOException("Minecraft dosyasının bütünlük doğrulaması başarısız.");
        }
        try {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String downloadText(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(45))
                .header("User-Agent", "AkachiLauncher/" + UPDATE_BUILD).GET().build();
        HttpResponse<String> response = httpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) throw new IOException("Minecraft sürüm bilgisi indirilemedi (HTTP " + response.statusCode() + ").");
        return response.body();
    }

    private static void addGameArgument(List<String> command, String name, String value) {
        command.add(name);
        command.add(value);
    }

    private static String expandArgument(String value, Map<String, String> substitutions) {
        String expanded = value;
        for (Map.Entry<String, String> entry : substitutions.entrySet()) {
            expanded = expanded.replace(entry.getKey(), entry.getValue());
        }
        return expanded;
    }

    private static Map<String, Object> readJsonObject(Path file) throws IOException {
        return parseJsonObject(Files.readString(file, StandardCharsets.UTF_8));
    }

    private static Map<String, Object> parseJsonObject(String json) throws IOException {
        Object value = new JsonReader(json).parse();
        if (!(value instanceof Map<?, ?> map)) throw new IOException("Minecraft JSON bilgisi geçersiz.");
        return asObject(map);
    }

    private static Map<String, Object> asObject(Object value) {
        if (!(value instanceof Map<?, ?> map)) return Map.of();
        Map<String, Object> result = new LinkedHashMap<>();
        map.forEach((key, entry) -> result.put(String.valueOf(key), entry));
        return result;
    }

    private static Map<String, Object> object(Map<String, Object> parent, String key) {
        return asObject(parent.get(key));
    }

    private static List<Object> list(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        return value instanceof List<?> items ? new ArrayList<>(items) : List.of();
    }

    private static List<String> strings(Object value) {
        if (!(value instanceof List<?> items)) return List.of();
        List<String> result = new ArrayList<>();
        for (Object item : items) if (item instanceof String text) result.add(text);
        return result;
    }

    private static String string(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private static long number(Map<String, Object> parent, String key) {
        Object value = parent.get(key);
        return value instanceof Number number ? number.longValue() : 0L;
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

    private boolean isForgeInstalled() {
        return Files.isRegularFile(MINECRAFT_DIRECTORY.resolve("versions").resolve(FORGE_PROFILE)
                .resolve(FORGE_PROFILE + ".json"));
    }

    private void updateLaunchButtonLabel() {
        launchButton.setText(isForgeInstalled() ? "Oyuna gir" : "Oyunu kur ve aç");
    }

    private void selectForgeLauncherProfile(Path gameDirectory) throws IOException {
        Path profilesFile = gameDirectory.resolve("launcher_profiles.json");
        if (!Files.isRegularFile(profilesFile)) {
            Path storeProfiles = gameDirectory.resolve("launcher_profiles_microsoft_store.json");
            if (Files.isRegularFile(storeProfiles)) profilesFile = storeProfiles;
        }
        if (!Files.isRegularFile(profilesFile)) return;
        String json = Files.readString(profilesFile, StandardCharsets.UTF_8);
        String profileId = "akachi-forge-" + MINECRAFT_VERSION.replace(".", "");
        String timestamp = java.time.Instant.now().toString();
        String profilesKey = "\"profiles\"";
        int profilesKeyAt = json.indexOf(profilesKey);
        if (profilesKeyAt < 0) throw new IOException("Minecraft profilleri bulunamadı.");
        int profilesStart = json.indexOf('{', profilesKeyAt + profilesKey.length());
        int profilesEnd = matchingJsonObjectEnd(json, profilesStart);
        if (profilesStart < 0 || profilesEnd < 0) throw new IOException("Minecraft profil listesi okunamadı.");

        String profileMarker = jsonQuote(profileId) + ":";
        if (!json.substring(profilesStart + 1, profilesEnd).contains(profileMarker)) {
            String profile = "\n    " + profileMarker + " {\n"
                    + "      \"name\": \"Akachi Forge " + MINECRAFT_VERSION + "\",\n"
                    + "      \"type\": \"custom\",\n"
                    + "      \"created\": " + jsonQuote(timestamp) + ",\n"
                    + "      \"lastUsed\": " + jsonQuote(timestamp) + ",\n"
                    + "      \"lastVersionId\": " + jsonQuote(FORGE_PROFILE) + ",\n"
                    + "      \"gameDir\": " + jsonQuote(gameDirectory.toString()) + "\n"
                    + "    },";
            json = json.substring(0, profilesStart + 1) + profile + json.substring(profilesStart + 1);
        }
        java.util.regex.Pattern selected = java.util.regex.Pattern.compile(
                "\\\"selectedProfile\\\"\\s*:\\s*(?:\\\"(?:\\\\.|[^\\\"\\\\])*\\\"|null)");
        java.util.regex.Matcher matcher = selected.matcher(json);
        if (matcher.find()) {
            json = matcher.replaceFirst(java.util.regex.Matcher.quoteReplacement("\"selectedProfile\": " + jsonQuote(profileId)));
        } else {
            int rootStart = json.indexOf('{');
            if (rootStart < 0) throw new IOException("Minecraft başlatıcı ayarları okunamadı.");
            json = json.substring(0, rootStart + 1) + "\n  \"selectedProfile\": " + jsonQuote(profileId) + "," + json.substring(rootStart + 1);
        }
        Files.writeString(profilesFile, json, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    private static int matchingJsonObjectEnd(String json, int start) {
        if (start < 0 || start >= json.length() || json.charAt(start) != '{') return -1;
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (inString) {
                if (escaped) escaped = false;
                else if (ch == '\\') escaped = true;
                else if (ch == '"') inString = false;
                continue;
            }
            if (ch == '"') inString = true;
            else if (ch == '{') depth++;
            else if (ch == '}' && --depth == 0) return i;
        }
        return -1;
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

    private static final class JsonReader {
        private final String source;
        private int index;

        private JsonReader(String source) { this.source = source; }

        private Object parse() throws IOException {
            Object value = readValue();
            skipWhitespace();
            if (index != source.length()) throw new IOException("Minecraft JSON bilgisinde fazla veri var.");
            return value;
        }

        private Object readValue() throws IOException {
            skipWhitespace();
            if (index >= source.length()) throw new IOException("Minecraft JSON bilgisi eksik.");
            return switch (source.charAt(index)) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject() throws IOException {
            Map<String, Object> result = new LinkedHashMap<>();
            index++;
            skipWhitespace();
            if (consume('}')) return result;
            while (true) {
                skipWhitespace();
                if (index >= source.length() || source.charAt(index) != '"') throw new IOException("Minecraft JSON anahtarı geçersiz.");
                String key = readString();
                skipWhitespace();
                if (!consume(':')) throw new IOException("Minecraft JSON iki nokta işareti eksik.");
                result.put(key, readValue());
                skipWhitespace();
                if (consume('}')) return result;
                if (!consume(',')) throw new IOException("Minecraft JSON virgülü eksik.");
            }
        }

        private List<Object> readArray() throws IOException {
            List<Object> result = new ArrayList<>();
            index++;
            skipWhitespace();
            if (consume(']')) return result;
            while (true) {
                result.add(readValue());
                skipWhitespace();
                if (consume(']')) return result;
                if (!consume(',')) throw new IOException("Minecraft JSON dizisi geçersiz.");
            }
        }

        private String readString() throws IOException {
            if (!consume('"')) throw new IOException("Minecraft JSON metni geçersiz.");
            StringBuilder result = new StringBuilder();
            while (index < source.length()) {
                char ch = source.charAt(index++);
                if (ch == '"') return result.toString();
                if (ch != '\\') {
                    result.append(ch);
                    continue;
                }
                if (index >= source.length()) throw new IOException("Minecraft JSON kaçış dizisi eksik.");
                char escaped = source.charAt(index++);
                switch (escaped) {
                    case '"', '\\', '/' -> result.append(escaped);
                    case 'b' -> result.append('\b');
                    case 'f' -> result.append('\f');
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case 'u' -> {
                        if (index + 4 > source.length()) throw new IOException("Minecraft JSON Unicode kaçışı eksik.");
                        try { result.append((char) Integer.parseInt(source.substring(index, index + 4), 16)); }
                        catch (NumberFormatException ex) { throw new IOException("Minecraft JSON Unicode kaçışı geçersiz.", ex); }
                        index += 4;
                    }
                    default -> throw new IOException("Minecraft JSON kaçış karakteri geçersiz.");
                }
            }
            throw new IOException("Minecraft JSON metni kapatılmamış.");
        }

        private Object readNumber() throws IOException {
            int start = index;
            while (index < source.length() && "-+0123456789.eE".indexOf(source.charAt(index)) >= 0) index++;
            if (start == index) throw new IOException("Minecraft JSON değeri geçersiz.");
            String value = source.substring(start, index);
            try {
                if (value.contains(".") || value.contains("e") || value.contains("E")) return Double.parseDouble(value);
                return Long.parseLong(value);
            } catch (NumberFormatException ex) {
                throw new IOException("Minecraft JSON sayısı geçersiz.", ex);
            }
        }

        private Object readLiteral(String literal, Object value) throws IOException {
            if (!source.startsWith(literal, index)) throw new IOException("Minecraft JSON değeri geçersiz.");
            index += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (index < source.length() && Character.isWhitespace(source.charAt(index))) index++;
        }

        private boolean consume(char expected) {
            if (index >= source.length() || source.charAt(index) != expected) return false;
            index++;
            return true;
        }
    }

    private record GameLaunchConfig(List<String> command, Path logFile) {}
    private record AuthResult(String accessToken, String refreshToken, String email, String userId) {}
    private record RemoteFile(String name, String downloadUrl, String sha) {}
    private record RemoteAsset(String name, String downloadUrl, String sha) {}
    private record ServerInfo(int online, int maxPlayers, String motd, long latencyMs) {}
}
