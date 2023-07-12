# Project_C

### About the project 
This project involves the development of a programming language and corresponding compiler, with the primary objective being the  description and graphic visualization of automata <p>
The [language grammar](src/adv.g4), implemented in ANTLR4, allows users to write code in a file with the adv format. The [compiler](src/advCompiler.java), implemented as a visitor, in Java, is responsible for translating that adv code into Python code. To standardize and facilitate the code generation process, the compiler utilizes [string templates](src/python.stg), which define the structure and formatting of the resulting Python code <p>
With this, in addition to the standard functionalities available in programming languages, users can also create automata and define their constituent items (states, transitions, labels), create views to describe and manipulate the automata items on a canvas with coordinates and, finally, show those views in interactive graphic animations <br>

In terms of generic functionalities, the language provides the following:
- declaration and assignment of variables ``point p1, p2;`` ``p2 = (10,10) / 2 + p1 * 5;``
- numeric expressions with the usual precedences ``^`` ``*`` ``\`` ``%`` ``+`` ``-`` ``()``
- boolean expressions with the usual precedences ``!`` ``&&`` ``||`` ``==`` ``!=`` ``>`` ``<`` ``>=`` ``<=`` ``()``
- conditional instruction ``if``
- repetitive instruction ``while`` ``break`` ``continue``
- terminal output ``print``
- terminal input ``read``
- inline and multi-line comments ``//`` ``/* */``

In terms of generic data types, the language provides the following:
- String ``"any chars123 º.,* "``
- Boolean ``true`` ``false``
- Number (negative/positive) with two types of representation:
    - integer ``+10`` ``39`` ``-63``
    - float ``+56.37`` ``46.3`` ``-0.6``
- Point with two types of representation:
    - cartesian coordinates (x,y) ``(10, -30.98)``
    - polar coordinates (angle:norm) ``(200:10)``
- List with key-pair values ``[]`` ``[val = 10, initial = true]``
- Set with generic values ``{}`` ``{ 'a', "abcdawe", -10.42, (10,10) }``

Additionally, the language has specific functionalities and data types related to automata description and graphic visualization:
- Alphabet construction ``alphabet {'a', 'b', 'c', '1', '8'}``
  - must always be the first instruction of the program, can only appear once and must contain at least 1 symbol
  - symbol can only be a letter or a decimal digit, in single quotes and separated by commas
    
- Automaton data type ``NFA a1 <<< ... >>>`` ``DFA a2 <<< ... >>>`` ``complete DFA a3 <<< ... >>>``, with optional instructions that can only be defined inside it:
    - state construction and property manipulation ``state A;`` ``A [initial = true];``
        - properties are initial, accepting and highlighted, all with boolean values
    - transition construction ``transition A -> 'a','b' -> A, C -> B;``
        - can contain alphabet symbols or be empty
        - must be between two existing states
          
- View data type ``view v1 of a1 <<< ... >>>`` that is necessarily associated with an automaton (a1), with optional instructions that can only be defined inside it:
    - grid construction ``grid g3 (30,50) [step = 0.7, margin = 0.45, color = gray, line = solid];``
        - point represents the dimensions of the grid, such as (width, height)
        - step represents the cell size
        - margin represents the external margin and must be lower than step
        - color represents the line color that can be: **red**, **green**, **blue**, **gray**
        - line represents the type of line that can only be **solid**
    - place construction ``place A at (10, -40), B at (300:-10)`` ``place <A,B>#label at p1;``
        - can be used to place existing states or transition labels at specific points
        - all states must be explicitly placed 
        - transition labels can be explicitly placed, if not they will be automatically placed in their default position 
    - transition label alignment construction ``<A,B>#label [align=below right];``
        - align can be: **left**, **right**, **above**, **above center**, **above left**, **above right**,  **below**, **below center**, **below left**, **below right**
        - above and below will translate to above center and below center, respectively
        - left and right will translate to above left and above right, respectively
        - if this instruction is not used, the default alignment of the label will be above center
    - state reference ``(A)``
        - this will reference the origin point of the state
        - can only be used after the respective state has been placed at a point
      
- Animation data type ``animation m1 <<< ... >>>``, with optional instructions that can only be defined inside it:
    - viewport data type ``viewport vp1 for v1 at (250,20) -- ++(700,500);`` that is necessarily associated with a certain view (v1)
        - first point represents the point where the upper-left corner of the viewport will be
        - second point represents the dimensions of the viewport, such as (width, height)
    - viewport manipulation construction``on vp1 <<< ... >>>``, with optional instructions that can only be defined inside it:
        - show instruction ``show g3`` ``show A, B`` ``show <A,B>`` that shows grids, states and/or transitions in the animation
        - pause instruction ``pause`` that pauses the animation and waits for user to continue

### Detailed description/usage 
In the directory **examples** there are 3 scripts that together allow the testing of the languages:
- build.sh - used to compile the language compiler
- compile.sh - used to compile an adv file into a python file
- run.sh - used to run the newly obtained python file

There are also 3 test files that demonstrate the functionalities of the language:
- test_01.adv - Demonstrate language ability to create and manipulate automata, views and animations
- test_02.adv - Demonstrate language ability to create and manipulate automata, views and animations
- test_03.adv - Demonstrate language ability to operate with expressions, user<->terminal interaction, and repetitive and conditional instructions

An example of a full test would be (assuming you are in the directory **examples**):
```
./build.sh
./compile.sh test_01.adv
./run.sh test_01.py
```

https://github.com/LuisFilipeCouto/Project_C/assets/70239504/ef6730e6-ad19-43cf-afab-bf1c03b841ee
