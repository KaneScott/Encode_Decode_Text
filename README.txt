Author: Kane Scott
Student ID: 1298685

Due to not knowing anyone in Hamilton, my work schedule with 3 other 300 papers, and general social issues I ended up working on this project alone.
I did not make it to writing shell scripts for the encoder/decoder. As of now if you give main a file argument it will encrypt and decrypt it with
the appropriate output. 

Known Issues:
	The encoding process is very slow. This may be due to poor implementation of the trie, or lack of maximum byte size.
	The encoding dictionary stores values as integer and string nodes, unsure if I was meant to implement it for integer and byte nodes.
	The encoding may lose a character or two at the end due to running out of text while attempting to add another character.
	The process does not have a maximum byte size of the dictionary. 