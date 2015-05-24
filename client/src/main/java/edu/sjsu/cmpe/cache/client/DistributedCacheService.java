package edu.sjsu.cmpe.cache.client;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.HashMap;
import java.util.concurrent.Future;

public class DistributedCacheService implements CacheServiceInterface {
    CRDT crdt;

    // Constructor to call CRDT POJO file.
    public DistributedCacheService(String serverUrl, Integer port, Integer serviceCount) {
        crdt = new CRDT(serverUrl, port, serviceCount);
    }


    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#get(long)
     */
    @Override
    public String get(long key) {
        String value = null;
        int i = 0;
        while (i < crdt.getServiceCount()) {
            Integer port = crdt.getPort() + new Integer(i);
            ;
            String cacheUrl = crdt.getCacheServerUrl() + ":" + port.toString();
            System.out.println("HTTP GET: Server => " + cacheUrl);
            Future<HttpResponse<JsonNode>> future = Unirest.get(cacheUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key)).asJsonAsync(new Callback<JsonNode>() {
                        @Override
                        public void completed(HttpResponse<JsonNode> httpResponse) {
                            synchronized (DistributedCacheService.this) {
                                DistributedCacheService.this.crdt.getAttemptsCount().getAndIncrement();
                                if (httpResponse.getStatus() != 200) {
                                    System.out.println("\nHTTP GET failed to get value from one of the server. Checking whether repair needs to be performed or not.");
                                } else {
                                    DistributedCacheService.this.crdt.getAttemptsCount().getAndIncrement();
                                    String values = httpResponse.getBody().getObject().getString("value");
                                    int index = DistributedCacheService.this.crdt.getIndex().get();
                                    DistributedCacheService.this.crdt.setValues(values, index);
                                    DistributedCacheService.this.crdt.getIndex().getAndIncrement();
                                }
                                crdt.getResponseWaiter().countDown();
                            }
                        }

                        @Override
                        public void failed(UnirestException e) {
                            DistributedCacheService.this.crdt.getAttemptsCount().getAndIncrement();
                            System.out.println("\nHTTP GET request failed. Checking to whether repair needs to be performed.");
                            crdt.getResponseWaiter().countDown();
                        }

                        @Override
                        public void cancelled() {
                            DistributedCacheService.this.crdt.getAttemptsCount().getAndIncrement();
                            System.out.println("\nHTTP GET request Cancelled");
                            crdt.getResponseWaiter().countDown();
                        }
                    });

            i++;
        }
        try {
            crdt.getResponseWaiter().await();
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

        String repairString = getMaxReadCount();
        if (repairString != null) {
            System.out.println("Trying to perform Read-Repair of value => " + repairString+"\n");
            put(key, repairString);
        } else {
            repairString = crdt.getValues(0);
        }
        return repairString;
    }


    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#put(long,
     * java.lang.String)
     */
    @Override
    public void put(final long key, final String value) {
        HttpResponse<JsonNode> response = null;
        for (int i = 0; i < crdt.getServiceCount().intValue(); i++) {
            Integer port = crdt.getPort() + new Integer(i);

            String cacheUrl = crdt.getCacheServerUrl() + ":" + port.toString();
            System.out.println("HTTP PUT: Server => " + cacheUrl);

            Future<HttpResponse<JsonNode>> future = Unirest
                    .put(cacheUrl + "/cache/{key}/{value}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .routeParam("value", value).asJsonAsync(new Callback<JsonNode>() {
                        private void checkForSuccess() {
                            int flag = 0;
                            if (DistributedCacheService.this.crdt.getAttemptsCount().get() == DistributedCacheService.this.crdt.getServiceCount()) {
                                if (DistributedCacheService.this.crdt.getSuccessCount().get() >= DistributedCacheService.this.crdt.getWriteCount()) {
                                } else {
                                    flag = 1;
                                }
                                DistributedCacheService.this.crdt.getAttemptsCount().set(0);
                                DistributedCacheService.this.crdt.getSuccessCount().set(0);
                                if (flag == 1) {
                                    DistributedCacheService.this.delete(key);
                                }
                            }
                        }

                        @Override
                        public void completed(HttpResponse<JsonNode> httpResponse) {
                            synchronized (DistributedCacheService.this) {
                                DistributedCacheService.this.crdt.getAttemptsCount().getAndIncrement();
                                if (httpResponse.getStatus() != 200) {
                                    System.out.println("\nHTTP PUT failed to add value in cache.");
                                } else {
                                    DistributedCacheService.this.crdt.getSuccessCount().getAndIncrement();
                                }
                                checkForSuccess();
                            }
                        }

                        @Override
                        public void failed(UnirestException e) {
                            DistributedCacheService.this.crdt.getAttemptsCount().getAndIncrement();
                            System.out.println("\nHTTP PUT request failed. Wait for sometime.. checking error.");
                            checkForSuccess();
                        }

                        @Override
                        public void cancelled() {
                            DistributedCacheService.this.crdt.getAttemptsCount().getAndIncrement();
                            System.out.println("\nHTTP PUT request cancelled.");
                            checkForSuccess();
                        }
                    });

        }
    }

    /**
     * @see edu.sjsu.cmpe.cache.client.CacheServiceInterface#delete(long)
     */

    @Override
    public void delete(final long key) {
        HttpResponse<JsonNode> response = null;
        for (int i = 0; i < crdt.getServiceCount(); i++) {
            Integer port = crdt.getPort() + new Integer(i);

            String cacheUrl = crdt.getCacheServerUrl() + ":" + port.toString();
            System.out.println("HTTP DELETE: Server => " + cacheUrl);

            Future<HttpResponse<JsonNode>> future = Unirest
                    .delete(cacheUrl + "/cache/{key}")
                    .header("accept", "application/json")
                    .routeParam("key", Long.toString(key))
                    .asJsonAsync(new Callback<JsonNode>() {

                        @Override
                        public void completed(HttpResponse<JsonNode> httpResponse) {
                            if (httpResponse.getStatus() != 204) {
                                System.out.println("\nHTTP DELETE failed to delete from cache.");
                            } else {
                                DistributedCacheService.this.crdt.getSuccessCount().getAndIncrement();
                            }
                        }

                        @Override
                        public void failed(UnirestException e) {
                            System.out.println("\nHTTP DELETE request failed : UnirestException: " + e);
                        }

                        @Override
                        public void cancelled() {
                            System.out.println("\nHTTP DELETE request cancelled.");
                        }
                    });

        }
        DistributedCacheService.this.crdt.getSuccessCount().set(0);
        DistributedCacheService.this.crdt.getAttemptsCount().set(0);
    }

    private String getMaxReadCount() {
        HashMap<String, Integer> countMap = new HashMap<String, Integer>();
        int maxCount = 1;
        String rVal = crdt.getValues(0);
        for (int j = 0; j < crdt.getIndex().get(); j++) {
            if (countMap.containsKey(crdt.getValues(j))) {
                int count = countMap.get(crdt.getValues(j)).intValue();
                countMap.put(crdt.getValues(j), new Integer(count + 1));
                if (count + 1 > maxCount) {
                    rVal = crdt.getValues(j);
                    maxCount = count + 1;
                }
            } else {
                countMap.put(crdt.getValues(j), 1);
            }
        }
        if (maxCount == crdt.getServiceCount()) {
            rVal = null;
        } else if ((maxCount < crdt.getReadCount()) && (rVal != null)) {
            System.out.println("\nMajority servers failed to provide agreement. Skipping repair operation.");
            for (int j = 0; j < crdt.getIndex().get(); j++) {
                crdt.setValues(null, j);
            }
        }
        return rVal;
    }
}