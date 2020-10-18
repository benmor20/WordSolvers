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

 Crossword Solver: Finds words that match a given pattern of simplified regex, largely for crossword-esque puzzles
   (where some but not all letters are known)
   Simplified Regex:
     _ - blank character. Can be any letter.
     [abc] - one character that can match a, b, or c
     [^abc] - one character that can match anything except a, b, or c

     Coming soon:
     [abc/defg/hi] - matches "abc", "defg", or "hi"
     [2,5] - matches between 2 and 5 blanks (inclusive)
     [2,] - matches at least 2 blanks
     [,5] - matches up to 5 blanks (inclusive)
     [,] - matches any number of blanks
     [2,abc] - matches at least 2 blanks, all limited to a, b, or c
     [2,^abc] - matches at least 2 blanks, all limited to anything except a, b, or c
     (cde) - at end of phrase, will limit all unrestricted blanks to c, d, or e. ^ operator works as well.
        - _(cde) will match c, d, or e
        - [abc](cde) will match a, b, or c
        - [^abc](cde) will match any letter except a, b, or c
        - [^](cde) will match any letter
     {cde} - at end of phrase, will limit all blanks (restricted or not) to c, d, or e. ^ operator works as well.
        - _{cde} will match c, d, or e
        - [abc]{cde} will match only c
        - [^abc]{cde} will match d or e

 Word Jumble: Creates anagrams for a "word jumble" game. Adjust game settings in code.
 - JUMBLE_LEN: number of words in the jumble
 - MIN_WORD_LEN: Minimum length of allowed words
 - NUM_ACCEPTABLE_WORDS: How many words the program can choose from. A value of 5000 will choose from the top 5000 most
     common words which are at least MIN_WORD_LEN in length.

 Word Search Solver: Solves word searches, given either a list of words, or using the dictionary.
 - WORD_SEARCH: A String representation of the word search, each line separated by \n.
