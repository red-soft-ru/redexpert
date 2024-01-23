grammar Gstat;

parse:
header end_line+
header_page end_line+;

header_page:
'Database header page information:' end_line
(Flags spaces flags end_line)?
(Checksum spaces checksum end_line)?
( Generation spaces generation end_line)?
( SystemChangeNumber spaces system_change_number end_line)?
( 'Page size' spaces page_size end_line)?
( 'Server' spaces server end_line)?
( 'ODS version' spaces ods_version end_line)?
( 'Oldest transaction' spaces oldest_transaction end_line)?
( 'Oldest active' spaces oldest_active end_line)?
( 'Oldest snapshot' spaces oldest_snapshot end_line)?
( 'Next transaction' spaces next_transaction end_line)?
( 'Autosweep gap' spaces autosweep_gap end_line)?
( 'Bumped transaction' spaces bumped_transaction end_line)?
( 'Sequence number' spaces sequence_number end_line)?
( 'Next attachment ID' spaces next_attachment_ID end_line)?
( 'Implementation ID' implementation_ID spaces end_line)?
( 'Implementation' spaces implementation end_line)?
( 'Shadow count' spaces shadow_count end_line)?
( 'Page buffers' spaces page_buffers end_line)?
( 'Next header page' spaces next_header_page end_line)?
( 'Database dialect' spaces database_dialect end_line)?
( 'Creation date' spaces creation_date end_line)?
( 'Attributes' spaces attributes? end_line+)?
(variable_header_data_rule end_line)?;

header:
database_rule end_line time_rule;

file_sequence:
'Database file sequence:' end_line
File spaces path_file_sequence 'is the only file';

variable_header_data_rule:
Variable_header_data end_line
(variable end_line)*
End_variables end_line;

variable:
variable_name ':' variable_value;

variable_name:
(LETTER|lexem|spaces)+;

variable_value:
single_line_value;

flags:
int_value;

checksum:
int_value;

generation:
int_value;

system_change_number
:int_value;

page_size:
int_value;

server:
single_line_value;

ods_version:
digit_value;

oldest_transaction:
int_value;

oldest_active:
int_value;

oldest_snapshot:
int_value;

next_transaction:
int_value;

autosweep_gap:
int_value;

bumped_transaction:
int_value;

sequence_number:
int_value;

next_attachment_ID:
int_value;

implementation_ID:
int_value;

implementation:
single_line_value;

shadow_count:
int_value;

page_buffers:
int_value;

next_header_page:
int_value;

database_dialect:
int_value;

creation_date:
mounth spaces day ',' spaces year spaces time;

attributes:
(header_attribut (',' spaces)?)*
;

header_attribut:
(LETTER|DIGIT|spaces|'-'|lexem)+;
/*'force write'| 'no reserve'| 'shared cache disabled'|
'active shadow'|
'encrypted'| 'crypt process'|'plugin ' (LETTER|DIGIT)+|
'multi-user maintenance'|
'single-user maintenance'| 'full shutdown'| 'wrong shutdown state ' (LETTER|DIGIT)+|
'read only'|
'backup lock'| 'backup merge'| 'wrong backup state' (LETTER|DIGIT)+|
'read-only replica' | 'read-write replica' | 'wrong replica state'(LETTER|DIGIT)+;*/

single_line_value:
single_line;

single_line:
((~('\n')|lexem)+);

digit_value:
int_value|float_value;

int_value:
DIGIT+;

float_value:
DIGIT+ '.' DIGIT+;

time_rule:
Gstat_execution_time datetime_with_weekday;

datetime_with_weekday:
weekday spaces mounth spaces day spaces time spaces year;

weekday:
LETTER+;

mounth:
LETTER+;

day:
DIGIT+;

year:
DIGIT+;

time:
hours ':' minutes ':' seconds;

hours:
DIGIT+;

minutes:
DIGIT+;

seconds:
DIGIT+;

database_rule:
Database spaces database;

database:
QUOTE_IDENTIFIER;

begin_line
:SPACE*
|TAB*
;
spaces:
(SPACE|TAB)+
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

lexem:
Database
|Gstat_execution_time
|Variable_header_data
|Flags
|Checksum
|Generation
|SystemChangeNumber
|Page_size;

Flags:
'Flags';

Checksum:
'Checksum';

Generation:
'Generation';

SystemChangeNumber:
'System Change Number';

Page_size:
'Page size';

Database:
'Database';

Gstat_execution_time:
'Gstat execution time ';

End_variables:
'*END*';

Variable_header_data:
'Variable header data:';

QUOTE_IDENTIFIER
  : '"' (~'"' | '""')* ('"'|EOF)
  ;

DIGIT:[0-9];

LETTER:[a-zA-Z];

CYRILLIC_LETTER:[\u0410-\u042F\u0430-\u044F];

SPECIFIC_CHAR:[=/-]|'{'|'}';

SPACE:
(' ')+
;

TAB:
'\t'
;