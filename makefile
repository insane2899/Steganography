
Steganography:	Steganography.jar
	echo '#!/usr/bin/java -jar'	> Steganography
	cat Steganography.jar >> Steganography
	chmod +x Steganography



Steganography.jar:	Steganography.java Lib.jar
	javac -cp Lib.jar Steganography.java
	printf "Manifest-Version: 1.0\nClass-Path: Lib.jar\nMain-Class: Steganography\n" >> MANIFEST.MF
	jar  -cvfm Steganography.jar ./MANIFEST.MF Steganography.class

clean: 
	rm *.class