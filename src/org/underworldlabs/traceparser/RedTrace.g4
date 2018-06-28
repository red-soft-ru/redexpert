grammar RedTrace;

parse
:event
;
/*: ( event| error )* EOF
;
error
 : UNEXPECTED_CHAR
   {
     throw new RuntimeException("UNEXPECTED_CHAR=" + $UNEXPECTED_CHAR.text);
   }
 ;*/
//events
event
:trace_event
|database_event
|transaction_event
|statement_event
|start_service_event
;

trace_event
:header_event WS? type_trace_event WS? ID_SESSION WS name_session WS database
;

database_event
:header_event WS type_database_event WS connection_info WS client_process_info?
;

transaction_event
:header_event WS type_transaction_event WS connection_info WS client_process_info? transaction_info WS?
global_counters? WS?
table_counters? WS?
;

statement_event
:header_event WS type_statement_event WS connection_info WS client_process_info? transaction_info WS id_statement WS
MINUSES WS
query_and_params
;

start_service_event
:header_event WS type_start_service_event WS 'service_mgr,' WS? '(' id_service ',' WS? username ',' WS? protocol ':' client_address (',' client_process_info)? WS? ')'
 WS '"' type_query_service '"'
 WS options_service
;

//types
type_trace_event
: 'TRACE_INIT'
| 'TRACE_FINI'
;

type_database_event
: 'CREATE_DATABASE'
| 'ATTACH_DATABASE'
| 'DROP_DATABASE'
| 'DETACH_DATABASE'
;

type_transaction_event
: 'START_TRANSACTION'
| 'COMMIT_RETAINING'
| 'COMMIT_TRANSACTION'
| 'ROLLBACK_RETAINING'
| 'ROLLBACK_TRANSACTION'
;

type_statement_event
:'EXECUTE_STATEMENT_START'
|'EXECUTE_STATEMENT_FINISH'
;

type_start_service_event
: 'START_SERVICE'
;

header_event
:timestamp WS? '(' id_process ':' id_thread ')'
;

connection_info
:database WS? '(' ID_CONNECTION ', '  username ':' rolename ', ' charset ', ' WS? (( protocol ':' client_address)|'<internal>') WS? ')'
;

query_and_params
:.*?
;




params
:'param0 = ' any_name ('(' ID (',' ID) ')')? ',' WS (('"' path '"')|path)
(ID WS '=' WS any_name ('(' ID (',' ID) ')')? ','WS (('"' path '"')|path))*
;

records_fetched
:ID WS 'records fetched'
;

transaction_info
:'(' ID_TRANSACTION ', ' level_isolation ' | ' mode_of_block ' | ' mode_of_access ')'
;

level_isolation
:'CONSISTENCY' | 'CONCURRENCY' | 'READ_COMMITTED | REC_VERSION' |
 'READ_COMMITTED | NO_REC_VERSION'
;

mode_of_block
:'WAIT' ('N')? |'NOWAIT'
;

mode_of_access
:'READ_ONLY' | 'READ_WRITE'
;

global_counters
:time_execution WS 'ms' (', ')? (reads WS 'read(s)')? (', ')? (writes WS 'write(s)')? (', ')? (fetches WS 'fetch(es)')? (', ')? (marks WS 'mark(s)')?
;

id_statement
:'Statement' WS ID ':'
;

time_execution
:ID
;

reads
:ID
;

writes
:ID
;

fetches
:ID
;

marks
:ID
;

table_counters : table natural index update insert delete backout purge expunge ;

table
:any_name
;

natural
:ID
;

index
:ID
;

update
:ID
;

insert
:ID
;

delete
:ID
;

backout
:ID
;

purge
:ID
;

expunge
:ID
;

options_service
: .*?
;

type_query_service
:any_name (WS any_name)*
;

client_process_info
:client_process ':' id_client_process WS?
;

id_service
:'Service' WS ID
;

client_process
:path
;
id_client_process
:ID
;
client_address
:CLIENT_ADDRESS
|any_name
;

protocol
:any_name
;

username
:any_name
;

rolename
:any_name
;

charset
:any_name
;
any_name
:ID
|ANY_NAME
;

id_process
:ID
;

id_thread
:ID
;

name_session
:any_name
;

database
:path
;

path
:PATH
|any_name;


timestamp
:TIMESTAMP
;

//keywords
MINUSES
:('-')+
;

ID_TRANSACTION
:'TRA_' ID
;

ID_CONNECTION
:'ATT_' ID
;

ID_SESSION
:'SESSION_' ID
;

CLIENT_ADDRESS
:IP_ADDRESS ('/' DIGIT+)?
;

IP_ADDRESS
:IP_SEG '.' IP_SEG '.' IP_SEG '.' IP_SEG
;

TIMESTAMP
:DATE 'T' TIME
;

TIME
:TWO_DIGIT ':' TWO_DIGIT ':' TWO_DIGIT '.' DIGIT+
;

DATE
:FOUR_DIGIT '-' TWO_DIGIT '-' TWO_DIGIT
;

fragment FOUR_DIGIT
:TWO_DIGIT TWO_DIGIT;

fragment IP_SEG
:DIGIT
|TWO_DIGIT
|TWO_DIGIT DIGIT
;

fragment TWO_DIGIT
:DIGIT DIGIT
;

ID
:(LETTER|DIGIT)+
;

ANY_NAME
:(LETTER|DIGIT|'_')+
;

PATH
:(LETTER|DIGIT|':\\'|':/'|'_'|'-'|'.'|'/'|'\\'|'$'|'%')+
;

fragment DIGIT:[0-9];

fragment LETTER:[a-zA-Z];

WS
: [ \t\u000C\r\n]+
;















