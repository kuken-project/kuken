import "@/assets/styles/main.scss"
import "vue-progressive-image/dist/style.css"
import { createApp } from "vue"
import App from "@/App.vue"
import { createVfm } from "vue-final-modal"
import ECharts from "vue-echarts"
import router from "@/router"
import { createPinia } from "pinia"
import VueProgressiveImage from "vue-progressive-image"
import logService from "@/modules/platform/api/services/log.service"
import configService from "@/modules/platform/api/services/config.service"

createApp(App)
    .component("v-chart", ECharts)
    .use(createVfm())
    .use(createPinia())
    .use(VueProgressiveImage)
    .use(router)
    .mount("#app")

logService.info(configService.toVersionInfoString())