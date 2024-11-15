# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

latex_use_latex_multicolumn = True

project = 'Red_Expert'
copyright = '2024, Red Soft'
author = 'Red Soft'

# General configuration

import re
import os
import sys
sys.path.append(os.path.abspath("./_ext"))
import globalvar


# функция подменяет один список слов на другой в rst файлах
# эта функция вспомогательная, она работает, но выполнить ее достаточно один раз для замены необходимых слов
'''
def replace_words_in_files(words_to_replace, new_words):
    rst_files = [file for file in os.listdir() if file.endswith(".rst")]

    for file_name in rst_files:
        with open(file_name, 'r') as file:
            file_content = file.read()

        # Заменяем слова из списка words_to_replace на соответствующие слова из списка new_words
        for i in range(len(words_to_replace)):
            file_content = file_content.replace(words_to_replace[i], new_words[i])

        with open(file_name, 'w') as file:
            file.write(file_content)


# Список слов для замены
words_to_replace = ["security_version", "rdb_version"]
new_words = ["security5.fdb", "5.0"]

# Вызываем функцию для замены слов
replace_words_in_files(words_to_replace, new_words)
'''


# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

extensions = ['NewCodeBlockDirective', 'HideShowIfCertDirective', 'HideShowIfCertRole', 'UnindentedListDirective']

#f = open('defs.tex.txt', 'r+');
#PREAMBLE = f.read();

smartquotes = False

templates_path = ['_templates']
exclude_patterns = []

language = 'ru'

latex_additional_files = ["defs.sty"]

latex_engine = 'pdflatex'

latex_toplevel_sectioning = 'section'

latex_documents = [
 ('index', 'Red_Expert.tex', u'Ред Эксперт', u'YourName', 'article'),
]

latex_elements = {
'passoptionstopackages' : r'''
    \PassOptionsToPackage{pdftex}{graphicx}
    \PassOptionsToPackage{numbered}{bookmark}
    \PassOptionsToPackage{tikz}{bclogo}
    ''',
'fontenc' : r'''
    \usepackage[T2A]{fontenc}
    ''',
'fontsubstitution' : r'',
'inputenc' : r'\usepackage[utf8]{inputenc}',
'preamble': r"""
\usepackage{defs}
""",
'hyperref' : r'''
\usepackage[colorlinks=true,linkcolor=blue]{hyperref}
''',
'maketitle': r"""
\nonstopmode

\thispagestyle{empty}
\begin{titlepage}
\renewcommand{\maketitle}{ O{\ } O{\ } m }{
\fancyhf{}
\thispagestyle{empty}

\topskip0pt
\vspace*{\fill}

\begin{flushright}
\Huge {\xhrulefill{red}{2mm}\color{red} Ред} Эксперт\\
\LARGE Версия 2024.11\\
\huge Руководство пользователя\\

\end{flushright}

\vspace*{\fill}}
\end{titlepage}
""",
'tableofcontents' : r"""
\addtocounter{page}{1}

\definecolor{MidnightBlue}{RGB}{25, 25, 112}

\titleformat{\section}[display]
{\filcenter\Huge\bfseries\color{MidnightBlue}}
{\raggedright\normalfont\Large Глава \thesection}{3pt}{}

\titleformat{\subsection}
{\filright\LARGE\bfseries\color{MidnightBlue}}
{\thesubsection}{10pt}{}

\titleformat{\subsubsection}
{\filright\Large\bfseries\color{MidnightBlue}}
{\thesubsubsection}{10pt}{}

\titleformat{\paragraph}
{\filright\large\bfseries\color{MidnightBlue}}
{\theparagraph}{1em}{}

\renewcommand{\thetable}{\thesection.\arabic{table}}
\renewcommand{\thefigure}{\thesection.\arabic{figure}}

\makeatletter
\fancypagestyle{normal}{
\pagestyle{fancy}
\fancyhf{}
\fancyhead[R]{Руководство пользователя\\\rightmark}
\fancyfoot[C]{\xhrulefill{red}{2mm} Стр. \thepage}
\renewcommand{\headrulewidth}{0.5pt}
}
\makeatother

\setcounter{tocdepth}{10}
\setlength{\headheight}{24pt}
\renewcommand\contentsname{Содержание}
\tableofcontents
""",
'figure_align': 'H',
}


latex_table_style = []

numfig = True  #  чтобы :numref: не игнорировался
highlight_language = 'none'  # подсветка синтаксиса в код-блоках по умолчанию выключена


#latex_show_urls = 'footnote'
# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = 'alabaster'
html_static_path = ['_static']
html_css_files = [
    'css/codeblock_style.css',
]

# Параметр для директивы и роли hideifcert (showifcert).
# Если версия документации сертифицированная (True), то текст внутри hideifcert (showifcert) скрывается (виден)
globalvar.IsCertifiedVersion = False


# Замены для ролей hideifcert и showifcert.
# Роли не умеют парсить моноширинный текст, но могут распарсить замены
rst_prolog = """
"""






