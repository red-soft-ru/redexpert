from docutils import nodes
from docutils.parsers.rst.states import Struct
import globalvar

def Hide_ifcert_role(role, rawtext, text, lineno, inliner, options={}, content=[]):
    # Prepare context for nested parsing
    memo = Struct(document=inliner.document,
                  reporter=inliner.reporter,
                  language=inliner.language)
    parent = nodes.inline(rawtext, '', **options)

    if not globalvar.IsCertifiedVersion:
        # Parse role text for markup and add to parent
        processed, messages = inliner.parse(text, lineno, memo, parent)
        parent += processed
    else:
        parent = nodes.Text('')
        messages = []

    return [parent], messages

def Show_ifcert_role(role, rawtext, text, lineno, inliner, options={}, content=[]):
    # Prepare context for nested parsing
    memo = Struct(document=inliner.document,
                  reporter=inliner.reporter,
                  language=inliner.language)
    parent = nodes.inline(rawtext, '', **options)

    if globalvar.IsCertifiedVersion:
        # Parse role text for markup and add to parent
        processed, messages = inliner.parse(text, lineno, memo, parent)
        parent += processed
    else:
        parent = nodes.Text('')
        messages = []

    return [parent], messages


def setup(app):
    app.add_role('hideifcert', Hide_ifcert_role)
    app.add_role('showifcert', Show_ifcert_role)


