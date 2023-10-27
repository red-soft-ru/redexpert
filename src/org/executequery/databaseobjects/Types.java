package org.executequery.databaseobjects;

@SuppressWarnings("unused")
public interface Types {
    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code BIT}.
     */
    int BIT = -7;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code TINYINT}.
     */
    int TINYINT = -6;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code SMALLINT}.
     */
    int SMALLINT = 5;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code INTEGER}.
     */
    int INTEGER = 4;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code BIGINT}.
     */
    int BIGINT = -5;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code FLOAT}.
     */
    int FLOAT = 6;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code REAL}.
     */
    int REAL = 7;


    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code DOUBLE}.
     */
    int DOUBLE = 8;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code NUMERIC}.
     */
    int NUMERIC = 2;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code DECIMAL}.
     */
    int DECIMAL = 3;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code CHAR}.
     */
    int CHAR = 1;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code VARCHAR}.
     */
    int VARCHAR = 12;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code LONGVARCHAR}.
     */
    int LONGVARCHAR = -1;


    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code DATE}.
     */
    int DATE = 91;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code TIME}.
     */
    int TIME = 92;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code TIMESTAMP}.
     */
    int TIMESTAMP = 93;


    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code BINARY}.
     */
    int BINARY = -2;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code VARBINARY}.
     */
    int VARBINARY = -3;

    /**
     * <P>The constant in the Java programming language, sometimes referred
     * to as a type code, that identifies the generic SQL type
     * {@code LONGVARBINARY}.
     */
    int LONGVARBINARY = -4;

    /**
     * <P>The constant in the Java programming language
     * that identifies the generic SQL value
     * {@code NULL}.
     */
    int NULL = 0;

    /**
     * The constant in the Java programming language that indicates
     * that the SQL type is database-specific and
     * gets mapped to a Java object that can be accessed via
     * the methods {@code getObject} and {@code setObject}.
     */
    int OTHER = 1111;


    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code JAVA_OBJECT}.
     *
     * @since 1.2
     */
    int JAVA_OBJECT = 2000;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code DISTINCT}.
     *
     * @since 1.2
     */
    int DISTINCT = 2001;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code STRUCT}.
     *
     * @since 1.2
     */
    int STRUCT = 2002;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code ARRAY}.
     *
     * @since 1.2
     */
    int ARRAY = 2003;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code BLOB}.
     *
     * @since 1.2
     */
    int BLOB = 2004;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code CLOB}.
     *
     * @since 1.2
     */
    int CLOB = 2005;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code REF}.
     *
     * @since 1.2
     */
    int REF = 2006;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code DATALINK}.
     *
     * @since 1.4
     */
    int DATALINK = 70;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code BOOLEAN}.
     *
     * @since 1.4
     */
    int BOOLEAN = 16;

    //------------------------- JDBC 4.0 -----------------------------------

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code ROWID}
     *
     * @since 1.6
     */
    int ROWID = -8;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code NCHAR}
     *
     * @since 1.6
     */
    int NCHAR = -15;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code NVARCHAR}.
     *
     * @since 1.6
     */
    int NVARCHAR = -9;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code LONGNVARCHAR}.
     *
     * @since 1.6
     */
    int LONGNVARCHAR = -16;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code NCLOB}.
     *
     * @since 1.6
     */
    int NCLOB = 2011;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code XML}.
     *
     * @since 1.6
     */
    int SQLXML = 2009;

    //--------------------------JDBC 4.2 -----------------------------

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type {@code REF CURSOR}.
     *
     * @since 1.8
     */
    int REF_CURSOR = 2012;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code TIME WITH TIMEZONE}.
     *
     * @since 1.8
     */
    int TIME_WITH_TIMEZONE = 2013;

    /**
     * The constant in the Java programming language, sometimes referred to
     * as a type code, that identifies the generic SQL type
     * {@code TIMESTAMP WITH TIMEZONE}.
     *
     * @since 1.8
     */
    int TIMESTAMP_WITH_TIMEZONE = 2014;

    //-------------------------- RDB -----------------------------

    /**
     * The constant as a type code,
     * that identifies the specific RedDatabase SQL type
     * {@code INT128}.
     */
    int INT128 = -55;

}
