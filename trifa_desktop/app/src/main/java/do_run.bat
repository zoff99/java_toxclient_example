cd /D "%~dp0"
java.exe -Djava.library.path="." -classpath ".;json-20210307.jar;emoji-java-5.1.1.jar;sqlite-jdbc-3.32.3.2.jar;webcam-capture-0.3.12.jar;bridj-0.7.0.jar;slf4j-api-1.7.2.jar;flatlaf-1.0.jar" com.zoffcc.applications.trifa.MainActivity
