import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import en from './locales/en.json'
import de from './locales/de.json'
import es from './locales/es.json'
import fa from './locales/fa.json'
import hi from './locales/hi.json'
import ja from './locales/ja.json'
import ko from './locales/ko.json'
import pt from './locales/pt.json'
import ru from './locales/ru.json'
import tr from './locales/tr.json'

void i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    de: { translation: de },
    es: { translation: es },
    fa: { translation: fa },
    hi: { translation: hi },
    ja: { translation: ja },
    ko: { translation: ko },
    pt: { translation: pt },
    ru: { translation: ru },
    tr: { translation: tr },
  },
  lng: 'en',
  fallbackLng: 'en',
  interpolation: {
    escapeValue: false,
  },
})

export default i18n
