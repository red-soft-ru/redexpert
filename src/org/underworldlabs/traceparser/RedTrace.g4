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
:header_event SPACE type_trace_event end_line ID_SESSION SPACE name_session end_line database
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
 transaction_info ws
 id_statement end_line
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
 transaction_info ws
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
: 'ERROR AT'
| 'WARNING AT'
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
:begin_line database SPACE? '(' ID_CONNECTION ', '  username ':' rolename ', ' charset ', ' SPACE? (( protocol ':' client_address)|'<internal>') SPACE? ')'
;

query_and_params
:query (end_line
 CARETS end_line
 not_query)?
;

query
:~CARETS*
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

not_query
:(plan end_line+)?
(params end_line+)?
(records_fetched end_line+)?
(global_counters end_line+)?
(table_counters end_line+)?
;

procedure_info
:procedure_name ':' end_line
(params end_line+)?
('returns: ' return_value end_line+)?
(records_fetched end_line+)?
(global_counters end_line+)?
(table_counters end_line+)?
;

procedure_name:
('Procedure'|'Function') SPACE any_name;

trigger_info:
(~('\n'))+
;

return_value
:any_name
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
:'Executed by' SPACE executor SPACE 'as' SPACE grantor ', operation:' SPACE privilege end_line
 object SPACE 'for' SPACE username end_line
 attachment ', ' transaction
;

executor
:username
;

grantor
:username
;

attachment
:'Attachment:' SPACE ID
;

transaction
:'Transaction:' SPACE ID
;

object
:any_name ('(' any_name ')')?
;

privilege
: ('ADD'|'DELETE') SPACE 'PRIVILEGE' SPACE ((
'ALL' | 'INSERT' | 'UPDATE' | 'DELETE' | 'SELECT'
| 'EXECUTE' | 'REFERENCE' | 'CREATE' | 'ALTER' | 'ALTER ANY'
| 'DROP' | 'DROP ANY' | 'ROLE' | 'ENCRYPTION KEY') SPACE?)+
;

plan
:'Select Expression' end_line
 ( '->' (~('\n'))+ end_line)+
 |'PLAN' (~('\n'))+ end_line
;

params
:(PARAM (~('\n'))+ end_line)+
;

records_fetched
:ID SPACE 'records fetched'
;

transaction_info
:'(' ID_TRANSACTION ', ' level_isolation ' | ' mode_of_block ' | ' mode_of_access ')'
;

level_isolation
:'CONSISTENCY' | 'CONCURRENCY' | 'READ_COMMITTED | REC_VERSION' |
 'READ_COMMITTED | NO_REC_VERSION'
;

mode_of_block
:'WAIT' (SPACE time_wait)? |'NOWAIT'
;

time_wait
:ID
;

mode_of_access
:'READ_ONLY' | 'READ_WRITE'
;

global_counters
:time_execution SPACE 'ms' (', ')? (reads SPACE 'read(s)')? (', ')? (writes SPACE 'write(s)')? (', ')? (fetches SPACE 'fetch(es)')? (', ')? (marks SPACE 'mark(s)')?
;

id_statement
:'Statement' SPACE ID ':'
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

table_counters : 'Table' (SPACE 'Natural')? (SPACE 'Index')? (SPACE 'Update')?
 (SPACE 'Insert')? (SPACE 'Delete')? (SPACE 'Backout')? (SPACE 'Purge')? (SPACE 'Expunge')? end_line
 ('*')+ end_line
 (~'\n')+ end_line;

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
:any_name (SPACE any_name)*
;

client_process_info
:client_process ':' id_client_process
;

id_service
:'Service' SPACE ID
;

client_process
:path (SPACE path)*
|client_process 'IP:' client_process
|client_process ':[' client_process
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
|database SPACE path
;

path
:PATH
|any_name;


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
MINUSES
:('-')+
;

CARETS
:('^')+
;

PARAM
:'param' ID ' = '
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

SIMPLE_ID
:'ID_' ID
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
:(LETTER|DIGIT|'_'|'$')+
;

PATH
:(LETTER|DIGIT|KIRILLIC_LETTER|':\\'|':/'|'_'|'-'|'.'|'/'|'\\'|'$'|'%'|'['|']')+
;

fragment DIGIT:[0-9];

fragment LETTER:[a-zA-Z];

fragment KIRILLIC_LETTER:[А-Яа-я];

SPACE:(' ')+;

TAB:
'\t'
;














