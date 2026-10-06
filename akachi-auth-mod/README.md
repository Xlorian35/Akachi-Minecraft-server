# Akachi Account Guard

Forge 1.20.1 / 47.x client-server mod. The client reads `%APPDATA%\Akachi Launcher\Minecraft\akachi-auth.json` written by the launcher and sends its Supabase access token during connection. The server validates the token with Supabase Auth, reads the RLS-protected `akachi_profiles` row, and only leaves the player connected when the linked Minecraft Java name matches.

Before enabling the mod:

1. Run `../supabase/akachi_profiles.sql` once in the Supabase SQL Editor.
2. Build this project using `gradlew.bat build` with Java 17. The output JAR is in `build/libs/`.
3. Install the same JAR on the Forge server and in the launcher's top-level `mods/` folder; push that `mods/` folder to GitHub so clients can download it.
4. Keep `online-mode=true`; this mod adds Akachi authentication and does not replace a licensed Minecraft Java account.

The Supabase publishable key in source is public by design. Never put a Supabase secret or service-role key in this mod.