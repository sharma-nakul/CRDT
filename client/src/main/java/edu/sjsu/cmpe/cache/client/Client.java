package edu.sjsu.cmpe.cache.client;

import java.util.Map;

public class Client {

    public static void main(String[] args) throws Exception {
        CRDT crdtClient = new CRDT();
        int key = 1;
        int successCount = crdtClient.put(key, "b");
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }


        // Checking majority of servers for the same value.
        if (successCount <= 2) {
            int deletValue = crdtClient.delete(key);
            System.out.println("Delete Value => " + deletValue + " having key => " + key);
        }


        System.out.println("\nStarting Cache Client...");
        CacheServiceInterface cache = new DistributedCacheService("http://localhost:3000");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }


        Map<String, Integer> getMap = crdtClient.get(key);
        if (getMap.containsValue(2)) {
            crdtClient.put(key, "b");
        }

        System.out.println("\nValue has been replicated in " + successCount + " servers");
        System.out.println("\nExisting Cache Client...");
    }

}
