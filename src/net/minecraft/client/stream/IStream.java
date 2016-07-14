package net.minecraft.client.stream;

import tv.twitch.ErrorCode;
import tv.twitch.broadcast.IngestServer;
import tv.twitch.chat.ChatUserInfo;

public interface IStream {
    /**
     * Shuts down a steam
     */
    void shutdownStream();

    void func_152935_j();

    void func_152922_k();

    boolean func_152936_l();

    boolean isReadyToBroadcast();

    boolean isBroadcasting();

    void func_152911_a(Metadata var1, long var2);

    void func_176026_a(Metadata var1, long var2, long var4);

    boolean isPaused();

    void requestCommercial();

    /**
     * pauses a stream
     */
    void pause();

    /**
     * unpauses a stream
     */
    void unpause();

    void updateStreamVolume();

    void func_152930_t();

    void stopBroadcasting();

    IngestServer[] func_152925_v();

    void func_152909_x();

    IngestServerTester func_152932_y();

    boolean func_152908_z();

    int func_152920_A();

    boolean func_152927_B();

    String func_152921_C();

    ChatUserInfo func_152926_a(String var1);

    void func_152917_b(String var1);

    boolean func_152928_D();

    ErrorCode func_152912_E();

    boolean func_152913_F();

    /**
     * mutes or unmutes the microphone based on the boolean parameter passed into the method
     */
    void muteMicrophone(boolean var1);

    boolean func_152929_G();

    IStream.AuthFailureReason func_152918_H();

    public static enum AuthFailureReason {
        ERROR("ERROR", 0),
        INVALID_TOKEN("INVALID_TOKEN", 1);

        private AuthFailureReason(String p_i1014_1_, int p_i1014_2_) {
        }
    }
}