import { isNull } from "@/utils"

const PREFIX = "katan_"

class LocalStorageService {
    /**
	 * Returns the current value associated with the given key, or `null` if
	 * the given key does not exist.
	 * @param {string} key - The pair key.
	 */
    get<T>(key: string): T | null {
        const value = localStorage.getItem(PREFIX + key)
        if (isNull(value)) return null
        return JSON.parse(value) as T
    }

    /**
	 * Sets the value of the pair identified by key to value, creating a new
	 * key/value pair if none existed for key previously.
	 * @param {string} key - The pair key.
	 * @param {*} value - The pair value.
	 */
    set(key: string, value: unknown): void {
        localStorage.setItem(PREFIX + key, JSON.stringify(value))
    }

    /**
	 * Removes the key/value pair with the given key from the list associated
	 * with the object
	 * @param {string} key - The pair key.
	 */
    remove(key: string): void {
        localStorage.removeItem(PREFIX + key)
    }

    /**
	 * Returns `true` if there is a value associated with the given key or
	 * `false` otherwise.
	 * @param {string} key - The pair key.
	 */
    has(key: string): boolean {
        return !isNull(localStorage.getItem(PREFIX + key))
    }
}

export default new LocalStorageService()
