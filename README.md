# WordSolvers
 Libarary to solve word-based puzzles

 Anagram Solver: Solves anagrams, even with multiple words. Adjust settings in code.
 - MAX_RESULTS: number of results to display. Will take the results with the most common words. Will repeat the same
     results in different orders (for now!)
 - MAX_WORDS: the maximum number of words the anagram can have

 Cipher Solver: Solves some of the more basic ciphers.
 - Caesar (will find shift)
 - Atbash (AKA reversing the alphabet)
 - A1Z26
 - Vigenere (given a key)

 Word Jumble: Creates anagrams for a "word jumble" game. Adjust game settings in code.
 - JUMBLE_LEN: number of words in the jumble
 - MIN_WORD_LEN: Minimum length of allowed words
 - NUM_ACCEPTABLE_WORDS: How many words the program can choose from. A value of 5000 will choose from the top 5000 most
     common words which are at least MIN_WORD_LEN in length.

 Word Search Solver:
 Solves word searches, given either a list of words, or using the dictionary.
 - WORD_SEARCH: A String representation of the word search, each line separated by \n.
