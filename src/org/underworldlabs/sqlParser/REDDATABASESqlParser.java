// Generated from D:/gitProjects/executequery/src/org/underworldlabs/sqlParser\REDDATABASESql.g4 by ANTLR 4.7
package org.underworldlabs.sqlParser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class REDDATABASESqlParser extends Parser {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, BIGINT = 5, BLOB = 6, CHAR = 7, CHARACTER = 8,
            DATE = 9, DECIMAL = 10, DOUBLE_PRECISION = 11, FLOAT = 12, INT = 13, INTEGER = 14,
            NATIONAL_CHARACTER = 15, NATIONAL_CHAR = 16, NCHAR = 17, NATIONAL_CHARACTER_VARYING = 18,
            NATIONAL_CHAR_VARYING = 19, NCHAR_VARYING = 20, NUMERIC = 21, SMALLINT = 22, TIME = 23,
            TIMESTAMP = 24, VARYING_CHARACTER = 25, VARCHAR = 26, VARYING = 27, SUB_TYPE = 28,
            SEGMENT_SIZE = 29, CHARACTER_SET = 30, SCOL = 31, DOT = 32, OPEN_PAR = 33, CLOSE_PAR = 34,
            COMMA = 35, ASSIGN = 36, STAR = 37, PLUS = 38, MINUS = 39, TILDE = 40, PIPE2 = 41, DIV = 42,
            MOD = 43, LT2 = 44, GT2 = 45, AMP = 46, PIPE = 47, LT = 48, LT_EQ = 49, GT = 50, GT_EQ = 51,
            EQ = 52, NOT_EQ1 = 53, NOT_EQ2 = 54, K_ABORT = 55, K_ACTION = 56, K_ADD = 57, K_AFTER = 58,
            K_ALL = 59, K_ALTER = 60, K_ANALYZE = 61, K_AND = 62, K_AS = 63, K_ASC = 64, K_ATTACH = 65,
            K_AUTHID = 66, K_AUTOINCREMENT = 67, K_BEFORE = 68, K_BEGIN = 69, K_BETWEEN = 70,
            K_BLOCK = 71, K_BY = 72, K_CALLER = 73, K_CASCADE = 74, K_CASE = 75, K_CAST = 76,
            K_CHECK = 77, K_COLLATE = 78, K_COLUMN = 79, K_COMMIT = 80, K_CONFLICT = 81, K_CONSTRAINT = 82,
            K_CREATE = 83, K_CREATE_OR_ALTER = 84, K_CROSS = 85, K_CURRENT_DATE = 86, K_CURRENT_TIME = 87,
            K_CURRENT_TIMESTAMP = 88, K_DATABASE = 89, K_DECLARE = 90, K_DEFAULT = 91, K_DEFERRABLE = 92,
            K_DEFERRED = 93, K_DELETE = 94, K_DESC = 95, K_DETACH = 96, K_DISTINCT = 97, K_DROP = 98,
            K_EACH = 99, K_ELSE = 100, K_END = 101, K_ENABLE = 102, K_ESCAPE = 103, K_EXCEPT = 104,
            K_EXCLUSIVE = 105, K_EXECUTE = 106, K_EXISTS = 107, K_EXPLAIN = 108, K_FAIL = 109,
            K_FOR = 110, K_FOREIGN = 111, K_FROM = 112, K_FULL = 113, K_GLOB = 114, K_GROUP = 115,
            K_HAVING = 116, K_IF = 117, K_IGNORE = 118, K_IMMEDIATE = 119, K_IN = 120, K_INDEX = 121,
            K_INDEXED = 122, K_INITIALLY = 123, K_INNER = 124, K_INSERT = 125, K_INSTEAD = 126,
            K_INTERSECT = 127, K_INTO = 128, K_IS = 129, K_ISNULL = 130, K_JOIN = 131, K_KEY = 132,
            K_LEFT = 133, K_LIKE = 134, K_LIMIT = 135, K_MATCH = 136, K_NATURAL = 137, K_NEXTVAL = 138,
            K_NO = 139, K_NOT = 140, K_NOTNULL = 141, K_NULL = 142, K_OF = 143, K_OFFSET = 144,
            K_ON = 145, K_ONLY = 146, K_OR = 147, K_ORDER = 148, K_OUTER = 149, K_OWNER = 150,
            K_PLAN = 151, K_PRAGMA = 152, K_PROCEDURE = 153, K_PRIMARY = 154, K_QUERY = 155,
            K_RAISE = 156, K_RECURSIVE = 157, K_REFERENCES = 158, K_REGEXP = 159, K_REINDEX = 160,
            K_RELEASE = 161, K_RENAME = 162, K_REPLACE = 163, K_RESTRICT = 164, K_RETURNS = 165,
            K_RIGHT = 166, K_ROLLBACK = 167, K_ROW = 168, K_SAVEPOINT = 169, K_SELECT = 170,
            K_SET = 171, K_TABLE = 172, K_TEMP = 173, K_TEMPORARY = 174, K_THEN = 175, K_TO = 176,
            K_TRANSACTION = 177, K_TRIGGER = 178, K_TYPE_OF = 179, K_UNION = 180, K_UNIQUE = 181,
            K_UPDATE = 182, K_USING = 183, K_VACUUM = 184, K_VALUES = 185, K_VARIABLE = 186,
            K_VIEW = 187, K_VIRTUAL = 188, K_WHEN = 189, K_WHERE = 190, K_WITH = 191, K_WITHOUT = 192,
            IDENTIFIER = 193, NUMERIC_LITERAL = 194, BIND_PARAMETER = 195, STRING_LITERAL = 196,
            BLOB_LITERAL = 197, SINGLE_LINE_COMMENT = 198, MULTILINE_COMMENT = 199, SPACES = 200,
            UNEXPECTED_CHAR = 201, DOUBLE = 202, PRECISION = 203, CHARACTER_VARYING = 204,
            DIGIT = 205, SEGMENT = 206, SIZE = 207;
    public static final int
            RULE_parse = 0, RULE_error = 1, RULE_sql_stmt_list = 2, RULE_sql_stmt = 3,
            RULE_alter_table_stmt = 4, RULE_alter_table_add_constraint = 5, RULE_alter_table_add = 6,
            RULE_analyze_stmt = 7, RULE_attach_stmt = 8, RULE_begin_stmt = 9, RULE_commit_stmt = 10,
            RULE_compound_select_stmt = 11, RULE_create_index_stmt = 12, RULE_create_table_stmt = 13,
            RULE_create_trigger_stmt = 14, RULE_create_view_stmt = 15, RULE_create_virtual_table_stmt = 16,
            RULE_create_procedure_stmt = 17, RULE_execute_block_stmt = 18, RULE_declare_block = 19,
            RULE_body = 20, RULE_local_variable = 21, RULE_output_parameter = 22,
            RULE_default_value = 23, RULE_variable_name = 24, RULE_input_parameter = 25,
            RULE_desciption_parameter = 26, RULE_parameter_name = 27, RULE_datatype = 28,
            RULE_datatypeSQL = 29, RULE_segment_size = 30, RULE_int_number = 31, RULE_array_size = 32,
            RULE_delete_stmt = 33, RULE_delete_stmt_limited = 34, RULE_detach_stmt = 35,
            RULE_drop_index_stmt = 36, RULE_drop_table_stmt = 37, RULE_drop_trigger_stmt = 38,
            RULE_drop_view_stmt = 39, RULE_factored_select_stmt = 40, RULE_insert_stmt = 41,
            RULE_pragma_stmt = 42, RULE_reindex_stmt = 43, RULE_release_stmt = 44,
            RULE_rollback_stmt = 45, RULE_savepoint_stmt = 46, RULE_simple_select_stmt = 47,
            RULE_select_stmt = 48, RULE_select_or_values = 49, RULE_update_stmt = 50,
            RULE_update_stmt_limited = 51, RULE_vacuum_stmt = 52, RULE_column_def = 53,
            RULE_type_name = 54, RULE_column_constraint = 55, RULE_column_constraint_primary_key = 56,
            RULE_column_constraint_foreign_key = 57, RULE_column_constraint_not_null = 58,
            RULE_column_constraint_null = 59, RULE_column_default = 60, RULE_column_default_value = 61,
            RULE_conflict_clause = 62, RULE_expr = 63, RULE_foreign_key_clause = 64,
            RULE_fk_target_column_name = 65, RULE_raise_function = 66, RULE_indexed_column = 67,
            RULE_table_constraint = 68, RULE_table_constraint_primary_key = 69, RULE_table_constraint_foreign_key = 70,
            RULE_table_constraint_unique = 71, RULE_table_constraint_key = 72, RULE_fk_origin_column_name = 73,
            RULE_with_clause = 74, RULE_qualified_table_name = 75, RULE_ordering_term = 76,
            RULE_order_collate = 77, RULE_pragma_value = 78, RULE_common_table_expression = 79,
            RULE_result_column = 80, RULE_table_or_subquery = 81, RULE_join_clause = 82,
            RULE_join_operator = 83, RULE_join_constraint = 84, RULE_select_core = 85,
            RULE_compound_operator = 86, RULE_cte_table_name = 87, RULE_signed_number = 88,
            RULE_literal_value = 89, RULE_unary_operator = 90, RULE_error_message = 91,
            RULE_module_argument = 92, RULE_column_alias = 93, RULE_keyword = 94,
            RULE_unknown = 95, RULE_name = 96, RULE_function_name = 97, RULE_database_name = 98,
            RULE_domain_name = 99, RULE_source_table_name = 100, RULE_table_name = 101,
            RULE_procedure_name = 102, RULE_table_or_index_name = 103, RULE_new_table_name = 104,
            RULE_column_name = 105, RULE_collation_name = 106, RULE_foreign_table = 107,
            RULE_index_name = 108, RULE_trigger_name = 109, RULE_view_name = 110,
            RULE_module_name = 111, RULE_pragma_name = 112, RULE_savepoint_name = 113,
            RULE_table_alias = 114, RULE_transaction_name = 115, RULE_charset_name = 116,
            RULE_any_name = 117;
    public static final String[] ruleNames = {
            "parse", "error", "sql_stmt_list", "sql_stmt", "alter_table_stmt", "alter_table_add_constraint",
            "alter_table_add", "analyze_stmt", "attach_stmt", "begin_stmt", "commit_stmt",
            "compound_select_stmt", "create_index_stmt", "create_table_stmt", "create_trigger_stmt",
            "create_view_stmt", "create_virtual_table_stmt", "create_procedure_stmt",
            "execute_block_stmt", "declare_block", "body", "local_variable", "output_parameter",
            "default_value", "variable_name", "input_parameter", "desciption_parameter",
            "parameter_name", "datatype", "datatypeSQL", "segment_size", "int_number",
            "array_size", "delete_stmt", "delete_stmt_limited", "detach_stmt", "drop_index_stmt",
            "drop_table_stmt", "drop_trigger_stmt", "drop_view_stmt", "factored_select_stmt",
            "insert_stmt", "pragma_stmt", "reindex_stmt", "release_stmt", "rollback_stmt",
            "savepoint_stmt", "simple_select_stmt", "select_stmt", "select_or_values",
            "update_stmt", "update_stmt_limited", "vacuum_stmt", "column_def", "type_name",
            "column_constraint", "column_constraint_primary_key", "column_constraint_foreign_key",
            "column_constraint_not_null", "column_constraint_null", "column_default",
            "column_default_value", "conflict_clause", "expr", "foreign_key_clause",
            "fk_target_column_name", "raise_function", "indexed_column", "table_constraint",
            "table_constraint_primary_key", "table_constraint_foreign_key", "table_constraint_unique",
            "table_constraint_key", "fk_origin_column_name", "with_clause", "qualified_table_name",
            "ordering_term", "order_collate", "pragma_value", "common_table_expression",
            "result_column", "table_or_subquery", "join_clause", "join_operator",
            "join_constraint", "select_core", "compound_operator", "cte_table_name",
            "signed_number", "literal_value", "unary_operator", "error_message", "module_argument",
            "column_alias", "keyword", "unknown", "name", "function_name", "database_name",
            "domain_name", "source_table_name", "table_name", "procedure_name", "table_or_index_name",
            "new_table_name", "column_name", "collation_name", "foreign_table", "index_name",
            "trigger_name", "view_name", "module_name", "pragma_name", "savepoint_name",
            "table_alias", "transaction_name", "charset_name", "any_name"
    };
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\u00d1\u0886\4\2\t" +
                    "\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
                    "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
                    "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31" +
                    "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!" +
                    "\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4" +
                    ",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t" +
                    "\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t=" +
                    "\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I" +
                    "\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT" +
                    "\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4" +
                    "`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\4f\tf\4g\tg\4h\th\4i\ti\4j\tj\4k\t" +
                    "k\4l\tl\4m\tm\4n\tn\4o\to\4p\tp\4q\tq\4r\tr\4s\ts\4t\tt\4u\tu\4v\tv\4" +
                    "w\tw\3\2\3\2\7\2\u00f1\n\2\f\2\16\2\u00f4\13\2\3\2\3\2\3\3\3\3\3\3\3\4" +
                    "\7\4\u00fc\n\4\f\4\16\4\u00ff\13\4\3\4\3\4\6\4\u0103\n\4\r\4\16\4\u0104" +
                    "\3\4\7\4\u0108\n\4\f\4\16\4\u010b\13\4\3\4\7\4\u010e\n\4\f\4\16\4\u0111" +
                    "\13\4\3\5\3\5\3\5\5\5\u0116\n\5\5\5\u0118\n\5\3\5\3\5\3\5\3\5\3\5\3\5" +
                    "\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3" +
                    "\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5\u013a\n\5\3\6\3\6\3\6\5\6\u013f" +
                    "\n\6\3\6\3\6\3\6\5\6\u0144\n\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\5\6\u014e" +
                    "\n\6\3\6\5\6\u0151\n\6\3\6\5\6\u0154\n\6\3\6\5\6\u0157\n\6\3\7\3\7\3\7" +
                    "\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t\u0168\n\t\3\n\3\n" +
                    "\5\n\u016c\n\n\3\n\3\n\3\n\3\n\3\13\3\13\5\13\u0174\n\13\3\13\3\13\5\13" +
                    "\u0178\n\13\5\13\u017a\n\13\3\f\3\f\3\f\5\f\u017f\n\f\5\f\u0181\n\f\3" +
                    "\r\3\r\5\r\u0185\n\r\3\r\3\r\3\r\7\r\u018a\n\r\f\r\16\r\u018d\13\r\5\r" +
                    "\u018f\n\r\3\r\3\r\3\r\5\r\u0194\n\r\3\r\3\r\5\r\u0198\n\r\3\r\6\r\u019b" +
                    "\n\r\r\r\16\r\u019c\3\r\3\r\3\r\3\r\3\r\7\r\u01a4\n\r\f\r\16\r\u01a7\13" +
                    "\r\5\r\u01a9\n\r\3\r\3\r\3\r\3\r\5\r\u01af\n\r\5\r\u01b1\n\r\3\16\3\16" +
                    "\5\16\u01b5\n\16\3\16\3\16\3\16\3\16\5\16\u01bb\n\16\3\16\3\16\3\16\5" +
                    "\16\u01c0\n\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\7\16\u01c9\n\16\f\16" +
                    "\16\16\u01cc\13\16\3\16\3\16\3\16\5\16\u01d1\n\16\3\17\3\17\5\17\u01d5" +
                    "\n\17\3\17\3\17\3\17\3\17\5\17\u01db\n\17\3\17\3\17\3\17\5\17\u01e0\n" +
                    "\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\7\17\u01e9\n\17\f\17\16\17\u01ec" +
                    "\13\17\3\17\3\17\3\17\5\17\u01f1\n\17\3\17\3\17\5\17\u01f5\n\17\3\17\5" +
                    "\17\u01f8\n\17\3\20\3\20\5\20\u01fc\n\20\3\20\3\20\3\20\3\20\5\20\u0202" +
                    "\n\20\3\20\3\20\3\20\5\20\u0207\n\20\3\20\3\20\3\20\3\20\3\20\5\20\u020e" +
                    "\n\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\7\20\u0217\n\20\f\20\16\20\u021a" +
                    "\13\20\5\20\u021c\n\20\5\20\u021e\n\20\3\20\3\20\3\20\3\20\5\20\u0224" +
                    "\n\20\3\20\3\20\3\20\3\20\5\20\u022a\n\20\3\20\3\20\5\20\u022e\n\20\3" +
                    "\20\3\20\3\20\3\20\3\20\5\20\u0235\n\20\3\20\3\20\6\20\u0239\n\20\r\20" +
                    "\16\20\u023a\3\20\3\20\3\21\3\21\5\21\u0241\n\21\3\21\3\21\3\21\3\21\5" +
                    "\21\u0247\n\21\3\21\3\21\3\21\5\21\u024c\n\21\3\21\3\21\3\21\3\21\3\22" +
                    "\3\22\3\22\3\22\3\22\3\22\5\22\u0258\n\22\3\22\3\22\3\22\5\22\u025d\n" +
                    "\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\7\22\u0266\n\22\f\22\16\22\u0269" +
                    "\13\22\3\22\3\22\5\22\u026d\n\22\3\23\3\23\3\23\3\23\3\23\5\23\u0274\n" +
                    "\23\3\23\3\23\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\7\25\u0280\n\25" +
                    "\f\25\16\25\u0283\13\25\3\25\3\25\5\25\u0287\n\25\3\25\3\25\3\25\3\25" +
                    "\3\25\7\25\u028e\n\25\f\25\16\25\u0291\13\25\3\25\3\25\5\25\u0295\n\25" +
                    "\3\25\3\25\7\25\u0299\n\25\f\25\16\25\u029c\13\25\3\25\3\25\3\25\3\25" +
                    "\3\26\7\26\u02a3\n\26\f\26\16\26\u02a6\13\26\3\26\7\26\u02a9\n\26\f\26" +
                    "\16\26\u02ac\13\26\3\26\3\26\3\26\3\26\7\26\u02b2\n\26\f\26\16\26\u02b5" +
                    "\13\26\5\26\u02b7\n\26\3\27\3\27\5\27\u02bb\n\27\3\27\3\27\3\27\5\27\u02c0" +
                    "\n\27\3\27\3\27\5\27\u02c4\n\27\3\27\3\27\5\27\u02c8\n\27\3\27\3\27\3" +
                    "\30\3\30\3\31\3\31\5\31\u02d0\n\31\3\32\3\32\3\33\3\33\3\33\5\33\u02d7" +
                    "\n\33\3\34\3\34\3\34\5\34\u02dc\n\34\3\34\3\34\5\34\u02e0\n\34\3\35\3" +
                    "\35\3\36\3\36\5\36\u02e6\n\36\3\36\3\36\3\36\3\36\3\36\3\36\5\36\u02ee" +
                    "\n\36\3\37\3\37\5\37\u02f2\n\37\3\37\3\37\3\37\5\37\u02f7\n\37\3\37\5" +
                    "\37\u02fa\n\37\3\37\3\37\5\37\u02fe\n\37\3\37\3\37\3\37\3\37\3\37\5\37" +
                    "\u0305\n\37\3\37\3\37\5\37\u0309\n\37\3\37\5\37\u030c\n\37\3\37\3\37\3" +
                    "\37\3\37\3\37\3\37\3\37\3\37\5\37\u0316\n\37\3\37\3\37\5\37\u031a\n\37" +
                    "\3\37\5\37\u031d\n\37\3\37\3\37\5\37\u0321\n\37\3\37\3\37\6\37\u0325\n" +
                    "\37\r\37\16\37\u0326\3\37\5\37\u032a\n\37\3\37\5\37\u032d\n\37\3\37\3" +
                    "\37\3\37\3\37\5\37\u0333\n\37\5\37\u0335\n\37\3\37\3\37\3\37\5\37\u033a" +
                    "\n\37\3\37\3\37\5\37\u033e\n\37\3\37\3\37\3\37\3\37\3\37\5\37\u0345\n" +
                    "\37\3\37\3\37\5\37\u0349\n\37\5\37\u034b\n\37\3 \3 \3!\6!\u0350\n!\r!" +
                    "\16!\u0351\3\"\3\"\6\"\u0356\n\"\r\"\16\"\u0357\3\"\5\"\u035b\n\"\3\"" +
                    "\6\"\u035e\n\"\r\"\16\"\u035f\3\"\3\"\6\"\u0364\n\"\r\"\16\"\u0365\3\"" +
                    "\5\"\u0369\n\"\3\"\6\"\u036c\n\"\r\"\16\"\u036d\7\"\u0370\n\"\f\"\16\"" +
                    "\u0373\13\"\3\"\3\"\3#\5#\u0378\n#\3#\3#\3#\3#\3#\5#\u037f\n#\3$\5$\u0382" +
                    "\n$\3$\3$\3$\3$\3$\5$\u0389\n$\3$\3$\3$\3$\3$\7$\u0390\n$\f$\16$\u0393" +
                    "\13$\5$\u0395\n$\3$\3$\3$\3$\5$\u039b\n$\5$\u039d\n$\3%\3%\5%\u03a1\n" +
                    "%\3%\3%\3&\3&\3&\3&\5&\u03a9\n&\3&\3&\3&\5&\u03ae\n&\3&\3&\3\'\3\'\3\'" +
                    "\3\'\5\'\u03b6\n\'\3\'\3\'\3\'\5\'\u03bb\n\'\3\'\3\'\3(\3(\3(\3(\5(\u03c3" +
                    "\n(\3(\3(\3(\5(\u03c8\n(\3(\3(\3)\3)\3)\3)\5)\u03d0\n)\3)\3)\3)\5)\u03d5" +
                    "\n)\3)\3)\3*\3*\5*\u03db\n*\3*\3*\3*\7*\u03e0\n*\f*\16*\u03e3\13*\5*\u03e5" +
                    "\n*\3*\3*\3*\3*\7*\u03eb\n*\f*\16*\u03ee\13*\3*\3*\3*\3*\3*\7*\u03f5\n" +
                    "*\f*\16*\u03f8\13*\5*\u03fa\n*\3*\3*\3*\3*\5*\u0400\n*\5*\u0402\n*\3+" +
                    "\5+\u0405\n+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\3+\5+\u0418" +
                    "\n+\3+\3+\3+\3+\5+\u041e\n+\3+\3+\3+\3+\3+\7+\u0425\n+\f+\16+\u0428\13" +
                    "+\3+\3+\5+\u042c\n+\3+\3+\3+\3+\3+\7+\u0433\n+\f+\16+\u0436\13+\3+\3+" +
                    "\3+\3+\3+\3+\7+\u043e\n+\f+\16+\u0441\13+\3+\3+\7+\u0445\n+\f+\16+\u0448" +
                    "\13+\3+\3+\3+\5+\u044d\n+\3,\3,\3,\3,\5,\u0453\n,\3,\3,\3,\3,\3,\3,\3" +
                    ",\5,\u045c\n,\3-\3-\3-\3-\3-\5-\u0463\n-\3-\3-\5-\u0467\n-\5-\u0469\n" +
                    "-\3.\3.\5.\u046d\n.\3.\3.\3/\3/\3/\5/\u0474\n/\5/\u0476\n/\3/\3/\5/\u047a" +
                    "\n/\3/\5/\u047d\n/\3\60\3\60\3\60\3\61\3\61\5\61\u0484\n\61\3\61\3\61" +
                    "\3\61\7\61\u0489\n\61\f\61\16\61\u048c\13\61\5\61\u048e\n\61\3\61\3\61" +
                    "\3\61\3\61\3\61\3\61\7\61\u0496\n\61\f\61\16\61\u0499\13\61\5\61\u049b" +
                    "\n\61\3\61\3\61\3\61\3\61\5\61\u04a1\n\61\5\61\u04a3\n\61\3\62\3\62\5" +
                    "\62\u04a7\n\62\3\62\3\62\3\62\7\62\u04ac\n\62\f\62\16\62\u04af\13\62\5" +
                    "\62\u04b1\n\62\3\62\3\62\3\62\3\62\7\62\u04b7\n\62\f\62\16\62\u04ba\13" +
                    "\62\3\62\3\62\3\62\3\62\3\62\7\62\u04c1\n\62\f\62\16\62\u04c4\13\62\5" +
                    "\62\u04c6\n\62\3\62\3\62\3\62\3\62\5\62\u04cc\n\62\5\62\u04ce\n\62\3\63" +
                    "\3\63\5\63\u04d2\n\63\3\63\3\63\3\63\7\63\u04d7\n\63\f\63\16\63\u04da" +
                    "\13\63\3\63\3\63\3\63\3\63\7\63\u04e0\n\63\f\63\16\63\u04e3\13\63\3\63" +
                    "\5\63\u04e6\n\63\5\63\u04e8\n\63\3\63\3\63\5\63\u04ec\n\63\3\63\3\63\3" +
                    "\63\3\63\3\63\7\63\u04f3\n\63\f\63\16\63\u04f6\13\63\3\63\3\63\5\63\u04fa" +
                    "\n\63\5\63\u04fc\n\63\3\63\3\63\3\63\3\63\3\63\7\63\u0503\n\63\f\63\16" +
                    "\63\u0506\13\63\3\63\3\63\3\63\3\63\3\63\3\63\7\63\u050e\n\63\f\63\16" +
                    "\63\u0511\13\63\3\63\3\63\7\63\u0515\n\63\f\63\16\63\u0518\13\63\5\63" +
                    "\u051a\n\63\3\64\5\64\u051d\n\64\3\64\3\64\3\64\3\64\3\64\3\64\3\64\3" +
                    "\64\3\64\3\64\3\64\5\64\u052a\n\64\3\64\3\64\3\64\3\64\3\64\3\64\3\64" +
                    "\3\64\3\64\3\64\7\64\u0536\n\64\f\64\16\64\u0539\13\64\3\64\3\64\5\64" +
                    "\u053d\n\64\3\65\5\65\u0540\n\65\3\65\3\65\3\65\3\65\3\65\3\65\3\65\3" +
                    "\65\3\65\3\65\3\65\5\65\u054d\n\65\3\65\3\65\3\65\3\65\3\65\3\65\3\65" +
                    "\3\65\3\65\3\65\7\65\u0559\n\65\f\65\16\65\u055c\13\65\3\65\3\65\5\65" +
                    "\u0560\n\65\3\65\3\65\3\65\3\65\3\65\7\65\u0567\n\65\f\65\16\65\u056a" +
                    "\13\65\5\65\u056c\n\65\3\65\3\65\3\65\3\65\5\65\u0572\n\65\5\65\u0574" +
                    "\n\65\3\66\3\66\3\67\3\67\3\67\7\67\u057b\n\67\f\67\16\67\u057e\13\67" +
                    "\38\38\38\38\58\u0584\n8\38\38\38\38\38\58\u058b\n8\38\38\38\58\u0590" +
                    "\n8\38\38\58\u0594\n8\39\39\59\u0598\n9\39\39\39\39\39\39\39\39\39\39" +
                    "\39\39\39\39\59\u05a8\n9\3:\3:\3:\5:\u05ad\n:\3:\3:\5:\u05b1\n:\3;\3;" +
                    "\3<\3<\3<\3<\3=\3=\3=\3>\3>\3>\3>\3>\3>\3>\3>\3>\3>\3>\3>\5>\u05c8\n>" +
                    "\3>\3>\6>\u05cc\n>\r>\16>\u05cd\5>\u05d0\n>\3?\3?\5?\u05d4\n?\3@\3@\3" +
                    "@\5@\u05d9\n@\3A\3A\3A\3A\3A\3A\5A\u05e1\nA\3A\3A\3A\5A\u05e6\nA\3A\3" +
                    "A\3A\3A\3A\3A\3A\5A\u05ef\nA\3A\3A\3A\7A\u05f4\nA\fA\16A\u05f7\13A\3A" +
                    "\5A\u05fa\nA\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\5A\u060a\nA\3A" +
                    "\5A\u060d\nA\3A\3A\3A\3A\3A\3A\5A\u0615\nA\3A\3A\3A\3A\3A\6A\u061c\nA" +
                    "\rA\16A\u061d\3A\3A\5A\u0622\nA\3A\3A\3A\5A\u0627\nA\3A\3A\3A\3A\3A\3" +
                    "A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\5" +
                    "A\u0645\nA\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\5A\u0651\nA\3A\3A\3A\5A\u0656" +
                    "\nA\3A\3A\3A\3A\3A\3A\3A\3A\3A\3A\5A\u0662\nA\3A\3A\3A\3A\5A\u0668\nA" +
                    "\3A\3A\3A\3A\3A\5A\u066f\nA\3A\3A\5A\u0673\nA\3A\3A\3A\3A\3A\3A\7A\u067b" +
                    "\nA\fA\16A\u067e\13A\5A\u0680\nA\3A\3A\3A\3A\5A\u0686\nA\3A\5A\u0689\n" +
                    "A\7A\u068b\nA\fA\16A\u068e\13A\3B\3B\3B\3B\5B\u0694\nB\3B\3B\3B\3B\3B" +
                    "\7B\u069b\nB\fB\16B\u069e\13B\3B\3B\5B\u06a2\nB\3B\3B\3B\3B\3B\3B\3B\3" +
                    "B\3B\3B\5B\u06ae\nB\3B\3B\5B\u06b2\nB\7B\u06b4\nB\fB\16B\u06b7\13B\3B" +
                    "\5B\u06ba\nB\3B\3B\3B\3B\3B\5B\u06c1\nB\3B\5B\u06c4\nB\5B\u06c6\nB\3C" +
                    "\3C\3D\3D\3D\3D\3D\3D\5D\u06d0\nD\3D\3D\3E\3E\3E\5E\u06d7\nE\3E\5E\u06da" +
                    "\nE\3F\3F\5F\u06de\nF\3F\3F\3F\3F\3F\3F\3F\3F\3F\5F\u06e9\nF\3G\3G\3G" +
                    "\3G\3G\3G\7G\u06f1\nG\fG\16G\u06f4\13G\3G\3G\3G\3H\3H\3H\3H\3H\3H\7H\u06ff" +
                    "\nH\fH\16H\u0702\13H\3H\3H\3H\3I\3I\5I\u0709\nI\3I\5I\u070c\nI\3I\3I\3" +
                    "I\3I\7I\u0712\nI\fI\16I\u0715\13I\3I\3I\3I\3J\3J\5J\u071c\nJ\3J\3J\3J" +
                    "\3J\7J\u0722\nJ\fJ\16J\u0725\13J\3J\3J\3J\3K\3K\3L\3L\5L\u072e\nL\3L\3" +
                    "L\3L\3L\3L\3L\3L\3L\3L\3L\3L\3L\7L\u073c\nL\fL\16L\u073f\13L\3M\3M\3M" +
                    "\5M\u0744\nM\3M\3M\3M\3M\3M\3M\5M\u074c\nM\3N\3N\3N\5N\u0751\nN\3N\5N" +
                    "\u0754\nN\3O\3O\3P\3P\3P\5P\u075b\nP\3Q\3Q\3Q\3Q\3Q\7Q\u0762\nQ\fQ\16" +
                    "Q\u0765\13Q\3Q\3Q\5Q\u0769\nQ\3Q\3Q\3Q\3Q\3Q\3R\3R\3R\3R\3R\3R\3R\5R\u0777" +
                    "\nR\3R\5R\u077a\nR\5R\u077c\nR\3S\3S\3S\5S\u0781\nS\3S\3S\5S\u0785\nS" +
                    "\3S\5S\u0788\nS\3S\3S\3S\3S\3S\5S\u078f\nS\3S\3S\3S\3S\7S\u0795\nS\fS" +
                    "\16S\u0798\13S\3S\5S\u079b\nS\3S\3S\5S\u079f\nS\3S\5S\u07a2\nS\3S\3S\3" +
                    "S\3S\5S\u07a8\nS\3S\5S\u07ab\nS\5S\u07ad\nS\3T\3T\3T\3T\3T\7T\u07b4\n" +
                    "T\fT\16T\u07b7\13T\3U\3U\5U\u07bb\nU\3U\3U\5U\u07bf\nU\3U\3U\5U\u07c3" +
                    "\nU\3U\5U\u07c6\nU\3V\3V\3V\3V\3V\3V\3V\7V\u07cf\nV\fV\16V\u07d2\13V\3" +
                    "V\3V\5V\u07d6\nV\3W\3W\5W\u07da\nW\3W\3W\3W\7W\u07df\nW\fW\16W\u07e2\13" +
                    "W\3W\3W\3W\3W\7W\u07e8\nW\fW\16W\u07eb\13W\3W\5W\u07ee\nW\5W\u07f0\nW" +
                    "\3W\3W\5W\u07f4\nW\3W\3W\3W\3W\3W\7W\u07fb\nW\fW\16W\u07fe\13W\3W\3W\5" +
                    "W\u0802\nW\5W\u0804\nW\3W\3W\3W\3W\3W\7W\u080b\nW\fW\16W\u080e\13W\3W" +
                    "\3W\3W\3W\3W\3W\7W\u0816\nW\fW\16W\u0819\13W\3W\3W\7W\u081d\nW\fW\16W" +
                    "\u0820\13W\5W\u0822\nW\3X\3X\3X\3X\3X\5X\u0829\nX\3Y\3Y\3Y\3Y\3Y\7Y\u0830" +
                    "\nY\fY\16Y\u0833\13Y\3Y\3Y\5Y\u0837\nY\3Z\5Z\u083a\nZ\3Z\3Z\5Z\u083e\n" +
                    "Z\3[\3[\3\\\3\\\3]\3]\3^\3^\5^\u0848\n^\3_\3_\3`\3`\3a\6a\u084f\na\ra" +
                    "\16a\u0850\3b\3b\3c\3c\3d\3d\3e\3e\3f\3f\3g\3g\3h\3h\3i\3i\3j\3j\3k\3" +
                    "k\3l\3l\3m\3m\3n\3n\3o\3o\3p\3p\3q\3q\3r\3r\3s\3s\3t\3t\3u\3u\3v\3v\3" +
                    "w\3w\3w\3w\3w\3w\3w\5w\u0884\nw\3w\2\3\u0080x\2\4\6\b\n\f\16\20\22\24" +
                    "\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtv" +
                    "xz|~\u0080\u0082\u0084\u0086\u0088\u008a\u008c\u008e\u0090\u0092\u0094" +
                    "\u0096\u0098\u009a\u009c\u009e\u00a0\u00a2\u00a4\u00a6\u00a8\u00aa\u00ac" +
                    "\u00ae\u00b0\u00b2\u00b4\u00b6\u00b8\u00ba\u00bc\u00be\u00c0\u00c2\u00c4" +
                    "\u00c6\u00c8\u00ca\u00cc\u00ce\u00d0\u00d2\u00d4\u00d6\u00d8\u00da\u00dc" +
                    "\u00de\u00e0\u00e2\u00e4\u00e6\u00e8\u00ea\u00ec\2\32\5\2__kkyy\4\2RR" +
                    "gg\4\2%%\u0092\u0092\3\2\u00af\u00b0\4\2KK\u0098\u0098\4\2&&]]\5\2\7\7" +
                    "\20\20\30\30\4\2\13\13\31\32\4\2\f\f\27\27\3\2\21\23\4\2==cc\4\2BBaa\7" +
                    "\299ooxx\u00a5\u00a5\u00a9\u00a9\4\2\'\',-\3\2()\3\2.\61\3\2\62\65\6\2" +
                    "tt\u0088\u0088\u008a\u008a\u00a1\u00a1\4\2``\u00b8\u00b8\5\299oo\u00a9" +
                    "\u00a9\6\2XZ\u0090\u0090\u00c4\u00c4\u00c6\u00c7\4\2(*\u008e\u008e\4\2" +
                    "\u00c3\u00c3\u00c6\u00c6\17\29CEHJJLUW[]km\u0093\u0095\u0097\u0099\u009a" +
                    "\u009c\u00a6\u00a8\u00b4\u00b6\u00bb\u00bd\u00c2\2\u09c2\2\u00f2\3\2\2" +
                    "\2\4\u00f7\3\2\2\2\6\u00fd\3\2\2\2\b\u0117\3\2\2\2\n\u013b\3\2\2\2\f\u0158" +
                    "\3\2\2\2\16\u015d\3\2\2\2\20\u0160\3\2\2\2\22\u0169\3\2\2\2\24\u0171\3" +
                    "\2\2\2\26\u017b\3\2\2\2\30\u018e\3\2\2\2\32\u01b2\3\2\2\2\34\u01d2\3\2" +
                    "\2\2\36\u01f9\3\2\2\2 \u023e\3\2\2\2\"\u0251\3\2\2\2$\u026e\3\2\2\2&\u0277" +
                    "\3\2\2\2(\u0286\3\2\2\2*\u02b6\3\2\2\2,\u02b8\3\2\2\2.\u02cb\3\2\2\2\60" +
                    "\u02cf\3\2\2\2\62\u02d1\3\2\2\2\64\u02d3\3\2\2\2\66\u02d8\3\2\2\28\u02e1" +
                    "\3\2\2\2:\u02ed\3\2\2\2<\u034a\3\2\2\2>\u034c\3\2\2\2@\u034f\3\2\2\2B" +
                    "\u0353\3\2\2\2D\u0377\3\2\2\2F\u0381\3\2\2\2H\u039e\3\2\2\2J\u03a4\3\2" +
                    "\2\2L\u03b1\3\2\2\2N\u03be\3\2\2\2P\u03cb\3\2\2\2R\u03e4\3\2\2\2T\u0404" +
                    "\3\2\2\2V\u044e\3\2\2\2X\u045d\3\2\2\2Z\u046a\3\2\2\2\\\u0470\3\2\2\2" +
                    "^\u047e\3\2\2\2`\u048d\3\2\2\2b\u04b0\3\2\2\2d\u0519\3\2\2\2f\u051c\3" +
                    "\2\2\2h\u053f\3\2\2\2j\u0575\3\2\2\2l\u0577\3\2\2\2n\u057f\3\2\2\2p\u0597" +
                    "\3\2\2\2r\u05a9\3\2\2\2t\u05b2\3\2\2\2v\u05b4\3\2\2\2x\u05b8\3\2\2\2z" +
                    "\u05bb\3\2\2\2|\u05d3\3\2\2\2~\u05d8\3\2\2\2\u0080\u0626\3\2\2\2\u0082" +
                    "\u068f\3\2\2\2\u0084\u06c7\3\2\2\2\u0086\u06c9\3\2\2\2\u0088\u06d3\3\2" +
                    "\2\2\u008a\u06dd\3\2\2\2\u008c\u06ea\3\2\2\2\u008e\u06f8\3\2\2\2\u0090" +
                    "\u0706\3\2\2\2\u0092\u0719\3\2\2\2\u0094\u0729\3\2\2\2\u0096\u072b\3\2" +
                    "\2\2\u0098\u0743\3\2\2\2\u009a\u074d\3\2\2\2\u009c\u0755\3\2\2\2\u009e" +
                    "\u075a\3\2\2\2\u00a0\u075c\3\2\2\2\u00a2\u077b\3\2\2\2\u00a4\u07ac\3\2" +
                    "\2\2\u00a6\u07ae\3\2\2\2\u00a8\u07c5\3\2\2\2\u00aa\u07d5\3\2\2\2\u00ac" +
                    "\u0821\3\2\2\2\u00ae\u0828\3\2\2\2\u00b0\u082a\3\2\2\2\u00b2\u083d\3\2" +
                    "\2\2\u00b4\u083f\3\2\2\2\u00b6\u0841\3\2\2\2\u00b8\u0843\3\2\2\2\u00ba" +
                    "\u0847\3\2\2\2\u00bc\u0849\3\2\2\2\u00be\u084b\3\2\2\2\u00c0\u084e\3\2" +
                    "\2\2\u00c2\u0852\3\2\2\2\u00c4\u0854\3\2\2\2\u00c6\u0856\3\2\2\2\u00c8" +
                    "\u0858\3\2\2\2\u00ca\u085a\3\2\2\2\u00cc\u085c\3\2\2\2\u00ce\u085e\3\2" +
                    "\2\2\u00d0\u0860\3\2\2\2\u00d2\u0862\3\2\2\2\u00d4\u0864\3\2\2\2\u00d6" +
                    "\u0866\3\2\2\2\u00d8\u0868\3\2\2\2\u00da\u086a\3\2\2\2\u00dc\u086c\3\2" +
                    "\2\2\u00de\u086e\3\2\2\2\u00e0\u0870\3\2\2\2\u00e2\u0872\3\2\2\2\u00e4" +
                    "\u0874\3\2\2\2\u00e6\u0876\3\2\2\2\u00e8\u0878\3\2\2\2\u00ea\u087a\3\2" +
                    "\2\2\u00ec\u0883\3\2\2\2\u00ee\u00f1\5\6\4\2\u00ef\u00f1\5\4\3\2\u00f0" +
                    "\u00ee\3\2\2\2\u00f0\u00ef\3\2\2\2\u00f1\u00f4\3\2\2\2\u00f2\u00f0\3\2" +
                    "\2\2\u00f2\u00f3\3\2\2\2\u00f3\u00f5\3\2\2\2\u00f4\u00f2\3\2\2\2\u00f5" +
                    "\u00f6\7\2\2\3\u00f6\3\3\2\2\2\u00f7\u00f8\7\u00cb\2\2\u00f8\u00f9\b\3" +
                    "\1\2\u00f9\5\3\2\2\2\u00fa\u00fc\7!\2\2\u00fb\u00fa\3\2\2\2\u00fc\u00ff" +
                    "\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fd\u00fe\3\2\2\2\u00fe\u0100\3\2\2\2\u00ff" +
                    "\u00fd\3\2\2\2\u0100\u0109\5\b\5\2\u0101\u0103\7!\2\2\u0102\u0101\3\2" +
                    "\2\2\u0103\u0104\3\2\2\2\u0104\u0102\3\2\2\2\u0104\u0105\3\2\2\2\u0105" +
                    "\u0106\3\2\2\2\u0106\u0108\5\b\5\2\u0107\u0102\3\2\2\2\u0108\u010b\3\2" +
                    "\2\2\u0109\u0107\3\2\2\2\u0109\u010a\3\2\2\2\u010a\u010f\3\2\2\2\u010b" +
                    "\u0109\3\2\2\2\u010c\u010e\7!\2\2\u010d\u010c\3\2\2\2\u010e\u0111\3\2" +
                    "\2\2\u010f\u010d\3\2\2\2\u010f\u0110\3\2\2\2\u0110\7\3\2\2\2\u0111\u010f" +
                    "\3\2\2\2\u0112\u0115\7n\2\2\u0113\u0114\7\u009d\2\2\u0114\u0116\7\u0099" +
                    "\2\2\u0115\u0113\3\2\2\2\u0115\u0116\3\2\2\2\u0116\u0118\3\2\2\2\u0117" +
                    "\u0112\3\2\2\2\u0117\u0118\3\2\2\2\u0118\u0139\3\2\2\2\u0119\u013a\5\n" +
                    "\6\2\u011a\u013a\5\20\t\2\u011b\u013a\5\22\n\2\u011c\u013a\5\24\13\2\u011d" +
                    "\u013a\5\26\f\2\u011e\u013a\5\30\r\2\u011f\u013a\5\32\16\2\u0120\u013a" +
                    "\5\34\17\2\u0121\u013a\5\36\20\2\u0122\u013a\5 \21\2\u0123\u013a\5\"\22" +
                    "\2\u0124\u013a\5$\23\2\u0125\u013a\5&\24\2\u0126\u013a\5D#\2\u0127\u013a" +
                    "\5F$\2\u0128\u013a\5H%\2\u0129\u013a\5J&\2\u012a\u013a\5L\'\2\u012b\u013a" +
                    "\5N(\2\u012c\u013a\5P)\2\u012d\u013a\5R*\2\u012e\u013a\5T+\2\u012f\u013a" +
                    "\5V,\2\u0130\u013a\5X-\2\u0131\u013a\5Z.\2\u0132\u013a\5\\/\2\u0133\u013a" +
                    "\5^\60\2\u0134\u013a\5`\61\2\u0135\u013a\5b\62\2\u0136\u013a\5f\64\2\u0137" +
                    "\u013a\5h\65\2\u0138\u013a\5j\66\2\u0139\u0119\3\2\2\2\u0139\u011a\3\2" +
                    "\2\2\u0139\u011b\3\2\2\2\u0139\u011c\3\2\2\2\u0139\u011d\3\2\2\2\u0139" +
                    "\u011e\3\2\2\2\u0139\u011f\3\2\2\2\u0139\u0120\3\2\2\2\u0139\u0121\3\2" +
                    "\2\2\u0139\u0122\3\2\2\2\u0139\u0123\3\2\2\2\u0139\u0124\3\2\2\2\u0139" +
                    "\u0125\3\2\2\2\u0139\u0126\3\2\2\2\u0139\u0127\3\2\2\2\u0139\u0128\3\2" +
                    "\2\2\u0139\u0129\3\2\2\2\u0139\u012a\3\2\2\2\u0139\u012b\3\2\2\2\u0139" +
                    "\u012c\3\2\2\2\u0139\u012d\3\2\2\2\u0139\u012e\3\2\2\2\u0139\u012f\3\2" +
                    "\2\2\u0139\u0130\3\2\2\2\u0139\u0131\3\2\2\2\u0139\u0132\3\2\2\2\u0139" +
                    "\u0133\3\2\2\2\u0139\u0134\3\2\2\2\u0139\u0135\3\2\2\2\u0139\u0136\3\2" +
                    "\2\2\u0139\u0137\3\2\2\2\u0139\u0138\3\2\2\2\u013a\t\3\2\2\2\u013b\u013c" +
                    "\7>\2\2\u013c\u013e\7\u00ae\2\2\u013d\u013f\7\u0094\2\2\u013e\u013d\3" +
                    "\2\2\2\u013e\u013f\3\2\2\2\u013f\u0143\3\2\2\2\u0140\u0141\5\u00c6d\2" +
                    "\u0141\u0142\7\"\2\2\u0142\u0144\3\2\2\2\u0143\u0140\3\2\2\2\u0143\u0144" +
                    "\3\2\2\2\u0144\u0145\3\2\2\2\u0145\u0150\5\u00caf\2\u0146\u0147\7\u00a4" +
                    "\2\2\u0147\u0148\7\u00b2\2\2\u0148\u0151\5\u00d2j\2\u0149\u0151\5\16\b" +
                    "\2\u014a\u0151\5\f\7\2\u014b\u014d\7;\2\2\u014c\u014e\7Q\2\2\u014d\u014c" +
                    "\3\2\2\2\u014d\u014e\3\2\2\2\u014e\u014f\3\2\2\2\u014f\u0151\5l\67\2\u0150" +
                    "\u0146\3\2\2\2\u0150\u0149\3\2\2\2\u0150\u014a\3\2\2\2\u0150\u014b\3\2" +
                    "\2\2\u0151\u0153\3\2\2\2\u0152\u0154\7h\2\2\u0153\u0152\3\2\2\2\u0153" +
                    "\u0154\3\2\2\2\u0154\u0156\3\2\2\2\u0155\u0157\5\u00c0a\2\u0156\u0155" +
                    "\3\2\2\2\u0156\u0157\3\2\2\2\u0157\13\3\2\2\2\u0158\u0159\7;\2\2\u0159" +
                    "\u015a\7T\2\2\u015a\u015b\5\u00ecw\2\u015b\u015c\5\u008aF\2\u015c\r\3" +
                    "\2\2\2\u015d\u015e\7;\2\2\u015e\u015f\5\u008aF\2\u015f\17\3\2\2\2\u0160" +
                    "\u0167\7?\2\2\u0161\u0168\5\u00c6d\2\u0162\u0168\5\u00d0i\2\u0163\u0164" +
                    "\5\u00c6d\2\u0164\u0165\7\"\2\2\u0165\u0166\5\u00d0i\2\u0166\u0168\3\2" +
                    "\2\2\u0167\u0161\3\2\2\2\u0167\u0162\3\2\2\2\u0167\u0163\3\2\2\2\u0167" +
                    "\u0168\3\2\2\2\u0168\21\3\2\2\2\u0169\u016b\7C\2\2\u016a\u016c\7[\2\2" +
                    "\u016b\u016a\3\2\2\2\u016b\u016c\3\2\2\2\u016c\u016d\3\2\2\2\u016d\u016e" +
                    "\5\u0080A\2\u016e\u016f\7A\2\2\u016f\u0170\5\u00c6d\2\u0170\23\3\2\2\2" +
                    "\u0171\u0173\7G\2\2\u0172\u0174\t\2\2\2\u0173\u0172\3\2\2\2\u0173\u0174" +
                    "\3\2\2\2\u0174\u0179\3\2\2\2\u0175\u0177\7\u00b3\2\2\u0176\u0178\5\u00e8" +
                    "u\2\u0177\u0176\3\2\2\2\u0177\u0178\3\2\2\2\u0178\u017a\3\2\2\2\u0179" +
                    "\u0175\3\2\2\2\u0179\u017a\3\2\2\2\u017a\25\3\2\2\2\u017b\u0180\t\3\2" +
                    "\2\u017c\u017e\7\u00b3\2\2\u017d\u017f\5\u00e8u\2\u017e\u017d\3\2\2\2" +
                    "\u017e\u017f\3\2\2\2\u017f\u0181\3\2\2\2\u0180\u017c\3\2\2\2\u0180\u0181" +
                    "\3\2\2\2\u0181\27\3\2\2\2\u0182\u0184\7\u00c1\2\2\u0183\u0185\7\u009f" +
                    "\2\2\u0184\u0183\3\2\2\2\u0184\u0185\3\2\2\2\u0185\u0186\3\2\2\2\u0186" +
                    "\u018b\5\u00a0Q\2\u0187\u0188\7%\2\2\u0188\u018a\5\u00a0Q\2\u0189\u0187" +
                    "\3\2\2\2\u018a\u018d\3\2\2\2\u018b\u0189\3\2\2\2\u018b\u018c\3\2\2\2\u018c" +
                    "\u018f\3\2\2\2\u018d\u018b\3\2\2\2\u018e\u0182\3\2\2\2\u018e\u018f\3\2" +
                    "\2\2\u018f\u0190\3\2\2\2\u0190\u019a\5\u00acW\2\u0191\u0193\7\u00b6\2" +
                    "\2\u0192\u0194\7=\2\2\u0193\u0192\3\2\2\2\u0193\u0194\3\2\2\2\u0194\u0198" +
                    "\3\2\2\2\u0195\u0198\7\u0081\2\2\u0196\u0198\7j\2\2\u0197\u0191\3\2\2" +
                    "\2\u0197\u0195\3\2\2\2\u0197\u0196\3\2\2\2\u0198\u0199\3\2\2\2\u0199\u019b" +
                    "\5\u00acW\2\u019a\u0197\3\2\2\2\u019b\u019c\3\2\2\2\u019c\u019a\3\2\2" +
                    "\2\u019c\u019d\3\2\2\2\u019d\u01a8\3\2\2\2\u019e\u019f\7\u0096\2\2\u019f" +
                    "\u01a0\7J\2\2\u01a0\u01a5\5\u009aN\2\u01a1\u01a2\7%\2\2\u01a2\u01a4\5" +
                    "\u009aN\2\u01a3\u01a1\3\2\2\2\u01a4\u01a7\3\2\2\2\u01a5\u01a3\3\2\2\2" +
                    "\u01a5\u01a6\3\2\2\2\u01a6\u01a9\3\2\2\2\u01a7\u01a5\3\2\2\2\u01a8\u019e" +
                    "\3\2\2\2\u01a8\u01a9\3\2\2\2\u01a9\u01b0\3\2\2\2\u01aa\u01ab\7\u0089\2" +
                    "\2\u01ab\u01ae\5\u0080A\2\u01ac\u01ad\t\4\2\2\u01ad\u01af\5\u0080A\2\u01ae" +
                    "\u01ac\3\2\2\2\u01ae\u01af\3\2\2\2\u01af\u01b1\3\2\2\2\u01b0\u01aa\3\2" +
                    "\2\2\u01b0\u01b1\3\2\2\2\u01b1\31\3\2\2\2\u01b2\u01b4\7U\2\2\u01b3\u01b5" +
                    "\7\u00b7\2\2\u01b4\u01b3\3\2\2\2\u01b4\u01b5\3\2\2\2\u01b5\u01b6\3\2\2" +
                    "\2\u01b6\u01ba\7{\2\2\u01b7\u01b8\7w\2\2\u01b8\u01b9\7\u008e\2\2\u01b9" +
                    "\u01bb\7m\2\2\u01ba\u01b7\3\2\2\2\u01ba\u01bb\3\2\2\2\u01bb\u01bf\3\2" +
                    "\2\2\u01bc\u01bd\5\u00c6d\2\u01bd\u01be\7\"\2\2\u01be\u01c0\3\2\2\2\u01bf" +
                    "\u01bc\3\2\2\2\u01bf\u01c0\3\2\2\2\u01c0\u01c1\3\2\2\2\u01c1\u01c2\5\u00da" +
                    "n\2\u01c2\u01c3\7\u0093\2\2\u01c3\u01c4\5\u00ccg\2\u01c4\u01c5\7#\2\2" +
                    "\u01c5\u01ca\5\u0088E\2\u01c6\u01c7\7%\2\2\u01c7\u01c9\5\u0088E\2\u01c8" +
                    "\u01c6\3\2\2\2\u01c9\u01cc\3\2\2\2\u01ca\u01c8\3\2\2\2\u01ca\u01cb\3\2" +
                    "\2\2\u01cb\u01cd\3\2\2\2\u01cc\u01ca\3\2\2\2\u01cd\u01d0\7$\2\2\u01ce" +
                    "\u01cf\7\u00c0\2\2\u01cf\u01d1\5\u0080A\2\u01d0\u01ce\3\2\2\2\u01d0\u01d1" +
                    "\3\2\2\2\u01d1\33\3\2\2\2\u01d2\u01d4\7U\2\2\u01d3\u01d5\t\5\2\2\u01d4" +
                    "\u01d3\3\2\2\2\u01d4\u01d5\3\2\2\2\u01d5\u01d6\3\2\2\2\u01d6\u01da\7\u00ae" +
                    "\2\2\u01d7\u01d8\7w\2\2\u01d8\u01d9\7\u008e\2\2\u01d9\u01db\7m\2\2\u01da" +
                    "\u01d7\3\2\2\2\u01da\u01db\3\2\2\2\u01db\u01df\3\2\2\2\u01dc\u01dd\5\u00c6" +
                    "d\2\u01dd\u01de\7\"\2\2\u01de\u01e0\3\2\2\2\u01df\u01dc\3\2\2\2\u01df" +
                    "\u01e0\3\2\2\2\u01e0\u01e1\3\2\2\2\u01e1\u01f4\5\u00ccg\2\u01e2\u01e3" +
                    "\7#\2\2\u01e3\u01ea\5l\67\2\u01e4\u01e5\7%\2\2\u01e5\u01e9\5\u008aF\2" +
                    "\u01e6\u01e7\7%\2\2\u01e7\u01e9\5l\67\2\u01e8\u01e4\3\2\2\2\u01e8\u01e6" +
                    "\3\2\2\2\u01e9\u01ec\3\2\2\2\u01ea\u01e8\3\2\2\2\u01ea\u01eb\3\2\2\2\u01eb" +
                    "\u01ed\3\2\2\2\u01ec\u01ea\3\2\2\2\u01ed\u01f0\7$\2\2\u01ee\u01ef\7\u00c2" +
                    "\2\2\u01ef\u01f1\7\u00c3\2\2\u01f0\u01ee\3\2\2\2\u01f0\u01f1\3\2\2\2\u01f1" +
                    "\u01f5\3\2\2\2\u01f2\u01f3\7A\2\2\u01f3\u01f5\5b\62\2\u01f4\u01e2\3\2" +
                    "\2\2\u01f4\u01f2\3\2\2\2\u01f5\u01f7\3\2\2\2\u01f6\u01f8\5\u00c0a\2\u01f7" +
                    "\u01f6\3\2\2\2\u01f7\u01f8\3\2\2\2\u01f8\35\3\2\2\2\u01f9\u01fb\7U\2\2" +
                    "\u01fa\u01fc\t\5\2\2\u01fb\u01fa\3\2\2\2\u01fb\u01fc\3\2\2\2\u01fc\u01fd" +
                    "\3\2\2\2\u01fd\u0201\7\u00b4\2\2\u01fe\u01ff\7w\2\2\u01ff\u0200\7\u008e" +
                    "\2\2\u0200\u0202\7m\2\2\u0201\u01fe\3\2\2\2\u0201\u0202\3\2\2\2\u0202" +
                    "\u0206\3\2\2\2\u0203\u0204\5\u00c6d\2\u0204\u0205\7\"\2\2\u0205\u0207" +
                    "\3\2\2\2\u0206\u0203\3\2\2\2\u0206\u0207\3\2\2\2\u0207\u0208\3\2\2\2\u0208" +
                    "\u020d\5\u00dco\2\u0209\u020e\7F\2\2\u020a\u020e\7<\2\2\u020b\u020c\7" +
                    "\u0080\2\2\u020c\u020e\7\u0091\2\2\u020d\u0209\3\2\2\2\u020d\u020a\3\2" +
                    "\2\2\u020d\u020b\3\2\2\2\u020d\u020e\3\2\2\2\u020e\u021d\3\2\2\2\u020f" +
                    "\u021e\7`\2\2\u0210\u021e\7\177\2\2\u0211\u021b\7\u00b8\2\2\u0212\u0213" +
                    "\7\u0091\2\2\u0213\u0218\5\u00d4k\2\u0214\u0215\7%\2\2\u0215\u0217\5\u00d4" +
                    "k\2\u0216\u0214\3\2\2\2\u0217\u021a\3\2\2\2\u0218\u0216\3\2\2\2\u0218" +
                    "\u0219\3\2\2\2\u0219\u021c\3\2\2\2\u021a\u0218\3\2\2\2\u021b\u0212\3\2" +
                    "\2\2\u021b\u021c\3\2\2\2\u021c\u021e\3\2\2\2\u021d\u020f\3\2\2\2\u021d" +
                    "\u0210\3\2\2\2\u021d\u0211\3\2\2\2\u021e\u021f\3\2\2\2\u021f\u0223\7\u0093" +
                    "\2\2\u0220\u0221\5\u00c6d\2\u0221\u0222\7\"\2\2\u0222\u0224\3\2\2\2\u0223" +
                    "\u0220\3\2\2\2\u0223\u0224\3\2\2\2\u0224\u0225\3\2\2\2\u0225\u0229\5\u00cc" +
                    "g\2\u0226\u0227\7p\2\2\u0227\u0228\7e\2\2\u0228\u022a\7\u00aa\2\2\u0229" +
                    "\u0226\3\2\2\2\u0229\u022a\3\2\2\2\u022a\u022d\3\2\2\2\u022b\u022c\7\u00bf" +
                    "\2\2\u022c\u022e\5\u0080A\2\u022d\u022b\3\2\2\2\u022d\u022e\3\2\2\2\u022e" +
                    "\u022f\3\2\2\2\u022f\u0238\7G\2\2\u0230\u0235\5f\64\2\u0231\u0235\5T+" +
                    "\2\u0232\u0235\5D#\2\u0233\u0235\5b\62\2\u0234\u0230\3\2\2\2\u0234\u0231" +
                    "\3\2\2\2\u0234\u0232\3\2\2\2\u0234\u0233\3\2\2\2\u0235\u0236\3\2\2\2\u0236" +
                    "\u0237\7!\2\2\u0237\u0239\3\2\2\2\u0238\u0234\3\2\2\2\u0239\u023a\3\2" +
                    "\2\2\u023a\u0238\3\2\2\2\u023a\u023b\3\2\2\2\u023b\u023c\3\2\2\2\u023c" +
                    "\u023d\7g\2\2\u023d\37\3\2\2\2\u023e\u0240\7U\2\2\u023f\u0241\t\5\2\2" +
                    "\u0240\u023f\3\2\2\2\u0240\u0241\3\2\2\2\u0241\u0242\3\2\2\2\u0242\u0246" +
                    "\7\u00bd\2\2\u0243\u0244\7w\2\2\u0244\u0245\7\u008e\2\2\u0245\u0247\7" +
                    "m\2\2\u0246\u0243\3\2\2\2\u0246\u0247\3\2\2\2\u0247\u024b\3\2\2\2\u0248" +
                    "\u0249\5\u00c6d\2\u0249\u024a\7\"\2\2\u024a\u024c\3\2\2\2\u024b\u0248" +
                    "\3\2\2\2\u024b\u024c\3\2\2\2\u024c\u024d\3\2\2\2\u024d\u024e\5\u00dep" +
                    "\2\u024e\u024f\7A\2\2\u024f\u0250\5b\62\2\u0250!\3\2\2\2\u0251\u0252\7" +
                    "U\2\2\u0252\u0253\7\u00be\2\2\u0253\u0257\7\u00ae\2\2\u0254\u0255\7w\2" +
                    "\2\u0255\u0256\7\u008e\2\2\u0256\u0258\7m\2\2\u0257\u0254\3\2\2\2\u0257" +
                    "\u0258\3\2\2\2\u0258\u025c\3\2\2\2\u0259\u025a\5\u00c6d\2\u025a\u025b" +
                    "\7\"\2\2\u025b\u025d\3\2\2\2\u025c\u0259\3\2\2\2\u025c\u025d\3\2\2\2\u025d" +
                    "\u025e\3\2\2\2\u025e\u025f\5\u00ccg\2\u025f\u0260\7\u00b9\2\2\u0260\u026c" +
                    "\5\u00e0q\2\u0261\u0262\7#\2\2\u0262\u0267\5\u00ba^\2\u0263\u0264\7%\2" +
                    "\2\u0264\u0266\5\u00ba^\2\u0265\u0263\3\2\2\2\u0266\u0269\3\2\2\2\u0267" +
                    "\u0265\3\2\2\2\u0267\u0268\3\2\2\2\u0268\u026a\3\2\2\2\u0269\u0267\3\2" +
                    "\2\2\u026a\u026b\7$\2\2\u026b\u026d\3\2\2\2\u026c\u0261\3\2\2\2\u026c" +
                    "\u026d\3\2\2\2\u026d#\3\2\2\2\u026e\u026f\7U\2\2\u026f\u0270\7\u009b\2" +
                    "\2\u0270\u0273\5\u00ceh\2\u0271\u0272\7D\2\2\u0272\u0274\t\6\2\2\u0273" +
                    "\u0271\3\2\2\2\u0273\u0274\3\2\2\2\u0274\u0275\3\2\2\2\u0275\u0276\5(" +
                    "\25\2\u0276%\3\2\2\2\u0277\u0278\7l\2\2\u0278\u0279\7I\2\2\u0279\u027a" +
                    "\5(\25\2\u027a\'\3\2\2\2\u027b\u027c\7#\2\2\u027c\u0281\5\64\33\2\u027d" +
                    "\u027e\7%\2\2\u027e\u0280\5\64\33\2\u027f\u027d\3\2\2\2\u0280\u0283\3" +
                    "\2\2\2\u0281\u027f\3\2\2\2\u0281\u0282\3\2\2\2\u0282\u0284\3\2\2\2\u0283" +
                    "\u0281\3\2\2\2\u0284\u0285\7$\2\2\u0285\u0287\3\2\2\2\u0286\u027b\3\2" +
                    "\2\2\u0286\u0287\3\2\2\2\u0287\u0294\3\2\2\2\u0288\u0289\7\u00a7\2\2\u0289" +
                    "\u028a\7#\2\2\u028a\u028f\5.\30\2\u028b\u028c\7%\2\2\u028c\u028e\5.\30" +
                    "\2\u028d\u028b\3\2\2\2\u028e\u0291\3\2\2\2\u028f\u028d\3\2\2\2\u028f\u0290" +
                    "\3\2\2\2\u0290\u0292\3\2\2\2\u0291\u028f\3\2\2\2\u0292\u0293\7$\2\2\u0293" +
                    "\u0295\3\2\2\2\u0294\u0288\3\2\2\2\u0294\u0295\3\2\2\2\u0295\u0296\3\2" +
                    "\2\2\u0296\u029a\7A\2\2\u0297\u0299\5,\27\2\u0298\u0297\3\2\2\2\u0299" +
                    "\u029c\3\2\2\2\u029a\u0298\3\2\2\2\u029a\u029b\3\2\2\2\u029b\u029d\3\2" +
                    "\2\2\u029c\u029a\3\2\2\2\u029d\u029e\7G\2\2\u029e\u029f\5*\26\2\u029f" +
                    "\u02a0\7g\2\2\u02a0)\3\2\2\2\u02a1\u02a3\13\2\2\2\u02a2\u02a1\3\2\2\2" +
                    "\u02a3\u02a6\3\2\2\2\u02a4\u02a2\3\2\2\2\u02a4\u02a5\3\2\2\2\u02a5\u02b7" +
                    "\3\2\2\2\u02a6\u02a4\3\2\2\2\u02a7\u02a9\13\2\2\2\u02a8\u02a7\3\2\2\2" +
                    "\u02a9\u02ac\3\2\2\2\u02aa\u02a8\3\2\2\2\u02aa\u02ab\3\2\2\2\u02ab\u02ad" +
                    "\3\2\2\2\u02ac\u02aa\3\2\2\2\u02ad\u02ae\7G\2\2\u02ae\u02af\5*\26\2\u02af" +
                    "\u02b3\7g\2\2\u02b0\u02b2\13\2\2\2\u02b1\u02b0\3\2\2\2\u02b2\u02b5\3\2" +
                    "\2\2\u02b3\u02b1\3\2\2\2\u02b3\u02b4\3\2\2\2\u02b4\u02b7\3\2\2\2\u02b5" +
                    "\u02b3\3\2\2\2\u02b6\u02a4\3\2\2\2\u02b6\u02aa\3\2\2\2\u02b7+\3\2\2\2" +
                    "\u02b8\u02ba\7\\\2\2\u02b9\u02bb\7\u00bc\2\2\u02ba\u02b9\3\2\2\2\u02ba" +
                    "\u02bb\3\2\2\2\u02bb\u02bc\3\2\2\2\u02bc\u02bd\5\62\32\2\u02bd\u02bf\5" +
                    ":\36\2\u02be\u02c0\7\u008f\2\2\u02bf\u02be\3\2\2\2\u02bf\u02c0\3\2\2\2" +
                    "\u02c0\u02c3\3\2\2\2\u02c1\u02c2\7P\2\2\u02c2\u02c4\5\u009cO\2\u02c3\u02c1" +
                    "\3\2\2\2\u02c3\u02c4\3\2\2\2\u02c4\u02c7\3\2\2\2\u02c5\u02c6\t\7\2\2\u02c6" +
                    "\u02c8\5\60\31\2\u02c7\u02c5\3\2\2\2\u02c7\u02c8\3\2\2\2\u02c8\u02c9\3" +
                    "\2\2\2\u02c9\u02ca\7!\2\2\u02ca-\3\2\2\2\u02cb\u02cc\5\66\34\2\u02cc/" +
                    "\3\2\2\2\u02cd\u02d0\5\u00b4[\2\u02ce\u02d0\7\u00c5\2\2\u02cf\u02cd\3" +
                    "\2\2\2\u02cf\u02ce\3\2\2\2\u02d0\61\3\2\2\2\u02d1\u02d2\5\u00ecw\2\u02d2" +
                    "\63\3\2\2\2\u02d3\u02d6\5\66\34\2\u02d4\u02d5\t\7\2\2\u02d5\u02d7\5\60" +
                    "\31\2\u02d6\u02d4\3\2\2\2\u02d6\u02d7\3\2\2\2\u02d7\65\3\2\2\2\u02d8\u02d9" +
                    "\58\35\2\u02d9\u02db\5:\36\2\u02da\u02dc\7\u008f\2\2\u02db\u02da\3\2\2" +
                    "\2\u02db\u02dc\3\2\2\2\u02dc\u02df\3\2\2\2\u02dd\u02de\7P\2\2\u02de\u02e0" +
                    "\5\u009cO\2\u02df\u02dd\3\2\2\2\u02df\u02e0\3\2\2\2\u02e0\67\3\2\2\2\u02e1" +
                    "\u02e2\5\u00ecw\2\u02e29\3\2\2\2\u02e3\u02ee\5<\37\2\u02e4\u02e6\7\u00b5" +
                    "\2\2\u02e5\u02e4\3\2\2\2\u02e5\u02e6\3\2\2\2\u02e6\u02e7\3\2\2\2\u02e7" +
                    "\u02ee\5\u00c8e\2\u02e8\u02e9\7\u00b5\2\2\u02e9\u02ea\5\u00ccg\2\u02ea" +
                    "\u02eb\7\"\2\2\u02eb\u02ec\5\u00d4k\2\u02ec\u02ee\3\2\2\2\u02ed\u02e3" +
                    "\3\2\2\2\u02ed\u02e5\3\2\2\2\u02ed\u02e8\3\2\2\2\u02ee;\3\2\2\2\u02ef" +
                    "\u02f1\t\b\2\2\u02f0\u02f2\5B\"\2\u02f1\u02f0\3\2\2\2\u02f1\u02f2\3\2" +
                    "\2\2\u02f2\u034b\3\2\2\2\u02f3\u02f7\7\16\2\2\u02f4\u02f5\7\u00cc\2\2" +
                    "\u02f5\u02f7\7\u00cd\2\2\u02f6\u02f3\3\2\2\2\u02f6\u02f4\3\2\2\2\u02f7" +
                    "\u02f9\3\2\2\2\u02f8\u02fa\5B\"\2\u02f9\u02f8\3\2\2\2\u02f9\u02fa\3\2" +
                    "\2\2\u02fa\u034b\3\2\2\2\u02fb\u02fd\t\t\2\2\u02fc\u02fe\5B\"\2\u02fd" +
                    "\u02fc\3\2\2\2\u02fd\u02fe\3\2\2\2\u02fe\u034b\3\2\2\2\u02ff\u0308\t\n" +
                    "\2\2\u0300\u0301\7#\2\2\u0301\u0304\5@!\2\u0302\u0303\7%\2\2\u0303\u0305" +
                    "\5@!\2\u0304\u0302\3\2\2\2\u0304\u0305\3\2\2\2\u0305\u0306\3\2\2\2\u0306" +
                    "\u0307\7$\2\2\u0307\u0309\3\2\2\2\u0308\u0300\3\2\2\2\u0308\u0309\3\2" +
                    "\2\2\u0309\u030b\3\2\2\2\u030a\u030c\5B\"\2\u030b\u030a\3\2\2\2\u030b" +
                    "\u030c\3\2\2\2\u030c\u034b\3\2\2\2\u030d\u034b\7\t\2\2\u030e\u034b\7\n" +
                    "\2\2\u030f\u034b\7\u00ce\2\2\u0310\u0315\7\34\2\2\u0311\u0312\7#\2\2\u0312" +
                    "\u0313\5@!\2\u0313\u0314\7$\2\2\u0314\u0316\3\2\2\2\u0315\u0311\3\2\2" +
                    "\2\u0315\u0316\3\2\2\2\u0316\u0319\3\2\2\2\u0317\u0318\7 \2\2\u0318\u031a" +
                    "\5\u00eav\2\u0319\u0317\3\2\2\2\u0319\u031a\3\2\2\2\u031a\u031c\3\2\2" +
                    "\2\u031b\u031d\5B\"\2\u031c\u031b\3\2\2\2\u031c\u031d\3\2\2\2\u031d\u034b" +
                    "\3\2\2\2\u031e\u0320\t\13\2\2\u031f\u0321\7\35\2\2\u0320\u031f\3\2\2\2" +
                    "\u0320\u0321\3\2\2\2\u0321\u0329\3\2\2\2\u0322\u0324\7#\2\2\u0323\u0325" +
                    "\7\u00cf\2\2\u0324\u0323\3\2\2\2\u0325\u0326\3\2\2\2\u0326\u0324\3\2\2" +
                    "\2\u0326\u0327\3\2\2\2\u0327\u0328\3\2\2\2\u0328\u032a\7$\2\2\u0329\u0322" +
                    "\3\2\2\2\u0329\u032a\3\2\2\2\u032a\u032c\3\2\2\2\u032b\u032d\5B\"\2\u032c" +
                    "\u032b\3\2\2\2\u032c\u032d\3\2\2\2\u032d\u034b\3\2\2\2\u032e\u0334\7\b" +
                    "\2\2\u032f\u0332\7\36\2\2\u0330\u0333\5\u00ecw\2\u0331\u0333\5@!\2\u0332" +
                    "\u0330\3\2\2\2\u0332\u0331\3\2\2\2\u0333\u0335\3\2\2\2\u0334\u032f\3\2" +
                    "\2\2\u0334\u0335\3\2\2\2\u0335\u0339\3\2\2\2\u0336\u0337\7\u00d0\2\2\u0337" +
                    "\u0338\7\u00d1\2\2\u0338\u033a\5@!\2\u0339\u0336\3\2\2\2\u0339\u033a\3" +
                    "\2\2\2\u033a\u033d\3\2\2\2\u033b\u033c\7 \2\2\u033c\u033e\5\u00eav\2\u033d" +
                    "\u033b\3\2\2\2\u033d\u033e\3\2\2\2\u033e\u034b\3\2\2\2\u033f\u0348\7\b" +
                    "\2\2\u0340\u0341\7#\2\2\u0341\u0344\5> \2\u0342\u0343\7%\2\2\u0343\u0345" +
                    "\5@!\2\u0344\u0342\3\2\2\2\u0344\u0345\3\2\2\2\u0345\u0346\3\2\2\2\u0346" +
                    "\u0347\7$\2\2\u0347\u0349\3\2\2\2\u0348\u0340\3\2\2\2\u0348\u0349\3\2" +
                    "\2\2\u0349\u034b\3\2\2\2\u034a\u02ef\3\2\2\2\u034a\u02f6\3\2\2\2\u034a" +
                    "\u02fb\3\2\2\2\u034a\u02ff\3\2\2\2\u034a\u030d\3\2\2\2\u034a\u030e\3\2" +
                    "\2\2\u034a\u030f\3\2\2\2\u034a\u0310\3\2\2\2\u034a\u031e\3\2\2\2\u034a" +
                    "\u032e\3\2\2\2\u034a\u033f\3\2\2\2\u034b=\3\2\2\2\u034c\u034d\5@!\2\u034d" +
                    "?\3\2\2\2\u034e\u0350\7\u00cf\2\2\u034f\u034e\3\2\2\2\u0350\u0351\3\2" +
                    "\2\2\u0351\u034f\3\2\2\2\u0351\u0352\3\2\2\2\u0352A\3\2\2\2\u0353\u035a" +
                    "\7\3\2\2\u0354\u0356\7\u00cf\2\2\u0355\u0354\3\2\2\2\u0356\u0357\3\2\2" +
                    "\2\u0357\u0355\3\2\2\2\u0357\u0358\3\2\2\2\u0358\u0359\3\2\2\2\u0359\u035b" +
                    "\7\4\2\2\u035a\u0355\3\2\2\2\u035a\u035b\3\2\2\2\u035b\u035d\3\2\2\2\u035c" +
                    "\u035e\7\u00cf\2\2\u035d\u035c\3\2\2\2\u035e\u035f\3\2\2\2\u035f\u035d" +
                    "\3\2\2\2\u035f\u0360\3\2\2\2\u0360\u0371\3\2\2\2\u0361\u0368\7%\2\2\u0362" +
                    "\u0364\7\u00cf\2\2\u0363\u0362\3\2\2\2\u0364\u0365\3\2\2\2\u0365\u0363" +
                    "\3\2\2\2\u0365\u0366\3\2\2\2\u0366\u0367\3\2\2\2\u0367\u0369\7\4\2\2\u0368" +
                    "\u0363\3\2\2\2\u0368\u0369\3\2\2\2\u0369\u036b\3\2\2\2\u036a\u036c\7\u00cf" +
                    "\2\2\u036b\u036a\3\2\2\2\u036c\u036d\3\2\2\2\u036d\u036b\3\2\2\2\u036d" +
                    "\u036e\3\2\2\2\u036e\u0370\3\2\2\2\u036f\u0361\3\2\2\2\u0370\u0373\3\2" +
                    "\2\2\u0371\u036f\3\2\2\2\u0371\u0372\3\2\2\2\u0372\u0374\3\2\2\2\u0373" +
                    "\u0371\3\2\2\2\u0374\u0375\7\5\2\2\u0375C\3\2\2\2\u0376\u0378\5\u0096" +
                    "L\2\u0377\u0376\3\2\2\2\u0377\u0378\3\2\2\2\u0378\u0379\3\2\2\2\u0379" +
                    "\u037a\7`\2\2\u037a\u037b\7r\2\2\u037b\u037e\5\u0098M\2\u037c\u037d\7" +
                    "\u00c0\2\2\u037d\u037f\5\u0080A\2\u037e\u037c\3\2\2\2\u037e\u037f\3\2" +
                    "\2\2\u037fE\3\2\2\2\u0380\u0382\5\u0096L\2\u0381\u0380\3\2\2\2\u0381\u0382" +
                    "\3\2\2\2\u0382\u0383\3\2\2\2\u0383\u0384\7`\2\2\u0384\u0385\7r\2\2\u0385" +
                    "\u0388\5\u0098M\2\u0386\u0387\7\u00c0\2\2\u0387\u0389\5\u0080A\2\u0388" +
                    "\u0386\3\2\2\2\u0388\u0389\3\2\2\2\u0389\u039c\3\2\2\2\u038a\u038b\7\u0096" +
                    "\2\2\u038b\u038c\7J\2\2\u038c\u0391\5\u009aN\2\u038d\u038e\7%\2\2\u038e" +
                    "\u0390\5\u009aN\2\u038f\u038d\3\2\2\2\u0390\u0393\3\2\2\2\u0391\u038f" +
                    "\3\2\2\2\u0391\u0392\3\2\2\2\u0392\u0395\3\2\2\2\u0393\u0391\3\2\2\2\u0394" +
                    "\u038a\3\2\2\2\u0394\u0395\3\2\2\2\u0395\u0396\3\2\2\2\u0396\u0397\7\u0089" +
                    "\2\2\u0397\u039a\5\u0080A\2\u0398\u0399\t\4\2\2\u0399\u039b\5\u0080A\2" +
                    "\u039a\u0398\3\2\2\2\u039a\u039b\3\2\2\2\u039b\u039d\3\2\2\2\u039c\u0394" +
                    "\3\2\2\2\u039c\u039d\3\2\2\2\u039dG\3\2\2\2\u039e\u03a0\7b\2\2\u039f\u03a1" +
                    "\7[\2\2\u03a0\u039f\3\2\2\2\u03a0\u03a1\3\2\2\2\u03a1\u03a2\3\2\2\2\u03a2" +
                    "\u03a3\5\u00c6d\2\u03a3I\3\2\2\2\u03a4\u03a5\7d\2\2\u03a5\u03a8\7{\2\2" +
                    "\u03a6\u03a7\7w\2\2\u03a7\u03a9\7m\2\2\u03a8\u03a6\3\2\2\2\u03a8\u03a9" +
                    "\3\2\2\2\u03a9\u03ad\3\2\2\2\u03aa\u03ab\5\u00c6d\2\u03ab\u03ac\7\"\2" +
                    "\2\u03ac\u03ae\3\2\2\2\u03ad\u03aa\3\2\2\2\u03ad\u03ae\3\2\2\2\u03ae\u03af" +
                    "\3\2\2\2\u03af\u03b0\5\u00dan\2\u03b0K\3\2\2\2\u03b1\u03b2\7d\2\2\u03b2" +
                    "\u03b5\7\u00ae\2\2\u03b3\u03b4\7w\2\2\u03b4\u03b6\7m\2\2\u03b5\u03b3\3" +
                    "\2\2\2\u03b5\u03b6\3\2\2\2\u03b6\u03ba\3\2\2\2\u03b7\u03b8\5\u00c6d\2" +
                    "\u03b8\u03b9\7\"\2\2\u03b9\u03bb\3\2\2\2\u03ba\u03b7\3\2\2\2\u03ba\u03bb" +
                    "\3\2\2\2\u03bb\u03bc\3\2\2\2\u03bc\u03bd\5\u00ccg\2\u03bdM\3\2\2\2\u03be" +
                    "\u03bf\7d\2\2\u03bf\u03c2\7\u00b4\2\2\u03c0\u03c1\7w\2\2\u03c1\u03c3\7" +
                    "m\2\2\u03c2\u03c0\3\2\2\2\u03c2\u03c3\3\2\2\2\u03c3\u03c7\3\2\2\2\u03c4" +
                    "\u03c5\5\u00c6d\2\u03c5\u03c6\7\"\2\2\u03c6\u03c8\3\2\2\2\u03c7\u03c4" +
                    "\3\2\2\2\u03c7\u03c8\3\2\2\2\u03c8\u03c9\3\2\2\2\u03c9\u03ca\5\u00dco" +
                    "\2\u03caO\3\2\2\2\u03cb\u03cc\7d\2\2\u03cc\u03cf\7\u00bd\2\2\u03cd\u03ce" +
                    "\7w\2\2\u03ce\u03d0\7m\2\2\u03cf\u03cd\3\2\2\2\u03cf\u03d0\3\2\2\2\u03d0" +
                    "\u03d4\3\2\2\2\u03d1\u03d2\5\u00c6d\2\u03d2\u03d3\7\"\2\2\u03d3\u03d5" +
                    "\3\2\2\2\u03d4\u03d1\3\2\2\2\u03d4\u03d5\3\2\2\2\u03d5\u03d6\3\2\2\2\u03d6" +
                    "\u03d7\5\u00dep\2\u03d7Q\3\2\2\2\u03d8\u03da\7\u00c1\2\2\u03d9\u03db\7" +
                    "\u009f\2\2\u03da\u03d9\3\2\2\2\u03da\u03db\3\2\2\2\u03db\u03dc\3\2\2\2" +
                    "\u03dc\u03e1\5\u00a0Q\2\u03dd\u03de\7%\2\2\u03de\u03e0\5\u00a0Q\2\u03df" +
                    "\u03dd\3\2\2\2\u03e0\u03e3\3\2\2\2\u03e1\u03df\3\2\2\2\u03e1\u03e2\3\2" +
                    "\2\2\u03e2\u03e5\3\2\2\2\u03e3\u03e1\3\2\2\2\u03e4\u03d8\3\2\2\2\u03e4" +
                    "\u03e5\3\2\2\2\u03e5\u03e6\3\2\2\2\u03e6\u03ec\5\u00acW\2\u03e7\u03e8" +
                    "\5\u00aeX\2\u03e8\u03e9\5\u00acW\2\u03e9\u03eb\3\2\2\2\u03ea\u03e7\3\2" +
                    "\2\2\u03eb\u03ee\3\2\2\2\u03ec\u03ea\3\2\2\2\u03ec\u03ed\3\2\2\2\u03ed" +
                    "\u03f9\3\2\2\2\u03ee\u03ec\3\2\2\2\u03ef\u03f0\7\u0096\2\2\u03f0\u03f1" +
                    "\7J\2\2\u03f1\u03f6\5\u009aN\2\u03f2\u03f3\7%\2\2\u03f3\u03f5\5\u009a" +
                    "N\2\u03f4\u03f2\3\2\2\2\u03f5\u03f8\3\2\2\2\u03f6\u03f4\3\2\2\2\u03f6" +
                    "\u03f7\3\2\2\2\u03f7\u03fa\3\2\2\2\u03f8\u03f6\3\2\2\2\u03f9\u03ef\3\2" +
                    "\2\2\u03f9\u03fa\3\2\2\2\u03fa\u0401\3\2\2\2\u03fb\u03fc\7\u0089\2\2\u03fc" +
                    "\u03ff\5\u0080A\2\u03fd\u03fe\t\4\2\2\u03fe\u0400\5\u0080A\2\u03ff\u03fd" +
                    "\3\2\2\2\u03ff\u0400\3\2\2\2\u0400\u0402\3\2\2\2\u0401\u03fb\3\2\2\2\u0401" +
                    "\u0402\3\2\2\2\u0402S\3\2\2\2\u0403\u0405\5\u0096L\2\u0404\u0403\3\2\2" +
                    "\2\u0404\u0405\3\2\2\2\u0405\u0417\3\2\2\2\u0406\u0418\7\177\2\2\u0407" +
                    "\u0418\7\u00a5\2\2\u0408\u0409\7\177\2\2\u0409\u040a\7\u0095\2\2\u040a" +
                    "\u0418\7\u00a5\2\2\u040b\u040c\7\177\2\2\u040c\u040d\7\u0095\2\2\u040d" +
                    "\u0418\7\u00a9\2\2\u040e\u040f\7\177\2\2\u040f\u0410\7\u0095\2\2\u0410" +
                    "\u0418\79\2\2\u0411\u0412\7\177\2\2\u0412\u0413\7\u0095\2\2\u0413\u0418" +
                    "\7o\2\2\u0414\u0415\7\177\2\2\u0415\u0416\7\u0095\2\2\u0416\u0418\7x\2" +
                    "\2\u0417\u0406\3\2\2\2\u0417\u0407\3\2\2\2\u0417\u0408\3\2\2\2\u0417\u040b" +
                    "\3\2\2\2\u0417\u040e\3\2\2\2\u0417\u0411\3\2\2\2\u0417\u0414\3\2\2\2\u0418" +
                    "\u0419\3\2\2\2\u0419\u041d\7\u0082\2\2\u041a\u041b\5\u00c6d\2\u041b\u041c" +
                    "\7\"\2\2\u041c\u041e\3\2\2\2\u041d\u041a\3\2\2\2\u041d\u041e\3\2\2\2\u041e" +
                    "\u041f\3\2\2\2\u041f\u042b\5\u00ccg\2\u0420\u0421\7#\2\2\u0421\u0426\5" +
                    "\u00d4k\2\u0422\u0423\7%\2\2\u0423\u0425\5\u00d4k\2\u0424\u0422\3\2\2" +
                    "\2\u0425\u0428\3\2\2\2\u0426\u0424\3\2\2\2\u0426\u0427\3\2\2\2\u0427\u0429" +
                    "\3\2\2\2\u0428\u0426\3\2\2\2\u0429\u042a\7$\2\2\u042a\u042c\3\2\2\2\u042b" +
                    "\u0420\3\2\2\2\u042b\u042c\3\2\2\2\u042c\u044c\3\2\2\2\u042d\u042e\7\u00bb" +
                    "\2\2\u042e\u042f\7#\2\2\u042f\u0434\5\u0080A\2\u0430\u0431\7%\2\2\u0431" +
                    "\u0433\5\u0080A\2\u0432\u0430\3\2\2\2\u0433\u0436\3\2\2\2\u0434\u0432" +
                    "\3\2\2\2\u0434\u0435\3\2\2\2\u0435\u0437\3\2\2\2\u0436\u0434\3\2\2\2\u0437" +
                    "\u0446\7$\2\2\u0438\u0439\7%\2\2\u0439\u043a\7#\2\2\u043a\u043f\5\u0080" +
                    "A\2\u043b\u043c\7%\2\2\u043c\u043e\5\u0080A\2\u043d\u043b\3\2\2\2\u043e" +
                    "\u0441\3\2\2\2\u043f\u043d\3\2\2\2\u043f\u0440\3\2\2\2\u0440\u0442\3\2" +
                    "\2\2\u0441\u043f\3\2\2\2\u0442\u0443\7$\2\2\u0443\u0445\3\2\2\2\u0444" +
                    "\u0438\3\2\2\2\u0445\u0448\3\2\2\2\u0446\u0444\3\2\2\2\u0446\u0447\3\2" +
                    "\2\2\u0447\u044d\3\2\2\2\u0448\u0446\3\2\2\2\u0449\u044d\5b\62\2\u044a" +
                    "\u044b\7]\2\2\u044b\u044d\7\u00bb\2\2\u044c\u042d\3\2\2\2\u044c\u0449" +
                    "\3\2\2\2\u044c\u044a\3\2\2\2\u044dU\3\2\2\2\u044e\u0452\7\u009a\2\2\u044f" +
                    "\u0450\5\u00c6d\2\u0450\u0451\7\"\2\2\u0451\u0453\3\2\2\2\u0452\u044f" +
                    "\3\2\2\2\u0452\u0453\3\2\2\2\u0453\u0454\3\2\2\2\u0454\u045b\5\u00e2r" +
                    "\2\u0455\u0456\7&\2\2\u0456\u045c\5\u009eP\2\u0457\u0458\7#\2\2\u0458" +
                    "\u0459\5\u009eP\2\u0459\u045a\7$\2\2\u045a\u045c\3\2\2\2\u045b\u0455\3" +
                    "\2\2\2\u045b\u0457\3\2\2\2\u045b\u045c\3\2\2\2\u045cW\3\2\2\2\u045d\u0468" +
                    "\7\u00a2\2\2\u045e\u0469\5\u00d6l\2\u045f\u0460\5\u00c6d\2\u0460\u0461" +
                    "\7\"\2\2\u0461\u0463\3\2\2\2\u0462\u045f\3\2\2\2\u0462\u0463\3\2\2\2\u0463" +
                    "\u0466\3\2\2\2\u0464\u0467\5\u00ccg\2\u0465\u0467\5\u00dan\2\u0466\u0464" +
                    "\3\2\2\2\u0466\u0465\3\2\2\2\u0467\u0469\3\2\2\2\u0468\u045e\3\2\2\2\u0468" +
                    "\u0462\3\2\2\2\u0468\u0469\3\2\2\2\u0469Y\3\2\2\2\u046a\u046c\7\u00a3" +
                    "\2\2\u046b\u046d\7\u00ab\2\2\u046c\u046b\3\2\2\2\u046c\u046d\3\2\2\2\u046d" +
                    "\u046e\3\2\2\2\u046e\u046f\5\u00e4s\2\u046f[\3\2\2\2\u0470\u0475\7\u00a9" +
                    "\2\2\u0471\u0473\7\u00b3\2\2\u0472\u0474\5\u00e8u\2\u0473\u0472\3\2\2" +
                    "\2\u0473\u0474\3\2\2\2\u0474\u0476\3\2\2\2\u0475\u0471\3\2\2\2\u0475\u0476" +
                    "\3\2\2\2\u0476\u047c\3\2\2\2\u0477\u0479\7\u00b2\2\2\u0478\u047a\7\u00ab" +
                    "\2\2\u0479\u0478\3\2\2\2\u0479\u047a\3\2\2\2\u047a\u047b\3\2\2\2\u047b" +
                    "\u047d\5\u00e4s\2\u047c\u0477\3\2\2\2\u047c\u047d\3\2\2\2\u047d]\3\2\2" +
                    "\2\u047e\u047f\7\u00ab\2\2\u047f\u0480\5\u00e4s\2\u0480_\3\2\2\2\u0481" +
                    "\u0483\7\u00c1\2\2\u0482\u0484\7\u009f\2\2\u0483\u0482\3\2\2\2\u0483\u0484" +
                    "\3\2\2\2\u0484\u0485\3\2\2\2\u0485\u048a\5\u00a0Q\2\u0486\u0487\7%\2\2" +
                    "\u0487\u0489\5\u00a0Q\2\u0488\u0486\3\2\2\2\u0489\u048c\3\2\2\2\u048a" +
                    "\u0488\3\2\2\2\u048a\u048b\3\2\2\2\u048b\u048e\3\2\2\2\u048c\u048a\3\2" +
                    "\2\2\u048d\u0481\3\2\2\2\u048d\u048e\3\2\2\2\u048e\u048f\3\2\2\2\u048f" +
                    "\u049a\5\u00acW\2\u0490\u0491\7\u0096\2\2\u0491\u0492\7J\2\2\u0492\u0497" +
                    "\5\u009aN\2\u0493\u0494\7%\2\2\u0494\u0496\5\u009aN\2\u0495\u0493\3\2" +
                    "\2\2\u0496\u0499\3\2\2\2\u0497\u0495\3\2\2\2\u0497\u0498\3\2\2\2\u0498" +
                    "\u049b\3\2\2\2\u0499\u0497\3\2\2\2\u049a\u0490\3\2\2\2\u049a\u049b\3\2" +
                    "\2\2\u049b\u04a2\3\2\2\2\u049c\u049d\7\u0089\2\2\u049d\u04a0\5\u0080A" +
                    "\2\u049e\u049f\t\4\2\2\u049f\u04a1\5\u0080A\2\u04a0\u049e\3\2\2\2\u04a0" +
                    "\u04a1\3\2\2\2\u04a1\u04a3\3\2\2\2\u04a2\u049c\3\2\2\2\u04a2\u04a3\3\2" +
                    "\2\2\u04a3a\3\2\2\2\u04a4\u04a6\7\u00c1\2\2\u04a5\u04a7\7\u009f\2\2\u04a6" +
                    "\u04a5\3\2\2\2\u04a6\u04a7\3\2\2\2\u04a7\u04a8\3\2\2\2\u04a8\u04ad\5\u00a0" +
                    "Q\2\u04a9\u04aa\7%\2\2\u04aa\u04ac\5\u00a0Q\2\u04ab\u04a9\3\2\2\2\u04ac" +
                    "\u04af\3\2\2\2\u04ad\u04ab\3\2\2\2\u04ad\u04ae\3\2\2\2\u04ae\u04b1\3\2" +
                    "\2\2\u04af\u04ad\3\2\2\2\u04b0\u04a4\3\2\2\2\u04b0\u04b1\3\2\2\2\u04b1" +
                    "\u04b2\3\2\2\2\u04b2\u04b8\5d\63\2\u04b3\u04b4\5\u00aeX\2\u04b4\u04b5" +
                    "\5d\63\2\u04b5\u04b7\3\2\2\2\u04b6\u04b3\3\2\2\2\u04b7\u04ba\3\2\2\2\u04b8" +
                    "\u04b6\3\2\2\2\u04b8\u04b9\3\2\2\2\u04b9\u04c5\3\2\2\2\u04ba\u04b8\3\2" +
                    "\2\2\u04bb\u04bc\7\u0096\2\2\u04bc\u04bd\7J\2\2\u04bd\u04c2\5\u009aN\2" +
                    "\u04be\u04bf\7%\2\2\u04bf\u04c1\5\u009aN\2\u04c0\u04be\3\2\2\2\u04c1\u04c4" +
                    "\3\2\2\2\u04c2\u04c0\3\2\2\2\u04c2\u04c3\3\2\2\2\u04c3\u04c6\3\2\2\2\u04c4" +
                    "\u04c2\3\2\2\2\u04c5\u04bb\3\2\2\2\u04c5\u04c6\3\2\2\2\u04c6\u04cd\3\2" +
                    "\2\2\u04c7\u04c8\7\u0089\2\2\u04c8\u04cb\5\u0080A\2\u04c9\u04ca\t\4\2" +
                    "\2\u04ca\u04cc\5\u0080A\2\u04cb\u04c9\3\2\2\2\u04cb\u04cc\3\2\2\2\u04cc" +
                    "\u04ce\3\2\2\2\u04cd\u04c7\3\2\2\2\u04cd\u04ce\3\2\2\2\u04cec\3\2\2\2" +
                    "\u04cf\u04d1\7\u00ac\2\2\u04d0\u04d2\t\f\2\2\u04d1\u04d0\3\2\2\2\u04d1" +
                    "\u04d2\3\2\2\2\u04d2\u04d3\3\2\2\2\u04d3\u04d8\5\u00a2R\2\u04d4\u04d5" +
                    "\7%\2\2\u04d5\u04d7\5\u00a2R\2\u04d6\u04d4\3\2\2\2\u04d7\u04da\3\2\2\2" +
                    "\u04d8\u04d6\3\2\2\2\u04d8\u04d9\3\2\2\2\u04d9\u04e7\3\2\2\2\u04da\u04d8" +
                    "\3\2\2\2\u04db\u04e5\7r\2\2\u04dc\u04e1\5\u00a4S\2\u04dd\u04de\7%\2\2" +
                    "\u04de\u04e0\5\u00a4S\2\u04df\u04dd\3\2\2\2\u04e0\u04e3\3\2\2\2\u04e1" +
                    "\u04df\3\2\2\2\u04e1\u04e2\3\2\2\2\u04e2\u04e6\3\2\2\2\u04e3\u04e1\3\2" +
                    "\2\2\u04e4\u04e6\5\u00a6T\2\u04e5\u04dc\3\2\2\2\u04e5\u04e4\3\2\2\2\u04e6" +
                    "\u04e8\3\2\2\2\u04e7\u04db\3\2\2\2\u04e7\u04e8\3\2\2\2\u04e8\u04eb\3\2" +
                    "\2\2\u04e9\u04ea\7\u00c0\2\2\u04ea\u04ec\5\u0080A\2\u04eb\u04e9\3\2\2" +
                    "\2\u04eb\u04ec\3\2\2\2\u04ec\u04fb\3\2\2\2\u04ed\u04ee\7u\2\2\u04ee\u04ef" +
                    "\7J\2\2\u04ef\u04f4\5\u0080A\2\u04f0\u04f1\7%\2\2\u04f1\u04f3\5\u0080" +
                    "A\2\u04f2\u04f0\3\2\2\2\u04f3\u04f6\3\2\2\2\u04f4\u04f2\3\2\2\2\u04f4" +
                    "\u04f5\3\2\2\2\u04f5\u04f9\3\2\2\2\u04f6\u04f4\3\2\2\2\u04f7\u04f8\7v" +
                    "\2\2\u04f8\u04fa\5\u0080A\2\u04f9\u04f7\3\2\2\2\u04f9\u04fa\3\2\2\2\u04fa" +
                    "\u04fc\3\2\2\2\u04fb\u04ed\3\2\2\2\u04fb\u04fc\3\2\2\2\u04fc\u051a\3\2" +
                    "\2\2\u04fd\u04fe\7\u00bb\2\2\u04fe\u04ff\7#\2\2\u04ff\u0504\5\u0080A\2" +
                    "\u0500\u0501\7%\2\2\u0501\u0503\5\u0080A\2\u0502\u0500\3\2\2\2\u0503\u0506" +
                    "\3\2\2\2\u0504\u0502\3\2\2\2\u0504\u0505\3\2\2\2\u0505\u0507\3\2\2\2\u0506" +
                    "\u0504\3\2\2\2\u0507\u0516\7$\2\2\u0508\u0509\7%\2\2\u0509\u050a\7#\2" +
                    "\2\u050a\u050f\5\u0080A\2\u050b\u050c\7%\2\2\u050c\u050e\5\u0080A\2\u050d" +
                    "\u050b\3\2\2\2\u050e\u0511\3\2\2\2\u050f\u050d\3\2\2\2\u050f\u0510\3\2" +
                    "\2\2\u0510\u0512\3\2\2\2\u0511\u050f\3\2\2\2\u0512\u0513\7$\2\2\u0513" +
                    "\u0515\3\2\2\2\u0514\u0508\3\2\2\2\u0515\u0518\3\2\2\2\u0516\u0514\3\2" +
                    "\2\2\u0516\u0517\3\2\2\2\u0517\u051a\3\2\2\2\u0518\u0516\3\2\2\2\u0519" +
                    "\u04cf\3\2\2\2\u0519\u04fd\3\2\2\2\u051ae\3\2\2\2\u051b\u051d\5\u0096" +
                    "L\2\u051c\u051b\3\2\2\2\u051c\u051d\3\2\2\2\u051d\u051e\3\2\2\2\u051e" +
                    "\u0529\7\u00b8\2\2\u051f\u0520\7\u0095\2\2\u0520\u052a\7\u00a9\2\2\u0521" +
                    "\u0522\7\u0095\2\2\u0522\u052a\79\2\2\u0523\u0524\7\u0095\2\2\u0524\u052a" +
                    "\7\u00a5\2\2\u0525\u0526\7\u0095\2\2\u0526\u052a\7o\2\2\u0527\u0528\7" +
                    "\u0095\2\2\u0528\u052a\7x\2\2\u0529\u051f\3\2\2\2\u0529\u0521\3\2\2\2" +
                    "\u0529\u0523\3\2\2\2\u0529\u0525\3\2\2\2\u0529\u0527\3\2\2\2\u0529\u052a" +
                    "\3\2\2\2\u052a\u052b\3\2\2\2\u052b\u052c\5\u0098M\2\u052c\u052d\7\u00ad" +
                    "\2\2\u052d\u052e\5\u00d4k\2\u052e\u052f\7&\2\2\u052f\u0537\5\u0080A\2" +
                    "\u0530\u0531\7%\2\2\u0531\u0532\5\u00d4k\2\u0532\u0533\7&\2\2\u0533\u0534" +
                    "\5\u0080A\2\u0534\u0536\3\2\2\2\u0535\u0530\3\2\2\2\u0536\u0539\3\2\2" +
                    "\2\u0537\u0535\3\2\2\2\u0537\u0538\3\2\2\2\u0538\u053c\3\2\2\2\u0539\u0537" +
                    "\3\2\2\2\u053a\u053b\7\u00c0\2\2\u053b\u053d\5\u0080A\2\u053c\u053a\3" +
                    "\2\2\2\u053c\u053d\3\2\2\2\u053dg\3\2\2\2\u053e\u0540\5\u0096L\2\u053f" +
                    "\u053e\3\2\2\2\u053f\u0540\3\2\2\2\u0540\u0541\3\2\2\2\u0541\u054c\7\u00b8" +
                    "\2\2\u0542\u0543\7\u0095\2\2\u0543\u054d\7\u00a9\2\2\u0544\u0545\7\u0095" +
                    "\2\2\u0545\u054d\79\2\2\u0546\u0547\7\u0095\2\2\u0547\u054d\7\u00a5\2" +
                    "\2\u0548\u0549\7\u0095\2\2\u0549\u054d\7o\2\2\u054a\u054b\7\u0095\2\2" +
                    "\u054b\u054d\7x\2\2\u054c\u0542\3\2\2\2\u054c\u0544\3\2\2\2\u054c\u0546" +
                    "\3\2\2\2\u054c\u0548\3\2\2\2\u054c\u054a\3\2\2\2\u054c\u054d\3\2\2\2\u054d" +
                    "\u054e\3\2\2\2\u054e\u054f\5\u0098M\2\u054f\u0550\7\u00ad\2\2\u0550\u0551" +
                    "\5\u00d4k\2\u0551\u0552\7&\2\2\u0552\u055a\5\u0080A\2\u0553\u0554\7%\2" +
                    "\2\u0554\u0555\5\u00d4k\2\u0555\u0556\7&\2\2\u0556\u0557\5\u0080A\2\u0557" +
                    "\u0559\3\2\2\2\u0558\u0553\3\2\2\2\u0559\u055c\3\2\2\2\u055a\u0558\3\2" +
                    "\2\2\u055a\u055b\3\2\2\2\u055b\u055f\3\2\2\2\u055c\u055a\3\2\2\2\u055d" +
                    "\u055e\7\u00c0\2\2\u055e\u0560\5\u0080A\2\u055f\u055d\3\2\2\2\u055f\u0560" +
                    "\3\2\2\2\u0560\u0573\3\2\2\2\u0561\u0562\7\u0096\2\2\u0562\u0563\7J\2" +
                    "\2\u0563\u0568\5\u009aN\2\u0564\u0565\7%\2\2\u0565\u0567\5\u009aN\2\u0566" +
                    "\u0564\3\2\2\2\u0567\u056a\3\2\2\2\u0568\u0566\3\2\2\2\u0568\u0569\3\2" +
                    "\2\2\u0569\u056c\3\2\2\2\u056a\u0568\3\2\2\2\u056b\u0561\3\2\2\2\u056b" +
                    "\u056c\3\2\2\2\u056c\u056d\3\2\2\2\u056d\u056e\7\u0089\2\2\u056e\u0571" +
                    "\5\u0080A\2\u056f\u0570\t\4\2\2\u0570\u0572\5\u0080A\2\u0571\u056f\3\2" +
                    "\2\2\u0571\u0572\3\2\2\2\u0572\u0574\3\2\2\2\u0573\u056b\3\2\2\2\u0573" +
                    "\u0574\3\2\2\2\u0574i\3\2\2\2\u0575\u0576\7\u00ba\2\2\u0576k\3\2\2\2\u0577" +
                    "\u057c\5\u00d4k\2\u0578\u057b\5p9\2\u0579\u057b\5n8\2\u057a\u0578\3\2" +
                    "\2\2\u057a\u0579\3\2\2\2\u057b\u057e\3\2\2\2\u057c\u057a\3\2\2\2\u057c" +
                    "\u057d\3\2\2\2\u057dm\3\2\2\2\u057e\u057c\3\2\2\2\u057f\u0593\5\u00c2" +
                    "b\2\u0580\u0581\7#\2\2\u0581\u0583\5\u00b2Z\2\u0582\u0584\5\u00ecw\2\u0583" +
                    "\u0582\3\2\2\2\u0583\u0584\3\2\2\2\u0584\u0585\3\2\2\2\u0585\u0586\7$" +
                    "\2\2\u0586\u0594\3\2\2\2\u0587\u0588\7#\2\2\u0588\u058a\5\u00b2Z\2\u0589" +
                    "\u058b\5\u00ecw\2\u058a\u0589\3\2\2\2\u058a\u058b\3\2\2\2\u058b\u058c" +
                    "\3\2\2\2\u058c\u058d\7%\2\2\u058d\u058f\5\u00b2Z\2\u058e\u0590\5\u00ec" +
                    "w\2\u058f\u058e\3\2\2\2\u058f\u0590\3\2\2\2\u0590\u0591\3\2\2\2\u0591" +
                    "\u0592\7$\2\2\u0592\u0594\3\2\2\2\u0593\u0580\3\2\2\2\u0593\u0587\3\2" +
                    "\2\2\u0593\u0594\3\2\2\2\u0594o\3\2\2\2\u0595\u0596\7T\2\2\u0596\u0598" +
                    "\5\u00c2b\2\u0597\u0595\3\2\2\2\u0597\u0598\3\2\2\2\u0598\u05a7\3\2\2" +
                    "\2\u0599\u05a8\5r:\2\u059a\u05a8\5t;\2\u059b\u05a8\5v<\2\u059c\u05a8\5" +
                    "x=\2\u059d\u059e\7\u00b7\2\2\u059e\u05a8\5~@\2\u059f\u05a0\7O\2\2\u05a0" +
                    "\u05a1\7#\2\2\u05a1\u05a2\5\u0080A\2\u05a2\u05a3\7$\2\2\u05a3\u05a8\3" +
                    "\2\2\2\u05a4\u05a8\5z>\2\u05a5\u05a6\7P\2\2\u05a6\u05a8\5\u00d6l\2\u05a7" +
                    "\u0599\3\2\2\2\u05a7\u059a\3\2\2\2\u05a7\u059b\3\2\2\2\u05a7\u059c\3\2" +
                    "\2\2\u05a7\u059d\3\2\2\2\u05a7\u059f\3\2\2\2\u05a7\u05a4\3\2\2\2\u05a7" +
                    "\u05a5\3\2\2\2\u05a8q\3\2\2\2\u05a9\u05aa\7\u009c\2\2\u05aa\u05ac\7\u0086" +
                    "\2\2\u05ab\u05ad\t\r\2\2\u05ac\u05ab\3\2\2\2\u05ac\u05ad\3\2\2\2\u05ad" +
                    "\u05ae\3\2\2\2\u05ae\u05b0\5~@\2\u05af\u05b1\7E\2\2\u05b0\u05af\3\2\2" +
                    "\2\u05b0\u05b1\3\2\2\2\u05b1s\3\2\2\2\u05b2\u05b3\5\u0082B\2\u05b3u\3" +
                    "\2\2\2\u05b4\u05b5\7\u008e\2\2\u05b5\u05b6\7\u0090\2\2\u05b6\u05b7\5~" +
                    "@\2\u05b7w\3\2\2\2\u05b8\u05b9\7\u0090\2\2\u05b9\u05ba\5~@\2\u05bay\3" +
                    "\2\2\2\u05bb\u05c7\7]\2\2\u05bc\u05c8\5|?\2\u05bd\u05be\7#\2\2\u05be\u05bf" +
                    "\5\u0080A\2\u05bf\u05c0\7$\2\2\u05c0\u05c8\3\2\2\2\u05c1\u05c2\7\u008c" +
                    "\2\2\u05c2\u05c3\7#\2\2\u05c3\u05c4\5\u0080A\2\u05c4\u05c5\7$\2\2\u05c5" +
                    "\u05c8\3\2\2\2\u05c6\u05c8\5\u00ecw\2\u05c7\u05bc\3\2\2\2\u05c7\u05bd" +
                    "\3\2\2\2\u05c7\u05c1\3\2\2\2\u05c7\u05c6\3\2\2\2\u05c8\u05cf\3\2\2\2\u05c9" +
                    "\u05cb\7\6\2\2\u05ca\u05cc\5\u00ecw\2\u05cb\u05ca\3\2\2\2\u05cc\u05cd" +
                    "\3\2\2\2\u05cd\u05cb\3\2\2\2\u05cd\u05ce\3\2\2\2\u05ce\u05d0\3\2\2\2\u05cf" +
                    "\u05c9\3\2\2\2\u05cf\u05d0\3\2\2\2\u05d0{\3\2\2\2\u05d1\u05d4\5\u00b2" +
                    "Z\2\u05d2\u05d4\5\u00b4[\2\u05d3\u05d1\3\2\2\2\u05d3\u05d2\3\2\2\2\u05d4" +
                    "}\3\2\2\2\u05d5\u05d6\7\u0093\2\2\u05d6\u05d7\7S\2\2\u05d7\u05d9\t\16" +
                    "\2\2\u05d8\u05d5\3\2\2\2\u05d8\u05d9\3\2\2\2\u05d9\177\3\2\2\2\u05da\u05db" +
                    "\bA\1\2\u05db\u0627\5\u00b4[\2\u05dc\u0627\7\u00c5\2\2\u05dd\u05de\5\u00c6" +
                    "d\2\u05de\u05df\7\"\2\2\u05df\u05e1\3\2\2\2\u05e0\u05dd\3\2\2\2\u05e0" +
                    "\u05e1\3\2\2\2\u05e1\u05e2\3\2\2\2\u05e2\u05e3\5\u00ccg\2\u05e3\u05e4" +
                    "\7\"\2\2\u05e4\u05e6\3\2\2\2\u05e5\u05e0\3\2\2\2\u05e5\u05e6\3\2\2\2\u05e6" +
                    "\u05e7\3\2\2\2\u05e7\u0627\5\u00d4k\2\u05e8\u05e9\5\u00b6\\\2\u05e9\u05ea" +
                    "\5\u0080A\27\u05ea\u0627\3\2\2\2\u05eb\u05ec\5\u00c4c\2\u05ec\u05f9\7" +
                    "#\2\2\u05ed\u05ef\7c\2\2\u05ee\u05ed\3\2\2\2\u05ee\u05ef\3\2\2\2\u05ef" +
                    "\u05f0\3\2\2\2\u05f0\u05f5\5\u0080A\2\u05f1\u05f2\7%\2\2\u05f2\u05f4\5" +
                    "\u0080A\2\u05f3\u05f1\3\2\2\2\u05f4\u05f7\3\2\2\2\u05f5\u05f3\3\2\2\2" +
                    "\u05f5\u05f6\3\2\2\2\u05f6\u05fa\3\2\2\2\u05f7\u05f5\3\2\2\2\u05f8\u05fa" +
                    "\7\'\2\2\u05f9\u05ee\3\2\2\2\u05f9\u05f8\3\2\2\2\u05f9\u05fa\3\2\2\2\u05fa" +
                    "\u05fb\3\2\2\2\u05fb\u05fc\7$\2\2\u05fc\u0627\3\2\2\2\u05fd\u05fe\7#\2" +
                    "\2\u05fe\u05ff\5\u0080A\2\u05ff\u0600\7$\2\2\u0600\u0627\3\2\2\2\u0601" +
                    "\u0602\7N\2\2\u0602\u0603\7#\2\2\u0603\u0604\5\u0080A\2\u0604\u0605\7" +
                    "A\2\2\u0605\u0606\5n8\2\u0606\u0607\7$\2\2\u0607\u0627\3\2\2\2\u0608\u060a" +
                    "\7\u008e\2\2\u0609\u0608\3\2\2\2\u0609\u060a\3\2\2\2\u060a\u060b\3\2\2" +
                    "\2\u060b\u060d\7m\2\2\u060c\u0609\3\2\2\2\u060c\u060d\3\2\2\2\u060d\u060e" +
                    "\3\2\2\2\u060e\u060f\7#\2\2\u060f\u0610\5b\62\2\u0610\u0611\7$\2\2\u0611" +
                    "\u0627\3\2\2\2\u0612\u0614\7M\2\2\u0613\u0615\5\u0080A\2\u0614\u0613\3" +
                    "\2\2\2\u0614\u0615\3\2\2\2\u0615\u061b\3\2\2\2\u0616\u0617\7\u00bf\2\2" +
                    "\u0617\u0618\5\u0080A\2\u0618\u0619\7\u00b1\2\2\u0619\u061a\5\u0080A\2" +
                    "\u061a\u061c\3\2\2\2\u061b\u0616\3\2\2\2\u061c\u061d\3\2\2\2\u061d\u061b" +
                    "\3\2\2\2\u061d\u061e\3\2\2\2\u061e\u0621\3\2\2\2\u061f\u0620\7f\2\2\u0620" +
                    "\u0622\5\u0080A\2\u0621\u061f\3\2\2\2\u0621\u0622\3\2\2\2\u0622\u0623" +
                    "\3\2\2\2\u0623\u0624\7g\2\2\u0624\u0627\3\2\2\2\u0625\u0627\5\u0086D\2" +
                    "\u0626\u05da\3\2\2\2\u0626\u05dc\3\2\2\2\u0626\u05e5\3\2\2\2\u0626\u05e8" +
                    "\3\2\2\2\u0626\u05eb\3\2\2\2\u0626\u05fd\3\2\2\2\u0626\u0601\3\2\2\2\u0626" +
                    "\u060c\3\2\2\2\u0626\u0612\3\2\2\2\u0626\u0625\3\2\2\2\u0627\u068c\3\2" +
                    "\2\2\u0628\u0629\f\26\2\2\u0629\u062a\7+\2\2\u062a\u068b\5\u0080A\27\u062b" +
                    "\u062c\f\25\2\2\u062c\u062d\t\17\2\2\u062d\u068b\5\u0080A\26\u062e\u062f" +
                    "\f\24\2\2\u062f\u0630\t\20\2\2\u0630\u068b\5\u0080A\25\u0631\u0632\f\23" +
                    "\2\2\u0632\u0633\t\21\2\2\u0633\u068b\5\u0080A\24\u0634\u0635\f\22\2\2" +
                    "\u0635\u0636\t\22\2\2\u0636\u068b\5\u0080A\23\u0637\u0644\f\21\2\2\u0638" +
                    "\u0645\7&\2\2\u0639\u0645\7\66\2\2\u063a\u0645\7\67\2\2\u063b\u0645\7" +
                    "8\2\2\u063c\u0645\7\u0083\2\2\u063d\u063e\7\u0083\2\2\u063e\u0645\7\u008e" +
                    "\2\2\u063f\u0645\7z\2\2\u0640\u0645\7\u0088\2\2\u0641\u0645\7t\2\2\u0642" +
                    "\u0645\7\u008a\2\2\u0643\u0645\7\u00a1\2\2\u0644\u0638\3\2\2\2\u0644\u0639" +
                    "\3\2\2\2\u0644\u063a\3\2\2\2\u0644\u063b\3\2\2\2\u0644\u063c\3\2\2\2\u0644" +
                    "\u063d\3\2\2\2\u0644\u063f\3\2\2\2\u0644\u0640\3\2\2\2\u0644\u0641\3\2" +
                    "\2\2\u0644\u0642\3\2\2\2\u0644\u0643\3\2\2\2\u0645\u0646\3\2\2\2\u0646" +
                    "\u068b\5\u0080A\22\u0647\u0648\f\20\2\2\u0648\u0649\7@\2\2\u0649\u068b" +
                    "\5\u0080A\21\u064a\u064b\f\17\2\2\u064b\u064c\7\u0095\2\2\u064c\u068b" +
                    "\5\u0080A\20\u064d\u064e\f\b\2\2\u064e\u0650\7\u0083\2\2\u064f\u0651\7" +
                    "\u008e\2\2\u0650\u064f\3\2\2\2\u0650\u0651\3\2\2\2\u0651\u0652\3\2\2\2" +
                    "\u0652\u068b\5\u0080A\t\u0653\u0655\f\7\2\2\u0654\u0656\7\u008e\2\2\u0655" +
                    "\u0654\3\2\2\2\u0655\u0656\3\2\2\2\u0656\u0657\3\2\2\2\u0657\u0658\7H" +
                    "\2\2\u0658\u0659\5\u0080A\2\u0659\u065a\7@\2\2\u065a\u065b\5\u0080A\b" +
                    "\u065b\u068b\3\2\2\2\u065c\u065d\f\13\2\2\u065d\u065e\7P\2\2\u065e\u068b" +
                    "\5\u00d6l\2\u065f\u0661\f\n\2\2\u0660\u0662\7\u008e\2\2\u0661\u0660\3" +
                    "\2\2\2\u0661\u0662\3\2\2\2\u0662\u0663\3\2\2\2\u0663\u0664\t\23\2\2\u0664" +
                    "\u0667\5\u0080A\2\u0665\u0666\7i\2\2\u0666\u0668\5\u0080A\2\u0667\u0665" +
                    "\3\2\2\2\u0667\u0668\3\2\2\2\u0668\u068b\3\2\2\2\u0669\u066e\f\t\2\2\u066a" +
                    "\u066f\7\u0084\2\2\u066b\u066f\7\u008f\2\2\u066c\u066d\7\u008e\2\2\u066d" +
                    "\u066f\7\u0090\2\2\u066e\u066a\3\2\2\2\u066e\u066b\3\2\2\2\u066e\u066c" +
                    "\3\2\2\2\u066f\u068b\3\2\2\2\u0670\u0672\f\6\2\2\u0671\u0673\7\u008e\2" +
                    "\2\u0672\u0671\3\2\2\2\u0672\u0673\3\2\2\2\u0673\u0674\3\2\2\2\u0674\u0688" +
                    "\7z\2\2\u0675\u067f\7#\2\2\u0676\u0680\5b\62\2\u0677\u067c\5\u0080A\2" +
                    "\u0678\u0679\7%\2\2\u0679\u067b\5\u0080A\2\u067a\u0678\3\2\2\2\u067b\u067e" +
                    "\3\2\2\2\u067c\u067a\3\2\2\2\u067c\u067d\3\2\2\2\u067d\u0680\3\2\2\2\u067e" +
                    "\u067c\3\2\2\2\u067f\u0676\3\2\2\2\u067f\u0677\3\2\2\2\u067f\u0680\3\2" +
                    "\2\2\u0680\u0681\3\2\2\2\u0681\u0689\7$\2\2\u0682\u0683\5\u00c6d\2\u0683" +
                    "\u0684\7\"\2\2\u0684\u0686\3\2\2\2\u0685\u0682\3\2\2\2\u0685\u0686\3\2" +
                    "\2\2\u0686\u0687\3\2\2\2\u0687\u0689\5\u00ccg\2\u0688\u0675\3\2\2\2\u0688" +
                    "\u0685\3\2\2\2\u0689\u068b\3\2\2\2\u068a\u0628\3\2\2\2\u068a\u062b\3\2" +
                    "\2\2\u068a\u062e\3\2\2\2\u068a\u0631\3\2\2\2\u068a\u0634\3\2\2\2\u068a" +
                    "\u0637\3\2\2\2\u068a\u0647\3\2\2\2\u068a\u064a\3\2\2\2\u068a\u064d\3\2" +
                    "\2\2\u068a\u0653\3\2\2\2\u068a\u065c\3\2\2\2\u068a\u065f\3\2\2\2\u068a" +
                    "\u0669\3\2\2\2\u068a\u0670\3\2\2\2\u068b\u068e\3\2\2\2\u068c\u068a\3\2" +
                    "\2\2\u068c\u068d\3\2\2\2\u068d\u0081\3\2\2\2\u068e\u068c\3\2\2\2\u068f" +
                    "\u0693\7\u00a0\2\2\u0690\u0691\5\u00c6d\2\u0691\u0692\7\"\2\2\u0692\u0694" +
                    "\3\2\2\2\u0693\u0690\3\2\2\2\u0693\u0694\3\2\2\2\u0694\u0695\3\2\2\2\u0695" +
                    "\u06a1\5\u00d8m\2\u0696\u0697\7#\2\2\u0697\u069c\5\u0084C\2\u0698\u0699" +
                    "\7%\2\2\u0699\u069b\5\u0084C\2\u069a\u0698\3\2\2\2\u069b\u069e\3\2\2\2" +
                    "\u069c\u069a\3\2\2\2\u069c\u069d\3\2\2\2\u069d\u069f\3\2\2\2\u069e\u069c" +
                    "\3\2\2\2\u069f\u06a0\7$\2\2\u06a0\u06a2\3\2\2\2\u06a1\u0696\3\2\2\2\u06a1" +
                    "\u06a2\3\2\2\2\u06a2\u06b5\3\2\2\2\u06a3\u06a4\7\u0093\2\2\u06a4\u06ad" +
                    "\t\24\2\2\u06a5\u06a6\7\u00ad\2\2\u06a6\u06ae\7\u0090\2\2\u06a7\u06a8" +
                    "\7\u00ad\2\2\u06a8\u06ae\7]\2\2\u06a9\u06ae\7L\2\2\u06aa\u06ae\7\u00a6" +
                    "\2\2\u06ab\u06ac\7\u008d\2\2\u06ac\u06ae\7:\2\2\u06ad\u06a5\3\2\2\2\u06ad" +
                    "\u06a7\3\2\2\2\u06ad\u06a9\3\2\2\2\u06ad\u06aa\3\2\2\2\u06ad\u06ab\3\2" +
                    "\2\2\u06ae\u06b2\3\2\2\2\u06af\u06b0\7\u008a\2\2\u06b0\u06b2\5\u00c2b" +
                    "\2\u06b1\u06a3\3\2\2\2\u06b1\u06af\3\2\2\2\u06b2\u06b4\3\2\2\2\u06b3\u06b1" +
                    "\3\2\2\2\u06b4\u06b7\3\2\2\2\u06b5\u06b3\3\2\2\2\u06b5\u06b6\3\2\2\2\u06b6" +
                    "\u06c5\3\2\2\2\u06b7\u06b5\3\2\2\2\u06b8\u06ba\7\u008e\2\2\u06b9\u06b8" +
                    "\3\2\2\2\u06b9\u06ba\3\2\2\2\u06ba\u06bb\3\2\2\2\u06bb\u06c0\7^\2\2\u06bc" +
                    "\u06bd\7}\2\2\u06bd\u06c1\7_\2\2\u06be\u06bf\7}\2\2\u06bf\u06c1\7y\2\2" +
                    "\u06c0\u06bc\3\2\2\2\u06c0\u06be\3\2\2\2\u06c0\u06c1\3\2\2\2\u06c1\u06c3" +
                    "\3\2\2\2\u06c2\u06c4\7h\2\2\u06c3\u06c2\3\2\2\2\u06c3\u06c4\3\2\2\2\u06c4" +
                    "\u06c6\3\2\2\2\u06c5\u06b9\3\2\2\2\u06c5\u06c6\3\2\2\2\u06c6\u0083\3\2" +
                    "\2\2\u06c7\u06c8\5\u00c2b\2\u06c8\u0085\3\2\2\2\u06c9\u06ca\7\u009e\2" +
                    "\2\u06ca\u06cf\7#\2\2\u06cb\u06d0\7x\2\2\u06cc\u06cd\t\25\2\2\u06cd\u06ce" +
                    "\7%\2\2\u06ce\u06d0\5\u00b8]\2\u06cf\u06cb\3\2\2\2\u06cf\u06cc\3\2\2\2" +
                    "\u06d0\u06d1\3\2\2\2\u06d1\u06d2\7$\2\2\u06d2\u0087\3\2\2\2\u06d3\u06d6" +
                    "\5\u00d4k\2\u06d4\u06d5\7P\2\2\u06d5\u06d7\5\u00d6l\2\u06d6\u06d4\3\2" +
                    "\2\2\u06d6\u06d7\3\2\2\2\u06d7\u06d9\3\2\2\2\u06d8\u06da\t\r\2\2\u06d9" +
                    "\u06d8\3\2\2\2\u06d9\u06da\3\2\2\2\u06da\u0089\3\2\2\2\u06db\u06dc\7T" +
                    "\2\2\u06dc\u06de\5\u00c2b\2\u06dd\u06db\3\2\2\2\u06dd\u06de\3\2\2\2\u06de" +
                    "\u06e8\3\2\2\2\u06df\u06e9\5\u008cG\2\u06e0\u06e9\5\u0092J\2\u06e1\u06e9" +
                    "\5\u0090I\2\u06e2\u06e3\7O\2\2\u06e3\u06e4\7#\2\2\u06e4\u06e5\5\u0080" +
                    "A\2\u06e5\u06e6\7$\2\2\u06e6\u06e9\3\2\2\2\u06e7\u06e9\5\u008eH\2\u06e8" +
                    "\u06df\3\2\2\2\u06e8\u06e0\3\2\2\2\u06e8\u06e1\3\2\2\2\u06e8\u06e2\3\2" +
                    "\2\2\u06e8\u06e7\3\2\2\2\u06e9\u008b\3\2\2\2\u06ea\u06eb\7\u009c\2\2\u06eb" +
                    "\u06ec\7\u0086\2\2\u06ec\u06ed\7#\2\2\u06ed\u06f2\5\u0088E\2\u06ee\u06ef" +
                    "\7%\2\2\u06ef\u06f1\5\u0088E\2\u06f0\u06ee\3\2\2\2\u06f1\u06f4\3\2\2\2" +
                    "\u06f2\u06f0\3\2\2\2\u06f2\u06f3\3\2\2\2\u06f3\u06f5\3\2\2\2\u06f4\u06f2" +
                    "\3\2\2\2\u06f5\u06f6\7$\2\2\u06f6\u06f7\5~@\2\u06f7\u008d\3\2\2\2\u06f8" +
                    "\u06f9\7q\2\2\u06f9\u06fa\7\u0086\2\2\u06fa\u06fb\7#\2\2\u06fb\u0700\5" +
                    "\u0094K\2\u06fc\u06fd\7%\2\2\u06fd\u06ff\5\u0094K\2\u06fe\u06fc\3\2\2" +
                    "\2\u06ff\u0702\3\2\2\2\u0700\u06fe\3\2\2\2\u0700\u0701\3\2\2\2\u0701\u0703" +
                    "\3\2\2\2\u0702\u0700\3\2\2\2\u0703\u0704\7$\2\2\u0704\u0705\5\u0082B\2" +
                    "\u0705\u008f\3\2\2\2\u0706\u0708\7\u00b7\2\2\u0707\u0709\7\u0086\2\2\u0708" +
                    "\u0707\3\2\2\2\u0708\u0709\3\2\2\2\u0709\u070b\3\2\2\2\u070a\u070c\5\u00c2" +
                    "b\2\u070b\u070a\3\2\2\2\u070b\u070c\3\2\2\2\u070c\u070d\3\2\2\2\u070d" +
                    "\u070e\7#\2\2\u070e\u0713\5\u0088E\2\u070f\u0710\7%\2\2\u0710\u0712\5" +
                    "\u0088E\2\u0711\u070f\3\2\2\2\u0712\u0715\3\2\2\2\u0713\u0711\3\2\2\2" +
                    "\u0713\u0714\3\2\2\2\u0714\u0716\3\2\2\2\u0715\u0713\3\2\2\2\u0716\u0717" +
                    "\7$\2\2\u0717\u0718\5~@\2\u0718\u0091\3\2\2\2\u0719\u071b\7\u0086\2\2" +
                    "\u071a\u071c\5\u00c2b\2\u071b\u071a\3\2\2\2\u071b\u071c\3\2\2\2\u071c" +
                    "\u071d\3\2\2\2\u071d\u071e\7#\2\2\u071e\u0723\5\u0088E\2\u071f\u0720\7" +
                    "%\2\2\u0720\u0722\5\u0088E\2\u0721\u071f\3\2\2\2\u0722\u0725\3\2\2\2\u0723" +
                    "\u0721\3\2\2\2\u0723\u0724\3\2\2\2\u0724\u0726\3\2\2\2\u0725\u0723\3\2" +
                    "\2\2\u0726\u0727\7$\2\2\u0727\u0728\5~@\2\u0728\u0093\3\2\2\2\u0729\u072a" +
                    "\5\u00d4k\2\u072a\u0095\3\2\2\2\u072b\u072d\7\u00c1\2\2\u072c\u072e\7" +
                    "\u009f\2\2\u072d\u072c\3\2\2\2\u072d\u072e\3\2\2\2\u072e\u072f\3\2\2\2" +
                    "\u072f\u0730\5\u00b0Y\2\u0730\u0731\7A\2\2\u0731\u0732\7#\2\2\u0732\u0733" +
                    "\5b\62\2\u0733\u073d\7$\2\2\u0734\u0735\7%\2\2\u0735\u0736\5\u00b0Y\2" +
                    "\u0736\u0737\7A\2\2\u0737\u0738\7#\2\2\u0738\u0739\5b\62\2\u0739\u073a" +
                    "\7$\2\2\u073a\u073c\3\2\2\2\u073b\u0734\3\2\2\2\u073c\u073f\3\2\2\2\u073d" +
                    "\u073b\3\2\2\2\u073d\u073e\3\2\2\2\u073e\u0097\3\2\2\2\u073f\u073d\3\2" +
                    "\2\2\u0740\u0741\5\u00c6d\2\u0741\u0742\7\"\2\2\u0742\u0744\3\2\2\2\u0743" +
                    "\u0740\3\2\2\2\u0743\u0744\3\2\2\2\u0744\u0745\3\2\2\2\u0745\u074b\5\u00cc" +
                    "g\2\u0746\u0747\7|\2\2\u0747\u0748\7J\2\2\u0748\u074c\5\u00dan\2\u0749" +
                    "\u074a\7\u008e\2\2\u074a\u074c\7|\2\2\u074b\u0746\3\2\2\2\u074b\u0749" +
                    "\3\2\2\2\u074b\u074c\3\2\2\2\u074c\u0099\3\2\2\2\u074d\u0750\5\u0080A" +
                    "\2\u074e\u074f\7P\2\2\u074f\u0751\5\u00d6l\2\u0750\u074e\3\2\2\2\u0750" +
                    "\u0751\3\2\2\2\u0751\u0753\3\2\2\2\u0752\u0754\t\r\2\2\u0753\u0752\3\2" +
                    "\2\2\u0753\u0754\3\2\2\2\u0754\u009b\3\2\2\2\u0755\u0756\t\r\2\2\u0756" +
                    "\u009d\3\2\2\2\u0757\u075b\5\u00b2Z\2\u0758\u075b\5\u00c2b\2\u0759\u075b" +
                    "\7\u00c6\2\2\u075a\u0757\3\2\2\2\u075a\u0758\3\2\2\2\u075a\u0759\3\2\2" +
                    "\2\u075b\u009f\3\2\2\2\u075c\u0768\5\u00ccg\2\u075d\u075e\7#\2\2\u075e" +
                    "\u0763\5\u00d4k\2\u075f\u0760\7%\2\2\u0760\u0762\5\u00d4k\2\u0761\u075f" +
                    "\3\2\2\2\u0762\u0765\3\2\2\2\u0763\u0761\3\2\2\2\u0763\u0764\3\2\2\2\u0764" +
                    "\u0766\3\2\2\2\u0765\u0763\3\2\2\2\u0766\u0767\7$\2\2\u0767\u0769\3\2" +
                    "\2\2\u0768\u075d\3\2\2\2\u0768\u0769\3\2\2\2\u0769\u076a\3\2\2\2\u076a" +
                    "\u076b\7A\2\2\u076b\u076c\7#\2\2\u076c\u076d\5b\62\2\u076d\u076e\7$\2" +
                    "\2\u076e\u00a1\3\2\2\2\u076f\u077c\7\'\2\2\u0770\u0771\5\u00ccg\2\u0771" +
                    "\u0772\7\"\2\2\u0772\u0773\7\'\2\2\u0773\u077c\3\2\2\2\u0774\u0779\5\u0080" +
                    "A\2\u0775\u0777\7A\2\2\u0776\u0775\3\2\2\2\u0776\u0777\3\2\2\2\u0777\u0778" +
                    "\3\2\2\2\u0778\u077a\5\u00bc_\2\u0779\u0776\3\2\2\2\u0779\u077a\3\2\2" +
                    "\2\u077a\u077c\3\2\2\2\u077b\u076f\3\2\2\2\u077b\u0770\3\2\2\2\u077b\u0774" +
                    "\3\2\2\2\u077c\u00a3\3\2\2\2\u077d\u077e\5\u00c6d\2\u077e\u077f\7\"\2" +
                    "\2\u077f\u0781\3\2\2\2\u0780\u077d\3\2\2\2\u0780\u0781\3\2\2\2\u0781\u0782" +
                    "\3\2\2\2\u0782\u0787\5\u00ccg\2\u0783\u0785\7A\2\2\u0784\u0783\3\2\2\2" +
                    "\u0784\u0785\3\2\2\2\u0785\u0786\3\2\2\2\u0786\u0788\5\u00e6t\2\u0787" +
                    "\u0784\3\2\2\2\u0787\u0788\3\2\2\2\u0788\u078e\3\2\2\2\u0789\u078a\7|" +
                    "\2\2\u078a\u078b\7J\2\2\u078b\u078f\5\u00dan\2\u078c\u078d\7\u008e\2\2" +
                    "\u078d\u078f\7|\2\2\u078e\u0789\3\2\2\2\u078e\u078c\3\2\2\2\u078e\u078f" +
                    "\3\2\2\2\u078f\u07ad\3\2\2\2\u0790\u079a\7#\2\2\u0791\u0796\5\u00a4S\2" +
                    "\u0792\u0793\7%\2\2\u0793\u0795\5\u00a4S\2\u0794\u0792\3\2\2\2\u0795\u0798" +
                    "\3\2\2\2\u0796\u0794\3\2\2\2\u0796\u0797\3\2\2\2\u0797\u079b\3\2\2\2\u0798" +
                    "\u0796\3\2\2\2\u0799\u079b\5\u00a6T\2\u079a\u0791\3\2\2\2\u079a\u0799" +
                    "\3\2\2\2\u079b\u079c\3\2\2\2\u079c\u07a1\7$\2\2\u079d\u079f\7A\2\2\u079e" +
                    "\u079d\3\2\2\2\u079e\u079f\3\2\2\2\u079f\u07a0\3\2\2\2\u07a0\u07a2\5\u00e6" +
                    "t\2\u07a1\u079e\3\2\2\2\u07a1\u07a2\3\2\2\2\u07a2\u07ad\3\2\2\2\u07a3" +
                    "\u07a4\7#\2\2\u07a4\u07a5\5b\62\2\u07a5\u07aa\7$\2\2\u07a6\u07a8\7A\2" +
                    "\2\u07a7\u07a6\3\2\2\2\u07a7\u07a8\3\2\2\2\u07a8\u07a9\3\2\2\2\u07a9\u07ab" +
                    "\5\u00e6t\2\u07aa\u07a7\3\2\2\2\u07aa\u07ab\3\2\2\2\u07ab\u07ad\3\2\2" +
                    "\2\u07ac\u0780\3\2\2\2\u07ac\u0790\3\2\2\2\u07ac\u07a3\3\2\2\2\u07ad\u00a5" +
                    "\3\2\2\2\u07ae\u07b5\5\u00a4S\2\u07af\u07b0\5\u00a8U\2\u07b0\u07b1\5\u00a4" +
                    "S\2\u07b1\u07b2\5\u00aaV\2\u07b2\u07b4\3\2\2\2\u07b3\u07af\3\2\2\2\u07b4" +
                    "\u07b7\3\2\2\2\u07b5\u07b3\3\2\2\2\u07b5\u07b6\3\2\2\2\u07b6\u00a7\3\2" +
                    "\2\2\u07b7\u07b5\3\2\2\2\u07b8\u07c6\7%\2\2\u07b9\u07bb\7\u008b\2\2\u07ba" +
                    "\u07b9\3\2\2\2\u07ba\u07bb\3\2\2\2\u07bb\u07c2\3\2\2\2\u07bc\u07be\7\u0087" +
                    "\2\2\u07bd\u07bf\7\u0097\2\2\u07be\u07bd\3\2\2\2\u07be\u07bf\3\2\2\2\u07bf" +
                    "\u07c3\3\2\2\2\u07c0\u07c3\7~\2\2\u07c1\u07c3\7W\2\2\u07c2\u07bc\3\2\2" +
                    "\2\u07c2\u07c0\3\2\2\2\u07c2\u07c1\3\2\2\2\u07c2\u07c3\3\2\2\2\u07c3\u07c4" +
                    "\3\2\2\2\u07c4\u07c6\7\u0085\2\2\u07c5\u07b8\3\2\2\2\u07c5\u07ba\3\2\2" +
                    "\2\u07c6\u00a9\3\2\2\2\u07c7\u07c8\7\u0093\2\2\u07c8\u07d6\5\u0080A\2" +
                    "\u07c9\u07ca\7\u00b9\2\2\u07ca\u07cb\7#\2\2\u07cb\u07d0\5\u00d4k\2\u07cc" +
                    "\u07cd\7%\2\2\u07cd\u07cf\5\u00d4k\2\u07ce\u07cc\3\2\2\2\u07cf\u07d2\3" +
                    "\2\2\2\u07d0\u07ce\3\2\2\2\u07d0\u07d1\3\2\2\2\u07d1\u07d3\3\2\2\2\u07d2" +
                    "\u07d0\3\2\2\2\u07d3\u07d4\7$\2\2\u07d4\u07d6\3\2\2\2\u07d5\u07c7\3\2" +
                    "\2\2\u07d5\u07c9\3\2\2\2\u07d5\u07d6\3\2\2\2\u07d6\u00ab\3\2\2\2\u07d7" +
                    "\u07d9\7\u00ac\2\2\u07d8\u07da\t\f\2\2\u07d9\u07d8\3\2\2\2\u07d9\u07da" +
                    "\3\2\2\2\u07da\u07db\3\2\2\2\u07db\u07e0\5\u00a2R\2\u07dc\u07dd\7%\2\2" +
                    "\u07dd\u07df\5\u00a2R\2\u07de\u07dc\3\2\2\2\u07df\u07e2\3\2\2\2\u07e0" +
                    "\u07de\3\2\2\2\u07e0\u07e1\3\2\2\2\u07e1\u07ef\3\2\2\2\u07e2\u07e0\3\2" +
                    "\2\2\u07e3\u07ed\7r\2\2\u07e4\u07e9\5\u00a4S\2\u07e5\u07e6\7%\2\2\u07e6" +
                    "\u07e8\5\u00a4S\2\u07e7\u07e5\3\2\2\2\u07e8\u07eb\3\2\2\2\u07e9\u07e7" +
                    "\3\2\2\2\u07e9\u07ea\3\2\2\2\u07ea\u07ee\3\2\2\2\u07eb\u07e9\3\2\2\2\u07ec" +
                    "\u07ee\5\u00a6T\2\u07ed\u07e4\3\2\2\2\u07ed\u07ec\3\2\2\2\u07ee\u07f0" +
                    "\3\2\2\2\u07ef\u07e3\3\2\2\2\u07ef\u07f0\3\2\2\2\u07f0\u07f3\3\2\2\2\u07f1" +
                    "\u07f2\7\u00c0\2\2\u07f2\u07f4\5\u0080A\2\u07f3\u07f1\3\2\2\2\u07f3\u07f4" +
                    "\3\2\2\2\u07f4\u0803\3\2\2\2\u07f5\u07f6\7u\2\2\u07f6\u07f7\7J\2\2\u07f7" +
                    "\u07fc\5\u0080A\2\u07f8\u07f9\7%\2\2\u07f9\u07fb\5\u0080A\2\u07fa\u07f8" +
                    "\3\2\2\2\u07fb\u07fe\3\2\2\2\u07fc\u07fa\3\2\2\2\u07fc\u07fd\3\2\2\2\u07fd" +
                    "\u0801\3\2\2\2\u07fe\u07fc\3\2\2\2\u07ff\u0800\7v\2\2\u0800\u0802\5\u0080" +
                    "A\2\u0801\u07ff\3\2\2\2\u0801\u0802\3\2\2\2\u0802\u0804\3\2\2\2\u0803" +
                    "\u07f5\3\2\2\2\u0803\u0804\3\2\2\2\u0804\u0822\3\2\2\2\u0805\u0806\7\u00bb" +
                    "\2\2\u0806\u0807\7#\2\2\u0807\u080c\5\u0080A\2\u0808\u0809\7%\2\2\u0809" +
                    "\u080b\5\u0080A\2\u080a\u0808\3\2\2\2\u080b\u080e\3\2\2\2\u080c\u080a" +
                    "\3\2\2\2\u080c\u080d\3\2\2\2\u080d\u080f\3\2\2\2\u080e\u080c\3\2\2\2\u080f" +
                    "\u081e\7$\2\2\u0810\u0811\7%\2\2\u0811\u0812\7#\2\2\u0812\u0817\5\u0080" +
                    "A\2\u0813\u0814\7%\2\2\u0814\u0816\5\u0080A\2\u0815\u0813\3\2\2\2\u0816" +
                    "\u0819\3\2\2\2\u0817\u0815\3\2\2\2\u0817\u0818\3\2\2\2\u0818\u081a\3\2" +
                    "\2\2\u0819\u0817\3\2\2\2\u081a\u081b\7$\2\2\u081b\u081d\3\2\2\2\u081c" +
                    "\u0810\3\2\2\2\u081d\u0820\3\2\2\2\u081e\u081c\3\2\2\2\u081e\u081f\3\2" +
                    "\2\2\u081f\u0822\3\2\2\2\u0820\u081e\3\2\2\2\u0821\u07d7\3\2\2\2\u0821" +
                    "\u0805\3\2\2\2\u0822\u00ad\3\2\2\2\u0823\u0829\7\u00b6\2\2\u0824\u0825" +
                    "\7\u00b6\2\2\u0825\u0829\7=\2\2\u0826\u0829\7\u0081\2\2\u0827\u0829\7" +
                    "j\2\2\u0828\u0823\3\2\2\2\u0828\u0824\3\2\2\2\u0828\u0826\3\2\2\2\u0828" +
                    "\u0827\3\2\2\2\u0829\u00af\3\2\2\2\u082a\u0836\5\u00ccg\2\u082b\u082c" +
                    "\7#\2\2\u082c\u0831\5\u00d4k\2\u082d\u082e\7%\2\2\u082e\u0830\5\u00d4" +
                    "k\2\u082f\u082d\3\2\2\2\u0830\u0833\3\2\2\2\u0831\u082f\3\2\2\2\u0831" +
                    "\u0832\3\2\2\2\u0832\u0834\3\2\2\2\u0833\u0831\3\2\2\2\u0834\u0835\7$" +
                    "\2\2\u0835\u0837\3\2\2\2\u0836\u082b\3\2\2\2\u0836\u0837\3\2\2\2\u0837" +
                    "\u00b1\3\2\2\2\u0838\u083a\t\20\2\2\u0839\u0838\3\2\2\2\u0839\u083a\3" +
                    "\2\2\2\u083a\u083b\3\2\2\2\u083b\u083e\7\u00c4\2\2\u083c\u083e\7\'\2\2" +
                    "\u083d\u0839\3\2\2\2\u083d\u083c\3\2\2\2\u083e\u00b3\3\2\2\2\u083f\u0840" +
                    "\t\26\2\2\u0840\u00b5\3\2\2\2\u0841\u0842\t\27\2\2\u0842\u00b7\3\2\2\2" +
                    "\u0843\u0844\7\u00c6\2\2\u0844\u00b9\3\2\2\2\u0845\u0848\5\u0080A\2\u0846" +
                    "\u0848\5l\67\2\u0847\u0845\3\2\2\2\u0847\u0846\3\2\2\2\u0848\u00bb\3\2" +
                    "\2\2\u0849\u084a\t\30\2\2\u084a\u00bd\3\2\2\2\u084b\u084c\t\31\2\2\u084c" +
                    "\u00bf\3\2\2\2\u084d\u084f\13\2\2\2\u084e\u084d\3\2\2\2\u084f\u0850\3" +
                    "\2\2\2\u0850\u084e\3\2\2\2\u0850\u0851\3\2\2\2\u0851\u00c1\3\2\2\2\u0852" +
                    "\u0853\5\u00ecw\2\u0853\u00c3\3\2\2\2\u0854\u0855\5\u00ecw\2\u0855\u00c5" +
                    "\3\2\2\2\u0856\u0857\5\u00ecw\2\u0857\u00c7\3\2\2\2\u0858\u0859\5\u00ec" +
                    "w\2\u0859\u00c9\3\2\2\2\u085a\u085b\5\u00ecw\2\u085b\u00cb\3\2\2\2\u085c" +
                    "\u085d\5\u00ecw\2\u085d\u00cd\3\2\2\2\u085e\u085f\5\u00ecw\2\u085f\u00cf" +
                    "\3\2\2\2\u0860\u0861\5\u00ecw\2\u0861\u00d1\3\2\2\2\u0862\u0863\5\u00ec" +
                    "w\2\u0863\u00d3\3\2\2\2\u0864\u0865\5\u00ecw\2\u0865\u00d5\3\2\2\2\u0866" +
                    "\u0867\5\u00ecw\2\u0867\u00d7\3\2\2\2\u0868\u0869\5\u00ecw\2\u0869\u00d9" +
                    "\3\2\2\2\u086a\u086b\5\u00ecw\2\u086b\u00db\3\2\2\2\u086c\u086d\5\u00ec" +
                    "w\2\u086d\u00dd\3\2\2\2\u086e\u086f\5\u00ecw\2\u086f\u00df\3\2\2\2\u0870" +
                    "\u0871\5\u00ecw\2\u0871\u00e1\3\2\2\2\u0872\u0873\5\u00ecw\2\u0873\u00e3" +
                    "\3\2\2\2\u0874\u0875\5\u00ecw\2\u0875\u00e5\3\2\2\2\u0876\u0877\5\u00ec" +
                    "w\2\u0877\u00e7\3\2\2\2\u0878\u0879\5\u00ecw\2\u0879\u00e9\3\2\2\2\u087a" +
                    "\u087b\5\u00ecw\2\u087b\u00eb\3\2\2\2\u087c\u0884\7\u00c3\2\2\u087d\u0884" +
                    "\5\u00be`\2\u087e\u0884\7\u00c6\2\2\u087f\u0880\7#\2\2\u0880\u0881\5\u00ec" +
                    "w\2\u0881\u0882\7$\2\2\u0882\u0884\3\2\2\2\u0883\u087c\3\2\2\2\u0883\u087d" +
                    "\3\2\2\2\u0883\u087e\3\2\2\2\u0883\u087f\3\2\2\2\u0884\u00ed\3\2\2\2\u0137" +
                    "\u00f0\u00f2\u00fd\u0104\u0109\u010f\u0115\u0117\u0139\u013e\u0143\u014d" +
                    "\u0150\u0153\u0156\u0167\u016b\u0173\u0177\u0179\u017e\u0180\u0184\u018b" +
                    "\u018e\u0193\u0197\u019c\u01a5\u01a8\u01ae\u01b0\u01b4\u01ba\u01bf\u01ca" +
                    "\u01d0\u01d4\u01da\u01df\u01e8\u01ea\u01f0\u01f4\u01f7\u01fb\u0201\u0206" +
                    "\u020d\u0218\u021b\u021d\u0223\u0229\u022d\u0234\u023a\u0240\u0246\u024b" +
                    "\u0257\u025c\u0267\u026c\u0273\u0281\u0286\u028f\u0294\u029a\u02a4\u02aa" +
                    "\u02b3\u02b6\u02ba\u02bf\u02c3\u02c7\u02cf\u02d6\u02db\u02df\u02e5\u02ed" +
                    "\u02f1\u02f6\u02f9\u02fd\u0304\u0308\u030b\u0315\u0319\u031c\u0320\u0326" +
                    "\u0329\u032c\u0332\u0334\u0339\u033d\u0344\u0348\u034a\u0351\u0357\u035a" +
                    "\u035f\u0365\u0368\u036d\u0371\u0377\u037e\u0381\u0388\u0391\u0394\u039a" +
                    "\u039c\u03a0\u03a8\u03ad\u03b5\u03ba\u03c2\u03c7\u03cf\u03d4\u03da\u03e1" +
                    "\u03e4\u03ec\u03f6\u03f9\u03ff\u0401\u0404\u0417\u041d\u0426\u042b\u0434" +
                    "\u043f\u0446\u044c\u0452\u045b\u0462\u0466\u0468\u046c\u0473\u0475\u0479" +
                    "\u047c\u0483\u048a\u048d\u0497\u049a\u04a0\u04a2\u04a6\u04ad\u04b0\u04b8" +
                    "\u04c2\u04c5\u04cb\u04cd\u04d1\u04d8\u04e1\u04e5\u04e7\u04eb\u04f4\u04f9" +
                    "\u04fb\u0504\u050f\u0516\u0519\u051c\u0529\u0537\u053c\u053f\u054c\u055a" +
                    "\u055f\u0568\u056b\u0571\u0573\u057a\u057c\u0583\u058a\u058f\u0593\u0597" +
                    "\u05a7\u05ac\u05b0\u05c7\u05cd\u05cf\u05d3\u05d8\u05e0\u05e5\u05ee\u05f5" +
                    "\u05f9\u0609\u060c\u0614\u061d\u0621\u0626\u0644\u0650\u0655\u0661\u0667" +
                    "\u066e\u0672\u067c\u067f\u0685\u0688\u068a\u068c\u0693\u069c\u06a1\u06ad" +
                    "\u06b1\u06b5\u06b9\u06c0\u06c3\u06c5\u06cf\u06d6\u06d9\u06dd\u06e8\u06f2" +
                    "\u0700\u0708\u070b\u0713\u071b\u0723\u072d\u073d\u0743\u074b\u0750\u0753" +
                    "\u075a\u0763\u0768\u0776\u0779\u077b\u0780\u0784\u0787\u078e\u0796\u079a" +
                    "\u079e\u07a1\u07a7\u07aa\u07ac\u07b5\u07ba\u07be\u07c2\u07c5\u07d0\u07d5" +
                    "\u07d9\u07e0\u07e9\u07ed\u07ef\u07f3\u07fc\u0801\u0803\u080c\u0817\u081e" +
                    "\u0821\u0828\u0831\u0836\u0839\u083d\u0847\u0850\u0883";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = {
            null, "'['", "':'", "']'", "'::'", null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, "';'", "'.'", "'('", "')'",
            "','", "'='", "'*'", "'+'", "'-'", "'~'", "'||'", "'/'", "'%'", "'<<'",
            "'>>'", "'&'", "'|'", "'<'", "'<='", "'>'", "'>='", "'=='", "'!='", "'<>'"
    };
    private static final String[] _SYMBOLIC_NAMES = {
            null, null, null, null, null, "BIGINT", "BLOB", "CHAR", "CHARACTER", "DATE",
            "DECIMAL", "DOUBLE_PRECISION", "FLOAT", "INT", "INTEGER", "NATIONAL_CHARACTER",
            "NATIONAL_CHAR", "NCHAR", "NATIONAL_CHARACTER_VARYING", "NATIONAL_CHAR_VARYING",
            "NCHAR_VARYING", "NUMERIC", "SMALLINT", "TIME", "TIMESTAMP", "VARYING_CHARACTER",
            "VARCHAR", "VARYING", "SUB_TYPE", "SEGMENT_SIZE", "CHARACTER_SET", "SCOL",
            "DOT", "OPEN_PAR", "CLOSE_PAR", "COMMA", "ASSIGN", "STAR", "PLUS", "MINUS",
            "TILDE", "PIPE2", "DIV", "MOD", "LT2", "GT2", "AMP", "PIPE", "LT", "LT_EQ",
            "GT", "GT_EQ", "EQ", "NOT_EQ1", "NOT_EQ2", "K_ABORT", "K_ACTION", "K_ADD",
            "K_AFTER", "K_ALL", "K_ALTER", "K_ANALYZE", "K_AND", "K_AS", "K_ASC",
            "K_ATTACH", "K_AUTHID", "K_AUTOINCREMENT", "K_BEFORE", "K_BEGIN", "K_BETWEEN",
            "K_BLOCK", "K_BY", "K_CALLER", "K_CASCADE", "K_CASE", "K_CAST", "K_CHECK",
            "K_COLLATE", "K_COLUMN", "K_COMMIT", "K_CONFLICT", "K_CONSTRAINT", "K_CREATE",
            "K_CREATE_OR_ALTER", "K_CROSS", "K_CURRENT_DATE", "K_CURRENT_TIME", "K_CURRENT_TIMESTAMP",
            "K_DATABASE", "K_DECLARE", "K_DEFAULT", "K_DEFERRABLE", "K_DEFERRED",
            "K_DELETE", "K_DESC", "K_DETACH", "K_DISTINCT", "K_DROP", "K_EACH", "K_ELSE",
            "K_END", "K_ENABLE", "K_ESCAPE", "K_EXCEPT", "K_EXCLUSIVE", "K_EXECUTE",
            "K_EXISTS", "K_EXPLAIN", "K_FAIL", "K_FOR", "K_FOREIGN", "K_FROM", "K_FULL",
            "K_GLOB", "K_GROUP", "K_HAVING", "K_IF", "K_IGNORE", "K_IMMEDIATE", "K_IN",
            "K_INDEX", "K_INDEXED", "K_INITIALLY", "K_INNER", "K_INSERT", "K_INSTEAD",
            "K_INTERSECT", "K_INTO", "K_IS", "K_ISNULL", "K_JOIN", "K_KEY", "K_LEFT",
            "K_LIKE", "K_LIMIT", "K_MATCH", "K_NATURAL", "K_NEXTVAL", "K_NO", "K_NOT",
            "K_NOTNULL", "K_NULL", "K_OF", "K_OFFSET", "K_ON", "K_ONLY", "K_OR", "K_ORDER",
            "K_OUTER", "K_OWNER", "K_PLAN", "K_PRAGMA", "K_PROCEDURE", "K_PRIMARY",
            "K_QUERY", "K_RAISE", "K_RECURSIVE", "K_REFERENCES", "K_REGEXP", "K_REINDEX",
            "K_RELEASE", "K_RENAME", "K_REPLACE", "K_RESTRICT", "K_RETURNS", "K_RIGHT",
            "K_ROLLBACK", "K_ROW", "K_SAVEPOINT", "K_SELECT", "K_SET", "K_TABLE",
            "K_TEMP", "K_TEMPORARY", "K_THEN", "K_TO", "K_TRANSACTION", "K_TRIGGER",
            "K_TYPE_OF", "K_UNION", "K_UNIQUE", "K_UPDATE", "K_USING", "K_VACUUM",
            "K_VALUES", "K_VARIABLE", "K_VIEW", "K_VIRTUAL", "K_WHEN", "K_WHERE",
            "K_WITH", "K_WITHOUT", "IDENTIFIER", "NUMERIC_LITERAL", "BIND_PARAMETER",
            "STRING_LITERAL", "BLOB_LITERAL", "SINGLE_LINE_COMMENT", "MULTILINE_COMMENT",
            "SPACES", "UNEXPECTED_CHAR", "DOUBLE", "PRECISION", "CHARACTER_VARYING",
            "DIGIT", "SEGMENT", "SIZE"
    };
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    static {
        RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION);
    }

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }

    public REDDATABASESqlParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "REDDATABASESql.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public final ParseContext parse() throws RecognitionException {
        ParseContext _localctx = new ParseContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_parse);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(240);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SCOL) | (1L << K_ALTER) | (1L << K_ANALYZE))) != 0) || ((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & ((1L << (K_ATTACH - 65)) | (1L << (K_BEGIN - 65)) | (1L << (K_COMMIT - 65)) | (1L << (K_CREATE - 65)) | (1L << (K_DELETE - 65)) | (1L << (K_DETACH - 65)) | (1L << (K_DROP - 65)) | (1L << (K_END - 65)) | (1L << (K_EXECUTE - 65)) | (1L << (K_EXPLAIN - 65)) | (1L << (K_INSERT - 65)))) != 0) || ((((_la - 152)) & ~0x3f) == 0 && ((1L << (_la - 152)) & ((1L << (K_PRAGMA - 152)) | (1L << (K_REINDEX - 152)) | (1L << (K_RELEASE - 152)) | (1L << (K_REPLACE - 152)) | (1L << (K_ROLLBACK - 152)) | (1L << (K_SAVEPOINT - 152)) | (1L << (K_SELECT - 152)) | (1L << (K_UPDATE - 152)) | (1L << (K_VACUUM - 152)) | (1L << (K_VALUES - 152)) | (1L << (K_WITH - 152)) | (1L << (UNEXPECTED_CHAR - 152)))) != 0)) {
                    {
                        setState(238);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case SCOL:
                            case K_ALTER:
                            case K_ANALYZE:
                            case K_ATTACH:
                            case K_BEGIN:
                            case K_COMMIT:
                            case K_CREATE:
                            case K_DELETE:
                            case K_DETACH:
                            case K_DROP:
                            case K_END:
                            case K_EXECUTE:
                            case K_EXPLAIN:
                            case K_INSERT:
                            case K_PRAGMA:
                            case K_REINDEX:
                            case K_RELEASE:
                            case K_REPLACE:
                            case K_ROLLBACK:
                            case K_SAVEPOINT:
                            case K_SELECT:
                            case K_UPDATE:
                            case K_VACUUM:
                            case K_VALUES:
                            case K_WITH: {
                                setState(236);
                                sql_stmt_list();
                            }
                            break;
                            case UNEXPECTED_CHAR: {
                                setState(237);
                                error();
                            }
                            break;
                            default:
                                throw new NoViableAltException(this);
                        }
                    }
                    setState(242);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(243);
                match(EOF);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ErrorContext error() throws RecognitionException {
        ErrorContext _localctx = new ErrorContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_error);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(245);
                ((ErrorContext) _localctx).UNEXPECTED_CHAR = match(UNEXPECTED_CHAR);

                throw new RuntimeException("UNEXPECTED_CHAR=" + (((ErrorContext) _localctx).UNEXPECTED_CHAR != null ? ((ErrorContext) _localctx).UNEXPECTED_CHAR.getText() : null));

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Sql_stmt_listContext sql_stmt_list() throws RecognitionException {
        Sql_stmt_listContext _localctx = new Sql_stmt_listContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_sql_stmt_list);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(251);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == SCOL) {
                    {
                        {
                            setState(248);
                            match(SCOL);
                        }
                    }
                    setState(253);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(254);
                sql_stmt();
                setState(263);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(256);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                do {
                                    {
                                        {
                                            setState(255);
                                            match(SCOL);
                                        }
                                    }
                                    setState(258);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                } while (_la == SCOL);
                                setState(260);
                                sql_stmt();
                            }
                        }
                    }
                    setState(265);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 4, _ctx);
                }
                setState(269);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 5, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(266);
                                match(SCOL);
                            }
                        }
                    }
                    setState(271);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 5, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Sql_stmtContext sql_stmt() throws RecognitionException {
        Sql_stmtContext _localctx = new Sql_stmtContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_sql_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(277);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_EXPLAIN) {
                    {
                        setState(272);
                        match(K_EXPLAIN);
                        setState(275);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == K_QUERY) {
                            {
                                setState(273);
                                match(K_QUERY);
                                setState(274);
                                match(K_PLAN);
                            }
                        }

                    }
                }

                setState(311);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 8, _ctx)) {
                    case 1: {
                        setState(279);
                        alter_table_stmt();
                    }
                    break;
                    case 2: {
                        setState(280);
                        analyze_stmt();
                    }
                    break;
                    case 3: {
                        setState(281);
                        attach_stmt();
                    }
                    break;
                    case 4: {
                        setState(282);
                        begin_stmt();
                    }
                    break;
                    case 5: {
                        setState(283);
                        commit_stmt();
                    }
                    break;
                    case 6: {
                        setState(284);
                        compound_select_stmt();
                    }
                    break;
                    case 7: {
                        setState(285);
                        create_index_stmt();
                    }
                    break;
                    case 8: {
                        setState(286);
                        create_table_stmt();
                    }
                    break;
                    case 9: {
                        setState(287);
                        create_trigger_stmt();
                    }
                    break;
                    case 10: {
                        setState(288);
                        create_view_stmt();
                    }
                    break;
                    case 11: {
                        setState(289);
                        create_virtual_table_stmt();
                    }
                    break;
                    case 12: {
                        setState(290);
                        create_procedure_stmt();
                    }
                    break;
                    case 13: {
                        setState(291);
                        execute_block_stmt();
                    }
                    break;
                    case 14: {
                        setState(292);
                        delete_stmt();
                    }
                    break;
                    case 15: {
                        setState(293);
                        delete_stmt_limited();
                    }
                    break;
                    case 16: {
                        setState(294);
                        detach_stmt();
                    }
                    break;
                    case 17: {
                        setState(295);
                        drop_index_stmt();
                    }
                    break;
                    case 18: {
                        setState(296);
                        drop_table_stmt();
                    }
                    break;
                    case 19: {
                        setState(297);
                        drop_trigger_stmt();
                    }
                    break;
                    case 20: {
                        setState(298);
                        drop_view_stmt();
                    }
                    break;
                    case 21: {
                        setState(299);
                        factored_select_stmt();
                    }
                    break;
                    case 22: {
                        setState(300);
                        insert_stmt();
                    }
                    break;
                    case 23: {
                        setState(301);
                        pragma_stmt();
                    }
                    break;
                    case 24: {
                        setState(302);
                        reindex_stmt();
                    }
                    break;
                    case 25: {
                        setState(303);
                        release_stmt();
                    }
                    break;
                    case 26: {
                        setState(304);
                        rollback_stmt();
                    }
                    break;
                    case 27: {
                        setState(305);
                        savepoint_stmt();
                    }
                    break;
                    case 28: {
                        setState(306);
                        simple_select_stmt();
                    }
                    break;
                    case 29: {
                        setState(307);
                        select_stmt();
                    }
                    break;
                    case 30: {
                        setState(308);
                        update_stmt();
                    }
                    break;
                    case 31: {
                        setState(309);
                        update_stmt_limited();
                    }
                    break;
                    case 32: {
                        setState(310);
                        vacuum_stmt();
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Alter_table_stmtContext alter_table_stmt() throws RecognitionException {
        Alter_table_stmtContext _localctx = new Alter_table_stmtContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_alter_table_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(313);
                match(K_ALTER);
                setState(314);
                match(K_TABLE);
                setState(316);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_ONLY) {
                    {
                        setState(315);
                        match(K_ONLY);
                    }
                }

                setState(321);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 10, _ctx)) {
                    case 1: {
                        setState(318);
                        database_name();
                        setState(319);
                        match(DOT);
                    }
                    break;
                }
                setState(323);
                source_table_name();
                setState(334);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 12, _ctx)) {
                    case 1: {
                        setState(324);
                        match(K_RENAME);
                        setState(325);
                        match(K_TO);
                        setState(326);
                        new_table_name();
                    }
                    break;
                    case 2: {
                        setState(327);
                        alter_table_add();
                    }
                    break;
                    case 3: {
                        setState(328);
                        alter_table_add_constraint();
                    }
                    break;
                    case 4: {
                        setState(329);
                        match(K_ADD);
                        setState(331);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 11, _ctx)) {
                            case 1: {
                                setState(330);
                                match(K_COLUMN);
                            }
                            break;
                        }
                        setState(333);
                        column_def();
                    }
                    break;
                }
                setState(337);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 13, _ctx)) {
                    case 1: {
                        setState(336);
                        match(K_ENABLE);
                    }
                    break;
                }
                setState(340);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 14, _ctx)) {
                    case 1: {
                        setState(339);
                        unknown();
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Alter_table_add_constraintContext alter_table_add_constraint() throws RecognitionException {
        Alter_table_add_constraintContext _localctx = new Alter_table_add_constraintContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_alter_table_add_constraint);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(342);
                match(K_ADD);
                setState(343);
                match(K_CONSTRAINT);
                setState(344);
                any_name();
                setState(345);
                table_constraint();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Alter_table_addContext alter_table_add() throws RecognitionException {
        Alter_table_addContext _localctx = new Alter_table_addContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_alter_table_add);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(347);
                match(K_ADD);
                setState(348);
                table_constraint();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Analyze_stmtContext analyze_stmt() throws RecognitionException {
        Analyze_stmtContext _localctx = new Analyze_stmtContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_analyze_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(350);
                match(K_ANALYZE);
                setState(357);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 15, _ctx)) {
                    case 1: {
                        setState(351);
                        database_name();
                    }
                    break;
                    case 2: {
                        setState(352);
                        table_or_index_name();
                    }
                    break;
                    case 3: {
                        setState(353);
                        database_name();
                        setState(354);
                        match(DOT);
                        setState(355);
                        table_or_index_name();
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Attach_stmtContext attach_stmt() throws RecognitionException {
        Attach_stmtContext _localctx = new Attach_stmtContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_attach_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(359);
                match(K_ATTACH);
                setState(361);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 16, _ctx)) {
                    case 1: {
                        setState(360);
                        match(K_DATABASE);
                    }
                    break;
                }
                setState(363);
                expr(0);
                setState(364);
                match(K_AS);
                setState(365);
                database_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Begin_stmtContext begin_stmt() throws RecognitionException {
        Begin_stmtContext _localctx = new Begin_stmtContext(_ctx, getState());
        enterRule(_localctx, 18, RULE_begin_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(367);
                match(K_BEGIN);
                setState(369);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (((((_la - 93)) & ~0x3f) == 0 && ((1L << (_la - 93)) & ((1L << (K_DEFERRED - 93)) | (1L << (K_EXCLUSIVE - 93)) | (1L << (K_IMMEDIATE - 93)))) != 0)) {
                    {
                        setState(368);
                        _la = _input.LA(1);
                        if (!(((((_la - 93)) & ~0x3f) == 0 && ((1L << (_la - 93)) & ((1L << (K_DEFERRED - 93)) | (1L << (K_EXCLUSIVE - 93)) | (1L << (K_IMMEDIATE - 93)))) != 0))) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                    }
                }

                setState(375);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_TRANSACTION) {
                    {
                        setState(371);
                        match(K_TRANSACTION);
                        setState(373);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 18, _ctx)) {
                            case 1: {
                                setState(372);
                                transaction_name();
                            }
                            break;
                        }
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Commit_stmtContext commit_stmt() throws RecognitionException {
        Commit_stmtContext _localctx = new Commit_stmtContext(_ctx, getState());
        enterRule(_localctx, 20, RULE_commit_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(377);
                _la = _input.LA(1);
                if (!(_la == K_COMMIT || _la == K_END)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
                setState(382);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_TRANSACTION) {
                    {
                        setState(378);
                        match(K_TRANSACTION);
                        setState(380);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 20, _ctx)) {
                            case 1: {
                                setState(379);
                                transaction_name();
                            }
                            break;
                        }
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Compound_select_stmtContext compound_select_stmt() throws RecognitionException {
        Compound_select_stmtContext _localctx = new Compound_select_stmtContext(_ctx, getState());
        enterRule(_localctx, 22, RULE_compound_select_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(396);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WITH) {
                    {
                        setState(384);
                        match(K_WITH);
                        setState(386);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 22, _ctx)) {
                            case 1: {
                                setState(385);
                                match(K_RECURSIVE);
                            }
                            break;
                        }
                        setState(388);
                        common_table_expression();
                        setState(393);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(389);
                                    match(COMMA);
                                    setState(390);
                                    common_table_expression();
                                }
                            }
                            setState(395);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(398);
                select_core();
                setState(408);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        {
                            setState(405);
                            _errHandler.sync(this);
                            switch (_input.LA(1)) {
                                case K_UNION: {
                                    setState(399);
                                    match(K_UNION);
                                    setState(401);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    if (_la == K_ALL) {
                                        {
                                            setState(400);
                                            match(K_ALL);
                                        }
                                    }

                                }
                                break;
                                case K_INTERSECT: {
                                    setState(403);
                                    match(K_INTERSECT);
                                }
                                break;
                                case K_EXCEPT: {
                                    setState(404);
                                    match(K_EXCEPT);
                                }
                                break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                            setState(407);
                            select_core();
                        }
                    }
                    setState(410);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while (_la == K_EXCEPT || _la == K_INTERSECT || _la == K_UNION);
                setState(422);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_ORDER) {
                    {
                        setState(412);
                        match(K_ORDER);
                        setState(413);
                        match(K_BY);
                        setState(414);
                        ordering_term();
                        setState(419);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(415);
                                    match(COMMA);
                                    setState(416);
                                    ordering_term();
                                }
                            }
                            setState(421);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(430);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_LIMIT) {
                    {
                        setState(424);
                        match(K_LIMIT);
                        setState(425);
                        expr(0);
                        setState(428);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == COMMA || _la == K_OFFSET) {
                            {
                                setState(426);
                                _la = _input.LA(1);
                                if (!(_la == COMMA || _la == K_OFFSET)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(427);
                                expr(0);
                            }
                        }

                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Create_index_stmtContext create_index_stmt() throws RecognitionException {
        Create_index_stmtContext _localctx = new Create_index_stmtContext(_ctx, getState());
        enterRule(_localctx, 24, RULE_create_index_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(432);
                match(K_CREATE);
                setState(434);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_UNIQUE) {
                    {
                        setState(433);
                        match(K_UNIQUE);
                    }
                }

                setState(436);
                match(K_INDEX);
                setState(440);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 33, _ctx)) {
                    case 1: {
                        setState(437);
                        match(K_IF);
                        setState(438);
                        match(K_NOT);
                        setState(439);
                        match(K_EXISTS);
                    }
                    break;
                }
                setState(445);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 34, _ctx)) {
                    case 1: {
                        setState(442);
                        database_name();
                        setState(443);
                        match(DOT);
                    }
                    break;
                }
                setState(447);
                index_name();
                setState(448);
                match(K_ON);
                setState(449);
                table_name();
                setState(450);
                match(OPEN_PAR);
                setState(451);
                indexed_column();
                setState(456);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(452);
                            match(COMMA);
                            setState(453);
                            indexed_column();
                        }
                    }
                    setState(458);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(459);
                match(CLOSE_PAR);
                setState(462);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WHERE) {
                    {
                        setState(460);
                        match(K_WHERE);
                        setState(461);
                        expr(0);
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Create_table_stmtContext create_table_stmt() throws RecognitionException {
        Create_table_stmtContext _localctx = new Create_table_stmtContext(_ctx, getState());
        enterRule(_localctx, 26, RULE_create_table_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(464);
                match(K_CREATE);
                setState(466);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_TEMP || _la == K_TEMPORARY) {
                    {
                        setState(465);
                        _la = _input.LA(1);
                        if (!(_la == K_TEMP || _la == K_TEMPORARY)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                    }
                }

                setState(468);
                match(K_TABLE);
                setState(472);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 38, _ctx)) {
                    case 1: {
                        setState(469);
                        match(K_IF);
                        setState(470);
                        match(K_NOT);
                        setState(471);
                        match(K_EXISTS);
                    }
                    break;
                }
                setState(477);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 39, _ctx)) {
                    case 1: {
                        setState(474);
                        database_name();
                        setState(475);
                        match(DOT);
                    }
                    break;
                }
                setState(479);
                table_name();
                setState(498);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case OPEN_PAR: {
                        setState(480);
                        match(OPEN_PAR);
                        setState(481);
                        column_def();
                        setState(488);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                setState(486);
                                _errHandler.sync(this);
                                switch (getInterpreter().adaptivePredict(_input, 40, _ctx)) {
                                    case 1: {
                                        setState(482);
                                        match(COMMA);
                                        setState(483);
                                        table_constraint();
                                    }
                                    break;
                                    case 2: {
                                        setState(484);
                                        match(COMMA);
                                        setState(485);
                                        column_def();
                                    }
                                    break;
                                }
                            }
                            setState(490);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(491);
                        match(CLOSE_PAR);
                        setState(494);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 42, _ctx)) {
                            case 1: {
                                setState(492);
                                match(K_WITHOUT);
                                setState(493);
                                match(IDENTIFIER);
                            }
                            break;
                        }
                    }
                    break;
                    case K_AS: {
                        setState(496);
                        match(K_AS);
                        setState(497);
                        select_stmt();
                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(501);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 44, _ctx)) {
                    case 1: {
                        setState(500);
                        unknown();
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Create_trigger_stmtContext create_trigger_stmt() throws RecognitionException {
        Create_trigger_stmtContext _localctx = new Create_trigger_stmtContext(_ctx, getState());
        enterRule(_localctx, 28, RULE_create_trigger_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(503);
                match(K_CREATE);
                setState(505);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_TEMP || _la == K_TEMPORARY) {
                    {
                        setState(504);
                        _la = _input.LA(1);
                        if (!(_la == K_TEMP || _la == K_TEMPORARY)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                    }
                }

                setState(507);
                match(K_TRIGGER);
                setState(511);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 46, _ctx)) {
                    case 1: {
                        setState(508);
                        match(K_IF);
                        setState(509);
                        match(K_NOT);
                        setState(510);
                        match(K_EXISTS);
                    }
                    break;
                }
                setState(516);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 47, _ctx)) {
                    case 1: {
                        setState(513);
                        database_name();
                        setState(514);
                        match(DOT);
                    }
                    break;
                }
                setState(518);
                trigger_name();
                setState(523);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case K_BEFORE: {
                        setState(519);
                        match(K_BEFORE);
                    }
                    break;
                    case K_AFTER: {
                        setState(520);
                        match(K_AFTER);
                    }
                    break;
                    case K_INSTEAD: {
                        setState(521);
                        match(K_INSTEAD);
                        setState(522);
                        match(K_OF);
                    }
                    break;
                    case K_DELETE:
                    case K_INSERT:
                    case K_UPDATE:
                        break;
                    default:
                        break;
                }
                setState(539);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case K_DELETE: {
                        setState(525);
                        match(K_DELETE);
                    }
                    break;
                    case K_INSERT: {
                        setState(526);
                        match(K_INSERT);
                    }
                    break;
                    case K_UPDATE: {
                        setState(527);
                        match(K_UPDATE);
                        setState(537);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == K_OF) {
                            {
                                setState(528);
                                match(K_OF);
                                setState(529);
                                column_name();
                                setState(534);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(530);
                                            match(COMMA);
                                            setState(531);
                                            column_name();
                                        }
                                    }
                                    setState(536);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                        }

                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(541);
                match(K_ON);
                setState(545);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 52, _ctx)) {
                    case 1: {
                        setState(542);
                        database_name();
                        setState(543);
                        match(DOT);
                    }
                    break;
                }
                setState(547);
                table_name();
                setState(551);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_FOR) {
                    {
                        setState(548);
                        match(K_FOR);
                        setState(549);
                        match(K_EACH);
                        setState(550);
                        match(K_ROW);
                    }
                }

                setState(555);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WHEN) {
                    {
                        setState(553);
                        match(K_WHEN);
                        setState(554);
                        expr(0);
                    }
                }

                setState(557);
                match(K_BEGIN);
                setState(566);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        {
                            setState(562);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 55, _ctx)) {
                                case 1: {
                                    setState(558);
                                    update_stmt();
                                }
                                break;
                                case 2: {
                                    setState(559);
                                    insert_stmt();
                                }
                                break;
                                case 3: {
                                    setState(560);
                                    delete_stmt();
                                }
                                break;
                                case 4: {
                                    setState(561);
                                    select_stmt();
                                }
                                break;
                            }
                            setState(564);
                            match(SCOL);
                        }
                    }
                    setState(568);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                while (_la == K_DELETE || _la == K_INSERT || ((((_la - 163)) & ~0x3f) == 0 && ((1L << (_la - 163)) & ((1L << (K_REPLACE - 163)) | (1L << (K_SELECT - 163)) | (1L << (K_UPDATE - 163)) | (1L << (K_VALUES - 163)) | (1L << (K_WITH - 163)))) != 0));
                setState(570);
                match(K_END);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Create_view_stmtContext create_view_stmt() throws RecognitionException {
        Create_view_stmtContext _localctx = new Create_view_stmtContext(_ctx, getState());
        enterRule(_localctx, 30, RULE_create_view_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(572);
                match(K_CREATE);
                setState(574);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_TEMP || _la == K_TEMPORARY) {
                    {
                        setState(573);
                        _la = _input.LA(1);
                        if (!(_la == K_TEMP || _la == K_TEMPORARY)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                    }
                }

                setState(576);
                match(K_VIEW);
                setState(580);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 58, _ctx)) {
                    case 1: {
                        setState(577);
                        match(K_IF);
                        setState(578);
                        match(K_NOT);
                        setState(579);
                        match(K_EXISTS);
                    }
                    break;
                }
                setState(585);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 59, _ctx)) {
                    case 1: {
                        setState(582);
                        database_name();
                        setState(583);
                        match(DOT);
                    }
                    break;
                }
                setState(587);
                view_name();
                setState(588);
                match(K_AS);
                setState(589);
                select_stmt();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Create_virtual_table_stmtContext create_virtual_table_stmt() throws RecognitionException {
        Create_virtual_table_stmtContext _localctx = new Create_virtual_table_stmtContext(_ctx, getState());
        enterRule(_localctx, 32, RULE_create_virtual_table_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(591);
                match(K_CREATE);
                setState(592);
                match(K_VIRTUAL);
                setState(593);
                match(K_TABLE);
                setState(597);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 60, _ctx)) {
                    case 1: {
                        setState(594);
                        match(K_IF);
                        setState(595);
                        match(K_NOT);
                        setState(596);
                        match(K_EXISTS);
                    }
                    break;
                }
                setState(602);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 61, _ctx)) {
                    case 1: {
                        setState(599);
                        database_name();
                        setState(600);
                        match(DOT);
                    }
                    break;
                }
                setState(604);
                table_name();
                setState(605);
                match(K_USING);
                setState(606);
                module_name();
                setState(618);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == OPEN_PAR) {
                    {
                        setState(607);
                        match(OPEN_PAR);
                        setState(608);
                        module_argument();
                        setState(613);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(609);
                                    match(COMMA);
                                    setState(610);
                                    module_argument();
                                }
                            }
                            setState(615);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(616);
                        match(CLOSE_PAR);
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Create_procedure_stmtContext create_procedure_stmt() throws RecognitionException {
        Create_procedure_stmtContext _localctx = new Create_procedure_stmtContext(_ctx, getState());
        enterRule(_localctx, 34, RULE_create_procedure_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(620);
                match(K_CREATE);
                setState(621);
                match(K_PROCEDURE);
                setState(622);
                procedure_name();
                setState(625);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_AUTHID) {
                    {
                        setState(623);
                        match(K_AUTHID);
                        setState(624);
                        _la = _input.LA(1);
                        if (!(_la == K_CALLER || _la == K_OWNER)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                    }
                }

                setState(627);
                declare_block();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Execute_block_stmtContext execute_block_stmt() throws RecognitionException {
        Execute_block_stmtContext _localctx = new Execute_block_stmtContext(_ctx, getState());
        enterRule(_localctx, 36, RULE_execute_block_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(629);
                match(K_EXECUTE);
                setState(630);
                match(K_BLOCK);
                setState(631);
                declare_block();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Declare_blockContext declare_block() throws RecognitionException {
        Declare_blockContext _localctx = new Declare_blockContext(_ctx, getState());
        enterRule(_localctx, 38, RULE_declare_block);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(644);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == OPEN_PAR) {
                    {
                        setState(633);
                        match(OPEN_PAR);
                        setState(634);
                        input_parameter();
                        setState(639);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(635);
                                    match(COMMA);
                                    setState(636);
                                    input_parameter();
                                }
                            }
                            setState(641);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(642);
                        match(CLOSE_PAR);
                    }
                }

                setState(658);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_RETURNS) {
                    {
                        setState(646);
                        match(K_RETURNS);
                        setState(647);
                        match(OPEN_PAR);
                        setState(648);
                        output_parameter();
                        setState(653);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(649);
                                    match(COMMA);
                                    setState(650);
                                    output_parameter();
                                }
                            }
                            setState(655);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(656);
                        match(CLOSE_PAR);
                    }
                }

                setState(660);
                match(K_AS);
                setState(664);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == K_DECLARE) {
                    {
                        {
                            setState(661);
                            local_variable();
                        }
                    }
                    setState(666);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(667);
                match(K_BEGIN);
                setState(668);
                body();
                setState(669);
                match(K_END);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final BodyContext body() throws RecognitionException {
        BodyContext _localctx = new BodyContext(_ctx, getState());
        enterRule(_localctx, 40, RULE_body);
        try {
            int _alt;
            setState(692);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 73, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(674);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 70, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(671);
                                    matchWildcard();
                                }
                            }
                        }
                        setState(676);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 70, _ctx);
                    }
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(680);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 71, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(677);
                                    matchWildcard();
                                }
                            }
                        }
                        setState(682);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 71, _ctx);
                    }
                    setState(683);
                    match(K_BEGIN);
                    setState(684);
                    body();
                    setState(685);
                    match(K_END);
                    setState(689);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 72, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(686);
                                    matchWildcard();
                                }
                            }
                        }
                        setState(691);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 72, _ctx);
                    }
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Local_variableContext local_variable() throws RecognitionException {
        Local_variableContext _localctx = new Local_variableContext(_ctx, getState());
        enterRule(_localctx, 42, RULE_local_variable);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(694);
                match(K_DECLARE);
                setState(696);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_VARIABLE) {
                    {
                        setState(695);
                        match(K_VARIABLE);
                    }
                }

                setState(698);
                variable_name();
                setState(699);
                datatype();
                setState(701);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_NOTNULL) {
                    {
                        setState(700);
                        match(K_NOTNULL);
                    }
                }

                setState(705);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_COLLATE) {
                    {
                        setState(703);
                        match(K_COLLATE);
                        setState(704);
                        order_collate();
                    }
                }

                setState(709);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == ASSIGN || _la == K_DEFAULT) {
                    {
                        setState(707);
                        _la = _input.LA(1);
                        if (!(_la == ASSIGN || _la == K_DEFAULT)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                        setState(708);
                        default_value();
                    }
                }

                setState(711);
                match(SCOL);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Output_parameterContext output_parameter() throws RecognitionException {
        Output_parameterContext _localctx = new Output_parameterContext(_ctx, getState());
        enterRule(_localctx, 44, RULE_output_parameter);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(713);
                desciption_parameter();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Default_valueContext default_value() throws RecognitionException {
        Default_valueContext _localctx = new Default_valueContext(_ctx, getState());
        enterRule(_localctx, 46, RULE_default_value);
        try {
            setState(717);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case K_CURRENT_DATE:
                case K_CURRENT_TIME:
                case K_CURRENT_TIMESTAMP:
                case K_NULL:
                case NUMERIC_LITERAL:
                case STRING_LITERAL:
                case BLOB_LITERAL:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(715);
                    literal_value();
                }
                break;
                case BIND_PARAMETER:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(716);
                    match(BIND_PARAMETER);
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Variable_nameContext variable_name() throws RecognitionException {
        Variable_nameContext _localctx = new Variable_nameContext(_ctx, getState());
        enterRule(_localctx, 48, RULE_variable_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(719);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Input_parameterContext input_parameter() throws RecognitionException {
        Input_parameterContext _localctx = new Input_parameterContext(_ctx, getState());
        enterRule(_localctx, 50, RULE_input_parameter);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(721);
                desciption_parameter();
                setState(724);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == ASSIGN || _la == K_DEFAULT) {
                    {
                        setState(722);
                        _la = _input.LA(1);
                        if (!(_la == ASSIGN || _la == K_DEFAULT)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                        setState(723);
                        default_value();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Desciption_parameterContext desciption_parameter() throws RecognitionException {
        Desciption_parameterContext _localctx = new Desciption_parameterContext(_ctx, getState());
        enterRule(_localctx, 52, RULE_desciption_parameter);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(726);
                parameter_name();
                setState(727);
                datatype();
                setState(729);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_NOTNULL) {
                    {
                        setState(728);
                        match(K_NOTNULL);
                    }
                }

                setState(733);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_COLLATE) {
                    {
                        setState(731);
                        match(K_COLLATE);
                        setState(732);
                        order_collate();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Parameter_nameContext parameter_name() throws RecognitionException {
        Parameter_nameContext _localctx = new Parameter_nameContext(_ctx, getState());
        enterRule(_localctx, 54, RULE_parameter_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(735);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final DatatypeContext datatype() throws RecognitionException {
        DatatypeContext _localctx = new DatatypeContext(_ctx, getState());
        enterRule(_localctx, 56, RULE_datatype);
        int _la;
        try {
            setState(747);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 83, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(737);
                    datatypeSQL();
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(739);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == K_TYPE_OF) {
                        {
                            setState(738);
                            match(K_TYPE_OF);
                        }
                    }

                    setState(741);
                    domain_name();
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(742);
                    match(K_TYPE_OF);
                    setState(743);
                    table_name();
                    setState(744);
                    match(DOT);
                    setState(745);
                    column_name();
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final DatatypeSQLContext datatypeSQL() throws RecognitionException {
        DatatypeSQLContext _localctx = new DatatypeSQLContext(_ctx, getState());
        enterRule(_localctx, 58, RULE_datatypeSQL);
        int _la;
        try {
            setState(840);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 104, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(749);
                    _la = _input.LA(1);
                    if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << BIGINT) | (1L << INTEGER) | (1L << SMALLINT))) != 0))) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(751);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == T__0) {
                        {
                            setState(750);
                            array_size();
                        }
                    }

                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(756);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case FLOAT: {
                            setState(753);
                            match(FLOAT);
                        }
                        break;
                        case DOUBLE: {
                            setState(754);
                            match(DOUBLE);
                            setState(755);
                            match(PRECISION);
                        }
                        break;
                        default:
                            throw new NoViableAltException(this);
                    }
                    setState(759);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == T__0) {
                        {
                            setState(758);
                            array_size();
                        }
                    }

                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(761);
                    _la = _input.LA(1);
                    if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DATE) | (1L << TIME) | (1L << TIMESTAMP))) != 0))) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(763);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == T__0) {
                        {
                            setState(762);
                            array_size();
                        }
                    }

                }
                break;
                case 4:
                    enterOuterAlt(_localctx, 4);
                {
                    setState(765);
                    _la = _input.LA(1);
                    if (!(_la == DECIMAL || _la == NUMERIC)) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(774);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == OPEN_PAR) {
                        {
                            setState(766);
                            match(OPEN_PAR);
                            setState(767);
                            int_number();
                            setState(770);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(768);
                                    match(COMMA);
                                    setState(769);
                                    int_number();
                                }
                            }

                            setState(772);
                            match(CLOSE_PAR);
                        }
                    }

                    setState(777);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == T__0) {
                        {
                            setState(776);
                            array_size();
                        }
                    }

                }
                break;
                case 5:
                    enterOuterAlt(_localctx, 5);
                {
                    setState(779);
                    match(CHAR);
                }
                break;
                case 6:
                    enterOuterAlt(_localctx, 6);
                {
                    setState(780);
                    match(CHARACTER);
                }
                break;
                case 7:
                    enterOuterAlt(_localctx, 7);
                {
                    setState(781);
                    match(CHARACTER_VARYING);
                }
                break;
                case 8:
                    enterOuterAlt(_localctx, 8);
                {
                    setState(782);
                    match(VARCHAR);
                    setState(787);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == OPEN_PAR) {
                        {
                            setState(783);
                            match(OPEN_PAR);
                            setState(784);
                            int_number();
                            setState(785);
                            match(CLOSE_PAR);
                        }
                    }

                    setState(791);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == CHARACTER_SET) {
                        {
                            setState(789);
                            match(CHARACTER_SET);
                            setState(790);
                            charset_name();
                        }
                    }

                    setState(794);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == T__0) {
                        {
                            setState(793);
                            array_size();
                        }
                    }

                }
                break;
                case 9:
                    enterOuterAlt(_localctx, 9);
                {
                    setState(796);
                    _la = _input.LA(1);
                    if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NATIONAL_CHARACTER) | (1L << NATIONAL_CHAR) | (1L << NCHAR))) != 0))) {
                        _errHandler.recoverInline(this);
                    } else {
                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                        _errHandler.reportMatch(this);
                        consume();
                    }
                    setState(798);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == VARYING) {
                        {
                            setState(797);
                            match(VARYING);
                        }
                    }

                    setState(807);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == OPEN_PAR) {
                        {
                            setState(800);
                            match(OPEN_PAR);
                            setState(802);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            do {
                                {
                                    {
                                        setState(801);
                                        match(DIGIT);
                                    }
                                }
                                setState(804);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            } while (_la == DIGIT);
                            setState(806);
                            match(CLOSE_PAR);
                        }
                    }

                    setState(810);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == T__0) {
                        {
                            setState(809);
                            array_size();
                        }
                    }

                }
                break;
                case 10:
                    enterOuterAlt(_localctx, 10);
                {
                    setState(812);
                    match(BLOB);
                    setState(818);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SUB_TYPE) {
                        {
                            setState(813);
                            match(SUB_TYPE);
                            setState(816);
                            _errHandler.sync(this);
                            switch (_input.LA(1)) {
                                case OPEN_PAR:
                                case K_ABORT:
                                case K_ACTION:
                                case K_ADD:
                                case K_AFTER:
                                case K_ALL:
                                case K_ALTER:
                                case K_ANALYZE:
                                case K_AND:
                                case K_AS:
                                case K_ASC:
                                case K_ATTACH:
                                case K_AUTOINCREMENT:
                                case K_BEFORE:
                                case K_BEGIN:
                                case K_BETWEEN:
                                case K_BY:
                                case K_CASCADE:
                                case K_CASE:
                                case K_CAST:
                                case K_CHECK:
                                case K_COLLATE:
                                case K_COLUMN:
                                case K_COMMIT:
                                case K_CONFLICT:
                                case K_CONSTRAINT:
                                case K_CREATE:
                                case K_CROSS:
                                case K_CURRENT_DATE:
                                case K_CURRENT_TIME:
                                case K_CURRENT_TIMESTAMP:
                                case K_DATABASE:
                                case K_DEFAULT:
                                case K_DEFERRABLE:
                                case K_DEFERRED:
                                case K_DELETE:
                                case K_DESC:
                                case K_DETACH:
                                case K_DISTINCT:
                                case K_DROP:
                                case K_EACH:
                                case K_ELSE:
                                case K_END:
                                case K_ENABLE:
                                case K_ESCAPE:
                                case K_EXCEPT:
                                case K_EXCLUSIVE:
                                case K_EXISTS:
                                case K_EXPLAIN:
                                case K_FAIL:
                                case K_FOR:
                                case K_FOREIGN:
                                case K_FROM:
                                case K_FULL:
                                case K_GLOB:
                                case K_GROUP:
                                case K_HAVING:
                                case K_IF:
                                case K_IGNORE:
                                case K_IMMEDIATE:
                                case K_IN:
                                case K_INDEX:
                                case K_INDEXED:
                                case K_INITIALLY:
                                case K_INNER:
                                case K_INSERT:
                                case K_INSTEAD:
                                case K_INTERSECT:
                                case K_INTO:
                                case K_IS:
                                case K_ISNULL:
                                case K_JOIN:
                                case K_KEY:
                                case K_LEFT:
                                case K_LIKE:
                                case K_LIMIT:
                                case K_MATCH:
                                case K_NATURAL:
                                case K_NEXTVAL:
                                case K_NO:
                                case K_NOT:
                                case K_NOTNULL:
                                case K_NULL:
                                case K_OF:
                                case K_OFFSET:
                                case K_ON:
                                case K_OR:
                                case K_ORDER:
                                case K_OUTER:
                                case K_PLAN:
                                case K_PRAGMA:
                                case K_PRIMARY:
                                case K_QUERY:
                                case K_RAISE:
                                case K_RECURSIVE:
                                case K_REFERENCES:
                                case K_REGEXP:
                                case K_REINDEX:
                                case K_RELEASE:
                                case K_RENAME:
                                case K_REPLACE:
                                case K_RESTRICT:
                                case K_RIGHT:
                                case K_ROLLBACK:
                                case K_ROW:
                                case K_SAVEPOINT:
                                case K_SELECT:
                                case K_SET:
                                case K_TABLE:
                                case K_TEMP:
                                case K_TEMPORARY:
                                case K_THEN:
                                case K_TO:
                                case K_TRANSACTION:
                                case K_TRIGGER:
                                case K_UNION:
                                case K_UNIQUE:
                                case K_UPDATE:
                                case K_USING:
                                case K_VACUUM:
                                case K_VALUES:
                                case K_VIEW:
                                case K_VIRTUAL:
                                case K_WHEN:
                                case K_WHERE:
                                case K_WITH:
                                case K_WITHOUT:
                                case IDENTIFIER:
                                case STRING_LITERAL: {
                                    setState(814);
                                    any_name();
                                }
                                break;
                                case DIGIT: {
                                    setState(815);
                                    int_number();
                                }
                                break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                        }
                    }

                    setState(823);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == SEGMENT) {
                        {
                            setState(820);
                            match(SEGMENT);
                            setState(821);
                            match(SIZE);
                            setState(822);
                            int_number();
                        }
                    }

                    setState(827);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == CHARACTER_SET) {
                        {
                            setState(825);
                            match(CHARACTER_SET);
                            setState(826);
                            charset_name();
                        }
                    }

                }
                break;
                case 11:
                    enterOuterAlt(_localctx, 11);
                {
                    setState(829);
                    match(BLOB);
                    setState(838);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == OPEN_PAR) {
                        {
                            setState(830);
                            match(OPEN_PAR);
                            setState(831);
                            segment_size();
                            setState(834);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == COMMA) {
                                {
                                    setState(832);
                                    match(COMMA);
                                    setState(833);
                                    int_number();
                                }
                            }

                            setState(836);
                            match(CLOSE_PAR);
                        }
                    }

                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Segment_sizeContext segment_size() throws RecognitionException {
        Segment_sizeContext _localctx = new Segment_sizeContext(_ctx, getState());
        enterRule(_localctx, 60, RULE_segment_size);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(842);
                int_number();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Int_numberContext int_number() throws RecognitionException {
        Int_numberContext _localctx = new Int_numberContext(_ctx, getState());
        enterRule(_localctx, 62, RULE_int_number);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(845);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        {
                            setState(844);
                            match(DIGIT);
                        }
                    }
                    setState(847);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while (_la == DIGIT);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Array_sizeContext array_size() throws RecognitionException {
        Array_sizeContext _localctx = new Array_sizeContext(_ctx, getState());
        enterRule(_localctx, 64, RULE_array_size);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(849);
                match(T__0);
                setState(856);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 107, _ctx)) {
                    case 1: {
                        setState(851);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        do {
                            {
                                {
                                    setState(850);
                                    match(DIGIT);
                                }
                            }
                            setState(853);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        } while (_la == DIGIT);
                        setState(855);
                        match(T__1);
                    }
                    break;
                }
                setState(859);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        {
                            setState(858);
                            match(DIGIT);
                        }
                    }
                    setState(861);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while (_la == DIGIT);
                setState(879);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(863);
                            match(COMMA);
                            setState(870);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 110, _ctx)) {
                                case 1: {
                                    setState(865);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    do {
                                        {
                                            {
                                                setState(864);
                                                match(DIGIT);
                                            }
                                        }
                                        setState(867);
                                        _errHandler.sync(this);
                                        _la = _input.LA(1);
                                    } while (_la == DIGIT);
                                    setState(869);
                                    match(T__1);
                                }
                                break;
                            }
                            setState(873);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            do {
                                {
                                    {
                                        setState(872);
                                        match(DIGIT);
                                    }
                                }
                                setState(875);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            } while (_la == DIGIT);
                        }
                    }
                    setState(881);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(882);
                match(T__2);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Delete_stmtContext delete_stmt() throws RecognitionException {
        Delete_stmtContext _localctx = new Delete_stmtContext(_ctx, getState());
        enterRule(_localctx, 66, RULE_delete_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(885);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WITH) {
                    {
                        setState(884);
                        with_clause();
                    }
                }

                setState(887);
                match(K_DELETE);
                setState(888);
                match(K_FROM);
                setState(889);
                qualified_table_name();
                setState(892);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WHERE) {
                    {
                        setState(890);
                        match(K_WHERE);
                        setState(891);
                        expr(0);
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Delete_stmt_limitedContext delete_stmt_limited() throws RecognitionException {
        Delete_stmt_limitedContext _localctx = new Delete_stmt_limitedContext(_ctx, getState());
        enterRule(_localctx, 68, RULE_delete_stmt_limited);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(895);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WITH) {
                    {
                        setState(894);
                        with_clause();
                    }
                }

                setState(897);
                match(K_DELETE);
                setState(898);
                match(K_FROM);
                setState(899);
                qualified_table_name();
                setState(902);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WHERE) {
                    {
                        setState(900);
                        match(K_WHERE);
                        setState(901);
                        expr(0);
                    }
                }

                setState(922);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_LIMIT || _la == K_ORDER) {
                    {
                        setState(914);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == K_ORDER) {
                            {
                                setState(904);
                                match(K_ORDER);
                                setState(905);
                                match(K_BY);
                                setState(906);
                                ordering_term();
                                setState(911);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(907);
                                            match(COMMA);
                                            setState(908);
                                            ordering_term();
                                        }
                                    }
                                    setState(913);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                        }

                        setState(916);
                        match(K_LIMIT);
                        setState(917);
                        expr(0);
                        setState(920);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == COMMA || _la == K_OFFSET) {
                            {
                                setState(918);
                                _la = _input.LA(1);
                                if (!(_la == COMMA || _la == K_OFFSET)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(919);
                                expr(0);
                            }
                        }

                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Detach_stmtContext detach_stmt() throws RecognitionException {
        Detach_stmtContext _localctx = new Detach_stmtContext(_ctx, getState());
        enterRule(_localctx, 70, RULE_detach_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(924);
                match(K_DETACH);
                setState(926);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 121, _ctx)) {
                    case 1: {
                        setState(925);
                        match(K_DATABASE);
                    }
                    break;
                }
                setState(928);
                database_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Drop_index_stmtContext drop_index_stmt() throws RecognitionException {
        Drop_index_stmtContext _localctx = new Drop_index_stmtContext(_ctx, getState());
        enterRule(_localctx, 72, RULE_drop_index_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(930);
                match(K_DROP);
                setState(931);
                match(K_INDEX);
                setState(934);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 122, _ctx)) {
                    case 1: {
                        setState(932);
                        match(K_IF);
                        setState(933);
                        match(K_EXISTS);
                    }
                    break;
                }
                setState(939);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 123, _ctx)) {
                    case 1: {
                        setState(936);
                        database_name();
                        setState(937);
                        match(DOT);
                    }
                    break;
                }
                setState(941);
                index_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Drop_table_stmtContext drop_table_stmt() throws RecognitionException {
        Drop_table_stmtContext _localctx = new Drop_table_stmtContext(_ctx, getState());
        enterRule(_localctx, 74, RULE_drop_table_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(943);
                match(K_DROP);
                setState(944);
                match(K_TABLE);
                setState(947);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 124, _ctx)) {
                    case 1: {
                        setState(945);
                        match(K_IF);
                        setState(946);
                        match(K_EXISTS);
                    }
                    break;
                }
                setState(952);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 125, _ctx)) {
                    case 1: {
                        setState(949);
                        database_name();
                        setState(950);
                        match(DOT);
                    }
                    break;
                }
                setState(954);
                table_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Drop_trigger_stmtContext drop_trigger_stmt() throws RecognitionException {
        Drop_trigger_stmtContext _localctx = new Drop_trigger_stmtContext(_ctx, getState());
        enterRule(_localctx, 76, RULE_drop_trigger_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(956);
                match(K_DROP);
                setState(957);
                match(K_TRIGGER);
                setState(960);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 126, _ctx)) {
                    case 1: {
                        setState(958);
                        match(K_IF);
                        setState(959);
                        match(K_EXISTS);
                    }
                    break;
                }
                setState(965);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 127, _ctx)) {
                    case 1: {
                        setState(962);
                        database_name();
                        setState(963);
                        match(DOT);
                    }
                    break;
                }
                setState(967);
                trigger_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Drop_view_stmtContext drop_view_stmt() throws RecognitionException {
        Drop_view_stmtContext _localctx = new Drop_view_stmtContext(_ctx, getState());
        enterRule(_localctx, 78, RULE_drop_view_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(969);
                match(K_DROP);
                setState(970);
                match(K_VIEW);
                setState(973);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 128, _ctx)) {
                    case 1: {
                        setState(971);
                        match(K_IF);
                        setState(972);
                        match(K_EXISTS);
                    }
                    break;
                }
                setState(978);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 129, _ctx)) {
                    case 1: {
                        setState(975);
                        database_name();
                        setState(976);
                        match(DOT);
                    }
                    break;
                }
                setState(980);
                view_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Factored_select_stmtContext factored_select_stmt() throws RecognitionException {
        Factored_select_stmtContext _localctx = new Factored_select_stmtContext(_ctx, getState());
        enterRule(_localctx, 80, RULE_factored_select_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(994);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WITH) {
                    {
                        setState(982);
                        match(K_WITH);
                        setState(984);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 130, _ctx)) {
                            case 1: {
                                setState(983);
                                match(K_RECURSIVE);
                            }
                            break;
                        }
                        setState(986);
                        common_table_expression();
                        setState(991);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(987);
                                    match(COMMA);
                                    setState(988);
                                    common_table_expression();
                                }
                            }
                            setState(993);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(996);
                select_core();
                setState(1002);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == K_EXCEPT || _la == K_INTERSECT || _la == K_UNION) {
                    {
                        {
                            setState(997);
                            compound_operator();
                            setState(998);
                            select_core();
                        }
                    }
                    setState(1004);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(1015);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_ORDER) {
                    {
                        setState(1005);
                        match(K_ORDER);
                        setState(1006);
                        match(K_BY);
                        setState(1007);
                        ordering_term();
                        setState(1012);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1008);
                                    match(COMMA);
                                    setState(1009);
                                    ordering_term();
                                }
                            }
                            setState(1014);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(1023);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_LIMIT) {
                    {
                        setState(1017);
                        match(K_LIMIT);
                        setState(1018);
                        expr(0);
                        setState(1021);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == COMMA || _la == K_OFFSET) {
                            {
                                setState(1019);
                                _la = _input.LA(1);
                                if (!(_la == COMMA || _la == K_OFFSET)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(1020);
                                expr(0);
                            }
                        }

                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Insert_stmtContext insert_stmt() throws RecognitionException {
        Insert_stmtContext _localctx = new Insert_stmtContext(_ctx, getState());
        enterRule(_localctx, 82, RULE_insert_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1026);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WITH) {
                    {
                        setState(1025);
                        with_clause();
                    }
                }

                setState(1045);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 139, _ctx)) {
                    case 1: {
                        setState(1028);
                        match(K_INSERT);
                    }
                    break;
                    case 2: {
                        setState(1029);
                        match(K_REPLACE);
                    }
                    break;
                    case 3: {
                        setState(1030);
                        match(K_INSERT);
                        setState(1031);
                        match(K_OR);
                        setState(1032);
                        match(K_REPLACE);
                    }
                    break;
                    case 4: {
                        setState(1033);
                        match(K_INSERT);
                        setState(1034);
                        match(K_OR);
                        setState(1035);
                        match(K_ROLLBACK);
                    }
                    break;
                    case 5: {
                        setState(1036);
                        match(K_INSERT);
                        setState(1037);
                        match(K_OR);
                        setState(1038);
                        match(K_ABORT);
                    }
                    break;
                    case 6: {
                        setState(1039);
                        match(K_INSERT);
                        setState(1040);
                        match(K_OR);
                        setState(1041);
                        match(K_FAIL);
                    }
                    break;
                    case 7: {
                        setState(1042);
                        match(K_INSERT);
                        setState(1043);
                        match(K_OR);
                        setState(1044);
                        match(K_IGNORE);
                    }
                    break;
                }
                setState(1047);
                match(K_INTO);
                setState(1051);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 140, _ctx)) {
                    case 1: {
                        setState(1048);
                        database_name();
                        setState(1049);
                        match(DOT);
                    }
                    break;
                }
                setState(1053);
                table_name();
                setState(1065);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == OPEN_PAR) {
                    {
                        setState(1054);
                        match(OPEN_PAR);
                        setState(1055);
                        column_name();
                        setState(1060);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1056);
                                    match(COMMA);
                                    setState(1057);
                                    column_name();
                                }
                            }
                            setState(1062);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(1063);
                        match(CLOSE_PAR);
                    }
                }

                setState(1098);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 146, _ctx)) {
                    case 1: {
                        setState(1067);
                        match(K_VALUES);
                        setState(1068);
                        match(OPEN_PAR);
                        setState(1069);
                        expr(0);
                        setState(1074);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1070);
                                    match(COMMA);
                                    setState(1071);
                                    expr(0);
                                }
                            }
                            setState(1076);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(1077);
                        match(CLOSE_PAR);
                        setState(1092);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1078);
                                    match(COMMA);
                                    setState(1079);
                                    match(OPEN_PAR);
                                    setState(1080);
                                    expr(0);
                                    setState(1085);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    while (_la == COMMA) {
                                        {
                                            {
                                                setState(1081);
                                                match(COMMA);
                                                setState(1082);
                                                expr(0);
                                            }
                                        }
                                        setState(1087);
                                        _errHandler.sync(this);
                                        _la = _input.LA(1);
                                    }
                                    setState(1088);
                                    match(CLOSE_PAR);
                                }
                            }
                            setState(1094);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                    break;
                    case 2: {
                        setState(1095);
                        select_stmt();
                    }
                    break;
                    case 3: {
                        setState(1096);
                        match(K_DEFAULT);
                        setState(1097);
                        match(K_VALUES);
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Pragma_stmtContext pragma_stmt() throws RecognitionException {
        Pragma_stmtContext _localctx = new Pragma_stmtContext(_ctx, getState());
        enterRule(_localctx, 84, RULE_pragma_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1100);
                match(K_PRAGMA);
                setState(1104);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 147, _ctx)) {
                    case 1: {
                        setState(1101);
                        database_name();
                        setState(1102);
                        match(DOT);
                    }
                    break;
                }
                setState(1106);
                pragma_name();
                setState(1113);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case ASSIGN: {
                        setState(1107);
                        match(ASSIGN);
                        setState(1108);
                        pragma_value();
                    }
                    break;
                    case OPEN_PAR: {
                        setState(1109);
                        match(OPEN_PAR);
                        setState(1110);
                        pragma_value();
                        setState(1111);
                        match(CLOSE_PAR);
                    }
                    break;
                    case EOF:
                    case SCOL:
                    case K_ALTER:
                    case K_ANALYZE:
                    case K_ATTACH:
                    case K_BEGIN:
                    case K_COMMIT:
                    case K_CREATE:
                    case K_DELETE:
                    case K_DETACH:
                    case K_DROP:
                    case K_END:
                    case K_EXECUTE:
                    case K_EXPLAIN:
                    case K_INSERT:
                    case K_PRAGMA:
                    case K_REINDEX:
                    case K_RELEASE:
                    case K_REPLACE:
                    case K_ROLLBACK:
                    case K_SAVEPOINT:
                    case K_SELECT:
                    case K_UPDATE:
                    case K_VACUUM:
                    case K_VALUES:
                    case K_WITH:
                    case UNEXPECTED_CHAR:
                        break;
                    default:
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Reindex_stmtContext reindex_stmt() throws RecognitionException {
        Reindex_stmtContext _localctx = new Reindex_stmtContext(_ctx, getState());
        enterRule(_localctx, 86, RULE_reindex_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1115);
                match(K_REINDEX);
                setState(1126);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 151, _ctx)) {
                    case 1: {
                        setState(1116);
                        collation_name();
                    }
                    break;
                    case 2: {
                        setState(1120);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 149, _ctx)) {
                            case 1: {
                                setState(1117);
                                database_name();
                                setState(1118);
                                match(DOT);
                            }
                            break;
                        }
                        setState(1124);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 150, _ctx)) {
                            case 1: {
                                setState(1122);
                                table_name();
                            }
                            break;
                            case 2: {
                                setState(1123);
                                index_name();
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Release_stmtContext release_stmt() throws RecognitionException {
        Release_stmtContext _localctx = new Release_stmtContext(_ctx, getState());
        enterRule(_localctx, 88, RULE_release_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1128);
                match(K_RELEASE);
                setState(1130);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 152, _ctx)) {
                    case 1: {
                        setState(1129);
                        match(K_SAVEPOINT);
                    }
                    break;
                }
                setState(1132);
                savepoint_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Rollback_stmtContext rollback_stmt() throws RecognitionException {
        Rollback_stmtContext _localctx = new Rollback_stmtContext(_ctx, getState());
        enterRule(_localctx, 90, RULE_rollback_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1134);
                match(K_ROLLBACK);
                setState(1139);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_TRANSACTION) {
                    {
                        setState(1135);
                        match(K_TRANSACTION);
                        setState(1137);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 153, _ctx)) {
                            case 1: {
                                setState(1136);
                                transaction_name();
                            }
                            break;
                        }
                    }
                }

                setState(1146);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_TO) {
                    {
                        setState(1141);
                        match(K_TO);
                        setState(1143);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 155, _ctx)) {
                            case 1: {
                                setState(1142);
                                match(K_SAVEPOINT);
                            }
                            break;
                        }
                        setState(1145);
                        savepoint_name();
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Savepoint_stmtContext savepoint_stmt() throws RecognitionException {
        Savepoint_stmtContext _localctx = new Savepoint_stmtContext(_ctx, getState());
        enterRule(_localctx, 92, RULE_savepoint_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1148);
                match(K_SAVEPOINT);
                setState(1149);
                savepoint_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Simple_select_stmtContext simple_select_stmt() throws RecognitionException {
        Simple_select_stmtContext _localctx = new Simple_select_stmtContext(_ctx, getState());
        enterRule(_localctx, 94, RULE_simple_select_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1163);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WITH) {
                    {
                        setState(1151);
                        match(K_WITH);
                        setState(1153);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 157, _ctx)) {
                            case 1: {
                                setState(1152);
                                match(K_RECURSIVE);
                            }
                            break;
                        }
                        setState(1155);
                        common_table_expression();
                        setState(1160);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1156);
                                    match(COMMA);
                                    setState(1157);
                                    common_table_expression();
                                }
                            }
                            setState(1162);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(1165);
                select_core();
                setState(1176);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_ORDER) {
                    {
                        setState(1166);
                        match(K_ORDER);
                        setState(1167);
                        match(K_BY);
                        setState(1168);
                        ordering_term();
                        setState(1173);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1169);
                                    match(COMMA);
                                    setState(1170);
                                    ordering_term();
                                }
                            }
                            setState(1175);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(1184);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_LIMIT) {
                    {
                        setState(1178);
                        match(K_LIMIT);
                        setState(1179);
                        expr(0);
                        setState(1182);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == COMMA || _la == K_OFFSET) {
                            {
                                setState(1180);
                                _la = _input.LA(1);
                                if (!(_la == COMMA || _la == K_OFFSET)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(1181);
                                expr(0);
                            }
                        }

                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Select_stmtContext select_stmt() throws RecognitionException {
        Select_stmtContext _localctx = new Select_stmtContext(_ctx, getState());
        enterRule(_localctx, 96, RULE_select_stmt);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1198);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WITH) {
                    {
                        setState(1186);
                        match(K_WITH);
                        setState(1188);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 164, _ctx)) {
                            case 1: {
                                setState(1187);
                                match(K_RECURSIVE);
                            }
                            break;
                        }
                        setState(1190);
                        common_table_expression();
                        setState(1195);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1191);
                                    match(COMMA);
                                    setState(1192);
                                    common_table_expression();
                                }
                            }
                            setState(1197);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                    }
                }

                setState(1200);
                select_or_values();
                setState(1206);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 167, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1201);
                                compound_operator();
                                setState(1202);
                                select_or_values();
                            }
                        }
                    }
                    setState(1208);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 167, _ctx);
                }
                setState(1219);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 169, _ctx)) {
                    case 1: {
                        setState(1209);
                        match(K_ORDER);
                        setState(1210);
                        match(K_BY);
                        setState(1211);
                        ordering_term();
                        setState(1216);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 168, _ctx);
                        while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                            if (_alt == 1) {
                                {
                                    {
                                        setState(1212);
                                        match(COMMA);
                                        setState(1213);
                                        ordering_term();
                                    }
                                }
                            }
                            setState(1218);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 168, _ctx);
                        }
                    }
                    break;
                }
                setState(1227);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 171, _ctx)) {
                    case 1: {
                        setState(1221);
                        match(K_LIMIT);
                        setState(1222);
                        expr(0);
                        setState(1225);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 170, _ctx)) {
                            case 1: {
                                setState(1223);
                                _la = _input.LA(1);
                                if (!(_la == COMMA || _la == K_OFFSET)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(1224);
                                expr(0);
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Select_or_valuesContext select_or_values() throws RecognitionException {
        Select_or_valuesContext _localctx = new Select_or_valuesContext(_ctx, getState());
        enterRule(_localctx, 98, RULE_select_or_values);
        int _la;
        try {
            int _alt;
            setState(1303);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case K_SELECT:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(1229);
                    match(K_SELECT);
                    setState(1231);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 172, _ctx)) {
                        case 1: {
                            setState(1230);
                            _la = _input.LA(1);
                            if (!(_la == K_ALL || _la == K_DISTINCT)) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                        }
                        break;
                    }
                    setState(1233);
                    result_column();
                    setState(1238);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 173, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(1234);
                                    match(COMMA);
                                    setState(1235);
                                    result_column();
                                }
                            }
                        }
                        setState(1240);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 173, _ctx);
                    }
                    setState(1253);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 176, _ctx)) {
                        case 1: {
                            setState(1241);
                            match(K_FROM);
                            setState(1251);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 175, _ctx)) {
                                case 1: {
                                    setState(1242);
                                    table_or_subquery();
                                    setState(1247);
                                    _errHandler.sync(this);
                                    _alt = getInterpreter().adaptivePredict(_input, 174, _ctx);
                                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                        if (_alt == 1) {
                                            {
                                                {
                                                    setState(1243);
                                                    match(COMMA);
                                                    setState(1244);
                                                    table_or_subquery();
                                                }
                                            }
                                        }
                                        setState(1249);
                                        _errHandler.sync(this);
                                        _alt = getInterpreter().adaptivePredict(_input, 174, _ctx);
                                    }
                                }
                                break;
                                case 2: {
                                    setState(1250);
                                    join_clause();
                                }
                                break;
                            }
                        }
                        break;
                    }
                    setState(1257);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 177, _ctx)) {
                        case 1: {
                            setState(1255);
                            match(K_WHERE);
                            setState(1256);
                            expr(0);
                        }
                        break;
                    }
                    setState(1273);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 180, _ctx)) {
                        case 1: {
                            setState(1259);
                            match(K_GROUP);
                            setState(1260);
                            match(K_BY);
                            setState(1261);
                            expr(0);
                            setState(1266);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 178, _ctx);
                            while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                                if (_alt == 1) {
                                    {
                                        {
                                            setState(1262);
                                            match(COMMA);
                                            setState(1263);
                                            expr(0);
                                        }
                                    }
                                }
                                setState(1268);
                                _errHandler.sync(this);
                                _alt = getInterpreter().adaptivePredict(_input, 178, _ctx);
                            }
                            setState(1271);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 179, _ctx)) {
                                case 1: {
                                    setState(1269);
                                    match(K_HAVING);
                                    setState(1270);
                                    expr(0);
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
                break;
                case K_VALUES:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(1275);
                    match(K_VALUES);
                    setState(1276);
                    match(OPEN_PAR);
                    setState(1277);
                    expr(0);
                    setState(1282);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(1278);
                                match(COMMA);
                                setState(1279);
                                expr(0);
                            }
                        }
                        setState(1284);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(1285);
                    match(CLOSE_PAR);
                    setState(1300);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 183, _ctx);
                    while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                        if (_alt == 1) {
                            {
                                {
                                    setState(1286);
                                    match(COMMA);
                                    setState(1287);
                                    match(OPEN_PAR);
                                    setState(1288);
                                    expr(0);
                                    setState(1293);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    while (_la == COMMA) {
                                        {
                                            {
                                                setState(1289);
                                                match(COMMA);
                                                setState(1290);
                                                expr(0);
                                            }
                                        }
                                        setState(1295);
                                        _errHandler.sync(this);
                                        _la = _input.LA(1);
                                    }
                                    setState(1296);
                                    match(CLOSE_PAR);
                                }
                            }
                        }
                        setState(1302);
                        _errHandler.sync(this);
                        _alt = getInterpreter().adaptivePredict(_input, 183, _ctx);
                    }
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Update_stmtContext update_stmt() throws RecognitionException {
        Update_stmtContext _localctx = new Update_stmtContext(_ctx, getState());
        enterRule(_localctx, 100, RULE_update_stmt);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1306);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WITH) {
                    {
                        setState(1305);
                        with_clause();
                    }
                }

                setState(1308);
                match(K_UPDATE);
                setState(1319);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 186, _ctx)) {
                    case 1: {
                        setState(1309);
                        match(K_OR);
                        setState(1310);
                        match(K_ROLLBACK);
                    }
                    break;
                    case 2: {
                        setState(1311);
                        match(K_OR);
                        setState(1312);
                        match(K_ABORT);
                    }
                    break;
                    case 3: {
                        setState(1313);
                        match(K_OR);
                        setState(1314);
                        match(K_REPLACE);
                    }
                    break;
                    case 4: {
                        setState(1315);
                        match(K_OR);
                        setState(1316);
                        match(K_FAIL);
                    }
                    break;
                    case 5: {
                        setState(1317);
                        match(K_OR);
                        setState(1318);
                        match(K_IGNORE);
                    }
                    break;
                }
                setState(1321);
                qualified_table_name();
                setState(1322);
                match(K_SET);
                setState(1323);
                column_name();
                setState(1324);
                match(ASSIGN);
                setState(1325);
                expr(0);
                setState(1333);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(1326);
                            match(COMMA);
                            setState(1327);
                            column_name();
                            setState(1328);
                            match(ASSIGN);
                            setState(1329);
                            expr(0);
                        }
                    }
                    setState(1335);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(1338);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WHERE) {
                    {
                        setState(1336);
                        match(K_WHERE);
                        setState(1337);
                        expr(0);
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Update_stmt_limitedContext update_stmt_limited() throws RecognitionException {
        Update_stmt_limitedContext _localctx = new Update_stmt_limitedContext(_ctx, getState());
        enterRule(_localctx, 102, RULE_update_stmt_limited);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1341);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WITH) {
                    {
                        setState(1340);
                        with_clause();
                    }
                }

                setState(1343);
                match(K_UPDATE);
                setState(1354);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 190, _ctx)) {
                    case 1: {
                        setState(1344);
                        match(K_OR);
                        setState(1345);
                        match(K_ROLLBACK);
                    }
                    break;
                    case 2: {
                        setState(1346);
                        match(K_OR);
                        setState(1347);
                        match(K_ABORT);
                    }
                    break;
                    case 3: {
                        setState(1348);
                        match(K_OR);
                        setState(1349);
                        match(K_REPLACE);
                    }
                    break;
                    case 4: {
                        setState(1350);
                        match(K_OR);
                        setState(1351);
                        match(K_FAIL);
                    }
                    break;
                    case 5: {
                        setState(1352);
                        match(K_OR);
                        setState(1353);
                        match(K_IGNORE);
                    }
                    break;
                }
                setState(1356);
                qualified_table_name();
                setState(1357);
                match(K_SET);
                setState(1358);
                column_name();
                setState(1359);
                match(ASSIGN);
                setState(1360);
                expr(0);
                setState(1368);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(1361);
                            match(COMMA);
                            setState(1362);
                            column_name();
                            setState(1363);
                            match(ASSIGN);
                            setState(1364);
                            expr(0);
                        }
                    }
                    setState(1370);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(1373);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_WHERE) {
                    {
                        setState(1371);
                        match(K_WHERE);
                        setState(1372);
                        expr(0);
                    }
                }

                setState(1393);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_LIMIT || _la == K_ORDER) {
                    {
                        setState(1385);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == K_ORDER) {
                            {
                                setState(1375);
                                match(K_ORDER);
                                setState(1376);
                                match(K_BY);
                                setState(1377);
                                ordering_term();
                                setState(1382);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(1378);
                                            match(COMMA);
                                            setState(1379);
                                            ordering_term();
                                        }
                                    }
                                    setState(1384);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                        }

                        setState(1387);
                        match(K_LIMIT);
                        setState(1388);
                        expr(0);
                        setState(1391);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == COMMA || _la == K_OFFSET) {
                            {
                                setState(1389);
                                _la = _input.LA(1);
                                if (!(_la == COMMA || _la == K_OFFSET)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                                setState(1390);
                                expr(0);
                            }
                        }

                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Vacuum_stmtContext vacuum_stmt() throws RecognitionException {
        Vacuum_stmtContext _localctx = new Vacuum_stmtContext(_ctx, getState());
        enterRule(_localctx, 104, RULE_vacuum_stmt);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1395);
                match(K_VACUUM);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_defContext column_def() throws RecognitionException {
        Column_defContext _localctx = new Column_defContext(_ctx, getState());
        enterRule(_localctx, 106, RULE_column_def);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1397);
                column_name();
                setState(1402);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 198, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            setState(1400);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 197, _ctx)) {
                                case 1: {
                                    setState(1398);
                                    column_constraint();
                                }
                                break;
                                case 2: {
                                    setState(1399);
                                    type_name();
                                }
                                break;
                            }
                        }
                    }
                    setState(1404);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 198, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Type_nameContext type_name() throws RecognitionException {
        Type_nameContext _localctx = new Type_nameContext(_ctx, getState());
        enterRule(_localctx, 108, RULE_type_name);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1405);
                name();
                setState(1425);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 202, _ctx)) {
                    case 1: {
                        setState(1406);
                        match(OPEN_PAR);
                        setState(1407);
                        signed_number();
                        setState(1409);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (((((_la - 33)) & ~0x3f) == 0 && ((1L << (_la - 33)) & ((1L << (OPEN_PAR - 33)) | (1L << (K_ABORT - 33)) | (1L << (K_ACTION - 33)) | (1L << (K_ADD - 33)) | (1L << (K_AFTER - 33)) | (1L << (K_ALL - 33)) | (1L << (K_ALTER - 33)) | (1L << (K_ANALYZE - 33)) | (1L << (K_AND - 33)) | (1L << (K_AS - 33)) | (1L << (K_ASC - 33)) | (1L << (K_ATTACH - 33)) | (1L << (K_AUTOINCREMENT - 33)) | (1L << (K_BEFORE - 33)) | (1L << (K_BEGIN - 33)) | (1L << (K_BETWEEN - 33)) | (1L << (K_BY - 33)) | (1L << (K_CASCADE - 33)) | (1L << (K_CASE - 33)) | (1L << (K_CAST - 33)) | (1L << (K_CHECK - 33)) | (1L << (K_COLLATE - 33)) | (1L << (K_COLUMN - 33)) | (1L << (K_COMMIT - 33)) | (1L << (K_CONFLICT - 33)) | (1L << (K_CONSTRAINT - 33)) | (1L << (K_CREATE - 33)) | (1L << (K_CROSS - 33)) | (1L << (K_CURRENT_DATE - 33)) | (1L << (K_CURRENT_TIME - 33)) | (1L << (K_CURRENT_TIMESTAMP - 33)) | (1L << (K_DATABASE - 33)) | (1L << (K_DEFAULT - 33)) | (1L << (K_DEFERRABLE - 33)) | (1L << (K_DEFERRED - 33)) | (1L << (K_DELETE - 33)) | (1L << (K_DESC - 33)) | (1L << (K_DETACH - 33)))) != 0) || ((((_la - 97)) & ~0x3f) == 0 && ((1L << (_la - 97)) & ((1L << (K_DISTINCT - 97)) | (1L << (K_DROP - 97)) | (1L << (K_EACH - 97)) | (1L << (K_ELSE - 97)) | (1L << (K_END - 97)) | (1L << (K_ENABLE - 97)) | (1L << (K_ESCAPE - 97)) | (1L << (K_EXCEPT - 97)) | (1L << (K_EXCLUSIVE - 97)) | (1L << (K_EXISTS - 97)) | (1L << (K_EXPLAIN - 97)) | (1L << (K_FAIL - 97)) | (1L << (K_FOR - 97)) | (1L << (K_FOREIGN - 97)) | (1L << (K_FROM - 97)) | (1L << (K_FULL - 97)) | (1L << (K_GLOB - 97)) | (1L << (K_GROUP - 97)) | (1L << (K_HAVING - 97)) | (1L << (K_IF - 97)) | (1L << (K_IGNORE - 97)) | (1L << (K_IMMEDIATE - 97)) | (1L << (K_IN - 97)) | (1L << (K_INDEX - 97)) | (1L << (K_INDEXED - 97)) | (1L << (K_INITIALLY - 97)) | (1L << (K_INNER - 97)) | (1L << (K_INSERT - 97)) | (1L << (K_INSTEAD - 97)) | (1L << (K_INTERSECT - 97)) | (1L << (K_INTO - 97)) | (1L << (K_IS - 97)) | (1L << (K_ISNULL - 97)) | (1L << (K_JOIN - 97)) | (1L << (K_KEY - 97)) | (1L << (K_LEFT - 97)) | (1L << (K_LIKE - 97)) | (1L << (K_LIMIT - 97)) | (1L << (K_MATCH - 97)) | (1L << (K_NATURAL - 97)) | (1L << (K_NEXTVAL - 97)) | (1L << (K_NO - 97)) | (1L << (K_NOT - 97)) | (1L << (K_NOTNULL - 97)) | (1L << (K_NULL - 97)) | (1L << (K_OF - 97)) | (1L << (K_OFFSET - 97)) | (1L << (K_ON - 97)) | (1L << (K_OR - 97)) | (1L << (K_ORDER - 97)) | (1L << (K_OUTER - 97)) | (1L << (K_PLAN - 97)) | (1L << (K_PRAGMA - 97)) | (1L << (K_PRIMARY - 97)) | (1L << (K_QUERY - 97)) | (1L << (K_RAISE - 97)) | (1L << (K_RECURSIVE - 97)) | (1L << (K_REFERENCES - 97)) | (1L << (K_REGEXP - 97)) | (1L << (K_REINDEX - 97)))) != 0) || ((((_la - 161)) & ~0x3f) == 0 && ((1L << (_la - 161)) & ((1L << (K_RELEASE - 161)) | (1L << (K_RENAME - 161)) | (1L << (K_REPLACE - 161)) | (1L << (K_RESTRICT - 161)) | (1L << (K_RIGHT - 161)) | (1L << (K_ROLLBACK - 161)) | (1L << (K_ROW - 161)) | (1L << (K_SAVEPOINT - 161)) | (1L << (K_SELECT - 161)) | (1L << (K_SET - 161)) | (1L << (K_TABLE - 161)) | (1L << (K_TEMP - 161)) | (1L << (K_TEMPORARY - 161)) | (1L << (K_THEN - 161)) | (1L << (K_TO - 161)) | (1L << (K_TRANSACTION - 161)) | (1L << (K_TRIGGER - 161)) | (1L << (K_UNION - 161)) | (1L << (K_UNIQUE - 161)) | (1L << (K_UPDATE - 161)) | (1L << (K_USING - 161)) | (1L << (K_VACUUM - 161)) | (1L << (K_VALUES - 161)) | (1L << (K_VIEW - 161)) | (1L << (K_VIRTUAL - 161)) | (1L << (K_WHEN - 161)) | (1L << (K_WHERE - 161)) | (1L << (K_WITH - 161)) | (1L << (K_WITHOUT - 161)) | (1L << (IDENTIFIER - 161)) | (1L << (STRING_LITERAL - 161)))) != 0)) {
                            {
                                setState(1408);
                                any_name();
                            }
                        }

                        setState(1411);
                        match(CLOSE_PAR);
                    }
                    break;
                    case 2: {
                        setState(1413);
                        match(OPEN_PAR);
                        setState(1414);
                        signed_number();
                        setState(1416);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (((((_la - 33)) & ~0x3f) == 0 && ((1L << (_la - 33)) & ((1L << (OPEN_PAR - 33)) | (1L << (K_ABORT - 33)) | (1L << (K_ACTION - 33)) | (1L << (K_ADD - 33)) | (1L << (K_AFTER - 33)) | (1L << (K_ALL - 33)) | (1L << (K_ALTER - 33)) | (1L << (K_ANALYZE - 33)) | (1L << (K_AND - 33)) | (1L << (K_AS - 33)) | (1L << (K_ASC - 33)) | (1L << (K_ATTACH - 33)) | (1L << (K_AUTOINCREMENT - 33)) | (1L << (K_BEFORE - 33)) | (1L << (K_BEGIN - 33)) | (1L << (K_BETWEEN - 33)) | (1L << (K_BY - 33)) | (1L << (K_CASCADE - 33)) | (1L << (K_CASE - 33)) | (1L << (K_CAST - 33)) | (1L << (K_CHECK - 33)) | (1L << (K_COLLATE - 33)) | (1L << (K_COLUMN - 33)) | (1L << (K_COMMIT - 33)) | (1L << (K_CONFLICT - 33)) | (1L << (K_CONSTRAINT - 33)) | (1L << (K_CREATE - 33)) | (1L << (K_CROSS - 33)) | (1L << (K_CURRENT_DATE - 33)) | (1L << (K_CURRENT_TIME - 33)) | (1L << (K_CURRENT_TIMESTAMP - 33)) | (1L << (K_DATABASE - 33)) | (1L << (K_DEFAULT - 33)) | (1L << (K_DEFERRABLE - 33)) | (1L << (K_DEFERRED - 33)) | (1L << (K_DELETE - 33)) | (1L << (K_DESC - 33)) | (1L << (K_DETACH - 33)))) != 0) || ((((_la - 97)) & ~0x3f) == 0 && ((1L << (_la - 97)) & ((1L << (K_DISTINCT - 97)) | (1L << (K_DROP - 97)) | (1L << (K_EACH - 97)) | (1L << (K_ELSE - 97)) | (1L << (K_END - 97)) | (1L << (K_ENABLE - 97)) | (1L << (K_ESCAPE - 97)) | (1L << (K_EXCEPT - 97)) | (1L << (K_EXCLUSIVE - 97)) | (1L << (K_EXISTS - 97)) | (1L << (K_EXPLAIN - 97)) | (1L << (K_FAIL - 97)) | (1L << (K_FOR - 97)) | (1L << (K_FOREIGN - 97)) | (1L << (K_FROM - 97)) | (1L << (K_FULL - 97)) | (1L << (K_GLOB - 97)) | (1L << (K_GROUP - 97)) | (1L << (K_HAVING - 97)) | (1L << (K_IF - 97)) | (1L << (K_IGNORE - 97)) | (1L << (K_IMMEDIATE - 97)) | (1L << (K_IN - 97)) | (1L << (K_INDEX - 97)) | (1L << (K_INDEXED - 97)) | (1L << (K_INITIALLY - 97)) | (1L << (K_INNER - 97)) | (1L << (K_INSERT - 97)) | (1L << (K_INSTEAD - 97)) | (1L << (K_INTERSECT - 97)) | (1L << (K_INTO - 97)) | (1L << (K_IS - 97)) | (1L << (K_ISNULL - 97)) | (1L << (K_JOIN - 97)) | (1L << (K_KEY - 97)) | (1L << (K_LEFT - 97)) | (1L << (K_LIKE - 97)) | (1L << (K_LIMIT - 97)) | (1L << (K_MATCH - 97)) | (1L << (K_NATURAL - 97)) | (1L << (K_NEXTVAL - 97)) | (1L << (K_NO - 97)) | (1L << (K_NOT - 97)) | (1L << (K_NOTNULL - 97)) | (1L << (K_NULL - 97)) | (1L << (K_OF - 97)) | (1L << (K_OFFSET - 97)) | (1L << (K_ON - 97)) | (1L << (K_OR - 97)) | (1L << (K_ORDER - 97)) | (1L << (K_OUTER - 97)) | (1L << (K_PLAN - 97)) | (1L << (K_PRAGMA - 97)) | (1L << (K_PRIMARY - 97)) | (1L << (K_QUERY - 97)) | (1L << (K_RAISE - 97)) | (1L << (K_RECURSIVE - 97)) | (1L << (K_REFERENCES - 97)) | (1L << (K_REGEXP - 97)) | (1L << (K_REINDEX - 97)))) != 0) || ((((_la - 161)) & ~0x3f) == 0 && ((1L << (_la - 161)) & ((1L << (K_RELEASE - 161)) | (1L << (K_RENAME - 161)) | (1L << (K_REPLACE - 161)) | (1L << (K_RESTRICT - 161)) | (1L << (K_RIGHT - 161)) | (1L << (K_ROLLBACK - 161)) | (1L << (K_ROW - 161)) | (1L << (K_SAVEPOINT - 161)) | (1L << (K_SELECT - 161)) | (1L << (K_SET - 161)) | (1L << (K_TABLE - 161)) | (1L << (K_TEMP - 161)) | (1L << (K_TEMPORARY - 161)) | (1L << (K_THEN - 161)) | (1L << (K_TO - 161)) | (1L << (K_TRANSACTION - 161)) | (1L << (K_TRIGGER - 161)) | (1L << (K_UNION - 161)) | (1L << (K_UNIQUE - 161)) | (1L << (K_UPDATE - 161)) | (1L << (K_USING - 161)) | (1L << (K_VACUUM - 161)) | (1L << (K_VALUES - 161)) | (1L << (K_VIEW - 161)) | (1L << (K_VIRTUAL - 161)) | (1L << (K_WHEN - 161)) | (1L << (K_WHERE - 161)) | (1L << (K_WITH - 161)) | (1L << (K_WITHOUT - 161)) | (1L << (IDENTIFIER - 161)) | (1L << (STRING_LITERAL - 161)))) != 0)) {
                            {
                                setState(1415);
                                any_name();
                            }
                        }

                        setState(1418);
                        match(COMMA);
                        setState(1419);
                        signed_number();
                        setState(1421);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (((((_la - 33)) & ~0x3f) == 0 && ((1L << (_la - 33)) & ((1L << (OPEN_PAR - 33)) | (1L << (K_ABORT - 33)) | (1L << (K_ACTION - 33)) | (1L << (K_ADD - 33)) | (1L << (K_AFTER - 33)) | (1L << (K_ALL - 33)) | (1L << (K_ALTER - 33)) | (1L << (K_ANALYZE - 33)) | (1L << (K_AND - 33)) | (1L << (K_AS - 33)) | (1L << (K_ASC - 33)) | (1L << (K_ATTACH - 33)) | (1L << (K_AUTOINCREMENT - 33)) | (1L << (K_BEFORE - 33)) | (1L << (K_BEGIN - 33)) | (1L << (K_BETWEEN - 33)) | (1L << (K_BY - 33)) | (1L << (K_CASCADE - 33)) | (1L << (K_CASE - 33)) | (1L << (K_CAST - 33)) | (1L << (K_CHECK - 33)) | (1L << (K_COLLATE - 33)) | (1L << (K_COLUMN - 33)) | (1L << (K_COMMIT - 33)) | (1L << (K_CONFLICT - 33)) | (1L << (K_CONSTRAINT - 33)) | (1L << (K_CREATE - 33)) | (1L << (K_CROSS - 33)) | (1L << (K_CURRENT_DATE - 33)) | (1L << (K_CURRENT_TIME - 33)) | (1L << (K_CURRENT_TIMESTAMP - 33)) | (1L << (K_DATABASE - 33)) | (1L << (K_DEFAULT - 33)) | (1L << (K_DEFERRABLE - 33)) | (1L << (K_DEFERRED - 33)) | (1L << (K_DELETE - 33)) | (1L << (K_DESC - 33)) | (1L << (K_DETACH - 33)))) != 0) || ((((_la - 97)) & ~0x3f) == 0 && ((1L << (_la - 97)) & ((1L << (K_DISTINCT - 97)) | (1L << (K_DROP - 97)) | (1L << (K_EACH - 97)) | (1L << (K_ELSE - 97)) | (1L << (K_END - 97)) | (1L << (K_ENABLE - 97)) | (1L << (K_ESCAPE - 97)) | (1L << (K_EXCEPT - 97)) | (1L << (K_EXCLUSIVE - 97)) | (1L << (K_EXISTS - 97)) | (1L << (K_EXPLAIN - 97)) | (1L << (K_FAIL - 97)) | (1L << (K_FOR - 97)) | (1L << (K_FOREIGN - 97)) | (1L << (K_FROM - 97)) | (1L << (K_FULL - 97)) | (1L << (K_GLOB - 97)) | (1L << (K_GROUP - 97)) | (1L << (K_HAVING - 97)) | (1L << (K_IF - 97)) | (1L << (K_IGNORE - 97)) | (1L << (K_IMMEDIATE - 97)) | (1L << (K_IN - 97)) | (1L << (K_INDEX - 97)) | (1L << (K_INDEXED - 97)) | (1L << (K_INITIALLY - 97)) | (1L << (K_INNER - 97)) | (1L << (K_INSERT - 97)) | (1L << (K_INSTEAD - 97)) | (1L << (K_INTERSECT - 97)) | (1L << (K_INTO - 97)) | (1L << (K_IS - 97)) | (1L << (K_ISNULL - 97)) | (1L << (K_JOIN - 97)) | (1L << (K_KEY - 97)) | (1L << (K_LEFT - 97)) | (1L << (K_LIKE - 97)) | (1L << (K_LIMIT - 97)) | (1L << (K_MATCH - 97)) | (1L << (K_NATURAL - 97)) | (1L << (K_NEXTVAL - 97)) | (1L << (K_NO - 97)) | (1L << (K_NOT - 97)) | (1L << (K_NOTNULL - 97)) | (1L << (K_NULL - 97)) | (1L << (K_OF - 97)) | (1L << (K_OFFSET - 97)) | (1L << (K_ON - 97)) | (1L << (K_OR - 97)) | (1L << (K_ORDER - 97)) | (1L << (K_OUTER - 97)) | (1L << (K_PLAN - 97)) | (1L << (K_PRAGMA - 97)) | (1L << (K_PRIMARY - 97)) | (1L << (K_QUERY - 97)) | (1L << (K_RAISE - 97)) | (1L << (K_RECURSIVE - 97)) | (1L << (K_REFERENCES - 97)) | (1L << (K_REGEXP - 97)) | (1L << (K_REINDEX - 97)))) != 0) || ((((_la - 161)) & ~0x3f) == 0 && ((1L << (_la - 161)) & ((1L << (K_RELEASE - 161)) | (1L << (K_RENAME - 161)) | (1L << (K_REPLACE - 161)) | (1L << (K_RESTRICT - 161)) | (1L << (K_RIGHT - 161)) | (1L << (K_ROLLBACK - 161)) | (1L << (K_ROW - 161)) | (1L << (K_SAVEPOINT - 161)) | (1L << (K_SELECT - 161)) | (1L << (K_SET - 161)) | (1L << (K_TABLE - 161)) | (1L << (K_TEMP - 161)) | (1L << (K_TEMPORARY - 161)) | (1L << (K_THEN - 161)) | (1L << (K_TO - 161)) | (1L << (K_TRANSACTION - 161)) | (1L << (K_TRIGGER - 161)) | (1L << (K_UNION - 161)) | (1L << (K_UNIQUE - 161)) | (1L << (K_UPDATE - 161)) | (1L << (K_USING - 161)) | (1L << (K_VACUUM - 161)) | (1L << (K_VALUES - 161)) | (1L << (K_VIEW - 161)) | (1L << (K_VIRTUAL - 161)) | (1L << (K_WHEN - 161)) | (1L << (K_WHERE - 161)) | (1L << (K_WITH - 161)) | (1L << (K_WITHOUT - 161)) | (1L << (IDENTIFIER - 161)) | (1L << (STRING_LITERAL - 161)))) != 0)) {
                            {
                                setState(1420);
                                any_name();
                            }
                        }

                        setState(1423);
                        match(CLOSE_PAR);
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_constraintContext column_constraint() throws RecognitionException {
        Column_constraintContext _localctx = new Column_constraintContext(_ctx, getState());
        enterRule(_localctx, 110, RULE_column_constraint);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1429);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_CONSTRAINT) {
                    {
                        setState(1427);
                        match(K_CONSTRAINT);
                        setState(1428);
                        name();
                    }
                }

                setState(1445);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case K_PRIMARY: {
                        setState(1431);
                        column_constraint_primary_key();
                    }
                    break;
                    case K_REFERENCES: {
                        setState(1432);
                        column_constraint_foreign_key();
                    }
                    break;
                    case K_NOT: {
                        setState(1433);
                        column_constraint_not_null();
                    }
                    break;
                    case K_NULL: {
                        setState(1434);
                        column_constraint_null();
                    }
                    break;
                    case K_UNIQUE: {
                        setState(1435);
                        match(K_UNIQUE);
                        setState(1436);
                        conflict_clause();
                    }
                    break;
                    case K_CHECK: {
                        setState(1437);
                        match(K_CHECK);
                        setState(1438);
                        match(OPEN_PAR);
                        setState(1439);
                        expr(0);
                        setState(1440);
                        match(CLOSE_PAR);
                    }
                    break;
                    case K_DEFAULT: {
                        setState(1442);
                        column_default();
                    }
                    break;
                    case K_COLLATE: {
                        setState(1443);
                        match(K_COLLATE);
                        setState(1444);
                        collation_name();
                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_constraint_primary_keyContext column_constraint_primary_key() throws RecognitionException {
        Column_constraint_primary_keyContext _localctx = new Column_constraint_primary_keyContext(_ctx, getState());
        enterRule(_localctx, 112, RULE_column_constraint_primary_key);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1447);
                match(K_PRIMARY);
                setState(1448);
                match(K_KEY);
                setState(1450);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 205, _ctx)) {
                    case 1: {
                        setState(1449);
                        _la = _input.LA(1);
                        if (!(_la == K_ASC || _la == K_DESC)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                    }
                    break;
                }
                setState(1452);
                conflict_clause();
                setState(1454);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 206, _ctx)) {
                    case 1: {
                        setState(1453);
                        match(K_AUTOINCREMENT);
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_constraint_foreign_keyContext column_constraint_foreign_key() throws RecognitionException {
        Column_constraint_foreign_keyContext _localctx = new Column_constraint_foreign_keyContext(_ctx, getState());
        enterRule(_localctx, 114, RULE_column_constraint_foreign_key);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1456);
                foreign_key_clause();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_constraint_not_nullContext column_constraint_not_null() throws RecognitionException {
        Column_constraint_not_nullContext _localctx = new Column_constraint_not_nullContext(_ctx, getState());
        enterRule(_localctx, 116, RULE_column_constraint_not_null);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1458);
                match(K_NOT);
                setState(1459);
                match(K_NULL);
                setState(1460);
                conflict_clause();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_constraint_nullContext column_constraint_null() throws RecognitionException {
        Column_constraint_nullContext _localctx = new Column_constraint_nullContext(_ctx, getState());
        enterRule(_localctx, 118, RULE_column_constraint_null);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1462);
                match(K_NULL);
                setState(1463);
                conflict_clause();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_defaultContext column_default() throws RecognitionException {
        Column_defaultContext _localctx = new Column_defaultContext(_ctx, getState());
        enterRule(_localctx, 120, RULE_column_default);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1465);
                match(K_DEFAULT);
                setState(1477);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 207, _ctx)) {
                    case 1: {
                        setState(1466);
                        column_default_value();
                    }
                    break;
                    case 2: {
                        setState(1467);
                        match(OPEN_PAR);
                        setState(1468);
                        expr(0);
                        setState(1469);
                        match(CLOSE_PAR);
                    }
                    break;
                    case 3: {
                        setState(1471);
                        match(K_NEXTVAL);
                        setState(1472);
                        match(OPEN_PAR);
                        setState(1473);
                        expr(0);
                        setState(1474);
                        match(CLOSE_PAR);
                    }
                    break;
                    case 4: {
                        setState(1476);
                        any_name();
                    }
                    break;
                }
                setState(1485);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 209, _ctx)) {
                    case 1: {
                        setState(1479);
                        match(T__3);
                        setState(1481);
                        _errHandler.sync(this);
                        _alt = 1;
                        do {
                            switch (_alt) {
                                case 1: {
                                    {
                                        setState(1480);
                                        any_name();
                                    }
                                }
                                break;
                                default:
                                    throw new NoViableAltException(this);
                            }
                            setState(1483);
                            _errHandler.sync(this);
                            _alt = getInterpreter().adaptivePredict(_input, 208, _ctx);
                        } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_default_valueContext column_default_value() throws RecognitionException {
        Column_default_valueContext _localctx = new Column_default_valueContext(_ctx, getState());
        enterRule(_localctx, 122, RULE_column_default_value);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1489);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 210, _ctx)) {
                    case 1: {
                        setState(1487);
                        signed_number();
                    }
                    break;
                    case 2: {
                        setState(1488);
                        literal_value();
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Conflict_clauseContext conflict_clause() throws RecognitionException {
        Conflict_clauseContext _localctx = new Conflict_clauseContext(_ctx, getState());
        enterRule(_localctx, 124, RULE_conflict_clause);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1494);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 211, _ctx)) {
                    case 1: {
                        setState(1491);
                        match(K_ON);
                        setState(1492);
                        match(K_CONFLICT);
                        setState(1493);
                        _la = _input.LA(1);
                        if (!(_la == K_ABORT || ((((_la - 109)) & ~0x3f) == 0 && ((1L << (_la - 109)) & ((1L << (K_FAIL - 109)) | (1L << (K_IGNORE - 109)) | (1L << (K_REPLACE - 109)) | (1L << (K_ROLLBACK - 109)))) != 0))) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ExprContext expr() throws RecognitionException {
        return expr(0);
    }

    private ExprContext expr(int _p) throws RecognitionException {
        ParserRuleContext _parentctx = _ctx;
        int _parentState = getState();
        ExprContext _localctx = new ExprContext(_ctx, _parentState);
        ExprContext _prevctx = _localctx;
        int _startState = 126;
        enterRecursionRule(_localctx, 126, RULE_expr, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1572);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 222, _ctx)) {
                    case 1: {
                        setState(1497);
                        literal_value();
                    }
                    break;
                    case 2: {
                        setState(1498);
                        match(BIND_PARAMETER);
                    }
                    break;
                    case 3: {
                        setState(1507);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 213, _ctx)) {
                            case 1: {
                                setState(1502);
                                _errHandler.sync(this);
                                switch (getInterpreter().adaptivePredict(_input, 212, _ctx)) {
                                    case 1: {
                                        setState(1499);
                                        database_name();
                                        setState(1500);
                                        match(DOT);
                                    }
                                    break;
                                }
                                setState(1504);
                                table_name();
                                setState(1505);
                                match(DOT);
                            }
                            break;
                        }
                        setState(1509);
                        column_name();
                    }
                    break;
                    case 4: {
                        setState(1510);
                        unary_operator();
                        setState(1511);
                        expr(21);
                    }
                    break;
                    case 5: {
                        setState(1513);
                        function_name();
                        setState(1514);
                        match(OPEN_PAR);
                        setState(1527);
                        _errHandler.sync(this);
                        switch (_input.LA(1)) {
                            case OPEN_PAR:
                            case PLUS:
                            case MINUS:
                            case TILDE:
                            case K_ABORT:
                            case K_ACTION:
                            case K_ADD:
                            case K_AFTER:
                            case K_ALL:
                            case K_ALTER:
                            case K_ANALYZE:
                            case K_AND:
                            case K_AS:
                            case K_ASC:
                            case K_ATTACH:
                            case K_AUTOINCREMENT:
                            case K_BEFORE:
                            case K_BEGIN:
                            case K_BETWEEN:
                            case K_BY:
                            case K_CASCADE:
                            case K_CASE:
                            case K_CAST:
                            case K_CHECK:
                            case K_COLLATE:
                            case K_COLUMN:
                            case K_COMMIT:
                            case K_CONFLICT:
                            case K_CONSTRAINT:
                            case K_CREATE:
                            case K_CROSS:
                            case K_CURRENT_DATE:
                            case K_CURRENT_TIME:
                            case K_CURRENT_TIMESTAMP:
                            case K_DATABASE:
                            case K_DEFAULT:
                            case K_DEFERRABLE:
                            case K_DEFERRED:
                            case K_DELETE:
                            case K_DESC:
                            case K_DETACH:
                            case K_DISTINCT:
                            case K_DROP:
                            case K_EACH:
                            case K_ELSE:
                            case K_END:
                            case K_ENABLE:
                            case K_ESCAPE:
                            case K_EXCEPT:
                            case K_EXCLUSIVE:
                            case K_EXISTS:
                            case K_EXPLAIN:
                            case K_FAIL:
                            case K_FOR:
                            case K_FOREIGN:
                            case K_FROM:
                            case K_FULL:
                            case K_GLOB:
                            case K_GROUP:
                            case K_HAVING:
                            case K_IF:
                            case K_IGNORE:
                            case K_IMMEDIATE:
                            case K_IN:
                            case K_INDEX:
                            case K_INDEXED:
                            case K_INITIALLY:
                            case K_INNER:
                            case K_INSERT:
                            case K_INSTEAD:
                            case K_INTERSECT:
                            case K_INTO:
                            case K_IS:
                            case K_ISNULL:
                            case K_JOIN:
                            case K_KEY:
                            case K_LEFT:
                            case K_LIKE:
                            case K_LIMIT:
                            case K_MATCH:
                            case K_NATURAL:
                            case K_NEXTVAL:
                            case K_NO:
                            case K_NOT:
                            case K_NOTNULL:
                            case K_NULL:
                            case K_OF:
                            case K_OFFSET:
                            case K_ON:
                            case K_OR:
                            case K_ORDER:
                            case K_OUTER:
                            case K_PLAN:
                            case K_PRAGMA:
                            case K_PRIMARY:
                            case K_QUERY:
                            case K_RAISE:
                            case K_RECURSIVE:
                            case K_REFERENCES:
                            case K_REGEXP:
                            case K_REINDEX:
                            case K_RELEASE:
                            case K_RENAME:
                            case K_REPLACE:
                            case K_RESTRICT:
                            case K_RIGHT:
                            case K_ROLLBACK:
                            case K_ROW:
                            case K_SAVEPOINT:
                            case K_SELECT:
                            case K_SET:
                            case K_TABLE:
                            case K_TEMP:
                            case K_TEMPORARY:
                            case K_THEN:
                            case K_TO:
                            case K_TRANSACTION:
                            case K_TRIGGER:
                            case K_UNION:
                            case K_UNIQUE:
                            case K_UPDATE:
                            case K_USING:
                            case K_VACUUM:
                            case K_VALUES:
                            case K_VIEW:
                            case K_VIRTUAL:
                            case K_WHEN:
                            case K_WHERE:
                            case K_WITH:
                            case K_WITHOUT:
                            case IDENTIFIER:
                            case NUMERIC_LITERAL:
                            case BIND_PARAMETER:
                            case STRING_LITERAL:
                            case BLOB_LITERAL: {
                                setState(1516);
                                _errHandler.sync(this);
                                switch (getInterpreter().adaptivePredict(_input, 214, _ctx)) {
                                    case 1: {
                                        setState(1515);
                                        match(K_DISTINCT);
                                    }
                                    break;
                                }
                                setState(1518);
                                expr(0);
                                setState(1523);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(1519);
                                            match(COMMA);
                                            setState(1520);
                                            expr(0);
                                        }
                                    }
                                    setState(1525);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                            break;
                            case STAR: {
                                setState(1526);
                                match(STAR);
                            }
                            break;
                            case CLOSE_PAR:
                                break;
                            default:
                                break;
                        }
                        setState(1529);
                        match(CLOSE_PAR);
                    }
                    break;
                    case 6: {
                        setState(1531);
                        match(OPEN_PAR);
                        setState(1532);
                        expr(0);
                        setState(1533);
                        match(CLOSE_PAR);
                    }
                    break;
                    case 7: {
                        setState(1535);
                        match(K_CAST);
                        setState(1536);
                        match(OPEN_PAR);
                        setState(1537);
                        expr(0);
                        setState(1538);
                        match(K_AS);
                        setState(1539);
                        type_name();
                        setState(1540);
                        match(CLOSE_PAR);
                    }
                    break;
                    case 8: {
                        setState(1546);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == K_EXISTS || _la == K_NOT) {
                            {
                                setState(1543);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                if (_la == K_NOT) {
                                    {
                                        setState(1542);
                                        match(K_NOT);
                                    }
                                }

                                setState(1545);
                                match(K_EXISTS);
                            }
                        }

                        setState(1548);
                        match(OPEN_PAR);
                        setState(1549);
                        select_stmt();
                        setState(1550);
                        match(CLOSE_PAR);
                    }
                    break;
                    case 9: {
                        setState(1552);
                        match(K_CASE);
                        setState(1554);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 219, _ctx)) {
                            case 1: {
                                setState(1553);
                                expr(0);
                            }
                            break;
                        }
                        setState(1561);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        do {
                            {
                                {
                                    setState(1556);
                                    match(K_WHEN);
                                    setState(1557);
                                    expr(0);
                                    setState(1558);
                                    match(K_THEN);
                                    setState(1559);
                                    expr(0);
                                }
                            }
                            setState(1563);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        } while (_la == K_WHEN);
                        setState(1567);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == K_ELSE) {
                            {
                                setState(1565);
                                match(K_ELSE);
                                setState(1566);
                                expr(0);
                            }
                        }

                        setState(1569);
                        match(K_END);
                    }
                    break;
                    case 10: {
                        setState(1571);
                        raise_function();
                    }
                    break;
                }
                _ctx.stop = _input.LT(-1);
                setState(1674);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 235, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(1672);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 234, _ctx)) {
                                case 1: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1574);
                                    if (!(precpred(_ctx, 20)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 20)");
                                    setState(1575);
                                    match(PIPE2);
                                    setState(1576);
                                    expr(21);
                                }
                                break;
                                case 2: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1577);
                                    if (!(precpred(_ctx, 19)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 19)");
                                    setState(1578);
                                    _la = _input.LA(1);
                                    if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STAR) | (1L << DIV) | (1L << MOD))) != 0))) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(1579);
                                    expr(20);
                                }
                                break;
                                case 3: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1580);
                                    if (!(precpred(_ctx, 18)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 18)");
                                    setState(1581);
                                    _la = _input.LA(1);
                                    if (!(_la == PLUS || _la == MINUS)) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(1582);
                                    expr(19);
                                }
                                break;
                                case 4: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1583);
                                    if (!(precpred(_ctx, 17)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 17)");
                                    setState(1584);
                                    _la = _input.LA(1);
                                    if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LT2) | (1L << GT2) | (1L << AMP) | (1L << PIPE))) != 0))) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(1585);
                                    expr(18);
                                }
                                break;
                                case 5: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1586);
                                    if (!(precpred(_ctx, 16)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 16)");
                                    setState(1587);
                                    _la = _input.LA(1);
                                    if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LT) | (1L << LT_EQ) | (1L << GT) | (1L << GT_EQ))) != 0))) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(1588);
                                    expr(17);
                                }
                                break;
                                case 6: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1589);
                                    if (!(precpred(_ctx, 15)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 15)");
                                    setState(1602);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 223, _ctx)) {
                                        case 1: {
                                            setState(1590);
                                            match(ASSIGN);
                                        }
                                        break;
                                        case 2: {
                                            setState(1591);
                                            match(EQ);
                                        }
                                        break;
                                        case 3: {
                                            setState(1592);
                                            match(NOT_EQ1);
                                        }
                                        break;
                                        case 4: {
                                            setState(1593);
                                            match(NOT_EQ2);
                                        }
                                        break;
                                        case 5: {
                                            setState(1594);
                                            match(K_IS);
                                        }
                                        break;
                                        case 6: {
                                            setState(1595);
                                            match(K_IS);
                                            setState(1596);
                                            match(K_NOT);
                                        }
                                        break;
                                        case 7: {
                                            setState(1597);
                                            match(K_IN);
                                        }
                                        break;
                                        case 8: {
                                            setState(1598);
                                            match(K_LIKE);
                                        }
                                        break;
                                        case 9: {
                                            setState(1599);
                                            match(K_GLOB);
                                        }
                                        break;
                                        case 10: {
                                            setState(1600);
                                            match(K_MATCH);
                                        }
                                        break;
                                        case 11: {
                                            setState(1601);
                                            match(K_REGEXP);
                                        }
                                        break;
                                    }
                                    setState(1604);
                                    expr(16);
                                }
                                break;
                                case 7: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1605);
                                    if (!(precpred(_ctx, 14)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 14)");
                                    setState(1606);
                                    match(K_AND);
                                    setState(1607);
                                    expr(15);
                                }
                                break;
                                case 8: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1608);
                                    if (!(precpred(_ctx, 13)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 13)");
                                    setState(1609);
                                    match(K_OR);
                                    setState(1610);
                                    expr(14);
                                }
                                break;
                                case 9: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1611);
                                    if (!(precpred(_ctx, 6)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 6)");
                                    setState(1612);
                                    match(K_IS);
                                    setState(1614);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 224, _ctx)) {
                                        case 1: {
                                            setState(1613);
                                            match(K_NOT);
                                        }
                                        break;
                                    }
                                    setState(1616);
                                    expr(7);
                                }
                                break;
                                case 10: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1617);
                                    if (!(precpred(_ctx, 5)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 5)");
                                    setState(1619);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    if (_la == K_NOT) {
                                        {
                                            setState(1618);
                                            match(K_NOT);
                                        }
                                    }

                                    setState(1621);
                                    match(K_BETWEEN);
                                    setState(1622);
                                    expr(0);
                                    setState(1623);
                                    match(K_AND);
                                    setState(1624);
                                    expr(6);
                                }
                                break;
                                case 11: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1626);
                                    if (!(precpred(_ctx, 9)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 9)");
                                    setState(1627);
                                    match(K_COLLATE);
                                    setState(1628);
                                    collation_name();
                                }
                                break;
                                case 12: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1629);
                                    if (!(precpred(_ctx, 8)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 8)");
                                    setState(1631);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    if (_la == K_NOT) {
                                        {
                                            setState(1630);
                                            match(K_NOT);
                                        }
                                    }

                                    setState(1633);
                                    _la = _input.LA(1);
                                    if (!(((((_la - 114)) & ~0x3f) == 0 && ((1L << (_la - 114)) & ((1L << (K_GLOB - 114)) | (1L << (K_LIKE - 114)) | (1L << (K_MATCH - 114)) | (1L << (K_REGEXP - 114)))) != 0))) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(1634);
                                    expr(0);
                                    setState(1637);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 227, _ctx)) {
                                        case 1: {
                                            setState(1635);
                                            match(K_ESCAPE);
                                            setState(1636);
                                            expr(0);
                                        }
                                        break;
                                    }
                                }
                                break;
                                case 13: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1639);
                                    if (!(precpred(_ctx, 7)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 7)");
                                    setState(1644);
                                    _errHandler.sync(this);
                                    switch (_input.LA(1)) {
                                        case K_ISNULL: {
                                            setState(1640);
                                            match(K_ISNULL);
                                        }
                                        break;
                                        case K_NOTNULL: {
                                            setState(1641);
                                            match(K_NOTNULL);
                                        }
                                        break;
                                        case K_NOT: {
                                            setState(1642);
                                            match(K_NOT);
                                            setState(1643);
                                            match(K_NULL);
                                        }
                                        break;
                                        default:
                                            throw new NoViableAltException(this);
                                    }
                                }
                                break;
                                case 14: {
                                    _localctx = new ExprContext(_parentctx, _parentState);
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(1646);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(1648);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    if (_la == K_NOT) {
                                        {
                                            setState(1647);
                                            match(K_NOT);
                                        }
                                    }

                                    setState(1650);
                                    match(K_IN);
                                    setState(1670);
                                    _errHandler.sync(this);
                                    switch (getInterpreter().adaptivePredict(_input, 233, _ctx)) {
                                        case 1: {
                                            setState(1651);
                                            match(OPEN_PAR);
                                            setState(1661);
                                            _errHandler.sync(this);
                                            switch (getInterpreter().adaptivePredict(_input, 231, _ctx)) {
                                                case 1: {
                                                    setState(1652);
                                                    select_stmt();
                                                }
                                                break;
                                                case 2: {
                                                    setState(1653);
                                                    expr(0);
                                                    setState(1658);
                                                    _errHandler.sync(this);
                                                    _la = _input.LA(1);
                                                    while (_la == COMMA) {
                                                        {
                                                            {
                                                                setState(1654);
                                                                match(COMMA);
                                                                setState(1655);
                                                                expr(0);
                                                            }
                                                        }
                                                        setState(1660);
                                                        _errHandler.sync(this);
                                                        _la = _input.LA(1);
                                                    }
                                                }
                                                break;
                                            }
                                            setState(1663);
                                            match(CLOSE_PAR);
                                        }
                                        break;
                                        case 2: {
                                            setState(1667);
                                            _errHandler.sync(this);
                                            switch (getInterpreter().adaptivePredict(_input, 232, _ctx)) {
                                                case 1: {
                                                    setState(1664);
                                                    database_name();
                                                    setState(1665);
                                                    match(DOT);
                                                }
                                                break;
                                            }
                                            setState(1669);
                                            table_name();
                                        }
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                    setState(1676);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 235, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            unrollRecursionContexts(_parentctx);
        }
        return _localctx;
    }

    public final Foreign_key_clauseContext foreign_key_clause() throws RecognitionException {
        Foreign_key_clauseContext _localctx = new Foreign_key_clauseContext(_ctx, getState());
        enterRule(_localctx, 128, RULE_foreign_key_clause);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1677);
                match(K_REFERENCES);
                setState(1681);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 236, _ctx)) {
                    case 1: {
                        setState(1678);
                        database_name();
                        setState(1679);
                        match(DOT);
                    }
                    break;
                }
                setState(1683);
                foreign_table();
                setState(1695);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 238, _ctx)) {
                    case 1: {
                        setState(1684);
                        match(OPEN_PAR);
                        setState(1685);
                        fk_target_column_name();
                        setState(1690);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1686);
                                    match(COMMA);
                                    setState(1687);
                                    fk_target_column_name();
                                }
                            }
                            setState(1692);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(1693);
                        match(CLOSE_PAR);
                    }
                    break;
                }
                setState(1715);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 241, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1711);
                                _errHandler.sync(this);
                                switch (_input.LA(1)) {
                                    case K_ON: {
                                        setState(1697);
                                        match(K_ON);
                                        setState(1698);
                                        _la = _input.LA(1);
                                        if (!(_la == K_DELETE || _la == K_UPDATE)) {
                                            _errHandler.recoverInline(this);
                                        } else {
                                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                            _errHandler.reportMatch(this);
                                            consume();
                                        }
                                        setState(1707);
                                        _errHandler.sync(this);
                                        switch (getInterpreter().adaptivePredict(_input, 239, _ctx)) {
                                            case 1: {
                                                setState(1699);
                                                match(K_SET);
                                                setState(1700);
                                                match(K_NULL);
                                            }
                                            break;
                                            case 2: {
                                                setState(1701);
                                                match(K_SET);
                                                setState(1702);
                                                match(K_DEFAULT);
                                            }
                                            break;
                                            case 3: {
                                                setState(1703);
                                                match(K_CASCADE);
                                            }
                                            break;
                                            case 4: {
                                                setState(1704);
                                                match(K_RESTRICT);
                                            }
                                            break;
                                            case 5: {
                                                setState(1705);
                                                match(K_NO);
                                                setState(1706);
                                                match(K_ACTION);
                                            }
                                            break;
                                        }
                                    }
                                    break;
                                    case K_MATCH: {
                                        setState(1709);
                                        match(K_MATCH);
                                        setState(1710);
                                        name();
                                    }
                                    break;
                                    default:
                                        throw new NoViableAltException(this);
                                }
                            }
                        }
                    }
                    setState(1717);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 241, _ctx);
                }
                setState(1731);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 245, _ctx)) {
                    case 1: {
                        setState(1719);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == K_NOT) {
                            {
                                setState(1718);
                                match(K_NOT);
                            }
                        }

                        setState(1721);
                        match(K_DEFERRABLE);
                        setState(1726);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 243, _ctx)) {
                            case 1: {
                                setState(1722);
                                match(K_INITIALLY);
                                setState(1723);
                                match(K_DEFERRED);
                            }
                            break;
                            case 2: {
                                setState(1724);
                                match(K_INITIALLY);
                                setState(1725);
                                match(K_IMMEDIATE);
                            }
                            break;
                        }
                        setState(1729);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 244, _ctx)) {
                            case 1: {
                                setState(1728);
                                match(K_ENABLE);
                            }
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Fk_target_column_nameContext fk_target_column_name() throws RecognitionException {
        Fk_target_column_nameContext _localctx = new Fk_target_column_nameContext(_ctx, getState());
        enterRule(_localctx, 130, RULE_fk_target_column_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1733);
                name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Raise_functionContext raise_function() throws RecognitionException {
        Raise_functionContext _localctx = new Raise_functionContext(_ctx, getState());
        enterRule(_localctx, 132, RULE_raise_function);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1735);
                match(K_RAISE);
                setState(1736);
                match(OPEN_PAR);
                setState(1741);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case K_IGNORE: {
                        setState(1737);
                        match(K_IGNORE);
                    }
                    break;
                    case K_ABORT:
                    case K_FAIL:
                    case K_ROLLBACK: {
                        setState(1738);
                        _la = _input.LA(1);
                        if (!(_la == K_ABORT || _la == K_FAIL || _la == K_ROLLBACK)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                        setState(1739);
                        match(COMMA);
                        setState(1740);
                        error_message();
                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
                setState(1743);
                match(CLOSE_PAR);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Indexed_columnContext indexed_column() throws RecognitionException {
        Indexed_columnContext _localctx = new Indexed_columnContext(_ctx, getState());
        enterRule(_localctx, 134, RULE_indexed_column);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1745);
                column_name();
                setState(1748);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_COLLATE) {
                    {
                        setState(1746);
                        match(K_COLLATE);
                        setState(1747);
                        collation_name();
                    }
                }

                setState(1751);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_ASC || _la == K_DESC) {
                    {
                        setState(1750);
                        _la = _input.LA(1);
                        if (!(_la == K_ASC || _la == K_DESC)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Table_constraintContext table_constraint() throws RecognitionException {
        Table_constraintContext _localctx = new Table_constraintContext(_ctx, getState());
        enterRule(_localctx, 136, RULE_table_constraint);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1755);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == K_CONSTRAINT) {
                    {
                        setState(1753);
                        match(K_CONSTRAINT);
                        setState(1754);
                        name();
                    }
                }

                setState(1766);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case K_PRIMARY: {
                        setState(1757);
                        table_constraint_primary_key();
                    }
                    break;
                    case K_KEY: {
                        setState(1758);
                        table_constraint_key();
                    }
                    break;
                    case K_UNIQUE: {
                        setState(1759);
                        table_constraint_unique();
                    }
                    break;
                    case K_CHECK: {
                        setState(1760);
                        match(K_CHECK);
                        setState(1761);
                        match(OPEN_PAR);
                        setState(1762);
                        expr(0);
                        setState(1763);
                        match(CLOSE_PAR);
                    }
                    break;
                    case K_FOREIGN: {
                        setState(1765);
                        table_constraint_foreign_key();
                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Table_constraint_primary_keyContext table_constraint_primary_key() throws RecognitionException {
        Table_constraint_primary_keyContext _localctx = new Table_constraint_primary_keyContext(_ctx, getState());
        enterRule(_localctx, 138, RULE_table_constraint_primary_key);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1768);
                match(K_PRIMARY);
                setState(1769);
                match(K_KEY);
                setState(1770);
                match(OPEN_PAR);
                setState(1771);
                indexed_column();
                setState(1776);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(1772);
                            match(COMMA);
                            setState(1773);
                            indexed_column();
                        }
                    }
                    setState(1778);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(1779);
                match(CLOSE_PAR);
                setState(1780);
                conflict_clause();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Table_constraint_foreign_keyContext table_constraint_foreign_key() throws RecognitionException {
        Table_constraint_foreign_keyContext _localctx = new Table_constraint_foreign_keyContext(_ctx, getState());
        enterRule(_localctx, 140, RULE_table_constraint_foreign_key);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1782);
                match(K_FOREIGN);
                setState(1783);
                match(K_KEY);
                setState(1784);
                match(OPEN_PAR);
                setState(1785);
                fk_origin_column_name();
                setState(1790);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(1786);
                            match(COMMA);
                            setState(1787);
                            fk_origin_column_name();
                        }
                    }
                    setState(1792);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(1793);
                match(CLOSE_PAR);
                setState(1794);
                foreign_key_clause();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Table_constraint_uniqueContext table_constraint_unique() throws RecognitionException {
        Table_constraint_uniqueContext _localctx = new Table_constraint_uniqueContext(_ctx, getState());
        enterRule(_localctx, 142, RULE_table_constraint_unique);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1796);
                match(K_UNIQUE);
                setState(1798);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 253, _ctx)) {
                    case 1: {
                        setState(1797);
                        match(K_KEY);
                    }
                    break;
                }
                setState(1801);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 254, _ctx)) {
                    case 1: {
                        setState(1800);
                        name();
                    }
                    break;
                }
                setState(1803);
                match(OPEN_PAR);
                setState(1804);
                indexed_column();
                setState(1809);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(1805);
                            match(COMMA);
                            setState(1806);
                            indexed_column();
                        }
                    }
                    setState(1811);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(1812);
                match(CLOSE_PAR);
                setState(1813);
                conflict_clause();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Table_constraint_keyContext table_constraint_key() throws RecognitionException {
        Table_constraint_keyContext _localctx = new Table_constraint_keyContext(_ctx, getState());
        enterRule(_localctx, 144, RULE_table_constraint_key);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1815);
                match(K_KEY);
                setState(1817);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 256, _ctx)) {
                    case 1: {
                        setState(1816);
                        name();
                    }
                    break;
                }
                setState(1819);
                match(OPEN_PAR);
                setState(1820);
                indexed_column();
                setState(1825);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(1821);
                            match(COMMA);
                            setState(1822);
                            indexed_column();
                        }
                    }
                    setState(1827);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(1828);
                match(CLOSE_PAR);
                setState(1829);
                conflict_clause();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Fk_origin_column_nameContext fk_origin_column_name() throws RecognitionException {
        Fk_origin_column_nameContext _localctx = new Fk_origin_column_nameContext(_ctx, getState());
        enterRule(_localctx, 146, RULE_fk_origin_column_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1831);
                column_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final With_clauseContext with_clause() throws RecognitionException {
        With_clauseContext _localctx = new With_clauseContext(_ctx, getState());
        enterRule(_localctx, 148, RULE_with_clause);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1833);
                match(K_WITH);
                setState(1835);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 258, _ctx)) {
                    case 1: {
                        setState(1834);
                        match(K_RECURSIVE);
                    }
                    break;
                }
                setState(1837);
                cte_table_name();
                setState(1838);
                match(K_AS);
                setState(1839);
                match(OPEN_PAR);
                setState(1840);
                select_stmt();
                setState(1841);
                match(CLOSE_PAR);
                setState(1851);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == COMMA) {
                    {
                        {
                            setState(1842);
                            match(COMMA);
                            setState(1843);
                            cte_table_name();
                            setState(1844);
                            match(K_AS);
                            setState(1845);
                            match(OPEN_PAR);
                            setState(1846);
                            select_stmt();
                            setState(1847);
                            match(CLOSE_PAR);
                        }
                    }
                    setState(1853);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Qualified_table_nameContext qualified_table_name() throws RecognitionException {
        Qualified_table_nameContext _localctx = new Qualified_table_nameContext(_ctx, getState());
        enterRule(_localctx, 150, RULE_qualified_table_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1857);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 260, _ctx)) {
                    case 1: {
                        setState(1854);
                        database_name();
                        setState(1855);
                        match(DOT);
                    }
                    break;
                }
                setState(1859);
                table_name();
                setState(1865);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case K_INDEXED: {
                        setState(1860);
                        match(K_INDEXED);
                        setState(1861);
                        match(K_BY);
                        setState(1862);
                        index_name();
                    }
                    break;
                    case K_NOT: {
                        setState(1863);
                        match(K_NOT);
                        setState(1864);
                        match(K_INDEXED);
                    }
                    break;
                    case EOF:
                    case SCOL:
                    case K_ALTER:
                    case K_ANALYZE:
                    case K_ATTACH:
                    case K_BEGIN:
                    case K_COMMIT:
                    case K_CREATE:
                    case K_DELETE:
                    case K_DETACH:
                    case K_DROP:
                    case K_END:
                    case K_EXECUTE:
                    case K_EXPLAIN:
                    case K_INSERT:
                    case K_LIMIT:
                    case K_ORDER:
                    case K_PRAGMA:
                    case K_REINDEX:
                    case K_RELEASE:
                    case K_REPLACE:
                    case K_ROLLBACK:
                    case K_SAVEPOINT:
                    case K_SELECT:
                    case K_SET:
                    case K_UPDATE:
                    case K_VACUUM:
                    case K_VALUES:
                    case K_WHERE:
                    case K_WITH:
                    case UNEXPECTED_CHAR:
                        break;
                    default:
                        break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Ordering_termContext ordering_term() throws RecognitionException {
        Ordering_termContext _localctx = new Ordering_termContext(_ctx, getState());
        enterRule(_localctx, 152, RULE_ordering_term);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1867);
                expr(0);
                setState(1870);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 262, _ctx)) {
                    case 1: {
                        setState(1868);
                        match(K_COLLATE);
                        setState(1869);
                        collation_name();
                    }
                    break;
                }
                setState(1873);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 263, _ctx)) {
                    case 1: {
                        setState(1872);
                        _la = _input.LA(1);
                        if (!(_la == K_ASC || _la == K_DESC)) {
                            _errHandler.recoverInline(this);
                        } else {
                            if (_input.LA(1) == Token.EOF) matchedEOF = true;
                            _errHandler.reportMatch(this);
                            consume();
                        }
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Order_collateContext order_collate() throws RecognitionException {
        Order_collateContext _localctx = new Order_collateContext(_ctx, getState());
        enterRule(_localctx, 154, RULE_order_collate);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1875);
                _la = _input.LA(1);
                if (!(_la == K_ASC || _la == K_DESC)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Pragma_valueContext pragma_value() throws RecognitionException {
        Pragma_valueContext _localctx = new Pragma_valueContext(_ctx, getState());
        enterRule(_localctx, 156, RULE_pragma_value);
        try {
            setState(1880);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 264, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(1877);
                    signed_number();
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(1878);
                    name();
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(1879);
                    match(STRING_LITERAL);
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Common_table_expressionContext common_table_expression() throws RecognitionException {
        Common_table_expressionContext _localctx = new Common_table_expressionContext(_ctx, getState());
        enterRule(_localctx, 158, RULE_common_table_expression);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(1882);
                table_name();
                setState(1894);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == OPEN_PAR) {
                    {
                        setState(1883);
                        match(OPEN_PAR);
                        setState(1884);
                        column_name();
                        setState(1889);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1885);
                                    match(COMMA);
                                    setState(1886);
                                    column_name();
                                }
                            }
                            setState(1891);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(1892);
                        match(CLOSE_PAR);
                    }
                }

                setState(1896);
                match(K_AS);
                setState(1897);
                match(OPEN_PAR);
                setState(1898);
                select_stmt();
                setState(1899);
                match(CLOSE_PAR);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Result_columnContext result_column() throws RecognitionException {
        Result_columnContext _localctx = new Result_columnContext(_ctx, getState());
        enterRule(_localctx, 160, RULE_result_column);
        int _la;
        try {
            setState(1913);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 269, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(1901);
                    match(STAR);
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(1902);
                    table_name();
                    setState(1903);
                    match(DOT);
                    setState(1904);
                    match(STAR);
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(1906);
                    expr(0);
                    setState(1911);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 268, _ctx)) {
                        case 1: {
                            setState(1908);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == K_AS) {
                                {
                                    setState(1907);
                                    match(K_AS);
                                }
                            }

                            setState(1910);
                            column_alias();
                        }
                        break;
                    }
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Table_or_subqueryContext table_or_subquery() throws RecognitionException {
        Table_or_subqueryContext _localctx = new Table_or_subqueryContext(_ctx, getState());
        enterRule(_localctx, 162, RULE_table_or_subquery);
        int _la;
        try {
            setState(1962);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 280, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(1918);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 270, _ctx)) {
                        case 1: {
                            setState(1915);
                            database_name();
                            setState(1916);
                            match(DOT);
                        }
                        break;
                    }
                    setState(1920);
                    table_name();
                    setState(1925);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 272, _ctx)) {
                        case 1: {
                            setState(1922);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 271, _ctx)) {
                                case 1: {
                                    setState(1921);
                                    match(K_AS);
                                }
                                break;
                            }
                            setState(1924);
                            table_alias();
                        }
                        break;
                    }
                    setState(1932);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 273, _ctx)) {
                        case 1: {
                            setState(1927);
                            match(K_INDEXED);
                            setState(1928);
                            match(K_BY);
                            setState(1929);
                            index_name();
                        }
                        break;
                        case 2: {
                            setState(1930);
                            match(K_NOT);
                            setState(1931);
                            match(K_INDEXED);
                        }
                        break;
                    }
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(1934);
                    match(OPEN_PAR);
                    setState(1944);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 275, _ctx)) {
                        case 1: {
                            setState(1935);
                            table_or_subquery();
                            setState(1940);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(1936);
                                        match(COMMA);
                                        setState(1937);
                                        table_or_subquery();
                                    }
                                }
                                setState(1942);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                        }
                        break;
                        case 2: {
                            setState(1943);
                            join_clause();
                        }
                        break;
                    }
                    setState(1946);
                    match(CLOSE_PAR);
                    setState(1951);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 277, _ctx)) {
                        case 1: {
                            setState(1948);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 276, _ctx)) {
                                case 1: {
                                    setState(1947);
                                    match(K_AS);
                                }
                                break;
                            }
                            setState(1950);
                            table_alias();
                        }
                        break;
                    }
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(1953);
                    match(OPEN_PAR);
                    setState(1954);
                    select_stmt();
                    setState(1955);
                    match(CLOSE_PAR);
                    setState(1960);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 279, _ctx)) {
                        case 1: {
                            setState(1957);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 278, _ctx)) {
                                case 1: {
                                    setState(1956);
                                    match(K_AS);
                                }
                                break;
                            }
                            setState(1959);
                            table_alias();
                        }
                        break;
                    }
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Join_clauseContext join_clause() throws RecognitionException {
        Join_clauseContext _localctx = new Join_clauseContext(_ctx, getState());
        enterRule(_localctx, 164, RULE_join_clause);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(1964);
                table_or_subquery();
                setState(1971);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 281, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        {
                            {
                                setState(1965);
                                join_operator();
                                setState(1966);
                                table_or_subquery();
                                setState(1967);
                                join_constraint();
                            }
                        }
                    }
                    setState(1973);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 281, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Join_operatorContext join_operator() throws RecognitionException {
        Join_operatorContext _localctx = new Join_operatorContext(_ctx, getState());
        enterRule(_localctx, 166, RULE_join_operator);
        int _la;
        try {
            setState(1987);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case COMMA:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(1974);
                    match(COMMA);
                }
                break;
                case K_CROSS:
                case K_INNER:
                case K_JOIN:
                case K_LEFT:
                case K_NATURAL:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(1976);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == K_NATURAL) {
                        {
                            setState(1975);
                            match(K_NATURAL);
                        }
                    }

                    setState(1984);
                    _errHandler.sync(this);
                    switch (_input.LA(1)) {
                        case K_LEFT: {
                            setState(1978);
                            match(K_LEFT);
                            setState(1980);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == K_OUTER) {
                                {
                                    setState(1979);
                                    match(K_OUTER);
                                }
                            }

                        }
                        break;
                        case K_INNER: {
                            setState(1982);
                            match(K_INNER);
                        }
                        break;
                        case K_CROSS: {
                            setState(1983);
                            match(K_CROSS);
                        }
                        break;
                        case K_JOIN:
                            break;
                        default:
                            break;
                    }
                    setState(1986);
                    match(K_JOIN);
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Join_constraintContext join_constraint() throws RecognitionException {
        Join_constraintContext _localctx = new Join_constraintContext(_ctx, getState());
        enterRule(_localctx, 168, RULE_join_constraint);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2003);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 287, _ctx)) {
                    case 1: {
                        setState(1989);
                        match(K_ON);
                        setState(1990);
                        expr(0);
                    }
                    break;
                    case 2: {
                        setState(1991);
                        match(K_USING);
                        setState(1992);
                        match(OPEN_PAR);
                        setState(1993);
                        column_name();
                        setState(1998);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(1994);
                                    match(COMMA);
                                    setState(1995);
                                    column_name();
                                }
                            }
                            setState(2000);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(2001);
                        match(CLOSE_PAR);
                    }
                    break;
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Select_coreContext select_core() throws RecognitionException {
        Select_coreContext _localctx = new Select_coreContext(_ctx, getState());
        enterRule(_localctx, 170, RULE_select_core);
        int _la;
        try {
            setState(2079);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case K_SELECT:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(2005);
                    match(K_SELECT);
                    setState(2007);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 288, _ctx)) {
                        case 1: {
                            setState(2006);
                            _la = _input.LA(1);
                            if (!(_la == K_ALL || _la == K_DISTINCT)) {
                                _errHandler.recoverInline(this);
                            } else {
                                if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                _errHandler.reportMatch(this);
                                consume();
                            }
                        }
                        break;
                    }
                    setState(2009);
                    result_column();
                    setState(2014);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(2010);
                                match(COMMA);
                                setState(2011);
                                result_column();
                            }
                        }
                        setState(2016);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(2029);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == K_FROM) {
                        {
                            setState(2017);
                            match(K_FROM);
                            setState(2027);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 291, _ctx)) {
                                case 1: {
                                    setState(2018);
                                    table_or_subquery();
                                    setState(2023);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    while (_la == COMMA) {
                                        {
                                            {
                                                setState(2019);
                                                match(COMMA);
                                                setState(2020);
                                                table_or_subquery();
                                            }
                                        }
                                        setState(2025);
                                        _errHandler.sync(this);
                                        _la = _input.LA(1);
                                    }
                                }
                                break;
                                case 2: {
                                    setState(2026);
                                    join_clause();
                                }
                                break;
                            }
                        }
                    }

                    setState(2033);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == K_WHERE) {
                        {
                            setState(2031);
                            match(K_WHERE);
                            setState(2032);
                            expr(0);
                        }
                    }

                    setState(2049);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if (_la == K_GROUP) {
                        {
                            setState(2035);
                            match(K_GROUP);
                            setState(2036);
                            match(K_BY);
                            setState(2037);
                            expr(0);
                            setState(2042);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            while (_la == COMMA) {
                                {
                                    {
                                        setState(2038);
                                        match(COMMA);
                                        setState(2039);
                                        expr(0);
                                    }
                                }
                                setState(2044);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                            }
                            setState(2047);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                            if (_la == K_HAVING) {
                                {
                                    setState(2045);
                                    match(K_HAVING);
                                    setState(2046);
                                    expr(0);
                                }
                            }

                        }
                    }

                }
                break;
                case K_VALUES:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(2051);
                    match(K_VALUES);
                    setState(2052);
                    match(OPEN_PAR);
                    setState(2053);
                    expr(0);
                    setState(2058);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(2054);
                                match(COMMA);
                                setState(2055);
                                expr(0);
                            }
                        }
                        setState(2060);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                    setState(2061);
                    match(CLOSE_PAR);
                    setState(2076);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    while (_la == COMMA) {
                        {
                            {
                                setState(2062);
                                match(COMMA);
                                setState(2063);
                                match(OPEN_PAR);
                                setState(2064);
                                expr(0);
                                setState(2069);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == COMMA) {
                                    {
                                        {
                                            setState(2065);
                                            match(COMMA);
                                            setState(2066);
                                            expr(0);
                                        }
                                    }
                                    setState(2071);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                                setState(2072);
                                match(CLOSE_PAR);
                            }
                        }
                        setState(2078);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                    }
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Compound_operatorContext compound_operator() throws RecognitionException {
        Compound_operatorContext _localctx = new Compound_operatorContext(_ctx, getState());
        enterRule(_localctx, 172, RULE_compound_operator);
        try {
            setState(2086);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 301, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(2081);
                    match(K_UNION);
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(2082);
                    match(K_UNION);
                    setState(2083);
                    match(K_ALL);
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(2084);
                    match(K_INTERSECT);
                }
                break;
                case 4:
                    enterOuterAlt(_localctx, 4);
                {
                    setState(2085);
                    match(K_EXCEPT);
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Cte_table_nameContext cte_table_name() throws RecognitionException {
        Cte_table_nameContext _localctx = new Cte_table_nameContext(_ctx, getState());
        enterRule(_localctx, 174, RULE_cte_table_name);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2088);
                table_name();
                setState(2100);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == OPEN_PAR) {
                    {
                        setState(2089);
                        match(OPEN_PAR);
                        setState(2090);
                        column_name();
                        setState(2095);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        while (_la == COMMA) {
                            {
                                {
                                    setState(2091);
                                    match(COMMA);
                                    setState(2092);
                                    column_name();
                                }
                            }
                            setState(2097);
                            _errHandler.sync(this);
                            _la = _input.LA(1);
                        }
                        setState(2098);
                        match(CLOSE_PAR);
                    }
                }

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Signed_numberContext signed_number() throws RecognitionException {
        Signed_numberContext _localctx = new Signed_numberContext(_ctx, getState());
        enterRule(_localctx, 176, RULE_signed_number);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2107);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case PLUS:
                    case MINUS:
                    case NUMERIC_LITERAL: {
                        setState(2103);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if (_la == PLUS || _la == MINUS) {
                            {
                                setState(2102);
                                _la = _input.LA(1);
                                if (!(_la == PLUS || _la == MINUS)) {
                                    _errHandler.recoverInline(this);
                                } else {
                                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                    _errHandler.reportMatch(this);
                                    consume();
                                }
                            }
                        }

                        setState(2105);
                        match(NUMERIC_LITERAL);
                    }
                    break;
                    case STAR: {
                        setState(2106);
                        match(STAR);
                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Literal_valueContext literal_value() throws RecognitionException {
        Literal_valueContext _localctx = new Literal_valueContext(_ctx, getState());
        enterRule(_localctx, 178, RULE_literal_value);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2109);
                _la = _input.LA(1);
                if (!(((((_la - 86)) & ~0x3f) == 0 && ((1L << (_la - 86)) & ((1L << (K_CURRENT_DATE - 86)) | (1L << (K_CURRENT_TIME - 86)) | (1L << (K_CURRENT_TIMESTAMP - 86)) | (1L << (K_NULL - 86)))) != 0) || ((((_la - 194)) & ~0x3f) == 0 && ((1L << (_la - 194)) & ((1L << (NUMERIC_LITERAL - 194)) | (1L << (STRING_LITERAL - 194)) | (1L << (BLOB_LITERAL - 194)))) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Unary_operatorContext unary_operator() throws RecognitionException {
        Unary_operatorContext _localctx = new Unary_operatorContext(_ctx, getState());
        enterRule(_localctx, 180, RULE_unary_operator);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2111);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PLUS) | (1L << MINUS) | (1L << TILDE))) != 0) || _la == K_NOT)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Error_messageContext error_message() throws RecognitionException {
        Error_messageContext _localctx = new Error_messageContext(_ctx, getState());
        enterRule(_localctx, 182, RULE_error_message);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2113);
                match(STRING_LITERAL);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Module_argumentContext module_argument() throws RecognitionException {
        Module_argumentContext _localctx = new Module_argumentContext(_ctx, getState());
        enterRule(_localctx, 184, RULE_module_argument);
        try {
            setState(2117);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 306, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(2115);
                    expr(0);
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(2116);
                    column_def();
                }
                break;
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_aliasContext column_alias() throws RecognitionException {
        Column_aliasContext _localctx = new Column_aliasContext(_ctx, getState());
        enterRule(_localctx, 186, RULE_column_alias);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2119);
                _la = _input.LA(1);
                if (!(_la == IDENTIFIER || _la == STRING_LITERAL)) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final KeywordContext keyword() throws RecognitionException {
        KeywordContext _localctx = new KeywordContext(_ctx, getState());
        enterRule(_localctx, 188, RULE_keyword);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2121);
                _la = _input.LA(1);
                if (!(((((_la - 55)) & ~0x3f) == 0 && ((1L << (_la - 55)) & ((1L << (K_ABORT - 55)) | (1L << (K_ACTION - 55)) | (1L << (K_ADD - 55)) | (1L << (K_AFTER - 55)) | (1L << (K_ALL - 55)) | (1L << (K_ALTER - 55)) | (1L << (K_ANALYZE - 55)) | (1L << (K_AND - 55)) | (1L << (K_AS - 55)) | (1L << (K_ASC - 55)) | (1L << (K_ATTACH - 55)) | (1L << (K_AUTOINCREMENT - 55)) | (1L << (K_BEFORE - 55)) | (1L << (K_BEGIN - 55)) | (1L << (K_BETWEEN - 55)) | (1L << (K_BY - 55)) | (1L << (K_CASCADE - 55)) | (1L << (K_CASE - 55)) | (1L << (K_CAST - 55)) | (1L << (K_CHECK - 55)) | (1L << (K_COLLATE - 55)) | (1L << (K_COLUMN - 55)) | (1L << (K_COMMIT - 55)) | (1L << (K_CONFLICT - 55)) | (1L << (K_CONSTRAINT - 55)) | (1L << (K_CREATE - 55)) | (1L << (K_CROSS - 55)) | (1L << (K_CURRENT_DATE - 55)) | (1L << (K_CURRENT_TIME - 55)) | (1L << (K_CURRENT_TIMESTAMP - 55)) | (1L << (K_DATABASE - 55)) | (1L << (K_DEFAULT - 55)) | (1L << (K_DEFERRABLE - 55)) | (1L << (K_DEFERRED - 55)) | (1L << (K_DELETE - 55)) | (1L << (K_DESC - 55)) | (1L << (K_DETACH - 55)) | (1L << (K_DISTINCT - 55)) | (1L << (K_DROP - 55)) | (1L << (K_EACH - 55)) | (1L << (K_ELSE - 55)) | (1L << (K_END - 55)) | (1L << (K_ENABLE - 55)) | (1L << (K_ESCAPE - 55)) | (1L << (K_EXCEPT - 55)) | (1L << (K_EXCLUSIVE - 55)) | (1L << (K_EXISTS - 55)) | (1L << (K_EXPLAIN - 55)) | (1L << (K_FAIL - 55)) | (1L << (K_FOR - 55)) | (1L << (K_FOREIGN - 55)) | (1L << (K_FROM - 55)) | (1L << (K_FULL - 55)) | (1L << (K_GLOB - 55)) | (1L << (K_GROUP - 55)) | (1L << (K_HAVING - 55)) | (1L << (K_IF - 55)) | (1L << (K_IGNORE - 55)))) != 0) || ((((_la - 119)) & ~0x3f) == 0 && ((1L << (_la - 119)) & ((1L << (K_IMMEDIATE - 119)) | (1L << (K_IN - 119)) | (1L << (K_INDEX - 119)) | (1L << (K_INDEXED - 119)) | (1L << (K_INITIALLY - 119)) | (1L << (K_INNER - 119)) | (1L << (K_INSERT - 119)) | (1L << (K_INSTEAD - 119)) | (1L << (K_INTERSECT - 119)) | (1L << (K_INTO - 119)) | (1L << (K_IS - 119)) | (1L << (K_ISNULL - 119)) | (1L << (K_JOIN - 119)) | (1L << (K_KEY - 119)) | (1L << (K_LEFT - 119)) | (1L << (K_LIKE - 119)) | (1L << (K_LIMIT - 119)) | (1L << (K_MATCH - 119)) | (1L << (K_NATURAL - 119)) | (1L << (K_NEXTVAL - 119)) | (1L << (K_NO - 119)) | (1L << (K_NOT - 119)) | (1L << (K_NOTNULL - 119)) | (1L << (K_NULL - 119)) | (1L << (K_OF - 119)) | (1L << (K_OFFSET - 119)) | (1L << (K_ON - 119)) | (1L << (K_OR - 119)) | (1L << (K_ORDER - 119)) | (1L << (K_OUTER - 119)) | (1L << (K_PLAN - 119)) | (1L << (K_PRAGMA - 119)) | (1L << (K_PRIMARY - 119)) | (1L << (K_QUERY - 119)) | (1L << (K_RAISE - 119)) | (1L << (K_RECURSIVE - 119)) | (1L << (K_REFERENCES - 119)) | (1L << (K_REGEXP - 119)) | (1L << (K_REINDEX - 119)) | (1L << (K_RELEASE - 119)) | (1L << (K_RENAME - 119)) | (1L << (K_REPLACE - 119)) | (1L << (K_RESTRICT - 119)) | (1L << (K_RIGHT - 119)) | (1L << (K_ROLLBACK - 119)) | (1L << (K_ROW - 119)) | (1L << (K_SAVEPOINT - 119)) | (1L << (K_SELECT - 119)) | (1L << (K_SET - 119)) | (1L << (K_TABLE - 119)) | (1L << (K_TEMP - 119)) | (1L << (K_TEMPORARY - 119)) | (1L << (K_THEN - 119)) | (1L << (K_TO - 119)) | (1L << (K_TRANSACTION - 119)) | (1L << (K_TRIGGER - 119)) | (1L << (K_UNION - 119)) | (1L << (K_UNIQUE - 119)) | (1L << (K_UPDATE - 119)))) != 0) || ((((_la - 183)) & ~0x3f) == 0 && ((1L << (_la - 183)) & ((1L << (K_USING - 183)) | (1L << (K_VACUUM - 183)) | (1L << (K_VALUES - 183)) | (1L << (K_VIEW - 183)) | (1L << (K_VIRTUAL - 183)) | (1L << (K_WHEN - 183)) | (1L << (K_WHERE - 183)) | (1L << (K_WITH - 183)) | (1L << (K_WITHOUT - 183)))) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final UnknownContext unknown() throws RecognitionException {
        UnknownContext _localctx = new UnknownContext(_ctx, getState());
        enterRule(_localctx, 190, RULE_unknown);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(2124);
                _errHandler.sync(this);
                _alt = 1;
                do {
                    switch (_alt) {
                        case 1: {
                            {
                                setState(2123);
                                matchWildcard();
                            }
                        }
                        break;
                        default:
                            throw new NoViableAltException(this);
                    }
                    setState(2126);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 307, _ctx);
                } while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final NameContext name() throws RecognitionException {
        NameContext _localctx = new NameContext(_ctx, getState());
        enterRule(_localctx, 192, RULE_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2128);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Function_nameContext function_name() throws RecognitionException {
        Function_nameContext _localctx = new Function_nameContext(_ctx, getState());
        enterRule(_localctx, 194, RULE_function_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2130);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Database_nameContext database_name() throws RecognitionException {
        Database_nameContext _localctx = new Database_nameContext(_ctx, getState());
        enterRule(_localctx, 196, RULE_database_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2132);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Domain_nameContext domain_name() throws RecognitionException {
        Domain_nameContext _localctx = new Domain_nameContext(_ctx, getState());
        enterRule(_localctx, 198, RULE_domain_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2134);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Source_table_nameContext source_table_name() throws RecognitionException {
        Source_table_nameContext _localctx = new Source_table_nameContext(_ctx, getState());
        enterRule(_localctx, 200, RULE_source_table_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2136);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Table_nameContext table_name() throws RecognitionException {
        Table_nameContext _localctx = new Table_nameContext(_ctx, getState());
        enterRule(_localctx, 202, RULE_table_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2138);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Procedure_nameContext procedure_name() throws RecognitionException {
        Procedure_nameContext _localctx = new Procedure_nameContext(_ctx, getState());
        enterRule(_localctx, 204, RULE_procedure_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2140);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Table_or_index_nameContext table_or_index_name() throws RecognitionException {
        Table_or_index_nameContext _localctx = new Table_or_index_nameContext(_ctx, getState());
        enterRule(_localctx, 206, RULE_table_or_index_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2142);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final New_table_nameContext new_table_name() throws RecognitionException {
        New_table_nameContext _localctx = new New_table_nameContext(_ctx, getState());
        enterRule(_localctx, 208, RULE_new_table_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2144);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Column_nameContext column_name() throws RecognitionException {
        Column_nameContext _localctx = new Column_nameContext(_ctx, getState());
        enterRule(_localctx, 210, RULE_column_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2146);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Collation_nameContext collation_name() throws RecognitionException {
        Collation_nameContext _localctx = new Collation_nameContext(_ctx, getState());
        enterRule(_localctx, 212, RULE_collation_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2148);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Foreign_tableContext foreign_table() throws RecognitionException {
        Foreign_tableContext _localctx = new Foreign_tableContext(_ctx, getState());
        enterRule(_localctx, 214, RULE_foreign_table);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2150);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Index_nameContext index_name() throws RecognitionException {
        Index_nameContext _localctx = new Index_nameContext(_ctx, getState());
        enterRule(_localctx, 216, RULE_index_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2152);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Trigger_nameContext trigger_name() throws RecognitionException {
        Trigger_nameContext _localctx = new Trigger_nameContext(_ctx, getState());
        enterRule(_localctx, 218, RULE_trigger_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2154);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final View_nameContext view_name() throws RecognitionException {
        View_nameContext _localctx = new View_nameContext(_ctx, getState());
        enterRule(_localctx, 220, RULE_view_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2156);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Module_nameContext module_name() throws RecognitionException {
        Module_nameContext _localctx = new Module_nameContext(_ctx, getState());
        enterRule(_localctx, 222, RULE_module_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2158);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Pragma_nameContext pragma_name() throws RecognitionException {
        Pragma_nameContext _localctx = new Pragma_nameContext(_ctx, getState());
        enterRule(_localctx, 224, RULE_pragma_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2160);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Savepoint_nameContext savepoint_name() throws RecognitionException {
        Savepoint_nameContext _localctx = new Savepoint_nameContext(_ctx, getState());
        enterRule(_localctx, 226, RULE_savepoint_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2162);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Table_aliasContext table_alias() throws RecognitionException {
        Table_aliasContext _localctx = new Table_aliasContext(_ctx, getState());
        enterRule(_localctx, 228, RULE_table_alias);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2164);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Transaction_nameContext transaction_name() throws RecognitionException {
        Transaction_nameContext _localctx = new Transaction_nameContext(_ctx, getState());
        enterRule(_localctx, 230, RULE_transaction_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2166);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Charset_nameContext charset_name() throws RecognitionException {
        Charset_nameContext _localctx = new Charset_nameContext(_ctx, getState());
        enterRule(_localctx, 232, RULE_charset_name);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(2168);
                any_name();
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final Any_nameContext any_name() throws RecognitionException {
        Any_nameContext _localctx = new Any_nameContext(_ctx, getState());
        enterRule(_localctx, 234, RULE_any_name);
        try {
            setState(2177);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case IDENTIFIER:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(2170);
                    match(IDENTIFIER);
                }
                break;
                case K_ABORT:
                case K_ACTION:
                case K_ADD:
                case K_AFTER:
                case K_ALL:
                case K_ALTER:
                case K_ANALYZE:
                case K_AND:
                case K_AS:
                case K_ASC:
                case K_ATTACH:
                case K_AUTOINCREMENT:
                case K_BEFORE:
                case K_BEGIN:
                case K_BETWEEN:
                case K_BY:
                case K_CASCADE:
                case K_CASE:
                case K_CAST:
                case K_CHECK:
                case K_COLLATE:
                case K_COLUMN:
                case K_COMMIT:
                case K_CONFLICT:
                case K_CONSTRAINT:
                case K_CREATE:
                case K_CROSS:
                case K_CURRENT_DATE:
                case K_CURRENT_TIME:
                case K_CURRENT_TIMESTAMP:
                case K_DATABASE:
                case K_DEFAULT:
                case K_DEFERRABLE:
                case K_DEFERRED:
                case K_DELETE:
                case K_DESC:
                case K_DETACH:
                case K_DISTINCT:
                case K_DROP:
                case K_EACH:
                case K_ELSE:
                case K_END:
                case K_ENABLE:
                case K_ESCAPE:
                case K_EXCEPT:
                case K_EXCLUSIVE:
                case K_EXISTS:
                case K_EXPLAIN:
                case K_FAIL:
                case K_FOR:
                case K_FOREIGN:
                case K_FROM:
                case K_FULL:
                case K_GLOB:
                case K_GROUP:
                case K_HAVING:
                case K_IF:
                case K_IGNORE:
                case K_IMMEDIATE:
                case K_IN:
                case K_INDEX:
                case K_INDEXED:
                case K_INITIALLY:
                case K_INNER:
                case K_INSERT:
                case K_INSTEAD:
                case K_INTERSECT:
                case K_INTO:
                case K_IS:
                case K_ISNULL:
                case K_JOIN:
                case K_KEY:
                case K_LEFT:
                case K_LIKE:
                case K_LIMIT:
                case K_MATCH:
                case K_NATURAL:
                case K_NEXTVAL:
                case K_NO:
                case K_NOT:
                case K_NOTNULL:
                case K_NULL:
                case K_OF:
                case K_OFFSET:
                case K_ON:
                case K_OR:
                case K_ORDER:
                case K_OUTER:
                case K_PLAN:
                case K_PRAGMA:
                case K_PRIMARY:
                case K_QUERY:
                case K_RAISE:
                case K_RECURSIVE:
                case K_REFERENCES:
                case K_REGEXP:
                case K_REINDEX:
                case K_RELEASE:
                case K_RENAME:
                case K_REPLACE:
                case K_RESTRICT:
                case K_RIGHT:
                case K_ROLLBACK:
                case K_ROW:
                case K_SAVEPOINT:
                case K_SELECT:
                case K_SET:
                case K_TABLE:
                case K_TEMP:
                case K_TEMPORARY:
                case K_THEN:
                case K_TO:
                case K_TRANSACTION:
                case K_TRIGGER:
                case K_UNION:
                case K_UNIQUE:
                case K_UPDATE:
                case K_USING:
                case K_VACUUM:
                case K_VALUES:
                case K_VIEW:
                case K_VIRTUAL:
                case K_WHEN:
                case K_WHERE:
                case K_WITH:
                case K_WITHOUT:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(2171);
                    keyword();
                }
                break;
                case STRING_LITERAL:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(2172);
                    match(STRING_LITERAL);
                }
                break;
                case OPEN_PAR:
                    enterOuterAlt(_localctx, 4);
                {
                    setState(2173);
                    match(OPEN_PAR);
                    setState(2174);
                    any_name();
                    setState(2175);
                    match(CLOSE_PAR);
                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
        switch (ruleIndex) {
            case 63:
                return expr_sempred((ExprContext) _localctx, predIndex);
        }
        return true;
    }

    private boolean expr_sempred(ExprContext _localctx, int predIndex) {
        switch (predIndex) {
            case 0:
                return precpred(_ctx, 20);
            case 1:
                return precpred(_ctx, 19);
            case 2:
                return precpred(_ctx, 18);
            case 3:
                return precpred(_ctx, 17);
            case 4:
                return precpred(_ctx, 16);
            case 5:
                return precpred(_ctx, 15);
            case 6:
                return precpred(_ctx, 14);
            case 7:
                return precpred(_ctx, 13);
            case 8:
                return precpred(_ctx, 6);
            case 9:
                return precpred(_ctx, 5);
            case 10:
                return precpred(_ctx, 9);
            case 11:
                return precpred(_ctx, 8);
            case 12:
                return precpred(_ctx, 7);
            case 13:
                return precpred(_ctx, 4);
        }
        return true;
    }

    public static class ParseContext extends ParserRuleContext {
        public ParseContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode EOF() {
            return getToken(REDDATABASESqlParser.EOF, 0);
        }

        public List<Sql_stmt_listContext> sql_stmt_list() {
            return getRuleContexts(Sql_stmt_listContext.class);
        }

        public Sql_stmt_listContext sql_stmt_list(int i) {
            return getRuleContext(Sql_stmt_listContext.class, i);
        }

        public List<ErrorContext> error() {
            return getRuleContexts(ErrorContext.class);
        }

        public ErrorContext error(int i) {
            return getRuleContext(ErrorContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_parse;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterParse(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitParse(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitParse(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ErrorContext extends ParserRuleContext {
        public Token UNEXPECTED_CHAR;

        public ErrorContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode UNEXPECTED_CHAR() {
            return getToken(REDDATABASESqlParser.UNEXPECTED_CHAR, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_error;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterError(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitError(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitError(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Sql_stmt_listContext extends ParserRuleContext {
        public Sql_stmt_listContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<Sql_stmtContext> sql_stmt() {
            return getRuleContexts(Sql_stmtContext.class);
        }

        public Sql_stmtContext sql_stmt(int i) {
            return getRuleContext(Sql_stmtContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_sql_stmt_list;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterSql_stmt_list(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitSql_stmt_list(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSql_stmt_list(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Sql_stmtContext extends ParserRuleContext {
        public Sql_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Alter_table_stmtContext alter_table_stmt() {
            return getRuleContext(Alter_table_stmtContext.class, 0);
        }

        public Analyze_stmtContext analyze_stmt() {
            return getRuleContext(Analyze_stmtContext.class, 0);
        }

        public Attach_stmtContext attach_stmt() {
            return getRuleContext(Attach_stmtContext.class, 0);
        }

        public Begin_stmtContext begin_stmt() {
            return getRuleContext(Begin_stmtContext.class, 0);
        }

        public Commit_stmtContext commit_stmt() {
            return getRuleContext(Commit_stmtContext.class, 0);
        }

        public Compound_select_stmtContext compound_select_stmt() {
            return getRuleContext(Compound_select_stmtContext.class, 0);
        }

        public Create_index_stmtContext create_index_stmt() {
            return getRuleContext(Create_index_stmtContext.class, 0);
        }

        public Create_table_stmtContext create_table_stmt() {
            return getRuleContext(Create_table_stmtContext.class, 0);
        }

        public Create_trigger_stmtContext create_trigger_stmt() {
            return getRuleContext(Create_trigger_stmtContext.class, 0);
        }

        public Create_view_stmtContext create_view_stmt() {
            return getRuleContext(Create_view_stmtContext.class, 0);
        }

        public Create_virtual_table_stmtContext create_virtual_table_stmt() {
            return getRuleContext(Create_virtual_table_stmtContext.class, 0);
        }

        public Create_procedure_stmtContext create_procedure_stmt() {
            return getRuleContext(Create_procedure_stmtContext.class, 0);
        }

        public Execute_block_stmtContext execute_block_stmt() {
            return getRuleContext(Execute_block_stmtContext.class, 0);
        }

        public Delete_stmtContext delete_stmt() {
            return getRuleContext(Delete_stmtContext.class, 0);
        }

        public Delete_stmt_limitedContext delete_stmt_limited() {
            return getRuleContext(Delete_stmt_limitedContext.class, 0);
        }

        public Detach_stmtContext detach_stmt() {
            return getRuleContext(Detach_stmtContext.class, 0);
        }

        public Drop_index_stmtContext drop_index_stmt() {
            return getRuleContext(Drop_index_stmtContext.class, 0);
        }

        public Drop_table_stmtContext drop_table_stmt() {
            return getRuleContext(Drop_table_stmtContext.class, 0);
        }

        public Drop_trigger_stmtContext drop_trigger_stmt() {
            return getRuleContext(Drop_trigger_stmtContext.class, 0);
        }

        public Drop_view_stmtContext drop_view_stmt() {
            return getRuleContext(Drop_view_stmtContext.class, 0);
        }

        public Factored_select_stmtContext factored_select_stmt() {
            return getRuleContext(Factored_select_stmtContext.class, 0);
        }

        public Insert_stmtContext insert_stmt() {
            return getRuleContext(Insert_stmtContext.class, 0);
        }

        public Pragma_stmtContext pragma_stmt() {
            return getRuleContext(Pragma_stmtContext.class, 0);
        }

        public Reindex_stmtContext reindex_stmt() {
            return getRuleContext(Reindex_stmtContext.class, 0);
        }

        public Release_stmtContext release_stmt() {
            return getRuleContext(Release_stmtContext.class, 0);
        }

        public Rollback_stmtContext rollback_stmt() {
            return getRuleContext(Rollback_stmtContext.class, 0);
        }

        public Savepoint_stmtContext savepoint_stmt() {
            return getRuleContext(Savepoint_stmtContext.class, 0);
        }

        public Simple_select_stmtContext simple_select_stmt() {
            return getRuleContext(Simple_select_stmtContext.class, 0);
        }

        public Select_stmtContext select_stmt() {
            return getRuleContext(Select_stmtContext.class, 0);
        }

        public Update_stmtContext update_stmt() {
            return getRuleContext(Update_stmtContext.class, 0);
        }

        public Update_stmt_limitedContext update_stmt_limited() {
            return getRuleContext(Update_stmt_limitedContext.class, 0);
        }

        public Vacuum_stmtContext vacuum_stmt() {
            return getRuleContext(Vacuum_stmtContext.class, 0);
        }

        public TerminalNode K_EXPLAIN() {
            return getToken(REDDATABASESqlParser.K_EXPLAIN, 0);
        }

        public TerminalNode K_QUERY() {
            return getToken(REDDATABASESqlParser.K_QUERY, 0);
        }

        public TerminalNode K_PLAN() {
            return getToken(REDDATABASESqlParser.K_PLAN, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_sql_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterSql_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitSql_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSql_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Alter_table_stmtContext extends ParserRuleContext {
        public Alter_table_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ALTER() {
            return getToken(REDDATABASESqlParser.K_ALTER, 0);
        }

        public TerminalNode K_TABLE() {
            return getToken(REDDATABASESqlParser.K_TABLE, 0);
        }

        public Source_table_nameContext source_table_name() {
            return getRuleContext(Source_table_nameContext.class, 0);
        }

        public TerminalNode K_RENAME() {
            return getToken(REDDATABASESqlParser.K_RENAME, 0);
        }

        public TerminalNode K_TO() {
            return getToken(REDDATABASESqlParser.K_TO, 0);
        }

        public New_table_nameContext new_table_name() {
            return getRuleContext(New_table_nameContext.class, 0);
        }

        public Alter_table_addContext alter_table_add() {
            return getRuleContext(Alter_table_addContext.class, 0);
        }

        public Alter_table_add_constraintContext alter_table_add_constraint() {
            return getRuleContext(Alter_table_add_constraintContext.class, 0);
        }

        public TerminalNode K_ADD() {
            return getToken(REDDATABASESqlParser.K_ADD, 0);
        }

        public Column_defContext column_def() {
            return getRuleContext(Column_defContext.class, 0);
        }

        public TerminalNode K_ONLY() {
            return getToken(REDDATABASESqlParser.K_ONLY, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public TerminalNode K_ENABLE() {
            return getToken(REDDATABASESqlParser.K_ENABLE, 0);
        }

        public UnknownContext unknown() {
            return getRuleContext(UnknownContext.class, 0);
        }

        public TerminalNode K_COLUMN() {
            return getToken(REDDATABASESqlParser.K_COLUMN, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_alter_table_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterAlter_table_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitAlter_table_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitAlter_table_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Alter_table_add_constraintContext extends ParserRuleContext {
        public Alter_table_add_constraintContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ADD() {
            return getToken(REDDATABASESqlParser.K_ADD, 0);
        }

        public TerminalNode K_CONSTRAINT() {
            return getToken(REDDATABASESqlParser.K_CONSTRAINT, 0);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        public Table_constraintContext table_constraint() {
            return getRuleContext(Table_constraintContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_alter_table_add_constraint;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterAlter_table_add_constraint(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitAlter_table_add_constraint(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitAlter_table_add_constraint(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Alter_table_addContext extends ParserRuleContext {
        public Alter_table_addContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ADD() {
            return getToken(REDDATABASESqlParser.K_ADD, 0);
        }

        public Table_constraintContext table_constraint() {
            return getRuleContext(Table_constraintContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_alter_table_add;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterAlter_table_add(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitAlter_table_add(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitAlter_table_add(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Analyze_stmtContext extends ParserRuleContext {
        public Analyze_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ANALYZE() {
            return getToken(REDDATABASESqlParser.K_ANALYZE, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public Table_or_index_nameContext table_or_index_name() {
            return getRuleContext(Table_or_index_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_analyze_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterAnalyze_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitAnalyze_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitAnalyze_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Attach_stmtContext extends ParserRuleContext {
        public Attach_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ATTACH() {
            return getToken(REDDATABASESqlParser.K_ATTACH, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode K_AS() {
            return getToken(REDDATABASESqlParser.K_AS, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public TerminalNode K_DATABASE() {
            return getToken(REDDATABASESqlParser.K_DATABASE, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_attach_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterAttach_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitAttach_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitAttach_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Begin_stmtContext extends ParserRuleContext {
        public Begin_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_BEGIN() {
            return getToken(REDDATABASESqlParser.K_BEGIN, 0);
        }

        public TerminalNode K_TRANSACTION() {
            return getToken(REDDATABASESqlParser.K_TRANSACTION, 0);
        }

        public TerminalNode K_DEFERRED() {
            return getToken(REDDATABASESqlParser.K_DEFERRED, 0);
        }

        public TerminalNode K_IMMEDIATE() {
            return getToken(REDDATABASESqlParser.K_IMMEDIATE, 0);
        }

        public TerminalNode K_EXCLUSIVE() {
            return getToken(REDDATABASESqlParser.K_EXCLUSIVE, 0);
        }

        public Transaction_nameContext transaction_name() {
            return getRuleContext(Transaction_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_begin_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterBegin_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitBegin_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitBegin_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Commit_stmtContext extends ParserRuleContext {
        public Commit_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_COMMIT() {
            return getToken(REDDATABASESqlParser.K_COMMIT, 0);
        }

        public TerminalNode K_END() {
            return getToken(REDDATABASESqlParser.K_END, 0);
        }

        public TerminalNode K_TRANSACTION() {
            return getToken(REDDATABASESqlParser.K_TRANSACTION, 0);
        }

        public Transaction_nameContext transaction_name() {
            return getRuleContext(Transaction_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_commit_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterCommit_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitCommit_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCommit_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Compound_select_stmtContext extends ParserRuleContext {
        public Compound_select_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<Select_coreContext> select_core() {
            return getRuleContexts(Select_coreContext.class);
        }

        public Select_coreContext select_core(int i) {
            return getRuleContext(Select_coreContext.class, i);
        }

        public TerminalNode K_WITH() {
            return getToken(REDDATABASESqlParser.K_WITH, 0);
        }

        public List<Common_table_expressionContext> common_table_expression() {
            return getRuleContexts(Common_table_expressionContext.class);
        }

        public Common_table_expressionContext common_table_expression(int i) {
            return getRuleContext(Common_table_expressionContext.class, i);
        }

        public TerminalNode K_ORDER() {
            return getToken(REDDATABASESqlParser.K_ORDER, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public List<Ordering_termContext> ordering_term() {
            return getRuleContexts(Ordering_termContext.class);
        }

        public Ordering_termContext ordering_term(int i) {
            return getRuleContext(Ordering_termContext.class, i);
        }

        public TerminalNode K_LIMIT() {
            return getToken(REDDATABASESqlParser.K_LIMIT, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public List<TerminalNode> K_UNION() {
            return getTokens(REDDATABASESqlParser.K_UNION);
        }

        public TerminalNode K_UNION(int i) {
            return getToken(REDDATABASESqlParser.K_UNION, i);
        }

        public List<TerminalNode> K_INTERSECT() {
            return getTokens(REDDATABASESqlParser.K_INTERSECT);
        }

        public TerminalNode K_INTERSECT(int i) {
            return getToken(REDDATABASESqlParser.K_INTERSECT, i);
        }

        public List<TerminalNode> K_EXCEPT() {
            return getTokens(REDDATABASESqlParser.K_EXCEPT);
        }

        public TerminalNode K_EXCEPT(int i) {
            return getToken(REDDATABASESqlParser.K_EXCEPT, i);
        }

        public TerminalNode K_RECURSIVE() {
            return getToken(REDDATABASESqlParser.K_RECURSIVE, 0);
        }

        public TerminalNode K_OFFSET() {
            return getToken(REDDATABASESqlParser.K_OFFSET, 0);
        }

        public List<TerminalNode> K_ALL() {
            return getTokens(REDDATABASESqlParser.K_ALL);
        }

        public TerminalNode K_ALL(int i) {
            return getToken(REDDATABASESqlParser.K_ALL, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_compound_select_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCompound_select_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCompound_select_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCompound_select_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Create_index_stmtContext extends ParserRuleContext {
        public Create_index_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_CREATE() {
            return getToken(REDDATABASESqlParser.K_CREATE, 0);
        }

        public TerminalNode K_INDEX() {
            return getToken(REDDATABASESqlParser.K_INDEX, 0);
        }

        public Index_nameContext index_name() {
            return getRuleContext(Index_nameContext.class, 0);
        }

        public TerminalNode K_ON() {
            return getToken(REDDATABASESqlParser.K_ON, 0);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public List<Indexed_columnContext> indexed_column() {
            return getRuleContexts(Indexed_columnContext.class);
        }

        public Indexed_columnContext indexed_column(int i) {
            return getRuleContext(Indexed_columnContext.class, i);
        }

        public TerminalNode K_UNIQUE() {
            return getToken(REDDATABASESqlParser.K_UNIQUE, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public TerminalNode K_WHERE() {
            return getToken(REDDATABASESqlParser.K_WHERE, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_create_index_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCreate_index_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCreate_index_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCreate_index_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Create_table_stmtContext extends ParserRuleContext {
        public Create_table_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_CREATE() {
            return getToken(REDDATABASESqlParser.K_CREATE, 0);
        }

        public TerminalNode K_TABLE() {
            return getToken(REDDATABASESqlParser.K_TABLE, 0);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public List<Column_defContext> column_def() {
            return getRuleContexts(Column_defContext.class);
        }

        public Column_defContext column_def(int i) {
            return getRuleContext(Column_defContext.class, i);
        }

        public TerminalNode K_AS() {
            return getToken(REDDATABASESqlParser.K_AS, 0);
        }

        public Select_stmtContext select_stmt() {
            return getRuleContext(Select_stmtContext.class, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public UnknownContext unknown() {
            return getRuleContext(UnknownContext.class, 0);
        }

        public TerminalNode K_TEMP() {
            return getToken(REDDATABASESqlParser.K_TEMP, 0);
        }

        public TerminalNode K_TEMPORARY() {
            return getToken(REDDATABASESqlParser.K_TEMPORARY, 0);
        }

        public List<Table_constraintContext> table_constraint() {
            return getRuleContexts(Table_constraintContext.class);
        }

        public Table_constraintContext table_constraint(int i) {
            return getRuleContext(Table_constraintContext.class, i);
        }

        public TerminalNode K_WITHOUT() {
            return getToken(REDDATABASESqlParser.K_WITHOUT, 0);
        }

        public TerminalNode IDENTIFIER() {
            return getToken(REDDATABASESqlParser.IDENTIFIER, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_create_table_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCreate_table_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCreate_table_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCreate_table_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Create_trigger_stmtContext extends ParserRuleContext {
        public Create_trigger_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_CREATE() {
            return getToken(REDDATABASESqlParser.K_CREATE, 0);
        }

        public TerminalNode K_TRIGGER() {
            return getToken(REDDATABASESqlParser.K_TRIGGER, 0);
        }

        public Trigger_nameContext trigger_name() {
            return getRuleContext(Trigger_nameContext.class, 0);
        }

        public TerminalNode K_ON() {
            return getToken(REDDATABASESqlParser.K_ON, 0);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public TerminalNode K_BEGIN() {
            return getToken(REDDATABASESqlParser.K_BEGIN, 0);
        }

        public TerminalNode K_END() {
            return getToken(REDDATABASESqlParser.K_END, 0);
        }

        public TerminalNode K_DELETE() {
            return getToken(REDDATABASESqlParser.K_DELETE, 0);
        }

        public TerminalNode K_INSERT() {
            return getToken(REDDATABASESqlParser.K_INSERT, 0);
        }

        public TerminalNode K_UPDATE() {
            return getToken(REDDATABASESqlParser.K_UPDATE, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public List<Database_nameContext> database_name() {
            return getRuleContexts(Database_nameContext.class);
        }

        public Database_nameContext database_name(int i) {
            return getRuleContext(Database_nameContext.class, i);
        }

        public TerminalNode K_BEFORE() {
            return getToken(REDDATABASESqlParser.K_BEFORE, 0);
        }

        public TerminalNode K_AFTER() {
            return getToken(REDDATABASESqlParser.K_AFTER, 0);
        }

        public TerminalNode K_INSTEAD() {
            return getToken(REDDATABASESqlParser.K_INSTEAD, 0);
        }

        public List<TerminalNode> K_OF() {
            return getTokens(REDDATABASESqlParser.K_OF);
        }

        public TerminalNode K_OF(int i) {
            return getToken(REDDATABASESqlParser.K_OF, i);
        }

        public TerminalNode K_FOR() {
            return getToken(REDDATABASESqlParser.K_FOR, 0);
        }

        public TerminalNode K_EACH() {
            return getToken(REDDATABASESqlParser.K_EACH, 0);
        }

        public TerminalNode K_ROW() {
            return getToken(REDDATABASESqlParser.K_ROW, 0);
        }

        public TerminalNode K_WHEN() {
            return getToken(REDDATABASESqlParser.K_WHEN, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode K_TEMP() {
            return getToken(REDDATABASESqlParser.K_TEMP, 0);
        }

        public TerminalNode K_TEMPORARY() {
            return getToken(REDDATABASESqlParser.K_TEMPORARY, 0);
        }

        public List<Column_nameContext> column_name() {
            return getRuleContexts(Column_nameContext.class);
        }

        public Column_nameContext column_name(int i) {
            return getRuleContext(Column_nameContext.class, i);
        }

        public List<Update_stmtContext> update_stmt() {
            return getRuleContexts(Update_stmtContext.class);
        }

        public Update_stmtContext update_stmt(int i) {
            return getRuleContext(Update_stmtContext.class, i);
        }

        public List<Insert_stmtContext> insert_stmt() {
            return getRuleContexts(Insert_stmtContext.class);
        }

        public Insert_stmtContext insert_stmt(int i) {
            return getRuleContext(Insert_stmtContext.class, i);
        }

        public List<Delete_stmtContext> delete_stmt() {
            return getRuleContexts(Delete_stmtContext.class);
        }

        public Delete_stmtContext delete_stmt(int i) {
            return getRuleContext(Delete_stmtContext.class, i);
        }

        public List<Select_stmtContext> select_stmt() {
            return getRuleContexts(Select_stmtContext.class);
        }

        public Select_stmtContext select_stmt(int i) {
            return getRuleContext(Select_stmtContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_create_trigger_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCreate_trigger_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCreate_trigger_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCreate_trigger_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Create_view_stmtContext extends ParserRuleContext {
        public Create_view_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_CREATE() {
            return getToken(REDDATABASESqlParser.K_CREATE, 0);
        }

        public TerminalNode K_VIEW() {
            return getToken(REDDATABASESqlParser.K_VIEW, 0);
        }

        public View_nameContext view_name() {
            return getRuleContext(View_nameContext.class, 0);
        }

        public TerminalNode K_AS() {
            return getToken(REDDATABASESqlParser.K_AS, 0);
        }

        public Select_stmtContext select_stmt() {
            return getRuleContext(Select_stmtContext.class, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public TerminalNode K_TEMP() {
            return getToken(REDDATABASESqlParser.K_TEMP, 0);
        }

        public TerminalNode K_TEMPORARY() {
            return getToken(REDDATABASESqlParser.K_TEMPORARY, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_create_view_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCreate_view_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCreate_view_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCreate_view_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Create_virtual_table_stmtContext extends ParserRuleContext {
        public Create_virtual_table_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_CREATE() {
            return getToken(REDDATABASESqlParser.K_CREATE, 0);
        }

        public TerminalNode K_VIRTUAL() {
            return getToken(REDDATABASESqlParser.K_VIRTUAL, 0);
        }

        public TerminalNode K_TABLE() {
            return getToken(REDDATABASESqlParser.K_TABLE, 0);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public TerminalNode K_USING() {
            return getToken(REDDATABASESqlParser.K_USING, 0);
        }

        public Module_nameContext module_name() {
            return getRuleContext(Module_nameContext.class, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public List<Module_argumentContext> module_argument() {
            return getRuleContexts(Module_argumentContext.class);
        }

        public Module_argumentContext module_argument(int i) {
            return getRuleContext(Module_argumentContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_create_virtual_table_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCreate_virtual_table_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCreate_virtual_table_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCreate_virtual_table_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Create_procedure_stmtContext extends ParserRuleContext {
        public Create_procedure_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_CREATE() {
            return getToken(REDDATABASESqlParser.K_CREATE, 0);
        }

        public TerminalNode K_PROCEDURE() {
            return getToken(REDDATABASESqlParser.K_PROCEDURE, 0);
        }

        public Procedure_nameContext procedure_name() {
            return getRuleContext(Procedure_nameContext.class, 0);
        }

        public Declare_blockContext declare_block() {
            return getRuleContext(Declare_blockContext.class, 0);
        }

        public TerminalNode K_AUTHID() {
            return getToken(REDDATABASESqlParser.K_AUTHID, 0);
        }

        public TerminalNode K_OWNER() {
            return getToken(REDDATABASESqlParser.K_OWNER, 0);
        }

        public TerminalNode K_CALLER() {
            return getToken(REDDATABASESqlParser.K_CALLER, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_create_procedure_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCreate_procedure_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCreate_procedure_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCreate_procedure_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Execute_block_stmtContext extends ParserRuleContext {
        public Execute_block_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_EXECUTE() {
            return getToken(REDDATABASESqlParser.K_EXECUTE, 0);
        }

        public TerminalNode K_BLOCK() {
            return getToken(REDDATABASESqlParser.K_BLOCK, 0);
        }

        public Declare_blockContext declare_block() {
            return getRuleContext(Declare_blockContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_execute_block_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterExecute_block_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitExecute_block_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitExecute_block_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Declare_blockContext extends ParserRuleContext {
        public Declare_blockContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_AS() {
            return getToken(REDDATABASESqlParser.K_AS, 0);
        }

        public TerminalNode K_BEGIN() {
            return getToken(REDDATABASESqlParser.K_BEGIN, 0);
        }

        public BodyContext body() {
            return getRuleContext(BodyContext.class, 0);
        }

        public TerminalNode K_END() {
            return getToken(REDDATABASESqlParser.K_END, 0);
        }

        public List<Input_parameterContext> input_parameter() {
            return getRuleContexts(Input_parameterContext.class);
        }

        public Input_parameterContext input_parameter(int i) {
            return getRuleContext(Input_parameterContext.class, i);
        }

        public TerminalNode K_RETURNS() {
            return getToken(REDDATABASESqlParser.K_RETURNS, 0);
        }

        public List<Output_parameterContext> output_parameter() {
            return getRuleContexts(Output_parameterContext.class);
        }

        public Output_parameterContext output_parameter(int i) {
            return getRuleContext(Output_parameterContext.class, i);
        }

        public List<Local_variableContext> local_variable() {
            return getRuleContexts(Local_variableContext.class);
        }

        public Local_variableContext local_variable(int i) {
            return getRuleContext(Local_variableContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_declare_block;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterDeclare_block(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitDeclare_block(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDeclare_block(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class BodyContext extends ParserRuleContext {
        public BodyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_BEGIN() {
            return getToken(REDDATABASESqlParser.K_BEGIN, 0);
        }

        public BodyContext body() {
            return getRuleContext(BodyContext.class, 0);
        }

        public TerminalNode K_END() {
            return getToken(REDDATABASESqlParser.K_END, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_body;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterBody(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitBody(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitBody(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Local_variableContext extends ParserRuleContext {
        public Local_variableContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_DECLARE() {
            return getToken(REDDATABASESqlParser.K_DECLARE, 0);
        }

        public Variable_nameContext variable_name() {
            return getRuleContext(Variable_nameContext.class, 0);
        }

        public DatatypeContext datatype() {
            return getRuleContext(DatatypeContext.class, 0);
        }

        public TerminalNode K_VARIABLE() {
            return getToken(REDDATABASESqlParser.K_VARIABLE, 0);
        }

        public TerminalNode K_NOTNULL() {
            return getToken(REDDATABASESqlParser.K_NOTNULL, 0);
        }

        public TerminalNode K_COLLATE() {
            return getToken(REDDATABASESqlParser.K_COLLATE, 0);
        }

        public Order_collateContext order_collate() {
            return getRuleContext(Order_collateContext.class, 0);
        }

        public Default_valueContext default_value() {
            return getRuleContext(Default_valueContext.class, 0);
        }

        public TerminalNode K_DEFAULT() {
            return getToken(REDDATABASESqlParser.K_DEFAULT, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_local_variable;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterLocal_variable(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitLocal_variable(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitLocal_variable(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Output_parameterContext extends ParserRuleContext {
        public Output_parameterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Desciption_parameterContext desciption_parameter() {
            return getRuleContext(Desciption_parameterContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_output_parameter;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterOutput_parameter(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitOutput_parameter(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitOutput_parameter(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Default_valueContext extends ParserRuleContext {
        public Default_valueContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Literal_valueContext literal_value() {
            return getRuleContext(Literal_valueContext.class, 0);
        }

        public TerminalNode BIND_PARAMETER() {
            return getToken(REDDATABASESqlParser.BIND_PARAMETER, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_default_value;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterDefault_value(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitDefault_value(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDefault_value(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Variable_nameContext extends ParserRuleContext {
        public Variable_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_variable_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterVariable_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitVariable_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitVariable_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Input_parameterContext extends ParserRuleContext {
        public Input_parameterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Desciption_parameterContext desciption_parameter() {
            return getRuleContext(Desciption_parameterContext.class, 0);
        }

        public Default_valueContext default_value() {
            return getRuleContext(Default_valueContext.class, 0);
        }

        public TerminalNode K_DEFAULT() {
            return getToken(REDDATABASESqlParser.K_DEFAULT, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_input_parameter;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterInput_parameter(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitInput_parameter(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitInput_parameter(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Desciption_parameterContext extends ParserRuleContext {
        public Desciption_parameterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Parameter_nameContext parameter_name() {
            return getRuleContext(Parameter_nameContext.class, 0);
        }

        public DatatypeContext datatype() {
            return getRuleContext(DatatypeContext.class, 0);
        }

        public TerminalNode K_NOTNULL() {
            return getToken(REDDATABASESqlParser.K_NOTNULL, 0);
        }

        public TerminalNode K_COLLATE() {
            return getToken(REDDATABASESqlParser.K_COLLATE, 0);
        }

        public Order_collateContext order_collate() {
            return getRuleContext(Order_collateContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_desciption_parameter;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterDesciption_parameter(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitDesciption_parameter(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDesciption_parameter(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Parameter_nameContext extends ParserRuleContext {
        public Parameter_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_parameter_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterParameter_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitParameter_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitParameter_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class DatatypeContext extends ParserRuleContext {
        public DatatypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public DatatypeSQLContext datatypeSQL() {
            return getRuleContext(DatatypeSQLContext.class, 0);
        }

        public Domain_nameContext domain_name() {
            return getRuleContext(Domain_nameContext.class, 0);
        }

        public TerminalNode K_TYPE_OF() {
            return getToken(REDDATABASESqlParser.K_TYPE_OF, 0);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public Column_nameContext column_name() {
            return getRuleContext(Column_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_datatype;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterDatatype(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitDatatype(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDatatype(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class DatatypeSQLContext extends ParserRuleContext {
        public DatatypeSQLContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode SMALLINT() {
            return getToken(REDDATABASESqlParser.SMALLINT, 0);
        }

        public TerminalNode INTEGER() {
            return getToken(REDDATABASESqlParser.INTEGER, 0);
        }

        public TerminalNode BIGINT() {
            return getToken(REDDATABASESqlParser.BIGINT, 0);
        }

        public Array_sizeContext array_size() {
            return getRuleContext(Array_sizeContext.class, 0);
        }

        public TerminalNode FLOAT() {
            return getToken(REDDATABASESqlParser.FLOAT, 0);
        }

        public TerminalNode DOUBLE() {
            return getToken(REDDATABASESqlParser.DOUBLE, 0);
        }

        public TerminalNode PRECISION() {
            return getToken(REDDATABASESqlParser.PRECISION, 0);
        }

        public TerminalNode DATE() {
            return getToken(REDDATABASESqlParser.DATE, 0);
        }

        public TerminalNode TIME() {
            return getToken(REDDATABASESqlParser.TIME, 0);
        }

        public TerminalNode TIMESTAMP() {
            return getToken(REDDATABASESqlParser.TIMESTAMP, 0);
        }

        public TerminalNode DECIMAL() {
            return getToken(REDDATABASESqlParser.DECIMAL, 0);
        }

        public TerminalNode NUMERIC() {
            return getToken(REDDATABASESqlParser.NUMERIC, 0);
        }

        public List<Int_numberContext> int_number() {
            return getRuleContexts(Int_numberContext.class);
        }

        public Int_numberContext int_number(int i) {
            return getRuleContext(Int_numberContext.class, i);
        }

        public TerminalNode CHAR() {
            return getToken(REDDATABASESqlParser.CHAR, 0);
        }

        public TerminalNode CHARACTER() {
            return getToken(REDDATABASESqlParser.CHARACTER, 0);
        }

        public TerminalNode CHARACTER_VARYING() {
            return getToken(REDDATABASESqlParser.CHARACTER_VARYING, 0);
        }

        public TerminalNode VARCHAR() {
            return getToken(REDDATABASESqlParser.VARCHAR, 0);
        }

        public TerminalNode CHARACTER_SET() {
            return getToken(REDDATABASESqlParser.CHARACTER_SET, 0);
        }

        public Charset_nameContext charset_name() {
            return getRuleContext(Charset_nameContext.class, 0);
        }

        public TerminalNode NCHAR() {
            return getToken(REDDATABASESqlParser.NCHAR, 0);
        }

        public TerminalNode NATIONAL_CHARACTER() {
            return getToken(REDDATABASESqlParser.NATIONAL_CHARACTER, 0);
        }

        public TerminalNode NATIONAL_CHAR() {
            return getToken(REDDATABASESqlParser.NATIONAL_CHAR, 0);
        }

        public TerminalNode VARYING() {
            return getToken(REDDATABASESqlParser.VARYING, 0);
        }

        public List<TerminalNode> DIGIT() {
            return getTokens(REDDATABASESqlParser.DIGIT);
        }

        public TerminalNode DIGIT(int i) {
            return getToken(REDDATABASESqlParser.DIGIT, i);
        }

        public TerminalNode BLOB() {
            return getToken(REDDATABASESqlParser.BLOB, 0);
        }

        public TerminalNode SUB_TYPE() {
            return getToken(REDDATABASESqlParser.SUB_TYPE, 0);
        }

        public TerminalNode SEGMENT() {
            return getToken(REDDATABASESqlParser.SEGMENT, 0);
        }

        public TerminalNode SIZE() {
            return getToken(REDDATABASESqlParser.SIZE, 0);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        public Segment_sizeContext segment_size() {
            return getRuleContext(Segment_sizeContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_datatypeSQL;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterDatatypeSQL(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitDatatypeSQL(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDatatypeSQL(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Segment_sizeContext extends ParserRuleContext {
        public Segment_sizeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Int_numberContext int_number() {
            return getRuleContext(Int_numberContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_segment_size;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterSegment_size(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitSegment_size(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSegment_size(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Int_numberContext extends ParserRuleContext {
        public Int_numberContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<TerminalNode> DIGIT() {
            return getTokens(REDDATABASESqlParser.DIGIT);
        }

        public TerminalNode DIGIT(int i) {
            return getToken(REDDATABASESqlParser.DIGIT, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_int_number;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterInt_number(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitInt_number(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitInt_number(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Array_sizeContext extends ParserRuleContext {
        public Array_sizeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<TerminalNode> DIGIT() {
            return getTokens(REDDATABASESqlParser.DIGIT);
        }

        public TerminalNode DIGIT(int i) {
            return getToken(REDDATABASESqlParser.DIGIT, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_array_size;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterArray_size(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitArray_size(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitArray_size(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Delete_stmtContext extends ParserRuleContext {
        public Delete_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_DELETE() {
            return getToken(REDDATABASESqlParser.K_DELETE, 0);
        }

        public TerminalNode K_FROM() {
            return getToken(REDDATABASESqlParser.K_FROM, 0);
        }

        public Qualified_table_nameContext qualified_table_name() {
            return getRuleContext(Qualified_table_nameContext.class, 0);
        }

        public With_clauseContext with_clause() {
            return getRuleContext(With_clauseContext.class, 0);
        }

        public TerminalNode K_WHERE() {
            return getToken(REDDATABASESqlParser.K_WHERE, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_delete_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterDelete_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitDelete_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDelete_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Delete_stmt_limitedContext extends ParserRuleContext {
        public Delete_stmt_limitedContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_DELETE() {
            return getToken(REDDATABASESqlParser.K_DELETE, 0);
        }

        public TerminalNode K_FROM() {
            return getToken(REDDATABASESqlParser.K_FROM, 0);
        }

        public Qualified_table_nameContext qualified_table_name() {
            return getRuleContext(Qualified_table_nameContext.class, 0);
        }

        public With_clauseContext with_clause() {
            return getRuleContext(With_clauseContext.class, 0);
        }

        public TerminalNode K_WHERE() {
            return getToken(REDDATABASESqlParser.K_WHERE, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode K_LIMIT() {
            return getToken(REDDATABASESqlParser.K_LIMIT, 0);
        }

        public TerminalNode K_ORDER() {
            return getToken(REDDATABASESqlParser.K_ORDER, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public List<Ordering_termContext> ordering_term() {
            return getRuleContexts(Ordering_termContext.class);
        }

        public Ordering_termContext ordering_term(int i) {
            return getRuleContext(Ordering_termContext.class, i);
        }

        public TerminalNode K_OFFSET() {
            return getToken(REDDATABASESqlParser.K_OFFSET, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_delete_stmt_limited;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterDelete_stmt_limited(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitDelete_stmt_limited(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDelete_stmt_limited(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Detach_stmtContext extends ParserRuleContext {
        public Detach_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_DETACH() {
            return getToken(REDDATABASESqlParser.K_DETACH, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public TerminalNode K_DATABASE() {
            return getToken(REDDATABASESqlParser.K_DATABASE, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_detach_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterDetach_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitDetach_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDetach_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Drop_index_stmtContext extends ParserRuleContext {
        public Drop_index_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_DROP() {
            return getToken(REDDATABASESqlParser.K_DROP, 0);
        }

        public TerminalNode K_INDEX() {
            return getToken(REDDATABASESqlParser.K_INDEX, 0);
        }

        public Index_nameContext index_name() {
            return getRuleContext(Index_nameContext.class, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_drop_index_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterDrop_index_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitDrop_index_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDrop_index_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Drop_table_stmtContext extends ParserRuleContext {
        public Drop_table_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_DROP() {
            return getToken(REDDATABASESqlParser.K_DROP, 0);
        }

        public TerminalNode K_TABLE() {
            return getToken(REDDATABASESqlParser.K_TABLE, 0);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_drop_table_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterDrop_table_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitDrop_table_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDrop_table_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Drop_trigger_stmtContext extends ParserRuleContext {
        public Drop_trigger_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_DROP() {
            return getToken(REDDATABASESqlParser.K_DROP, 0);
        }

        public TerminalNode K_TRIGGER() {
            return getToken(REDDATABASESqlParser.K_TRIGGER, 0);
        }

        public Trigger_nameContext trigger_name() {
            return getRuleContext(Trigger_nameContext.class, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_drop_trigger_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterDrop_trigger_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitDrop_trigger_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDrop_trigger_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Drop_view_stmtContext extends ParserRuleContext {
        public Drop_view_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_DROP() {
            return getToken(REDDATABASESqlParser.K_DROP, 0);
        }

        public TerminalNode K_VIEW() {
            return getToken(REDDATABASESqlParser.K_VIEW, 0);
        }

        public View_nameContext view_name() {
            return getRuleContext(View_nameContext.class, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_drop_view_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterDrop_view_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitDrop_view_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDrop_view_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Factored_select_stmtContext extends ParserRuleContext {
        public Factored_select_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<Select_coreContext> select_core() {
            return getRuleContexts(Select_coreContext.class);
        }

        public Select_coreContext select_core(int i) {
            return getRuleContext(Select_coreContext.class, i);
        }

        public TerminalNode K_WITH() {
            return getToken(REDDATABASESqlParser.K_WITH, 0);
        }

        public List<Common_table_expressionContext> common_table_expression() {
            return getRuleContexts(Common_table_expressionContext.class);
        }

        public Common_table_expressionContext common_table_expression(int i) {
            return getRuleContext(Common_table_expressionContext.class, i);
        }

        public List<Compound_operatorContext> compound_operator() {
            return getRuleContexts(Compound_operatorContext.class);
        }

        public Compound_operatorContext compound_operator(int i) {
            return getRuleContext(Compound_operatorContext.class, i);
        }

        public TerminalNode K_ORDER() {
            return getToken(REDDATABASESqlParser.K_ORDER, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public List<Ordering_termContext> ordering_term() {
            return getRuleContexts(Ordering_termContext.class);
        }

        public Ordering_termContext ordering_term(int i) {
            return getRuleContext(Ordering_termContext.class, i);
        }

        public TerminalNode K_LIMIT() {
            return getToken(REDDATABASESqlParser.K_LIMIT, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode K_RECURSIVE() {
            return getToken(REDDATABASESqlParser.K_RECURSIVE, 0);
        }

        public TerminalNode K_OFFSET() {
            return getToken(REDDATABASESqlParser.K_OFFSET, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_factored_select_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterFactored_select_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitFactored_select_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitFactored_select_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Insert_stmtContext extends ParserRuleContext {
        public Insert_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_INTO() {
            return getToken(REDDATABASESqlParser.K_INTO, 0);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public TerminalNode K_INSERT() {
            return getToken(REDDATABASESqlParser.K_INSERT, 0);
        }

        public TerminalNode K_REPLACE() {
            return getToken(REDDATABASESqlParser.K_REPLACE, 0);
        }

        public TerminalNode K_OR() {
            return getToken(REDDATABASESqlParser.K_OR, 0);
        }

        public TerminalNode K_ROLLBACK() {
            return getToken(REDDATABASESqlParser.K_ROLLBACK, 0);
        }

        public TerminalNode K_ABORT() {
            return getToken(REDDATABASESqlParser.K_ABORT, 0);
        }

        public TerminalNode K_FAIL() {
            return getToken(REDDATABASESqlParser.K_FAIL, 0);
        }

        public TerminalNode K_IGNORE() {
            return getToken(REDDATABASESqlParser.K_IGNORE, 0);
        }

        public TerminalNode K_VALUES() {
            return getToken(REDDATABASESqlParser.K_VALUES, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public Select_stmtContext select_stmt() {
            return getRuleContext(Select_stmtContext.class, 0);
        }

        public TerminalNode K_DEFAULT() {
            return getToken(REDDATABASESqlParser.K_DEFAULT, 0);
        }

        public With_clauseContext with_clause() {
            return getRuleContext(With_clauseContext.class, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public List<Column_nameContext> column_name() {
            return getRuleContexts(Column_nameContext.class);
        }

        public Column_nameContext column_name(int i) {
            return getRuleContext(Column_nameContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_insert_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterInsert_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitInsert_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitInsert_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Pragma_stmtContext extends ParserRuleContext {
        public Pragma_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_PRAGMA() {
            return getToken(REDDATABASESqlParser.K_PRAGMA, 0);
        }

        public Pragma_nameContext pragma_name() {
            return getRuleContext(Pragma_nameContext.class, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public Pragma_valueContext pragma_value() {
            return getRuleContext(Pragma_valueContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_pragma_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterPragma_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitPragma_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitPragma_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Reindex_stmtContext extends ParserRuleContext {
        public Reindex_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_REINDEX() {
            return getToken(REDDATABASESqlParser.K_REINDEX, 0);
        }

        public Collation_nameContext collation_name() {
            return getRuleContext(Collation_nameContext.class, 0);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public Index_nameContext index_name() {
            return getRuleContext(Index_nameContext.class, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_reindex_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterReindex_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitReindex_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitReindex_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Release_stmtContext extends ParserRuleContext {
        public Release_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_RELEASE() {
            return getToken(REDDATABASESqlParser.K_RELEASE, 0);
        }

        public Savepoint_nameContext savepoint_name() {
            return getRuleContext(Savepoint_nameContext.class, 0);
        }

        public TerminalNode K_SAVEPOINT() {
            return getToken(REDDATABASESqlParser.K_SAVEPOINT, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_release_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterRelease_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitRelease_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitRelease_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Rollback_stmtContext extends ParserRuleContext {
        public Rollback_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ROLLBACK() {
            return getToken(REDDATABASESqlParser.K_ROLLBACK, 0);
        }

        public TerminalNode K_TRANSACTION() {
            return getToken(REDDATABASESqlParser.K_TRANSACTION, 0);
        }

        public TerminalNode K_TO() {
            return getToken(REDDATABASESqlParser.K_TO, 0);
        }

        public Savepoint_nameContext savepoint_name() {
            return getRuleContext(Savepoint_nameContext.class, 0);
        }

        public Transaction_nameContext transaction_name() {
            return getRuleContext(Transaction_nameContext.class, 0);
        }

        public TerminalNode K_SAVEPOINT() {
            return getToken(REDDATABASESqlParser.K_SAVEPOINT, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_rollback_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterRollback_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitRollback_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitRollback_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Savepoint_stmtContext extends ParserRuleContext {
        public Savepoint_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_SAVEPOINT() {
            return getToken(REDDATABASESqlParser.K_SAVEPOINT, 0);
        }

        public Savepoint_nameContext savepoint_name() {
            return getRuleContext(Savepoint_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_savepoint_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterSavepoint_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitSavepoint_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSavepoint_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Simple_select_stmtContext extends ParserRuleContext {
        public Simple_select_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Select_coreContext select_core() {
            return getRuleContext(Select_coreContext.class, 0);
        }

        public TerminalNode K_WITH() {
            return getToken(REDDATABASESqlParser.K_WITH, 0);
        }

        public List<Common_table_expressionContext> common_table_expression() {
            return getRuleContexts(Common_table_expressionContext.class);
        }

        public Common_table_expressionContext common_table_expression(int i) {
            return getRuleContext(Common_table_expressionContext.class, i);
        }

        public TerminalNode K_ORDER() {
            return getToken(REDDATABASESqlParser.K_ORDER, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public List<Ordering_termContext> ordering_term() {
            return getRuleContexts(Ordering_termContext.class);
        }

        public Ordering_termContext ordering_term(int i) {
            return getRuleContext(Ordering_termContext.class, i);
        }

        public TerminalNode K_LIMIT() {
            return getToken(REDDATABASESqlParser.K_LIMIT, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode K_RECURSIVE() {
            return getToken(REDDATABASESqlParser.K_RECURSIVE, 0);
        }

        public TerminalNode K_OFFSET() {
            return getToken(REDDATABASESqlParser.K_OFFSET, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_simple_select_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterSimple_select_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitSimple_select_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSimple_select_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Select_stmtContext extends ParserRuleContext {
        public Select_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<Select_or_valuesContext> select_or_values() {
            return getRuleContexts(Select_or_valuesContext.class);
        }

        public Select_or_valuesContext select_or_values(int i) {
            return getRuleContext(Select_or_valuesContext.class, i);
        }

        public TerminalNode K_WITH() {
            return getToken(REDDATABASESqlParser.K_WITH, 0);
        }

        public List<Common_table_expressionContext> common_table_expression() {
            return getRuleContexts(Common_table_expressionContext.class);
        }

        public Common_table_expressionContext common_table_expression(int i) {
            return getRuleContext(Common_table_expressionContext.class, i);
        }

        public List<Compound_operatorContext> compound_operator() {
            return getRuleContexts(Compound_operatorContext.class);
        }

        public Compound_operatorContext compound_operator(int i) {
            return getRuleContext(Compound_operatorContext.class, i);
        }

        public TerminalNode K_ORDER() {
            return getToken(REDDATABASESqlParser.K_ORDER, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public List<Ordering_termContext> ordering_term() {
            return getRuleContexts(Ordering_termContext.class);
        }

        public Ordering_termContext ordering_term(int i) {
            return getRuleContext(Ordering_termContext.class, i);
        }

        public TerminalNode K_LIMIT() {
            return getToken(REDDATABASESqlParser.K_LIMIT, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode K_RECURSIVE() {
            return getToken(REDDATABASESqlParser.K_RECURSIVE, 0);
        }

        public TerminalNode K_OFFSET() {
            return getToken(REDDATABASESqlParser.K_OFFSET, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_select_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterSelect_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitSelect_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSelect_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Select_or_valuesContext extends ParserRuleContext {
        public Select_or_valuesContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_SELECT() {
            return getToken(REDDATABASESqlParser.K_SELECT, 0);
        }

        public List<Result_columnContext> result_column() {
            return getRuleContexts(Result_columnContext.class);
        }

        public Result_columnContext result_column(int i) {
            return getRuleContext(Result_columnContext.class, i);
        }

        public TerminalNode K_FROM() {
            return getToken(REDDATABASESqlParser.K_FROM, 0);
        }

        public TerminalNode K_WHERE() {
            return getToken(REDDATABASESqlParser.K_WHERE, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode K_GROUP() {
            return getToken(REDDATABASESqlParser.K_GROUP, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public TerminalNode K_DISTINCT() {
            return getToken(REDDATABASESqlParser.K_DISTINCT, 0);
        }

        public TerminalNode K_ALL() {
            return getToken(REDDATABASESqlParser.K_ALL, 0);
        }

        public List<Table_or_subqueryContext> table_or_subquery() {
            return getRuleContexts(Table_or_subqueryContext.class);
        }

        public Table_or_subqueryContext table_or_subquery(int i) {
            return getRuleContext(Table_or_subqueryContext.class, i);
        }

        public Join_clauseContext join_clause() {
            return getRuleContext(Join_clauseContext.class, 0);
        }

        public TerminalNode K_HAVING() {
            return getToken(REDDATABASESqlParser.K_HAVING, 0);
        }

        public TerminalNode K_VALUES() {
            return getToken(REDDATABASESqlParser.K_VALUES, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_select_or_values;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterSelect_or_values(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitSelect_or_values(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSelect_or_values(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Update_stmtContext extends ParserRuleContext {
        public Update_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_UPDATE() {
            return getToken(REDDATABASESqlParser.K_UPDATE, 0);
        }

        public Qualified_table_nameContext qualified_table_name() {
            return getRuleContext(Qualified_table_nameContext.class, 0);
        }

        public TerminalNode K_SET() {
            return getToken(REDDATABASESqlParser.K_SET, 0);
        }

        public List<Column_nameContext> column_name() {
            return getRuleContexts(Column_nameContext.class);
        }

        public Column_nameContext column_name(int i) {
            return getRuleContext(Column_nameContext.class, i);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public With_clauseContext with_clause() {
            return getRuleContext(With_clauseContext.class, 0);
        }

        public TerminalNode K_OR() {
            return getToken(REDDATABASESqlParser.K_OR, 0);
        }

        public TerminalNode K_ROLLBACK() {
            return getToken(REDDATABASESqlParser.K_ROLLBACK, 0);
        }

        public TerminalNode K_ABORT() {
            return getToken(REDDATABASESqlParser.K_ABORT, 0);
        }

        public TerminalNode K_REPLACE() {
            return getToken(REDDATABASESqlParser.K_REPLACE, 0);
        }

        public TerminalNode K_FAIL() {
            return getToken(REDDATABASESqlParser.K_FAIL, 0);
        }

        public TerminalNode K_IGNORE() {
            return getToken(REDDATABASESqlParser.K_IGNORE, 0);
        }

        public TerminalNode K_WHERE() {
            return getToken(REDDATABASESqlParser.K_WHERE, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_update_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterUpdate_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitUpdate_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitUpdate_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Update_stmt_limitedContext extends ParserRuleContext {
        public Update_stmt_limitedContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_UPDATE() {
            return getToken(REDDATABASESqlParser.K_UPDATE, 0);
        }

        public Qualified_table_nameContext qualified_table_name() {
            return getRuleContext(Qualified_table_nameContext.class, 0);
        }

        public TerminalNode K_SET() {
            return getToken(REDDATABASESqlParser.K_SET, 0);
        }

        public List<Column_nameContext> column_name() {
            return getRuleContexts(Column_nameContext.class);
        }

        public Column_nameContext column_name(int i) {
            return getRuleContext(Column_nameContext.class, i);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public With_clauseContext with_clause() {
            return getRuleContext(With_clauseContext.class, 0);
        }

        public TerminalNode K_OR() {
            return getToken(REDDATABASESqlParser.K_OR, 0);
        }

        public TerminalNode K_ROLLBACK() {
            return getToken(REDDATABASESqlParser.K_ROLLBACK, 0);
        }

        public TerminalNode K_ABORT() {
            return getToken(REDDATABASESqlParser.K_ABORT, 0);
        }

        public TerminalNode K_REPLACE() {
            return getToken(REDDATABASESqlParser.K_REPLACE, 0);
        }

        public TerminalNode K_FAIL() {
            return getToken(REDDATABASESqlParser.K_FAIL, 0);
        }

        public TerminalNode K_IGNORE() {
            return getToken(REDDATABASESqlParser.K_IGNORE, 0);
        }

        public TerminalNode K_WHERE() {
            return getToken(REDDATABASESqlParser.K_WHERE, 0);
        }

        public TerminalNode K_LIMIT() {
            return getToken(REDDATABASESqlParser.K_LIMIT, 0);
        }

        public TerminalNode K_ORDER() {
            return getToken(REDDATABASESqlParser.K_ORDER, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public List<Ordering_termContext> ordering_term() {
            return getRuleContexts(Ordering_termContext.class);
        }

        public Ordering_termContext ordering_term(int i) {
            return getRuleContext(Ordering_termContext.class, i);
        }

        public TerminalNode K_OFFSET() {
            return getToken(REDDATABASESqlParser.K_OFFSET, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_update_stmt_limited;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterUpdate_stmt_limited(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitUpdate_stmt_limited(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitUpdate_stmt_limited(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Vacuum_stmtContext extends ParserRuleContext {
        public Vacuum_stmtContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_VACUUM() {
            return getToken(REDDATABASESqlParser.K_VACUUM, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_vacuum_stmt;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterVacuum_stmt(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitVacuum_stmt(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitVacuum_stmt(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_defContext extends ParserRuleContext {
        public Column_defContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Column_nameContext column_name() {
            return getRuleContext(Column_nameContext.class, 0);
        }

        public List<Column_constraintContext> column_constraint() {
            return getRuleContexts(Column_constraintContext.class);
        }

        public Column_constraintContext column_constraint(int i) {
            return getRuleContext(Column_constraintContext.class, i);
        }

        public List<Type_nameContext> type_name() {
            return getRuleContexts(Type_nameContext.class);
        }

        public Type_nameContext type_name(int i) {
            return getRuleContext(Type_nameContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_def;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterColumn_def(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitColumn_def(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_def(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Type_nameContext extends ParserRuleContext {
        public Type_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        public List<Signed_numberContext> signed_number() {
            return getRuleContexts(Signed_numberContext.class);
        }

        public Signed_numberContext signed_number(int i) {
            return getRuleContext(Signed_numberContext.class, i);
        }

        public List<Any_nameContext> any_name() {
            return getRuleContexts(Any_nameContext.class);
        }

        public Any_nameContext any_name(int i) {
            return getRuleContext(Any_nameContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_type_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterType_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitType_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitType_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_constraintContext extends ParserRuleContext {
        public Column_constraintContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Column_constraint_primary_keyContext column_constraint_primary_key() {
            return getRuleContext(Column_constraint_primary_keyContext.class, 0);
        }

        public Column_constraint_foreign_keyContext column_constraint_foreign_key() {
            return getRuleContext(Column_constraint_foreign_keyContext.class, 0);
        }

        public Column_constraint_not_nullContext column_constraint_not_null() {
            return getRuleContext(Column_constraint_not_nullContext.class, 0);
        }

        public Column_constraint_nullContext column_constraint_null() {
            return getRuleContext(Column_constraint_nullContext.class, 0);
        }

        public TerminalNode K_UNIQUE() {
            return getToken(REDDATABASESqlParser.K_UNIQUE, 0);
        }

        public Conflict_clauseContext conflict_clause() {
            return getRuleContext(Conflict_clauseContext.class, 0);
        }

        public TerminalNode K_CHECK() {
            return getToken(REDDATABASESqlParser.K_CHECK, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public Column_defaultContext column_default() {
            return getRuleContext(Column_defaultContext.class, 0);
        }

        public TerminalNode K_COLLATE() {
            return getToken(REDDATABASESqlParser.K_COLLATE, 0);
        }

        public Collation_nameContext collation_name() {
            return getRuleContext(Collation_nameContext.class, 0);
        }

        public TerminalNode K_CONSTRAINT() {
            return getToken(REDDATABASESqlParser.K_CONSTRAINT, 0);
        }

        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_constraint;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterColumn_constraint(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitColumn_constraint(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_constraint(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_constraint_primary_keyContext extends ParserRuleContext {
        public Column_constraint_primary_keyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_PRIMARY() {
            return getToken(REDDATABASESqlParser.K_PRIMARY, 0);
        }

        public TerminalNode K_KEY() {
            return getToken(REDDATABASESqlParser.K_KEY, 0);
        }

        public Conflict_clauseContext conflict_clause() {
            return getRuleContext(Conflict_clauseContext.class, 0);
        }

        public TerminalNode K_AUTOINCREMENT() {
            return getToken(REDDATABASESqlParser.K_AUTOINCREMENT, 0);
        }

        public TerminalNode K_ASC() {
            return getToken(REDDATABASESqlParser.K_ASC, 0);
        }

        public TerminalNode K_DESC() {
            return getToken(REDDATABASESqlParser.K_DESC, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_constraint_primary_key;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterColumn_constraint_primary_key(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitColumn_constraint_primary_key(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_constraint_primary_key(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_constraint_foreign_keyContext extends ParserRuleContext {
        public Column_constraint_foreign_keyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Foreign_key_clauseContext foreign_key_clause() {
            return getRuleContext(Foreign_key_clauseContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_constraint_foreign_key;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterColumn_constraint_foreign_key(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitColumn_constraint_foreign_key(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_constraint_foreign_key(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_constraint_not_nullContext extends ParserRuleContext {
        public Column_constraint_not_nullContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_NULL() {
            return getToken(REDDATABASESqlParser.K_NULL, 0);
        }

        public Conflict_clauseContext conflict_clause() {
            return getRuleContext(Conflict_clauseContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_constraint_not_null;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterColumn_constraint_not_null(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitColumn_constraint_not_null(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_constraint_not_null(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_constraint_nullContext extends ParserRuleContext {
        public Column_constraint_nullContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_NULL() {
            return getToken(REDDATABASESqlParser.K_NULL, 0);
        }

        public Conflict_clauseContext conflict_clause() {
            return getRuleContext(Conflict_clauseContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_constraint_null;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterColumn_constraint_null(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitColumn_constraint_null(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_constraint_null(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_defaultContext extends ParserRuleContext {
        public Column_defaultContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_DEFAULT() {
            return getToken(REDDATABASESqlParser.K_DEFAULT, 0);
        }

        public Column_default_valueContext column_default_value() {
            return getRuleContext(Column_default_valueContext.class, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode K_NEXTVAL() {
            return getToken(REDDATABASESqlParser.K_NEXTVAL, 0);
        }

        public List<Any_nameContext> any_name() {
            return getRuleContexts(Any_nameContext.class);
        }

        public Any_nameContext any_name(int i) {
            return getRuleContext(Any_nameContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_default;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterColumn_default(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitColumn_default(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_default(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_default_valueContext extends ParserRuleContext {
        public Column_default_valueContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Signed_numberContext signed_number() {
            return getRuleContext(Signed_numberContext.class, 0);
        }

        public Literal_valueContext literal_value() {
            return getRuleContext(Literal_valueContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_default_value;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterColumn_default_value(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitColumn_default_value(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_default_value(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Conflict_clauseContext extends ParserRuleContext {
        public Conflict_clauseContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ON() {
            return getToken(REDDATABASESqlParser.K_ON, 0);
        }

        public TerminalNode K_CONFLICT() {
            return getToken(REDDATABASESqlParser.K_CONFLICT, 0);
        }

        public TerminalNode K_ROLLBACK() {
            return getToken(REDDATABASESqlParser.K_ROLLBACK, 0);
        }

        public TerminalNode K_ABORT() {
            return getToken(REDDATABASESqlParser.K_ABORT, 0);
        }

        public TerminalNode K_FAIL() {
            return getToken(REDDATABASESqlParser.K_FAIL, 0);
        }

        public TerminalNode K_IGNORE() {
            return getToken(REDDATABASESqlParser.K_IGNORE, 0);
        }

        public TerminalNode K_REPLACE() {
            return getToken(REDDATABASESqlParser.K_REPLACE, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_conflict_clause;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterConflict_clause(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitConflict_clause(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitConflict_clause(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ExprContext extends ParserRuleContext {
        public ExprContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Literal_valueContext literal_value() {
            return getRuleContext(Literal_valueContext.class, 0);
        }

        public TerminalNode BIND_PARAMETER() {
            return getToken(REDDATABASESqlParser.BIND_PARAMETER, 0);
        }

        public Column_nameContext column_name() {
            return getRuleContext(Column_nameContext.class, 0);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public Unary_operatorContext unary_operator() {
            return getRuleContext(Unary_operatorContext.class, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public Function_nameContext function_name() {
            return getRuleContext(Function_nameContext.class, 0);
        }

        public TerminalNode K_DISTINCT() {
            return getToken(REDDATABASESqlParser.K_DISTINCT, 0);
        }

        public TerminalNode K_CAST() {
            return getToken(REDDATABASESqlParser.K_CAST, 0);
        }

        public TerminalNode K_AS() {
            return getToken(REDDATABASESqlParser.K_AS, 0);
        }

        public Type_nameContext type_name() {
            return getRuleContext(Type_nameContext.class, 0);
        }

        public Select_stmtContext select_stmt() {
            return getRuleContext(Select_stmtContext.class, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_CASE() {
            return getToken(REDDATABASESqlParser.K_CASE, 0);
        }

        public TerminalNode K_END() {
            return getToken(REDDATABASESqlParser.K_END, 0);
        }

        public List<TerminalNode> K_WHEN() {
            return getTokens(REDDATABASESqlParser.K_WHEN);
        }

        public TerminalNode K_WHEN(int i) {
            return getToken(REDDATABASESqlParser.K_WHEN, i);
        }

        public List<TerminalNode> K_THEN() {
            return getTokens(REDDATABASESqlParser.K_THEN);
        }

        public TerminalNode K_THEN(int i) {
            return getToken(REDDATABASESqlParser.K_THEN, i);
        }

        public TerminalNode K_ELSE() {
            return getToken(REDDATABASESqlParser.K_ELSE, 0);
        }

        public Raise_functionContext raise_function() {
            return getRuleContext(Raise_functionContext.class, 0);
        }

        public TerminalNode K_IS() {
            return getToken(REDDATABASESqlParser.K_IS, 0);
        }

        public TerminalNode K_IN() {
            return getToken(REDDATABASESqlParser.K_IN, 0);
        }

        public TerminalNode K_LIKE() {
            return getToken(REDDATABASESqlParser.K_LIKE, 0);
        }

        public TerminalNode K_GLOB() {
            return getToken(REDDATABASESqlParser.K_GLOB, 0);
        }

        public TerminalNode K_MATCH() {
            return getToken(REDDATABASESqlParser.K_MATCH, 0);
        }

        public TerminalNode K_REGEXP() {
            return getToken(REDDATABASESqlParser.K_REGEXP, 0);
        }

        public TerminalNode K_AND() {
            return getToken(REDDATABASESqlParser.K_AND, 0);
        }

        public TerminalNode K_OR() {
            return getToken(REDDATABASESqlParser.K_OR, 0);
        }

        public TerminalNode K_BETWEEN() {
            return getToken(REDDATABASESqlParser.K_BETWEEN, 0);
        }

        public TerminalNode K_COLLATE() {
            return getToken(REDDATABASESqlParser.K_COLLATE, 0);
        }

        public Collation_nameContext collation_name() {
            return getRuleContext(Collation_nameContext.class, 0);
        }

        public TerminalNode K_ESCAPE() {
            return getToken(REDDATABASESqlParser.K_ESCAPE, 0);
        }

        public TerminalNode K_ISNULL() {
            return getToken(REDDATABASESqlParser.K_ISNULL, 0);
        }

        public TerminalNode K_NOTNULL() {
            return getToken(REDDATABASESqlParser.K_NOTNULL, 0);
        }

        public TerminalNode K_NULL() {
            return getToken(REDDATABASESqlParser.K_NULL, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_expr;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterExpr(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitExpr(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitExpr(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Foreign_key_clauseContext extends ParserRuleContext {
        public Foreign_key_clauseContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_REFERENCES() {
            return getToken(REDDATABASESqlParser.K_REFERENCES, 0);
        }

        public Foreign_tableContext foreign_table() {
            return getRuleContext(Foreign_tableContext.class, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public List<Fk_target_column_nameContext> fk_target_column_name() {
            return getRuleContexts(Fk_target_column_nameContext.class);
        }

        public Fk_target_column_nameContext fk_target_column_name(int i) {
            return getRuleContext(Fk_target_column_nameContext.class, i);
        }

        public TerminalNode K_DEFERRABLE() {
            return getToken(REDDATABASESqlParser.K_DEFERRABLE, 0);
        }

        public List<TerminalNode> K_ON() {
            return getTokens(REDDATABASESqlParser.K_ON);
        }

        public TerminalNode K_ON(int i) {
            return getToken(REDDATABASESqlParser.K_ON, i);
        }

        public List<TerminalNode> K_MATCH() {
            return getTokens(REDDATABASESqlParser.K_MATCH);
        }

        public TerminalNode K_MATCH(int i) {
            return getToken(REDDATABASESqlParser.K_MATCH, i);
        }

        public List<NameContext> name() {
            return getRuleContexts(NameContext.class);
        }

        public NameContext name(int i) {
            return getRuleContext(NameContext.class, i);
        }

        public List<TerminalNode> K_DELETE() {
            return getTokens(REDDATABASESqlParser.K_DELETE);
        }

        public TerminalNode K_DELETE(int i) {
            return getToken(REDDATABASESqlParser.K_DELETE, i);
        }

        public List<TerminalNode> K_UPDATE() {
            return getTokens(REDDATABASESqlParser.K_UPDATE);
        }

        public TerminalNode K_UPDATE(int i) {
            return getToken(REDDATABASESqlParser.K_UPDATE, i);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_INITIALLY() {
            return getToken(REDDATABASESqlParser.K_INITIALLY, 0);
        }

        public TerminalNode K_DEFERRED() {
            return getToken(REDDATABASESqlParser.K_DEFERRED, 0);
        }

        public TerminalNode K_IMMEDIATE() {
            return getToken(REDDATABASESqlParser.K_IMMEDIATE, 0);
        }

        public TerminalNode K_ENABLE() {
            return getToken(REDDATABASESqlParser.K_ENABLE, 0);
        }

        public List<TerminalNode> K_SET() {
            return getTokens(REDDATABASESqlParser.K_SET);
        }

        public TerminalNode K_SET(int i) {
            return getToken(REDDATABASESqlParser.K_SET, i);
        }

        public List<TerminalNode> K_NULL() {
            return getTokens(REDDATABASESqlParser.K_NULL);
        }

        public TerminalNode K_NULL(int i) {
            return getToken(REDDATABASESqlParser.K_NULL, i);
        }

        public List<TerminalNode> K_DEFAULT() {
            return getTokens(REDDATABASESqlParser.K_DEFAULT);
        }

        public TerminalNode K_DEFAULT(int i) {
            return getToken(REDDATABASESqlParser.K_DEFAULT, i);
        }

        public List<TerminalNode> K_CASCADE() {
            return getTokens(REDDATABASESqlParser.K_CASCADE);
        }

        public TerminalNode K_CASCADE(int i) {
            return getToken(REDDATABASESqlParser.K_CASCADE, i);
        }

        public List<TerminalNode> K_RESTRICT() {
            return getTokens(REDDATABASESqlParser.K_RESTRICT);
        }

        public TerminalNode K_RESTRICT(int i) {
            return getToken(REDDATABASESqlParser.K_RESTRICT, i);
        }

        public List<TerminalNode> K_NO() {
            return getTokens(REDDATABASESqlParser.K_NO);
        }

        public TerminalNode K_NO(int i) {
            return getToken(REDDATABASESqlParser.K_NO, i);
        }

        public List<TerminalNode> K_ACTION() {
            return getTokens(REDDATABASESqlParser.K_ACTION);
        }

        public TerminalNode K_ACTION(int i) {
            return getToken(REDDATABASESqlParser.K_ACTION, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_foreign_key_clause;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterForeign_key_clause(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitForeign_key_clause(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitForeign_key_clause(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Fk_target_column_nameContext extends ParserRuleContext {
        public Fk_target_column_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_fk_target_column_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterFk_target_column_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitFk_target_column_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitFk_target_column_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Raise_functionContext extends ParserRuleContext {
        public Raise_functionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_RAISE() {
            return getToken(REDDATABASESqlParser.K_RAISE, 0);
        }

        public TerminalNode K_IGNORE() {
            return getToken(REDDATABASESqlParser.K_IGNORE, 0);
        }

        public Error_messageContext error_message() {
            return getRuleContext(Error_messageContext.class, 0);
        }

        public TerminalNode K_ROLLBACK() {
            return getToken(REDDATABASESqlParser.K_ROLLBACK, 0);
        }

        public TerminalNode K_ABORT() {
            return getToken(REDDATABASESqlParser.K_ABORT, 0);
        }

        public TerminalNode K_FAIL() {
            return getToken(REDDATABASESqlParser.K_FAIL, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_raise_function;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterRaise_function(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitRaise_function(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitRaise_function(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Indexed_columnContext extends ParserRuleContext {
        public Indexed_columnContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Column_nameContext column_name() {
            return getRuleContext(Column_nameContext.class, 0);
        }

        public TerminalNode K_COLLATE() {
            return getToken(REDDATABASESqlParser.K_COLLATE, 0);
        }

        public Collation_nameContext collation_name() {
            return getRuleContext(Collation_nameContext.class, 0);
        }

        public TerminalNode K_ASC() {
            return getToken(REDDATABASESqlParser.K_ASC, 0);
        }

        public TerminalNode K_DESC() {
            return getToken(REDDATABASESqlParser.K_DESC, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_indexed_column;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterIndexed_column(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitIndexed_column(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitIndexed_column(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Table_constraintContext extends ParserRuleContext {
        public Table_constraintContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Table_constraint_primary_keyContext table_constraint_primary_key() {
            return getRuleContext(Table_constraint_primary_keyContext.class, 0);
        }

        public Table_constraint_keyContext table_constraint_key() {
            return getRuleContext(Table_constraint_keyContext.class, 0);
        }

        public Table_constraint_uniqueContext table_constraint_unique() {
            return getRuleContext(Table_constraint_uniqueContext.class, 0);
        }

        public TerminalNode K_CHECK() {
            return getToken(REDDATABASESqlParser.K_CHECK, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public Table_constraint_foreign_keyContext table_constraint_foreign_key() {
            return getRuleContext(Table_constraint_foreign_keyContext.class, 0);
        }

        public TerminalNode K_CONSTRAINT() {
            return getToken(REDDATABASESqlParser.K_CONSTRAINT, 0);
        }

        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_table_constraint;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterTable_constraint(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitTable_constraint(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTable_constraint(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Table_constraint_primary_keyContext extends ParserRuleContext {
        public Table_constraint_primary_keyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_PRIMARY() {
            return getToken(REDDATABASESqlParser.K_PRIMARY, 0);
        }

        public TerminalNode K_KEY() {
            return getToken(REDDATABASESqlParser.K_KEY, 0);
        }

        public List<Indexed_columnContext> indexed_column() {
            return getRuleContexts(Indexed_columnContext.class);
        }

        public Indexed_columnContext indexed_column(int i) {
            return getRuleContext(Indexed_columnContext.class, i);
        }

        public Conflict_clauseContext conflict_clause() {
            return getRuleContext(Conflict_clauseContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_table_constraint_primary_key;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterTable_constraint_primary_key(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitTable_constraint_primary_key(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTable_constraint_primary_key(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Table_constraint_foreign_keyContext extends ParserRuleContext {
        public Table_constraint_foreign_keyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_FOREIGN() {
            return getToken(REDDATABASESqlParser.K_FOREIGN, 0);
        }

        public TerminalNode K_KEY() {
            return getToken(REDDATABASESqlParser.K_KEY, 0);
        }

        public List<Fk_origin_column_nameContext> fk_origin_column_name() {
            return getRuleContexts(Fk_origin_column_nameContext.class);
        }

        public Fk_origin_column_nameContext fk_origin_column_name(int i) {
            return getRuleContext(Fk_origin_column_nameContext.class, i);
        }

        public Foreign_key_clauseContext foreign_key_clause() {
            return getRuleContext(Foreign_key_clauseContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_table_constraint_foreign_key;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterTable_constraint_foreign_key(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitTable_constraint_foreign_key(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTable_constraint_foreign_key(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Table_constraint_uniqueContext extends ParserRuleContext {
        public Table_constraint_uniqueContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_UNIQUE() {
            return getToken(REDDATABASESqlParser.K_UNIQUE, 0);
        }

        public List<Indexed_columnContext> indexed_column() {
            return getRuleContexts(Indexed_columnContext.class);
        }

        public Indexed_columnContext indexed_column(int i) {
            return getRuleContext(Indexed_columnContext.class, i);
        }

        public Conflict_clauseContext conflict_clause() {
            return getRuleContext(Conflict_clauseContext.class, 0);
        }

        public TerminalNode K_KEY() {
            return getToken(REDDATABASESqlParser.K_KEY, 0);
        }

        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_table_constraint_unique;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterTable_constraint_unique(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitTable_constraint_unique(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTable_constraint_unique(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Table_constraint_keyContext extends ParserRuleContext {
        public Table_constraint_keyContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_KEY() {
            return getToken(REDDATABASESqlParser.K_KEY, 0);
        }

        public List<Indexed_columnContext> indexed_column() {
            return getRuleContexts(Indexed_columnContext.class);
        }

        public Indexed_columnContext indexed_column(int i) {
            return getRuleContext(Indexed_columnContext.class, i);
        }

        public Conflict_clauseContext conflict_clause() {
            return getRuleContext(Conflict_clauseContext.class, 0);
        }

        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_table_constraint_key;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterTable_constraint_key(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitTable_constraint_key(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTable_constraint_key(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Fk_origin_column_nameContext extends ParserRuleContext {
        public Fk_origin_column_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Column_nameContext column_name() {
            return getRuleContext(Column_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_fk_origin_column_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterFk_origin_column_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitFk_origin_column_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitFk_origin_column_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class With_clauseContext extends ParserRuleContext {
        public With_clauseContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_WITH() {
            return getToken(REDDATABASESqlParser.K_WITH, 0);
        }

        public List<Cte_table_nameContext> cte_table_name() {
            return getRuleContexts(Cte_table_nameContext.class);
        }

        public Cte_table_nameContext cte_table_name(int i) {
            return getRuleContext(Cte_table_nameContext.class, i);
        }

        public List<TerminalNode> K_AS() {
            return getTokens(REDDATABASESqlParser.K_AS);
        }

        public TerminalNode K_AS(int i) {
            return getToken(REDDATABASESqlParser.K_AS, i);
        }

        public List<Select_stmtContext> select_stmt() {
            return getRuleContexts(Select_stmtContext.class);
        }

        public Select_stmtContext select_stmt(int i) {
            return getRuleContext(Select_stmtContext.class, i);
        }

        public TerminalNode K_RECURSIVE() {
            return getToken(REDDATABASESqlParser.K_RECURSIVE, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_with_clause;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterWith_clause(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitWith_clause(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitWith_clause(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Qualified_table_nameContext extends ParserRuleContext {
        public Qualified_table_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public TerminalNode K_INDEXED() {
            return getToken(REDDATABASESqlParser.K_INDEXED, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public Index_nameContext index_name() {
            return getRuleContext(Index_nameContext.class, 0);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_qualified_table_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterQualified_table_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitQualified_table_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitQualified_table_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Ordering_termContext extends ParserRuleContext {
        public Ordering_termContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode K_COLLATE() {
            return getToken(REDDATABASESqlParser.K_COLLATE, 0);
        }

        public Collation_nameContext collation_name() {
            return getRuleContext(Collation_nameContext.class, 0);
        }

        public TerminalNode K_ASC() {
            return getToken(REDDATABASESqlParser.K_ASC, 0);
        }

        public TerminalNode K_DESC() {
            return getToken(REDDATABASESqlParser.K_DESC, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_ordering_term;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterOrdering_term(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitOrdering_term(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitOrdering_term(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Order_collateContext extends ParserRuleContext {
        public Order_collateContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ASC() {
            return getToken(REDDATABASESqlParser.K_ASC, 0);
        }

        public TerminalNode K_DESC() {
            return getToken(REDDATABASESqlParser.K_DESC, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_order_collate;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterOrder_collate(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitOrder_collate(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitOrder_collate(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Pragma_valueContext extends ParserRuleContext {
        public Pragma_valueContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Signed_numberContext signed_number() {
            return getRuleContext(Signed_numberContext.class, 0);
        }

        public NameContext name() {
            return getRuleContext(NameContext.class, 0);
        }

        public TerminalNode STRING_LITERAL() {
            return getToken(REDDATABASESqlParser.STRING_LITERAL, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_pragma_value;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterPragma_value(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitPragma_value(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitPragma_value(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Common_table_expressionContext extends ParserRuleContext {
        public Common_table_expressionContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public TerminalNode K_AS() {
            return getToken(REDDATABASESqlParser.K_AS, 0);
        }

        public Select_stmtContext select_stmt() {
            return getRuleContext(Select_stmtContext.class, 0);
        }

        public List<Column_nameContext> column_name() {
            return getRuleContexts(Column_nameContext.class);
        }

        public Column_nameContext column_name(int i) {
            return getRuleContext(Column_nameContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_common_table_expression;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCommon_table_expression(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCommon_table_expression(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCommon_table_expression(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Result_columnContext extends ParserRuleContext {
        public Result_columnContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public Column_aliasContext column_alias() {
            return getRuleContext(Column_aliasContext.class, 0);
        }

        public TerminalNode K_AS() {
            return getToken(REDDATABASESqlParser.K_AS, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_result_column;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterResult_column(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitResult_column(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitResult_column(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Table_or_subqueryContext extends ParserRuleContext {
        public Table_or_subqueryContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public Database_nameContext database_name() {
            return getRuleContext(Database_nameContext.class, 0);
        }

        public Table_aliasContext table_alias() {
            return getRuleContext(Table_aliasContext.class, 0);
        }

        public TerminalNode K_INDEXED() {
            return getToken(REDDATABASESqlParser.K_INDEXED, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public Index_nameContext index_name() {
            return getRuleContext(Index_nameContext.class, 0);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_AS() {
            return getToken(REDDATABASESqlParser.K_AS, 0);
        }

        public List<Table_or_subqueryContext> table_or_subquery() {
            return getRuleContexts(Table_or_subqueryContext.class);
        }

        public Table_or_subqueryContext table_or_subquery(int i) {
            return getRuleContext(Table_or_subqueryContext.class, i);
        }

        public Join_clauseContext join_clause() {
            return getRuleContext(Join_clauseContext.class, 0);
        }

        public Select_stmtContext select_stmt() {
            return getRuleContext(Select_stmtContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_table_or_subquery;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterTable_or_subquery(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitTable_or_subquery(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTable_or_subquery(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Join_clauseContext extends ParserRuleContext {
        public Join_clauseContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<Table_or_subqueryContext> table_or_subquery() {
            return getRuleContexts(Table_or_subqueryContext.class);
        }

        public Table_or_subqueryContext table_or_subquery(int i) {
            return getRuleContext(Table_or_subqueryContext.class, i);
        }

        public List<Join_operatorContext> join_operator() {
            return getRuleContexts(Join_operatorContext.class);
        }

        public Join_operatorContext join_operator(int i) {
            return getRuleContext(Join_operatorContext.class, i);
        }

        public List<Join_constraintContext> join_constraint() {
            return getRuleContexts(Join_constraintContext.class);
        }

        public Join_constraintContext join_constraint(int i) {
            return getRuleContext(Join_constraintContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_join_clause;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterJoin_clause(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitJoin_clause(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitJoin_clause(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Join_operatorContext extends ParserRuleContext {
        public Join_operatorContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_JOIN() {
            return getToken(REDDATABASESqlParser.K_JOIN, 0);
        }

        public TerminalNode K_NATURAL() {
            return getToken(REDDATABASESqlParser.K_NATURAL, 0);
        }

        public TerminalNode K_LEFT() {
            return getToken(REDDATABASESqlParser.K_LEFT, 0);
        }

        public TerminalNode K_INNER() {
            return getToken(REDDATABASESqlParser.K_INNER, 0);
        }

        public TerminalNode K_CROSS() {
            return getToken(REDDATABASESqlParser.K_CROSS, 0);
        }

        public TerminalNode K_OUTER() {
            return getToken(REDDATABASESqlParser.K_OUTER, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_join_operator;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterJoin_operator(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitJoin_operator(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitJoin_operator(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Join_constraintContext extends ParserRuleContext {
        public Join_constraintContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ON() {
            return getToken(REDDATABASESqlParser.K_ON, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public TerminalNode K_USING() {
            return getToken(REDDATABASESqlParser.K_USING, 0);
        }

        public List<Column_nameContext> column_name() {
            return getRuleContexts(Column_nameContext.class);
        }

        public Column_nameContext column_name(int i) {
            return getRuleContext(Column_nameContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_join_constraint;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterJoin_constraint(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitJoin_constraint(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitJoin_constraint(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Select_coreContext extends ParserRuleContext {
        public Select_coreContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_SELECT() {
            return getToken(REDDATABASESqlParser.K_SELECT, 0);
        }

        public List<Result_columnContext> result_column() {
            return getRuleContexts(Result_columnContext.class);
        }

        public Result_columnContext result_column(int i) {
            return getRuleContext(Result_columnContext.class, i);
        }

        public TerminalNode K_FROM() {
            return getToken(REDDATABASESqlParser.K_FROM, 0);
        }

        public TerminalNode K_WHERE() {
            return getToken(REDDATABASESqlParser.K_WHERE, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode K_GROUP() {
            return getToken(REDDATABASESqlParser.K_GROUP, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public TerminalNode K_DISTINCT() {
            return getToken(REDDATABASESqlParser.K_DISTINCT, 0);
        }

        public TerminalNode K_ALL() {
            return getToken(REDDATABASESqlParser.K_ALL, 0);
        }

        public List<Table_or_subqueryContext> table_or_subquery() {
            return getRuleContexts(Table_or_subqueryContext.class);
        }

        public Table_or_subqueryContext table_or_subquery(int i) {
            return getRuleContext(Table_or_subqueryContext.class, i);
        }

        public Join_clauseContext join_clause() {
            return getRuleContext(Join_clauseContext.class, 0);
        }

        public TerminalNode K_HAVING() {
            return getToken(REDDATABASESqlParser.K_HAVING, 0);
        }

        public TerminalNode K_VALUES() {
            return getToken(REDDATABASESqlParser.K_VALUES, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_select_core;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterSelect_core(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitSelect_core(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSelect_core(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Compound_operatorContext extends ParserRuleContext {
        public Compound_operatorContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_UNION() {
            return getToken(REDDATABASESqlParser.K_UNION, 0);
        }

        public TerminalNode K_ALL() {
            return getToken(REDDATABASESqlParser.K_ALL, 0);
        }

        public TerminalNode K_INTERSECT() {
            return getToken(REDDATABASESqlParser.K_INTERSECT, 0);
        }

        public TerminalNode K_EXCEPT() {
            return getToken(REDDATABASESqlParser.K_EXCEPT, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_compound_operator;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCompound_operator(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCompound_operator(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCompound_operator(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Cte_table_nameContext extends ParserRuleContext {
        public Cte_table_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Table_nameContext table_name() {
            return getRuleContext(Table_nameContext.class, 0);
        }

        public List<Column_nameContext> column_name() {
            return getRuleContexts(Column_nameContext.class);
        }

        public Column_nameContext column_name(int i) {
            return getRuleContext(Column_nameContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_cte_table_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCte_table_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCte_table_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCte_table_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Signed_numberContext extends ParserRuleContext {
        public Signed_numberContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode NUMERIC_LITERAL() {
            return getToken(REDDATABASESqlParser.NUMERIC_LITERAL, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_signed_number;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterSigned_number(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitSigned_number(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSigned_number(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Literal_valueContext extends ParserRuleContext {
        public Literal_valueContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode NUMERIC_LITERAL() {
            return getToken(REDDATABASESqlParser.NUMERIC_LITERAL, 0);
        }

        public TerminalNode STRING_LITERAL() {
            return getToken(REDDATABASESqlParser.STRING_LITERAL, 0);
        }

        public TerminalNode BLOB_LITERAL() {
            return getToken(REDDATABASESqlParser.BLOB_LITERAL, 0);
        }

        public TerminalNode K_NULL() {
            return getToken(REDDATABASESqlParser.K_NULL, 0);
        }

        public TerminalNode K_CURRENT_TIME() {
            return getToken(REDDATABASESqlParser.K_CURRENT_TIME, 0);
        }

        public TerminalNode K_CURRENT_DATE() {
            return getToken(REDDATABASESqlParser.K_CURRENT_DATE, 0);
        }

        public TerminalNode K_CURRENT_TIMESTAMP() {
            return getToken(REDDATABASESqlParser.K_CURRENT_TIMESTAMP, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_literal_value;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterLiteral_value(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitLiteral_value(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitLiteral_value(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Unary_operatorContext extends ParserRuleContext {
        public Unary_operatorContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_unary_operator;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterUnary_operator(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitUnary_operator(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitUnary_operator(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Error_messageContext extends ParserRuleContext {
        public Error_messageContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode STRING_LITERAL() {
            return getToken(REDDATABASESqlParser.STRING_LITERAL, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_error_message;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterError_message(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitError_message(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitError_message(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Module_argumentContext extends ParserRuleContext {
        public Module_argumentContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public Column_defContext column_def() {
            return getRuleContext(Column_defContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_module_argument;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterModule_argument(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitModule_argument(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitModule_argument(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_aliasContext extends ParserRuleContext {
        public Column_aliasContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode IDENTIFIER() {
            return getToken(REDDATABASESqlParser.IDENTIFIER, 0);
        }

        public TerminalNode STRING_LITERAL() {
            return getToken(REDDATABASESqlParser.STRING_LITERAL, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_alias;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterColumn_alias(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitColumn_alias(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_alias(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class KeywordContext extends ParserRuleContext {
        public KeywordContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode K_ABORT() {
            return getToken(REDDATABASESqlParser.K_ABORT, 0);
        }

        public TerminalNode K_ACTION() {
            return getToken(REDDATABASESqlParser.K_ACTION, 0);
        }

        public TerminalNode K_ADD() {
            return getToken(REDDATABASESqlParser.K_ADD, 0);
        }

        public TerminalNode K_AFTER() {
            return getToken(REDDATABASESqlParser.K_AFTER, 0);
        }

        public TerminalNode K_ALL() {
            return getToken(REDDATABASESqlParser.K_ALL, 0);
        }

        public TerminalNode K_ALTER() {
            return getToken(REDDATABASESqlParser.K_ALTER, 0);
        }

        public TerminalNode K_ANALYZE() {
            return getToken(REDDATABASESqlParser.K_ANALYZE, 0);
        }

        public TerminalNode K_AND() {
            return getToken(REDDATABASESqlParser.K_AND, 0);
        }

        public TerminalNode K_AS() {
            return getToken(REDDATABASESqlParser.K_AS, 0);
        }

        public TerminalNode K_ASC() {
            return getToken(REDDATABASESqlParser.K_ASC, 0);
        }

        public TerminalNode K_ATTACH() {
            return getToken(REDDATABASESqlParser.K_ATTACH, 0);
        }

        public TerminalNode K_AUTOINCREMENT() {
            return getToken(REDDATABASESqlParser.K_AUTOINCREMENT, 0);
        }

        public TerminalNode K_BEFORE() {
            return getToken(REDDATABASESqlParser.K_BEFORE, 0);
        }

        public TerminalNode K_BEGIN() {
            return getToken(REDDATABASESqlParser.K_BEGIN, 0);
        }

        public TerminalNode K_BETWEEN() {
            return getToken(REDDATABASESqlParser.K_BETWEEN, 0);
        }

        public TerminalNode K_BY() {
            return getToken(REDDATABASESqlParser.K_BY, 0);
        }

        public TerminalNode K_CASCADE() {
            return getToken(REDDATABASESqlParser.K_CASCADE, 0);
        }

        public TerminalNode K_CASE() {
            return getToken(REDDATABASESqlParser.K_CASE, 0);
        }

        public TerminalNode K_CAST() {
            return getToken(REDDATABASESqlParser.K_CAST, 0);
        }

        public TerminalNode K_CHECK() {
            return getToken(REDDATABASESqlParser.K_CHECK, 0);
        }

        public TerminalNode K_COLLATE() {
            return getToken(REDDATABASESqlParser.K_COLLATE, 0);
        }

        public TerminalNode K_COLUMN() {
            return getToken(REDDATABASESqlParser.K_COLUMN, 0);
        }

        public TerminalNode K_COMMIT() {
            return getToken(REDDATABASESqlParser.K_COMMIT, 0);
        }

        public TerminalNode K_CONFLICT() {
            return getToken(REDDATABASESqlParser.K_CONFLICT, 0);
        }

        public TerminalNode K_CONSTRAINT() {
            return getToken(REDDATABASESqlParser.K_CONSTRAINT, 0);
        }

        public TerminalNode K_CREATE() {
            return getToken(REDDATABASESqlParser.K_CREATE, 0);
        }

        public TerminalNode K_CROSS() {
            return getToken(REDDATABASESqlParser.K_CROSS, 0);
        }

        public TerminalNode K_CURRENT_DATE() {
            return getToken(REDDATABASESqlParser.K_CURRENT_DATE, 0);
        }

        public TerminalNode K_CURRENT_TIME() {
            return getToken(REDDATABASESqlParser.K_CURRENT_TIME, 0);
        }

        public TerminalNode K_CURRENT_TIMESTAMP() {
            return getToken(REDDATABASESqlParser.K_CURRENT_TIMESTAMP, 0);
        }

        public TerminalNode K_DATABASE() {
            return getToken(REDDATABASESqlParser.K_DATABASE, 0);
        }

        public TerminalNode K_DEFAULT() {
            return getToken(REDDATABASESqlParser.K_DEFAULT, 0);
        }

        public TerminalNode K_DEFERRABLE() {
            return getToken(REDDATABASESqlParser.K_DEFERRABLE, 0);
        }

        public TerminalNode K_DEFERRED() {
            return getToken(REDDATABASESqlParser.K_DEFERRED, 0);
        }

        public TerminalNode K_DELETE() {
            return getToken(REDDATABASESqlParser.K_DELETE, 0);
        }

        public TerminalNode K_DESC() {
            return getToken(REDDATABASESqlParser.K_DESC, 0);
        }

        public TerminalNode K_DETACH() {
            return getToken(REDDATABASESqlParser.K_DETACH, 0);
        }

        public TerminalNode K_DISTINCT() {
            return getToken(REDDATABASESqlParser.K_DISTINCT, 0);
        }

        public TerminalNode K_DROP() {
            return getToken(REDDATABASESqlParser.K_DROP, 0);
        }

        public TerminalNode K_EACH() {
            return getToken(REDDATABASESqlParser.K_EACH, 0);
        }

        public TerminalNode K_ELSE() {
            return getToken(REDDATABASESqlParser.K_ELSE, 0);
        }

        public TerminalNode K_END() {
            return getToken(REDDATABASESqlParser.K_END, 0);
        }

        public TerminalNode K_ENABLE() {
            return getToken(REDDATABASESqlParser.K_ENABLE, 0);
        }

        public TerminalNode K_ESCAPE() {
            return getToken(REDDATABASESqlParser.K_ESCAPE, 0);
        }

        public TerminalNode K_EXCEPT() {
            return getToken(REDDATABASESqlParser.K_EXCEPT, 0);
        }

        public TerminalNode K_EXCLUSIVE() {
            return getToken(REDDATABASESqlParser.K_EXCLUSIVE, 0);
        }

        public TerminalNode K_EXISTS() {
            return getToken(REDDATABASESqlParser.K_EXISTS, 0);
        }

        public TerminalNode K_EXPLAIN() {
            return getToken(REDDATABASESqlParser.K_EXPLAIN, 0);
        }

        public TerminalNode K_FAIL() {
            return getToken(REDDATABASESqlParser.K_FAIL, 0);
        }

        public TerminalNode K_FOR() {
            return getToken(REDDATABASESqlParser.K_FOR, 0);
        }

        public TerminalNode K_FOREIGN() {
            return getToken(REDDATABASESqlParser.K_FOREIGN, 0);
        }

        public TerminalNode K_FROM() {
            return getToken(REDDATABASESqlParser.K_FROM, 0);
        }

        public TerminalNode K_FULL() {
            return getToken(REDDATABASESqlParser.K_FULL, 0);
        }

        public TerminalNode K_GLOB() {
            return getToken(REDDATABASESqlParser.K_GLOB, 0);
        }

        public TerminalNode K_GROUP() {
            return getToken(REDDATABASESqlParser.K_GROUP, 0);
        }

        public TerminalNode K_HAVING() {
            return getToken(REDDATABASESqlParser.K_HAVING, 0);
        }

        public TerminalNode K_IF() {
            return getToken(REDDATABASESqlParser.K_IF, 0);
        }

        public TerminalNode K_IGNORE() {
            return getToken(REDDATABASESqlParser.K_IGNORE, 0);
        }

        public TerminalNode K_IMMEDIATE() {
            return getToken(REDDATABASESqlParser.K_IMMEDIATE, 0);
        }

        public TerminalNode K_IN() {
            return getToken(REDDATABASESqlParser.K_IN, 0);
        }

        public TerminalNode K_INDEX() {
            return getToken(REDDATABASESqlParser.K_INDEX, 0);
        }

        public TerminalNode K_INDEXED() {
            return getToken(REDDATABASESqlParser.K_INDEXED, 0);
        }

        public TerminalNode K_INITIALLY() {
            return getToken(REDDATABASESqlParser.K_INITIALLY, 0);
        }

        public TerminalNode K_INNER() {
            return getToken(REDDATABASESqlParser.K_INNER, 0);
        }

        public TerminalNode K_INSERT() {
            return getToken(REDDATABASESqlParser.K_INSERT, 0);
        }

        public TerminalNode K_INSTEAD() {
            return getToken(REDDATABASESqlParser.K_INSTEAD, 0);
        }

        public TerminalNode K_INTERSECT() {
            return getToken(REDDATABASESqlParser.K_INTERSECT, 0);
        }

        public TerminalNode K_INTO() {
            return getToken(REDDATABASESqlParser.K_INTO, 0);
        }

        public TerminalNode K_IS() {
            return getToken(REDDATABASESqlParser.K_IS, 0);
        }

        public TerminalNode K_ISNULL() {
            return getToken(REDDATABASESqlParser.K_ISNULL, 0);
        }

        public TerminalNode K_JOIN() {
            return getToken(REDDATABASESqlParser.K_JOIN, 0);
        }

        public TerminalNode K_KEY() {
            return getToken(REDDATABASESqlParser.K_KEY, 0);
        }

        public TerminalNode K_LEFT() {
            return getToken(REDDATABASESqlParser.K_LEFT, 0);
        }

        public TerminalNode K_LIKE() {
            return getToken(REDDATABASESqlParser.K_LIKE, 0);
        }

        public TerminalNode K_LIMIT() {
            return getToken(REDDATABASESqlParser.K_LIMIT, 0);
        }

        public TerminalNode K_MATCH() {
            return getToken(REDDATABASESqlParser.K_MATCH, 0);
        }

        public TerminalNode K_NATURAL() {
            return getToken(REDDATABASESqlParser.K_NATURAL, 0);
        }

        public TerminalNode K_NO() {
            return getToken(REDDATABASESqlParser.K_NO, 0);
        }

        public TerminalNode K_NOT() {
            return getToken(REDDATABASESqlParser.K_NOT, 0);
        }

        public TerminalNode K_NOTNULL() {
            return getToken(REDDATABASESqlParser.K_NOTNULL, 0);
        }

        public TerminalNode K_NULL() {
            return getToken(REDDATABASESqlParser.K_NULL, 0);
        }

        public TerminalNode K_OF() {
            return getToken(REDDATABASESqlParser.K_OF, 0);
        }

        public TerminalNode K_OFFSET() {
            return getToken(REDDATABASESqlParser.K_OFFSET, 0);
        }

        public TerminalNode K_ON() {
            return getToken(REDDATABASESqlParser.K_ON, 0);
        }

        public TerminalNode K_OR() {
            return getToken(REDDATABASESqlParser.K_OR, 0);
        }

        public TerminalNode K_ORDER() {
            return getToken(REDDATABASESqlParser.K_ORDER, 0);
        }

        public TerminalNode K_OUTER() {
            return getToken(REDDATABASESqlParser.K_OUTER, 0);
        }

        public TerminalNode K_PLAN() {
            return getToken(REDDATABASESqlParser.K_PLAN, 0);
        }

        public TerminalNode K_PRAGMA() {
            return getToken(REDDATABASESqlParser.K_PRAGMA, 0);
        }

        public TerminalNode K_PRIMARY() {
            return getToken(REDDATABASESqlParser.K_PRIMARY, 0);
        }

        public TerminalNode K_QUERY() {
            return getToken(REDDATABASESqlParser.K_QUERY, 0);
        }

        public TerminalNode K_RAISE() {
            return getToken(REDDATABASESqlParser.K_RAISE, 0);
        }

        public TerminalNode K_RECURSIVE() {
            return getToken(REDDATABASESqlParser.K_RECURSIVE, 0);
        }

        public TerminalNode K_REFERENCES() {
            return getToken(REDDATABASESqlParser.K_REFERENCES, 0);
        }

        public TerminalNode K_REGEXP() {
            return getToken(REDDATABASESqlParser.K_REGEXP, 0);
        }

        public TerminalNode K_REINDEX() {
            return getToken(REDDATABASESqlParser.K_REINDEX, 0);
        }

        public TerminalNode K_RELEASE() {
            return getToken(REDDATABASESqlParser.K_RELEASE, 0);
        }

        public TerminalNode K_RENAME() {
            return getToken(REDDATABASESqlParser.K_RENAME, 0);
        }

        public TerminalNode K_REPLACE() {
            return getToken(REDDATABASESqlParser.K_REPLACE, 0);
        }

        public TerminalNode K_RESTRICT() {
            return getToken(REDDATABASESqlParser.K_RESTRICT, 0);
        }

        public TerminalNode K_RIGHT() {
            return getToken(REDDATABASESqlParser.K_RIGHT, 0);
        }

        public TerminalNode K_ROLLBACK() {
            return getToken(REDDATABASESqlParser.K_ROLLBACK, 0);
        }

        public TerminalNode K_ROW() {
            return getToken(REDDATABASESqlParser.K_ROW, 0);
        }

        public TerminalNode K_SAVEPOINT() {
            return getToken(REDDATABASESqlParser.K_SAVEPOINT, 0);
        }

        public TerminalNode K_SELECT() {
            return getToken(REDDATABASESqlParser.K_SELECT, 0);
        }

        public TerminalNode K_SET() {
            return getToken(REDDATABASESqlParser.K_SET, 0);
        }

        public TerminalNode K_TABLE() {
            return getToken(REDDATABASESqlParser.K_TABLE, 0);
        }

        public TerminalNode K_TEMP() {
            return getToken(REDDATABASESqlParser.K_TEMP, 0);
        }

        public TerminalNode K_TEMPORARY() {
            return getToken(REDDATABASESqlParser.K_TEMPORARY, 0);
        }

        public TerminalNode K_THEN() {
            return getToken(REDDATABASESqlParser.K_THEN, 0);
        }

        public TerminalNode K_TO() {
            return getToken(REDDATABASESqlParser.K_TO, 0);
        }

        public TerminalNode K_TRANSACTION() {
            return getToken(REDDATABASESqlParser.K_TRANSACTION, 0);
        }

        public TerminalNode K_TRIGGER() {
            return getToken(REDDATABASESqlParser.K_TRIGGER, 0);
        }

        public TerminalNode K_UNION() {
            return getToken(REDDATABASESqlParser.K_UNION, 0);
        }

        public TerminalNode K_UNIQUE() {
            return getToken(REDDATABASESqlParser.K_UNIQUE, 0);
        }

        public TerminalNode K_UPDATE() {
            return getToken(REDDATABASESqlParser.K_UPDATE, 0);
        }

        public TerminalNode K_USING() {
            return getToken(REDDATABASESqlParser.K_USING, 0);
        }

        public TerminalNode K_VACUUM() {
            return getToken(REDDATABASESqlParser.K_VACUUM, 0);
        }

        public TerminalNode K_VALUES() {
            return getToken(REDDATABASESqlParser.K_VALUES, 0);
        }

        public TerminalNode K_VIEW() {
            return getToken(REDDATABASESqlParser.K_VIEW, 0);
        }

        public TerminalNode K_VIRTUAL() {
            return getToken(REDDATABASESqlParser.K_VIRTUAL, 0);
        }

        public TerminalNode K_WHEN() {
            return getToken(REDDATABASESqlParser.K_WHEN, 0);
        }

        public TerminalNode K_WHERE() {
            return getToken(REDDATABASESqlParser.K_WHERE, 0);
        }

        public TerminalNode K_WITH() {
            return getToken(REDDATABASESqlParser.K_WITH, 0);
        }

        public TerminalNode K_WITHOUT() {
            return getToken(REDDATABASESqlParser.K_WITHOUT, 0);
        }

        public TerminalNode K_NEXTVAL() {
            return getToken(REDDATABASESqlParser.K_NEXTVAL, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_keyword;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterKeyword(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitKeyword(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitKeyword(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class UnknownContext extends ParserRuleContext {
        public UnknownContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_unknown;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterUnknown(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitUnknown(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitUnknown(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class NameContext extends ParserRuleContext {
        public NameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterName(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitName(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitName(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Function_nameContext extends ParserRuleContext {
        public Function_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_function_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterFunction_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitFunction_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitFunction_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Database_nameContext extends ParserRuleContext {
        public Database_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_database_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterDatabase_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitDatabase_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDatabase_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Domain_nameContext extends ParserRuleContext {
        public Domain_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_domain_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterDomain_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitDomain_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitDomain_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Source_table_nameContext extends ParserRuleContext {
        public Source_table_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_source_table_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterSource_table_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitSource_table_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSource_table_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Table_nameContext extends ParserRuleContext {
        public Table_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_table_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterTable_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitTable_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTable_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Procedure_nameContext extends ParserRuleContext {
        public Procedure_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_procedure_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterProcedure_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitProcedure_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitProcedure_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Table_or_index_nameContext extends ParserRuleContext {
        public Table_or_index_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_table_or_index_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterTable_or_index_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitTable_or_index_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTable_or_index_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class New_table_nameContext extends ParserRuleContext {
        public New_table_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_new_table_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterNew_table_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitNew_table_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitNew_table_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Column_nameContext extends ParserRuleContext {
        public Column_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_column_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterColumn_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitColumn_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitColumn_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Collation_nameContext extends ParserRuleContext {
        public Collation_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_collation_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterCollation_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitCollation_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCollation_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Foreign_tableContext extends ParserRuleContext {
        public Foreign_tableContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_foreign_table;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterForeign_table(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitForeign_table(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitForeign_table(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Index_nameContext extends ParserRuleContext {
        public Index_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_index_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterIndex_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitIndex_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitIndex_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Trigger_nameContext extends ParserRuleContext {
        public Trigger_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_trigger_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterTrigger_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitTrigger_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTrigger_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class View_nameContext extends ParserRuleContext {
        public View_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_view_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterView_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitView_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitView_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Module_nameContext extends ParserRuleContext {
        public Module_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_module_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterModule_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitModule_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitModule_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Pragma_nameContext extends ParserRuleContext {
        public Pragma_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_pragma_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterPragma_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitPragma_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitPragma_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Savepoint_nameContext extends ParserRuleContext {
        public Savepoint_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_savepoint_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterSavepoint_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitSavepoint_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitSavepoint_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Table_aliasContext extends ParserRuleContext {
        public Table_aliasContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_table_alias;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterTable_alias(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitTable_alias(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTable_alias(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Transaction_nameContext extends ParserRuleContext {
        public Transaction_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_transaction_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).enterTransaction_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener)
                ((REDDATABASESqlListener) listener).exitTransaction_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitTransaction_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Charset_nameContext extends ParserRuleContext {
        public Charset_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_charset_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterCharset_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitCharset_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitCharset_name(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class Any_nameContext extends ParserRuleContext {
        public Any_nameContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode IDENTIFIER() {
            return getToken(REDDATABASESqlParser.IDENTIFIER, 0);
        }

        public KeywordContext keyword() {
            return getRuleContext(KeywordContext.class, 0);
        }

        public TerminalNode STRING_LITERAL() {
            return getToken(REDDATABASESqlParser.STRING_LITERAL, 0);
        }

        public Any_nameContext any_name() {
            return getRuleContext(Any_nameContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_any_name;
        }

        @Override
        public void enterRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).enterAny_name(this);
        }

        @Override
        public void exitRule(ParseTreeListener listener) {
            if (listener instanceof REDDATABASESqlListener) ((REDDATABASESqlListener) listener).exitAny_name(this);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof REDDATABASESqlVisitor)
                return ((REDDATABASESqlVisitor<? extends T>) visitor).visitAny_name(this);
            else return visitor.visitChildren(this);
        }
    }
}