from __future__ import annotations

from docutils import nodes
from sphinx.locale import __
from sphinx.util import logging
from sphinx.util.docutils import SphinxDirective
from docutils.parsers.rst import Directive
from docutils.parsers.rst.directives.admonitions import Note

logger = logging.getLogger(__name__)


class UnindentedList(SphinxDirective):
    has_content = True
    required_arguments = 0
    optional_arguments = 0
    final_argument_whitespace = False
    option_spec: OptionSpec = {}

    def run(self) -> list[Node]:
        node = nodes.paragraph()
        node.document = self.state.document
        self.state.nested_parse(self.content, self.content_offset, node)
        if len(node.children) != 1 or not isinstance(node.children[0],
                                                     nodes.bullet_list):
            if not isinstance(node.children[0], nodes.enumerated_list):
                logger.warning(__('.. list content is not a list'),
                               location=(self.env.docname, self.lineno))
                return []
        bullet_list = node.children[0]
        prefix = '\\begin{listenv}'
        suffix = '\end{listenv}'
        latex_prefix = nodes.raw('', prefix, format='latex')
        latex_suffix = nodes.raw('', suffix, format='latex')
        return  [latex_prefix, bullet_list, latex_suffix]


def setup(app):
    app.add_directive('unindened_list', UnindentedList)
