package net.minecraft.world;

public enum EnumDifficulty {
    PEACEFUL("PEACEFUL", 0, 0, "options.difficulty.peaceful"),
    EASY("EASY", 1, 1, "options.difficulty.easy"),
    NORMAL("NORMAL", 2, 2, "options.difficulty.normal"),
    HARD("HARD", 3, 3, "options.difficulty.hard");
    private static final EnumDifficulty[] difficultyEnums = new EnumDifficulty[values().length];
    private final int difficultyId;
    private final String difficultyResourceKey;

    private EnumDifficulty(String p_i45312_1_, int p_i45312_2_, int difficultyIdIn, String difficultyResourceKeyIn) {
        this.difficultyId = difficultyIdIn;
        this.difficultyResourceKey = difficultyResourceKeyIn;
    }

    public int getDifficultyId() {
        return this.difficultyId;
    }

    public static EnumDifficulty getDifficultyEnum(int p_151523_0_) {
        return difficultyEnums[p_151523_0_ % difficultyEnums.length];
    }

    public String getDifficultyResourceKey() {
        return this.difficultyResourceKey;
    }

    static {
        EnumDifficulty[] var0 = values();
        int var1 = var0.length;

        for (int var2 = 0; var2 < var1; ++var2) {
            EnumDifficulty var3 = var0[var2];
            difficultyEnums[var3.difficultyId] = var3;
        }
    }
}
