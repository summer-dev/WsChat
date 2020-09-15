JAVAC=javac
JAVACX=-Xbootclasspath/a:lib/jwd.jar -Xlint:unchecked
JAVA=java
JAVAX=-Xbootclasspath/a:lib/jwd.jar

JAR=jar
JARX=-cfm
all: server go

server: ChatServer.java
	$(JAVAC) $(JAVACX) $^

cc:  man_lib.mf 
	jar $(JARX) cc.jar $^ Chat*.class
go:
	$(JAVA) $(JAVAX) ChatServer