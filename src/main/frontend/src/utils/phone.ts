/**
 * Formats a UK mobile number for display in national format: "07xxx xxxxxx".
 *
 * The 11 digits are grouped as five, a space, then six
 * (e.g. "07700900123" -> "07700 900123"). Input that isn't a well-formed
 * 11-digit UK mobile starting with "07" is returned unchanged, so unexpected
 * data still renders as-is rather than being hidden.
 */
export function formatUkMobile(value: string | null | undefined): string {
  if (!value) {
    return ''
  }
  const digits = value.replace(/\D/g, '')
  if (digits.length === 11 && digits.startsWith('07')) {
    return `${digits.slice(0, 5)} ${digits.slice(5)}`
  }
  return value
}
