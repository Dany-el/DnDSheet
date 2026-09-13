import sys
import re
import unittest
import json
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[2] / 'main/python'))
from character_sheet_generator import render_character_sheet, render_character_sheet_json
from localization import merge_catalogs, normalize_language_code, validate_catalogs, validate_translation_files


class CharacterSheetTest(unittest.TestCase):
    def test_android_json_bridge_matches_native_render_for_each_language(self):
        for language in ('en', 'ru', 'uk'):
            data = self.data()
            data['character']['name'] = 'Аарон <&>'
            self.assertEqual(
                render_character_sheet(data, language),
                render_character_sheet_json(json.dumps(data), language),
            )

    def data(self):
        return dict(character=dict(name='Example', charClass='Bard', race='Human', level=9,
                    hitDice='d8', currentHp=66, maxHp=66, tempHp=0, totalAc=14,
                    initiative='+2', speed=30, profBonus=4, traits='Feature text',
                    feats='Feat text', inventory='Rope & torch', backstory='Story text',
                    notes='Notes text', proficiencies='Common', coins=dict(gold=2,silver=0,copper=1)),
                    abilities=[], saving_throws=[], skills=[], attacks=[], spells='', spell_details=[])

    def test_language_codes_are_normalized_and_unsupported_codes_fall_back_to_english(self):
        cases = {
            None: 'en', '': 'en', '  UK_ua  ': 'uk', 'ru-RU': 'ru',
            'en-US': 'en', 'fr-FR': 'en'
        }
        for value, expected in cases.items():
            with self.subTest(value=value):
                self.assertEqual(expected, normalize_language_code(value))

    def test_translation_files_have_matching_keys_and_placeholders(self):
        validate_translation_files()

    def test_placeholder_mismatch_is_rejected(self):
        catalogs = {
            'en': {'message': 'Portrait of {name}'},
            'uk': {'message': 'Портрет {character}'},
        }
        with self.assertRaisesRegex(ValueError, 'placeholder'):
            validate_catalogs(catalogs)

    def test_missing_nested_translation_falls_back_to_english(self):
        merged = merge_catalogs(
            {'section': {'title': 'English title', 'label': 'English label'}},
            {'section': {'title': 'Назва'}},
        )
        self.assertEqual('Назва', merged['section']['title'])
        self.assertEqual('English label', merged['section']['label'])

    def test_ukrainian_render_translates_layout_and_enum_values_only(self):
        data = self.data()
        data['character']['name'] = 'Example <Hero>'
        data['abilities'] = [dict(id='DEX', score=14, modifier='+2')]
        data['saving_throws'] = [dict(id='DEX', modifier='+2', proficient=False)]
        data['skills'] = [dict(id='SLEIGHT_OF_HAND', ability='DEX', modifier='+2',
                               proficient=False, proficiency='NONE')]
        data['attacks'] = [dict(name='Rapier', bonus='+4', damage='1d8', damageType='PIERCING')]
        data['portrait_data_uri'] = 'data:image/png;base64,AA=='

        html = render_character_sheet(data, 'uk-UA')

        self.assertIn('<html lang="uk">', html)
        self.assertIn('Аркуш персонажа', html)
        self.assertIn('Спритність рук', html)
        self.assertIn('Колючий', html)
        self.assertIn('Rapier', html)
        self.assertIn('Example &lt;Hero&gt;', html)
        self.assertIn('alt="Портрет Example &lt;Hero&gt;"', html)

    def test_renderer_owned_localization_context_cannot_be_overridden_by_payload(self):
        data = self.data()
        data['language_code'] = 'malicious'
        data['t'] = lambda _key: 'malicious'
        html = render_character_sheet(data, 'ru')
        self.assertIn('<html lang="ru">', html)
        self.assertIn('Лист персонажа', html)
        self.assertNotIn('<html lang="malicious">', html)

    def test_concurrent_renders_do_not_leak_languages(self):
        with ThreadPoolExecutor(max_workers=3) as executor:
            rendered = list(executor.map(
                lambda code: render_character_sheet(self.data(), code),
                ['en', 'uk', 'ru'] * 4,
            ))
        for code, html in zip(['en', 'uk', 'ru'] * 4, rendered):
            self.assertIn(f'<html lang="{code}">', html)

    def test_inventory_and_additional_pages(self):
        html = render_character_sheet(self.data())
        self.assertIn('Rope &amp; torch', html)
        self.assertLess(html.index('Rope &amp; torch'), html.index('data-page="features"'))
        for value in ['Feat text', 'Story text', 'Notes text', 'Common']:
            self.assertIn(value, html)

    def test_user_line_breaks_and_leading_spaces_are_preserved(self):
        data = self.data()
        data['character']['traits'] = 'First line\n\n  Indented line'
        html = render_character_sheet(data)
        self.assertIn('First line<br><br>  Indented line', html)
        self.assertIn('white-space: break-spaces', html)

    def test_expertise_and_half_have_distinct_accessible_markers(self):
        data = self.data()
        data['skills'] = [dict(id='STEALTH', ability='DEX', modifier='+8', proficient=True,
                               proficiency=level) for level in ['NONE','HALF','PROFICIENT','EXPERT']]
        html = render_character_sheet(data)
        rows = re.findall(r'<div class="skill-row">(.*?)</div>', html, re.S)
        for row, label in zip(rows, ['Not proficient', 'Half proficiency', 'Proficient', 'Expertise']):
            self.assertIn('aria-label="' + label + '"', row)
        self.assertEqual(len(rows), 4)

    def test_spell_pages_are_conditional_and_escape_descriptions(self):
        data = self.data()
        self.assertNotIn('data-page="spell-descriptions"', render_character_sheet(data))
        data['spell_details'] = [dict(name='Test spell', level=0, school='ILLUSION',
            castTime='ACTION', rangeType='SELF', duration='ONE_MINUTE', components=['VERBAL'],
            isRitual=True, isConcentration=True, description='<b>Plain text</b>\nSecond line',
            higherLevels='Higher level text', material='A feather')]
        html = render_character_sheet(data)
        self.assertIn('data-page="spell-descriptions"', html)
        self.assertIn('&lt;b&gt;Plain text&lt;/b&gt;<br>Second line', html)
        self.assertIn('Higher level text', html)
        self.assertIn('A feather', html)
        self.assertIn('Concentration', html)

    def test_empty_details_do_not_add_empty_pages(self):
        data = self.data()
        for key in ['traits','feats','backstory','notes','proficiencies']:
            data['character'][key] = ''
        html = render_character_sheet(data)
        self.assertNotIn('data-page="features"', html)
        self.assertNotIn('<article class="page" data-page="story"', html)
        self.assertNotIn('<article class="page" data-page="notes"', html)
        for tag in ['<input', '<button', '<script', '<textarea', '<form']:
            self.assertNotIn(tag, html)


if __name__ == '__main__':
    unittest.main()
