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
