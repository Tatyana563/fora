package com.example.fora;

public class Test6 {
    public static void main(String[] args) {
        String itemUrl=  "  https://fora.kz/catalog/komplektuusie/bloki-pitania/karaganda";
       // int index1 = itemUrl.lastIndexOf("_");
        int index2 = itemUrl.lastIndexOf("/");
        String substring = itemUrl.substring(0,index2);
        System.out.println(substring);
    }
}
