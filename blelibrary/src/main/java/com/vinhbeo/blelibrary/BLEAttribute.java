package com.vinhbeo.blelibrary;

import java.util.HashMap;

public class BLEAttribute {

    private static HashMap<String, String> attributes = new HashMap<String, String>();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";


    /*DSPS*/

    public static String BLE_SHIELD_SERVICE = "0783b03e-8535-b5a0-7140-a304d2495cb7";
    public static String BLE_SHIELD_TX =      "0783b03e-8535-b5a0-7140-a304d2495cb8";
    public static String BLE_SHIELD_RX =      "0783b03e-8535-b5a0-7140-a304d2495cba";


//    /*Mi band 3*/
//    public static String BLE_SHIELD_SERVICE = "0000fee0-0000-1000-8000-00805f9b34fb";
//    public static String BLE_SHIELD_TX =      "00002a2b-0000-1000-8000-00805f9b34fb";
//    public static String BLE_SHIELD_RX =      "0783b03e-8535-b5a0-7140-a304d2495cba";

    static {
        // RBL Services.
        attributes.put(BLE_SHIELD_SERVICE, "BLE Shield Service");
        // RBL Characteristics.
        attributes.put(BLE_SHIELD_TX, "BLE Shield TX");
        attributes.put(BLE_SHIELD_RX, "BLE Shield RX");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

}
