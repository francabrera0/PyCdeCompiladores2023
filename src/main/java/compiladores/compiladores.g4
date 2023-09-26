grammar compiladores;

@header {
package compiladores;
}

/*Begin fragments*/
fragment LETRA : [A-Za-z];
fragment DIGITO : [0-9];
fragment INT : 'int';
fragment CHAR : 'char';
fragment DOUBLE : 'double';
/*end fragments*/

/*Begin lexical rules*/
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
INCDECOPERATORS : (ADD ADD | SUB SUB);

RETURN : 'return';
IF : 'if';
ELSE : 'else';
WHILE : 'while';
FOR : 'for';

TYPE : (INT | DOUBLE | CHAR);

ID : (LETRA | '_')(LETRA | DIGITO | '_')?;
NUMBER : DIGITO+ ;
WS : [ \n\t\r] -> skip;
OTHER : . ;
/*end lexical rules*/


/*Begin syntactic rules*/

/*
 * Initial symbol. Search for instructions until the file is finished.
 */
program : instructions EOF;


/*
 * This rule, allows the concatenation of instructions
 */
instructions : instruction instructions
             |
             ;

/*
 * Allowed instructions
 */
instruction : compoudInstruction
            | statement
            | assignments SEMICOLON                
            | instructionReturn  
            | instructionIf
            | instructionWhile
            | instructionFor
            //| functionStatement
            //| functionCall
            | incDec SEMICOLON
            ;

/*
 * Compount instruction
 *  
 * @brief: Series of instructions enclosed in braces
 */
compoudInstruction : BRACES_O instructions BRACES_C;


/*
 * Statement
 *  
 * @brief: variables statements. Allows multiple statements and/or assignments of variables of the same type
 *        
 *         example: int a=10, b, d, m=0;
 */
statement : TYPE statementsTypes SEMICOLON;

statementsTypes : ID COMMA statementsTypes
                | ID
                | assignment COMMA statementsTypes
                | assignment
                ;

/*
 * Assignment
 *  
 * @brief: variables assignment. Allows multiple assignments sepparated by commas.
 *        
 *         example: a=10, i=0;
 */
assignments : assignment COMMA assignments
            | assignment
            ;

assignment : ID EQUAL logicalArithmeticExpression;


/*
 * Logical Arithmetic expressions --> INCOMPLETE, logical operations need to be added
 *
 * @brief:
 */
logicalArithmeticExpression : arithmeticExpression; //Logical expression missing


/*
 * Arithmetic expressions
 *
 * @brief: An arithmetic expression is a series of terms sepparated by '+' or '-'.
 *         Each of the terms is composed of a series od factors that can be multiplied ('*') or divided ('/').
 *         Factors can be:
 *          - A number
 *          - A variable name (ID)
 *          - Another arithmetic expression
 *          - A pre/post increment/deecrement
 *          - A function call
 *
 *          example: 10 + a + (5*10+3) * --i 
 */
arithmeticExpression : term t;

term : factor f;

t : ADD term t
  | SUB term t
  |
  ;

factor : NUMBER
       | ID
       | PARENTHESES_O arithmeticExpression PARENTHESES_C
       | incDec
       //| functionCall
       ;

f : MUL factor f
  | DIV factor f
  |
  ;


/*
 * Pre/Post Increment/Decrement
 *
 * @brief: An expression of this type is composed of a variable name preceded 
 *          or followed by a '++' or '--' operator
 *
 *   example: a++, i--, --i
 */
incDec : INCDECOPERATORS ID
       | ID INCDECOPERATORS
       ;



/*
 * Return
 *
 * @brief: A return statement can return an logical arithmetic expression or nothing
 *
 *  example: return;, return a+1; 
 */
instructionReturn : RETURN logicalArithmeticExpression SEMICOLON
                  | RETURN SEMICOLON
                  ;

/*
 * If else
 *
 * @brief: An if statement has as condition a logical arithmetic expression that must be enclosed in parentheses.
 *          After condition, the instruction must be placed.
 *          Optionally, you can find the 'else if' (which must also have a condition) or 'else' statement
 *
 *   example: if(a>10)
 *              printf("a greater than 10")
 *            else
 *              printf ("a equal or less than 10)
 */
instructionIf : IF PARENTHESES_O logicalArithmeticExpression PARENTHESES_C  instruction instructionElseIf
              ;

instructionElseIf : ELSE IF PARENTHESES_O logicalArithmeticExpression PARENTHESES_C instruction instructionElseIf
                  | ELSE instruction
                  |
                  ;

/*
 * While
 *
 * @brief: A while loop has as condition a logical arithmetic expression that must be enclosed in parentheses.
 *          After condition, the instruction must be placed.
 *
 *  example: while(a>10){}
 */
instructionWhile : WHILE PARENTHESES_O logicalArithmeticExpression PARENTHESES_C (instruction | SEMICOLON)
                 ;


/*
 * For
 *
 * @brief: A for loop, after the reserved word, has three expressions that must be 
 *          enclosed in parentheses and sepparated by semicolons.
 *         These expressions are:
 *            - init: Here you can declare (or assign) one or more variable. This expression is optional.
 *            - condition: logical arithmetic expression. This expression is optional.
 *            - update: A series of logical arithmetic operations or variable assignments. This expression is optional.
 *
 *   example: for(;;); , for(int i=0; i<10; i++)
 */
instructionFor : FOR PARENTHESES_O init condition update PARENTHESES_C (instruction | SEMICOLON)
               ;


init : statement
     | assignments SEMICOLON
     | SEMICOLON
     ;

condition : logicalArithmeticExpression SEMICOLON
          | SEMICOLON
          ;

update : updates //Assignments missing
       |
       ;
 
updates : logicalArithmeticExpression COMMA updates
        | logicalArithmeticExpression
        ;


/*end syntactic rules*/