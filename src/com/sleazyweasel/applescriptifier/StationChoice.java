package com.sleazyweasel.applescriptifier;

class StationChoice {
    private final Integer key;
    private final String stationName;

    public StationChoice(Integer key, String stationName) {
        this.key = key;
        this.stationName = stationName;
    }

    public Integer getKey() {
        return key;
    }

    public String getStationName() {
        return stationName;
    }

    @Override
    public String toString() {
        //todo I've never felt so dirty in my life, using toString as a renderer. Please make this better!
        return stationName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StationChoice that = (StationChoice) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (stationName != null ? !stationName.equals(that.stationName) : that.stationName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (stationName != null ? stationName.hashCode() : 0);
        return result;
    }
}
