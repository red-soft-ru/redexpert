from __future__ import annotations

import sys

from docutils import nodes
from docutils.parsers.rst import directives


from sphinx import addnodes
from sphinx.directives import optional_int
from sphinx.locale import __
from sphinx.util import logging, parselinenos
from sphinx.util.docutils import SphinxDirective
import re
import globalvar


def remove_ifcert(content):
    result = []
    pattern = r':hideifcert:\`(.+?)\`'
    for line in content:
        if ":hideifcert:" not in line:
            result.append(line)
        else:
            if not globalvar.IsCertifiedVersion:    # если версия документации обычная
                line = re.sub(pattern, r'\1', line) # то вставляем все строки с :hideifcert:
                result.append(line)
            else:
                line = re.sub(pattern, r'', line)  # если версия документации сертифицированная, то пропускаем то, что внутри роли :hideifcert:
                if not re.match(r'^\s*$', line):
                    result.append(line)
    return result

class ReCodeBlock(SphinxDirective):
    has_content = True
    required_arguments = 0
    optional_arguments = 1
    final_argument_whitespace = False
    option_spec: OptionSpec = {
        'force': directives.flag,
        'linenos': directives.flag,
        'lineno-start': int,
        'emphasize-lines': directives.unchanged_required,
        'caption': directives.unchanged_required,
        'class': directives.class_option,
        'name': directives.unchanged,
    }

    def run(self) -> list[Node]:
        document = self.state.document
        self.content = remove_ifcert(self.content)
        code = '\n'.join(self.content)
        location = self.state_machine.get_source_and_line(self.lineno)

        linespec = self.options.get('emphasize-lines')
        if linespec:
            try:
                nlines = len(self.content)
                hl_lines = parselinenos(linespec, nlines)
                if any(i >= nlines for i in hl_lines):
                    logger.warning(__('line number spec is out of range(1-%d): %r') %
                                   (nlines, self.options['emphasize-lines']),
                                   location=location)

                hl_lines = [x + 1 for x in hl_lines if x < nlines]
            except ValueError as err:
                return [document.reporter.warning(err, line=self.lineno)]
        else:
            hl_lines = None

        literal: Element = nodes.literal_block(code, code)
        if 'linenos' in self.options or 'lineno-start' in self.options:
            literal['linenos'] = True
        literal['classes'] += self.options.get('class', [])
        literal['force'] = 'force' in self.options
        if self.arguments:
            literal['language'] = self.env.temp_data.get('highlight_language',
                                                         self.config.highlight_language)
            prefix = '\\redexamplestyle'
            arg = self.arguments[0]
            if arg.lower()=='redstatement':
                prefix = '\\redstatementstyle'
            elif arg.lower()=='redlisting':
                capt = self.options.get('caption', '')
                prefix = '\\redlistingtitlestyle{'+ capt +'} ' +' \\vspace{-27pt} '
            elif arg.lower()=='redbordless':
                prefix = '\\redbordlessstyle'
            elif arg.lower()=='sql':
                literal['language'] = arg
        else:
            prefix = '\\redexamplestyle'
            literal['language'] = self.env.temp_data.get('highlight_language',
                                                         self.config.highlight_language)

        extra_args = literal['highlight_args'] = {}
        if hl_lines is not None:
            extra_args['hl_lines'] = hl_lines
        if 'lineno-start' in self.options:
            extra_args['linenostart'] = self.options['lineno-start']
        self.set_source_info(literal)
        self.add_name(literal)

        latex_prefix = nodes.raw('', prefix, format='latex')
        return [latex_prefix, literal]

def setup(app):
    app.add_directive('code-block', ReCodeBlock)

