### What,Why and How?

    This repo is set for personal use only,I have beening willing to build my own chating system long time ago.Recently,I bought a cloud machine from Tencent cloud.
A mini server which implemented websocket protocol is listening request within java vm.The original server side code is from https://github.com/TooTallNate/Java-WebSocket/tree/master/src/main/example/ChatServer.java with tiny modification.It parses request and forward or broadcast messages to different client.The app is incharge of writing and displaying messages from others.

#### Setting up your server.

1.Make sure java(jdk 1.8 or later) can be searched by your system.

2.Then put https://github.com/TooTallNate/Java-WebSocket/releases/download/v1.4.1/Java-WebSocket-1.4.1-with-dependencies.jar,rename as jwd1.4.1.jar and move it into your class_path.

3.Modify your test/deploy ip address and corresponding port numpber in `cs/src/ChatUtils.java`

4.Open terminal and navigate to path `cs` ,type `make`

#### Setting up ChatClient (PC)

1.Configure server address and port number in `cs/src/ChatClient.java`.

2.Put jwd.jar within CLASS_PATH,

3.Open terminal and navigate to path `cs` ,type `make c`

4.Double click `client.bat`
