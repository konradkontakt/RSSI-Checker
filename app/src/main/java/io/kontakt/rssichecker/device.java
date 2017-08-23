package io.kontakt.rssichecker;

/**
 * Created by Admin on 23.08.2017.
 */

class device {
    static boolean safeStatus;
    static boolean dangerStatus;
    static void isInSafeZone(){
        safeStatus = true;
    }
    static void isInDangerZone(){
        dangerStatus = true;
    }
}
