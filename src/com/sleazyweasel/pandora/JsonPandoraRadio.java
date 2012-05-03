package com.sleazyweasel.pandora;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sleazyweasel.applescriptifier.BadPandoraPasswordException;
import de.felixbruns.jotify.util.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonPandoraRadio implements PandoraRadio {
    private static final String ANDROID_DECRYPTION_KEY = "R=U!LH$O2B#";
    private static final String ANDROID_ENCRYPTION_KEY = "6#26FRL$ZWD";
    private static final String BLOWFISH_ECB_PKCS5_PADDING = "Blowfish/ECB/PKCS5Padding";
    private static final String BASE_URL = "https://tuner.pandora.com/services/json/?";
    private static final String ANDROID_PARTNER_PASSWORD = "AC7IBG09A3DTSYM4R41UJWL07VLN8JI7";

    private Long syncTime;
    private Long clientStartTime;
    private Integer partnerId;
    private String partnerAuthToken;
    private String userAuthToken;

    private List<Station> stations;

    @Override
    public void connect(String user, String password) throws BadPandoraPasswordException {
        clientStartTime = System.currentTimeMillis() / 1000L;
//        System.out.println("clientStartTime = " + clientStartTime);     partnerLogin();
        partnerLogin();
        userLogin(user, password);
    }

    private void userLogin(String user, String password) {
        Map<String, Object> userLoginInputs = new HashMap<String, Object>();
        userLoginInputs.put("loginType", "user");
        userLoginInputs.put("username", user);
        userLoginInputs.put("password", password);
        userLoginInputs.put("partnerAuthToken", partnerAuthToken);
        userLoginInputs.put("syncTime", getPandoraTime());
        String userLoginData = new Gson().toJson(userLoginInputs);
//        System.out.println("userLoginData = " + userLoginData);
        String encryptedUserLoginData = encrypt(userLoginData);
//        System.out.println("encryptedUserLoginData = " + encryptedUserLoginData);
        String urlEncodedPartnerAuthToken = urlEncode(partnerAuthToken);

        String userLoginUrl = String.format(BASE_URL + "method=auth.userLogin&auth_token=%s&partner_id=%d", urlEncodedPartnerAuthToken, partnerId);
//        System.out.println("userLoginUrl = " + userLoginUrl);
        JsonObject jsonElement = doPost(userLoginUrl, encryptedUserLoginData).getAsJsonObject();
        String loginStatus = jsonElement.get("stat").getAsString();
//        System.out.println("loginStatus = " + loginStatus);
        if (loginStatus.equals("ok")) {
            JsonObject userLoginResult = jsonElement.get("result").getAsJsonObject();
            userAuthToken = userLoginResult.get("userAuthToken").getAsString();
//            System.out.println("userAuthToken.getAsString() = " + userAuthToken.getAsString());
        }
        else {
            throw new BadPandoraPasswordException();
//            System.out.println("loginStatus = " + loginStatus);
        }
    }

    private String urlEncode(String f) {
        try {
            return URLEncoder.encode(f, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("This better not happen, because ISO-8859-1 is a valid encoding", e);
        }
    }

    private long getPandoraTime() {
        return syncTime + ((System.currentTimeMillis() / 1000) - clientStartTime);
    }

    private void partnerLogin() {
        JsonElement partnerLoginData = doPartnerLogin();
//        System.out.println("parse array = " + partnerLoginData.isJsonArray());
//        System.out.println("parse object= " + partnerLoginData.isJsonObject());
        JsonObject asJsonObject = partnerLoginData.getAsJsonObject();
        JsonElement stat = asJsonObject.get("stat");
//        System.out.println("stat.getAsString() = " + stat.getAsString());
        JsonObject result = asJsonObject.getAsJsonObject("result");
        String encryptedSyncTime = result.get("syncTime").getAsString();
//        System.out.println("syncTime = " + encryptedSyncTime);
        partnerAuthToken = result.get("partnerAuthToken").getAsString();
        syncTime = Long.valueOf(decrypt(encryptedSyncTime));
        partnerId = result.get("partnerId").getAsInt();
    }

    @Override
    public void sync() {
        //don't think we need to do this, since it's a part of the core json APIs.
    }

    @Override
    public void disconnect() {
        syncTime = null;
        clientStartTime = null;
        partnerId = null;
        partnerAuthToken = null;
        userAuthToken = null;
        stations = null;
    }

    @Override
    public List<Station> getStations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Station getStationById(long sid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rate(Song song, boolean rating) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAlive() {
        throw new UnsupportedOperationException();
    }

    private static JsonElement doPartnerLogin() {
        String partnerLoginUrl = BASE_URL + "method=auth.partnerLogin";
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("username", "android");
        data.put("password", ANDROID_PARTNER_PASSWORD);
        data.put("deviceModel", "android-generic");
        data.put("version", "5");
        data.put("includeUrls", true);
        String stringData = new Gson().toJson(data);
//        System.out.println("stringData = " + stringData);

        return doPost(partnerLoginUrl, stringData);
    }

    private static JsonElement doPost(String urlInput, String stringData) {
        try {
            URL url = new URL(urlInput);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            setRequestHeaders(urlConnection);

            urlConnection.setRequestProperty("Content-length", String.valueOf(stringData.length()));
            urlConnection.connect();
            DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());

            out.writeBytes(stringData);
            out.flush();
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
    //            System.out.println("line = " + line);
                JsonParser parser = new JsonParser();
                return parser.parse(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Pandora", e);
        }
        throw new RuntimeException("Failed to get a response from Pandora");
    }


    private static void setRequestHeaders(HttpsURLConnection conn) {
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Accept", "*/*");
    }

    private static String encrypt(String input)  {
        try {
            Cipher encryptionCipher = Cipher.getInstance(BLOWFISH_ECB_PKCS5_PADDING);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, new Key() {
                @Override
                public String getAlgorithm() {
                    return "Blowfish";
                }

                @Override
                public String getFormat() {
                    return "RAW";
                }

                @Override
                public byte[] getEncoded() {
                    return ANDROID_ENCRYPTION_KEY.getBytes();
                }
            });
            byte[] bytes = encryptionCipher.doFinal(input.getBytes());
            return Hex.toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to properly encrypt data", e);
        }
    }

    private static String decrypt(String input) {
        byte[] result = new byte[0];
        try {
            Cipher decryptionCipher = Cipher.getInstance(BLOWFISH_ECB_PKCS5_PADDING);

            decryptionCipher.init(Cipher.DECRYPT_MODE, new Key() {
                @Override
                public String getAlgorithm() {
                    return "Blowfish";
                }

                @Override
                public String getFormat() {
                    return "RAW";
                }

                @Override
                public byte[] getEncoded() {
                    return ANDROID_DECRYPTION_KEY.getBytes();
                }
            });

            result = decryptionCipher.doFinal(Hex.toBytes(input));
        } catch (Exception e) {
            throw new RuntimeException("Failed to properly decrypt data", e);
        }

        byte[] chopped = new byte[result.length - 4];
        System.arraycopy(result, 4, chopped, 0, chopped.length);

        return new String(chopped);
    }
}
