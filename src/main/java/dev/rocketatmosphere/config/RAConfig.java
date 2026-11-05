package dev.rocketatmosphere.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Config de cliente: config/rocketatmosphere-client.toml */
public final class RAConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.EnumValue<QualityPreset> QUALITY;
    public static final ModConfigSpec.IntValue MAX_DISTANCE;
    public static final ModConfigSpec.DoubleValue PARTICLE_SCALE;

    public static final ModConfigSpec.BooleanValue FLAMES;
    public static final ModConfigSpec.BooleanValue TRAIL_SMOKE;
    public static final ModConfigSpec.BooleanValue GROUND_SMOKE;
    public static final ModConfigSpec.BooleanValue AIR_DENSITY;
    public static final ModConfigSpec.BooleanValue ELECTRIC;

    public static final ModConfigSpec.BooleanValue SPACE_EFFECTS;
    public static final ModConfigSpec.DoubleValue FIXED_ALTITUDE;
    public static final ModConfigSpec.BooleanValue COSMONAUTICS;

    public static final ModConfigSpec.BooleanValue SHAKE_ENABLED;
    public static final ModConfigSpec.DoubleValue SHAKE_STRENGTH;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("general");
        ENABLED = b.comment("Liga/desliga todos os efeitos.")
                .define("enabled", true);
        QUALITY = b.comment("Qualidade: LOW (Android fraco), MEDIUM, HIGH. Tambem: /rocketatmosphere quality low|medium|high")
                .defineEnum("quality", QualityPreset.MEDIUM);
        MAX_DISTANCE = b.comment("Distancia maxima (blocos) dos efeitos. 0 = usar o valor da qualidade.")
                .defineInRange("maxRenderDistance", 0, 0, 256);
        PARTICLE_SCALE = b.comment("Tamanho das nuvens de fumaca (1.0 = padrao).")
                .defineInRange("particleScale", 1.0, 0.25, 3.0);
        b.pop();

        b.push("effects");
        FLAMES = b.comment("Faiscas/chamas extras na saida dos motores.")
                .define("engineFlames", true);
        TRAIL_SMOKE = b.comment("Fumaca nos motores e rastro durante a subida.")
                .define("trailSmoke", true);
        GROUND_SMOKE = b.comment("Nuvem de fumaca espalhada no chao durante ignicao/decolagem.")
                .define("groundSmoke", true);
        ELECTRIC = b.comment("Aplica os efeitos tambem aos motores eletricos/ionicos (Vector FE, Ion, creative plasma/ion), em versao mais leve e azulada.")
                .define("electricThrusters", true);
        AIR_DENSITY = b.comment("Reduz a fumaca em ar rarefeito (usa a pressao do ar do Sable; sem fumaça no vacuo).")
                .define("fadeWithAirPressure", true);
        b.pop();

        b.push("space");
        SPACE_EFFECTS = b.comment("Ajusta os efeitos ao ambiente: no vacuo some a fumaca e a chama se abre mais.")
                .define("spaceEffects", true);
        FIXED_ALTITUDE = b.comment("Detector simples: altitude (Y) a partir da qual o foguete conta como no espaco. "
                        + "Referencias do Create: Cosmonautics: 1000 = ceu/neblina comecam a mudar; 2000 = sem neblina; "
                        + "3000 = ceu totalmente preto; 5000 = ar rarefeito (LOW_DENSITY); 20000 = transferencia para o espaco profundo. "
                        + "Tambem: /rocketatmosphere space altitude <valor>")
                .defineInRange("fixedAltitude", 3000.0, 100.0, 100000.0);
        COSMONAUTICS = b.comment("Usa a integracao com o Create: Cosmonautics quando instalado (dimensao espacial, flags de atmosfera, deslocamento de altura).")
                .define("cosmonauticsIntegration", true);
        b.pop();

        b.push("camera_shake");
        SHAKE_ENABLED = b.comment("Vibracao de camera perto de motores ligados.")
                .define("enabled", true);
        SHAKE_STRENGTH = b.comment("Intensidade da vibracao (0 = desligada, 1 = padrao, 2 = forte).")
                .defineInRange("strength", 1.0, 0.0, 2.0);
        b.pop();

        SPEC = b.build();
    }

    private RAConfig() {}
}
