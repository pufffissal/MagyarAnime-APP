package com.maapp.matapp;

import java.util.Arrays;
import java.util.List;

public class AdBlocker {
    private static final List<String> adUrls = Arrays.asList(
            "doubleclick.net", "ads.", "googlesyndication.com", "adservice.google.",
            "amazon-adsystem.com", "moatads.com", "scorecardresearch.com"
    ); //Ha a MA frissítni a reklámjait, addj hozzá több d.

    public static boolean isAd(String url) {
        for (String ad : adUrls) {
            if (url.contains(ad)) return true;
        }
        return false;
    }
}
