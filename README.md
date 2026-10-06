# Akachi Minecraft

Akachi sunucusu için launcher ve mod paketi deposu.

## İlk launcher sürümünü çalıştırma

1. Bilgisayarda JDK 21 kurulu olsun.
2. `launcher/run.bat` dosyasına çift tıkla.
3. **Sürümü kur ve aç** düğmesi Minecraft **1.20.1** istemcisini ve **Forge 47.4.26** bileşenlerini ilk kullanımda resmi Forge yükleyicisinden `%APPDATA%\Akachi Launcher\Minecraft` klasörüne indirir, ardından Minecraft Launcher'ı açar.
4. Resmi Minecraft Launcher'da Microsoft hesabınla giriş yapıp **1.20.1-forge-47.4.26** profilini başlat.
5. **Sunucu modlarını güncelle** düğmesi, bu depodaki `mods/` dosyalarını Minecraft klasöründeki `mods/` klasörüne eşitler.
6. Mod Manager, texture pack ve shader pack bölümleri isteğe bağlı içerikleri ilgili klasörlere indirir.

Oyun dosyaları ve modlar `%APPDATA%\Akachi Launcher` klasöründe tutulur. Launcher bu klasörü otomatik oluşturur; kurulum yeri seçmen gerekmez.

Launcher sabit olarak Minecraft 1.20.1 ve Forge 47.4.26 kullanır. Minecraft oyun dosyaları Forge yükleyicisi ve resmi Minecraft Launcher üzerinden edinilir; launcher oyun dosyalarını kendi deposunda dağıtmaz.

## Akachi hesabı ve Minecraft adını bağlama

Launcher'da oyuncular e-posta/şifreyle veya Discord ile Akachi hesabına giriş yapar. Kayıt sırasında seçtikleri Minecraft Java oyun adı bu hesaba bir kez bağlanır; başka bir Akachi hesabı aynı adı alamaz. Launcher'da giriş yapılmadan oyuna geçiş düğmesi açılmaz.

İlk kurulumda `supabase/akachi_profiles.sql` içeriğini Supabase Dashboard → **SQL Editor**'a yapıştırıp **Run** ile bir kez çalıştır. Bu tablo oyun adını Akachi kullanıcı kimliğine bağlar. E-posta doğrulaması açıksa kayıt sonrası e-postadaki bağlantıya basıp sonra giriş yapılmalıdır.

Supabase bağlantısı launcher'a gömülüdür; yalnızca **Project URL** ve herkese açık **Publishable key** kullanılır. `sb_secret_...` veya `service_role` anahtarı launcher'a ya da Minecraft moduna konmamalıdır. Supabase publishable key gizli anahtar değildir.

Discord girişi için Supabase **Authentication → URL Configuration → Redirect URLs** listesine `http://127.0.0.1:43821/auth/callback` ekleyin. Discord Developer Portal'daki OAuth2 Redirect URI alanına ise Supabase'in **Authentication → Sign In / Providers → Discord** bölümünde gösterdiği `https://<project-ref>.supabase.co/auth/v1/callback` adresini ekleyin. Discord Client ID ve Client Secret sadece Supabase'te tutulur.

### Sunucuda oyun adı doğrulaması

Launcher girişi tek başına yeterli değildir. Sunucu da oyuncunun Akachi oturum belirtecini Supabase'te doğrulayıp token'a bağlı Minecraft adını bağlantıdaki Java adıyla karşılaştırmalıdır. Bunun için aynı `akachi-auth-mod` Forge modunun sunucuya ve oyuncu bilgisayarlarına kurulması gerekir. GitHub'da **Actions → Build Akachi Account Guard** çalışınca `akachi-auth-mod` adlı artifact çıkar. Artifact içindeki JAR'ı sunucunun `mods/` klasörüne ve deponun `mods/` klasörüne koyup GitHub Desktop'tan push et; launcher istemci kopyasını GitHub'dan kurar. Mod JAR'ı sunucuda çalışmadan sunucu tarafı kimlik doğrulaması aktif olmaz.

Sunucuda `online-mode=true` ayarını açık tut. Akachi doğrulaması, resmi Minecraft hesabı doğrulamasına ek korumadır; Microsoft/Minecraft Java hesabı gereksiniminin yerini almaz. Böylece başka biri yalnızca aynı oyun adını yazıp giremez: Akachi hesabı da o ada bağlı olmalı ve istemci modunun geçerli token göndermesi gerekir.

## Zorunlu sunucu modları

Forge modlarının `.jar` dosyalarını deponun kökündeki `mods/` klasöründe tut. GitHub Desktop'tan değişiklikleri commit edip **Push origin** yaptığında launcher güncel listeyi görür. Launcher aynı içeriğe sahip yinelenen dosyaları bir kez indirir. Modları depoda yeniden dağıtmadan önce her modun lisansını ve yazarının dağıtım koşullarını kontrol et.

Minecraft sürümü: **1.20.1**  
Mod yükleyici: **Forge**

## İsteğe bağlı içerikler

- Forge modları: `optional-mods/` içine `.jar`
- Texture pack'ler: `resource-packs/` içine `.zip`
- Shader pack'ler: `shader-packs/` içine `.zip`

Oyuncular launcher'da istediklerini seçip kurabilir veya kaldırabilir. Texture pack
ve shader dosyaları oyuna kopyalanır; oyun içinden etkinleştirilir. Shader için
Forge 1.20.1 ile uyumlu Oculus gibi bir shader yükleyicisi gerekir.
