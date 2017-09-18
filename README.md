# DataServicesChallenge

## Requirements For Challenge:

* The Application must accept input from at most 5 concurrent clients on TCP/IP port 4000.
* Input lines presented to the Application via its socket must either be composed of exactly nine decimal digits (e.g.: 314159265 or  007007009) immediately followed by a server-native newline sequence; or a termination sequence as detailed in #9, below.
* Numbers presented to the Application must include leading zeros as necessary to ensure they are each 9 decimal digits.
* The log file, to be named "numbers.log”, must be created anew and/or cleared when the Application starts.
* Only numbers may be written to the log file. Each number must be followed by a server-native newline sequence.
* No duplicate numbers may be written to the log file.
* Any data that does not conform to a valid line of input should be discarded and the client connection terminated immediately and without   comment.
* Every 10 seconds, the Application must print a report to standard output:
* The difference since the last report of the count of new unique numbers that have been received.
* The difference since the last report of the count of new duplicate numbers that have been received.
* The total number of unique numbers received for this run of the Application.
* Example text for #8: Received 50 unique numbers, 2 duplicates. Unique total: 567231
* If any connected client writes a single line with only the word "terminate" followed by a server-native newline sequence, the   Application must disconnect all clients and perform a clean shutdown as quickly as possible.
* Clearly state all of the assumptions you made in completing the Application.

## Results:
```
Received 10834 unique numbers, 1910 duplicates.  Unique total: 13550
Received 11158 unique numbers, 1613 duplicates.  Unique total: 24262
Received 11485 unique numbers, 1301 duplicates.  Unique total: 35243
Received 11502 unique numbers, 1321 duplicates.  Unique total: 46178
Received 11482 unique numbers, 1318 duplicates.  Unique total: 57077
Received 11338 unique numbers, 1462 duplicates.  Unique total: 67851
Received 11387 unique numbers, 1388 duplicates.  Unique total: 78764
Received 11564 unique numbers, 1196 duplicates.  Unique total: 89921
Received 11702 unique numbers, 1099 duplicates.  Unique total: 101260
Received 11395 unique numbers, 1385 duplicates.  Unique total: 112291
```
