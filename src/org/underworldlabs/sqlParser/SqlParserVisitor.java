package org.underworldlabs.sqlParser;
// Generated from D:/\SqlParser.g4 by ANTLR 4.7

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SqlParserParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 *            operations with no return type.
 */
public interface SqlParserVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by {@link SqlParserParser#parse}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitParse(SqlParserParser.ParseContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#error}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitError(SqlParserParser.ErrorContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#sql_stmt_list}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSql_stmt_list(SqlParserParser.Sql_stmt_listContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#sql_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSql_stmt(SqlParserParser.Sql_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#alter_table_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlter_table_stmt(SqlParserParser.Alter_table_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#alter_table_add_constraint}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlter_table_add_constraint(SqlParserParser.Alter_table_add_constraintContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#alter_table_add}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlter_table_add(SqlParserParser.Alter_table_addContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#analyze_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAnalyze_stmt(SqlParserParser.Analyze_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#attach_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAttach_stmt(SqlParserParser.Attach_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#begin_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBegin_stmt(SqlParserParser.Begin_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#commit_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCommit_stmt(SqlParserParser.Commit_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#compound_select_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCompound_select_stmt(SqlParserParser.Compound_select_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#create_index_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_index_stmt(SqlParserParser.Create_index_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#create_table_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_table_stmt(SqlParserParser.Create_table_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#create_trigger_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_trigger_stmt(SqlParserParser.Create_trigger_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#create_view_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_view_stmt(SqlParserParser.Create_view_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#create_virtual_table_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_virtual_table_stmt(SqlParserParser.Create_virtual_table_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#delete_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDelete_stmt(SqlParserParser.Delete_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#delete_stmt_limited}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDelete_stmt_limited(SqlParserParser.Delete_stmt_limitedContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#detach_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDetach_stmt(SqlParserParser.Detach_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#drop_index_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDrop_index_stmt(SqlParserParser.Drop_index_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#drop_table_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDrop_table_stmt(SqlParserParser.Drop_table_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#drop_trigger_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDrop_trigger_stmt(SqlParserParser.Drop_trigger_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#drop_view_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDrop_view_stmt(SqlParserParser.Drop_view_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#factored_select_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFactored_select_stmt(SqlParserParser.Factored_select_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#insert_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitInsert_stmt(SqlParserParser.Insert_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#pragma_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPragma_stmt(SqlParserParser.Pragma_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#reindex_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitReindex_stmt(SqlParserParser.Reindex_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#release_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitRelease_stmt(SqlParserParser.Release_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#rollback_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitRollback_stmt(SqlParserParser.Rollback_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#savepoint_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSavepoint_stmt(SqlParserParser.Savepoint_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#simple_select_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSimple_select_stmt(SqlParserParser.Simple_select_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#select_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSelect_stmt(SqlParserParser.Select_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#select_or_values}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSelect_or_values(SqlParserParser.Select_or_valuesContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#update_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUpdate_stmt(SqlParserParser.Update_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#update_stmt_limited}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUpdate_stmt_limited(SqlParserParser.Update_stmt_limitedContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#vacuum_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVacuum_stmt(SqlParserParser.Vacuum_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_def}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_def(SqlParserParser.Column_defContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#type_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitType_name(SqlParserParser.Type_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_constraint}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint(SqlParserParser.Column_constraintContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_constraint_primary_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint_primary_key(SqlParserParser.Column_constraint_primary_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint_foreign_key(SqlParserParser.Column_constraint_foreign_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_constraint_not_null}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint_not_null(SqlParserParser.Column_constraint_not_nullContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_constraint_null}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint_null(SqlParserParser.Column_constraint_nullContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_default}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_default(SqlParserParser.Column_defaultContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_default_value}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_default_value(SqlParserParser.Column_default_valueContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#conflict_clause}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitConflict_clause(SqlParserParser.Conflict_clauseContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitExpr(SqlParserParser.ExprContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#foreign_key_clause}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitForeign_key_clause(SqlParserParser.Foreign_key_clauseContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#fk_target_column_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFk_target_column_name(SqlParserParser.Fk_target_column_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#raise_function}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitRaise_function(SqlParserParser.Raise_functionContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#indexed_column}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIndexed_column(SqlParserParser.Indexed_columnContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#table_constraint}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint(SqlParserParser.Table_constraintContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#table_constraint_primary_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint_primary_key(SqlParserParser.Table_constraint_primary_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#table_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint_foreign_key(SqlParserParser.Table_constraint_foreign_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#table_constraint_unique}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint_unique(SqlParserParser.Table_constraint_uniqueContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#table_constraint_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint_key(SqlParserParser.Table_constraint_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#fk_origin_column_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFk_origin_column_name(SqlParserParser.Fk_origin_column_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#with_clause}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitWith_clause(SqlParserParser.With_clauseContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#qualified_table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitQualified_table_name(SqlParserParser.Qualified_table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#ordering_term}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOrdering_term(SqlParserParser.Ordering_termContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#pragma_value}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPragma_value(SqlParserParser.Pragma_valueContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#common_table_expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCommon_table_expression(SqlParserParser.Common_table_expressionContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#result_column}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitResult_column(SqlParserParser.Result_columnContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#table_or_subquery}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_or_subquery(SqlParserParser.Table_or_subqueryContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#join_clause}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitJoin_clause(SqlParserParser.Join_clauseContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#join_operator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitJoin_operator(SqlParserParser.Join_operatorContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#join_constraint}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitJoin_constraint(SqlParserParser.Join_constraintContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#select_core}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSelect_core(SqlParserParser.Select_coreContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#compound_operator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCompound_operator(SqlParserParser.Compound_operatorContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#cte_table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCte_table_name(SqlParserParser.Cte_table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#signed_number}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSigned_number(SqlParserParser.Signed_numberContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#literal_value}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitLiteral_value(SqlParserParser.Literal_valueContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#unary_operator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUnary_operator(SqlParserParser.Unary_operatorContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#error_message}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitError_message(SqlParserParser.Error_messageContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#module_argument}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitModule_argument(SqlParserParser.Module_argumentContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_alias}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_alias(SqlParserParser.Column_aliasContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#keyword}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitKeyword(SqlParserParser.KeywordContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#unknown}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUnknown(SqlParserParser.UnknownContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitName(SqlParserParser.NameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#function_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFunction_name(SqlParserParser.Function_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#database_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDatabase_name(SqlParserParser.Database_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#source_table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSource_table_name(SqlParserParser.Source_table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_name(SqlParserParser.Table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#table_or_index_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_or_index_name(SqlParserParser.Table_or_index_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#new_table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNew_table_name(SqlParserParser.New_table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#column_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_name(SqlParserParser.Column_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#collation_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCollation_name(SqlParserParser.Collation_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#foreign_table}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitForeign_table(SqlParserParser.Foreign_tableContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#index_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIndex_name(SqlParserParser.Index_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#trigger_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTrigger_name(SqlParserParser.Trigger_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#view_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitView_name(SqlParserParser.View_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#module_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitModule_name(SqlParserParser.Module_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#pragma_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPragma_name(SqlParserParser.Pragma_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#savepoint_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSavepoint_name(SqlParserParser.Savepoint_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#table_alias}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_alias(SqlParserParser.Table_aliasContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#transaction_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTransaction_name(SqlParserParser.Transaction_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link SqlParserParser#any_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAny_name(SqlParserParser.Any_nameContext ctx);
}