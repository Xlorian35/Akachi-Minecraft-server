using System;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Reflection;
using System.Windows.Forms;

internal static class AkachiSetup
{
    private const string PayloadResource = "AkachiLauncherSetup.Payload.zip";
    private static Form window;
    private static Label status;
    private static Label destination;
    private static ProgressBar progress;
    private static Button installButton;
    private static Button cancelButton;
    private static CheckBox desktopShortcut;

    [STAThread]
    private static void Main()
    {
        Application.EnableVisualStyles();
        Application.SetCompatibleTextRenderingDefault(false);
        window = new Form();
        window.Text = "Akachi Launcher Setup";
        window.StartPosition = FormStartPosition.CenterScreen;
        window.FormBorderStyle = FormBorderStyle.FixedDialog;
        window.MaximizeBox = false;
        window.MinimizeBox = false;
        window.ClientSize = new Size(520, 292);
        window.Icon = Icon.ExtractAssociatedIcon(Application.ExecutablePath);
        window.BackColor = Color.FromArgb(17, 21, 28);
        window.ForeColor = Color.FromArgb(238, 205, 208);
        window.Font = new Font("Segoe UI", 9F, FontStyle.Regular);
        status = new Label();
        status.Text = "Akachi Launcher bilgisayarına kurulacak.";
        status.ForeColor = Color.FromArgb(245, 225, 226);
        status.Font = new Font("Segoe UI", 13F, FontStyle.Bold);
        status.Location = new Point(26, 24);
        status.Size = new Size(468, 30);
        Label details = new Label();
        details.Text = "Devam etmeden önce kurulum konumunu kontrol et.\r\nMinecraft dosyaların ayrı bir klasörde tutulacak.";
        details.ForeColor = Color.FromArgb(190, 178, 183);
        details.Location = new Point(28, 66);
        details.Size = new Size(460, 44);
        Label locationTitle = new Label();
        locationTitle.Text = "KURULUM KONUMU";
        locationTitle.ForeColor = Color.FromArgb(193, 57, 73);
        locationTitle.Font = new Font("Segoe UI", 8F, FontStyle.Bold);
        locationTitle.Location = new Point(28, 124);
        locationTitle.Size = new Size(460, 20);
        destination = new Label();
        destination.Text = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "Akachi Launcher");
        destination.ForeColor = Color.FromArgb(238, 205, 208);
        destination.Location = new Point(28, 148);
        destination.Size = new Size(460, 24);
        desktopShortcut = new CheckBox();
        desktopShortcut.Text = "Masaüstüne Akachi Launcher kısayolu oluştur";
        desktopShortcut.Location = new Point(28, 178);
        desktopShortcut.Size = new Size(460, 24);
        desktopShortcut.Checked = true;
        desktopShortcut.ForeColor = Color.FromArgb(238, 205, 208);
        desktopShortcut.BackColor = window.BackColor;
        progress = new ProgressBar();
        progress.Location = new Point(28, 209);
        progress.Size = new Size(460, 18);
        progress.Style = ProgressBarStyle.Continuous;
        progress.Visible = false;
        installButton = new Button();
        installButton.Text = "Kur";
        installButton.DialogResult = DialogResult.None;
        installButton.Location = new Point(318, 244);
        installButton.Size = new Size(82, 30);
        installButton.FlatStyle = FlatStyle.Flat;
        installButton.BackColor = Color.FromArgb(169, 44, 61);
        installButton.ForeColor = Color.White;
        installButton.Click += delegate { Install(); };
        cancelButton = new Button();
        cancelButton.Text = "İptal";
        cancelButton.Location = new Point(406, 244);
        cancelButton.Size = new Size(82, 30);
        cancelButton.FlatStyle = FlatStyle.Flat;
        cancelButton.BackColor = Color.FromArgb(34, 40, 50);
        cancelButton.ForeColor = Color.FromArgb(238, 205, 208);
        cancelButton.Click += delegate { window.Close(); };
        window.CancelButton = cancelButton;
        window.AcceptButton = installButton;
        window.Controls.Add(status);
        window.Controls.Add(details);
        window.Controls.Add(locationTitle);
        window.Controls.Add(destination);
        window.Controls.Add(desktopShortcut);
        window.Controls.Add(progress);
        window.Controls.Add(installButton);
        window.Controls.Add(cancelButton);
        Application.Run(window);
    }

    private static void Install()
    {
        installButton.Enabled = false;
        cancelButton.Enabled = false;
        desktopShortcut.Enabled = false;
        progress.Visible = true;
        status.Text = "Kurulum başlatılıyor...";
        Application.DoEvents();
        try
        {
            string root = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "Akachi Launcher");
            string launcher = Path.Combine(root, "Launcher");
            string minecraft = Path.Combine(root, "Minecraft");
            Directory.CreateDirectory(launcher);
            Directory.CreateDirectory(minecraft);

            using (Stream payload = Assembly.GetExecutingAssembly().GetManifestResourceStream(PayloadResource))
            {
                if (payload == null) throw new InvalidOperationException("Kurulum paketi bulunamadı.");
                using (ZipArchive archive = new ZipArchive(payload, ZipArchiveMode.Read))
                {
                    progress.Minimum = 0;
                    progress.Maximum = Math.Max(1, archive.Entries.Count);
                    string safeRoot = Path.GetFullPath(launcher + Path.DirectorySeparatorChar);
                    int index = 0;
                    foreach (ZipArchiveEntry entry in archive.Entries)
                    {
                        string destination = Path.GetFullPath(Path.Combine(launcher, entry.FullName.Replace('/', Path.DirectorySeparatorChar)));
                        if (!destination.StartsWith(safeRoot, StringComparison.OrdinalIgnoreCase))
                            throw new InvalidDataException("Kurulum arşivinde geçersiz dosya yolu var.");
                        if (entry.Name.Length == 0)
                        {
                            Directory.CreateDirectory(destination);
                        }
                        else
                        {
                            Directory.CreateDirectory(Path.GetDirectoryName(destination));
                            using (Stream source = entry.Open())
                            using (FileStream output = new FileStream(destination, FileMode.Create, FileAccess.Write, FileShare.None))
                                source.CopyTo(output);
                        }
                        index++;
                        progress.Value = Math.Min(index, progress.Maximum);
                        status.Text = "Dosyalar yükleniyor: " + entry.Name;
                        Application.DoEvents();
                    }
                }
            }

            string executable = Path.Combine(launcher, "AkachiLauncher.exe");
            CreateShortcut(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData), "Microsoft", "Windows", "Start Menu", "Programs", "Akachi Launcher.lnk"), executable, launcher);
            string desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
            if (desktopShortcut.Checked && !String.IsNullOrEmpty(desktop))
                CreateShortcut(Path.Combine(desktop, "Akachi Launcher.lnk"), executable, launcher);

            status.Text = "Kurulum tamamlandı. Akachi Launcher açılıyor...";
            progress.Value = progress.Maximum;
            Application.DoEvents();
            Process.Start(new ProcessStartInfo(executable) { WorkingDirectory = launcher });
            window.Close();
        }
        catch (Exception ex)
        {
            MessageBox.Show(window, "Akachi Launcher kurulamadı.\r\n\r\n" + ex.Message, "Kurulum hatası", MessageBoxButtons.OK, MessageBoxIcon.Error);
            window.Close();
        }
    }

    private static void CreateShortcut(string shortcutPath, string target, string workingDirectory)
    {
        Directory.CreateDirectory(Path.GetDirectoryName(shortcutPath));
        Type shellType = Type.GetTypeFromProgID("WScript.Shell");
        object shell = Activator.CreateInstance(shellType);
        object shortcut = shellType.InvokeMember("CreateShortcut", BindingFlags.InvokeMethod, null, shell, new object[] { shortcutPath });
        Type shortcutType = shortcut.GetType();
        shortcutType.InvokeMember("TargetPath", BindingFlags.SetProperty, null, shortcut, new object[] { target });
        shortcutType.InvokeMember("WorkingDirectory", BindingFlags.SetProperty, null, shortcut, new object[] { workingDirectory });
        shortcutType.InvokeMember("IconLocation", BindingFlags.SetProperty, null, shortcut, new object[] { target + ",0" });
        shortcutType.InvokeMember("Description", BindingFlags.SetProperty, null, shortcut, new object[] { "Akachi Minecraft Launcher" });
        shortcutType.InvokeMember("Save", BindingFlags.InvokeMethod, null, shortcut, null);
    }
}
