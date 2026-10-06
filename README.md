# Akachi Minecraft

Akachi sunucusu için launcher ve mod paketi deposu.

## İlk launcher sürümünü çalıştırma

1. Bilgisayarda JDK 21 kurulu olsun.
2. `launcher/run.bat` dosyasına çift tıkla.
3. **Oyunu kur ve aç** düğmesi Minecraft **1.20.1** istemcisini, Forge **47.4.26** profilini ve eksik oyun dosyalarını `%APPDATA%\Akachi Launcher\Minecraft` klasörüne indirip oyunu doğrudan başlatır.
4. **Sunucu modlarını güncelle** düğmesi, bu depodaki `mods/` dosyalarını Minecraft klasöründeki `mods/` klasörüne eşitler.
5. Mod Manager, texture pack ve shader pack bölümleri isteğe bağlı içerikleri ilgili klasörlere indirir.

Oyun dosyaları ve modlar `%APPDATA%\Akachi Launcher` klasöründe tutulur. Launcher bu klasörü otomatik oluşturur; kurulum yeri seçmen gerekmez.

Launcher sabit olarak Minecraft 1.20.1 ve Forge 47.4.26 kullanır. Minecraft dosyalarını resmî Mojang indirme adreslerinden edinir; launcher oyun dosyalarını kendi deposunda dağıtmaz.

## Akachi hesabı ve Minecraft adını bağlama

Launcher'da oyuncular e-posta/şifreyle veya Discord ile Akachi hesabına giriş yapar. Kayıt sırasında seçtikleri Minecraft Java oyun adı bu hesaba bir kez bağlanır; başka bir Akachi hesabı aynı adı alamaz. Launcher'da giriş yapılmadan oyuna geçiş düğmesi açılmaz.

İlk kurulumda `supabase/akachi_profiles.sql` içeriğini Supabase Dashboard → **SQL Editor**'a yapıştırıp **Run** ile bir kez çalıştır. Bu tablo oyun adını Akachi kullanıcı kimliğine bağlar. E-posta doğrulaması açıksa kayıt sonrası e-postadaki bağlantıya basıp sonra giriş yapılmalıdır.

Supabase bağlantısı launcher'a gömülüdür; yalnızca **Project URL** ve herkese açık **Publishable key** kullanılır. `sb_secret_...` veya `service_role` anahtarı launcher'a ya da Minecraft moduna konmamalıdır. Supabase publishable key gizli anahtar değildir.

Discord girişi için Supabase **Authentication → URL Configuration → Redirect URLs** listesine `http://127.0.0.1:43821/auth/callback` ekleyin. Discord Developer Portal'daki OAuth2 Redirect URI alanına ise Supabase'in **Authentication → Sign In / Providers → Discord** bölümünde gösterdiği `https://<project-ref>.supabase.co/auth/v1/callback` adresini ekleyin. Discord Client ID ve Client Secret sadece Supabase'te tutulur.

### Sunucuda oyun adı doğrulaması

Launcher girişi tek başına yeterli değildir. Sunucu da oyuncunun Akachi oturum belirtecini Supabase'te doğrulayıp token'a bağlı Minecraft adını bağlantıdaki Java adıyla karşılaştırmalıdır. Bunun için aynı `akachi-auth-mod` Forge modunun sunucuya ve oyuncu bilgisayarlarına kurulması gerekir. GitHub'da **Actions → Build Akachi Account Guard** çalışınca `akachi-auth-mod` adlı artifact çıkar. Artifact içindeki JAR'ı sunucunun `mods/` klasörüne ve deponun `mods/` klasörüne koyup GitHub Desktop'tan push et; launcher istemci kopyasını GitHub'dan kurar. Mod JAR'ı sunucuda çalışmadan sunucu tarafı kimlik doğrulaması aktif olmaz.

Launcher oyunu Forge istemcisi olarak doğrudan başlatır ve Minecraft oturum anahtarı yerine Akachi hesabına bağlı oyun adını kullanır. Bu özel sunucuda `server.properties` içindeki `online-mode=false` olmalı; Akachi doğrulama modu hem sunucuya hem oyuncuların Forge istemcilerine kurulup etkin olmalıdır. Mod çalışmadan bu giriş yöntemi kullanıcı doğrulaması sağlamaz.

## Zorunlu sunucu modları

Forge modlarının `.jar` dosyalarını deponun kökündeki `mods/` klasöründe tut. GitHub Desktop'tan değişiklikleri commit edip **Push origin** yaptığında launcher güncel listeyi görür. Launcher aynı içeriğe sahip yinelenen dosyaları bir kez indirir. Modları depoda yeniden dağıtmadan önce her modun lisansını ve yazarının dağıtım koşullarını kontrol et.

Minecraft sürümü: **1.20.1**  
Mod yükleyici: **Forge**

## Launcher güncellemeleri

Launcher açılışta `launcher/version.txt` güncelleme yapısını kontrol eder. Yeni yapı varsa alt çubukta **Güncelleme var** düğmesi görünür. Düğme güncel `AkachiLauncherSetup.exe` dosyasını indirip kurulum onayını açar; kurulum tamamlanınca launcher yeniden başlar. İlk kez güncelleme sistemini veya aynı görünen sürümün düzeltmesini yayımlarken `UPDATE_BUILD` ve `launcher/version.txt` değerlerini artır; ekranda görünen `LAUNCHER_VERSION` kullanıcının belirlediği sürüm adıdır.

Yeni görünen sürüm yayımlarken `AkachiLauncher.java` içindeki `LAUNCHER_VERSION` değerini artır. Her kurulum yayımlamasında ayrıca `UPDATE_BUILD` ile `launcher/version.txt` değerlerini artır, `AkachiLauncherSetup.exe` dosyasını depo köküne koyup GitHub'a gönder. İlk güncelleme sistemini içeren setup'ı oyuncuların bir kez elle kurması gerekir; sonraki sürümlerde launcher güncellemeyi kendisi bulur.

## İsteğe bağlı içerikler

- Forge modları: `optional-mods/` içine `.jar`
- Texture pack'ler: `resource-packs/` içine `.zip`
- Shader pack'ler: `shader-packs/` içine `.zip`

Oyuncular launcher'da istediklerini seçip kurabilir veya kaldırabilir. Texture pack
ve shader dosyaları oyuna kopyalanır; oyun içinden etkinleştirilir. Shader için
Forge 1.20.1 ile uyumlu Oculus gibi bir shader yükleyicisi gerekir.
