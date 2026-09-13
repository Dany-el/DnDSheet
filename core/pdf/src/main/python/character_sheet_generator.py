from jinja2 import Environment, FileSystemLoader
import os
import json

from localization import create_translator


def render_character_sheet_json(data_json: str, language_code: str = "en") -> str:
    """Android bridge: decode JSON into native Python containers before rendering."""
    return render_character_sheet(json.loads(data_json), language_code)


def render_character_sheet(data: dict, language_code: str = "en") -> str:
    """
    Renders the character sheet HTML from a data dict.
    Called from Kotlin via Chaquopy.
    """
    template_dir = os.path.join(os.path.dirname(__file__), "template")
    env = Environment(loader=FileSystemLoader(template_dir), autoescape=True)
    template = env.get_template("character_sheet.html")
    resolved_language_code, translator = create_translator(language_code)
    context = dict(data)
    context.update(t=translator, language_code=resolved_language_code)
    return template.render(**context)
