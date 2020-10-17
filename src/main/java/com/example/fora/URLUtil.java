package com.example.fora;

public final class URLUtil {

    private URLUtil() {
        // Only static methods
    }

    public static String removeCityFromUrl(String rawUrl) {
        int index = rawUrl.lastIndexOf("/");
        return rawUrl.substring(0,index);
    }

    public static String extractExternalIdFromUrl(String rawUrl) {
        //  String line=  "https:fora.kz/catalog/smartfony-plansety/smartfony/samsung-galaxy-a01-core-red_616857/karaganda";
        int index1 = rawUrl.lastIndexOf("_");
        int index2 = rawUrl.lastIndexOf("/");
        return rawUrl.substring(index1+1,index2);
    }

}

