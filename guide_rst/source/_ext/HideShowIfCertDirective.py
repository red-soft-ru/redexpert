from __future__ import annotations

__docformat__ = 'reStructuredText'

import os.path

from docutils import statemachine
from docutils.parsers.rst import Directive, states

import globalvar

class BaseInclude(Directive):
    has_content = True
    required_arguments = 0
    optional_arguments = 0
    final_argument_whitespace = True
    option_spec = {}

    def run(self):
        tab_width = self.state.document.settings.tab_width
        rawtext = '\n'.join(self.content)
        include_lines = statemachine.string2lines(rawtext, tab_width, convert_whitespace=1)
        self.state_machine.insert_input(include_lines, '')
        return []

class HideIfCert(BaseInclude):
    def run(self):
        if not globalvar.IsCertifiedVersion:
            return super().run()
        else:
            return []

class ShowIfCert(BaseInclude):
    def run(self):
        if globalvar.IsCertifiedVersion:
            return super().run()
        else:
            return []

def setup(app):
    app.add_directive('hideifcert', HideIfCert)
    app.add_directive('showifcert', ShowIfCert)
