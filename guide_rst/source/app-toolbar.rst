.. raw:: latex

  \appendix

  \titleformat{\section}
  {\huge\color{red}}
  {\thesection}{1em}{}

  \titleformat{\subsection}
  {\LARGE\color{red}}
  {\thesubsection}{1em}{}

  \renewcommand{\thetable}{\Asbuk{application}.\arabic{table}} % нумерация таблиц с номером section

  \renewcommand{\thefigure}{\Asbuk{application}.\arabic{figure}}

  \renewcommand{\thesubsection}{\Asbuk{application}.\arabic{subsection}} % нумерация подразделов типа F.1

  \renewcommand{\theredlisting}{\Asbuk{application}.\arabic{redlisting}}

  \appcount

.. _toolbar:

Панель инструментов
==========================

Далее следует описание каждой панели инструментов и связанных с ней кнопок и действий.

.. tabularcolumns:: |>{\center}\X{3}{17}|>{\ttfamily\arraybackslash}\X{9}{17}|>{\ttfamily\arraybackslash}\X{5}{17}|
.. list-table:: Панель инструментов
   :class: longtable
   :header-rows: 1

   * - 
     - Описание
     - Горячие клавиши
   * - 
       .. image:: img/icon_connection.png
          :scale: 15%
     - Подключение к выбранной базе данных.
     -  
   * - 
       .. image:: img/icon_connection_all.png
          :scale: 15%
     - Подключение ко всем базам данных, которые добавлены в дерево подключений.
     - 
   * - 
       .. image:: img/icon_refresh.png
          :scale: 15%
     - Обновление всех объектов в выбранном подключении.    
     - 
   * - 
       .. image:: img/icon_zoom.png
          :scale: 15%
     - Поиск объекта в дереве в установленном соединении.
     - Ctrl + F
   * - 
       .. image:: img/icon_connection_new.png
          :scale: 15%
     - Создание нового подключения.
     - Ctrl + Shift + N
   * - 
       .. image:: img/icon_create_db.png
          :scale: 15%
     - Создание базы данных.
     - 
   * - 
       .. image:: img/icon_execute_script.png
          :scale: 15%
     - Выполнение SQL-скрипта из файла.
     - 
   * - 
       .. image:: img/icon_compare_db.png
          :scale: 15%
     - Открыть инструмент сравнения метаданных баз данных.
     - 
   * - 
       .. image:: img/icon_create_script.png
          :scale: 15%
     - Открыть инструмент извлечения метаданных в скрипт.
     - 
   * - 
       .. image:: img/icon_query_editor.png
          :scale: 15%
     - Открыть редактор запросов.
     - 
   * - 
       .. image:: img/icon_erd_panel.png
          :scale: 15%
     - Открыть редактор ER-диаграмм.
     - 
   * - 
       .. image:: img/icon_db_statistic.png
          :scale: 15%
     - Открыть инструмент сбора статистики по базе данных.
     - 
   * - 
       .. image:: img/icon_manager_trace.png
          :scale: 15%
     - Открыть трейс менеджер.
     - 
   * - 
       .. image:: img/icon_manager_user.png
          :scale: 15%
     - Открыть менеджер пользователей.
     - 
   * - 
       .. image:: img/icon_manager_grant.png
          :scale: 15%
     - Открыть менеджер привилегий.
     - 
   * - 
       .. image:: img/icon_execute_profiler.png
          :scale: 15%
     - Открыть профайлер.
     - 
   * - 
       .. image:: img/icon_table_validation.png
          :scale: 15%
     - Открыть инструмент валидации таблицы.
     - 
   * - 
       .. image:: img/icon_import_file.png
          :scale: 15%
     - Открыть инструмент импорта данных.
     - 
   * - 
       .. image:: img/icon_generator.png
          :scale: 15%
     - Открыть генератор тестовых данных.
     - 
   * - 
       .. image:: img/icon_console_system.png
          :scale: 15%
     - Открыть системную консоль.
     - 
   * - 
       .. image:: img/icon_application_log.png
          :scale: 15%
     - Просмотр системного журнала.
     - 
   * - 
       .. image:: img/icon_preferences.png
          :scale: 15%
     - Открыть настройки приложения.
     - 
   * - 
       .. image:: img/icon_help.png
          :scale: 15%
     - Открыть документацию.
     - 

Панель инструментов редактора запросов
----------------------------------------------

.. tabularcolumns:: |>{\center\arraybackslash}\X{3}{17}|>{\ttfamily\arraybackslash}\X{9}{17}|>{\ttfamily\arraybackslash}\X{5}{17}|
.. list-table:: Панель инструментов редактора запросов
   :class: longtable
   :header-rows: 1

   * - 
     - Описание
     - Горячие клавиши
   * - 
       .. image:: img/icon_execute.png
          :scale: 15%
     - Выполнить SQL-скрипт.
     - F9
   * - 
       .. image:: img/icon_execute_statement.png
          :scale: 15%
     - Выполнить скрипт одним запросом.
     - F5 
   * - 
       .. image:: img/icon_execute_profiler.png
          :scale: 15%
     - Выполнить SQL-скрипт в профайлере.
     - Shift + F5
   * - 
       .. image:: img/icon_execute_stop.png
          :scale: 15%
     - Остановить выполнение текущего запроса.
     - 
   * - 
       .. image:: img/icon_commit.png
          :scale: 15%
     - Зафиксировать транзакцию.
     - Ctrl + Shift + Q
   * - 
       .. image:: img/icon_rollback.png
          :scale: 15%
     - Откатить транзакцию.
     - Ctrl + Shift + R
   * - 
       .. image:: img/icon_auto_commit.png
          :scale: 15%
     - Включить режим автоматической фиксации.
     - 
   * - 
       .. image:: img/icon_warning.png
          :scale: 15%
     - Останавливать выполнение SQL-скрипта при возникновении ошибки.
     - 
   * - 
       .. image:: img/icon_execute_to_file.png
          :scale: 15%
     - Экспортировать результат запроса в файл.
     - 
   * - 
       .. image:: img/icon_limit_row_count.png
          :scale: 15%
     - Ограничить количество вводимых строк.
     - 
   * - 
       .. image:: img/icon_bookmarks.png
          :scale: 15%
     - Управление закладками запросов.
     - Ctrl + B
   * - 
       .. image:: img/icon_history.png
          :scale: 15%
     - Открыть историю выполнения запросов.
     - Ctrl + Shift + H
   * - 
       .. image:: img/icon_move_previous.png
          :scale: 15%
     - Ввести в редакторе предыдущий выполненный запрос.
     - Ctrl + Shift + Down
   * - 
       .. image:: img/icon_move_next.png
          :scale: 15%
     - Ввести в редакторе следующий выполненный запрос.
     - Ctrl + Shift + Up
   * - 
       .. image:: img/icon_export_file.png
          :scale: 15%
     - Экспортировать выделенный набор данных в файл.
     - 
   * - 
       .. image:: img/icon_rs_metadata.png
          :scale: 15%
     - Показать метаданные текущего набора результатов.
     - 
   * - 
       .. image:: img/icon_filter.png
          :scale: 15%
     - Добавить фильтры для текущего набора результатов.
     - 
   * - 
       .. image:: img/icon_print_plan.png
          :scale: 15%
     - Показать план запроса.
     - Ctrl + Shift + P
   * - 
       .. image:: img/icon_toggle_transaction_parameters.png
          :scale: 15%
     - Показать настройки параметров транзакции.
     - 
   * - 
       .. image:: img/icon_toggle_editor_output.png
          :scale: 15%
     - Показать панель вывода результатов.
     - Ctrl + E
   * - 
       .. image:: img/icon_toggle_split.png
          :scale: 15%
     - Изменить ориентацию разделителя.
     - Ctrl + Alt + Q

Панель инструментов редактора ER-диаграмм
-----------------------------------------------

.. tabularcolumns:: |>{\center\arraybackslash}\X{3}{17}|>{\ttfamily\arraybackslash}\X{9}{17}|>{\arraybackslash}\X{5}{17}|
.. list-table:: Панель инструментов редактора ER-диаграмм
   :class: longtable
   :header-rows: 1

   * - 
     - Описание
     - Горячие клавиши
   * - 
       .. image:: img/icon_table_add.png
          :scale: 15%
     - Создать новую таблицу.
     - 
   * - 
       .. image:: img/icon_table_drop.png
          :scale: 15%
     - Удалить выбранный объект.
     - 
   * - 
       .. image:: img/icon_erd_relation_add.png
          :scale: 15%
     - Добавить связь.
     - 
   * - 
       .. image:: img/icon_erd_relation_delete.png
          :scale: 15%
     - Удалить связь между выделенными объектами.
     - 
   * - 
       .. image:: img/icon_create_script.png
          :scale: 15%
     - Сгенерировать скрипт для создания объектов диаграммы.
     - 
   * - 
       .. image:: img/icon_refresh_connection.png
          :scale: 15%
     - Построить ER-диаграмму существующей базы данных.
     - 
   * - 
       .. image:: img/icon_comment.png
          :scale: 15%
     - Добавить текстовый блок.
     - 
   * - 
       .. image:: img/icon_title.png
          :scale: 15%
     - Добавить заголовок диаграммы.
     - 
   * - 
       .. image:: img/icon_style_font.png
          :scale: 15%
     - Открыть настройки шрифта.
     - 
   * - 
       .. image:: img/icon_style_line.png
          :scale: 15%
     - Открыть настройки линий.
     - 
   * - 
       .. image:: img/icon_foreground.png
          :scale: 15%
     - Изменить цвет выбранного объекта.
     - 
   * - 
       .. image:: img/icon_background.png
          :scale: 15%
     - Изменить цвет фона диаграммы.
     - 
   * - 
       .. image:: img/icon_zoom_out.png
          :scale: 15%
     - Уменьшить масштаб.
     - 
   * - 
       .. image:: img/icon_zoom_in.png
          :scale: 15%
     - Увеличить масштаб.
     - 