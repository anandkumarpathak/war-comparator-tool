# War Comparator Tool

WarComparator Tool allows to compare two war files.

It compares each war file by recursively comparing each internal file. If the internal files are found to be same, then the tool compares the internal jars for the files inside the jar.
The tool stops search at first different file. 

The tool exit with return value 1 if files are different or 0 if wars are similar.

Steps to run:

The tool can be run from command line as below:

java -jar -cp warcomparator-1.0.0-SNAPSHOT.jar;commons-codec-1.10.jar;guava-19.0.jar com.andy.tools.WarComparator <newWar> <newWarExtractFolder> <oldWar> <oldWarExtractFolder>