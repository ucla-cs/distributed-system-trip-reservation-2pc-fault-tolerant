
This project consisted in implementing a two-phase commit fault tolerant protocol. That is, we implemented commit and abort for any transaction under the assumption that sites do fail. 

Design					
We made our data persistent by using shadowing. Each resource manager has have two files for the two versions of its data and a master record which are all be located in a specific directory for this RM. At startup of the RM, if the master record file exists, it automatically recovers, otherwise it creates new files and starts as a new RM.
			
		

The diagram above shows the general architecture of your system

The diagram above shows a general view of the our system

We used RMI to communicate between both from the clients to the Middle Ware and from the Middle Ware to the ResourceManagers. The client connects to the MiddleWare instead of directly to Resource Managers.


Now, instead of having the clients connect to a single Resource Manager, they all connect to a single MiddleWare. This MiddleWare receives all incoming requests and forwards them to the appropriate Resource Manager according to what the client wishes to do. This allows us to store Car, Flight, and Room information each in their own Resource Manager and nowhere else.

As for the customer information, the MiddleWare maintains this information (and thus becomes a sort of Resource Manager itself). This way, if a customer simply wishes to query a client, then the MiddleWare can handle it without having to bother any of the Resource Managers. Storing Customer information within the MiddleWare is also done for simplicity. As every client call must go through the MiddleWare, the Customer information is readily available for use both before or after the MiddleWare forwards a request to a Resource Manager, eliminating the need for another remote call if the Customer information was stored on its own server.


Performance Results
 The average response time if only one resource manager is involved with three update
methods : 4.53 ms
 
The way we analyse response time is by taking the time before doing all 3 update operations and the time after doing the 3 operations. We then calculate that time difference which we divide by 3 to get the average response time.  We repeat this process for 20 times and we take the average response time for all of these.
 
The average response time if all resource managers are involved (each with one update
method): 5.15 ms
 
The way we analyse the response time in this case is similar to part a). We take the time before and the time after doing the 3 operations and we calculate that time difference which we divide by 3 to get the average response time. We repeat this process 20 times and calculate the average response time for all of these.
 
The average response time if two clients execute concurrently. Each sends one transac-
tion after the other (without sleep). Each transaction has 3 update operations:  6.66964 ms
 
For part c), we calculate calculate the average response time for each Client the same way we do it in Part b), then we take the average of the response time for each client.s



How each of the individual features of the system is implemented:

Shadowing
The diagram below shows a view of the directory that is saved for each RM.
Each directory saves a master file which contains a pointer to two serialized objects, the shadow_0 and shadow_1, both of these saving the data for the resource manager.

The created a master file which is a serialized Master file object which contains a pointer to two file: the current shadow file and the old shadow file. 
Both of these are serialized versions of the RM. This way, if the RM was saving his state on shadow_1 and it crashed ( the information saved is incomplete) , it will recover from the other file shadow_0. There will be no information lost.


Algorithm in case of optimistic concurrency control
We implemented a Barrier for vote request and decision:
On commit or abort, the algorithm creates a barrier to simulate multi casting. This barrier creates 3 threads which all send a vote request or decision to all the participants at the same time. This considerably increases the speed of the system and response time of the system since the middleware doesn’t have to wait for the response of some participants in order to send another message.


How you tested the system for correctness:
We considered the following scenarios: 
Senarios :
RM Crash Scenarios (5)
1 = The RM Crash BEFORE receiving a VOTE REQUEST
2 = The RM Crash BEFORE returning a VOTE REQUEST
3 = The RM Crash AFTER returning a VOTE REQUEST
4 = The RM Crash BEFORE receiving a COMMIT ACKNOWLEDGEMENT
5 = The RM Crash AFTER receiving a COMMIT ACKNOWLEDGEMENT
RECOVERY OF RM 
MW Crash Scenarios ( 8 )
6 = Crash before sending vote request
7 = Crash after sending vote request and before receiving any replies 
8 = Crash after receiving some replies but not all : BRIDGE
9 = Crash after receiving all replies but before deciding:
10 = Crash after deciding but before sending decision
11 = Crash after sending some but not all decisions:Bridge
12 = Crash after having sent all decisions

We added two parameters to the commit command, the first parameter corresponds to the crash scenario that is described above, the second parameter is the name of the machine which will crash, ( either Flight,Car,Room or middleware) . We also injected crashes into the code corresponding to each crash scenario above.
