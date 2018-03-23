// Generated from D:/gitProjects/executequery/src/org/underworldlabs/sqlParser\REDDATABASESql.g4 by ANTLR 4.7
package org.underworldlabs.sqlParser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link REDDATABASESqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface REDDATABASESqlVisitor<T> extends ParseTreeVisitor<T> {
    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#parse}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitParse(REDDATABASESqlParser.ParseContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#error}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitError(REDDATABASESqlParser.ErrorContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#sql_stmt_list}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSql_stmt_list(REDDATABASESqlParser.Sql_stmt_listContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#sql_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSql_stmt(REDDATABASESqlParser.Sql_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#alter_table_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlter_table_stmt(REDDATABASESqlParser.Alter_table_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#alter_table_add_constraint}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlter_table_add_constraint(REDDATABASESqlParser.Alter_table_add_constraintContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#alter_table_add}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlter_table_add(REDDATABASESqlParser.Alter_table_addContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#analyze_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAnalyze_stmt(REDDATABASESqlParser.Analyze_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#attach_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAttach_stmt(REDDATABASESqlParser.Attach_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#begin_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBegin_stmt(REDDATABASESqlParser.Begin_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#commit_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCommit_stmt(REDDATABASESqlParser.Commit_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#compound_select_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCompound_select_stmt(REDDATABASESqlParser.Compound_select_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#create_index_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_index_stmt(REDDATABASESqlParser.Create_index_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#create_table_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_table_stmt(REDDATABASESqlParser.Create_table_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#create_trigger_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_trigger_stmt(REDDATABASESqlParser.Create_trigger_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#create_view_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_view_stmt(REDDATABASESqlParser.Create_view_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#create_virtual_table_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_virtual_table_stmt(REDDATABASESqlParser.Create_virtual_table_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#create_procedure_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_procedure_stmt(REDDATABASESqlParser.Create_procedure_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#create_or_alter_procedure_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCreate_or_alter_procedure_stmt(REDDATABASESqlParser.Create_or_alter_procedure_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#recreate_procedure_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitRecreate_procedure_stmt(REDDATABASESqlParser.Recreate_procedure_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#alter_procedure_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAlter_procedure_stmt(REDDATABASESqlParser.Alter_procedure_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#execute_block_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitExecute_block_stmt(REDDATABASESqlParser.Execute_block_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#declare_block}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDeclare_block(REDDATABASESqlParser.Declare_blockContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#body}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitBody(REDDATABASESqlParser.BodyContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#local_variable}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitLocal_variable(REDDATABASESqlParser.Local_variableContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#output_parameter}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOutput_parameter(REDDATABASESqlParser.Output_parameterContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#default_value}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDefault_value(REDDATABASESqlParser.Default_valueContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#variable_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVariable_name(REDDATABASESqlParser.Variable_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#input_parameter}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitInput_parameter(REDDATABASESqlParser.Input_parameterContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#desciption_parameter}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDesciption_parameter(REDDATABASESqlParser.Desciption_parameterContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#parameter_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitParameter_name(REDDATABASESqlParser.Parameter_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#datatype}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDatatype(REDDATABASESqlParser.DatatypeContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#datatypeSQL}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDatatypeSQL(REDDATABASESqlParser.DatatypeSQLContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#segment_size}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSegment_size(REDDATABASESqlParser.Segment_sizeContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#int_number}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitInt_number(REDDATABASESqlParser.Int_numberContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#array_size}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitArray_size(REDDATABASESqlParser.Array_sizeContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#delete_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDelete_stmt(REDDATABASESqlParser.Delete_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#delete_stmt_limited}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDelete_stmt_limited(REDDATABASESqlParser.Delete_stmt_limitedContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#detach_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDetach_stmt(REDDATABASESqlParser.Detach_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#drop_index_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDrop_index_stmt(REDDATABASESqlParser.Drop_index_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#drop_table_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDrop_table_stmt(REDDATABASESqlParser.Drop_table_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#drop_trigger_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDrop_trigger_stmt(REDDATABASESqlParser.Drop_trigger_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#drop_view_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDrop_view_stmt(REDDATABASESqlParser.Drop_view_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#factored_select_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFactored_select_stmt(REDDATABASESqlParser.Factored_select_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#insert_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitInsert_stmt(REDDATABASESqlParser.Insert_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#pragma_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPragma_stmt(REDDATABASESqlParser.Pragma_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#reindex_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitReindex_stmt(REDDATABASESqlParser.Reindex_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#release_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitRelease_stmt(REDDATABASESqlParser.Release_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#rollback_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitRollback_stmt(REDDATABASESqlParser.Rollback_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#savepoint_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSavepoint_stmt(REDDATABASESqlParser.Savepoint_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#simple_select_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSimple_select_stmt(REDDATABASESqlParser.Simple_select_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#select_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSelect_stmt(REDDATABASESqlParser.Select_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#select_or_values}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSelect_or_values(REDDATABASESqlParser.Select_or_valuesContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#update_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUpdate_stmt(REDDATABASESqlParser.Update_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#update_stmt_limited}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUpdate_stmt_limited(REDDATABASESqlParser.Update_stmt_limitedContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#vacuum_stmt}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitVacuum_stmt(REDDATABASESqlParser.Vacuum_stmtContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_def}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_def(REDDATABASESqlParser.Column_defContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#type_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitType_name(REDDATABASESqlParser.Type_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_constraint}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint(REDDATABASESqlParser.Column_constraintContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_constraint_primary_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint_primary_key(REDDATABASESqlParser.Column_constraint_primary_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint_foreign_key(REDDATABASESqlParser.Column_constraint_foreign_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_constraint_not_null}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint_not_null(REDDATABASESqlParser.Column_constraint_not_nullContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_constraint_null}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_constraint_null(REDDATABASESqlParser.Column_constraint_nullContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_default}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_default(REDDATABASESqlParser.Column_defaultContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_default_value}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_default_value(REDDATABASESqlParser.Column_default_valueContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#conflict_clause}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitConflict_clause(REDDATABASESqlParser.Conflict_clauseContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#expr}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitExpr(REDDATABASESqlParser.ExprContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#foreign_key_clause}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitForeign_key_clause(REDDATABASESqlParser.Foreign_key_clauseContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#fk_target_column_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFk_target_column_name(REDDATABASESqlParser.Fk_target_column_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#raise_function}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitRaise_function(REDDATABASESqlParser.Raise_functionContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#indexed_column}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIndexed_column(REDDATABASESqlParser.Indexed_columnContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#table_constraint}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint(REDDATABASESqlParser.Table_constraintContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#table_constraint_primary_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint_primary_key(REDDATABASESqlParser.Table_constraint_primary_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#table_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint_foreign_key(REDDATABASESqlParser.Table_constraint_foreign_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#table_constraint_unique}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint_unique(REDDATABASESqlParser.Table_constraint_uniqueContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#table_constraint_key}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_constraint_key(REDDATABASESqlParser.Table_constraint_keyContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#fk_origin_column_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFk_origin_column_name(REDDATABASESqlParser.Fk_origin_column_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#with_clause}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitWith_clause(REDDATABASESqlParser.With_clauseContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#qualified_table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitQualified_table_name(REDDATABASESqlParser.Qualified_table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#ordering_term}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOrdering_term(REDDATABASESqlParser.Ordering_termContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#order_collate}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitOrder_collate(REDDATABASESqlParser.Order_collateContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#pragma_value}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPragma_value(REDDATABASESqlParser.Pragma_valueContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#common_table_expression}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCommon_table_expression(REDDATABASESqlParser.Common_table_expressionContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#result_column}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitResult_column(REDDATABASESqlParser.Result_columnContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#table_or_subquery}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_or_subquery(REDDATABASESqlParser.Table_or_subqueryContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#join_clause}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitJoin_clause(REDDATABASESqlParser.Join_clauseContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#join_operator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitJoin_operator(REDDATABASESqlParser.Join_operatorContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#join_constraint}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitJoin_constraint(REDDATABASESqlParser.Join_constraintContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#select_core}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSelect_core(REDDATABASESqlParser.Select_coreContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#compound_operator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCompound_operator(REDDATABASESqlParser.Compound_operatorContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#cte_table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCte_table_name(REDDATABASESqlParser.Cte_table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#signed_number}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSigned_number(REDDATABASESqlParser.Signed_numberContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#literal_value}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitLiteral_value(REDDATABASESqlParser.Literal_valueContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#unary_operator}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUnary_operator(REDDATABASESqlParser.Unary_operatorContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#error_message}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitError_message(REDDATABASESqlParser.Error_messageContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#module_argument}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitModule_argument(REDDATABASESqlParser.Module_argumentContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_alias}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_alias(REDDATABASESqlParser.Column_aliasContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#keyword}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitKeyword(REDDATABASESqlParser.KeywordContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#unknown}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitUnknown(REDDATABASESqlParser.UnknownContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitName(REDDATABASESqlParser.NameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#function_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitFunction_name(REDDATABASESqlParser.Function_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#database_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDatabase_name(REDDATABASESqlParser.Database_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#domain_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitDomain_name(REDDATABASESqlParser.Domain_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#source_table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSource_table_name(REDDATABASESqlParser.Source_table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_name(REDDATABASESqlParser.Table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#procedure_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitProcedure_name(REDDATABASESqlParser.Procedure_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#table_or_index_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_or_index_name(REDDATABASESqlParser.Table_or_index_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#new_table_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitNew_table_name(REDDATABASESqlParser.New_table_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#column_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitColumn_name(REDDATABASESqlParser.Column_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#collation_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCollation_name(REDDATABASESqlParser.Collation_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#foreign_table}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitForeign_table(REDDATABASESqlParser.Foreign_tableContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#index_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitIndex_name(REDDATABASESqlParser.Index_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#trigger_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTrigger_name(REDDATABASESqlParser.Trigger_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#view_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitView_name(REDDATABASESqlParser.View_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#module_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitModule_name(REDDATABASESqlParser.Module_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#pragma_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitPragma_name(REDDATABASESqlParser.Pragma_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#savepoint_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitSavepoint_name(REDDATABASESqlParser.Savepoint_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#table_alias}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTable_alias(REDDATABASESqlParser.Table_aliasContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#transaction_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitTransaction_name(REDDATABASESqlParser.Transaction_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#charset_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitCharset_name(REDDATABASESqlParser.Charset_nameContext ctx);

    /**
     * Visit a parse tree produced by {@link REDDATABASESqlParser#any_name}.
     *
     * @param ctx the parse tree
     * @return the visitor result
     */
    T visitAny_name(REDDATABASESqlParser.Any_nameContext ctx);
}