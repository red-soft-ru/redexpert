lexer  grammar SqlLexer;
 DATATYPE_SQL
 : K_BIGINT
 | K_BLOB
 | K_BOOLEAN
 | K_CHAR
 | K_CHARACTER
 | K_DATE
 | K_DECIMAL
 | K_DOUBLE
 | K_FLOAT
 | K_INTEGER
 | K_INT
 | K_NATIONAL
 | K_NCHAR
 | K_NUMERIC
 | K_PRECISION
 | K_SMALLINT
 | K_TIME
 | K_TIMESTAMP
 | K_VARYING
 | K_VARCHAR
 ;

fragment INT_NUMBER:
  DIGIT+;

 fragment ARRAY_SIZE : '[' (DIGIT+ ':')? DIGIT+ (',' (DIGIT+ ':')? DIGIT+)* ']'
 ;


LINTERAL_VALUE
 : BLOB_LITERAL
 | K_NULL
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
  : K_ABS
  | K_ABSOLUTE
  | K_ACCENT
  | K_ACOS
  | K_ACOSH
  | K_ACTION
  | K_ADA
  | K_ADD
  | K_ALL
  | K_ALLOCATE
  | K_ALTER
  | K_ALWAYS
  | K_AND
  | K_ANY
  | K_ARE
  | K_AS
  | K_ASC
  | K_ASCII_CHAR
  | K_ASCII_VAL
  | K_ASIN
  | K_ASINH
  | K_ASSERTION
  | K_AT
  | K_ATAN
  | K_ATAN2
  | K_ATANH
  | K_AUTHORIZATION
  | K_AUTONOMOUS
  | K_AVG
  | K_BACKUP
  | K_BEGIN
  | K_BETWEEN
  | K_BINARY
  | K_BIN_AND
  | K_BIN_NOT
  | K_BIN_OR
  | K_BIN_SHL
  | K_BIN_SHR
  | K_BIN_XOR
  | K_BIT
  | K_BIT_LENGTH
  | K_BLOCK
  | K_BODY
  | K_BOTH
  | K_BREAK
  | K_BY
  | K_C
  | K_CALLER
  | K_CASCADE
  | K_CASCADED
  | K_CASE
  | K_CAST
  | K_CATALOG
  | K_CATALOG_NAME
  | K_CEIL
  | K_CEILING
  | K_CHARACTER_LENGTH
  | K_CHARACTER_SET_CATALOG
  | K_CHARACTER_SET_NAME
  | K_CHARACTER_SET_SCHEMA
  | K_CHAR_LENGTH
  | K_CHAR_TO_UUID
  | K_CHECK
  | K_CLASS_ORIGIN
  | K_CLOSE
  | K_COALESCE
  | K_COBOL
  | K_COLLATE
  | K_COLLATION
  | K_COLLATION_CATALOG
  | K_COLLATION_NAME
  | K_COLLATION_SCHEMA
  | K_COLUMN
  | K_COLUMN_NAME
  | K_COMMAND_FUNCTION
  | K_COMMENT
  | K_COMMIT
  | K_COMMITTED
  | K_COMMON
  | K_CONDITION_NUMBER
  | K_CONNECT
  | K_CONNECTION
  | K_CONNECTION_NAME
  | K_CONSTRAINT
  | K_CONSTRAINTS
  | K_CONSTRAINT_CATALOG
  | K_CONSTRAINT_NAME
  | K_CONSTRAINT_SCHEMA
  | K_CONTINUE
  | K_CONVERT
  | K_CORR
  | K_CORRESPONDING
  | K_COS
  | K_COSH
  | K_COT
  | K_COUNT
  | K_COVAR_POP
  | K_COVAR_SAMP
  | K_CREATE
  | K_CROSS
  | K_CUME_DIST
  | K_CURRENT
  | K_CURRENT_CONNECTION
  | K_CURRENT_DATE
  | K_CURRENT_ROLE
  | K_CURRENT_TIME
  | K_CURRENT_TIMESTAMP
  | K_CURRENT_TRANSACTION
  | K_CURRENT_USER
  | K_CURSOR
  | K_CURSOR_NAME
  | K_DATA
  | K_DATEADD
  | K_DATEDIFF
  | K_DATETIME_INTERVAL_CODE
  | K_DATETIME_INTERVAL_PRECISION
  | K_DAY
  | K_DDL
  | K_DEALLOCATE
  | K_DEC
  | K_DECLARE
  | K_DECODE
  | K_DECRYPT
  | K_DEFAULT
  | K_DEFERRABLE
  | K_DEFERRED
  | K_DELETE
  | K_DELETING
  | K_DENSE_RANK
  | K_DESC
  | K_DESCRIBE
  | K_DESCRIPTOR
  | K_DETERMINISTIC
  | K_DIAGNOSTICS
  | K_DIFFERENCE
  | K_DISCONNECT
  | K_DISTINCT
  | K_DOMAIN
  | K_DROP
  | K_DYNAMIC_FUNCTION
  | K_ELSE
  | K_ENCRYPT
  | K_END
  | K_END_EXEC
  | K_ENGINE
  | K_ESCAPE
  | K_EXCEPT
  | K_EXCEPTION
  | K_EXCLUDE
  | K_EXEC
  | K_EXECUTE
  | K_EXISTS
  | K_EXP
  | K_EXTERNAL
  | K_EXTRACT
  | K_FALSE
  | K_FETCH
  | K_FIRST
  | K_FIRSTNAME
  | K_FIRST_VALUE
  | K_FLOOR
  | K_FOLLOWING
  | K_FOR
  | K_FOREIGN
  | K_FORTRAN
  | K_FOUND
  | K_FROM
  | K_FULL
  | K_GENERATED
  | K_GEN_UUID
  | K_GET
  | K_GLOBAL
  | K_GO
  | K_GOTO
  | K_GRANT
  | K_GRANTED
  | K_GROUP
  | K_HASH
  | K_HAVING
  | K_HOUR
  | K_IDENTITY
  | K_IIF
  | K_IMMEDIATE
  | K_IN
  | K_INCREMENT
  | K_INDEX
  | K_INDICATOR
  | K_INITIALLY
  | K_INNER
  | K_INPUT
  | K_INSENSITIVE
  | K_INSERT
  | K_INSERTING
  | K_INTERSECT
  | K_INTERVAL
  | K_INTO
  | K_IS
  | K_ISOLATION
  | K_JOIN
  | K_KEY
  | K_LAG
  | K_LANGUAGE
  | K_LAST
  | K_LASTNAME
  | K_LAST_VALUE
  | K_LEAD
  | K_LEADING
  | K_LEAVE
  | K_LEFT
  | K_LENGTH
  | K_LEVEL
  | K_LIKE
  | K_LINGER
  | K_LIST
  | K_LN
  | K_LOCAL
  | K_LOCK
  | K_LOG
  | K_LOG10
  | K_LOWER
  | K_LPAD
  | K_MAPPING
  | K_MATCH
  | K_MATCHED
  | K_MATCHING
  | K_MAX
  | K_MAXVALUE
  | K_MESSAGE_LENGTH
  | K_MESSAGE_OCTET_LENGTH
  | K_MESSAGE_TEXT
  | K_MIDDLENAME
  | K_MILLISECOND
  | K_MIN
  | K_MINUTE
  | K_MINVALUE
  | K_MOD
  | K_MODULE
  | K_MONTH
  | K_MORE
  | K_MUMPS
  | K_NAME
  | K_NAMES
  | K_NATURAL
  | K_NEXT
  | K_NO
  | K_NOT
  | K_NTH_VALUE
  | K_NTILE
  | K_NULL
  | K_NULLABLE
  | K_NULLIF
  | K_NULLS
  | K_NUMBER
  | K_OCTET_LENGTH
  | K_OF
  | K_OFFSET
  | K_ON
  | K_ONLY
  | K_OPEN
  | K_OPTION
  | K_OR
  | K_ORDER
  | K_OS_NAME
  | K_OTHERS
  | K_OUTER
  | K_OUTPUT
  | K_OVER
  | K_OVERLAPS
  | K_OVERLAY
  | K_PACKAGE
  | K_PAD
  | K_PARTIAL
  | K_PARTITION
  | K_PASCAL
  | K_PERCENT_RANK
  | K_PI
  | K_PLACING
  | K_PLI
  | K_PLUGIN
  | K_POSITION
  | K_POWER
  | K_PRECEDING
  | K_PREPARE
  | K_PRESERVE
  | K_PRIMARY
  | K_PRIOR
  | K_PRIVILEGE
  | K_PRIVILEGES
  | K_PROCEDURE
  | K_PUBLIC
  | K_RAND
  | K_RANGE
  | K_RANK
  | K_RDB_RECORD_VERSION
  | K_RDB_ROLE_IN_USE
  | K_RDB_SYSTEM_PRIVILEGE
  | K_READ
  | K_REAL
  | K_RECREATE
  | K_RECURSIVE
  | K_REFERENCES
  | K_REGR_AVGX
  | K_REGR_AVGY
  | K_REGR_COUNT
  | K_REGR_INTERCEPT
  | K_REGR_R2
  | K_REGR_SLOPE
  | K_REGR_SXX
  | K_REGR_SXY
  | K_REGR_SYY
  | K_RELATIVE
  | K_RELEASE
  | K_REPEATABLE
  | K_REPLACE
  | K_RESTART
  | K_RESTRICT
  | K_RETURN
  | K_RETURNED_LENGTH
  | K_RETURNED_OCTET_LENGTH
  | K_RETURNED_SQLSTATE
  | K_RETURNING
  | K_REVERSE
  | K_REVOKE
  | K_RIGHT
  | K_ROLE
  | K_ROLLBACK
  | K_ROUND
  | K_ROW
  | K_ROWS
  | K_ROW_COUNT
  | K_ROW_NUMBER
  | K_RPAD
  | K_SAVEPOINT
  | K_SCALAR_ARRAY
  | K_SCALE
  | K_SCHEMA
  | K_SCHEMA_NAME
  | K_SCROLL
  | K_SECOND
  | K_SECTION
  | K_SELECT
  | K_SENSITIVE
  | K_SEQUENCE
  | K_SERIALIZABLE
  | K_SERVERWIDE
  | K_SERVER_NAME
  | K_SESSION
  | K_SESSION_USER
  | K_SET
  | K_SIGN
  | K_SIMILAR
  | K_SIN
  | K_SINH
  | K_SIZE
  | K_SKIP
  | K_SOME
  | K_SOURCE
  | K_SPACE
  | K_SQL
  | K_SQLCODE
  | K_SQLERROR
  | K_SQLSTATE
  | K_SQRT
  | K_START
  | K_STATEMENT
  | K_STDDEV_POP
  | K_STDDEV_SAMP
  | K_SUBCLASS_ORIGIN
  | K_SUBSTRING
  | K_SUM
  | K_SYSTEM
  | K_SYSTEM_USER
  | K_TABLE
  | K_TABLE_NAME
  | K_TAGS
  | K_TAN
  | K_TANH
  | K_TEMPORARY
  | K_THEN
  | K_TIES
  | K_TIMEZONE_HOUR
  | K_TIMEZONE_MINUTE
  | K_TO
  | K_TRAILING
  | K_TRANSACTION
  | K_TRANSLATE
  | K_TRANSLATION
  | K_TRIGGER
  | K_TRIM
  | K_TRUE
  | K_TRUNC
  | K_TRUSTED
  | K_TWO_PHASE
  | K_TYPE
  | K_UNBOUNDED
  | K_UNCOMMITTED
  | K_UNION
  | K_UNIQUE
  | K_UNKNOWN
  | K_UNNAMED
  | K_UPDATE
  | K_UPDATING
  | K_UPPER
  | K_USAGE
  | K_USER
  | K_USING
  | K_UUID_TO_CHAR
  | K_VALUE
  | K_VALUES
  | K_VARBINARY
  | K_VAR_POP
  | K_VAR_SAMP
  | K_VIEW
  | K_WEEK
  | K_WHEN
  | K_WHENEVER
  | K_WHERE
  | K_WINDOW
  | K_WITH
  | K_WORK
  | K_WRITE
  | K_YEAR
  | K_ZONE
 ;


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

fragment K_ABS : A B S ;
fragment K_ABSOLUTE : A B S O L U T E ;
fragment K_ACCENT : A C C E N T ;
fragment K_ACOS : A C O S ;
fragment K_ACOSH : A C O S H ;
fragment K_ACTION : A C T I O N ;
fragment K_ADA : A D A ;
fragment K_ADD : A D D ;
fragment K_ALL : A L L ;
fragment K_ALLOCATE : A L L O C A T E ;
fragment K_ALTER : A L T E R ;
fragment K_ALWAYS : A L W A Y S ;
fragment K_AND : A N D ;
fragment K_ANY : A N Y ;
fragment K_ARE : A R E ;
fragment K_AS : A S ;
fragment K_ASC : A S C ;
fragment K_ASCII_CHAR : A S C I I '_' C H A R ;
fragment K_ASCII_VAL : A S C I I '_' V A L ;
fragment K_ASIN : A S I N ;
fragment K_ASINH : A S I N H ;
fragment K_ASSERTION : A S S E R T I O N ;
fragment K_AT : A T ;
fragment K_ATAN : A T A N ;
fragment K_ATAN2 : A T A N '2' ;
fragment K_ATANH : A T A N H ;
fragment K_AUTHORIZATION : A U T H O R I Z A T I O N ;
fragment K_AUTONOMOUS : A U T O N O M O U S ;
fragment K_AVG : A V G ;
fragment K_BACKUP : B A C K U P ;
fragment K_BEGIN : B E G I N ;
fragment K_BETWEEN : B E T W E E N ;
fragment K_BIGINT : B I G I N T ;
fragment K_BINARY : B I N A R Y ;
fragment K_BIN_AND : B I N '_' A N D ;
fragment K_BIN_NOT : B I N '_' N O T ;
fragment K_BIN_OR : B I N '_' O R ;
fragment K_BIN_SHL : B I N '_' S H L ;
fragment K_BIN_SHR : B I N '_' S H R ;
fragment K_BIN_XOR : B I N '_' X O R ;
fragment K_BIT : B I T ;
fragment K_BIT_LENGTH : B I T '_' L E N G T H ;
fragment K_BLOB : B L O B ;
fragment K_BLOCK : B L O C K ;
fragment K_BODY : B O D Y ;
fragment K_BOOLEAN : B O O L E A N ;
fragment K_BOTH : B O T H ;
fragment K_BREAK : B R E A K ;
fragment K_BY : B Y ;
fragment K_C : C ;
fragment K_CALLER : C A L L E R ;
fragment K_CASCADE : C A S C A D E ;
fragment K_CASCADED : C A S C A D E D ;
fragment K_CASE : C A S E ;
fragment K_CAST : C A S T ;
fragment K_CATALOG : C A T A L O G ;
fragment K_CATALOG_NAME : C A T A L O G '_' N A M E ;
fragment K_CEIL : C E I L ;
fragment K_CEILING : C E I L I N G ;
fragment K_CHAR : C H A R ;
fragment K_CHARACTER : C H A R A C T E R ;
fragment K_CHARACTER_LENGTH : C H A R A C T E R '_' L E N G T H ;
fragment K_CHARACTER_SET_CATALOG : C H A R A C T E R '_' S E T '_' C A T A L O G ;
fragment K_CHARACTER_SET_NAME : C H A R A C T E R '_' S E T '_' N A M E ;
fragment K_CHARACTER_SET_SCHEMA : C H A R A C T E R '_' S E T '_' S C H E M A ;
fragment K_CHAR_LENGTH : C H A R '_' L E N G T H ;
fragment K_CHAR_TO_UUID : C H A R '_' T O '_' U U I D ;
fragment K_CHECK : C H E C K ;
fragment K_CLASS_ORIGIN : C L A S S '_' O R I G I N ;
fragment K_CLOSE : C L O S E ;
fragment K_COALESCE : C O A L E S C E ;
fragment K_COBOL : C O B O L ;
fragment K_COLLATE : C O L L A T E ;
fragment K_COLLATION : C O L L A T I O N ;
fragment K_COLLATION_CATALOG : C O L L A T I O N '_' C A T A L O G ;
fragment K_COLLATION_NAME : C O L L A T I O N '_' N A M E ;
fragment K_COLLATION_SCHEMA : C O L L A T I O N '_' S C H E M A ;
fragment K_COLUMN : C O L U M N ;
fragment K_COLUMN_NAME : C O L U M N '_' N A M E ;
fragment K_COMMAND_FUNCTION : C O M M A N D '_' F U N C T I O N ;
fragment K_COMMENT : C O M M E N T ;
fragment K_COMMIT : C O M M I T ;
fragment K_COMMITTED : C O M M I T T E D ;
fragment K_COMMON : C O M M O N ;
fragment K_CONDITION_NUMBER : C O N D I T I O N '_' N U M B E R ;
fragment K_CONNECT : C O N N E C T ;
fragment K_CONNECTION : C O N N E C T I O N ;
fragment K_CONNECTION_NAME : C O N N E C T I O N '_' N A M E ;
fragment K_CONSTRAINT : C O N S T R A I N T ;
fragment K_CONSTRAINTS : C O N S T R A I N T S ;
fragment K_CONSTRAINT_CATALOG : C O N S T R A I N T '_' C A T A L O G ;
fragment K_CONSTRAINT_NAME : C O N S T R A I N T '_' N A M E ;
fragment K_CONSTRAINT_SCHEMA : C O N S T R A I N T '_' S C H E M A ;
fragment K_CONTINUE : C O N T I N U E ;
fragment K_CONVERT : C O N V E R T ;
fragment K_CORR : C O R R ;
fragment K_CORRESPONDING : C O R R E S P O N D I N G ;
fragment K_COS : C O S ;
fragment K_COSH : C O S H ;
fragment K_COT : C O T ;
fragment K_COUNT : C O U N T ;
fragment K_COVAR_POP : C O V A R '_' P O P ;
fragment K_COVAR_SAMP : C O V A R '_' S A M P ;
fragment K_CREATE : C R E A T E ;
fragment K_CROSS : C R O S S ;
fragment K_CUME_DIST : C U M E '_' D I S T ;
fragment K_CURRENT : C U R R E N T ;
fragment K_CURRENT_CONNECTION : C U R R E N T '_' C O N N E C T I O N ;
fragment K_CURRENT_DATE : C U R R E N T '_' D A T E ;
fragment K_CURRENT_ROLE : C U R R E N T '_' R O L E ;
fragment K_CURRENT_TIME : C U R R E N T '_' T I M E ;
fragment K_CURRENT_TIMESTAMP : C U R R E N T '_' T I M E S T A M P ;
fragment K_CURRENT_TRANSACTION : C U R R E N T '_' T R A N S A C T I O N ;
fragment K_CURRENT_USER : C U R R E N T '_' U S E R ;
fragment K_CURSOR : C U R S O R ;
fragment K_CURSOR_NAME : C U R S O R '_' N A M E ;
fragment K_DATA : D A T A ;
fragment K_DATE : D A T E ;
fragment K_DATEADD : D A T E A D D ;
fragment K_DATEDIFF : D A T E D I F F ;
fragment K_DATETIME_INTERVAL_CODE : D A T E T I M E '_' I N T E R V A L '_' C O D E ;
fragment K_DATETIME_INTERVAL_PRECISION : D A T E T I M E '_' I N T E R V A L '_' P R E C I S I O N ;
fragment K_DAY : D A Y ;
fragment K_DDL : D D L ;
fragment K_DEALLOCATE : D E A L L O C A T E ;
fragment K_DEC : D E C ;
fragment K_DECIMAL : D E C I M A L ;
fragment K_DECLARE : D E C L A R E ;
fragment K_DECODE : D E C O D E ;
fragment K_DECRYPT : D E C R Y P T ;
fragment K_DEFAULT : D E F A U L T ;
fragment K_DEFERRABLE : D E F E R R A B L E ;
fragment K_DEFERRED : D E F E R R E D ;
fragment K_DELETE : D E L E T E ;
fragment K_DELETING : D E L E T I N G ;
fragment K_DENSE_RANK : D E N S E '_' R A N K ;
fragment K_DESC : D E S C ;
fragment K_DESCRIBE : D E S C R I B E ;
fragment K_DESCRIPTOR : D E S C R I P T O R ;
fragment K_DETERMINISTIC : D E T E R M I N I S T I C ;
fragment K_DIAGNOSTICS : D I A G N O S T I C S ;
fragment K_DIFFERENCE : D I F F E R E N C E ;
fragment K_DISCONNECT : D I S C O N N E C T ;
fragment K_DISTINCT : D I S T I N C T ;
fragment K_DOMAIN : D O M A I N ;
fragment K_DOUBLE : D O U B L E ;
fragment K_DROP : D R O P ;
fragment K_DYNAMIC_FUNCTION : D Y N A M I C '_' F U N C T I O N ;
fragment K_ELSE : E L S E ;
fragment K_ENCRYPT : E N C R Y P T ;
fragment K_END : E N D ;
fragment K_END_EXEC : E N D '-' E X E C ;
fragment K_ENGINE : E N G I N E ;
fragment K_ESCAPE : E S C A P E ;
fragment K_EXCEPT : E X C E P T ;
fragment K_EXCEPTION : E X C E P T I O N ;
fragment K_EXCLUDE : E X C L U D E ;
fragment K_EXEC : E X E C ;
fragment K_EXECUTE : E X E C U T E ;
fragment K_EXISTS : E X I S T S ;
fragment K_EXP : E X P ;
fragment K_EXTERNAL : E X T E R N A L ;
fragment K_EXTRACT : E X T R A C T ;
fragment K_FALSE : F A L S E ;
fragment K_FETCH : F E T C H ;
fragment K_FIRST : F I R S T ;
fragment K_FIRSTNAME : F I R S T N A M E ;
fragment K_FIRST_VALUE : F I R S T '_' V A L U E ;
fragment K_FLOAT : F L O A T ;
fragment K_FLOOR : F L O O R ;
fragment K_FOLLOWING : F O L L O W I N G ;
fragment K_FOR : F O R ;
fragment K_FOREIGN : F O R E I G N ;
fragment K_FORTRAN : F O R T R A N ;
fragment K_FOUND : F O U N D ;
fragment K_FROM : F R O M ;
fragment K_FULL : F U L L ;
fragment K_GENERATED : G E N E R A T E D ;
fragment K_GEN_UUID : G E N '_' U U I D ;
fragment K_GET : G E T ;
fragment K_GLOBAL : G L O B A L ;
fragment K_GO : G O ;
fragment K_GOTO : G O T O ;
fragment K_GRANT : G R A N T ;
fragment K_GRANTED : G R A N T E D ;
fragment K_GROUP : G R O U P ;
fragment K_HASH : H A S H ;
fragment K_HAVING : H A V I N G ;
fragment K_HOUR : H O U R ;
fragment K_IDENTITY : I D E N T I T Y ;
fragment K_IIF : I I F ;
fragment K_IMMEDIATE : I M M E D I A T E ;
fragment K_IN : I N ;
fragment K_INCREMENT : I N C R E M E N T ;
fragment K_INDEX : I N D E X ;
fragment K_INDICATOR : I N D I C A T O R ;
fragment K_INITIALLY : I N I T I A L L Y ;
fragment K_INNER : I N N E R ;
fragment K_INPUT : I N P U T ;
fragment K_INSENSITIVE : I N S E N S I T I V E ;
fragment K_INSERT : I N S E R T ;
fragment K_INSERTING : I N S E R T I N G ;
fragment K_INT : I N T ;
fragment K_INTEGER : I N T E G E R ;
fragment K_INTERSECT : I N T E R S E C T ;
fragment K_INTERVAL : I N T E R V A L ;
fragment K_INTO : I N T O ;
fragment K_IS : I S ;
fragment K_ISOLATION : I S O L A T I O N ;
fragment K_JOIN : J O I N ;
fragment K_KEY : K E Y ;
fragment K_LAG : L A G ;
fragment K_LANGUAGE : L A N G U A G E ;
fragment K_LAST : L A S T ;
fragment K_LASTNAME : L A S T N A M E ;
fragment K_LAST_VALUE : L A S T '_' V A L U E ;
fragment K_LEAD : L E A D ;
fragment K_LEADING : L E A D I N G ;
fragment K_LEAVE : L E A V E ;
fragment K_LEFT : L E F T ;
fragment K_LENGTH : L E N G T H ;
fragment K_LEVEL : L E V E L ;
fragment K_LIKE : L I K E ;
fragment K_LINGER : L I N G E R ;
fragment K_LIST : L I S T ;
fragment K_LN : L N ;
fragment K_LOCAL : L O C A L ;
fragment K_LOCK : L O C K ;
fragment K_LOG : L O G ;
fragment K_LOG10 : L O G '10' ;
fragment K_LOWER : L O W E R ;
fragment K_LPAD : L P A D ;
fragment K_MAPPING : M A P P I N G ;
fragment K_MATCH : M A T C H ;
fragment K_MATCHED : M A T C H E D ;
fragment K_MATCHING : M A T C H I N G ;
fragment K_MAX : M A X ;
fragment K_MAXVALUE : M A X V A L U E ;
fragment K_MESSAGE_LENGTH : M E S S A G E '_' L E N G T H ;
fragment K_MESSAGE_OCTET_LENGTH : M E S S A G E '_' O C T E T '_' L E N G T H ;
fragment K_MESSAGE_TEXT : M E S S A G E '_' T E X T ;
fragment K_MIDDLENAME : M I D D L E N A M E ;
fragment K_MILLISECOND : M I L L I S E C O N D ;
fragment K_MIN : M I N ;
fragment K_MINUTE : M I N U T E ;
fragment K_MINVALUE : M I N V A L U E ;
fragment K_MOD : M O D ;
fragment K_MODULE : M O D U L E ;
fragment K_MONTH : M O N T H ;
fragment K_MORE : M O R E ;
fragment K_MUMPS : M U M P S ;
fragment K_NAME : N A M E ;
fragment K_NAMES : N A M E S ;
fragment K_NATIONAL : N A T I O N A L ;
fragment K_NATURAL : N A T U R A L ;
fragment K_NCHAR : N C H A R ;
fragment K_NEXT : N E X T ;
fragment K_NO : N O ;
fragment K_NOT : N O T ;
fragment K_NTH_VALUE : N T H '_' V A L U E ;
fragment K_NTILE : N T I L E ;
fragment K_NULL : N U L L ;
fragment K_NULLABLE : N U L L A B L E ;
fragment K_NULLIF : N U L L I F ;
fragment K_NULLS : N U L L S ;
fragment K_NUMBER : N U M B E R ;
fragment K_NUMERIC : N U M E R I C ;
fragment K_OCTET_LENGTH : O C T E T '_' L E N G T H ;
fragment K_OF : O F ;
fragment K_OFFSET : O F F S E T ;
fragment K_ON : O N ;
fragment K_ONLY : O N L Y ;
fragment K_OPEN : O P E N ;
fragment K_OPTION : O P T I O N ;
fragment K_OR : O R ;
fragment K_ORDER : O R D E R ;
fragment K_OS_NAME : O S '_' N A M E ;
fragment K_OTHERS : O T H E R S ;
fragment K_OUTER : O U T E R ;
fragment K_OUTPUT : O U T P U T ;
fragment K_OVER : O V E R ;
fragment K_OVERLAPS : O V E R L A P S ;
fragment K_OVERLAY : O V E R L A Y ;
fragment K_PACKAGE : P A C K A G E ;
fragment K_PAD : P A D ;
fragment K_PARTIAL : P A R T I A L ;
fragment K_PARTITION : P A R T I T I O N ;
fragment K_PASCAL : P A S C A L ;
fragment K_PERCENT_RANK : P E R C E N T '_' R A N K ;
fragment K_PI : P I ;
fragment K_PLACING : P L A C I N G ;
fragment K_PLI : P L I ;
fragment K_PLUGIN : P L U G I N ;
fragment K_POSITION : P O S I T I O N ;
fragment K_POWER : P O W E R ;
fragment K_PRECEDING : P R E C E D I N G ;
fragment K_PRECISION : P R E C I S I O N ;
fragment K_PREPARE : P R E P A R E ;
fragment K_PRESERVE : P R E S E R V E ;
fragment K_PRIMARY : P R I M A R Y ;
fragment K_PRIOR : P R I O R ;
fragment K_PRIVILEGE : P R I V I L E G E ;
fragment K_PRIVILEGES : P R I V I L E G E S ;
fragment K_PROCEDURE : P R O C E D U R E ;
fragment K_PUBLIC : P U B L I C ;
fragment K_RAND : R A N D ;
fragment K_RANGE : R A N G E ;
fragment K_RANK : R A N K ;
fragment K_RDB_RECORD_VERSION : R D B '$' R E C O R D '_' V E R S I O N ;
fragment K_RDB_ROLE_IN_USE : R D B '$' R O L E '_' I N '_' U S E ;
fragment K_RDB_SYSTEM_PRIVILEGE : R D B '$' S Y S T E M '_' P R I V I L E G E ;
fragment K_READ : R E A D ;
fragment K_REAL : R E A L ;
fragment K_RECREATE : R E C R E A T E ;
fragment K_RECURSIVE : R E C U R S I V E ;
fragment K_REFERENCES : R E F E R E N C E S ;
fragment K_REGR_AVGX : R E G R '_' A V G X ;
fragment K_REGR_AVGY : R E G R '_' A V G Y ;
fragment K_REGR_COUNT : R E G R '_' C O U N T ;
fragment K_REGR_INTERCEPT : R E G R '_' I N T E R C E P T ;
fragment K_REGR_R2 : R E G R '_' R '2' ;
fragment K_REGR_SLOPE : R E G R '_' S L O P E ;
fragment K_REGR_SXX : R E G R '_' S X X ;
fragment K_REGR_SXY : R E G R '_' S X Y ;
fragment K_REGR_SYY : R E G R '_' S Y Y ;
fragment K_RELATIVE : R E L A T I V E ;
fragment K_RELEASE : R E L E A S E ;
fragment K_REPEATABLE : R E P E A T A B L E ;
fragment K_REPLACE : R E P L A C E ;
fragment K_RESTART : R E S T A R T ;
fragment K_RESTRICT : R E S T R I C T ;
fragment K_RETURN : R E T U R N ;
fragment K_RETURNED_LENGTH : R E T U R N E D '_' L E N G T H ;
fragment K_RETURNED_OCTET_LENGTH : R E T U R N E D '_' O C T E T '_' L E N G T H ;
fragment K_RETURNED_SQLSTATE : R E T U R N E D '_' S Q L S T A T E ;
fragment K_RETURNING : R E T U R N I N G ;
fragment K_REVERSE : R E V E R S E ;
fragment K_REVOKE : R E V O K E ;
fragment K_RIGHT : R I G H T ;
fragment K_ROLE : R O L E ;
fragment K_ROLLBACK : R O L L B A C K ;
fragment K_ROUND : R O U N D ;
fragment K_ROW : R O W ;
fragment K_ROWS : R O W S ;
fragment K_ROW_COUNT : R O W '_' C O U N T ;
fragment K_ROW_NUMBER : R O W '_' N U M B E R ;
fragment K_RPAD : R P A D ;
fragment K_SAVEPOINT : S A V E P O I N T ;
fragment K_SCALAR_ARRAY : S C A L A R '_' A R R A Y ;
fragment K_SCALE : S C A L E ;
fragment K_SCHEMA : S C H E M A ;
fragment K_SCHEMA_NAME : S C H E M A '_' N A M E ;
fragment K_SCROLL : S C R O L L ;
fragment K_SECOND : S E C O N D ;
fragment K_SECTION : S E C T I O N ;
fragment K_SELECT : S E L E C T ;
fragment K_SENSITIVE : S E N S I T I V E ;
fragment K_SEQUENCE : S E Q U E N C E ;
fragment K_SERIALIZABLE : S E R I A L I Z A B L E ;
fragment K_SERVERWIDE : S E R V E R W I D E ;
fragment K_SERVER_NAME : S E R V E R '_' N A M E ;
fragment K_SESSION : S E S S I O N ;
fragment K_SESSION_USER : S E S S I O N '_' U S E R ;
fragment K_SET : S E T ;
fragment K_SIGN : S I G N ;
fragment K_SIMILAR : S I M I L A R ;
fragment K_SIN : S I N ;
fragment K_SINH : S I N H ;
fragment K_SIZE : S I Z E ;
fragment K_SKIP : S K I P ;
fragment K_SMALLINT : S M A L L I N T ;
fragment K_SOME : S O M E ;
fragment K_SOURCE : S O U R C E ;
fragment K_SPACE : S P A C E ;
fragment K_SQL : S Q L ;
fragment K_SQLCODE : S Q L C O D E ;
fragment K_SQLERROR : S Q L E R R O R ;
fragment K_SQLSTATE : S Q L S T A T E ;
fragment K_SQRT : S Q R T ;
fragment K_START : S T A R T ;
fragment K_STATEMENT : S T A T E M E N T ;
fragment K_STDDEV_POP : S T D D E V '_' P O P ;
fragment K_STDDEV_SAMP : S T D D E V '_' S A M P ;
fragment K_SUBCLASS_ORIGIN : S U B C L A S S '_' O R I G I N ;
fragment K_SUBSTRING : S U B S T R I N G ;
fragment K_SUM : S U M ;
fragment K_SYSTEM : S Y S T E M ;
fragment K_SYSTEM_USER : S Y S T E M '_' U S E R ;
fragment K_TABLE : T A B L E ;
fragment K_TABLE_NAME : T A B L E '_' N A M E ;
fragment K_TAGS : T A G S ;
fragment K_TAN : T A N ;
fragment K_TANH : T A N H ;
fragment K_TEMPORARY : T E M P O R A R Y ;
fragment K_THEN : T H E N ;
fragment K_TIES : T I E S ;
fragment K_TIME : T I M E ;
fragment K_TIMESTAMP : T I M E S T A M P ;
fragment K_TIMEZONE_HOUR : T I M E Z O N E '_' H O U R ;
fragment K_TIMEZONE_MINUTE : T I M E Z O N E '_' M I N U T E ;
fragment K_TO : T O ;
fragment K_TRAILING : T R A I L I N G ;
fragment K_TRANSACTION : T R A N S A C T I O N ;
fragment K_TRANSLATE : T R A N S L A T E ;
fragment K_TRANSLATION : T R A N S L A T I O N ;
fragment K_TRIGGER : T R I G G E R ;
fragment K_TRIM : T R I M ;
fragment K_TRUE : T R U E ;
fragment K_TRUNC : T R U N C ;
fragment K_TRUSTED : T R U S T E D ;
fragment K_TWO_PHASE : T W O '_' P H A S E ;
fragment K_TYPE : T Y P E ;
fragment K_UNBOUNDED : U N B O U N D E D ;
fragment K_UNCOMMITTED : U N C O M M I T T E D ;
fragment K_UNION : U N I O N ;
fragment K_UNIQUE : U N I Q U E ;
fragment K_UNKNOWN : U N K N O W N ;
fragment K_UNNAMED : U N N A M E D ;
fragment K_UPDATE : U P D A T E ;
fragment K_UPDATING : U P D A T I N G ;
fragment K_UPPER : U P P E R ;
fragment K_USAGE : U S A G E ;
fragment K_USER : U S E R ;
fragment K_USING : U S I N G ;
fragment K_UUID_TO_CHAR : U U I D '_' T O '_' C H A R ;
fragment K_VALUE : V A L U E ;
fragment K_VALUES : V A L U E S ;
fragment K_VARBINARY : V A R B I N A R Y ;
fragment K_VARCHAR : V A R C H A R ;
fragment K_VARYING : V A R Y I N G ;
fragment K_VAR_POP : V A R '_' P O P ;
fragment K_VAR_SAMP : V A R '_' S A M P ;
fragment K_VIEW : V I E W ;
fragment K_WEEK : W E E K ;
fragment K_WHEN : W H E N ;
fragment K_WHENEVER : W H E N E V E R ;
fragment K_WHERE : W H E R E ;
fragment K_WINDOW : W I N D O W ;
fragment K_WITH : W I T H ;
fragment K_WORK : W O R K ;
fragment K_WRITE : W R I T E ;
fragment K_YEAR : Y E A R ;
fragment K_ZONE : Z O N E ;



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
ERROR_CHAR:.;