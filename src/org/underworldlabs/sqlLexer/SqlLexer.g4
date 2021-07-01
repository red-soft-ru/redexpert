lexer  grammar SqlLexer;

 DATATYPE_SQL
 : SMALLINT
 | INTEGER
 | BIGINT
 | FLOAT
 | DOUBLE_PRECISION
 | DATE
 | TIME
 | TIMESTAMP
 | DECIMAL
 | NUMERIC
 | CHAR
 | CHARACTER
 | VARYING_CHARACTER
 | VARCHAR
 | CHARACTER_SET
 | NCHAR
 | NATIONAL_CHARACTER
 | NATIONAL_CHAR
 | VARYING
 | BLOB (SUB_TYPE (IDENTIFIER | INT_NUMBER))?
 | SEGMENT_SIZE
 ;

fragment INT_NUMBER:
  DIGIT+;

 fragment ARRAY_SIZE : '[' (DIGIT+ ':')? DIGIT+ (',' (DIGIT+ ':')? DIGIT+)* ']'
 ;


LINTERAL_VALUE
 : BLOB_LITERAL
 | K_NULL
 | K_CURRENT_TIME
 | K_CURRENT_DATE
 | K_CURRENT_TIMESTAMP
 | K_TRUE
 | K_FALSE
 ;

UNARY_OPERATOR
 : '-'
 | '+'
 | '~'
 | K_NOT
 ;

 OPERATOR
 : OPEN_PAR
  | CLOSE_PAR
  | SCOL
  | DOT
  | COMMA
  | ASSIGN
  | STAR
  | PLUS
  | MINUS
  | TILDE
  | PIPE2
  | DIV
  | MOD
  | LT2
  | GT2
  | AMP
  | PIPE
  | LT
  | LT_EQ
  | GT
  | GT_EQ
  | EQ
  | NOT_EQ1
  | NOT_EQ2
 ;

KEYWORD
 : K_ABORT
 | K_ACTION
 | K_ADD
 | K_AFTER
 | K_ALL
 | K_ALTER
 | K_ANALYZE
 | K_AND
 | K_AS
 | K_ASC
 | K_ATTACH
 | K_AUTOINCREMENT
 | K_BEFORE
 | K_BEGIN
 | K_BETWEEN
 | K_BY
 | K_CASCADE
 | K_CASE
 | K_CAST
 | K_CHECK
 | K_COLLATE
 | K_COLUMN
 | K_COMMIT
 | K_CONFLICT
 | K_CONSTRAINT
 | K_CREATE
 | K_CROSS
 | K_CURRENT_DATE
 | K_CURRENT_TIME
 | K_CURRENT_TIMESTAMP
 | K_DATABASE
 | K_DECLARE
 | K_DEFAULT
 | K_DEFERRABLE
 | K_DEFERRED
 | K_DELETE
 | K_DESC
 | K_DETACH
 | K_DISTINCT
 | K_DROP
 | K_EACH
 | K_ELSE
 | K_END
 | K_ENABLE
 | K_ESCAPE
 | K_EXCEPT
 | K_EXCLUSIVE
 | K_EXISTS
 | K_EXPLAIN
 | K_FAIL
 | K_FOR
 | K_FOREIGN
 | K_FROM
 | K_FULL
 | K_GLOB
 | K_GROUP
 | K_HAVING
 | K_IF
 | K_IGNORE
 | K_IMMEDIATE
 | K_IN
 | K_INDEX
 | K_INDEXED
 | K_INITIALLY
 | K_INNER
 | K_INSERT
 | K_INSTEAD
 | K_INTERSECT
 | K_INTO
 | K_IS
 | K_ISNULL
 | K_JOIN
 | K_KEY
 | K_LEFT
 | K_LIKE
 | K_LIMIT
 | K_MATCH
 | K_NATURAL
 | K_NO
 | K_NOT
 | K_NOTNULL
 | K_NULL
 | K_OF
 | K_OFFSET
 | K_ON
 | K_OR
 | K_ORDER
 | K_OUTER
 | K_PLAN
 | K_PRAGMA
 | K_PRIMARY
 | K_QUERY
 | K_RAISE
 | K_RECURSIVE
 | K_REFERENCES
 | K_REGEXP
 | K_REINDEX
 | K_RELEASE
 | K_RENAME
 | K_REPLACE
 | K_RESTRICT
 | K_RIGHT
 | K_ROLLBACK
 | K_ROW
 | K_SAVEPOINT
 | K_SELECT
 | K_SET
 | K_TABLE
 | K_TEMP
 | K_TEMPORARY
 | K_THEN
 | K_TO
 | K_TRANSACTION
 | K_TRIGGER
 | K_UNION
 | K_UNIQUE
 | K_UPDATE
 | K_USING
 | K_VACUUM
 | K_VALUES
 | K_VARIABLE
 | K_VIEW
 | K_VIRTUAL
 | K_WHEN
 | K_WHERE
 | K_WITH
 | K_WITHOUT
 | K_NEXTVAL
 ;

 //datatypes
fragment BIGINT : B I G I N T;
fragment  BLOB : B L O B;
fragment  CHAR : C H A R;
fragment  CHARACTER : C H A R A C T E R;
fragment  DATE : D A T E;
fragment  DECIMAL : D E C I M A L;
fragment  DOUBLE_PRECISION : D O U B L E ' ' P R E C I S I O N;
fragment  FLOAT : F L O A T;
fragment  INT : I N T;
fragment  INTEGER : I N T E G E R;
fragment  NATIONAL_CHARACTER : N A T I O N A L ' ' C H A R A C T E R;
fragment  NATIONAL_CHAR : N A T I O N A L ' ' CHAR;
fragment  NCHAR : N CHAR;
fragment  NATIONAL_CHARACTER_VARYING : NATIONAL_CHARACTER ' ' V A R Y N G;
fragment  NATIONAL_CHAR_VARYING : N A T I O N A L ' ' C H A R ' ' V A R Y I N G;
fragment  NCHAR_VARYING : N C H A R ' ' V A R Y I N G;
fragment  NUMERIC :  N U M E R I C;
fragment  SMALLINT : S M A L L I N T;
fragment  TIME : T I M E;
fragment  TIMESTAMP : T I M E S T A M P;
fragment  VARYING_CHARACTER : V A R Y I N G ' ' CHARACTER;
fragment  VARCHAR : V A R C H A R;
fragment  VARYING : V A R Y I N G;
fragment  SUB_TYPE : S U B '_' T Y P E;
fragment  SEGMENT_SIZE : S E G M E N T ' ' S I Z E;
fragment  CHARACTER_SET : CHARACTER ' ' S E T;
 // TODO check all names below

//[a-zA-Z_0-9\t \-\[\]\=]+

fragment OPEN_PAR : '(';
fragment CLOSE_PAR : ')';


fragment SCOL : ';';
fragment DOT : '.';
fragment COMMA : ',';
fragment ASSIGN : '=';
fragment STAR : '*';
fragment PLUS : '+';
fragment MINUS : '-';
fragment TILDE : '~';
fragment PIPE2 : '||';
fragment DIV : '/';
fragment MOD : '%';
fragment LT2 : '<<';
fragment GT2 : '>>';
fragment AMP : '&';
fragment PIPE : '|';
fragment LT : '<';
fragment LT_EQ : '<=';
fragment GT : '>';
fragment GT_EQ : '>=';
fragment EQ : '==';
fragment NOT_EQ1 : '!=';
fragment NOT_EQ2 : '<>';

// http://www.sqlite.org/lang_keywords.html
fragment K_ABORT : A B O R T;
fragment K_ACTION : A C T I O N;
fragment K_ADD : A D D;
fragment K_AFTER : A F T E R;
fragment K_ALL : A L L;
fragment K_ALTER : A L T E R;
fragment K_ANALYZE : A N A L Y Z E;
fragment K_AND : A N D;
fragment K_AS : A S;
fragment K_ASC : A S C;
fragment K_ATTACH : A T T A C H;
fragment K_AUTHID : A U T H I D;
fragment K_AUTOINCREMENT : A U T O I N C R E M E N T;
fragment K_BEFORE : B E F O R E;
fragment K_BEGIN : B E G I N;
fragment K_BETWEEN : B E T W E E N;
fragment K_BLOCK : B L O C K;
fragment K_BY : B Y;
fragment K_CALLER : C A L L E R;
fragment K_CASCADE : C A S C A D E;
fragment K_CASE : C A S E;
fragment K_CAST : C A S T;
fragment K_CHECK : C H E C K;
fragment K_COLLATE : C O L L A T E;
fragment K_COLUMN : C O L U M N;
fragment K_COMMIT : C O M M I T;
fragment K_CONFLICT : C O N F L I C T;
fragment K_CONSTRAINT : C O N S T R A I N T;
fragment K_CREATE : C R E A T E;
fragment K_CREATE_OR_ALTER: K_CREATE ' ' O R ' ' K_ALTER;
fragment K_CROSS : C R O S S;
fragment K_CURRENT_DATE : C U R R E N T '_' D A T E;
fragment K_CURRENT_TIME : C U R R E N T '_' T I M E;
fragment K_CURRENT_TIMESTAMP : C U R R E N T '_' T I M E S T A M P;
fragment K_DATABASE : D A T A B A S E;
fragment K_DECLARE : D E C L A R E;
fragment K_DEFAULT : D E F A U L T;
fragment K_DEFERRABLE : D E F E R R A B L E;
fragment K_DEFERRED : D E F E R R E D;
fragment K_DELETE : D E L E T E;
fragment K_DESC : D E S C;
fragment K_DETACH : D E T A C H;
fragment K_DISTINCT : D I S T I N C T;
fragment K_DROP : D R O P;
fragment K_EACH : E A C H;
fragment K_ELSE : E L S E;
fragment K_END : E N D;
fragment K_ENABLE : E N A B L E;
fragment K_ESCAPE : E S C A P E;
fragment K_EXCEPT : E X C E P T;
fragment K_EXCLUSIVE : E X C L U S I V E;
fragment K_EXECUTE : E X E C U T E;
fragment K_EXISTS : E X I S T S;
fragment K_EXPLAIN : E X P L A I N;
fragment K_FAIL : F A I L;
fragment K_FALSE : F A L S E;
fragment K_FOR : F O R;
fragment K_FOREIGN : F O R E I G N;
fragment K_FROM : F R O M;
fragment K_FULL : F U L L;
fragment K_GLOB : G L O B;
fragment K_GROUP : G R O U P;
fragment K_HAVING : H A V I N G;
fragment K_IF : I F;
fragment K_IGNORE : I G N O R E;
fragment K_IMMEDIATE : I M M E D I A T E;
fragment K_IN : I N;
fragment K_INDEX : I N D E X;
fragment K_INDEXED : I N D E X E D;
fragment K_INITIALLY : I N I T I A L L Y;
fragment K_INNER : I N N E R;
fragment K_INSERT : I N S E R T;
fragment K_INSTEAD : I N S T E A D;
fragment K_INTERSECT : I N T E R S E C T;
fragment K_INTO : I N T O;
fragment K_IS : I S;
fragment K_ISNULL : I S N U L L;
fragment K_JOIN : J O I N;
fragment K_KEY : K E Y;
fragment K_LEFT : L E F T;
fragment K_LIKE : L I K E;
fragment K_LIMIT : L I M I T;
fragment K_MATCH : M A T C H;
fragment K_NATURAL : N A T U R A L;
fragment K_NEXTVAL : N E X T V A L;
fragment K_NO : N O;
fragment K_NOT : N O T;
fragment K_NOTNULL : N O T ' ' N U L L;
fragment K_NULL : N U L L;
fragment K_OF : O F;
fragment K_OFFSET : O F F S E T;
fragment K_ON : O N;
fragment K_ONLY : O N L Y;
fragment K_OR : O R;
fragment K_ORDER : O R D E R;
fragment K_OUTER : O U T E R;
fragment K_OWNER : O W N E R;
fragment K_PLAN : P L A N;
fragment K_PRAGMA : P R A G M A;
fragment K_PROCEDURE : P R O C E D U R E;
fragment K_PRIMARY : P R I M A R Y;
fragment K_QUERY : Q U E R Y;
fragment K_RAISE : R A I S E;
fragment K_RECREATE : R E C R E A T E;
fragment K_RECURSIVE : R E C U R S I V E;
fragment K_REFERENCES : R E F E R E N C E S;
fragment K_REGEXP : R E G E X P;
fragment K_REINDEX : R E I N D E X;
fragment K_RELEASE : R E L E A S E;
fragment K_RENAME : R E N A M E;
fragment K_REPLACE : R E P L A C E;
fragment K_RESTRICT : R E S T R I C T;
fragment K_RETURNS : R E T U R N S;
fragment K_RIGHT : R I G H T;
fragment K_ROLLBACK : R O L L B A C K;
fragment K_ROW : R O W;
fragment K_SAVEPOINT : S A V E P O I N T;
fragment K_SELECT : S E L E C T;
fragment K_SET : S E T;
fragment K_TABLE : T A B L E;
fragment K_TEMP : T E M P;
fragment K_TEMPORARY : T E M P O R A R Y;
fragment K_THEN : T H E N;
fragment K_TO : T O;
fragment K_TRANSACTION : T R A N S A C T I O N;
fragment K_TRIGGER : T R I G G E R;
fragment K_TRUE : T R U E;
fragment K_TYPE_OF : T Y P E ' ' O F;
fragment K_UNION : U N I O N;
fragment K_UNIQUE : U N I Q U E;
fragment K_UPDATE : U P D A T E;
fragment K_USING : U S I N G;
fragment K_VACUUM : V A C U U M;
fragment K_VALUES : V A L U E S;
fragment K_VARIABLE : V A R I A B L E;
fragment K_VIEW : V I E W;
fragment K_VIRTUAL : V I R T U A L;
fragment K_WHEN : W H E N;
fragment K_WHERE : W H E R E;
fragment K_WITH : W I T H;
fragment K_WITHOUT : W I T H O U T;



NUMERIC_LITERAL
 : DIGIT+ ( '.' DIGIT* )? ( E [-+]? DIGIT+ )?
 | '.' DIGIT+ ( E [-+]? DIGIT+ )?
 ;

BIND_PARAMETER
 : '?' DIGIT*
 | [:@] IDENTIFIER
 ;

 PART_OBJECT:
 IDENTIFIER '.' IDENTIFIER
 ;

 IDENTIFIER
  : '"' (~'"' | '""')* '"'
  | '`' (~'`' | '``')* '`'
  | '[' ~']'* ']'
  | [a-zA-Z_] [a-zA-Z_$0-9]* // TODO check: needs more chars in set
  ;

STRING_LITERAL
 : '\'' ( ~'\'' )* ('\''| EOF)
 ;

fragment BLOB_LITERAL
 : X STRING_LITERAL
 ;

SINGLE_LINE_COMMENT
 : '--' ~[\r\n]* -> channel(HIDDEN)
 ;

MULTILINE_COMMENT
 : '/*' .*? ( '*/' | EOF ) -> channel(HIDDEN)
 ;

SPACES
 : [ \u000B\t\r\n] -> channel(HIDDEN)
 ;

fragment UNEXPECTED_CHAR
 : .
 ;

fragment DIGIT : [0-9];

fragment A : [aA];
fragment B : [bB];
fragment C : [cC];
fragment D : [dD];
fragment E : [eE];
fragment F : [fF];
fragment G : [gG];
fragment H : [hH];
fragment I : [iI];
fragment J : [jJ];
fragment K : [kK];
fragment L : [lL];
fragment M : [mM];
fragment N : [nN];
fragment O : [oO];
fragment P : [pP];
fragment Q : [qQ];
fragment R : [rR];
fragment S : [sS];
fragment T : [tT];
fragment U : [uU];
fragment V : [vV];
fragment W : [wW];
fragment X : [xX];
fragment Y : [yY];
fragment Z : [zZ];