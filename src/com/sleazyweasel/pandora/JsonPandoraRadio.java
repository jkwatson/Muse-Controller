package com.sleazyweasel.pandora;

import com.google.gson.*;
import com.sleazyweasel.applescriptifier.BadPandoraPasswordException;
import de.felixbruns.jotify.util.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonPandoraRadio implements PandoraRadio {
    private static final String ANDROID_DECRYPTION_KEY = "R=U!LH$O2B#";
    private static final String ANDROID_ENCRYPTION_KEY = "6#26FRL$ZWD";
    private static final String BLOWFISH_ECB_PKCS5_PADDING = "Blowfish/ECB/PKCS5Padding";
    private static final String BASE_URL = "https://tuner.pandora.com/services/json/?";
    private static final String BASE_NON_TLS_URL = "http://tuner.pandora.com/services/json/?";
    private static final String ANDROID_PARTNER_PASSWORD = "AC7IBG09A3DTSYM4R41UJWL07VLN8JI7";

    private Long syncTime;
    private Long clientStartTime;
    private Integer partnerId;
    private String partnerAuthToken;
    private String userAuthToken;
    private Long userId;

    private List<Station> stations;

    @Override
    public void connect(String user, String password) throws BadPandoraPasswordException {
        clientStartTime = System.currentTimeMillis() / 1000L;
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
        String encryptedUserLoginData = encrypt(userLoginData);
        String urlEncodedPartnerAuthToken = urlEncode(partnerAuthToken);

        String userLoginUrl = String.format(BASE_URL + "method=auth.userLogin&auth_token=%s&partner_id=%d", urlEncodedPartnerAuthToken, partnerId);
        JsonObject jsonElement = doPost(userLoginUrl, encryptedUserLoginData).getAsJsonObject();
        String loginStatus = jsonElement.get("stat").getAsString();
        if ("ok".equals(loginStatus)) {
            JsonObject userLoginResult = jsonElement.get("result").getAsJsonObject();
            userAuthToken = userLoginResult.get("userAuthToken").getAsString();
            userId = userLoginResult.get("userId").getAsLong();
        } else {
            throw new BadPandoraPasswordException();
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
        JsonObject asJsonObject = partnerLoginData.getAsJsonObject();
        checkForError(asJsonObject, "Failed at Partner Login");
        JsonObject result = asJsonObject.getAsJsonObject("result");
        String encryptedSyncTime = result.get("syncTime").getAsString();
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
        JsonObject result = doStandardCall("user.getStationList", new HashMap<String, Object>(), false);
        checkForError(result, "Failed to get Stations");
        JsonArray stationArray = result.get("result").getAsJsonObject().getAsJsonArray("stations");
        stations = new ArrayList<Station>();
        for (JsonElement jsonStationElement : stationArray) {
            JsonObject jsonStation = jsonStationElement.getAsJsonObject();

            String stationId = jsonStation.get("stationId").getAsString();
            String stationIdToken = jsonStation.get("stationToken").getAsString();
            boolean isQuickMix = jsonStation.getAsJsonPrimitive("isQuickMix").getAsBoolean();
            String stationName = jsonStation.get("stationName").getAsString();
            stations.add(new Station(stationId, stationIdToken, false, isQuickMix, stationName));
        }
        return stations;
    }

    private JsonObject doStandardCall(String method, Map<String, Object> postData, boolean useSsl) {
        String url = String.format((useSsl ? BASE_URL : BASE_NON_TLS_URL) + "method=%s&auth_token=%s&partner_id=%d&user_id=%s", method, urlEncode(userAuthToken), partnerId, userId);
        System.out.println("url = " + url);
        postData.put("userAuthToken", userAuthToken);
        postData.put("syncTime", getPandoraTime());
        String jsonData = new Gson().toJson(postData);
        System.out.println("jsonData = " + jsonData);
        return doPost(url, encrypt(jsonData)).getAsJsonObject();
    }

    @Override
    public Station getStationById(long sid) {
        if (stations == null) {
            getStations();
        }
        for (Station station : stations) {
            if (sid == station.getId()) {
                return station;
            }
        }
        return null;
    }

    @Override
    public void rate(Song song, boolean rating) {
        String method = "station.addFeedback";
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("trackToken", song.getTrackToken());
        data.put("isPositive", rating);
        JsonObject ratingResult = doStandardCall(method, data, false);
        checkForError(ratingResult, "failed to rate song");
    }

    @Override
    public boolean isAlive() {
        return userAuthToken != null;
    }

    @Override
    public Song[] getPlaylist(Station station, String format) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("stationToken", station.getStationIdToken());
        data.put("additionalAudioUrl", "HTTP_192_MP3"); // Totally retarded that Pandora lets me get this url, even though the user doesn't pay for the service.
        JsonObject songResult = doStandardCall("station.getPlaylist", data, true);
        checkForError(songResult, "Failed to get playlist from station");

        JsonArray songsArray = songResult.get("result").getAsJsonObject().get("items").getAsJsonArray();
        List<Song> results = new ArrayList<Song>();
        for (JsonElement songElement : songsArray) {
            JsonObject songData = songElement.getAsJsonObject();
            //it is completely retarded that pandora leaves this up to the client. Come on, Pandora! Use your brains!
            if (songData.get("adToken") != null) {
                continue;
            }
            String album = songData.get("albumName").getAsString();
            String artist = songData.get("artistName").getAsString();
            String audioUrl = songData.get("additionalAudioUrl").getAsString();

            String title = songData.get("songName").getAsString();
            String albumDetailUrl = songData.get("albumDetailUrl").getAsString();
            String artRadio = songData.get("albumArtUrl").getAsString();
            String trackToken = songData.get("trackToken").getAsString();

            Integer rating =songData.get("songRating").getAsInt();
            results.add(new Song(album, artist, audioUrl, station.getStationId(), title, albumDetailUrl, artRadio, trackToken, rating));
        }
        return results.toArray(new Song[results.size()]);
    }

    private void checkForError(JsonObject songResult, String errorMessage) {
        String stat = songResult.get("stat").getAsString();
        if (!"ok".equals(stat)) {
            throw new RuntimeException(errorMessage);
        }
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

        return doPost(partnerLoginUrl, stringData);
    }

    private static JsonElement doPost(String urlInput, String stringData) {
        try {
            URL url = new URL(urlInput);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("response = " + line);
                JsonParser parser = new JsonParser();
                return parser.parse(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to Pandora", e);
        }
        throw new RuntimeException("Failed to get a response from Pandora");
    }


    private static void setRequestHeaders(HttpURLConnection conn) {
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)");
        conn.setRequestProperty("Content-Type", "text/plain");
        conn.setRequestProperty("Accept", "*/*");
    }

    private static String encrypt(String input) {
        try {
            Cipher encryptionCipher = Cipher.getInstance(BLOWFISH_ECB_PKCS5_PADDING);
            encryptionCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ANDROID_ENCRYPTION_KEY.getBytes(), "Blowfish"));
            byte[] bytes = encryptionCipher.doFinal(input.getBytes());
            return Hex.toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to properly encrypt data", e);
        }
    }

    private static String decrypt(String input) {
        byte[] result;
        try {
            Cipher decryptionCipher = Cipher.getInstance(BLOWFISH_ECB_PKCS5_PADDING);
            decryptionCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(ANDROID_DECRYPTION_KEY.getBytes(), "Blowfish"));
            result = decryptionCipher.doFinal(Hex.toBytes(input));
        } catch (Exception e) {
            throw new RuntimeException("Failed to properly decrypt data", e);
        }

        byte[] chopped = new byte[result.length - 4];
        System.arraycopy(result, 4, chopped, 0, chopped.length);

        return new String(chopped);
    }
}
