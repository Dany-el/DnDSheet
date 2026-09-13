"""Localization support for the static character-sheet renderer."""

from __future__ import annotations

import json
from functools import lru_cache
from pathlib import Path
from string import Formatter
from types import MappingProxyType
from typing import Any, Mapping


SUPPORTED_LANGUAGES = frozenset({"en", "uk", "ru"})
TRANSLATIONS_DIR = Path(__file__).resolve().parent / "translations"


def normalize_language_code(language_code: str | None) -> str:
    if not language_code:
        return "en"
    base_code = str(language_code).strip().lower().replace("_", "-").split("-", 1)[0]
    return base_code if base_code in SUPPORTED_LANGUAGES else "en"


def _freeze(value: Any) -> Any:
    if isinstance(value, dict):
        return MappingProxyType({key: _freeze(child) for key, child in value.items()})
    return value


@lru_cache(maxsize=len(SUPPORTED_LANGUAGES))
def _load_catalog(language_code: str) -> Mapping[str, Any]:
    path = TRANSLATIONS_DIR / f"{language_code}.json"
    with path.open("r", encoding="utf-8") as stream:
        catalog = json.load(stream)
    if not isinstance(catalog, dict):
        raise ValueError(f"Translation catalog must be an object: {path}")
    return _freeze(catalog)


def merge_catalogs(baseline: Mapping[str, Any], overlay: Mapping[str, Any]) -> dict[str, Any]:
    merged: dict[str, Any] = {}
    for key, baseline_value in baseline.items():
        overlay_value = overlay.get(key)
        if isinstance(baseline_value, Mapping):
            overlay_mapping = overlay_value if isinstance(overlay_value, Mapping) else {}
            merged[key] = merge_catalogs(baseline_value, overlay_mapping)
        else:
            merged[key] = overlay_value if isinstance(overlay_value, str) else baseline_value
    return merged


def _flatten(catalog: Mapping[str, Any], prefix: str = "") -> dict[str, str]:
    flattened: dict[str, str] = {}
    for key, value in catalog.items():
        path = f"{prefix}.{key}" if prefix else key
        if isinstance(value, Mapping):
            flattened.update(_flatten(value, path))
        elif isinstance(value, str) and value:
            flattened[path] = value
        else:
            raise ValueError(f"Translation value at '{path}' must be a non-empty string")
    return flattened


def _placeholders(value: str) -> set[str]:
    return {name for _, name, _, _ in Formatter().parse(value) if name is not None}


def validate_catalogs(catalogs: Mapping[str, Mapping[str, Any]]) -> None:
    if "en" not in catalogs:
        raise ValueError("English translation catalog is required")
    baseline = _flatten(catalogs["en"])
    for language_code, catalog in catalogs.items():
        flattened = _flatten(catalog)
        missing = sorted(baseline.keys() - flattened.keys())
        extra = sorted(flattened.keys() - baseline.keys())
        if missing or extra:
            raise ValueError(
                f"Translation key mismatch for '{language_code}': missing={missing}, extra={extra}"
            )
        for key, baseline_value in baseline.items():
            if _placeholders(flattened[key]) != _placeholders(baseline_value):
                raise ValueError(f"Translation placeholder mismatch for '{language_code}.{key}'")


def validate_translation_files() -> None:
    validate_catalogs({code: _load_catalog(code) for code in sorted(SUPPORTED_LANGUAGES)})


class Translator:
    def __init__(self, catalog: Mapping[str, Any]):
        self._catalog = catalog

    def __call__(self, key: str, **values: Any) -> str:
        current: Any = self._catalog
        for part in key.split("."):
            if not isinstance(current, Mapping) or part not in current:
                raise KeyError(f"Missing translation key: {key}")
            current = current[part]
        if not isinstance(current, str):
            raise KeyError(f"Translation key does not refer to a string: {key}")
        return current.format(**values)


def create_translator(language_code: str | None) -> tuple[str, Translator]:
    resolved_code = normalize_language_code(language_code)
    english = _load_catalog("en")
    selected = _load_catalog(resolved_code)
    return resolved_code, Translator(_freeze(merge_catalogs(english, selected)))
