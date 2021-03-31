#!/usr/bin/python
# -*- coding: utf-8 -*-

from pygments import highlight
from pygments.formatters.html import HtmlFormatter
from pygments.lexers.python import PythonLexer


with open(source_code_filepath, 'rb') as source_code_file:
    source_code = source_code_file.read()
lexer = PythonLexer(encoding='utf-8')
html_formatter = HtmlFormatter(full='true', linenos=True, style=style, encoding="utf-8")
with open(html_file_path, 'wb') as html_file:
    highlight(source_code, lexer, html_formatter, html_file)
