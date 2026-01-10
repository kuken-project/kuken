import "@/assets/styles/main.scss"
import { createApp } from "vue"
import App from "@/App.vue"
import { createVfm } from "vue-final-modal"
import router from "@/router"
import { createPinia } from "pinia"
import VueProgressiveImage from "vue-progressive-image"
import logService from "@/modules/platform/api/services/log.service"
import configService from "@/modules/platform/api/services/config.service"
import { createHead } from "@unhead/vue";

createApp(App)
    .use(createVfm())
    .use(createPinia())
    .use(VueProgressiveImage)
    .use(router)
    .use(createHead())
    .mount("#app")

logService.info(configService.toVersionInfoString())
