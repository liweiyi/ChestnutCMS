const MAX_INTEGER = 2147483647;

/** Validate optional windows (seconds) -> maximum attempts; empty means no rate limits. */
export function isValidRateLimits(value) {
  if (value == null) return true;
  if (typeof value !== 'object' || Array.isArray(value)) return false;
  const entries = Object.entries(value);
  const windows = new Set();
  for (const [window, count] of entries) {
    const numbers = [window, count].map(item => {
      const text = String(item ?? '').trim();
      return /^\+?\d+$/.test(text) ? Number(text) : NaN;
    });
    if (numbers.some(item => !Number.isInteger(item) || item <= 0 || item > MAX_INTEGER)) return false;
    if (windows.has(numbers[0])) return false;
    windows.add(numbers[0]);
  }
  return true;
}
