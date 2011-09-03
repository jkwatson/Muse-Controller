package com.sleazyweasel.applescriptifier;

import java.util.concurrent.Semaphore;

public class Main2 {

    public static void main(String[] args) throws Exception {
        Semaphore semaphore = new Semaphore(1);
        System.out.println(semaphore.tryAcquire());
        System.out.println(semaphore.tryAcquire());
        System.out.println(semaphore.tryAcquire());
        System.out.println(semaphore.tryAcquire());
        System.out.println(semaphore.tryAcquire());
        System.out.println(semaphore.tryAcquire());
//        String property = System.getProperty("java.library.path");
//        System.out.println("property = " + property);
//        Native.loadLibrary(libspotify.LibspotifyLibrary.class);
////        System.loadLibrary("sparkle_init");
    }

}
