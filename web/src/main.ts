import App from "@/App.vue"
import "@/assets/styles/main.scss"
import configService from "@/modules/platform/api/services/config.service"
import logService from "@/modules/platform/api/services/log.service"
import router from "@/router"
import hljsVuePlugin from "@highlightjs/vue-plugin"
import ContextMenu from "@imengyu/vue3-context-menu"
import "@imengyu/vue3-context-menu/lib/vue3-context-menu.css"
import { createHead } from "@unhead/vue"
import dayjs from "dayjs"
import localizedFormat from "dayjs/plugin/localizedFormat"
import hljs from "highlight.js/lib/core"
import json from "highlight.js/lib/languages/json"
import "highlight.js/styles/atom-one-light.css"
import { createPinia } from "pinia"
import { createApp } from "vue"
import { createVfm } from "vue-final-modal"
import VueProgressiveImage from "vue-progressive-image"
import VueVirtualScroller from "vue-virtual-scroller"
import "vue-virtual-scroller/dist/vue-virtual-scroller.css"

dayjs.extend(localizedFormat)

hljs.registerLanguage("json", json)

createApp(App)
  .use(createVfm())
  .use(createPinia())
  .use(VueProgressiveImage)
  .use(VueVirtualScroller)
  .use(router)
  .use(createHead())
  .use(hljsVuePlugin)
  .use(ContextMenu)
  .mount("#app")

logService.info(configService.toVersionInfoString())
