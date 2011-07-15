package com.sleazyweasel.applescriptifier;

public enum Application {
    AIRFOIL("Airfoil", "Airfoil", "com.rogueamoeba.Airfoil", true, false, false, false, false, false),
    PANDORABOY("PandoraBoy", "PandoraBoy", "com.frozensilicon.PandoraBoy", true, true, true, false, true, true),
    PULSAR("Pulsar", "Pulsar", "com.rogueamoeba.Pulsar", false, true, true, true, false, false),
    ITUNES("iTunes", "iTunes", "com.apple.iTunes", false, true, true, true, false, false),
    PANDORAONE("Pandora", "Pandora", "com.pandora.desktop.FB9956FD96E03239939108614098AD95535EE674.1", false, true, true, false, true, true),
    RDIO("Rdio", "Rdio", "com.rdio.desktop", false, true, true, true, false, false),
    MUSECONTROLLER("Muse Controller", "Muse Controller", "com.sleazyweasel.MuseController", true, true, true, false, true, true),
    SPOTIFY("Spotify", "Spotify", "com.spotify.client", false, true, true, true, false, false),
    OTHER("Other", "Other", "unknown", false, false, false, false, false, false);

    private String displayName;
    private String name;
    private String identifier;
    private boolean fullSupport;
    private boolean playPauseSupport;
    private boolean nextSupport;
    private boolean previousSupport;
    private boolean thumbsUpSupport;
    private boolean thumbsDownSupport;

    Application(String name, String displayName, String identifier, boolean fullSupport, boolean playPauseSupport, boolean nextSupport, boolean previousSupport, boolean thumbsUpSupport, boolean thumbsDownSupport) {
        this.name = name;
        this.displayName = displayName;
        this.identifier = identifier;
        this.fullSupport = fullSupport;
        this.playPauseSupport = playPauseSupport;
        this.nextSupport = nextSupport;
        this.previousSupport = previousSupport;
        this.thumbsUpSupport = thumbsUpSupport;
        this.thumbsDownSupport = thumbsDownSupport;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean hasFullSupport() {
        return fullSupport;
    }

    public boolean hasPlayPauseSupport() {
        return playPauseSupport;
    }

    public boolean hasNextSupport() {
        return nextSupport;
    }

    public boolean hasPreviousSupport() {
        return previousSupport;
    }

    public boolean hasThumbsUpSupport() {
        return thumbsUpSupport;
    }

    public boolean hasThumbsDownSupport() {
        return thumbsDownSupport;
    }

    public ApplicationSupport getApplicationSupport(AppleScriptTemplate appleScriptTemplate, NativePianobarSupport pianobarSupport) {
        switch (this) {
            case PANDORABOY:
                return new PandoraBoySupport(appleScriptTemplate);
            case PULSAR:
                return new PulsarSupport(appleScriptTemplate);
            case ITUNES:
                return new ITunesSupport(appleScriptTemplate);
            case PANDORAONE:
                return new PandoraOneSupport(appleScriptTemplate);
            case RDIO:
                return new RdioSupport(appleScriptTemplate);
            case MUSECONTROLLER:
                return pianobarSupport;
            case SPOTIFY:
                return new SpotifySupport(appleScriptTemplate);
            default:
                return null;
        }
    }

    public static Application forName(String name) {
        for (Application application : values()) {
            if (application.getName().equalsIgnoreCase(name)) {
                return application;
            }
        }
        return OTHER;
    }

}
