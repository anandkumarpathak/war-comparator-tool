# public

WarComparator Tool allows to compare two war files.

It compares each war file by recursively comparing each internal file. If the internal files are found to be same, then the tool compares the internal jars for the files inside the jar.
The tool stops search at first different file. 

The tool exit with return value 1 if files are different or 0 if wars are similar.