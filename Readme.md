System that executes requests - simple POJO identified by a UUID and associated with a client (identified with an int).
Each request requires about 5 milliseconds to be executed. 
The service will also have to following requirements:

* 500 requests per second must be executed in _real_ time (meaning that request
execution delay cannot be queued indefinitely).
* Request associated with the same client must be executed in FIFO order and 
must be executed sequentially.
* Sometimes a request will be duplicated. Duplicated requests (request 
with the same request id) will only appear on 20 seconds windows. If duplicated 
request are received, just execute the first one.
* The method _execute_ on the interface RequestService returns a CompletableFuture
for each request. You have to decide what to return when a duplicated request 
is received.

* Request: A simple POJO with three properties: its request id, a client id and 
the instant when it was created.
* RequestExecutor: A class that _executes_ requests. It sleeps the calling thread
for a given number of seconds and stores the latency on a metric.

mvn findbugs:findbugs
mvn findbugs:gui
mvn clean install site

RequestSystem works under 500 requests per second load. Requests with the same client executed sequentially.
If duplicated request received, first one is processed, duplicated request send error message.

MetricsCollector.log

2016-12-23 05:04:21 DEBUG RequestExecutor:40 - Execution complete Request: 2016-12-23T02:04:20.947Z clientId: 73047 requestId: 5341566c-ec40-4550-8f25-9bddd28110e4
2016-12-23 05:04:21 DEBUG RequestExecutor:36 - Executing Request: 2016-12-23T02:04:20.947Z clientId: 975862 requestId: bb55e6c5-46e5-4413-bcb0-b58c247c8ce8
2016-12-23 05:04:21 DEBUG RequestExecutor:36 - Executing Request: 2016-12-23T02:04:20.947Z clientId: 485865 requestId: 5e13e7ac-4f2a-4c65-b16e-e75af14a60f6
2016-12-23 05:04:21 DEBUG RequestExecutor:40 - Execution complete Request: 2016-12-23T02:04:20.947Z clientId: 156988 requestId: 28ea13ef-42ff-4eca-9252-b9346b02395c
2016-12-23 05:04:21 DEBUG RequestExecutor:40 - Execution complete Request: 2016-12-23T02:04:20.947Z clientId: 943339 requestId: 58e34495-461b-4d1d-9327-ca6b3ca67ed4
2016-12-23 05:04:21 DEBUG RequestExecutor:36 - Executing Request: 2016-12-23T02:04:20.947Z clientId: 28172 requestId: fe91eff4-8641-43d4-8d86-1c01ede60404
2016-12-23 05:04:21 DEBUG RequestExecutor:36 - Executing Request: 2016-12-23T02:04:20.947Z clientId: 94153 requestId: 02323a5f-f24a-44fe-8223-a1583e1e5899
2016-12-23 05:04:21 DEBUG RequestExecutor:40 - Execution complete Request: 2016-12-23T02:04:20.947Z clientId: 975862 requestId: bb55e6c5-46e5-4413-bcb0-b58c247c8ce8
2016-12-23 05:04:21 DEBUG RequestExecutor:40 - Execution complete Request: 2016-12-23T02:04:20.947Z clientId: 485865 requestId: 5e13e7ac-4f2a-4c65-b16e-e75af14a60f6
2016-12-23 05:04:21 DEBUG RequestExecutor:36 - Executing Request: 2016-12-23T02:04:20.947Z clientId: 509208 requestId: 3b50414b-c0e2-448d-8acd-3fbc885d8b65
2016-12-23 05:04:21 DEBUG RequestExecutor:36 - Executing Request: 2016-12-23T02:04:20.947Z clientId: 701911 requestId: f1031115-8f95-4575-87ca-358a48cc0ddd
2016-12-23 05:04:21 DEBUG RequestExecutor:40 - Execution complete Request: 2016-12-23T02:04:20.947Z clientId: 28172 requestId: fe91eff4-8641-43d4-8d86-1c01ede60404

2016-12-23 05:06:41 WARN  OCCRequestResolver:54 - Concurrency control issue: Deducted duplicate request id of the client 6624
2016-12-23 05:06:42 WARN  OCCRequestResolver:54 - Concurrency control issue: Deducted duplicate request id of the client 6624
2016-12-23 05:06:43 WARN  OCCRequestResolver:54 - Concurrency control issue: Deducted duplicate request id of the client 6624


23.12.16 5:04:21 ===============================================================
service-latency
             count = 6497
         mean rate = 487,81 calls/second
     1-minute rate = 472,02 calls/second
     5-minute rate = 470,26 calls/second
    15-minute rate = 469,95 calls/second
               min = 0,00 milliseconds
               max = 113,00 milliseconds
              mean = 32,26 milliseconds
            stddev = 20,14 milliseconds
            median = 32,00 milliseconds
              75% <= 49,00 milliseconds
              95% <= 62,00 milliseconds
              98% <= 67,00 milliseconds
              99% <= 71,00 milliseconds
            99.9% <= 107,00 milliseconds
	
queue value = 345
	cacheSize value = 697
	
duplicates
             count = 3
         mean rate = 0,23 events/second
