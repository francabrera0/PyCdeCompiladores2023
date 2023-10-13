grammar compiladores;

@header {
package compiladores;
}

/*Begin fragments*/
fragment LETTER : [A-Za-z];
fragment DIGIT : [0-9];
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
AND : '&&';
OR : '||';
EQUALS : '==';
NOT_EQUALS : '!=';
LESS_THAN : '<';
LESS_OR_EQUALS_THAN : '<=';
GREATER_THAN : '>';
GREATER_OR_EQUALS_THAN : '>=';

INCDECOPERATORS : (ADD ADD | SUB SUB);

RETURN : 'return';
IF : 'if';
ELSE : 'else';
WHILE : 'while';
FOR : 'for';

TYPE : (INT | DOUBLE | CHAR);

ID : (LETTER | '_')(LETTER | DIGIT | '_')*;
NUMBER : DIGIT+ ;
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
            | returnStatement  
            | ifStatement
            | whileStatement
            | forStatement
            | logicalArithmeticExpression SEMICOLON
            | functionStatement
            | incDec SEMICOLON
            ;

/*
 * Compound instruction
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
 * Logical Arithmetic expressions
 *
 * @brief:
 */
logicalArithmeticExpression : arithmeticExpression
                            | logicalExpression 
                            ;


/*
 * Arithmetic expressions
 *
 * @brief: An arithmetic expression is a series of terms sepparated by '+' or '-'.
 *         Each of the terms is composed of a series of factors that can be multiplied ('*') or divided ('/').
 *         Factors can be:
 *          - A number
 *          - A variable name (ID)
 *          - Another arithmetic expression
 *          - A pre/post increment/deecrement
 *          - A function call
 *
 *          example: 10 + a + (5*10+3) * --i 
 */
arithmeticExpression : arithmeticTerm at;

arithmeticTerm : factor af;

at : ADD arithmeticTerm at
   | SUB arithmeticTerm at
   |
   ;

factor : NUMBER
       | ID
       | PARENTHESES_O logicalArithmeticExpression PARENTHESES_C
       | incDec
       | functionCall
       ;

af : MUL factor af
   | DIV factor af
   |
   ;

/*
 * Logical expressions
 *
 * @brief: An logical expression is a series of logical terms sepparated by '&&' or '||'.
 *         Each of the terms is composed of a series of factors that can be compared with different operators (>,>=,<,<=,==,!=).
 *         Factors can be:
 *          - A number
 *          - A variable name (ID)
 *          - Another arithmetic expression
 *          - A pre/post increment/deecrement
 *          - A function call
 *
 *           example: a < b && a != 2 
 */
logicalExpression : logicalTerm lt;

logicalTerm : factor lf;

lt : AND logicalTerm lt
   | OR logicalTerm lt
   |
   ;

lf : EQUALS logicalTerm lf
   | NOT_EQUALS logicalTerm lf
   | LESS_THAN logicalTerm lf
   | LESS_OR_EQUALS_THAN logicalTerm lf
   | GREATER_THAN logicalTerm lf
   | GREATER_OR_EQUALS_THAN logicalTerm lf
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
returnStatement : RETURN logicalArithmeticExpression SEMICOLON
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
ifStatement : IF PARENTHESES_O logicalArithmeticExpression PARENTHESES_C  instruction elseIfStatement
            ;

elseIfStatement : ELSE IF PARENTHESES_O logicalArithmeticExpression PARENTHESES_C instruction elseIfStatement
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
whileStatement : WHILE PARENTHESES_O logicalArithmeticExpression PARENTHESES_C (instruction | SEMICOLON)
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
forStatement : FOR PARENTHESES_O init condition update PARENTHESES_C (instruction | SEMICOLON)
             ;


init : statement
     | assignments SEMICOLON
     | SEMICOLON
     ;

condition : logicalArithmeticExpression SEMICOLON
          | SEMICOLON
          ;

update : logicalArithmeticExpression COMMA update
       | logicalArithmeticExpression
       | assignments
       |
       ;

/*
 * Function statement
 *
 * @brief: A function statement can be of two types: 
 *            just the prototype or the definition with implementation
 *           
 *   example: int sum(int, int); , int sum(int a, int b) { return a + b;}
 */

functionStatement : functionDeclaration compoudInstruction
                  | functionPrototype
                  ;

functionDeclaration : TYPE ID PARENTHESES_O parameters PARENTHESES_C
                    ;
parameters : TYPE ID
           | TYPE ID COMMA parameters
           |
           ;

functionPrototype : TYPE ID PARENTHESES_O parametersPrototype PARENTHESES_C SEMICOLON
                  ;

parametersPrototype : TYPE ID
                    | TYPE ID COMMA parametersPrototype
                    | TYPE
                    | TYPE COMMA parametersPrototype
                    |
                    ;

/*
 * Function call
 *
 * @brief: A function call is made up of the function name, 
 *             parameters enclosed in parentheses and a semicolon.
 *         Parameters can be zero or more factors.
 *         
 *           
 *   example: sum(a,b); , sum(3, sum(a, 4)); 
 */

functionCall : ID PARENTHESES_O callParameters PARENTHESES_C
             ;

callParameters : factor 
               | factor COMMA callParameters
               |
               ;

/*end syntactic rules*/