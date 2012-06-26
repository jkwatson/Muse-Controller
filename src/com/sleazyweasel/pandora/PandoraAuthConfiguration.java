package com.sleazyweasel.pandora;

public class PandoraAuthConfiguration {
    static final PandoraAuthConfiguration ANDROID_CONFIG = new PandoraAuthConfiguration(PandoraAuthConfiguration.HOST, PandoraAuthConfiguration.ANDROID_PARTNER_USERNAME,
            PandoraAuthConfiguration.ANDROID_PARTNER_PASSWORD, PandoraAuthConfiguration.ANDROID_ENCRYPTION_KEY, PandoraAuthConfiguration.ANDROID_DECRYPTION_KEY, "android-generic");
    static final PandoraAuthConfiguration PANDORAONE_CONFIG = new PandoraAuthConfiguration(PandoraAuthConfiguration.PANDORA_ONE_HOST, PandoraAuthConfiguration.PANDORAONE_USERNAME,
            PandoraAuthConfiguration.PANDORAONE_PARTNER_PASSWORD, PandoraAuthConfiguration.PANDORAONE_ENCRYPTION_KEY, PandoraAuthConfiguration.PANDORAONE_DECRYPTION_KEY, "D01");

    private static final String ANDROID_PARTNER_USERNAME = "android";
    private static final String ANDROID_DECRYPTION_KEY = "R=U!LH$O2B#";
    private static final String ANDROID_ENCRYPTION_KEY = "6#26FRL$ZWD";
    private static final String ANDROID_PARTNER_PASSWORD = "AC7IBG09A3DTSYM4R41UJWL07VLN8JI7";

    private static final String PANDORAONE_USERNAME = "pandora one";
    private static final String PANDORAONE_DECRYPTION_KEY = "U#IO$RZPAB%VX2";
    private static final String PANDORAONE_ENCRYPTION_KEY = "2%3WCL*JU$MP]4";
    private static final String PANDORAONE_PARTNER_PASSWORD = "TVCKIBGS9AO9TSYLNNFUML0743LH82D";

    private static final String PANDORA_ONE_HOST = "internal-tuner.pandora.com";

    private static final String HOST = "tuner.pandora.com";
    private static final String BASE_URL = "https://tuner.pandora.com/services/json/?";

    private static final String BASE_NON_TLS_URL = "http://tuner.pandora.com/services/json/?";


    private final String baseUrl;
    private final String nonTlsBaseUrl;
    private final String encryptionKey;
    private final String decryptionKey;
    private final String password;
    private final String userName;
    private final String deviceModel;

    public PandoraAuthConfiguration(String host, String userName, String password, String encryptionKey, String decryptionKey, String deviceModel) {
        this.deviceModel = deviceModel;
        this.baseUrl = "https://" + host + "/services/json/?";
        this.nonTlsBaseUrl = "http://" + host + "/services/json/?";
        this.userName = userName;
        this.password = password;
        this.encryptionKey = encryptionKey;
        this.decryptionKey = decryptionKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getNonTlsBaseUrl() {
        return nonTlsBaseUrl;
    }

    public String getEncryptionKey() {
        return encryptionKey;
    }

    public String getDecriptionKey() {
        return decryptionKey;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public String getDeviceModel() {
        return deviceModel;
    }
}
