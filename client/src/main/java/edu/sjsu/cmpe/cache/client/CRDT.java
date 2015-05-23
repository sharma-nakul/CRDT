package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class CRDT {

    private ArrayList<String> servers;
    int code;
    public AtomicInteger successCount;
    public AtomicInteger getCount;
    Map<String, Integer> map;

    //Adding servers in the list.
    public CRDT() {
        servers = new ArrayList<String>(3);
        servers.add("http://localhost:3000");
        servers.add("http://localhost:3001");
        servers.add("http://localhost:3002");
    }

    /*-------------------Performing PUT Operation------------------------------------------*/
    public int put(long key, String value) {
        successCount = new AtomicInteger();
        for (final String serverUrl : servers) {
            try {
                Thread.sleep(100);
                Future<HttpResponse<JsonNode>> future = Unirest.put(serverUrl + "/cache/{key}/{value}")
                        .header("accept", "application/json")
                        .routeParam("key", Long.toString(key))
                        .routeParam("value", value)
                        .asJsonAsync(new Callback<JsonNode>() {
                            public void failed(UnirestException e) {
                                System.out.println("The request has failed for Post =>" + serverUrl);
                            }

                            public void completed(HttpResponse<JsonNode> response) {
                                code = response.getStatus();
                                System.out.println("\nResponse code for PUT => " + code);
                                if (code == 200) {
                                    successCount.incrementAndGet();
                                    System.out.println("PUT request success count => " + successCount);
                                }
                            }
                            public void cancelled() {
                                System.out.println("The request has been cancelled");
                            }
                        });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return (int) successCount.floatValue();
    }


    /*-------------------Performing GET Operation------------------------------------------*/
    public Map<String, Integer> get(long key) {
        getCount = new AtomicInteger();
        map = new HashMap<String, Integer>();
        for (final String serverUrl : servers) {
            try {
                Thread.sleep(150);
                // Asynchronous call
                Future<HttpResponse<JsonNode>> future = Unirest.get(serverUrl + "/cache/{key}")
                        .header("accept", "application/json")
                        .routeParam("key", Long.toString(key))
                        .asJsonAsync(new Callback<JsonNode>() {

                            public void failed(UnirestException e) {
                                System.out.println("The request has failed");
                            }

                            public void completed(HttpResponse<JsonNode> response) {
                                code = response.getStatus();
                                //String value = response.getBody().getObject().getString("value");
                                System.out.println("GET Request completed => Server: " + serverUrl + " & Code: " + code);
                                if (code == 200) {
                                    int tempVar = getCount.incrementAndGet();
                                    map.put(response.getBody().getObject().getString("value"), tempVar);
                                    System.out.println("GET request success count => " + successCount + " & Map value => " + map.values());
                                }
                            }

                            public void cancelled() {
                                System.out.println("The request has been cancelled");
                            }
                        });
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return map;
    }


    /*-------------------Performing DELETE Operation------------------------------------------*/
    public int delete(long key) {
        for (final String serverUrl : servers) {
            Future<HttpResponse<JsonNode>> future = Unirest.delete(serverUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .asJsonAsync(new Callback<JsonNode>() {
                        public void failed(UnirestException e) {
                            System.out.println("Delete request failed on => " + serverUrl);
                        }

                        public void completed(HttpResponse<JsonNode> response) {
                            code = response.getStatus();
                            //String value = response.getBody().getObject().getString("value");
                            System.out.println("Delete request completed: Code => " + code);
                            if (code == 201) {
                                successCount.incrementAndGet();
                                System.out.println("DELETE success count => " + successCount);
                            }
                        }

                        public void cancelled() {
                            System.out.println("The request has been cancelled");
                        }
                    });
        }
        return (int) successCount.floatValue();
    }
}