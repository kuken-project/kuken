import type { ConsolaInstance, LogObject } from "consola"
import { createConsola } from "consola"

export type Logger = ConsolaInstance
export type LogMessage = string | LogObject

class LogService {
    private readonly logger!: Logger

    constructor() {
        this.logger = createConsola({
            level: import.meta.env.NODE_ENV !== "production" ? 5 /* trace */ : 3 /* info */
        })
    }

    create(tag: string): Logger {
        return this.logger.withTag(tag)
    }

    log(message: LogMessage, ...args: unknown[]): void {
        this.logger.log(message, ...args)
    }

    debug(message: LogMessage, ...args: unknown[]): void {
        this.logger.debug(message, ...args)
    }

    info(message: LogMessage, ...args: unknown[]): void {
        this.logger.info(message, ...args)
    }

    warn(message: LogMessage, ...args: unknown[]): void {
        this.logger.warn(message, ...args)
    }

    error(message: LogMessage | unknown, ...args: unknown[]): void {
        this.logger.error(message, ...args)
    }
}

export default new LogService()
