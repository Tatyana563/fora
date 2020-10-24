package com.example.fora;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test4 {

    public static void main(String[] args) {
        getProductExternalId("https:fora.kz/catalog/smartfony-plansety/smartfony/samsung-galaxy-a01-core-red_616857/karaganda");
    }

    private static String getProductExternalId(String itemUrl) {

    //  String line=  "https:fora.kz/catalog/smartfony-plansety/smartfony/samsung-galaxy-a01-core-red_616857/karaganda";
        int index1 = itemUrl.lastIndexOf("_");
        int index2 = itemUrl.lastIndexOf("/");
        String substring = itemUrl.substring(index1+1,index2);
        System.out.println(substring);
      return substring;
    }
}
