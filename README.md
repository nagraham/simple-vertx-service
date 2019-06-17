
# Simple Vertx Service

A simple REST API built using Vert.x to play around and practice using the framework and to use as a learning reference for future projects.

Right now, project includes:

- An HTTP server exposing two RESTful actions (GET user and PUT user)
- A database Vert.x service (using HSQLDB)
- Unit tests for the database and HTTP request handlers
- Integ tests for the database service
- System tests using a local server

### To deploy and run

Step 1: Generate the fat JAR

```
maven clean package
```

Step 2: Run the app from the command line

```
java -jar target/simple-vertx-service-1.0-SNAPSHOT-fat.jar
```

Step 3: Have fun!

```

// try getting a non-existant use
curl http://localhost:8080/user/user1234; echo
{"success":false,"error":"User for id [user1234] not found"}

// let's create that user!
curl -d '{"uuid":"user1234", "name": "Han Solo", "age": 32}' -H "Content-Type: application/json" -X PUT http://localhost:8080/user/user1234; echo
{"success":true}

// user1234 exists now!
curl http://localhost:8080/user/user1234; echo
{"success":true,"user":{"uuid":"user1234","name":"Han Solo","age":32}}
```