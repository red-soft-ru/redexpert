--- Execute Query - RELEASE NOTES ---

Date: 27 April 2016

Summary of changes - v4.3.3 Build 5456:
----------------------------------------

- Fixed SQL text pane background hardcoded to white for theme changes.
- Wrapped statement escape setting on query execution to avoid possible error
  for unimplemented with some JDBC drivers. 
- Separated binary types to use BLOB or BYTE.
- Modified thread model for connection disconnections to avoid locks.
- Modified to allow for correct multi-line text values in the table data editor.
- Added column names option to result set cell/row copy to clipboard.


Summary of changes - v4.3.2 Build 5449:
----------------------------------------

- Modified to examine result set return values as objects and strings to render
  appropriately within result set tables - eg. timestamp with time zone values.
- Allowed for execution of stored procs where the meta data call for the proc 
  fails. Proc with supplied parameters is executed as-is.
- Introduced a new dark theme - Execute Query Dark Theme.
- Now logging a warning only when attempting and failing to set transaction 
  isolation level on a connection.
- Modified text pane log event thread in render to avoid possible write locks.
- Fixed result set table sort issue when selecting and rendering boolean types.
- Added delay to table row count query for browser node selections to avoid 
  system hang when moving through selection nodes - eg. keyboard arrow
  scrolling up/down quickly.
- Added delay to table data retrieval for browser node selection ensuring no 
  unecessary connections are opened and statements executed. 
- Fixed error on selecting to disconnect from the browser tree popup menu when 
  the current selection is not a host node.
- Modified to allow double-click item selections on available/selected list 
  panels, ie. import/export column selections.
- Added new menu items to Result Set table to copy cells as comma-separated 
  values with or without quotes. 


Summary of changes - v4.3.1 Build 5443:
----------------------------------------

- Added view menu to table references tab in the Browser. 
- Added menu item for a schema node selection to create a new connection with 
  the selected schema as the data source.
- Fixed possible issue with older JDBC drivers where DatabaseMetaData method 
  getFunctions() is not implemented throwing an AbstractMethodError and failing
  to render the selection details.
- Fixed possible concurrency issue with connection pool.
- Modified to restore new connections XML on failed file load.
- Added SSH tunnel support to enable database connections over SSH.
- Upgraded to Liquibase 3.4.1
- Fixed possible error on table selection from the Browser tree nodes where 
  no constraints are available for that table.
- Fixed possible concurrency issue on open connections when applying the 
  transaction isolation level.
- Added 'Move to folder' menu item and dialog for a database connections. 
- Added edit text case to support underscore and camel


Summary of changes - v4.3.0 Build 5427:
----------------------------------------

- Modified format SQL text to apply formatting to query at cursor and replace
  only that region.
- Added result set filter for the current selection in the Query Editor.
- Added wildcard starts with for browser node search.
- Fixed result set table column in the editor not showing correct label on some 
  DBs when executing statements with aliases.
- Added ability to select the visible result set columns after a query is 
  executed.
- Added procedure and function names to Query Editor autocomplete suggestions.
- Fixed issue with number format errors returned when generating SQL scripts.
- Fixed column data type precision not showing for some database impls.
- Fixed column width size not being retained when set via preferences within the
  Query Editor. 
- Fixed locking issue for certain database types where on open transaction would
  cause the browser to hang.
- Added new updated default EQ look and feel.
- Modified references tab in Browser panel to always show selected table 
  irrespective of imported or exported constraints.
- Added SQLite to preconfigured database list for driver selection.
- Fixed SQLite issue with rendering query result sets within the editor.


Summary of changes - v4.2.0 Build 5406:
----------------------------------------

- Added user preference option to disable row count query on table selection.
- Fixed export as SQL not respecting column selections for single table exports.
- Fixed multi-line comment issue when start of each line within the comment 
  contains an asterisk (*).
- Added ability to maximize and restore the results pane within the editor tab 
  on double-click.
- Fixed find/replace dialog functionality on replace replacing the next find 
  instead of the current one.
- Added ability to cancel the query executed within the Data tab on table 
  selection.  
- Removed dependency on agent to set the correct application name for X11.
- Removed proprietary dependency on sun.misc.BASE64Encoder/Decoder.
- Added column, constraints and index nodes under table nodes within the Browser
  tree structure.
- Improved Query Editor undo/redo support.
- Improved support for large result set exports.
- Fixed issue where a driver class name change was not being applied on 
  reconnection with that driver.
- Moved to Java7 - no longer direct support for Java 6.
- Modified Execute SQL Script to support progress logging.


Summary of changes - v4.1.1 Build 5388:
----------------------------------------

- Fixed issue with return type and value from executing a stored procedure or 
  function from the Query Editor.
- Modified application update check on startup to not block database connection 
  attempts. 
- Added on delete, on update and deferability to SQL generation for constraints.
- Modified Query Editor history dialog to accept multiple selections.
- Upgraded to Liquibase 3.1.1
- Fixed BIT and BOOLEAN column definition with default value for statement 
  generation.
- Fixed transaction isolation level setting not being correctly assigned.
- Added copy action for the connection Show Password dialog.
- Added Insert at Cursor action for the editor's SQL Command History.


Summary of changes - v4.1.0 Build 5326:
----------------------------------------

- Fixed issue where a lost data source causes a closing editor to hang.
- Modified type conversion of result set table views.
- Fixed determination and display of array values in the result set table views.
- Modified the query generated to apply data changes within the Data tab to use
  a schema or catalog prefix where available.
- Update Liquibase to v3.0.2 and modified to use new API.
- Added new library juniversalchardet to detect file encoding when opening text
  files and apply this encoding for rendering in the Query Editor.
- Added XML as export option of result set from Query Editor.
- Modified CSV result set exporter to quote char, varchar and longvarchar 
  values.


Summary of changes - v4.0.1 Build 5306:
----------------------------------------

- Modified result set model to log and return for unsupported data conversions
  in values.
- Modified time value returns in result set to check for full length as in 
  HH:mm:ss.S or HH:mm:ss before attempting to parse.


Summary of changes - v4.0.0 Build 5297:
----------------------------------------

- Fixed possible NPE on null catalog value when setting show default cat/schema
  option only.
- Added database table data edit support for the browser Data tab where a 
  primary key exists for that table.
- Modified Query Editor autocomplete to incrementally add to the list instead of
  waiting to populate the complete list.
- Modified Query Editor autocomplete to retain derived suggestions list with a 
  new action from te toolbar to force a refresh.
- Added main connection and driver details panel to scrollpane to avoid 
  field/button collapsing on view resize. 
- Added object count value to all object lists on the Database Browser view.
- Added summary file info to all export data workers.
- Added TRACE log level.
- Modified driver panel class field to use a drop-down box with available 
  drivers in addition to the 'Find' button.
- Misc UI tweaks for for Mac OS look and feel. 
- Modified to display column names as returned - previously defaulted to upper.
- Fixed applying a column NULL change being executed as NOT NULL from the 
  Database Browser Table view.  
- Modified datbase object node loading to check for a single item only and load
  contents on selection from the Database Browser.
- Performance improvements on result set table model. 
- Modified to allow for custom settings directory supplied as an argument on 
  startup using: executequery.user.home.dir=[some_path]
- Added key event modifiers check for INS key in the Query Editor.

 

Summary of changes - v3.6 Build 5213:
----------------------------------------

- Added connection folders.
- Fixed issue wth References Panel showing current table multiple times for 
  composite keys.
- Added Collapse All feature for connection nodes in the Browser Tree.
- Added show/hide tools panel for the Query Editor.
- Added alternating row colour for results table.
- Fixed possible hang on connect when open panels are being updated.
 


Summary of changes - v3.5.1 Build 5196:
----------------------------------------

- Upgraded to Liquibase v2.0.5.
- Added meta data and SQL tabs to database object views in the browser.
- Added SQL text for database views in the browser tab.
- Added improved MAC meta key support for selected command shortcuts.
- Added data row count field for database object views in the browser. 


Summary of changes - v3.5.0 Build 5190:
----------------------------------------

- Fixed issue where saving text and selecting 'All Files' would throw an 
  exception on save. 
- Added support for SELECT...INTO... queries for the Query Editor. 
- Added separate icon for triggers in the database browser. 
- Fixed issue where new or deleted connections were not being updated in the 
  connections table list.
- Added double-click to size column to fit largest value in query editor.
- Changed errors in data tab or any object to display on the panel and not use
  the exception error dialog.
- Modified detection and execution of create function or procedure statement 
  from the Query Editor to strip out any comments.
- Added 'Copy Name' popup menu item for all items in the browser tree.
- Application icons updated.
- Fixed procedure execution from editor reporting invalid procedure when params
  have were not loaded by EQ.
- Added 'Execute as Single Statement' editor action shortcut key.
- Added database table views to the Query Editor autocomplete list. 


Summary of changes - v3.3.0 Build 5181:
----------------------------------------

- Added method to scan and remove old user settings directories from 
  <user.home>/.executequery
- Modified dialog sizes for ERD panels.
- UI tweaks and new icon set from http://www.famfamfam.com/lab/icons/silk/
- Modified node tool tips to be more descriptive, table columns in particular.
- Added type, size and nullability to code-completion column values.


Summary of changes - v3.2.5 Build 5176:
----------------------------------------

- Fixed transaction issue noted on data tab when connection auto commit mode is
  reset before the result set is closed (noted for SolidDB).
- Fixed lurking transaction isolation issue within the browser tabs holding open
  locks and preventing query execution elsewhere.
- Modified GUI to use the default screen for positioning where multiple screens
  exist.
- Changed system properties tables to use sortable models.
- Selective layout changes for row heights in tables and trees.
- Modified entity labels to be text-selectable.
- Added delete button for JDBC advanced property key/value pairs.
- Added limited drag support to the saved connections tree.
- Fixed ERD add existing schema table where under some DBs (Oracle) the column 
  names would be repeated for like tables across different schemas.
- Added ability to change connection names directly from editing the tree node.


Summary of changes - v3.2.4 Build 5169:
----------------------------------------

- Moved build to Java 1.6.
- Added right-alignment for numeric values option for result set table views.
- Fixed unrecoverable location exception on Query Editor when attempting to
  define a table alias for auto-completion.
- Added multiple connection connect and disconnect.
- Added additional schema objects shown for Query Editor auto-complete popup.
- Modified procedure execution from the Query Editor to execute 'as-is' when the
  provided procedure can not be located using the driver meta data.
- Added anti-alias font option.
- Modified to save connections after a sort.


Summary of changes - v3.2.3 Build 5164:
----------------------------------------

- Fixed occassional error (NullPointerException) when opening a script from file
  and closing the editor without doing anything else.
- Modified host checking for application update to not throw a 'loud' exception.
- Added a warning dialog when attempting to close an editor window where a query
  is executing. The tab will now not close and suggest further action.
- Added a checkbox to enable/disable the max rows for a result set in the Query
  Editor.
- Fixed XLS export from result set panel in editor to display correctly read
  CLOB values.
- Fixed feedback dialog issue when cancel button selected on the progress dialog
  and the dialog and text fields remain blocked.
- Added output console popup menu for text selection, copy, clear, reset and
  save to file.
- Added FrontBase, jTDS, MckoiDDB and ThinkSQL to database name list.
- Added new text editor functions including duplicate rows/selections up/down,
  move row/selection up/down etc.
- Fixed different case identifier detection and use for executed queries (ie.
  row counts in the database browser that would throw an error if not quoted
  when required).
- Added customisable coloured results table cells for data types.
- Split out autocomplete option into keywords and database objects - added these
  to the editor popup menu.
- Updated libraries Log4j, Liquibase, Commons Lang and Commons Codec.


Summary of changes - v3.2.2 Build 5148:
----------------------------------------

- Fixed table editing in browser when deleting a column and restoring/cancelling
  the action not restoring that column's state.
- Fixed errors in adding and removing column constraints from the table browser.
- Added simple text component popup menu with cut/copy/paste for all panel text
  fields.
- Fixed export as XML error throwing ClassCastException for DataSource
- Fixed exception thrown for DB2 tables when selecting from browser tree view.
- Added proxy user and password to proxy preferences.
- Added 'show password' action on login panel to reveal password in plain text.
- Improved DESC <table_name> statement support.
- Fixed JDBC advanced properties not being applied when entered.
- Improved code completion - now recognises table and columns by name and alias
  in queries.
- Added ability to output generated create/drop statement to the Query Editor
  and not just to file, file selection and output is no longer required for
  writing create/drop scripts.
- Added SQL shortcuts to the Quey Editor allowing user defined SQL text
  abbreviations and autocorrect.


Summary of changes - v3.2.1 Build 5146:
----------------------------------------

- Minor help doc update.
- Help menu changes.
- Added deb package for download.
- Modified generated queries to use escaped column names where appropriate (ie.
  in the Query Editor, import/export processes etc).
- Fixed editor auto-complete popup for less than 2 characters not scrolling to
  'starts with' word.
- Ongoing look and feel tweaks - field dimension changes, font changes.
- Added option to execute queries with comments, whitespace, line-breaks etc
  as-is. Defaults to executing 'sanitised' queries with comments etc stripped
  out as has always been the case.
- Added default insets for UIManager returning null for some look and feels
  using TabbedPane.tabInsets in determining tab dimensions for scrolling center
  panel.
- Modified callable statement generation removing left-hand equals where out
  parameters do not exist.
- Fixed issue with data exports for multiple tables where back/next button
  selection from export file entry would duplicate selected tables in returning
  to file selection panel.
- Fixed query execution with trailing semi-colon errors noted with Oracle.
- Added export result set to file allowing for executing arbitrary SQL SELECT
  statements and writing the output to a delimited file.
- Added open and execute SQL script feature allowing for SQL script execution
  without loading the script in the editor first.


Summary of changes - v3.2.0 Build 5140:
----------------------------------------

- Fixed result set exporter printing 'null' string for null values.
- Fixed tab character in SQL statement causing errors in editor execution.
- Modified row count in browser threading.
- Modified Excel data export cell style creation to reuse data cell style across
  all exported values.
- Added make donation help menu item :)
- Fixed execution of 'unknown' statement types that would return a valid result
  set that would be closed before access in the Query Editor.
- Modified autocomplete popup to use schema lookups when at least 2 characters
  have been entered.
- Added docked notepad.
- Modified autocomplete to insert text after any non-letter-or-digit characters.
- Modified look and feel - default font size changes, button sizes.
- Fixed dialog popups off open dialogs being hidden behind current modal views.


Summary of changes - v3.1.6 Build 5132:
----------------------------------------

- Improved support for table and column names with spaces.
- Fixed "Save As" not storing the last save path between application sessions.
- Fixed row count hang on database table browser tab.
- Fixed statement generator from table node not selecting correct connection in
  the Query Editor.
- Added line separator property option (Unix, Windows, Mac) for writing text
  to file.
- Artwork update stage 1.


Summary of changes - v3.1.5 Build 5124:
----------------------------------------

- Updated dependancy libraries.
- Last used file path now persistent between application sessions.
- Added option to store editor result set table column widths when resized.
- Fixed stored procedure and function errors on execution from editor and stored
  object execution tab.
- Added option to use a single result set tab within the editor and not open
  multiple for each returned result set.
- Fixed advanced connection properties not applied when connection created.
- Added ability to define a custom delimiter for import/export data.


Summary of changes - v3.1.4 Build 5121:
----------------------------------------

- Fixed query execution where insert/update char values have line breaks.
- Added BLOB and CLOB support for the Query Editor and Schema Browser.
- Added record item viewer for result set cells.
- Added code completion for the Query Editor - stage 1 supporting default
  connected schema only (this is a very early release of this feature with much
  yet to be done - if issues present, disable from Preferences | Editor)
- Changed export as SQL to write 'default' value types as quoted values.
- Added transpose row feature for editor result sets.
- Fixed table data errors for non-connected default schemas/catalogs.
- Minor UI tweaks.


Summary of changes - v3.1.3 Build 5117:
----------------------------------------

- Fixed proxy configuration for external http calls including update checks
- Modified ERD to reset x,y-0,0 position when a table is moved off screen
- Added SQL text pane to query history dialog
- Added connection and schema tree panel node sorting (a-z, z-a and restore)
- Added schema tree panel node search lookup and selection
- Fixed create DROP scripts where constraints were being added as ALTER TABLE
- Fixed SQL statement export to account for column selections
- Fixed editor connection errors when the single and selected connection is
  closed and reopened and used by the same editor
- Fixed export as SQL where CREATE TABLE was not being included when selected
- Added window nav menu and shortcuts


Summary of changes - v3.1.2 Build 5111:
----------------------------------------

- Added [close/close others/close all] popup menu on editor's result set tabs.
- Fixed result set tab rollover label error with set indexes.
- Fixed reported leak when executing 'unknown' statement types - resources were
  not being correctly handled and cleaned up after use.
- Fixed recycle connection on host node error on selection.
- Fixed menu item shortcut key display errors.
- Fixed SaveAs action not opening a new save dialog.
- Fixed possible statement execution error (result set closed) after query
  auto-format.
- Fixed quoted strings syntax highlighting errors
- Added export to/as SQL feature
- Modified SQL statement generation and ordering of constraints


Summary of changes - v3.1.1 Build 5107:
----------------------------------------

- Modified result set tab pane of the Query Editor to not close tabs when a
  non-result set generating statement is executed.
- Fixed incorrect handling of query bookmark deletion where the query deleted
  was instead attached to the previous bookmarks name value.
- Added new icons to identify primary and foreign key columns within the
  connections/schema browser tree.
- Added new submitted graphics work for logos, icons and splash.
- Added formatted error message for cell value when exception thrown on get
  value for result set table views.
- Fixed query history scrolling that would miss the very last query executed.
- Added format SQL button and icon to editor tool bar.
- Updated third-party lib dependencies.
- Improved Mac installation and configuration support.
- Improved support for non Metal look and feel extensions.


--------

Thanks to those who submitted bugs and suggestions. Keep them coming!


License:
--------
This is free software, and you are welcome to redistribute it under
certain conditions. See the GNU General Public License for details.

Deployment Environment:
-----------------------
At present the recommended Java 2 Runtime Environment (JRE) version is at least
1.6.0.

This program has been tested under JRE version 1.6.0 on the
following platforms:

   -- Windows versions 2000, XP and Vista, 7, 8, 
   -- Solaris8, Solaris9, Solaris10 and OpenSolaris
   -- Various flavours of Linux


Startup files:
--------------
The application may be started by using the executable file eq.exe (Windows
only) or the Unix shell scripts eq.sh.

Alternatively you may simply execute the Java archive eq.jar from the
installation directory using your installed Java 1.6 runtime environment.


Source Code:
------------
Source code is available for download directly from the download link at
executequery.org (where this program was downloaded). The source file is
typically named executequery-src-<version>.tar.gz.

Source code snapshots are also available from the Execute Query project
on sourceforge at http://sourceforge.net/projects/executequery. Source may be
checked out using svn as shown below:

svn co svn://svn.code.sf.net/p/executequery/code/trunk executequery

The source requires Java version 1.6 to build successfully.

If you are interested in a particular feature but can not locate it within the
code, please contact myself at the above address and you will be pointed in the
right direction.

Note: In line with the significant redesign of much of Execute Query, the
opportunity was taken to refactor much of the source code. Packages have been
reorganised and many classes renamed, merged or removed. This process is ongoing
and will continue across the next few versions.


Web Site:
---------
Please visit executequery.org for updates and information on upcoming
releases including improved documentation. Use the relevant form to report
any bugs or send an email directly to Takis Diakoumis at the address below.
Please consult the page Known Bugs of the help documentation before sending
any bug report. Requests for enhancements/improvements can also be made
via the site or email.

Thank you for trying Execute Query.

Takis Diakoumis
takisd@executequery.org
27 April 2016
