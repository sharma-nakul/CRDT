package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.Unirest;

public class Client {

    public static void main(String[] args) throws Exception {
        String serverURL= "http://localhost";
        int port=3000;
        int serviceCount=3;
        CacheServiceInterface cache = new DistributedCacheService(serverURL,port,serviceCount);
        System.out.println("\nStarting Cache Client...");

        // Call 1 - First HTTP PUT call to store “a” to key 1.First HTTP PUT call to store “a” to key 1.
        // Then, sleep for ~30 seconds so that you will have enough time to stop the server A.
        System.out.println("\nHTTP PUT:  Key{1} => Value{a}");
        cache.put(1, "a");
        System.out.println("sleeping for 30 seconds...");
        Thread.sleep(30000);


        // Call 2 - Second HTTP PUT call to update key 1 value to “b”.
        // Then, sleep again for another ~30 seconds while bringing the server A back.
        System.out.println("\nHTTP PUT:  Key{1} => Value{b}");
        cache.put(1, "b");
        System.out.println("sleeping for 30 seconds...");
        Thread.sleep(30000);

        // Call 3 - Final HTTP GET call to retrieve key “1” value.
        System.out.println("\nHTTP GET: Pulling Key{1} from all servers");
        String value = cache.get(1);
        System.out.println("\nHTTP GET for Key{1}: Value => " + value);

        System.out.println("\nExiting Cache Client...");
        Unirest.shutdown();
    }

}