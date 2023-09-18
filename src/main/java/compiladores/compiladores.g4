grammar compiladores;

@header {
package compiladores;
}


fragment LETRA : [A-Za-z];
fragment DIGITO : [0-9];
fragment INT : 'int';
fragment CHAR : 'char';
fragment DOUBLE : 'double';

PARENTHESES_O : '(';
PARENTHESES_C : ')';
BRACKETS_O : '[';
BRACKETS_C : ']';
BRACES_O : '{';
BRACES_C : '}';
SEMICOLON : ';';
COMMA : ',';
EQUAL : '=';
ADD : '+';
SUB : '-';
MUL : '*';
DIV : '/';

TYPE : (INT | DOUBLE | CHAR);

ID : (LETRA | '_')(LETRA | DIGITO | '_')?;
NUMBER : DIGITO+ ;
WS : [ \n\t\r] -> skip;
OTHER : . ;


program : instructions EOF;

instructions : instruction instructions
             |
             ;

instruction : compoudInstruction
            | statement
            | assignment
            //| Return
            //| If
            //| While
            ;
            
compoudInstruction : BRACES_O instructions BRACES_C;

statement : TYPE variablesSequence SEMICOLON;

variablesSequence : ID COMMA variablesSequence
                  | ID
                  | assignment COMMA variablesSequence
                  | assignment
                  ;


assignment : ID EQUAL arithmeticExpresion;

//Arithmetic expression
arithmeticExpresion : term t;

term : factor f;

t : ADD term t
  | SUB term t
  |
  ;

factor : NUMBER
       | ID
       | PARENTHESES_O arithmeticExpresion PARENTHESES_C
       ;

f : MUL factor f
  | DIV factor f
  |
  ;
//end arithmetic expression