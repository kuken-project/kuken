/* eslint-env node */
require('@rushstack/eslint-patch/modern-module-resolution')

module.exports = {
    root: true,
    'extends': [
        'plugin:vue/vue3-recommended',
        'eslint:recommended',
        '@vue/eslint-config-typescript',
    ],
    parserOptions: {
        ecmaVersion: 'latest'
    },
    rules: {
        'vue/attributes-order': 'error',
        'vue/no-unused-vars': 'error',
        'indent': ['error', 4],
        'no-mixed-spaces-and-tabs': 'off',
        'vue/multi-word-component-names': 'off'
    }
}
