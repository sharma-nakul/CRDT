cmpe273-lab4
============

CMPE 273 Lab 4 - POC on CRDT

Client Changes: 
1. CRDT.java - POJO class to store in-memory data
2. DistributedCacheService.java 
     - Perform HTTP GET, HTTP PUT and HTTP DELETE operations.
     - Read-Repair operation
3. CacheServiceInterface.java 
     - Added delete method interface.
4. Client.java 
     - Perform Call 1 - First HTTP PUT call to store “a” to key 1.First HTTP PUT call to store “a” to key 1.
                        Then, sleep for ~30 seconds so that you will have enough time to stop the server A.
     - Perform Call 2 - Second HTTP PUT call to update key 1 value to “b”.
                        Then, sleep again for another ~30 seconds while bringing the server A back.
     - Perform Call 3 - Final HTTP GET call to retrieve key “1” value.       


Server Changes:
1. CacheResource.java - Added HTTP DELETE 
2. CacheInterface.java - Added interface to for Delete operation.
3. InMemoryCache.java - Added code to support DELETE method.


