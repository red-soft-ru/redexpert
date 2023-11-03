grammar SessionInfo;

session_info:
'Session ID:' SPACE id end_line
'name:' SPACE name_session end_line
'user:' SPACE username? end_line
'date:' SPACE datetime end_line
'flags:' flags
 ;

 flags:
 SPACE any_name(', ' any_name)*
 ;

 datetime
 :DATE SPACE TIME
 ;

 username
 :any_name
 ;

 name_session
 :any_name
 ;

 any_name
 :(LETTER|DIGIT|'_'|'$'|'@'|'.')+
 ;

 id
 :(LETTER|DIGIT)+
 ;

 end_line
 :'\n'
 |'\n' TAB+
 |'\r\n'
 | '\n' SPACE
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

 fragment TWO_DIGIT
 :DIGIT DIGIT
 ;

 DIGIT:[0-9];

 LETTER:[a-zA-Z];

 CYRILLIC_LETTER:[\u0410-\u042F\u0430-\u044F];

 SPACE:(' ')+;

 TAB:
 '\t'
 ;