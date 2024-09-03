# Minimal makefile for Sphinx documentation
#

# You can set these variables from the command line, and also
# from the environment for the first two.
SPHINXOPTS    ?=
SPHINXBUILD   ?= $(VENV)/bin/sphinx-build
SOURCEDIR     = source
BUILDDIR      = build

PYTHON ?= python3
ROOT := $(shell pwd)
VENV ?= $(ROOT)/.venv
PIP ?= $(VENV)/bin/pip

.DEFAULT_GOAL := all


# Put it first so that "make" without argument is like "make help".
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

.PHONY: help Makefile clean distclean bootstrap

clean:
	- rm -rf $(BUILDDIR)

distclean: clean
	- rm -r $(VENV)

# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile
	@$(SPHINXBUILD) -M $@ "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

bootstrap:
	$(PYTHON) -m venv .venv
	$(PIP) install -r requirements.txt
