import { describe, it, expect } from 'vitest'
import { formatUkMobile } from './phone'

describe('formatUkMobile', () => {
  it('formats an 11-digit UK mobile as "07xxx xxxxxx"', () => {
    expect(formatUkMobile('07700900123')).toBe('07700 900123')
  })

  it('strips existing separators before regrouping', () => {
    expect(formatUkMobile('07700 900123')).toBe('07700 900123')
    expect(formatUkMobile('07700-900-123')).toBe('07700 900123')
  })

  it('returns non-UK-mobile input unchanged', () => {
    expect(formatUkMobile('6085551023')).toBe('6085551023')
    expect(formatUkMobile('+441234567890')).toBe('+441234567890')
  })

  it('returns an empty string for missing values', () => {
    expect(formatUkMobile('')).toBe('')
    expect(formatUkMobile(null)).toBe('')
    expect(formatUkMobile(undefined)).toBe('')
  })
})
