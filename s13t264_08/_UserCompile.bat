set classpath=.\..\..\..\..\

REM 戦略のコンパイル(strategy/user/アカウント/)

javac -encoding UTF-8 *.java

javac -encoding UTF-8 .\..\..\..\..\SetPlayer.java
javac -encoding UTF-8 .\..\..\..\..\SetLeague.java

java GogoStart

pause
