import js from '@eslint/js';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import globals from 'globals';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  { ignores: ['dist', 'node_modules'] },
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      ...tseslint.configs.recommended,
      reactHooks.configs.flat['recommended-latest'],
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2023,
      globals: globals.browser,
    },
  },
  {
    // Module boundaries (docs/architecture/03-frontend-architecture.md):
    // a module may import from shared/ or another module's index.ts only.
    files: ['src/modules/**/*.{ts,tsx}'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              group: ['@/modules/*/*', '../*/!(index)', '../../*/*'],
              message:
                "Import another module only through its index.ts; everything else inside a module is private.",
            },
          ],
        },
      ],
    },
  },
  {
    // shared/ is the bottom layer and must not depend on feature modules.
    files: ['src/shared/**/*.{ts,tsx}'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            {
              group: ['@/modules/*', '**/modules/*'],
              message: 'shared/ must not import from modules/.',
            },
          ],
        },
      ],
    },
  },
);
