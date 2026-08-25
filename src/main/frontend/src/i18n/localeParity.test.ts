import { describe, it, expect } from 'vitest'
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

function leafKeys(obj: object, prefix = ''): string[] {
  return Object.entries(obj).flatMap(([key, value]) => {
    const path = prefix ? `${prefix}.${key}` : key
    return typeof value === 'object' && value !== null ? leafKeys(value, path) : [path]
  })
}

const locales: Record<string, object> = { de, es, fa, hi, ja, ko, pt, ru, tr }
const baseKeys = leafKeys(en).sort()

describe('i18n locale parity', () => {
  it.each(Object.entries(locales))('%s has the same keys as en', (_name, translation) => {
    expect(leafKeys(translation).sort()).toEqual(baseKeys)
  })
})
