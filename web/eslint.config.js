import eslintConfigPrettier from "eslint-config-prettier"
import eslintPluginVue from "eslint-plugin-vue"
import globals from "globals"
import typescriptEslint, * as eslint from "typescript-eslint"

export default typescriptEslint.config([
  { ignores: ["*.d.ts"] },
  {
    files: ["**/*.{ts,vue}"],
    extends: [
      eslint.configs.recommended,
      ...typescriptEslint.configs.recommended,
      ...eslintPluginVue.configs["flat/recommended"]
    ],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
      globals: globals.browser,
      parserOptions: {
        parser: typescriptEslint.parser
      }
    }
  },
  eslintConfigPrettier
])
