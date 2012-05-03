package com.sleazyweasel.pandora;/* Pandoroid Radio - open source pandora.com client for android
 * Copyright (C) 2011  Andrew Regner <andrew@aregner.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;


public class Station implements Comparable<Station>, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String idToken;
    private boolean isCreator;
    private boolean isQuickMix;
    private String name;

    transient private boolean useQuickMix;

    public Station(HashMap<String, Object> data) {
        id = (String) data.get("stationId");
        idToken = (String) data.get("stationIdToken");
        isCreator = (Boolean) data.get("isCreator");
        isQuickMix = (Boolean) data.get("isQuickMix");
        name = (String) data.get("stationName");

        useQuickMix = false;
    }

    public Station(String id, String idToken, boolean creator, boolean quickMix, String name) {
        this.id = id;
        this.idToken = idToken;
        isCreator = creator;
        isQuickMix = quickMix;
        this.name = name;
    }

    public long getId() {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            return id.hashCode();
        }
    }

    public String getName() {
        return name;
    }

    public int compareTo(Station another) {
        return getName().compareTo(another.getName());
    }

    public boolean equals(Station another) {
        return getName().equals(another.getName());
    }

    public String getStationId() {
        return id;
    }

    public String getStationIdToken() {
        return idToken;
    }

    public boolean isCreator() {
        return isCreator;
    }

    public boolean isQuickMix() {
        return isQuickMix;
    }

    @Override
    public String toString() {
        return name;
    }
}
