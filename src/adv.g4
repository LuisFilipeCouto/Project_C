grammar adv;

@header{
        import types.Type;
}
@parser::members{
        int insideLoop = 0;
}

/* The first declaration in the program must be the alphabet definition, that can only appear once in the program 
It can then be followed by zero or more statements
*/
program: (alphabetDefinition statement* EOF) | EOF;

/* Alphabet must contain one or more symbols, separated by comma, and enclosed in curly brackets */ 
alphabetDefinition: 'alphabet' '{' SYMBOL (',' SYMBOL)* '}' STOPSYMB? ;

/* A statement can be a context free instruction or a section of code */
statement: (instruction | section) ;

/* Sections that can exist in the program */
section: (automatonSection | viewSection| animationSection) ;

/* Instructions that can exist in the program and are context free */
instruction: (declare | assign | forEach | readInput | whileLoop | conditional | play | print) ;

/* Expressions that can exist in the program */
expression returns[String var=null, Type t=null]:
         <assoc=right> e1=expression '^' e2=expression                                  #ExpressionPow
        | sign=('+'|'-') expression                                                     #ExpressionSign
        | e1=expression op=('*' | '/' | '%') e2=expression                              #ExpressionMultDivMod
        | e1=expression op=('+' | '-') e2=expression                                    #ExpressionAddSub
        | op='!' e=expression                                                           #ExpressionNot
        | e1=expression op='&&' e2=expression                                           #ExpressionAnd
        | e1=expression op='||' e2=expression                                           #ExpressionOr
        | e1=expression op=('==' | '!=' | '<' | '>' | '>=' | '<=') e2=expression        #ExpressionConditional
        | point                                                                         #ExpressionPoint        
        | number                                                                        #ExpressionNumber  
        | readInput                                                                     #ExpressionReadInput
        | BOOLEAN                                                                       #ExpressionBoolean
        | SYMBOL                                                                        #ExpressionSymbol
        | IDENTIFIER                                                                    #ExpressionIdentifier
        | STRING                                                                        #ExpressionString
        | '(' e=expression ')'                                                          #ExpressionParenthesis
        ;

/* Types of variable that can exist in the program */
type returns[Type t=null]:
        'boolean'                               #TypeBoolean
        | 'integer'                             #TypeInteger
        | 'float'                               #TypeFloat
        | 'string'                              #TypeString
        | 'number'                              #TypeNumber
        | 'point'                               #TypePoint
        | 'state'                               #TypeState
        ;

/* DECLARATION AND ASSIGNMENT OF VARIABLES 
declare -> declare one or more variables of the same type, such as integer a, b; 
assign -> assign one or more already declared variables to an expression, such as a = 1, b = 2+2;
declareAndAssign -> declare and assign one or more variables of the same type to an expression, such as integer c = 3, d = 4+4;
*/
declare returns[String var]: type IDENTIFIER (',' IDENTIFIER)* STOPSYMB ;

assign returns[String var]: type? IDENTIFIER '=' expression (',' IDENTIFIER '=' expression)* STOPSYMB ;

/* DEFINITION OF SECTIONS
automatonSection -> define an automaton that can be non deterministic (NFA), deterministic (DFA) or complete deterministic (complete DFA)
viewSection -> define a view of an automaton
animationSection -> define an animation
*/
automatonSection returns[String var]: ('NFA' | 'DFA' | 'complete' 'DFA') IDENTIFIER segment ;

viewSection returns[String var]: 'view' IDENTIFIER 'of' IDENTIFIER segment ;

animationSection returns[String var]: 'animation' IDENTIFIER segment ;

/* DEFINITION OF SEGMENTS OF CODE
A segment of code is delimited by <<< and >>>  
Each segment contains a specific set of allowed instructions
*/
segment: OPENSEG (allowedAutomaton | allowedView | allowedAnimation | allowedViewPort | allowedForEach) CLOSESEG ;

allowedAutomaton: (declare | assign | transitionDefinition | stateProperty | forEach | whileLoop | conditional | print)* ;
allowedView: (declare | assign | place | labelConstruction | arrowProperties | gridConstruction | forEach | whileLoop | conditional | print)* ;
allowedAnimation: (declare | assign | viewPortDefinition | viewPortAccess | forEach | whileLoop | conditional | print)* ;
allowedViewPort: (declare | assign | pause | show | forEach | whileLoop | conditional | print)* ;
allowedForEach: (declare | assign | stateProperty | show | play | pause | whileLoop | conditional | print)* ;

/* CONTEXT RESTRICTED INSTRUCTIONS 
viewPortDefinition -> define a viewport, such as viewport vp1 for v1 at (10,10) -- ++(500,500);
viewPortAccess -> access a viewport, such as on vp1;
stateProperty -> change state properties by accessing the list of key-value pair of the state, such as A [initial = true];
transitionDefinition -> define one or more transitions between two states using zero (meaning empty word) or more alphabet symbols
transitionConstruction -> transition construction such as <A,B>
place -> place a state or a label at a certain point in the view, such as place A at (10,10) OR place <B,A>#label [align = below] at pa;
labelConstruction -> label construction such as <A,B>#label OR <A,B>#label [align = below]
arrowProperties -> change type (polyline or curse) and slope of a transition arrow, such as <A,E> as p1 -- pm [slope=0] -- p2;
show -> Show an element or a view, such as show OR show A OR such <A,A>, B [accepting = true];
gridConstruction -> define a grid with/without specifying some/all the properties, such as grid g1 (10,10) OR grid g1 (10,10) [step = 0.5, margin = 0.25, color = gray, thickness = 0.1];
*/
viewPortDefinition: 'viewport' IDENTIFIER 'for' IDENTIFIER 'at' point '--' '++' point STOPSYMB ;

viewPortAccess: 'on' IDENTIFIER segment ;

stateProperty: IDENTIFIER genericList STOPSYMB? ;

transitionDefinition: 'transition' transitionSequence (',' transitionSequence)* STOPSYMB ;
transitionSequence: IDENTIFIER '->' (SYMBOL (',' SYMBOL)* '->')? IDENTIFIER ;                                    

transitionConstruction: '<' IDENTIFIER ',' IDENTIFIER '>' ;

place: 'place' statePlacement (',' statePlacement)* STOPSYMB                    #PlaceState
        | 'place' transitionPlacement (',' transitionPlacement)* STOPSYMB       #PlaceTransition
        ;
statePlacement: IDENTIFIER 'at' (point | IDENTIFIER) ;
transitionPlacement: labelConstruction 'at' (point | IDENTIFIER) ;

labelConstruction: label genericList? STOPSYMB? ;
label: transitionConstruction '#' IDENTIFIER ;

arrowProperties: transitionConstruction 'as' IDENTIFIER genericList? arrow=('..' | '--') IDENTIFIER genericList? (arrow=('..' | '--') IDENTIFIER genericList?)* STOPSYMB ;

show: 'show' (stateProperty | IDENTIFIER | transitionConstruction) (',' (stateProperty | IDENTIFIER | transitionConstruction))* STOPSYMB ;

gridConstruction: 'grid' IDENTIFIER point genericList STOPSYMB ;

/* CONTEXT FREE INSTRUCTIONS 
pause -> pause an animation or a viewport, such as pause anim1 OR pause;
play -> play an animation, such as play anim1;
readInput -> read input from terminal with/without prompt message, such as read OR read [prompt="Insira uma palavra: "];
forEach -> loop over elements of a list or set, such as for elem in collection. Can be used with/without opening a segment
whileLoop -> repetitive instruction, such as while (expression) { zero or more instructions }
conditional -> conditional instruction, such as if (expression)
print -> print something into the terminal, such as print("Hello World!");
*/
pause: 'pause' IDENTIFIER? STOPSYMB ;

play: 'play' IDENTIFIER STOPSYMB ;

readInput: 'read' ('[' 'prompt' '=' STRING ']')? ;

forEach: 'for' IDENTIFIER 'in' (genericList | IDENTIFIER) segment                       #ForEachListSegment
        | 'for' IDENTIFIER 'in' (genericList | IDENTIFIER) allowedForEach               #ForEachList
        | 'for' IDENTIFIER 'in' '{' genericSet '}' segment                              #ForEachSetSegment
        | 'for' IDENTIFIER 'in' '{' genericSet '}' allowedForEach                       #ForEachSet
        ;

conditional: 'if' '(' expression ')' '{' condLoopStats* '}' ;

whileLoop: 'while' '(' expression ')' '{'
        {insideLoop++;}
                condLoopStats*
        {insideLoop--;}
        '}'
        ;

condLoopStats: (instruction | breakLoop | continueLoop) ;
breakLoop: {insideLoop > 0}? 'break' STOPSYMB ;
continueLoop: {insideLoop > 0}? 'continue' STOPSYMB ;

print: 'print' '(' expression ')' STOPSYMB ;

/* GENERIC TYPE OF DATA 
genericList -> list type of zero or more key-value pairs, such as [key1=value1, key2=value2, ...] 
genericSet -> set type of zero or more elements, such as {elem1, elem2, ...}
number -> number type that can be a positive/negative integer or float
point -> point type that can be defined in both cartesian (x,y) coordinates or polar (angle:norm) coordinates
*/ 
genericList: '[' listElement? (',' listElement)* ']' ;
listElement: (point | number | IDENTIFIER | STRING) '=' (point | number | BOOLEAN | IDENTIFIER+ | STRING) ;

genericSet: '{' setElement? (',' setElement)* '}' ;
setElement: (point | number | IDENTIFIER | STRING) ;

number returns [Type t=null]: sign=('-' | '+')? num=(INTEGER | FLOAT) ;

point returns [Type t=null]: '(' x=number sep=(',' | ':') y=number ')' ;

/* Boolean value must be true or false */
BOOLEAN: 'true' | 'false' | 'True' | 'False' ;

/* Integer value must only contain decimal digits */
INTEGER: [0-9]+ ;

/* Float value must contain a decimal point and decimal digits */
FLOAT: [0-9]+ '.' [0-9]+ ;

/* Alphabet symbol must be a letter or a decimal digit separated by single quotes */ 
SYMBOL: '\'' [a-zA-Z0-9] '\'' ;

/* Identifier must start with a letter and can then, optionally, contain letters, decimal digits and underscore */ 
IDENTIFIER: [a-zA-Z][a-zA-Z0-9_]* ;

/* String can be any character between double quotes*/
STRING: '"' .*? '"' ;

/* Instruction stop symbol is a semicolon */
STOPSYMB: ';' ;

/* Define the start and end of a segment of code */
OPENSEG: '<<<' ;
CLOSESEG: '>>>' ;

/* Skip whitespace */ 
WS: [ \t\r\n]+ -> skip ;

/* Allow single-line and multi-line comments */
MULTICOMMENT: '/*' .*? '*/' -> skip ;
SINGLECOMMENT: '//' ~[\r\n]* -> skip ;
