import { PHONE_COUNTRY_CODES } from './phoneCountryCodes.js'

export const DEFAULT_PHONE_COUNTRY_CODE = '+86'
export const PHONE_NUMBER_PATTERN = /^\+[1-9][0-9]{1,14}$/

const callingCodes = [...new Set(PHONE_COUNTRY_CODES.map(item => item.code))]
  .sort((left, right) => right.length - left.length)

const compact = value => String(value ?? '').replace(/[\s-]/g, '')

export function normalizePhoneNumber(value, countryCode) {
  const parts = arguments.length < 2 ? normalizePhoneParts(value) : normalizePhoneParts(value, countryCode)
  return parts.phoneNumber ? `${compact(parts.phoneCallingCode)}${parts.phoneNumber}` : ''
}

// A standalone phone input defaults to +86; explicit record fields retain missing codes.
export function normalizePhoneParts(value, phoneCallingCode) {
  const number = compact(value)
  if (arguments.length < 2 && number.startsWith('+')) {
    const parts = splitPhoneNumber(number)
    return { phoneCallingCode: parts.countryCode, phoneNumber: parts.nationalNumber }
  }
  const code = arguments.length < 2 ? DEFAULT_PHONE_COUNTRY_CODE
    : phoneCallingCode == null ? phoneCallingCode : compact(phoneCallingCode)
  return { phoneCallingCode: code, phoneNumber: number }
}

// Backend records store the full number; convert it once when loading a record.
export function normalizeStoredPhoneParts(value, phoneCallingCode) {
  const parts = normalizePhoneParts(value, phoneCallingCode)
  if (parts.phoneNumber && parts.phoneCallingCode) {
    parts.phoneNumber = parts.phoneNumber.slice(parts.phoneCallingCode.length)
  }
  return parts
}

export function splitPhoneNumber(value) {
  const number = compact(value)
  if (!number.startsWith('+')) {
    return { countryCode: DEFAULT_PHONE_COUNTRY_CODE, nationalNumber: number }
  }
  const countryCode = callingCodes.find(code => number.startsWith(code))
    || number.slice(0, Math.min(4, number.length))
  return { countryCode, nationalNumber: number.slice(countryCode.length) }
}

export function validatePhoneNumber(value, phoneCallingCode) {
  const parts = arguments.length < 2 ? normalizePhoneParts(value) : normalizePhoneParts(value, phoneCallingCode)
  return validPhoneCallingCode(parts.phoneCallingCode) && /^[0-9]+$/.test(parts.phoneNumber)
    && PHONE_NUMBER_PATTERN.test(normalizePhoneNumber(parts.phoneNumber, parts.phoneCallingCode))
}

export function validPhoneCallingCode(value) {
  return callingCodes.includes(compact(value))
}

export function normalizePhoneNumbers(values) {
  return Array.isArray(values) ? values.map(value => normalizePhoneNumber(value)) : []
}

export function getPhoneNumberMatchValues(value, phoneCallingCode) {
  const parts = arguments.length < 2 ? normalizePhoneParts(value) : normalizePhoneParts(value, phoneCallingCode)
  if (!parts.phoneNumber) return []
  return [...new Set([normalizePhoneNumber(parts.phoneNumber, parts.phoneCallingCode), parts.phoneNumber])]
}

export function containsPhoneNumber(text, phoneNumber, phoneCallingCode) {
  const values = arguments.length < 3 ? getPhoneNumberMatchValues(phoneNumber)
    : getPhoneNumberMatchValues(phoneNumber, phoneCallingCode)
  return values.some(value => String(text ?? '').includes(value))
}
