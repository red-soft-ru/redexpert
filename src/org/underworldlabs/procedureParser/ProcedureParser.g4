grammar ProcedureParser;

 queries
 :query*
 ;
 query
 :create_or_alter_procedure_stmt
 |execute_block_stmt
 |.* ';'
 ;

 create_procedure_stmt
 :K_CREATE spases_or_comment K_PROCEDURE spases_or_comment procedure_name
    (K_AUTHID spases_or_comment (K_OWNER|K_CALLER))?
    declare_block
 ;

 create_or_alter_procedure_stmt
 :K_CREATE spases_or_comment K_OR spases_or_comment K_ALTER spases_or_comment K_PROCEDURE spases_or_comment procedure_name
      (K_AUTHID (K_OWNER|K_CALLER))?
      declare_block
 |recreate_procedure_stmt
 |alter_procedure_stmt
 |create_procedure_stmt
 ;

 declare_procedure_stmt
  :K_DECLARE spases_or_comment K_PROCEDURE spases_or_comment procedure_name
     (K_AUTHID (K_OWNER|K_CALLER))?
     declare_block
  ;

declare_function_stmt
:K_DECLARE spases_or_comment K_FUNCTION spases_or_comment procedure_name
declare_function_block
;

 recreate_procedure_stmt
 :K_RECREATE  K_PROCEDURE procedure_name
      (K_AUTHID (K_OWNER|K_CALLER))?
      declare_block
 ;

 alter_procedure_stmt
  : K_ALTER K_PROCEDURE procedure_name
     (K_AUTHID (K_OWNER|K_CALLER))?
     declare_block
  ;

 execute_block_stmt
  :spases_or_comment? K_EXECUTE spases_or_comment K_BLOCK
     declare_block
  ;

 declare_function_block
 : ('('spases_or_comment? input_parameter (','spases_or_comment? input_parameter)*')')?
       K_RETURNS spases_or_comment  output_parameter
       spases_or_comment? K_AS spases_or_comment
       declare_stmt*
       K_BEGIN
       body
       K_END
;

 declare_block
 : spases_or_comment?('('spases_or_comment? input_parameter (','spases_or_comment? input_parameter)* spases_or_comment?')')?
       (spases_or_comment? K_RETURNS spases_or_comment '('spases_or_comment?  output_parameter (',' spases_or_comment? output_parameter)*')')?
       spases_or_comment? K_AS spases_or_comment
       declare_stmt*
       K_BEGIN
       body
       K_END
       spases_or_comment?
;

declare_stmt
:local_variable
 |declare_procedure_stmt
 |declare_function_stmt
 ;

declare_block_without_params
:spases_or_comment?
declare_stmt*
full_body
;

full_body
:spases_or_comment* K_BEGIN .*
;

body:
  ~(K_END)*
  | (~(K_BEGIN|K_END|K_CASE)* (K_BEGIN|K_CASE) body K_END ~(K_END)*)*
  ;

 local_variable
 :K_DECLARE spases_or_comment (K_VARIABLE spases_or_comment)? variable_name
 (spases_or_comment cursor|
  spases_or_comment datatype
  (spases_or_comment notnull)?
  (spases_or_comment K_COLLATE spases_or_comment order_collate)?
  (spases_or_comment default_statement)?)
  (spases_or_comment)?';' SPACES* comment? SPACES*
  ;

  default_statement:
  ('=' | K_DEFAULT (spases_or_comment '=')?) spases_or_comment default_value
  ;

  cursor:
  K_CURSOR spases_or_comment K_FOR (spases_or_comment scroll)? spases_or_comment operator_select
  ;

  operator_select:
 '(' operator_select_in ')'
 ;

 operator_select_in:
 (~';'|COMMENT)*
   |.* '(' operator_select_in ')'.*
 ;


  scroll:
  K_SCROLL | K_NO spases_or_comment K_SCROLL;

  notnull:
  K_NOT spases_or_comment K_NULL
  ;

 output_parameter
 :desciption_parameter
 ;

  default_value
  :literal_value
  |BIND_PARAMETER
  ;


 variable_name
 : IDENTIFIER
   | STRING_LITERAL
 ;

 input_parameter
 :desciption_parameter (spases_or_comment default_statement)?
 ;

 desciption_parameter
 :parameter_name spases_or_comment datatype (spases_or_comment notnull)? (spases_or_comment K_COLLATE spases_or_comment order_collate)?
 ;
 parameter_name
 : any_name
 ;


 datatype
 : datatypeSQL
 | domain_name
 | type_of
 ;

 type_of
 :K_TYPE spases_or_comment K_OF spases_or_comment domain_name
  | K_TYPE spases_or_comment K_OF spases_or_comment K_COLUMN spases_or_comment table_name'.'column_name
;
 datatypeSQL
 : K_BOOLEAN array_size?
    | (K_SMALLINT | K_INTEGER | K_BIGINT | K_INT128 | K_INT | K_LONG )spases_or_comment? array_size?
    | (K_FLOAT | K_REAL ) spases_or_comment?('('scale')')?spases_or_comment? array_size?
    | (K_DOUBLE spases_or_comment K_PRECISION| K_LONG spases_or_comment K_FLOAT) spases_or_comment? array_size?
    | (K_DATE | K_TIME | K_TIMESTAMP| ((K_TIME | K_TIMESTAMP) spases_or_comment (K_WITH|K_WITHOUT) spases_or_comment K_TIME spases_or_comment K_ZONE )) spases_or_comment? array_size?
    | (K_DECIMAL | K_NUMERIC | K_DECFLOAT| K_DEC)spases_or_comment? ('(' type_size spases_or_comment?(',' spases_or_comment? scale)?')')?spases_or_comment? array_size?
    | (K_CHAR | K_CHARACTER | K_VARYING spases_or_comment K_CHARACTER | K_VARCHAR | K_VARBINARY ) spases_or_comment?('('type_size')')?
    (spases_or_comment K_CHARACTER spases_or_comment K_SET spases_or_comment charset_name)?spases_or_comment? array_size?
    | (K_NCHAR | K_BINARY | K_NATIONAL spases_or_comment K_CHARACTER | K_NATIONAL spases_or_comment K_CHAR) (spases_or_comment K_VARYING)? spases_or_comment?('(' spases_or_comment? type_size spases_or_comment?')')? spases_or_comment? array_size?
    | K_BLOB (spases_or_comment K_SUB_TYPE spases_or_comment subtype)?
    (spases_or_comment K_SEGMENT spases_or_comment K_SIZE spases_or_comment type_size)? (spases_or_comment K_CHARACTER spases_or_comment K_SET spases_or_comment charset_name)?
    | K_BLOB ('('spases_or_comment? (type_size spases_or_comment?)? (','spases_or_comment? subtype spases_or_comment?)?')')?
 ;

scale
: int_number
;

subtype
:(any_name | int_number)
;

type_size
: int_number
 ;

segment_size :
 int_number
;

int_number:
INT_LITERAL;

 array_size : '[' (INT_LITERAL ':')? INT_LITERAL (',' (INT_LITERAL ':')? INT_LITERAL)* ']'
 ;
 order_collate
  : K_ASC | K_DESC
  ;
signed_number
 : ( ( '+' | '-' )? NUMERIC_LITERAL | '*' )
 ;

literal_value
 : NUMERIC_LITERAL
 | INT_LITERAL
 | STRING_LITERAL
 | BLOB_LITERAL
 | K_NULL
 | K_CURRENT_TIME
 | K_CURRENT_DATE
 | K_CURRENT_TIMESTAMP
 ;

unary_operator
 : '-'
 | '+'
 | '~'
 | K_NOT
 ;

error_message
 : STRING_LITERAL
 ;


column_alias
 : IDENTIFIER
 | STRING_LITERAL
 ;

keyword
 : datatypeKeyword
       | K_ABS
       | K_ABSENT
       | K_ABSOLUTE
       | K_ACCENT
       | K_ACOS
       | K_ACOSH
       | K_ACTION
       | K_ACTIVE
       | K_ADAPTER
       | K_ADD
       | K_ADMIN
       | K_AFTER
       | K_ALL
       | K_ALTER
       | K_ALWAYS
       | K_AND
       | K_ANY
       | K_ARRAY
       | K_AS
       | K_ASC
       | K_ASCENDING
       | K_ASCII_CHAR
       | K_ASCII_VAL
       | K_ASIN
       | K_ASINH
       | K_AT
       | K_ATAN
       | K_ATAN2
       | K_ATANH
       | K_AUTHID
       | K_AUTH_FACTORS
       | K_AUTO
       | K_AUTONOMOUS
       | K_AVG
       | K_BACKUP
       | K_BASE64_DECODE
       | K_BASE64_ENCODE
       | K_BEFORE
       | K_BEGIN
       | K_BETWEEN
       | K_BIGINT
       | K_BIN_AND
       | K_BIN_NOT
       | K_BIN_OR
       | K_BIN_SHL
       | K_BIN_SHR
       | K_BIN_XOR
       | K_BINARY
       | K_BIND
       | K_BIT_LENGTH
       | K_BLOB
       | K_BLOB_APPEND
       | K_BLOCK
       | K_BODY
       | K_BOOLEAN
       | K_BOTH
       | K_BREAK
       | K_BY
       | K_CALLER
       | K_CASCADE
       | K_CASE
       | K_CAST
       | K_CEIL
       | K_CEILING
       | K_CERTIFICATE
       | K_CHAR
       | K_CHAR_LENGTH
       | K_CHAR_TO_UUID
       | K_CHARACTER
       | K_CHARACTER_LENGTH
       | K_CHECK
       | K_CLEAR
       | K_CLOSE
       | K_COALESCE
       | K_COLLATE
       | K_COLLATION
       | K_COLUMN
       | K_COLUMNS
       | K_COMMAND
       | K_COMMENT
       | K_COMMIT
       | K_COMMITTED
       | K_COMMON
       | K_COMPARE_DECFLOAT
       | K_COMPUTED
       | K_CONDITIONAL
       | K_CONNECT
       | K_CONNECTIONS
       | K_CONSISTENCY
       | K_CONSTRAINT
       | K_CONTAINING
       | K_CONTENTS
       | K_CONTINUE
       | K_CORR
       | K_COS
       | K_COSH
       | K_COT
       | K_COUNT
       | K_COUNTER
       | K_COVAR_POP
       | K_COVAR_SAMP
       | K_CPU_LOAD
       | K_CREATE
       | K_CREATE_FILE
       | K_CROSS
       | K_CRYPT_HASH
       | K_CSTRING
       | K_CTR_BIG_ENDIAN
       | K_CTR_LENGTH
       | K_CTR_LITTLE_ENDIAN
       | K_CUME_DIST
       | K_CURRENT
       | K_CURRENT_CONNECTION
       | K_CURRENT_DATE
       | K_CURRENT_LABEL
       | K_CURRENT_ROLE
       | K_CURRENT_TIME
       | K_CURRENT_TIMESTAMP
       | K_CURRENT_TRANSACTION
       | K_CURRENT_USER
       | K_CURSOR
       | K_DAMLEV
       | K_DATABASE
       | K_DATA
       | K_DATE
       | K_DATEADD
       | K_DATEDIFF
       | K_DAY
       | K_DDL
       | K_DEBUG
       | K_DEC
       | K_DECFLOAT
       | K_DECIMAL
       | K_DECLARE
       | K_DECODE
       | K_DECRYPT
       | K_DEFAULT
       | K_DEFINER
       | K_DELETE
       | K_DELETE_FILE
       | K_DELETING
       | K_DENSE_RANK
       | K_DESC
       | K_DESCENDING
       | K_DESCRIPTOR
       | K_DETERMINISTIC
       | K_DIFFERENCE
       | K_DISABLE
       | K_DISCONNECT
       | K_DISTINCT
       | K_DO
       | K_DOMAIN
       | K_DOUBLE
       | K_DROP
       | K_DUMP
       | K_ELSE
       | K_EMPTY
       | K_ENABLE
       | K_ENCRYPT
       | K_END
       | K_ENGINE
       | K_ENTRY_POINT
       | K_ERROR
       | K_ESCAPE
       | K_EXCEPTION
       | K_EXCESS
       | K_EXCLUDE
       | K_EXECUTE
       | K_EXISTS
       | K_EXIT
       | K_EXP
       | K_EXTENDED
       | K_EXTERNAL
       | K_EXTRACT
       | K_FALSE
       | K_FETCH
       | K_FILE
       | K_FILTER
       | K_FIRST
       | K_FIRST_DAY
       | K_FIRST_VALUE
       | K_FIRSTNAME
       | K_FLOAT
       | K_FLOOR
       | K_FOLLOWING
       | K_FOR
       | K_FOREIGN
       | K_FORMAT
       | K_FREE_IT
       | K_FROM
       | K_FULL
       | K_FUNCTION
       | K_GDSCODE
       | K_GENERATED
       | K_GENERATOR
       | K_GEN_ID
       | K_GEN_UUID
       | K_GLOBAL
       | K_GOSTPASSWORD
       | K_GRANT
       | K_GRANTED
       | K_GROUP
       | K_GSS
       | K_HASH
       | K_HASH_CP
       | K_HASHAGG
       | K_HAVING
       | K_HEX_DECODE
       | K_HEX_ENCODE
       | K_HOUR
       | K_IDENTITY
       | K_IDLE
       | K_IF
       | K_IGNORE
       | K_IIF
       | K_IN
       | K_INACTIVE
       | K_INCLUDE
       | K_INCLUDING
       | K_INCREMENT
       | K_INDEX
       | K_INITIAL_LABEL
       | K_INNER
       | K_INPUT_TYPE
       | K_INSENSITIVE
       | K_INSERT
       | K_INSERTING
       | K_INT
       | K_INT128
       | K_INTEGER
       | K_INTO
       | K_INVOKER
       | K_IS
       | K_IS_LABEL_VALID
       | K_ISOLATION
       | K_IV
       | K_JOB
       | K_JOIN
       | K_JSON
       | K_JSON_ARRAY
       | K_JSON_ARRAYAGG
       | K_JSON_EXISTS
       | K_JSON_MODIFY
       | K_JSON_OBJECT
       | K_JSON_OBJECTAGG
       | K_JSON_QUERY
       | K_JSON_TABLE
       | K_JSON_VALUE
       | K_KEEP
       | K_KEY
       | K_KEYS
       | K_LAG
       | K_LAST
       | K_LAST_DAY
       | K_LAST_VALUE
       | K_LASTNAME
       | K_LDAP_ATTR
       | K_LDAP_GROUPS
       | K_LDAP_USER_GROUPS
       | K_LEAD
       | K_LEADING
       | K_LEAVE
       | K_LEFT
       | K_LEGACY
       | K_LENGTH
       | K_LEVEL
       | K_LIFETIME
       | K_LIKE
       | K_LIMBO
       | K_LINGER
       | K_LIST
       | K_LN
       | K_LATERAL
       | K_LOCAL
       | K_LOCALTIME
       | K_LOCALTIMESTAMP
       | K_LOCK
       | K_LOCKED
       | K_LOG
       | K_LOG10
       | K_LONG
       | K_LOWER
       | K_LPAD
       | K_LPARAM
       | K_MAKE_DBKEY
       | K_MANUAL
       | K_MAPPING
       | K_MATCHED
       | K_MATCHING
       | K_MAX_FAILED_COUNT
       | K_MAX_SESSIONS
       | K_MAX_IDLE_TIME
       | K_MAX_UNUSED_DAYS
       | K_MAX
       | K_MAXVALUE
       | K_MERGE
       | K_MESSAGE
       | K_MILLISECOND
       | K_MIDDLENAME
       | K_MIN
       | K_MINUTE
       | K_MINVALUE
       | K_MOD
       | K_MODE
       | K_MODULE_NAME
       | K_MONTH
       | K_NAME
       | K_NAMES
       | K_NATIONAL
       | K_NATIVE
       | K_NATURAL
       | K_NCHAR
       | K_NEXT
       | K_NO
       | K_NORMALIZE_DECFLOAT
       | K_NOT
       | K_NTH_VALUE
       | K_NTILE
       | K_NULLIF
       | K_NULL
       | K_NULLS
       | K_NUMBER
       | K_NUMERIC
       | K_OBJECT
       | K_OCTET_LENGTH
       | K_OF
       | K_OFFSET
       | K_OLDEST
       | K_OMIT
       | K_ON
       | K_ONCE
       | K_OFFLINE
       | K_ONLINE
       | K_ONLY
       | K_OPEN
       | K_OPTIMIZE
       | K_OPTION
       | K_OR
       | K_ORDER
       | K_OS_NAME
       | K_OTHERS
       | K_OUTER
       | K_OUTPUT_TYPE
       | K_OVER
       | K_OVERFLOW
       | K_OVERLAY
       | K_OVERRIDING
       | K_OWNER
       | K_PACKAGE
       | K_PAD
       | K_PAGE
       | K_PAGES
       | K_PAGE_SIZE
       | K_PARAMETER
       | K_PARTITION
       | K_PASSWORD
       | K_PERCENT_RANK
       | K_PI
       | K_PIN
       | K_PKCS_1_5
       | K_PLACING
       | K_PLAN
       | K_PLUGIN
       | K_POLICY
       | K_POOL
       | K_POSITION
       | K_POST_EVENT
       | K_POWER
       | K_PRECEDING
       | K_PRECISION
       | K_PRESERVE
       | K_PRIMARY
       | K_PRIOR
       | K_PRIVILEGE
       | K_PRIVILEGES
       | K_PROCEDURE
       | K_PROTECTED
       | K_PSWD_NEED_CHAR
       | K_PSWD_NEED_DIGIT
       | K_PSWD_NEED_DIFF_CASE
       | K_PSWD_MIN_LEN
       | K_PSWD_VALID_DAYS
       | K_PSWD_UNIQUE_COUNT
       | K_PUBLICATION
       | K_QUANTIZE
       | K_QUOTES
       | K_RAND
       | K_RANGE
       | K_RANK
       | K_RDB_DB_KEY
       | K_RDB_ERROR
       | K_RDB_GET_CONTEXT
       | K_RDB_GET_TRANSACTION_CN
       | K_RDB_RECORD_VERSION
       | K_RDB_ROLE_IN_USE
       | K_RDB_SET_CONTEXT
       | K_RDB_SYSTEM_PRIVILEGE
       | K_READ
       | K_READ_FILE
       | K_REAL
       | K_RECORD_VERSION
       | K_RECREATE
       | K_RECURSIVE
       | K_REFERENCES
       | K_REGEXP_SUBSTR
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
       | K_REPLACE
       | K_REQUESTS
       | K_RESERV
       | K_RESERVING
       | K_RESET
       | K_RESETTING
       | K_RESTART
       | K_RESTRICT
       | K_RETAIN
       | K_RETURN
       | K_RETURNING
       | K_RETURNING_VALUES
       | K_RETURNS
       | K_REVERSE
       | K_REVOKE
       | K_RIGHT
       | K_ROLE
       | K_ROLLBACK
       | K_ROUND
       | K_ROW
       | K_ROW_COUNT
       | K_ROW_NUMBER
       | K_ROWS
       | K_RPAD
       | K_RSA_DECRYPT
       | K_RSA_ENCRYPT
       | K_RSA_PRIVATE
       | K_RSA_PUBLIC
       | K_RSA_SIGN_HASH
       | K_RSA_VERIFY_HASH
       | K_RUN
       | K_SALT_LENGTH
       | K_SAVEPOINT
       | K_SCALAR
       | K_SCALAR_ARRAY
       | K_SCHEMA
       | K_SCROLL
       | K_SECOND
       | K_SECURITY
       | K_SEGMENT
       | K_SELECT
       | K_SENSITIVE
       | K_SEQUENCE
       | K_SERVERWIDE
       | K_SESSION
       | K_SET
       | K_SHADOW
       | K_SHARED
       | K_SIGN
       | K_SIGNATURE
       | K_SIMILAR
       | K_SIN
       | K_SINGULAR
       | K_SINH
       | K_SIZE
       | K_SKIP
       | K_SMALLINT
       | K_SNAPSHOT
       | K_SOME
       | K_SORT
       | K_SOURCE
       | K_SPACE
       | K_SQL
       | K_SQLCODE
       | K_SQLSTATE
       | K_SQRT
       | K_SRP
       | K_STABILITY
       | K_START
       | K_STARTING
       | K_STARTS
       | K_STATEMENT
       | K_STATISTICS
       | K_STDDEV_POP
       | K_STDDEV_SAMP
       | K_STRING
       | K_SUBSTRING
       | K_SUB_TYPE
       | K_SUM
       | K_SUSPEND
       | K_SYSTEM
       | K_TABLE
       | K_TABLESPACE
       | K_TAGS
       | K_TAN
       | K_TANH
       | K_TARGET
       | K_TEMPORARY
       | K_THEN
       | K_TIES
       | K_TIME
       | K_TIMESTAMP
       | K_TIMEOUT
       | K_TIMEZONE_HOUR
       | K_TIMEZONE_MINUTE
       | K_TIMEZONE_NAME
       | K_TO
       | K_TOTALORDER
       | K_TRAILING
       | K_TRANSACTION
       | K_TRAPS
       | K_TRIGGER
       | K_TRIM
       | K_TRUE
       | K_TRUNC
       | K_TRUSTED
       | K_TWO_PHASE
       | K_TYPE
       | K_UNBOUNDED
       | K_UNCOMMITTED
       | K_UNCONDITIONAL
       | K_UNDO
       | K_UNICODE_CHAR
       | K_UNICODE_VAL
       | K_UNION
       | K_UNIQUE
       | K_UNKNOWN
       | K_UPDATE
       | K_UPDATING
       | K_UPPER
       | K_USAGE
       | K_USER
       | K_USING
       | K_UTC_TIMESTAMP
       | K_UUID_TO_CHAR
       | K_VALUE
       | K_VALUES
       | K_VAR_POP
       | K_VAR_SAMP
       | K_VARBINARY
       | K_VARCHAR
       | K_VARIABLE
       | K_VARYING
       | K_VERIFYSERVER
       | K_VIEW
       | K_WAIT
       | K_WEEK
       | K_WEEKDAY
       | K_WHEN
       | K_WHERE
       | K_WHILE
       | K_WIN_SSPI
       | K_WINDOW
       | K_WITH
       | K_WITHOUT
       | K_WORK
       | K_WRAPPER
       | K_WRITE
       | K_YEAR
       | K_YEARDAY
       | K_ZONE
     ;

datatypeKeyword
 : K_BIGINT
 | K_BLOB
 | K_BOOLEAN
 | K_CHAR
 | K_CHARACTER
 | K_DATE
 | K_DECIMAL
 | K_DECFLOAT
 | K_DOUBLE
 | K_FLOAT
 | K_INTEGER
 | K_INT
 | K_INT128
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

//[a-zA-Z_0-9\t \-\[\]\=]+
unknown
 : .+
 ;

name
 : any_name
 ;

function_name
 : any_name
 ;

database_name
 : any_name
 ;

 domain_name
  : any_name
  ;

source_table_name
 : any_name
 ;

table_name
 : any_name
 ;

procedure_name
: any_name
;

table_or_index_name
 : any_name
 ;

new_table_name
 : any_name
 ;

column_name
 : any_name
 ;

collation_name
 : any_name
 ;

foreign_table
 : any_name
 ;

index_name
 : any_name
 ;

trigger_name
 : any_name
 ;

view_name
 : any_name
 ;

module_name
 : any_name
 ;

pragma_name
 : any_name
 ;

savepoint_name
 : any_name
 ;

table_alias
 : any_name
 ;

transaction_name
 : any_name
 ;

charset_name
 : any_name
 ;

any_name
 : IDENTIFIER
 | keyword
 | STRING_LITERAL
 | '(' any_name ')'
 ;

SCOL : ';';
DOT : '.';
OPEN_PAR : '(';
CLOSE_PAR : ')';
COMMA : ',';
ASSIGN : '=';
STAR : '*';
PLUS : '+';
MINUS : '-';
TILDE : '~';
PIPE2 : '||';
DIV : '/';
MOD : '%';
LT2 : '<<';
GT2 : '>>';
AMP : '&';
PIPE : '|';
LT : '<';
LT_EQ : '<=';
GT : '>';
GT_EQ : '>=';
EQ : '==';
NOT_EQ1 : '!=';
NOT_EQ2 : '<>';

K_ABS : A B S ;
K_ABSENT : A B S E N T ;
K_ABSOLUTE : A B S O L U T E ;
K_ACCENT : A C C E N T ;
K_ACOS : A C O S ;
K_ACOSH : A C O S H ;
K_ACTION : A C T I O N ;
K_ACTIVE : A C T I V E ;
K_ADAPTER : A D A P T E R ;
K_ADD : A D D ;
K_ADMIN : A D M I N ;
K_AFTER : A F T E R ;
K_ALL : A L L ;
K_ALTER : A L T E R ;
K_ALWAYS : A L W A Y S ;
K_AND : A N D ;
K_ANY : A N Y ;
K_ARRAY : A R R A Y ;
K_AS : A S ;
K_ASC : A S C ;
K_ASCENDING : A S C E N D I N G ;
K_ASCII_CHAR : A S C I I '_' C H A R ;
K_ASCII_VAL : A S C I I '_' V A L ;
K_ASIN : A S I N ;
K_ASINH : A S I N H ;
K_AT : A T ;
K_ATAN : A T A N ;
K_ATAN2 : A T A N '2' ;
K_ATANH : A T A N H ;
K_AUTHID : A U T H I D ;
K_AUTH_FACTORS : A U T H '_' F A C T O R S ;
K_AUTO : A U T O ;
K_AUTONOMOUS : A U T O N O M O U S ;
K_AVG : A V G ;
K_BACKUP : B A C K U P ;
K_BASE64_DECODE : B A S E '6' '4' '_' D E C O D E ;
K_BASE64_ENCODE : B A S E '6' '4' '_' E N C O D E ;
K_BEFORE : B E F O R E ;
K_BEGIN : B E G I N ;
K_BETWEEN : B E T W E E N ;
K_BIGINT : B I G I N T ;
K_BIN_AND : B I N '_' A N D ;
K_BIN_NOT : B I N '_' N O T ;
K_BIN_OR : B I N '_' O R ;
K_BIN_SHL : B I N '_' S H L ;
K_BIN_SHR : B I N '_' S H R ;
K_BIN_XOR : B I N '_' X O R ;
K_BINARY : B I N A R Y ;
K_BIND : B I N D ;
K_BIT_LENGTH : B I T '_' L E N G T H ;
K_BLOB : B L O B ;
K_BLOB_APPEND : B L O B '_' A P P E N D ;
K_BLOCK : B L O C K ;
K_BODY : B O D Y ;
K_BOOLEAN : B O O L E A N ;
K_BOTH : B O T H ;
K_BREAK : B R E A K ;
K_BY : B Y ;
K_CALLER : C A L L E R ;
K_CASCADE : C A S C A D E ;
K_CASE : C A S E ;
K_CAST : C A S T ;
K_CEIL : C E I L ;
K_CEILING : C E I L I N G ;
K_CERTIFICATE : C E R T I F I C A T E ;
K_CHAR : C H A R ;
K_CHAR_LENGTH : C H A R '_' L E N G T H ;
K_CHAR_TO_UUID : C H A R '_' T O '_' U U I D ;
K_CHARACTER : C H A R A C T E R ;
K_CHARACTER_LENGTH : C H A R A C T E R '_' L E N G T H ;
K_CHECK : C H E C K ;
K_CLEAR : C L E A R ;
K_CLOSE : C L O S E ;
K_COALESCE : C O A L E S C E ;
K_COLLATE : C O L L A T E ;
K_COLLATION : C O L L A T I O N ;
K_COLUMN : C O L U M N ;
K_COLUMNS : C O L U M N S ;
K_COMMAND : C O M M A N D ;
K_COMMENT : C O M M E N T ;
K_COMMIT : C O M M I T ;
K_COMMITTED : C O M M I T T E D ;
K_COMMON : C O M M O N ;
K_COMPARE_DECFLOAT : C O M P A R E '_' D E C F L O A T ;
K_COMPUTED : C O M P U T E D ;
K_CONDITIONAL : C O N D I T I O N A L ;
K_CONNECT : C O N N E C T ;
K_CONNECTIONS : C O N N E C T I O N S ;
K_CONSISTENCY : C O N S I S T E N C Y ;
K_CONSTRAINT : C O N S T R A I N T ;
K_CONTAINING : C O N T A I N I N G ;
K_CONTENTS : C O N T E N T S ;
K_CONTINUE : C O N T I N U E ;
K_CORR : C O R R ;
K_COS : C O S ;
K_COSH : C O S H ;
K_COT : C O T ;
K_COUNT : C O U N T ;
K_COUNTER : C O U N T E R ;
K_COVAR_POP : C O V A R '_' P O P ;
K_COVAR_SAMP : C O V A R '_' S A M P ;
K_CPU_LOAD : C P U '_' L O A D ;
K_CREATE : C R E A T E ;
K_CREATE_FILE : C R E A T E '_' F I L E ;
K_CROSS : C R O S S ;
K_CRYPT_HASH : C R Y P T '_' H A S H ;
K_CSTRING : C S T R I N G ;
K_CTR_BIG_ENDIAN : C T R '_' B I G '_' E N D I A N ;
K_CTR_LENGTH : C T R '_' L E N G T H ;
K_CTR_LITTLE_ENDIAN : C T R '_' L I T T L E '_' E N D I A N ;
K_CUME_DIST : C U M E '_' D I S T ;
K_CURRENT : C U R R E N T ;
K_CURRENT_CONNECTION : C U R R E N T '_' C O N N E C T I O N ;
K_CURRENT_DATE : C U R R E N T '_' D A T E ;
K_CURRENT_LABEL : C U R R E N T '_' L A B E L ;
K_CURRENT_ROLE : C U R R E N T '_' R O L E ;
K_CURRENT_TIME : C U R R E N T '_' T I M E ;
K_CURRENT_TIMESTAMP : C U R R E N T '_' T I M E S T A M P ;
K_CURRENT_TRANSACTION : C U R R E N T '_' T R A N S A C T I O N ;
K_CURRENT_USER : C U R R E N T '_' U S E R ;
K_CURSOR : C U R S O R ;
K_DAMLEV : D A M L E V ;
K_DATABASE : D A T A B A S E ;
K_DATA : D A T A ;
K_DATE : D A T E ;
K_DATEADD : D A T E A D D ;
K_DATEDIFF : D A T E D I F F ;
K_DAY : D A Y ;
K_DDL : D D L ;
K_DEBUG : D E B U G ;
K_DEC : D E C ;
K_DECFLOAT : D E C F L O A T ;
K_DECIMAL : D E C I M A L ;
K_DECLARE : D E C L A R E ;
K_DECODE : D E C O D E ;
K_DECRYPT : D E C R Y P T ;
K_DEFAULT : D E F A U L T ;
K_DEFINER : D E F I N E R ;
K_DELETE : D E L E T E ;
K_DELETE_FILE : D E L E T E '_' F I L E ;
K_DELETING : D E L E T I N G ;
K_DENSE_RANK : D E N S E '_' R A N K ;
K_DESC : D E S C ;
K_DESCENDING : D E S C E N D I N G ;
K_DESCRIPTOR : D E S C R I P T O R ;
K_DETERMINISTIC : D E T E R M I N I S T I C ;
K_DIFFERENCE : D I F F E R E N C E ;
K_DISABLE : D I S A B L E ;
K_DISCONNECT : D I S C O N N E C T ;
K_DISTINCT : D I S T I N C T ;
K_DO : D O ;
K_DOMAIN : D O M A I N ;
K_DOUBLE : D O U B L E ;
K_DROP : D R O P ;
K_DUMP : D U M P ;
K_ELSE : E L S E ;
K_EMPTY : E M P T Y ;
K_ENABLE : E N A B L E ;
K_ENCRYPT : E N C R Y P T ;
K_END : E N D ;
K_ENGINE : E N G I N E ;
K_ENTRY_POINT : E N T R Y '_' P O I N T ;
K_ERROR : E R R O R ;
K_ESCAPE : E S C A P E ;
K_EXCEPTION : E X C E P T I O N ;
K_EXCESS : E X C E S S ;
K_EXCLUDE : E X C L U D E ;
K_EXECUTE : E X E C U T E ;
K_EXISTS : E X I S T S ;
K_EXIT : E X I T ;
K_EXP : E X P ;
K_EXTENDED : E X T E N D E D ;
K_EXTERNAL : E X T E R N A L ;
K_EXTRACT : E X T R A C T ;
K_FALSE : F A L S E ;
K_FETCH : F E T C H ;
K_FILE : F I L E ;
K_FILTER : F I L T E R ;
K_FIRST : F I R S T ;
K_FIRST_DAY : F I R S T '_' D A Y ;
K_FIRST_VALUE : F I R S T '_' V A L U E ;
K_FIRSTNAME : F I R S T N A M E ;
K_FLOAT : F L O A T ;
K_FLOOR : F L O O R ;
K_FOLLOWING : F O L L O W I N G ;
K_FOR : F O R ;
K_FOREIGN : F O R E I G N ;
K_FORMAT : F O R M A T ;
K_FREE_IT : F R E E '_' I T ;
K_FROM : F R O M ;
K_FULL : F U L L ;
K_FUNCTION : F U N C T I O N ;
K_GDSCODE : G D S C O D E ;
K_GENERATED : G E N E R A T E D ;
K_GENERATOR : G E N E R A T O R ;
K_GEN_ID : G E N '_' I D ;
K_GEN_UUID : G E N '_' U U I D ;
K_GLOBAL : G L O B A L ;
K_GOSTPASSWORD : G O S T P A S S W O R D ;
K_GRANT : G R A N T ;
K_GRANTED : G R A N T E D ;
K_GROUP : G R O U P ;
K_GSS : G S S ;
K_HASH : H A S H ;
K_HASH_CP : H A S H '_' C P ;
K_HASHAGG : H A S H A G G ;
K_HAVING : H A V I N G ;
K_HEX_DECODE : H E X '_' D E C O D E ;
K_HEX_ENCODE : H E X '_' E N C O D E ;
K_HOUR : H O U R ;
K_IDENTITY : I D E N T I T Y ;
K_IDLE : I D L E ;
K_IF : I F ;
K_IGNORE : I G N O R E ;
K_IIF : I I F ;
K_IN : I N ;
K_INACTIVE : I N A C T I V E ;
K_INCLUDE : I N C L U D E ;
K_INCLUDING : I N C L U D I N G ;
K_INCREMENT : I N C R E M E N T ;
K_INDEX : I N D E X ;
K_INITIAL_LABEL : I N I T I A L '_' L A B E L ;
K_INNER : I N N E R ;
K_INPUT_TYPE : I N P U T '_' T Y P E ;
K_INSENSITIVE : I N S E N S I T I V E ;
K_INSERT : I N S E R T ;
K_INSERTING : I N S E R T I N G ;
K_INT : I N T ;
K_INT128 : I N T '1' '2' '8' ;
K_INTEGER : I N T E G E R ;
K_INTO : I N T O ;
K_INVOKER : I N V O K E R ;
K_IS : I S ;
K_IS_LABEL_VALID : I S '_' L A B E L '_' V A L I D ;
K_ISOLATION : I S O L A T I O N ;
K_IV : I V ;
K_JOB : J O B ;
K_JOIN : J O I N ;
K_JSON : J S O N ;
K_JSON_ARRAY : J S O N '_' A R R A Y ;
K_JSON_ARRAYAGG : J S O N '_' A R R A Y A G G ;
K_JSON_EXISTS : J S O N '_' E X I S T S ;
K_JSON_MODIFY : J S O N '_' M O D I F Y ;
K_JSON_OBJECT : J S O N '_' O B J E C T ;
K_JSON_OBJECTAGG : J S O N '_' O B J E C T A G G ;
K_JSON_QUERY : J S O N '_' Q U E R Y ;
K_JSON_TABLE : J S O N '_' T A B L E ;
K_JSON_VALUE : J S O N '_' V A L U E ;
K_KEEP : K E E P ;
K_KEY : K E Y ;
K_KEYS : K E Y S ;
K_LAG : L A G ;
K_LAST : L A S T ;
K_LAST_DAY : L A S T '_' D A Y ;
K_LAST_VALUE : L A S T '_' V A L U E ;
K_LASTNAME : L A S T N A M E ;
K_LDAP_ATTR : L D A P '_' A T T R ;
K_LDAP_GROUPS : L D A P '_' G R O U P S ;
K_LDAP_USER_GROUPS : L D A P '_' U S E R '_' G R O U P S ;
K_LEAD : L E A D ;
K_LEADING : L E A D I N G ;
K_LEAVE : L E A V E ;
K_LEFT : L E F T ;
K_LEGACY : L E G A C Y ;
K_LENGTH : L E N G T H ;
K_LEVEL : L E V E L ;
K_LIFETIME : L I F E T I M E ;
K_LIKE : L I K E ;
K_LIMBO : L I M B O ;
K_LINGER : L I N G E R ;
K_LIST : L I S T ;
K_LN : L N ;
K_LATERAL : L A T E R A L ;
K_LOCAL : L O C A L ;
K_LOCALTIME : L O C A L T I M E ;
K_LOCALTIMESTAMP : L O C A L T I M E S T A M P ;
K_LOCK : L O C K ;
K_LOCKED : L O C K E D ;
K_LOG : L O G ;
K_LOG10 : L O G '1' '0' ;
K_LONG : L O N G ;
K_LOWER : L O W E R ;
K_LPAD : L P A D ;
K_LPARAM : L P A R A M ;
K_MAKE_DBKEY : M A K E '_' D B K E Y ;
K_MANUAL : M A N U A L ;
K_MAPPING : M A P P I N G ;
K_MATCHED : M A T C H E D ;
K_MATCHING : M A T C H I N G ;
K_MAX_FAILED_COUNT : M A X '_' F A I L E D '_' C O U N T ;
K_MAX_SESSIONS : M A X '_' S E S S I O N S ;
K_MAX_IDLE_TIME : M A X '_' I D L E '_' T I M E ;
K_MAX_UNUSED_DAYS : M A X '_' U N U S E D '_' D A Y S ;
K_MAX : M A X ;
K_MAXVALUE : M A X V A L U E ;
K_MERGE : M E R G E ;
K_MESSAGE : M E S S A G E ;
K_MILLISECOND : M I L L I S E C O N D ;
K_MIDDLENAME : M I D D L E N A M E ;
K_MIN : M I N ;
K_MINUTE : M I N U T E ;
K_MINVALUE : M I N V A L U E ;
K_MOD : M O D ;
K_MODE : M O D E ;
K_MODULE_NAME : M O D U L E '_' N A M E ;
K_MONTH : M O N T H ;
K_NAME : N A M E ;
K_NAMES : N A M E S ;
K_NATIONAL : N A T I O N A L ;
K_NATIVE : N A T I V E ;
K_NATURAL : N A T U R A L ;
K_NCHAR : N C H A R ;
K_NEXT : N E X T ;
K_NO : N O ;
K_NORMALIZE_DECFLOAT : N O R M A L I Z E '_' D E C F L O A T ;
K_NOT : N O T ;
K_NTH_VALUE : N T H '_' V A L U E ;
K_NTILE : N T I L E ;
K_NULLIF : N U L L I F ;
K_NULL : N U L L ;
K_NULLS : N U L L S ;
K_NUMBER : N U M B E R ;
K_NUMERIC : N U M E R I C ;
K_OBJECT : O B J E C T ;
K_OCTET_LENGTH : O C T E T '_' L E N G T H ;
K_OF : O F ;
K_OFFSET : O F F S E T ;
K_OLDEST : O L D E S T ;
K_OMIT : O M I T ;
K_ON : O N ;
K_ONCE : O N C E ;
K_OFFLINE : O F F L I N E ;
K_ONLINE : O N L I N E ;
K_ONLY : O N L Y ;
K_OPEN : O P E N ;
K_OPTIMIZE : O P T I M I Z E ;
K_OPTION : O P T I O N ;
K_OR : O R ;
K_ORDER : O R D E R ;
K_OS_NAME : O S '_' N A M E ;
K_OTHERS : O T H E R S ;
K_OUTER : O U T E R ;
K_OUTPUT_TYPE : O U T P U T '_' T Y P E ;
K_OVER : O V E R ;
K_OVERFLOW : O V E R F L O W ;
K_OVERLAY : O V E R L A Y ;
K_OVERRIDING : O V E R R I D I N G ;
K_OWNER : O W N E R ;
K_PACKAGE : P A C K A G E ;
K_PAD : P A D ;
K_PAGE : P A G E ;
K_PAGES : P A G E S ;
K_PAGE_SIZE : P A G E '_' S I Z E ;
K_PARAMETER : P A R A M E T E R ;
K_PARTITION : P A R T I T I O N ;
K_PASSWORD : P A S S W O R D ;
K_PERCENT_RANK : P E R C E N T '_' R A N K ;
K_PI : P I ;
K_PIN : P I N ;
K_PKCS_1_5 : P K C S '_' '1' '_' '5' ;
K_PLACING : P L A C I N G ;
K_PLAN : P L A N ;
K_PLUGIN : P L U G I N ;
K_POLICY : P O L I C Y ;
K_POOL : P O O L ;
K_POSITION : P O S I T I O N ;
K_POST_EVENT : P O S T '_' E V E N T ;
K_POWER : P O W E R ;
K_PRECEDING : P R E C E D I N G ;
K_PRECISION : P R E C I S I O N ;
K_PRESERVE : P R E S E R V E ;
K_PRIMARY : P R I M A R Y ;
K_PRIOR : P R I O R ;
K_PRIVILEGE : P R I V I L E G E ;
K_PRIVILEGES : P R I V I L E G E S ;
K_PROCEDURE : P R O C E D U R E ;
K_PROTECTED : P R O T E C T E D ;
K_PSWD_NEED_CHAR : P S W D '_' N E E D '_' C H A R ;
K_PSWD_NEED_DIGIT : P S W D '_' N E E D '_' D I G I T ;
K_PSWD_NEED_DIFF_CASE : P S W D '_' N E E D '_' D I F F '_' C A S E ;
K_PSWD_MIN_LEN : P S W D '_' M I N '_' L E N ;
K_PSWD_VALID_DAYS : P S W D '_' V A L I D '_' D A Y S ;
K_PSWD_UNIQUE_COUNT : P S W D '_' U N I Q U E '_' C O U N T ;
K_PUBLICATION : P U B L I C A T I O N ;
K_QUANTIZE : Q U A N T I Z E ;
K_QUOTES : Q U O T E S ;
K_RAND : R A N D ;
K_RANGE : R A N G E ;
K_RANK : R A N K ;
K_RDB_DB_KEY : R D B '$' D B '_' K E Y ;
K_RDB_ERROR : R D B '$' E R R O R ;
K_RDB_GET_CONTEXT : R D B '$' G E T '_' C O N T E X T ;
K_RDB_GET_TRANSACTION_CN : R D B '$' G E T '_' T R A N S A C T I O N '_' C N ;
K_RDB_RECORD_VERSION : R D B '$' R E C O R D '_' V E R S I O N ;
K_RDB_ROLE_IN_USE : R D B '$' R O L E '_' I N '_' U S E ;
K_RDB_SET_CONTEXT : R D B '$' S E T '_' C O N T E X T ;
K_RDB_SYSTEM_PRIVILEGE : R D B '$' S Y S T E M '_' P R I V I L E G E ;
K_READ : R E A D ;
K_READ_FILE : R E A D '_' F I L E ;
K_REAL : R E A L ;
K_RECORD_VERSION : R E C O R D '_' V E R S I O N ;
K_RECREATE : R E C R E A T E ;
K_RECURSIVE : R E C U R S I V E ;
K_REFERENCES : R E F E R E N C E S ;
K_REGEXP_SUBSTR : R E G E X P '_' S U B S T R ;
K_REGR_AVGX : R E G R '_' A V G X ;
K_REGR_AVGY : R E G R '_' A V G Y ;
K_REGR_COUNT : R E G R '_' C O U N T ;
K_REGR_INTERCEPT : R E G R '_' I N T E R C E P T ;
K_REGR_R2 : R E G R '_' R '2' ;
K_REGR_SLOPE : R E G R '_' S L O P E ;
K_REGR_SXX : R E G R '_' S X X ;
K_REGR_SXY : R E G R '_' S X Y ;
K_REGR_SYY : R E G R '_' S Y Y ;
K_RELATIVE : R E L A T I V E ;
K_RELEASE : R E L E A S E ;
K_REPLACE : R E P L A C E ;
K_REQUESTS : R E Q U E S T S ;
K_RESERV : R E S E R V ;
K_RESERVING : R E S E R V I N G ;
K_RESET : R E S E T ;
K_RESETTING : R E S E T T I N G ;
K_RESTART : R E S T A R T ;
K_RESTRICT : R E S T R I C T ;
K_RETAIN : R E T A I N ;
K_RETURN : R E T U R N ;
K_RETURNING : R E T U R N I N G ;
K_RETURNING_VALUES : R E T U R N I N G '_' V A L U E S ;
K_RETURNS : R E T U R N S ;
K_REVERSE : R E V E R S E ;
K_REVOKE : R E V O K E ;
K_RIGHT : R I G H T ;
K_ROLE : R O L E ;
K_ROLLBACK : R O L L B A C K ;
K_ROUND : R O U N D ;
K_ROW : R O W ;
K_ROW_COUNT : R O W '_' C O U N T ;
K_ROW_NUMBER : R O W '_' N U M B E R ;
K_ROWS : R O W S ;
K_RPAD : R P A D ;
K_RSA_DECRYPT : R S A '_' D E C R Y P T ;
K_RSA_ENCRYPT : R S A '_' E N C R Y P T ;
K_RSA_PRIVATE : R S A '_' P R I V A T E ;
K_RSA_PUBLIC : R S A '_' P U B L I C ;
K_RSA_SIGN_HASH : R S A '_' S I G N '_' H A S H ;
K_RSA_VERIFY_HASH : R S A '_' V E R I F Y '_' H A S H ;
K_RUN : R U N ;
K_SALT_LENGTH : S A L T '_' L E N G T H ;
K_SAVEPOINT : S A V E P O I N T ;
K_SCALAR : S C A L A R ;
K_SCALAR_ARRAY : S C A L A R '_' A R R A Y ;
K_SCHEMA : S C H E M A ;
K_SCROLL : S C R O L L ;
K_SECOND : S E C O N D ;
K_SECURITY : S E C U R I T Y ;
K_SEGMENT : S E G M E N T ;
K_SELECT : S E L E C T ;
K_SENSITIVE : S E N S I T I V E ;
K_SEQUENCE : S E Q U E N C E ;
K_SERVERWIDE : S E R V E R W I D E ;
K_SESSION : S E S S I O N ;
K_SET : S E T ;
K_SHADOW : S H A D O W ;
K_SHARED : S H A R E D ;
K_SIGN : S I G N ;
K_SIGNATURE : S I G N A T U R E ;
K_SIMILAR : S I M I L A R ;
K_SIN : S I N ;
K_SINGULAR : S I N G U L A R ;
K_SINH : S I N H ;
K_SIZE : S I Z E ;
K_SKIP : S K I P ;
K_SMALLINT : S M A L L I N T ;
K_SNAPSHOT : S N A P S H O T ;
K_SOME : S O M E ;
K_SORT : S O R T ;
K_SOURCE : S O U R C E ;
K_SPACE : S P A C E ;
K_SQL : S Q L ;
K_SQLCODE : S Q L C O D E ;
K_SQLSTATE : S Q L S T A T E ;
K_SQRT : S Q R T ;
K_SRP : S R P ;
K_STABILITY : S T A B I L I T Y ;
K_START : S T A R T ;
K_STARTING : S T A R T I N G ;
K_STARTS : S T A R T S ;
K_STATEMENT : S T A T E M E N T ;
K_STATISTICS : S T A T I S T I C S ;
K_STDDEV_POP : S T D D E V '_' P O P ;
K_STDDEV_SAMP : S T D D E V '_' S A M P ;
K_STRING : S T R I N G ;
K_SUBSTRING : S U B S T R I N G ;
K_SUB_TYPE : S U B '_' T Y P E ;
K_SUM : S U M ;
K_SUSPEND : S U S P E N D ;
K_SYSTEM : S Y S T E M ;
K_TABLE : T A B L E ;
K_TABLESPACE : T A B L E S P A C E ;
K_TAGS : T A G S ;
K_TAN : T A N ;
K_TANH : T A N H ;
K_TARGET : T A R G E T ;
K_TEMPORARY : T E M P O R A R Y ;
K_THEN : T H E N ;
K_TIES : T I E S ;
K_TIME : T I M E ;
K_TIMESTAMP : T I M E S T A M P ;
K_TIMEOUT : T I M E O U T ;
K_TIMEZONE_HOUR : T I M E Z O N E '_' H O U R ;
K_TIMEZONE_MINUTE : T I M E Z O N E '_' M I N U T E ;
K_TIMEZONE_NAME : T I M E Z O N E '_' N A M E ;
K_TO : T O ;
K_TOTALORDER : T O T A L O R D E R ;
K_TRAILING : T R A I L I N G ;
K_TRANSACTION : T R A N S A C T I O N ;
K_TRAPS : T R A P S ;
K_TRIGGER : T R I G G E R ;
K_TRIM : T R I M ;
K_TRUE : T R U E ;
K_TRUNC : T R U N C ;
K_TRUSTED : T R U S T E D ;
K_TWO_PHASE : T W O '_' P H A S E ;
K_TYPE : T Y P E ;
K_UNBOUNDED : U N B O U N D E D ;
K_UNCOMMITTED : U N C O M M I T T E D ;
K_UNCONDITIONAL : U N C O N D I T I O N A L ;
K_UNDO : U N D O ;
K_UNICODE_CHAR : U N I C O D E '_' C H A R ;
K_UNICODE_VAL : U N I C O D E '_' V A L ;
K_UNION : U N I O N ;
K_UNIQUE : U N I Q U E ;
K_UNKNOWN : U N K N O W N ;
K_UPDATE : U P D A T E ;
K_UPDATING : U P D A T I N G ;
K_UPPER : U P P E R ;
K_USAGE : U S A G E ;
K_USER : U S E R ;
K_USING : U S I N G ;
K_UTC_TIMESTAMP : U T C '_' T I M E S T A M P ;
K_UUID_TO_CHAR : U U I D '_' T O '_' C H A R ;
K_VALUE : V A L U E ;
K_VALUES : V A L U E S ;
K_VAR_POP : V A R '_' P O P ;
K_VAR_SAMP : V A R '_' S A M P ;
K_VARBINARY : V A R B I N A R Y ;
K_VARCHAR : V A R C H A R ;
K_VARIABLE : V A R I A B L E ;
K_VARYING : V A R Y I N G ;
K_VERIFYSERVER : V E R I F Y S E R V E R ;
K_VIEW : V I E W ;
K_WAIT : W A I T ;
K_WEEK : W E E K ;
K_WEEKDAY : W E E K D A Y ;
K_WHEN : W H E N ;
K_WHERE : W H E R E ;
K_WHILE : W H I L E ;
K_WIN_SSPI : W I N '_' S S P I ;
K_WINDOW : W I N D O W ;
K_WITH : W I T H ;
K_WITHOUT : W I T H O U T ;
K_WORK : W O R K ;
K_WRAPPER : W R A P P E R ;
K_WRITE : W R I T E ;
K_YEAR : Y E A R ;
K_YEARDAY : Y E A R D A Y ;
K_ZONE : Z O N E ;

IDENTIFIER
 : '"' (~'"' | '""')* '"'
 | '`' (~'`' | '``')* '`'
 | '[' ~']'* ']'
 | [a-zA-Z_] [a-zA-Z_$0-9]* // TODO check: needs more chars in set
 ;
INT_LITERAL
:DIGIT+
;

NUMERIC_LITERAL
 : DIGIT+ ( '.' DIGIT* )? ( E [-+]? DIGIT+ )?
 | '.' DIGIT+ ( E [-+]? DIGIT+ )?
 ;

BIND_PARAMETER
 : '?' DIGIT*
 | [:@] IDENTIFIER
 ;

STRING_LITERAL
 : '\'' ( ~'\'' | '\'\'' )* '\''
 ;

BLOB_LITERAL
 : X STRING_LITERAL
 ;

spases_or_comment
:SPACES+ COMMENT SPACES+
|SPACES+ COMMENT
|COMMENT SPACES+
|SPACES+
|COMMENT
;

comment:
 COMMENT
 ;

COMMENT
:SINGLE_LINE_COMMENT
|MULTILINE_COMMENT
;

SINGLE_LINE_COMMENT
 : '--' ~[\r\n]* //-> channel(HIDDEN)
 ;

MULTILINE_COMMENT
 : '/*' .*? ( '*/' | EOF ) //-> channel(HIDDEN)
 ;

SPACES
 : [ \u000B\t\r\n] //-> channel(HIDDEN)
 ;

UNEXPECTED_CHAR
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