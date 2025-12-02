import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import { resolve, dirname } from 'node:path'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import VueI18nPlugin from '@intlify/unplugin-vue-i18n/vite'
import pkg from "./package.json"
import { splitVendorChunkPlugin } from 'vite'

process.env.VITE_APP_VERSION = pkg.version

export default defineConfig({
    plugins: [
        splitVendorChunkPlugin(),
        vue(),
        vueJsx(),
        VueI18nPlugin({
            include: resolve(dirname(fileURLToPath(import.meta.url)), './src/lang/**'),
        })
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url)),
            '@icons': fileURLToPath(new URL('./node_modules/vue-material-design-icons', import.meta.url)),
        }
    }
})
