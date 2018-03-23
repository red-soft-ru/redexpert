// Generated from D:/gitProjects/executequery/src/org/underworldlabs/sqlParser\REDDATABASESql.g4 by ANTLR 4.7
package org.underworldlabs.sqlParser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link REDDATABASESqlParser}.
 */
public interface REDDATABASESqlListener extends ParseTreeListener {
    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#parse}.
     *
     * @param ctx the parse tree
     */
    void enterParse(REDDATABASESqlParser.ParseContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#parse}.
     *
     * @param ctx the parse tree
     */
    void exitParse(REDDATABASESqlParser.ParseContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#error}.
     *
     * @param ctx the parse tree
     */
    void enterError(REDDATABASESqlParser.ErrorContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#error}.
     *
     * @param ctx the parse tree
     */
    void exitError(REDDATABASESqlParser.ErrorContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#sql_stmt_list}.
     *
     * @param ctx the parse tree
     */
    void enterSql_stmt_list(REDDATABASESqlParser.Sql_stmt_listContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#sql_stmt_list}.
     *
     * @param ctx the parse tree
     */
    void exitSql_stmt_list(REDDATABASESqlParser.Sql_stmt_listContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#sql_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterSql_stmt(REDDATABASESqlParser.Sql_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#sql_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitSql_stmt(REDDATABASESqlParser.Sql_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#alter_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterAlter_table_stmt(REDDATABASESqlParser.Alter_table_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#alter_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitAlter_table_stmt(REDDATABASESqlParser.Alter_table_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#alter_table_add_constraint}.
     *
     * @param ctx the parse tree
     */
    void enterAlter_table_add_constraint(REDDATABASESqlParser.Alter_table_add_constraintContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#alter_table_add_constraint}.
     *
     * @param ctx the parse tree
     */
    void exitAlter_table_add_constraint(REDDATABASESqlParser.Alter_table_add_constraintContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#alter_table_add}.
     *
     * @param ctx the parse tree
     */
    void enterAlter_table_add(REDDATABASESqlParser.Alter_table_addContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#alter_table_add}.
     *
     * @param ctx the parse tree
     */
    void exitAlter_table_add(REDDATABASESqlParser.Alter_table_addContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#analyze_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterAnalyze_stmt(REDDATABASESqlParser.Analyze_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#analyze_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitAnalyze_stmt(REDDATABASESqlParser.Analyze_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#attach_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterAttach_stmt(REDDATABASESqlParser.Attach_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#attach_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitAttach_stmt(REDDATABASESqlParser.Attach_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#begin_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterBegin_stmt(REDDATABASESqlParser.Begin_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#begin_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitBegin_stmt(REDDATABASESqlParser.Begin_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#commit_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCommit_stmt(REDDATABASESqlParser.Commit_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#commit_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCommit_stmt(REDDATABASESqlParser.Commit_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#compound_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCompound_select_stmt(REDDATABASESqlParser.Compound_select_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#compound_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCompound_select_stmt(REDDATABASESqlParser.Compound_select_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#create_index_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_index_stmt(REDDATABASESqlParser.Create_index_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#create_index_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_index_stmt(REDDATABASESqlParser.Create_index_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#create_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_table_stmt(REDDATABASESqlParser.Create_table_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#create_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_table_stmt(REDDATABASESqlParser.Create_table_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#create_trigger_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_trigger_stmt(REDDATABASESqlParser.Create_trigger_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#create_trigger_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_trigger_stmt(REDDATABASESqlParser.Create_trigger_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#create_view_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_view_stmt(REDDATABASESqlParser.Create_view_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#create_view_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_view_stmt(REDDATABASESqlParser.Create_view_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#create_virtual_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_virtual_table_stmt(REDDATABASESqlParser.Create_virtual_table_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#create_virtual_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_virtual_table_stmt(REDDATABASESqlParser.Create_virtual_table_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#create_procedure_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_procedure_stmt(REDDATABASESqlParser.Create_procedure_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#create_procedure_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_procedure_stmt(REDDATABASESqlParser.Create_procedure_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#create_or_alter_procedure_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterCreate_or_alter_procedure_stmt(REDDATABASESqlParser.Create_or_alter_procedure_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#create_or_alter_procedure_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitCreate_or_alter_procedure_stmt(REDDATABASESqlParser.Create_or_alter_procedure_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#recreate_procedure_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterRecreate_procedure_stmt(REDDATABASESqlParser.Recreate_procedure_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#recreate_procedure_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitRecreate_procedure_stmt(REDDATABASESqlParser.Recreate_procedure_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#alter_procedure_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterAlter_procedure_stmt(REDDATABASESqlParser.Alter_procedure_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#alter_procedure_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitAlter_procedure_stmt(REDDATABASESqlParser.Alter_procedure_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#execute_block_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterExecute_block_stmt(REDDATABASESqlParser.Execute_block_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#execute_block_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitExecute_block_stmt(REDDATABASESqlParser.Execute_block_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#declare_block}.
     *
     * @param ctx the parse tree
     */
    void enterDeclare_block(REDDATABASESqlParser.Declare_blockContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#declare_block}.
     *
     * @param ctx the parse tree
     */
    void exitDeclare_block(REDDATABASESqlParser.Declare_blockContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#body}.
     *
     * @param ctx the parse tree
     */
    void enterBody(REDDATABASESqlParser.BodyContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#body}.
     *
     * @param ctx the parse tree
     */
    void exitBody(REDDATABASESqlParser.BodyContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#local_variable}.
     *
     * @param ctx the parse tree
     */
    void enterLocal_variable(REDDATABASESqlParser.Local_variableContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#local_variable}.
     *
     * @param ctx the parse tree
     */
    void exitLocal_variable(REDDATABASESqlParser.Local_variableContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#output_parameter}.
     *
     * @param ctx the parse tree
     */
    void enterOutput_parameter(REDDATABASESqlParser.Output_parameterContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#output_parameter}.
     *
     * @param ctx the parse tree
     */
    void exitOutput_parameter(REDDATABASESqlParser.Output_parameterContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#default_value}.
     *
     * @param ctx the parse tree
     */
    void enterDefault_value(REDDATABASESqlParser.Default_valueContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#default_value}.
     *
     * @param ctx the parse tree
     */
    void exitDefault_value(REDDATABASESqlParser.Default_valueContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#variable_name}.
     *
     * @param ctx the parse tree
     */
    void enterVariable_name(REDDATABASESqlParser.Variable_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#variable_name}.
     *
     * @param ctx the parse tree
     */
    void exitVariable_name(REDDATABASESqlParser.Variable_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#input_parameter}.
     *
     * @param ctx the parse tree
     */
    void enterInput_parameter(REDDATABASESqlParser.Input_parameterContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#input_parameter}.
     *
     * @param ctx the parse tree
     */
    void exitInput_parameter(REDDATABASESqlParser.Input_parameterContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#desciption_parameter}.
     *
     * @param ctx the parse tree
     */
    void enterDesciption_parameter(REDDATABASESqlParser.Desciption_parameterContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#desciption_parameter}.
     *
     * @param ctx the parse tree
     */
    void exitDesciption_parameter(REDDATABASESqlParser.Desciption_parameterContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#parameter_name}.
     *
     * @param ctx the parse tree
     */
    void enterParameter_name(REDDATABASESqlParser.Parameter_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#parameter_name}.
     *
     * @param ctx the parse tree
     */
    void exitParameter_name(REDDATABASESqlParser.Parameter_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#datatype}.
     *
     * @param ctx the parse tree
     */
    void enterDatatype(REDDATABASESqlParser.DatatypeContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#datatype}.
     *
     * @param ctx the parse tree
     */
    void exitDatatype(REDDATABASESqlParser.DatatypeContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#datatypeSQL}.
     *
     * @param ctx the parse tree
     */
    void enterDatatypeSQL(REDDATABASESqlParser.DatatypeSQLContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#datatypeSQL}.
     *
     * @param ctx the parse tree
     */
    void exitDatatypeSQL(REDDATABASESqlParser.DatatypeSQLContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#segment_size}.
     *
     * @param ctx the parse tree
     */
    void enterSegment_size(REDDATABASESqlParser.Segment_sizeContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#segment_size}.
     *
     * @param ctx the parse tree
     */
    void exitSegment_size(REDDATABASESqlParser.Segment_sizeContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#int_number}.
     *
     * @param ctx the parse tree
     */
    void enterInt_number(REDDATABASESqlParser.Int_numberContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#int_number}.
     *
     * @param ctx the parse tree
     */
    void exitInt_number(REDDATABASESqlParser.Int_numberContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#array_size}.
     *
     * @param ctx the parse tree
     */
    void enterArray_size(REDDATABASESqlParser.Array_sizeContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#array_size}.
     *
     * @param ctx the parse tree
     */
    void exitArray_size(REDDATABASESqlParser.Array_sizeContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#delete_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDelete_stmt(REDDATABASESqlParser.Delete_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#delete_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDelete_stmt(REDDATABASESqlParser.Delete_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#delete_stmt_limited}.
     *
     * @param ctx the parse tree
     */
    void enterDelete_stmt_limited(REDDATABASESqlParser.Delete_stmt_limitedContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#delete_stmt_limited}.
     *
     * @param ctx the parse tree
     */
    void exitDelete_stmt_limited(REDDATABASESqlParser.Delete_stmt_limitedContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#detach_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDetach_stmt(REDDATABASESqlParser.Detach_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#detach_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDetach_stmt(REDDATABASESqlParser.Detach_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#drop_index_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDrop_index_stmt(REDDATABASESqlParser.Drop_index_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#drop_index_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDrop_index_stmt(REDDATABASESqlParser.Drop_index_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#drop_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDrop_table_stmt(REDDATABASESqlParser.Drop_table_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#drop_table_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDrop_table_stmt(REDDATABASESqlParser.Drop_table_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#drop_trigger_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDrop_trigger_stmt(REDDATABASESqlParser.Drop_trigger_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#drop_trigger_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDrop_trigger_stmt(REDDATABASESqlParser.Drop_trigger_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#drop_view_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterDrop_view_stmt(REDDATABASESqlParser.Drop_view_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#drop_view_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitDrop_view_stmt(REDDATABASESqlParser.Drop_view_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#factored_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterFactored_select_stmt(REDDATABASESqlParser.Factored_select_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#factored_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitFactored_select_stmt(REDDATABASESqlParser.Factored_select_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#insert_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterInsert_stmt(REDDATABASESqlParser.Insert_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#insert_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitInsert_stmt(REDDATABASESqlParser.Insert_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#pragma_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterPragma_stmt(REDDATABASESqlParser.Pragma_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#pragma_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitPragma_stmt(REDDATABASESqlParser.Pragma_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#reindex_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterReindex_stmt(REDDATABASESqlParser.Reindex_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#reindex_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitReindex_stmt(REDDATABASESqlParser.Reindex_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#release_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterRelease_stmt(REDDATABASESqlParser.Release_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#release_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitRelease_stmt(REDDATABASESqlParser.Release_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#rollback_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterRollback_stmt(REDDATABASESqlParser.Rollback_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#rollback_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitRollback_stmt(REDDATABASESqlParser.Rollback_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#savepoint_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterSavepoint_stmt(REDDATABASESqlParser.Savepoint_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#savepoint_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitSavepoint_stmt(REDDATABASESqlParser.Savepoint_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#simple_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterSimple_select_stmt(REDDATABASESqlParser.Simple_select_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#simple_select_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitSimple_select_stmt(REDDATABASESqlParser.Simple_select_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#select_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterSelect_stmt(REDDATABASESqlParser.Select_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#select_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitSelect_stmt(REDDATABASESqlParser.Select_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#select_or_values}.
     *
     * @param ctx the parse tree
     */
    void enterSelect_or_values(REDDATABASESqlParser.Select_or_valuesContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#select_or_values}.
     *
     * @param ctx the parse tree
     */
    void exitSelect_or_values(REDDATABASESqlParser.Select_or_valuesContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#update_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterUpdate_stmt(REDDATABASESqlParser.Update_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#update_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitUpdate_stmt(REDDATABASESqlParser.Update_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#update_stmt_limited}.
     *
     * @param ctx the parse tree
     */
    void enterUpdate_stmt_limited(REDDATABASESqlParser.Update_stmt_limitedContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#update_stmt_limited}.
     *
     * @param ctx the parse tree
     */
    void exitUpdate_stmt_limited(REDDATABASESqlParser.Update_stmt_limitedContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#vacuum_stmt}.
     *
     * @param ctx the parse tree
     */
    void enterVacuum_stmt(REDDATABASESqlParser.Vacuum_stmtContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#vacuum_stmt}.
     *
     * @param ctx the parse tree
     */
    void exitVacuum_stmt(REDDATABASESqlParser.Vacuum_stmtContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_def}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_def(REDDATABASESqlParser.Column_defContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_def}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_def(REDDATABASESqlParser.Column_defContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#type_name}.
     *
     * @param ctx the parse tree
     */
    void enterType_name(REDDATABASESqlParser.Type_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#type_name}.
     *
     * @param ctx the parse tree
     */
    void exitType_name(REDDATABASESqlParser.Type_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_constraint}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint(REDDATABASESqlParser.Column_constraintContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_constraint}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint(REDDATABASESqlParser.Column_constraintContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_constraint_primary_key}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint_primary_key(REDDATABASESqlParser.Column_constraint_primary_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_constraint_primary_key}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint_primary_key(REDDATABASESqlParser.Column_constraint_primary_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint_foreign_key(REDDATABASESqlParser.Column_constraint_foreign_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint_foreign_key(REDDATABASESqlParser.Column_constraint_foreign_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_constraint_not_null}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint_not_null(REDDATABASESqlParser.Column_constraint_not_nullContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_constraint_not_null}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint_not_null(REDDATABASESqlParser.Column_constraint_not_nullContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_constraint_null}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_constraint_null(REDDATABASESqlParser.Column_constraint_nullContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_constraint_null}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_constraint_null(REDDATABASESqlParser.Column_constraint_nullContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_default}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_default(REDDATABASESqlParser.Column_defaultContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_default}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_default(REDDATABASESqlParser.Column_defaultContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_default_value}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_default_value(REDDATABASESqlParser.Column_default_valueContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_default_value}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_default_value(REDDATABASESqlParser.Column_default_valueContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#conflict_clause}.
     *
     * @param ctx the parse tree
     */
    void enterConflict_clause(REDDATABASESqlParser.Conflict_clauseContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#conflict_clause}.
     *
     * @param ctx the parse tree
     */
    void exitConflict_clause(REDDATABASESqlParser.Conflict_clauseContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#expr}.
     *
     * @param ctx the parse tree
     */
    void enterExpr(REDDATABASESqlParser.ExprContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#expr}.
     *
     * @param ctx the parse tree
     */
    void exitExpr(REDDATABASESqlParser.ExprContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#foreign_key_clause}.
     *
     * @param ctx the parse tree
     */
    void enterForeign_key_clause(REDDATABASESqlParser.Foreign_key_clauseContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#foreign_key_clause}.
     *
     * @param ctx the parse tree
     */
    void exitForeign_key_clause(REDDATABASESqlParser.Foreign_key_clauseContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#fk_target_column_name}.
     *
     * @param ctx the parse tree
     */
    void enterFk_target_column_name(REDDATABASESqlParser.Fk_target_column_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#fk_target_column_name}.
     *
     * @param ctx the parse tree
     */
    void exitFk_target_column_name(REDDATABASESqlParser.Fk_target_column_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#raise_function}.
     *
     * @param ctx the parse tree
     */
    void enterRaise_function(REDDATABASESqlParser.Raise_functionContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#raise_function}.
     *
     * @param ctx the parse tree
     */
    void exitRaise_function(REDDATABASESqlParser.Raise_functionContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#indexed_column}.
     *
     * @param ctx the parse tree
     */
    void enterIndexed_column(REDDATABASESqlParser.Indexed_columnContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#indexed_column}.
     *
     * @param ctx the parse tree
     */
    void exitIndexed_column(REDDATABASESqlParser.Indexed_columnContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#table_constraint}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint(REDDATABASESqlParser.Table_constraintContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#table_constraint}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint(REDDATABASESqlParser.Table_constraintContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#table_constraint_primary_key}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint_primary_key(REDDATABASESqlParser.Table_constraint_primary_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#table_constraint_primary_key}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint_primary_key(REDDATABASESqlParser.Table_constraint_primary_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#table_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint_foreign_key(REDDATABASESqlParser.Table_constraint_foreign_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#table_constraint_foreign_key}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint_foreign_key(REDDATABASESqlParser.Table_constraint_foreign_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#table_constraint_unique}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint_unique(REDDATABASESqlParser.Table_constraint_uniqueContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#table_constraint_unique}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint_unique(REDDATABASESqlParser.Table_constraint_uniqueContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#table_constraint_key}.
     *
     * @param ctx the parse tree
     */
    void enterTable_constraint_key(REDDATABASESqlParser.Table_constraint_keyContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#table_constraint_key}.
     *
     * @param ctx the parse tree
     */
    void exitTable_constraint_key(REDDATABASESqlParser.Table_constraint_keyContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#fk_origin_column_name}.
     *
     * @param ctx the parse tree
     */
    void enterFk_origin_column_name(REDDATABASESqlParser.Fk_origin_column_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#fk_origin_column_name}.
     *
     * @param ctx the parse tree
     */
    void exitFk_origin_column_name(REDDATABASESqlParser.Fk_origin_column_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#with_clause}.
     *
     * @param ctx the parse tree
     */
    void enterWith_clause(REDDATABASESqlParser.With_clauseContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#with_clause}.
     *
     * @param ctx the parse tree
     */
    void exitWith_clause(REDDATABASESqlParser.With_clauseContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#qualified_table_name}.
     *
     * @param ctx the parse tree
     */
    void enterQualified_table_name(REDDATABASESqlParser.Qualified_table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#qualified_table_name}.
     *
     * @param ctx the parse tree
     */
    void exitQualified_table_name(REDDATABASESqlParser.Qualified_table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#ordering_term}.
     *
     * @param ctx the parse tree
     */
    void enterOrdering_term(REDDATABASESqlParser.Ordering_termContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#ordering_term}.
     *
     * @param ctx the parse tree
     */
    void exitOrdering_term(REDDATABASESqlParser.Ordering_termContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#order_collate}.
     *
     * @param ctx the parse tree
     */
    void enterOrder_collate(REDDATABASESqlParser.Order_collateContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#order_collate}.
     *
     * @param ctx the parse tree
     */
    void exitOrder_collate(REDDATABASESqlParser.Order_collateContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#pragma_value}.
     *
     * @param ctx the parse tree
     */
    void enterPragma_value(REDDATABASESqlParser.Pragma_valueContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#pragma_value}.
     *
     * @param ctx the parse tree
     */
    void exitPragma_value(REDDATABASESqlParser.Pragma_valueContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#common_table_expression}.
     *
     * @param ctx the parse tree
     */
    void enterCommon_table_expression(REDDATABASESqlParser.Common_table_expressionContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#common_table_expression}.
     *
     * @param ctx the parse tree
     */
    void exitCommon_table_expression(REDDATABASESqlParser.Common_table_expressionContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#result_column}.
     *
     * @param ctx the parse tree
     */
    void enterResult_column(REDDATABASESqlParser.Result_columnContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#result_column}.
     *
     * @param ctx the parse tree
     */
    void exitResult_column(REDDATABASESqlParser.Result_columnContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#table_or_subquery}.
     *
     * @param ctx the parse tree
     */
    void enterTable_or_subquery(REDDATABASESqlParser.Table_or_subqueryContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#table_or_subquery}.
     *
     * @param ctx the parse tree
     */
    void exitTable_or_subquery(REDDATABASESqlParser.Table_or_subqueryContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#join_clause}.
     *
     * @param ctx the parse tree
     */
    void enterJoin_clause(REDDATABASESqlParser.Join_clauseContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#join_clause}.
     *
     * @param ctx the parse tree
     */
    void exitJoin_clause(REDDATABASESqlParser.Join_clauseContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#join_operator}.
     *
     * @param ctx the parse tree
     */
    void enterJoin_operator(REDDATABASESqlParser.Join_operatorContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#join_operator}.
     *
     * @param ctx the parse tree
     */
    void exitJoin_operator(REDDATABASESqlParser.Join_operatorContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#join_constraint}.
     *
     * @param ctx the parse tree
     */
    void enterJoin_constraint(REDDATABASESqlParser.Join_constraintContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#join_constraint}.
     *
     * @param ctx the parse tree
     */
    void exitJoin_constraint(REDDATABASESqlParser.Join_constraintContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#select_core}.
     *
     * @param ctx the parse tree
     */
    void enterSelect_core(REDDATABASESqlParser.Select_coreContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#select_core}.
     *
     * @param ctx the parse tree
     */
    void exitSelect_core(REDDATABASESqlParser.Select_coreContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#compound_operator}.
     *
     * @param ctx the parse tree
     */
    void enterCompound_operator(REDDATABASESqlParser.Compound_operatorContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#compound_operator}.
     *
     * @param ctx the parse tree
     */
    void exitCompound_operator(REDDATABASESqlParser.Compound_operatorContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#cte_table_name}.
     *
     * @param ctx the parse tree
     */
    void enterCte_table_name(REDDATABASESqlParser.Cte_table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#cte_table_name}.
     *
     * @param ctx the parse tree
     */
    void exitCte_table_name(REDDATABASESqlParser.Cte_table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#signed_number}.
     *
     * @param ctx the parse tree
     */
    void enterSigned_number(REDDATABASESqlParser.Signed_numberContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#signed_number}.
     *
     * @param ctx the parse tree
     */
    void exitSigned_number(REDDATABASESqlParser.Signed_numberContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#literal_value}.
     *
     * @param ctx the parse tree
     */
    void enterLiteral_value(REDDATABASESqlParser.Literal_valueContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#literal_value}.
     *
     * @param ctx the parse tree
     */
    void exitLiteral_value(REDDATABASESqlParser.Literal_valueContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#unary_operator}.
     *
     * @param ctx the parse tree
     */
    void enterUnary_operator(REDDATABASESqlParser.Unary_operatorContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#unary_operator}.
     *
     * @param ctx the parse tree
     */
    void exitUnary_operator(REDDATABASESqlParser.Unary_operatorContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#error_message}.
     *
     * @param ctx the parse tree
     */
    void enterError_message(REDDATABASESqlParser.Error_messageContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#error_message}.
     *
     * @param ctx the parse tree
     */
    void exitError_message(REDDATABASESqlParser.Error_messageContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#module_argument}.
     *
     * @param ctx the parse tree
     */
    void enterModule_argument(REDDATABASESqlParser.Module_argumentContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#module_argument}.
     *
     * @param ctx the parse tree
     */
    void exitModule_argument(REDDATABASESqlParser.Module_argumentContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_alias}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_alias(REDDATABASESqlParser.Column_aliasContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_alias}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_alias(REDDATABASESqlParser.Column_aliasContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#keyword}.
     *
     * @param ctx the parse tree
     */
    void enterKeyword(REDDATABASESqlParser.KeywordContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#keyword}.
     *
     * @param ctx the parse tree
     */
    void exitKeyword(REDDATABASESqlParser.KeywordContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#unknown}.
     *
     * @param ctx the parse tree
     */
    void enterUnknown(REDDATABASESqlParser.UnknownContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#unknown}.
     *
     * @param ctx the parse tree
     */
    void exitUnknown(REDDATABASESqlParser.UnknownContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#name}.
     *
     * @param ctx the parse tree
     */
    void enterName(REDDATABASESqlParser.NameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#name}.
     *
     * @param ctx the parse tree
     */
    void exitName(REDDATABASESqlParser.NameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#function_name}.
     *
     * @param ctx the parse tree
     */
    void enterFunction_name(REDDATABASESqlParser.Function_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#function_name}.
     *
     * @param ctx the parse tree
     */
    void exitFunction_name(REDDATABASESqlParser.Function_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#database_name}.
     *
     * @param ctx the parse tree
     */
    void enterDatabase_name(REDDATABASESqlParser.Database_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#database_name}.
     *
     * @param ctx the parse tree
     */
    void exitDatabase_name(REDDATABASESqlParser.Database_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#domain_name}.
     *
     * @param ctx the parse tree
     */
    void enterDomain_name(REDDATABASESqlParser.Domain_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#domain_name}.
     *
     * @param ctx the parse tree
     */
    void exitDomain_name(REDDATABASESqlParser.Domain_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#source_table_name}.
     *
     * @param ctx the parse tree
     */
    void enterSource_table_name(REDDATABASESqlParser.Source_table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#source_table_name}.
     *
     * @param ctx the parse tree
     */
    void exitSource_table_name(REDDATABASESqlParser.Source_table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#table_name}.
     *
     * @param ctx the parse tree
     */
    void enterTable_name(REDDATABASESqlParser.Table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#table_name}.
     *
     * @param ctx the parse tree
     */
    void exitTable_name(REDDATABASESqlParser.Table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#procedure_name}.
     *
     * @param ctx the parse tree
     */
    void enterProcedure_name(REDDATABASESqlParser.Procedure_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#procedure_name}.
     *
     * @param ctx the parse tree
     */
    void exitProcedure_name(REDDATABASESqlParser.Procedure_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#table_or_index_name}.
     *
     * @param ctx the parse tree
     */
    void enterTable_or_index_name(REDDATABASESqlParser.Table_or_index_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#table_or_index_name}.
     *
     * @param ctx the parse tree
     */
    void exitTable_or_index_name(REDDATABASESqlParser.Table_or_index_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#new_table_name}.
     *
     * @param ctx the parse tree
     */
    void enterNew_table_name(REDDATABASESqlParser.New_table_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#new_table_name}.
     *
     * @param ctx the parse tree
     */
    void exitNew_table_name(REDDATABASESqlParser.New_table_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#column_name}.
     *
     * @param ctx the parse tree
     */
    void enterColumn_name(REDDATABASESqlParser.Column_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#column_name}.
     *
     * @param ctx the parse tree
     */
    void exitColumn_name(REDDATABASESqlParser.Column_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#collation_name}.
     *
     * @param ctx the parse tree
     */
    void enterCollation_name(REDDATABASESqlParser.Collation_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#collation_name}.
     *
     * @param ctx the parse tree
     */
    void exitCollation_name(REDDATABASESqlParser.Collation_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#foreign_table}.
     *
     * @param ctx the parse tree
     */
    void enterForeign_table(REDDATABASESqlParser.Foreign_tableContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#foreign_table}.
     *
     * @param ctx the parse tree
     */
    void exitForeign_table(REDDATABASESqlParser.Foreign_tableContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#index_name}.
     *
     * @param ctx the parse tree
     */
    void enterIndex_name(REDDATABASESqlParser.Index_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#index_name}.
     *
     * @param ctx the parse tree
     */
    void exitIndex_name(REDDATABASESqlParser.Index_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#trigger_name}.
     *
     * @param ctx the parse tree
     */
    void enterTrigger_name(REDDATABASESqlParser.Trigger_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#trigger_name}.
     *
     * @param ctx the parse tree
     */
    void exitTrigger_name(REDDATABASESqlParser.Trigger_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#view_name}.
     *
     * @param ctx the parse tree
     */
    void enterView_name(REDDATABASESqlParser.View_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#view_name}.
     *
     * @param ctx the parse tree
     */
    void exitView_name(REDDATABASESqlParser.View_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#module_name}.
     *
     * @param ctx the parse tree
     */
    void enterModule_name(REDDATABASESqlParser.Module_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#module_name}.
     *
     * @param ctx the parse tree
     */
    void exitModule_name(REDDATABASESqlParser.Module_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#pragma_name}.
     *
     * @param ctx the parse tree
     */
    void enterPragma_name(REDDATABASESqlParser.Pragma_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#pragma_name}.
     *
     * @param ctx the parse tree
     */
    void exitPragma_name(REDDATABASESqlParser.Pragma_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#savepoint_name}.
     *
     * @param ctx the parse tree
     */
    void enterSavepoint_name(REDDATABASESqlParser.Savepoint_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#savepoint_name}.
     *
     * @param ctx the parse tree
     */
    void exitSavepoint_name(REDDATABASESqlParser.Savepoint_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#table_alias}.
     *
     * @param ctx the parse tree
     */
    void enterTable_alias(REDDATABASESqlParser.Table_aliasContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#table_alias}.
     *
     * @param ctx the parse tree
     */
    void exitTable_alias(REDDATABASESqlParser.Table_aliasContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#transaction_name}.
     *
     * @param ctx the parse tree
     */
    void enterTransaction_name(REDDATABASESqlParser.Transaction_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#transaction_name}.
     *
     * @param ctx the parse tree
     */
    void exitTransaction_name(REDDATABASESqlParser.Transaction_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#charset_name}.
     *
     * @param ctx the parse tree
     */
    void enterCharset_name(REDDATABASESqlParser.Charset_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#charset_name}.
     *
     * @param ctx the parse tree
     */
    void exitCharset_name(REDDATABASESqlParser.Charset_nameContext ctx);

    /**
     * Enter a parse tree produced by {@link REDDATABASESqlParser#any_name}.
     *
     * @param ctx the parse tree
     */
    void enterAny_name(REDDATABASESqlParser.Any_nameContext ctx);

    /**
     * Exit a parse tree produced by {@link REDDATABASESqlParser#any_name}.
     *
     * @param ctx the parse tree
     */
    void exitAny_name(REDDATABASESqlParser.Any_nameContext ctx);
}