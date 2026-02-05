export function undefinedOr<T, R = unknown>(
  value: T | undefined,
  fallback: (arg0: T) => unknown
): R | undefined {
  if (!isUndefined(value)) return fallback(value) as R

  return undefined
}

/**
 * Returns `true` if the {@param value} is `undefined` or `false` otherwise.
 * @param {*} value - the value.
 */
export function isUndefined(value: unknown): value is undefined {
  return typeof value === "undefined"
}

/**
 * Returns `true` if the {@param value} is `null` or `false` otherwise.
 * @param {*} value - the value.
 */
export function isNull(value: unknown): value is null {
  return value === null
}

/**
 * Returns `true` if the {@param value} is a number or `false` otherwise.
 * @param {*} value - the value.
 */
export function isNumber(value: unknown): value is number {
  return typeof value === "number"
}

/**
 * Returns `true` if the {@param value} is a object or `false` otherwise.
 * @param {*} value - the value.
 */
export function isObject(value: unknown): value is object {
  return typeof value === "object"
}

/**
 * Returns `true` if the {@param value} is a function or `false` otherwise.
 * @param {*} value - the value.
 */
export function isFunction(value: unknown): boolean {
  return typeof value === "function"
}

/**
 * Returns `null` if the {@param value} is `undefined` or the value itself otherwise.
 * @param {T | undefined} value - the value.
 */
export function undefinedToNull<T>(value: T | undefined): T | null {
  return isUndefined(value) ? null : value
}

/**
 * Returns `undefined` if the {@param value} is `null` or the value itself otherwise.
 * @param {T | null} value - the value.
 */
export function nullToUndefined<T>(value: T | null): T | undefined {
  return isNull(value) ? undefined : value
}

export function assertIsDefined<T>(val: T | undefined | null): asserts val is T {
  if (val === undefined || val === null) {
    throw new Error("Value was not defined")
  }
}

// See https://stackoverflow.com/a/52171480
export function cybrh3(value: string, seed: number = 0) {
  let h1 = 0xdeadbeef ^ seed,
    h2 = 0x41c6ce57 ^ seed
  for (let i = 0, ch; i < value.length; i++) {
    ch = value.charCodeAt(i)
    h1 = Math.imul(h1 ^ ch, 2654435761)
    h2 = Math.imul(h2 ^ ch, 1597334677)
  }
  h1 = Math.imul(h1 ^ (h1 >>> 16), 2246822507)
  h1 ^= Math.imul(h2 ^ (h2 >>> 13), 3266489909)
  h2 = Math.imul(h2 ^ (h2 >>> 16), 2246822507)
  h2 ^= Math.imul(h1 ^ (h1 >>> 13), 3266489909)

  return 4294967296 * (2097151 & h2) + (h1 >>> 0)
}

export function substringBeforeLast(input: string, search: string): string {
  return input.substring(0, input.lastIndexOf(search))
}

export function systemPathSepatator(path: string) {
  return path.indexOf("/") === -1
    ? path.indexOf("\\") === -1
      ? undefined
      : "\\" /* Windows */
    : path.indexOf("/") === -1
      ? undefined
      : "/" /* UNIX */
}
