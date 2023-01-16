grammar RedTrace;

parse
:event
;

event
:trace_event
|database_event
|transaction_event
|statement_event
|start_service_event
|statement_prepare_event
|free_statement_event
|context_event
|privileges_change_event
|procedure_function_event
|trigger_event
|compile_blr_event
|execute_blr_event
|execute_dyn_event
|service_event
|service_query_event
|error_event
|sweep_event
;

trace_event
:header_event SPACE type_trace_event end_line id_session SPACE name_session end_line database
;

database_event
:header_event SPACE type_database_event end_line connection_info end_line client_process_info?
;

transaction_event
:header_event SPACE type_transaction_event end_line connection_info end_line (client_process_info end_line)? transaction_info end_line
(global_counters end_line)?
(table_counters end_line)?
;

statement_event
:header_event SPACE type_statement_event end_line
 connection_info end_line
 (client_process_info end_line)?
 transaction_info ws+
 (id_statement end_line)?
MINUSES end_line
query_and_params
;

start_service_event
:header_event SPACE type_start_service_event end_line 'service_mgr, ' '(' id_service ', ' username ', ' protocol ':' client_address (','end_line client_process_info )? ')' end_line
 '"' type_query_service '"' end_line
 options_service
;

statement_prepare_event
:header_event SPACE type_statement_prepare_event end_line
connection_info end_line
 (client_process_info end_line)?
 transaction_info ws
 id_statement end_line
MINUSES end_line
query_and_params
;

free_statement_event
:header_event SPACE type_free_statement_event end_line
connection_info end_line
(client_process_info end_line)?
id_statement end_line
MINUSES end_line
query_and_params
;

context_event
:header_event SPACE type_context_event end_line
 connection_info end_line
 (client_process_info end_line)?
 transaction_info ws
declare_context_variables
;

privileges_change_event
:header_event SPACE type_privileges_change_event end_line
connection_info end_line
(client_process_info end_line)?
 transaction_info ws
 privileges_change_info
;

procedure_function_event
:header_event SPACE type_procedure_event end_line
connection_info end_line
(client_process_info end_line)?
 transaction_info ws+
 procedure_info
;

trigger_event
:header_event SPACE type_trigger_event end_line
connection_info end_line
(client_process_info end_line)?
 transaction_info ws
 trigger_info end_line
 (global_counters end_line+)?
 (table_counters end_line+)?
;

compile_blr_event
:header_event SPACE type_compile_blr_event end_line
 connection_info end_line
 (client_process_info end_line)?
 transaction_info ws
 id_statement end_line
MINUSES end_line
query_and_params
;

execute_blr_event
:header_event SPACE type_execute_blr_event end_line
 connection_info end_line
 (client_process_info end_line)?
 transaction_info ws
 id_statement end_line
MINUSES end_line
query_and_params
;

execute_dyn_event
:header_event SPACE type_execute_dyn_event end_line
 connection_info end_line
 (client_process_info end_line)?
 transaction_info ws
MINUSES end_line
query_and_params
;

service_event
:header_event SPACE type_service_event end_line
 'service_mgr, '  '(' id_service ', ' username ', ' protocol ':' client_address (','end_line client_process_info )? ')'
;

service_query_event
:header_event SPACE type_query_service_event end_line
 'service_mgr, ' '(' id_service ', ' username ', ' protocol ':' client_address (','end_line client_process_info )? ')' end_line
'"' type_query_service '"' end_line
('Send portion of the query:' sended_data)?
('Receive portion of the query:' received_data)?
;

error_event
:header_event SPACE type_error_event end_line
  connection_info end_line
  (client_process_info end_line)?
  error_message
;
sweep_event
:header_event SPACE type_sweep_event end_line
connection_info end_line
(client_process_info end_line)?
'Transaction counters:' end_line
'Oldest interesting' SPACE oldest_interesting end_line
'Oldest active' SPACE oldest_active end_line
'Oldest snapshot' SPACE oldest_snapshot end_line
'Next transaction' SPACE next_transaction end_line
(global_counters end_line+)?
 (table_counters end_line+)?
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

type_statement_prepare_event
: 'PREPARE_STATEMENT'
;

type_free_statement_event
:'FREE_STATEMENT' | 'CLOSE_CURSOR'
;

type_context_event
:'SET_CONTEXT'
;

type_privileges_change_event
:'PRIVILEGES_CHANGE'
;

type_procedure_event
:'EXECUTE_PROCEDURE_START'
| 'EXECUTE_FUNCTION_START'
| 'EXECUTE_PROCEDURE_FINISH'
| 'EXECUTE_FUNCTION_FINISH'
;

type_trigger_event
:'EXECUTE_TRIGGER_START'
|'EXECUTE_TRIGGER_FINISH'
 ;

 type_compile_blr_event
 :'COMPILE_BLR'
 ;

 type_execute_blr_event
 : 'EXECUTE_BLR'
 ;

 type_execute_dyn_event
 : 'EXECUTE_DYN'
 ;

 type_service_event
 :'ATTACH_SERVICE'
 |'DETACH_SERVICE'
 ;

 type_query_service_event
 :'QUERY_SERVICE'
 ;

type_error_event
: ('ERROR AT' | 'WARNING AT') (~('\n'))*
;

type_sweep_event
: 'SWEEP_START'
| 'SWEEP_FINISH'
| 'SWEEP_FAILED'
| 'SWEEP_PROGRESS'
;


header_event
:timestamp SPACE? '(' id_process ':' id_thread ')' failed?
;

connection_info
:begin_line database SPACE? '(' id_connection ', '  username ':' rolename ', ' charset ', ' SPACE? (( protocol ':' client_address)|'<internal>') SPACE? ')'
;

query_and_params
:query (end_line
 CARETS)? end_line?
 (plan end_line*)?
 (params end_line+)?
 (records_fetched end_line+)?
 (memory_size_rule end_line+)?
 (global_counters end_line+)?
 (table_counters end_line+)?

;

query
:~(CARETS
|'records fetched'
|'sorting memory usage: total: ')*
;

oldest_interesting
:any_name
;

oldest_active
:any_name
;

oldest_snapshot
:any_name
;

next_transaction
:any_name
;

procedure_info
:procedure_name ':' end_line
(params end_line+)?
('returns:' (SPACE|end_line) return_value end_line+)?
(records_fetched end_line+)?
(memory_size_rule end_line+)?
(global_counters end_line+)?
(table_counters end_line+)?
;

procedure_name:
(PROCEDURE_OR_FUNCTION) SPACE any_name;

trigger_info:
(~('\n'))+
;

return_value
:params
|any_name
|'"' path '"'
;

failed
:' FAILED'
|' UNAUTHORIZED'
;

declare_context_variables
:.*?
;

privileges_change_info
:'Executed by' SPACE executor SPACE AS SPACE grantor ', operation:' SPACE privilege end_line
 object SPACE FOR SPACE username end_line
 attachment ', ' transaction
;

executor
:username
;

grantor
:username
;

attachment
:'Attachment:' SPACE id
;

transaction
:'Transaction:' SPACE id
;

object
:any_name ('(' any_name ')')?
;

privilege
:PRIVILEGE
;

PRIVILEGE
: ('ADD'|'DELETE') SPACE 'PRIVILEGE' SPACE ((
'ALL' | 'INSERT' | 'UPDATE' | 'DELETE' | 'SELECT'
| 'EXECUTE' | 'REFERENCE' | 'CREATE' | 'ALTER' | 'ALTER ANY'
| 'DROP' | 'DROP ANY' | 'ROLE' | 'ENCRYPTION KEY') SPACE?)+
;

plan
:'Select Expression' end_line
 ( '->' (~('\n'))+ end_line)+
 |(PLAN (~('\n'))+ end_line)+
;

params
:parameter+
;

parameter:
param (((~('"'))+ ', ' str)|(~('\n')))
;

str
:'"' (~'"\n')* '"\n'
;



records_fetched
:id SPACE 'records fetched' (SPACE 'without sorting')?
;

transaction_info
:'(' id_transaction ', ' level_isolation ' | ' mode_of_block ' | ' mode_of_access ')'
;

level_isolation
:LEVEL_ISOLATION
;

mode_of_block
:WAIT (SPACE time_wait)? |NOWAIT
;

time_wait
:id
;

mode_of_access
:MODE_OF_ACCES
;

memory_size_rule:
'sorting memory usage: total: ' sum_cache ', cached: ' ram_cache ', on disk: ' disk_cache
;

sum_cache
: cache;

ram_cache:
cache;

disk_cache:
cache;

cache:
size_cache SPACE BYTES
;

size_cache
:id
;

global_counters
:time_execution SPACE MS (', ')? (reads SPACE 'read(s)')? (', ')? (writes SPACE 'write(s)')? (', ')? (fetches SPACE 'fetch(es)')? (', ')? (marks SPACE 'mark(s)')?
;

id_statement
:STATEMENT SPACE id ':'
;

sended_data
:(~('Receive portion of the query:'))+
;

received_data
:.*?
;

error_message
:.*?
;

time_execution
:id
;

reads
:id
;

writes
:id
;

fetches
:id
;

marks
:id
;

table_counters : Table (SPACE Natural)? (SPACE Index)? (SPACE Update)?
 (SPACE Insert)? (SPACE Delete)? (SPACE Backout)? (SPACE Purge)? (SPACE Expunge)?
 (SPACE Lock)? (SPACE Wait)? (SPACE Conflict)? (SPACE BVersion)? (SPACE Fragment)? (SPACE Refetch)? end_line
 ('*')+ end_line
 (any_name SPACE id (SPACE id)* SPACE? end_line)+;

options_service
: .*?
;

type_query_service
:any_name (SPACE any_name)*
;

client_process_info
:client_process ':' id_client_process
;

id_service
:Service SPACE id
;

client_process
:path (SPACE path)*
|client_process ':' client_process
;
id_client_process
:id
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

id_process
:id
;

id_thread
:id
;

name_session
:any_name
;

database
:path
|database SPACE path
;




timestamp
:TIMESTAMP
;

begin_line
:SPACE*
|TAB*
;

ws
:SPACE
|end_line
|TAB+
;

end_line
:'\n'
|'\n' TAB+
|'\r\n'
| '\n' SPACE
;

//keywords

CARETS
:('^')+
;

param
:PARAM id ' = '
;

id_transaction
:TRA_ id
;

id_connection
:ATT_ id
;

id_session
:SESSION_ id
;

client_address
:ip_address ('/' (DIGIT|IP_SEG)+)?
|any_name
;

ip_address
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

IP_SEG
:DIGIT
|TWO_DIGIT
|TWO_DIGIT DIGIT
;

fragment TWO_DIGIT
:DIGIT DIGIT
;

id
:(LETTER|DIGIT|IP_SEG)+
;

any_name
:(LETTER|DIGIT|'_'|'$'|'@'|'.'|lexem)+
;

path
:(LETTER|DIGIT|CYRILLIC_LETTER|MINUSES|':\\'|':/'|'_'|'.'|'/'|'\\'|'$'|'%'|'['|']'|'\''|'='|'?'|'-'|'('|')'|lexem)+
;

//LEXEMS
PLAN
:'PLAN'
;

PROCEDURE_OR_FUNCTION
:'Procedure'|'Function'
;

AS
:'as'
;

FOR
:'for'
;

LEVEL_ISOLATION
:'CONSISTENCY'
| 'CONCURRENCY'
| 'READ_COMMITTED | REC_VERSION'
| 'READ_COMMITTED | NO_REC_VERSION'
| 'READ_COMMITTED | READ_CONSISTENCY'
;

WAIT
:'WAIT'
;

NOWAIT
:'NOWAIT'
;

MODE_OF_ACCES
:'READ_ONLY' | 'READ_WRITE'
;

BYTES
:'bytes'
;

MS
:'ms'
;

STATEMENT
:'Statement'
;

Table
:'Table'
;
Natural
:'Natural'
;

Index
:'Index'
;

Update:
'Update'
;
Insert
:'Insert'
;

Delete
:'Delete'
;

Backout
:'Backout'
;

Purge
:'Purge'
;

Expunge
:'Expunge'
;

Lock:
'Lock'
;

Wait
:'Wait'
;

Conflict
:'Conflict'
;

BVersion
:'BVersion'
;

Fragment
:'Fragment'
;

Refetch
:'Refetch'
;

Service
:'Service'
;

PARAM
:'param'
;

TRA_
:'TRA_'
;

ATT_
:'ATT_'
;

SESSION_
:'SESSION_'
;

lexem
:IP_SEG
|PLAN
|PROCEDURE_OR_FUNCTION
|AS
|FOR
|LEVEL_ISOLATION
|WAIT
|NOWAIT
|MODE_OF_ACCES
|BYTES
|MS
|STATEMENT
|Table
|Natural
|Index
|Update
|Insert
|Delete
|Backout
|Purge
|Expunge
|Lock
|Wait
|Conflict
|BVersion
|Fragment
|Refetch
|Service
|PARAM
|TRA_
|ATT_
|SESSION_
;




MINUSES
:('-')+
;





DIGIT:[0-9];

LETTER:[a-zA-Z];

CYRILLIC_LETTER:[\u0410-\u042F\u0430-\u044F];

SPACE:(' ')+;

TAB:
'\t'
;
