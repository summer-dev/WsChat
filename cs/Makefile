JAVAC=javac
JAVACX=-Xbootclasspath/a:lib/jwd.jar -Xlint:unchecked
JAVA=java
JAVAX=-Xbootclasspath/a:lib/jwd.jar
SRC=src/
BUILD=build/
RES=res/

JAR=jar
JARX=-cfm
#all: s c client server deploy
all: s c deploy

s: $(SRC)ChatServer.java  $(SRC)ChatUtils.java
	$(JAVAC) $(JAVACX) $^
	
c: $(SRC)ChatClient.java $(SRC)ChatUtils.java
	$(JAVAC) $(JAVACX) $^

client:  $(RES)client.mf
#	java -Xbootclasspath/a:lib/jwd.jar -cp build ChatClient
#	jar $(JARX) $(BUILD)client.jar $^ $(BUILD)Chat*.class
	mv $(SRC)*.class $(BUILD)
	jar -cfm client.jar res/client.mf $(BUILD)Chat*.class
	
server:  $(RES)server.mf
	jar $(JARX) server.jar $^ $(BUILD)Chat*.class
	
deploy:
	$(JAVA) $(JAVAX) -cp $(BUILD) ChatServer