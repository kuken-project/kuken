import { createI18n, type I18n } from "petite-vue-i18n"
import { nextTick } from "vue"

export const DEFAULT_LOCALE = "en"
export const SUPPORTED_LOCALES = ["en"]

export function setupI18n(options = { locale: DEFAULT_LOCALE }) {
  const i18n = createI18n(options)
  setI18nLanguage(i18n, options.locale)
  loadLocaleMessages(i18n, options.locale)
  return i18n
}

export function setI18nLanguage(i18n: I18n, locale: string) {
  i18n.global.locale.value = locale

  /**
   * NOTE:
   * If you need to specify the language setting for headers, such as the `fetch` API, set it here.
   * The following is an example for axios.
   *
   * axios.defaults.headers.common['Accept-Language'] = locale
   */
  document.querySelector("html")!.setAttribute("lang", locale)
}

export async function loadLocaleMessages(i18n: I18n, locale: string) {
  // load locale messages with dynamic import
  const messages = await import(
    /* webpackChunkName: "locale-[request]" */ `@/locales/${locale}.json`
  )

  // set locale and locale message
  i18n.global.setLocaleMessage(locale, messages.default)

  return nextTick()
}
