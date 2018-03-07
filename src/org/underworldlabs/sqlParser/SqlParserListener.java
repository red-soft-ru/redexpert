package org.underworldlabs.sqlParser;
// Generated from D:/\SqlParser.g4 by ANTLR 4.7

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SqlParserParser}.
 */
public interface SqlParserListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link SqlParserParser#parse}.
     *
     * @param ctx the parse tree
     */
    void enterParse(SqlParserParser.ParseContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#parse}.
     *
     * @param ctx the parse tree
     */
    void exitParse(SqlParserParser.ParseContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#error}.
     *
     * @param ctx the parse tree
     */
    void enterError(SqlParserParser.ErrorContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#error}.
     *
     * @param ctx the parse tree
     */
    void exitError(SqlParserParser.ErrorContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#sql_stmt_list}.
     *
     * @param ctx the parse tree
     */
    void enterSql_stmt_list(SqlParserParser.Sql_stmt_listContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#sql_stmt_list}.
     *
     * @param ctx the parse tree
     */
    void exitSql_stmt_list(SqlParserParser.Sql_stmt_listContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#sql_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterSql_stmt(SqlParserParser.Sql_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#sql_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitSql_stmt(SqlParserParser.Sql_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#alter_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterAlter_table_stmt(SqlParserParser.Alter_table_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#alter_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitAlter_table_stmt(SqlParserParser.Alter_table_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#alter_table_add_constraint}.
     *
     * @param ctx the parse tree
     */
    void enterAlter_table_add_constraint(SqlParserParser.Alter_table_add_constraintContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#alter_table_add_constraint}.
     *
     * @param ctx the parse tree
     */
    void exitAlter_table_add_constraint(SqlParserParser.Alter_table_add_constraintContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#alter_table_add}.
     *
     * @param ctx the parse tree
     */
    void enterAlter_table_add(SqlParserParser.Alter_table_addContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#alter_table_add}.
     *
     * @param ctx the parse tree
     */
    void exitAlter_table_add(SqlParserParser.Alter_table_addContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#analyze_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterAnalyze_stmt(SqlParserParser.Analyze_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#analyze_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitAnalyze_stmt(SqlParserParser.Analyze_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#attach_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterAttach_stmt(SqlParserParser.Attach_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#attach_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitAttach_stmt(SqlParserParser.Attach_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#begin_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterBegin_stmt(SqlParserParser.Begin_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#begin_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitBegin_stmt(SqlParserParser.Begin_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#commit_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCommit_stmt(SqlParserParser.Commit_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#commit_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCommit_stmt(SqlParserParser.Commit_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#compound_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCompound_select_stmt(SqlParserParser.Compound_select_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#compound_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCompound_select_stmt(SqlParserParser.Compound_select_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#create_index_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_index_stmt(SqlParserParser.Create_index_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#create_index_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_index_stmt(SqlParserParser.Create_index_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#create_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_table_stmt(SqlParserParser.Create_table_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#create_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_table_stmt(SqlParserParser.Create_table_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#create_trigger_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_trigger_stmt(SqlParserParser.Create_trigger_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#create_trigger_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_trigger_stmt(SqlParserParser.Create_trigger_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#create_view_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_view_stmt(SqlParserParser.Create_view_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#create_view_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_view_stmt(SqlParserParser.Create_view_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#create_virtual_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_virtual_table_stmt(SqlParserParser.Create_virtual_table_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#create_virtual_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_virtual_table_stmt(SqlParserParser.Create_virtual_table_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#delete_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDelete_stmt(SqlParserParser.Delete_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#delete_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDelete_stmt(SqlParserParser.Delete_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#delete_stmt_limited}.
     *
     * @param ctx the parse tree
     */
    void enterDelete_stmt_limited(SqlParserParser.Delete_stmt_limitedContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#delete_stmt_limited}.
     *
     * @param ctx the parse tree
     */
    void exitDelete_stmt_limited(SqlParserParser.Delete_stmt_limitedContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#detach_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDetach_stmt(SqlParserParser.Detach_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#detach_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDetach_stmt(SqlParserParser.Detach_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#drop_index_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDrop_index_stmt(SqlParserParser.Drop_index_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#drop_index_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDrop_index_stmt(SqlParserParser.Drop_index_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#drop_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDrop_table_stmt(SqlParserParser.Drop_table_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#drop_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDrop_table_stmt(SqlParserParser.Drop_table_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#drop_trigger_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDrop_trigger_stmt(SqlParserParser.Drop_trigger_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#drop_trigger_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDrop_trigger_stmt(SqlParserParser.Drop_trigger_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#drop_view_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDrop_view_stmt(SqlParserParser.Drop_view_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#drop_view_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDrop_view_stmt(SqlParserParser.Drop_view_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#factored_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterFactored_select_stmt(SqlParserParser.Factored_select_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#factored_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitFactored_select_stmt(SqlParserParser.Factored_select_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#insert_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterInsert_stmt(SqlParserParser.Insert_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#insert_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitInsert_stmt(SqlParserParser.Insert_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#pragma_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterPragma_stmt(SqlParserParser.Pragma_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#pragma_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitPragma_stmt(SqlParserParser.Pragma_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#reindex_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterReindex_stmt(SqlParserParser.Reindex_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#reindex_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitReindex_stmt(SqlParserParser.Reindex_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#release_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterRelease_stmt(SqlParserParser.Release_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#release_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitRelease_stmt(SqlParserParser.Release_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#rollback_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterRollback_stmt(SqlParserParser.Rollback_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#rollback_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitRollback_stmt(SqlParserParser.Rollback_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#savepoint_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterSavepoint_stmt(SqlParserParser.Savepoint_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#savepoint_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitSavepoint_stmt(SqlParserParser.Savepoint_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#simple_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterSimple_select_stmt(SqlParserParser.Simple_select_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#simple_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitSimple_select_stmt(SqlParserParser.Simple_select_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#select_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterSelect_stmt(SqlParserParser.Select_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#select_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitSelect_stmt(SqlParserParser.Select_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#select_or_values}.
     *
     * @param ctx the parse tree
     */
    void enterSelect_or_values(SqlParserParser.Select_or_valuesContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#select_or_values}.
     *
     * @param ctx the parse tree
     */
    void exitSelect_or_values(SqlParserParser.Select_or_valuesContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#update_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterUpdate_stmt(SqlParserParser.Update_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#update_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitUpdate_stmt(SqlParserParser.Update_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#update_stmt_limited}.
     *
     * @param ctx the parse tree
     */
    void enterUpdate_stmt_limited(SqlParserParser.Update_stmt_limitedContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#update_stmt_limited}.
     *
     * @param ctx the parse tree
     */
    void exitUpdate_stmt_limited(SqlParserParser.Update_stmt_limitedContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#vacuum_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterVacuum_stmt(SqlParserParser.Vacuum_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#vacuum_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitVacuum_stmt(SqlParserParser.Vacuum_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_def}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_def(SqlParserParser.Column_defContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_def}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_def(SqlParserParser.Column_defContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#type_name}.
     *
     * @param ctx the parse tree
     */
    void enterType_name(SqlParserParser.Type_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#type_name}.
     *
     * @param ctx the parse tree
     */
    void exitType_name(SqlParserParser.Type_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_constraint}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint(SqlParserParser.Column_constraintContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_constraint}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint(SqlParserParser.Column_constraintContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_constraint_primary_key}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint_primary_key(SqlParserParser.Column_constraint_primary_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_constraint_primary_key}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint_primary_key(SqlParserParser.Column_constraint_primary_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint_foreign_key(SqlParserParser.Column_constraint_foreign_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint_foreign_key(SqlParserParser.Column_constraint_foreign_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_constraint_not_null}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint_not_null(SqlParserParser.Column_constraint_not_nullContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_constraint_not_null}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint_not_null(SqlParserParser.Column_constraint_not_nullContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_constraint_null}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint_null(SqlParserParser.Column_constraint_nullContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_constraint_null}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint_null(SqlParserParser.Column_constraint_nullContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_default}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_default(SqlParserParser.Column_defaultContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_default}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_default(SqlParserParser.Column_defaultContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_default_value}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_default_value(SqlParserParser.Column_default_valueContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_default_value}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_default_value(SqlParserParser.Column_default_valueContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#conflict_clause}.
     *
     * @param ctx the parse tree
     */
    void enterConflict_clause(SqlParserParser.Conflict_clauseContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#conflict_clause}.
     *
     * @param ctx the parse tree
     */
    void exitConflict_clause(SqlParserParser.Conflict_clauseContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterExpr(SqlParserParser.ExprContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitExpr(SqlParserParser.ExprContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#foreign_key_clause}.
     *
     * @param ctx the parse tree
     */
    void enterForeign_key_clause(SqlParserParser.Foreign_key_clauseContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#foreign_key_clause}.
     *
     * @param ctx the parse tree
     */
    void exitForeign_key_clause(SqlParserParser.Foreign_key_clauseContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#fk_target_column_name}.
     *
     * @param ctx the parse tree
     */
    void enterFk_target_column_name(SqlParserParser.Fk_target_column_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#fk_target_column_name}.
     *
     * @param ctx the parse tree
     */
    void exitFk_target_column_name(SqlParserParser.Fk_target_column_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#raise_function}.
     *
     * @param ctx the parse tree
     */
    void enterRaise_function(SqlParserParser.Raise_functionContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#raise_function}.
     *
     * @param ctx the parse tree
     */
    void exitRaise_function(SqlParserParser.Raise_functionContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#indexed_column}.
     *
     * @param ctx the parse tree
     */
    void enterIndexed_column(SqlParserParser.Indexed_columnContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#indexed_column}.
     *
     * @param ctx the parse tree
     */
    void exitIndexed_column(SqlParserParser.Indexed_columnContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#table_constraint}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint(SqlParserParser.Table_constraintContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#table_constraint}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint(SqlParserParser.Table_constraintContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#table_constraint_primary_key}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint_primary_key(SqlParserParser.Table_constraint_primary_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#table_constraint_primary_key}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint_primary_key(SqlParserParser.Table_constraint_primary_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#table_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint_foreign_key(SqlParserParser.Table_constraint_foreign_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#table_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint_foreign_key(SqlParserParser.Table_constraint_foreign_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#table_constraint_unique}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint_unique(SqlParserParser.Table_constraint_uniqueContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#table_constraint_unique}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint_unique(SqlParserParser.Table_constraint_uniqueContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#table_constraint_key}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint_key(SqlParserParser.Table_constraint_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#table_constraint_key}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint_key(SqlParserParser.Table_constraint_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#fk_origin_column_name}.
     *
     * @param ctx the parse tree
     */
    void enterFk_origin_column_name(SqlParserParser.Fk_origin_column_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#fk_origin_column_name}.
     *
     * @param ctx the parse tree
     */
    void exitFk_origin_column_name(SqlParserParser.Fk_origin_column_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#with_clause}.
     *
     * @param ctx the parse tree
     */
    void enterWith_clause(SqlParserParser.With_clauseContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#with_clause}.
     *
     * @param ctx the parse tree
     */
    void exitWith_clause(SqlParserParser.With_clauseContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#qualified_table_name}.
     *
     * @param ctx the parse tree
     */
    void enterQualified_table_name(SqlParserParser.Qualified_table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#qualified_table_name}.
     *
     * @param ctx the parse tree
     */
    void exitQualified_table_name(SqlParserParser.Qualified_table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#ordering_term}.
     *
     * @param ctx the parse tree
     */
    void enterOrdering_term(SqlParserParser.Ordering_termContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#ordering_term}.
     *
     * @param ctx the parse tree
     */
    void exitOrdering_term(SqlParserParser.Ordering_termContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#pragma_value}.
     *
     * @param ctx the parse tree
     */
    void enterPragma_value(SqlParserParser.Pragma_valueContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#pragma_value}.
     *
     * @param ctx the parse tree
     */
    void exitPragma_value(SqlParserParser.Pragma_valueContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#common_table_expression}.
     *
     * @param ctx the parse tree
     */
    void enterCommon_table_expression(SqlParserParser.Common_table_expressionContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#common_table_expression}.
     *
     * @param ctx the parse tree
     */
    void exitCommon_table_expression(SqlParserParser.Common_table_expressionContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#result_column}.
     *
     * @param ctx the parse tree
     */
    void enterResult_column(SqlParserParser.Result_columnContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#result_column}.
     *
     * @param ctx the parse tree
     */
    void exitResult_column(SqlParserParser.Result_columnContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#table_or_subquery}.
     *
     * @param ctx the parse tree
     */
    void enterTable_or_subquery(SqlParserParser.Table_or_subqueryContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#table_or_subquery}.
     *
     * @param ctx the parse tree
     */
    void exitTable_or_subquery(SqlParserParser.Table_or_subqueryContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#join_clause}.
     *
     * @param ctx the parse tree
     */
    void enterJoin_clause(SqlParserParser.Join_clauseContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#join_clause}.
     *
     * @param ctx the parse tree
     */
    void exitJoin_clause(SqlParserParser.Join_clauseContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#join_operator}.
     *
     * @param ctx the parse tree
     */
    void enterJoin_operator(SqlParserParser.Join_operatorContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#join_operator}.
     *
     * @param ctx the parse tree
     */
    void exitJoin_operator(SqlParserParser.Join_operatorContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#join_constraint}.
     *
     * @param ctx the parse tree
     */
    void enterJoin_constraint(SqlParserParser.Join_constraintContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#join_constraint}.
     *
     * @param ctx the parse tree
     */
    void exitJoin_constraint(SqlParserParser.Join_constraintContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#select_core}.
     *
     * @param ctx the parse tree
     */
    void enterSelect_core(SqlParserParser.Select_coreContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#select_core}.
     *
     * @param ctx the parse tree
     */
    void exitSelect_core(SqlParserParser.Select_coreContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#compound_operator}.
     *
     * @param ctx the parse tree
     */
    void enterCompound_operator(SqlParserParser.Compound_operatorContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#compound_operator}.
     *
     * @param ctx the parse tree
     */
    void exitCompound_operator(SqlParserParser.Compound_operatorContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#cte_table_name}.
     *
     * @param ctx the parse tree
     */
    void enterCte_table_name(SqlParserParser.Cte_table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#cte_table_name}.
     *
     * @param ctx the parse tree
     */
    void exitCte_table_name(SqlParserParser.Cte_table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#signed_number}.
     *
     * @param ctx the parse tree
     */
    void enterSigned_number(SqlParserParser.Signed_numberContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#signed_number}.
     *
     * @param ctx the parse tree
     */
    void exitSigned_number(SqlParserParser.Signed_numberContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#literal_value}.
     *
     * @param ctx the parse tree
     */
    void enterLiteral_value(SqlParserParser.Literal_valueContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#literal_value}.
     *
     * @param ctx the parse tree
     */
    void exitLiteral_value(SqlParserParser.Literal_valueContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#unary_operator}.
     *
     * @param ctx the parse tree
     */
    void enterUnary_operator(SqlParserParser.Unary_operatorContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#unary_operator}.
     *
     * @param ctx the parse tree
     */
    void exitUnary_operator(SqlParserParser.Unary_operatorContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#error_message}.
     *
     * @param ctx the parse tree
     */
    void enterError_message(SqlParserParser.Error_messageContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#error_message}.
     *
     * @param ctx the parse tree
     */
    void exitError_message(SqlParserParser.Error_messageContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#module_argument}.
     *
     * @param ctx the parse tree
     */
    void enterModule_argument(SqlParserParser.Module_argumentContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#module_argument}.
     *
     * @param ctx the parse tree
     */
    void exitModule_argument(SqlParserParser.Module_argumentContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_alias}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_alias(SqlParserParser.Column_aliasContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_alias}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_alias(SqlParserParser.Column_aliasContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#keyword}.
     *
     * @param ctx the parse tree
     */
    void enterKeyword(SqlParserParser.KeywordContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#keyword}.
     *
     * @param ctx the parse tree
     */
    void exitKeyword(SqlParserParser.KeywordContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#unknown}.
     *
     * @param ctx the parse tree
     */
    void enterUnknown(SqlParserParser.UnknownContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#unknown}.
     *
     * @param ctx the parse tree
     */
    void exitUnknown(SqlParserParser.UnknownContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#name}.
     *
     * @param ctx the parse tree
     */
    void enterName(SqlParserParser.NameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#name}.
     *
     * @param ctx the parse tree
     */
    void exitName(SqlParserParser.NameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#function_name}.
     *
     * @param ctx the parse tree
     */
    void enterFunction_name(SqlParserParser.Function_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#function_name}.
     *
     * @param ctx the parse tree
     */
    void exitFunction_name(SqlParserParser.Function_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#database_name}.
     *
     * @param ctx the parse tree
     */
    void enterDatabase_name(SqlParserParser.Database_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#database_name}.
     *
     * @param ctx the parse tree
     */
    void exitDatabase_name(SqlParserParser.Database_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#source_table_name}.
     *
     * @param ctx the parse tree
     */
    void enterSource_table_name(SqlParserParser.Source_table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#source_table_name}.
     *
     * @param ctx the parse tree
     */
    void exitSource_table_name(SqlParserParser.Source_table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#table_name}.
     *
     * @param ctx the parse tree
     */
    void enterTable_name(SqlParserParser.Table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#table_name}.
     *
     * @param ctx the parse tree
     */
    void exitTable_name(SqlParserParser.Table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#table_or_index_name}.
     *
     * @param ctx the parse tree
     */
    void enterTable_or_index_name(SqlParserParser.Table_or_index_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#table_or_index_name}.
     *
     * @param ctx the parse tree
     */
    void exitTable_or_index_name(SqlParserParser.Table_or_index_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#new_table_name}.
     *
     * @param ctx the parse tree
     */
    void enterNew_table_name(SqlParserParser.New_table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#new_table_name}.
     *
     * @param ctx the parse tree
     */
    void exitNew_table_name(SqlParserParser.New_table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#column_name}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_name(SqlParserParser.Column_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#column_name}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_name(SqlParserParser.Column_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#collation_name}.
     *
     * @param ctx the parse tree
     */
    void enterCollation_name(SqlParserParser.Collation_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#collation_name}.
     *
     * @param ctx the parse tree
     */
    void exitCollation_name(SqlParserParser.Collation_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#foreign_table}.
     *
     * @param ctx the parse tree
     */
    void enterForeign_table(SqlParserParser.Foreign_tableContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#foreign_table}.
     *
     * @param ctx the parse tree
     */
    void exitForeign_table(SqlParserParser.Foreign_tableContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#index_name}.
     *
     * @param ctx the parse tree
     */
    void enterIndex_name(SqlParserParser.Index_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#index_name}.
     *
     * @param ctx the parse tree
     */
    void exitIndex_name(SqlParserParser.Index_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#trigger_name}.
     *
     * @param ctx the parse tree
     */
    void enterTrigger_name(SqlParserParser.Trigger_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#trigger_name}.
     *
     * @param ctx the parse tree
     */
    void exitTrigger_name(SqlParserParser.Trigger_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#view_name}.
     *
     * @param ctx the parse tree
     */
    void enterView_name(SqlParserParser.View_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#view_name}.
     *
     * @param ctx the parse tree
     */
    void exitView_name(SqlParserParser.View_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#module_name}.
     *
     * @param ctx the parse tree
     */
    void enterModule_name(SqlParserParser.Module_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#module_name}.
     *
     * @param ctx the parse tree
     */
    void exitModule_name(SqlParserParser.Module_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#pragma_name}.
     *
     * @param ctx the parse tree
     */
    void enterPragma_name(SqlParserParser.Pragma_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#pragma_name}.
     *
     * @param ctx the parse tree
     */
    void exitPragma_name(SqlParserParser.Pragma_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#savepoint_name}.
     *
     * @param ctx the parse tree
     */
    void enterSavepoint_name(SqlParserParser.Savepoint_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#savepoint_name}.
     *
     * @param ctx the parse tree
     */
    void exitSavepoint_name(SqlParserParser.Savepoint_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#table_alias}.
     *
     * @param ctx the parse tree
     */
    void enterTable_alias(SqlParserParser.Table_aliasContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#table_alias}.
     *
     * @param ctx the parse tree
     */
    void exitTable_alias(SqlParserParser.Table_aliasContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#transaction_name}.
     *
     * @param ctx the parse tree
     */
    void enterTransaction_name(SqlParserParser.Transaction_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#transaction_name}.
     *
     * @param ctx the parse tree
     */
    void exitTransaction_name(SqlParserParser.Transaction_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link SqlParserParser#any_name}.
     *
     * @param ctx the parse tree
     */
    void enterAny_name(SqlParserParser.Any_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link SqlParserParser#any_name}.
     *
     * @param ctx the parse tree
     */
    void exitAny_name(SqlParserParser.Any_nameContext ctx);
}